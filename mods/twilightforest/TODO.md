# Twilight Forest – offen nach Teil 24a

Stand nach 24a (Dimension, Gelände, Biome, Portal, Struktur-Raster). Was die nächsten Unterteile brauchen.

## Einhängepunkte

- Strukturen (24d/24e): `majorFeatureGenerator.generateStructuresInChunk` fehlt in `TFPopulator.populate` (Kommentar
  an der Stelle). Es zieht als Erstes Zufallszahlen aus dem Chunk-Zufall: bis dahin liegen Seen/Dungeons in den
  Chunks, die ein Bauwerk schneidet, anders als im Original. Auch `majorFeatureGenerator.generate` in
  `provideChunk` (Startstücke) fehlt; `TFTerrain` braucht davon nichts.
- `TFFeature`: Mob-Listen der Bauwerke und `getSpawnableList` (24c), `structureEnabled` für 24d/24e.
- Mob-Spawns (24c): Biom-JSONs haben leere Listen (`tools/gen.py`, `BIOMES`), `TFChunkGenerator.spawnOriginalMobs`
  und `getMobsAt` (Untergrund-Liste `TFFeature.underground` unter y 32, Listen der Bauwerke) fehlen.
- Fortschritte „Portal“/„Ankunft“ (24f, `TFAchievementPage`).

## Abweichungen

- Portalsuche am Ziel über den POI-Index statt Block für Block in 401 × 401 Spalten: findet nur Portale in Chunks, die
  es schon gibt (das Original erzeugte dabei 625 Chunks). Wahl wie im Original (unterste Portalzeile, nächster
  Abstand, nur y < 128).
- Funken über dem Diamanten: Partikel `effect` statt „spell“.
- Lichtwerte des Himmels (Sonnenwinkel 81°, Himmelslicht 13) nachgebildet, nicht verglichen.
- `CommandTFLocate` (Debug-Ausgabe auf die Konsole) nicht portiert.

## Prüfung

- Kern gegen Spiel: `tools/expect.sh 1234 twilight find 120` + `verify_blocks.py`: 0 Abweichungen.
- Kern gegen Original: offen. Der 1.5.2-Server erzeugt ohne Spieler keine Chunks der Dimension 7.
- Client-Prüfung offen: Himmel, Nebel, Sterne, Helligkeit; Portalmodell und Partikel; Wasserfarben der Sümpfe; die
  zufälligen Graskolorierungen des Verzauberten Walds (`getBiomeGrassColor` mit Zufall) sind nicht nachgebaut.

## Teil 24b – Dekoration (Kern)

- `gen/TFDecorator` (TFBiomeDecorator + Dunkelwald), Werte je Biom in `gen/TFBiomes` (Konstruktoren der TFBiome*,
  `getRandomWorldGenForTrees/Grass`, was `decorate()` danach anhängt). Bäume in `gen/TFTrees`, kleine Bauten
  (ruinList) in `gen/TFRuins`, Wurzeln/Beeren/Myzel/Feuerdüsen/Pinguine/Pflanzen in `gen/TFPlants`, Truhen in
  `gen/TFTreasure` (nur Tabellen 9 Keller und 14 Baumversteck; die übrigen 14 Tabellen mit 24d/24e).
- Die nächste Bauwerks-Art (`TFFeature.getNearestFeature`) legt `TFPopulator` in `world.chunkState` ab
  (`TFDecorator.NEAR_FEATURE`), Dekorator und Feuersumpf lesen sie dort.
- Mobs der Dekoration (Pinguine im Gletscher, Raben am Monolithen): Zufallszahlen werden gezogen, gespawnt wird erst
  mit 24c (`gen/TFMobs.spawn`). Spawner der Blätterverliese („TwilightForest.Swarm Spider“) und Hexenhütten
  („TwilightForest.Skeleton Druid“) zeigen bis 24c auf Höhlenspinne/Skelett (`OldGameIds.registerEntity`).
- Abweichungen, die das Original selbst hat und die bleiben: `TFGenFallenSmallLog` setzt wegen fehlender `break`
  immer Tropenholz (17:3); `TFTreasure` Typ 3 trägt `add(sapling, 1, 4)` als Gewicht 4 statt Metadaten 4 ein.
- `worldgen152` erweitert: `OldBlockInfo.registerAdded` (onBlockAdded von Mod-Blöcken: Kleintiere suchen sich eine
  Seite oder fallen ab), `registerLeaves(id, opaque, normal, lightOpacity)`, `OldTreeGens.BigTree(log, leaves, meta,
  replaceable)` für die große Regenbogeneiche.
- Setzlinge: `TFSaplings` lässt die Generatoren über `OldLiveGen` wachsen (Reichweiten gemessen).

