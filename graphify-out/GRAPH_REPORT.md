# Graph Report - HeroMC  (2026-11-16)

## Corpus Check
- 2871 files · ~284,170 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 25 file(s) not represented in the graph (top: .properties 21, (none) 1, .toml 1)

## Summary
- 3765 nodes · 11949 edges · 132 communities (75 shown, 57 thin omitted)
- Extraction: 92% EXTRACTED · 8% INFERRED · 0% AMBIGUOUS · INFERRED: 967 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `b63df733`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- StructureBoundingBox
- Layer
- reference.py
- Menü, Screen und Pakete in 26.3 (NeoForge 26.3.0.102)
- net.minecraft.world.item.ItemStack
- expect.sh
- ImportOriginalAssets.groovy
- OldTerrain
- Arbeitsplan: Hexxit 1.0.11 → NeoForge 26.3
- OldChunkGenerator.java
- HeroMC – Hinweise für Claude
- .claude/CLAUDE.md
- OldBiome
- OldBlockInfo
- OldDecoratedChunk
- net.minecraft.world.entity.EntityType
- EbxlWorldGen
- StructureComponent
- BigTree
- Decorate
- OldStructures
- .findIntersecting
- net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
- net.minecraft.core.BlockPos
- ArmorBarHud
- minecraft-dev
- InvTweaksContainerManager
- BackItemLayer.java
- net.minecraft.world.item.ItemStack
- FrostArrow
- net.minecraft.world.level.Level
- OldBiomeSource.java
- NaturaGen
- OldRails
- EnchantingPlus.java
- net.neoforged.bus.api.IEventBus
- ArmorBarHud.java
- biomes.py
- PerlinNoise
- StructurePieceBlockSelector
- EplusConfig
- hexxit.worldgen152.core.populate.OldGen
- .getBlockId
- .setBlock
- InvTweaksConfig
- net.minecraft.server.level.ServerLevel
- compare_original.py
- net.minecraft.world.level.block.Block
- verify_blocks.py
- NaturaClient.java
- OldBlockStates
- net.minecraft.core.Direction
- TableEvents.java
- NaturaBlocks
- net.minecraft.network.chat.Component
- BackToolsClient
- MoreBowsClient
- original_server.sh
- .setBlock
- net.minecraft.world.item.ItemStack
- EnchantTableScreen
- StructureComponent
- net.minecraft.world.level.Level
- net.minecraft.resources.Identifier
- .func_74992_a
- OldTerrainGen
- EnchantingCategory.java
- JEI-Plugin für einen portierten Mod
- InvTweaksKeys
- net.minecraft.world.entity.player.Player
- .populate
- hexxit.worldgen152.core.populate.OldWorld
- NaturaItems.java
- InvTweaks
- HexxitGearClient.java
- ScarecrowRenderer
- .get
- Client-Darstellung in 26.3 (NeoForge 26.3.0.102)
- net.minecraft.world.level.Level
- ScarecrowModel.java
- CLOptions
- NaturaWorkbenchBlock.java
- net.minecraft.client.model.geom.builders.LayerDefinition
- OldStack
- FrostArrowRenderer
- InvTweaksItemTree
- net.minecraft.world.level.block.state.BlockState
- Type
- LanCommands
- Holzsets und Pflanzen in 26.3
- InvTweaksHandlerSorting
- net.minecraft.resources.Identifier
- NaturaTree
- Expect.java
- convert_tree.py
- net.minecraft.world.item.context.BlockPlaceContext
- .copyVanilla
- CattailBlock
- SakuraTreeGen
- OffLawn.java
- GuiCustomLanPermissions
- Ore
- Scope
- Bäume und Äxte bei TreeCapitator anmelden
- HeroMC – Hexxit 1:1 für Minecraft 26.3
- EnchantTableMenu
- BaseTreeWorldgen
- Weltgenerierung eines 1.5.2-Mods in worldgen152 einhängen
- Listener
- Populate
- .generate
- .generate
- VillageHandler
- Target
- NaturaIds

## God Nodes (most connected - your core abstractions)
1. `OldWorld` - 251 edges
2. `StructureBoundingBox` - 111 edges
3. `OldBlockInfo` - 76 edges
4. `StructureComponent` - 67 edges
5. `OldLoot` - 65 edges
6. `OldTile` - 65 edges
7. `OldEntity` - 63 edges
8. `NaturaBlocks` - 61 edges
9. `RedwoodTreeGen` - 55 edges
10. `InvTweaksContainerManager` - 48 edges

