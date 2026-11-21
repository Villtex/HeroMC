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
- Turm und Festung (Subagent, nur kompiliert): Turm-Ghast fest 4,5-fach (der Client kannte den attackCounter nie);
  Kettenblock/-glieder mit dem Licht des Goblins, Kettenwinkel zwischen den Ticks geschätzt; Speerarm des oberen Ritters
  mit dem 1.5.2-Fehler (Modell-Skalierung statt partialTick); Termite mit dem heutigen Silberfischchen-Modell.
- `OldBiped`: der Hut ist in 26.3 ein Kind des Kopfes (HumanoidModel sucht ihn dort); alle Zweibeiner prüfen.


## Teil 24d – Strukturen A

Kern in `gen/structure` (Klassen mit den Namen des Originals, per Skript aus den Quellen übersetzt, Block-Konstanten als
alte IDs): `MapGenTFMajorFeature` (Starts je `OldWorld` über `worldState`, Reichweite 8 Chunks), `StructureTFMajorFeatureStart`,
`StructureTFComponent`, Hohle Hügel, Heckenlabyrinth, Naga-Hof, Quest-Hain, Lich-Turm (`ComponentTFTower*`), Labyrinth
(`ComponentTFMaze*`, `TFMaze`, `ComponentTFMinotaurMaze`). Truhen: alle 16 Tabellen in `gen/TFTreasure`, Zufallsverzauberung
`gen/OldEnchanting`. Bilder: `worldgen152` `OldPainting` (Kopie der Rechnung aus `betterdungeons.gen.BDPainting`).
Einhängepunkt: `TFPopulator.populate` ruft `generateStructuresInChunk` als Erstes; `TFWorld.spawnListIndexAt` für `TFSpawns`.

### Einhängepunkte für andere Teile

- 24e: `StructureTFMajorFeatureStart.makeFirstComponent` gibt für Hydra-Höhle, Dunklen Turm und Ritter-Festung noch `null`
  zurück (Start ohne Teile, zieht keine Zufallszahlen); dort die ersten Bauteile einsetzen. Die Ritter-Festung braucht danach
  den Zweig mit `ComponentStrongholdStairs2`/`offY = -33` aus dem Original, der Dunkle Turm `moveToAvgGroundLevel` und `y - 1`.
- 24f: Boss-Spawner `twilightforest:{naga,lich,hydra,tower}_boss_spawner` (alte ID 1351:0..3) sind Platzhalter ohne
  Funktion (Aussehen des Monster-Spawners). Der Minoshroom im Labyrinth heißt im Kern `TwilightForest.Minoshroom@7`
  (setHomeArea Radius 7): mit `OldGameIds.registerEntity` anmelden, Heimat setzen, und den Ausschluss in
  `tools/java/Expect.java` (Mob-Zeilen) entfernen. Beute-Items, die erst 24f bringt (Eisenholz, Stahlblatt, Naga-Schuppe,
  Pfauenfächer, Labyrinth-Waffeln …), lässt der Chunk-Writer bis dahin mit einer Warnung weg.

### Abweichungen

- Hohle Bäume (`MapGenTFHollowTree`, `ComponentTFHollowTree*`, `ComponentTFLeafSphere`) nicht portiert: im Original toter
  Code (`canSpawnStructureAtCoords` immer false, nirgends benutzt). Die hohlen Bäume der Dekoration sind seit 24b da.
- Überlappende Starts werden nach Chunk x, dann z gebaut (im Original Reihenfolge einer HashMap).
- Zufallsverzauberung nur mit den Vanilla-Verzauberungen von 1.5.2 (im Original standen auch die der anderen Mods des
  Packs in der Liste, etwa Feurige Aura 206).
- Pflanzkübel im Lich-Turm: `growTree`/`fertilizeMushroom` liefen mit `world.rand` (nicht seed-fest); hier ein Zufall aus
  Seed und Position.
