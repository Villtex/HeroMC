# Graph Report - HeroMC  (2026-11-15)

## Corpus Check
- 1015 files · ~155,594 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 20 file(s) not represented in the graph (top: .properties 16, (none) 1, .toml 1)

## Summary
- 2451 nodes · 7703 edges · 97 communities (50 shown, 47 thin omitted)
- Extraction: 93% EXTRACTED · 7% INFERRED · 0% AMBIGUOUS · INFERRED: 567 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `f56de7c2`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- StructureBoundingBox
- Layer
- reference.py
- .server
- net.minecraft.world.item.ItemStack
- expect.sh
- ImportOriginalAssets.groovy
- OldTerrain
- Arbeitsplan: Hexxit 1.0.11 → NeoForge 26.3
- OldChunkGenerator.java
- HeroMC – Hinweise für Claude
- .claude/CLAUDE.md
- .generate
- OldBlockInfo
- OldDecoratedChunk
- HexxitGear.java
- EbxlWorldGen
- StructureComponent
- BigTree
- Decorate
- OldStructures
- .findIntersecting
- OldGen
- net.minecraft.world.phys.shapes.VoxelShape
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
- EnchantingPlus.java
- net.neoforged.bus.api.IEventBus
- ArmorBarHud.java
- biomes.py
- PerlinNoise
- StructurePieceBlockSelector
- EnchantTableScreen.java
- hexxit.worldgen152.core.populate.OldGen
- .getBlockId
- .setBlock
- Material
- net.minecraft.server.level.ServerLevel
- compare_original.py
- net.minecraft.world.item.Item
- verify_blocks.py
- OldLiveGen
- OldBlockStates
- net.minecraft.world.level.block.state.BlockState
- TableEvents.java
- EbxlBlocks
- SoulEvents.java
- ScarecrowEntity
- net.neoforged.bus.api.IEventBus
- original_server.sh
- .setBlock
- net.minecraft.world.item.ItemStack
- EnchantTableScreen
- StructureComponent
- net.minecraft.core.BlockPos
- net.minecraft.resources.Identifier
- .func_74992_a
- OldGen
- EnchantingCategory.java
- JEI-Plugin für einen portierten Mod
- net.minecraft.world.level.Level
- net.minecraft.world.entity.player.Player
- OldWorld
- EplusNetwork.java
- OffLawn.java
- net.neoforged.bus.api.SubscribeEvent
- HexxitGearClient.java
- ScarecrowRenderer
- .chunkState
- Client-Darstellung in 26.3 (NeoForge 26.3.0.102)
- PlayerMobPlateBlock.java
- ScarecrowModel.java
- HeroMC – Hexxit 1:1 für Minecraft 26.3
- StalactiteBlock
- Override
- net.minecraft.client.model.geom.builders.LayerDefinition
- Category
- .helmet
- VillageHandler
- Acuteness

## God Nodes (most connected - your core abstractions)
1. `OldWorld` - 251 edges
2. `StructureBoundingBox` - 111 edges
3. `OldBlockInfo` - 76 edges
4. `StructureComponent` - 67 edges
5. `OldLoot` - 65 edges
6. `OldTile` - 65 edges
7. `OldEntity` - 63 edges
8. `EbxlBlocks` - 46 edges
9. `OldBiome` - 44 edges
10. `ComponentVillageStartPiece` - 43 edges

## Surprising Connections (you probably didn't know these)
- `Aufbau` --references--> `OldDungeonGen`  [INFERRED]
  .claude/skills/worldgen152/SKILL.md → mods/worldgen152/src/main/java/hexxit/worldgen152/core/populate/OldDungeonGen.java
- `Bäume im Spiel (Setzlinge)` --references--> `OldGen`  [INFERRED]
  .claude/skills/worldgen152/SKILL.md → mods/worldgen152/src/main/java/hexxit/worldgen152/core/populate/OldGen.java
- `Teil 12 und 13 – Better Dungeons (Chocolate Quest)` --references--> `VillageHandler`  [INFERRED]
  docs/PLAN.md → mods/worldgen152/src/main/java/hexxit/worldgen152/core/structure/OldStructures.java
- `Teil 5 – Hexxit Gear, More Bows, Enchanting Plus, offLawn, Wild Caves 3` --references--> `IClientItemExtensions`  [INFERRED]
  docs/PLAN.md → mods/hexxitgear/src/main/java/sct/hexxitgear/client/HexxitGearClient.java
- `Teil 5 – Hexxit Gear, More Bows, Enchanting Plus, offLawn, Wild Caves 3` --references--> `Skull`  [INFERRED]
  docs/PLAN.md → mods/worldgen152/src/main/java/hexxit/worldgen152/core/populate/OldTile.java

