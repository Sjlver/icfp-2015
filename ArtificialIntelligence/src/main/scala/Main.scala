object Main {
  def main(args: Array[String]): Unit = {
    if (args.size < 2)
      println("Please specify input file")
    else {
      val board = new Board()
      board.fromJson(scala.io.Source.fromFile(args(1)).getLines.mkString)

      val aiRunner = new AIRunner(
        board,
        b => new SamplingAI(b),
        c => new PowerPhraseEncoder(c),
        "AIRunnerSpec")

      val result = aiRunner.run().prettyPrint
      println(result)
    }
  }
}