## Surprising Connections (you probably didn't know these)
- `Bäume im Spiel (Setzlinge)` --references--> `OldGen`  [INFERRED]
  .claude/skills/worldgen152/SKILL.md → mods/worldgen152/src/main/java/hexxit/worldgen152/core/populate/OldGen.java
- `Teil 12 und 13 – Better Dungeons (Chocolate Quest)` --references--> `VillageHandler`  [INFERRED]
  docs/PLAN.md → mods/worldgen152/src/main/java/hexxit/worldgen152/core/structure/OldStructures.java
- `Aufbau` --references--> `OldDecorator`  [INFERRED]
  .claude/skills/worldgen152/SKILL.md → mods/worldgen152/src/main/java/hexxit/worldgen152/core/populate/OldDecorator.java
- `Teil 1 – Vanilla-1.5.2-Dekoration` --references--> `OldDecorator`  [INFERRED]
  docs/PLAN.md → mods/worldgen152/src/main/java/hexxit/worldgen152/core/populate/OldDecorator.java
- `Aufbau` --references--> `OldGen`  [INFERRED]
  .claude/skills/worldgen152/SKILL.md → mods/worldgen152/src/main/java/hexxit/worldgen152/core/populate/OldGen.java

## Import Cycles
- None detected.

## Communities (132 total, 57 thin omitted)

### Community 1 - "Layer"
Cohesion: 0.08
Nodes (18): Layer, AddIsland, AddMushroomIsland, AddSnow, Biome, FuzzyZoom, Hills, Island (+10 more)

### Community 2 - "reference.py"
Cohesion: 0.18
Nodes (10): build_mappings(), digest(), download(), inner_archives(), java(), main(), minecraft_reference(), process() (+2 more)

### Community 3 - "Menü, Screen und Pakete in 26.3 (NeoForge 26.3.0.102)"
Cohesion: 0.25
Nodes (5): Konfiguration und Spielerdaten, Menü, Screen und Pakete in 26.3 (NeoForge 26.3.0.102), Pakete → Custom Payloads, Truhen-GUIs und Inventory Tweaks, Vanilla-Block ersetzen (Mod hat Block-ID überschrieben)

### Community 4 - "net.minecraft.world.item.ItemStack"
Cohesion: 0.14
Nodes (3): EnchantHelper, OldCost, Restriction

### Community 7 - "OldTerrain"
Cohesion: 0.14
Nodes (4): OctaveNoise, OldChunk, OldMath, OldTerrain

### Community 8 - "Arbeitsplan: Hexxit 1.0.11 → NeoForge 26.3"
Cohesion: 0.10
Nodes (20): Arbeitsplan: Hexxit 1.0.11 → NeoForge 26.3, Regeln für jeden Teil, Teil 10 und 11 – Project Zulu, Teil 14 – Battle Towers, Ruins, Teil 15 – Dimensional Doors, Teil 16 – BiblioCraft, Teil 17 – BetterStorage, Backpacks, qCraft, Teil 18 – ChocoCraft, Infernal Mobs (+12 more)

### Community 11 - "HeroMC – Hinweise für Claude"
Cohesion: 0.29
Nodes (6): Arbeitsweise beim Portieren, Branches, graphify, HeroMC – Hinweise für Claude, Skills anlegen, Tokens sparen

### Community 17 - "OldBiome"
Cohesion: 0.06
Nodes (10): EbxlBiomes, Decoration, GrassChooser, OldBiome, TreeChooser, OldBiomes, OldCarver, OldCaves (+2 more)

### Community 18 - "OldBlockInfo"
Cohesion: 0.06
Nodes (41): EbxlIds, HGWorldGen, NaturaWorldGen, OldBlocks, Material, AIR, CACTUS, CIRCUITS (+33 more)

### Community 19 - "OldDecoratedChunk"
Cohesion: 0.11
Nodes (3): OldDecoratedChunk, OldDecoration, OldChunkWriter

### Community 20 - "net.minecraft.world.entity.EntityType"
Cohesion: 0.07
Nodes (4): ScarecrowEntity, HomeDimensions, SoulEvents, SoulShardItem

### Community 21 - "EbxlWorldGen"
Cohesion: 0.14
Nodes (5): Choices, EbxlWorldGen, Jungle, Weighted, DesertWell