## Import Cycles
- None detected.

## Communities (97 total, 47 thin omitted)

### Community 0 - "StructureBoundingBox"
Cohesion: 0.11
Nodes (4): Villager, Witch, Spawner, StructureBoundingBox

### Community 1 - "Layer"
Cohesion: 0.09
Nodes (18): Layer, AddIsland, AddMushroomIsland, AddSnow, Biome, FuzzyZoom, Hills, Island (+10 more)

### Community 2 - "reference.py"
Cohesion: 0.17
Nodes (10): build_mappings(), digest(), download(), inner_archives(), java(), main(), minecraft_reference(), process() (+2 more)

### Community 3 - ".server"
Cohesion: 0.15
Nodes (5): Container → `AbstractContainerMenu`, Konfiguration und Spielerdaten, Menü, Screen und Pakete in 26.3 (NeoForge 26.3.0.102), Pakete → Custom Payloads, Vanilla-Block ersetzen (Mod hat Block-ID überschrieben)

### Community 4 - "net.minecraft.world.item.ItemStack"
Cohesion: 0.14
Nodes (5): EnchantHelper, OldCost, Restriction, EnchantTableMenu, Settings

### Community 7 - "OldTerrain"
Cohesion: 0.16
Nodes (3): BiomeLayout, OctaveNoise, OldTerrain

### Community 8 - "Arbeitsplan: Hexxit 1.0.11 → NeoForge 26.3"
Cohesion: 0.10
Nodes (20): Arbeitsplan: Hexxit 1.0.11 → NeoForge 26.3, Regeln für jeden Teil, Teil 10 und 11 – Project Zulu, Teil 14 – Battle Towers, Ruins, Teil 15 – Dimensional Doors, Teil 16 – BiblioCraft, Teil 17 – BetterStorage, Backpacks, qCraft, Teil 18 – ChocoCraft, Infernal Mobs (+12 more)

### Community 11 - "HeroMC – Hinweise für Claude"
Cohesion: 0.29
Nodes (6): Arbeitsweise beim Portieren, Branches, graphify, HeroMC – Hinweise für Claude, Skills anlegen, Tokens sparen

### Community 18 - "OldBlockInfo"
Cohesion: 0.10
Nodes (13): OldBlocks, OldBlockInfo, MinecartChest, OldEntity, OldLakeGens, OldLoot, OldOreGens, OldPopulator (+5 more)

### Community 19 - "OldDecoratedChunk"
Cohesion: 0.11
Nodes (3): OldDecoratedChunk, OldChunkWriter, Expect

### Community 20 - "HexxitGear.java"
Cohesion: 0.07
Nodes (6): Truhen und Spawner, HGWorldGen, HexxitGear, Soul, SoulShards, OldGameIds

### Community 21 - "EbxlWorldGen"
Cohesion: 0.14
Nodes (5): Choices, EbxlWorldGen, Jungle, Weighted, DesertWell

### Community 22 - "StructureComponent"
Cohesion: 0.07
Nodes (16): ComponentVillage, ComponentVillageChurch, ComponentVillageField2, ComponentVillageField, ComponentVillageHall, ComponentVillageHouse1, ComponentVillageHouse2, ComponentVillageHouse3 (+8 more)

### Community 24 - "Decorate"
Cohesion: 0.07
Nodes (24): Decorate, BIG_SHROOM, CACTUS, CLAY, DEAD_BUSH, FLOWERS, GRASS, LAKE (+16 more)

### Community 25 - "OldStructures"
Cohesion: 0.10
Nodes (7): Found, OldStructures, StructureMineshaftStart, StructureScatteredFeatureStart, StructureStart, StructureStrongholdStart, StructureVillageStart

### Community 26 - ".findIntersecting"
Cohesion: 0.07
Nodes (20): ComponentStronghold, ComponentStrongholdChestCorridor, ComponentStrongholdCrossing, ComponentStrongholdLeftTurn, ComponentStrongholdLibrary, ComponentStrongholdPortalRoom, ComponentStrongholdPrison, ComponentStrongholdRightTurn (+12 more)

### Community 27 - "OldGen"
Cohesion: 0.12
Nodes (8): Cactus, DeadBush, Flowers, OldPlantGens, Reed, TallGrass, Vines, Waterlily

### Community 28 - "net.minecraft.world.phys.shapes.VoxelShape"
Cohesion: 0.12
Nodes (5): CattailBlock, EbxlPlantBlock, HexbiscusBlock, FloraBlock, IcicleBlock

