class DavarRandomSpec extends UnitSpec {
  "A DavarRandom" should "generate the right sequence of random vars" in {
    val r = new DavarRandom(17)
    Array(0, 24107, 16552, 12125, 9427, 13152, 21440, 3383, 6873, 16117).foreach {
      expected => r.next should be (expected)
    }
  }
}