- Bilder und Fackeln im Lich-Turm: `getRandomWallSpot` ohne Treffer warf im Original eine NullPointerException (beim
  Hauptturm nie, er liegt in einem Population-Feld); hier wird der Platz übersprungen. Kollisionsboxen der Treppen ohne
  Eckformen, Zäune immer volle 1,5 hoch (`OldPainting`).
- `nullifySkyLight`/`updateAllLightTypes`: der Kern hat keine Lichtwerte (nur Licht und Mob-Spawns betroffen).


## Teil 24e – Strukturen B

Kern in `gen/structure`: Dunkler Turm (`ComponentTFDarkTower*`, `StructureDecoratorDarkTower`, `EnumDarkTowerDoor`),
Ritter-Festung (`ComponentTFStronghold*`, `StructureTFStronghold*`, `TFStrongholdPieces`, `TFStrongholdPieceWeight`,
`StructureTFDecoratorStronghold`), `ComponentTFHydraLair`. Spielseite in `TFTower` und `block/Tower*`, `block/*BlockEntity`
(Baumeister, Rückbauer, Ghast-Falle, Reaktor). Daten in `tools/gen.py` (Abschnitt Teil 24e).

### Einhängepunkte für andere Teile

- 24f: Ur-Ghast in der Ghast-Falle: `GhastTrapBlockEntity.IS_UR_GHAST` und `UR_GHAST_IN_TRAP` (stopTantrum) setzen. Der
  Turm-Boss-Spawner (1351:3) im Dunklen Turm und der Hydra-Spawner (1351:2) in der Höhle sind die Platzhalter aus 24d.
- 24f: Karminit-Rezept (Bohreressenz, Redstone, Ghast-Träne; die Bilderrahmen der Turmschmiede zeigen es), Fortschritte.

### Abweichungen

- `TFStrongholdPieces`: Gewichte und `lastPieceMade` waren statisch (das erste Teil einer Festung hing von der zuvor
  gebauten ab, und Threads teilten sie); hier gehören sie zu einer Festung.
- `MaxNearbyEntities` der Mini-Ghast-Spawner = Schwierigkeit beim Erzeugen; hier fest 1 (leicht, Vorgabe von 1.5.2).
- Bäume in Festung und Turm und der Platz des Schlüssels in der Truhe: `world.rand` → Zufall aus Seed und Position.
- `MapGenTFMajorFeature.REACH` 10 statt 8 (die unteren Festungsteile reichen bis 112 Blöcke vom Eingang).
- Spiel: ein Block je Metadaten (BE kommt und geht mit dem Block). Partikel „ghasttrap“ → `portal` (lila statt rot),
  „hugesmoke“ → `LARGE_SMOKE`; die Fallen-Sounds starten nur bei Tick 1/30/80 (1.5.2 rief sie jeden Tick auf).
  Reaktortrümmer: feste Kombination aus Grundgestein/Netherrack/Obsidian/Portal statt Zufall je Fläche und Bild,
  volle Hitbox statt Zufallsgrenzen. Bau-, Rückbau- und Fallenzustand wird nicht gespeichert (wie 1.5.2).
- Rückbauer vergleicht wie 1.5.2 nur den Block (nicht die Zustände); die Bedingung „replaceBlockID == 4“ des Originals
  (Bruchstein statt Metadaten 4) ist beibehalten.

### Client-Prüfung offen

- Modelle und Durchsichtigkeit der Turmblöcke, Ghast-Fallen-Deckel, Funken (`sparkle`), Partikel der Falle und des
  Rückbauers; Bilderrahmen in der Turmschmiede; Karminit-Baumeister und Turmschlüssel mit einem Spieler.


## Teil 24f – Bosse und Ausrüstung

