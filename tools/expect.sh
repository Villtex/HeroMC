#!/bin/sh
# Predicts the 1.5.2 terrain for a seed without starting the game, for tools/verify_blocks.py.
#   tools/expect.sh <seed> find <radius-in-chunks> > expect.txt   (one chunk per biome found near 0,0)
#   tools/expect.sh <seed> structures <radius-in-chunks> > expect.txt   (chunks of villages, strongholds, temples, mineshafts)
#   tools/expect.sh <seed> <chunkX> <chunkZ> [...]   > expect.txt
#   tools/expect.sh <seed> spawn                     (the world spawn)
# "vanilla" after the seed leaves out the biomes of mods (to compare with a vanilla 1.5.2 world).
# Compiles hexxit.worldgen152.core and the ExtrabiomesXL biome table with plain javac (JDK 21+).
set -e
ROOT=$(cd "$(dirname "$0")/.." && pwd)
OUT=$(mktemp -d)
trap 'rm -rf "$OUT"' EXIT
mkdir -p "$OUT/stub/extrabiomes"
# EbxlBiomes only needs the mod id from the (game-dependent) mod class.
printf 'package extrabiomes;\npublic final class Extrabiomes { public static final String MOD_ID = "extrabiomesxl"; }\n' \
  > "$OUT/stub/extrabiomes/Extrabiomes.java"
JAVAC=${JAVA_HOME:+$JAVA_HOME/bin/}javac
JAVA=${JAVA_HOME:+$JAVA_HOME/bin/}java
"$JAVAC" -nowarn -d "$OUT/classes" \
  $(find "$ROOT/mods/worldgen152/src/main/java/hexxit/worldgen152/core" -name '*.java') \
  "$ROOT/mods/ebxl/src/main/java/extrabiomes/EbxlBiomes.java" "$OUT/stub/extrabiomes/Extrabiomes.java" \
  "$ROOT/tools/java/Expect.java"
"$JAVA" -cp "$OUT/classes" Expect "$@"
