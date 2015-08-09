// An artificial intelligence built on Monte Carlo Tree Search

import scala.collection.mutable.ArrayBuffer
import scala.util.Random

object SamplingAI {
  // The number of tree nodes to explore per move
  val NUM_ALTERNATIVES = 200

  // The number of duplicates before giving up searching for new alternatives
  val DUPLICATES_BEFORE_GIVING_UP = 500

  // The number of random playouts per alternative
  val NUM_PLAYOUTS = 100
}

class SamplingAI(board: Board) {
  // Runs the AI on a single game, and produces a sequence of moves.
  def run(): ArrayBuffer[Moves.Move] = {
    System.err.println("SamplingAI running on board:\n" + board)

    var result = ArrayBuffer.empty[Moves.Move]
    while (board.isActive) {

      // Generate some tree nodes to explore
      // TODO: currently, the alternatives are just the current board with
      //       one more block locked. There are certainly better options.
      val alternatives = scala.collection.mutable.HashSet.empty[TreeNode]
      var numDuplicates = 0
      while (alternatives.size < SamplingAI.NUM_ALTERNATIVES && numDuplicates < SamplingAI.DUPLICATES_BEFORE_GIVING_UP) {
        val node = new TreeNode(board.clone())
        node.playUntilLocked()
        if (alternatives.contains(node)) {
          numDuplicates += 1
        } else {
          alternatives += node
          numDuplicates = 0
        }
      }
      System.err.println("Exploring " + alternatives.size + " alternatives...")

      // Score those alternatives using random playouts
      alternatives.foreach { node =>
        0.to(SamplingAI.NUM_PLAYOUTS - 1).foreach { i =>
          node.addPlayout()
        }
      }

      // Choose the best alternative
      val sortedAlternatives = alternatives.toArray.sortBy { node => -node.avgScore }
      val bestAlternative = sortedAlternatives(0)
      bestAlternative.movesToGetHere.foreach { move =>
        board.doMove(move)
        result += move
      }
      System.err.println("Chose move with avgScore " + bestAlternative.avgScore + ":\n" + board)
    }
    result
  }
}

// A node in the search tree
class TreeNode(_board: Board) {

  // Moves this TreeNode until the piece locks.
  def playUntilLocked() = {
    if (!board.isActive) {
      throw new AssertionError("playUntilLocked must be called on an active board")
    }
    val oldNumBlocksPlayed = board.numBlocksPlayed
    while (board.numBlocksPlayed == oldNumBlocksPlayed) {
      movesToGetHere += playRandomMove(board)
    }
  }

  // Performs a random playout, in order to evaluate the score.
  def addPlayout() {
    val playoutBoard = board.clone()
    while (playoutBoard.isActive) {
      playRandomMove(playoutBoard)
    }

    numPlayouts += 1
    sumScores += playoutBoard.score
  }

  // Plays a random move *on the given board*. Returns the move played.
  private def playRandomMove(b: Board): Moves.Move = {
    var move = Moves.randomMove()
    var success = false
    while (!success) {
      try {
        b.doMove(move)
        success = true
      } catch {
        case _: b.InvalidMoveException =>
          // Try another move
          move = Moves.randomMove()
      }
    }
    move
  }

  // Tree nodes are equal if their boards are equal (we don't consider all other fields)
  override def equals(other: Any): Boolean = {
    if (other == null || !other.isInstanceOf[TreeNode]) return false

    other.asInstanceOf[TreeNode].board.equals(board)
  }

  // Override hashCode along with equals
  override def hashCode(): Int = board.hashCode()

  // Accessor for board
  def board = _board

  // The moves it took to get to this TreeNode
  val movesToGetHere = ArrayBuffer.empty[Moves.Move]

  // The number of playouts we've done from this node
  var numPlayouts = 0

  // The sum of scores found in all playouts
  var sumScores = 0

  // The average score from all playouts
  def avgScore: Double = {
    if (numPlayouts != 0) sumScores / numPlayouts.toDouble else 0.0
  }
}
