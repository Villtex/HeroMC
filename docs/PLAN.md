# Arbeitsplan: Hexxit 1.0.11 → NeoForge 26.3

Jeder Teil ist so geschnitten, dass er in **eine Session** passt. Start in einem neuen Chat mit
„starte Teil N“. Die Session liest dann diesen Abschnitt und `CLAUDE.md` und arbeitet ihn ab.

## Regeln für jeden Teil

1. **Vorbereitung**
   - **Zuerst den Nutzer an das empfohlene Modell erinnern** und den Aufwand (Spalte „Modell, Aufwand“ in der Übersicht). Läuft die Session auf einem anderen Modell, kurz darauf hinweisen. Er wechselt mit `/model`. Den Aufwand stellt er im selben Menü ein. Bleibt ein Fehler nach zwei, drei Versuchen hängen, eine Stufe höher gehen. Erst danach anfangen. Faustregel: Opus 5.5 als Standard; Fable 5.2 für bitgenaue Weltgenerierung, schweres Rendering und festgefahrene Fehler; Sonnet 5.5 für kleine, mechanische Teile.
   - `git pull`, danach den Abschnitt des Teils lesen und prüfen, ob die Voraussetzungen erledigt sind (Status unten).
   - Original-Quellen holen: `python3 tools/reference.py --only <archiv>[,<archiv>...]`.
2. **Port**
   - Pro Original-Archiv ein Teilprojekt `mods/<archiv>/`. Die Mod-ID ist die alte ID in Kleinbuchstaben.
   - Texturen und Sounds kommen aus `original-assets.txt`, nie ins Repo.
   - Werte aus `z-hexxit-configs` (Pack-Konfiguration) sind die Standardwerte, nicht die Defaults des Mods.
   - Rezepte und Loot als Daten-JSON.
3. **JEI**
   - Jeder Mod mit eigenen Rezepttypen oder Maschinen bekommt ein JEI-Plugin (siehe Teil 3).
   - Normale Werkbank- und Ofenrezepte zeigt JEI von selbst.
4. **Weltgenerierung**
   - Alles, was in 1.5.2 Gelände oder Dekoration erzeugte, wird über `worldgen152` eingehängt, in der Reihenfolge des Originals.
   - Danach `tools/expect.sh` + `tools/verify_blocks.py` laufen lassen. Ergebnis: 0 Abweichungen.
5. **Prüfen**
   - `CI=true ./gradlew clean pack` muss grün sein.
   - Testserver bis `Done` ohne Fehler der portierten Mods.
   - Verhalten per RCON prüfen (Rezepte, Drops, Spawns).
6. **Abschluss**
   - Status in dieser Datei und in der Tabelle in `README.md` eintragen.
   - **Skill-Check:** Prüfen, ob in diesem Teil Wissen entstanden ist, das ein späterer Teil wieder braucht (Muster, Werkzeug, Stolperfalle). Wenn ja, einen Projekt-Skill nach den Regeln in `CLAUDE.md` („Skills anlegen“) anlegen oder erweitern. Wenn nein, nichts anlegen.
   - `graphify update .` ausführen, dann committen und pushen.
   - Was offen bleibt, beim Teil unter „Offen“ notieren.

Was sich nur im Client prüfen lässt (Modelle, GUIs, Render), wird als „Client-Prüfung offen“ vermerkt.

## Übersicht

