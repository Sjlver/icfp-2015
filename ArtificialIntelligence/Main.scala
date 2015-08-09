import java.nio.file.{Paths, Files}
import java.nio.charset.StandardCharsets

object SolveIt {
  def main(args: Array[String]): Unit = {
    if (args.length < 3)
      println("Please specify input file, number of moves and output file")
    else {
      var board = new Board()
      board.fromJson(scala.io.Source.fromFile(args(0)).getLines.reduceLeft(_+_))
      board.startNewGame
      var configs = "configurations = [";
      var i = 0
      for (i <- 1 to args(1).toInt) {
        configs += board.toJsonObject.compactPrint + ", "
      }
      configs += "];";
      Files.write(Paths.get(args(2)), configs.getBytes(StandardCharsets.UTF_8));
    }
  }
}