### Community 22 - "StructureComponent"
Cohesion: 0.06
Nodes (17): Teil 3 – JEI-Grundlage und ExtrabiomesXL fertig, ComponentVillage, ComponentVillageChurch, ComponentVillageField2, ComponentVillageField, ComponentVillageHall, ComponentVillageHouse1, ComponentVillageHouse2 (+9 more)

### Community 24 - "Decorate"
Cohesion: 0.13
Nodes (15): Decorate, BIG_SHROOM, CACTUS, CLAY, DEAD_BUSH, FLOWERS, GRASS, LAKE (+7 more)

### Community 25 - "OldStructures"
Cohesion: 0.10
Nodes (7): TerrainSource, Found, OldStructures, StructureMineshaftStart, StructureScatteredFeatureStart, StructureStart, StructureVillageStart

### Community 26 - ".findIntersecting"
Cohesion: 0.07
Nodes (18): ComponentStrongholdChestCorridor, ComponentStrongholdCrossing, ComponentStrongholdLeftTurn, ComponentStrongholdLibrary, ComponentStrongholdPortalRoom, ComponentStrongholdPrison, ComponentStrongholdRightTurn, ComponentStrongholdRoomCrossing (+10 more)

### Community 27 - "net.minecraft.client.gui.screens.inventory.AbstractContainerScreen"
Cohesion: 0.15
Nodes (3): InvTweaksGuiIconButton, InvTweaksGuiSettingsButton, InvTweaksGuiSortingButton

### Community 28 - "net.minecraft.core.BlockPos"
Cohesion: 0.06
Nodes (8): EbxlPlantBlock, HexbiscusBlock, HeatSandBlock, LargeGlowshroomBlock, NaturaPlantBlock, FloraBlock, IcicleBlock, StalactiteBlock

### Community 30 - "ArmorBarHud"
Cohesion: 0.14
Nodes (10): Kind, AMOUNT, CHEST, DURABILITY, FEET, HEAD, LEGS, Unit (+2 more)

### Community 31 - "minecraft-dev"
Cohesion: 0.50
Nodes (3): npx, minecraft-dev, @mcdxai/minecraft-dev-mcp

### Community 32 - "InvTweaksContainerManager"
Cohesion: 0.06
Nodes (22): ContainerSection, ARMOR, BREWING_BOTTLES, BREWING_INGREDIENT, CHEST, CRAFTING_IN, CRAFTING_IN_PERSISTENT, CRAFTING_OUT (+14 more)

### Community 34 - "net.minecraft.world.item.ItemStack"
Cohesion: 0.16
Nodes (7): Kind, ENDER, FLAME, FROST, MULTI, PLAIN, MoreBowItem

### Community 36 - "net.minecraft.world.level.Level"
Cohesion: 0.07
Nodes (6): Teil 6 – bspkrsCore, TreeCapitator, Inventory Tweaks, Custom LAN Ports, TreeCapitator, TreeCapitatorMod, Data, TreeDefinition, TreeRegistry

### Community 38 - "NaturaGen"
Cohesion: 0.07
Nodes (6): BerryBushGen, DarkwoodGen, NaturaGen, NetherBerryBushGen, RareTreeGen, WhiteTreeGen

### Community 41 - "EnchantingPlus.java"
Cohesion: 0.08
Nodes (7): EnchantingPlus, ClientOptionPayload, EnchantPayload, OpenModTablePayload, UseModPayload, Soul, SoulShards

### Community 42 - "net.neoforged.bus.api.IEventBus"
Cohesion: 0.05
Nodes (11): ArmorBarMod, BackTools, bspkrsCoreMod, BuffBarMod, CustomLAN, RFTL, InvTweaksMod, MoreBows (+3 more)

### Community 44 - "biomes.py"
Cohesion: 0.15
Nodes (9): ColorMaps, f32(), fixed(), hexcolor(), main(), multiply(), sky_color(), tinted() (+1 more)

### Community 46 - "StructurePieceBlockSelector"
Cohesion: 0.09
Nodes (7): ComponentScatteredFeature, ComponentScatteredFeatureDesertPyramid, ComponentScatteredFeatureJunglePyramid, ComponentScatteredFeatureSwampHut, StructurePieceBlockSelector, StructureScatteredFeatureStones, StructureStrongholdStones

