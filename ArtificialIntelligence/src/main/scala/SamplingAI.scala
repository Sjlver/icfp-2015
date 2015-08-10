// An artificial intelligence built on Monte Carlo Tree Search

import scala.collection.mutable.ArrayBuffer
import scala.util.Random

object SamplingAI {
  // The number of random playouts per move.
  val DEFAULT_NUM_PLAYOUTS_PER_MOVE = 100
  val MAX_NUM_PLAYOUTS_PER_MOVE = 5000
  val MIN_NUM_PLAYOUTS_PER_MOVE = 10

  // How much we favor exploration over exploitation.
  val EXPLORATION_FACTOR = 2.0

  // How much we prefer locking moves over other moves (<1 because we prefer others).
  val LOCKING_MOVE_FACTOR = 0.02

  // The maximum amount by which we adjust the number of playouts per move
  val MAX_ABSOLUTE_ADJUSTMENT = 20

  // Returns an element sampled from `elems` according to the given weights.
  // Weights must be non-negative.
  def weightedRandomSample[T](elems: Seq[T], weights: Seq[Double]): T = {
    // Compute the cumulative density function.
    val cdf = Array.fill(elems.size)(0.0)
    cdf(0) = weights(0)
    1.to(elems.size - 1).foreach { i =>
      cdf(i) = cdf(i - 1) + weights(i)
    }

    val r = Random.nextDouble() * cdf(elems.size - 1)
    val (_, index) = cdf.zipWithIndex.find(_._1 >= r).get
    elems(index)
  }
}

class SamplingAI(board: Board, endMillis: Long) {
  // Runs the AI on a single game, and produces a sequence of moves.
  def run(): ArrayBuffer[Moves.Move] = {
    Options.log("SamplingAI running on board:\n" + board)

    var result = ArrayBuffer.empty[Moves.Move]

    while (root.board.isActive) {
      // Explore the game tree
      val numPlayoutsAtExplorationStart = numPlayoutsInTree
      while (numPlayoutsInTree - numPlayoutsAtExplorationStart < numPlayoutsPerMove) {
        val node = root.selectLeaf()
        node.expand()
        if (node.isLeaf) {
          // Expansion failed... just add a playout from this node
          node.addPlayout()
        } else {
          // Expansion was successful. Ensure that each child has at least one playout.
          node.children.foreach { case (move, child) =>
            child.addPlayout()
          }
        }
      }

      // Perform a move
      val (bestMove, bestChild) = root.bestMove()
      Options.log("SamplingAI: performed " +
          (numPlayoutsInTree - numPlayoutsAtExplorationStart) + "/" + numPlayoutsPerMove +
          " playouts on board:")
      Options.log("  " + board.toString().replaceAll("\n", "\n  "))
      Options.log("  Chose move " + bestMove +
          " with avgScore=" + root.avgScore +
          ", avgMovesDone=" + root.avgMovesDone)

      board.doMove(bestMove)
      result += bestMove

      // Update the tree. We try to re-use as much of it as possible
      root = bestChild
      numPlayoutsInTree = root.numPlayouts

      adjustNumPlayoutsPerMove()
    }
    result
  }


  // Adjusts the number of playouts, according to how well we're doing.
  private def adjustNumPlayoutsPerMove() {
    val currentMillis = System.currentTimeMillis()

    val timeElapsed = (currentMillis - startMillis).toDouble
    val totalTime = (endMillis - startMillis).toDouble
    val playoutsPerMilli = numPlayoutsTotal / timeElapsed

    val timeLeft = (endMillis - currentMillis).toDouble
    val playoutsLeft = timeLeft * playoutsPerMilli

    val movesLeft = (root.avgMovesDone - board.numMovesDone).max(1.0)
    val targetNumPlayoutsPerMove = playoutsLeft / movesLeft

    // Smooth the adjustment, otherwise we react too extremely
    def limit(value: Double, min: Double, max: Double) = value.min(max).max(min)

    val adjustment = limit(targetNumPlayoutsPerMove - numPlayoutsPerMove,
        -SamplingAI.MAX_ABSOLUTE_ADJUSTMENT, SamplingAI.MAX_ABSOLUTE_ADJUSTMENT)

    Options.log("  adjust " + (if (adjustment < 0.0) "v" else "^") +
        ": elapsed=" + timeElapsed + " (" + 100 * timeElapsed / totalTime + "%) " +
        ", actual=" + numPlayoutsPerMove +
        ", target=" + targetNumPlayoutsPerMove)

    numPlayoutsPerMove = limit(numPlayoutsPerMove + adjustment,
        SamplingAI.MIN_NUM_PLAYOUTS_PER_MOVE, SamplingAI.MAX_NUM_PLAYOUTS_PER_MOVE)
  }

  // The root of the game tree, for the current move
	var root: TreeNode = new TreeNode(board, null, this)

  // The total number of playouts in the tree
  var numPlayoutsInTree = 0

  // The total number of playouts done so far (used for estimating the time it takes)
  var numPlayoutsTotal = 0

  // The number of playouts per move (will be adjusted according to the time limit)
  var numPlayoutsPerMove = SamplingAI.DEFAULT_NUM_PLAYOUTS_PER_MOVE.toDouble