## Teil 24b – Spielseite

Blöcke, Items, Block-Entities, Renderer, Daten (`tools/gen.py`) und alte Ids (`TFBlocks.registerOldBlocks`) sind da.
Offen bzw. vereinfacht:

### Einhängepunkte für andere Teile

- Setzlinge: `TFBlocks.GROWER` (`TreeGrower.grow(level, pos, meta, random)`) liefert bis jetzt immer `false`, der
  Setzling bleibt stehen. Die 1.5.2-Generatoren von `BlockTFSapling.growTree` (meta 0..9, Regenbogeneiche 1 in 7 groß)
  über worldgen152 `OldLiveGen` einhängen.
- Befallenes Turmholz: `TowerWoodBlock.TERMITE` setzen, sobald `EntityTFTowerTermite` existiert (24c).
- Labyrinthstein: `MazestoneBlock.SPARED` um die Labyrinthbrecher-Spitzhacke ergänzen (24f).
- Erzmagnet (24f): `block/OreMagnet.doMagnet(level, x0, y0, z0, x1, y1, z1)` mit der Blicklinie aufrufen
  (`ItemTFOreMagnet.doMagnet` mit Gier-/Nick-Versatz ±10° und `getOffsetLook`).
- Amulett des Lebens I / der Bewahrung I: nur Items mit Textur; Wirkung (TFEventListener) kommt mit 24f.

### Abweichungen

- Kritter mit Metadaten 0 (von der Generierung gesetzt): statt `onBlockAdded` sucht `updateShape` eine Stütze
  (Reihenfolge x-1, x+1, z-1, z+1, unten, oben), das läuft über `OldBlockStates.registerConnecting`. Folge: Steht
  die Stütze eines Kritters im Spiel nicht mehr, hängt er sich an eine andere Stütze, falls es eine gibt, statt
  abzufallen. Metadaten 0 gelten bis dahin als „auf dem Boden“.
- Zikadengesang und die kleinen Glühwürmchen laufen nur auf dem Client (das Original spielte den Gesang über den
  Server ab); Glühen des Glühwürmchens mit `RenderTypes.eyes` (additiv) statt GL-Blending mit Alpha.
- Partikel: „hugesmoke“ → `LARGE_SMOKE`, „largeflame“ → `FLAME`, „leafrune“ → `ENCHANT`, „townaura“ → `MYCELIUM`.
- Feuerdüse: Das Original setzte „popping“ ohne Meldung an den Client (Flag 0); hier sehen Clients das Knacken.
- Baum der Verwandlung: Biome sind heute 4 × 4 × 4 Blöcke groß, es wird die ganze 4 × 4-Säule (alle Höhen) zum
  Verzauberten Wald, danach `resendBiomesForChunks` für den Chunk.
- Sortierbaum: Gruppierung über die Kreativ-Tabs von 26.3 (erster Kategorie-Tab, der das Item zeigt; Aufbau einmal
  über `CreativeModeTabs.tryRebuildTabContents`), Items ohne Tab nach Mod-Namespace; gleiche Items: gleiche
  Komponenten statt id+damage. Nur einfache Truhen (`Blocks.CHEST`), wie `Block.chest` im Original.
- Erzmagnet: „Erz“ = `#c:ores` (ohne Kohle), Lebenswurzelblock, oder Block-Id mit dem Wort „ore“. Ersetzt werden nur
  Erde, Gras, Kies, Stein (bzw. Netherrack), kein Tiefenschiefer.
- Magische Kerne: Metadaten 4/8 (liegend, aktiv) werden wie stehend aktiv gezeigt; Rechtsklick schaltet immer um
  (das Original nur bei 0 und 12). Von der Generierung gesetzte Kerne bekommen ihren ersten Tick über einen
  Zufallstick.
- Rinden-Blöcke (`*_wood`, Metadaten 12) lassen wie `BlockLog.damageDropped` den Stamm fallen.
- Nagastein: Kopf (0..3) und Körper (4..15) sind zwei Blöcke mit Eigenschaft `meta` = alte Metadaten; Einpassen bei
  Platzieren und Nachbaränderung wie im Original (`determineOrientation` des Kolbens nachgebaut).
- Laub fällt mit der Distanz von Vanilla (7) statt im Umkreis 4 von 1.5.2; Schere gibt das Laub.

### Client-Prüfung offen (nur Code gelesen, kein Client gestartet)

- UV-Drehungen der Nagastein-Seiten (uvRotate 1 → 90°, 2 → 270°, 3 → 180°, Seitenzuordnung East=Nord-, West=Süd-,
  North=West-, South=Ost-Fläche angenommen).
