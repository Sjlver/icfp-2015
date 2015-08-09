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
    case 'P'                                => Moves.W
    case 'b' | 'c' | 'e' | 'f' | 'y' | '2'  => Moves.E
    case 'B' | 'C' | 'E' | 'F' | 'Y'        => Moves.E
    case 'a' | 'g' | 'h' | 'i' | 'j' | '4'  => Moves.SW
    case 'A' | 'G' | 'H' | 'I' | 'J'        => Moves.SW
    case 'l' | 'm' | 'n' | 'o' | ' ' | '5'  => Moves.SE
    case 'L' | 'M' | 'N' | 'O'              => Moves.SE
    case 'd' | 'q' | 'r' | 'v' | 'z' | '1'  => Moves.CW
    case 'D' | 'Q' | 'R' | 'V' | 'Z'        => Moves.CW
    case 'k' | 's' | 't' | 'u' | 'w' | 'x'  => Moves.CCW
    case 'K' | 'S' | 'T' | 'U' | 'W' | 'X'  => Moves.CCW
    case _ => throw new AssertionError("Illegal character: '" + c + "'")
  }

  def isValidMoveChar(c: Char): Boolean = c match {
    case 'p' | '!' | '.' | '0' | '3' | '\'' |
         'P' |
         'b' | 'c' | 'e' | 'f' | 'y' | '2'  |
         'B' | 'C' | 'E' | 'F' | 'Y' |
         'a' | 'g' | 'h' | 'i' | 'j' | '4'  |
         'A' | 'G' | 'H' | 'I' | 'J' |
         'l' | 'm' | 'n' | 'o' | ' ' | '5'  |
         'L' | 'M' | 'N' | 'O' |
         'd' | 'q' | 'r' | 'v' | 'z' | '1'  |
         'D' | 'Q' | 'R' | 'V' | 'Z' |
         'k' | 's' | 't' | 'u' | 'w' | 'x'  |
         'K' | 'S' | 'T' | 'U' | 'W' | 'X'  => true
    case _ => false
  }

  val ALL_MOVES = Array(Moves.E, Moves.W, Moves.SE, Moves.SW, Moves.CW, Moves.CCW)

  def randomMove(): Moves.Move = {
    ALL_MOVES(Random.nextInt(ALL_MOVES.size))
  }

}
