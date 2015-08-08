import spray.json._

// Contains the game board, does moves, undoes moves, checks validity, computes
// scores, ...
class Board {
  def loadFromJson(jsonString: String) {
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

  var height = -1
  var width = -1
  
  var grid = Array.ofDim[Boolean](0, 0)
}