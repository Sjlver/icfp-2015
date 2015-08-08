import spray.json._
import scala.collection.mutable.ArrayBuffer

object BlockTemplate {
  def fromJsonObject(jsonObject: JsObject): BlockTemplate = {
    // We ignore potential invalid JSON, hence the @unchecked.
    
    val transformedMembers = ArrayBuffer.empty[(Int, Int)]
    jsonObject.getFields("pivot") match { case Seq(JsObject(pivot)) =>
      ((pivot("x"), pivot("y")): @unchecked) match { case (JsNumber(px), JsNumber(py)) =>
        jsonObject.getFields("members") match { case Seq(JsArray(members)) =>
          members.foreach { memberObject => (memberObject: @unchecked) match {
            case JsObject(member) =>
              ((member("x"), member("y")): @unchecked) match { case (JsNumber(mx), JsNumber(my)) =>
                transformedMembers += ((mx.toInt - px.toInt, my.toInt - py.toInt))
              }
            }
          }
        }
      }
    }
    
    new BlockTemplate(transformedMembers.toArray)
  }
}

class BlockTemplate(val members: Array[(Int, Int)]) {
  
  // The number of different rotational positions of this block.
  // Depending on the block symmetry, this can be 1, 2, 3, or 6
  // TODO: issue #1
  val numRotations = 6
}