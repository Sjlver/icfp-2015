
object Options {
  var inputFname = ""
  var timeLimitSeconds = -1
  var memory = -1
  var cores = -1
  var phrases :Seq[String] = Seq()
  var outputFname = ""
  var nMoves = -1
  var nRepetitions = 1
  var tag = "int4_t"
  var verbose = false

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
        case "-v" :: tail =>
          verbose = true
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
          if (timeLimitSeconds != -1) usage("Hmm... got more than one time bounds")
          timeLimitSeconds = t.toInt
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

  def log(message: String) = {
    if (verbose) System.err.println(message)
  }
}