### Community 30 - "ArmorBarHud"
Cohesion: 0.13
Nodes (10): Kind, AMOUNT, CHEST, DURABILITY, FEET, HEAD, LEGS, Unit (+2 more)

### Community 31 - "minecraft-dev"
Cohesion: 0.50
Nodes (3): npx, minecraft-dev, @mcdxai/minecraft-dev-mcp

### Community 33 - "BackToolsClient.java"
Cohesion: 0.09
Nodes (4): BackItemLayer, Back, BackItem, BackToolsClient

### Community 34 - "net.minecraft.world.item.ItemStack"
Cohesion: 0.15
Nodes (7): Kind, ENDER, FLAME, FROST, MULTI, PLAIN, MoreBowItem

### Community 35 - ".tunnel"
Cohesion: 0.07
Nodes (9): EbxlBiomes, Decoration, GrassChooser, OldBiome, OldBiomes, OldCarver, OldCaves, OldMath (+1 more)

### Community 38 - "OldDecoration"
Cohesion: 0.12
Nodes (3): OldChunk, OldDecoration, TerrainSource

### Community 41 - "EnchantingPlus.java"
Cohesion: 0.11
Nodes (5): EnchantingPlus, ClientOptionPayload, EnchantPayload, OpenModTablePayload, UseModPayload

### Community 42 - "net.neoforged.bus.api.IEventBus"
Cohesion: 0.13
Nodes (4): ArmorBarMod, BackTools, BuffBarMod, RFTL

### Community 44 - "biomes.py"
Cohesion: 0.14
Nodes (9): ColorMaps, f32(), fixed(), hexcolor(), main(), multiply(), sky_color(), tinted() (+1 more)

### Community 46 - "StructurePieceBlockSelector"
Cohesion: 0.09
Nodes (7): ComponentScatteredFeature, ComponentScatteredFeatureDesertPyramid, ComponentScatteredFeatureJunglePyramid, ComponentScatteredFeatureSwampHut, StructurePieceBlockSelector, StructureScatteredFeatureStones, StructureStrongholdStones

### Community 48 - "hexxit.worldgen152.core.populate.OldGen"
Cohesion: 0.10
Nodes (15): CatTail, EbxlPlantGens, Flowers, LeafPile, Marsh, Melon, Oasis, Quicksand (+7 more)

### Community 49 - ".getBlockId"
Cohesion: 0.15
Nodes (4): Autumn, BigAutumn, CustomSwamp, EbxlTreeGens

### Community 50 - ".setBlock"
Cohesion: 0.14
Nodes (9): BigMushroom, Birch, Huge, OldTreeGens, Pine, Shrub, Spruce, Swamp (+1 more)

### Community 51 - "Material"
Cohesion: 0.07
Nodes (27): Material, AIR, CACTUS, CIRCUITS, CLAY, CLOTH, CRAFTED_SNOW, FIRE (+19 more)

### Community 53 - "compare_original.py"
Cohesion: 0.18
Nodes (7): cells(), main(), read_nbt(), payload(), string(), take(), region_chunks()

### Community 54 - "net.minecraft.world.item.Item"
Cohesion: 0.05
Nodes (5): LogTurnerItem, ScarecrowItem, SunflowerSeedItem, ShadowPickaxeItem, SoulShardItem

### Community 55 - "verify_blocks.py"
Cohesion: 0.18
Nodes (10): main(), packet(), read(), block(), ebxl(), flag(), main(), test() (+2 more)

### Community 60 - "EbxlBlocks"
Cohesion: 0.06
Nodes (10): EbxlLeavesBlock, LeafPileBlock, Bark, NE, NW, SE, SW, QuarterLogBlock (+2 more)

### Community 63 - "net.neoforged.bus.api.IEventBus"
Cohesion: 0.08
Nodes (5): EbxlClient, Extrabiomes, FrostArrowRenderer, MoreBowsClient, OffLawnClient

### Community 67 - "EnchantTableScreen"
Cohesion: 0.15
Nodes (3): GuiContainer → `AbstractContainerScreen`, EnchantTableScreen, GuiItem

### Community 68 - "StructureComponent"
Cohesion: 0.07
Nodes (7): ComponentMineshaftCorridor, ComponentMineshaftCross, ComponentMineshaftRoom, ComponentMineshaftStairs, ComponentStrongholdStairs2, StructureComponent, StructureVillagePieces

### Community 69 - "net.minecraft.core.BlockPos"
Cohesion: 0.13
Nodes (3): EbxlSaplingBlock, LawnBlock, SunflowerBlock

