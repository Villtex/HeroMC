# HeroMC – Hexxit 1:1 für Minecraft 26.3

Ziel: das originale **Hexxit 1.0.11** (Technic, Minecraft 1.5.2, Forge 7.8.1.737) Mod für Mod auf
**Minecraft 26.3 / NeoForge 26.3.0.102** portieren: dieselben Mods, dieselben Inhalte, dasselbe Verhalten.
Keine Ersatz-Mods.

Zwischen 1.5.2 und 26.3 ist praktisch jede Schnittstelle neu (Registries statt Block-IDs, Blockstates statt
Metadata, Data Components statt NBT, datengetriebene Rezepte und Weltgenerierung, neue Render-Pipeline).
Jeder Mod wird deshalb gegen das dekompilierte Original als Referenz **neu geschrieben**.

## Vorgaben

- **Loader:** NeoForge 26.3.
- **Terrain:** Die Oberfläche (Geländeform, Biom-Verteilung, Deckschichten) soll exakt der des alten Packs
  entsprechen. Dafür gibt es einen eigenen Chunk-Generator, der die Generierung von 1.5.2 nachbildet
  (`mods/worldgen152`) und gegen Welten des Original-Servers geprüft wird.
- **Sodium und Iris:** Der Client läuft mit **Sodium** (`build/pack/client-mods`, im Testclient standardmäßig an,
  `./gradlew runClient -Psodium_iris=false` für den Vanilla-Renderer). Geprüft mit Sodium 0.9.3: Tarnblöcke (Secret Rooms,
  Tinkers-Zugbrücke/Landmine, qCraft) über NeoForge-ModelData, Flüssigkeiten, Glas, Blattfärbung, Block-Entity-Renderer,
  leuchtende Mobs und die Mapwriter-Karte sehen aus wie mit dem Vanilla-Renderer. Eigene Mods brauchen dafür keine
  Mixins in den Renderer; neue Client-Darstellung bleibt bei Vanilla-Render-Typen und `DynamicBlockStateModel` mit
  ModelData. **Iris** ist vorbereitet (`iris_version` in `gradle.properties`), aber aus: Das neueste Iris für 26.3
  (1.11.7) stürzt auf NeoForge 26.3.0.102 mit jedem Shaderpack ab, auch ganz ohne Hexxit-Mods, und Sodium 0.9.3 lässt
  erst Iris ≥ 1.12.1 zu. Sobald das erscheint: Version eintragen, mit Shaderpack testen.
- **Rezeptanzeige:** Not Enough Items wird nicht portiert. Stattdessen sind alle portierten Mods mit
  **JEI** kompatibel; eigene Rezepttypen bekommen ein JEI-Plugin.
- **Twilight Forest** ist in die Teile 24a–24f aufgeteilt, **Tinkers' Construct** in die Teile 25a–25f.
- **Tinkers' Construct 3.12.1 für Hexxit Integrated (Teil 27a erledigt, 27b–27k offen):** Beide Tinkers-Versionen sind
  installiert, der Welttyp entscheidet: Legacy hat das originale 1.4.3 (`tconstruct`), Integrated das heutige 3.12.1
  (`tconstruct3`, mit Mantle). Rezepte, Beute und Weltgenerierung der jeweils anderen Version laden nicht, JEI und
  Kreativ-Tabs blenden sie aus. Lösung und Aufteilung in `docs/PLAN.md`, Teil 27. Stand 27a: Gerüst `mantle` und
  `tconstruct3` (Assets aus dem Original-Tag), Daten-Konverter, Welttyp-Schalter `HexxitWorldType`; 1.4.3-Rezepte
  laden nur noch in Legacy-Welten.
- **Welttypen (Teil 26 erledigt):** „Hexxit Integrated“ ist der Standard (`minecraft:normal`: 26.3-Generierung
  plus Hexxit-Inhalte, offen für fremde 26.3-Mods: deren Biome, Bauwerke, Rohre und Werkzeuge arbeiten mit der Welt).
  „Hexxit Legacy (1.5.2)“ (`hexxit:legacy`) ist die bitgenaue 1.5.2-Welt. Am Server: `level-type=hexxit\:legacy` in
  `server.properties` für Legacy, ohne Eintrag Integrated. Erze, Pflanzen, Bäume und Nether-Generatoren sind in Integrated
  eingehängt (Teil 26b). Bauwerke (Battle Towers, Better Dungeons, Project-Zulu-Pyramide/Labyrinth/Friedhof/Oase)
  sind echte 26.3-Strukturen mit Struktur-Sets (`/locate structure battletowers:tower`, Tag `#hexxit:structures`), kleine
  Bauten (Ruinen, Risse) Features, die Tinkers-Werkstatt und -Schmelze Häuser der 26.3-Dörfer (Teil 26c). Die
  ExtrabiomesXL-Biome kommen über eine **TerraBlender**-Region dazu (Bibliothek, liegt mit im Pack), mit eigenen
  Oberflächen, Biom-Tags (`c:`, `minecraft:has_structure/*`) und ihrer 1.5.2-Dekoration; die Mob-Spawns aller Mods
  gelten über Biom-Tags auch für Vanilla- und fremde Biome (Teil 26d). Erze, einzelne Generatoren und das Gewicht
  der EBXL-Region lassen sich in `config/worldgen152-startup.toml` einstellen.
