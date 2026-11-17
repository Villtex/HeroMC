#!/bin/sh
# Predicts the 1.5.2 terrain for a seed without starting the game, for tools/verify_blocks.py.
#   tools/expect.sh <seed> find <radius-in-chunks> > expect.txt   (one chunk per biome found near 0,0)
#   tools/expect.sh <seed> structures <radius-in-chunks> > expect.txt   (chunks of villages, strongholds, temples, mineshafts)
#   tools/expect.sh <seed> <chunkX> <chunkZ> [...]   > expect.txt
#   tools/expect.sh <seed> spawn                     (the world spawn)
# "vanilla" after the seed leaves out the biomes of mods (to compare with a vanilla 1.5.2 world).
# Compiles hexxit.worldgen152.core, the ExtrabiomesXL biome table and the core classes of the mod generators with plain javac (JDK 21+).
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
  $(find "$ROOT/mods/ebxl/src/main/java/extrabiomes/gen" -name '*.java') \
  $(find "$ROOT/mods/hexxitgear/src/main/java/sct/hexxitgear/gen" -name '*.java') \
  $(find "$ROOT/mods/natura/src/main/java/natura/gen" -name '*.java') \
  $(find "$ROOT/mods/offlawn/src/main/java/offlawn/gen" -name '*.java') \
  $(find "$ROOT/mods/wildcaves3/src/main/java/wildcaves/gen" -name '*.java') \
  $(find "$ROOT/mods/cofhcore/src/main/java/cofh/gen" -name '*.java') \
  $(find "$ROOT/mods/artifice/src/main/java/artifice/gen" -name '*.java') \
  $(find "$ROOT/mods/harkenscythe/src/main/java/harkenscythe/gen" -name '*.java') \
  $(find "$ROOT/mods/projectzulu/src/main/java/projectzulu/gen" -name '*.java') \
  "$ROOT/tools/java/Expect.java"
"$JAVA" -cp "$OUT/classes" Expect "$@"
