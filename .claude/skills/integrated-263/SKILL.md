---
name: integrated-263
description: "Beim Arbeiten am Welttyp Hexxit Integrated (Teile 26b–26f) benutzen: einen 1.5.2-Generator aus OldTerrainGen als 26.3-Feature an Biome hängen (Biom-Modifier mit Tags), die Chunk-Brücke OldGeneratorFeature verstehen und erweitern (Höhenfenster, Biom-Zuordnung über Tags, welche Blöcke ersetzt werden), Legacy vs. Integrated am Testserver umschalten und prüfen."
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

## Die Brücke (`mods/worldgen152/.../OldGeneratorFeature.java`)

- Alte Koordinaten = echte + 8 (`SHIFT`): Der 1.5.2-Populationsbereich ist genau der Chunk, Reichweite 16 nach jeder
  Seite. Gelesen/geschrieben wird nur der 3×3-Ausschnitt der `WorldGenRegion`; außerhalb sieht der Generator Stein
  bis y 61, Schreibversuche dorthin fallen weg (große Bauten → 26c als echte `Structure`).
- Höhen 1:1 (Meer 63), alte Welt 256 hoch; `min_y`/`max_y` begrenzen Lesen und Schreiben. Unter y 0 sieht kein
  Generator etwas → für Erze eine Abbildung ergänzen (Teil 26b).
- Biome: `OldBiomeTags.oldId` – alte Biome (worldgen152/EBXL) behalten ihre ID, sonst erste passende Regel über
  `c:`/`minecraft:`-Tags, kalt (`c:is_snowy`/`c:is_icy`) wählt die Frost-Variante. Neue Regeln dort, nicht im Generator.
  Immer `getNoiseBiome` (ungezoomt) – `getBiome` liest am Rand Chunks außerhalb → „Requested chunk unavailable“.
- Ersetzen nur über `replaceable(state)`: Luft, `canBeReplaced`, Stein-/Erd-/Sand-/Kies-Tags, Gras/Myzel/Podsol
  (stehen in 26.3 **nicht** in `#minecraft:dirt`), Stämme, Laub, Hexxit-Blöcke (`OldBlockStates.modCell`). Nie:
  Grundgestein, Block-Entities, fremde Blöcke.
- Zufall: `modRandom`-Formel von 1.5.2 mit `seed ^ hash(name)`, je Generator eigene Folge.
- `world.layout()` und `world.structures()` sind in Integrated `null`; Generatoren, die sie brauchen, erst in 26c.
  Fehler werden einmal je Generator geloggt („1.5.2 world generator … failed“) und der Chunk bleibt ohne ihn.

## Prüfen am Server

```
python3 tools/rcon.py 'locate biome minecraft:forest'
python3 tools/rcon.py 'forceload add <x1> <z1> <x2> <z2>'          # dann ~40 s warten
python3 tools/rcon.py 'execute if block X Y Z natura:sakura_log'  # einzelne Blöcke
grep -E "ERROR|Exception" run/server/logs/latest.log
```
Zählen mit `fill … X replace X` meldet nur Blöcke, deren Zustand sich ändert (Laub mit Distanz ja, senkrechte
Stämme nicht) – keine Mengenprüfung damit. Für Mengen einen Block mit anderem Zustand einsetzen oder
`execute if block` in einer Schleife.