- **Fremde Mods (Teil 26e):** Kisten, Regale und Tinkers-Tische bieten Item-Capabilities, Tinkers-Tanks/Abfluss/Gießblöcke
  Fluid-Capabilities; Erze, Metalle, Kisten, Werkzeuge und Bosse tragen die `c:`- und `minecraft:`-Tags, Hexxit-Rezepte und
  die Schmelzerei nehmen fremde Metalle über diese Tags. Der 1.5.2-Nahkampf (`combat152`) gilt in Legacy immer, in anderen
  Welttypen nach `integratedCombat152` in `config/combat152-startup.toml` (Standard an; im Einzelspieler auch per Schalter
  „1.5.2-Kampf“ in den Weltoptionen des Pausenmenüs). Project-Zulu-Brauen ändert den
  Vanilla-Sprungtrank nur in Legacy.
- **Getestet mit fremden 26.3-Mods (Teil 26f):** Biomes O' Plenty, Towns and Towers, Assorted Ores, Pipez, Immersive
  Ores und Jade (Versionen und Download: `tools/extra_mods.py`). In Hexxit Integrated laufen sie neben allen Hexxit-Mods
  ohne Fehler: BOP- und EBXL-Biome kommen nebeneinander vor, `/locate` findet Hexxit- und fremde Bauwerke, Pipez-Rohre
  ziehen Items aus BetterStorage-Kisten und Flüssigkeit aus Tinkers-Tanks (und füllen sie), die Schmelzerei schmilzt
  fremdes Zinn-, Silber- und Aluminiumerz, Hexxit-Rezepte nehmen fremde Barren, fremde Spitzhacken bauen Hexxit-Erze ab
  und umgekehrt. **Grenzen:** In Hexxit Legacy erzeugen fremde Mods ihre Erze und Features weiter (wie unter Forge 1.5.2),
  die Welt ist dann nur ohne sie bitgenau. Fremde Biome und Bauwerke gibt es in Legacy nicht, denn die Biome kommen aus
  der 1.5.2-Biomschicht. Tinkers-Kupfererz entsteht in Integrated nicht, weil Vanilla eigenes Kupfer hat. Anzeigen (Jade)
  und die Darstellung fremder Biome sind nur im Client prüfbar.
- **Leistungs-Mods:** **C2ME** (`c2me-neoforge` 0.4.2-alpha.0.110), **Lithium** (0.26.4) und **ScalableLux** (0.3.0-alpha.0.17)
  laufen mit dem Pack, in Hexxit Legacy und Integrated (`tools/extra_mods.py --set perf --install`). Die Hexxit-Mods brauchen
  dafür keine Änderung: Sie haben keine Mixins, und die Weltgenerierung (`worldgen152`) ist schon für parallele Chunk-Erzeugung
  gebaut (Gelände je Chunk ohne geteilten Zustand, Population in Kacheln über `FutureTask`, Caches synchronisiert). Legacy bleibt
  mit den drei Mods bitgenau (Oberwelt, Nether und Twilight Forest 0 Abweichungen), in Integrated entstehen dieselben Bauwerke.
- **Weltgenerierung in Integrated beschleunigt:** 16×16 neue Chunks brauchen am Testserver (4 Kerne) 12–15 s statt
  53–68 s, mit C2ME/Lithium/ScalableLux 8–9 s statt 15–25 s. Die Hilfs-Chunks der Hexxit-Bauwerke fragen ihre Biome nicht
  mehr für jeden Block beim Klima-Rauschen ab, und die Chunk-Brücke liest für jeden Generator nur noch die Abschnitte, die
  er anfasst. Die Welt bleibt dieselbe (alte und neue Brücke im selben Lauf verglichen, Legacy bytegleich).

## Arbeitsplan

Der Port ist in Teile aufgeteilt, die je in eine Session passen: [`docs/PLAN.md`](docs/PLAN.md).

## Aufbau

