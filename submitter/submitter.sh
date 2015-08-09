#!/bin/bash

# Invokes our solution on a file, and submits this to the server

set -e

SCRIPT_DIR="$( dirname "${BASH_SOURCE[0]}" )"
API_TOKEN="s+yzAvnSP4yRZZYjzBnWDQ+Rgk1RNitp0fRqpBBgO18="
TEAM_ID=242

run_solution() {
  (cd ArtificialIntelligence && sbt --warn "run Main $@")
}

submit() {
  curl --user :$API_TOKEN -X POST -H "Content-Type: application/json" -d "$1" \
    https://davar.icfpcontest.org/teams/$TEAM_ID/solutions
}

cd "$SCRIPT_DIR/.."

submit "$( run_solution "$@" )"