| Teil | Inhalt | Zeilen (Original) | Braucht | Modell, Aufwand | Status |
|---:|---|---:|---|---|---|
| 0 | Grundgerüst, RottenFleshToLeather, 1.5.2-Gelände, ExtrabiomesXL-Biome | – | – | – | ✅ erledigt |
| 1 | Vanilla-1.5.2-Dekoration und Schnittstelle für Mod-Weltgeneratoren | ~3.000 | 0 | Fable 5.2, extra hoch | ✅ erledigt |
| 2 | Vanilla-1.5.2-Strukturen, Spawnpunkt, Abgleich mit Original-Welt | ~6.000 | 1 | Fable 5.2, extra hoch | ✅ erledigt |
| 3 | JEI-Grundlage, ExtrabiomesXL fertig (Bäume, Pflanzen, Holz, Scarecrow) | ~9.000 | 1 | Opus 5.5, mittel | offen |
| 4 | Kleine Mods: Obsidian Plates, Chest Transporter, ArmorBar, BuffBar, Back Tools, Soul Shards | ~5.000 | 3 | Sonnet 5.5, niedrig | offen |
| 5 | Hexxit Gear, More Bows, Enchanting Plus, offLawn, Wild Caves 3 | ~13.000 | 1, 3 | Opus 5.5, mittel | offen |
| 6 | bspkrsCore, TreeCapitator, Inventory Tweaks, Custom LAN Ports | ~19.000 | 3 | Opus 5.5, mittel | offen |
| 7 | Natura | ~20.000 | 1, 3 | Opus 5.5, mittel | offen |
| 8 | Artifice, CoFHCore (nur was das Pack nutzt) | ~27.000 | 1, 3 | Opus 5.5, mittel | offen |
| 9 | Harken Scythe, Asgard Shield, Asgard Shield Addons | ~30.000 | 3 | Opus 5.5, mittel | offen |
| 10 | Project Zulu A: Kreaturen | ~25.000 | 3 | Fable 5.2, hoch | offen |
| 11 | Project Zulu B: Blöcke, Items, Weltgenerierung, Spawns | ~18.000 | 10 | Opus 5.5, mittel | offen |
| 12 | Better Dungeons A: Bau-Engine und Gebäude-Import | ~18.000 | 1 | Fable 5.2, hoch | offen |
| 13 | Better Dungeons B: Dungeon-Typen, Mobs, Loot | ~18.000 | 12 | Fable 5.2, hoch | offen |
| 14 | Battle Towers, Ruins (mit allen Ruinen-Vorlagen) | ~5.000 + Vorlagen | 1 | Opus 5.5, mittel | offen |
| 15 | Dimensional Doors | ~11.000 | 2 | Opus 5.5, mittel | offen |
| 16 | BiblioCraft | ~21.000 | 3 | Fable 5.2, hoch | offen |
| 17 | BetterStorage, Backpacks, qCraft | ~17.000 | 3 | Opus 5.5, mittel | offen |
| 18 | ChocoCraft, Infernal Mobs | ~16.000 | 1, 3 | Opus 5.5, mittel | offen |
| 19 | LegendGear, Falling Meteors | ~19.000 | 1, 3 | Opus 5.5, mittel | offen |
| 20 | Xeno's Reliquary, Secret Rooms | ~18.000 | 3 | Opus 5.5, mittel | offen |
| 21 | Immibis Core, Immibis's Microblocks | ~26.000 | 3 | Fable 5.2, extra hoch | offen |
| 22 | Client-Mods: Damage Indicators, MapWriter | ~12.000 | – | Sonnet 5.5, niedrig | offen |
| 23 | Gesamttest und Modpack-Export | – | alle | Opus 5.5, mittel | offen |
| 24 | später: Twilight Forest | ~50.000 | – | Fable 5.2, hoch | zurückgestellt |
| 25 | später: Tinkers' Construct | ~60.000 | – | Fable 5.2, hoch | zurückgestellt |

Entfallen ganz: Not Enough Items, NEI Plugins, CodeChickenCore (durch JEI ersetzt), Lumy Skin Patch,
`basemods` (Forge 1.5.2 selbst).

Die Reihenfolge ab Teil 4 ist frei wählbar, solange „Braucht“ erfüllt ist.

---

## Teil 1 – Vanilla-1.5.2-Dekoration

**Ziel:** Die Oberfläche sieht nach der Dekoration aus wie in 1.5.2, nicht nur das nackte Gelände.

- Den `populate`-Schritt von `ChunkProviderGenerate` und `BiomeDecorator` nachbauen, mit derselben Zufallsfolge:
  - Seen (Wasser, Lava), Dungeons (Spawner + Truhen)
  - Erze, Ton, Sand, Kies
  - Bäume je Biom, Gras, Blumen, Pilze, Zuckerrohr, Kürbisse, Kakteen, Seerosen
  - Schnee und Eis
