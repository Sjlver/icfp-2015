import scala.collection.mutable.Stack

class BoardSpec extends UnitSpec {

  "A Board" should "load itself from JSON" in {
    val board = new Board()
    board.loadFromJson("""
      {
        "height":10,
        "width":10,
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
         "filled":[],
         "sourceLength":100}
    """)
    
    board.height should be (10)
    board.width should be (10)
  }
}