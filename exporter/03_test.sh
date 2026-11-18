#!/usr/bin/env bash
# 03 — run the exporter's pure-function unit tests (ordering, slugs, date ranges).
# No DB needed. jbang handles Java 21 and JUnit deps (//JAVA 21, //DEPS in the test file).
set -euo pipefail
cd "$(dirname "$0")"

command -v jbang >/dev/null 2>&1 || { echo "ERROR: jbang not found (sdk install jbang)."; exit 1; }

echo "==> Running exporter unit tests ..."
jbang ExportContentTest.java
