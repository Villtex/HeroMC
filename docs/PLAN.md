# Arbeitsplan: Welttyp „Hexxit Integrated“ (Branch `integrated`)

Ziel: Bei der Welterstellung unter „Welttyp“ zwei Varianten.

- **Hexxit Legacy**: die bitgenaue 1.5.2-Generierung aus `worldgen152`, wie auf dem Hauptbranch. Bleibt unverändert.
- **Hexxit Integrated**: die komplette Vanilla-Generierung von 26.3 (Gelände, Höhlen, Biome, Bauwerke, Mobs) plus alle Hexxit-Inhalte: Erze, Pflanzen, Bäume, Bauwerke, Dungeons, Biome und Mob-Spawns der Mods.

Items, Blöcke, Mobs, GUIs und Rezepte der Mods hängen nicht an der Generierung und gelten in beiden Varianten.

Der Port der Mods selbst (Teile 0–25) läuft auf dem Hauptbranch `ccr-83d04117-2w49l8` weiter, sein Plan liegt hier als `docs/PLAN-legacy.md` (nur Referenz, Status dort nicht pflegen). Jeder Teil hier ist so geschnitten, dass er in **eine Session** passt. Start mit „starte Teil I<N>“.

## Regeln für jeden Teil

1. **Vorbereitung**
   - Nutzer an das empfohlene Modell und den Aufwand erinnern (Übersicht). Faustregel wie in `PLAN-legacy.md`: Opus 5.5 Standard, Fable 5.2 für knifflige Generierung und festgefahrene Fehler, Sonnet 5.5 für Mechanisches.
   - Hauptbranch einmischen: `git fetch origin && git merge origin/ccr-83d04117-2w49l8` (Konflikte vor allem in `README.md`, `graphify-out/`). Danach Abschnitt des Teils lesen, Voraussetzungen prüfen.
   - Original-Quellen bei Bedarf: `python3 tools/reference.py --only <archiv>`.
2. **Umsetzung**
   - Mod-Generatoren nicht neu schreiben: Sie laufen weiter als `OldGen`/Populator auf einem `OldWorld`; die Brücke aus Teil I1 übersetzt zum echten 26.3-Chunk.
   - Code für Integrated liegt in `mods/worldgen152` unter `hexxit.worldgen152.integrated` und je Mod in einer kleinen Klasse `<Mod>Integrated` neben der Legacy-Anbindung. Legacy-Code nur ändern, wenn nötig und ohne Verhaltensänderung.
   - Keine Bitgenauigkeit. Ziel ist eine stimmige 26.3-Welt: Bauwerke auf der echten Oberfläche, Erze in sinnvollen 26.3-Höhen, Mengen ungefähr wie in 1.5.2 pro Fläche.
3. **Prüfen**
   - `CI=true ./gradlew pack` grün.
   - **Legacy unverändert:** `tools/expect.sh` + `tools/verify_blocks.py` wie auf dem Hauptbranch, 0 Abweichungen.
   - Integrated: Testserver mit `level-type=hexxit:integrated`, Seed 1234; Stichproben per RCON (`/locate`, Blöcke zählen in Bereichen, Skill `server-tests-263`). Bei Bedarf Bilder mit dem Skill `headless-client`.
4. **Abschluss**
   - Status hier in der Übersicht und unter „Stand“ beim Teil eintragen, `README.md` kurz ergänzen.
   - Skill-Check nach `CLAUDE.md`. Absehbar lohnt ein Skill `integrated-gen` (Mod-Generator in Integrated einhängen) nach Teil I2.
   - `graphify update .`, committen, `git push origin HEAD:integrated`.

## Übersicht

