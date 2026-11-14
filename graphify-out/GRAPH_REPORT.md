# Graph Report - agent-a560461f7acc57ffa  (2026-11-14)

## Corpus Check
- 687 files · ~130,397 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 17 file(s) not represented in the graph (top: .properties 12, (none) 2, .toml 1)

## Summary
- 1988 nodes · 5993 edges · 83 communities (42 shown, 41 thin omitted)
- Extraction: 92% EXTRACTED · 8% INFERRED · 0% AMBIGUOUS · INFERRED: 507 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `abca46a1`
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
- StructureComponent
- BigTree
- Decorate
- OldStructures
- .findIntersecting
- OldGen
- EbxlSaplingBlock.java
- ArmorBarHud
- minecraft-dev
- .column
- BackToolsClient.java
- net.minecraft.world.item.ItemStack
- .tunnel
- OldBiomeSource.java
- OldDecoration
- FrostArrow.java
- OldRails
- Weltgenerierung eines 1.5.2-Mods in worldgen152 einhängen
- net.neoforged.bus.api.IEventBus
- ArmorBarHud.java
- biomes.py
- PerlinNoise
- .addComponentParts
- OldStack
- hexxit.worldgen152.core.populate.OldGen
- .getBlockId
- .setBlock
- ChestTransporter.java
- SoulCageBlockEntity.java
- compare_original.py
- net.minecraft.world.item.Item
- verify_blocks.py
- OldLiveGen
- OldBlockStates
- EbxlLeavesBlock.java
- EbxlBlocks
- SoulEvents.java
- ScarecrowEntity
- EbxlClient.java
- original_server.sh
- QuarterLogBlock
- net.minecraft.world.item.ItemStack
- BackItemLayer.java
- StructureComponent
- net.minecraft.core.BlockPos
- net.minecraft.resources.Identifier
- .fillWithRandomizedBlocks
- OldWorld
- JEI-Plugin für einen portierten Mod
- TransportEvents.java
- SoulCageBlock.java
- LegendOak
- .updateBoundingBox
- SoulShards.java
- .StructureVillageStart
- Client-Darstellung in 26.3 (NeoForge 26.3.0.102)
- PlayerMobPlateBlock.java

## God Nodes (most connected - your core abstractions)
1. `OldWorld` - 251 edges
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
- `Aufbau` --references--> `WorldGenerator`  [INFERRED]
  .claude/skills/worldgen152/SKILL.md → mods/worldgen152/src/main/java/hexxit/worldgen152/core/populate/OldTerrainGen.java
- `Teil 1 – Vanilla-1.5.2-Dekoration` --references--> `OldDecorator`  [INFERRED]
  docs/PLAN.md → mods/worldgen152/src/main/java/hexxit/worldgen152/core/populate/OldDecorator.java
- `Teil 12 und 13 – Better Dungeons (Chocolate Quest)` --references--> `VillageHandler`  [INFERRED]
  docs/PLAN.md → mods/worldgen152/src/main/java/hexxit/worldgen152/core/structure/OldStructures.java
- `Aufbau` --references--> `OldDungeonGen`  [INFERRED]
  .claude/skills/worldgen152/SKILL.md → mods/worldgen152/src/main/java/hexxit/worldgen152/core/populate/OldDungeonGen.java

## Import Cycles
- None detected.

## Communities (83 total, 41 thin omitted)

### Community 0 - "StructureBoundingBox"
Cohesion: 0.13
Nodes (4): Villager, Witch, Spawner, StructureBoundingBox

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
Nodes (3): BiomeLayout, OctaveNoise, OldTerrain

### Community 8 - "Arbeitsplan: Hexxit 1.0.11 → NeoForge 26.3"
Cohesion: 0.06
Nodes (30): Arbeitsplan: Hexxit 1.0.11 → NeoForge 26.3, Regeln für jeden Teil, Teil 10 und 11 – Project Zulu, Teil 14 – Battle Towers, Ruins, Teil 15 – Dimensional Doors, Teil 16 – BiblioCraft, Teil 17 – BetterStorage, Backpacks, qCraft, Teil 18 – ChocoCraft, Infernal Mobs (+22 more)

### Community 11 - "HeroMC – Hinweise für Claude"
Cohesion: 0.29
Nodes (6): Arbeitsweise beim Portieren, Branches, graphify, HeroMC – Hinweise für Claude, Skills anlegen, Tokens sparen

### Community 17 - ".getBlockId"
Cohesion: 0.10
Nodes (3): Clay, Minable, Sand

### Community 18 - "OldBlockInfo"
Cohesion: 0.13
Nodes (8): OldBlocks, OldBlockInfo, OldEntity, OldLakeGens, OldLoot, OldOreGens, OldPopulator, OldTile

