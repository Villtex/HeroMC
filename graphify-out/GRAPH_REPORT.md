# Graph Report - HeroMC  (2026-11-15)

## Corpus Check
- 1150 files · ~238,000 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 25 file(s) not represented in the graph (top: .properties 21, (none) 1, .toml 1)

## Summary
- 3757 nodes · 11941 edges · 131 communities (74 shown, 57 thin omitted)
- Extraction: 92% EXTRACTED · 8% INFERRED · 0% AMBIGUOUS · INFERRED: 966 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `fc296771`
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
- OldCarver
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
- net.minecraft.core.BlockPos
- ArmorBarHud
- minecraft-dev
- InvTweaksContainerManager
- BackItemLayer.java
- net.minecraft.world.item.ItemStack
- .tunnel
- net.minecraft.world.level.Level
- OldBiomeSource.java
- NaturaGen
- FrostArrow.java
- OldRails
- EnchantingPlus.java
- net.neoforged.bus.api.IEventBus
- ArmorBarHud.java
- biomes.py
- PerlinNoise
- StructurePieceBlockSelector
- BackToolsClient.java
- hexxit.worldgen152.core.populate.OldGen
- .getBlockId
- .setBlock
- InvTweaksConfig
- net.minecraft.server.level.ServerLevel
- compare_original.py
- net.minecraft.world.item.Item
- verify_blocks.py
- NaturaClient.java
- OldBlockStates
- net.minecraft.core.Direction
- TableEvents.java
- NaturaBlocks.java
- SoulEvents.java
- ScarecrowEntity
- EbxlClient.java
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
- net.minecraft.world.level.Level
- net.minecraft.world.entity.player.Player
- .populate
- hexxit.worldgen152.core.populate.OldWorld
- NaturaItems.java
- InvTweaks
- HexxitGearClient.java
- ScarecrowRenderer
- .get
- Client-Darstellung in 26.3 (NeoForge 26.3.0.102)
- BeanstalkBlock.java
- ScarecrowModel.java
- GuiCustomLan
- .addComponentParts
- InvTweaksClient.java
- net.minecraft.world.level.block.Block
- map
- net.minecraft.client.model.geom.builders.LayerDefinition
- OldStack
- .helmet
- InvTweaksConfigSortingRule
- net.minecraft.world.level.block.state.BlockState
- InvTweaksHandlerShortcuts
- LanCommands
- net.minecraft.world.item.Item
- InvTweaksItemTree
- InvTweaksHandlerSorting
- net.minecraft.resources.Identifier
- NaturaTree
- WorldGen152.java
- InvTweaksItemTreeLoader.java
- convert_tree.py
- QuarterLogBlock
- .copyVanilla
- Wood
- SakuraTreeGen
- GuiCustomLanPermissions
- Ore
- InvTweaksItemTree
- Bäume und Äxte bei TreeCapitator anmelden
- HeroMC – Hexxit 1:1 für Minecraft 26.3
- TableEvents
- SaguaroGen
- Weltgenerierung eines 1.5.2-Mods in worldgen152 einhängen
- Listener
- Populate
- .generate
- .generate
- VillageHandler
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
- `Teil 1 – Vanilla-1.5.2-Dekoration` --references--> `OldDecorator`  [INFERRED]
  docs/PLAN.md → mods/worldgen152/src/main/java/hexxit/worldgen152/core/populate/OldDecorator.java
- `Teil 12 und 13 – Better Dungeons (Chocolate Quest)` --references--> `VillageHandler`  [INFERRED]
  docs/PLAN.md → mods/worldgen152/src/main/java/hexxit/worldgen152/core/structure/OldStructures.java
- `Aufbau` --references--> `OldDecorator`  [INFERRED]
  .claude/skills/worldgen152/SKILL.md → mods/worldgen152/src/main/java/hexxit/worldgen152/core/populate/OldDecorator.java
