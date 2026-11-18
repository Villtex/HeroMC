---
name: templates-152
description: "Beim Portieren von Gebäude-Vorlagen und Schematics eines 1.5.2-Mods benutzen (Better Dungeons Teil 13, Ruins Teil 14 mit .tml-Vorlagen, Dimensional Doors Teil 15 mit .schematic, Battle Towers): Daten-Archive des Packs beim Bauen ins Jar holen, Vorlagen ohne Spielklassen lesen, Blöcke/Kisten/Spawner/Schilder/Bilder/Mobs über worldgen152 in die Welt setzen und mit expect.sh prüfen."
---

# Vorlagen und Schematics von 1.5.2 übernehmen

Vorbild: `mods/betterdungeons` (Teil 12). Die Vorlagen gehören den Autoren: nie ins Repo, beim Bauen aus dem
Technic-Archiv ins Jar.

## 1. Daten-Archive importieren

`original-assets.txt` des Mods: Zeilen gelten für das Archiv mit dem Namen des Mods; `[archiv]` schaltet für die
folgenden Zeilen auf ein anderes Archiv des Manifests (`pack/hexxit-1.0.11.json`) um, z. B. reine Daten-Archive:

```
[betterdungeons-data1]
Chocolate/ -> betterdungeons-original/Chocolate/
[betterdungeons-data2]
Chocolate/ -> betterdungeons-original/Chocolate/
```

- Ordner-Zeilen (`/` am Ende) bekommen neben sich eine `.index`-Datei mit allen Dateien (relativ, sortiert),
  weil ein Jar keinen Ordner auflisten kann. Zwei Archive in dasselbe Ziel ergeben einen gemeinsamen Index.
- Ziel **außerhalb** von `assets/` und `data/` und mit `-` im ersten Ordner (`<mod>-original/`): kein Java-Paket,
  also im NeoForge-Modul nicht gekapselt; `getClassLoader().getResourceAsStream(...)` findet es.
- Daten-Archive enthalten oft auch die **Pack-Konfiguration** des Mods (Better Dungeons: `Chocolate/config.prop`
  mit Block-ID 2001, Item-ID 7351, Dschungel-/Sumpfhöhlen an). Erst dort nachsehen, dann die Defaults des Codes.

## 2. Lesen ohne Spielklassen (für expect.sh)

- NBT von 1.5.2 (unkomprimiert, `readNamedTag`): `betterdungeons/gen/BDNbt.java` (Maps, Listen, Zahlen). Große
  Vorlagen beim Lesen gleich in Arrays packen (`BDTemplate`: `short[]` IDs, `byte[]` Metadaten, nur Blöcke mit
  Tile-Daten behalten ihr Compound), einmal laden und cachen.
- `File.listFiles()`-Reihenfolge des Originals: hier NTFS-Ordnung (`BDFiles.NTFS_ORDER`, Großbuchstaben-Vergleich).
  Die Wahl `random.nextInt(files.length)` hängt daran; unter „Offen“ notieren.
- `Properties.elements()`/`keys()` von Java 6–8 (Hashtable-Reihenfolge, wichtig für gewichtete Listen):
  `BDFiles.Ordered.hashtableValues()`.
- Bugs des Originals beim Einlesen mitnehmen (Better Dungeons: Schleife mit `stkn.countTokens()` liest nur die
  erste Hälfte einer Biom-Liste).
- `new Random()` ohne Seed im Original: Ersatz aus Welt-Seed und Position (`BuilderHelper.positionRandom`).

## 3. In die Welt setzen (`OldWorld`)

| Original | Hier |
|---|---|
| `world.setBlock(x,y,z,id,meta,flag)` | `world.setBlock(x,y,z,id,meta)` (onBlockAdded läuft: Truhen, Öfen, Fackeln, Schienen) |
| `setBlockMetadataWithNotify` | `world.setMetadata` (ohne onBlockAdded) |
| `ItemDoor.placeDoorBlock` | `world.placeDoor(x,y,z,facing,id)` |
| TileEntity aus NBT | `world.setTile(..., new OldTile.Chest/Dispenser/Furnace/Sign/Jukebox/BrewingStand/Skull/Spawner)` |
| eigene TileEntity | Record `implements OldTile.Mod` im Kern + `OldModData.registerTile(Klasse, Schreiber)` in der Mod-Klasse |
| `EntityPainting`/`EntityItemFrame` | `OldEntity.Painting/ItemFrame(tileX,tileY,tileZ = Wandblock, direction 0 S 1 W 2 N 3 E, …)` |
| `new EntityPainting(world, x, y, z, dir)` (Motiv zufällig, onValidSurface) | `betterdungeons.gen.BDPainting.place` (Kollisionsboxen, umgedrehte Box) |
| andere Entities (`EntityList`-Name) | `OldEntity.Mob(name, x,y,z, yaw, equipment)`, `OldEntity.Item(...)` |

