lazy val root = (project in file(".")).
  settings(
    name := "ArtificialIntelligence",
    version := "1.0",
    scalaVersion := "2.11.7",
    libraryDependencies += "io.spray" %%  "spray-json" % "1.3.2", 
    libraryDependencies += "org.scalatest" % "scalatest_2.11" % "2.2.4" % "test",
    showSuccess := false
  )
