import spray.json._
import scala.collection.mutable.ArrayBuffer

object BlockTemplate {
  def fromJsonObject(jsonObject: JsObject): BlockTemplate = {
    // We ignore potential invalid JSON, hence the @unchecked.
    
    val membersWithPivotAtOrigin = ArrayBuffer.empty[HexCell]
    jsonObject.getFields("pivot", "members") match { case Seq(pivotObject: JsObject, JsArray(members)) =>
      val pivot = HexCell.fromJsonObject(pivotObject)
      members.foreach { memberObject => (memberObject: @unchecked) match {
        case memberObject: JsObject =>
          val member = HexCell.fromJsonObject(memberObject)
          membersWithPivotAtOrigin += member.translated(pivot.negated)
        }
      }
    }
    
    BlockTemplate(membersWithPivotAtOrigin.toArray)
  }
}

case class BlockTemplate(members: Array[HexCell]) {
  
  // The number of different rotational positions of this block.
  // Depending on the block symmetry, this can be 1, 2, 3, or 6
  val numRotations = {
    var result = 0

    // Try out all rotations, until we see a configuration that we've seen before.
    var rotationsSeen = scala.collection.mutable.Set.empty[Set[HexCell]]
    var rotatedMembers = members.toSet
    while (!rotationsSeen.contains(rotatedMembers)) {
      rotationsSeen += rotatedMembers
      result += 1
      rotatedMembers = rotatedMembers.map(_.rotated(1))
    }
    result
  }
}