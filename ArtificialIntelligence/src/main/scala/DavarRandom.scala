// The linear congruential generator from the problem specification
class DavarRandom(var seed: Int) {
  def next = {
    val result = (seed >>> 16) & ((1 << 15) - 1)
    seed = seed * 1103515245 + 12345
    result
  }
}