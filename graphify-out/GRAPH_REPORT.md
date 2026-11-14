# Graph Report - HeroMC  (2026-11-14)

## Corpus Check
- 134 files · ~50,830 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 9 file(s) not represented in the graph (top: .properties 5, (none) 1, .toml 1)

## Summary
- 765 nodes · 1681 edges · 34 communities (17 shown, 17 thin omitted)
- Extraction: 89% EXTRACTED · 11% INFERRED · 0% AMBIGUOUS · INFERRED: 192 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `9d46100c`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- OldChunkGenerator
- Layer
- reference.py
- EbxlBlocks.java
- OldCarver
- expect.sh
- ImportOriginalAssets.groovy
- OldTerrain
- Arbeitsplan: Hexxit 1.0.11 → NeoForge 26.3
- OldBiomeSource.java
- HeroMC – Hinweise für Claude
- .claude/CLAUDE.md
- OldWorld
- OldGen
- OldChunkWriter.java
- OldGameIds.java
- Material
- BigTree
- Decorate
- BiomeLayout
- Ore
- OldTerrainGen
- Listener
- PerlinNoise
- Populate
- minecraft-dev

## God Nodes (most connected - your core abstractions)
1. `OldWorld` - 87 edges
2. `OldBiome` - 49 edges
3. `Material` - 38 edges
4. `Layer` - 36 edges
5. `OldGen` - 35 edges
6. `Arbeitsplan: Hexxit 1.0.11 → NeoForge 26.3` - 24 edges
7. `OldChunkGenerator` - 20 edges
8. `OldBlockInfo` - 20 edges
9. `Layers` - 20 edges
10. `OldDecoratedChunk` - 18 edges

## Surprising Connections (you probably didn't know these)
- `Teil 1 – Vanilla-1.5.2-Dekoration` --references--> `OldDecorator`  [INFERRED]
  docs/PLAN.md → mods/worldgen152/src/main/java/hexxit/worldgen152/core/populate/OldDecorator.java
- `OldChunkGenerator` --references--> `OldDecoration`  [EXTRACTED]
  mods/worldgen152/src/main/java/hexxit/worldgen152/OldChunkGenerator.java → mods/worldgen152/src/main/java/hexxit/worldgen152/core/populate/OldDecoration.java
- `OldChunkGenerator` --references--> `OldBiomeSource`  [EXTRACTED]
  mods/worldgen152/src/main/java/hexxit/worldgen152/OldChunkGenerator.java → mods/worldgen152/src/main/java/hexxit/worldgen152/OldBiomeSource.java
- `BiomeLayout` --references--> `OldBiome`  [EXTRACTED]
  mods/worldgen152/src/main/java/hexxit/worldgen152/core/BiomeLayout.java → mods/worldgen152/src/main/java/hexxit/worldgen152/core/OldBiome.java
- `Biome` --references--> `OldBiome`  [EXTRACTED]
  mods/worldgen152/src/main/java/hexxit/worldgen152/core/layer/Layers.java → mods/worldgen152/src/main/java/hexxit/worldgen152/core/OldBiome.java

## Import Cycles
- None detected.

## Communities (34 total, 17 thin omitted)

### Community 1 - "Layer"
Cohesion: 0.09
Nodes (18): Layer, AddIsland, AddMushroomIsland, AddSnow, Biome, FuzzyZoom, Hills, Island (+10 more)

### Community 2 - "reference.py"
Cohesion: 0.06
Nodes (26): ColorMaps, f32(), fixed(), hexcolor(), main(), multiply(), sky_color(), tinted() (+18 more)

### Community 3 - "EbxlBlocks.java"
Cohesion: 0.06
Nodes (5): EbxlBlocks, Extrabiomes, RFTL, OldBlockStates, WorldGen152

### Community 4 - "OldCarver"
Cohesion: 0.05
Nodes (11): EbxlBiomes, Decoration, GrassChooser, OldBiome, TreeChooser, OldBiomes, OldBlocks, OldCarver (+3 more)

### Community 7 - "OldTerrain"
Cohesion: 0.16
Nodes (3): OctaveNoise, OldChunk, OldTerrain

### Community 8 - "Arbeitsplan: Hexxit 1.0.11 → NeoForge 26.3"
Cohesion: 0.06
Nodes (32): Arbeitsplan: Hexxit 1.0.11 → NeoForge 26.3, Regeln für jeden Teil, Teil 10 und 11 – Project Zulu, Teil 12 und 13 – Better Dungeons (Chocolate Quest), Teil 14 – Battle Towers, Ruins, Teil 15 – Dimensional Doors, Teil 16 – BiblioCraft, Teil 17 – BetterStorage, Backpacks, qCraft (+24 more)

### Community 11 - "HeroMC – Hinweise für Claude"
Cohesion: 0.40
Nodes (4): Arbeitsweise beim Portieren, graphify, HeroMC – Hinweise für Claude, Tokens sparen

### Community 17 - "OldWorld"
Cohesion: 0.08
Nodes (5): OldDungeonGen, DeadBush, Pumpkin, Column, OldWorld

### Community 18 - "OldGen"
Cohesion: 0.05
Nodes (27): OldDecorator, OldGen, DesertWell, Lake, OldLakeGens, Spring, Clay, Minable (+19 more)

### Community 19 - "OldChunkWriter.java"
Cohesion: 0.06
Nodes (7): OldDecoratedChunk, OldDecoration, OldTile, Spawner, TerrainSource, OldChunkWriter, Expect

### Community 20 - "OldGameIds.java"
Cohesion: 0.08
Nodes (6): Category, Entry, OldLoot, OldStack, Chest, OldGameIds

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

### Community 30 - "Populate"
Cohesion: 0.33
Nodes (5): Populate, DUNGEON, ICE, LAKE, LAVA

### Community 31 - "minecraft-dev"
Cohesion: 0.50
Nodes (3): npx, minecraft-dev, @mcdxai/minecraft-dev-mcp

## Knowledge Gaps
- **54 isolated node(s):** `npx`, `@mcdxai/minecraft-dev-mcp`, `AIR`, `GRASS`, `GROUND` (+49 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 239 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **17 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `OldBiome` connect `OldCarver` to `OldDecorator.java`, `Layer`, `OldTerrain`, `OldBiomeSource.java`, `OldWorld`, `OldGen`, `OldChunkWriter.java`, `BiomeLayout`?**
  _High betweenness centrality (0.219) - this node is a cross-community bridge._
- **What connects `npx`, `@mcdxai/minecraft-dev-mcp`, `AIR` to the rest of the system?**
  _54 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Layer` be split into smaller, more focused modules?**
  _Cohesion score 0.08831306187202538 - nodes in this community are weakly interconnected._
- **Why does `OldWorld` connect `OldWorld` to `OldCarver`, `OldGen`, `OldChunkWriter.java`, `BigTree`, `Ore`, `OldTerrainGen`, `Listener`, `Populate`?**
  _High betweenness centrality (0.147) - this node is a cross-community bridge._
- **Should `reference.py` be split into smaller, more focused modules?**
  _Cohesion score 0.06298076923076923 - nodes in this community are weakly interconnected._
- **Why does `Teil 1 – Vanilla-1.5.2-Dekoration` connect `OldTerrainGen` to `Arbeitsplan: Hexxit 1.0.11 → NeoForge 26.3`, `OldGen`, `EbxlBlocks.java`, `OldCarver`?**
  _High betweenness centrality (0.080) - this node is a cross-community bridge._
- **Should `EbxlBlocks.java` be split into smaller, more focused modules?**
  _Cohesion score 0.06458635703918723 - nodes in this community are weakly interconnected._