| Pfad | Inhalt |
|---|---|
| `mods/<name>/` | ein portierter Mod = ein Gradle-Teilprojekt (Name wie im Technic-Manifest) |
| `mods/combat152/` | Nahkampf wie in 1.5.2 für das ganze Pack: keine Abklingzeit und keine Anzeige dafür, kein Schwungangriff, Kritische Treffer, Axt-/Schaufelschaden, Stärke/Schwäche, Schärfe/Bann/Nemesis, Angriffs-Rückstoß und Blocken mit dem Schwert wie damals, keine Schlag-Geräusche |
| `mods/<name>/original-assets.txt` | welche Texturen/Sounds/Vorlagen der Mod beim Bauen aus Original-Archiven übernimmt (`[archiv]` wechselt das Archiv) |
| `buildSrc/` | Gradle-Task, der diese Dateien lädt (MD5-geprüft) und in das gebaute Jar legt |
| `pack/hexxit-1.0.11.json` | exaktes Manifest des Originalpacks: Mods, Versionen, Prüfsummen, Download-URLs |
| `tools/reference.py` | lädt das Original und erzeugt lesbare Referenzquellen (lokal, wird nicht eingecheckt) |
| `tools/biomes.py` | erzeugt die Biom-Daten (Klima, Farben aus den 1.5.2-Farbkarten, Mob-Spawns) |
| `tools/original_server.sh`, `tools/original_spawn.py`, `tools/compare_original.py` | Original-Server 1.5.2 (Vanilla oder Hexxit) einmal laufen lassen und seine Welt mit dem Kern vergleichen |
| `tools/integrated_tags.py` | Biom-Tags der EBXL-Biome und Mob-Spawns über Biom-Tags für Hexxit Integrated (Teil 26d) |
| `tools/extra_mods.py` | fremde NeoForge-26.3-Mods für den Kompatibilitätstest nach `run/mods-extra/` laden (`--install` kopiert sie nach `run/server/mods`, `--uninstall` entfernt sie; Teil 26f; `--set perf` = C2ME, Lithium, ScalableLux, `--set all` = beide) |
| `tools/compat_tags.py` | `c:`-/`minecraft:`-Tags aller Mods und Rezepte über Tags (Teil 26e); nach jedem `gen.py` erneut laufen lassen |
| `tools/rcon.py` | kleiner RCON-Client für Verhaltenstests am laufenden Testserver |
| `tools/bench_chunks.py`, `tools/jfr_top.py`, `tools/compare_worlds.py` | Weltgenerierung messen (Zeit für ein Chunk-Gebiet per RCON), JFR-Profil zusammenfassen, Blöcke zweier 26.3-Welten vergleichen |
| `tools/modelbase_to_layer.py` | überträgt die Geometrie eines 1.5.2-`ModelBase` (Boxen, Hierarchie, Texturversätze) in 26.3-`createBodyLayer`-Zeilen |
| `gradle/neoforge.mods.toml` | gemeinsame Vorlage für die Mod-Metadaten |
| `docs/JEI.md` | Vorlage für das JEI-Plugin eines Mods (JEI ersetzt NEI) |
| `.claude/skills/worldgen152/` | Projekt-Skill: Weltgenerierung eines Mods in `worldgen152` einhängen, Setzlinge, Nether, eigene Dimensionen (auch mit eigenem Gelände und Population wie Twilight Forest), Prüfung |
| `.claude/skills/client-render-263/` | Projekt-Skill: HUD-Ebenen, Gegenstände am Spielermodell, Rüstungen mit eigenen Helm-Modellen, durchscheinende Blockmodelle, Rechtsklick vor dem Block abfangen, Blöcke mit dem Modell eines anderen Blocks (Tarnblöcke), Blöcke mit TESR/IItemRenderer als BlockEntityRenderer + Special-Item-Renderer (26.3), Vorlage BiblioCraft |
| `.claude/skills/menu-gui-263/` | Projekt-Skill: eigene GUI mit Container (Menü, Screen, Pakete Client → Server, Server-Konfiguration für die Vorschau im Client) |
| `.claude/skills/wood-sets-263/` | Projekt-Skill: Holzarten und Pflanzen eines Mods (Planken, Stämme, Blätter, Setzlinge, Treppen bis Werkbänke, Brennwert, Rezeptkonflikte mit Vanilla), Vorlage Natura |
| `.claude/skills/mobs-263/` | Projekt-Skill: Mobs, Spawn-Eier, Boote, Mob-Drops und Spawns (Biome-Modifier), viele Mobs auf einmal (Modell-Konverter, Easing, Fliegen, Reiten, Mehrteiler, Boss), Mobs ohne KI und Reiter-Bewegung, eigene Spawner, Zusatzdaten an fremden Mobs, Prüfen ohne Spieler; Vorlagen Natura, Project Zulu, ChocoCraft |
| `.claude/skills/server-tests-263/` | Projekt-Skill: Verhalten ohne Spieler am Testserver prüfen (Rezepte über Crafter + RCON, temporärer FakePlayer-Befehl, forceloaded Chunks) |
| `.claude/skills/treecapitator-trees/` | Projekt-Skill: Bäume und Äxte eines Mods bei TreeCapitator anmelden (Daten-Registry `treecapitator:tree`, Item-Tags) |
| `.claude/skills/templates-152/` | Projekt-Skill: Gebäude-Vorlagen und Schematics eines 1.5.2-Mods übernehmen (Daten-Archive beim Bauen ins Jar, NBT ohne Spielklassen, Kisten/Schilder/Bilder/Mobs über worldgen152, Text-Vorlagen ohne Umwandlung, Vorlagen zur Laufzeit in die Welt, Prüfung), Vorlagen Better Dungeons, Ruins, Dimensional Doors |
| `.claude/skills/items-weapons-263/` | Projekt-Skill: Waffen, Stäbe, Bögen, Geschosse und Spezial-Items (Schwert-Werte, Blocken, Geschoss-Entity je Typ, magische Pfeile/Köcher, Taschen, Damage-Varianten mit alten IDs, Kapseln, Einmal-Spawner), Vorlage Better Dungeons |
| `.claude/skills/api-263-lookup/` | Projekt-Skill: 26.3-Signaturen und Vanilla-Daten lokal nachschlagen (javap, Vineflower, NeoForge-Quellen), Loot-/Advancement-Format, bekannte Umbenennungen, Ressourcenpfade nur klein, ICraftingHandler über ItemCraftedEvent, WAV→Ogg beim Asset-Import |
| `.claude/skills/tinkers3-263/` | Projekt-Skill: Tinkers' Construct 3.12.1 + Mantle (Teil 27): Mod-ID `tconstruct3` neben 1.4.3, Quellen vom Tag, Assets mit umbenanntem Namensraum, Daten-Konverter und Bericht, Daten ordnerweise einschalten, Welttyp-Regel in Daten und Code (`HexxitWorldType`) |
| `.claude/skills/tinkers-tools-263/` | Projekt-Skill: Tinkers' Construct weiterbauen (Komponente `tconstruct:tool`, Teile/Materialien/Muster, alte IDs, Modifikatoren, Schmelzerei-Tabellen und Flüssigkeiten, Prüfen ohne Spieler) |
| `.claude/skills/twilight-forest-263/` | Projekt-Skill: Twilight Forest weiterbauen (Kern `twilightforest.gen`, Bauwerke `gen.structure` mit Übersetzungsskript, Starts, Truhen, Spawnlisten, Prüfung mit `expect.sh … twilight structures`) |
| `.claude/skills/headless-client/` | Projekt-Skill: Testclient im Cloud-Container ohne Bildschirm starten (Xvfb, `SDL_VIDEO_FORCE_EGL=1`, Screenshots, xdotool, Testserver beitreten), Sodium/Iris gegen den Vanilla-Renderer vergleichen |
| `.claude/skills/integrated-263/` | Projekt-Skill: Welttyp Hexxit Integrated (Teil 26) – 1.5.2-Generator als 26.3-Feature mit Biom-Modifier einhängen, Chunk-Brücke `OldGeneratorFeature` (Verschiebung, Höhenfenster, Biome über Tags, was ersetzt wird), Bauwerke als 26.3-Strukturen (`worldgen152:old_structure`), 1.5.2-Dorfteile in 26.3-Dörfern, Legacy/Integrated am Server umschalten und prüfen, Weltgenerierung messen und Gleichheit prüfen |
| `.claude/skills/compat-263/` | Projekt-Skill: Schnittstellen für fremde Mods (Teil 26e) – Item-/Fluid-Capabilities mit der Transfer-API, `c:`-Tags über `tools/compat_tags.py`, Rezepte über Tags, Daten nur in Legacy (`worldgen152:world_type`), Prüfbefehl `/hexxitcompat`, Test mit fremden Mods (Pipez-Rohre per NBT, `mine`) |

