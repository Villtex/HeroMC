# Graph Report - agent-a0308e29cb55910cf  (2026-11-15)

## Corpus Check
- 698 files · ~130,845 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 17 file(s) not represented in the graph (top: .properties 12, (none) 2, .toml 1)

## Summary
- 2003 nodes · 6068 edges · 85 communities (45 shown, 40 thin omitted)
- Extraction: 92% EXTRACTED · 8% INFERRED · 0% AMBIGUOUS · INFERRED: 510 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `abca46a1`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- StructureBoundingBox
- Layer
- reference.py
- net.minecraft.world.entity.player.Player
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
- OldLiveGen.java
- ArmorBarHud.java
- minecraft-dev
- BigAutumn
- BackToolsClient.java
- .tunnel
- OldBiomeSource.java
- OldDecoration
- OldGen
- OldRails
- Weltgenerierung eines 1.5.2-Mods in worldgen152 einhängen
- net.neoforged.bus.api.IEventBus
- HexxitGearClient.java
- biomes.py
- PerlinNoise
- .addComponentParts
- Expect.java
- hexxit.worldgen152.core.populate.OldGen
- .getBlockId
- OldTreeGens
- ArmorBarHud.java
- SoulCageBlockEntity.java
- compare_original.py
- EbxlBlocks.java
- verify_blocks.py
- net.minecraft.client.model.geom.builders.LayerDefinition
- OldBlockStates
- EbxlLeavesBlock.java
- ScarecrowRenderer
- EbxlBlocks
- SoulEvents.java
- ScarecrowEntity
- EbxlClient.java
- original_server.sh
- QuarterLogBlock
- net.minecraft.world.item.ItemStack
- BackItemLayer.java
- .findIntersecting
- net.minecraft.core.BlockPos
- net.minecraft.resources.Identifier
- .fillWithRandomizedBlocks
- .generate
- LegendOak
- JEI-Plugin für einen portierten Mod
- net.minecraft.world.phys.shapes.VoxelShape
- SoulCageBlock.java
- Acuteness
- ScarecrowModel.java
- SoulShards.java
- SoulShardItem.java
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

## Communities (85 total, 40 thin omitted)

### Community 0 - "StructureBoundingBox"
Cohesion: 0.13
Nodes (3): Villager, Witch, Spawner

### Community 1 - "Layer"
Cohesion: 0.09
Nodes (18): Layer, AddIsland, AddMushroomIsland, AddSnow, Biome, FuzzyZoom, Hills, Island (+10 more)

### Community 2 - "reference.py"
Cohesion: 0.17
Nodes (10): build_mappings(), digest(), download(), inner_archives(), java(), main(), minecraft_reference(), process() (+2 more)

### Community 3 - "net.minecraft.world.entity.player.Player"
Cohesion: 0.09
Nodes (6): ArmorSet, SCALE, THIEF, TRIBAL, SetBonuses, State

### Community 4 - "OldBiome"
Cohesion: 0.12
Nodes (6): EbxlBiomes, Decoration, GrassChooser, OldBiome, TreeChooser, OldBiomes

### Community 7 - "OldTerrain"
Cohesion: 0.12
Nodes (4): BiomeLayout, OctaveNoise, OldChunk, OldTerrain

### Community 8 - "Arbeitsplan: Hexxit 1.0.11 → NeoForge 26.3"
Cohesion: 0.05
Nodes (33): Arbeitsplan: Hexxit 1.0.11 → NeoForge 26.3, Regeln für jeden Teil, Teil 10 und 11 – Project Zulu, Teil 12 und 13 – Better Dungeons (Chocolate Quest), Teil 14 – Battle Towers, Ruins, Teil 15 – Dimensional Doors, Teil 16 – BiblioCraft, Teil 17 – BetterStorage, Backpacks, qCraft (+25 more)

### Community 9 - "OldChunkGenerator.java"
Cohesion: 0.08
Nodes (3): RFTL, OldChunkGenerator, WorldGen152

### Community 11 - "HeroMC – Hinweise für Claude"
Cohesion: 0.29
Nodes (6): Arbeitsweise beim Portieren, Branches, graphify, HeroMC – Hinweise für Claude, Skills anlegen, Tokens sparen

### Community 17 - "OldWorld"
Cohesion: 0.09
Nodes (3): Listener, Column, OldWorld

### Community 18 - "OldBlockInfo"
Cohesion: 0.11
Nodes (11): OldBlocks, OldBlockInfo, MinecartChest, OldEntity, OldLakeGens, OldLoot, OldPopulator, OldStack (+3 more)

### Community 20 - "OldGameIds.java"
Cohesion: 0.11
Nodes (3): Truhen und Spawner, HomeDimensions, OldGameIds

### Community 21 - "Material"
Cohesion: 0.06
Nodes (31): Choices, EbxlWorldGen, Jungle, Weighted, Material, AIR, CACTUS, CIRCUITS (+23 more)

