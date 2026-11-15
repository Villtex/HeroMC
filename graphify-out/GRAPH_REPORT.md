# Graph Report - HeroMC  (2026-11-15)

## Corpus Check
- 1100 files · ~196,309 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 24 file(s) not represented in the graph (top: .properties 20, (none) 1, .toml 1)

## Summary
- 3180 nodes · 9856 edges · 119 communities (65 shown, 54 thin omitted)
- Extraction: 92% EXTRACTED · 8% INFERRED · 0% AMBIGUOUS · INFERRED: 818 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `5b443da0`
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
- net.minecraft.world.entity.EntityType
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
- InvTweaksContainerManager
- BackItemLayer.java
- net.minecraft.world.item.ItemStack
- .tunnel
- net.minecraft.world.level.Level
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
- EplusClient
- hexxit.worldgen152.core.populate.OldGen
- .getBlockId
- .setBlock
- InvTweaksConfig
- net.minecraft.server.level.ServerLevel
- compare_original.py
- OldChunkWriter.java
- verify_blocks.py
- InvTweaksContainerManager.java
- OldBlockStates
- net.minecraft.world.level.block.state.BlockState
- TableEvents.java
- EbxlBlocks.java
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
- OldTerrainGen
- EnchantingCategory.java
- JEI-Plugin für einen portierten Mod
- net.minecraft.world.level.Level
- net.minecraft.world.entity.player.Player
- OldWorld
- net.minecraft.network.chat.Component
- OffLawn.java
- InvTweaks
- HexxitGearClient.java
- ScarecrowRenderer
- .get
- Client-Darstellung in 26.3 (NeoForge 26.3.0.102)
- PlayerMobPlateBlock.java
- ScarecrowModel.java
- GuiCustomLan
- .addComponentParts
- EnchantTableMenu
- map
- net.minecraft.client.model.geom.builders.LayerDefinition
- OldStack
- .helmet
- InvTweaksConfigSortingRule
- LegendOak
- InvTweaksHandlerShortcuts
- LanCommands
- net.minecraft.world.item.Item
- InvTweaksItemTree
- InvTweaksHandlerSorting
- net.minecraft.resources.Identifier
- BackToolsClient.java
- WorldGen152.java
- InvTweaksItemTreeLoader.java
- convert_tree.py
- QuarterLogBlock
- .register
- BackToolsClient
- .generate
- EplusConfig
- Type
- ArmorBarHud
- FrostArrowRenderer
- Bäume und Äxte bei TreeCapitator anmelden

## God Nodes (most connected - your core abstractions)
1. `OldWorld` - 251 edges
2. `StructureBoundingBox` - 111 edges
3. `OldBlockInfo` - 76 edges
4. `StructureComponent` - 67 edges
5. `OldLoot` - 65 edges
6. `OldTile` - 65 edges
7. `OldEntity` - 63 edges
8. `InvTweaksConfig` - 48 edges
9. `InvTweaksContainerManager` - 48 edges
10. `EbxlBlocks` - 46 edges

## Surprising Connections (you probably didn't know these)
- `Teil 1 – Vanilla-1.5.2-Dekoration` --references--> `OldDecorator`  [INFERRED]
  docs/PLAN.md → mods/worldgen152/src/main/java/hexxit/worldgen152/core/populate/OldDecorator.java
- `Teil 5 – Hexxit Gear, More Bows, Enchanting Plus, offLawn, Wild Caves 3` --references--> `IClientItemExtensions`  [INFERRED]
  docs/PLAN.md → mods/hexxitgear/src/main/java/sct/hexxitgear/client/HexxitGearClient.java
- `Teil 12 und 13 – Better Dungeons (Chocolate Quest)` --references--> `VillageHandler`  [INFERRED]
  docs/PLAN.md → mods/worldgen152/src/main/java/hexxit/worldgen152/core/structure/OldStructures.java
- `Bäume im Spiel (Setzlinge)` --references--> `OldGen`  [INFERRED]
  .claude/skills/worldgen152/SKILL.md → mods/worldgen152/src/main/java/hexxit/worldgen152/core/populate/OldGen.java
- `Aufbau` --references--> `WorldGenerator`  [INFERRED]
  .claude/skills/worldgen152/SKILL.md → mods/worldgen152/src/main/java/hexxit/worldgen152/core/populate/OldTerrainGen.java

## Import Cycles
- None detected.

## Communities (119 total, 54 thin omitted)

