#!/usr/bin/env bash
set -e

echo "==========================================================================="
echo "  Building Assignment 3 Core Application Suite (Linux / macOS)"
echo "==========================================================================="

mkdir -p bin

echo "Compiling Java source files..."
javac -d bin -sourcepath src $(find src -name "*.java")

echo "[BUILD SUCCESS] All classes compiled to bin/"
