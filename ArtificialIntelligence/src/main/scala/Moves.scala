import scala.util.Random

object Moves {
  sealed abstract class Move
  case object E extends Move
  case object W extends Move
  case object SE extends Move
  case object SW extends Move
  case object CW extends Move
  case object CCW extends Move

  def fromChar(c: Char): Move = c match {
    case 'p' | '!' | '.' | '0' | '3' | '\'' => Moves.W
    case 'b' | 'c' | 'e' | 'f' | 'y' | '2'  => Moves.E
    case 'a' | 'g' | 'h' | 'i' | 'j' | '4'  => Moves.SW
    case 'l' | 'm' | 'n' | 'o' | ' ' | '5'  => Moves.SE
    case 'd' | 'q' | 'r' | 'v' | 'z' | '1'  => Moves.CW
    case 'k' | 's' | 't' | 'u' | 'w' | 'x'  => Moves.CCW
    case _ => throw new AssertionError("Illegal character: '" + c + "'")
  }

  def isValidMoveChar(c: Char): Boolean = c match {
    case 'p' | '!' | '.' | '0' | '3' | '\'' |
         'b' | 'c' | 'e' | 'f' | 'y' | '2'  |
         'a' | 'g' | 'h' | 'i' | 'j' | '4'  |
         'l' | 'm' | 'n' | 'o' | ' ' | '5'  |
         'd' | 'q' | 'r' | 'v' | 'z' | '1'  |
         'k' | 's' | 't' | 'u' | 'w' | 'x'  => true
    case _ => false
  }

  val ALL_MOVES = Array(Moves.E, Moves.W, Moves.SE, Moves.SW, Moves.CW, Moves.CCW)

  def randomMove(): Moves.Move = {
    ALL_MOVES(Random.nextInt(ALL_MOVES.size))
  }

}
