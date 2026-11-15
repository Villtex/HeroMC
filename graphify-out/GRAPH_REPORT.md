# Graph Report - agent-ad98be805d89d633b  (2026-11-15)

## Corpus Check
- 651 files · ~135,407 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 17 file(s) not represented in the graph (top: .properties 12, (none) 2, .toml 1)

## Summary
- 2142 nodes · 6517 edges · 89 communities (48 shown, 41 thin omitted)
- Extraction: 92% EXTRACTED · 8% INFERRED · 0% AMBIGUOUS · INFERRED: 516 edges (avg confidence: 0.85)
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
- OldWorld
- OldBlockInfo
- OldDecoratedChunk
- net.minecraft.core.Holder
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
- EnchantingPlus.java
- BackToolsClient.java
- OldGen
- .tunnel
- OldBiomeSource.java
- OldDecoration
- TableEvents.java
- OldRails
- Weltgenerierung eines 1.5.2-Mods in worldgen152 einhängen
- net.neoforged.bus.api.IEventBus
- net.minecraft.world.entity.EntityType
- biomes.py
- PerlinNoise
- .placeBlockAtCurrentPosition
- Expect.java
- .isAirBlock
- .getBlockId
- OldTreeGens
- EnchantTableMenu
- SoulCageBlockEntity.java
- compare_original.py
- EbxlBlocks.java
- verify_blocks.py
- EnchantTableScreen.java
- OldBlockStates
- EbxlLeavesBlock.java
- EnchantingCategory.java
- EbxlBlocks
- SoulEvents.java
- ScarecrowEntity
- EbxlClient.java
- original_server.sh
- net.minecraft.world.level.block.state.BlockState
- net.minecraft.world.item.ItemStack
- BackItemLayer.java
- StructureComponent
- net.minecraft.core.BlockPos
- net.minecraft.resources.Identifier
- StructurePieceBlockSelector
- .setBlock
- ArmorBarHud.java
- JEI-Plugin für einen portierten Mod
- net.minecraft.world.item.ItemStack
- SoulCageBlock.java
- Acuteness
- BackItemLayer.java
- SoulShards.java
- HeroMC – Hexxit 1:1 für Minecraft 26.3
- .StructureVillageStart
- Client-Darstellung in 26.3 (NeoForge 26.3.0.102)
- PlayerMobPlateBlock.java
- .generate
- StructureVillagePieceWeight
- .find

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

## Communities (89 total, 41 thin omitted)

### Community 1 - "Layer"
Cohesion: 0.08
Nodes (18): Layer, AddIsland, AddMushroomIsland, AddSnow, Biome, FuzzyZoom, Hills, Island (+10 more)

### Community 2 - "reference.py"
Cohesion: 0.17
Nodes (10): build_mappings(), digest(), download(), inner_archives(), java(), main(), minecraft_reference(), process() (+2 more)

### Community 4 - "OldBiome"
Cohesion: 0.12
Nodes (6): EbxlBiomes, Decoration, GrassChooser, OldBiome, TreeChooser, OldBiomes

### Community 7 - "OldTerrain"
Cohesion: 0.17
Nodes (3): BiomeLayout, OctaveNoise, OldTerrain

### Community 8 - "Arbeitsplan: Hexxit 1.0.11 → NeoForge 26.3"
Cohesion: 0.10
Nodes (21): Arbeitsplan: Hexxit 1.0.11 → NeoForge 26.3, Regeln für jeden Teil, Teil 10 und 11 – Project Zulu, Teil 14 – Battle Towers, Ruins, Teil 15 – Dimensional Doors, Teil 16 – BiblioCraft, Teil 17 – BetterStorage, Backpacks, qCraft, Teil 18 – ChocoCraft, Infernal Mobs (+13 more)

### Community 11 - "HeroMC – Hinweise für Claude"
Cohesion: 0.29
Nodes (6): Arbeitsweise beim Portieren, Branches, graphify, HeroMC – Hinweise für Claude, Skills anlegen, Tokens sparen

### Community 17 - "OldWorld"
Cohesion: 0.08
Nodes (3): Listener, Column, OldWorld

### Community 18 - "OldBlockInfo"
Cohesion: 0.10
Nodes (12): OldBlocks, OldBlockInfo, MinecartChest, OldEntity, OldLakeGens, OldLoot, OldPopulator, OldStack (+4 more)

### Community 19 - "OldDecoratedChunk"
Cohesion: 0.11
Nodes (3): OldChunk, OldDecoratedChunk, OldChunkWriter

### Community 20 - "net.minecraft.core.Holder"
Cohesion: 0.13
Nodes (4): GuiContainer → `AbstractContainerScreen`, EnchantTableScreen, GuiItem, Restriction

### Community 21 - "Material"
Cohesion: 0.06
Nodes (31): Choices, EbxlWorldGen, Jungle, Weighted, Material, AIR, CACTUS, CIRCUITS (+23 more)

### Community 22 - "StructureComponent"
Cohesion: 0.08
Nodes (15): ComponentVillage, ComponentVillageChurch, ComponentVillageField2, ComponentVillageField, ComponentVillageHall, ComponentVillageHouse1, ComponentVillageHouse2, ComponentVillageHouse3 (+7 more)