- Referenz: `python3 tools/reference.py --only minecraft`, dort die Klassen `BiomeDecorator`, `WorldGen*`, `BiomeGen*` und `SpawnerAnimals` (Tiere beim Generieren).
- Schnittstelle in `worldgen152` für Mod-Generatoren. Sie entspricht `IWorldGenerator`/`GameRegistry.registerWorldGenerator` und läuft in der Registrierungsreihenfolge des Originals. Dazu Hooks für die `TerrainGen`-Events, die Mods damals nutzten:
  - `DecorateBiomeEvent` (Tinkers, Natura)
  - `OreGenEvent` (CoFHCore)
- Biom-Dekoratoren müssen ersetzbar sein (ExtrabiomesXL hat eigene, siehe Teil 3).
- `tools/expect.sh` und `Expect.java` auf dekorierte Chunks erweitern. Bei der Dekoration ändert ein Chunk seine Nachbarn, deshalb mit fertig dekorierten Bereichen vergleichen.

**Fertig wenn:** Testserver mit Seed 1234 und die Vorhersage stimmen auch nach der Dekoration überein.

**Letzter Schritt von Teil 1:** einen schlanken Projekt-Skill `.claude/skills/worldgen152/SKILL.md` anlegen, höchstens etwa 150 Zeilen. Er ist für alle späteren Teile, die eigene Generatoren einhängen (3, 5, 7, 8, 11–14, 18, 19), und beschreibt:

- wie ein Mod einen 1.5.2-Generator in `worldgen152` anmeldet: API, Reihenfolge wie im Original, Zufall und Seed
- wie Biome, Terrain-Blöcke und Biom-Dekoratoren registriert oder ersetzt werden
- die Prüfung mit `tools/expect.sh` und `tools/verify_blocks.py`, auch für dekorierte Chunks
- die Stolperfallen aus Teil 1, zum Beispiel Dekoration über Chunk-Grenzen und Zufallsfolgen

In der Beschreibung des Skills soll stehen, wann er greift: beim Portieren von Weltgenerierung eines Hexxit-Mods. Nichts doppeln, was in `CLAUDE.md`, `docs/PLAN.md` oder im Skill `minecraft-modding` steht. Den Skill mitcommitten.

**Status: erledigt.** Seed 1234, je ein Chunk aller 49 Biome: 132.102 Blöcke, 784 Biom-Proben, 5 Spawner/Truhen
stimmen, 0 Abweichungen (2 Blöcke von fließendem Quellwasser erreicht, das zählt nicht als Abweichung).

So ist es gebaut (`hexxit.worldgen152.core.populate`, reines Java):

- `OldPopulator` = `ChunkProviderGenerate.populate` + `GameRegistry.generateWorld`, `OldDecorator` = `BiomeDecorator`,
  `Old*Gens` = die `WorldGen*`-Klassen, `OldWorld` = der Teil von `World`, den die Generatoren benutzen.
- Mod-Schnittstelle: `OldTerrainGen.registerWorldGenerator` (IWorldGenerator) und `OldTerrainGen.addListener`
  (Populate-, Decorate- und OreGen-Events, `false` = `Result.DENY`). Dekorator ersetzen: `OldBiome.decorator`,
  Baum-/Graswahl: `OldBiome.trees` / `.grass`, Zusatzschritt: `OldBiome.afterDecoration`.
- Neue Blöcke brauchen zwei Einträge: `OldBlockInfo.register*` (Material, Lichtdurchlass) und
  `OldBlockStates.register`/`registerMeta` (Blockzustand). Dungeon-Beute und -Mobs: `OldLoot`, Übersetzung der
  alten Item-IDs und Mob-Namen: `OldGameIds`.
- **Feste Reihenfolge statt Ladereihenfolge:** In 1.5.2 hing das Ergebnis davon ab, in welcher Reihenfolge Chunks
  geladen wurden. `OldDecoration` rechnet in Kacheln von 8×8 Chunks mit Rand (4 davor, 2 danach) und populiert
  x-aufsteigend, dann z-aufsteigend, wie der Original-Server sein Startgebiet. Gleicher Seed = gleiche Blöcke.

**Offen / bewusst abweichend:**

- Himmelslicht wird nur spaltenweise gerechnet (kein seitliches Ausbreiten, kein Blocklicht). Betrifft, wo Pilze
  und Blumen stehen dürfen. In 1.5.2 war das beim Generieren meist genauso, aber nicht immer.
