import scala.util.Random
import spray.json._

object AIRunnerSpec {
  val TWOSEEDS_JSON = """
    {
      "height":1,
      "width":1,
      "sourceSeeds":[17, 42],
      "units":[{"members":[{"x":0,"y":0}],"pivot":{"x":0,"y":0}}],
      "id":42,
      "filled":[],
      "sourceLength":100
    }
  """ 
}

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
  
  "An AIRunner" should "process all seeds" in {
    Random.setSeed(42)
    val board = new Board
    board.fromJson(AIRunnerSpec.TWOSEEDS_JSON)
    board.sourceSeeds should be (Array(17, 42))
    
    val aiRunner = new AIRunner(
        board,
        b => new SamplingAI(b),
        c => new PowerPhraseEncoder(c),
        "AIRunnerSpec")
    
    val solutionsArray = aiRunner.run()
    println(solutionsArray.prettyPrint)
    solutionsArray match {
      case JsArray(solutions) =>
        solutions.size should be (2)
    }
  }
}