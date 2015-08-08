class HexCellSpec extends UnitSpec {
  "A HexCell" should "be constructible from x and y coords" in {
    val cell = HexCell.fromXY(1, 2)
    cell.x should be (1)
    cell.y should be (2)
    cell.q should be (0)
    cell.r should be (2)
  }

  "A HexCell" should "know how to rotate itself" in {
    val cell = HexCell.fromXY(1, 2).rotated(1)
    cell.x should be (2)
    cell.y should be (0)

    val cell2 = HexCell.fromXY(1, 1).rotated(-1)
    cell2.x should be (0)
    cell2.y should be (2)

    val cell3 = HexCell.fromXY(1, 1).rotated(-9)
    cell3.x should be (-2)
    cell3.y should be (-1)
}

}