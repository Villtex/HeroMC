---
name: twilight-forest-263
description: Beim Weiterbauen von Twilight Forest (mods/twilightforest, Teile 24e und 24f) benutzen – wo Kern (Gelände, Dekoration, Bauwerke) und Spielseite liegen, wie Bauteile des Originals per Skript in den Kern übersetzt und als Start eingehängt werden (Dunkler Turm, Ritter-Festung, Hydra-Höhle), Truhen/Spawner/Mobs/Bilder der Bauten, Boss-Spawner-Platzhalter, Prüfung mit expect.sh twilight structures.
---

# Twilight Forest weiterbauen

Teilprojekt `mods/twilightforest` (Mod-ID `twilightforest`, braucht `worldgen152`). Offenes je Unterteil in
`mods/twilightforest/TODO.md`, Stand in `docs/PLAN.md` (Teil 24a–24e).

## Aufbau

| Was | Wo |
|---|---|
| Kern ohne Spielklassen: Layout, Gelände, Höhlen, Population | `twilightforest.gen` (`TFWorld`, `TFLayout`, `TFTerrain`, `TFPopulator`) |
| Bauwerks-Raster (welche Art in welchem Chunk) | `gen/TFFeature` |
| Dekoration, Bäume, kleine Bauten, Truhen | `gen/TFDecorator`, `TFTrees`, `TFRuins`, `TFPlants`, `TFTreasure` (16 Tabellen), `OldEnchanting` |
| Große Bauwerke (Teile 24d, 24e) | `gen/structure` (Klassennamen des Originals) |
| Turm-Geräte 1370/1371, Turmschlüssel (24e) | `TFTower`, `block/TowerDeviceBlock`, `TowerTranslucentBlock`, `*BlockEntity` |
| Blöcke/Items, alte IDs | `TFBlocks.registerOldBlocks`, Daten aus `tools/gen.py` |
| Mobs, Spawner-Namen, Spawns | `TFEntities` (Namen `TwilightForest.<Name>`), `world/TFSpawns`, `tools/gen_mobs.py` |
| Items, Ausrüstung, Sonder-Items (24f) | `TFItems`, `item/*`, `TFEventListener` (Amulette, `award(player, name)` für Fortschritte), `tools/gen_items.py` |
| Bosse, Boss-Spawner, Trophäen (24f) | `TFBosses`, `entity/boss/*`, `TFHydra` + `entity/boss/hydra`, `block/BossSpawnerBlock`, `client/entity/BossRenderers`, `client/TrophyRenderer` |
| Entwerkbank, Karten (24f) | `TFUncrafting` (`block/UncraftingMenu`, JEI in `compat/`), `TFMaps`, `client/TFMapsClient` |

## Bauteile aus dem Original übernehmen

1. `python3 tools/reference.py --only twilightforest`.
2. Übersetzen (überschreibt Dateien gleichen Namens in `gen/structure`!):
   ```
   cd .reference/twilightforest/src/twilightforest/structures
   python3 ../../../../../mods/twilightforest/tools/translate_structures.py darktower/*.java
   ```
   Das Skript ersetzt `World` → `OldWorld`, `Block.x.blockID`/`TFBlocks.x.blockID` → alte ID (fehlt eine, steht
   `/*TODO*/` dahinter: Tabelle `BLOCKS`/`TF` im Skript ergänzen), `TFTreasure.x` → `TFTreasure.X`, `TFFeature.x` →
   Konstanten des Ports, `TFCreatures.getSpawnerNameFor("X")` → `"TwilightForest.X"`, `setBlock(..., 2)` → 5 Argumente.
   Andere Flags (`, 3)`, `, 4)`) bleiben stehen und fallen beim Übersetzen auf: Flag 4 = wie 2 (onBlockAdded läuft),
   Flag 3 mit Nachbar-Updates braucht ein eigenes Modell (Skill `worldgen152`, Stolperfallen).
3. Übersetzen lassen (schnell, ohne Gradle):
   ```
   javac -nowarn -d /tmp/x $(find mods/worldgen152/src/main/java/hexxit/worldgen152/core -name '*.java') \
     $(find mods/twilightforest/src/main/java/twilightforest/gen -name '*.java')
   ```