### Community 0 - "StructureBoundingBox"
Cohesion: 0.13
Nodes (4): Witch, Spawner, ComponentMineshaftRoom, StructureBoundingBox

### Community 1 - "Layer"
Cohesion: 0.09
Nodes (18): Layer, AddIsland, AddMushroomIsland, AddSnow, Biome, FuzzyZoom, Hills, Island (+10 more)

### Community 2 - "reference.py"
Cohesion: 0.18
Nodes (10): build_mappings(), digest(), download(), inner_archives(), java(), main(), minecraft_reference(), process() (+2 more)

### Community 3 - ".server"
Cohesion: 0.18
Nodes (6): Container → `AbstractContainerMenu`, Konfiguration und Spielerdaten, Menü, Screen und Pakete in 26.3 (NeoForge 26.3.0.102), Pakete → Custom Payloads, Truhen-GUIs und Inventory Tweaks, Vanilla-Block ersetzen (Mod hat Block-ID überschrieben)

### Community 4 - "net.minecraft.world.item.ItemStack"
Cohesion: 0.19
Nodes (4): EnchantHelper, OldCost, Restriction, Settings

### Community 7 - "OldTerrain"
Cohesion: 0.19
Nodes (3): BiomeLayout, OctaveNoise, OldTerrain

### Community 8 - "Arbeitsplan: Hexxit 1.0.11 → NeoForge 26.3"
Cohesion: 0.05
Nodes (31): Arbeitsplan: Hexxit 1.0.11 → NeoForge 26.3, Regeln für jeden Teil, Teil 10 und 11 – Project Zulu, Teil 12 und 13 – Better Dungeons (Chocolate Quest), Teil 14 – Battle Towers, Ruins, Teil 15 – Dimensional Doors, Teil 16 – BiblioCraft, Teil 17 – BetterStorage, Backpacks, qCraft (+23 more)

### Community 11 - "HeroMC – Hinweise für Claude"
Cohesion: 0.29
Nodes (6): Arbeitsweise beim Portieren, Branches, graphify, HeroMC – Hinweise für Claude, Skills anlegen, Tokens sparen

### Community 17 - ".generate"
Cohesion: 0.13
Nodes (4): OldMath, Clay, Minable, Sand

### Community 18 - "OldBlockInfo"
Cohesion: 0.07
Nodes (36): HGWorldGen, OldBlocks, Material, AIR, CACTUS, CIRCUITS, CLAY, CLOTH (+28 more)

### Community 19 - "OldDecoratedChunk"
Cohesion: 0.09
Nodes (4): OldChunk, OldDecoratedChunk, OldChunkWriter, Expect

### Community 20 - "net.minecraft.world.entity.EntityType"
Cohesion: 0.10
Nodes (5): Truhen und Spawner, HomeDimensions, SoulShardItem, Soul, OldGameIds

### Community 21 - "EbxlWorldGen"
Cohesion: 0.16
Nodes (4): Choices, EbxlWorldGen, Jungle, Weighted

### Community 22 - "StructureComponent"
Cohesion: 0.07
Nodes (17): ComponentVillage, ComponentVillageChurch, ComponentVillageField2, ComponentVillageField, ComponentVillageHall, ComponentVillageHouse1, ComponentVillageHouse2, ComponentVillageHouse3 (+9 more)

### Community 24 - "Decorate"
Cohesion: 0.07
Nodes (24): Decorate, BIG_SHROOM, CACTUS, CLAY, DEAD_BUSH, FLOWERS, GRASS, LAKE (+16 more)

### Community 25 - "OldStructures"
Cohesion: 0.12
Nodes (7): Found, OldStructures, StructureMineshaftStart, StructureScatteredFeatureStart, StructureStart, StructureStrongholdStart, StructureVillageStart

### Community 26 - ".findIntersecting"
Cohesion: 0.08
Nodes (17): ComponentMineshaftStairs, ComponentStrongholdChestCorridor, ComponentStrongholdCrossing, ComponentStrongholdLeftTurn, ComponentStrongholdLibrary, ComponentStrongholdPortalRoom, ComponentStrongholdPrison, ComponentStrongholdRightTurn (+9 more)

### Community 27 - "OldGen"
Cohesion: 0.07
Nodes (23): Aufbau, Biome und Dekoratoren, Blöcke anmelden, Bäume im Spiel (Setzlinge), Generator anmelden, Prüfen, Regel 1: Kern-Klassen ohne Spielklassen, Stolperfallen (+15 more)

