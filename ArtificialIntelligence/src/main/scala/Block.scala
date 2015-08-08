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
    template.members.foreach { case cell =>
      leftMost = leftMost.min(cell.x)
      rightMost = rightMost.max(cell.x)
      topMost = topMost.min(cell.y)
    }
    
    val blockWidth = rightMost - leftMost + 1
    val spaceAvailable = width - blockWidth
    val pivotY = -topMost
    val pivotX = -leftMost + spaceAvailable / 2
    
    Block(template, HexCell.fromXY(pivotX, pivotY), 0)
  }
}

case class Block(template: BlockTemplate, pivot: HexCell, rotation: Int) {
  // Return a new block, translated and rotated according to `move`.
  def moved(move: Moves.Move): Block = {
     move match {
      case Moves.E | Moves.W | Moves.SE | Moves.SW => Block(template, translatedCell(pivot, move), rotation)
      case Moves.CW | Moves.CCW => Block(template, pivot, updatedRotation(move))
    }
  }
  
  private def translatedCell(cell: HexCell, move: Moves.Move): HexCell = {
    move match {
      case Moves.E => cell.translated(HexCell(1, 0))
      case Moves.W => cell.translated(HexCell(-1, 0))
      case Moves.SE => cell.translated(HexCell(0, 1))
      case Moves.SW => cell.translated(HexCell(-1, 1))
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
  def transformedCells: Array[HexCell] = {
    template.members.map { cell =>
      // Rotate around origin, then translate to pivot position.
      cell.rotated(rotation).translated(pivot)
    }
  }
}