- Der große Eichen-Generator merkt sich seine Höhengrenze nur innerhalb eines Chunks, nicht wie im Original über
  die ganze Sitzung (sonst wäre das Ergebnis wieder von der Reihenfolge abhängig).
- Quellen (`WorldGenLiquids`) werden als Quellblock gesetzt und fließen erst im Spiel los; der erste Fließschritt
  des Originals beim Generieren fehlt.
- Tiere beim Generieren kommen weiter von `NaturalSpawner` (gleicher Algorithmus, Spawn-Listen aus den Biom-Daten),
  nicht aus der 1.5.2-Zufallsfolge.
- ExtrabiomesXL-Biome benutzen bis Teil 3 den Standard-Dekorator (vereinzelte Eichen, Gras, Blumen).
- Strukturen fehlen noch (Teil 2); die Stelle in `OldPopulator` ist markiert. Sie verbrauchen Zufallszahlen vor den
  Seen, dadurch verschieben sich danach Seen und Dungeons in Chunks mit Strukturen.
- Wie man Generatoren einhängt und prüft, steht im Skill `.claude/skills/worldgen152/SKILL.md`.
- Client-Prüfung offen (Aussehen von Riesenpilzen, Ranken, Kakao, Kürbissen).

## Teil 2 – Vanilla-1.5.2-Strukturen und Abgleich

- Dörfer, Minenschächte, Festungen, Wüsten- und Dschungeltempel, Hexenhütten im 1.5.2-Stil und an den 1.5.2-Positionen (`MapGen*`, `ComponentVillage*`, `ComponentStronghold*`, ...).
- Spawnpunkt-Suche wie in 1.5.2 (`WorldChunkManager.findBiomePosition`, `getSpawnFuzz`).
- **Abgleich mit dem echten Original:**
  - Den Hexxit-1.5.2-Server einmal laufen lassen (Seed 1234, Java 8 von corretto.aws) und Chunks mit dem Port vergleichen.
  - In einer früheren Session hat das Sicherheitssystem den Start des Original-Servers blockiert. Wenn das wieder passiert, den Nutzer um Freigabe bitten. Alternative: die Region-Dateien (`.mca`, altes Format) einer Welt lesen, die der Nutzer selbst erzeugt hat.

**Fertig wenn:** Gelände, Dekoration und Strukturen einer Original-Welt stimmen mit dem Port überein. Erwartete Unterschiede (Zufall aus Mods, die noch fehlen) sind dokumentiert.

**Status: erledigt.** Der Port ist gegen Welten des echten Originals geprüft (Seed 1234), `tools/compare_original.py`:

| Original | verglichen | Ergebnis |
|---|---|---|
| Vanilla-Server 1.5.2: Spawngebiet, ein Dorf, eine Festung, Minenschächte | 1.023 Chunks, 67,0 Mio. Blöcke | 0,0046 % weichen ab (3.086 Blöcke), 932 Chunks ohne jede Abweichung; Spawnpunkt 293/58 gleich |
| Hexxit-Server 1.0.10: Spawngebiet | 331 Chunks, 84.736 Biom-Spalten | Biome 100 % gleich, Spawnpunkt 329/60 gleich; Blöcke siehe „Erwartete Unterschiede“ |
| Hexxit-Server 1.0.10: Gebiet des vorhergesagten Dorfs (Chunk -20/-28) | 49 Chunks | Dorf steht an der vorhergesagten Stelle, einzelne Häuser decken sich; der Grundriss weicht ab, siehe unten |

Im Vanilla-Abgleich stimmen Dorf, Festung und Minenschacht Block für Block, die Dungeon-Truhen Slot für Slot
(Gegenstand, Anzahl, Verzauberung). Was abweicht:

- große Eichen (Höhengrenze hängt im Original an der Sitzung, siehe Teil 1) und die Chunks, die das Original
  schon bei der Spawnsuche geladen hat (andere Reihenfolge, dadurch stehen einzelne Bäume anders);
- nicht mitgezählt, weil erst das laufende Spiel sie bewegt: fließendes Wasser und Lava samt Obsidian und
  Bruchstein, gefallener Sand und Kies, Schnee (28.128 Blöcke);