## Bauen und Starten

Voraussetzung ist nur ein JDK; das passende JDK 25 lädt Gradle bei Bedarf selbst.

```
./gradlew pack         # baut alle Mods nach build/pack/mods, Client-Extras (Sodium) nach build/pack/client-mods
./gradlew runServer    # Testserver mit allen portierten Mods (run/server)
./gradlew runClient    # Testclient mit allen portierten Mods und Sodium (run/client); -Psodium_iris=false ohne
```

## Referenzquellen des Originals

```
python3 tools/reference.py            # alles (ca. 3 Minuten)
python3 tools/reference.py --only tconstruct,natura
```

Lädt die 55 Archive vom Technic-Mirror (MD5-geprüft), übersetzt die verschleierten Namen mit den
MCP-Mappings für 1.5.2 und dekompiliert nach `.reference/<mod>/src`; Texturen, Sounds und Modelle landen in
`.reference/<mod>/res`. `.cache/` und `.reference/` stehen in `.gitignore`: Die Originale gehören ihren
Autoren und werden hier nicht weiterverteilt.

Aus demselben Grund liegen keine Original-Texturen im Repository. Sie werden erst beim Bauen aus dem
Technic-Archiv in das Jar übernommen. Die gebauten Jars enthalten also fremdes Material und sind für den
eigenen Gebrauch gedacht; eine Veröffentlichung braucht die Zustimmung der jeweiligen Autoren.

## Weltgenerierung wie in 1.5.2

`mods/worldgen152` bildet den Overworld-Generator von Minecraft 1.5.2 (und den Nether, siehe unten) nach: Biom-Schichten, Geländeform,
Deckschichten, Grundgestein, Höhlen und Schluchten, und die Population der Chunks: Seen, Dungeons, Erze, Sand
und Ton, Bäume, Gras, Blumen, Pilze, Zuckerrohr, Kürbisse, Kakteen, Seerosen, Ranken, Quellen, Schnee und Eis.
Der Kern (`hexxit.worldgen152.core`) ist reines Java ohne Spielklassen und verbraucht seine Zufallszahlen in
der Reihenfolge des Originals, damit derselbe Seed dieselbe Welt ergibt. Die Welt ist wie damals 256 Blöcke
hoch (y 0–255), das Gelände reicht bis y 127, der Meeresspiegel liegt bei 63. Der Generator ist der Welttyp
„Hexxit Legacy (1.5.2)“ (`hexxit:legacy`); die Standard-Welt `minecraft:normal` ist Vanilla-Generierung, in die die
1.5.2-Generatoren einzeln als Features eingehängt werden (`OldGeneratorFeature`, Teil 26).

