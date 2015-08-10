all:
	cd ArtificialIntelligence && sbt compile

test:
	cd ArtificialIntelligence && sbt test

clean:
	cd ArtificialIntelligence && sbt clean
