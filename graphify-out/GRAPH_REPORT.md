# Graph Report - HeroMC  (2026-11-14)

## Corpus Check
- 135 files · ~52,183 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 9 file(s) not represented in the graph (top: .properties 5, (none) 1, .toml 1)

## Summary
- 775 nodes · 1699 edges · 40 communities (18 shown, 22 thin omitted)
- Extraction: 88% EXTRACTED · 12% INFERRED · 0% AMBIGUOUS · INFERRED: 201 edges (avg confidence: 0.86)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `9c9719ab`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- OldChunkGenerator
- Layer
- reference.py
- EbxlBlocks.java
- OldBiome
- expect.sh
- ImportOriginalAssets.groovy
- OldTerrain
- Arbeitsplan: Hexxit 1.0.11 → NeoForge 26.3
- OldBiomeSource.java
- HeroMC – Hinweise für Claude
- .claude/CLAUDE.md
- OldWorld
- OldBiomes.java
- OldChunkWriter.java
- OldGameIds.java
- Material
- BigTree
- Decorate
- BiomeLayout
- Ore
- OldTerrainGen
- Listener
- WorldGen152.java
- Populate
- minecraft-dev
- OldCarver
- OldCaves
- .decorate
- OldBlocks.java
- .pick

## God Nodes (most connected - your core abstractions)
1. `OldWorld` - 87 edges
2. `OldBiome` - 49 edges
3. `Material` - 38 edges
4. `Layer` - 36 edges
5. `OldGen` - 36 edges
6. `Arbeitsplan: Hexxit 1.0.11 → NeoForge 26.3` - 24 edges
7. `OldChunkGenerator` - 20 edges
8. `Layers` - 20 edges
9. `OldBlockInfo` - 20 edges
10. `OldPlantGens` - 18 edges

## Surprising Connections (you probably didn't know these)
- `Aufbau` --references--> `WorldGenerator`  [INFERRED]
  .claude/skills/worldgen152/SKILL.md → mods/worldgen152/src/main/java/hexxit/worldgen152/core/populate/OldTerrainGen.java
- `Teil 1 – Vanilla-1.5.2-Dekoration` --references--> `OldDecorator`  [INFERRED]
  docs/PLAN.md → mods/worldgen152/src/main/java/hexxit/worldgen152/core/populate/OldDecorator.java
- `Aufbau` --references--> `OldDecorator`  [INFERRED]
  .claude/skills/worldgen152/SKILL.md → mods/worldgen152/src/main/java/hexxit/worldgen152/core/populate/OldDecorator.java
- `Aufbau` --references--> `OldDungeonGen`  [INFERRED]
  .claude/skills/worldgen152/SKILL.md → mods/worldgen152/src/main/java/hexxit/worldgen152/core/populate/OldDungeonGen.java
- `Aufbau` --references--> `OldGen`  [INFERRED]
  .claude/skills/worldgen152/SKILL.md → mods/worldgen152/src/main/java/hexxit/worldgen152/core/populate/OldGen.java

## Import Cycles
- None detected.

## Communities (40 total, 22 thin omitted)

### Community 1 - "Layer"
Cohesion: 0.09
Nodes (18): Layer, AddIsland, AddMushroomIsland, AddSnow, Biome, FuzzyZoom, Hills, Island (+10 more)

### Community 2 - "reference.py"
Cohesion: 0.06
Nodes (26): ColorMaps, f32(), fixed(), hexcolor(), main(), multiply(), sky_color(), tinted() (+18 more)

### Community 4 - "OldBiome"
Cohesion: 0.15
Nodes (4): EbxlBiomes, GrassChooser, OldBiome, OldBiomes

### Community 7 - "OldTerrain"
Cohesion: 0.13
Nodes (4): OctaveNoise, OldChunk, OldTerrain, PerlinNoise

### Community 8 - "Arbeitsplan: Hexxit 1.0.11 → NeoForge 26.3"
Cohesion: 0.06
Nodes (32): Arbeitsplan: Hexxit 1.0.11 → NeoForge 26.3, Regeln für jeden Teil, Teil 10 und 11 – Project Zulu, Teil 12 und 13 – Better Dungeons (Chocolate Quest), Teil 14 – Battle Towers, Ruins, Teil 15 – Dimensional Doors, Teil 16 – BiblioCraft, Teil 17 – BetterStorage, Backpacks, qCraft (+24 more)