### Community 24 - "Decorate"
Cohesion: 0.07
Nodes (24): Decorate, BIG_SHROOM, CACTUS, CLAY, DEAD_BUSH, FLOWERS, GRASS, LAKE (+16 more)

### Community 25 - "OldStructures"
Cohesion: 0.15
Nodes (5): OldStructures, StructureMineshaftStart, StructureScatteredFeatureStart, StructureStart, StructureStrongholdStart

### Community 26 - ".findIntersecting"
Cohesion: 0.06
Nodes (20): ComponentStronghold, ComponentStrongholdChestCorridor, ComponentStrongholdCrossing, ComponentStrongholdLeftTurn, ComponentStrongholdLibrary, ComponentStrongholdPortalRoom, ComponentStrongholdPrison, ComponentStrongholdRightTurn (+12 more)

### Community 27 - "OldTerrainGen"
Cohesion: 0.16
Nodes (9): Teil 1 – Vanilla-1.5.2-Dekoration, Entry, OldTerrainGen, Populate, DUNGEON, ICE, LAKE, LAVA (+1 more)

### Community 30 - "ArmorBarHud.java"
Cohesion: 0.12
Nodes (10): Kind, AMOUNT, CHEST, DURABILITY, FEET, HEAD, LEGS, Unit (+2 more)

### Community 31 - "minecraft-dev"
Cohesion: 0.50
Nodes (3): npx, minecraft-dev, @mcdxai/minecraft-dev-mcp

### Community 32 - "EnchantingPlus.java"
Cohesion: 0.08
Nodes (6): EnchantingPlus, ClientOptionPayload, EnchantPayload, EplusNetwork, OpenModTablePayload, UseModPayload

### Community 33 - "BackToolsClient.java"
Cohesion: 0.14
Nodes (3): Back, BackItem, BackToolsClient

### Community 34 - "OldGen"
Cohesion: 0.08
Nodes (14): Aufbau, OldDecorator, OldGen, Lake, Spring, Cactus, DeadBush, Flowers (+6 more)

### Community 35 - ".tunnel"
Cohesion: 0.12
Nodes (4): OldCarver, OldCaves, OldMath, OldRavines

### Community 39 - "TableEvents.java"
Cohesion: 0.08
Nodes (6): Container → `AbstractContainerMenu`, Konfiguration und Spielerdaten, Menü, Screen und Pakete in 26.3 (NeoForge 26.3.0.102), Pakete → Custom Payloads, Vanilla-Block ersetzen (Mod hat Block-ID überschrieben), TableEvents

### Community 41 - "Weltgenerierung eines 1.5.2-Mods in worldgen152 einhängen"
Cohesion: 0.20
Nodes (9): Biome und Dekoratoren, Blöcke anmelden, Bäume im Spiel (Setzlinge), Generator anmelden, Prüfen, Regel 1: Kern-Klassen ohne Spielklassen, Stolperfallen, Strukturen (+1 more)

### Community 42 - "net.neoforged.bus.api.IEventBus"
Cohesion: 0.14
Nodes (4): ArmorBarMod, BackTools, BuffBarMod, EbxlIds

### Community 43 - "net.minecraft.world.entity.EntityType"
Cohesion: 0.10
Nodes (5): Truhen und Spawner, HomeDimensions, SoulShardItem, Soul, OldGameIds

### Community 44 - "biomes.py"
Cohesion: 0.14
Nodes (9): ColorMaps, f32(), fixed(), hexcolor(), main(), multiply(), sky_color(), tinted() (+1 more)

### Community 46 - ".placeBlockAtCurrentPosition"
Cohesion: 0.13
Nodes (4): Witch, Category, Entry, Spawner

### Community 48 - ".isAirBlock"
Cohesion: 0.10
Nodes (12): CatTail, EbxlPlantGens, Flowers, LeafPile, Marsh, Melon, Oasis, Quicksand (+4 more)

### Community 49 - ".getBlockId"
Cohesion: 0.18
Nodes (7): Acacia, Autumn, CustomSwamp, EbxlTreeGens, Fir, FirHuge, Redwood

### Community 50 - "OldTreeGens"
Cohesion: 0.13
Nodes (9): BigMushroom, Birch, Huge, OldTreeGens, Pine, Shrub, Spruce, Swamp (+1 more)

### Community 51 - "EnchantTableMenu"
Cohesion: 0.15
Nodes (3): EnchantTableMenu, Slot, Settings

### Community 53 - "compare_original.py"
Cohesion: 0.18
Nodes (7): cells(), main(), read_nbt(), payload(), string(), take(), region_chunks()

### Community 54 - "EbxlBlocks.java"
Cohesion: 0.09
Nodes (3): QuarterLogBlock, LogTurnerItem, ScarecrowItem

### Community 55 - "verify_blocks.py"
Cohesion: 0.22
Nodes (8): main(), packet(), read(), block(), ebxl(), flag(), main(), test()

### Community 60 - "EbxlBlocks"
Cohesion: 0.11
Nodes (3): LeafPileBlock, QuicksandBlock, EbxlBlocks