- `Aufbau` --references--> `OldDungeonGen`  [INFERRED]
  .claude/skills/worldgen152/SKILL.md → mods/worldgen152/src/main/java/hexxit/worldgen152/core/populate/OldDungeonGen.java

## Import Cycles
- None detected.

## Communities (131 total, 57 thin omitted)

### Community 0 - "StructureBoundingBox"
Cohesion: 0.15
Nodes (4): Witch, Spawner, OldWorld, StructureBoundingBox

### Community 1 - "Layer"
Cohesion: 0.09
Nodes (18): Layer, AddIsland, AddMushroomIsland, AddSnow, Biome, FuzzyZoom, Hills, Island (+10 more)

### Community 2 - "reference.py"
Cohesion: 0.18
Nodes (10): build_mappings(), digest(), download(), inner_archives(), java(), main(), minecraft_reference(), process() (+2 more)

### Community 3 - ".server"
Cohesion: 0.14
Nodes (7): Container → `AbstractContainerMenu`, Konfiguration und Spielerdaten, Menü, Screen und Pakete in 26.3 (NeoForge 26.3.0.102), Pakete → Custom Payloads, Truhen-GUIs und Inventory Tweaks, Vanilla-Block ersetzen (Mod hat Block-ID überschrieben), Slot

### Community 4 - "net.minecraft.world.item.ItemStack"
Cohesion: 0.14
Nodes (5): EnchantHelper, OldCost, Restriction, EnchantTableMenu, Settings

### Community 7 - "OldTerrain"
Cohesion: 0.19
Nodes (3): BiomeLayout, OctaveNoise, OldTerrain

### Community 8 - "Arbeitsplan: Hexxit 1.0.11 → NeoForge 26.3"
Cohesion: 0.09
Nodes (22): Arbeitsplan: Hexxit 1.0.11 → NeoForge 26.3, Regeln für jeden Teil, Teil 10 und 11 – Project Zulu, Teil 14 – Battle Towers, Ruins, Teil 15 – Dimensional Doors, Teil 16 – BiblioCraft, Teil 17 – BetterStorage, Backpacks, qCraft, Teil 18 – ChocoCraft, Infernal Mobs (+14 more)

### Community 11 - "HeroMC – Hinweise für Claude"
Cohesion: 0.29
Nodes (6): Arbeitsweise beim Portieren, Branches, graphify, HeroMC – Hinweise für Claude, Skills anlegen, Tokens sparen

### Community 17 - "OldCarver"
Cohesion: 0.10
Nodes (5): OldCarver, OldCaves, OldMath, OldRavines, Minable

### Community 18 - "OldBlockInfo"
Cohesion: 0.06
Nodes (38): NaturaWorldGen, OldBlocks, Material, AIR, CACTUS, CIRCUITS, CLAY, CLOTH (+30 more)

### Community 19 - "OldDecoratedChunk"
Cohesion: 0.09
Nodes (5): OldChunk, OldDecoratedChunk, OldDecoration, OldChunkWriter, Expect

### Community 20 - "net.minecraft.world.entity.EntityType"
Cohesion: 0.11
Nodes (4): Truhen und Spawner, HomeDimensions, SoulShardItem, OldGameIds

### Community 21 - "EbxlWorldGen"
Cohesion: 0.09
Nodes (7): Choices, EbxlWorldGen, Jungle, Weighted, OffLawnGen, DesertWell, Column

### Community 22 - "StructureComponent"
Cohesion: 0.07
Nodes (14): ComponentVillageChurch, ComponentVillageField2, ComponentVillageField, ComponentVillageHall, ComponentVillageHouse1, ComponentVillageHouse2, ComponentVillageHouse3, ComponentVillageHouse4_Garden (+6 more)

### Community 24 - "Decorate"
Cohesion: 0.13
Nodes (15): Decorate, BIG_SHROOM, CACTUS, CLAY, DEAD_BUSH, FLOWERS, GRASS, LAKE (+7 more)