### Community 11 - "HeroMC – Hinweise für Claude"
Cohesion: 0.40
Nodes (4): Arbeitsweise beim Portieren, graphify, HeroMC – Hinweise für Claude, Tokens sparen

### Community 17 - "OldWorld"
Cohesion: 0.06
Nodes (24): Aufbau, OldDecorator, OldDungeonGen, OldGen, Lake, Cactus, DeadBush, Flowers (+16 more)

### Community 18 - "OldBiomes.java"
Cohesion: 0.07
Nodes (9): DesertWell, OldLakeGens, Spring, Clay, Minable, OldOreGens, Sand, OldPopulator (+1 more)

### Community 19 - "OldChunkWriter.java"
Cohesion: 0.06
Nodes (7): OldDecoratedChunk, OldDecoration, OldTile, Spawner, Column, TerrainSource, OldChunkWriter

### Community 20 - "OldGameIds.java"
Cohesion: 0.06
Nodes (14): Biome und Dekoratoren, Blöcke anmelden, Generator anmelden, Prüfen, Regel 1: Kern-Klassen ohne Spielklassen, Stolperfallen, Truhen und Spawner, Weltgenerierung eines 1.5.2-Mods in worldgen152 einhängen (+6 more)

### Community 21 - "Material"
Cohesion: 0.06
Nodes (28): Material, AIR, CACTUS, CIRCUITS, CLAY, CLOTH, CRAFTED_SNOW, FIRE (+20 more)

### Community 24 - "Decorate"
Cohesion: 0.13
Nodes (15): Decorate, BIG_SHROOM, CACTUS, CLAY, DEAD_BUSH, FLOWERS, GRASS, LAKE (+7 more)

### Community 26 - "Ore"
Cohesion: 0.20
Nodes (9): Ore, COAL, DIAMOND, DIRT, GOLD, GRAVEL, IRON, LAPIS (+1 more)

### Community 27 - "OldTerrainGen"
Cohesion: 0.31
Nodes (3): Teil 1 – Vanilla-1.5.2-Dekoration, OldTerrainGen, WorldGenerator

### Community 29 - "WorldGen152.java"
Cohesion: 0.15
Nodes (3): Extrabiomes, RFTL, WorldGen152

### Community 30 - "Populate"
Cohesion: 0.33
Nodes (5): Populate, DUNGEON, ICE, LAKE, LAVA

### Community 31 - "minecraft-dev"
Cohesion: 0.50
Nodes (3): npx, minecraft-dev, @mcdxai/minecraft-dev-mcp

### Community 34 - "OldCarver"
Cohesion: 0.15
Nodes (3): OldCarver, OldMath, OldRavines

## Knowledge Gaps
- **54 isolated node(s):** `AIR`, `CACTUS`, `CIRCUITS`, `CLAY`, `CLOTH` (+49 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 243 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **22 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `OldBiome` connect `OldBiome` to `OldDecorator.java`, `Layer`, `OldCarver`, `OldCaves`, `.decorate`, `.toString`, `.pick`, `OldTerrain`, `OldBiomeSource.java`, `OldWorld`, `OldBiomes.java`, `OldChunkWriter.java`, `BiomeLayout`?**
  _High betweenness centrality (0.218) - this node is a cross-community bridge._
- **What connects `AIR`, `CACTUS`, `CIRCUITS` to the rest of the system?**
  _54 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Layer` be split into smaller, more focused modules?**
  _Cohesion score 0.08831306187202538 - nodes in this community are weakly interconnected._
- **Why does `OldWorld` connect `OldWorld` to `.decorate`, `.pick`, `OldBiomes.java`, `OldChunkWriter.java`, `BigTree`, `Ore`, `OldTerrainGen`, `Listener`, `Populate`?**
  _High betweenness centrality (0.143) - this node is a cross-community bridge._
- **Should `reference.py` be split into smaller, more focused modules?**
  _Cohesion score 0.06298076923076923 - nodes in this community are weakly interconnected._
- **Why does `Teil 1 – Vanilla-1.5.2-Dekoration` connect `OldTerrainGen` to `Arbeitsplan: Hexxit 1.0.11 → NeoForge 26.3`, `OldWorld`, `EbxlBlocks.java`, `.decorate`?**
  _High betweenness centrality (0.079) - this node is a cross-community bridge._
- **Should `EbxlBlocks.java` be split into smaller, more focused modules?**
  _Cohesion score 0.09879032258064516 - nodes in this community are weakly interconnected._