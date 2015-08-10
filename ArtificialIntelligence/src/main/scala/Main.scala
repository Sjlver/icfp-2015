import java.io.{File, FileWriter, BufferedWriter}
import scala.collection.mutable.ArrayBuffer

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
      var boardWriter :Option[java.io.Writer] = None
      if (outputFname != "") {
        val out = new File(outputFname)
        boardWriter = Some(new BufferedWriter(new FileWriter(out)))
      }
      val boardStateDumper = boardWriter match {
        case None => {(b: Board, c: ArrayBuffer[Moves.Move]) => }
        case Some(writer) => (b:Board, commands: ArrayBuffer[Moves.Move]) => {
          writer.write("var configurations = [")
          writer.write(b.toJsonObject.compactPrint)
          writer.write(", ")
            for (command <- commands){
              b.doMove(command)
              writer.write(b.toJsonObject.compactPrint)
              writer.write(", ")
            }
          }
      }
      val aiRunner = new AIRunner(
        board,
        b => new SamplingAI(b),
        c => new PowerPhraseEncoder(c, Array[String]()),
        boardStateDumper,
        tag)
      
      val result = aiRunner.run().prettyPrint
      println(result)
      
      boardWriter foreach {writer => writer.write("];"); writer.close()}
    }
  }
}