### Community 47 - "EplusConfig"
Cohesion: 0.11
Nodes (3): EplusClient, EplusConfig, TCSettings

### Community 48 - "hexxit.worldgen152.core.populate.OldGen"
Cohesion: 0.09
Nodes (12): CatTail, EbxlPlantGens, Flowers, LeafPile, Marsh, Melon, Oasis, Quicksand (+4 more)

### Community 49 - ".getBlockId"
Cohesion: 0.09
Nodes (17): Acacia, Acuteness, LOOSE, TIGHT, TIGHTER, Autumn, Bend, LEFT (+9 more)

### Community 50 - ".setBlock"
Cohesion: 0.06
Nodes (21): OldDecorator, OldGen, Spring, Cactus, DeadBush, Flowers, OldPlantGens, Pumpkin (+13 more)

### Community 51 - "InvTweaksConfig"
Cohesion: 0.08
Nodes (4): InvTweaksConfig, OrderedProperties, InvTweaksConfigManager, InvTweaksHandlerAutoRefill

### Community 53 - "compare_original.py"
Cohesion: 0.19
Nodes (7): cells(), main(), read_nbt(), payload(), string(), take(), region_chunks()

### Community 54 - "net.minecraft.world.level.block.Block"
Cohesion: 0.05
Nodes (10): LeafPileBlock, QuarterLogBlock, QuicksandBlock, EbxlBlocks, LogTurnerItem, ScarecrowItem, FusewoodLogBlock, BoneBagItem (+2 more)

### Community 55 - "verify_blocks.py"
Cohesion: 0.18
Nodes (10): main(), packet(), read(), block(), ebxl(), flag(), main(), test() (+2 more)

### Community 56 - "NaturaClient.java"
Cohesion: 0.08
Nodes (4): EbxlClient, ColormapTint, NaturaClient, OffLawnClient

### Community 58 - "net.minecraft.core.Direction"
Cohesion: 0.11
Nodes (3): EbxlLeavesBlock, SaguaroFruitBlock, BeanstalkBlock

### Community 60 - "NaturaBlocks"
Cohesion: 0.06
Nodes (16): NaturaLeavesBlock, NaturaBlocks, Wood, AMARANTH, BLOODWOOD, DARKWOOD, EUCALYPTUS, FUSEWOOD (+8 more)

### Community 62 - "BackToolsClient"
Cohesion: 0.27
Nodes (3): Back, BackItem, BackToolsClient

### Community 65 - ".setBlock"
Cohesion: 0.16
Nodes (6): Generator anmelden, Teil 5 – Hexxit Gear, More Bows, Enchanting Plus, offLawn, Wild Caves 3, WildCavesGen, Clay, Sand, Skull

### Community 67 - "EnchantTableScreen"
Cohesion: 0.14
Nodes (3): GuiContainer → `AbstractContainerScreen`, EnchantTableScreen, GuiItem

### Community 68 - "StructureComponent"
Cohesion: 0.06
Nodes (9): ComponentMineshaftCorridor, ComponentMineshaftCross, ComponentMineshaftRoom, ComponentMineshaftStairs, ComponentStronghold, ComponentStrongholdStairs2, ComponentStrongholdStairs, StructureComponent (+1 more)

### Community 69 - "net.minecraft.world.level.Level"
Cohesion: 0.09
Nodes (4): EbxlSaplingBlock, GlowshroomBlock, LawnBlock, SunflowerBlock

### Community 70 - "net.minecraft.resources.Identifier"
Cohesion: 0.10
Nodes (8): ChestTransporterJeiPlugin, EbxlJeiPlugin, EplusJeiPlugin, HexxitGearJeiPlugin, MoreBowsJeiPlugin, NaturaJeiPlugin, ObsidiPlatesJeiPlugin, SoulShardsJeiPlugin

### Community 72 - "OldTerrainGen"
Cohesion: 0.21
Nodes (5): Aufbau, Teil 1 – Vanilla-1.5.2-Dekoration, Entry, OldTerrainGen, WorldGenerator

### Community 74 - "JEI-Plugin für einen portierten Mod"
Cohesion: 0.40
Nodes (4): Einbindung (schon erledigt, in `build.gradle`), JEI-Plugin für einen portierten Mod, Prüfen, Vorlage

