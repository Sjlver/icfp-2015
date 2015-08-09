// A class to run an AI on all games in a board, and encode the output to JSON.

import spray.json._
import scala.collection.mutable.ArrayBuffer

class AIRunner(
    board: Board,
    aiFactory: Board => SamplingAI,
    encoderFactory: Iterable[Moves.Move] => PowerPhraseEncoder,
    intermediateStateTracer: (Board, ArrayBuffer[Moves.Move]) => Unit,
    tag: String) {

  def run(): JsArray = {
    val solutions = ArrayBuffer.empty[JsObject]
    while (board.startNewGame()) {
      val reproBoard = board.clone()
      val seed = board.currentSourceSeed
      val ai = aiFactory(board)
      val commands = ai.run()
      intermediateStateTracer(reproBoard, commands)
      val encoder = encoderFactory(commands)
      solutions += JsObject(
        "problemId" -> JsNumber(board.problemId),
        "seed" -> JsNumber(seed),
        "tag" -> JsString(tag),
        "solution" -> JsString(encoder.encode())
      )
    }
    JsArray(solutions: _*)
  }
}