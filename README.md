ICFP 2015, Team int4_t
======================

Setup
-----

ArtificialIntelligence:

- Install Scala IDE from <http://scala-ide.org>
- Install the ScalaTest Plugin (help > Install new Software > select the Scala
  IDE update site > Scala IDE plugins > ScalaTest
- Install sbt (`brew install sbt`, or similar)
- Generate an eclipse project file with `sbt eclipse`

Submitting solutions
--------------------

    for i in qualifying_problems/problem_*; do ./submitter/submitter.sh $i -tag someTag; done
