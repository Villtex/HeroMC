# Twilight Forest – offen nach Teil 24a

Stand nach 24a (Dimension, Gelände, Biome, Portal, Struktur-Raster). Was die nächsten Unterteile brauchen.

## Einhängepunkte

- Biom-Dekoration (24b): `gen/TFBiomes.biome()` setzt `decoration` auf leer; je Biom den `TFBiomeDecorator` (und
  `TFDarkForestBiomeDecorator`) als `OldBiome.decoration` eintragen. `TFPopulator` ruft sie schon an der Stelle des
  Originals (nach Seen und Dungeons, vor Eis/Schnee).
- Strukturen (24d/24e): `majorFeatureGenerator.generateStructuresInChunk` fehlt in `TFPopulator.populate` (Kommentar
  an der Stelle). Es zieht als Erstes Zufallszahlen aus dem Chunk-Zufall: bis dahin liegen Seen/Dungeons in den
  Chunks, die ein Bauwerk schneidet, anders als im Original. Auch `majorFeatureGenerator.generate` in
  `provideChunk` (Startstücke) fehlt; `TFTerrain` braucht davon nichts.
- `TFFeature`: Mob-Listen der Bauwerke und `getSpawnableList` (24c), `structureEnabled` für 24d/24e.
- Mob-Spawns (24c): Biom-JSONs haben leere Listen (`tools/gen.py`, `BIOMES`), `TFChunkGenerator.spawnOriginalMobs`
  und `getMobsAt` (Untergrund-Liste `TFFeature.underground` unter y 32, Listen der Bauwerke) fehlen.
- Hecke (24b): Schaden beim Weiterschlagen (`updateTick` mit Schwung), Dunkelholzlaub lässt 1 in 40 einen
  Dunkelholz-Setzling fallen (Loot-Tabelle fehlt), Brennbarkeit.
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
