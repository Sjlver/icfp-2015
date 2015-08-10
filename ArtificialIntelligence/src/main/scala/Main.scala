import java.io.{File, FileWriter, BufferedWriter}
import scala.collection.mutable.ArrayBuffer

object Main {

  def main(args: Array[String]): Unit = {
    Options.parseArgs(args.toList)

    if (Options.inputFname == "") return Options.usage("Must specify an input file!")

    var sumScores = 0
    0.to(Options.nRepetitions - 1).foreach { repetition =>
      val board = Board.fromJson(scala.io.Source.fromFile(Options.inputFname).getLines.mkString)
      val aiRunner = new AIRunner(
        board,
        (b, endMillis) => new SamplingAI(b, endMillis),
        c => new PowerPhraseEncoder(c, Options.phrases),
        new GameToJsonPrinter(Options.outputFname),
        Options.tag,
        Options.timeLimitSeconds)

      val result = aiRunner.run().prettyPrint
      println(result)

      sumScores += aiRunner.sumScores
    }

    System.err.println("Average score: " + sumScores / Options.nRepetitions)
  }
}
