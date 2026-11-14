#!/bin/sh
# Runs the original Minecraft 1.5.2 server (Java 8) once, to get a world the port is compared with.
#   tools/original_server.sh <dir> <seconds> [java options]
# <dir> is a server directory under .cache/original/ holding the server jar as server.jar (and, for the
# Hexxit pack, its mods/, coremods/ and config/). The server starts there, runs <seconds>, then gets "stop".
# Java 8 (Amazon Corretto) is downloaded to .cache/original/jdk8 on first use. Nothing here is committed.
set -e
ROOT=$(cd "$(dirname "$0")/.." && pwd)
BASE="$ROOT/.cache/original"
DIR="$BASE/$1"
SECONDS_TO_RUN=${2:-120}
[ -f "$DIR/server.jar" ] || { echo "no server.jar in $DIR" >&2; exit 1; }
case "$1" in */*|.*|"") echo "directory name only" >&2; exit 1;; esac
shift 2 || true
if [ ! -x "$BASE/jdk8/bin/java" ]; then
  mkdir -p "$BASE/jdk8"
  curl -sSL -o "$BASE/jdk8.tgz" https://corretto.aws/downloads/latest/amazon-corretto-8-x64-linux-jdk.tar.gz
  tar xzf "$BASE/jdk8.tgz" -C "$BASE/jdk8" --strip-components=1
  rm "$BASE/jdk8.tgz"
fi
cd "$DIR"
unset JAVA_TOOL_OPTIONS
(sleep "$SECONDS_TO_RUN"; echo stop) | "$BASE/jdk8/bin/java" -Xmx2G "$@" -jar server.jar nogui > run.log 2>&1 || true
tail -n 3 run.log
