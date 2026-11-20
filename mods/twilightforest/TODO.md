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
  („TwilightForest.Skeleton Druid“) zeigen bis 24c auf Vanilla-Spinne/-Skelett (`OldGameIds.registerEntity`).
- Abweichungen, die das Original selbst hat und die bleiben: `TFGenFallenSmallLog` setzt wegen fehlender `break`
  immer Tropenholz (17:3); `TFTreasure` Typ 3 trägt `add(sapling, 1, 4)` als Gewicht 4 statt Metadaten 4 ein.
- `worldgen152` erweitert: `OldBlockInfo.registerAdded` (onBlockAdded von Mod-Blöcken: Kleintiere suchen sich eine
  Seite oder fallen ab), `registerLeaves(id, opaque, normal, lightOpacity)`, `OldTreeGens.BigTree(log, leaves, meta,
  replaceable)` für die große Regenbogeneiche.
- Setzlinge: `TFSaplings` lässt die Generatoren über `OldLiveGen` wachsen (Reichweiten gemessen).
