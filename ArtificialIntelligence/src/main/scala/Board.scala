import spray.json._
import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.HashSet

// Contains the game board, does moves, undoes moves, checks validity, computes
// scores, ...
class Board {
  // Thrown when a move would be invalid (i.e., lead to a score of zero according to the specification)
  class InvalidMoveException(message: String) extends Exception
  
  def fromJson(jsonString: String) {
    // We ignore potential invalid JSON, hence the @unchecked.

    val jsonObject = jsonString.parseJson.asJsObject
    jsonObject.getFields("width", "height", "sourceLength") match {
      case Seq(JsNumber(w), JsNumber(h), JsNumber(sl)) =>
        width = w.toInt
        height = h.toInt
        sourceLength = sl.toInt
    }
    
    initialGrid = Array.fill[Boolean](width, height)(false)
    jsonObject.getFields("filled") match {
      case Seq(JsArray(cells)) => {
        cells.foreach { cellObject =>
          val cell = HexCell.fromJsonObject(cellObject.asJsObject)
          initialGrid(cell.x)(cell.y) = true
        }
      }
    }
    
    jsonObject.getFields("sourceSeeds") match {
      case Seq(JsArray(ss)) =>
        sourceSeeds = ss.map(jsValue => (jsValue: @unchecked) match {case JsNumber(s) => s.toInt}).toArray
    }
    
    jsonObject.getFields("units") match {
      case Seq(JsArray(units)) => {
        blocks = units.map(unit => BlockTemplate.fromJsonObject(unit.asJsObject)).toArray
      }
    }
  }
  
  def toJson: String = {
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
    		}: _*)
    ).compactPrint
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

    grid = initialGrid.clone()
    random = new DavarRandom(sourceSeeds(sourceSeedIndex))
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
  def clearFilledLines(affectedLines: Iterable[Int]): Int = {
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
  
  // Width and height of the board
  var height = -1
  var width = -1
  
  // The grid ((0, 3) is column zero, row three).
  var grid = Array.ofDim[Boolean](0, 0)
  
  // The grid at the start of a game
  var initialGrid = Array.ofDim[Boolean](0, 0)
  
  // The current game we're playing
  var sourceSeedIndex = -1
  
  // The list of source seeds
  var sourceSeeds = Array.emptyIntArray
  
  // How many units have already completed their move
  var numBlocksPlayed = -1
  
  // The number of blocks in the source
  var sourceLength = -1
  
  // The index of the current unit into the blocks
  var blockIndex = -1

  // The list of blocks
  var blocks = Array.empty[BlockTemplate]

  // This board's random number generator
  var random: DavarRandom = null
  
  // The block that is currently active. This is the block that is moved around.
  // It will become part of the grid once it is locked.
  var activeBlock: Block = null

  // This buffer stores the list of past positions/rotations of the active block.
  // Used to detect invalid moves
  var pastBlockStates = HashSet.empty[Block]

  // The score of the current game
  var score = 0
  
  // The number of lines cleared with the previous block (ls_old from the spec)
  var lsOld = 0
  
  // Whether a game is in progress or not
  var isActive = false
}