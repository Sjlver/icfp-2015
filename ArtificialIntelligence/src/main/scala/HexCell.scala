// A cell in a hexagonal grid.
// Inspired by this wonderful page: http://www.redblobgames.com/grids/hexagons/

// Davar uses an "odd-r" coordinate system, with x and y coordinates.
// Most computations are done in a "cube" coordinate system, with cx, cy, and cz coordinates.
// This class internally stores "axial" coordinates q (= cx) and r (= cz).
// Be careful when you use the case class constructor, it expects (q, r).
// You probably want to call fromXY instead.
object HexCell {
  def fromXY(x: Int, y: Int): HexCell = {
    val cx = x - (y - (y&1)) / 2
    val cz = y
    HexCell(cx, cz)
  }
}

case class HexCell(q: Int, r: Int) {
  // convert to cube coordinates
  def cx = q
  def cy = -q-r
  def cz = r
  
  // convert to odd-r coordinates
  def x = cx + (cz - (cz&1)) / 2
  def y = cz
  
  def translated(offset: HexCell) = HexCell(q + offset.q, r + offset.r)
  def rotated(angle: Int) = {
    val bracketedAngle = angle % 6 + (if (angle < 0) 6 else 0)
    bracketedAngle match {
      case 0 => this
      case 1 => HexCell(-cy, -q)
      case 2 => HexCell(r, cy)
      case 3 => HexCell(-q, -r)
      case 4 => HexCell(cy, q)
      case 5 => HexCell(-r, -cy)
      case _ => throw new AssertionError("Impossible modulus result: " + angle)
    }  
  }
  def negated = HexCell(-q, -r)
}