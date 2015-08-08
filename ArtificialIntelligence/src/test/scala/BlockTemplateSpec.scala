import spray.json._

class BlockTemplateSpec extends UnitSpec {
  "A BlockTemplateSpec" should "initialize itself from a JsonObject" in {
    val jsonObject = """{
      "members":[{"x":0,"y":0},{"x":2,"y":0}],
      "pivot":{"x":1,"y":0}}""".parseJson.asJsObject
    val blockTemplate = BlockTemplate.fromJsonObject(jsonObject)
    
    blockTemplate.members should be (Array((-1, 0), (1, 0)))
  }  
}