4. Von Hand nacharbeiten (Muster stehen schon in `gen/structure`):
   - Tile-Entities: Spawner `placeSpawnerAtCurrentPosition` (setzt `OldTile.Spawner`), Truhen `placeTreasure*`,
     Werfer `new OldTile.Dispenser()` + `world.setTile` (`ComponentTFQuestGrove`).
   - Entities: `world.spawnEntity(new OldEntity.Mob(name, x, y, z, yaw, null))`; Name mit `OldGameIds.registerEntity`
     in `TFEntities.registerOld`. Heimat-Radius (`setHomeArea`) im Namen tragen (`…@7`), Init im Spiel.
   - Bilder: `worldgen152` `OldPainting` (Box, Kollision, andere Bilder), Vorlage `ComponentTFTowerWing.checkPainting`.
   - `world.rand` (nicht seed-fest) → `ComponentTFTowerWing.worldRand`; Bäume/Pilze wachsen lassen:
     `ComponentTFTowerWing.growTree`/`fertilizeMushroom`; TF-Generatoren über `TFTrees`, `TFRuins`.
   - Licht (`nullifySkyLight`, `updateAllLightTypes`) entfällt; `getAverageGroundLevel` des Providers = 33.
   - `System.out.println` des Originals auskommentieren: stdout ist die Vorhersage von `expect.sh`!
   - Raw-Listen der Bauteile (`List list`) so lassen; nur eigene Felder generisch machen.
   - Spawner mit eigenen Einstellungen (writeToNBT/readFromNBT): `world.setTile(..., spawner.with(range, max, count))`.
   - Item-Rahmen: `OldEntity.ItemFrame` (tileX/Y/Z = Wandblock wie EntityHanging), Ofen-Inhalt `OldTile.Furnace`,
     Truhen nachträglich füllen über `world.getTile(...) instanceof OldTile.Chest` (Vorlage `ComponentTFDarkTowerWing`).
   - Statische Felder des Originals (z. B. `TFStrongholdPieces.lastPieceMade`) pro Bauwerk machen: Kern und Spiel
     erzeugen Starts in anderer Reihenfolge und in mehreren Threads.
5. Einhängen: erstes Bauteil in `StructureTFMajorFeatureStart.makeFirstComponent` (alle Arten sind seit 24e da; der
   `ComponentStrongholdStairs2`-Zweig des Originals ist toter Code).
   `MapGenTFMajorFeature` baut alles, was das Population-Feld des Chunks schneidet; `TFPopulator` ruft es als Erstes.
   Reicht ein Bauwerk weiter als 8 Chunks vom Start, `MapGenTFMajorFeature.REACH` erhöhen.
6. Neue Blöcke: im Kern `OldBlockInfo.register…` in `TFWorld.register`, im Spiel `OldBlockStates.registerMeta` in
   `TFBlocks.registerOldBlocks`, in `tools/verify_blocks.py` in `twilight()` (Name aus ID:Meta).

## Prüfen

```
tools/expect.sh 1234 twilight structures 120 [n] > expect.txt   # n Bauwerke je Art (Standard 1), alle Chunks ihrer Box
python3 tools/verify_blocks.py expect.txt                       # Server mit level-seed=1234, RCON, pause-when-empty-seconds=-1
```
stderr nennt jedes Bauwerk mit Chunk, Teilezahl und Box. Mob-Zeilen brauchen einen Eintrag in `verify_blocks.py`
(`MOBS` für Spawner, `ENTITIES` für `Mob:…`; Twilight-Mobs werden aus `TFEntities` erzeugt, Bauwerks-Namen mit `@`
eigens). Mobs, die es noch nicht gibt, in `tools/java/Expect.java` bei den Mob-Zeilen auslassen (Minoshroom bis 24f).
Bekannte Reste: Biome an Grenzen (`if biome` geht über den Zufallsversatz von `BiomeManager`), Pilzblöcke an
Chunk-Grenzen mit verbundenen Seiten.

## Stolperfallen

- Fortschritte/Tags dürfen nur auf Items, Rezepte und Entities zeigen, die es schon gibt: sonst startet der Server nicht
  („Registry loading errors“). Kriterium `recipe_crafted` heißt in 26.3 `"recipes"`; Entity-Tags mit noch fehlenden
  Einträgen `{"id": ..., "required": false}`. Nach jeder Datenänderung den Server bis `Done` starten.
- Alte Bosse mit KI ohne Goals (updateEntityActionState): eigene `MoveControl` ohne Wirkung, `zza/xxa` in
  `customServerAiStep` setzen, Springen über `getJumpControl().jump()` (Vorlagen `Naga`, `Lich`).

- Bauteile erzeugen ihren Inhalt aus eigenen Zufallszahlen (`new Random(world.seed() + minX * minZ)` u. ä.), die
  Population-Folge ziehen sie nur über das übergebene `rand`. Ganzzahl-Überläufe (`minX * 321534781 * …`) genau so lassen.
- `getRandomWallSpot` kann `null` liefern; im Original war das eine NullPointerException (Hauptturm liegt in einem Feld).
- Items, die es im Spiel noch nicht gibt, lässt der Chunk-Writer mit einer Warnung weg; `verify_blocks.py` zählt bei
  Truhen nur Vanilla-Items streng.
- Server hängt beim Erzeugen („tick took 60 seconds“, Chunk-Thread in `OldChunkWriter.entity`): eine Entity macht beim
  Anlegen Nachbar-Updates (war `ItemFrame.setRotation(int)`); nur Varianten ohne Level-Zugriff benutzen.
- Blöcke mit mehreren Metadaten und Tile-Entity nur für einige davon: ein Block je Metadaten (`TFTower.DEVICES`), dann
  kommt und geht die Block-Entity mit dem Block wie in 1.5.2 (bei Zustands-Properties bliebe die alte Block-Entity).