- am Rand des erzeugten Gebiets fehlen dem Original Nachbarn, die der Port zuerst populiert. Verglichen wird
  deshalb erst 3 Chunks innerhalb des populierten Gebiets (`--interior`).

Vanilla ohne Forge füllte Truhen anders als Forge 7.8 (Dungeon-Beute per `pickCheckLootItem`, Stapelgröße einmal
statt zweimal gewürfelt). Das Pack läuft mit Forge, der Port folgt Forge. Für den Vanilla-Abgleich schaltet
`tools/expect.sh <seed> vanilla ...` auf das Vanilla-Verhalten um (`OldDungeonGen.vanillaLoot`,
`OldLoot.vanillaDraws`) und lässt die Mod-Biome weg.

**Erwartete Unterschiede zur Hexxit-Welt** (Zufall aus Mods, die noch fehlen): Im Spawngebiet weichen 8,8 % der
Blöcke ab. Artifice ersetzt Stein schichtweise durch Basalt und Marmor (IDs 1004/1005, zusammen rund 800.000
Blöcke), CoFHWorld ersetzt die Vanilla-Erze, und jeder Mod-Generator verbraucht Zufallszahlen, wodurch auch
Erde-, Kies- und Erzadern der Vanilla-Dekoration woanders liegen. Das löst sich erst mit den Teilen 3, 5, 7, 8,
11–14, 18 und 19 auf; `compare_original.py` gegen `.cache/original/hexxit/world` ist dafür der Maßstab.

**Dörfer in der Hexxit-Welt haben einen anderen Grundriss als im Port,** solange die Dorf-Bauteile der Mods
fehlen: Tinkers' Construct (Werkzeugschmiede, Schmelzerei; zurückgestellt als Teil 25) und Better Dungeons
(Teil 12/13) hängen eigene Bauteile in die Auswahl. Schon deren Gewichte verbrauchen Zufallszahlen des Dorfs,
danach wählt das Dorf andere Häuser. Die Schnittstelle dafür ist da (`OldStructures.VILLAGE_HANDLERS`). Wer die
Dörfer vor Teil 25 originalgetreu haben will, muss die zwei Tinkers-Bauteile vorziehen. Forge hielt diese
Handler in einer Hash-Tabelle nach Klasse; ihre Reihenfolge wechselte im Original von Start zu Start.

**Ladereihenfolge der Mods im Original** (FML-Log des Hexxit-Servers; in dieser Reihenfolge melden sie ihre
Weltgeneratoren an): `mod_bspkrsCore`, `CoFHCore`, `CoFHWorld`, `TreeCapitator`, `ImmibisMicroblocksInstallCheck`, `DamageIndicatorsMod`, `AsgardShield_Core`, `ASAddon_Hallowed`, `HarkenScythe_Core`, `ASAddon_HarkenScythe`, `ASAddon_NetherEnder`, `ASAddon_ColorAPI`, `Artifice`, `ArtificeCompat|Chisel`, `ArtificeCompat|EE3`, `ArtificeCompat|Forestry`, `ArtificeCompat|MFR`, `ArtificeCompat|Railcraft`, `ArtificeCompat|Vanilla`, `BattleTowers`, `BetterDungeons`, `BiblioCraft`, `DimDoors`, `eplus`, `ExtrabiomesXL`, `hexxitgear`, `InfernalMobs`, `Meteors`, `More Bows`, `LegendGear`, `Natura`, `Natura|CompatMineFactoryReloaded`, `OffLawn`, `ProjectZulu|Core`, `ProjectZulu|Blocks`, `ProjectZulu|Dungeon`, `ProjectZulu|Mobs`, `ProjectZulu|World`, `xreliquary`, `rftl`, `AS_Ruins`, `secretroomsmod`, `SoulShards`, `TConstruct`, `TConstruct|CompatMineFactoryReloaded`, `WildCaves3`, `BetterStorage`, `Backpack`, `ChestTransporter`, `ImmibisCore`, `ObsidiPlates`, `QuantumCraft`, `TwilightForest`, `chococraft`.