### Community 19 - "OldDecoratedChunk"
Cohesion: 0.11
Nodes (4): OldChunk, OldDecoratedChunk, OldChunkWriter, Expect

### Community 21 - "Material"
Cohesion: 0.06
Nodes (32): Choices, EbxlWorldGen, Jungle, Weighted, Material, AIR, CACTUS, CIRCUITS (+24 more)

### Community 22 - "StructureComponent"
Cohesion: 0.07
Nodes (19): Teil 12 und 13 – Better Dungeons (Chocolate Quest), ComponentVillage, ComponentVillageChurch, ComponentVillageField2, ComponentVillageField, ComponentVillageHall, ComponentVillageHouse1, ComponentVillageHouse2 (+11 more)

### Community 24 - "Decorate"
Cohesion: 0.07
Nodes (24): Decorate, BIG_SHROOM, CACTUS, CLAY, DEAD_BUSH, FLOWERS, GRASS, LAKE (+16 more)

### Community 25 - "OldStructures"
Cohesion: 0.10
Nodes (7): TerrainSource, Found, OldStructures, VillageBlocks, StructureMineshaftStart, StructureScatteredFeatureStart, StructureStart

### Community 26 - ".findIntersecting"
Cohesion: 0.08
Nodes (18): ComponentStronghold, ComponentStrongholdChestCorridor, ComponentStrongholdCrossing, ComponentStrongholdLeftTurn, ComponentStrongholdLibrary, ComponentStrongholdPortalRoom, ComponentStrongholdPrison, ComponentStrongholdRoomCrossing (+10 more)

### Community 27 - "OldGen"
Cohesion: 0.09
Nodes (14): Aufbau, OldDecorator, OldGen, Lake, Spring, Cactus, DeadBush, Flowers (+6 more)

### Community 30 - "ArmorBarHud"
Cohesion: 0.13
Nodes (10): Kind, AMOUNT, CHEST, DURABILITY, FEET, HEAD, LEGS, Unit (+2 more)

### Community 31 - "minecraft-dev"
Cohesion: 0.50
Nodes (3): npx, minecraft-dev, @mcdxai/minecraft-dev-mcp

### Community 33 - "BackToolsClient.java"
Cohesion: 0.14
Nodes (3): Back, BackItem, BackToolsClient

### Community 34 - "net.minecraft.world.item.ItemStack"
Cohesion: 0.13
Nodes (7): Kind, ENDER, FLAME, FROST, MULTI, PLAIN, MoreBowItem

### Community 35 - ".tunnel"
Cohesion: 0.11
Nodes (4): OldCarver, OldCaves, OldMath, OldRavines

### Community 41 - "Weltgenerierung eines 1.5.2-Mods in worldgen152 einhängen"
Cohesion: 0.15
Nodes (10): Biome und Dekoratoren, Blöcke anmelden, Bäume im Spiel (Setzlinge), Generator anmelden, Prüfen, Regel 1: Kern-Klassen ohne Spielklassen, Stolperfallen, Strukturen (+2 more)

### Community 42 - "net.neoforged.bus.api.IEventBus"
Cohesion: 0.19
Nodes (4): ArmorBarMod, BackTools, BuffBarMod, EbxlIds

### Community 44 - "biomes.py"
Cohesion: 0.14
Nodes (9): ColorMaps, f32(), fixed(), hexcolor(), main(), multiply(), sky_color(), tinted() (+1 more)

### Community 46 - ".addComponentParts"
Cohesion: 0.09
Nodes (7): Category, Entry, ComponentScatteredFeature, ComponentScatteredFeatureDesertPyramid, ComponentScatteredFeatureJunglePyramid, ComponentScatteredFeatureSwampHut, StructureScatteredFeatureStones

### Community 47 - "OldStack"
Cohesion: 0.22
Nodes (4): MinecartChest, OldStack, Chest, Dispenser

### Community 48 - "hexxit.worldgen152.core.populate.OldGen"
Cohesion: 0.10
Nodes (11): CatTail, EbxlPlantGens, Flowers, LeafPile, Marsh, Melon, Oasis, Quicksand (+3 more)

### Community 49 - ".getBlockId"
Cohesion: 0.16
Nodes (7): Acacia, Autumn, CustomSwamp, EbxlTreeGens, Fir, FirHuge, Redwood

### Community 50 - ".setBlock"
Cohesion: 0.14
Nodes (9): BigMushroom, Birch, Huge, OldTreeGens, Pine, Shrub, Spruce, Swamp (+1 more)

### Community 53 - "compare_original.py"
Cohesion: 0.18
Nodes (7): cells(), main(), read_nbt(), payload(), string(), take(), region_chunks()

### Community 54 - "net.minecraft.world.item.Item"
Cohesion: 0.10
Nodes (3): LogTurnerItem, ScarecrowItem, SoulShardItem

