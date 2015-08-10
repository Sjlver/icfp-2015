// Encodes a sequence of moves into a string, trying to maximize the power.
class PowerPhraseEncoder(moves: Iterable[Moves.Move], phrasesIterable: Iterable[String]) {
  val phrases = phrasesIterable.filter({phrase => phrase forall (Moves.isValidMoveChar(_))})
                .toArray
                .sortBy(-_.length)
  val movePhrases = phrases map (phrase =>  phrase map {c => Moves.fromChar(c)})
  
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
    val phraseReplacements = movePhrasesStr.zip(phrases)
    phraseReplacements.foldLeft(movesStr)({(str:String, pair) => 
        val (expr,repl) = pair; expr.r replaceAllIn(str, repl) })
  }
  //TODO: count overlapping phrases
  def score(): Int = {
    val str = encode()
    (phrases.map(phrase => phrase.r.findAllMatchIn(str).length * phrase.length) sum) * 2 +
    (phrases.map(phrase => if (phrase.r.findAllMatchIn(str).length > 0) 1; else 0).sum) * 300
  }
}