### Community 28 - "net.minecraft.world.phys.shapes.VoxelShape"
Cohesion: 0.12
Nodes (5): CattailBlock, EbxlPlantBlock, HexbiscusBlock, FloraBlock, OldLiveGen

### Community 30 - "ArmorBarHud"
Cohesion: 0.16
Nodes (9): Kind, AMOUNT, CHEST, DURABILITY, FEET, HEAD, LEGS, Unit (+1 more)

### Community 31 - "minecraft-dev"
Cohesion: 0.50
Nodes (3): npx, minecraft-dev, @mcdxai/minecraft-dev-mcp

### Community 32 - "InvTweaksContainerManager"
Cohesion: 0.07
Nodes (17): ContainerSection, ARMOR, BREWING_BOTTLES, BREWING_INGREDIENT, CHEST, CRAFTING_IN, CRAFTING_IN_PERSISTENT, CRAFTING_OUT (+9 more)

### Community 34 - "net.minecraft.world.item.ItemStack"
Cohesion: 0.13
Nodes (7): Kind, ENDER, FLAME, FROST, MULTI, PLAIN, MoreBowItem

### Community 35 - ".tunnel"
Cohesion: 0.08
Nodes (8): EbxlBiomes, Decoration, GrassChooser, OldBiome, OldBiomes, OldCarver, OldCaves, OldRavines

### Community 36 - "net.minecraft.world.level.Level"
Cohesion: 0.08
Nodes (5): Teil 6 – bspkrsCore, TreeCapitator, Inventory Tweaks, Custom LAN Ports, TreeCapitator, Data, TreeDefinition, TreeRegistry

### Community 41 - "EnchantingPlus.java"
Cohesion: 0.09
Nodes (5): EnchantingPlus, ClientOptionPayload, EplusNetwork, OpenModTablePayload, UseModPayload

### Community 42 - "net.neoforged.bus.api.IEventBus"
Cohesion: 0.10
Nodes (7): ArmorBarMod, BackTools, bspkrsCoreMod, BuffBarMod, CustomLAN, InvTweaksMod, TreeCapitatorMod

### Community 44 - "biomes.py"
Cohesion: 0.15
Nodes (9): ColorMaps, f32(), fixed(), hexcolor(), main(), multiply(), sky_color(), tinted() (+1 more)

### Community 46 - "StructurePieceBlockSelector"
Cohesion: 0.09
Nodes (7): ComponentScatteredFeature, ComponentScatteredFeatureDesertPyramid, ComponentScatteredFeatureJunglePyramid, ComponentScatteredFeatureSwampHut, StructurePieceBlockSelector, StructureScatteredFeatureStones, StructureStrongholdStones

### Community 48 - "hexxit.worldgen152.core.populate.OldGen"
Cohesion: 0.10
Nodes (10): CatTail, EbxlPlantGens, Flowers, LeafPile, Marsh, Melon, Oasis, Root (+2 more)

### Community 49 - ".getBlockId"
Cohesion: 0.12
Nodes (8): Acacia, Autumn, BigAutumn, CustomSwamp, EbxlTreeGens, Fir, FirHuge, Redwood

### Community 50 - ".setBlock"
Cohesion: 0.14
Nodes (9): BigMushroom, Birch, Huge, OldTreeGens, Pine, Shrub, Spruce, Swamp (+1 more)

### Community 51 - "InvTweaksConfig"
Cohesion: 0.08
Nodes (4): InvTweaksConfig, OrderedProperties, InvTweaksConfigManager, InvTweaksHandlerAutoRefill

### Community 53 - "compare_original.py"
Cohesion: 0.19
Nodes (7): cells(), main(), read_nbt(), payload(), string(), take(), region_chunks()

### Community 54 - "OldChunkWriter.java"
Cohesion: 0.05
Nodes (3): LogTurnerItem, ScarecrowItem, SunflowerSeedItem

### Community 55 - "verify_blocks.py"
Cohesion: 0.18
Nodes (10): main(), packet(), read(), block(), ebxl(), flag(), main(), test() (+2 more)

### Community 56 - "InvTweaksContainerManager.java"
Cohesion: 0.06
Nodes (5): InvTweaksGuiIconButton, InvTweaksGuiSettingsButton, InvTweaksGuiSortingButton, InvTweaksConst, InvTweaksLocalization

