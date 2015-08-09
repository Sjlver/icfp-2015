// An artificial intelligence built on Monte Carlo Tree Search

import scala.collection.mutable.ArrayBuffer
import scala.util.Random

object SamplingAI {
  // The number of random playouts per move
  val NUM_PLAYOUTS = 100
}

class SamplingAI(board: Board) {
  // Runs the AI on a single game, and produces a sequence of moves.
  def run(): ArrayBuffer[Moves.Move] = {
    System.err.println("SamplingAI running on board:\n" + board)

    var result = ArrayBuffer.empty[Moves.Move]
    while (board.isActive) {

      // Generate a game tree
      nodesInTree.clear()
      val root = new TreeNode(board.clone(), null, this)
      0.to(SamplingAI.NUM_PLAYOUTS - 1).foreach { i =>
        val node = root.selectLeaf()
        node.expand()
        val child = node.selectLeaf()
        child.addPlayout()
      }

      val bestMove = root.bestMove()
      board.doMove(bestMove)
      result += bestMove

      System.err.println("Chose move with avgScore " + root.avgScore + ":\n" + board)
    }
    result
  }

  // The set of nodes in the game tree of this AI
  val nodesInTree = scala.collection.mutable.HashSet.empty[TreeNode]

  // The root of the game tree, for the current move
	var root: TreeNode = null
}

// A node in the search tree
class TreeNode(_board: Board, parent: TreeNode, ai: SamplingAI) {

  // Selects a leaf in the tree rooted at this node
  def selectLeaf(): TreeNode = {
    if (isLeaf) return this

    // TODO: select based on desirability
    var childIndex = Random.nextInt(children.size)
    children.foreach { case (move, child) =>
      if (childIndex == 0) return child.selectLeaf()
      childIndex -= 1
    }

    throw new AssertionError("Bad random sampling code?!")
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

    numPlayouts += 1
    sumScores += playoutBoard.score
  }

  def bestMove(): Moves.Move = {
    if (isLeaf) {
      throw new AssertionError("bestMove must be called on a non-leaf node.")
    }

    var bestScore = Double.MinValue
    var result: Moves.Move  = null
    children.foreach { case (move, child) =>
      if (child.avgScore > bestScore) {
        bestScore = child.avgScore
        result = move
      }
    }

    result
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