### Community 76 - "net.minecraft.world.entity.player.Player"
Cohesion: 0.14
Nodes (6): ArmorSet, SCALE, THIEF, TRIBAL, SetBonuses, State

### Community 77 - ".populate"
Cohesion: 0.08
Nodes (4): OffLawnGen, Lake, OldPopulator, Column

### Community 78 - "hexxit.worldgen152.core.populate.OldWorld"
Cohesion: 0.06
Nodes (5): BloodTreeLargeGen, BushTreeGen, EucalyptusTreeGenShort, RedwoodTreeGen, WillowGen

### Community 79 - "NaturaItems.java"
Cohesion: 0.07
Nodes (6): Truhen und Spawner, HexxitGear, KamaItem, NaturaItems, ToolSet, OldGameIds

### Community 80 - "InvTweaks"
Cohesion: 0.09
Nodes (3): InvTweaks, Scheduled, InvTweaksClient

### Community 83 - ".get"
Cohesion: 0.07
Nodes (8): InvTweaksGuiModNotWorking, InvTweaksGuiSettings, InvTweaksGuiSettingsAbstract, TextButton, InvTweaksGuiSettingsAdvanced, InvTweaksGuiShortcutsHelp, InvTweaksConst, InvTweaksLocalization

### Community 84 - "Client-Darstellung in 26.3 (NeoForge 26.3.0.102)"
Cohesion: 0.22
Nodes (8): Client-Darstellung in 26.3 (NeoForge 26.3.0.102), Etwas am Spielermodell zeichnen (Rücken, Schultern), Halbdurchsichtige Blöcke (Alpha im Renderer), HUD (statt `ITickHandler` mit `TickType.RENDER`), Nur-Client-Mod sauber laden, Rechtsklick vor dem Block (Original: `PlayerInteractEvent` mit `RIGHT_CLICK_BLOCK`), Rüstung: Texturen und eigene Helm-Modelle (`ItemArmor`, `getArmorTexture`, `getArmorModel`), Stolperfallen

### Community 85 - "net.minecraft.world.level.Level"
Cohesion: 0.05
Nodes (7): CloudBlock, Barley, Cotton, NaturaCropBlock, ThornVinesBlock, PlayerMobPlateBlock, ShadowPickaxeItem

### Community 90 - "NaturaWorkbenchBlock.java"
Cohesion: 0.16
Nodes (3): Slot, Menu, NaturaWorkbenchBlock

### Community 93 - "OldStack"
Cohesion: 0.13
Nodes (7): OldDungeonGen, MinecartChest, Category, Entry, OldStack, Chest, Spawner

### Community 95 - "InvTweaksItemTree"
Cohesion: 0.05
Nodes (10): InvTweaksConfigInventoryRuleset, InvTweaksConfigSortingRule, Type, COLUMN, RECTANGLE, ROW, SLOT, InvTweaksItemTree (+2 more)

### Community 96 - "net.minecraft.world.level.block.state.BlockState"
Cohesion: 0.11
Nodes (3): BerryBushBlock, SaguaroBlock, SaguaroSproutBlock

### Community 97 - "Type"
Cohesion: 0.20
Nodes (9): Type, DROP, MOVE_ALL_ITEMS, MOVE_DOWN, MOVE_EVERYTHING, MOVE_ONE_ITEM, MOVE_ONE_STACK, MOVE_TO_SPECIFIC_HOTBAR_SLOT (+1 more)

### Community 101 - "Holzsets und Pflanzen in 26.3"
Cohesion: 0.29
Nodes (6): Brennwert und Brennbarkeit, Holzsets und Pflanzen in 26.3, Modelle und Blockstates aus Vanilla erzeugen, Prüfen, Rezeptkonflikte mit Vanilla, Tags (Block + Item)

### Community 105 - "NaturaTree"
Cohesion: 0.06
Nodes (30): Blöcke (Java), Growth, FLORA, NETHER, RARE, REDWOOD, NaturaSaplingBlock, Soil (+22 more)

### Community 108 - "convert_tree.py"
Cohesion: 0.21
Nodes (8): camel(), convert(), emit_additions(), walk(), main(), Out, resolve_tags(), get()

### Community 109 - "net.minecraft.world.item.context.BlockPlaceContext"
Cohesion: 0.10
Nodes (11): Bark, NE, NW, SE, SW, Bark, NE, NW (+3 more)

