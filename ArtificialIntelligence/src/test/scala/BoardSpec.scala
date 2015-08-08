import scala.collection.mutable.Stack

class BoardSpec extends UnitSpec {

  "A Board" should "load itself from JSON" in {
    val board = new Board()
    board.fromJson("""
      {
        "height":10,
        "width":5,
        "sourceSeeds":[0],
        "units":[{"members":[{"x":0,"y":0}],"pivot":{"x":0,"y":0}},
                 {"members":[{"x":0,"y":0},{"x":2,"y":0}],"pivot":{"x":1,"y":0}},
                 {"members":[{"x":0,"y":0},{"x":0,"y":2}],"pivot":{"x":0,"y":1}},
                 {"members":[{"x":2,"y":0},{"x":0,"y":1},{"x":2,"y":2}],"pivot":{"x":1,"y":1}},
                 {"members":[{"x":0,"y":0},{"x":1,"y":1},{"x":0,"y":2}],"pivot":{"x":0,"y":1}},
                 {"members":[{"x":0,"y":0},{"x":1,"y":0}],"pivot":{"x":0,"y":0}},
                 {"members":[{"x":0,"y":0},{"x":1,"y":0}],"pivot":{"x":1,"y":0}},
                 {"members":[{"x":0,"y":0},{"x":0,"y":1}],"pivot":{"x":0,"y":0}},
                 {"members":[{"x":0,"y":0},{"x":0,"y":1}],"pivot":{"x":0,"y":1}},
                 {"members":[{"x":0,"y":0},{"x":1,"y":0},{"x":2,"y":0}],"pivot":{"x":0,"y":0}},
                 {"members":[{"x":0,"y":0},{"x":1,"y":0},{"x":2,"y":0}],"pivot":{"x":1,"y":0}},
                 {"members":[{"x":0,"y":0},{"x":1,"y":0},{"x":2,"y":0}],"pivot":{"x":2,"y":0}},
                 {"members":[{"x":0,"y":0},{"x":0,"y":1},{"x":0,"y":2}],"pivot":{"x":0,"y":0}},
                 {"members":[{"x":0,"y":0},{"x":0,"y":1},{"x":0,"y":2}],"pivot":{"x":0,"y":1}},
                 {"members":[{"x":0,"y":0},{"x":0,"y":1},{"x":0,"y":2}],"pivot":{"x":0,"y":2}},
                 {"members":[{"x":1,"y":0},{"x":0,"y":1},{"x":1,"y":2}],"pivot":{"x":1,"y":0}},
                 {"members":[{"x":1,"y":0},{"x":0,"y":1},{"x":1,"y":2}],"pivot":{"x":1,"y":1}},
                 {"members":[{"x":1,"y":0},{"x":0,"y":1},{"x":1,"y":2}],"pivot":{"x":1,"y":2}}],
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
         "sourceLength":100}
    """)
    
    board.height should be (10)
    board.width should be (5)
    
    board.grid(0)(0) should be (false)
    board.grid(3)(8) should be (false)
    board.grid(0)(8) should be (true)
    board.grid(3)(9) should be (true)
  }
  
  "A Board" should "write itself to JSON" in {
    val board = new Board
    board.width = 2
    board.height = 2
    board.grid = Array(Array(false, true), Array(false, false))
    
    board.toJson should be ("""{"width":2,"height":2,"filled":[{"x":0,"y":1}]}""")
  }
    
}