### Community 22 - "StructureComponent"
Cohesion: 0.05
Nodes (17): ComponentVillage, ComponentVillageChurch, ComponentVillageField2, ComponentVillageField, ComponentVillageHall, ComponentVillageHouse1, ComponentVillageHouse2, ComponentVillageHouse3 (+9 more)

### Community 24 - "Decorate"
Cohesion: 0.07
Nodes (24): Decorate, BIG_SHROOM, CACTUS, CLAY, DEAD_BUSH, FLOWERS, GRASS, LAKE (+16 more)

### Community 25 - "OldStructures"
Cohesion: 0.08
Nodes (8): Found, OldStructures, VillageBlocks, StructureMineshaftStart, StructureScatteredFeatureStart, StructureStart, StructureStrongholdStart, StructureVillageStart

### Community 26 - ".findIntersecting"
Cohesion: 0.06
Nodes (20): ComponentStronghold, ComponentStrongholdChestCorridor, ComponentStrongholdCrossing, ComponentStrongholdLeftTurn, ComponentStrongholdLibrary, ComponentStrongholdPortalRoom, ComponentStrongholdPrison, ComponentStrongholdRoomCrossing (+12 more)

### Community 27 - "OldTerrainGen"
Cohesion: 0.16
Nodes (9): Teil 1 – Vanilla-1.5.2-Dekoration, Entry, OldTerrainGen, Populate, DUNGEON, ICE, LAKE, LAVA (+1 more)

### Community 28 - "OldLiveGen.java"
Cohesion: 0.15
Nodes (3): CattailBlock, LeafPileBlock, OldLiveGen

### Community 30 - "ArmorBarHud.java"
Cohesion: 0.13
Nodes (10): Kind, AMOUNT, CHEST, DURABILITY, FEET, HEAD, LEGS, Unit (+2 more)

### Community 31 - "minecraft-dev"
Cohesion: 0.50
Nodes (3): npx, minecraft-dev, @mcdxai/minecraft-dev-mcp

### Community 32 - "BigAutumn"
Cohesion: 0.11
Nodes (6): BigAutumn, OldMath, Clay, Minable, OldOreGens, Sand

### Community 33 - "BackToolsClient.java"
Cohesion: 0.14
Nodes (3): Back, BackItem, BackToolsClient

### Community 35 - ".tunnel"
Cohesion: 0.15
Nodes (3): OldCarver, OldCaves, OldRavines

### Community 39 - "OldGen"
Cohesion: 0.08
Nodes (15): Aufbau, OldDecorator, OldGen, DesertWell, Lake, Spring, Cactus, DeadBush (+7 more)

### Community 41 - "Weltgenerierung eines 1.5.2-Mods in worldgen152 einhängen"
Cohesion: 0.20
Nodes (9): Biome und Dekoratoren, Blöcke anmelden, Bäume im Spiel (Setzlinge), Generator anmelden, Prüfen, Regel 1: Kern-Klassen ohne Spielklassen, Stolperfallen, Strukturen (+1 more)

### Community 42 - "net.neoforged.bus.api.IEventBus"
Cohesion: 0.23
Nodes (3): ArmorBarMod, BackTools, BuffBarMod

### Community 44 - "biomes.py"
Cohesion: 0.14
Nodes (9): ColorMaps, f32(), fixed(), hexcolor(), main(), multiply(), sky_color(), tinted() (+1 more)

### Community 46 - ".addComponentParts"
Cohesion: 0.11
Nodes (7): Category, Entry, ComponentScatteredFeature, ComponentScatteredFeatureDesertPyramid, ComponentScatteredFeatureJunglePyramid, ComponentScatteredFeatureSwampHut, StructureScatteredFeatureStones

### Community 48 - "hexxit.worldgen152.core.populate.OldGen"
Cohesion: 0.11
Nodes (11): CatTail, EbxlPlantGens, Flowers, LeafPile, Marsh, Melon, Oasis, Quicksand (+3 more)

### Community 49 - ".getBlockId"
Cohesion: 0.14
Nodes (7): Acacia, Autumn, CustomSwamp, EbxlTreeGens, Fir, FirHuge, Redwood

### Community 50 - "OldTreeGens"
Cohesion: 0.13
Nodes (9): BigMushroom, Birch, Huge, OldTreeGens, Pine, Shrub, Spruce, Swamp (+1 more)

### Community 53 - "compare_original.py"
Cohesion: 0.18
Nodes (7): cells(), main(), read_nbt(), payload(), string(), take(), region_chunks()

### Community 54 - "EbxlBlocks.java"
Cohesion: 0.09
Nodes (3): LogTurnerItem, ScarecrowItem, ShadowPickaxeItem

### Community 55 - "verify_blocks.py"
Cohesion: 0.22
Nodes (8): main(), packet(), read(), block(), ebxl(), flag(), main(), test()

