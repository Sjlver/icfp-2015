

class PowerPhraseEncoderSpec extends UnitSpec{
  "A PowerPhraseEncoder" should "produces specific letters" in {
    val moves = Array(
      Moves.SW, Moves.W, Moves.CCW, Moves.SE, Moves.CW, Moves.SE, Moves.SE, Moves.SW, Moves.SW, Moves.W, 
      Moves.SE, Moves.SE, Moves.CCW, Moves.SE, Moves.W, Moves.CCW, Moves.CCW, Moves.W, Moves.SE, Moves.CW, 
      Moves.W, Moves.CCW, Moves.E, Moves.SE, Moves.CW, Moves.SW, Moves.CCW, Moves.E, Moves.W, Moves.SE, 
      Moves.E, Moves.E, Moves.CCW, Moves.SW, Moves.SE, Moves.W, Moves.E, Moves.CCW, Moves.SW, Moves.W, 
      Moves.E, Moves.W, Moves.SW, Moves.CCW, Moves.SE, Moves.E, Moves.E, Moves.E, Moves.W, Moves.SW, 
      Moves.E, Moves.W, Moves.SW, Moves.SE, Moves.SE, Moves.SW, Moves.CW, Moves.CCW, Moves.SW, Moves.E, 
      Moves.SE, Moves.SE, Moves.CW, Moves.E, Moves.SE, Moves.SE, Moves.SE, Moves.CW, Moves.CW, Moves.W, 
      Moves.CW, Moves.E, Moves.CCW, Moves.E, Moves.CW, Moves.CW, Moves.SE, Moves.CW, Moves.SE, Moves.SE, 
      Moves.SE, Moves.E, Moves.CCW, Moves.SE, Moves.CW, Moves.SW, Moves.E, Moves.CW, Moves.W, Moves.SW, 
      Moves.W, Moves.SW, Moves.CW, Moves.SE, Moves.SE, Moves.CCW, Moves.SE, Moves.CW, Moves.W, Moves.E)
    
    val voidEncoder = new PowerPhraseEncoder(moves, Array[String]("dfvd"))
    for (c <- voidEncoder.encode) {
      Moves.isValidMoveChar(c) should be (true)
    }
    voidEncoder.encode map {Moves.fromChar(_)} should be (moves)
    val threePhrasesEncoder = new PowerPhraseEncoder(moves,
                                                     Array("first phrase11", 
                                                           "Ei!", 
                                                           "aaOnther onex#!@343"))
    for (c <- threePhrasesEncoder.encode) {
      Moves.isValidMoveChar(c) should be (true)
    }
    threePhrasesEncoder.encode map {Moves.fromChar(_)} should be (moves)
  }

  "A PowerPhraseEncoder" should "captures an exact power phrase" in {
    val phrase = "Aleshka molodec!"
    val moves = phrase.map ({c => Moves.fromChar(c)})
    val encoder = new PowerPhraseEncoder(moves, Array(phrase))
    encoder.encode should be (phrase)
    encoder.encode map {c => Moves.fromChar(c)} should be (moves)
  }
  
  "A PowerPhraseEncoder" should "captures an existing power phrase" in {
    val phrase = "Nola"
    val moves = ("! !" + phrase + "lis").map {c => Moves.fromChar(c)}
    val encoder = new PowerPhraseEncoder(moves, Array(phrase))
    encoder.encode contains phrase should be (true)
    encoder.encode map {c => Moves.fromChar(c)} should be (moves)
  }
  
  "A PowerPhraseEncoder" should "captures an existing whole power phrase," +
                                 "when fractions present" in {
    val phrase = "Nola"
    val moves = ("No" + phrase + "lis").map {c => Moves.fromChar(c)}
    val encoder = new PowerPhraseEncoder(moves, Array(phrase))
    encoder.encode contains phrase should be (true)
    encoder.encode map {c => Moves.fromChar(c)} should be (moves)
  }
  
  "A PowerPhraseEncoder" should "prefers a longer power phrase over a shorter one" in {
    val longPhrase = "olololo"
    val shortPhrase = "mnm" //m, o, l all correspond to the same moves
    val moves = longPhrase.map(Moves.fromChar)
    val encoder1 = new PowerPhraseEncoder(moves, Array(shortPhrase, longPhrase))
    encoder1.encode contains longPhrase should be (true)
    encoder1.encode map(Moves.fromChar) should be (moves)
    
    val encoder2 = new PowerPhraseEncoder(moves, Array(longPhrase, shortPhrase))
    encoder2.encode contains longPhrase should be (true)
    encoder2.encode map(Moves.fromChar) should be (moves)
  }
  
  "A PowerPhraseEncoder" should "compute the score for non-overlapping phrases" in {
    val phrase1 = "lam"
    val moves1 = phrase1.map(Moves.fromChar)
    val encoder1 = new PowerPhraseEncoder(moves1, Array(phrase1))
    encoder1.score should be (2*phrase1.length + 300)
  }
  
  "A PowerPhraseEncoder" should "compute the score of mutually overlapping phrases" in {
    val phrase1 = "mm"
    val phrase2 = "mmo"
    val phrase3 = "mmomm"
    val moves = phrase3.map(Moves.fromChar)
    val encoder = new PowerPhraseEncoder(moves, Array(phrase1, phrase2, phrase3))
    encoder.score should be (2*(2*phrase1.length + phrase2.length + phrase3.length) + 3*300)
  }
  
  "A PowerPhraseEncoder" should "compute the score of self overlapping phrase" in {
    val phrase1 = "lll"
    val moves1 = "lllll".map(Moves.fromChar)
    val encoder1 = new PowerPhraseEncoder(moves1, Array(phrase1))
    encoder1.score should be (2*(3*phrase1.length) + 300)
    
    val phrase2 = "mmm"
    val moves2 = "mmmmm".map(Moves.fromChar)
    val encoder2 = new PowerPhraseEncoder(moves2, Array(phrase2))
    encoder2.score should be (2*(3*phrase2.length) + 300)
  }
  
  /*"A PowerPhraseEncoder" should "prefers a unique power phrase" in {
    
  }*/
  
  /*
  //TODO: actually mmmmmml is better than olololo in this case
  "A PowerPhraseEncoder" should "prefer a longer power phrase over a shorter one" in {
    val longPhrase = "olololo"
    val shortPhrase = "mm" //m, o, l all correspond to the same moves
    val moves = longPhrase.map(Moves.fromChar)
    val encoder1 = new PowerPhraseEncoder(moves, Array(shortPhrase, longPhrase))
    println(encoder1.encode)
    encoder1.encode contains longPhrase should be (true)
    encoder1.encode map(Moves.fromChar) should be (moves)
    
    val encoder2 = new PowerPhraseEncoder(moves, Array(longPhrase, shortPhrase))
    encoder2.encode contains longPhrase should be (true)
    encoder2.encode map(Moves.fromChar) should be (moves)
  }*/
}