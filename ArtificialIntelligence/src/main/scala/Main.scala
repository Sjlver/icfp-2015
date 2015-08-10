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
  var nRepetitions = 1
  var tag = "int4_t"

  def usage(message: String) {
    System.err.println("usage: sbt 'run-main Main -f <input> <other options>'")
    System.err.println(message)
    System.exit(1)
  }

  def parseArgs(list: List[String]) {
      list match {
        case Nil => Unit
        case "-f" :: fname :: tail =>
          if (inputFname != "") usage("Hmm... got more than one input files. Whan should I do with it?")
          inputFname = fname
          parseArgs(tail)
        case "-out" :: fname :: tail =>
          outputFname = fname
          parseArgs(tail)
        case "-moves" :: n :: tail =>
          nMoves = n.toInt
          parseArgs(tail)
        case "-repetitions" :: n :: tail =>
          nRepetitions = n.toInt
          parseArgs(tail)
        case "-t" :: t :: tail =>
          if (time != -1) usage("Hmm... got more than one time bounds")
          time = t.toInt
          parseArgs(tail)
        case "-m" :: mem :: tail =>
          if (memory != -1) usage("Hmm... got more than one memory bounds")
          memory = mem.toInt
          parseArgs(tail)
        case "-c" :: c :: tail =>
          if (cores != -1) usage("Hmm... got more than one core numbers")
          cores = c.toInt
          parseArgs(tail)
        case "-p" :: phrase :: tail =>
          phrases = phrases :+ phrase
          parseArgs(tail)
        case "-tag" :: t :: tail =>
          tag = t
          parseArgs(tail)
        case _ =>
          usage("Invalid option: " + list.toString)
      }
  }

  def main(args: Array[String]): Unit = {
    parseArgs(args.toList)

    if (inputFname == "") return usage("Must specify an input file!")

    var sumScores = 0
    0.to(nRepetitions - 1).foreach { repetition =>
      val board = Board.fromJson(scala.io.Source.fromFile(inputFname).getLines.mkString)
      val aiRunner = new AIRunner(
        board,
        b => new SamplingAI(b),
        c => new PowerPhraseEncoder(c, Array[String]()),
        new GameToJsonPrinter(outputFname),
        tag)

      val result = aiRunner.run().prettyPrint
      println(result)

      sumScores += aiRunner.sumScores
    }

    System.err.println("Average score: " + sumScores / nRepetitions)
  }
}
