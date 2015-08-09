import java.io.File

object Main {

  var inputFname = ""
  var time = -1
  var memory = -1
  var cores = -1
  var phrases :Seq[String] = Seq()
  var outputFname = ""
  var nMoves = -1
  var tag = "int4_t"
  
  def parseArgs(list: List[String]): Boolean = {
      list match {
        case Nil => true
        case "-f" :: fname :: tail =>
          if (inputFname != "")
            println ("Hmm... got more than one input files. Whan should I do with it?")
          inputFname = fname
          parseArgs(tail)
        case "-out" :: fname :: tail =>
          outputFname = fname
          parseArgs(tail)
        case "-moves" :: n :: tail =>
          nMoves = n.toInt
          parseArgs(tail)
        case "-t" :: t :: tail =>
          if (time != -1)
            println ("Hmm... got more than one time bounds")
          time = t.toInt
          parseArgs(tail)
        case "-m" :: mem :: tail =>
          if (memory != -1)
            println ("Hmm... got more than one memory bounds")
          memory = mem.toInt
          parseArgs(tail)
        case "-c" :: c :: tail =>
          if (cores != -1)
            println ("Hmm... got more than one core numbers")
          cores = c.toInt
          parseArgs(tail)
        case "-p" :: phrase :: tail =>
          phrases = phrases :+ phrase
          parseArgs(tail)
        case "-tag" :: t :: tail =>
          tag = t
          parseArgs(tail)
        case _ =>
          println("Invalid option: " + list.toString)
          return false
      }
    
  } 
  
  def main(args: Array[String]): Unit = {
    if (args.length == 0) {
     println("please supply some options")
     return
    }
    val argList = args.toList
    if (!parseArgs(argList))
      println("Invalid options")
    else {
      if (inputFname == "") {
        println("Must specify an input file!")
        return
      }
      val board = Board.fromJson(scala.io.Source.fromFile(inputFname).getLines.mkString)
      val aiRunner = new AIRunner(
        board,
        b => new SamplingAI(b),
        c => new PowerPhraseEncoder(c),
        tag)

      val result = aiRunner.run().prettyPrint
      println(result)
    }
  }
}