### Community 65 - "QuarterLogBlock"
Cohesion: 0.12
Nodes (8): Bark, NE, NW, SE, SW, QuarterLogBlock, Extrabiomes, EbxlIds

### Community 68 - ".findIntersecting"
Cohesion: 0.11
Nodes (5): ComponentMineshaftCorridor, ComponentMineshaftCross, ComponentMineshaftStairs, ComponentStrongholdCorridor, StructureMineshaftPieces

### Community 70 - "net.minecraft.resources.Identifier"
Cohesion: 0.16
Nodes (5): ChestTransporterJeiPlugin, EbxlJeiPlugin, HexxitGearJeiPlugin, ObsidiPlatesJeiPlugin, SoulShardsJeiPlugin

### Community 71 - ".fillWithRandomizedBlocks"
Cohesion: 0.14
Nodes (5): ComponentMineshaftRoom, ComponentStrongholdRightTurn, StructureBoundingBox, StructurePieceBlockSelector, StructureStrongholdStones

### Community 73 - "LegendOak"
Cohesion: 0.40
Nodes (5): Bend, LEFT, RIGHT, STRAIGHT, LegendOak

### Community 74 - "JEI-Plugin für einen portierten Mod"
Cohesion: 0.40
Nodes (4): Einbindung (schon erledigt, in `build.gradle`), JEI-Plugin für einen portierten Mod, Prüfen, Vorlage

### Community 75 - "net.minecraft.world.phys.shapes.VoxelShape"
Cohesion: 0.15
Nodes (3): EbxlPlantBlock, QuicksandBlock, HexbiscusBlock

### Community 77 - "Acuteness"
Cohesion: 0.50
Nodes (4): Acuteness, LOOSE, TIGHT, TIGHTER

### Community 79 - "SoulShards.java"
Cohesion: 0.07
Nodes (6): ChestTransporter, HGWorldGen, HexxitGear, ObsidiPlates, Soul, SoulShards

### Community 84 - "Client-Darstellung in 26.3 (NeoForge 26.3.0.102)"
Cohesion: 0.22
Nodes (8): Client-Darstellung in 26.3 (NeoForge 26.3.0.102), Etwas am Spielermodell zeichnen (Rücken, Schultern), Halbdurchsichtige Blöcke (Alpha im Renderer), HUD (statt `ITickHandler` mit `TickType.RENDER`), Nur-Client-Mod sauber laden, Rechtsklick vor dem Block (Original: `PlayerInteractEvent` mit `RIGHT_CLICK_BLOCK`), Rüstung: Texturen und eigene Helm-Modelle (`ItemArmor`, `getArmorTexture`, `getArmorModel`), Stolperfallen

## Knowledge Gaps
- **77 isolated node(s):** `TRIBAL`, `THIEF`, `SCALE`, `LAKE`, `LAVA` (+72 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 451 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **40 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `OldWorld` connect `OldWorld` to `StructureBoundingBox`, `OldBiome`, `Arbeitsplan: Hexxit 1.0.11 → NeoForge 26.3`, `OldBlockInfo`, `Material`, `StructureComponent`, `BigTree`, `Decorate`, `OldTerrainGen`, `OldLiveGen.java`, `BigAutumn`, `OldDecoration`, `OldGen`, `OldRails`, `.addComponentParts`, `hexxit.worldgen152.core.populate.OldGen`, `.getBlockId`, `OldTreeGens`, `.findIntersecting`, `.fillWithRandomizedBlocks`, `.generate`, `LegendOak`?**
  _High betweenness centrality (0.252) - this node is a cross-community bridge._
- **What connects `TRIBAL`, `THIEF`, `SCALE` to the rest of the system?**
  _77 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `StructureBoundingBox` be split into smaller, more focused modules?**
  _Cohesion score 0.1320754716981132 - nodes in this community are weakly interconnected._
- **Why does `OldBiome` connect `OldBiome` to `Layer`, `OldChunkGenerator.java`, `.tunnel`, `OldBiomeSource.java`, `OldGen`, `OldTerrain`, `OldBlockInfo`, `Decorate`?**
  _High betweenness centrality (0.068) - this node is a cross-community bridge._
- **Should `Layer` be split into smaller, more focused modules?**
  _Cohesion score 0.08831306187202538 - nodes in this community are weakly interconnected._
- **Why does `Arbeitsplan: Hexxit 1.0.11 → NeoForge 26.3` connect `Arbeitsplan: Hexxit 1.0.11 → NeoForge 26.3` to `OldTerrainGen`?**
  _High betweenness centrality (0.044) - this node is a cross-community bridge._
- **Should `net.minecraft.world.entity.player.Player` be split into smaller, more focused modules?**
  _Cohesion score 0.09024390243902439 - nodes in this community are weakly interconnected._