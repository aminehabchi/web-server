#!/usr/bin/env bash
set -e

ROOT="$(cd "$(dirname "$0")" && pwd)"

echo "==> Building framework..."
mvn -f "$ROOT/framework/pom.xml" install -q

echo "==> Compiling test-project..."
mvn -f "$ROOT/test-project/pom.xml" compile -q

echo "==> Running test-project..."
mvn -f "$ROOT/test-project/pom.xml" exec:java -Dexec.mainClass=com.app.Main
