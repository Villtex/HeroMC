# Graph Report - HeroMC  (2026-11-14)

## Corpus Check
- 194 files · ~90,526 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 9 file(s) not represented in the graph (top: .properties 5, (none) 1, .toml 1)

## Summary
- 1290 nodes · 3994 edges · 65 communities (28 shown, 37 thin omitted)
- Extraction: 90% EXTRACTED · 10% INFERRED · 0% AMBIGUOUS · INFERRED: 389 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `baae3318`
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
- OldWorld
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
- .getBlockIdAtCurrentPosition
- minecraft-dev
- OldGen
- OldCarver
- .tunnel
- OldBiomeSource.java
- OldDecoration
- .decorate
- OldRails
- Weltgenerierung eines 1.5.2-Mods in worldgen152 einhängen
- .generate
- .generate
- biomes.py
- PerlinNoise
- StructurePieceBlockSelector
- Expect.java
- .func_74919_a
- .func_74898_a
- .findIntersecting
- BiomeLayout
- OldStack
- compare_original.py
- EbxlBlocks.java
- verify_blocks.py
- Ore
- OldBlockStates
- Listener
- ComponentStrongholdPortalRoom
- StructureStrongholdPieceWeight
- original_server.sh

## God Nodes (most connected - your core abstractions)
1. `OldWorld` - 209 edges
2. `StructureBoundingBox` - 111 edges
3. `OldBlockInfo` - 74 edges
4. `StructureComponent` - 67 edges
5. `OldEntity` - 65 edges
6. `OldLoot` - 64 edges
7. `OldTile` - 63 edges
8. `OldStructures` - 44 edges
9. `OldBiome` - 44 edges
10. `ComponentVillageStartPiece` - 43 edges

## Surprising Connections (you probably didn't know these)
- `Aufbau` --references--> `OldDungeonGen`  [INFERRED]
  .claude/skills/worldgen152/SKILL.md → mods/worldgen152/src/main/java/hexxit/worldgen152/core/populate/OldDungeonGen.java
- `Aufbau` --references--> `OldDecorator`  [INFERRED]
  .claude/skills/worldgen152/SKILL.md → mods/worldgen152/src/main/java/hexxit/worldgen152/core/populate/OldDecorator.java
- `Aufbau` --references--> `OldGen`  [INFERRED]
  .claude/skills/worldgen152/SKILL.md → mods/worldgen152/src/main/java/hexxit/worldgen152/core/populate/OldGen.java
- `Teil 1 – Vanilla-1.5.2-Dekoration` --references--> `OldDecorator`  [INFERRED]
  docs/PLAN.md → mods/worldgen152/src/main/java/hexxit/worldgen152/core/populate/OldDecorator.java
- `Aufbau` --references--> `WorldGenerator`  [INFERRED]
  .claude/skills/worldgen152/SKILL.md → mods/worldgen152/src/main/java/hexxit/worldgen152/core/populate/OldTerrainGen.java

## Import Cycles
- None detected.

## Communities (65 total, 37 thin omitted)

### Community 0 - "StructureBoundingBox"
Cohesion: 0.12
Nodes (4): Spawner, ComponentMineshaftCorridor, ComponentStrongholdCorridor, StructureBoundingBox

### Community 1 - "Layer"
Cohesion: 0.08
Nodes (18): Layer, AddIsland, AddMushroomIsland, AddSnow, Biome, FuzzyZoom, Hills, Island (+10 more)

### Community 2 - "reference.py"
Cohesion: 0.17
Nodes (10): build_mappings(), digest(), download(), inner_archives(), java(), main(), minecraft_reference(), process() (+2 more)

### Community 3 - "WorldGen152.java"
Cohesion: 0.11
Nodes (3): Extrabiomes, RFTL, WorldGen152

### Community 4 - "OldBiome"
Cohesion: 0.13
Nodes (6): EbxlBiomes, Decoration, GrassChooser, OldBiome, TreeChooser, OldBiomes

### Community 7 - "OldTerrain"
Cohesion: 0.19
Nodes (3): OctaveNoise, OldChunk, OldTerrain

### Community 8 - "Arbeitsplan: Hexxit 1.0.11 → NeoForge 26.3"
Cohesion: 0.06
Nodes (32): Arbeitsplan: Hexxit 1.0.11 → NeoForge 26.3, Regeln für jeden Teil, Teil 10 und 11 – Project Zulu, Teil 12 und 13 – Better Dungeons (Chocolate Quest), Teil 14 – Battle Towers, Ruins, Teil 15 – Dimensional Doors, Teil 16 – BiblioCraft, Teil 17 – BetterStorage, Backpacks, qCraft (+24 more)

### Community 11 - "HeroMC – Hinweise für Claude"
Cohesion: 0.40
Nodes (4): Arbeitsweise beim Portieren, graphify, HeroMC – Hinweise für Claude, Tokens sparen

### Community 18 - "OldBlockInfo"
Cohesion: 0.16
Nodes (7): OldBlockInfo, OldEntity, Villager, Witch, OldLoot, OldPopulator, OldTile

### Community 21 - "Material"
Cohesion: 0.06
Nodes (27): Material, AIR, CACTUS, CIRCUITS, CLAY, CLOTH, CRAFTED_SNOW, FIRE (+19 more)

### Community 22 - "ComponentVillageStartPiece"
Cohesion: 0.09
Nodes (12): ComponentVillage, ComponentVillageHouse2, ComponentVillageHouse4_Garden, ComponentVillagePathGen, ComponentVillageRoadPiece, ComponentVillageStartPiece, ComponentVillageTorch, ComponentVillageWell (+4 more)