### Community 55 - "verify_blocks.py"
Cohesion: 0.22
Nodes (8): main(), packet(), read(), block(), ebxl(), flag(), main(), test()

### Community 60 - "EbxlBlocks"
Cohesion: 0.11
Nodes (4): LeafPileBlock, QuarterLogBlock, QuicksandBlock, EbxlBlocks

### Community 65 - "QuarterLogBlock"
Cohesion: 0.13
Nodes (5): Bark, NE, NW, SE, SW

### Community 68 - "StructureComponent"
Cohesion: 0.07
Nodes (8): ComponentMineshaftCorridor, ComponentMineshaftCross, ComponentMineshaftRoom, ComponentMineshaftStairs, ComponentStrongholdCorridor, ComponentStrongholdStairs2, StructureComponent, StructureMineshaftPieces

### Community 70 - "net.minecraft.resources.Identifier"
Cohesion: 0.06
Nodes (9): ChestTransporterJeiPlugin, ScarecrowModel, ScarecrowRenderer, EbxlJeiPlugin, FrostArrowRenderer, MoreBowsClient, MoreBowsJeiPlugin, ObsidiPlatesJeiPlugin (+1 more)

### Community 71 - ".fillWithRandomizedBlocks"
Cohesion: 0.10
Nodes (3): ComponentStrongholdRightTurn, StructurePieceBlockSelector, StructureStrongholdStones

### Community 72 - "OldWorld"
Cohesion: 0.08
Nodes (13): Teil 1 – Vanilla-1.5.2-Dekoration, TreeChooser, OldDungeonGen, Entry, Listener, OldTerrainGen, Populate, DUNGEON (+5 more)

### Community 74 - "JEI-Plugin für einen portierten Mod"
Cohesion: 0.40
Nodes (4): Einbindung (schon erledigt, in `build.gradle`), JEI-Plugin für einen portierten Mod, Prüfen, Vorlage

### Community 77 - "LegendOak"
Cohesion: 0.24
Nodes (9): Acuteness, LOOSE, TIGHT, TIGHTER, Bend, LEFT, RIGHT, STRAIGHT (+1 more)

### Community 79 - "SoulShards.java"
Cohesion: 0.08
Nodes (4): MoreBows, ObsidiPlates, Soul, SoulShards

### Community 84 - "Client-Darstellung in 26.3 (NeoForge 26.3.0.102)"
Cohesion: 0.25
Nodes (7): Client-Darstellung in 26.3 (NeoForge 26.3.0.102), Etwas am Spielermodell zeichnen (Rücken, Schultern), Halbdurchsichtige Blöcke (Alpha im Renderer), HUD (statt `ITickHandler` mit `TickType.RENDER`), Nur-Client-Mod sauber laden, Rechtsklick vor dem Block (Original: `PlayerInteractEvent` mit `RIGHT_CLICK_BLOCK`), Stolperfallen

## Knowledge Gaps
- **79 isolated node(s):** `PLAIN`, `FLAME`, `FROST`, `MULTI`, `ENDER` (+74 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 446 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **41 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `OldWorld` connect `OldWorld` to `StructureBoundingBox`, `OldBiome`, `.getBlockId`, `OldBlockInfo`, `Material`, `BigTree`, `Decorate`, `OldStructures`, `OldGen`, `.column`, `OldDecoration`, `OldRails`, `.addComponentParts`, `hexxit.worldgen152.core.populate.OldGen`, `.getBlockId`, `.setBlock`, `StructureComponent`, `.fillWithRandomizedBlocks`, `LegendOak`, `.StructureVillageStart`?**
  _High betweenness centrality (0.211) - this node is a cross-community bridge._
- **What connects `PLAIN`, `FLAME`, `FROST` to the rest of the system?**
  _79 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `StructureBoundingBox` be split into smaller, more focused modules?**
  _Cohesion score 0.12510974539069358 - nodes in this community are weakly interconnected._
- **Why does `OldBlockInfo` connect `OldBlockInfo` to `.column`, `OldChunkWriter.java`, `.getBlockId`, `.setBlock`, `.getBlockId`, `Material`, `OldGen`?**
  _High betweenness centrality (0.069) - this node is a cross-community bridge._
- **Should `Layer` be split into smaller, more focused modules?**
  _Cohesion score 0.08831306187202538 - nodes in this community are weakly interconnected._
- **Why does `OldBiome` connect `OldBiome` to `Layer`, `.tunnel`, `OldBiomeSource.java`, `OldDecoration`, `OldTerrain`, `OldWorld`, `OldBlockInfo`, `Decorate`, `OldGen`?**
  _High betweenness centrality (0.053) - this node is a cross-community bridge._
- **Should `OldBiome` be split into smaller, more focused modules?**
  _Cohesion score 0.12923076923076923 - nodes in this community are weakly interconnected._