### Community 114 - "OffLawn.java"
Cohesion: 0.07
Nodes (4): ChestTransporter, ObsidiPlates, OffLawn, WildCaves

### Community 115 - "GuiCustomLanPermissions"
Cohesion: 0.15
Nodes (3): GuiCustomLan, GuiCustomLanPermissions, CLPermissions

### Community 116 - "Ore"
Cohesion: 0.17
Nodes (9): Ore, COAL, DIAMOND, DIRT, GOLD, GRAVEL, IRON, LAPIS (+1 more)

### Community 117 - "Scope"
Cohesion: 0.40
Nodes (5): Scope, ALL_ITEMS, EVERYTHING, ONE_ITEM, ONE_STACK

### Community 118 - "Bäume und Äxte bei TreeCapitator anmelden"
Cohesion: 0.40
Nodes (4): Bäume: Daten-Registry `treecapitator:tree`, Bäume und Äxte bei TreeCapitator anmelden, Prüfen, Äxte und Scheren: Item-Tags

### Community 119 - "HeroMC – Hexxit 1:1 für Minecraft 26.3"
Cohesion: 0.18
Nodes (9): Arbeitsplan, Aufbau, Bauen und Starten, HeroMC – Hexxit 1:1 für Minecraft 26.3, Referenzquellen des Originals, Status, Vorgaben, Wann ein Mod als portiert gilt (+1 more)

### Community 120 - "EnchantTableMenu"
Cohesion: 0.26
Nodes (3): Container → `AbstractContainerMenu`, EnchantTableMenu, Settings

### Community 121 - "BaseTreeWorldgen"
Cohesion: 0.10
Nodes (5): BaseTreeWorldgen, FusewoodGen, GlowshroomGenBlueGreen, GlowshroomGenPurple, SaguaroGen

### Community 122 - "Weltgenerierung eines 1.5.2-Mods in worldgen152 einhängen"
Cohesion: 0.22
Nodes (8): Biome und Dekoratoren, Blöcke anmelden, Bäume im Spiel (Setzlinge), Prüfen, Regel 1: Kern-Klassen ohne Spielklassen, Stolperfallen, Strukturen, Weltgenerierung eines 1.5.2-Mods in worldgen152 einhängen

### Community 124 - "Populate"
Cohesion: 0.33
Nodes (5): Populate, DUNGEON, ICE, LAKE, LAVA

### Community 128 - "Target"
Cohesion: 0.40
Nodes (5): Target, DOWN, HOTBAR_SLOT, UNSPECIFIED, UP

## Knowledge Gaps
- **159 isolated node(s):** `FLORA`, `NETHER`, `RARE`, `REDWOOD`, `CEILING` (+154 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 782 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **57 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `OldWorld` connect `StructureBoundingBox` to `OldBiome`, `OldBlockInfo`, `OldDecoratedChunk`, `EbxlWorldGen`, `StructureComponent`, `BigTree`, `OldStructures`, `OldRails`, `hexxit.worldgen152.core.populate.OldGen`, `.getBlockId`, `.setBlock`, `net.minecraft.world.level.block.Block`, `.setBlock`, `.func_74992_a`, `OldTerrainGen`, `.populate`, `hexxit.worldgen152.core.populate.OldWorld`, `OldStack`, `Listener`, `Populate`?**
  _High betweenness centrality (0.116) - this node is a cross-community bridge._
- **What connects `FLORA`, `NETHER`, `RARE` to the rest of the system?**
  _159 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `StructureBoundingBox` be split into smaller, more focused modules?**
  _Cohesion score 0.1297801866907558 - nodes in this community are weakly interconnected._
- **Why does `OldBiome` connect `OldBiome` to `Layer`, `OldBiomeSource.java`, `OldTerrain`, `OldBlockInfo`, `.setBlock`, `Ore`, `OldStructures`?**
  _High betweenness centrality (0.031) - this node is a cross-community bridge._
- **Should `Layer` be split into smaller, more focused modules?**
  _Cohesion score 0.08432539682539683 - nodes in this community are weakly interconnected._
- **Should `net.minecraft.world.item.ItemStack` be split into smaller, more focused modules?**
  _Cohesion score 0.13636363636363635 - nodes in this community are weakly interconnected._
- **Should `ImportOriginalAssets.groovy` be split into smaller, more focused modules?**
  _Cohesion score 0.09333333333333334 - nodes in this community are weakly interconnected._