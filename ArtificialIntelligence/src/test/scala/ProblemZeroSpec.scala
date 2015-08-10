class ProblemZeroSpec extends UnitSpec {
  val PROBLEM_ZERO_JSON = """{"height":10,"width":10,"sourceSeeds":[0],
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
    "id":0,"filled":[],"sourceLength":100}
"""

  val PROBLEM_ZERO_BAD_MOVES = """palblblpalapaapablplaalblppplalblpablaladbkkpkkklkakkllkappkklbb
dppldapddlldbkabddllpkplkkplbblblklaklldlabddadaappaplbddbbabakppaladlbbbbldddaldalkpppddbdbll
aldadppapaaplkadlppddpkpppkapabdbldpaablbdppppdlpkblbdbadbadlkdadapaababklkbbbkappdbdbldlblbal
pdakpdaabpdllkpkpdabldbdlbkaalpbbbbdllppdldddbbdpppklpaldldldpppallbkbbdklplkbbapkblbaadblkbdd
abblaadppdbdddpklpdppkppalabdkabdabadakpadllbbkllkdbkkkpkkabkbaaldlblbdppakblappdbda"""

  "A Board" should "correctly simulate problem zero" in {
    val board = Board.fromJson(PROBLEM_ZERO_JSON)
    board.startNewGame()

    // These are bad moves from a previous version of the code, so they should generate an exception.
    a [board.InvalidMoveException] should be thrownBy {
      0.to(PROBLEM_ZERO_BAD_MOVES.size - 1).foreach { i =>
        if (Moves.isValidMoveChar(PROBLEM_ZERO_BAD_MOVES(i))) {
          val move = Moves.fromChar(PROBLEM_ZERO_BAD_MOVES(i))
          board.doMove(move)
        }
      }
    }
  }
}
