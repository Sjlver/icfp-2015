import spray.json._
import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.HashSet

// Contains the game board, does moves, undoes moves, checks validity, computes
// scores, ...

object Board {
  // Create a board from a JSON string
  def fromJson(jsonString: String): Board = {
    // We ignore potential invalid JSON, hence the @unchecked.
    val jsonObject = jsonString.parseJson.asJsObject

    val problemId = (jsonObject.fields("id"): @unchecked) match { case JsNumber(id) => id.toInt }
    val width = (jsonObject.fields("width"): @unchecked) match { case JsNumber(w) => w.toInt }
    val height = (jsonObject.fields("height"): @unchecked) match { case JsNumber(h) => h.toInt }
    val sourceLength = (jsonObject.fields("sourceLength"): @unchecked) match { case JsNumber(sl) => sl.toInt }

    val initialGrid = Array.fill[Boolean](width, height)(false)
    (jsonObject.fields("filled"): @unchecked) match {
      case JsArray(cells) => {
        cells.foreach { cellObject =>
          val cell = HexCell.fromJsonObject(cellObject.asJsObject)
          initialGrid(cell.x)(cell.y) = true
        }
      }
    }

    val sourceSeeds = (jsonObject.fields("sourceSeeds"): @unchecked) match {
      case JsArray(ss) => ss.map {
        jsValue => (jsValue: @unchecked) match {case JsNumber(s) => s.toInt}
      }.toArray
    }

    val blocks = (jsonObject.fields("units"): @unchecked) match {
      case JsArray(units) => units.map { unit =>
        BlockTemplate.fromJsonObject(unit.asJsObject)
      }.toArray
    }

    new Board(problemId, width, height, sourceLength, initialGrid, sourceSeeds, blocks)
  }
}

