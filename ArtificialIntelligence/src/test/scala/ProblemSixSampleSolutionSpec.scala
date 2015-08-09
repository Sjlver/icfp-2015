// Tests the sample solution for problem six, given on the official contest website.

class ProblemSixSampleSolutionSpec extends UnitSpec {
  val MOVES = """
iiiiiiimimiiiiiimmimiiiimimimmimimimimmeemmimimiimmmmimmimiimimimmimmimeee
mmmimimmimeeemiimiimimimiiiipimiimimmmmeemimeemimimimmmmemimmimmmiiimmmiii
piimiiippiimmmeemimiipimmimmipppimmimeemeemimiieemimmmm"""
  
  val PROBLEM_6_JSON = """
    {"height":10,"width":10,
     "sourceSeeds":[0,13120,18588,31026,7610,25460,23256,19086,24334,22079,9816,8466,
                    3703,13185,26906,16903,24524,9536,11993,21728,2860,13859,21458,
                    15379,10919,7082,26708,8123,18093,26670,16650,1519,15671,24732,
                    16393,5343,28599,29169,8856,23220,25536,629,24513,14118,17013,
                    6839,25499,17114,25267,8780],
     "units":[
       {"members":[{"x":0,"y":0}],"pivot":{"x":0,"y":0}},
       {"members":[{"x":0,"y":0},{"x":1,"y":0}],"pivot":{"x":0,"y":0}},
       {"members":[{"x":0,"y":0},{"x":1,"y":0},{"x":2,"y":0}],"pivot":{"x":1,"y":0}},
       {"members":[{"x":0,"y":0},{"x":1,"y":0},{"x":0,"y":1}],"pivot":{"x":0,"y":0}},
       {"members":[{"x":0,"y":0},{"x":1,"y":0},{"x":1,"y":1}],"pivot":{"x":1,"y":0}}],
     "id":6,"filled":[],"sourceLength":150}"""
  
  "A Board" should "correctly replay a sample solution" in {
    val board = new Board
    board.fromJson(PROBLEM_6_JSON)
    board.startNewGame()
    
    0.to(MOVES.size - 2).foreach { i =>
      if (Moves.isValidMoveChar(MOVES(i))) {
        val move = Moves.fromChar(MOVES(i))
        board.doMove(move) should be (true)
        //println(board.toString)
      }
    }
    board.doMove(Moves.fromChar(MOVES(MOVES.size - 1))) should be (false)
    //println(board.toString)
    board.score should be (61)
  }
}