### Community 57 - "OldBlockStates"
Cohesion: 0.17
Nodes (3): LeafPileBlock, WildCaves, OldBlockStates

### Community 58 - "net.minecraft.world.level.block.state.BlockState"
Cohesion: 0.12
Nodes (3): EbxlLeavesBlock, BeanstalkBlock, IcicleBlock

### Community 63 - "net.neoforged.bus.api.IEventBus"
Cohesion: 0.13
Nodes (3): EbxlClient, Extrabiomes, OffLawnClient

### Community 65 - ".setBlock"
Cohesion: 0.23
Nodes (3): Teil 5 – Hexxit Gear, More Bows, Enchanting Plus, offLawn, Wild Caves 3, WildCavesGen, Skull

### Community 67 - "EnchantTableScreen"
Cohesion: 0.10
Nodes (4): GuiContainer → `AbstractContainerScreen`, EnchantTableScreen, GuiItem, EnchantPayload

### Community 68 - "StructureComponent"
Cohesion: 0.07
Nodes (8): ComponentMineshaftCorridor, ComponentMineshaftCross, ComponentStronghold, ComponentStrongholdStairs2, StructureComponent, StructureMineshaftPieces, StructureStrongholdPieces, StructureStrongholdPieceWeight

### Community 69 - "net.minecraft.core.BlockPos"
Cohesion: 0.11
Nodes (3): EbxlSaplingBlock, LawnBlock, SunflowerBlock

### Community 70 - "net.minecraft.resources.Identifier"
Cohesion: 0.10
Nodes (7): ChestTransporter, ChestTransporterJeiPlugin, EbxlJeiPlugin, HexxitGearJeiPlugin, MoreBowsJeiPlugin, ObsidiPlatesJeiPlugin, SoulShardsJeiPlugin

### Community 72 - "OldTerrainGen"
Cohesion: 0.12
Nodes (10): Teil 1 – Vanilla-1.5.2-Dekoration, OffLawnGen, Entry, OldTerrainGen, Populate, DUNGEON, ICE, LAKE (+2 more)

### Community 74 - "JEI-Plugin für einen portierten Mod"
Cohesion: 0.40
Nodes (4): Einbindung (schon erledigt, in `build.gradle`), JEI-Plugin für einen portierten Mod, Prüfen, Vorlage

### Community 76 - "net.minecraft.world.entity.player.Player"
Cohesion: 0.14
Nodes (6): ArmorSet, SCALE, THIEF, TRIBAL, SetBonuses, State

### Community 77 - "OldWorld"
Cohesion: 0.07
Nodes (4): Quicksand, Listener, Column, OldWorld

### Community 79 - "OffLawn.java"
Cohesion: 0.08
Nodes (4): HexxitGear, MoreBows, OffLawn, SoulShards

### Community 80 - "InvTweaks"
Cohesion: 0.08
Nodes (3): InvTweaks, Scheduled, InvTweaksClient

### Community 83 - ".get"
Cohesion: 0.08
Nodes (7): InvTweaksGuiModNotWorking, InvTweaksGuiSettings, InvTweaksGuiSettingsAbstract, TextButton, InvTweaksGuiSettingsAdvanced, InvTweaksGuiShortcutsHelp, InvTweaksKeys

### Community 84 - "Client-Darstellung in 26.3 (NeoForge 26.3.0.102)"
Cohesion: 0.22
Nodes (8): Client-Darstellung in 26.3 (NeoForge 26.3.0.102), Etwas am Spielermodell zeichnen (Rücken, Schultern), Halbdurchsichtige Blöcke (Alpha im Renderer), HUD (statt `ITickHandler` mit `TickType.RENDER`), Nur-Client-Mod sauber laden, Rechtsklick vor dem Block (Original: `PlayerInteractEvent` mit `RIGHT_CLICK_BLOCK`), Rüstung: Texturen und eigene Helm-Modelle (`ItemArmor`, `getArmorTexture`, `getArmorModel`), Stolperfallen

### Community 87 - "GuiCustomLan"
Cohesion: 0.10
Nodes (5): CustomLanClient, GuiCustomLan, GuiCustomLanPermissions, CLOptions, CLPermissions

### Community 93 - "OldStack"
Cohesion: 0.18
Nodes (6): MinecartChest, Category, Entry, OldStack, Chest, Dispenser

