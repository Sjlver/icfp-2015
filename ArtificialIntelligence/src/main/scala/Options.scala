
import scala.collection.mutable.ArrayBuffer

object Options {
  var inputFiles = ArrayBuffer.empty[String]
  var timeLimitSeconds = 60
  var memoryLimitMegabytes = -1
  var numCores = -1
  var phrasesOfPower = ArrayBuffer.empty[String]
  var guiJsonOutputFile: String = null
  var tag = "int4_t"
  var verbose = false

  def usage(message: String) {
    System.err.println("usage: sbt 'run-main Main -f <input> <other options>'")
    System.err.println(message)
    System.exit(1)
  }

  def parseArgs(args: Array[String]) {
    // We all love sbt. But it doesn't allow us to have spaces in arguments... whatever.
    // So here's the deal: our run script generates args that end in a backslash if
    // they contain a space. So, first of all, merge such args
    val mergedArgs = ArrayBuffer.empty[String]
    var startNewArg = true
    args.foreach { arg =>
      if (startNewArg) {
        mergedArgs += arg
      } else {
        mergedArgs(mergedArgs.size - 1) += arg
      }
      if (mergedArgs(mergedArgs.size - 1).endsWith("\\")) {
        mergedArgs(mergedArgs.size - 1) = mergedArgs(mergedArgs.size - 1).substring(0, mergedArgs.size - 1) + " "
        startNewArg = false
      }
    }

    var argsList = mergedArgs.toList
    while (!argsList.isEmpty) {
      argsList = argsList match {
        case "-f" :: fname :: tail =>
          inputFiles += fname
          tail
        case "-v" :: tail =>
          verbose = true
          tail
        case "-out" :: fname :: tail =>
          guiJsonOutputFile = fname
          tail
        case "-t" :: t :: tail =>
          timeLimitSeconds = t.toInt
          tail
        case "-m" :: mem :: tail =>
          if (memoryLimitMegabytes != -1) usage("Hmm... got more than one memory bounds")
          memoryLimitMegabytes = mem.toInt
          tail
        case "-c" :: c :: tail =>
          if (numCores != -1) usage("Hmm... got more than one core numbers")
          numCores = c.toInt
          tail
        case "-p" :: phrase :: tail =>
          phrasesOfPower += phrase
          tail
        case "-tag" :: t :: tail =>
          tag = t
          tail
        case other =>
          return usage("Invalid option: " + other)
      }
    }
  }

  def log(message: String) = {
    if (verbose) System.err.println(message)
  }
}