class Board(
      // The problem id from the input file
      _problemId: Int,

      // Width and height of the board
      _width: Int,
      _height: Int,

      // The number of blocks in the source
      sourceLength: Int,

      // The grid at the start of a game
      initialGrid: Array[Array[Boolean]],

      // The list of source seeds
      sourceSeeds: Array[Int],

      // The list of blocks
      blocks: Array[BlockTemplate]
    ) {

  // Thrown when a move would be invalid (i.e., lead to a score of zero according to the specification)
  class InvalidMoveException(message: String) extends Exception

  def toJsonObject: JsObject = {
    val filledCells = ArrayBuffer.empty[(Int, Int)]
    0.to(width - 1).foreach { x =>
      0.to(height - 1).foreach { y =>
        if (grid(x)(y)) {
          filledCells += ((x, y))
        }
      }
    }

    JsObject(
      "width" -> JsNumber(width),
      "height" -> JsNumber(height),
      "filled" -> JsArray(
    		filledCells.map { case (x, y) =>
    		  JsObject("x" -> JsNumber(x), "y" -> JsNumber(y))
    		}: _*),
      "activeBlock" -> JsObject(
        "members" -> JsArray(activeBlock.transformedCells.map {
          cell => JsObject("x" -> JsNumber(cell.x), "y" -> JsNumber(cell.y))
        }: _*),
        "pivot" -> JsObject("x" -> JsNumber(activeBlock.pivot.x), "y" -> JsNumber(activeBlock.pivot.y))
      )
    )
  }

  // Initializes a new game. Returns true on success, false when all source seeds have been exhausted.
  def startNewGame(): Boolean = {
    if (isActive) {
      throw new AssertionError("startNewGame must not be called on an active game.")
    }

    sourceSeedIndex += 1
    if (sourceSeedIndex >= sourceSeeds.size) {
      return false
    }

    0.to(width - 1).foreach { x =>
      Array.copy(initialGrid(x), 0, grid(x), 0, initialGrid(x).size)
    }
    random.seed = currentSourceSeed
    numBlocksPlayed = -1
    blockIndex = -1
    isActive = true

    spawnNextBlock()
  }

  // Perform a move. Returns true if the game is active after the move, and false if the game has ended.
  def doMove(move: Moves.Move): Boolean = {
    if (!isActive) {
      throw new AssertionError("doMove must not be called on an inactive game.")
    }

    val targetBlock = activeBlock.moved(move)

    // Check for moves that would lead to a repeated situation
    if (pastBlockStates.contains(targetBlock)) {
      throw new InvalidMoveException(moveToString(move) + " leads to repeated position")
    }

    // Check for moves the exit the grid
    if (exitsGrid(targetBlock) || collidesWithFullCell(targetBlock)) {
      lockBlock()
      return spawnNextBlock()
    }

    // All checks passed, move the target
    activeBlock = targetBlock
    pastBlockStates += activeBlock
    true
  }

  override def toString(): String = {
    "Board(sourceSeedIndex=" + sourceSeedIndex +
      ", numBlocksPlayed=" + numBlocksPlayed +
      ", score=" + score +
      ", lsOld=" + lsOld +
      ", isActive=" + isActive + ")\n" +
      gridToString()
  }

  // Creates a deep copy of this game board.
  override def clone(): Board = {
    val result = new Board(problemId, width, height, sourceLength, initialGrid, sourceSeeds, blocks)

    0.to(width - 1).foreach { x =>
      Array.copy(grid(x), 0, result.grid(x), 0, grid(x).size)
    }
    result.sourceSeedIndex = sourceSeedIndex
    result.numBlocksPlayed = numBlocksPlayed
    result.blockIndex = blockIndex
    result.random.seed = random.seed
    result.activeBlock = activeBlock
    result.pastBlockStates ++= pastBlockStates
    result.score = score
    result.lsOld = lsOld
    result.isActive = isActive

    result
  }

  // Boards are equal when they are roughly in the same state.
  // We only care about the grid and the position of the current block here.
  // We ignore things like the random seed, the pastBlockStates, ...
  override def equals(other: Any): Boolean = {
    if (other == null || !other.isInstanceOf[Board]) return false
    val otherBoard = other.asInstanceOf[Board]

    // Fast path check
    if (hashCode() != otherBoard.hashCode()) return false

    // Full comparison. Note that even here, we skip many fields
    if (sourceSeedIndex != otherBoard.sourceSeedIndex) return false
    if (numBlocksPlayed != otherBoard.numBlocksPlayed) return false
    if (activeBlock != otherBoard.activeBlock) return false

    if (width != otherBoard.width || height != otherBoard.height) return false
    0.to(width - 1).foreach { x =>
      0.to(height - 1).foreach { y =>
        if (grid(x)(y) != otherBoard.grid(x)(y)) return false
      }
    }

    true
  }

  // Override hashCode along with equals.
  // TODO: keep track of this as we are doing moves, to improve performance
  override def hashCode(): Int = {
    var result = 41
    result = 41 * (result + sourceSeedIndex)
    result = 41 * (result + numBlocksPlayed)
    result = 41 * (result + activeBlock.hashCode())
    0.to(width - 1).foreach { x =>
      0.to(height - 1).foreach { y =>
        result = 41 * (result + (if (grid(x)(y)) 1 else 0))
      }
    }

    result
  }

  def problemId = _problemId
  def width = _width
  def height = _height

  def currentSourceSeed = sourceSeeds(sourceSeedIndex)

  private def spawnNextBlock(): Boolean = {
    numBlocksPlayed += 1
    if (numBlocksPlayed == sourceLength) {
      // Game ended (all blocks played).
      isActive = false
      return false
    }

    blockIndex = random.next
    val spawnedBlock = Block.spawn(blocks(blockIndex % blocks.size), width)
    if (collidesWithFullCell(spawnedBlock)) {
      // Game ended (grid is full).
      isActive = false
      return false
    }

    activeBlock = spawnedBlock
    pastBlockStates.clear()
    pastBlockStates += activeBlock
    true
  }

  private def lockBlock() {
    val affectedLines = scala.collection.mutable.Set.empty[Int]
    activeBlock.transformedCells.foreach { cell =>
      affectedLines += cell.y
      grid(cell.x)(cell.y) = true
    }
    val numLinesCleared = clearFilledLines(affectedLines)
    updateScore(numLinesCleared)
  }

  // Clears all full lines. Returns the number of lines cleared
  private def clearFilledLines(affectedLines: Iterable[Int]): Int = {
    // Find lines that are really full.
    val linesToClear = affectedLines.filter( y => 0.to(width - 1).forall(x => grid(x)(y)) ).toSet
    if (linesToClear.isEmpty) return 0

    // Clear these lines.
    val lowestLineToClear = linesToClear.max
    var numLinesCleared = 0
    lowestLineToClear.to(0).by(-1).foreach { y =>
      if (linesToClear.contains(y)) {
        numLinesCleared += 1
      }
      val sourceY = y - numLinesCleared
      if (sourceY >= 0) {
        // Copy from sourceY
        0.to(width - 1).foreach { x => grid(x)(y) = grid(x)(sourceY) }
      } else {
        // Lines at the top become completely empty
        0.to(width - 1).foreach { x => grid(x)(y) = false }
      }
    }
    return numLinesCleared
  }

  // Updates the score after locking the current block and clearing `ls` lines
  private def updateScore(ls: Int) {
    val points = activeBlock.template.members.size + 100 * (1 + ls) * ls / 2
    val lineBonus = if (lsOld > 1) (lsOld - 1) * points / 20 else 0
    lsOld = ls
    score += points + lineBonus
  }

  // Converts a move to a detailed string, for debugging mostly.
  private def moveToString(move: Moves.Move): String = {
    "Move #" + pastBlockStates.size + " of unit #" + numBlocksPlayed + ": " + move
  }

  // Checks whether any part of the block is outside the grid.
  private def exitsGrid(block: Block): Boolean = {
    return block.transformedCells.exists { cell =>
      cell.x < 0 || cell.x >= width || cell.y < 0 || cell.y >= height
    }
  }

  // Checks whether any part of the block collides with a full cell.
  private def collidesWithFullCell(block: Block): Boolean = {
    return block.transformedCells.exists { cell => grid(cell.x)(cell.y) }
  }

  // Converts a grid to string, for debugging.
  private def gridToString(): String = {
    val lines = 0.to(height - 1).map { y =>
      val start = if (y % 2 == 0) "" else " "
      0.to(width - 1).map { x =>
        if (activeBlock.transformedCells.exists { cell => cell.x == x && cell.y == y }) {
          "0"
        } else if (activeBlock.pivot.x == x && activeBlock.pivot.y == y) {
          "+"
        } else if (grid(x)(y)) {
         "o"
        } else {
          "."
        }
      }.mkString(start, " ", "\n")
    }
    lines.mkString
  }

  // The grid ((0, 3) is column zero, row three).
  val grid = Array.ofDim[Boolean](width, height)

  // The current game we're playing
  var sourceSeedIndex = -1

  // How many units have already completed their move
  var numBlocksPlayed = -1

  // The index of the current unit into the blocks
  var blockIndex = -1

  // This board's random number generator
  val random: DavarRandom = new DavarRandom(0)

  // The block that is currently active. This is the block that is moved around.
  // It will become part of the grid once it is locked.
  var activeBlock: Block = null

  // This buffer stores the list of past positions/rotations of the active block.
  // Used to detect invalid moves
  val pastBlockStates = HashSet.empty[Block]

  // The score of the current game
  var score = 0

  // The number of lines cleared with the previous block (ls_old from the spec)
  var lsOld = 0

  // Whether a game is in progress or not
  var isActive = false
}
