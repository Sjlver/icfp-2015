
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
      } else {
        startNewArg = true
      }
    }

    var argsList = mergedArgs.toList
    while (!argsList.isEmpty) {
      argsList = argsList match {
        case "-f" :: fname :: tail =>
          //System.err.println("input file: " + fname)
          inputFiles += fname
          tail
        case "-v" :: tail =>
          //System.err.println("verbose")
          verbose = true
          tail
        case "-out" :: fname :: tail =>
          //System.err.println("output file: " + fname)
          guiJsonOutputFile = fname
          tail
        case "-t" :: t :: tail =>
          //System.err.println("time limit: " + t.toInt)
          timeLimitSeconds = t.toInt
          tail
        case "-m" :: mem :: tail =>
          //System.err.println("mem limit: " + mem.toInt)
          memoryLimitMegabytes = mem.toInt
          tail
        case "-c" :: c :: tail =>
          //System.err.println("cores: " + c.toInt)
          numCores = c.toInt
          tail
        case "-p" :: phrase :: tail =>
          //System.err.println("phrase: " + phrase)
          phrasesOfPower += phrase
          tail
        case "-tag" :: t :: tail =>
          //System.err.println("tag: " + tag)
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