In 1.5.2 hing die Population davon ab, in welcher Reihenfolge Chunks geladen wurden. Hier ist die Reihenfolge
fest (wie beim Erzeugen des Startgebiets auf dem Original-Server), sodass das Ergebnis nur vom Seed abhängt.

Mods melden an, was sie damals beisteuerten:

| Was | Wo |
|---|---|
| Biome, Gelände-Blöcke | `OldBiomes`, `OldBlockStates` |
| Blöcke, die generiert werden | `OldBlockInfo` (Eigenschaften) und `OldBlockStates` (Blockzustand) |
| Weltgeneratoren (`IWorldGenerator`), Terrain-Events | `OldTerrainGen` |
| eigener Biom-Dekorator, Bäume, Gras | `OldBiome.decorator`, `.trees`, `.grass`, `.afterDecoration` |
| Dungeon-Beute, Dungeon-Mobs | `OldLoot`, `OldGameIds` |

Im Pack ist das bisher ExtrabiomesXL: Mit seinen 28 Biomen wählt die Biom-Schicht aus 35 statt 7 Land-Biomen,
Mountain Ridge und Wasteland bestehen aus Red Rock bzw. Cracked Sand, und jedes seiner Biome hat seine eigene
Dekoration (Redwoods, Firs, Akazien, Herbstbäume, Legend Oaks, Blumen, Gräser, Laubhaufen, Quicksand, Oasen).
Setzlinge wachsen im Spiel mit denselben 1.5.2-Generatoren (`OldLiveGen`).

Prüfung ohne Client:

```
tools/expect.sh 1234 find 260 > expect.txt     # Vorhersage aus dem Kern, ein Chunk je Biom
./gradlew runServer                            # mit level-seed=1234, level-type=hexxit\:legacy, RCON und pause-when-empty-seconds=-1 in run/server/server.properties
python3 tools/verify_blocks.py expect.txt      # muss 0 Abweichungen melden
```

Stand der Prüfung: Für Seed 1234 stimmen je ein Chunk aller 49 Biome (137.790 Blöcke) und 36 Chunks an Dörfern,
Festungen, Minenschächten und Tempeln (113.533 Blöcke, dazu Truhen, Spawner, Dorfbewohner, Truhenloren) mit dem
Kern überein. Strukturen dafür: `tools/expect.sh 1234 structures 220`.

Gegen das echte Original (`tools/compare_original.py`, liest die Region-Dateien einer Welt des 1.5.2-Servers):
Mit dem Vanilla-Server stimmen Spawnpunkt und 99,995 % von 67 Mio. Blöcken überein, darunter ein Dorf, eine
Festung und Minenschächte. Mit dem Hexxit-Server stimmen Spawnpunkt und alle Biome; bei den Blöcken fehlen noch
die Weltgeneratoren der nicht portierten Mods (Einzelheiten und erwartete Unterschiede: Teil 2 in `docs/PLAN.md`).

Enthalten sind auch die Strukturen von 1.5.2, die Spawnsuche und die Dekoration der ExtrabiomesXL-Biome. Mit
ihr stimmen für Seed 1234 je ein Chunk aller 49 Biome (150.400 Blöcke) und Chunks an Quicksand und einer Legend
Oak (30.158 Blöcke) mit dem Kern überein.

Mit Hexxit Gear, offLawn und Wild Caves 3 (Teil 5) stimmen für Seed 1234 je ein Chunk aller 49 Biome (157.943 Blöcke),
36 Chunks an Strukturen (119.058 Blöcke, 137 Truhen, Spawner, Schädel und Entitäten) und Chunks mit Sonnenblumen und
Hexbiscus (72.274 Blöcke) mit dem Kern überein.

Mit Natura (Teil 7) stimmen für Seed 1234 je ein Chunk aller 49 Biome (159.260 Blöcke) und 297 Chunks mit
Natura-Bäumen, -Büschen, -Feldfrüchten und Saguaros (903.929 Blöcke) mit dem Kern überein.

Better Dungeons (Teil 12) baut seine Vorlagen-Dungeons (Burgen, Schneeburgen, Schiffe) in dieselbe Welt; für Seed 1234 stimmen
je 2×2 Chunks der nächsten Burg, Schneeburg und des nächsten Schiffs (117.004 Blöcke, Kisten, Spawner, Bilder, Rahmen) mit dem Kern
überein (`tools/expect.sh 1234 dungeons 150`). Mit Teil 13 kommen alle Höhlen- und Dungeon-Typen, die Dschungel- und
Sumpfhöhlen je Chunk und die große Mauer bei z = −8000 dazu: 71 Chunks (706.445 Blöcke, 322 Spawner, Kisten und Entitäten)
stimmen mit dem Kern überein.

