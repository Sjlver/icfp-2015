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
        cells.foreach { cell =>
          cell.asJsObject.getFields("x", "y") match {
            case Seq(JsNumber(x), JsNumber(y)) => grid(x.toInt)(y.toInt) = true
          }
        }
      }
    }
    
    jsonObject.getFields("sourceSeeds") match {
      case Seq(JsArray(ss)) =>
        sourceSeeds = ss.map(jsValue => (jsValue: @unchecked) match {case JsNumber(s) => s.toInt}).toArray
    }
    random = new DavarRandom(sourceSeeds(sourceSeedIndex))
    blockIndex = random.next
    
    jsonObject.getFields("units") match {
      case Seq(JsArray(units)) => {
        blocks = units.map(unitObject => (unitObject: @unchecked) match { case unit: JsObject =>
          BlockTemplate.fromJsonObject(unit)
        }).toArray
      }
     activeBlock = Block.spawn(blocks(blockIndex), width)
     pastBlockStates += activeBlock
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
  
  def doMove(move: Moves.Move) {
    val targetBlock = activeBlock.moved(move)

    // Check for moves that would lead to a repeated situation
    if (pastBlockStates.contains(targetBlock)) {
      throw new InvalidMoveException(moveToString(move) + " leads to repeated position")
    }
    
    targetBlock.transformedCells.foreach { cell =>
      if (cell.x < 0 || cell.x >= width || cell.y < 0 || cell.y >= height) {
      throw new InvalidMoveException(moveToString(move) + " exits the board")
      }
    }
    // TODO check for locking

    activeBlock = targetBlock
    pastBlockStates += activeBlock
  }

  // Converts a move to a detailed string, for debugging mostly
  def moveToString(move: Moves.Move): String = {
    "Move #" + pastBlockStates.size + " of unit #" + numUnitsPlayed + "(" + move + ")"
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
  var numUnitsPlayed = 0
  
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