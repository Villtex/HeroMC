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
   - `graphify update .` ausführen, dann committen und direkt auf den Hauptbranch pushen (`CLAUDE.md`, Abschnitt „Branches“).
   - Was offen bleibt, beim Teil unter „Offen“ notieren.

Was sich nur im Client prüfen lässt (Modelle, GUIs, Render), wird als „Client-Prüfung offen“ vermerkt.

## Übersicht

| Teil | Inhalt | Zeilen (Original) | Braucht | Modell, Aufwand | Status |
|---:|---|---:|---|---|---|
| 0 | Grundgerüst, RottenFleshToLeather, 1.5.2-Gelände, ExtrabiomesXL-Biome | – | – | – | ✅ erledigt |
| 1 | Vanilla-1.5.2-Dekoration und Schnittstelle für Mod-Weltgeneratoren | ~3.000 | 0 | Fable 5.2, extra hoch | ✅ erledigt |
| 2 | Vanilla-1.5.2-Strukturen, Spawnpunkt, Abgleich mit Original-Welt | ~6.000 | 1 | Fable 5.2, extra hoch | ✅ erledigt |
| 3 | JEI-Grundlage, ExtrabiomesXL fertig (Bäume, Pflanzen, Holz, Scarecrow) | ~9.000 | 1 | Opus 5.5, mittel | ✅ erledigt |
| 4 | Kleine Mods: Obsidian Plates, Chest Transporter, ArmorBar, BuffBar, Back Tools, Soul Shards | ~5.000 | 3 | Sonnet 5.5, niedrig | ✅ erledigt |
| 5 | Hexxit Gear, More Bows, Enchanting Plus, offLawn, Wild Caves 3 | ~13.000 | 1, 3 | Opus 5.5, mittel | ✅ erledigt |
| 6 | bspkrsCore, TreeCapitator, Inventory Tweaks, Custom LAN Ports | ~19.000 | 3 | Opus 5.5, mittel | ✅ erledigt |
| 7 | Natura | ~20.000 | 1, 3 | Opus 5.5, mittel | ✅ erledigt |
| 7b | Natura-Nether: 1.5.2-Nether (Netherite-Gelände, Höhlen, Netherfestung) in worldgen152, Nether-Generatoren von Natura | ~3.000 | 2, 7 | Fable 5.2, hoch | ✅ erledigt |
| 8 | Artifice, CoFHCore (nur was das Pack nutzt) | ~27.000 | 1, 3 | Opus 5.5, mittel | ✅ erledigt |
| 9 | Harken Scythe, Asgard Shield, Asgard Shield Addons | ~30.000 | 3 | Opus 5.5, mittel | ✅ erledigt |
| 10 | Project Zulu A: Kreaturen | ~25.000 | 3 | Fable 5.2, hoch | ✅ erledigt |
| 11 | Project Zulu B: Blöcke, Items, Weltgenerierung, Spawns | ~18.000 | 10 | Opus 5.5, mittel | offen |
| 12 | Better Dungeons A: Bau-Engine und Gebäude-Import | ~18.000 | 1 | Fable 5.2, hoch | ✅ erledigt |
| 13 | Better Dungeons B: Dungeon-Typen, Mobs, Loot | ~18.000 | 12 | Fable 5.2, hoch | ✅ erledigt |
| 14 | Battle Towers, Ruins (mit allen Ruinen-Vorlagen) | ~5.000 + Vorlagen | 1 | Opus 5.5, mittel | ✅ erledigt |
| 15 | Dimensional Doors | ~11.000 | 2 | Opus 5.5, mittel | ✅ erledigt |
| 16 | BiblioCraft | ~21.000 | 3 | Fable 5.2, hoch | ✅ erledigt |
| 17 | BetterStorage, Backpacks, qCraft | ~17.000 | 3 | Opus 5.5, mittel | ✅ erledigt |
| 18 | ChocoCraft, Infernal Mobs | ~16.000 | 1, 3 | Opus 5.5, mittel | ✅ erledigt |
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

**Außerhalb der Teile – Rezept für den verzauberten goldenen Apfel** (`mods/worldgen152`, ✅ erledigt): acht Goldblöcke um
einen Apfel wie in 1.5.2 (`data/minecraft/recipe/enchanted_golden_apple.json`), am Server über den Crafter geprüft.

**Außerhalb der Teile – Nahkampf wie 1.5.2** (`mods/combat152`, ✅ erledigt), nach `EntityPlayer`/`EntityMob`/
`EnchantmentHelper` von 1.5.2:
- keine Angriffs-Abklingzeit (Grund-Angriffsgeschwindigkeit des Spielers 1024, Basis-Tempo der Waffen entfernt),
  Abklingzeit-Anzeige im Client fest auf „Aus“, kein Schwungangriff, keine `entity.player.attack.*`-Geräusche;
- Kritische Treffer auch beim Sprinten, `+rand(i/2+2)` statt ×1,5; Äxte 3 + Material, Schaufeln 1 + Material;
- Stärke `+(3 << Stufe)`, Schwäche `−(2 << Stufe)` (Korrektur-Modifikator auf `attack_damage` aller Lebewesen);
- Schärfe/Bann/Nemesis der Gliederfüßer: `1 + rand(Summe)` mit `floor(2,75·L)` bzw. `floor(4,5·L)`, für Spieler und
  Mobs; die 26.3-Effekte sind in `data/minecraft/enchantment` geleert (Nemesis verlangsamt nicht mehr);
- Angriffs-Rückstoß (Sprint + Rückstoß-Verzauberung `j`): `addVelocity(-sin·j·0,5, 0,1, cos·j·0,5)` statt `knockback()`;
- Blocken mit dem Schwert (Rechtsklick): alle Schwerter (Werkzeug mit Spinnennetz-Regel + `WEAPON`, ohne eigenes
  `use`) bekommen `BLOCKS_ATTACKS` ohne eigene Minderung, Schaden `(1 + d) >> 1` vor der Rüstung von allen Seiten,
  außer `bypasses_armor`; kein Rückstoß für den Blockenden, keine Axt-Sperre.
Per FakePlayer geprüft (Schläge im selben Tick, Kritisch-Mittel, Schärfe V 1–13, Stärke III 19, Schwäche I 5,
Rückstoß 0,9/0,1, Blocken 1→1, 2→1, 7→4, 8→4, Fall 8→8). Better Dungeons (Schilde, Banner, Moonlight/Morning
Shine/Walker-Schwert) blockt über `Combat152.BLOCKING` (`mod_requires=combat152`). Client-Prüfung offen (Schwert-Haltung beim Blocken, keine Anzeige).

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
fehlen: Tinkers' Construct (Werkzeugschmiede, Schmelzerei; zurückgestellt als Teil 25) hängt eigene Bauteile in
die Auswahl (Better Dungeons nicht: sein `VillageHandler` ist nur ein Handels-Handler, Teil 13). Schon deren Gewichte verbrauchen Zufallszahlen des Dorfs,
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

**Status: erledigt.**

- **JEI** 31.17.0.113 (`jei_version` in `gradle.properties`, Maven `maven.blamejared.com`, braucht dort auch die
  Gruppe `net.mezzdev.config`): API als `compileOnly` in jedem Teilprojekt, JEI selbst in den Runs des Packs.
  Vorlage: `docs/JEI.md`. ExtrabiomesXL hat nur Werkbank- und Ofenrezepte; sein Plugin
  (`extrabiomes/compat/EbxlJeiPlugin.java`) zeigt Info-Seiten (Log Turner, 2x2-Setzlinge, Quicksand, Scarecrow).
- **Weltgenerierung** (`extrabiomes/gen`, reines Java, auch in `tools/expect.sh`): Dekorator-Zahlen je Biom
  (`DecorationSettings`, die Pack-Konfiguration wiederholt sie nur), gewichtete Baum- und Graswahl
  (`BiomeManagerImpl`), alle Bäume (`EbxlTreeGens`), Kleinpflanzen und Generatoren (`EbxlPlantGens`,
  `EbxlWorldGen`): Cattail, Flower, LeafPile, Marsh, MountainDesert, MountainRidge, VanillaFlora, LegendOak, Quicksand.
- **Blöcke**: jede Metadaten-Variante ist ein eigener Block (Namen in `EbxlBlocks`, alte IDs in `Extrabiomes`
  und `tools/verify_blocks.py`). Viertel-Stämme: ein Block je Holz mit `bark` (nw/ne/sw/se) und `axis`.
  Doppelstufen sind der `double`-Zustand der Stufe. Laub hat `check_decay` (das Verrottungs-Bit von 1.5.2, siehe
  unten). Rezepte, Beute, Tags und Übersetzungen als Daten; 62 Texturen aus dem Original.
- **Setzlinge** wachsen mit den 1.5.2-Generatoren: `hexxit.worldgen152.OldLiveGen` kopiert die Umgebung als alte IDs
  in eine `OldWorld`, lässt den Generator laufen und schreibt die Änderungen zurück. Fir und Redwood werden aus
  2x2 Setzlingen zu den großen Bäumen, ein einzelner Redwood-Setzling wächst nicht (wie im Original).
- **Scarecrow**: Entity (schaut Spieler und Mobs an, bleibt stehen), Item, Modell und Renderer.
- **Prüfung**: Seed 1234, je ein Chunk aller 49 Biome: 150.400 Blöcke, 784 Biom-Proben, 8 Spawner/Truhen,
  0 Abweichungen. Quicksand (Chunk -18/-24) und eine Legend Oak (Chunk 1095/20): 30.158 Blöcke, 0 Abweichungen;
  25 Chunks Mini Jungle: 75.261 Blöcke, 0 Abweichungen. Am Server per RCON: alle Setzlinge wachsen, auch 2x2
  Redwood und Fir; ein stehender Baum verliert kein Laub, ein gefällter verrottet; Beute und Scarecrow geprüft.
  Server startet mit JEI ohne Fehler bis `Done`.

**Offen / bewusst abweichend:**

- Forge hielt die Weltgeneratoren in einem `HashSet`; ihre Reihenfolge (auch gegenüber anderen Mods) wechselte
  im Original von Start zu Start. Hier: Registrierungsreihenfolge von ExtrabiomesXL (Cattail, Flower, LeafPile,
  Marsh, MountainDesert, MountainRidge, VanillaFlora, LegendOak, Quicksand).
- Cattail- und LegendOak-Generator fragen das Biom im Original an (x, x) statt (x, z) ab. Übernommen; dadurch
  gibt es Rohrkolben nur in Chunks, deren Diagonal-Punkt im Green Swamp liegt: mit Seed 1234 auf 4.000 Chunks
  Diagonale keiner, der Generator ist deshalb nicht am Server geprüft (nur die Blockzuordnung). Legend Oaks nur
  in Chunks, deren Diagonal-Punkt Forest, Forested Hills/Island oder Rainforest ist (selten). Melonen
  (VanillaFlora, 1 in 48 Chunks) kamen in keinem geprüften Chunk vor.
- Die Äste der Legend Oak reichen bis 3 Chunks weit; `OldDecoration.MARGIN_HIGH` ist deshalb 3 statt 2 (etwa 15 %
  mehr Rechenzeit je Kachel).
- Big Autumn Tree hält wie die große Eiche seine Höhengrenze nur einen Chunk lang (`OldWorld.chunkState`).
- WorldGenFirTreeHuge gab im Original neben nicht geladenen Chunks auf; hier sind alle Chunks da.
- Laub verrottet nur mit `check_decay`: gesetzt, wenn ein Nachbar zu Luft wird, und weitergegeben an
  angrenzendes markiertes Laub. Ohne das würden die äußeren Blätter großer Redwoods und Firs (bis 8 Blöcke vom
  Stamm, heute reicht Laub 6) von selbst verrotten. Fallraten: Setzling 1/20 (grünes Laub zusätzlich 1/90),
  Herbstlaub Apfel 1/200.
- Viertel-Stämme per Hand: Die Rinde zeigt zum angeklickten Viertel; das Original suchte passende Nachbarn.
  Log Turner dreht Stämme, bei Viertel-Stämmen die Rinden-Ecke.
- Farben: Fir-Laub fest (Kiefernfarbe 1.5.2), Akazie fest (`0xAEA42A`, Farbkarte bei 0,9/0,1), Redwood und
  Laubhaufen nach Biom; Herbstlaub ungefärbt.
- Client-Prüfung offen: alle Modelle und Texturen, besonders Viertel-Stämme, Laubhaufen, Rohrkolben, Scarecrow
  (Modell, Größe 1,25), Farben, JEI-Info-Seiten.

## Teil 4 – Kleine Mods

Obsidian Pressure Plates, Chest Transporter, ArmorBar, BuffBar, Back Tools, Soul Shards.

Drei davon sind reine Client-HUDs: ArmorBar, BuffBar und Back Tools. Back Tools zeigt Werkzeuge am Rücken. Das braucht das neue Render-System, siehe Skill `minecraft-modding`, Abschnitt „GUI / render cookbook“.

**Status: erledigt.** Sechs Teilprojekte, Mod-IDs wie im Original in Kleinbuchstaben (`obsidiplates`, `chesttransporter`,
`soulshards`, `mod_armorbarmod`, `mod_buffbarmod`, `mod_backtools`). Werte der Pack-Konfiguration als Standard.

- **Obsidian Pressure Plates**: acht Blöcke (Obsidian = nur Spieler, Moos = nur Mobs, je normal, still, verhüllt,
  beides). Härte 12,5 bzw. 2, Erfassungsbereich und 20-Tick-Takt des Originals. Still: Wolle an Seiten und Unterseite,
  kein Klick. Verhüllt: `force_translucent` + `neoforge_data.color` im Modell, Alpha 0,11 in der Welt (`shroudAlpha`),
  0,5 im Inventar. Rezepte und Schmelzen (zurück zur normalen Platte) als Daten. Namen wie im Original
  („Pressure Plate (Silent)“ …); JEI-Infoseiten sagen, wer die Platte drückt.
- **Chest Transporter**: Inhalt als `CONTAINER`-Komponente (Slots bleiben), Rechtsklick über
  `PlayerInteractEvent` vor dem Öffnen der Truhe, Ablegen wie ein Truhen-Item, danach bricht der Transporter. Truhenlore
  auf- und abladen. Last: Langsamkeit III, Abbaulähmung IV, Hunger I.
- **Soul Shards**: Seelensplitter mit Komponente `soulshards:soul` (Mob, Seelen), Stufen ab 64/128/256/512/1024,
  Seelenrune (Glowstone, Netherrack, Endstein, Diamant), Spawner des Mobs geben 200 Seelen, Soul Cage als Block-Entity mit
  der Spawn-Logik des Originals (Anzahl, Takt, Obergrenze, Spawnbedingungen, Redstone nur bei Stufe 5), Verzauberung
  Soul Stealer (Daten, nicht am Verzauberungstisch), Shadow Blade und Shadow Pickaxe (3x3x3), JEI-Infoseiten.
