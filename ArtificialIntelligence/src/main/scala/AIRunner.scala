// A class to run an AI on all games in a board, and encode the output to JSON.

import spray.json._
import scala.collection.mutable.ArrayBuffer

class AIRunner(
    board: Board,
    aiFactory: (Board, Long) => SamplingAI,
    encoderFactory: Iterable[Moves.Move] => PowerPhraseEncoder,
    gameToJsonPrinter: GameToJsonPrinter,
    tag: String,
    timeLimitSeconds: Int) {

  def run(): JsArray = {
    val solutions = ArrayBuffer.empty[JsObject]
    while (board.startNewGame()) {
      // Compute the time when this game must be done.
      val currentMillis = System.currentTimeMillis()
      val currentEndMillis = currentMillis + (endMillis - currentMillis) / board.numGamesRemaining

      val reproBoard = board.clone()
      val seed = board.currentSourceSeed
      val ai = aiFactory(board, currentEndMillis)
      val moves = ai.run()

      Options.log("Game finished with score: " + board.score)
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

  // The time when this AIRunner must be done (subtract 1% for safety)
  val endMillis = System.currentTimeMillis() + 990 * timeLimitSeconds

  // The sum of the scores of all games
  var sumScores = 0
}
