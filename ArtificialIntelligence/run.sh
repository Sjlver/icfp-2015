#!/bin/bash
sbt "run-main Main $*"
# Parse arguments
while [[ $# > 1 ]]
do
key="$1"

case $key in
    -out)
    OUTFILE="$2"
    shift # past argument
    ;;
    *)
            # unknown option
    ;;
esac
shift # past argument or value
done
if [ ! -z "$OUTFILE" ]; then
    cp "$OUTFILE" ../visualizer/game-states.json
    xdg-open ../visualizer/main.html
fi
