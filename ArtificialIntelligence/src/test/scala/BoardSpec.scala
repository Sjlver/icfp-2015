import scala.collection.mutable.Stack

object BoardSpec {
  val BOARD_JSON = """
    {
      "height":10,
      "width":5,
      "sourceSeeds":[17],
      "units":[{"members":[{"x":0,"y":0},{"x":2,"y":0}],"pivot":{"x":1,"y":0}}],
      "id":42,
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

  val MINIMAL_BOARD_JSON = """
    {
      "height":3,
      "width":3,
      "sourceSeeds":[17],
      "units":[{"members":[{"x":0,"y":0}],"pivot":{"x":0,"y":0}}],
      "id":42,
      "filled":[],
      "sourceLength":1
    }
  """

}

class BoardSpec extends UnitSpec {

  "A Board" should "load itself from JSON" in {
    val board = Board.fromJson(BoardSpec.BOARD_JSON)
    
    board.problemId should be (42)
    
    board.sourceSeedIndex should be (-1)
    board.isActive should be (false)
  }

  "A Board" should "write itself to JSON" in {
    val board = Board.fromJson(BoardSpec.BOARD_JSON)
    board.startNewGame()
    
    board.toJsonObject.compactPrint should be (
        """{"width":5,"height":10,""" +
        """"filled":[{"x":0,"y":8},{"x":0,"y":9},{"x":1,"y":8},{"x":1,"y":9},{"x":3,"y":9},{"x":4,"y":8},{"x":4,"y":9}],""" +
        """"activeBlock":{"members":[{"x":1,"y":0},{"x":3,"y":0}],"pivot":{"x":2,"y":0}}}""")
  }
    
  "A Board" should "should process moves" in {
    val board = Board.fromJson(BoardSpec.BOARD_JSON)
    board.startNewGame()
    
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
    val board = Board.fromJson(BoardSpec.BOARD_JSON)
    board.startNewGame()

    board.doMove(Moves.E)    
    a [board.InvalidMoveException] should be thrownBy {
      board.doMove(Moves.W)
    }
  }

  "A Board" should "detect rotations that lead to repetition" in {
    val board = Board.fromJson(BoardSpec.BOARD_JSON)
    board.startNewGame()

    board.doMove(Moves.SE)
    board.doMove(Moves.CW)
    board.doMove(Moves.CW)
    a [board.InvalidMoveException] should be thrownBy {
      board.doMove(Moves.CW)
    }
  }

  "A Board" should "detect moves that exit the grid" in {
    val board = Board.fromJson(BoardSpec.BOARD_JSON)
    board.startNewGame()

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
    val board = Board.fromJson(BoardSpec.BOARD_JSON)
    board.startNewGame()

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
    val board = Board.fromJson(BoardSpec.BOARD_JSON)
    board.startNewGame()

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

  "A Board" should "should end the game when the grid is full" in {
    val board = Board.fromJson(BoardSpec.BOARD_JSON)
    board.startNewGame() should be (true)
    board.isActive should be (true)

    // Lock the first block
    board.doMove(Moves.W) should be (true)
    board.doMove(Moves.W) should be (true)
    board.isActive should be (true)
 
    // Lock the second block right in the spawning area
    board.doMove(Moves.W) should be (false)
    board.isActive should be (false)
  }

  "A Board" should "should end the game when all blocks have been played" in {
    val board = Board.fromJson(BoardSpec.MINIMAL_BOARD_JSON)
    board.startNewGame() should be (true)

    // Lock the first block
    board.doMove(Moves.W) should be (true)
    board.doMove(Moves.W) should be (false)
  }

  "A Board" should "clone itself properly" in {
    val board = Board.fromJson(BoardSpec.MINIMAL_BOARD_JSON)
    board.startNewGame()
    board.doMove(Moves.W)

    val clone = board.clone()
    board.toString() should be (clone.toString())
    
    board.doMove(Moves.W)
    board.toString() should not be (clone.toString())
    clone.doMove(Moves.W)
    board.toString() should be (clone.toString())
  }

}