### Community 24 - "Decorate"
Cohesion: 0.13
Nodes (15): Decorate, BIG_SHROOM, CACTUS, CLAY, DEAD_BUSH, FLOWERS, GRASS, LAKE (+7 more)

### Community 25 - "OldStructures"
Cohesion: 0.06
Nodes (10): OldBlocks, TerrainSource, Found, OldStructures, VillageBlocks, StructureMineshaftStart, StructureScatteredFeatureStart, StructureStart (+2 more)

### Community 26 - ".findIntersecting"
Cohesion: 0.11
Nodes (16): ComponentStronghold, ComponentStrongholdChestCorridor, ComponentStrongholdCrossing, ComponentStrongholdLeftTurn, ComponentStrongholdLibrary, ComponentStrongholdPrison, ComponentStrongholdRoomCrossing, ComponentStrongholdStairs (+8 more)

### Community 27 - "OldTerrainGen"
Cohesion: 0.17
Nodes (9): Aufbau, Teil 1 – Vanilla-1.5.2-Dekoration, OldTerrainGen, Populate, DUNGEON, ICE, LAKE, LAVA (+1 more)

### Community 28 - "StructureComponent"
Cohesion: 0.08
Nodes (6): ComponentMineshaftCross, ComponentMineshaftRoom, ComponentStrongholdRightTurn, ComponentStrongholdStairs2, StructureComponent, StructureMineshaftPieces

### Community 31 - "minecraft-dev"
Cohesion: 0.50
Nodes (3): npx, minecraft-dev, @mcdxai/minecraft-dev-mcp

### Community 32 - "OldGen"
Cohesion: 0.11
Nodes (19): OldGen, Cactus, DeadBush, Flowers, OldPlantGens, Pumpkin, Reed, TallGrass (+11 more)

### Community 41 - "Weltgenerierung eines 1.5.2-Mods in worldgen152 einhängen"
Cohesion: 0.22
Nodes (8): Biome und Dekoratoren, Blöcke anmelden, Generator anmelden, Prüfen, Regel 1: Kern-Klassen ohne Spielklassen, Stolperfallen, Strukturen, Weltgenerierung eines 1.5.2-Mods in worldgen152 einhängen

### Community 42 - ".generate"
Cohesion: 0.25
Nodes (4): DesertWell, Lake, OldLakeGens, Spring

### Community 43 - ".generate"
Cohesion: 0.25
Nodes (4): Clay, Minable, OldOreGens, Sand

### Community 44 - "biomes.py"
Cohesion: 0.14
Nodes (9): ColorMaps, f32(), fixed(), hexcolor(), main(), multiply(), sky_color(), tinted() (+1 more)

### Community 46 - "StructurePieceBlockSelector"
Cohesion: 0.09
Nodes (7): ComponentScatteredFeature, ComponentScatteredFeatureDesertPyramid, ComponentScatteredFeatureJunglePyramid, ComponentScatteredFeatureSwampHut, StructurePieceBlockSelector, StructureScatteredFeatureStones, StructureStrongholdStones

### Community 50 - ".findIntersecting"
Cohesion: 0.14
Nodes (5): ComponentMineshaftStairs, ComponentVillageField2, ComponentVillageField, ComponentVillageHall, ComponentVillageHouse3

### Community 52 - "OldStack"
Cohesion: 0.13
Nodes (6): OldDungeonGen, MinecartChest, Category, OldStack, Chest, Dispenser

### Community 53 - "compare_original.py"
Cohesion: 0.18
Nodes (7): cells(), main(), read_nbt(), payload(), string(), take(), region_chunks()

### Community 55 - "verify_blocks.py"
Cohesion: 0.23
Nodes (7): main(), packet(), read(), block(), flag(), main(), test()

### Community 56 - "Ore"
Cohesion: 0.15
Nodes (9): Ore, COAL, DIAMOND, DIRT, GOLD, GRAVEL, IRON, LAPIS (+1 more)

## Knowledge Gaps
- **58 isolated node(s):** `AIR`, `CACTUS`, `CIRCUITS`, `CLAY`, `CLOTH` (+53 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 312 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **37 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `OldWorld` connect `OldWorld` to `StructureBoundingBox`, `OldBiome`, `OldDecoration`, `.decorate`, `OldRails`, `OldTerrainGen`, `OldBlockInfo`, `BigTree`, `Ore`, `OldStructures`, `Listener`, `.getBlockIdAtCurrentPosition`?**
  _High betweenness centrality (0.256) - this node is a cross-community bridge._
- **What connects `AIR`, `CACTUS`, `CIRCUITS` to the rest of the system?**
  _58 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `StructureBoundingBox` be split into smaller, more focused modules?**
  _Cohesion score 0.1244343891402715 - nodes in this community are weakly interconnected._
- **Why does `OldBiome` connect `OldBiome` to `OldGen`, `Layer`, `OldCarver`, `.tunnel`, `OldBiomeSource.java`, `.decorate`, `OldTerrain`, `OldBlockInfo`, `BiomeLayout`, `Ore`, `.toString`?**
  _High betweenness centrality (0.113) - this node is a cross-community bridge._
- **Should `Layer` be split into smaller, more focused modules?**
  _Cohesion score 0.08432539682539683 - nodes in this community are weakly interconnected._
- **Why does `OldStructures` connect `OldStructures` to `OldChunkGenerator.java`, `OldWorld`, `OldBlockInfo`, `BiomeLayout`, `ComponentVillageStartPiece`?**
  _High betweenness centrality (0.086) - this node is a cross-community bridge._
- **Should `WorldGen152.java` be split into smaller, more focused modules?**
  _Cohesion score 0.1111111111111111 - nodes in this community are weakly interconnected._