# Graph Report - HeroMC  (2026-11-14)

## Corpus Check
- 631 files · ~126,365 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 15 file(s) not represented in the graph (top: .properties 11, (none) 1, .toml 1)

## Summary
- 1903 nodes · 5758 edges · 88 communities (42 shown, 46 thin omitted)
- Extraction: 91% EXTRACTED · 9% INFERRED · 0% AMBIGUOUS · INFERRED: 506 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `b17731fe`
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
- StructureComponent
- BigTree
- Decorate
- OldStructures
- .findIntersecting
- OldTerrainGen
- EbxlSaplingBlock.java
- ArmorBarHud.java
- minecraft-dev
- .column
- BackToolsClient.java
- OldRavines
- .tunnel
- OldBiomeSource.java
- OldDecoration
- EbxlWorldGen
- OldRails
- Weltgenerierung eines 1.5.2-Mods in worldgen152 einhängen
- net.neoforged.bus.api.IEventBus
- net.minecraft.world.entity.EntityType
- biomes.py
- PerlinNoise
- .addComponentParts
- Expect.java
- hexxit.worldgen152.core.populate.OldGen
- .getBlockId
- OldGen
- BiomeLayout
- SoulCageBlockEntity.java
- compare_original.py
- EbxlBlocks.java
- verify_blocks.py
- Ore
- OldBlockStates
- EbxlLeavesBlock.java
- Listener
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
- .populate
- Populate
- JEI-Plugin für einen portierten Mod
- net.minecraft.world.level.Level
- SoulCageBlock.java
- Acuteness
- .updateBoundingBox
- SoulShards.java
- HeroMC – Hexxit 1:1 für Minecraft 26.3
- .StructureVillageStart
- Client-Darstellung in 26.3 (NeoForge 26.3.0.102)
- PlayerMobPlateBlock.java
- Extrabiomes

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
- `Teil 1 – Vanilla-1.5.2-Dekoration` --references--> `OldDecorator`  [INFERRED]
  docs/PLAN.md → mods/worldgen152/src/main/java/hexxit/worldgen152/core/populate/OldDecorator.java
- `Teil 12 und 13 – Better Dungeons (Chocolate Quest)` --references--> `VillageHandler`  [INFERRED]
  docs/PLAN.md → mods/worldgen152/src/main/java/hexxit/worldgen152/core/structure/OldStructures.java
- `Aufbau` --references--> `OldDecorator`  [INFERRED]
  .claude/skills/worldgen152/SKILL.md → mods/worldgen152/src/main/java/hexxit/worldgen152/core/populate/OldDecorator.java
- `Aufbau` --references--> `OldGen`  [INFERRED]
  .claude/skills/worldgen152/SKILL.md → mods/worldgen152/src/main/java/hexxit/worldgen152/core/populate/OldGen.java

## Import Cycles
- None detected.

## Communities (88 total, 46 thin omitted)

### Community 0 - "StructureBoundingBox"
Cohesion: 0.13
Nodes (4): Villager, Witch, Spawner, StructureBoundingBox

### Community 1 - "Layer"
Cohesion: 0.08
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
Cohesion: 0.10
Nodes (21): Arbeitsplan: Hexxit 1.0.11 → NeoForge 26.3, Regeln für jeden Teil, Teil 10 und 11 – Project Zulu, Teil 14 – Battle Towers, Ruins, Teil 15 – Dimensional Doors, Teil 16 – BiblioCraft, Teil 17 – BetterStorage, Backpacks, qCraft, Teil 18 – ChocoCraft, Infernal Mobs (+13 more)

### Community 11 - "HeroMC – Hinweise für Claude"
Cohesion: 0.33
Nodes (5): Arbeitsweise beim Portieren, graphify, HeroMC – Hinweise für Claude, Skills anlegen, Tokens sparen

### Community 18 - "OldBlockInfo"
Cohesion: 0.12
Nodes (12): OldBlocks, OldBlockInfo, MinecartChest, OldEntity, OldLakeGens, OldLoot, OldOreGens, OldPopulator (+4 more)

### Community 21 - "Material"
Cohesion: 0.07
Nodes (27): Material, AIR, CACTUS, CIRCUITS, CLAY, CLOTH, CRAFTED_SNOW, FIRE (+19 more)

### Community 22 - "StructureComponent"
Cohesion: 0.07
Nodes (19): Teil 12 und 13 – Better Dungeons (Chocolate Quest), ComponentVillage, ComponentVillageChurch, ComponentVillageField2, ComponentVillageField, ComponentVillageHall, ComponentVillageHouse1, ComponentVillageHouse2 (+11 more)

