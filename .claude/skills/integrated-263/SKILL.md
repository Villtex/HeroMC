---
name: integrated-263
description: "Beim Arbeiten am Welttyp Hexxit Integrated (Teile 26b–26f) benutzen: EBXL- oder eigene Biome über TerraBlender mit Oberflächenregeln, Mob-Spawns über Biom-Tags, einen 1.5.2-Generator aus OldTerrainGen als 26.3-Feature an Biome hängen (Biom-Modifier mit Tags), ein 1.5.2-Bauwerk als echte 26.3-Struktur (worldgen152:old_structure, Struktur-Set, /locate) oder eine 1.5.2-Dorfkomponente als Haus der 26.3-Dörfer, die Chunk-Brücke OldGeneratorFeature verstehen und erweitern (Höhenfenster, Biom-Zuordnung über Tags, welche Blöcke ersetzt werden), Legacy vs. Integrated am Testserver umschalten und prüfen."
---

# Welttyp Hexxit Integrated (26.3-Gelände + Hexxit-Generatoren)

## Welttypen

- `minecraft:normal` = **Hexxit Integrated** (Standard, Vanilla-Generierung; Sprachschlüssel umbenannt in
  `worldgen152/lang`). Hexxit-Inhalte kommen nur über registrierte Features + Biom-Modifier dazu.
- `hexxit:legacy` = bitgenaues 1.5.2 (`mods/worldgen152/src/main/resources/data/hexxit/worldgen/world_preset/legacy.json`,
  per Tag `minecraft:normal` in der Liste).
- Server: `run/server/server.properties` (nicht im Repo). Legacy: `level-type=hexxit\:legacy`; Integrated: Zeile weg.
  Welt nach dem Umschalten löschen (`rm -rf run/server/world`), sonst bleibt der alte Typ.
- In Legacy tut jedes Brücken-Feature nichts (`OldChunkGenerator`/`OldNetherChunkGenerator` → `false`).
  `expect.sh` + `verify_blocks.py` nur mit Legacy laufen lassen. Bekannt: 2 Ranken bei 2054/35–36/−818 (Seed 1234,
  `find 260`) weichen schon vor Teil 26 ab.

## Einen Generator einhängen

1. Er ist schon benannt: `OldTerrainGen.registerWorldGenerator(fmlId, "natura:trees", gen)` (Liste in
   `docs/PLAN.md`, Teil 26a – Stand). Neue Generatoren bekommen gleich einen Namen.
2. Feature + Platzierung liegen im Mod (für alle 30 schon angelegt):
   ```
   mods/<mod>/src/main/resources/data/<ns>/worldgen/feature/<name>.json
     {"type": "worldgen152:old_generator", "generator": "<ns>:<name>", "min_y": 0, "max_y": 255}
   mods/<mod>/src/main/resources/data/<ns>/worldgen/placed_feature/<name>.json
     {"feature": "<ns>:<name>", "placement": []}       # einmal je Chunk; der Generator wählt selbst
   ```
   26.3 hat kein `configured_feature` mehr: Die Konfiguration steckt im Feature selbst (`worldgen/feature`,
   Registry `FEATURE`, Typen in `FEATURE_TYPE`).
3. Biom-Modifier, eine Datei je Generator:
   ```
   mods/<mod>/src/main/resources/data/<ns>/neoforge/biome_modifier/<name>.json
     {"type": "neoforge:add_features", "biomes": "#minecraft:is_overworld",
      "features": "<ns>:<name>", "step": "vegetal_decoration"}
   ```
   Schritte: `underground_ores` (Erze), `vegetal_decoration` (Bäume, Pflanzen), `surface_structures` (kleine Bauten),
   `local_modifications`, `underground_decoration` (Höhlen-Deko). Nether: `#minecraft:is_nether`.
   Der Generator prüft das Biom selbst (wie 1.5.2), deshalb reicht meist der weite Tag; enger nur, um Arbeit zu sparen.
4. Bauen (`CI=true ./gradlew pack`), Integrated-Welt neu, prüfen (unten).

