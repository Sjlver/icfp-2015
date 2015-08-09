// Encodes a sequence of moves into a string, trying to maximize the power.
class PowerPhraseEncoder(moves: Iterable[Moves.Move]) {
  def encode(): String = {
    moves.map {
    	move => move match {
      	case Moves.W => '!'
      	case Moves.E => 'e'
      	case Moves.SW => 'i'
      	case Moves.SE => 'l'
      	case Moves.CW => 'd'
      	case Moves.CCW => 'k'
    	}
    }.mkString
  }
}