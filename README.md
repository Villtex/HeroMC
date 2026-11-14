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
| `mods/<name>/original-assets.txt` | welche Texturen/Sounds der Mod beim Bauen aus dem Original-Archiv übernimmt |
| `buildSrc/` | Gradle-Task, der diese Dateien lädt (MD5-geprüft) und in das gebaute Jar legt |
| `pack/hexxit-1.0.11.json` | exaktes Manifest des Originalpacks: Mods, Versionen, Prüfsummen, Download-URLs |
| `tools/reference.py` | lädt das Original und erzeugt lesbare Referenzquellen (lokal, wird nicht eingecheckt) |
| `tools/biomes.py` | erzeugt die Biom-Daten (Klima, Farben aus den 1.5.2-Farbkarten, Mob-Spawns) |
| `tools/rcon.py` | kleiner RCON-Client für Verhaltenstests am laufenden Testserver |
| `gradle/neoforge.mods.toml` | gemeinsame Vorlage für die Mod-Metadaten |

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

`mods/worldgen152` bildet den Overworld-Generator von Minecraft 1.5.2 nach: Biom-Schichten, Geländeform,
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

Im Pack war das bisher ExtrabiomesXL: Mit seinen 28 Biomen wählt die Biom-Schicht aus 35 statt 7 Land-Biomen,
und Mountain Ridge und Wasteland bestehen aus Red Rock bzw. Cracked Sand.

Prüfung ohne Client:

```
tools/expect.sh 1234 find 260 > expect.txt     # Vorhersage aus dem Kern, ein Chunk je Biom
./gradlew runServer                            # mit level-seed=1234 und RCON in run/server/server.properties
python3 tools/verify_blocks.py expect.txt      # muss 0 Abweichungen melden
```

Stand der Prüfung: Für Seed 1234 stimmen je ein Chunk aller 49 Biome mit dem Kern überein (132.102 Blöcke
einschließlich aller von der Population geänderten, 784 Biom-Proben, Spawner und Truhen). Der Abgleich des
Kerns mit einer Welt des Original-Packs steht noch aus.