Items in `TFItems` (Werkzeuge, Rüstungen, Essen, Materialien, Sonder-Items in `item/`), Bosse in `TFBosses` (+ `TFHydra`),
Entwerkbank in `TFUncrafting` (`block/UncraftingMenu`, `client/gui/UncraftingScreen`, `compat/TwilightForestJeiPlugin`),
Amulette in `TFEventListener`, Feurige Aura in `TFEnchantment`, Daten in `tools/gen_items.py` (und `gen_hydra.py`).

### Abweichungen

- Verzauberte Rezepte (addEnchantedRecipe): Ergebnis mit Verzauberungs-Komponente; die Werkzeuge/Rüstungen im Kreativ-Tab
  wie getSubItems verzaubert (Feuerschwert dort Feueraspekt I, aus dem Rezept II wie im Original).
- Stahlblatt-Rüstung hat wie im Original die Werte von Eisenholz (ARMOR_STEELEAF ist toter Code).
- Zepter brechen nicht (Schaden bleibt beim Maximum, nachladbar); Horn, Fächer, Magnet, Mondwurm-Königin zerbrechen bei
  Schaden = Maximum (1.5.2: Maximum + 1).
- Feuer-Spitzhacke: ersetzt die Drops des Blocks (BlockDropsEvent) statt sie zu unterdrücken und neu zu erzeugen.
- Lebensentzug: Ziel wie im Original durch Wände (nur Entities, keine Blöcke im Weg geprüft).
- Verwandlungspulver: wirkt vor der eigenen Interaktion des Ziels (sonst würde der Dorfbewohner handeln).
- Amulett des Lebens: prüft den Schaden nach Rüstung/Resistenz (LivingDamageEvent.Pre) statt der 1.5.2-Nachrechnung;
  Amulett der Bewahrung III behält auch die Nebenhand.
- Lich: die Partikelspuren der KI (Teleport, Diener, Mob-Platzen) sieht man hier; in 1.5.2 liefen sie nur auf dem Server
  (unsichtbar). Goldene Beute: 2..4 Würfe (der Plünderungs-Bonus des Originals fehlt), Verzauberung Stufe 10..39.
- Naga: Körpersegmente sind Part-Entities (in 1.5.2 eigene, nicht gespeicherte Entities beider Seiten); ihre eigene
  Bewegung (moveEntity vor dem Kopf) entfällt, sie stehen danach ohnehin an der vom Kopf gesetzten Stelle.
- Ur-Ghast: Feuerbälle sind Vanilla-Feuerbälle mit eigenem Treffer (16 Schaden, Explosion 1 mit Feuer); Tränen als
  fallende Item-Partikel statt EntityTFBossTearFX; Wetter über `setWeatherParameters` der Oberwelt wie im Original.
- Trophäen: ein stehender und ein Wandblock je Boss (wie Köpfe heute); Drehung nach heutiger Schädel-Logik.
- Entwerkbank: Rezeptsuche nur auf dem Server; Kosten und kaputte Teile als Container-Daten. Erstes passendes
  geformtes Rezept in der Reihenfolge des Rezept-Managers (1.5.2: Reihenfolge der Rezeptliste). Kopiert nur Name und
  Reparaturkosten (1.5.2 das ganze NBT). Beim Neu-Herstellen wird die Eingabe wie im Original 9 × verringert.
- Fortschritte: „Portal“ auch bei der Ankunft (BlockTFPortal vergab beide), Karten über „Rezept hergestellt“ oder
  „Karte im Inventar“, Jäger über ein Entity-Tag `#twilightforest:hunter_prey`.

### Client-Prüfung offen

- Rüstungstexturen, Item-Modelle (Magnet/Mondwurm-Königin wechseln beim Benutzen), Modelle und Renderer von Naga
  (Kopf doppelt groß, Segmente), Lich (Kragen, Umhang, Schildgürtel, Klone grau-durchsichtig), Diener (grün),
  Minoshroom (Kuh-Körper, Pilze), Ur-Ghast (12,5-fach), Trophäen, Amulett-Effekt, treuer Zombie (grün), Entwerkbank-GUI.