- **ArmorBar, BuffBar**: HUD-Ebenen über der Hotbar (`RegisterGuiLayersEvent`), alle Einstellungen der Pack-Konfiguration
  als Client-Konfiguration; Texturen aus dem Original.
- **Back Tools**: Zustand je Spieler wie `BT_EntityBackItem` (Client-Tick), über `registerAvatarEntityModifier` in den
  Render-State, eigene `RenderLayer` am Spielermodell. Nicht unter einem Umhang, nicht unsichtbar, nicht im Bett.
- **Prüfung**: `clean pack` grün, Server bis `Done` ohne Fehler und Warnungen der neuen Mods. Per RCON: Moos-Platte
  geht bei einem Zombie an und nach 20 Ticks aus, Obsidian-Platte nicht; Soul Cage Stufe 5 spawnt Skelette bis zur
  Obergrenze 12, mit Redstone keine, ohne wieder; Beute der Blöcke; Soul Stealer existiert im Register.

**Offen / bewusst abweichend:**

- Nicht ohne Spieler prüfbar (nur im Client): Obsidian-Platte mit Spieler, Truhe aufnehmen und ablegen, Truhenlore,
  Seelen beim Töten, Seelenrune, Splitter in den Käfig und auf Spawner, Shadow Pickaxe.
- Sprungkraft beim Truhentragen: Sprungkraft −2 gibt es als Effekt nicht mehr (Stufen < 0 werden 0). Ersatz: Attribut
  `jump_strength` −0,1, solange ein voller Transporter im Inventar liegt (gleiche Sprunghöhe, kein Effekt-Symbol).
- Soul Shards: Witherskelett und Zombiedorfbewohner sind eigene Mobs statt „spezieller“ Skelette/Zombies (gleiches
  Verhalten). Twilight-Forest-Mobs der Liste fehlen bis Teil 24. Ein zweiter Splitter in einem Käfig ersetzt den ersten
  (der erste geht verloren, wie im Original). Die Persistenz ab Stufe 3 endet nach 600 Ticks Lebenszeit (1.5.2: Alter
  ohne Spieler in der Nähe). Shadow Blade und Shadow Pickaxe hatten keine Textur und kein Rezept: hier mit den Texturen
  von Diamantschwert und -spitzhacke, nur im Kreativmodus.
- Chest Transporter: Iron Chests und MultiPageChest waren nicht im Pack, nur die normale Truhe.
- BuffBar: Die Effektsymbole von 26.3 oben rechts bleiben zusätzlich sichtbar (gab es 1.5.2 nicht). ArmorBar zeigt die
  Gegenstände als Modell statt als flaches Symbol (bei Blöcken sichtbar). Die Konfigurationswerte des Originals wurden
  wegen eines Fehlers nie gelesen; hier wirken sie.
- Back Tools: Werkzeuge werden über Tags erkannt (Schwerter, Spitzhacken, Äxte, Schaufeln, Hacken) statt über Klassen;
  die Liste der Fremd-Mod-Klassen (von einem Server geladen) entfällt, eigene Einträge über `item_support`.
- Client-Prüfung offen: verhüllte Platten (Durchsichtigkeit), alle Item-Modelle, beide HUDs (Lage, Balken, Farben),
  Lage und Drehung der Werkzeuge am Rücken (beim Schleichen, mit Brustpanzer), JEI-Infoseiten.

## Teil 5 – Hexxit Gear, More Bows, Enchanting Plus, offLawn, Wild Caves 3

- Hexxit Gear:
  - Rüstungs-Sets mit Fähigkeiten
  - Hexbiscus-Pflanze per Weltgenerierung
  - Truhen-Loot
- Enchanting Plus: eigene GUI und Rezepttyp → JEI.
- offLawn (Sonnenblumen) und Wild Caves 3 (Höhlen-Deko) hängen sich in `worldgen152` ein.

**Status: erledigt.** Fünf Teilprojekte: `hexxitgear`, `morebows`, `eplus` (`mods/enchantingplus`), `offlawn`,
`wildcaves3`. Werte der Pack-Konfiguration als Standard.

- **worldgen152, Reihenfolge**: NeoForge baut die Mods parallel. `OldTerrainGen.registerWorldGenerator`, `addListener`
  und `OldLoot.Category.add` nehmen deshalb als erstes Argument die FML-ID des Originals und ordnen nach der
  Ladereihenfolge des Hexxit-Servers (`OldTerrainGen.LOAD_ORDER`), innerhalb eines Mods nach Anmeldung. Neu im Kern:
  Schädel (Block 144, `OldTile.Skull` mit Art und Drehung, `OldChunkWriter` setzt den Kopf), Item 397.
- **Hexxit Gear**: Hexical Essence und Diamond, drei Sets (Tribal, Scale, Thief) mit den Werten der Diamantrüstung,
  eigene Helm-Modelle über `IClientItemExtensions`. Set-Boni wie im Original alle 21 Ticks geprüft: Tribal Stärke I,
  Nachtsicht, Sprungkraft III; Scale Stärke I, Feuerresistenz, Resistenz I; Thief Stärke I, Nachtsicht, Lauftempo 0,2
  und Lufttempo 0,1. Hexbiscus (ID 2011) mit Generator `HGWorldGen`, droppt Hexical Essence. 13 Rezepte, JEI-Infoseiten.
- **More Bows**: acht Bögen mit Haltbarkeit, Spannzeit, Schadensfaktoren und Sonderschüssen des Originals (Blazing:
  Feuer 70 s; Legia: bis zu drei Pfeile; Ender: Salve aus sechs Pfeilen; Frost: eigene Entity `morebows:frost_arrow`
  mit Langsamkeit III, Eis und Schnee am Einschlag). Spannstufen als Item-Modell, Vanilla-Verzauberungen über Tags.
- **Enchanting Plus**: kein eigener Block, wie im Original ersetzt es den Verzauberungstisch: Rechtsklick öffnet die
  GUI von `GuiModTable` (Liste mit Stufen-Schiebern, Kosten, Reparatur, Wechsel zur Vanilla-GUI). Kostenformeln aus
  `ContainerEnchantTable`, Bücherregale, Zurücknehmen nur für auf dem Tisch erreichte Stufen (`eplus:restrictions`).
  Pakete als Payloads, der Server prüft alles nach. Server-Werte als `SYNCED`-Konfiguration, `/eplus`-Befehl, JEI-Kategorie
  mit Kosten je Stufe. Angeboten werden die 22 Verzauberungen von 1.5.2 und Soul Stealer (`#eplus:table`).
- **offLawn**: Rasen (grasfarben, breitet sich nicht aus, bleibt unter Bäumen, Hacke macht Ackerboden, Knochenmehl wie
  `LawnBonemeal`), Sonnenblume (zwei Blöcke, Licht 8 oder Himmel) mit essbarem Samen (2 Hunger), Bohnenranke
  (kletterbar, hält am Boden oder an der Decke, Verlängern mit Rechtsklick, Scheren zehnmal schneller). Generator
  `OffLawnGen` (1290): in den häufigen Biomen 1 in 10, in den normalen 1 in 32 Chunks ein Versuch.
- **Wild Caves 3**: 40 Blöcke (je Metadatenwert einer): Stein- und Sandstein-Stalaktiten/-Stalagmiten, Eiszapfen,
  Glowcaps, Glow weed, Iceshrooms, Bone pile. Halt, Formen und Drops wie im Original. Generator `WildCavesGen` mit den
  fünf Biom-Varianten (gefroren, trocken, Dschungel, normal, feucht), Fossilien als zweiter Generator, Schädel in der
  Beute von Dungeon, Minenschacht und Festung.
- **Prüfung**: `clean pack` grün; Server bis `Done` ohne Fehler und Warnungen der neuen Mods. Kern gegen Server
  (Seed 1234, frische Welt): 49 Biom-Chunks (157.943 Blöcke), 36 Struktur-Chunks (119.058 Blöcke, 137 Truhen,
  Spawner, Schädel, Entitäten), 28 Chunks mit Sonnenblumen und Hexbiscus (72.274 Blöcke): 0 Abweichungen. Per RCON:
  Stalaktit, Eiszapfen, Glowcap und Bohnenranke brechen ohne Halt (mit Drop), Beutetabellen (Samen, Bone pile,
  Hexical Essence), Frostpfeil, `/eplus`.

**Offen / bewusst abweichend:**

- Wild Caves 3 zog viele Entscheidungen mit ungeseedetem `new Random()`; das Original war also nicht seed-fest. Hier
  kommen diese Züge aus einer Folge je Chunk (`WildCavesGen.choices`, Welt-Seed und Chunk). Ein Abgleich mit einer
  Original-Welt kann für Wild Caves deshalb nicht übereinstimmen.
- Wild Caves: Lücke von einem Block setzte im Original das Stein-Stück an (x, y, y) statt (x, y, z), in einen fernen
  Chunk; das entfällt. Glowcaps in „normalen“ Höhlen gibt es wie im Original nie (das Feld wurde nie gesetzt), der
  Dschungel nimmt die Ranken-Wahrscheinlichkeit für Glowcaps, „feucht“ macht im Stalaktiten-Fall Schädel. Leuchtkraft
  der Flora 6 (das Original setzte 150, gespeichert wurde 150 & 15). Ranken hängen wie im Original ohne Halt in der
  Luft; das erste Nachbar-Update in 26.3 entfernt sie (zählt `verify_blocks.py` getrennt).
- offLawn: Statt der „falschen“ Pflanzen des Originals stehen die normalen Pflanzen auf dem Rasen (Tags
  `supports_vegetation`, `supports_sugar_cane`, `cannot_replace_below_tree_trunk`, Riesenpilze). Die Hacke braucht
  wie bei Erde Luft darüber (Tag `turns_into_farmland`). Auf der Bohnenranke wachsen alle kleinen Blumen (1.5.2 kannte
  nur zwei), gesetzt wie normale Blöcke auf die Oberseite.
- Hexxit Gear: Umhänge entfallen (kamen von einer abgeschalteten Webseite), damit auch das Netzwerk. Set-Boni zählen
  je Spieler (Original: ein Zähler für alle). Thief-Lufttempo einen Tick versetzt. Effekte wirken wie heute. Babys
  tragen die Rüstung unsichtbar. Truhen-Loot hat der Mod selbst nicht; die Teile kommen über Battle Towers und Infernal
  Mobs (alte IDs in `OldGameIds` angemeldet).
- More Bows: Ender Bow ohne das 3-Sekunden-Einfrieren des Servers; der Frostpfeil ist jetzt eine richtige Entity (im
  Original für den Client unsichtbar und nicht gespeichert); kein zweiter Treffer des Frostpfeils; Legia-Versatz unter
  einem Block.
- Enchanting Plus: Licht 15 des Vanilla-Tischs per Reflection (ungeprüft), Partikel über eine Abtastung im Client,
  neuere Vanilla-Verzauberungen (Mending usw.) nicht im Angebot, strengere Server-Prüfung, `/eplus changelog`,
  Thaumcraft- und TConstruct-Plugins entfallen.
- Nur mit Spieler prüfbar: Set-Boni, Schießen aller Bögen, Rasen hacken und düngen, Samen pflanzen, Bohnenranke
  verlängern, die Enchanting-Plus-GUI.
- Client-Prüfung offen: alle Modelle und Texturen (Helme, Bögen mit Spannstufen, Frostpfeil, Stalaktiten, Glowcaps,
  Sonnenblume, Rasenfarbe), Enchanting-Plus-GUI, JEI-Seiten und -Kategorie, Licht der Glowcaps und des Tischs.

## Teil 6 – bspkrsCore, TreeCapitator, Inventory Tweaks, Custom LAN Ports

- bspkrsCore wird nur so weit portiert, wie TreeCapitator es braucht.
- Inventory Tweaks: Sortieren und Auto-Refill, Regeln aus `InvTweaksRules.txt` des Packs.
- Custom LAN Ports: Port beim Öffnen fürs LAN frei wählbar.

**Status: erledigt.** Vier Teilprojekte: `mods/bspkrscore` (`mod_bspkrscore`), `mods/treecapitator`,
`mods/inventorytweaks`, `mods/customport` (`customlan`). Werte der Pack-Konfiguration als Standard.

- **bspkrsCore**: nur `bspkrs.util.BlockID` (`ns:block`, `ns:block[eigenschaft=wert]`, `#ns:tag`, Block wird erst
  beim Abgleich gesucht, unbekannte passen auf nichts). Konfigurations-Klassen, Update-Prüfung, HUD-Helfer entfallen.
- **TreeCapitator**: Fäll-Logik von `TreeCapitator.java` r14 (Stämme per Flutfüllung ab dem gefällten aufwärts,
  oberste Stämme je Ast, Blätter um sie herum, Smart Tree Detection, Axt-Schaden je Stamm, Scheren-Zweig, Schleich-
  Aktion, Kreativ-Optionen), Abbau langsamer mit Axt (`breakSpeedModifier` 0,256; `treeHeightDecidesBreakSpeed`
  aus wie im Pack). Statt ASM-Hook `BreakBlockEvent` (LOWEST, vor dem Entfernen), der gefällte Block selbst bricht
  normal. Bäume als Daten-Registry `treecapitator:tree` (zum Client synchronisiert), Merge-Regeln von
  `TreeRegistry.registerTree`; Vanilla und ExtrabiomesXL (`acacia`, `fir`, `redwood`, Herbstlaub an `vanilla_oak`/
  `vanilla_spruce`, Eichen-Viertelstämme) wie in `tree_and_mod_configs`. Äxte `#treecapitator:axes` (=
  `#minecraft:axes`), Scheren `#treecapitator:shears`. Konfiguration `treecapitator-server.toml` (SYNCED).
  Neuer Skill `treecapitator-trees` für spätere Teile.
- **Inventory Tweaks** (Subagent): Sortieren (Taste R, Middle-Click, Truhen-Knöpfe, Algorithmus von
  `InvTweaksHandlerSorting`), Regeln aus `InvTweaksRules.txt` des Packs, Item-Baum aus `InvTweaksTree.txt` mit
  26.3-IDs (`mods/inventorytweaks/tools/convert_tree.py`, alle 1658 Items haben einen Eintrag, oreDict → Tags),
  Auto-Refill, Shortcuts, Sortieren beim Aufheben, Einstellungsseiten, 20 Sprachen. Alles über normale Slot-Klicks,
  also auch auf fremden Servern. Eigene Truhen-Screens späterer Mods: IMC an `inventorytweaks`, Methode `chest`,
  Inhalt `"Klassenname;Zeilenbreite"` (Menüs, die von `ChestMenu`/`ShulkerBoxMenu`/`DispenserMenu` erben, gehen ohne).
