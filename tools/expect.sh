#!/bin/sh
# Predicts the 1.5.2 terrain for a seed without starting the game, for tools/verify_blocks.py.
#   tools/expect.sh <seed> find <radius-in-chunks> > expect.txt   (one chunk per biome found near 0,0)
#   tools/expect.sh <seed> structures <radius-in-chunks> > expect.txt   (chunks of villages, strongholds, temples, mineshafts)
#   tools/expect.sh <seed> <chunkX> <chunkZ> [...]   > expect.txt
#   tools/expect.sh <seed> dungeons <radius-in-chunks> > expect.txt   (chunks of the nearest Better Dungeons dungeon of each type, chunk caves, wall castles)
#   tools/expect.sh <seed> ruins <count> [cx cz]     > expect.txt   (chunks of the first <count> Ruins ruins found around 0,0 or chunk cx,cz)
#   tools/expect.sh <seed> towers <count>            > expect.txt   (chunks of the first <count> Battle Towers around 0,0)
#   tools/expect.sh <seed> rifts <count>             > expect.txt   (chunks of the first <count> Dimensional Doors rift gateways and clusters around 0,0)
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
  $(find "$ROOT/mods/betterdungeons/src/main/java/betterdungeons/gen" -name '*.java') \
  $(find "$ROOT/mods/ruins/src/main/java/ruins/gen" -name '*.java') \
  $(find "$ROOT/mods/battletowers/src/main/java/battletowers/gen" -name '*.java') \
  $(find "$ROOT/mods/dimensional-doors/src/main/java/dimdoors/gen" -name '*.java') \
  $(find "$ROOT/mods/qcraft/src/main/java/qcraft/gen" -name '*.java') \
  "$ROOT/tools/java/Expect.java"
# Better Dungeons reads its templates from the original archives, which the build imports
BD="$ROOT/mods/betterdungeons/build/generated/original-assets"
[ -f "$BD/betterdungeons-original/Chocolate/.index" ] || (cd "$ROOT" && ./gradlew -q :betterdungeons:importOriginalAssets >&2)
# Ruins reads its templates and options from the original archives too
RU="$ROOT/mods/ruins/build/generated/original-assets"
[ -f "$RU/ruins-original/ruins/.index" ] || (cd "$ROOT" && ./gradlew -q :ruins:importOriginalAssets >&2)
# and Battle Towers its configuration
BT="$ROOT/mods/battletowers/build/generated/original-assets"
[ -f "$BT/battletowers-original/BattleTowers.cfg" ] || (cd "$ROOT" && ./gradlew -q :battletowers:importOriginalAssets >&2)
"$JAVA" -cp "$OUT/classes:$BD:$RU:$BT" Expect "$@"