Kern gegen Testserver (Seed 1234): 36 Chunks an Dörfern, Festungen, Minenschächten, Wüsten- und Dschungeltempel
(113.533 Blöcke, 35 Truhen/Spawner/Entitäten) und je ein Chunk aller 49 Biome (137.790 Blöcke): 0 Abweichungen.

So ist es gebaut:

- `hexxit.worldgen152.core.structure`: die Struktur-Klassen des Originals (`StructureComponent`, `ComponentVillage*`,
  `ComponentStronghold*`, `ComponentMineshaft*`, `ComponentScatteredFeature*`, `Structure*Pieces`) mit einem Skript
  aus der dekompilierten Vorlage umgesetzt. Klassen- und Methodennamen sind die des Originals, damit sich jede
  Zeile gegen `.reference/minecraft/src` halten lässt; Parameter heißen deshalb `var1`, `par2` usw.
- `OldStructures` = `MapGenStructure` + `MapGenVillage`/`Stronghold`/`Mineshaft`/`ScatteredFeature` und die
  Biom-Fragen des `WorldChunkManager` (`areBiomesViable`, `findBiomePosition`), dazu die Spawnsuche
  (`WorldServer.createSpawnPosition`). `OldPopulator` ruft sie vor den Seen auf, wie das Original.
- `OldWorld` kann jetzt, was die Strukturen von der Welt brauchten: Türen setzen, Fackeln ausrichten oder fallen
  lassen, Schienen verbinden (`OldRails` = `BlockBaseRailLogic`), Ofen und Werfer drehen, Entitäten ablegen.
- Beute: `OldLoot` hat die acht Kategorien von `ChestGenHooks` (Mineshaft, Tempel, Festung, Schmied).
- Spielseite: `OldBlockStates` kennt alle Struktur-Blöcke, `OldChunkWriter` setzt Türhälften zusammen, lässt Zäune,
  Scheiben, Treppen und Drähte ihre Nachbarn finden und schreibt Werfer, Dorfbewohner, Hexen und Truhenloren.
  Der Weltspawn kommt über `LevelEvent.CreateSpawnPosition`, Enderaugen und `/locate` finden die 1.5.2-Festungen.
- ExtrabiomesXL meldet seine Biome wie im Pack als Dorf-, Festungs- und Spawn-Biome an (`EbxlBiomes`).
- Prüfwerkzeuge: `tools/expect.sh <seed> structures <radius>` und `... spawn`, `vanilla` als zweites Argument
  rechnet ohne Mod-Biome. `verify_blocks.py` lädt Chunks einzeln, hält die Welt mit `tick freeze` an und prüft
  auch Entitäten.

**Offen:**

- Original-Server starten: `tools/original_server.sh <vanilla|hexxit> <sekunden>` (Java 8 und Server liegen unter
  `.cache/original/`, in `.claude/settings.json` freigegeben). Der Server erzeugt ohne Spieler nur das Gebiet um den
  Spawn; `tools/original_spawn.py <welt> <x> <z>` verlegt ihn vor dem nächsten Start. Server-Paket von Hexxit:
  `https://servers.technicpack.net/Technic/servers/hexxit/Hexxit_Server_v1.0.10.zip` nach `.cache/original/hexxit/`
  entpacken, `Hexxit.jar` als `server.jar` kopieren, `level-seed=1234` und `online-mode=false` setzen.
- Reihenfolge: In 1.5.2 entstand ein Strukturanfang erst, wenn ein Chunk in seiner Nähe erzeugt war, und
  überlappende Anfänge wurden in der Reihenfolge einer Hash-Tabelle gebaut. Hier gibt es alle Anfänge von Beginn
  an, überlappende werden nach Chunk-x, dann -z gebaut.
- Bauteile merken sich Zustand (Bodenhöhe eines Hauses, gesetzte Truhe, entferntes Festungsteil bei Wasser).
  Jede Kachel von 8×8 Chunks rechnet mit frischen Bauteilen; an Kachelgrenzen kann ein Haus deshalb um einen
  Block anders liegen, als es das Original bei seiner Ladereihenfolge gebaut hätte.
