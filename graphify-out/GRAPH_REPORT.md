# Graph Report - HeroMC  (2026-11-13)

## Corpus Check
- 115 files · ~30,411 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 9 file(s) not represented in the graph (top: .properties 5, (none) 1, .toml 1)

## Summary
- 406 nodes · 788 edges · 17 communities (7 shown, 10 thin omitted)
- Extraction: 94% EXTRACTED · 6% INFERRED · 0% AMBIGUOUS · INFERRED: 44 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `cfcb17a1`
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
- Arbeitsplan: Hexxit 1.0.11 → NeoForge 26.3
- OldBiomeSource.java
- HeroMC – Hinweise für Claude
- .claude/CLAUDE.md

## God Nodes (most connected - your core abstractions)
1. `OldBiome` - 37 edges
2. `Layer` - 36 edges
3. `Arbeitsplan: Hexxit 1.0.11 → NeoForge 26.3` - 24 edges
4. `Layers` - 20 edges
5. `OldChunkGenerator` - 18 edges
6. `BiomeLayout` - 16 edges
7. `OldBiomeSource` - 14 edges
8. `OldTerrain` - 14 edges
9. `EbxlBlocks` - 13 edges
10. `OldBiomes` - 13 edges

## Surprising Connections (you probably didn't know these)
- `OldBiomeSource` --references--> `BiomeLayout`  [EXTRACTED]
  mods/worldgen152/src/main/java/hexxit/worldgen152/OldBiomeSource.java → mods/worldgen152/src/main/java/hexxit/worldgen152/core/BiomeLayout.java
- `OldChunkGenerator` --references--> `OldBiomeSource`  [EXTRACTED]
  mods/worldgen152/src/main/java/hexxit/worldgen152/OldChunkGenerator.java → mods/worldgen152/src/main/java/hexxit/worldgen152/OldBiomeSource.java
- `OldChunkGenerator` --references--> `OldChunk`  [EXTRACTED]
  mods/worldgen152/src/main/java/hexxit/worldgen152/OldChunkGenerator.java → mods/worldgen152/src/main/java/hexxit/worldgen152/core/OldChunk.java
- `OldChunkGenerator` --references--> `OldTerrain`  [EXTRACTED]
  mods/worldgen152/src/main/java/hexxit/worldgen152/OldChunkGenerator.java → mods/worldgen152/src/main/java/hexxit/worldgen152/core/OldTerrain.java
- `Biome` --references--> `OldBiome`  [EXTRACTED]
  mods/worldgen152/src/main/java/hexxit/worldgen152/core/layer/Layers.java → mods/worldgen152/src/main/java/hexxit/worldgen152/core/OldBiome.java

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
Cohesion: 0.08
Nodes (4): EbxlBlocks, Extrabiomes, RFTL, WorldGen152

### Community 4 - "OldCarver"
Cohesion: 0.07
Nodes (8): EbxlBiomes, OldBiome, OldBiomes, OldBlocks, OldCarver, OldCaves, OldMath, OldRavines

### Community 7 - "BiomeLayout"
Cohesion: 0.08
Nodes (6): BiomeLayout, OctaveNoise, OldChunk, OldTerrain, PerlinNoise, Expect

### Community 8 - "Arbeitsplan: Hexxit 1.0.11 → NeoForge 26.3"
Cohesion: 0.06
Nodes (33): Arbeitsplan: Hexxit 1.0.11 → NeoForge 26.3, Regeln für jeden Teil, Teil 10 und 11 – Project Zulu, Teil 12 und 13 – Better Dungeons (Chocolate Quest), Teil 14 – Battle Towers, Ruins, Teil 15 – Dimensional Doors, Teil 16 – BiblioCraft, Teil 17 – BetterStorage, Backpacks, qCraft (+25 more)

### Community 11 - "HeroMC – Hinweise für Claude"
Cohesion: 0.40
Nodes (4): Arbeitsweise beim Portieren, graphify, HeroMC – Hinweise für Claude, Tokens sparen

## Knowledge Gaps
- **10 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `OldBiome` connect `OldCarver` to `OldChunkGenerator.java`, `Layer`, `OldBiomeSource.java`, `BiomeLayout`?**
  _High betweenness centrality (0.169) - this node is a cross-community bridge._
- **Should `OldChunkGenerator.java` be split into smaller, more focused modules?**
  _Cohesion score 0.0782608695652174 - nodes in this community are weakly interconnected._
- **Why does `Layers` connect `Layer` to `OldChunkGenerator.java`?**
  _High betweenness centrality (0.069) - this node is a cross-community bridge._
- **Should `Layer` be split into smaller, more focused modules?**
  _Cohesion score 0.08831306187202538 - nodes in this community are weakly interconnected._
- **Why does `OldTerrain` connect `BiomeLayout` to `OldChunkGenerator.java`, `OldCarver`?**
  _High betweenness centrality (0.057) - this node is a cross-community bridge._
- **Should `reference.py` be split into smaller, more focused modules?**
  _Cohesion score 0.07062146892655367 - nodes in this community are weakly interconnected._
- **Should `EbxlBlocks.java` be split into smaller, more focused modules?**
  _Cohesion score 0.08232118758434548 - nodes in this community are weakly interconnected._