### Community 70 - "net.minecraft.resources.Identifier"
Cohesion: 0.09
Nodes (7): ChestTransporterJeiPlugin, EbxlJeiPlugin, EplusJeiPlugin, HexxitGearJeiPlugin, MoreBowsJeiPlugin, ObsidiPlatesJeiPlugin, SoulShardsJeiPlugin

### Community 72 - "OldGen"
Cohesion: 0.06
Nodes (25): Aufbau, Biome und Dekoratoren, Blöcke anmelden, Bäume im Spiel (Setzlinge), Generator anmelden, Prüfen, Regel 1: Kern-Klassen ohne Spielklassen, Stolperfallen (+17 more)

### Community 74 - "JEI-Plugin für einen portierten Mod"
Cohesion: 0.40
Nodes (4): Einbindung (schon erledigt, in `build.gradle`), JEI-Plugin für einen portierten Mod, Prüfen, Vorlage

### Community 76 - "net.minecraft.world.entity.player.Player"
Cohesion: 0.21
Nodes (4): ArmorSet, SCALE, THIEF, TRIBAL

### Community 77 - "OldWorld"
Cohesion: 0.09
Nodes (9): Bend, LEFT, RIGHT, STRAIGHT, LegendOak, OldDungeonGen, Lake, Listener (+1 more)

### Community 79 - "OffLawn.java"
Cohesion: 0.08
Nodes (4): ChestTransporter, MoreBows, OffLawn, WildCaves

### Community 81 - "HexxitGearClient.java"
Cohesion: 0.19
Nodes (3): Teil 5 – Hexxit Gear, More Bows, Enchanting Plus, offLawn, Wild Caves 3, IClientItemExtensions, Skull

### Community 84 - "Client-Darstellung in 26.3 (NeoForge 26.3.0.102)"
Cohesion: 0.22
Nodes (8): Client-Darstellung in 26.3 (NeoForge 26.3.0.102), Etwas am Spielermodell zeichnen (Rücken, Schultern), Halbdurchsichtige Blöcke (Alpha im Renderer), HUD (statt `ITickHandler` mit `TickType.RENDER`), Nur-Client-Mod sauber laden, Rechtsklick vor dem Block (Original: `PlayerInteractEvent` mit `RIGHT_CLICK_BLOCK`), Rüstung: Texturen und eigene Helm-Modelle (`ItemArmor`, `getArmorTexture`, `getArmorModel`), Stolperfallen

### Community 88 - "HeroMC – Hexxit 1:1 für Minecraft 26.3"
Cohesion: 0.18
Nodes (9): Arbeitsplan, Aufbau, Bauen und Starten, HeroMC – Hexxit 1:1 für Minecraft 26.3, Referenzquellen des Originals, Status, Vorgaben, Wann ein Mod als portiert gilt (+1 more)

### Community 96 - "Acuteness"
Cohesion: 0.50
Nodes (4): Acuteness, LOOSE, TIGHT, TIGHTER

## Knowledge Gaps
- **82 isolated node(s):** `TRIBAL`, `THIEF`, `SCALE`, `AIR`, `GRASS` (+77 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 522 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **47 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `OldWorld` connect `OldWorld` to `StructureBoundingBox`, `.generate`, `OldBlockInfo`, `EbxlWorldGen`, `StructureComponent`, `BigTree`, `Decorate`, `OldGen`, `.column`, `.tunnel`, `OldDecoration`, `OldRails`, `hexxit.worldgen152.core.populate.OldGen`, `.getBlockId`, `.setBlock`, `net.minecraft.world.item.Item`, `.setBlock`, `StructureComponent`, `.func_74992_a`, `OldGen`, `.chunkState`?**
  _High betweenness centrality (0.216) - this node is a cross-community bridge._
- **What connects `TRIBAL`, `THIEF`, `SCALE` to the rest of the system?**
  _82 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `StructureBoundingBox` be split into smaller, more focused modules?**
  _Cohesion score 0.11171333935561578 - nodes in this community are weakly interconnected._
- **Why does `OldBiome` connect `.tunnel` to `Layer`, `OldBiomeSource.java`, `OldTerrain`, `OldGen`, `OldChunkGenerator.java`, `OldBlockInfo`, `Decorate`?**
  _High betweenness centrality (0.060) - this node is a cross-community bridge._
- **Should `Layer` be split into smaller, more focused modules?**
  _Cohesion score 0.08831306187202538 - nodes in this community are weakly interconnected._
- **Why does `Material` connect `Material` to `.column`, `OldBlockInfo`?**
  _High betweenness centrality (0.040) - this node is a cross-community bridge._
- **Should `net.minecraft.world.item.ItemStack` be split into smaller, more focused modules?**
  _Cohesion score 0.13652482269503546 - nodes in this community are weakly interconnected._