- Die Spawnsuche sieht das nackte Gelände. Im Original waren einzelne Chunks dabei schon dekoriert.
- Nachbar-Updates nach dem Setzen einer Tür und beim Stolperdraht sind nicht nachgebildet.
- Dorfbewohner haben die Berufe von heute (Schmied = Waffenschmied) und deren Handel, nicht den Handel von 1.5.2.
  Hexenhütten haben keine eigene Spawnliste für Hexen. Für eigene Dorfblöcke je Biom gibt es
  `OldStructures.villageBlocks`.
- Hexenhütte: im Umkreis von 220 Chunks um 0/0 gibt es mit den Pack-Biomen keine, sie ist nur über die
  Blockzuordnung geprüft, nicht am Server.
- Client-Prüfung offen (Aussehen von Türen, Schienen, Fackeln, Stolperdraht, Truhenloren).

## Teil 3 – JEI-Grundlage und ExtrabiomesXL fertig

- JEI für 26.3 als Abhängigkeit einbinden:
  - in `build.gradle`: `compileOnly` für die API, Runtime in den Runs
  - Version von Modrinth oder dem JEI-Maven
- Kleines gemeinsames Muster, wie ein Mod ein JEI-Plugin anmeldet. Am besten als Vorlage in `docs/`, kein eigenes Bibliotheks-Mod nötig.
- ExtrabiomesXL vollständig:
  - Hölzer (Acacia, Fir, Redwood), Blätter, Setzlinge mit Baumformen (inklusive Viertel-Stämme der Redwoods)
  - Blumen, Gräser, Rohrkolben, Laubhaufen, Quicksand
  - Treppen, Stufen, Mauern, Scarecrow
  - eigene Biom-Dekoratoren (Zahlen aus `extrabiomes.cfg` → decoration)
  - Generatoren: Marsh, Mountain Desert, Mountain Ridge, VanillaFlora, LegendOak
- Abgleich mit der Terrain-Prüfung, jetzt inklusive Dekoration.

## Teil 4 – Kleine Mods

Obsidian Pressure Plates, Chest Transporter, ArmorBar, BuffBar, Back Tools, Soul Shards.

Drei davon sind reine Client-HUDs: ArmorBar, BuffBar und Back Tools. Back Tools zeigt Werkzeuge am Rücken. Das braucht das neue Render-System, siehe Skill `minecraft-modding`, Abschnitt „GUI / render cookbook“.

## Teil 5 – Hexxit Gear, More Bows, Enchanting Plus, offLawn, Wild Caves 3

- Hexxit Gear:
  - Rüstungs-Sets mit Fähigkeiten
  - Hexbiscus-Pflanze per Weltgenerierung
  - Truhen-Loot
- Enchanting Plus: eigene GUI und Rezepttyp → JEI.
- offLawn (Sonnenblumen) und Wild Caves 3 (Höhlen-Deko) hängen sich in `worldgen152` ein.

## Teil 6 – bspkrsCore, TreeCapitator, Inventory Tweaks, Custom LAN Ports

- bspkrsCore wird nur so weit portiert, wie TreeCapitator es braucht.
- Inventory Tweaks: Sortieren und Auto-Refill, Regeln aus `InvTweaksRules.txt` des Packs.
- Custom LAN Ports: Port beim Öffnen fürs LAN frei wählbar.

## Teil 7 – Natura

- Bäume, Beeren, Wolken, Baumwolle, Gerste
- Nether-Inhalte (Netherite-Chunkprovider war eine eigene Nether-Variante → prüfen, wie das ins Pack passte)
- Weltgeneratoren in Originalreihenfolge
- JEI für eigene Rezepte

Kein Tinkers vorhanden: Integrationen dazu weglassen und notieren.

## Teil 8 – Artifice, CoFHCore

- Artifice: Steine, Marmor, Basalt, Lotus, Upgrade-Werkzeuge, Weltgeneration.
- CoFHCore: nur, was das Pack tatsächlich nutzt, zum Beispiel die Erz-Konfiguration (`config/cofh`).

## Teil 9 – Harken Scythe, Asgard Shield, Addons

- Harken Scythe: Seelen, Sensen, Altäre, Zauber.
- Asgard Shield samt Farb-API.
- Die drei Addons: Nether-Ender, Hallowed, HarkenScythe.

## Teil 10 und 11 – Project Zulu