- Alle Vanilla-IDs 1–158 (Blöcke) und alle Vanilla-Items/Schaden-Werte sind in `OldBlockStates`, `OldBlockInfo`,
  `OldGameIds` und `tools/verify_blocks.py` (Teil 12). Mod-Blöcke der Vorlage: wie im Skill `worldgen152`.
- Items/Entities noch nicht portierter Mods lässt der Chunk-Writer mit einer Warnung weg (kein Absturz).
  Mob-Namen eines Mods, dessen Mobs noch fehlen: vorübergehend `OldGameIds.registerEntity(name, vanilla)`.
- Blöcke mit Blockentität ohne Tile-Eintrag (Bett, Zaubertisch) bekommen ihre leere Blockentität vom Writer.
- Mehrfach-Durchgänge wie im Original nachbauen (erst feste Blöcke, dann Fackeln/Hebel/Leitern/Betten; Türen
  über `placeDoor`). Reihenfolge der Zufallsaufrufe (Kisten, Öfen, Spawner) exakt, sonst verschiebt sich alles.

## 4. Reichweite

Ein Generator darf ohne Weiteres etwa 4 Chunks in +x/+z und 3 in −x/−z schreiben (`OldDecoration.MARGIN_LOW/HIGH`).
Better Dungeons baut ab der Chunk-Ecke bis 73 Blöcke breit, das passt. Größere Bauten (Eishöhle 100 Blöcke, Vulkan
−50..+85, Burgen 70 Blöcke vor der Mauer): Reichweite messen (Builder in einer `OldWorld` mehrmals laufen lassen,
geänderte Blöcke umrahmen) und mit `OldTerrainGen.registerReach` anmelden (Skill `worldgen152`, „Reichweite“).
`new Random()` der Builder (Höhlenform, Dungeon-Grundriss): `BuilderHelper.positionRandom(world, x, y, z, salt)`.

## Text-Vorlagen ohne Umwandlung (Ruins, Teil 14)

Ruins' `.tml` liest der Kern wie das Original (`ruins.gen.RuinTemplate`, `RuinsFiles` mit Index und
`BufferedReader.readLine`-Semantik); kein Konverter nötig. Parser-Fehler des Originals mitnehmen (Leerzeile in einer
Schicht lässt die Vorlage fallen). Gewichtete Wahl aus einem `HashSet` mit `hashCode` = Dateipfad: einen festen
Pfad nehmen und unter „Offen“ notieren. Pack-Optionen aus `z-hexxit-configs` mit einer `[z-hexxit-configs]`-Zeile
ins Jar holen (`mods/ruins.txt`, `config/BattleTowers.cfg`).

Bauten, die sich aneinanderreihen (Ruinen-Gruppen mit wachsendem Abstand): vor dem Bauen die gelesene/geschriebene
Fläche (`RuinTemplate.extent`) gegen das Fenster −3..+4 Chunks prüfen und sonst auslassen, statt die Reichweite für
jede Kachel zu erhöhen.

## 5. Prüfen

```
tools/expect.sh 1234 dungeons 150 > expect.txt   # Better Dungeons: je Dungeon-Typ, Chunk-Höhle und Mauer-Burg der nächste
python3 tools/verify_blocks.py expect.txt
```

Generatoren, deren Funde von der Welt abhängen (Ruins, Battle Towers): ein statischer Melde-Haken im Kern
(`RuinsGen.built`, `BTWorldGen.built`, im Spiel `null`) und in `Expect.java` Kacheln ringweise populieren, bis genug
gebaut sind (`tools/expect.sh 1234 ruins 40 -30 -40`, `towers 6`, `nether ruins 15`).

Für einen neuen Mod: Suchmodus in `tools/java/Expect.java` nach dem Muster `dungeons` anlegen (Kandidaten nur über
`BiomeLayout` und den Seed bestimmen, ohne die Welt zu bevölkern), Kern-Pfad in `tools/expect.sh` ergänzen, den
Classpath auf `mods/<mod>/build/generated/original-assets` erweitern. `C x y z n v`-Zeilen: v Slots mit
Vanilla-Items, n alle; verify akzeptiert v..n.
