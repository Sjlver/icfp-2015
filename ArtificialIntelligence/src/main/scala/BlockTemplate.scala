import spray.json._
import scala.collection.mutable.ArrayBuffer

object BlockTemplate {
  def fromJsonObject(jsonObject: JsObject): BlockTemplate = {
    val transformedMembers = ArrayBuffer.empty[(Int, Int)]
    jsonObject.getFields("pivot") match { case Seq(JsObject(pivot)) =>
      (pivot("x"), pivot("y")) match { case (JsNumber(px), JsNumber(py)) =>
        jsonObject.getFields("members") match { case Seq(JsArray(members)) =>
          members.foreach { case JsObject(member) =>
            (member("x"), member("y")) match { case (JsNumber(mx), JsNumber(my)) =>
              transformedMembers += ((mx.toInt - px.toInt, my.toInt - py.toInt))
            }
          }
        }
      }
    }
    
    new BlockTemplate(transformedMembers.toArray)
  }
}

class BlockTemplate(val members: Array[(Int, Int)]) {
}