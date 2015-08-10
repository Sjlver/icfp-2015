#!/bin/bash

# Invokes our solution on a file, and submits this to the server

set -e

SCRIPT_DIR="$( dirname "${BASH_SOURCE[0]}" )"
WORKING_DIR="$( pwd )"

API_TOKEN="s+yzAvnSP4yRZZYjzBnWDQ+Rgk1RNitp0fRqpBBgO18="
TEAM_ID=242

if [ $# -lt 2 ]; then
  echo "usage: submitter.sh <problemfile> <tag> <other arguments>" >&2
  exit 1
fi
inputfile="$WORKING_DIR/$1"
tag="$2"
shift; shift

input_id="$( basename "$inputfile" .json )"

run_ai() {
  printf "Running AI: run-main Main $*" >&2
  result="$( cd ArtificialIntelligence && sbt --warn "run-main Main $*" )"
  printf " ... done.\n" >&2
  echo "Result: $result" >&2
  echo >&2
  echo "$result"
}

submit() {
  data="$1"

  curl --user :$API_TOKEN -X POST -H "Content-Type: application/json" -d "$data" \
    https://davar.icfpcontest.org/teams/$TEAM_ID/solutions
  echo; echo
}

cd "$SCRIPT_DIR/.."

(
  data="$( run_ai -f "$inputfile" -tag "$tag" "$@" )"
  submit "$data"
) 2>&1 | tee "$SCRIPT_DIR/../logs/$tag-$input_id.log"