- Magisches Laub: die UV-Drehungen von `RenderBlockTFMagicLeaves` (teils positionsabhängig) fehlen.
- Moosfleck und Kleefleck: feste Modelle (Teppich 1..15 bzw. Deckel auf 2/16) statt der zufälligen Einzüge und
  Moos-Büschel von `RenderBlockTFPlants`; die Umrissform rechnet die Einzüge wie das Original.
- Kritter-Modelle (Drehung je Seite, Gier, Mondwurm-Wellen), Glühwürmchen-Partikel (UV 20..30 × 0..10 der
  64 × 32-Textur), Farben aller getönten Blöcke, Animation der drei Laub-Texturen (16 Bilder à 2 Ticks).

## Teil 24c – Mobs

`TFEntities` (Typen, Attribute, Spawn-Regeln, Eier mit alter Id 7309:Mob-Id, Drop-Items, Sounds), Klassen in
`entity/` (Tiere in `entity/passive`, Geschosse in `entity/projectile`, Ziele in `entity/ai`), Modelle und Renderer in
`client/entity/` (Geometrie mit `tools/modelbase_to_layer.py`), Daten in `tools/gen_mobs.py` (Namen, Eier, Texturen,
Sounds, Loot, Biom-Spawnlisten), Spawn-Logik `world/TFSpawns` (getPossibleCreatures).

### Einhängepunkte für andere Teile

- Bauwerks-Spawnlisten (Hügel, Labyrinth, Turm, Festung …) stehen in `TFSpawns`; sie greifen erst, wenn
  `TFSpawns.spawnListIndexAt` (MapGenTFMajorFeature.getSpawnListIndexAt) mit 24d/24e die Bauteile kennt (bis dahin -1).
- Boss-Spawner (24f): Mini-Ghast als Diener `MiniGhast.makeBossMinion()`, Ghast-Falle (24e) `TowerGhast.setInTrap()`.
- Drops, die erst 24f als richtige Items bringt: Zerbröselhorn des Widders (`twilightforest:crumble_horn`, wird nur
  fallen gelassen, wenn das Item existiert), Eisenholz-Spitzhacke/-Stiefel der Pioniere (bis dahin Eisen),
  Kartenfokus-Items und Bohreressenz sind schon da (nur Items, Rezepte mit 24f).
- Fortschritte „Jäger“, „Hügel“ usw. (onDeath → triggerAchievement) mit 24f.
- Boggard (registriert, ohne Ei, nirgends gespawnt, „[NYI]“) und Turm-Schneemann nicht portiert; Lich-Diener, treuer
  Zombie, Minoshroom und die Bosse mit 24f.

### Abweichungen

- KI auf heutigen Goals; Geschwindigkeiten als Faktor der 1.5.2-moveSpeed. Nahkampf rechnet 1.5.2 (`TFMonster`:
  getAttackStrength = 2 + Waffe von 1.5.2, außer wo ein Mob ihn festlegt), Rüstung nach heutiger Formel.
- Spinnen (Schwarm, Hecke, König, Brütling): Ziele statt der alten Aktions-KI (Sprung + Biss), jagen in jedem Licht.
- Wölfe: heutige Wolf-Klasse, dauerhaft wütend (Zorn-Ende nie erreicht), nicht zähmbar; Nebelwolf blendet nur im
  Dunkeln (Helligkeit < 0,1 nach 1.5.2-Tabelle).
- Kettengoblin: Kettenblock und Glieder sind keine Entities, nur gezeichnet; der Treffer prüft die 0,75er-Box am Block.
- Klemmkäfer: trägt die Beute als Passagier 0,9 vor sich (heutiges Reiten, Absteigen mit Schleichen möglich).
- Sumpf/Dunkelwald geben ihre Monster 1 von 20 Mal frei; die Freigabe gilt für den Rest des Ticks im selben Chunk
  (heute fragt der Spawner die Liste mehrmals ab).
- Schaf-Fell des Bighorns: heutige Farben (`DyeColor`), Fell-Textur des heutigen Schafs.
- Ruhm/Erfolge und Despawn-Zeiten der alten `entityAge`-Zähler: nachgebildet, wo einfach (Turm-Ghast altert außerhalb
  des Turms schneller).

### Client-Prüfung offen (nur Code gelesen, kein Client gestartet)

- Alle Modelle (Geometrie per Werkzeug, Animationen von Hand), besonders Fragewidder-Bänder/-Linien, Mückenschwarm
  (halbe Größe über Teil-Skalierung), Todesbuch, Schleimkäfer-Würfel (durchscheinend), Nebelwolf (Transparenz),
  Geist (zweite Textur additiv statt mit Alpha 0,5), Glühwürmchen (Billboard, additiv), Jungtier-Köpfe.
