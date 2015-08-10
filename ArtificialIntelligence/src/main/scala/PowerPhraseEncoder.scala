// Encodes a sequence of moves into a string, trying to maximize the power.
class PowerPhraseEncoder(moves: Iterable[Moves.Move], phrasesIterable: Iterable[String]) {
  val phrases = phrasesIterable.filter({phrase => phrase forall (Moves.isValidMoveChar(_))}).toArray
  val movePhrases = phrases map {phrase =>
    phrase map {c => Moves.fromChar(c)}}
  
  def MoveChars(move: Moves.Move) :String = move match {
    case Moves.W => "p!.03\'P"
    case Moves.E => "bcefy2BCEFY"
    case Moves.SW => "aghij4AGHIJ"
    case Moves.SE => "lmno 5LMNO"
    case Moves.CW => "dqrvz1DQRVZ"
    case Moves.CCW => "kstuwxKSTUWX"
  }
    
  val movePhrasesStr = movePhrases map {ms => (ms map {move => MoveChars(move)(0)}).mkString}
  val movesStr = (moves map {move => MoveChars(move)(0)}).mkString
  
  def findPhraseStartingWith(move : Moves.Move): Int = {
    return movePhrases indexWhere (_(0) == move)
  }
  
  def encode(): String = {
    var result = movesStr
    for (i <- 0 until (movePhrasesStr.length)) {
      val expr = movePhrasesStr(i).r
      result = expr replaceAllIn(result, phrases(i))
    }
    result
  }
}