### Community 95 - "InvTweaksConfigSortingRule"
Cohesion: 0.08
Nodes (7): InvTweaksConfigInventoryRuleset, InvTweaksConfigSortingRule, Type, COLUMN, RECTANGLE, ROW, SLOT

### Community 96 - "LegendOak"
Cohesion: 0.24
Nodes (9): Acuteness, LOOSE, TIGHT, TIGHTER, Bend, LEFT, RIGHT, STRAIGHT (+1 more)

### Community 97 - "InvTweaksHandlerShortcuts"
Cohesion: 0.11
Nodes (16): Action, DROP, MOVE, InvTweaksHandlerShortcuts, Scope, ALL_ITEMS, EVERYTHING, ONE_ITEM (+8 more)

### Community 106 - "WorldGen152.java"
Cohesion: 0.15
Nodes (3): RFTL, TCLog, WorldGen152

### Community 108 - "convert_tree.py"
Cohesion: 0.21
Nodes (8): camel(), convert(), emit_additions(), walk(), main(), Out, resolve_tags(), get()

### Community 109 - "QuarterLogBlock"
Cohesion: 0.17
Nodes (6): Bark, NE, NW, SE, SW, QuarterLogBlock

### Community 111 - "BackToolsClient"
Cohesion: 0.27
Nodes (3): Back, BackItem, BackToolsClient

### Community 112 - ".generate"
Cohesion: 0.21
Nodes (4): DesertWell, Lake, OldLakeGens, Spring

### Community 115 - "Type"
Cohesion: 0.20
Nodes (9): Type, DROP, MOVE_ALL_ITEMS, MOVE_DOWN, MOVE_EVERYTHING, MOVE_ONE_ITEM, MOVE_ONE_STACK, MOVE_TO_SPECIFIC_HOTBAR_SLOT (+1 more)

### Community 118 - "Bäume und Äxte bei TreeCapitator anmelden"
Cohesion: 0.40
Nodes (4): Bäume: Daten-Registry `treecapitator:tree`, Bäume und Äxte bei TreeCapitator anmelden, Prüfen, Äxte und Scheren: Item-Tags

## Knowledge Gaps
- **118 isolated node(s):** `INVENTORY`, `INVENTORY_HOTBAR`, `INVENTORY_NOT_HOTBAR`, `CHEST`, `CRAFTING_IN` (+113 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 654 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **54 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `OldWorld` connect `OldWorld` to `StructureBoundingBox`, `Arbeitsplan: Hexxit 1.0.11 → NeoForge 26.3`, `.generate`, `OldBlockInfo`, `EbxlWorldGen`, `StructureComponent`, `BigTree`, `Decorate`, `OldGen`, `net.minecraft.world.phys.shapes.VoxelShape`, `.tunnel`, `OldDecoration`, `OldRails`, `hexxit.worldgen152.core.populate.OldGen`, `.getBlockId`, `.setBlock`, `.setBlock`, `.func_74992_a`, `OldTerrainGen`, `.addComponentParts`, `LegendOak`, `.generate`?**
  _High betweenness centrality (0.116) - this node is a cross-community bridge._
- **What connects `INVENTORY`, `INVENTORY_HOTBAR`, `INVENTORY_NOT_HOTBAR` to the rest of the system?**
  _118 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `StructureBoundingBox` be split into smaller, more focused modules?**
  _Cohesion score 0.13333333333333333 - nodes in this community are weakly interconnected._
- **Why does `Arbeitsplan: Hexxit 1.0.11 → NeoForge 26.3` connect `Arbeitsplan: Hexxit 1.0.11 → NeoForge 26.3` to `OldTerrainGen`, `.setBlock`, `net.minecraft.world.level.Level`?**
  _High betweenness centrality (0.048) - this node is a cross-community bridge._
- **Should `Layer` be split into smaller, more focused modules?**
  _Cohesion score 0.08831306187202538 - nodes in this community are weakly interconnected._
- **Why does `InvTweaksConfig` connect `InvTweaksConfig` to `InvTweaksHandlerShortcuts`, `InvTweaksItemTree`, `InvTweaksConfig.java`, `InvTweaks`, `.get`, `InvTweaksContainerManager.java`, `map`, `InvTweaksConfigSortingRule`?**
  _High betweenness centrality (0.032) - this node is a cross-community bridge._
- **Should `ImportOriginalAssets.groovy` be split into smaller, more focused modules?**
  _Cohesion score 0.09333333333333334 - nodes in this community are weakly interconnected._