Ruins und Battle Towers (Teil 14) kommen ebenfalls aus dem Kern: Für Seed 1234 stimmen die Chunks von 40 Ruinen an
Land und 6 Türmen (822.881 Blöcke, 660 Spawner, Kisten und Entitäten), 40 Ozean-Ruinen (607.877 Blöcke) und 15
Nether-Ruinen (119.768 Blöcke) mit dem Kern überein (`tools/expect.sh 1234 ruins 40 -30 -40`, `towers 6`,
`nether ruins 15`).

Dimensional Doors (Teil 15) setzt seine Rift-Gateways und Rift-Cluster über den Kern (`tools/expect.sh 1234 rifts 5`:
53.676 Blöcke, 0 Abweichungen). Das Gelände von Limbo rechnet `dimdoors.gen.LimboTerrain` wie das Original (Rauschen
mit festem Seed 45); 12.800 Blöcke einer Stichprobe stimmen mit dem Server überein.

Der Nether (Teil 7b) ist der 1.5.2-Nether in der Variante von Natura („Override Nether“: Tainted Soil, Heat Sand,
Netherfestungen, Glowshrooms, Nether-Bäume). Gelände und Höhlen sind byte-gleich mit dem dekompilierten
Original-Code; für Seed 1234 stimmen 520 Nether-Chunks (579.244 Blöcke, eine ganze Festung) mit dem Kern überein.
Prüfen: `tools/expect.sh 1234 nether structures 60 > expect.txt`, dann `verify_blocks.py`.

## Wann ein Mod als portiert gilt

1. sauberer Build (`./gradlew clean pack`)
2. Testserver startet bis `Done` ohne Fehler des Mods im Log
3. Verhalten gegen das Original geprüft (per RCON am Server, soweit serverseitig prüfbar)
4. Sichtprüfung im Client (Texturen, Modelle, GUIs); das kann nur ein echter Client

## Status

