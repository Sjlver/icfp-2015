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
    board.numUnitsPlayed should be (0)
    board.blockIndex should be (0)
  }
    
  "A Board" should "should process moves" in {
    val board = new Board()
    board.fromJson(BOARD_JSON)
    
    board.activeBlock.template.members should be (Array((-1, 0), (1, 0)))
    board.activeBlock.pivot should be (2, 0)
    board.activeBlock.rotation should be (0)
    
    board.doMove(Moves.SE)
    board.activeBlock.template.members should be (Array((-1, 0), (1, 0)))
    board.activeBlock.pivot should be (2, 1)
    board.activeBlock.rotation should be (0)
    
    board.doMove(Moves.CCW)
    board.activeBlock.template.members should be (Array((-1, 0), (1, 0)))
    board.activeBlock.pivot should be (2, 1)
    board.activeBlock.rotation should be (1)
  }
  
  "A Board" should "write itself to JSON" in {
    val board = new Board
    board.width = 2
    board.height = 2
    board.grid = Array(Array(false, true), Array(false, false))
    
    board.toJson should be ("""{"width":2,"height":2,"filled":[{"x":0,"y":1}]}""")
  }
    
}