Teile eines Sammel-Generators oder Varianten mit anderen Höhen: im Kern `OldTerrainGen.registerFeature("ns:teil", gen)`
(läuft nur als Feature, nie in Legacy; Vorbilder `TCWorldGen.registerIntegrated`, `ArtificeWorldGen.register`,
`CoFHWorldGen.generateCustom`). Den Sammel-Generator dann ohne Feature-Datei lassen, sonst doppelt.
Untergrund: Feature mit `"y_offset": -64` (echt = alt − 64) und Höhen über `OldTerrainGen.deepY(y)` (1.5.2 0–63 →
echt −64…63, darüber gleich). Erze, die ein fremder Mod auch bringt: Schalter in `IntegratedConfig` (worldgen152)
eintragen; jeder Name lässt sich außerdem über `disabledFeatures` abschalten. Vanilla-Features abhängig von der
Konfiguration herausnehmen: Biom-Modifier-Typ `worldgen152:replace_vanilla_ores` als Vorbild
(`ReplaceVanillaOresModifier`, Phase REMOVE). Vanilla-Erze, die der Generator setzt, werden in Deepslate/Tuff
automatisch zur Deepslate-Variante. Teile, die in Legacy zum Gelände gehörten (Natura-Nether-Boden), als
Nachbearbeitung im Schritt `raw_generation` (`natura/gen/NetherSoilGen`).

## Die Brücke (`mods/worldgen152/.../OldGeneratorFeature.java`)

- Alte Koordinaten = echte + 8 (`SHIFT`): Der 1.5.2-Populationsbereich ist genau der Chunk, Reichweite 16 nach jeder
  Seite. Gelesen/geschrieben wird nur der 3×3-Ausschnitt der `WorldGenRegion`; außerhalb sieht der Generator Stein
  bis y 61, Schreibversuche dorthin fallen weg (große Bauten → 26c als echte `Structure`).
- Höhen: echt = alt + `y_offset` (Standard 0, Meer 63); alte Welt 256 hoch; `min_y`/`max_y` (alte Höhen) begrenzen
  Lesen und Schreiben. Hängende Entities übernehmen den Offset noch nicht.
- Biome: `OldBiomeTags.oldId` – alte Biome (worldgen152/EBXL) behalten ihre ID, sonst erste passende Regel über
  `c:`/`minecraft:`-Tags, kalt (`c:is_snowy`/`c:is_icy`) wählt die Frost-Variante. Neue Regeln dort, nicht im Generator.
  Immer `getNoiseBiome` (ungezoomt) – `getBiome` liest am Rand Chunks außerhalb → „Requested chunk unavailable“.
- Ersetzen nur über `replaceable(state)`: Luft, `canBeReplaced`, Stein-/Erd-/Sand-/Kies-Tags, Gras/Myzel/Podsol
  (stehen in 26.3 **nicht** in `#minecraft:dirt`), Stämme, Laub, Hexxit-Blöcke (`OldBlockStates.modCell`). Nie:
  Grundgestein, Block-Entities, fremde Blöcke.
- Zufall: `modRandom`-Formel von 1.5.2 mit `seed ^ hash(name)`, je Generator eigene Folge.
- `world.layout()` und `world.structures()` sind in Integrated `null`; Generatoren, die sie brauchen, erst in 26c.
  Fehler werden einmal je Generator geloggt („1.5.2 world generator … failed“) und der Chunk bleibt ohne ihn.

## Ein Bauwerk als 26.3-Struktur (Teil 26c)

1. Builder im Mod: `OldTerrainGen.registerStructure("ns:name", (random, chunkX, chunkZ, world) -> …)` baut
   **ohne** die alte Seltenheit (die macht das Struktur-Set), mit den Gelände-/Biom-Prüfungen des Originals. Selten
   mögliche Chunks: dritter Parameter `(seed, cx, cz) -> boolean` (billig, vor jedem Gelände). Vorbilder:
   `BTWorldGen`, `BDWorldGen`, `ZuluWorldGen.Structure.build`. Den Sammel-Generator für Legacy lassen, seine
   Feature-Datei löschen, Rest (Pflanzen …) als `registerFeature` abtrennen.
2. Daten: `worldgen/structure/<name>.json` (`"type": "worldgen152:old_structure", "generator": "ns:name",
   "biomes": "#ns:has_structure/<name>", "spawn_overrides": {}, "step": "surface_structures", "avoid_radius": 3`),
   `worldgen/structure_set/<name>.json` (`minecraft:random_spread`, spacing/separation ≈ Kandidaten-Anteil × Chance
   des Originals, eigenes `salt`), Biom-Tag `tags/worldgen/biome/has_structure/<name>.json`; in
   `worldgen152/…/data/hexxit/tags/worldgen/structure/structures.json` eintragen.
3. So läuft es (`OldStructure`, `OldStructureBuild`): Gelände des Start-Chunks und aller berührten Chunks wird mit dem
   echten Chunk-Generator in Hilfs-`ProtoChunk`s vorberechnet (vor Features, ohne Beardifier), der Builder läuft einmal
   (alt = echt, y 0–255), die Box ist genau das Gebaute; jeder Chunk schreibt beim Dekorieren seinen Teil. Reichweite
   höchstens 8 Chunks vom Start. Kein Bau → kein Start. `disabledFeatures` gilt auch für Builder-Namen.
