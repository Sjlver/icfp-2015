ICFP 2015, Team int4_t
======================

int4_t: The team of four interns. Saving the world, one hexagonal cell at a
time.


What Might Interest You About Our Solution
------------------------------------------

Generating a solution is a two-step process:

1. Moves are generated using a Monte Carlo Tree Search algorithm
   (SamplingAI.scala).
2. Moves are translated into letters, while attempting to include as many
   phrases of power as possible, using a greedy algorithm
   (PowerPhraseEncoder.scala).

### Some nice tidbits:

Runtime is controlled by adjusting the number of playouts (i.e., board
evaluations) done in the Monte Carlo Tree Search (MCTS). The AI measures how
many playouts it can do in a given time, and how many moves remain. From this,
it computes the number of playouts per move.

This approach is particularly elegant because playouts make it very easy to
estimate the number of remaining moves.

Our playouts are very light, they are almost purely random. This is probably the
area where our AI could most easily be improved.


Setup Instructions for Developers
---------------------------------

ArtificialIntelligence:

- Install Scala IDE from <http://scala-ide.org>. We need version 2.11.7.
- Install the ScalaTest Plugin (help > Install new Software > select the Scala
  IDE update site > Scala IDE plugins > ScalaTest
- Install sbt (`brew install sbt`, or similar)
- Generate an eclipse project file with `sbt eclipse`


Submitting solutions
--------------------

    for i in qualifying_problems/problem_*.json; do \
        ./submitter/submitter.sh $i 6cb4e56 -t 150 -v \
        -p 'Ei!' -p 'Io!' -p 'Chthulu' -p 'Bigboote'; \
    done
