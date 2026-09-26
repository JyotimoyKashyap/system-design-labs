#!/usr/bin/env bash
set -e

# Build the native distribution
./gradlew installDist -q

# Run directly attached to terminal TTY
exec ./app/build/install/app/bin/app "$@"