- **Custom LAN 2.2**: Portfeld des Weltoptionen-Bildschirms (26.3 öffnet dort fürs LAN) startet mit dem gemerkten
  Port (`optionsCLP.txt`, 25565) statt einem zufälligen, der benutzte Port wird gespeichert. Knopf „Custom LAN
  Options“ (MOTD, Online-Modus, PvP) und zwei Seiten Befehlsrechte (`CLPermissions.txt`, 0 = alle, 2 = OPs, Standards
  des Originals). Befehle nur im integrierten Server: back, broadcast, explosion, fireball, fly, gettime, god, hat,
  heal, home, sethome, lightning, viewmotd, pm, reloadops, setspawn, spawn, spawnmob, setwarp, warp, removewarp,
  warplist; dazu die Server-Befehle von Vanilla, die ein LAN-Spiel sonst nicht hat (ban, ban-ip/banip, banlist,
  pardon/unban, pardon-ip/unban-ip/unbanip, op, deop, save-all/-off/-on, stop) mit den Rechten von Custom LAN.
- **Prüfung**: `clean pack` grün; Server bis `Done` ohne Fehler und Warnungen der neuen Mods (LAN-Befehle dort wie
  vorgesehen nicht registriert). TreeCapitator mit FakePlayer am Server: Eiche, große Eiche, Birke, Fichte, Dschungel,
  Riesenpilz, ExtrabiomesXL-Tanne fallen ganz (alle Stämme und Blätter, Axt-Schaden = Zahl der Stämme, Drops);
  persistente Blätter bleiben; Stamm ohne Blätter wird nicht als Baum erkannt; Abbaugeschwindigkeit ×0,256.

**Offen / bewusst abweichend:**

- TreeCapitator: zusätzliche Bäume, die 1.5.2 nicht kannte (Akazie, Schwarzeiche, Blasseiche, Mangrove, Kirsche,
  Karmesin- und Wirrpilz). Riesenpilze sind ein Baum (der Stiel ist heute ein Block für beide). Das „check decay“-Bit
  ist nachgebildet: nicht persistente Blätter höchstens 4 Blöcke von einem gefällten Stamm; bei Ranken bleibt der
  Bit-Test des Originals (Osten gesetzt, Norden nicht). Mit `maxHorLogBreakDist = -1` endete r14 nie; hier gelten
  die übrigen Grenzen trotzdem. Verzauberungsmodus (im Pack aus), IMC, MultiMine-Liste, ID Resolver und Fern-Konfig
  entfallen. Drops über die Loot-Tabellen mit der Axt (Behutsamkeit wirkt also). Baumdaten laden nur beim
  Serverstart. Bäume von Natura, Project Zulu, Twilight Forest, Äxte von Harken Scythe, Meteors, TConstruct: in
  deren Teilen (Skill `treecapitator-trees`).
- Inventory Tweaks: kein Server-Teil (Item-Tausch-Paket, `enableServerItemSwap` wirkungslos). Baum mit Item-IDs,
  Tags und Trank-Typen statt ID/Damage; Regeln nach ID heißen `D5 minecraft:stone`. Fehler des alten Baums
  korrigiert (Birke/Fichte vertauscht, Löwenzahn/Rose zeigten auf Schienen, Disc „wait“ fehlte). Neue Items in die
  passendste Kategorie. Mod-Werkzeuge über Werkzeug-Tags. Kein Sortieren im Kreativ-Inventar; die Sortiertaste wirkt
  nicht in Textfeldern. Werfen-Shortcut warf im Original zu viel (behoben). Auto-Refill wartet 200 ms im Client-Tick.
  Einstellungen als Bildschirm-Ebene. Weggelassen: Kompatibilität mit 1.5.2-Mods, NEI, Obfuskations-Helfer.
- Custom LAN: Spielerlimit (im integrierten Server von 26.3 fest 8), Flug, Tiere und NPCs (Einstellungen gibt es
  nicht mehr) und das Binden an eine IP (es lauscht auf allen Adressen) entfallen; PvP ist die Spielregel `pvp`.
  Der Knopf sitzt im Weltoptionen-Bildschirm statt in den Chat-Optionen. `/kick` und `/tp` sind die von Vanilla,
  `/reload` bleibt das Datapack-Neuladen. `/back` speichert den Todespunkt jetzt wirklich (im Original schrieb nichts
  `lastPos/*.pos`), mit Dimension. Abstürze des Originals behoben (Raycast ohne Treffer, Client-Aufrufe im Server,
  Welt 0 statt Welt des Spielers); `/spawnmob` sucht höchstens 256 Blöcke weit, Schweinezombie erreichbar, Warp-Namen
  ohne Pfadteile. Ban/Kick/Deop schützen den Host nicht mehr. Online-Modus wie im Original standardmäßig aus.
- Client-Prüfung offen: alle Bildschirme (Custom-LAN-Optionen und -Rechte, Inventory-Tweaks-Einstellungen,
  Truhen-Knöpfe), Sortieren, Shortcuts, Auto-Refill, LAN öffnen mit gemerktem Port, alle LAN-Befehle,
  TreeCapitator mit echtem Spieler (Abbaugeschwindigkeit im Client, Schleichen).

## Teil 7 – Natura

- Bäume, Beeren, Wolken, Baumwolle, Gerste
- Nether-Inhalte (Netherite-Chunkprovider war eine eigene Nether-Variante → prüfen, wie das ins Pack passte)
- Weltgeneratoren in Originalreihenfolge
- JEI für eigene Rezepte

Kein Tinkers vorhanden: Integrationen dazu weglassen und notieren.

**Status: erledigt** (ohne den 1.5.2-Nether, der ist Teil 7b). Teilprojekt `mods/natura` (`natura`).

- **Inhalte**: 13 Hölzer (Stämme mit `axis`, Bloodwood-Viertel, Redwood-Rinde/-Kern/-Wurzel, Planken, Blätter,
  Setzlinge, Treppen, Stufen, Zäune, Tore, Türen, Falltüren, Knöpfe, Druckplatten, Werkbänke, Bücherregale,
  Stöcke), Wolken, Saguaro, Beerenbüsche (Oberwelt, Nether), Gerste, Baumwolle, Bluebells, Thornvines,
  Glowshrooms klein/groß, Tainted Soil, Heat Sand, Topiary-Gras, Beutel, Beeren, Essen, Werkzeuge, Kamas, Bögen,
  Imp-Rüstung. 245 Rezepte, Loot, Tags, Brennbarkeit, Brennwerte (`cookingFuel`), Loot-Modifier für Samen aus Gras.
  Alle JSON und `original-assets.txt` erzeugt `mods/natura/tools/gen.py` (überschreibt `resources/{assets,data}`),
  `tools/check.py` prüft Verweise.
- **Mobs**: Imp, Heatscar Spider (Babys beim Tod, 2–4 laut Pack), Baby Heatscar Spider, Nitro Creeper; Spawns als
  Biome-Modifier für `#minecraft:is_nether` (seit Teil 7b auch `worldgen152:hell`). Vier Boote, Spawn-Eier, Imp-Fleisch.
- **Weltgenerierung** (`natura.gen`, ohne Spielklassen, in `expect.sh`): BaseCrop-, BaseCloud-, BaseTreeWorldgen
  unter FML-ID `Natura`, alle Gens als Port. Setzlinge und Glowshrooms wachsen über dieselben Gens
  (`OldLiveGen`, neu: ID/Meta der Mod-Blöcke, Tiefe, `OldBlockStates.registerConnecting` für Saguaro-Arme).
- **TreeCapitator**: 13 Bäume (Pack-Konfiguration plus Darkwood aus dem IMC des Originals), Hatchets in `#minecraft:axes`.
- **JEI**: keine eigenen Rezepttypen; Plugin meldet die 13 Werkbänke als Crafting-Stationen und Info-Seiten.
- **Prüfung**: `clean pack` grün; Server bis `Done` ohne Fehler von Natura; alle Setzlinge, große Glowshrooms und
  Saguaro wachsen; TreeCapitator fällt Eukalyptus, Ahorn, Sakura, Weide ganz; Rezepte, Drops, Spawns, Babys der
  Heatscar Spider per RCON. Abgleich Seed 1234: 49 Biome (159.260 Blöcke) und 297 Natura-Chunks (903.929 Blöcke),
  0 Abweichungen.

**Offen / bewusst abweichend:**

- Nether (Netherite-Gelände, Nether-Zweige der Generatoren, Spawns dort): Teil 7b (erledigt). Thornvines bekommen im
  Chunk-Writer ihre obere Fläche nur aus den Metadaten (erst im Nether relevant).
- Biomnamen: Natura fragt `AutumnWoods`, `BirchForest`, `PineForest`, `TemperateRainforest`, `ForestedHills`,
  `GreenHills` ab; ExtrabiomesXL schreibt sie mit Leerzeichen, im Original trafen sie also nie (übernommen).
- Sakura-Gen: Im Original eine Instanz für die ganze Sitzung (Höhengrenze lief über Bäume weiter, nicht seed-fest);
  hier eine je Chunk (`world.chunkState`). Gerste/Baumwolle/Bluebells prüfen nur Himmelslicht. Saguaro: fallender
  Sand und Wasser-Updates beim Generieren nicht nachgebildet.
- Im Original nie registriert, nicht portiert: Leitern, Stamm-Stufen, NaturaSnow, Herbs, Ivy, FusewoodArrow-Entity
  (der Bogen schießt normale Pfeile). Boote waren im Original toter Code: hier mit 1.5.2-Bootsrezept und dem
  Bootsystem von 26.3. Spawn-Eier nur in der Primärfarbe. Baby-Spinnen nur beim Tod, nicht beim Despawnen. Looting-
  Bonus der geladenen Nitro-Creeper-Drops fehlt.
- Vanilla-Rezepte für Werkbank, Stock, Bücherregal, Schüssel in `data/minecraft/recipe` überschrieben (schließen
  Natura-Planken per `neoforge:difference` aus), damit Natura-Rezepte greifen. Holzwerkzeuge mit normalen Mustern
  (Original nutzte für alle dasselbe). Werkbank nutzt das Vanilla-Menü. Bloodwood mit jeder Axt schnell abbaubar.
  Nether-Beeren-Effekte addieren Dauern nicht. Weidensetzling braucht Boden.
- Weggelassen: Minefactory-Reloaded-Plugin, Tinkers, Thaumcraft, Forestry, BuildCraft-Fassaden; Bonemeal auf Gras
  lässt keine Bluebells wachsen. Inventory-Tweaks-Kategorien für Natura-Items nicht eingetragen. Nur `en_us`
  (Original ohne Sprachdateien).
- Nicht Natura: 2 von 4.752 Biom-Stichproben weichen an 4x4-Rastergrenzen ab ((-382, -2026), (1298, -1122)).
- Client-Prüfung offen: Modelle, Färbungen (Topiary-/Blau-/Herbstgras), Transparenz von Wolken und großen
  Glowshrooms, Saguaro-Arme, Bögen, Imp-Rüstung, Imp-Modell, Heatscar-Skalierung, Boote (Lage im Wasser,
  Bloodwood-Boot auf Lava), Ei- und Boots-Icons.

## Teil 7b – Natura-Nether

Im Pack ist `Override Nether` an: Natura ersetzt den Nether durch `NetheriteWorldProvider`/`NetheriteChunkProvider`
(1.5.2-Nether mit Tainted Soil und Heat Sand). worldgen152 kann bisher nur die Oberwelt; in Teil 7 entschieden:
eigener Teil.

- 1.5.2-Nether in worldgen152: Gelände (`ChunkProviderHell` in der Netherite-Variante), `MapGenCavesHell`,
  Netherfestung (`StructureNetherBridgePieces`), Population (Feuer, Glowstone, Pilze, Quarz), Spawns.
- Nether-Zweige der Natura-Generatoren (`BaseTreeWorldgen`, Biom „Hell“): Bloodwood, Darkwood, Fusewood,
  Glowshrooms, Nether-Beeren, Thornvines; Reihenfolge wie im Original.
- Abgleich wie in Teil 2 mit `tools/expect.sh`/`verify_blocks.py` (dafür Nether-Unterstützung ergänzen).

**Status: erledigt.**

- **Kern** (`hexxit.worldgen152.core`): `nether/NetherTerrain` (Natura-`NetheriteChunkProvider`: Dichtefeld,
  Tainted Soil ab Dichte 56, Soul-Sand-/Kies-Ufer mit Heat Sand und Tainted Soil darunter, Bedrock),
  `nether/NetherCaves` (`MapGenCavesHell`), `structure/MapGenNetherBridge` mit allen 15 Bauteilen (Blaze-Spawner,
  Netherwarzen, Lava im Eingang), `populate/OldNetherPopulator` + `OldNetherGens` (Lavaquellen, Naturas `FireGen`,
  Glowstone, Quarz, kleine Glowshrooms), danach die Mod-Generatoren wie in der Oberwelt (`GameRegistry.generateWorld`
  lief in jeder Dimension). `OldWorld.nether(...)`, `world.dimension()`, `world.isHellWorld()`; Biom dort immer „Hell“.
- **Spiel**: `OldNetherChunkGenerator` (`worldgen152:nether`), Biom `worldgen152:hell` (Spawns von 1.5.2: Ghast 50,
  Zombie Pigman 100, Magmawürfel 1; in `#minecraft:is_nether`, damit Naturas Imp, Heatscar Spider und Nitro Creeper
  dort spawnen), im Welt-Preset `normal` für `minecraft:the_nether` eingetragen. In der Festung deren Spawnliste
  (Blaze 10, Zombie Pigman 5, Skelett 10 als Witherskelett 8 + Skelett 2, Magmawürfel 3). `/locate structure
  minecraft:fortress` findet die 1.5.2-Festungen; Strukturen von 26.3 (Bastion, Portalruine, Festung) entstehen nicht.
- **Natura**: Die Nether-Zweige waren in Teil 7 schon portiert; jetzt über `world.isHellWorld()` aktiv. Wild Caves
  läuft nur in Dimension 0 (Blacklist des Packs), Hexxit Gear, offLawn und ExtrabiomesXL laufen wie im Original
  auch im Nether (ohne dort etwas zu setzen).
