# Graph Report - HeroMC  (2026-11-14)

## Corpus Check
- 522 files · ~113,583 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 9 file(s) not represented in the graph (top: .properties 5, (none) 1, .toml 1)

## Summary
- 1625 nodes · 5012 edges · 80 communities (35 shown, 45 thin omitted)
- Extraction: 90% EXTRACTED · 10% INFERRED · 0% AMBIGUOUS · INFERRED: 500 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `3c6717d9`
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
- OldChunkGenerator
- HeroMC – Hinweise für Claude
- .claude/CLAUDE.md
- OldWorld
- OldBlockInfo
- OldDecoratedChunk
- OldGameIds.java
- Material
- StructureComponent
- BigTree
- Decorate
- OldStructures
- .findIntersecting
- OldTerrainGen
- net.minecraft.world.level.block.state.BlockState
- .getBlockIdAtCurrentPosition
- minecraft-dev
- OldGen
- OldLiveGen.java
- OldRavines
- .tunnel
- OldChunkWriter.java
- OldBiomeSource.java
- OldDecoration
- EbxlWorldGen
- OldRails
- Weltgenerierung eines 1.5.2-Mods in worldgen152 einhängen
- .generate
- .setBlock
- biomes.py
- PerlinNoise
- StructurePieceBlockSelector
- Expect.java
- hexxit.worldgen152.core.populate.OldGen
- .getBlockId
- OldTreeGens
- BiomeLayout
- OldStack
- compare_original.py
- EbxlBlocks.java
- verify_blocks.py
- Ore
- OldBlockStates
- EbxlLeavesBlock.java
- Listener
- EbxlBlocks
- ScarecrowEntity
- EbxlClient.java
- original_server.sh
- QuarterLogBlock
- ScarecrowRenderer
- .func_74992_a
- ScarecrowModel.java
- EbxlJeiPlugin.java
- StructurePieceBlockSelector
- .generate
- Populate
- JEI-Plugin für einen portierten Mod
- Category
- VillageHandler
- Acuteness

## God Nodes (most connected - your core abstractions)
1. `OldWorld` - 268 edges
2. `StructureBoundingBox` - 111 edges
3. `OldBlockInfo` - 73 edges
4. `StructureComponent` - 67 edges
5. `OldEntity` - 65 edges
6. `OldLoot` - 64 edges
7. `OldTile` - 63 edges
8. `EbxlBlocks` - 46 edges
9. `OldBiome` - 44 edges
10. `ComponentVillageStartPiece` - 43 edges

## Surprising Connections (you probably didn't know these)
- `Bäume im Spiel (Setzlinge)` --references--> `OldGen`  [INFERRED]
  .claude/skills/worldgen152/SKILL.md → mods/worldgen152/src/main/java/hexxit/worldgen152/core/populate/OldGen.java
- `Aufbau` --references--> `OldDungeonGen`  [INFERRED]
  .claude/skills/worldgen152/SKILL.md → mods/worldgen152/src/main/java/hexxit/worldgen152/core/populate/OldDungeonGen.java
- `Aufbau` --references--> `WorldGenerator`  [INFERRED]
  .claude/skills/worldgen152/SKILL.md → mods/worldgen152/src/main/java/hexxit/worldgen152/core/populate/OldTerrainGen.java
- `Teil 1 – Vanilla-1.5.2-Dekoration` --references--> `OldDecorator`  [INFERRED]
  docs/PLAN.md → mods/worldgen152/src/main/java/hexxit/worldgen152/core/populate/OldDecorator.java
- `Teil 12 und 13 – Better Dungeons (Chocolate Quest)` --references--> `VillageHandler`  [INFERRED]
  docs/PLAN.md → mods/worldgen152/src/main/java/hexxit/worldgen152/core/structure/OldStructures.java

## Import Cycles
- None detected.

## Communities (80 total, 45 thin omitted)

### Community 0 - "StructureBoundingBox"
Cohesion: 0.14
Nodes (5): Witch, Entry, Spawner, ComponentMineshaftRoom, StructureBoundingBox

### Community 1 - "Layer"
Cohesion: 0.09
Nodes (18): Layer, AddIsland, AddMushroomIsland, AddSnow, Biome, FuzzyZoom, Hills, Island (+10 more)

### Community 2 - "reference.py"
Cohesion: 0.17
Nodes (10): build_mappings(), digest(), download(), inner_archives(), java(), main(), minecraft_reference(), process() (+2 more)

