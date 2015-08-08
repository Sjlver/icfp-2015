import spray.json._
import scala.collection.mutable.ArrayBuffer

// Contains the game board, does moves, undoes moves, checks validity, computes
// scores, ...
class Board {
  def fromJson(jsonString: String) {
    val jsonAst = jsonString.parseJson
    jsonAst.asJsObject.getFields("width", "height") match {
      case Seq(JsNumber(w), JsNumber(h)) =>
        width = w.toInt
        height = h.toInt
    }
    
    grid = Array.fill[Boolean](width, height)(false)
    jsonAst.asJsObject.getFields("filled") match {
      case Seq(JsArray(cells)) =>
        cells.foreach { cell =>
          cell.asJsObject.getFields("x", "y") match {
            case Seq(JsNumber(x), JsNumber(y)) => grid(x.toInt)(y.toInt) = true
          }
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

  // Width and height of the board
  var height = -1
  var width = -1
  
  // The grid ((0, 3) is column zero, row three.
  var grid = Array.ofDim[Boolean](0, 0)
}