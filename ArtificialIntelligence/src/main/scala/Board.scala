import spray.json._

// Contains the game board, does moves, undoes moves, checks validity, computes
// scores, ...
class Board {
  def loadFromJson(jsonString: String) {
    val jsonAst = jsonString.parseJson
    print(jsonAst.prettyPrint)
  }

  def height = 9
  def width = 9
}