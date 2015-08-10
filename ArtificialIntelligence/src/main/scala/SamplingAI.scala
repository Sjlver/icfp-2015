// An artificial intelligence built on Monte Carlo Tree Search

import scala.collection.mutable.ArrayBuffer
import scala.util.Random

object SamplingAI {
  // The number of random playouts per move
  val NUM_PLAYOUTS = 1000
}

class SamplingAI(board: Board) {
  // Runs the AI on a single game, and produces a sequence of moves.
  def run(): ArrayBuffer[Moves.Move] = {
    System.err.println("SamplingAI running on board:\n" + board)

    var result = ArrayBuffer.empty[Moves.Move]
    while (board.isActive) {
      // Generate a game tree
      nodesInTree.clear()
      numPlayouts = 0
      root = new TreeNode(board.clone(), null, this)
      while (numPlayouts < SamplingAI.NUM_PLAYOUTS) {
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

      val bestMove = root.bestMove()
      board.doMove(bestMove)
      result += bestMove

      System.err.println("Chose move with avgScore " + root.avgScore)
      System.err.println(board)
      //System.err.println(root)
    }
    result
  }

  // The set of nodes in the game tree of this AI
  val nodesInTree = scala.collection.mutable.HashSet.empty[TreeNode]

  // The root of the game tree, for the current move
	var root: TreeNode = null

  // The total number of playouts in the tree
  var numPlayouts = 0
}

// A node in the search tree
class TreeNode(_board: Board, parent: TreeNode, ai: SamplingAI) {

  // Selects a leaf in the tree rooted at this node
  def selectLeaf(): TreeNode = {
    if (isLeaf) return this

    var maxUpperConfidenceBound = Double.MinValue
    var selectedChild: TreeNode = null
    children.foreach { case (move, child) =>
      if (child.upperConfidenceBound > maxUpperConfidenceBound) {
        maxUpperConfidenceBound = child.upperConfidenceBound
        selectedChild = child
      }
    }

    return selectedChild.selectLeaf()
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
        if (!ai.nodesInTree.contains(child)) {
          ai.nodesInTree += child
          children(move) = child
        }
      } catch {
        case _: childBoard.InvalidMoveException => Unit
      }
    }
  }

  // Performs a random playout, in order to evaluate the score.
  def addPlayout() {
    if (!isLeaf) {
      throw new AssertionError("addPlayout must be called on a leaf node.")
    }

    val playoutBoard = board.clone()
    while (playoutBoard.isActive) {
      playRandomMove(playoutBoard)
    }

    updatePlayoutScores(playoutBoard.score)
    ai.numPlayouts += 1
  }

  private def updatePlayoutScores(score: Int) {
    numPlayouts += 1
    sumScores += score

    if (parent != null) parent.updatePlayoutScores(score)
  }

  def bestMove(): Moves.Move = {
    if (isLeaf) {
      throw new AssertionError("bestMove must be called on a non-leaf node.")
    }

    // Wikipedia recommends to choose the move with the highest number of
    // simulations, not the one with the best average score...
    // If Wikipedia says it, it must be true :)
    var maxNumPlayouts = Int.MinValue
    var result: Moves.Move  = null
    children.foreach { case (move, child) =>
      if (child.numPlayouts > maxNumPlayouts) {
        maxNumPlayouts = child.numPlayouts
        result = move
      }
    }

    result
  }

  // How much do we want to explore this node?
  // The UCT value from Wikipedia's page on Monte Carlo Tree Sampling.
  def upperConfidenceBound: Double = {
    if (numPlayouts == 0) {
      throw new AssertionError("upperConfidenceBound requires at least one playout.")
    }

    // Given that our scores are integers rather than win/loss, we normalize them by
    val exploitation = avgScore / ai.root.avgScore
    val exploration = 1.4 * Math.sqrt(Math.log(ai.numPlayouts) / numPlayouts)
    exploitation + exploration
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

  // The average score from all playouts
  def avgScore: Double = {
    if (numPlayouts != 0) sumScores / numPlayouts.toDouble else 0.0
  }
}
