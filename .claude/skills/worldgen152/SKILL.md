---
name: worldgen152
description: "Beim Portieren der Weltgenerierung eines Hexxit-Mods benutzen: eigene Generatoren (IWorldGenerator), Erze, Bäume, Biome, Biom-Dekoratoren, Terrain-Events, Dungeon-Beute oder generierte Blöcke eines 1.5.2-Mods in mods/worldgen152 einhängen und mit tools/expect.sh und tools/verify_blocks.py prüfen."
---

# Weltgenerierung eines 1.5.2-Mods in worldgen152 einhängen

Die Welt wird nicht mit Features von heute erzeugt. `mods/worldgen152` rechnet sie in reinem Java so aus,
wie 1.5.2 es tat, und schreibt nur das Ergebnis in den Chunk. Ein Mod-Generator wird deshalb **als Java-Port
seines Originals in den Kern eingehängt**, nicht als `PlacedFeature` oder Biome Modifier.

## Aufbau

| Original | Hier (`hexxit.worldgen152.core.populate`) |
|---|---|
| `World` (was Generatoren davon brauchen) | `OldWorld` |
| `WorldGenerator` | `OldGen` |
| `ChunkProviderGenerate.populate` + `GameRegistry.generateWorld` | `OldPopulator` |
| `BiomeDecorator` | `OldDecorator` |
| `WorldGenMinable`, `WorldGenTrees`, ... | `OldOreGens`, `OldTreeGens`, `OldPlantGens`, `OldLakeGens`, `OldDungeonGen` |
| `ChestGenHooks`, `DungeonHooks` | `OldLoot` |
| `TerrainGen`-Events, `registerWorldGenerator` | `OldTerrainGen` |
| `Block`-Eigenschaften (Material, Lichtdurchlass) | `OldBlockInfo` |
| `MapGenStructure`, `StructureComponent`, `Component*` | `core.structure`: `OldStructures` und die Klassen mit den Namen des Originals |

Spielseite (`hexxit.worldgen152`): `OldBlockStates` (ID + Metadaten → Blockzustand), `OldGameIds` (alte Item-IDs,
Mob-Namen, Verzauberungen), `OldChunkWriter` (schreibt in den Chunk).

## Regel 1: Kern-Klassen ohne Spielklassen

Alles, was die Blöcke bestimmt, liegt in einer Klasse **ohne `net.minecraft`-Importe**, wie `EbxlBiomes` in
`mods/ebxl`. Nur so kann `tools/expect.sh` sie mit `javac` übersetzen und die Welt ohne Spiel vorhersagen.
Die Mod-Klasse ruft diese Registrierung im Konstruktor auf und meldet daneben die Blockzustände an.

Neuer Mod mit Weltgenerierung heißt deshalb auch: seine Kern-Klasse in `tools/expect.sh` (javac-Zeile) und den
`register()`-Aufruf in `tools/java/Expect.java` ergänzen. Sonst stimmt die Vorhersage nicht mit dem Server überein.

## Generator anmelden

```java
// IWorldGenerator: läuft nach der Vanilla-Population des Chunks
OldTerrainGen.registerWorldGenerator((random, chunkX, chunkZ, world) -> {
    int x = chunkX * 16 + random.nextInt(16);   // Koordinaten und Zufallsaufrufe 1:1 aus dem Original
    new OldOreGens.Minable(BLOCK_ID, META, 8, 1).generate(world, random, x, y, z);
});

// TerrainGen-Events; false = Result.DENY (Vanilla-Schritt fällt aus und zieht keine Zufallszahlen)
OldTerrainGen.addListener(new OldTerrainGen.Listener() {
    @Override public boolean generateOre(OldWorld w, Random r, int x, int z, OldTerrainGen.Ore type) { ... }
});
```

- **Reihenfolge = Ladereihenfolge der Mods im Original.** Alle Generatoren eines Chunks teilen sich *eine*
  Zufallsfolge (`fmlRandom`, einmal pro Chunk gesetzt). Ein Generator mehr oder in anderer Reihenfolge verschiebt
  alle folgenden. Die Reihenfolge des Originals ist noch nicht ermittelt; sie steht im Log des Original-Servers
  (Abgleich in Teil 2). Bis dahin die Reihenfolge wählen und im Plan unter „Offen“ notieren.
- Eigene `Random`-Objekte des Originals (`new Random(seed)`, `world.rand`) genau nachbauen. `world.rand` war nicht
  seed-fest: dafür einen festen Ersatz wählen und im Plan unter „Offen“ notieren.
- `MathHelper.sin/cos` → `OldMath.sin/cos` (Tabelle), `floor_double` → `OldMath.floor`. `Math.sin` nur, wo das
  Original `Math.sin` benutzte.
- Generatoren dürfen keinen Zustand zwischen Chunks behalten. Kacheln werden in mehreren Threads gerechnet.

## Blöcke anmelden

Jeder Block, der in einem generierten Chunk vorkommen kann, braucht **zwei** Einträge mit der ID aus der
Pack-Konfiguration (`z-hexxit-configs`):

```java
OldBlockInfo.registerCube(id, OldBlockInfo.Material.ROCK);   // oder registerLog, registerLeaves, register(...)
OldBlockStates.register(id, () -> MY_BLOCK.get().defaultBlockState());      // alle Metadaten gleich
OldBlockStates.registerMeta(id, meta -> ...);                               // je Metadatenwert
```

Fehlt einer, bricht die Generierung mit einer Meldung ab, die die ID nennt. Laub bekommt seine Distanz zum Stamm
vom `OldChunkWriter`, wenn der Block ein `LeavesBlock` ist und mit `registerLeaves`/`registerLog` gemeldet wurde.
Terrain-Blöcke der Deckschicht müssen IDs unter 256 haben (Byte-Array des Originals).

