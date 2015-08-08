import spray.json._
import scala.collection.mutable.ArrayBuffer

object BlockTemplate {
  def fromJsonObject(jsonObject: JsObject): BlockTemplate = {
    // We ignore potential invalid JSON, hence the @unchecked.
    
    val membersWithPivotAtOrigin = ArrayBuffer.empty[HexCell]
    jsonObject.getFields("pivot", "members") match { case Seq(JsObject(pivot), JsArray(members)) =>
      val px = (pivot("x"): @unchecked) match { case JsNumber(x) => x.toInt }
      val py = (pivot("y"): @unchecked) match { case JsNumber(y) => y.toInt }
      members.foreach { memberObject => (memberObject: @unchecked) match {
        case JsObject(member) =>
          val mx = (member("x"): @unchecked) match { case JsNumber(x) => x.toInt }
          val my = (member("y"): @unchecked) match { case JsNumber(y) => y.toInt }
          membersWithPivotAtOrigin += HexCell.fromXY(mx - px, my - py)
        }
      }
    }
    
    BlockTemplate(membersWithPivotAtOrigin.toArray)
  }
}

case class BlockTemplate(members: Array[HexCell]) {
  
  // The number of different rotational positions of this block.
  // Depending on the block symmetry, this can be 1, 2, 3, or 6
  // TODO: issue #1
  val numRotations = {
    6
  }
}