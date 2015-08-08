import spray.json._

class BlockSpec extends UnitSpec {
  "A Block" should "be able to transform its cells (translation)" in {
    val block = Block(BlockTemplate(Array(HexCell(0, 0))), HexCell(3, 4), 0)
    block.transformedCells should be (Array(HexCell(3, 4)))
  }  

  "A Block" should "be able to transform its cells (rotation)" in {
    val block1 = Block(BlockTemplate(Array(HexCell.fromXY(-1, 0))), HexCell(0, 0), 0)
    block1.transformedCells should be (Array(HexCell.fromXY(-1, 0)))

    val block2 = Block(BlockTemplate(Array(HexCell.fromXY(-1, 0))), HexCell(0, 0), 1)
    block2.transformedCells should be (Array(HexCell.fromXY(-1, 1)))

    val block3 = Block(BlockTemplate(Array(HexCell.fromXY(-1, 0))), HexCell(0, 0), 2)
    block3.transformedCells should be (Array(HexCell.fromXY(0, 1)))
  }  

  "A Block" should "be able to transform its cells (translation and rotation)" in {
    val block = Block(BlockTemplate(Array(HexCell.fromXY(-1, 0))), HexCell.fromXY(3, 4), 2)
    block.transformedCells should be (Array(HexCell.fromXY(3, 5)))
  }  
  
}