  // The time when we started this game.
  // Note that if the end time already passed, we adjust this to make the adjustment computation easier.
  val startMillis = Math.min(System.currentTimeMillis(), endMillis - 1)
}

// A node in the search tree
class TreeNode(_board: Board, parent: TreeNode, ai: SamplingAI) {

  // Selects a leaf in the tree rooted at this node
  def selectLeaf(): TreeNode = {
    if (isLeaf) return this

    val selectedChild = children.maxBy { case (move, child) => child.upperConfidenceBound }
    selectedChild._2.selectLeaf()
  }

  // Expand this node by adding all the children
  def expand() {
    if (!isLeaf) {
      throw new AssertionError("expand must be called on a leaf node.")
    }
    if (!board.isActive) {
      // Cannot expand... but I guess just doing nothing is fine in this case.
      return
    }

    Moves.ALL_MOVES.foreach { move =>
      val childBoard = board.clone()
      try {
        childBoard.doMove(move)
        val child = new TreeNode(childBoard, this, ai)
        children(move) = child
      } catch {
        case _: childBoard.InvalidMoveException => Unit
      }
    }
  }

  // Performs a random playout, in order to evaluate the score.
  // TODO: this is a main source of crappyness in our AI. Make this much better than random.
  def addPlayout() {
    if (!isLeaf) {
      throw new AssertionError("addPlayout must be called on a leaf node.")
    }

    val playoutBoard = board.clone()
    while (playoutBoard.isActive) {
      playRandomMove(playoutBoard)
    }

    processPlayoutResult(playoutBoard)
    ai.numPlayoutsInTree += 1
    ai.numPlayoutsTotal += 1
  }

  private def processPlayoutResult(board: Board) {
    numPlayouts += 1
    sumScores += board.score
    sumMovesDone += board.numMovesDone

    if (parent != null) parent.processPlayoutResult(board)
  }

  def bestMove(): (Moves.Move, TreeNode) = {
    if (isLeaf) {
      throw new AssertionError("bestMove must be called on a non-leaf node.")
    }

    // Wikipedia recommends to choose the move with the highest number of
    // simulations, not the one with the best average score...
    // If Wikipedia says it, it must be true :)
    children.maxBy { case (move, child) => child.numPlayouts }
  }

  // How much do we want to explore this node?
  // The UCT value from Wikipedia's page on Monte Carlo Tree Sampling.
  def upperConfidenceBound: Double = {
    if (numPlayouts == 0) {
      throw new AssertionError("upperConfidenceBound requires at least one playout.")
    }

    // Given that our scores are integers rather than win/loss, we normalize them by
    val exploitation = avgScore / ai.root.avgScore
    val exploration = Math.sqrt(Math.log(ai.numPlayoutsInTree) / numPlayouts)
    exploitation + SamplingAI.EXPLORATION_FACTOR * exploration
  }

  // Plays a random move *on the given board*. Returns the move played.
  private def playRandomMove(board: Board): Moves.Move = {
    val moveWeights = Moves.ALL_MOVES.map { move => 1.0 }
    Moves.ALL_MOVES.zipWithIndex.foreach { case (move, i) =>
      if (board.isInvalidMove(move)) {
        moveWeights(i) = 0.0
      } else if (board.isLockingMove(move)) {
        moveWeights(i) *= SamplingAI.LOCKING_MOVE_FACTOR
      }
    }

    val move = SamplingAI.weightedRandomSample(Moves.ALL_MOVES, moveWeights)
    board.doMove(move)
    move
  }

  // Tree nodes are equal if their boards are equal (we don't consider all other fields)
  override def equals(other: Any): Boolean = {
    if (other == null || !other.isInstanceOf[TreeNode]) return false

    other.asInstanceOf[TreeNode].board.equals(board)
  }

  override def toString(): String = toStringWithPrefix("")

  def toStringWithPrefix(prefix: String): String = {
    val result = new scala.collection.mutable.StringBuilder()
    result ++= "" + avgScore + " = " + sumScores + " / " + numPlayouts + ", UCB: " + upperConfidenceBound
    children.foreach { case (move, child) =>
      result ++= "\n" + prefix + "  " + move + " => " + child.toStringWithPrefix(prefix + "        ")
    }

    result.toString()
  }

  // Override hashCode along with equals
  override def hashCode(): Int = board.hashCode()

  // Accessor for board
  def board = _board

  // The children of this node
  val children = scala.collection.mutable.Map.empty[Moves.Move, TreeNode]

  def isLeaf: Boolean = children.isEmpty

  // The number of playouts we've done from this node (or its children)
  var numPlayouts = 0

  // The sum of scores found in all playouts
  var sumScores = 0

  // The sum of all the moves done in all playouts
  var sumMovesDone = 0

  // The average score from all playouts
  def avgScore: Double = {
    if (numPlayouts != 0) sumScores / numPlayouts.toDouble else 0.0
  }

  // The average number of moves from all playouts
  def avgMovesDone: Double = {
    if (numPlayouts != 0) sumMovesDone / numPlayouts.toDouble else 0.0
  }
}