### Community 4 - "OldBiome"
Cohesion: 0.13
Nodes (5): EbxlBiomes, Decoration, GrassChooser, OldBiome, OldBiomes

### Community 7 - "OldTerrain"
Cohesion: 0.19
Nodes (3): OctaveNoise, OldChunk, OldTerrain

### Community 8 - "Arbeitsplan: Hexxit 1.0.11 → NeoForge 26.3"
Cohesion: 0.06
Nodes (31): Arbeitsplan: Hexxit 1.0.11 → NeoForge 26.3, Regeln für jeden Teil, Teil 10 und 11 – Project Zulu, Teil 14 – Battle Towers, Ruins, Teil 15 – Dimensional Doors, Teil 16 – BiblioCraft, Teil 17 – BetterStorage, Backpacks, qCraft, Teil 18 – ChocoCraft, Infernal Mobs (+23 more)

### Community 11 - "HeroMC – Hinweise für Claude"
Cohesion: 0.33
Nodes (5): Arbeitsweise beim Portieren, graphify, HeroMC – Hinweise für Claude, Skills anlegen, Tokens sparen

### Community 18 - "OldBlockInfo"
Cohesion: 0.15
Nodes (7): OldBlocks, OldBlockInfo, OldEntity, OldLoot, OldOreGens, OldPopulator, OldTile

### Community 21 - "Material"
Cohesion: 0.07
Nodes (27): Material, AIR, CACTUS, CIRCUITS, CLAY, CLOTH, CRAFTED_SNOW, FIRE (+19 more)

### Community 22 - "StructureComponent"
Cohesion: 0.06
Nodes (22): ComponentMineshaftCorridor, ComponentMineshaftCross, ComponentMineshaftStairs, ComponentVillage, ComponentVillageChurch, ComponentVillageField2, ComponentVillageField, ComponentVillageHall (+14 more)

### Community 24 - "Decorate"
Cohesion: 0.13
Nodes (15): Decorate, BIG_SHROOM, CACTUS, CLAY, DEAD_BUSH, FLOWERS, GRASS, LAKE (+7 more)

### Community 25 - "OldStructures"
Cohesion: 0.07
Nodes (9): TerrainSource, Found, OldStructures, VillageBlocks, StructureMineshaftStart, StructureScatteredFeatureStart, StructureStart, StructureStrongholdStart (+1 more)

### Community 26 - ".findIntersecting"
Cohesion: 0.05
Nodes (20): ComponentStronghold, ComponentStrongholdChestCorridor, ComponentStrongholdCrossing, ComponentStrongholdLeftTurn, ComponentStrongholdLibrary, ComponentStrongholdPortalRoom, ComponentStrongholdPrison, ComponentStrongholdRightTurn (+12 more)

### Community 27 - "OldTerrainGen"
Cohesion: 0.36
Nodes (3): Teil 1 – Vanilla-1.5.2-Dekoration, OldTerrainGen, WorldGenerator

### Community 28 - "net.minecraft.world.level.block.state.BlockState"
Cohesion: 0.12
Nodes (4): CattailBlock, EbxlPlantBlock, EbxlSaplingBlock, QuicksandBlock

### Community 31 - "minecraft-dev"
Cohesion: 0.50
Nodes (3): npx, minecraft-dev, @mcdxai/minecraft-dev-mcp

### Community 32 - "OldGen"
Cohesion: 0.09
Nodes (13): Aufbau, TreeChooser, OldDecorator, OldGen, Cactus, DeadBush, Flowers, OldPlantGens (+5 more)

### Community 35 - ".tunnel"
Cohesion: 0.14
Nodes (3): OldCarver, OldCaves, OldMath

### Community 39 - "EbxlWorldGen"
Cohesion: 0.16
Nodes (4): Choices, EbxlWorldGen, Jungle, Weighted

### Community 41 - "Weltgenerierung eines 1.5.2-Mods in worldgen152 einhängen"
Cohesion: 0.20
Nodes (9): Biome und Dekoratoren, Blöcke anmelden, Bäume im Spiel (Setzlinge), Generator anmelden, Prüfen, Regel 1: Kern-Klassen ohne Spielklassen, Stolperfallen, Strukturen (+1 more)

### Community 42 - ".generate"
Cohesion: 0.21
Nodes (4): DesertWell, Lake, OldLakeGens, Spring

### Community 43 - ".setBlock"
Cohesion: 0.16
Nodes (8): Bend, LEFT, RIGHT, STRAIGHT, LegendOak, Clay, Minable, Sand

