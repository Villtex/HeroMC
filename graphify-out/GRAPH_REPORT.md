# Graph Report - HeroMC  (2026-11-14)

## Corpus Check
- 191 files · ~87,884 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 9 file(s) not represented in the graph (top: .properties 5, (none) 1, .toml 1)

## Summary
- 1261 nodes · 3940 edges · 52 communities (24 shown, 28 thin omitted)
- Extraction: 90% EXTRACTED · 10% INFERRED · 0% AMBIGUOUS · INFERRED: 389 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `a79be0c2`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- StructureBoundingBox
- Layer
- reference.py
- WorldGen152.java
- OldBiome
- expect.sh
- ImportOriginalAssets.groovy
- OldTerrain
- Arbeitsplan: Hexxit 1.0.11 → NeoForge 26.3
- OldChunkGenerator.java
- HeroMC – Hinweise für Claude
- .claude/CLAUDE.md
- .getBlockId
- OldBlockInfo
- OldDecoratedChunk
- OldGameIds.java
- Material
- ComponentVillageStartPiece
- BigTree
- Decorate
- OldStructures
- .findIntersecting
- OldTerrainGen
- StructureComponent
- OldWorld
- .getComponentType
- minecraft-dev
- OldPlantGens
- OldCarver
- .tunnel
- OldBlocks.java
- .column
- OldGen
- OldRails
- Weltgenerierung eines 1.5.2-Mods in worldgen152 einhängen
- .generate
- .generate
- OldChunkWriter
- PerlinNoise
- Expect.java
- .func_74919_a
- .func_74898_a
- OldChunk
- GrassChooser

## God Nodes (most connected - your core abstractions)
1. `OldWorld` - 213 edges
2. `StructureBoundingBox` - 111 edges
3. `OldBlockInfo` - 75 edges
4. `OldEntity` - 66 edges
5. `StructureComponent` - 66 edges
6. `OldTile` - 64 edges
7. `OldLoot` - 63 edges
8. `OldBiome` - 44 edges
9. `ComponentVillageStartPiece` - 41 edges
10. `OldStructures` - 41 edges

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

## Communities (52 total, 28 thin omitted)

### Community 0 - "StructureBoundingBox"
Cohesion: 0.05
Nodes (17): MinecartChest, Villager, Witch, Category, Entry, OldStack, Chest, Dispenser (+9 more)

### Community 1 - "Layer"
Cohesion: 0.09
Nodes (18): Layer, AddIsland, AddMushroomIsland, AddSnow, Biome, FuzzyZoom, Hills, Island (+10 more)

### Community 2 - "reference.py"
Cohesion: 0.06
Nodes (26): ColorMaps, f32(), fixed(), hexcolor(), main(), multiply(), sky_color(), tinted() (+18 more)

### Community 3 - "WorldGen152.java"
Cohesion: 0.05
Nodes (5): EbxlBlocks, Extrabiomes, RFTL, OldBlockStates, WorldGen152

### Community 4 - "OldBiome"
Cohesion: 0.14
Nodes (5): EbxlBiomes, Decoration, OldBiome, TreeChooser, OldBiomes

### Community 8 - "Arbeitsplan: Hexxit 1.0.11 → NeoForge 26.3"
Cohesion: 0.06
Nodes (32): Arbeitsplan: Hexxit 1.0.11 → NeoForge 26.3, Regeln für jeden Teil, Teil 10 und 11 – Project Zulu, Teil 12 und 13 – Better Dungeons (Chocolate Quest), Teil 14 – Battle Towers, Ruins, Teil 15 – Dimensional Doors, Teil 16 – BiblioCraft, Teil 17 – BetterStorage, Backpacks, qCraft (+24 more)

### Community 9 - "OldChunkGenerator.java"
Cohesion: 0.05
Nodes (3): BiomeLayout, OldBiomeSource, OldChunkGenerator

### Community 11 - "HeroMC – Hinweise für Claude"
Cohesion: 0.40
Nodes (4): Arbeitsweise beim Portieren, graphify, HeroMC – Hinweise für Claude, Tokens sparen

### Community 17 - ".getBlockId"
Cohesion: 0.16
Nodes (9): BigMushroom, Birch, Huge, OldTreeGens, Pine, Shrub, Spruce, Swamp (+1 more)

### Community 18 - "OldBlockInfo"
Cohesion: 0.18
Nodes (4): OldBlockInfo, OldEntity, OldLoot, OldTile

### Community 21 - "Material"
Cohesion: 0.06
Nodes (27): Material, AIR, CACTUS, CIRCUITS, CLAY, CLOTH, CRAFTED_SNOW, FIRE (+19 more)

