// Encodes a sequence of moves into a string, trying to maximize the power.
class PowerPhraseEncoder(moves: Iterable[Moves.Move], phrasesIterable: Iterable[String]) {
  val phrases = phrasesIterable.filter({phrase => phrase forall (Moves.isValidMoveChar(_))}).toArray
  val movePhrases = phrases map {phrase =>
    phrase map {c => Moves.fromChar(c)}}
  
  val MoveChars = Map[Moves.Move, String](
    Moves.W -> "p!.03\'P",
    Moves.E -> "bcefy2BCEFY",
    Moves.SW -> "aghij4AGHIJ",
    Moves.SE -> "lmno 5LMNO",
    Moves.CW -> "dqrvz1DQRVZ",
    Moves.CCW -> "kstuwxKSTUWX")
  
  def findPhraseStartingWith(move : Moves.Move): Int = {
    return movePhrases indexWhere (_(0) == move)
  }
  
  def encode(): String = {
    var result = ""
    var currentPhrase :Int = -1
    var inPhrasePosition = 0
    for(move <- moves){
      if (currentPhrase != -1) {
        val i = currentPhrase
        inPhrasePosition += 1
        if (inPhrasePosition < movePhrases(i).length &&
            move == movePhrases(i)(inPhrasePosition)) {
          result += phrases(i)(inPhrasePosition)
        } else {
          inPhrasePosition = 0
          currentPhrase = findPhraseStartingWith(move)
          if (currentPhrase != -1)
            result += phrases(currentPhrase)(0)
          else
            result += MoveChars(move)(0)
        }
      } else {
        currentPhrase = findPhraseStartingWith(move)
        inPhrasePosition = 0
        if (currentPhrase != -1)
          result += phrases(currentPhrase)(0)
        else
          result += MoveChars(move)(0)
      }
    }
    result
  }
}