### Community 44 - "biomes.py"
Cohesion: 0.14
Nodes (9): ColorMaps, f32(), fixed(), hexcolor(), main(), multiply(), sky_color(), tinted() (+1 more)

### Community 46 - "StructurePieceBlockSelector"
Cohesion: 0.15
Nodes (5): ComponentScatteredFeature, ComponentScatteredFeatureDesertPyramid, ComponentScatteredFeatureJunglePyramid, ComponentScatteredFeatureSwampHut, StructureScatteredFeatureStones

### Community 48 - "hexxit.worldgen152.core.populate.OldGen"
Cohesion: 0.10
Nodes (15): CatTail, EbxlPlantGens, Flowers, LeafPile, Marsh, Melon, Oasis, Quicksand (+7 more)

### Community 49 - ".getBlockId"
Cohesion: 0.15
Nodes (4): Autumn, BigAutumn, CustomSwamp, EbxlTreeGens

### Community 50 - "OldTreeGens"
Cohesion: 0.13
Nodes (9): BigMushroom, Birch, Huge, OldTreeGens, Pine, Shrub, Spruce, Swamp (+1 more)

### Community 52 - "OldStack"
Cohesion: 0.22
Nodes (4): MinecartChest, OldStack, Chest, Dispenser

### Community 53 - "compare_original.py"
Cohesion: 0.18
Nodes (7): cells(), main(), read_nbt(), payload(), string(), take(), region_chunks()

### Community 55 - "verify_blocks.py"
Cohesion: 0.22
Nodes (8): main(), packet(), read(), block(), ebxl(), flag(), main(), test()

### Community 56 - "Ore"
Cohesion: 0.20
Nodes (9): Ore, COAL, DIAMOND, DIRT, GOLD, GRAVEL, IRON, LAPIS (+1 more)

### Community 65 - "QuarterLogBlock"
Cohesion: 0.16
Nodes (6): Bark, NE, NW, SE, SW, QuarterLogBlock

### Community 73 - "Populate"
Cohesion: 0.33
Nodes (5): Populate, DUNGEON, ICE, LAKE, LAVA

### Community 74 - "JEI-Plugin für einen portierten Mod"
Cohesion: 0.40
Nodes (4): Einbindung (schon erledigt, in `build.gradle`), JEI-Plugin für einen portierten Mod, Prüfen, Vorlage

### Community 77 - "Acuteness"
Cohesion: 0.50
Nodes (4): Acuteness, LOOSE, TIGHT, TIGHTER

## Knowledge Gaps
- **68 isolated node(s):** `NW`, `NE`, `SW`, `SE`, `LEFT` (+63 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 368 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **45 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `OldWorld` connect `OldWorld` to `StructureBoundingBox`, `OldBiome`, `Arbeitsplan: Hexxit 1.0.11 → NeoForge 26.3`, `OldBlockInfo`, `BigTree`, `OldStructures`, `OldTerrainGen`, `.getBlockIdAtCurrentPosition`, `OldGen`, `OldLiveGen.java`, `EbxlWorldGen`, `OldRails`, `.generate`, `.setBlock`, `hexxit.worldgen152.core.populate.OldGen`, `.getBlockId`, `OldTreeGens`, `Ore`, `Listener`, `.func_74992_a`, `.generate`, `Populate`?**
  _High betweenness centrality (0.338) - this node is a cross-community bridge._
- **What connects `NW`, `NE`, `SW` to the rest of the system?**
  _68 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `StructureBoundingBox` be split into smaller, more focused modules?**
  _Cohesion score 0.14085330216247807 - nodes in this community are weakly interconnected._
- **Why does `OldBiome` connect `OldBiome` to `OldGen`, `Layer`, `OldRavines`, `.tunnel`, `OldBiomeSource.java`, `OldTerrain`, `OldDecorator.java`, `OldBlockInfo`, `BiomeLayout`, `OldChunkGenerator.java`?**
  _High betweenness centrality (0.084) - this node is a cross-community bridge._
- **Should `Layer` be split into smaller, more focused modules?**
  _Cohesion score 0.08831306187202538 - nodes in this community are weakly interconnected._
- **Why does `StructureBoundingBox` connect `StructureBoundingBox` to `.func_74992_a`, `OldWorld`, `OldBlockInfo`, `StructureComponent`, `OldStructures`, `.findIntersecting`, `.getBlockIdAtCurrentPosition`?**
  _High betweenness centrality (0.047) - this node is a cross-community bridge._
- **Should `OldBiome` be split into smaller, more focused modules?**
  _Cohesion score 0.12923076923076923 - nodes in this community are weakly interconnected._