### Community 22 - "ComponentVillageStartPiece"
Cohesion: 0.08
Nodes (13): ComponentVillage, ComponentVillageField2, ComponentVillageField, ComponentVillageHall, ComponentVillageHouse2, ComponentVillageHouse3, ComponentVillageHouse4_Garden, ComponentVillageStartPiece (+5 more)

### Community 24 - "Decorate"
Cohesion: 0.07
Nodes (24): Decorate, BIG_SHROOM, CACTUS, CLAY, DEAD_BUSH, FLOWERS, GRASS, LAKE (+16 more)

### Community 25 - "OldStructures"
Cohesion: 0.06
Nodes (10): OldDecoration, TerrainSource, Found, OldStructures, VillageBlocks, StructureMineshaftStart, StructureScatteredFeatureStart, StructureStart (+2 more)

### Community 26 - ".findIntersecting"
Cohesion: 0.10
Nodes (17): ComponentMineshaftStairs, ComponentStrongholdChestCorridor, ComponentStrongholdCorridor, ComponentStrongholdCrossing, ComponentStrongholdLeftTurn, ComponentStrongholdLibrary, ComponentStrongholdPortalRoom, ComponentStrongholdPrison (+9 more)

### Community 27 - "OldTerrainGen"
Cohesion: 0.10
Nodes (9): Teil 1 – Vanilla-1.5.2-Dekoration, Listener, OldTerrainGen, Populate, DUNGEON, ICE, LAKE, LAVA (+1 more)

### Community 28 - "StructureComponent"
Cohesion: 0.09
Nodes (6): ComponentStronghold, ComponentStrongholdRightTurn, ComponentStrongholdStairs2, StructureComponent, StructureStrongholdPieces, StructureStrongholdPieceWeight

### Community 30 - ".getComponentType"
Cohesion: 0.10
Nodes (6): ComponentMineshaftCorridor, ComponentMineshaftCross, ComponentMineshaftRoom, ComponentVillagePathGen, ComponentVillageRoadPiece, StructureMineshaftPieces

### Community 31 - "minecraft-dev"
Cohesion: 0.50
Nodes (3): npx, minecraft-dev, @mcdxai/minecraft-dev-mcp

### Community 32 - "OldPlantGens"
Cohesion: 0.13
Nodes (8): Cactus, DeadBush, Flowers, OldPlantGens, Reed, TallGrass, Vines, Waterlily

### Community 39 - "OldGen"
Cohesion: 0.26
Nodes (5): Aufbau, OldDecorator, OldDungeonGen, OldGen, Pumpkin

### Community 41 - "Weltgenerierung eines 1.5.2-Mods in worldgen152 einhängen"
Cohesion: 0.17
Nodes (9): Biome und Dekoratoren, Blöcke anmelden, Generator anmelden, Prüfen, Regel 1: Kern-Klassen ohne Spielklassen, Stolperfallen, Strukturen, Truhen und Spawner (+1 more)

### Community 42 - ".generate"
Cohesion: 0.21
Nodes (4): DesertWell, Lake, OldLakeGens, Spring

### Community 43 - ".generate"
Cohesion: 0.20
Nodes (4): Clay, Minable, OldOreGens, Sand

## Knowledge Gaps
- **58 isolated node(s):** `AIR`, `GRASS`, `GROUND`, `WOOD`, `ROCK` (+53 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 304 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **28 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `OldWorld` connect `OldWorld` to `OldPlantGens`, `StructureBoundingBox`, `OldBiome`, `.column`, `OldGen`, `OldRails`, `.generate`, `.generate`, `.getBlockId`, `OldBlockInfo`, `BigTree`, `Decorate`, `OldStructures`, `OldTerrainGen`?**
  _High betweenness centrality (0.283) - this node is a cross-community bridge._
- **What connects `AIR`, `GRASS`, `GROUND` to the rest of the system?**
  _58 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `StructureBoundingBox` be split into smaller, more focused modules?**
  _Cohesion score 0.053904873752201994 - nodes in this community are weakly interconnected._
- **Why does `OldBiome` connect `OldBiome` to `Layer`, `OldCarver`, `.tunnel`, `OldGen`, `OldTerrain`, `OldChunkGenerator.java`, `OldBlockInfo`, `GrassChooser`, `Decorate`?**
  _High betweenness centrality (0.127) - this node is a cross-community bridge._
- **Should `Layer` be split into smaller, more focused modules?**
  _Cohesion score 0.08831306187202538 - nodes in this community are weakly interconnected._
- **Why does `OldStructures` connect `OldStructures` to `OldChunkGenerator.java`, `.buildComponent`, `OldBlockInfo`, `ComponentVillageStartPiece`, `OldWorld`?**
  _High betweenness centrality (0.069) - this node is a cross-community bridge._
- **Should `reference.py` be split into smaller, more focused modules?**
  _Cohesion score 0.06346153846153846 - nodes in this community are weakly interconnected._