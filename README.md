ICFP 2015, Team int4_t
======================

int4_t: The team of four interns. Saving the world, one hexagonal cell at a
time.

There is a [blog post about this code][blogpost], where you can see a screencast
of the AI in action.

[blogpost]: https://blog.purpureus.net/technology/2015/08/11/monte-carlo-tetris-playing.html


Usage
-----

    ./play_icfp2015 -f qualifying_problems/problem6.json -t 150 -v


What might interest you about this code
---------------------------------------

Generating a solution is a two-step process:

1. Moves are generated using a Monte Carlo Tree Search algorithm
   (SamplingAI.scala).
2. Moves are translated into letters, while attempting to include as many
   phrases of power as possible, using a greedy algorithm
   (PowerPhraseEncoder.scala).

### Some nice tidbits

The time taken by the program is controlled by adjusting the number of playouts
(i.e., board evaluations) done in the Monte Carlo Tree Search (MCTS). The AI
measures how many playouts it can do in a given time, and estimates how many
moves remain. From this, it computes the number of playouts per move.

This approach is particularly elegant because, as a side effect of doing random
playouts, it is very easy to estimate the number of remaining moves.

Our playouts are very light. They are almost purely random. This is probably the
area where our AI could most easily be improved.


Setup Instructions for Developers
---------------------------------

ArtificialIntelligence:

- Install Scala IDE from <http://scala-ide.org>. The code needs version 2.11.7.
- Install the ScalaTest Plugin (Help > Install new Software > Select the Scala
  IDE update site > Scala IDE plugins > ScalaTest
- Install sbt (`brew install sbt`, or similar)
- Generate an Eclipse project file with `sbt eclipse`


Submitting solutions
--------------------

    for i in qualifying_problems/problem_*.json; do \
        ./submitter/submitter.sh $i 6cb4e56 -t 150 -v \
        -p 'Ei!' -p 'Ia! Ia!' -p 'Chthulu' -p 'Bigboote'; \
    done
