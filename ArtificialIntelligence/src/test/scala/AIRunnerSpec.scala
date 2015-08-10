import scala.util.Random
import spray.json._
import scala.collection.mutable.ArrayBuffer

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
    val board = Board.fromJson(BoardSpec.BOARD_JSON)
    
    val aiRunner = new AIRunner(
        board,
        b => new SamplingAI(b),
        c => new PowerPhraseEncoder(c, Array[String]()),
        {(b: Board, c: ArrayBuffer[Moves.Move]) => },
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
    val board = Board.fromJson(AIRunnerSpec.TWOSEEDS_JSON)
    
    val aiRunner = new AIRunner(
        board,
        b => new SamplingAI(b),
        c => new PowerPhraseEncoder(c, Array[String]()),
        {(b: Board, c: ArrayBuffer[Moves.Move]) => },
        "AIRunnerSpec")
    
    val solutionsArray = aiRunner.run()
    //println(solutionsArray.prettyPrint)
    solutionsArray match {
      case JsArray(solutions) =>
        solutions.size should be (2)
        (solutions(0).asJsObject.fields("seed"): @unchecked) match { case JsNumber(s) => s.toInt should be (17) }
        (solutions(1).asJsObject.fields("seed"): @unchecked) match { case JsNumber(s) => s.toInt should be (42) }
    }
  }
}