import spray.json._

class BlockTemplateSpec extends UnitSpec {
  "A BlockTemplate" should "initialize itself from a JsonObject" in {
    val jsonObject = """{
      "members":[{"x":0,"y":0},{"x":2,"y":0}],
      "pivot":{"x":1,"y":0}}""".parseJson.asJsObject
    val blockTemplate = BlockTemplate.fromJsonObject(jsonObject)
    
    blockTemplate.members should be (Array(HexCell.fromXY(-1, 0), HexCell.fromXY(1, 0)))
  }  

  "A BlockTemplate" should "know its symmetries (two cells with center pivot)" in {
    val jsonObject = """{
      "members":[{"x":0,"y":0},{"x":2,"y":0}],
      "pivot":{"x":1,"y":0}}""".parseJson.asJsObject
    val blockTemplate = BlockTemplate.fromJsonObject(jsonObject)
    blockTemplate.numRotations should be (3)
  }
  
  "A BlockTemplate" should "know its symmetries (single cell centered on pivot)" in {
    val jsonObject = """{
      "members":[{"x":0,"y":0}],
      "pivot":{"x":0,"y":0}}""".parseJson.asJsObject
    val blockTemplate = BlockTemplate.fromJsonObject(jsonObject)
    blockTemplate.numRotations should be (1)
  }  

  "A BlockTemplate" should "know its symmetries (single cell off-center)" in {
    val jsonObject = """{
      "members":[{"x":0,"y":0}],
      "pivot":{"x":1,"y":0}}""".parseJson.asJsObject
    val blockTemplate = BlockTemplate.fromJsonObject(jsonObject)
    blockTemplate.numRotations should be (6)
  }  

  "A BlockTemplate" should "know its symmetries (triangle)" in {
    val jsonObject = """{
      "members":[{"x":0,"y":1},{"x":2,"y":0},{"x":2,"y":2}],
      "pivot":{"x":1,"y":1}}""".parseJson.asJsObject
    val blockTemplate = BlockTemplate.fromJsonObject(jsonObject)
    blockTemplate.numRotations should be (2)
  }

}