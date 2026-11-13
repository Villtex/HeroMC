# Graph Report - HeroMC  (2026-11-13)

## Corpus Check
- 110 files · ~27,622 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 9 file(s) not represented in the graph (top: .properties 5, (none) 1, .toml 1)

## Summary
- 366 nodes · 739 edges · 16 communities (8 shown, 8 thin omitted)
- Extraction: 95% EXTRACTED · 5% INFERRED · 0% AMBIGUOUS · INFERRED: 40 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `0c2f997d`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- OldChunkGenerator.java
- Layer
- reference.py
- EbxlBlocks.java
- OldCarver
- OldBiome
- ImportOriginalAssets.groovy
- BiomeLayout
- HeroMC – Hexxit 1:1 für Minecraft 26.3
- PerlinNoise
- CLAUDE.md
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
- `OldBiomeSource` --references--> `BiomeLayout`  [EXTRACTED]
  mods/worldgen152/src/main/java/hexxit/worldgen152/OldBiomeSource.java → mods/worldgen152/src/main/java/hexxit/worldgen152/core/BiomeLayout.java
- `OldChunkGenerator` --references--> `OldTerrain`  [EXTRACTED]
  mods/worldgen152/src/main/java/hexxit/worldgen152/OldChunkGenerator.java → mods/worldgen152/src/main/java/hexxit/worldgen152/core/OldTerrain.java
- `BiomeLayout` --references--> `OldBiome`  [EXTRACTED]
  mods/worldgen152/src/main/java/hexxit/worldgen152/core/BiomeLayout.java → mods/worldgen152/src/main/java/hexxit/worldgen152/core/OldBiome.java
- `OctaveNoise` --references--> `PerlinNoise`  [EXTRACTED]
  mods/worldgen152/src/main/java/hexxit/worldgen152/core/OctaveNoise.java → mods/worldgen152/src/main/java/hexxit/worldgen152/core/PerlinNoise.java
- `Biome` --references--> `OldBiome`  [EXTRACTED]
  mods/worldgen152/src/main/java/hexxit/worldgen152/core/layer/Layers.java → mods/worldgen152/src/main/java/hexxit/worldgen152/core/OldBiome.java

## Import Cycles
- None detected.

## Communities (16 total, 8 thin omitted)

### Community 0 - "OldChunkGenerator.java"
Cohesion: 0.06
Nodes (4): OldChunk, OldBiomeSource, OldChunkGenerator, WorldGen152

### Community 1 - "Layer"
Cohesion: 0.08
Nodes (18): Layer, AddIsland, AddMushroomIsland, AddSnow, Biome, FuzzyZoom, Hills, Island (+10 more)

### Community 2 - "reference.py"
Cohesion: 0.07
Nodes (22): ColorMaps, f32(), fixed(), hexcolor(), main(), multiply(), sky_color(), tinted() (+14 more)

### Community 3 - "EbxlBlocks.java"
Cohesion: 0.07
Nodes (4): EbxlBlocks, Extrabiomes, RFTL, OldBlockStates

### Community 4 - "OldCarver"
Cohesion: 0.10
Nodes (4): OldCarver, OldCaves, OldMath, OldRavines

### Community 5 - "OldBiome"
Cohesion: 0.13
Nodes (4): EbxlBiomes, OldBiome, OldBiomes, OldBlocks

### Community 7 - "BiomeLayout"
Cohesion: 0.18
Nodes (3): BiomeLayout, OctaveNoise, OldTerrain

### Community 8 - "HeroMC – Hexxit 1:1 für Minecraft 26.3"
Cohesion: 0.22
Nodes (8): Aufbau, Bauen und Starten, HeroMC – Hexxit 1:1 für Minecraft 26.3, Referenzquellen des Originals, Status, Vorgaben, Wann ein Mod als portiert gilt, Weltgenerierung wie in 1.5.2

## Knowledge Gaps
- **8 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `OldBiome` connect `OldBiome` to `OldChunkGenerator.java`, `Layer`, `OldCarver`, `BiomeLayout`?**
  _High betweenness centrality (0.200) - this node is a cross-community bridge._
- **Should `OldChunkGenerator.java` be split into smaller, more focused modules?**
  _Cohesion score 0.05879692446856626 - nodes in this community are weakly interconnected._
- **Why does `OldTerrain` connect `BiomeLayout` to `OldChunkGenerator.java`, `OldCarver`?**
  _High betweenness centrality (0.069) - this node is a cross-community bridge._
- **Should `Layer` be split into smaller, more focused modules?**
  _Cohesion score 0.08018648018648018 - nodes in this community are weakly interconnected._
- **Should `reference.py` be split into smaller, more focused modules?**
  _Cohesion score 0.07205513784461152 - nodes in this community are weakly interconnected._
- **Should `EbxlBlocks.java` be split into smaller, more focused modules?**
  _Cohesion score 0.06570048309178744 - nodes in this community are weakly interconnected._
- **Should `OldCarver` be split into smaller, more focused modules?**
  _Cohesion score 0.1028225806451613 - nodes in this community are weakly interconnected._