### Community 25 - "OldStructures"
Cohesion: 0.08
Nodes (8): TerrainSource, Found, OldStructures, StructureMineshaftStart, StructureScatteredFeatureStart, StructureStart, StructureStrongholdStart, StructureVillageStart

### Community 26 - ".findIntersecting"
Cohesion: 0.07
Nodes (19): ComponentMineshaftStairs, ComponentStrongholdChestCorridor, ComponentStrongholdCrossing, ComponentStrongholdLeftTurn, ComponentStrongholdLibrary, ComponentStrongholdPortalRoom, ComponentStrongholdPrison, ComponentStrongholdRightTurn (+11 more)

### Community 27 - "OldGen"
Cohesion: 0.08
Nodes (10): BaseCropWorldgen, Cactus, DeadBush, Flowers, OldPlantGens, Pumpkin, Reed, TallGrass (+2 more)

### Community 28 - "net.minecraft.core.BlockPos"
Cohesion: 0.09
Nodes (5): HexbiscusBlock, HeatSandBlock, NaturaPlantBlock, FloraBlock, IcicleBlock

### Community 30 - "ArmorBarHud"
Cohesion: 0.14
Nodes (10): Kind, AMOUNT, CHEST, DURABILITY, FEET, HEAD, LEGS, Unit (+2 more)

### Community 31 - "minecraft-dev"
Cohesion: 0.50
Nodes (3): npx, minecraft-dev, @mcdxai/minecraft-dev-mcp

### Community 32 - "InvTweaksContainerManager"
Cohesion: 0.07
Nodes (18): ContainerSection, ARMOR, BREWING_BOTTLES, BREWING_INGREDIENT, CHEST, CRAFTING_IN, CRAFTING_IN_PERSISTENT, CRAFTING_OUT (+10 more)

### Community 34 - "net.minecraft.world.item.ItemStack"
Cohesion: 0.14
Nodes (7): Kind, ENDER, FLAME, FROST, MULTI, PLAIN, MoreBowItem

### Community 35 - ".tunnel"
Cohesion: 0.11
Nodes (6): EbxlBiomes, Decoration, GrassChooser, OldBiome, TreeChooser, OldBiomes

### Community 36 - "net.minecraft.world.level.Level"
Cohesion: 0.08
Nodes (4): TreeCapitator, Data, TreeDefinition, TreeRegistry

### Community 38 - "NaturaGen"
Cohesion: 0.04
Nodes (12): BaseTreeWorldgen, BerryBushGen, BloodTreeLargeGen, BushTreeGen, DarkwoodGen, FusewoodGen, GlowshroomGenBlueGreen, GlowshroomGenPurple (+4 more)

### Community 41 - "EnchantingPlus.java"
Cohesion: 0.07
Nodes (8): EnchantingPlus, ClientOptionPayload, EnchantPayload, EplusNetwork, OpenModTablePayload, UseModPayload, Soul, SoulShards

### Community 42 - "net.neoforged.bus.api.IEventBus"
Cohesion: 0.06
Nodes (11): ArmorBarMod, BackTools, bspkrsCoreMod, BuffBarMod, CustomLAN, InvTweaksMod, MoreBows, Natura (+3 more)

### Community 44 - "biomes.py"
Cohesion: 0.15
Nodes (9): ColorMaps, f32(), fixed(), hexcolor(), main(), multiply(), sky_color(), tinted() (+1 more)

### Community 46 - "StructurePieceBlockSelector"
Cohesion: 0.09
Nodes (7): ComponentScatteredFeature, ComponentScatteredFeatureDesertPyramid, ComponentScatteredFeatureJunglePyramid, ComponentScatteredFeatureSwampHut, StructurePieceBlockSelector, StructureScatteredFeatureStones, StructureStrongholdStones

### Community 47 - "BackToolsClient.java"
Cohesion: 0.08
Nodes (4): Back, BackItem, BackToolsClient, EplusClient

### Community 48 - "hexxit.worldgen152.core.populate.OldGen"
Cohesion: 0.06
Nodes (13): CatTail, EbxlPlantGens, Flowers, LeafPile, Marsh, Melon, Oasis, Quicksand (+5 more)

