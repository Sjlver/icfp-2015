// An artificial intelligence built on Monte Carlo Tree Sampling
// (well, to be honest, it's just random moves for now)

import scala.collection.mutable.ArrayBuffer
import scala.util.Random

class SamplingAI(board: Board) {
  // Runs the AI on a single game, and produces a sequence of moves.
  def run(): ArrayBuffer[Moves.Move] = {
    var result = ArrayBuffer.empty[Moves.Move]
    while (board.isActive) {
      val move = randomMove()
      try {
        board.doMove(move)
        result += move
      } catch {
        case _: board.InvalidMoveException =>
          // Ignore these, just try another move
          Unit
      }
    }
    result
  }
  
  def randomMove(): Moves.Move = {
    val allMoves = Array(Moves.E, Moves.W, Moves.SE, Moves.SW, Moves.CW, Moves.CCW)
    allMoves(Random.nextInt(allMoves.size))
  }
}