Noch nicht enthalten: Strukturen, die 1.5.2-Spawnsuche und die eigene Dekoration der ExtrabiomesXL-Biome.

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
| Asgard Shield Addons (Nether-Ender / Hallowed / HarkenScythe) | `asgardshield-addons` 2.0.0 | Jade Knightblazer | 169 | offen |
| Chest Transporter | `chesttransporter` 1.1.6 | cubex2 | 488 | offen |
| BuffBar | `buffbar` 0.7.1 | — | 571 | offen |
| ArmorBar | `armorbar` 0.7.1 | — | 683 | offen |
| Obsidian Pressure Plates | `obsidiplates` 1.5.0.13 | Myrathi | 752 | offen |
| Back Tools | `backtools` 1.5.2.v01 | — | 956 | offen |
| Soul Shards | `soulshards` 1.36 | ShadwDrgn | 1.503 | offen |
| Hexxit Gear | `hexxitgear` 1.0.23 | sct | 1.536 | offen |
| Battle Towers | `battletowers` 1.5.2 | AtomicStryker | 1.842 | offen |
| offLawn | `offlawn` 1.5.1.r02 | Rhodox, Slinky | 1.850 | offen |
| Wild Caves 3 | `wildcaves3` 0.4.2 | Alexmania | 2.094 | offen |
| Backpacks | `backpacks` 1.12.13 | Eydamos | 2.331 | offen |
| Asgard Shield | `asgardshield` 2.0.4 | Jade Knightblazer | 2.842 | offen |
| Ruins | `ruins` 1.5.2 | d00dv4d3r, kolt666, AtomicStryker | 2.951 | offen |
| bspkrsCore | `bspkrscore` 2.09 | DaftPVF, bspkrs | 3.066 | offen |
| Enchanting Plus | `enchantingplus` 1.15.4 | xkyouchoux, odininon, GnR Slash | 3.342 | offen |
| More Bows | `morebows` 1.5.2.2 | iDiamondhunter | 4.156 | offen |
| TreeCapitator | `treecapitator` 1.5.2.r14 | DaftPVF, bspkrs, Lunatrius | 4.498 | offen |
| Custom LAN Ports | `customport` 1.5.2 | CptHunter | 4.521 | offen |
| qCraft | `qcraft` 1.0.1 | Daniel Ratcliffe, TeacherGaming | 5.221 | offen |
| MapWriter | `mapwriter` 2.0.5 | — | 5.356 | offen |
| Secret Rooms | `secretrooms` 4.6.0.283 | AbrarSyed | 5.692 | offen |
| Inventory Tweaks | `inventorytweaks` 1.54b | Jimeo Wan, Kobata | 6.626 | offen |
| Damage Indicators | `damageindicators` 2.7.0.1 | rich1051414 | 6.783 | offen |
| Falling Meteors | `meteorsmod` 2.10.2a | AlexDGr8r | 6.957 | offen |
| Infernal Mobs | `infernalmobs` 1.5.2 | AtomicStryker | 7.089 | offen |
| BetterStorage | `betterstorage` 0.6.2.23.h2 | copygirl, copyboy | 9.015 | offen |
| ChocoCraft | `chococraft` 2.8.8 | Torojima | 9.331 | offen |
| Dimensional Doors | `dimensional-doors` 1.4.1.194 | StevenRS11, SenseiKiwi | 10.746 | offen |
| ExtrabiomesXL | `ebxl` 3.13.4 | ScottKillen u. a. | 10.762 | in Arbeit: 28 Biome, Red Rock, Cracked Sand samt Rezepten |
| (Vanilla 1.5.2) Weltgenerierung | `worldgen152` | Mojang | – | Gelände und Dekoration ✅ verifiziert; Strukturen offen (Teil 2) |
| LegendGear | `legendgear` 1.5.2 | NMcCoy | 11.590 | offen |
| NEI Plugins | `nei-plugins` 1.0.9.3 | mistaqur | 11.790 | entfällt, stattdessen JEI |
| Xeno's Reliquary | `xreliquary` 1.0.6d | x3n0ph0b3 | 11.868 | offen |
| CoFHCore | `cofhcore` 1.5.2.5 | KingLemmingCoFH, Cynycal, Zeldo | 12.302 | offen |
| Immibis Core | `immibiscore` 55.1.6 | immibis | 12.512 | offen |
| Immibis's Microblocks | `immibismicroblocks` 55.0.7 | immibis | 13.474 | offen |
| Artifice | `artifice` 1.1.2.182 | Shukaro | 14.781 | offen |
| CodeChickenCore | `codechickencore` 0.8.7.3 | ChickenBones | 14.969 | entfällt (Bibliothek von NEI) |
| Not Enough Items | `nei` 1.5.2.28 | ChickenBones | 16.239 | entfällt, stattdessen JEI |
| Natura | `natura` 2.1.5.1 | mDiyo | 20.430 | offen |
| BiblioCraft | `bibliocraft` 1.3.3 | Nuchaz | 20.970 | offen |
| Harken Scythe | `harkenscythe` 2.1.6 | Jade Knightblazer | 27.284 | offen |
| Better Dungeons (Chocolate Quest) | `betterdungeons` 1.09b | Chocolatin | 36.118 | offen |
| Project Zulu | `projectzulu` 1.0.3.8 | — | 42.845 | offen |
| The Twilight Forest | `twilightforest` 1.18.1 | Benimatic | 50.329 | zurückgestellt |
| Tinkers' Construct | `tconstruct` 1.4.3 | mDiyo | 59.808 | zurückgestellt |

Weitere Archive des Packs, die kein eigener Mod sind:

| Archiv | Bedeutung für den Port |
|---|---|
| `basemods` | Forge 7.8.1.737 + gepatchtes Minecraft 1.5.2. Wird durch NeoForge 26.3 ersetzt; die Weltgenerierung von 1.5.2 lebt in `mods/worldgen152` weiter |
| `betterdungeons-data1` | Dungeon-Vorlagen und Loot-Konfiguration für Better Dungeons. Wird mit dem Mod konvertiert |
| `betterdungeons-data2` | weitere Dungeon-Vorlagen für Better Dungeons. Wird mit dem Mod konvertiert |
| `lumyskinpatch` | Skin-Server-Patch für alte Versionen. In 26.3 gegenstandslos, kein Port |
| `ruins-data1` | Ruinen-Vorlagen (785 Dateien). Werden mit Ruins konvertiert |
| `ruins-data2` | Ruinen-Vorlagen (491 Dateien). Werden mit Ruins konvertiert |
| `ruins-data3` | Ruinen-Vorlagen (557 Dateien). Werden mit Ruins konvertiert |
| `z-hexxit-configs` | Konfigurationen des Packs (Block-IDs, Balancing, Biom-Auswahl). Die Werte werden pro Mod als Standard übernommen |