### Community 49 - ".getBlockId"
Cohesion: 0.08
Nodes (17): Acacia, Acuteness, LOOSE, TIGHT, TIGHTER, Autumn, Bend, LEFT (+9 more)

### Community 50 - ".setBlock"
Cohesion: 0.10
Nodes (13): OldDecorator, OldGen, Spring, BigMushroom, Birch, Huge, OldTreeGens, Pine (+5 more)

### Community 51 - "InvTweaksConfig"
Cohesion: 0.09
Nodes (4): InvTweaksConfig, OrderedProperties, InvTweaksConfigManager, InvTweaksHandlerAutoRefill

### Community 53 - "compare_original.py"
Cohesion: 0.19
Nodes (7): cells(), main(), read_nbt(), payload(), string(), take(), region_chunks()

### Community 54 - "net.minecraft.world.item.Item"
Cohesion: 0.09
Nodes (6): EbxlIds, LogTurnerItem, ScarecrowItem, BoneBagItem, SeedBagItem, SunflowerSeedItem

### Community 55 - "verify_blocks.py"
Cohesion: 0.18
Nodes (10): main(), packet(), read(), block(), ebxl(), flag(), main(), test() (+2 more)

### Community 57 - "OldBlockStates"
Cohesion: 0.18
Nodes (3): Extrabiomes, HGWorldGen, OldBlockStates

### Community 60 - "NaturaBlocks.java"
Cohesion: 0.05
Nodes (13): CattailBlock, EbxlPlantBlock, LeafPileBlock, EbxlBlocks, Bark, NE, NW, SE (+5 more)

### Community 63 - "EbxlClient.java"
Cohesion: 0.09
Nodes (4): EbxlClient, FrostArrowRenderer, MoreBowsClient, OffLawnClient

### Community 67 - "EnchantTableScreen"
Cohesion: 0.09
Nodes (4): GuiContainer → `AbstractContainerScreen`, EnchantTableScreen, GuiItem, EnchantException

### Community 68 - "StructureComponent"
Cohesion: 0.08
Nodes (7): ComponentMineshaftCorridor, ComponentMineshaftCross, ComponentMineshaftRoom, ComponentStronghold, ComponentStrongholdStairs2, StructureComponent, StructureMineshaftPieces

### Community 69 - "net.minecraft.world.level.Level"
Cohesion: 0.09
Nodes (4): EbxlSaplingBlock, NaturaSaplingBlock, LawnBlock, SunflowerBlock

### Community 70 - "net.minecraft.resources.Identifier"
Cohesion: 0.08
Nodes (8): ChestTransporter, ChestTransporterJeiPlugin, EbxlJeiPlugin, HexxitGearJeiPlugin, MoreBowsJeiPlugin, NaturaJeiPlugin, ObsidiPlatesJeiPlugin, SoulShardsJeiPlugin

### Community 71 - ".func_74992_a"
Cohesion: 0.18
Nodes (3): ComponentStrongholdCorridor, ComponentVillagePathGen, ComponentVillageRoadPiece

### Community 72 - "OldTerrainGen"
Cohesion: 0.27
Nodes (5): Aufbau, Teil 1 – Vanilla-1.5.2-Dekoration, Entry, OldTerrainGen, WorldGenerator

### Community 73 - "EnchantingCategory.java"
Cohesion: 0.10
Nodes (4): EnchantingCategory, TableRecipe, EplusConfig, TCSettings

### Community 74 - "JEI-Plugin für einen portierten Mod"
Cohesion: 0.40
Nodes (4): Einbindung (schon erledigt, in `build.gradle`), JEI-Plugin für einen portierten Mod, Prüfen, Vorlage

### Community 76 - "net.minecraft.world.entity.player.Player"
Cohesion: 0.13
Nodes (6): ArmorSet, SCALE, THIEF, TRIBAL, SetBonuses, State