- **Werkzeuge**: `tools/expect.sh <seed> nether <chunks>` bzw. `nether structures <radius>` (nächste Festungen);
  die Datei beginnt mit `D minecraft:the_nether`, `verify_blocks.py` prüft dann in dieser Dimension.
- **Prüfung**: `clean pack` grün, Server bis `Done` ohne Fehler. Gelände, Höhlen und Zufallszustand nach dem Gelände
  für 600 Chunks (2 Seeds) byte-gleich mit dem dekompilierten Original-Code (Natura-Methoden + Vanilla-Noise in
  einem Testrahmen). Kern gegen Spiel, Seed 1234: 256 Chunks um 0,0 (253.515 Blöcke), 256 Chunks mit einer ganzen
  Festung (313.019 Blöcke, 2 Blaze-Spawner), 8 Festungs-Chunks: 0 Abweichungen. Oberwelt weiter 0 Abweichungen
  (35 Biome, 111.410 Blöcke).

**Offen / bewusst abweichend:**

- **Population nicht seed-fest im Original:** `populate` zog aus `hellRNG`, ohne ihn neu zu setzen; das Ergebnis hing
  davon ab, welcher Chunk zuletzt erzeugt wurde. Hier: Zustand nach dem Gelände des Chunks bei +1/+1 (der Chunk, der
  bei zeilenweiser Erzeugung die Population auslöst). Gelände und Festungs-Layout sind seed-fest wie im Original.
- Kein Abgleich mit einer Original-Welt (`compare_original.py`): Der Original-Server erzeugt Nether-Chunks nur mit
  einem Spieler dort. Ersatz ist der Vergleich mit dem dekompilierten Code oben; die Festung ist nur Kern gegen Spiel
  geprüft.
- Lavaquellen fließen erst im Spiel (wie die Quellen der Oberwelt), nicht schon während der Generierung; spätere
  Generatoren sehen die geflossene Lava also nicht. Blocklicht ist nicht nachgebildet: Glowshrooms prüfen ihr
  Licht (< 13) nicht, direkt neben frisch gesetztem Glowstone/Feuer kann daher einer mehr stehen.
- Festungen existieren alle von Anfang an (im Original erst, wenn ein Chunk im Umkreis von 8 erzeugt war).
- Mob-Spawns im Nether (Biom-Liste, Festungsliste, Natura-Mobs, Blaze-Spawner) sind ohne Spieler am Server nicht
  prüfbar: im Client nachsehen. Client-Prüfung offen: Nebel/Atmosphäre des Bioms `worldgen152:hell`.
- Bestehende Welten: Schon erzeugte Nether-Chunks bleiben, neue kommen vom 1.5.2-Generator.

## Teil 8 – Artifice, CoFHCore

- Artifice: Steine, Marmor, Basalt, Lotus, Upgrade-Werkzeuge, Weltgeneration.
- CoFHCore: nur, was das Pack tatsächlich nutzt, zum Beispiel die Erz-Konfiguration (`config/cofh`).

**Status: erledigt.** Teilprojekte `mods/cofhcore` (`cofhcore`) und `mods/artifice` (`artifice`).

- **CoFHCore/CoFH World** (`cofh.gen.CoFHWorldGen`, Kern ohne Spielklassen, in `expect.sh`): `ReplaceVanillaGeneration`
  verweigert die acht Erz-Events des Dekorators und erzeugt die Erze selbst mit den Werten des Packs (`World.cfg`:
  Erde 20×32, Kies 10×32, Kohle 20×16, Eisen 40×8, Gold 8×8, Redstone 64×7, Diamant 8×7, alle y 0–128, Lapis 8×6
  um y 64±64). `FlatBedrock`: Grundgestein y 1–5 wird Stein, im Nether auch y 122–126, dort Netherrack.
  Köpfe als Drop (`HeadDrops`, `Core.cfg`), `/cofh tps [o|a|<dim>]`, `/cofh help|version` mit dem Originaltext.
  Für qCraft (Teil 17): `CoFHWorldGen.addCustomFeature("QOre", 2373, 0, 5, 4, 1, 24, false)` aufrufen; die Vorlage
  aus `WorldCustomGen.txt` lief im Original trotz `AllowCustomGeneration=false` (die Option wurde nie gelesen).
- **Artifice-Weltgenerierung** (`artifice.gen.ArtificeWorldGen`, in `expect.sh`): Blumen, Lotus-Seerosen (nur
  Swampland), Basaltschicht (y −2..7 mit eigenem `XSRandom` aus dem Seed, in jedem Chunk dasselbe Muster),
  Marmor-Cluster (4 je Chunk, Größe 400). IDs 1003/1004/1005/1014 in `verify_blocks.py`.
- **Artifice-Inhalte**: Basalt und Marmor (je 6 Varianten, Treppen, Stufen), 4 Blumen + Lotus (öffnet/schließt nach
  Licht), Lotus-Seerose, Farbstoffe, Stahl (Eisenbarren im Ofen), Rahmen/Sprengwand/Glaswand/Gerüst in 4 Stufen,
  Detektor, Vorschlaghämmer (zerlegen Blöcke in Teile, Werfer), Sicheln (Flächenernte), Kiste (Daten-Komponente
  `artifice:box_contents`, Rezept `artifice:box`), Münzen und Nuggets, 13 Upgrades (Rezept `artifice:upgrade`),
  Tooltips mit Flavor-Text. JSON aus `mods/artifice/tools/gen.py`, Prüfung `tools/check.py`. JEI: Beispielrezepte
  für Kiste und Upgrades, Info-Seiten (Vorschlaghammer-Teile, Sichel, Gerüst, Detektor, Lotus, Blumen).
- **Prüfung**: `clean pack` grün; Server bis `Done` ohne Fehler der beiden Mods. Kern gegen Spiel, Seed 1234:
  49 Biom-Chunks (285.241 Blöcke), 6 Chunks mit Lotus-Seerosen (32.923), 6 Nether-Chunks (12.125): 0 Abweichungen.
  Gegen eine neue Welt des Original-Hexxit-Servers 1.0.10 (`compare_original.py`, 120 Chunks, 7,9 Mio. Blöcke):
  keine Abweichung bei Grundgestein und Stein ↔ Basalt, die Basaltschicht und das flache Grundgestein stimmen also
  bitgenau. `/cofh tps`, Detektor-Puls (0 → 1 → 2 → 0) per RCON geprüft.

**Offen / bewusst abweichend:**

- **Generator-Reihenfolge:** Forge 1.5.2 hielt die Weltgeneratoren in einem `HashSet` (`GameRegistry.worldGenerators`),
  die Reihenfolge wechselte von Start zu Start. Hier Ladereihenfolge. Erze von CoFH, Marmor-Cluster und Blumen ziehen
  aus der gemeinsamen Zufallsfolge und liegen deshalb in der Original-Welt woanders (Mengen symmetrisch, z. B.
  Kohle 10.859 ↔ 10.141 Blöcke); Marmor macht den größten Teil der verbleibenden 7,1 % Abweichung aus.
- Lotus (Blume und Seerose) wurde im Original geschlossen gesetzt, wenn die Weltzeit > 12000 war (nicht seed-fest);
  hier immer offen, wie in einer neuen Welt. Er schließt sich im Spiel nach Licht.
- Köpfe: wie im Original lassen Skelette (Konfigurationsfehler in `CoFHCore.preInit`) und Spieler
  (`PlayerDropsEvent` übergab immer 0) keine Köpfe fallen; Zombie gilt für alle `Zombie`-Arten von 26.3.
  Nicht ohne Spieler prüfbar. Nicht portiert: Capes/Skins (`/cofh cape|skin`, Web-Adressen tot), Update-Check,
  Fluid-/GUI-Tab-APIs, ForgeIRC.
- Keine verbundenen Texturen: Paver/Antipaver und Rahmen nutzen Teilbild 0 (Paver und Antipaver sehen gleich aus).
- Kiste: hält bis zu 8 Gegenstände (ein Stück je Feld, wie im Original); Rest-Kiste über `getRemainingItems`, der
  Gegenstandsverlust des Originals beim Auspacken und beim Werfer ist nicht übernommen, keine Deep-Storage-Units.
- Vorschlaghammer: Haltbarkeit wie im Original nur bei Blöcken mit fester Metadaten-Zuordnung; farbige Betten geben
  ihre Wolle; Kürbis-Teil ist `carved_pumpkin`. Upgrades prüfen „passt“ über die Item-Tags von 26.3.
- Knochenmehl: Vanilla-Gras unverändert, zusätzlich setzt ein `BonemealEvent` Artifice-Blumen mit ihrem Anteil.
  Blumen folgen beim Setzen den Regeln von 26.3 (ohne Licht-Prüfung von 1.5.2). Gerüst: Suche verbundener Teile auf
  100.000 Blöcke begrenzt. Stein-Kreislauf-Rezepte entfielen schon im Pack (nur je eine Basalt-/Marmorart).
- Nicht ohne Spieler geprüft: Vorschlaghammer, Sichel, Gerüst-Klettern und -Halt, Kiste, Upgrades, Köpfe.
- Client-Prüfung offen: Rahmen-/Glaswand-/Gerüst-Modelle (Overlay), Lotus-Seerose, Tooltip-Farben, Glanz gefüllter
  Kisten, JEI-Seiten.

## Teil 9 – Harken Scythe, Asgard Shield, Addons

- Harken Scythe: Seelen, Sensen, Altäre, Zauber.
- Asgard Shield samt Farb-API.
- Die drei Addons: Nether-Ender, Hallowed, HarkenScythe.

**Status: erledigt.** Teilprojekte `mods/harkenscythe` (`harkenscythe_core`), `mods/asgardshield` (`asgardshield_core`)
und `mods/asgardshield-addons` (`asaddons`, alle drei Addons in einem Mod, braucht `asgardshield`, Harken-Teile über
IDs und `neoforge:mod_loaded`). Einzelheiten zu Schnittstellen: `mods/harkenscythe/NOTES-altar.md`, `NOTES-mobs.md`.

- **Harken Scythe, Kern:** alle Items/Blöcke (Sensen und Gleven mit Lade-Angriff, Keeper/Vessel mit Füllstand über die
  Haltbarkeit wie 1.5.2, Bücher, Necronomicon-Ritual, Talismane, Amulette, Livingmetal/Biomass mit Selbstreparatur,
  Soulweave/Bloodweave/Spectral-Rüstungen mit Färben, 16 Schädel als Helm und Block, Tiegel, Seelenlicht, Blutspritzer,
  Creep, Biomasse-Pflanze), 12 Augments als datengetriebene Verzauberungen (nicht am Zaubertisch; Ward/Sanguinary/
  Vitality/Exude auch für Asgard-Schilde und Riesenschwerter), alle Ereignisse (Seelenernte mit Harvest Moon,
  Blutspritzer, Talisman-Rettung, Decapitate, Afterlife …), Seelen-Entitäten, Handel, Spawn der verlorenen Seele,
  Necronomicon-Seite in Strukturtruhen über `OldLoot` (`harkenscythe.gen.HSWorldGen`, in `expect.sh`/`Expect.java`),
  Bonus-Truhe per Loot-Modifier. JEI-Plugin.
- **Altäre:** Altar of Souls / Altar of Blood mit Block-Entity, Menü, Screen, schwebendem Buch, eigenem Rezepttyp
  `harkenscythe_core:altar` (28 JSONs inkl. Augment-Würfel), JEI-Kategorien. Essenz aus Tiegeln im Ring wie 1.5.2.
- **Mobs:** 22 Spectral-Tiere + Spectral Miner (zähmbar, Basis `SpectralPet` auf `TamableAnimal`), böser Miner,
  Harbinger (Seelenernte → Spectral-Minions), Ektoplasma/Hämoglobin (Slime); Modelle Würfel für Würfel nach 1.5.2,
  durchscheinend; Spawn-Eier; Harbinger-Spawn (Gewicht 1 → 10).
- **Asgard Shield:** Schilde (4 Stufen + Gilded), 5 Riesenschwerter, Guard Gauge/Guard Break/Vanguard auf dem Server
  (Sync-Payload), alle Perks, HUD mit Original-Texturen, Farb-API (Client-Konfig, Tint-Quelle), Färben, Gilding-Rezept.
  Addons: 18 Items mit Originalrezepten.
- **Prüfung:** `clean pack` grün; Testserver bis `Done` ohne Fehler; per RCON (Crafter, temporäre Testbefehle, nicht
  committet): Rezepte, Seelenernte, Blutspritzer, Augments, Talisman, Keeper, Altar-Rezepte (je 10.000 Würfe Verteilung
  wie Original), alle 26 Mobs `/summon`, Harbinger-Minions, Slimes aus Tiegeln. Kern gegen Spiel, Seed 1234,
  `structures 220`: 36 Chunks, 211.284 Blöcke, 127 Truhen/Spawner/Entitäten, 0 Abweichungen.

**Offen / bewusst abweichend:**

- Client-Prüfung offen: Modelle und Animationen aller Mobs (Testclient lädt bis zum Titelbildschirm), Seelen-Kristall,
  Schädel-Renderer, Tiegel, Blutschichten, Altar-GUI und Buch, gefärbte Rüstungen, HUDs (Trinkzähler, Guard Gauge,
  Vanguard), Schild-Tints und Blockhaltung, JEI-Seiten. Mit Spieler ungetestet: Zähmen, Blocken, Amulett-Teleport.
- Nicht gezeichnet: Schweinesattel der Spectral Pig, Block in der Hand des Spectral Enderman, Trank der Spectral Witch.
- Asgard Shield: Gilding ergibt das richtige Gilded-Schild (Original „ID + 1“ traf mit den Pack-IDs falsche Items);
  Guard Gauge läuft serverseitig; Vanguard-Effekte wirken wirklich (1.5.2 nur clientseitig); Auren je Spieler;
  nur die Haupthand blockt.
- Harken Scythe: Seelenlicht bleibt auf dem Client (1.5.2 entfernte es per Desync); Spectral-Rüstung mit Leder-Rüstungswerten;
  Handel als zusätzliche Auswahl des Bibliothekars; tote Handler/Achievements des Originals nicht portiert;
  Altar-Augments nur auf unverzauberte Gegenstände; orange Wolle wie im Original ohne Rezept; Harbinger teleportiert
  nicht (lief im Original nie); Halsband/Katzenfell/Schleimwert werden gespeichert.

## Teil 10 und 11 – Project Zulu