- **Erinnerung an den Nutzer zu Beginn von Teil 10:** Er wollte auf den Blockbench-MCP hingewiesen werden (https://github.com/sosadly/blockbench-mcp). Damit lassen sich Mob-Modelle direkt in Blockbench bauen. Der Server braucht ein laufendes Blockbench auf dem eigenen Rechner. Vor dem Start fragen, ob er ihn einrichten will.
- **Teil 10:** Kreaturen. Modelle, Renderer, KI, Spawns je Biom. Die Pack-Konfiguration liegt in `config/Project Zulu`.
- **Teil 11:** Blöcke, Items, Rezepte, Weltgeneration (Pyramiden, Labyrinthe, ...), Mob-Spawn-Abgleich.

## Teil 12 und 13 – Better Dungeons (Chocolate Quest)

- **Teil 12:** Bau-Engine und Import der Gebäude- und Dungeon-Vorlagen aus `betterdungeons-data1/2` (`Chocolate/`).
- **Teil 13:** Dungeon-Typen, Wächter, Mobs, Loot (`ChestConfig`), Platzierung in der Welt.
- **Am Ende von Teil 13 den Nutzer erinnern** (von ihm so gewünscht): Die Dörfer haben erst dann den Grundriss
  des Originals, wenn auch die zwei Dorf-Bauteile von Tinkers' Construct da sind (Werkzeugschmiede, Schmelzerei;
  sonst erst Teil 25). Fragen, ob sie jetzt vorgezogen werden sollen, und das Dorf-Bauteil von Better Dungeons
  (`VillageHandler`) über `OldStructures.VILLAGE_HANDLERS` einhängen. Hintergrund: Teil 2, „Dörfer in der Hexxit-Welt“.

## Teil 14 – Battle Towers, Ruins

Ruins bringt rund 1.800 Vorlagen (`.tml`) aus `ruins-data1..3` mit. Dafür einen Konverter bauen, der die Vorlagen beim Bauen übernimmt. Sie kommen nicht ins Repo.

## Teil 15 – Dimensional Doors

Limbo, Pocket-Dimensionen, Rifts. Braucht Teil 2, weil eigene Dimensionen den Weltgenerator erweitern.

## Teil 16 – BiblioCraft

Viele Blöcke mit eigenen Modellen und GUIs. Ein Großteil der Arbeit ist Client-Rendering.

## Teil 17 – BetterStorage, Backpacks, qCraft

- BetterStorage: verschlossene Truhen, Schlüssel, Rüstungsständer, Crates.
- Backpacks.
- qCraft: Quanten-Blöcke, Observer-Dust.

## Teil 18 – ChocoCraft, Infernal Mobs

- ChocoCraft: Chocobos (Zucht, Reiten, Farben) und Gysahl-Pflanzen in der Welt.
- Infernal Mobs: Elite-Mobs mit Modifikatoren, Loot.

## Teil 19 – LegendGear, Falling Meteors

- LegendGear: Zelda-Ausrüstung, Bombenblumen und Sträucher per Weltgenerierung.
- Falling Meteors: Meteoriten-Events und -Erze.

## Teil 20 – Xeno's Reliquary, Secret Rooms

- Xeno's Reliquary: Artefakte, Handgun, Alkahest-Rezepte → JEI.
- Secret Rooms: Tarnblöcke. Die Tarnung zeigt das Modell eines anderen Blocks und ist damit ein Render-Thema.

## Teil 21 – Immibis Core, Immibis's Microblocks

Mikroblöcke sind der schwierigste technische Teil. Sie brauchen Teilblock-Kollision, eigenes Rendering und die Säge.

## Teil 22 – Client-Mods

- Damage Indicators: Lebensanzeige über Mobs.
- MapWriter: Minimap und Weltkarte.

## Teil 23 – Gesamttest und Modpack

- Alle Konfigurationen aus `z-hexxit-configs` übernommen? Liste abhaken.
- Testserver mit allen Mods, Welt mit Seed 1234, Terrain-Prüfung, RCON-Stichproben.
- Modpack bauen:
  - Jar-Ordner und Server-Paket
  - Import für den CurseForge- oder Prism-Launcher
  - Hinweis: Die Jars enthalten Original-Assets, nur für den eigenen Gebrauch.
- Client-Prüfliste für den Nutzer.
