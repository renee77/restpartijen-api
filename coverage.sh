#!/usr/bin/env sh
# Measures test coverage of the server (NFR-03) and renders the JaCoCo HTML report.
# The JaCoCo agent appends to jacoco.exec on every test run, so the old file is removed
# first; otherwise the report would also count earlier runs.
set -e
cd "$(dirname "$0")"
rm -f build/maven-target/jacoco.exec
# The report task runs the server tests itself first; `./kotlin test` alone does not render the report.
./kotlin task :server:jacoco-maven-plugin.report
echo "Coverage report: build/maven-target/reports/jacoco/index.html"
