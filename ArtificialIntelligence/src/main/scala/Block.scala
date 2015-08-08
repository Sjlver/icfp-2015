// A set of cells that can be moved, rotated, ...
// The task specification calls this a "unit".
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
    
    new Block(template, (pivotX, pivotY), 0)
  }
}

class Block(val template: BlockTemplate, var pivot: (Int, Int), var rotation: Int)