### Community 24 - "Decorate"
Cohesion: 0.13
Nodes (15): Decorate, BIG_SHROOM, CACTUS, CLAY, DEAD_BUSH, FLOWERS, GRASS, LAKE (+7 more)

### Community 25 - "OldStructures"
Cohesion: 0.12
Nodes (6): Found, OldStructures, VillageBlocks, StructureMineshaftStart, StructureScatteredFeatureStart, StructureStart

### Community 26 - ".findIntersecting"
Cohesion: 0.08
Nodes (18): ComponentStronghold, ComponentStrongholdChestCorridor, ComponentStrongholdCrossing, ComponentStrongholdLeftTurn, ComponentStrongholdLibrary, ComponentStrongholdPortalRoom, ComponentStrongholdPrison, ComponentStrongholdRoomCrossing (+10 more)

### Community 27 - "OldTerrainGen"
Cohesion: 0.27
Nodes (4): Aufbau, Teil 1 – Vanilla-1.5.2-Dekoration, OldTerrainGen, WorldGenerator

### Community 28 - "EbxlSaplingBlock.java"
Cohesion: 0.12
Nodes (4): CattailBlock, EbxlPlantBlock, LeafPileBlock, OldLiveGen

### Community 30 - "ArmorBarHud.java"
Cohesion: 0.07
Nodes (11): Kind, AMOUNT, CHEST, DURABILITY, FEET, HEAD, LEGS, Unit (+3 more)

### Community 31 - "minecraft-dev"
Cohesion: 0.50
Nodes (3): npx, minecraft-dev, @mcdxai/minecraft-dev-mcp

### Community 33 - "BackToolsClient.java"
Cohesion: 0.13
Nodes (3): Back, BackItem, BackToolsClient

### Community 35 - ".tunnel"
Cohesion: 0.14
Nodes (3): OldCarver, OldCaves, OldMath

### Community 39 - "EbxlWorldGen"
Cohesion: 0.15
Nodes (5): Choices, EbxlWorldGen, Jungle, Weighted, DesertWell

### Community 41 - "Weltgenerierung eines 1.5.2-Mods in worldgen152 einhängen"
Cohesion: 0.20
Nodes (9): Biome und Dekoratoren, Blöcke anmelden, Bäume im Spiel (Setzlinge), Generator anmelden, Prüfen, Regel 1: Kern-Klassen ohne Spielklassen, Stolperfallen, Strukturen (+1 more)

### Community 42 - "net.neoforged.bus.api.IEventBus"
Cohesion: 0.19
Nodes (4): ArmorBarMod, BackTools, BuffBarMod, EbxlIds

### Community 44 - "biomes.py"
Cohesion: 0.14
Nodes (9): ColorMaps, f32(), fixed(), hexcolor(), main(), multiply(), sky_color(), tinted() (+1 more)

### Community 46 - ".addComponentParts"
Cohesion: 0.09
Nodes (7): Category, Entry, ComponentScatteredFeature, ComponentScatteredFeatureDesertPyramid, ComponentScatteredFeatureJunglePyramid, ComponentScatteredFeatureSwampHut, StructureScatteredFeatureStones

### Community 48 - "hexxit.worldgen152.core.populate.OldGen"
Cohesion: 0.13
Nodes (15): CatTail, EbxlPlantGens, Flowers, LeafPile, Marsh, Melon, Oasis, Quicksand (+7 more)

### Community 49 - ".getBlockId"
Cohesion: 0.14
Nodes (8): Autumn, Bend, LEFT, RIGHT, STRAIGHT, CustomSwamp, EbxlTreeGens, LegendOak

### Community 50 - "OldGen"
Cohesion: 0.06
Nodes (26): TreeChooser, OldDecorator, OldGen, Lake, Spring, Clay, Minable, Sand (+18 more)

### Community 53 - "compare_original.py"
Cohesion: 0.18
Nodes (7): cells(), main(), read_nbt(), payload(), string(), take(), region_chunks()

### Community 54 - "EbxlBlocks.java"
Cohesion: 0.09
Nodes (3): LogTurnerItem, ScarecrowItem, ShadowPickaxeItem

### Community 55 - "verify_blocks.py"
Cohesion: 0.22
Nodes (8): main(), packet(), read(), block(), ebxl(), flag(), main(), test()

### Community 56 - "Ore"
Cohesion: 0.17
Nodes (9): Ore, COAL, DIAMOND, DIRT, GOLD, GRAVEL, IRON, LAPIS (+1 more)

### Community 65 - "QuarterLogBlock"
Cohesion: 0.18
Nodes (6): Bark, NE, NW, SE, SW, QuarterLogBlock