47 Mods im Original. Zu portieren sind rund 348.000 Zeilen dekompilierter Referenzcode
(ohne Twilight Forest, Tinkers' Construct, NEI, NEI Plugins und CodeChickenCore).

| Mod | Archiv, Version | Autor (Original) | Zeilen | Status |
|---|---|---|---:|---|
| RottenFleshToLeather | `flesh2leather` 1.5.1 | thesbros | 38 | ✅ portiert, am Server verifiziert |
| Asgard Shield Addons (Nether-Ender / Hallowed / HarkenScythe) | `asgardshield-addons` 2.0.0 | Jade Knightblazer | 169 | ✅ Teil 9 (Client-Prüfung offen) |
| Chest Transporter | `chesttransporter` 1.1.6 | cubex2 | 488 | ✅ portiert, Server startet (Spieler-Aktionen und Client-Prüfung offen) |
| BuffBar | `buffbar` 0.7.1 | — | 571 | ✅ portiert (nur Client, Client-Prüfung offen) |
| ArmorBar | `armorbar` 0.7.1 | — | 683 | ✅ portiert (nur Client, Client-Prüfung offen) |
| Obsidian Pressure Plates | `obsidiplates` 1.5.0.13 | Myrathi | 752 | ✅ portiert, am Server verifiziert (Client-Prüfung offen) |
| Back Tools | `backtools` 1.5.2.v01 | — | 956 | ✅ portiert (nur Client, Client-Prüfung offen) |
| Soul Shards | `soulshards` 1.36 | ShadwDrgn | 1.503 | ✅ portiert, am Server verifiziert (Spieler-Aktionen und Client-Prüfung offen) |
| Hexxit Gear | `hexxitgear` 1.0.23 | sct | 1.536 | ✅ portiert, am Server verifiziert (Set-Boni mit Spieler und Client-Prüfung offen) |
| Battle Towers | `battletowers` 1.5.2 | AtomicStryker | 1.842 | ✅ Teil 14 (Client-Prüfung offen) |
| offLawn | `offlawn` 1.5.1.r02 | Rhodox, Slinky | 1.850 | ✅ portiert, am Server verifiziert (Spieler-Aktionen und Client-Prüfung offen) |
| Wild Caves 3 | `wildcaves3` 0.4.2 | Alexmania | 2.094 | ✅ portiert, am Server verifiziert (Client-Prüfung offen) |
| Backpacks | `backpacks` 1.12.13 | Eydamos | 2.331 | ✅ Teil 17 (Rucksäcke in 16 Farben und groß, Ender-/Werkbank-Rucksack, Taste B, Färben/Vergrößern mit JEI; Client-Prüfung offen) |
| Asgard Shield | `asgardshield` 2.0.4 | Jade Knightblazer | 2.842 | ✅ Teil 9 (Schilde, Riesenschwerter, Guard-Mechanik, Farb-API; Client-Prüfung offen) |
| Ruins | `ruins` 1.5.2 | d00dv4d3r, kolt666, AtomicStryker | 2.951 | ✅ Teil 14 (Client-Prüfung offen) |
| bspkrsCore | `bspkrscore` 2.09 | DaftPVF, bspkrs | 3.066 | ✅ portiert, soweit TreeCapitator es braucht (Block-Angaben) |
| Enchanting Plus | `enchantingplus` 1.15.4 | xkyouchoux, odininon, GnR Slash | 3.342 | ✅ portiert, Server startet (GUI nur im Client prüfbar) |
| More Bows | `morebows` 1.5.2.2 | iDiamondhunter | 4.156 | ✅ portiert, Server startet (Schießen und Client-Prüfung offen) |
| TreeCapitator | `treecapitator` 1.5.2.r14 | DaftPVF, bspkrs, Lunatrius | 4.498 | ✅ portiert, am Server verifiziert (Vanilla- und ExtrabiomesXL-Bäume) |
| Custom LAN Ports | `customport` 1.5.2 | CptHunter | 4.521 | ✅ portiert (nur integrierter Server; Client-Prüfung offen) |
| qCraft | `qcraft` 1.0.1 | Daniel Ratcliffe, TeacherGaming | 5.221 | ✅ Teil 17 (qBlocks, Quantencomputer, Erz/Staub, Essenzen, Brille; Weltgenerierung wie im Pack aus; Client-Prüfung offen) |
| MapWriter | `mapwriter` 2.0.5 | — | 5.356 | ✅ Teil 22 (Minikarte, Vollbildkarte mit Markern und Gruppen, Spur, Kartenbilder je Welt; im Client geprüft) |
| Secret Rooms | `secretrooms` 4.6.0.283 | AbrarSyed | 5.692 | ✅ Teil 20 (Tarnblöcke mit dem Modell des Nachbarn, Geheimtüren und -falltür, Truhen, Redstone, Platten, Einwegglas, feste Luft, Fackelhebel; Client-Prüfung offen) |
| Inventory Tweaks | `inventorytweaks` 1.54b | Jimeo Wan, Kobata | 6.626 | ✅ portiert (reiner Client-Mod; Client-Prüfung offen) |
| Damage Indicators | `damageindicators` 2.7.0.1 | rich1051414 | 6.783 | ✅ Teil 22 (Portrait mit Lebensbalken, Effekten und Mob-Vorschau, Schadenszahlen; im Client geprüft) |
| Falling Meteors | `meteorsmod` 2.10.2a | AlexDGr8r | 6.957 | ✅ Teil 19 (Meteoriten mit Kratern, Schild, Timer, Detektoren, Beschwörer, Werkzeuge, Rüstungen, Alien Creeper, Kometenkatze, Meteoritenerz in der Welt; Client-Prüfung offen) |
| Infernal Mobs | `infernalmobs` 1.5.2 | AtomicStryker | 7.089 | ✅ Teil 18 (Rare/Ultra/Infernal mit 26 Modifikatoren, Leben, Beute, HUD; Client-Prüfung offen) |
| BetterStorage | `betterstorage` 0.6.2.23.h2 | copygirl, copyboy | 9.015 | ✅ Teil 17 (Kisten-Stapel, verstärkte Truhen mit Schloss/Schlüssel, Spind, Rüstungsständer, Rucksäcke, Frienderman; Client-Prüfung offen) |
| ChocoCraft | `chococraft` 2.8.8 | Torojima | 9.331 | ✅ Teil 18 (Chocobos in neun Farben, Zucht, Reiten, Taschen, Chocopedia, Gysahl-Pflanzen in der Welt; Client-Prüfung offen) |
| Dimensional Doors | `dimensional-doors` 1.4.1.194 | StevenRS11, SenseiKiwi | 10.746 | ✅ Teil 15 (Taschen, Dungeons, Limbo, Rifts; Client-Prüfung offen) |
| ExtrabiomesXL | `ebxl` 3.13.4 | ScottKillen u. a. | 10.762 | ✅ portiert, am Server verifiziert, in Integrated über TerraBlender (Teil 26d; Client-Prüfung offen) |
| (Vanilla 1.5.2) Weltgenerierung | `worldgen152` | Mojang | – | Gelände, Dekoration, Strukturen und Spawnpunkt ✅ gegen Spiel und gegen Original-Welten verifiziert; Nether ✅ (Teil 7b) |
| LegendGear | `legendgear` 1.5.2 | NMcCoy | 11.590 | ✅ Teil 19 (Medaillons, Amulette, Bumerang, Enterhaken, Bomben, Köcher, Zauberspiegel, Sternschnuppen, Sockel, Sternenstrahl-Fackeln, mystische Sträucher und Bombenblumen in der Welt; Client-Prüfung offen) |
| NEI Plugins | `nei-plugins` 1.0.9.3 | mistaqur | 11.790 | entfällt, stattdessen JEI |
| Xeno's Reliquary | `xreliquary` 1.0.6d | x3n0ph0b3 | 11.868 | ✅ Teil 20 (Pistole mit neun Kugeln, Seren und Elixiere, Alkahestrie mit JEI-Erklärung, Relikte, Altar des Lichts; Client-Prüfung offen) |
| CoFHCore | `cofhcore` 1.5.2.5 | KingLemmingCoFH, Cynycal, Zeldo | 12.302 | ✅ Teil 8 (Erz-Ersatz, flaches Grundgestein, Köpfe, `/cofh tps`) |
| Immibis Core | `immibiscore` 55.1.6 | immibis | 12.512 | offen |
| Immibis's Microblocks | `immibismicroblocks` 55.0.7 | immibis | 13.474 | offen |
| Artifice | `artifice` 1.1.2.182 | Shukaro | 14.781 | ✅ Teil 8 |
| CodeChickenCore | `codechickencore` 0.8.7.3 | ChickenBones | 14.969 | entfällt (Bibliothek von NEI) |
| Not Enough Items | `nei` 1.5.2.28 | ChickenBones | 16.239 | entfällt, stattdessen JEI |
| Natura | `natura` 2.1.5.1 | mDiyo | 20.430 | ✅ Teil 7, Nether ✅ Teil 7b |
| BiblioCraft | `bibliocraft` 1.3.3 | Nuchaz | 20.970 | ✅ Teil 16 (13 Blockarten in 79 Varianten, Lesebrille, Druckerei; Feinlage im Client offen) |
| Harken Scythe | `harkenscythe` 2.1.6 | Jade Knightblazer | 27.284 | ✅ Teil 9 (Seelen/Blut, Altäre, 26 Mobs; Client-Prüfung offen) |
| Better Dungeons (Chocolate Quest) | `betterdungeons` 1.09b | Chocolatin | 36.118 | ✅ Teil 12 und 13 (Client-Prüfung offen) |
| Project Zulu | `projectzulu` 1.0.3.8 | — | 42.845 | ✅ Teil 10 Kreaturen, Teil 11 Blöcke, Items, Tränke, Strukturen (Client-Prüfung offen) |
| The Twilight Forest | `twilightforest` 1.18.1 | Benimatic | 50.329 | Teil 24a–24f erledigt (Dimension, Gelände, Portal, Blöcke, Bäume, Dekoration, Mobs, Strukturen A und B mit Turm-Geräten, Bosse, Ausrüstung, Karten, Entwerkbank, Fortschritte) |
| Tinkers' Construct | `tconstruct` 1.4.3 | mDiyo | 59.808 | aufgeteilt in Teil 25a–25f; ✅ 25a (Werkzeug-Kern: Materialien, Teile, Muster, Tische, Zusammenbau, Modifikatoren anbringen; Client-Prüfung offen), ✅ 25b (Fähigkeiten, Modifikator-Effekte, Bogen/Pfeile, Dolch, Bratpfanne, Werkzeugmodelle in Schichten; Client-Prüfung offen), ✅ 25c (Schmelzerei, 24 Flüssigkeiten, Gießen, Legierungen, Trockengestell, Erze/Metallblöcke; Client-Prüfung offen), ✅ 25d (Erze, Erzbeeren, Schleiminseln, blauer Schleim, Werkzeugschmiede/Schmelzerei im Dorf, Tüftler; Client-Prüfung offen), ✅ 25e (Rüstungs-Inventar mit Tabs, Rucksack, Herzkanister, Handbücher, Essen, Essenzkristall, Schleim-Eier, Spieler-Regeln, Mob-Drops; Client-Prüfung offen), ✅ 25f (klares/gefärbtes Glas, Ziegel, Brownstone, Wollstufen, Steinfackel, Punji, Barrikaden, Landmine, Zugbrücke, Feueranzünder, Essenz-Extraktor, Natura-Saguaro, JEI-Meißel; Client-Prüfung offen) |
| Tinkers' Construct 3 (nur Hexxit Integrated) + Mantle | `tconstruct3` 3.12.1.231, `mantle` 1.11.117 (1.20.1, MIT) | SlimeKnights | 244.000 | 27a erledigt (Gerüst, Konverter, Welttyp-Schalter), 27b–27k offen |

Weitere Archive des Packs, die kein eigener Mod sind:

| Archiv | Bedeutung für den Port |
|---|---|
| `basemods` | Forge 7.8.1.737 + gepatchtes Minecraft 1.5.2. Wird durch NeoForge 26.3 ersetzt; die Weltgenerierung von 1.5.2 lebt in `mods/worldgen152` weiter |
| `betterdungeons-data1` | Dungeon-Vorlagen und Loot-Konfiguration für Better Dungeons. Wird mit dem Mod konvertiert |
| `betterdungeons-data2` | weitere Dungeon-Vorlagen für Better Dungeons. Wird mit dem Mod konvertiert |
| `lumyskinpatch` | Skin-Server-Patch für alte Versionen. In 26.3 gegenstandslos, kein Port |
| `ruins-data1` | Ruinen-Vorlagen (785 Dateien). Kommen beim Bauen unverändert ins Ruins-Jar |
| `ruins-data2` | Ruinen-Vorlagen (491 Dateien). Kommen beim Bauen unverändert ins Ruins-Jar |
| `ruins-data3` | Ruinen-Vorlagen (557 Dateien). Kommen beim Bauen unverändert ins Ruins-Jar |
| `z-hexxit-configs` | Konfigurationen des Packs (Block-IDs, Balancing, Biom-Auswahl). Die Werte werden pro Mod als Standard übernommen |