Für `verify_blocks.py` die ID in `MODS` (oder `block()`, wenn Metadaten zählen) eintragen.

## Biome und Dekoratoren

```java
OldBiome biome = OldBiomes.register(new OldBiome(id, "Name", "modid:pfad")).height(..).climate(..).surface(..);
OldBiomes.addToOverworld(biome);                 // WorldType.addNewBiome, Reihenfolge wie im Original
biome.decorator.treesPerChunk = 7;               // Felder wie BiomeDecorator
biome.decorator = new MyDecorator();             // eigener Dekorator (Unterklasse von OldDecorator)
biome.trees = (world, random) -> ...;            // getRandomWorldGenForTrees
biome.grass = random -> ...;                     // getRandomWorldGenForGrass
biome.afterDecoration((b, world, random, x, z) -> ...);   // wie decorate() mit super-Aufruf zuerst
```

`world.bigTree()` liefert die große Eiche des aktuellen Chunks. Dekoratoren sind zwischen Threads geteilt:
keine Felder für laufende Daten, alles über Parameter.

## Truhen und Spawner

`world.setBlock(x, y, z, 54)` und danach `world.setTile(x, y, z, chest)`; Inhalt über `OldLoot.Category.fill`.
Neue Beute: `OldLoot.DUNGEON_CHEST.add(new OldStack(itemId, damage, 1), min, max, weight)` und die Übersetzung in
`OldGameIds.registerItem`. Dungeon-Mobs: `OldLoot.addDungeonMob` und `OldGameIds.registerEntity`.

## Strukturen

Eigene Bauteile erben von `StructureComponent` (Namen und Hilfsmethoden wie im Original: `fillWithBlocks`,
`placeBlockAtCurrentPosition`, `getMetadataWithOffset`). Bauteile dürfen sich Zustand merken, sie werden je Kachel
neu erzeugt. Mod-Biome für Dörfer, Festungen und Spawn: `OldStructures.VILLAGE_BIOMES`, `STRONGHOLD_BIOMES`,
`SPAWN_BIOMES`. Eigene Dorfblöcke je Biom: `OldStructures.villageBlocks`. Entitäten: `world.spawnEntity(new
OldEntity...)`, neue Arten brauchen einen Fall in `OldChunkWriter.entity`. Strukturen laufen vor den Seen und
ziehen aus derselben Zufallsfolge wie die Population.

## Prüfen

```
tools/expect.sh 1234 find 260 > expect.txt        # ein Chunk je Biom
tools/expect.sh 1234 structures 220 > expect.txt  # Chunks an den nächsten Strukturen jeder Art
tools/expect.sh 1234 -36 -24 12 -8 > expect.txt   # bestimmte Chunks
python3 tools/verify_blocks.py expect.txt         # Server läuft mit level-seed=1234, muss 0 Abweichungen melden
```

`expect.sh` gibt auf stderr aus, wie viele Blöcke je ID die Population gesetzt hat. Taucht die ID des neuen
Erzes oder Baums dort nicht auf, liegt kein passender Chunk in der Auswahl: gezielt Chunks angeben. Die Datei
enthält vier volle Spalten je Chunk und jeden Block, den die Population geändert hat.

Bitgenauigkeit gegen das Original lässt sich so nicht prüfen, nur Kern gegen Spiel. Den Port deshalb Zeile für
Zeile gegen `.reference/<mod>/src` lesen, besonders die Reihenfolge der `nextInt`-Aufrufe.

## Stolperfallen

- **Versatz +8.** Population arbeitet auf dem 16×16-Feld, das 8 Blöcke in +x/+z verschoben ist. Die meisten
  Generatoren rechnen `x + nextInt(16) + 8`, Erze nur `x + nextInt(16)` (`WorldGenMinable` addiert die 8 selbst).
  Genau übernehmen, was im Original steht.
- **Reichweite.** Eine Kachel sind 8×8 Chunks, populiert wird mit Rand 4 davor und 2 danach. Ein Generator,
  der weiter als etwa 2 Chunks über seinen Chunk hinaus schreibt oder liest, wird an Kachelgrenzen abgeschnitten.
  Dann `MARGIN_LOW`/`MARGIN_HIGH` in `OldDecoration` anheben.
- **Kurzschluss-Auswertung.** `a || random.nextInt(2) != 0 && b` zieht nur manchmal eine Zufallszahl. Bedingungen
  nicht umstellen.
- **`setBlock` gibt bei unverändertem Block `false` zurück** und ruft sonst `onBlockAdded` nach: Lava neben Wasser
  wird Obsidian, Truhen richten sich aus. Nachbar-Updates gibt es nicht (Flag 2).
- **Fließende IDs 8 und 10** bedeuten „Quelle, die loslaufen soll“. Für stehendes Wasser 9, für Lava 11 setzen.
- **Licht** ist nur das Himmelslicht der eigenen Spalte (`world.skyLight`), Blocklicht gibt es nicht.
- **Höhen:** `getHeightValue` (oberster lichtdämpfender Block + 1), `getTopSolidOrLiquidBlock` (geht durch Wasser
  und Laub), `getPrecipitationHeight`. Nicht verwechseln, das Original unterscheidet sie.
- **Frische Welt.** Vor dem Abgleich `run/server/world` löschen. Der Abgleich friert die Welt ein (`tick freeze`);
  in einer Welt, die schon gelaufen ist, sind Kies gefallen, Lava geflossen und Türen geöffnet.
- **Vergleich am Server:** Der Abgleich stellt `random_tick_speed` auf 0. Blöcke, die fließendes Wasser erreicht,
  zählt er getrennt und nicht als Abweichung.
