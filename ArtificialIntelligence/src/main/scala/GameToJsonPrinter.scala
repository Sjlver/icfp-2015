// Prints the game state to JSON
import java.io.BufferedWriter
import java.io.FileWriter
import java.io.File
import spray.json._

class GameToJsonPrinter(filename: String) {

  // Call the given function with a writer
  def withPrintWriter(f: BufferedWriter => Unit) {
    filename match {
      case null | "" => Unit
      case _ =>
        val writer = new BufferedWriter(new FileWriter(new File(filename)))
        try { f(writer) } finally { writer.close() }
    }
  }

  def printMoves(board: Board, moves: Seq[Moves.Move]) {
    withPrintWriter { writer =>
      writer.write("var configurations = " + movesToJson(board, moves))
    }
  }

  def movesToJson(board: Board, moves: Seq[Moves.Move]): String = {
    val resultObjects = scala.collection.mutable.ArrayBuffer.empty[JsObject]
    resultObjects += board.toJsonObject
    moves.foreach { move =>
      board.doMove(move)
      resultObjects += board.toJsonObject
    }

    JsArray(resultObjects: _*).prettyPrint
  }
}