### Community 65 - "net.minecraft.world.level.block.state.BlockState"
Cohesion: 0.10
Nodes (6): Bark, NE, NW, SE, SW, OldLiveGen

### Community 68 - "StructureComponent"
Cohesion: 0.07
Nodes (8): ComponentMineshaftCorridor, ComponentMineshaftCross, ComponentMineshaftRoom, ComponentMineshaftStairs, ComponentStrongholdCorridor, ComponentVillagePathGen, StructureComponent, StructureMineshaftPieces

### Community 70 - "net.minecraft.resources.Identifier"
Cohesion: 0.12
Nodes (5): ChestTransporterJeiPlugin, EbxlJeiPlugin, EplusJeiPlugin, ObsidiPlatesJeiPlugin, SoulShardsJeiPlugin

### Community 71 - "StructurePieceBlockSelector"
Cohesion: 0.09
Nodes (7): ComponentScatteredFeature, ComponentScatteredFeatureDesertPyramid, ComponentScatteredFeatureJunglePyramid, ComponentScatteredFeatureSwampHut, StructurePieceBlockSelector, StructureScatteredFeatureStones, StructureStrongholdStones

### Community 72 - ".setBlock"
Cohesion: 0.20
Nodes (6): Bend, LEFT, RIGHT, STRAIGHT, LegendOak, OldDungeonGen

### Community 74 - "JEI-Plugin für einen portierten Mod"
Cohesion: 0.40
Nodes (4): Einbindung (schon erledigt, in `build.gradle`), JEI-Plugin für einen portierten Mod, Prüfen, Vorlage

### Community 77 - "Acuteness"
Cohesion: 0.50
Nodes (4): Acuteness, LOOSE, TIGHT, TIGHTER

### Community 79 - "SoulShards.java"
Cohesion: 0.11
Nodes (3): ChestTransporter, ObsidiPlates, SoulShards

### Community 81 - "HeroMC – Hexxit 1:1 für Minecraft 26.3"
Cohesion: 0.18
Nodes (9): Arbeitsplan, Aufbau, Bauen und Starten, HeroMC – Hexxit 1:1 für Minecraft 26.3, Referenzquellen des Originals, Status, Vorgaben, Wann ein Mod als portiert gilt (+1 more)

### Community 84 - "Client-Darstellung in 26.3 (NeoForge 26.3.0.102)"
Cohesion: 0.25
Nodes (7): Client-Darstellung in 26.3 (NeoForge 26.3.0.102), Etwas am Spielermodell zeichnen (Rücken, Schultern), Halbdurchsichtige Blöcke (Alpha im Renderer), HUD (statt `ITickHandler` mit `TickType.RENDER`), Nur-Client-Mod sauber laden, Rechtsklick vor dem Block (Original: `PlayerInteractEvent` mit `RIGHT_CLICK_BLOCK`), Stolperfallen

### Community 86 - ".generate"
Cohesion: 0.20
Nodes (4): Clay, Minable, OldOreGens, Sand

### Community 87 - "StructureVillagePieceWeight"
Cohesion: 0.25
Nodes (3): Teil 12 und 13 – Better Dungeons (Chocolate Quest), VillageHandler, StructureVillagePieceWeight

## Knowledge Gaps
- **74 isolated node(s):** `LAKE`, `LAVA`, `DUNGEON`, `ICE`, `SAND` (+69 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 475 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **41 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `OldWorld` connect `OldWorld` to `StructureBoundingBox`, `OldGen`, `OldBiome`, `StructureComponent`, `OldDecoration`, `.setBlock`, `OldRails`, `.placeBlockAtCurrentPosition`, `.isAirBlock`, `.getBlockId`, `OldBlockInfo`, `OldTreeGens`, `.StructureVillageStart`, `Material`, `.generate`, `BigTree`, `Decorate`, `OldTerrainGen`?**
  _High betweenness centrality (0.214) - this node is a cross-community bridge._
- **What connects `LAKE`, `LAVA`, `DUNGEON` to the rest of the system?**
  _74 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Layer` be split into smaller, more focused modules?**
  _Cohesion score 0.08432539682539683 - nodes in this community are weakly interconnected._
- **Why does `OldBiome` connect `OldBiome` to `Layer`, `OldGen`, `.tunnel`, `OldBiomeSource.java`, `OldTerrain`, `OldBlockInfo`, `Decorate`?**
  _High betweenness centrality (0.088) - this node is a cross-community bridge._
- **Should `OldBiome` be split into smaller, more focused modules?**
  _Cohesion score 0.1225071225071225 - nodes in this community are weakly interconnected._
- **Why does `OldStructures` connect `OldStructures` to `OldDecoration`, `OldChunkGenerator.java`, `OldBlockInfo`, `.StructureVillageStart`, `StructureComponent`, `StructureVillagePieceWeight`, `.find`?**
  _High betweenness centrality (0.050) - this node is a cross-community bridge._
- **Should `ImportOriginalAssets.groovy` be split into smaller, more focused modules?**
  _Cohesion score 0.09333333333333334 - nodes in this community are weakly interconnected._