### Community 67 - "BackItemLayer.java"
Cohesion: 0.09
Nodes (3): BackItemLayer, ScarecrowModel, ScarecrowRenderer

### Community 68 - "StructureComponent"
Cohesion: 0.07
Nodes (8): ComponentMineshaftCorridor, ComponentMineshaftCross, ComponentMineshaftRoom, ComponentMineshaftStairs, ComponentStrongholdCorridor, ComponentStrongholdStairs2, StructureComponent, StructureMineshaftPieces

### Community 70 - "net.minecraft.resources.Identifier"
Cohesion: 0.18
Nodes (4): ChestTransporterJeiPlugin, EbxlJeiPlugin, ObsidiPlatesJeiPlugin, SoulShardsJeiPlugin

### Community 71 - ".fillWithRandomizedBlocks"
Cohesion: 0.10
Nodes (3): ComponentStrongholdRightTurn, StructurePieceBlockSelector, StructureStrongholdStones

### Community 73 - "Populate"
Cohesion: 0.33
Nodes (5): Populate, DUNGEON, ICE, LAKE, LAVA

### Community 74 - "JEI-Plugin für einen portierten Mod"
Cohesion: 0.40
Nodes (4): Einbindung (schon erledigt, in `build.gradle`), JEI-Plugin für einen portierten Mod, Prüfen, Vorlage

### Community 77 - "Acuteness"
Cohesion: 0.50
Nodes (4): Acuteness, LOOSE, TIGHT, TIGHTER

### Community 79 - "SoulShards.java"
Cohesion: 0.09
Nodes (4): ChestTransporter, ObsidiPlates, Soul, SoulShards

### Community 81 - "HeroMC – Hexxit 1:1 für Minecraft 26.3"
Cohesion: 0.18
Nodes (9): Arbeitsplan, Aufbau, Bauen und Starten, HeroMC – Hexxit 1:1 für Minecraft 26.3, Referenzquellen des Originals, Status, Vorgaben, Wann ein Mod als portiert gilt (+1 more)

### Community 84 - "Client-Darstellung in 26.3 (NeoForge 26.3.0.102)"
Cohesion: 0.25
Nodes (7): Client-Darstellung in 26.3 (NeoForge 26.3.0.102), Etwas am Spielermodell zeichnen (Rücken, Schultern), Halbdurchsichtige Blöcke (Alpha im Renderer), HUD (statt `ITickHandler` mit `TickType.RENDER`), Nur-Client-Mod sauber laden, Rechtsklick vor dem Block (Original: `PlayerInteractEvent` mit `RIGHT_CLICK_BLOCK`), Stolperfallen

## Knowledge Gaps
- **74 isolated node(s):** `FEET`, `LEGS`, `CHEST`, `HEAD`, `AMOUNT` (+69 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 430 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **46 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `OldWorld` connect `OldWorld` to `StructureBoundingBox`, `OldBiome`, `OldBlockInfo`, `BigTree`, `OldTerrainGen`, `EbxlSaplingBlock.java`, `.column`, `OldDecoration`, `EbxlWorldGen`, `OldRails`, `.addComponentParts`, `hexxit.worldgen152.core.populate.OldGen`, `.getBlockId`, `OldGen`, `Listener`, `StructureComponent`, `.fillWithRandomizedBlocks`, `.populate`, `Populate`, `.StructureVillageStart`?**
  _High betweenness centrality (0.251) - this node is a cross-community bridge._
- **What connects `FEET`, `LEGS`, `CHEST` to the rest of the system?**
  _74 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `StructureBoundingBox` be split into smaller, more focused modules?**
  _Cohesion score 0.12510974539069358 - nodes in this community are weakly interconnected._
- **Why does `OldBiome` connect `OldBiome` to `Layer`, `OldRavines`, `.tunnel`, `OldBiomeSource.java`, `OldTerrain`, `OldBlockInfo`, `BiomeLayout`, `OldGen`, `Ore`?**
  _High betweenness centrality (0.066) - this node is a cross-community bridge._
- **Should `Layer` be split into smaller, more focused modules?**
  _Cohesion score 0.08432539682539683 - nodes in this community are weakly interconnected._
- **Why does `Arbeitsplan: Hexxit 1.0.11 → NeoForge 26.3` connect `Arbeitsplan: Hexxit 1.0.11 → NeoForge 26.3` to `HeroMC – Hexxit 1:1 für Minecraft 26.3`, `.StructureVillageStart`, `OldTerrainGen`, `StructureComponent`?**
  _High betweenness centrality (0.045) - this node is a cross-community bridge._
- **Should `WorldGen152.java` be split into smaller, more focused modules?**
  _Cohesion score 0.1437908496732026 - nodes in this community are weakly interconnected._