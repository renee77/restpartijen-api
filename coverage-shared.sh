#!/usr/bin/env sh
# Measures test coverage of the shared module (the app) and renders the JaCoCo HTML report.
# The Kotlin Toolchain runs Maven plugins only for JVM modules, so the server's JaCoCo setup
# (coverage.sh) does not work for shared, a kmp/lib module. Instead this script attaches the
# JaCoCo agent through `./kotlin test --jvm-args` and renders the report with the JaCoCo CLI.
set -e
cd "$(dirname "$0")"

JACOCO_VERSION=0.8.15
TOOLS=build/coverage-tools
EXEC=build/coverage/shared.exec
REPORT=build/coverage/shared

# Downloads a JaCoCo jar from Maven Central once and checks it against the published SHA-1.
fetch() {
    url="https://repo1.maven.org/maven2/org/jacoco/$1/$JACOCO_VERSION/$1-$JACOCO_VERSION-$2.jar"
    target="$TOOLS/$1.jar"
    if [ ! -f "$target" ]; then
        mkdir -p "$TOOLS"
        curl -sSfL -o "$target" "$url"
        expected="$(curl -sSfL "$url.sha1")"
        actual="$(sha1sum "$target" | cut -d' ' -f1)"
        if [ "$expected" != "$actual" ]; then
            rm -f "$target"
            echo "Checksum mismatch for $1" >&2
            exit 1
        fi
    fi
}
fetch org.jacoco.agent runtime
fetch org.jacoco.cli nodeps

# The agent appends to the exec file, so remove the old one; otherwise earlier runs would count too.
rm -f "$EXEC"
# Absolute paths: the test JVM does not start in the project directory.
./kotlin test -m shared --jvm-args="-javaagent:$(pwd)/$TOOLS/org.jacoco.agent.jar=destfile=$(pwd)/$EXEC"

java -jar "$TOOLS/org.jacoco.cli.jar" report "$EXEC" \
    --classfiles build/tasks/_shared_jarAndroidDebug/shared-jvm.jar \
    --sourcefiles shared/src \
    --sourcefiles shared/src@android \
    --html "$REPORT"
echo "Coverage report: $REPORT/index.html"
