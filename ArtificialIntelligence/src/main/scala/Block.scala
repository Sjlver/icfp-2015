// A set of cells that can be moved, rotated, ...
// The task specification calls this a "unit".
// Pivot position is stored as a tuple of x/y coordinates.
// Rotation is a number from zero to five. Rotating counter-clockwise increases the value.
object Block {
  def spawn(template: BlockTemplate, width: Int): Block = {
    // Find the extents of the object
    var leftMost = Int.MaxValue
    var rightMost = Int.MinValue
    var topMost = Int.MaxValue
    template.members.foreach { case (x, y) =>
      leftMost = leftMost.min(x)
      rightMost = rightMost.max(x)
      topMost = topMost.min(y)
    }
    
    val blockWidth = rightMost - leftMost + 1
    val spaceAvailable = width - blockWidth
    val pivotY = -topMost
    val pivotX = -leftMost + spaceAvailable / 2
    
    Block(template, (pivotX, pivotY), 0)
  }
}

case class Block(template: BlockTemplate, pivot: (Int, Int), rotation: Int) {
  // Return a new block, translated and rotated according to `move`.
  def moved(move: Moves.Move): Block = {
     move match {
      case Moves.E | Moves.W | Moves.SE | Moves.SW => Block(template, translateCell(pivot, move), rotation)
      case Moves.CW | Moves.CCW => Block(template, pivot, updatedRotation(move))
    }
  }
  
  private def translateCell(cell: (Int, Int), move: Moves.Move): (Int, Int) = {
    val isEvenRow = cell._2 % 2 == 0
    move match {
      case Moves.E => (cell._1 + 1, cell._2)
      case Moves.W => (cell._1 - 1, cell._2)
      case Moves.SE => (if (isEvenRow) cell._1 else cell._1 + 1, cell._2 + 1)
      case Moves.SW => (if (isEvenRow) cell._1 - 1 else cell._1, cell._2 + 1)
      case Moves.CW | Moves.CCW => throw new AssertionError("translateCell called with rotation")
    }
  }

  private def updatedRotation(move: Moves.Move): Int = {
    move match {
      case Moves.CW => (rotation + 5) % template.numRotations  
      case Moves.CCW => (rotation + 1) % template.numRotations
      case _ => throw new AssertionError("updatedRotation called with translation")
    }
  }

  // Transforms the template cells according to the current pivot position and rotation.
  // Rotation code inspired by
  // http://www.redblobgames.com/grids/hexagons/#rotation
  def transformedCells = {
    template.members
  }
}