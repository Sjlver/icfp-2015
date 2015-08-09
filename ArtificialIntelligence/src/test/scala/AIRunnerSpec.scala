import scala.util.Random
import spray.json._

class AIRunnerSpec extends UnitSpec {
  "An AIRunner" should "run AIs" in {
    Random.setSeed(42)
    val board = new Board
    board.fromJson(BoardSpec.BOARD_JSON)
    
    val aiRunner = new AIRunner(
        board,
        b => new SamplingAI(b),
        c => new PowerPhraseEncoder(c),
        "AIRunnerSpec")
    
    val solutionsArray = aiRunner.run()
    
    // Since this is a random solution, we just check whether the length seems OK
    solutionsArray match {
      case JsArray(Seq(solution)) => {
        (solution.asJsObject.fields("solution"): @unchecked) match {
          case JsString(s) => s.size should be > 10
        }
      }
    }
    
    // println(solutionsArray.compactPrint)
  }
}