| Teil | Inhalt | Braucht | Modell, Aufwand | Status |
|---:|---|---|---|---|
| I1 | Welttypen-Auswahl und Chunk-Brücke | – | Fable 5.2, hoch | offen |
| I2 | Erze, Pflanzen, Bäume, Höhlendeko | I1 | Opus 5.5, hoch | offen |
| I3 | Bauwerke und Dungeons A | I1 | Opus 5.5, hoch | offen |
| I4 | Bauwerke und Dungeons B, Dimensions-Eingänge | I3 | Opus 5.5, hoch | offen |
| I5 | ExtrabiomesXL-Biome in der 26.3-Biomverteilung | I1 | Fable 5.2, hoch | offen |
| I6 | Mob-Spawns, Dungeon-Beute, Feinschliff | I2–I5 | Opus 5.5, mittel | offen |
| I7 | Gesamtprüfung beider Welttypen | alle | Opus 5.5, mittel | offen |

Mods mit eigener Overworld-Generierung (Stand Hauptbranch, Teil 25): artifice, battletowers, betterdungeons, chococraft, cofhcore, dimensional-doors, ebxl, hexxitgear, legendgear, meteorsmod, natura, offlawn, projectzulu, qcraft, ruins, tconstruct, twilightforest, wildcaves3. Kommen auf dem Hauptbranch neue dazu (Teil 21, 24f), in I2–I4 nachziehen.

## Teil I1 – Welttypen-Auswahl und Chunk-Brücke

- **Welttypen:** Heute ersetzt `worldgen152` die Vorlage `data/minecraft/worldgen/world_preset/normal.json`. Neu: `hexxit:legacy` (Inhalt des heutigen `normal.json`) und `hexxit:integrated` (Vanilla-Overworld-Noise mit eigener Biomquelle, zunächst die Vanilla-Multi-Noise-Liste). `normal` wieder Vanilla lassen oder auf eine der beiden lenken; welcher Typ beim Erstellen vorausgewählt ist, den Nutzer fragen (Tag `minecraft:normal` in `tags/worldgen/world_preset`, Sprachschlüssel für die Namen). Nether und End: Legacy behält `OldNetherChunkGenerator`, Integrated nimmt Vanilla plus Mod-Inhalte.
- **Server:** `level-type` in `server.properties` dokumentieren (README).
- **Erkennen, welcher Typ läuft:** Generator-Klasse prüfen (`OldChunkGenerator` = Legacy). Vorhandene Ereignis-Hooks der Mods (z. B. Setzlinge über `OldLiveGen`) müssen in beiden Typen funktionieren.
- **Brücke:** Eigenes Feature `hexxit:old_populate` (ein `PlacedFeature` je Generationsschritt, über Biom-Modifier in alle Overworld-Biome). Es nimmt den 1.5.2-Ablauf „Chunk an (x+8, z+8) bevölkern“: einen Ausschnitt um den Chunk in ein `OldWorld` kopieren (Ausbau von `OldLiveGen`: Blockzuordnung heute → alte IDs, unbekannte Blöcke als Stein bzw. „nicht anfassen“), registrierte Populatoren laufen lassen, geänderte Blöcke, Tile-Daten (`OldTile`) und Entities (`OldEntity`) über `OldBlockStates`/`OldModData` zurückschreiben. Nur Blöcke im erlaubten Bereich des `WorldGenRegion` schreiben.
- **Höhenfenster:** `OldWorld` kennt 0–127. Je Generator eine Abbildung angeben: „Oberfläche“ (Ausschnitt so verschieben, dass die echte Oberfläche bei alter Höhe ~64 liegt), „fest“ (alte Höhe = neue Höhe) oder „Tiefe gestreckt“ (für Erze, alte 0–64 auf -64–64). Schnittstelle so bauen, dass I2–I4 sie nur noch auswählen.
- **Biome:** Tabelle 26.3-Biom → 1.5.2-Biom für `OldWorld.getBiome` (z. B. `badlands` → Desert, `cherry_grove` → Forest, `deep_dark` → was darüber liegt). Als JSON-Datei oder Tag-gestützt, damit EBXL in I5 dazukommt.
- **Prüfen:** Ein einfacher Generator (Natura-Wolken oder Bäume) erscheint in einer Integrated-Welt; Legacy-`expect.sh` grün.

