#!/usr/bin/env bash
set -e

if [ ! -f "bin/assignment3/core/MainApplication.class" ]; then
    echo "Binaries not found. Running build.sh first..."
    bash build.sh
fi

if [ $# -eq 0 ]; then
    java -cp bin assignment3.core.MainApplication
else
    java -cp bin assignment3.core.MainApplication "$@"
fi
