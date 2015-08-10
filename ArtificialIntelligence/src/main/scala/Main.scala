import java.io.{File, FileWriter, BufferedWriter}
import scala.collection.mutable.ArrayBuffer
import spray.json._

object Main {

  def main(args: Array[String]): Unit = {
    Options.parseArgs(args)

    if (Options.inputFiles.isEmpty) return Options.usage("Must specify an input file!")
    Options.timeLimitSeconds /= Options.inputFiles.size

    val allResults = ArrayBuffer.empty[JsValue]

    Options.inputFiles.foreach { inputFile =>
      val board = Board.fromJson(scala.io.Source.fromFile(inputFile).getLines.mkString)
      val aiRunner = new AIRunner(
        board,
        (b, endMillis) => new SamplingAI(b, endMillis),
        c => new PowerPhraseEncoder(c, Options.phrasesOfPower),
        new GameToJsonPrinter(Options.guiJsonOutputFile),
        Options.tag,
        Options.timeLimitSeconds)

      val result = aiRunner.run()
      allResults ++= result.elements
    }

    println(JsArray(allResults: _*).prettyPrint)
  }
}
