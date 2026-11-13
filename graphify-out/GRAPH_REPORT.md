# Graph Report - HeroMC  (2026-11-13)

## Corpus Check
- 114 files · ~28,723 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 9 file(s) not represented in the graph (top: .properties 5, (none) 1, .toml 1)

## Summary
- 380 nodes · 762 edges · 17 communities (7 shown, 10 thin omitted)
- Extraction: 94% EXTRACTED · 6% INFERRED · 0% AMBIGUOUS · INFERRED: 44 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `71f41768`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- OldChunkGenerator.java
- Layer
- reference.py
- EbxlBlocks.java
- OldCarver
- expect.sh
- ImportOriginalAssets.groovy
- BiomeLayout
- HeroMC – Hexxit 1:1 für Minecraft 26.3
- PerlinNoise
- HeroMC – Hinweise für Claude
- .claude/CLAUDE.md

## God Nodes (most connected - your core abstractions)
1. `OldBiome` - 37 edges
2. `Layer` - 36 edges
3. `Layers` - 20 edges
4. `OldChunkGenerator` - 18 edges
5. `BiomeLayout` - 16 edges
6. `OldBiomeSource` - 14 edges
7. `OldTerrain` - 14 edges
8. `EbxlBlocks` - 13 edges
9. `OldBiomes` - 13 edges
10. `ImportOriginalAssets` - 11 edges

## Surprising Connections (you probably didn't know these)
- `OldChunkGenerator` --references--> `OldChunk`  [EXTRACTED]
  mods/worldgen152/src/main/java/hexxit/worldgen152/OldChunkGenerator.java → mods/worldgen152/src/main/java/hexxit/worldgen152/core/OldChunk.java
- `OldBiomeSource` --references--> `BiomeLayout`  [EXTRACTED]
  mods/worldgen152/src/main/java/hexxit/worldgen152/OldBiomeSource.java → mods/worldgen152/src/main/java/hexxit/worldgen152/core/BiomeLayout.java
- `OldChunkGenerator` --references--> `OldTerrain`  [EXTRACTED]
  mods/worldgen152/src/main/java/hexxit/worldgen152/OldChunkGenerator.java → mods/worldgen152/src/main/java/hexxit/worldgen152/core/OldTerrain.java
- `Biome` --references--> `OldBiome`  [EXTRACTED]
  mods/worldgen152/src/main/java/hexxit/worldgen152/core/layer/Layers.java → mods/worldgen152/src/main/java/hexxit/worldgen152/core/OldBiome.java
- `OldTerrain` --references--> `OldCaves`  [EXTRACTED]
  mods/worldgen152/src/main/java/hexxit/worldgen152/core/OldTerrain.java → mods/worldgen152/src/main/java/hexxit/worldgen152/core/OldCaves.java

## Import Cycles
- None detected.

## Communities (17 total, 10 thin omitted)

### Community 1 - "Layer"
Cohesion: 0.09
Nodes (18): Layer, AddIsland, AddMushroomIsland, AddSnow, Biome, FuzzyZoom, Hills, Island (+10 more)

### Community 2 - "reference.py"
Cohesion: 0.07
Nodes (23): ColorMaps, f32(), fixed(), hexcolor(), main(), multiply(), sky_color(), tinted() (+15 more)

### Community 3 - "EbxlBlocks.java"
Cohesion: 0.07
Nodes (5): EbxlBlocks, Extrabiomes, RFTL, OldBlockStates, WorldGen152

### Community 4 - "OldCarver"
Cohesion: 0.07
Nodes (8): EbxlBiomes, OldBiome, OldBiomes, OldBlocks, OldCarver, OldCaves, OldMath, OldRavines

### Community 7 - "BiomeLayout"
Cohesion: 0.12
Nodes (4): BiomeLayout, OldChunk, OldTerrain, Expect

### Community 8 - "HeroMC – Hexxit 1:1 für Minecraft 26.3"
Cohesion: 0.22
Nodes (8): Aufbau, Bauen und Starten, HeroMC – Hexxit 1:1 für Minecraft 26.3, Referenzquellen des Originals, Status, Vorgaben, Wann ein Mod als portiert gilt, Weltgenerierung wie in 1.5.2

### Community 11 - "HeroMC – Hinweise für Claude"
Cohesion: 0.40
Nodes (4): Arbeitsweise beim Portieren, graphify, HeroMC – Hinweise für Claude, Tokens sparen

## Knowledge Gaps
- **10 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `OldBiome` connect `OldCarver` to `OldChunkGenerator.java`, `Layer`, `BiomeLayout`?**
  _High betweenness centrality (0.193) - this node is a cross-community bridge._
- **Should `OldChunkGenerator.java` be split into smaller, more focused modules?**
  _Cohesion score 0.05961538461538462 - nodes in this community are weakly interconnected._
- **Why does `Layers` connect `Layer` to `OldChunkGenerator.java`?**
  _High betweenness centrality (0.079) - this node is a cross-community bridge._
- **Should `Layer` be split into smaller, more focused modules?**
  _Cohesion score 0.08831306187202538 - nodes in this community are weakly interconnected._
- **Why does `OldTerrain` connect `BiomeLayout` to `OldChunkGenerator.java`, `PerlinNoise`, `OldCarver`?**
  _High betweenness centrality (0.065) - this node is a cross-community bridge._
- **Should `reference.py` be split into smaller, more focused modules?**
  _Cohesion score 0.07062146892655367 - nodes in this community are weakly interconnected._
- **Should `EbxlBlocks.java` be split into smaller, more focused modules?**
  _Cohesion score 0.06845513413506013 - nodes in this community are weakly interconnected._