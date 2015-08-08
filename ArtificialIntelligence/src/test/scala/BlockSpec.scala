import spray.json._

class BlockSpec extends UnitSpec {
  "A Block" should "be able to transform its cells (translation)" in {
    val block = Block(BlockTemplate(Array((0, 0))), (3, 4), 0)
    block.transformedCells should be (Array((3, 4)))
  }  

  "A Block" should "be able to transform its cells (rotation)" in {
    val block1 = Block(BlockTemplate(Array((-1, 0))), (0, 0), 0)
    block1.transformedCells should be (Array((-1, 0)))

    val block2 = Block(BlockTemplate(Array((-1, 0))), (0, 0), 1)
    block2.transformedCells should be (Array((-1, 1)))

    val block3 = Block(BlockTemplate(Array((-1, 0))), (0, 0), 2)
    block3.transformedCells should be (Array((0, 1)))
  }  

  
}