## Teil I2 – Erze, Pflanzen, Bäume, Höhlendeko

- natura (Bäume, Beeren, Wolken, Nether-Inhalte), tconstruct (Erze, Kies-Erze, Schleiminseln), cofhcore (Erz-Generator), artifice (Erze, Pflanzen), offlawn, meteorsmod (Meteore), wildcaves3 (Höhlendeko: Vanilla-Höhlen reichen bis -64, Deko auf echte Höhlenluft anwenden).
- Je Generator Höhenmodus und Biome festlegen; Mengen pro Fläche grob wie 1.5.2.
- TreeCapitator/Setzlinge prüfen (Skill `treecapitator-trees`).

## Teil I3 – Bauwerke und Dungeons A

- betterdungeons, ruins (Vorlagen auf echter Oberfläche, Skill `templates-152`), battletowers (Türme bis zur echten Höhe, unterirdische Türme), projectzulu (Pyramide, Labyrinth, Friedhof).
- Bauplatz über Heightmap; Abstand zu Vanilla-Dörfern, -Tempeln usw. halten, wo sie sich überlagern würden.

## Teil I4 – Bauwerke und Dungeons B, Dimensions-Eingänge

- legendgear, hexxitgear (Sträucher), chococraft (Gysahl-Pflanzen), qcraft, dimensional-doors (Rifts/Eingänge in der Overworld), twilightforest (Portal-Hinweise in der Overworld, falls vorhanden).
- Prüfen, dass Dimensionen von TF und DimDoors aus einer Integrated-Overworld erreichbar sind; ihre eigenen Dimensionen bleiben wie sie sind.

## Teil I5 – ExtrabiomesXL-Biome in der 26.3-Biomverteilung

- Vanilla-Multi-Noise-Parameterliste der Overworld per Skript als Datei erzeugen (aus dem 26.3-Jar bzw. `MultiNoiseBiomeSourceParameterList`), die EBXL-Biome mit eigenen Klimawerten ergänzen (Ersatz von Vanilla-Abschnitten mit ähnlichem Klima, z. B. Redwood-Wald statt eines Teils der Taiga), ohne TerraBlender. Ergebnis als Biomquelle von `hexxit:integrated`.
- Für jedes EBXL-Biom: 26.3-Biom-JSON mit Farben, Mob-Spawns, Vanilla-Features (Erze, Höhlen) plus die EBXL-Dekoration über die Brücke; Oberflächenregeln für eigene Böden (z. B. Quicksand, Red Rock) über die Noise-Settings von Integrated.
- Biomtabelle aus I1 um die EBXL-Biome erweitern, damit andere Mods dort generieren.
- Prüfen: `/locate biome` für jedes EBXL-Biom bei Seed 1234 findet etwas in vertretbarer Entfernung.

## Teil I6 – Mob-Spawns, Dungeon-Beute, Feinschliff

- Spawn-Einträge der Mods (Project Zulu, ChocoCraft, Infernal Mobs, Twilight Forest in der Overworld, …) über Biom-Modifier auf 26.3-Biome verteilen (Skill `mobs-263`); die Biomtabelle aus I1 als Grundlage.
- Hexxit-Beute in Vanilla-Truhen (Dungeons, Tempel, Minen), wie in 1.5.2 über `OldLoot`/ChestGenHooks, als Loot-Modifier.
- Liste von Unschönheiten aus I2–I5 abarbeiten.

## Teil I7 – Gesamtprüfung beider Welttypen

- Hauptbranch einmischen, neue Generatoren nachziehen.
- Legacy: komplette Prüfung wie Teil 23 des alten Plans.
- Integrated: Testwelt Seed 1234, Stichproben je Mod, Bilder für den Nutzer, Client-Prüfliste.
- Mit dem Nutzer klären, ob und wie `integrated` in den Hauptbranch zurückfließt.