4. Prüfen: `locate structure ns:name` (bei langer Suche `tools/rcon.py` mit größerem Socket-Timeout),
   in Stücken von ≤ 64 Chunks forceloaden, `tools/count_blocks.py` auf typische Blöcke (Spawner, Truhen).

Kleine Bauten (Ruinen-Vorlagen, Risse) bleiben Features: `world.writable(minX, minZ, maxX, maxZ)` sagt, ob ein Platz
ganz in den 3×3 Chunks liegt (Legacy: immer ja). Meldet ein Generator Orte an das Spiel (Risse, Pocket-Links), über
`world.realX/realY/realZ` umrechnen (Feature: alt = echt + 8).

## 1.5.2-Dorfkomponente als Haus der 26.3-Dörfer

`OldStructures.VILLAGE_PIECES.put("ns:name", new VillagePiece(breite, höhe, tiefe, floor, gewichtProzent,
(start, box, mode) -> new MyComponent(start, 0, new Random(0L), box, mode).grounded()))` – Größe in Ausrichtung 0
(Tür bei z = 0, in der Mitte von x, Breite ungerade), `floor` = Blöcke von Straßenhöhe bis zu alt y 0 (Komponente, die
`maxY + 4` versetzt: 0; `maxY + 2`: 1), `grounded()` setzt `averageGroundLevel` ≥ 0. `OldVillagePiece` trägt es beim
Serverstart in `minecraft:village/<typ>/houses` ein. Vorbild: `tconstruct/gen/TCWorldGen` (Werkstatt, Schmelze).

## Biome, Oberflächen, Spawns (Teil 26d)

- Biome kommen über **TerraBlender** (Region, wie fremde Biom-Mods): Vorbild `mods/ebxl/.../integrated/EbxlRegion`
  (`ModifiedVanillaOverworldBuilder().build()`, Vanilla-Biom je Klimapunkt ersetzen, Registrierung in
  `FMLCommonSetupEvent.enqueueWork`). Abhängigkeit: `mod_uses_terrablender=true` in `gradle.properties` des Mods.
- Oberflächen = 26.3-„material rules“ (`data/<ns>/worldgen/material_rule/*.json`, Bedingungen wie Vanilla unter
  `material_rule/overworld/*`), anmelden mit `MaterialRuleManager.addRules(OVERWORLD, modid, key)`; gilt nur für
  Biome des Namensraums, sonst Vanilla. Nur in `above_preliminary_surface` + `on_floor`/`under_floor` greifen.
- Biom-JSON: Carver + Vanilla-Untergrund-Features stehen drin (`tools/biomes.py --generation-only`); Legacy lässt alle
  `minecraft:`-Features und -Struktur-Sets weg (`OldChunkGenerator.legacySettings`/`createState`). Vanilla-Feature-
  Listen nur als Teilfolge der Vanilla-Reihenfolge, sonst „Feature order cycle“.
- Biom-Tags und Spawns: `tools/integrated_tags.py` (Tabellen `EBXL`, `STRUCTURES`, `OLD_TAGS`). Spawn-Biome als
  `{"type":"neoforge:or","values":[[alte Liste],{"type":"neoforge:and","values":[<tag|or>,{"type":"neoforge:not","value":"#worldgen152:old_biomes"}]}]}`
  – eine Holder-Set-Liste darf **keine** `#tags` enthalten, mehrere Tags in ein `neoforge:or`. Neue Spawn-Datei mit
  alter Liste anlegen, Skript erneut laufen lassen. Eigene Biom-Prüfungen im Code: `holder.is(#worldgen152:old_biomes)`
  → Original-Liste, sonst Tag (Vorbild `chococraft/entity/ChocoboSpawner.inSpawnBiome`).

## Prüfen am Server

```
python3 tools/rcon.py 'locate biome minecraft:forest'
python3 tools/rcon.py 'forceload add <x1> <z1> <x2> <z2>'          # dann ~40 s warten
python3 tools/rcon.py 'execute if block X Y Z natura:sakura_log'  # einzelne Blöcke
grep -E "ERROR|Exception" run/server/logs/latest.log
```
Für große `forceload`: `max-tick-time=-1` in `server.properties`, sonst beendet der Watchdog den Server.
Zählen: `python3 tools/count_blocks.py [--dim minecraft:the_nether] X1 Z1 X2 Z2 YMIN YMAX block…` (ersetzt durch
Luft, nur Test-Welt; Bereich vorher forceloaden und ~1–2 min warten). `fill … X replace X` taugt nicht zum Zählen
(meldet nur geänderte Zustände). Zeitvergleich: dieselbe Fläche mit allen Namen in `disabledFeatures`.
