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
- **Rezeptanzeige:** Not Enough Items wird nicht portiert. Stattdessen sind alle portierten Mods mit
  **JEI** kompatibel; eigene Rezepttypen bekommen ein JEI-Plugin.
- **Twilight Forest** und **Tinkers' Construct** sind vorerst zurückgestellt.

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
| `tools/rcon.py` | kleiner RCON-Client für Verhaltenstests am laufenden Testserver |
| `tools/modelbase_to_layer.py` | überträgt die Geometrie eines 1.5.2-`ModelBase` (Boxen, Hierarchie, Texturversätze) in 26.3-`createBodyLayer`-Zeilen |
| `gradle/neoforge.mods.toml` | gemeinsame Vorlage für die Mod-Metadaten |
| `docs/JEI.md` | Vorlage für das JEI-Plugin eines Mods (JEI ersetzt NEI) |
| `.claude/skills/worldgen152/` | Projekt-Skill: Weltgenerierung eines Mods in `worldgen152` einhängen, Setzlinge, Nether, Prüfung |
| `.claude/skills/client-render-263/` | Projekt-Skill: HUD-Ebenen, Gegenstände am Spielermodell, Rüstungen mit eigenen Helm-Modellen, durchscheinende Blockmodelle, Rechtsklick vor dem Block abfangen (26.3) |
| `.claude/skills/menu-gui-263/` | Projekt-Skill: eigene GUI mit Container (Menü, Screen, Pakete Client → Server, Server-Konfiguration für die Vorschau im Client) |
| `.claude/skills/wood-sets-263/` | Projekt-Skill: Holzarten und Pflanzen eines Mods (Planken, Stämme, Blätter, Setzlinge, Treppen bis Werkbänke, Brennwert, Rezeptkonflikte mit Vanilla), Vorlage Natura |
| `.claude/skills/mobs-263/` | Projekt-Skill: Mobs, Spawn-Eier, Boote, Mob-Drops und Spawns (Biome-Modifier), viele Mobs auf einmal (Modell-Konverter, Easing, Fliegen, Reiten, Mehrteiler, Boss), Prüfen ohne Spieler; Vorlagen Natura, Project Zulu |
| `.claude/skills/server-tests-263/` | Projekt-Skill: Verhalten ohne Spieler am Testserver prüfen (Rezepte über Crafter + RCON, temporärer FakePlayer-Befehl, forceloaded Chunks) |
| `.claude/skills/treecapitator-trees/` | Projekt-Skill: Bäume und Äxte eines Mods bei TreeCapitator anmelden (Daten-Registry `treecapitator:tree`, Item-Tags) |
| `.claude/skills/templates-152/` | Projekt-Skill: Gebäude-Vorlagen und Schematics eines 1.5.2-Mods übernehmen (Daten-Archive beim Bauen ins Jar, NBT ohne Spielklassen, Kisten/Schilder/Bilder/Mobs über worldgen152, Text-Vorlagen ohne Umwandlung, Prüfung), Vorlagen Better Dungeons, Ruins |
| `.claude/skills/items-weapons-263/` | Projekt-Skill: Waffen, Stäbe, Bögen, Geschosse und Spezial-Items (Schwert-Werte, Blocken, Geschoss-Entity je Typ, magische Pfeile/Köcher, Taschen, Damage-Varianten mit alten IDs, Kapseln, Einmal-Spawner), Vorlage Better Dungeons |
| `.claude/skills/headless-client/` | Projekt-Skill: Testclient im Cloud-Container ohne Bildschirm starten (Xvfb, `SDL_VIDEO_FORCE_EGL=1`, Screenshots, xdotool) |

## Bauen und Starten

Voraussetzung ist nur ein JDK; das passende JDK 25 lädt Gradle bei Bedarf selbst.