### Community 78 - "hexxit.worldgen152.core.populate.OldWorld"
Cohesion: 0.08
Nodes (3): EucalyptusTreeGenShort, RedwoodTreeGen, WillowGen

### Community 79 - "NaturaItems.java"
Cohesion: 0.10
Nodes (4): HexxitGear, KamaItem, NaturaItems, ToolSet

### Community 80 - "InvTweaks"
Cohesion: 0.10
Nodes (3): InvTweaks, Scheduled, InvTweaksClient

### Community 81 - "HexxitGearClient.java"
Cohesion: 0.24
Nodes (3): Teil 5 – Hexxit Gear, More Bows, Enchanting Plus, offLawn, Wild Caves 3, IClientItemExtensions, Skull

### Community 83 - ".get"
Cohesion: 0.05
Nodes (9): InvTweaksGuiModNotWorking, InvTweaksGuiSettings, InvTweaksGuiSettingsAbstract, TextButton, InvTweaksGuiSettingsAdvanced, InvTweaksGuiShortcutsHelp, InvTweaksConst, InvTweaksKeys (+1 more)

### Community 84 - "Client-Darstellung in 26.3 (NeoForge 26.3.0.102)"
Cohesion: 0.22
Nodes (8): Client-Darstellung in 26.3 (NeoForge 26.3.0.102), Etwas am Spielermodell zeichnen (Rücken, Schultern), Halbdurchsichtige Blöcke (Alpha im Renderer), HUD (statt `ITickHandler` mit `TickType.RENDER`), Nur-Client-Mod sauber laden, Rechtsklick vor dem Block (Original: `PlayerInteractEvent` mit `RIGHT_CLICK_BLOCK`), Rüstung: Texturen und eigene Helm-Modelle (`ItemArmor`, `getArmorTexture`, `getArmorModel`), Stolperfallen

### Community 85 - "BeanstalkBlock.java"
Cohesion: 0.07
Nodes (5): QuicksandBlock, CloudBlock, ThornVinesBlock, PlayerMobPlateBlock, ShadowPickaxeItem

### Community 87 - "GuiCustomLan"
Cohesion: 0.11
Nodes (3): CustomLanClient, GuiCustomLan, CLOptions

### Community 89 - "InvTweaksClient.java"
Cohesion: 0.07
Nodes (3): InvTweaksGuiIconButton, InvTweaksGuiSettingsButton, InvTweaksGuiSortingButton

### Community 90 - "net.minecraft.world.level.block.Block"
Cohesion: 0.09
Nodes (5): Barley, Cotton, NaturaCropBlock, Menu, NaturaWorkbenchBlock

### Community 93 - "OldStack"
Cohesion: 0.24
Nodes (5): MinecartChest, Category, Entry, OldStack, Chest

### Community 95 - "InvTweaksConfigSortingRule"
Cohesion: 0.08
Nodes (7): InvTweaksConfigInventoryRuleset, InvTweaksConfigSortingRule, Type, COLUMN, RECTANGLE, ROW, SLOT

### Community 96 - "net.minecraft.world.level.block.state.BlockState"
Cohesion: 0.08
Nodes (5): EbxlLeavesBlock, BerryBushBlock, NaturaLeavesBlock, SaguaroBlock, SaguaroSproutBlock

### Community 97 - "InvTweaksHandlerShortcuts"
Cohesion: 0.07
Nodes (24): Action, DROP, MOVE, InvTweaksHandlerShortcuts, Scope, ALL_ITEMS, EVERYTHING, ONE_ITEM (+16 more)

### Community 105 - "NaturaTree"
Cohesion: 0.05
Nodes (29): GlowshroomBlock, Growth, FLORA, NETHER, RARE, REDWOOD, Soil, CEILING (+21 more)

### Community 106 - "WorldGen152.java"
Cohesion: 0.14
Nodes (3): RFTL, TCLog, WorldGen152

### Community 108 - "convert_tree.py"
Cohesion: 0.21
Nodes (8): camel(), convert(), emit_additions(), walk(), main(), Out, resolve_tags(), get()

