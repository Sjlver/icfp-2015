// A class to run an AI on all games in a board, and encode the output to JSON.

import spray.json._
import scala.collection.mutable.ArrayBuffer

class AIRunner(
    board: Board,
    aiFactory: Board => SamplingAI,
    encoderFactory: Iterable[Moves.Move] => PowerPhraseEncoder,
    gameToJsonPrinter: GameToJsonPrinter,
    tag: String) {

  def run(): JsArray = {
    val solutions = ArrayBuffer.empty[JsObject]
    while (board.startNewGame()) {
      val reproBoard = board.clone()
      val seed = board.currentSourceSeed
      val ai = aiFactory(board)
      val moves = ai.run()

      System.err.println("Game finished with score: " + board.score)
      sumScores += board.score

      gameToJsonPrinter.printMoves(reproBoard, moves)
      val encoder = encoderFactory(moves)
      solutions += JsObject(
        "problemId" -> JsNumber(board.problemId),
        "seed" -> JsNumber(seed),
        "tag" -> JsString(tag),
        "solution" -> JsString(encoder.encode())
      )
    }
    JsArray(solutions: _*)
  }

  // The sum of the scores of all games
  var sumScores = 0
}
