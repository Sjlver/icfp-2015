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
    jsonObject.getFields("width", "height") match {
      case Seq(JsNumber(w), JsNumber(h)) =>
        width = w.toInt
        height = h.toInt
    }
    
    grid = Array.fill[Boolean](width, height)(false)
    jsonObject.getFields("filled") match {
      case Seq(JsArray(cells)) => {
        cells.foreach { cellObject =>
          val cell = HexCell.fromJsonObject(cellObject.asJsObject)
          grid(cell.x)(cell.y) = true
        }
      }
    }
    
    jsonObject.getFields("sourceSeeds") match {
      case Seq(JsArray(ss)) =>
        sourceSeeds = ss.map(jsValue => (jsValue: @unchecked) match {case JsNumber(s) => s.toInt}).toArray
    }
    random = new DavarRandom(sourceSeeds(sourceSeedIndex))
    
    jsonObject.getFields("units") match {
      case Seq(JsArray(units)) => {
        blocks = units.map(unit => BlockTemplate.fromJsonObject(unit.asJsObject)).toArray
      }
    }

    spawnNextBlock()
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
  
  def doMove(move: Moves.Move) {
    val targetBlock = activeBlock.moved(move)

    // Check for moves that would lead to a repeated situation
    if (pastBlockStates.contains(targetBlock)) {
      throw new InvalidMoveException(moveToString(move) + " leads to repeated position")
    }
    
    // Check for moves the exit the grid
    if (exitsGrid(targetBlock) || collidesWithFullCell(targetBlock)) {
      lockBlock()
      spawnNextBlock()
      return
    }

    // All checks passed, move the target
    activeBlock = targetBlock
    pastBlockStates += activeBlock
  }

  private def spawnNextBlock() {
    numBlocksPlayed += 1
    blockIndex = random.next
    activeBlock = Block.spawn(blocks(blockIndex % blocks.size), width)
    pastBlockStates.clear()
    pastBlockStates += activeBlock
  }
  
  private def lockBlock() {
    val affectedLines = scala.collection.mutable.Set.empty[Int]
    activeBlock.transformedCells.foreach { cell =>
      affectedLines += cell.y
      grid(cell.x)(cell.y) = true
    }
    clearFilledLines(affectedLines)
  }
  
  def clearFilledLines(affectedLines: Iterable[Int]) {
    // Find lines that are really full.
    val linesToClear = affectedLines.filter( y => 0.to(width - 1).forall(x => grid(x)(y)) ).toSet
    if (linesToClear.isEmpty) return
    
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
      0.to(width - 1).map { x => if (grid(x)(y)) "o" else "." }.mkString(start, " ", "\n")  
    }
    lines.mkString
  }
  
  // Width and height of the board
  var height = -1
  var width = -1
  
  // The grid ((0, 3) is column zero, row three).
  var grid = Array.ofDim[Boolean](0, 0)
  
  // The current game we're playing
  var sourceSeedIndex = 0
  
  // The list of source seeds
  var sourceSeeds = Array.emptyIntArray
  
  // How many units have already completed their move
  var numBlocksPlayed = -1
  
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
}