- **Erinnerung an den Nutzer zu Beginn von Teil 10:** Er wollte auf den Blockbench-MCP hingewiesen werden (https://github.com/sosadly/blockbench-mcp). Damit lassen sich Mob-Modelle direkt in Blockbench bauen. Der Server braucht ein laufendes Blockbench auf dem eigenen Rechner. Vor dem Start fragen, ob er ihn einrichten will.
- **Teil 10:** Kreaturen. Modelle, Renderer, KI, Spawns je Biom. Die Pack-Konfiguration liegt in `config/Project Zulu`.
- **Teil 11:** Blöcke, Items, Rezepte, Weltgeneration (Pyramiden, Labyrinthe, ...), Mob-Spawn-Abgleich.

### Teil 10 – Stand

**Status: erledigt.** Teilprojekt `mods/projectzulu` (Mod-ID `projectzulu`). Blockbench-MCP: Hinweis gegeben, nicht
nötig – die Modelle werden aus dem 1.5.2-Code exakt übernommen (`tools/modelbase_to_layer.py`).

- **40 Kreaturen** (alle mit `shouldExist=true` der Pack-Konfiguration; `horserandom` ist dort aus), je mit Modell,
  Renderer, Sounds, Spawn-Ei (außer Pharao, keine Eifarbe in der Konfig), Spawn-Regel (`getCanSpawnHere`):
  - Armadillo (Deckung vor Bogenschützen, Rollen) – `ZuluEntities`
  - Vögel: Finken rot/grün/blau, Adler, Geier (kreist, greift Geschwächte an), Horn Bill, Pelikan (landet als einziger)
    auf Flug-Basis `AerialAnimal` (EntityAerial-Physik) – `ZuluEntities`
  - Landtiere: Eber, Fuchs (zähmbar, Modus-Zyklus), Frosch, Kaninchen (hüpfen), Biber, Pinguin, Strauß
    auf `ZuluLandAnimal` (Flucht/Zorn, Alarm an Artgenossen) – `ZuluEntities`
  - Großtiere: Schwarz-, Braun-, Eisbär, Gorilla, Nashorn, Alligator, Giraffe, Elefant, Mammut, Baum-Ent
    (Schnee-Texturen in Taiga) auf `ZuluBigAnimal` (Original-Nahkampfreichweite, gedämpfter Rückstoß) – `ZuluBigAnimals`
  - Monster: Sandwurm (eingegraben), Echse + Spucke (Gift/Langsamkeit), Mumie, Pharao (Boss, 4 Phasen, Feuerbälle,
    Mumien-Beschwörung, Boss-Leiste), Mimic (Truhe), Minotaurus, Spukrüstung (setzt sich zusammen, Eisen/Gold),
    Tausendfüßer (9 PartEntity-Segmente) auf `ZuluMonster` – `ZuluMonsters`
  - Pferde in 7 Farben: reitbar mit Sattel, Lenken mit Stock (1.5.2-Schweine-KI), Paarung nur gleiche Farbe – `ZuluHorses`
- **Daten aus `ProjectZuluMobBiomeConfig.cfg`** (`tools/gen.py`): Biome-Spawns je Biom mit Gewicht und Packgröße
  (worldgen152- und ExtrabiomesXL-Biome; Biome von Twilight Forest/Dimensional Doors übersprungen), gewichtete
  Loot-Tabellen (min..max Würfe), Eifarben, `#minecraft:undead` für Mumie/Pharao, 16 Drop-Items (Scrap Meat, Fur Pelt,
  Scale, Ankh, Herzen, Talon …) mit Originaltexturen. 123 Texturen/Sounds aus dem Original-Archiv.
- **Peaceful:** `despawnInPeaceful=true` des Packs → alle Kreaturen `notInPeaceful()`, nur der Fuchs (zähmbar)
  verschwindet ungezähmt per `discard()`.
- **Prüfung:** `pack` grün; Testserver bis `Done` ohne Fehler/Warnungen; per RCON alle 40 `/summon` mit Original-Leben,
  `loot spawn … kill` für alle Loot-Tabellen; natürliche Chunk-Spawns: Wüste (Armadillo, Strauß, Nashorn), Savanne
  (+ Elefant, Pferd), Ebene (Giraffe, Pferde), Taiga-Rand (Biber, Alligator/Frosch aus Fluss/Sumpf), Dschungel (Eber).
  Weltgenerierung unverändert (nur Spawns), daher kein `expect.sh`-Lauf.

**Offen / bewusst abweichend:**

- Client-Prüfung offen: alle Modelle und Animationen (besonders Babys, Mammut ×4, Tausendfüßer-Segmente, Schwertlage
  der Spukrüstung, Horn-Bill-Körperhöhe), Reiten der Pferde, Sattel-Texturen, Kreativ-Tabs.
- Mit Spieler ungetestet: natürliche Spawns von Monstern und Vögeln (brauchen Spieler in der Nähe), Fuchs zähmen,
  Geier-Kreisen, Pharao-Kampf, Mimic-Weckruf, Spukrüstung-Zusammensetzen.
- Wie im Original: Mammut und Strauß nicht reitbar (`isRideable()` war false), Pferde nicht zähmbar (kein Zähm-Item),
  Sattel wird nicht verbraucht und nicht gedroppt; Finken-Sounds vertauscht; einige Modellfehler des Originals.
- Abweichend: Zähm-GUI (Paper), Namensschilder mit Zustands-Icon und Temperatur-System nicht portiert; Fuchs-Wachmodus
  nur gegen Monster; Echsen-Kragen bewegt sich (im Original nie synchronisiert); Pharao speichert Phase und Startpunkt.
- **Für Teil 11:** Mob-Köpfe (seltene Drops, `MobSkulls`), Jasper-Drop des Pharaos, eigener Kreativ-Tab (bis dahin
  Vanilla-Tabs „Spawn Eggs“/„Ingredients“), Strukturen mit Pharao/Mimic/Minotaurus/Spukrüstung (`setSummoned`,
  `setHovering`, `setRandomArmor` liegen bereit), Sound-Events `mummy_roar`/`summon_whispers` für BlockJasper,
  `LimitedMobSpawner`. Mob-Spawn-Abgleich der Biome mit dem Original.

## Teil 12 und 13 – Better Dungeons (Chocolate Quest)

- **Teil 12:** Bau-Engine und Import der Gebäude- und Dungeon-Vorlagen aus `betterdungeons-data1/2` (`Chocolate/`).
- **Teil 13:** Dungeon-Typen, Wächter, Mobs, Loot (`ChestConfig`), Platzierung in der Welt.
- Erinnerung an die Dorf-Bauteile von Tinkers' Construct ist erledigt: Der Nutzer will sie nicht vorziehen (bleiben
  in Teil 25). Better Dungeons hat kein Dorf-Bauteil; `VillageHandler` ist nur ein Handels-Handler (Kapselstationen
  beim Priester, portiert in `BDEvents`).

### Teil 12 – Stand

**Status: erledigt.** Teilprojekt `mods/betterdungeons` (Mod-ID `betterdungeons`, FML-ID `BetterDungeons`). Neuer
Projekt-Skill `templates-152` (Vorlagen und Schematics übernehmen, für Teil 13, 14, 15).

- **Vorlagen-Import:** `ImportOriginalAssets` liest jetzt mehrere Archive (`[archiv]`-Abschnitte in
  `original-assets.txt`) und schreibt neben jeden Ordner eine `.index`-Datei. `Chocolate/` aus
  `betterdungeons-data1/2` landet unter `betterdungeons-original/Chocolate/` im Jar (114 NBT-Vorlagen bis 73×206×70,
  rund 70 Text-Vorlagen, `DungeonConfig`, `ChestConfig`, `config.prop`), nicht im Repo.
- **Pack-Konfiguration** `Chocolate/config.prop` (liegt im Daten-Archiv): Block-IDs ab 2001 (eigener Spawner 2002),
  Item-IDs ab 7351, `dungeonSeparation` 5, Dschungel- und Sumpfhöhlen an, Unterwelt-Höhlen und Minenschächte aus.
- **Bau-Engine** `betterdungeons.gen` (ohne Spielklassen, läuft in `tools/expect.sh`): `BDNbt` (NBT von 1.5.2),
  `BDTemplate` (NBT- und Text-Vorlagen, kompakt, gecacht), `BDFiles` (Index, Ordner-Reihenfolge, Properties in
  Hashtable-Reihenfolge), `BuilderHelper` (alle `fillMap`-Varianten mit zweitem Durchgang für Fackeln/Hebel/Leitern/
  Betten, Türen, Kisten, Spawner, Mob-Blöcke 8000, Mod-Truhen 8001, Bosse 8002, Öfen, Werfer, Schilder, Plattenspieler,
  Braustände, Köpfe, Bilder, Rahmen, Mobs der Vorlage), `BDLoot` (`ChestConfig` + eigene Items), `BDMobs`,
  `BDDungeons` (alle 11 `DungeonConfig`-Typen mit ihren Werten), `BDWorldGen` (WorldGeneratorNew).
- **Platzierung:** jeder Chunk mit x, z durch 5 teilbar wie im Original (eigener Zufall je Chunk, keine Verschiebung der
  anderen Generatoren). Gebaut werden die Vorlagen-Dungeons: Burgen (`castles`), Schneeburgen (`castleSnow`) und
  Schiffe (`ship`). Die übrigen Typen werden gezogen wie im Original, bauen aber noch nichts (Teil 13).
- **worldgen152 erweitert:** alle Vanilla-Blöcke 1–158 mit jeder Metadaten-Belegung (`OldBlockStates`,
  `OldBlockInfo`, `verify_blocks.py`), alle Vanilla-Items und Schadenswerte (`OldGameIds`, Tränke, Spawn-Eier,
  Haltbarkeit, Verzauberungen auf Gegenständen), neue Tiles (`Furnace`, `Sign`, `Jukebox`, `BrewingStand`,
  `OldTile.Mod` + `OldModData.registerTile`), neue Entitäten (`Painting`, `ItemFrame`, `Mob`, `Item`), leere
  Blockentitäten für Blöcke ohne eigene Daten, Items/Entitäten nicht portierter Mods werden mit Warnung weggelassen.
  Korrigiert nach dem 1.5.2-Quelltext: Material von Schildern und Holz-Druckplatten (Holz) und Stein-Druckplatte
  (Stein), Endportalrahmen als normaler Block, glatte Doppelstufen (Meta 8, 9, 15).
- **Prüfung:** `pack` grün; Testserver bis `Done` ohne Fehler (Project Zulu dafür vorübergehend herausgenommen, sein
  Zwischenstand aus Teil 11 hat eine Loot-Tabelle mit dem noch fehlenden Item `projectzulu:jasper`; sein Generator ist
  dort noch leer, die Vorhersage also gleich). `tools/expect.sh 1234 dungeons 150`: Burg (−25, −35), Schneeburg
  (30, −35), Schiff (5, 0), je 2×2 Chunks: 117.004 Blöcke, 192 Biome, 57 Spawner/Kisten/Entitäten (Bilder, Rahmen, Mobs), 0 Abweichungen.
  Gegenprobe `find 260` (49 Biome, mit den korrigierten Vanilla-Einträgen): 285.204 Blöcke, 0 Abweichungen.

**Offen / bewusst abweichend:**

- **Teil 13:** die erzeugten Dungeon-Typen (`iceCave`, `jungleCave`, `lavaCity`, `linearDungeon`, `dungeon`,
  `volcano`, `emptyCave`, `skyscraper`), Dschungel- und Sumpfhöhlen je Chunk (`BuilderChunkJungle/Swamp`, in der
  Pack-Konfiguration an), die große Mauer bei z = −8000 mit Burgen/Schiffen (`BuilderWall`, `BuilderCastle`,
  `BuilderCastleSnow`, `BuilderPirateShip`), alle Mobs und Bosse (Spawner der Vorlagen machen bis dahin Vanilla-Mobs:
  Zombie, Enderman, Ertrunkener; Bosse der Mob-Blöcke 8002 fehlen), das Verhalten des eigenen Spawners (2002),
  Items, Rezepte, Kapseln, Dungeon-Platzierer.
- Ordner-Reihenfolge des Originals (`File.listFiles`) war vom Dateisystem abhängig; hier NTFS (Windows). Sie entscheidet,
  welche Vorlage und bei mehreren Treffern welcher Dungeon-Typ gewählt wird.
- `new Random()` des Originals (Inhalt der Mod-Truhen 8001, Werfer/Öfen aus `copySpecialBlocks`): hier aus Welt-Seed
  und Position, also seed-fest.
- Wie im Original: `StringTokenizer`-Fehler beim Einlesen (nur die erste Hälfte jeder Biom-Liste zählt, z. B.
  Burgen nur in Plains, Meadow, Savanna), `triton` findet keinen Mob („Triton“), die Nether-Stadt erscheint nie
  (Dimension 0, Biom Hell), die Block-Karten-Truhen sind immer Futterkisten.