### Community 109 - "QuarterLogBlock"
Cohesion: 0.17
Nodes (6): Bark, NE, NW, SE, SW, QuarterLogBlock

### Community 112 - "Wood"
Cohesion: 0.09
Nodes (14): Wood, AMARANTH, BLOODWOOD, DARKWOOD, EUCALYPTUS, FUSEWOOD, GHOSTWOOD, HOPSEED (+6 more)

### Community 116 - "Ore"
Cohesion: 0.15
Nodes (9): Ore, COAL, DIAMOND, DIRT, GOLD, GRAVEL, IRON, LAPIS (+1 more)

### Community 118 - "Bäume und Äxte bei TreeCapitator anmelden"
Cohesion: 0.40
Nodes (4): Bäume: Daten-Registry `treecapitator:tree`, Bäume und Äxte bei TreeCapitator anmelden, Prüfen, Äxte und Scheren: Item-Tags

### Community 119 - "HeroMC – Hexxit 1:1 für Minecraft 26.3"
Cohesion: 0.18
Nodes (9): Arbeitsplan, Aufbau, Bauen und Starten, HeroMC – Hexxit 1:1 für Minecraft 26.3, Referenzquellen des Originals, Status, Vorgaben, Wann ein Mod als portiert gilt (+1 more)

### Community 122 - "Weltgenerierung eines 1.5.2-Mods in worldgen152 einhängen"
Cohesion: 0.22
Nodes (8): Biome und Dekoratoren, Blöcke anmelden, Bäume im Spiel (Setzlinge), Prüfen, Regel 1: Kern-Klassen ohne Spielklassen, Stolperfallen, Strukturen, Weltgenerierung eines 1.5.2-Mods in worldgen152 einhängen

### Community 124 - "Populate"
Cohesion: 0.29
Nodes (5): Populate, DUNGEON, ICE, LAKE, LAVA

## Knowledge Gaps
- **159 isolated node(s):** `EUCALYPTUS`, `SAKURA`, `GHOSTWOOD`, `REDWOOD`, `BLOODWOOD` (+154 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 776 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **57 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `OldWorld` connect `StructureBoundingBox` to `OldCarver`, `OldBlockInfo`, `OldDecoratedChunk`, `EbxlWorldGen`, `BigTree`, `OldStructures`, `OldGen`, `.tunnel`, `NaturaGen`, `OldRails`, `hexxit.worldgen152.core.populate.OldGen`, `.getBlockId`, `.setBlock`, `.setBlock`, `.func_74992_a`, `OldTerrainGen`, `.populate`, `hexxit.worldgen152.core.populate.OldWorld`, `.addComponentParts`, `Ore`, `Listener`, `Populate`?**
  _High betweenness centrality (0.094) - this node is a cross-community bridge._
- **What connects `EUCALYPTUS`, `SAKURA`, `GHOSTWOOD` to the rest of the system?**
  _159 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `StructureBoundingBox` be split into smaller, more focused modules?**
  _Cohesion score 0.1491274457958752 - nodes in this community are weakly interconnected._
- **Why does `OldBiome` connect `.tunnel` to `Layer`, `OldBiomeSource.java`, `OldTerrain`, `OldCarver`, `OldBlockInfo`, `.setBlock`, `Ore`, `OldGen`?**
  _High betweenness centrality (0.040) - this node is a cross-community bridge._
- **Should `Layer` be split into smaller, more focused modules?**
  _Cohesion score 0.08831306187202538 - nodes in this community are weakly interconnected._
- **Why does `InvTweaks` connect `InvTweaks` to `net.minecraft.world.item.ItemStack`, `WorldGen152.java`, `.get`, `InvTweaksConfig`, `InvTweaksClient.java`?**
  _High betweenness centrality (0.029) - this node is a cross-community bridge._
- **Should `.server` be split into smaller, more focused modules?**
  _Cohesion score 0.14166666666666666 - nodes in this community are weakly interconnected._