```
./gradlew pack         # baut alle Mods nach build/pack/mods
./gradlew runServer    # Testserver mit allen portierten Mods (run/server)
./gradlew runClient    # Testclient mit allen portierten Mods (run/client)
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
hoch (y 0–255), das Gelände reicht bis y 127, der Meeresspiegel liegt bei 63. Der Generator ersetzt die
Standard-Welt (`minecraft:normal`).

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
./gradlew runServer                            # mit level-seed=1234, RCON und pause-when-empty-seconds=-1 in run/server/server.properties
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
| Backpacks | `backpacks` 1.12.13 | Eydamos | 2.331 | offen |
| Asgard Shield | `asgardshield` 2.0.4 | Jade Knightblazer | 2.842 | ✅ Teil 9 (Schilde, Riesenschwerter, Guard-Mechanik, Farb-API; Client-Prüfung offen) |
| Ruins | `ruins` 1.5.2 | d00dv4d3r, kolt666, AtomicStryker | 2.951 | ✅ Teil 14 (Client-Prüfung offen) |
| bspkrsCore | `bspkrscore` 2.09 | DaftPVF, bspkrs | 3.066 | ✅ portiert, soweit TreeCapitator es braucht (Block-Angaben) |
| Enchanting Plus | `enchantingplus` 1.15.4 | xkyouchoux, odininon, GnR Slash | 3.342 | ✅ portiert, Server startet (GUI nur im Client prüfbar) |
| More Bows | `morebows` 1.5.2.2 | iDiamondhunter | 4.156 | ✅ portiert, Server startet (Schießen und Client-Prüfung offen) |
| TreeCapitator | `treecapitator` 1.5.2.r14 | DaftPVF, bspkrs, Lunatrius | 4.498 | ✅ portiert, am Server verifiziert (Vanilla- und ExtrabiomesXL-Bäume) |
| Custom LAN Ports | `customport` 1.5.2 | CptHunter | 4.521 | ✅ portiert (nur integrierter Server; Client-Prüfung offen) |
| qCraft | `qcraft` 1.0.1 | Daniel Ratcliffe, TeacherGaming | 5.221 | offen |
| MapWriter | `mapwriter` 2.0.5 | — | 5.356 | offen |
| Secret Rooms | `secretrooms` 4.6.0.283 | AbrarSyed | 5.692 | offen |
| Inventory Tweaks | `inventorytweaks` 1.54b | Jimeo Wan, Kobata | 6.626 | ✅ portiert (reiner Client-Mod; Client-Prüfung offen) |
| Damage Indicators | `damageindicators` 2.7.0.1 | rich1051414 | 6.783 | offen |
| Falling Meteors | `meteorsmod` 2.10.2a | AlexDGr8r | 6.957 | offen |
| Infernal Mobs | `infernalmobs` 1.5.2 | AtomicStryker | 7.089 | offen |
| BetterStorage | `betterstorage` 0.6.2.23.h2 | copygirl, copyboy | 9.015 | offen |
| ChocoCraft | `chococraft` 2.8.8 | Torojima | 9.331 | offen |
| Dimensional Doors | `dimensional-doors` 1.4.1.194 | StevenRS11, SenseiKiwi | 10.746 | offen |
| ExtrabiomesXL | `ebxl` 3.13.4 | ScottKillen u. a. | 10.762 | ✅ portiert, am Server verifiziert (Client-Prüfung offen) |
| (Vanilla 1.5.2) Weltgenerierung | `worldgen152` | Mojang | – | Gelände, Dekoration, Strukturen und Spawnpunkt ✅ gegen Spiel und gegen Original-Welten verifiziert; Nether ✅ (Teil 7b) |
| LegendGear | `legendgear` 1.5.2 | NMcCoy | 11.590 | offen |
| NEI Plugins | `nei-plugins` 1.0.9.3 | mistaqur | 11.790 | entfällt, stattdessen JEI |
| Xeno's Reliquary | `xreliquary` 1.0.6d | x3n0ph0b3 | 11.868 | offen |
| CoFHCore | `cofhcore` 1.5.2.5 | KingLemmingCoFH, Cynycal, Zeldo | 12.302 | ✅ Teil 8 (Erz-Ersatz, flaches Grundgestein, Köpfe, `/cofh tps`) |
| Immibis Core | `immibiscore` 55.1.6 | immibis | 12.512 | offen |
| Immibis's Microblocks | `immibismicroblocks` 55.0.7 | immibis | 13.474 | offen |
| Artifice | `artifice` 1.1.2.182 | Shukaro | 14.781 | ✅ Teil 8 |
| CodeChickenCore | `codechickencore` 0.8.7.3 | ChickenBones | 14.969 | entfällt (Bibliothek von NEI) |
| Not Enough Items | `nei` 1.5.2.28 | ChickenBones | 16.239 | entfällt, stattdessen JEI |
| Natura | `natura` 2.1.5.1 | mDiyo | 20.430 | ✅ Teil 7, Nether ✅ Teil 7b |
| BiblioCraft | `bibliocraft` 1.3.3 | Nuchaz | 20.970 | offen |
| Harken Scythe | `harkenscythe` 2.1.6 | Jade Knightblazer | 27.284 | ✅ Teil 9 (Seelen/Blut, Altäre, 26 Mobs; Client-Prüfung offen) |
| Better Dungeons (Chocolate Quest) | `betterdungeons` 1.09b | Chocolatin | 36.118 | ✅ Teil 12 und 13 (Client-Prüfung offen) |
| Project Zulu | `projectzulu` 1.0.3.8 | — | 42.845 | Kreaturen ✅ Teil 10 (40 Mobs, Spawns, Drops; Client-Prüfung offen), Blöcke/Welt offen (Teil 11) |
| The Twilight Forest | `twilightforest` 1.18.1 | Benimatic | 50.329 | zurückgestellt |
| Tinkers' Construct | `tconstruct` 1.4.3 | mDiyo | 59.808 | zurückgestellt |

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