- Items noch nicht portierter Mods (Backpacks, Tinkers', LegendGear, ChocoCraft, die eigenen Items ab 7607 …) fehlen in
  den Kisten; Rüstungs- und Schwerttyp von Mod-Items für die Waffenkisten ist aus dem Namen in `weapons.prop` geraten.
- Sand, Kies, Amboss und Drachenei der Vorlagen fallen erst, wenn ein Block daneben sich ändert (im Original sofort
  beim nächsten Tick); Kolben, TNT und Redstone der Vorlagen werden nicht neu ausgewertet.
- Client-Prüfung offen: Aussehen der Bauten, Bilder (Lage bei mehrteiligen Motiven), Rahmen, Schilder.

### Teil 13 – Stand

**Status: erledigt.** Gearbeitet in vier Strängen (Weltgenerierung; Items, Blöcke, Geschosse; Menschen-Mobs und KI;
übrige Bosse und Beschwörungen). Neuer Projekt-Skill `items-weapons-263`; `mobs-263` (Bosse, bewaffnete Biped-Mobs),
`worldgen152` und `templates-152` (Reichweite, Bilder) erweitert.

- **Weltgenerierung** (`betterdungeons.gen`, ohne Spielklassen): `iceCave`, `jungleCave`, `linearDungeon`
  (Stronghold), `dungeon` (`BuilderDungeon`, `DungeonRoom`, `DecoratorRoom`, Bilder in `BDPainting`), `volcano` (Pfad,
  Dungeon-Räume), `emptyCave` (Schleim- und Schildkrötenhöhle, `Perlin3D`, Boss über Block 302); je Chunk
  `BuilderChunkJungle` (Biom „Jungle“) und `BuilderChunkSwamp` (`swampCaves`), die große Mauer bei Chunk z = −500 mit
  Burg/Schneeburg/Schiff alle 10 Chunks. Geprüft: Mineshafts (0 → `Integer.MAX_VALUE` × 2 = −2), Underworld aus,
  NetherMountains nie aufgerufen, `lavaCity` nie (Dimension 0, Biom „Hell“). Kern: `OldTerrainGen.registerReach`
  (breiterer Kachelrand nur für weit schreibende Bauten; gemessen: Eishöhle 0..+6 Chunks, Vulkan −4..+5 / −4..+3,
  Mauer-Burgen −5 in z), `OldWorld.entities(cx, cz)`.
- **Items** (135, jede Damage-Variante eigenes Item, `BDItems`, generiert von `tools/gen.py`): Essenzen, Granaten,
  magische Pfeile, Schilde, Banner, Mob-/Boss-Eier, Kapselstationen (Block 2001), Tasche, Köcher, Langbogen, Revolver,
  Stäbe, Dolche, Speere, große Schwerter, Rüstungen (Schildkröte, Wolkenstiefel, Scouter), Goldene Feder,
  Fluchtrolle, Verfluchter Knochen, Super-Spitzhacke, Hakenschüsse, Controller, Pfadmarker. Werte nach 1.5.2, Rezepte
  als JSON, Lang en/es/zh. Alte IDs 7351 + 256 + Offset in `OldGameIds`: alle 62 Einträge der Dungeon-Kisten lösen auf.
- **Geschosse:** `BDProjectile` (25 Typen), `BDMagicArrow`, `BDBlockProjectile`. **Blöcke:** eigener Spawner 2002
  (Spieler im Umkreis 16, 20 Ticks, 1 Mob, danach Luft), Kapselstationen; Kapsel kopiert Bereiche samt Kisten/Entities.
- **Mobs** (`mob/`, IDs aus `api/BDEntityIds`, `BDEntityIds.registerOldNames()` biegt Spawner/Vorlagen um):
  27 Menschen-Typen (`BDHumans`: Dungeon-Mobs, Bosse Piratenkapitän, Walker King, Specter Lord, Gremlin Shaman,
  Minotaurus, Goblin-Boss; Nekromant, Lich, Schweinemagier; Söldner mit Steuer-GUI und `AIModePayload`), KI in
  `mob/ai`, Loot von `tools/gen_humans.py` mit Bedingung `betterdungeons:mob_level`; Bosse ohne EntityHuman
  (`BDCreatures`): Monking, Golem-Boss, Eisbulle, Spinnenboss, Schleimboss, Schildkröte (+ `PartEntity`-Teile),
  Wyvern, dazu SummonedUndead und SummonedBait. Wurm, Biene, Chinesischer Drache, Schlange waren nie registriert.
- **Prüfung:** `clean pack` grün; Testserver bis `Done` ohne Fehler (Project Zulu vorübergehend heraus, s. u.).
  `tools/expect.sh 1234 dungeons 150` findet jeden Typ (Chunks: Burg −25,−35; Schneeburg 30,−35; Schiff 5,0;
  Eishöhle 20,−25; Dschungelhöhle 115,30; Schleimhöhle −20,−35; Stronghold 15,5; Schildkrötenhöhle 90,−45;
  Untergrund-Dungeon −10,5; Vulkan 15,−135; Dschungel-Chunkhöhle −37,−69; Sumpf-Chunkhöhle 86,−43; Mauer 0,−500,
  50,−500, −2110,−500): 71 Chunks, 706.445 Blöcke, 322 Spawner/Kisten/Entitäten, 0 Abweichungen. Gegenprobe
  `find 260`: 289.747 Blöcke, 0 Abweichungen. Bosse in den Höhlen-Chunks (IceBull, SpiderBoss, SlimeBoss, TurtleBoss):
  32.803 Blöcke, 36 Entitäten, 0 Abweichungen. Per RCON/FakePlayer: alle Item-Werte, 17 Rezepte, alle Geschosse am
  Zombie, Spawner, Kapsel, `/summon` aller Mobs mit Leben/Ausrüstung/Loot, Kämpfe gegen Zombies/Eisengolems,
  Beschwörungen. Headless-Client: Modelle backen, Menschen-Mobs rendern.

**Offen / bewusst abweichend:**

- `new Random()` des Originals aus Welt-Seed und Position (`BuilderHelper.positionRandom`): Form von Eis- und
  Dschungelhöhle, Grundriss des Untergrund-Dungeons, der ganze Stronghold, Vulkan, Bildmotive.
- Wie im Original übernommen: Vulkan räumt y 121–124 auf 100×100 frei und setzt auf y 5 fließende Lava; `oreChance`
  aus `size`; Vulkan-Dungeon nutzt `dungeons/`; Eisbulle und Bosse unabhängig von `spawnBoss`; Untergrund-Dungeon liest
  `rows`; `clean()` der Dschungelhöhlen wirkungslos; Mob-Blöcke 300 immer Mob 7 (specter); Zwerge droppen nichts,
  Goblins fast ohne Ambient-Laut, Verzauberungen auf Mob-Waffen ohne Wirkung; Schildkröte: stirbt ein Bein, stirbt das
  gegenüberliegende mit; Wyvern ohne Textur (`dragonbd.png` fehlte schon); keine Bossleisten.
- Behoben gegenüber dem Original: Boss-Typ wächst nicht mehr mit „ boss“, Klassenwechsel entfernt alte Goals,
  Schleimboss Stufe 0 endet (statt endloser Kette).
- 26.3-Grenzen: `MAX_HEALTH` ≤ 1024 (Monking 1024 statt 1500, Schaden/Regeneration im Verhältnis 1024/1500);
  `jumpMovementFactor` (Wolkenstiefel, Haken) ohne Gegenstück; Spinnenboss: Netz und Nahkampf parallel (Mutex-Bits);
  fehlende 1.5.2-Vanilla-Texturen durch 26.3 ersetzt (Zombie-Pigman mit Piglin-Modell).
- Nicht übernommen: `skyscraper`/`skyscraperGrid` (nicht im Pack); Ensaimada, Telekinese-Handschuh, Seelenwaffe,
  Elementarschwert, Kopf-Trophäe (nie registriert); Editor-Blöcke (`exporterEnabled = 0`); Dungeon-Eier (Damage 600+,
  bräuchten die Builder in der laufenden Welt); GUI der bearbeitbaren Kapselstation (fest 15×9).
- Nachbar-Updates beim Setzen (Flag 3) nicht nachgebildet: Wasser der Sumpf- und Schildkrötenhöhlen fließt erst beim
  nächsten Block-Update. Spawn-Regeln der Vanilla-Spawner (Pirat 20 %, Zwerg unter y 60) brauchen einen Spieler.
- Bei Seed 1234 ist die Mauerlinie fast nur Ozean; ein Monking kam in der Prüfsuche nicht vor. Kein Abgleich gegen eine
  Original-Welt (keine vorhanden; Chunk-Höhlen, Mauer, leere Höhlen wären seed-fest vergleichbar).
- **Project Zulu blockiert den Serverstart:** Loot-Tabelle `mummy_pharaoh` nennt das fehlende `projectzulu:jasper`
  (Zwischenstand Teil 11). Für alle Tests vorübergehend herausgenommen.
- Client-Prüfung offen: Item-Modelle, Geschoss-Renderer, Rüstungstexturen, Mob-Modelle/Animationen (Golem-Blitze,
  Schildkrötenteile, Specter-Geist, Umhang, Feder, Sprechblase, Sitzpose, Köder-Skin), Steuer-GUI des Söldners;
  Hakenschuss und Wolkenstiefel mit echtem Spieler.

## Teil 14 – Battle Towers, Ruins

Ruins bringt rund 1.800 Vorlagen (`.tml`) aus `ruins-data1..3` mit. Dafür einen Konverter bauen, der die Vorlagen beim Bauen übernimmt. Sie kommen nicht ins Repo.

### Teil 14 – Stand

**Status: erledigt.** Teilprojekte `mods/ruins` (Mod-ID `as_ruins`, FML-ID `AS_Ruins`) und `mods/battletowers`
(`battletowers`, FML-ID `BattleTowers`). Skills `templates-152` und `worldgen152` erweitert.

- **Ruins-Vorlagen:** keine Umwandlung nötig, der Kern liest die `.tml` wie das Original. Der Build holt
  `mods/resources/ruins/` aus `ruins-data1..3` (1.833 Dateien, mit Index) und die Pack-Optionen `mods/ruins.txt`
  ins Jar (`ruins-original/`). 1.697 Vorlagen laden; 4 in `generic/` scheitern wie im Original an Leerzeilen in
  einer Schicht, Unterordner (`generic/townpack`) liest das Original nicht.
- **Ruins-Generator** (`ruins.gen`, ohne Spielklassen): `RuinsGen` (RuinHandler + RuinGenerator), `RuinTemplate`,
  `RuinTemplateRule` (Bedingungen, Drehung samt Fallthrough-Switch, Spawner, Kisten mit Beute und
  `ChestGenHooks.getOneItem`, Enderkristall auf Grundgestein), `RuinRotation` (`rotation_mappings.txt`). Pack-Werte:
  6 Versuche je Chunk zu 3 %, 75 % Folgeruine, Biom-Vorlagen zu 75 % (Glacier und Feature-Biome 100 %). Läuft auch
  im Nether (Ordner `hell`; wie im Original landen die Ruinen fast alle unter der Decke auf y 126).
- **Battle Towers:** `battletowers.gen` (`BTWorldGen`, `BTTower` = AS_WorldGenTower, `BTItems`, `BTConfig` liest
  `config/BattleTowers.cfg` des Packs aus dem Jar): sieben Turmtypen, Etagen mit Spawnern und Truhen nach der
  Pack-Beute je Etage (7 Versuche, unterirdisch doppelt), 15 % unterirdisch, Golem oben. Biom-Prüfung wie im
  Original an den *Block*-Koordinaten (chunkX, chunkZ). Spielseite: Golem (`battletowers:golem`, schläft, wacht bei
  Treffer, Spieler in 6 Blöcken, Truhe öffnen in 8 / abbauen in 7 Blöcken; Feuerbälle, Sprung mit Explosion 4,
  Drops), Feuerball (`golem_fireball`), Einsturz nach dem Tod (`TowerDestroyer`, 15 s, dann alle 5 s eine Etage),
  Geräusche und Texturen aus dem Original-Jar.
- **worldgen152:** `OldGameIds.registerEntity(name, id, init)` (Entität aus der Weltgenerierung mit Variante, hier der
  Golem je Turmtyp), `OldLoot.stackLimit` öffentlich. `verify_blocks.py`: Creeper-Spawner, Enderkristall, Golem,
  Namen mit Leerzeichen bei `E`; die Spawner-Namen von Better Dungeons zeigen jetzt auf dessen Mobs (Teil 13).
- **Prüfung:** `clean pack` grün; Testserver bis `Done` ohne Fehler (Project Zulu wie in Teil 13 vorübergehend
  heraus). `tools/expect.sh 1234 ruins 40 -30 -40` (Land) + `towers 6`: 154 Chunks, 822.881 Blöcke, 660
  Spawner/Kisten/Entitäten, 0 Abweichungen; `ruins 40` (Ozean um 0,0): 607.877 Blöcke, 0; `nether ruins 15`:
  119.768 Blöcke, 0; Gegenprobe `find 260`: 290.560 Blöcke, 80 Spawner/Kisten/Entitäten, 0. Am Server: Golem aus
  der Welt mit Typ und Leben (Typ 1, 200), Aufwachen, Feuerbälle, Sprung-Explosionen (sprengt sich wie im Original
  durch die Böden), Drops, Einsturz der obersten Etage nach dem Tod.

**Offen / bewusst abweichend:**

- **Battle Towers, Abstand:** Das Original merkte sich die Türme der laufenden Sitzung (nicht gespeichert) und
  baute am ersten geladenen Chunk mit 850 Blöcken Abstand. Hier: Raster aus 60×60 Chunks, je Zelle ein
  Kandidat aus dem Seed (mindestens 20 Chunks von den fernen Zellrändern), dort die Prüfungen des Originals. Etwa
  9 von 10 Versuchen gelingen, die Dichte entspricht ungefähr dem Original. Kein Turm mehr zuverlässig nahe dem Spawn.
- **Ruins, Zustand zwischen Chunks:** Die letzten zehn Ruinen (Überlappungsprüfung) und das Entfernen einzigartiger
  Vorlagen (`unique`, 43 Vorlagen) gelten nur noch im selben Chunk und seiner Gruppe; einzigartige Ruinen können
  mehrfach vorkommen. Folgeruinen einer Gruppe, die weiter als 3 Chunks nach −x/−z oder 4 nach +x/+z reichen
  würden, entfallen (etwa jede dritte Folgeruine), damit an Kachelgrenzen nichts abgeschnitten wird. Der Fehler
  des Originals, dass ausweichende Ruinen nahe 0, 0 landen, fällt dadurch fast immer weg.
- Reihenfolge der Vorlagen: `HashSet` nach dem Pfad wie im Original, aber mit einem festen Pfad
  (`mods/resources/ruins/...`); der echte war der Installationsordner des Spielers.
- `world.rand` (Golem-Drehung, unterirdisch ja/nein, Enderkristall-Drehung): eigener Zufall aus Seed und Chunk.
- Golem: 1.5.2 setzte nach verlorenem Ziel 300 Leben über das Maximum; hier Maximum 300, Start mit dem Leben des
  Typs (150 + 50 je Typ). Feuerball- und Sprung-Explosionen brechen Blöcke unabhängig von `mobGriefing` (wie 1.5.2).
  Enderkristalle setzen außerhalb des Endes kein Feuer mehr (1.5.2 tat das jeden Tick).
- Die Info-Funktion von Ruins (Block mit Stock schlagen) nennt Blockname und Zustand statt ID und Metadaten.
- Client-Prüfung offen: Golem-Modell und -Texturen (wach/schlafend), Feuerball, Geräusche, Truhe öffnen und abbauen
  neben dem Golem mit echtem Spieler, Stock-Info.

## Teil 15 – Dimensional Doors

Limbo, Pocket-Dimensionen, Rifts. Braucht Teil 2, weil eigene Dimensionen den Weltgenerator erweitern.

### Teil 15 – Stand

**Status: erledigt.** Teilprojekt `mods/dimensional-doors` (Mod-ID `dimdoors`, FML-ID `DimDoors`), Werte aus der
Pack-`DimDoors.cfg` (IDs 1111–1117, 217, 220, Limbo −23, alle Rezepte, Beute-Schalter). Ressourcen schreibt
`mods/dimensional-doors/tools/gen.py`; Texturen, Klänge und die 69 Schematics kommen beim Bauen aus dem Original.

- **Kern ohne Spielklassen** (`dimdoors.gen`): `RiftGen` (RiftGenerator: Cluster 3/10.000, Gateways 40/10.000 mit
  Steinziegel-Ruine und Transient Door, in Overworld und Nether; Limbo und Taschen populierte das Original nie),
  `LimboTerrain` (LimboGenerator, Rauschen mit Seed 45, Eternal Fabric unter y 19), `Dungeons` (DungeonHelper: alle
  Listen, Gewichte, Auswahl `generateDungeonLink`, eigener Ordner `config/DimDoors_Custom_schematics`),
  `DungeonBuilder` + `Rotation` (generateSchematic, transformMetadata wörtlich mit seinen Durchfällen), `DDLoot`
  (dimensionalDungeonChest: sieben Kategorien nach Item-ID zusammengelegt, Java-8-HashMap-Reihenfolge, Buch auf 3,
  fünf Ziehungen).
- **DDLoot teilt die dungeonChest-Gewichte**: Das Original teilte sie beim Zusammenlegen an Ort und Stelle durch 10
  (mindestens 1, das verzauberte Buch ausgenommen); seitdem zogen *alle* Dungeon-Kisten des Packs damit
  (`OldLoot.Category.divideWeights`, wirkt auch auf Vanilla-Dungeons und Ruins).
- **worldgen152:** `OldLiveGen.place` (Ergebnis eines Kern-Builders samt Kisten/Spawnern/Schildern in die laufende
  Welt), `OldWorld.setBlockDirect` (setBlockDirectly ohne onBlockAdded), `OldChunkWriter.fillTile`,
  `OldBlockStates.door` öffentlich. `verify_blocks.py`: Rift 1113, Transient Door 1115.
- **Spielseite:** Dimensions-, Warp-, Unstable- und Transient-Tür (`DimDoorBlock`, eine Klasse mit Art), Falltür,
  Fabric of Reality/Ancient Fabric/Eternal Fabric/Unraveled Fabric, Rift (`RiftBlockEntity`: Wachsen, Endermen,
  Schließen; Zerstörung per Zufallsticks), Items (Türen, Rift Signature und stabilisierte, Rift Remover, Rift Blade,
  Stable Fabric, Spawn-Ei), Monolith (Zorn, 19 Texturen, Wurf nach Limbo; in neuen Limbo-Chunks und je Chunk
  geschlossener Dungeons), Dimensionen `dimdoors:limbo` (eigener Generator) und `dimdoors:pocket` (leer), Tod in einer
  Tasche → Limbo mit Inventar, Eternal Fabric in Limbo → zurück in die Overworld, Limbo-Zerfall, Fallschaden aus in
  Limbo, Befehle `dd-rift`, `dd-deletelinks`, `dd-???`, `dd-deletedimension`, `dd-dimensiondata`, `dd-prune`,
  `dd-resetdungeons`. Gespeichert in `DDData` (SavedData).
- **Prüfung:** `clean pack` grün; Testserver bis `Done` ohne Fehler (Project Zulu wie in Teil 13/14 vorübergehend
  heraus). `tools/expect.sh 1234 rifts 5`: 9 Chunks, 53.676 Blöcke, 0 Abweichungen (ein Schädel von Wild Caves unter
  zugeflossenem Wasser); Limbo-Gelände 12.800/12.800 Blöcke gegen den Kern. Per FakePlayer: Gateway → Dungeon
  (ComplexHall_SK-HiddenStairs) mit Seitentüren und neuen Taschen, Tiefe 2, Kiste mit DD-Beute; persönliche Tasche
  (Gewebe-Hülle 29³ innen, Ewiges Gewebe außen, Tür nach Norden); Warp-Tür in der Tasche → Overworld an den
  Koordinaten des Originals; Unstable-Tür → Limbo; geschlossener Dungeon (lockingExitHall) mit 4 Monolithen und
  Ausgang; 60 Monolithen in den geladenen Limbo-Chunks; Rezepte über den Crafter.

**Offen / bewusst abweichend:**

- **Taschen in einer Ebene:** Jede Tasche war im Original eine eigene Dimension an den Koordinaten ihrer Tür. Hier
  liegen alle in `dimdoors:pocket`, jede in einem Feld von 512 × 512 Blöcken; ein Versatz je Tasche rechnet zurück auf
  die Koordinaten des Originals, wo es sie in anderen Dimensionen benutzt (Warp-Türen, Ausgänge, Limbo-Rückkehr,
  Zufalls-Seed der Kisten). Dimensions-IDs der Taschen sind interne Zahlen; `dd-resetdungeons` leert nur den
  gespeicherten Bauraum des Dungeons (das Original löschte den Ordner).
- Licht in Limbo: das Original drehte die Helligkeitstabelle um (dunkle Stellen hell, Fackeln dunkel); hier
  `ambient_light` 0,75. Eigener Himmel mit Limbo-Sonne und -Mond fehlt (Texturen liegen im Jar).
- Pocket-Rifts werden nur gesetzt, wo der Chunk geladen ist (das Original setzte sie, sobald die Welt geladen war).
- Generierte Rifts melden ihre Taschen über `RiftGen.links` an den Server-Thread; jede Stelle nur einmal.
- Unseeded Random des Originals (Dungeon-Wahl, Monolith-Spawn in Limbo, Zufalls-Links) bleibt zufällig; Limbo-Monolithen
  ziehen aus einem Zufall aus Seed und Chunk.
- Rezepte mit Leerzeilen (`"   ","yxy","   "`) passen heute überall im Raster (26.3 kürzt Muster).
- Nicht portiert: `dd-create`/`dd-export` (Dungeon-Bau-Werkzeuge), die Brille (ItemRiftGoggles war nie registriert),
  Spells (nur für den Autor).
- Client-Prüfung offen: Portal in verlinkten Türen (hier mit dem End-Portal-Shader statt der 16 Schichten), Rift-
  Partikel (schwarzer Staub, in Taschen grau), Monolith-Modell, -Größe und -Texturen, Klänge, Limbo-Himmel und -Nebel.

## Teil 16 – BiblioCraft

Viele Blöcke mit eigenen Modellen und GUIs. Ein Großteil der Arbeit ist Client-Rendering.

### Teil 16 – Stand

**Status: erledigt.** Teilprojekt `mods/bibliocraft` (Mod-ID `bibliocraft`, FML-ID `BiblioCraft`), Werte aus der
Pack-`BiblioCraft.cfg` (`BCConfig`, SYNCED: Schlüsselwörter für Bücher/Werkzeuge/Tränke, AllowEmptyBooks=true,
Textfarbe, Schatten). Ressourcen schreibt `mods/bibliocraft/tools/gen.py`; Texturen, OBJ-Modelle und Klänge kommen beim
Bauen aus dem Original. Alte IDs (Blöcke 1071–1083 mit Metadaten, Items 5451–5457, Reading 196) in `OldGameIds`.

- **Aufbau:** Metadaten-Varianten sind eigene Blöcke (79: je vier Hölzer für Bücherregal, Trankregal, Regal,
  Werkzeugregal, Schild, Schreibtisch, Tisch; 16 Vitrinen Holz × Stoff; je 16 Farben Lampe und Laterne;
  Rüstungsständer, Setzmaschine, Druckerpresse). Jede Gruppe hat ihr Paket (`bookcase`, `shelf`, `display`, `decor`,
  `printing`) mit eigener `init` und Client-Klasse. Gemeinsam: `common/BiblioBlock` (der „angle“ der TileEntities als
  Block-Property `angle` 0..3 mit der Formel des Originals, `rotations` für die Formen), `BiblioBlockEntity` (Inventar,
  ganzer Inhalt per Update-Paket an den Client), `BiblioMenu`, `client/BiblioRender` (Lesebrillen-Blick,
  Text wie `renderText`, Items in der Welt), `client/BiblioScreen`, `client/BlockItemRenderers` (IItemRenderer →
  special model `bibliocraft:block`).
- **Darstellung:** jeder Block wie im Original über seinen Renderer: ModelBase-Modelle mit `tools/modelbase_to_layer.py`
  als ModelParts (ungespiegelt, Einheit wie `render(scale)`), Tisch/Lampe/Laterne mit eigenem OBJ-Leser
  (`decor/client/ObjModel`, Gruppen ein-/ausblenden wie im Original), Kerzenflamme als Partikel `bibliocraft:candle_flame`.
- **Verhalten:** Rechtsklick-Zonen je Winkel, Schleich-Regeln, Ablegen/Herausnehmen, GUIs mit den Slot-Positionen des
  Originals, Verzauberungskraft des Bücherregals (1/8 je Buch), Redstone-Buch (Signal = Fachnummer, nicht nach vorne),
  Vitrine (Deckel mit Klang, Redstoneblock darin = 15, Wandmontage), Licht von Regal/Vitrine/Schild/Tisch nach dem
  Block darauf (NeoForge `AuxiliaryLightManager`), Rüstungsständer zwei Blöcke hoch (Rüstung tauschen, bestromt alle
  vier; Darstellung über den Vanilla-Equipment-Layer), Tisch verbindet sich mit Nachbartischen, Lampe/Laterne mit
  Stil nach der Seite, Maßband, Schreibtisch mit eigenem Buch-Editor, Setzmaschine (Bücher als Dateien unter
  `<Welt>/books` bzw. `config/books`, Redstone-Druck nach 10 Ticks, Verzauberungs-Platten), Druckerpresse (200 Ticks je
  Tinte und Druck, Animation). Lesebrille, getönte Brille, Monokel (Helm ohne Haltbarkeit) und die Verzauberung
  Reading (nur auf Büchern am Tisch, `primary_items` leer) zeigen die Namen auf Regalen, Vitrinen, Schildern usw.
- **Prüfung:** `clean pack` grün; Testserver bis `Done` ohne Fehler (Project Zulu wie in Teil 13–15 vorübergehend
  heraus). Per RCON: Redstone-Buch in Fach 3 gibt 3 an die Seiten und hinten, 0 nach vorne; Rezepte über den Crafter
  (Bücherregal, Lampe färben). Im Client (Testserver + `--quickPlayMultiplayer`): alle Blöcke mit Inhalt in der Welt,
  3D-Items in der Hotbar, Brillentext am Bücherregal, Bücherregal-GUI.
- **Nebenbei behoben (Teil 15):** `DimDoorBlockEntity.setLevel` führte sein Update beim Chunk-Aufbau sofort aus; der
  `setChanged` auf den eigenen, noch nicht fertigen Chunk ließ den Server-Thread auf sich selbst warten
  (Watchdog-Absturz beim Laden eines Chunks mit Tür). Jetzt eingereiht (`schedule(new TickTask(...))`).

**Offen / bewusst abweichend:**

- Item-Darstellung im Inventar/in der Hand: Die IItemRenderer verschoben je Ansicht von Hand; hier wird das Modell im
  Einheitswürfel gezeichnet und die Vanilla-Block-Transformationen gelten (Laterne 1,4- statt 1,7-fach, Lampe 1,0
  statt 1,2).
- Gegenstände auf Regalen, im Schild, in der Vitrine und auf dem Tisch werden mit der GROUND-Ansicht von 26.3 gelegt und
  auf die Unterkante von 1.5.2 verschoben; nur der „Fancy Graphics“-Weg ist portiert.
- Pakete: `BiblioMCBEdit` schickt nur Seiten und Titel (kein ganzer ItemStack), der Server prüft alles; `BiblioAStand`
  entfällt (26.3 synchronisiert die Ausrüstung selbst). Buchdateinamen ohne `/ \ :`.
- Werkzeug-Test des Werkzeugregals über die Komponenten TOOL/WEAPON statt ItemTool/ItemSword/ItemHoe; Ausrüstbares
  ohne Blockitem gilt als Rüstung (auch Elytra); Köpfe nach Kopftyp statt Name.
- Harvest-Level „Axt 2“: in 26.3 sind alle Äxte schnell (das Original baute mit Holz-/Steinaxt in Handgeschwindigkeit ab).
- Die Druckerpressen-GUI öffnete das Original nie: Menü und Screen sind portiert, werden aber auch hier nicht geöffnet.
- Redstone-Buch: Der Name „by &lt;Spieler&gt;“ wird nur angezeigt, nicht ins Item geschrieben (Client darf den Stapel
  nicht ändern).
- Client-Prüfung offen: genaue Lage, Drehung und Größe von Modellen, Items und Brillentexten aller Blöcke gegen das
  Original, OBJ-UVs, Rüstung am Ständer, Animation der Presse, Buch-Editor (Tastatur), Bücherliste der Setzmaschine.

## Teil 17 – BetterStorage, Backpacks, qCraft

- BetterStorage: verschlossene Truhen, Schlüssel, Rüstungsständer, Crates.
- Backpacks.
- qCraft: Quanten-Blöcke, Observer-Dust.

### Teil 17 – Stand

**Status: erledigt.** Drei Teilprojekte; `clean pack` grün, gemeinsamer Testserver mit allen Mods bis `Done` ohne
Fehler (Project Zulu wie in Teil 13–16 vorübergehend heraus).

**BetterStorage** – Teilprojekt `mods/betterstorage` (Mod-ID `betterstorage`, FML-ID „BetterStorage“), `BSConfig`
(SYNCED, Werte aus der Pack-`BetterStorage.cfg`: backpackRows=3, reinforcedChestColumns=13,
enableCrateInventoryInterface=false). Ressourcen samt sieben Verzauberungen schreibt `mods/betterstorage/tools/gen.py`,
Texturen und `locker.obj` kommen beim Bauen aus dem Original. Alte IDs: Blöcke 1051–1056, Items 5426–5428,
Verzauberungen ab 180, Entity `BetterStorage.Frienderman` in `OldGameIds`.

- **Aufbau:** `BS` registriert Kiste, acht verstärkte Truhen, Spind, Rüstungsständer, Rucksack und Ender-Rucksack
  (Block und Item), Schlüssel, Schloss, Schlüsselbund, Frienderman, Rezepttypen `betterstorage:key`/`betterstorage:lock`
  (Basis `ComboRecipe`) und Rucksack-Attachments. Öffnen über die Menü-Typen, Rucksack-Status als synchronisiertes
  Attachment, `network/` nur für das Teleport-Partikel. Client: Truhe mit dem 1.5.2-ModelChest und Schloss als Item,
  Spind aus `locker.obj`, Rüstungsständer über die Vanilla-Equipment-Layer, Rucksack als Block und am Träger
  (`BackpackArmorModel`, Deckelwinkel über einen Render-State-Modifier), Screens mit den Maßen des Originals,
  Special-Item-Renderer `betterstorage:block`, Frienderman-Renderer. JEI: Beispielrezepte und Info-Seiten.
- **Verhalten:** Kisten-Stapel mit gemeinsamem Inventar (pro Welt gespeichert); Schlüssel-/Schlossrezepte übernehmen
  die Id, der Schlüssel bleibt im Raster, einzelner Schlüssel wird mit Eisen beschichtet oder mit der 1.5.2-Farbmischung
  gefärbt. Rucksack im Brust-Slot, Schleichen + Rechtsklick setzt ihn ab, andere öffnen ihn von hinten, Inhalt bleibt bei
  keepInventory. Zombies, Pigmen und Skelette spawnen mit den Wahrscheinlichkeiten aus CommonProxy mit Rucksack
  (Zufallsinhalt), 1/80 der Endermen wird zum Frienderman. Keine eigene Dungeon-Beute (wie im Original).
- **Prüfung:** per Crafter/RCON alle Rezepte (Schlüssel-Kopie mit gleicher Id, Schloss gibt den Schlüssel zurück),
  zwei Kisten als Stapel (Kapazität 36, Inhalt in beiden sichtbar), falscher/richtiger Schlüssel, Rucksack anlegen,
  absetzen, wieder anlegen mit Inhalt, Frienderman (60 Leben, Tausch Endertruhe → Ender-Rucksack direkt aufgerufen).

**Offen / bewusst abweichend (BetterStorage):** Thaumcraft-Addon entfällt; Kupfer-, Zinn-, Silber- und Zink-Truhen nur,
wenn ein Mod den `c:`-Tag füllt; Hervorhebung des Rüstungsständer-Slots und Tooltip des Ender-Rucksacks fehlen; Inventory
Tweaks nicht angemeldet; Items im Inventar gedreht statt mit den Versätzen von 1.5.2; Menü-Öffnen nicht per FakePlayer
prüfbar; Frienderman tauschte im Testlauf nicht von selbst (teleportierte weg, siehe `mods/betterstorage/TODO.md`).
Client-Prüfung offen: alle Renderer und ihre Ausrichtung, Rucksack am Spieler, Screens, JEI.

**Backpacks** – Teilprojekt `mods/backpacks` (Mod-ID `backpack`, FML-ID „Backpack“), Werte aus der Pack-`Backpack.cfg`
in `BackpackConfig` (STARTUP: backpackSizeL=6, backpackSizeM=3, enderRecipe=0, maxBackpackAmount=0,
openOnlyWearedBackpacks=false, airshipModCompatibility=false). Ressourcen schreibt `mods/backpacks/tools/gen.py`,
Texturen kommen beim Bauen aus dem Original. Alte IDs 5401 (Damage 0–16, 32–48, 31999), 5402, 5403, 5404 (18, 50) in `OldGameIds`.

- **Aufbau:** jede Damage-Variante ein eigenes `BackpackItem` (17 kleine, 17 große, Ender, Werkbank klein/groß),
  gebundenes und gegerbtes Leder. Inhalt in `DataComponents.CONTAINER`, Werkbank-Raster in `backpack:crafting`.
  Tragbar im Brust-Slot (`Equippable`, Modell `ModelBackpack` über `getHumanoidArmorModel`).
- **Verhalten:** Menüs mit den Slot-Positionen des Originals (Ender-Rucksack zeigt die Endertruhe), Werkbank-Rucksack
  füllt nach dem Herausnehmen aus dem Lager nach und tauscht Behälter; kein Rucksack in Rucksack-Slots, eigener Slot
  gesperrt. Schleich-Rechtsklick benennt um (Paket `backpack:rename`), Taste B öffnet den getragenen Rucksack.
  Rezepte als JSON mit Konfigurationsbedingung `backpack:config`; Spezialrezepte `backpack:recolor_backpack` und
  `backpack:enhance_backpack` übernehmen Inhalt und Name. JEI: Beispiele für Färben/Vergrößern, Werkbank-Rucksäcke als
  Stationen mit Recipe-Transfer. Inventory Tweaks per IMC.
- **Prüfung:** `pack` grün, Testserver bis `Done` ohne Fehler (Project Zulu vorübergehend heraus). Per RCON über den
  Crafter alle Rezepte, Vergrößern/Färben/Entfärben mit Inhalt und Name, Ofen; per FakePlayer Shift-Klick, Slot-Sperren,
  Werkbank-Rucksack (Nachfüllen, Raster bleibt im Item).

**Offen / bewusst abweichend (Backpacks):** kein Rüstungswert (das Original zeigte 3 bzw. 1,5, schützte aber praktisch
nicht); Farbstoffe nach `c:dyes`, andere Items im Färberezept lassen es fehlschlagen; Name nur beim Umbenennen;
Modell folgt dem Körper beim Schleichen; Öffnen auch aus der Nebenhand; NEI-Overlay entfällt. Client-Prüfung offen:
Modell am Spieler, Icons, GUIs, Umbenennen, Taste B, JEI und Transfer.

**qCraft** – Teilprojekt `mods/qcraft` (Mod-ID `quantumcraft`, FML-ID „QuantumCraft“), Konfiguration
`QuantumCraft.toml` (SYNCED, alle drei general-Schalter wie im Pack aus; Ersatzrezept für Staub über die Bedingung
`quantumcraft:config`). Ressourcen schreibt `mods/qcraft/tools/gen.py`. Alte IDs 2371–2374, 7526–7528 in `OldGameIds`/`OldBlockStates`.

- **Aufbau:** Quantenerz und leuchtendes Quantenerz (Licht 9, Zufallsticks), qBlock (66 Tarnblöcke in der Reihenfolge
  von `getImpostorBlockList`), Quantencomputer mit Menü, Quantenstaub, die Essenzen eos/eoo/eoe als eigene Items,
  Quantenbrille (Helm mit Kamera-Overlay). Item-NBT als Data Components (`qblock`, `entanglement`, `nonce`,
  `stored_area`), Verschränkungen in `<Welt>/quantum/entanglements.bin`. Erz-Generator über worldgen152, wie im Pack aus
  (`enableWorldGen=false`), in `tools/expect.sh` eingetragen. JEI-Plugin für die qBlock- und Verschränkungsrezepte.
- **Verhalten:** qBlock zeigt ohne Brille die angesehene Seite, ein ODB behält die zuletzt gezeigte, die Brille macht
  den Beobachter blind. Quantencomputer: Rahmen aus qBlocks (Obsidian/Gold), Obsidian-Säulen, Glas, Eis tauscht den
  Bereich mit dem Zwilling, ohne Zwilling speichern/wiederherstellen; Fehlercodes 0–8 wie im Original.
- **Prüfung:** `pack` grün, Testserver bis `Done` ohne Fehler (Project Zulu vorübergehend heraus). Per Crafter/RCON:
  qBlock-Rezept setzt die Seiten wie das Original (unzulässige Ecke ohne Ergebnis), eos, Computer, Brille, verschränkte
  Paare mit Frequenz; per FakePlayer: Seitenwahl nach Blickrichtung mit/ohne Brille, Bereich speichern und
  wiederherstellen, Fehlercodes 1/2/5; Erz gibt Quantenstaub.

**Offen / bewusst abweichend (qCraft):** Quantenfackel, -draht, -logik und Qubits nicht portiert (im Original per
Schalter aus). Teleport zwischen zwei verschränkten Computern nur im Code geprüft. Client-Prüfung offen: qBlock-Modell
und Fuzz-Textur, Brillen-Overlay und -Rüstung, Computer-GUI, Staub-Partikel, animierte Texturen.

## Teil 18 – ChocoCraft, Infernal Mobs

- ChocoCraft: Chocobos (Zucht, Reiten, Farben) und Gysahl-Pflanzen in der Welt.
- Infernal Mobs: Elite-Mobs mit Modifikatoren, Loot.

### Teil 18 – Stand

**Status: erledigt.** Zwei Teilprojekte; `clean pack` grün, Testserver mit allen Mods bis `Done` ohne Fehler (Project
Zulu wie in Teil 13–17 vorübergehend heraus).

**ChocoCraft** – Teilprojekt `mods/chococraft` (Mod-ID `chococraft`), Konfiguration `ChocoConfig` (STARTUP, Werte der
Pack-`chocobo_config.txt`). Ressourcen schreibt `mods/chococraft/tools/gen.py` (Chocopedia-Seiten aus dem Original),
Texturen und der Kweh-Sound kommen beim Bauen aus dem Original. Alte IDs: Blöcke 1101–1103, Items 5476–5496 (+256),
Entity-Namen `chococraft.Chocobo<Farbe>`/`chococraft.Chicobo` in `OldGameIds`/`OldBlockStates`.

- **Aufbau:** `AbstractChocobo` (TamableAnimal: Name, Geschlecht, folgen/wandern/bleiben, Zähmen mit Gysahl 1/3,
  Heilen, Strohheilung am Kessel), `Chocobo` (eine Klasse, Farbe = Entity-Typ, Werte in `ChocoboColor`; Sattel,
  Satteltaschen 36 / Packtaschen 54 Plätze als ChestMenu, Reiten, Federn legen, Paarung), `Chicobo` (Farbe synchron,
  Wachstum −2 je Tick wie im Original, Kuchen), `ChocoboBreeding` (die neun Zuchttabellen Zweig für Zweig, mit den
  Durchfall-Fehlern des Originals), `PurpleChocoboEgg`, `ChocoboSpawner` (eigener Spawner alle 200 Ticks je Spieler wie
  `ServerSpawnTickHandler`, gelb in den Pack-Biomen, lila im Nether/End). Die alte Nicht-KI-Steuerung
  (`updateEntityActionState`) als ein Goal (`Chocobo.ActionGoal`), Küken mit den KI-Aufgaben des Originals.
  Blöcke: Gysahl (Blume), Gysahl-Stängel (5 Stufen, Wachstum und Mutation wie BlockGysahlStem), Stroh. Chocopedia
  (Buch und Chocobo-Seite mit Umbenennen, Name zeigen, folgen, Ausrüstung abwerfen, Besitzer wechseln; Paket
  `chococraft:chocobo_action`), Feder auf Bücherregal → Chocopedia, Buch und Feder am Chocobo → Chocopedia.
- **Weltgenerierung:** `chococraft.gen.ChocoWorldGen` (WorldGenGysahls, 161/1000 Chunks, mit dem Höhenfehler
  `getTopSolidOrLiquidBlock(x, x)` des Originals, deshalb selten an der Oberfläche) und die Chocopedia in sieben
  Truhenkategorien (Gewicht 2); in `tools/expect.sh` und `verify_blocks.py` (1101) eingetragen.
- **Prüfung:** per RCON und temporärem FakePlayer-Befehl: Zähmen, Liebe mit goldenem Gysahl → Küken, Abklingzeit
  ♂ 3000/♀ 9000, Zuchttabelle gelb×gelb (10.000 Würfe: 51/25/24 %), Sattel/Taschen/Feder-Umschalten, Todesdrops
  (Ausrüstung, Federn, Keulen), Küken wächst zum Chocobo seiner Farbe, Ei schlüpft lila, Stängel-Ernte
  (40 reife: 34 grün, 6 Loverly, 47 Samen), Rezepte über den Crafter. `tools/expect.sh 1234 find 260` +
  `verify_blocks.py`: 0 Blöcke abweichend (zwei Schädel in Chunk 0 von Lava/Wasser überflossen, weil der Chunk
  vorher für Tests geladen war).

**Offen / bewusst abweichend (ChocoCraft):** Hunger nicht portiert (im Pack aus); Schleichen steigt in 26.3 ab, darum
sinken Flieger ohne gedrückte Sprungtaste statt mit Schleichen; der seitliche Versatz von Reiter und Modell entfällt;
Mutationsraten und Generierungsrate fest aus dem Pack (Loot-Tabelle, `ChocoWorldGen`); Gras-Samen 1/8 · 3/19
(Natura rechnet noch mit 3/16); Besitzer wechseln nur auf einem Server; Packtaschen-Inhalt geht bei „Drop Gear“ wie im
Original verloren. Reiten, Menüs per FakePlayer nicht prüfbar, Spawner nur im Code geprüft. Client-Prüfung offen:
Modelle und Animationen (Flügel, Beine), Texturen je Ausrüstung, Namensschild, Chocopedia-Bildschirme, Reiten und
Fliegen, Verkleidung.

**Infernal Mobs** – Teilprojekt `mods/infernalmobs` (Mod-ID `infernalmobs`), portiert von einem Subagenten, in der
Hauptsession geprüft. Konfiguration `IMConfig` mit den Pack-Werten (eliteRarity 30, ultra 10, inferno 10, alle 26
Modifikatoren an, verbotene Mobs von Harken Scythe, Project Zulu und Dimensional Doors als Entity-IDs, Beuteliste als
Item-IDs aus den alten Nummern).

- **Aufbau:** `InfernalMobsCore` (Auswahl der Modifikatoren wie `createMobModifiers`, Leben als `max_health`-Modifikator
  = Leben × Anzahl × Faktor, Beute mit Verzauberung je 5 Modifikatoren und 25 XP), `MobModifier` als verkettete Liste,
  26 `MM_*`-Klassen, Modifikator-Namen im Entity-NBT „InfernalMobsMod“ und als synchronisiertes Attachment,
  `InfernalMobsClient` (HUD mit Name, Lebensleiste, Modifikator-Zeilen; Partikel um Rare-Mobs).
- **Prüfung:** Zombie mit „Regen 1UP Vengeance Bulwark“ per NBT: Leben 80/80, Bulwark halbiert, Regen heilt, Tod durch
  Spieler gibt ein Item der Liste und 25 XP.

**Offen (Infernal Mobs):** Abweichungen und Offenes in `mods/infernalmobs/TODO.md`; Beuteliste um Falling Meteors
(Teil 19) und Xeno's Reliquary (Teil 20) ergänzen. Client-Prüfung offen: HUD, Partikel.

## Teil 19 – LegendGear, Falling Meteors

- LegendGear: Zelda-Ausrüstung, Bombenblumen und Sträucher per Weltgenerierung.
- Falling Meteors: Meteoriten-Events und -Erze.
- Infernal Mobs: die Meteors-Items der alten Beuteliste (IDs 6058–6081, siehe `mods/infernalmobs/TODO.md`) in
  `IMConfig.DEFAULT_ITEMS` eintragen.

## Teil 20 – Xeno's Reliquary, Secret Rooms

- Xeno's Reliquary: Artefakte, Handgun, Alkahest-Rezepte → JEI.
- Secret Rooms: Tarnblöcke. Die Tarnung zeigt das Modell eines anderen Blocks und ist damit ein Render-Thema.
- Infernal Mobs: das Reliquary-Item der alten Beuteliste (ID 6408) in `IMConfig.DEFAULT_ITEMS` eintragen.

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
