import scala.collection.mutable.Stack

class BoardSpec extends UnitSpec {

  val BOARD_JSON = """
    {
      "height":10,
      "width":5,
      "sourceSeeds":[17],
      "units":[{"members":[{"x":0,"y":0},{"x":2,"y":0}],"pivot":{"x":1,"y":0}}],
      "id":0,
      "filled":[
        {"x": 0, "y": 8},
        {"x": 1, "y": 8},
        {"x": 0, "y": 9},
        {"x": 1, "y": 9},
        {"x": 4, "y": 8},
        {"x": 3, "y": 9},
        {"x": 4, "y": 9}
      ],
      "sourceLength":100
    }
  """
  
  "A Board" should "load itself from JSON" in {
    val board = new Board()
    board.fromJson(BOARD_JSON)
    
    board.height should be (10)
    board.width should be (5)
    
    board.grid(0)(0) should be (false)
    board.grid(3)(8) should be (false)
    board.grid(0)(8) should be (true)
    board.grid(3)(9) should be (true)
    
    board.sourceSeedIndex should be (0)
    board.numBlocksPlayed should be (0)
    board.blockIndex should be (0)
  }

  "A Board" should "write itself to JSON" in {
    val board = new Board
    board.width = 2
    board.height = 2
    board.grid = Array(Array(false, true), Array(false, false))
    
    board.toJson should be ("""{"width":2,"height":2,"filled":[{"x":0,"y":1}]}""")
  }
    
  "A Board" should "should process moves" in {
    val board = new Board()
    board.fromJson(BOARD_JSON)
    
    board.activeBlock.template.members should be (Array(HexCell.fromXY(-1, 0), HexCell.fromXY(1, 0)))
    board.activeBlock.pivot should be (HexCell.fromXY(2, 0))
    board.activeBlock.rotation should be (0)
    
    board.doMove(Moves.SE)
    board.activeBlock.template.members should be (Array(HexCell.fromXY(-1, 0), HexCell.fromXY(1, 0)))
    board.activeBlock.pivot should be (HexCell.fromXY(2, 1))
    board.activeBlock.rotation should be (0)
    
    board.doMove(Moves.CCW)
    board.activeBlock.template.members should be (Array(HexCell.fromXY(-1, 0), HexCell.fromXY(1, 0)))
    board.activeBlock.pivot should be (HexCell.fromXY(2, 1))
    board.activeBlock.rotation should be (1)
  }
  
  "A Board" should "detect translations that lead to repetition" in {
    val board = new Board()
    board.fromJson(BOARD_JSON)

    board.doMove(Moves.E)    
    a [board.InvalidMoveException] should be thrownBy {
      board.doMove(Moves.W)
    }
  }

  "A Board" should "detect rotations that lead to repetition" in {
    val board = new Board()
    board.fromJson(BOARD_JSON)

    board.doMove(Moves.SE)
    board.doMove(Moves.CW)
    board.doMove(Moves.CW)
    a [board.InvalidMoveException] should be thrownBy {
      board.doMove(Moves.CW)
    }
  }

  "A Board" should "detect moves that exit the grid" in {
    val board = new Board()
    board.fromJson(BOARD_JSON)

    board.doMove(Moves.SW)
    board.numBlocksPlayed should be (0)
    board.grid(0)(1) should be (false)
    board.grid(1)(1) should be (false)
    board.grid(2)(1) should be (false)

    // Lock the block by moving it out of the grid
    board.doMove(Moves.W)
    board.numBlocksPlayed should be (1)
    board.grid(0)(1) should be (true)
    board.grid(1)(1) should be (false)
    board.grid(2)(1) should be (true)
  }
  
  "A Board" should "clear full rows" in {
    val board = new Board()
    board.fromJson(BOARD_JSON)

    // First block, fill (4, 3) and (3, 1)
    board.doMove(Moves.SE)
    board.doMove(Moves.CW)
    board.doMove(Moves.SE)
    board.doMove(Moves.E)
    board.doMove(Moves.E)
    
    // Second block, fill (0, 3) and (2, 3)
    board.doMove(Moves.SW)
    board.doMove(Moves.SW)
    board.doMove(Moves.SE)
    board.doMove(Moves.W)

    // Third block, fill (1, 3) and (3, 3). Don't lock just yet.
    board.doMove(Moves.SW)
    board.doMove(Moves.SE)
    board.doMove(Moves.SE)
    
    board.grid(0)(3) should be (true)
    board.grid(1)(3) should be (false)
    board.grid(2)(3) should be (true)
    board.grid(3)(3) should be (false)
    board.grid(4)(3) should be (true)
    
    // Lock and clear!
    board.doMove(Moves.E)
    // Cell at (3, 1) should move down to (3, 2)
    board.grid(3)(1) should be (false)
    board.grid(3)(2) should be (true)
    // Row 3 should now be clear
    0.to(4).foreach { i => board.grid(i)(3) should be (false) }
  }  

  "A Board" should "keep track of score correctly" in {
    val board = new Board()
    board.fromJson(BOARD_JSON)

    board.score should be (0)
    
    // First block, fill (4, 3) and (3, 1)
    board.doMove(Moves.SE)
    board.doMove(Moves.CW)
    board.doMove(Moves.SE)
    board.doMove(Moves.E)
    board.doMove(Moves.E)
    board.score should be (2)
    
    // Second block, fill (0, 3) and (2, 3)
    board.doMove(Moves.SW)
    board.doMove(Moves.SW)
    board.doMove(Moves.SE)
    board.doMove(Moves.W)
    board.score should be (4)

    // Third block, fill (1, 3) and (3, 3). Don't lock just yet.
    board.doMove(Moves.SW)
    board.doMove(Moves.SE)
    board.doMove(Moves.SE)
    board.score should be (4)
        
    // Lock and clear!
    board.doMove(Moves.E)
    board.score should be (106)
  }  

}