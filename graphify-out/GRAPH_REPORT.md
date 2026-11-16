# Graph Report - HeroMC  (2026-11-16)

## Corpus Check
- 3398 files · ~341,124 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 27 file(s) not represented in the graph (top: .properties 23, (none) 1, .toml 1)

## Summary
- 4523 nodes · 14443 edges · 157 communities (92 shown, 65 thin omitted)
- Extraction: 93% EXTRACTED · 7% INFERRED · 0% AMBIGUOUS · INFERRED: 1072 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `2298f38d`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- OldWorld
- Layer
- reference.py
- OldGen
- net.minecraft.core.Holder
- expect.sh
- ImportOriginalAssets.groovy
- OldTerrain
- Arbeitsplan: Hexxit 1.0.11 → NeoForge 26.3
- OldChunkGenerator.java
- HeroMC – Hinweise für Claude
- .claude/CLAUDE.md
- OldBiome
- random
- OldDecoratedChunk
- BoxRecipe.java
- EbxlWorldGen
- StructureComponent
- .isAirBlock
- OldTerrainGen
- OldStructures
- .findIntersecting
- InvTweaksClient.java
- net.minecraft.core.BlockPos
- ArmorBarHud
- minecraft-dev
- InvTweaksContainerManager
- net.minecraft.world.level.Level
- net.minecraft.world.item.ItemStack
- .getNextComponentNormal
- TreeDefinition
- OldBiomeSource.java
- .opaqueCube
- EnchantTableMenu
- OldRails
- EnchantingPlus.java
- net.neoforged.bus.api.IEventBus
- gen.py
- biomes.py
- PerlinNoise
- StructurePieceBlockSelector
- EplusConfig
- HeatscarSpider.java
- hexxit.worldgen152.core.populate.OldGen
- BigTree
- InvTweaksConfig
- net.minecraft.world.level.block.state.BlockState
- compare_original.py
- net.minecraft.world.level.block.Block
- verify_blocks.py
- net.minecraft.resources.ResourceKey
- artifice/tools/gen.py
- net.minecraft.core.Direction
- .isLeaves
- net.minecraft.world.level.block.Block
- net.minecraft.server.level.ServerLevel
- BackToolsClient.java
- EbxlClient.java
- original_server.sh
- ArtificeJeiPlugin.java
- net.minecraft.world.item.ItemStack
- EnchantTableScreen
- ComponentNetherBridgePiece
- Holzsets und Pflanzen in 26.3
- net.minecraft.resources.Identifier
- EnchantTableScreen.java
- InvTweaksConfigSortingRule
- EnchantingCategory.java
- JEI-Plugin für einen portierten Mod
- SoulCageBlockEntity.java
- net.minecraft.world.level.block.state.properties.BooleanProperty
- Acuteness
- hexxit.worldgen152.core.populate.OldWorld
- net.minecraft.world.item.Item
- InvTweaks
- HexxitGearClient.java
- net.minecraft.client.renderer.entity.state.LivingEntityRenderState
- net.minecraft.client.gui.GuiGraphicsExtractor
- Client-Darstellung in 26.3 (NeoForge 26.3.0.102)
- net.minecraft.world.level.Level
- ImpModel.java
- minecraft
- Material
- .appendHoverText
- net.neoforged.bus.api.SubscribeEvent
- ArmorBarHud.java
- ScarecrowEntity
- .generate
- Decorate
- ArtificeBlocks
- Wood
- InvTweaksHandlerShortcuts
- LanCommands
- ImpEntity.java
- .column
- InvTweaksHandlerSorting
- BackItemLayer.java
- Mobs, Spawn-Eier und Boote auf 26.3
- .setBlock
- EplusClient
- .register
- convert_tree.py
- Extrabiomes
- OldChunkWriter.java
- OldBlockStates
- SakuraTreeGen
- Type
- GuiCustomLanPermissions
- ArmorSet
- Bäume und Äxte bei TreeCapitator anmelden
- SaguaroGen
- ChestTransporterItem.java
- NaturaGen
- Natura
- OldNetherPopulator.java
- hexxit.worldgen152.core.OldChunk
- ArtificeEvents
- .generate
- .livingDeath
- OldGameIds
- BloodTreeLargeGen
- DispenseBox.java
- NitroCreeper
- InvTweaksItemTree
- net.minecraft.world.entity.EntityType
- .populate
- NaturaEntities.java
- Ore
- EucalyptusTreeGenShort
- Populate
- HeroMC – Hexxit 1:1 für Minecraft 26.3
- CoFHCommand.java
- TableEvents
- json
- CoFHWorldGen
- SoulShards
- HeatscarSpider
- NitroCreeperRenderer.java
- net.minecraft.world.level.block.RotatedPillarBlock
- .generate
- Artifice
- .generate
- StructureComponent
- ComponentStrongholdCorridor
- OffLawnGen
- Starting the test client without a screen

## God Nodes (most connected - your core abstractions)
1. `OldWorld` - 326 edges
2. `StructureBoundingBox` - 140 edges
3. `StructureComponent` - 84 edges
4. `OldBlockInfo` - 76 edges
5. `OldLoot` - 64 edges
6. `OldEntity` - 63 edges
7. `OldTile` - 63 edges
8. `NaturaBlocks` - 60 edges
9. `RedwoodTreeGen` - 55 edges
10. `InvTweaksContainerManager` - 48 edges

## Surprising Connections (you probably didn't know these)
- `Bäume im Spiel (Setzlinge)` --references--> `OldGen`  [INFERRED]
  .claude/skills/worldgen152/SKILL.md → mods/worldgen152/src/main/java/hexxit/worldgen152/core/populate/OldGen.java
- `Teil 12 und 13 – Better Dungeons (Chocolate Quest)` --references--> `VillageHandler`  [INFERRED]
  docs/PLAN.md → mods/worldgen152/src/main/java/hexxit/worldgen152/core/structure/OldStructures.java
- `Aufbau` --references--> `WorldGenerator`  [INFERRED]
  .claude/skills/worldgen152/SKILL.md → mods/worldgen152/src/main/java/hexxit/worldgen152/core/populate/OldTerrainGen.java
- `Teil 1 – Vanilla-1.5.2-Dekoration` --references--> `OldDecorator`  [INFERRED]
  docs/PLAN.md → mods/worldgen152/src/main/java/hexxit/worldgen152/core/populate/OldDecorator.java
- `Aufbau` --references--> `OldDungeonGen`  [INFERRED]
  .claude/skills/worldgen152/SKILL.md → mods/worldgen152/src/main/java/hexxit/worldgen152/core/populate/OldDungeonGen.java

## Import Cycles
- None detected.

## Communities (157 total, 65 thin omitted)

### Community 0 - "OldWorld"
Cohesion: 0.09
Nodes (3): Spawner, OldWorld, StructureBoundingBox

### Community 1 - "Layer"
Cohesion: 0.09
Nodes (18): Layer, AddIsland, AddMushroomIsland, AddSnow, Biome, FuzzyZoom, Hills, Island (+10 more)

### Community 2 - "reference.py"
Cohesion: 0.18
Nodes (10): build_mappings(), digest(), download(), inner_archives(), java(), main(), minecraft_reference(), process() (+2 more)

### Community 3 - "OldGen"
Cohesion: 0.07
Nodes (16): Aufbau, OldDecorator, OldGen, Spring, Clay, Minable, Sand, Cactus (+8 more)

### Community 4 - "net.minecraft.core.Holder"
Cohesion: 0.15
Nodes (4): EnchantHelper, OldCost, Restriction, Settings

### Community 7 - "OldTerrain"
Cohesion: 0.16
Nodes (3): BiomeLayout, OctaveNoise, OldTerrain

### Community 8 - "Arbeitsplan: Hexxit 1.0.11 → NeoForge 26.3"
Cohesion: 0.10
Nodes (20): Arbeitsplan: Hexxit 1.0.11 → NeoForge 26.3, Regeln für jeden Teil, Teil 10 und 11 – Project Zulu, Teil 14 – Battle Towers, Ruins, Teil 15 – Dimensional Doors, Teil 16 – BiblioCraft, Teil 17 – BetterStorage, Backpacks, qCraft, Teil 18 – ChocoCraft, Infernal Mobs (+12 more)

### Community 9 - "OldChunkGenerator.java"
Cohesion: 0.07
Nodes (3): OldChunkGenerator, OldNetherChunkGenerator, World

### Community 11 - "HeroMC – Hinweise für Claude"
Cohesion: 0.29
Nodes (6): Arbeitsweise beim Portieren, Branches, graphify, HeroMC – Hinweise für Claude, Skills anlegen, Tokens sparen

### Community 17 - "OldBiome"
Cohesion: 0.06
Nodes (10): EbxlBiomes, Decoration, GrassChooser, OldBiome, TreeChooser, OldBiomes, OldCarver, OldCaves (+2 more)

### Community 18 - "random"
Cohesion: 0.05
Nodes (18): EbxlIds, OldBlocks, OldBlockInfo, OldEntity, Villager, Witch, OldLakeGens, OldLoot (+10 more)

### Community 19 - "OldDecoratedChunk"
Cohesion: 0.09
Nodes (4): OldChunk, OldDecoratedChunk, OldDecoration, TerrainSource

### Community 20 - "BoxRecipe.java"
Cohesion: 0.12
Nodes (4): ArtificeRecipes, BoxItem, BoxRecipe, UpgradeRecipe

### Community 21 - "EbxlWorldGen"
Cohesion: 0.14
Nodes (5): Choices, EbxlWorldGen, Jungle, Weighted, DesertWell

### Community 22 - "StructureComponent"
Cohesion: 0.07
Nodes (16): ComponentVillage, ComponentVillageChurch, ComponentVillageField2, ComponentVillageField, ComponentVillageHall, ComponentVillageHouse1, ComponentVillageHouse2, ComponentVillageHouse3 (+8 more)

### Community 23 - ".isAirBlock"
Cohesion: 0.08
Nodes (12): CatTail, EbxlPlantGens, Flowers, LeafPile, Marsh, Melon, Oasis, Quicksand (+4 more)

### Community 24 - "OldTerrainGen"
Cohesion: 0.31
Nodes (4): Teil 1 – Vanilla-1.5.2-Dekoration, Entry, OldTerrainGen, WorldGenerator

### Community 25 - "OldStructures"
Cohesion: 0.07
Nodes (14): Teil 12 und 13 – Better Dungeons (Chocolate Quest), Found, MapGenNetherBridge, Region, SpawnEntry, Found, OldStructures, VillageHandler (+6 more)

### Community 26 - ".findIntersecting"
Cohesion: 0.10
Nodes (16): ComponentStronghold, ComponentStrongholdChestCorridor, ComponentStrongholdCrossing, ComponentStrongholdLeftTurn, ComponentStrongholdLibrary, ComponentStrongholdPortalRoom, ComponentStrongholdPrison, ComponentStrongholdRoomCrossing (+8 more)

### Community 27 - "InvTweaksClient.java"
Cohesion: 0.06
Nodes (3): InvTweaksGuiIconButton, InvTweaksGuiSettingsButton, InvTweaksGuiSortingButton

### Community 28 - "net.minecraft.core.BlockPos"
Cohesion: 0.09
Nodes (7): FloraBlock, CattailBlock, EbxlPlantBlock, QuicksandBlock, HexbiscusBlock, NaturaPlantBlock, FloraBlock

### Community 30 - "ArmorBarHud"
Cohesion: 0.14
Nodes (10): Kind, AMOUNT, CHEST, DURABILITY, FEET, HEAD, LEGS, Unit (+2 more)

### Community 31 - "minecraft-dev"
Cohesion: 0.50
Nodes (3): npx, minecraft-dev, @mcdxai/minecraft-dev-mcp

### Community 32 - "InvTweaksContainerManager"
Cohesion: 0.07
Nodes (17): ContainerSection, ARMOR, BREWING_BOTTLES, BREWING_INGREDIENT, CHEST, CRAFTING_IN, CRAFTING_IN_PERSISTENT, CRAFTING_OUT (+9 more)

### Community 33 - "net.minecraft.world.level.Level"
Cohesion: 0.06
Nodes (8): SickleItem, BerryBushBlock, FusewoodLogBlock, Barley, Cotton, NaturaCropBlock, Menu, NaturaWorkbenchBlock

### Community 34 - "net.minecraft.world.item.ItemStack"
Cohesion: 0.07
Nodes (8): FrostArrow, Kind, ENDER, FLAME, FROST, MULTI, PLAIN, MoreBowItem

### Community 35 - ".getNextComponentNormal"
Cohesion: 0.09
Nodes (4): ComponentStrongholdRightTurn, ComponentStrongholdStairs2, StructureStrongholdPieces, StructureStrongholdPieceWeight

### Community 36 - "TreeDefinition"
Cohesion: 0.06
Nodes (6): Teil 6 – bspkrsCore, TreeCapitator, Inventory Tweaks, Custom LAN Ports, BlockID, TreeCapitator, Data, TreeDefinition, TreeRegistry

### Community 41 - "EnchantingPlus.java"
Cohesion: 0.08
Nodes (8): EnchantingPlus, ClientOptionPayload, EnchantPayload, EplusNetwork, OpenModTablePayload, UseModPayload, RFTL, TCLog

### Community 42 - "net.neoforged.bus.api.IEventBus"
Cohesion: 0.04
Nodes (14): ArmorBarMod, ArtificeIds, BackTools, bspkrsCoreMod, BuffBarMod, CoFHCore, CustomLAN, HGWorldGen (+6 more)

### Community 43 - "gen.py"
Cohesion: 0.09
Nodes (22): age(), ages(), bitag(), blockstate(), btag(), cube_all(), flam(), generated_item() (+14 more)

### Community 44 - "biomes.py"
Cohesion: 0.15
Nodes (9): ColorMaps, f32(), fixed(), hexcolor(), main(), multiply(), sky_color(), tinted() (+1 more)

### Community 47 - "EplusConfig"
Cohesion: 0.10
Nodes (8): GuiContainer → `AbstractContainerScreen`, Konfiguration und Spielerdaten, Menü, Screen und Pakete in 26.3 (NeoForge 26.3.0.102), Pakete → Custom Payloads, Truhen-GUIs und Inventory Tweaks, Vanilla-Block ersetzen (Mod hat Block-ID überschrieben), EplusConfig, TCSettings

### Community 49 - "hexxit.worldgen152.core.populate.OldGen"
Cohesion: 0.10
Nodes (13): Acacia, Autumn, Bend, LEFT, RIGHT, STRAIGHT, BigAutumn, CustomSwamp (+5 more)

### Community 50 - "BigTree"
Cohesion: 0.10
Nodes (10): BigMushroom, BigTree, Birch, Huge, OldTreeGens, Pine, Shrub, Spruce (+2 more)

### Community 51 - "InvTweaksConfig"
Cohesion: 0.08
Nodes (4): InvTweaksConfig, OrderedProperties, InvTweaksConfigManager, InvTweaksHandlerAutoRefill

### Community 52 - "net.minecraft.world.level.block.state.BlockState"
Cohesion: 0.10
Nodes (3): DetectorBlock, SaguaroBlock, SaguaroSproutBlock

### Community 53 - "compare_original.py"
Cohesion: 0.19
Nodes (7): cells(), main(), read_nbt(), payload(), string(), take(), region_chunks()

### Community 54 - "net.minecraft.world.level.block.Block"
Cohesion: 0.04
Nodes (6): ScarecrowItem, BoneBagItem, ImpMeatItem, SeedBagItem, SunflowerSeedItem, ShadowPickaxeItem

### Community 55 - "verify_blocks.py"
Cohesion: 0.14
Nodes (15): main(), packet(), read(), block(), ebxl(), flag(), main(), saguaro() (+7 more)

### Community 56 - "net.minecraft.resources.ResourceKey"
Cohesion: 0.04
Nodes (20): Truhen und Spawner, ArtificeClient, Upgrade, ARMOR_SPIKES, COUNTERWEIGHT, DIVE_KIT, ELASTIC_LAYERING, ELASTIC_SOLES (+12 more)

### Community 57 - "artifice/tools/gen.py"
Cohesion: 0.11
Nodes (21): bitag(), blockstate(), btag(), cube_all(), cube_element(), face(), generated_item(), handheld_item() (+13 more)

### Community 58 - "net.minecraft.core.Direction"
Cohesion: 0.08
Nodes (4): SaguaroFruitBlock, BeanstalkBlock, IcicleBlock, StalactiteBlock

### Community 59 - ".isLeaves"
Cohesion: 0.13
Nodes (3): GlowshroomGenBlueGreen, GlowshroomGenPurple, RareTreeGen

### Community 60 - "net.minecraft.world.level.block.Block"
Cohesion: 0.06
Nodes (7): EbxlLeavesBlock, LeafPileBlock, EbxlBlocks, LogTurnerItem, NaturaLeavesBlock, NaturaIds, NaturaBlocks

### Community 61 - "net.minecraft.server.level.ServerLevel"
Cohesion: 0.04
Nodes (34): EbxlSaplingBlock, GlowshroomBlock, Growth, FLORA, NETHER, RARE, REDWOOD, NaturaSaplingBlock (+26 more)

### Community 62 - "BackToolsClient.java"
Cohesion: 0.27
Nodes (3): Back, BackItem, BackToolsClient

### Community 63 - "EbxlClient.java"
Cohesion: 0.07
Nodes (4): EbxlClient, FrostArrowRenderer, MoreBowsClient, NaturaEntityRenderers

### Community 65 - "ArtificeJeiPlugin.java"
Cohesion: 0.11
Nodes (3): ArtificeJeiPlugin, Entry, SledgeParts

### Community 68 - "ComponentNetherBridgePiece"
Cohesion: 0.05
Nodes (18): Teil 7b – Natura-Nether, ComponentNetherBridgeCorridor2, ComponentNetherBridgeCorridor3, ComponentNetherBridgeCorridor4, ComponentNetherBridgeCorridor5, ComponentNetherBridgeCorridor, ComponentNetherBridgeCrossing2, ComponentNetherBridgeCrossing3 (+10 more)

### Community 69 - "Holzsets und Pflanzen in 26.3"
Cohesion: 0.22
Nodes (7): Blöcke (Java), Brennwert und Brennbarkeit, Holzsets und Pflanzen in 26.3, Modelle und Blockstates aus Vanilla erzeugen, Prüfen, Rezeptkonflikte mit Vanilla, Tags (Block + Item)

### Community 70 - "net.minecraft.resources.Identifier"
Cohesion: 0.08
Nodes (9): ChestTransporter, ChestTransporterJeiPlugin, EbxlJeiPlugin, EplusJeiPlugin, HexxitGearJeiPlugin, MoreBowsJeiPlugin, NaturaJeiPlugin, ObsidiPlatesJeiPlugin (+1 more)

### Community 72 - "InvTweaksConfigSortingRule"
Cohesion: 0.08
Nodes (7): InvTweaksConfigInventoryRuleset, InvTweaksConfigSortingRule, Type, COLUMN, RECTANGLE, ROW, SLOT

### Community 74 - "JEI-Plugin für einen portierten Mod"
Cohesion: 0.40
Nodes (4): Einbindung (schon erledigt, in `build.gradle`), JEI-Plugin für einen portierten Mod, Prüfen, Vorlage

### Community 75 - "SoulCageBlockEntity.java"
Cohesion: 0.09
Nodes (3): PlayerMobPlateBlock, SoulCageBlock, SoulCageBlockEntity

### Community 77 - "Acuteness"
Cohesion: 0.50
Nodes (4): Acuteness, LOOSE, TIGHT, TIGHTER

### Community 79 - "net.minecraft.world.item.Item"
Cohesion: 0.06
Nodes (6): ArtificeItems, SledgeItem, UpgradeItem, KamaItem, NaturaItems, ToolSet

### Community 80 - "InvTweaks"
Cohesion: 0.10
Nodes (3): InvTweaks, Scheduled, InvTweaksClient

### Community 81 - "HexxitGearClient.java"
Cohesion: 0.14
Nodes (3): HelmetModels, HexxitGearClient, IClientItemExtensions

### Community 82 - "net.minecraft.client.renderer.entity.state.LivingEntityRenderState"
Cohesion: 0.12
Nodes (3): ScarecrowRenderer, HeatscarSpiderRenderer, ImpRenderer

### Community 83 - "net.minecraft.client.gui.GuiGraphicsExtractor"
Cohesion: 0.06
Nodes (9): InvTweaksGuiModNotWorking, InvTweaksGuiSettings, InvTweaksGuiSettingsAbstract, TextButton, InvTweaksGuiSettingsAdvanced, InvTweaksGuiShortcutsHelp, InvTweaksConst, InvTweaksKeys (+1 more)

### Community 84 - "Client-Darstellung in 26.3 (NeoForge 26.3.0.102)"
Cohesion: 0.22
Nodes (8): Client-Darstellung in 26.3 (NeoForge 26.3.0.102), Etwas am Spielermodell zeichnen (Rücken, Schultern), Halbdurchsichtige Blöcke (Alpha im Renderer), HUD (statt `ITickHandler` mit `TickType.RENDER`), Nur-Client-Mod sauber laden, Rechtsklick vor dem Block (Original: `PlayerInteractEvent` mit `RIGHT_CLICK_BLOCK`), Rüstung: Texturen und eigene Helm-Modelle (`ItemArmor`, `getArmorTexture`, `getArmorModel`), Stolperfallen

### Community 85 - "net.minecraft.world.level.Level"
Cohesion: 0.09
Nodes (4): CloudBlock, HeatSandBlock, LargeGlowshroomBlock, ThornVinesBlock

### Community 86 - "ImpModel.java"
Cohesion: 0.15
Nodes (4): ScarecrowModel, ImpModel, NaturaBoatModel, NaturaBoatRenderer

### Community 87 - "minecraft"
Cohesion: 0.11
Nodes (3): CustomLanClient, GuiCustomLan, CLOptions

### Community 88 - "Material"
Cohesion: 0.07
Nodes (27): Material, AIR, CACTUS, CIRCUITS, CLAY, CLOTH, CRAFTED_SNOW, FIRE (+19 more)

### Community 90 - "net.neoforged.bus.api.SubscribeEvent"
Cohesion: 0.21
Nodes (3): HeadDrops, SetBonuses, State

### Community 93 - ".generate"
Cohesion: 0.15
Nodes (6): OldDungeonGen, MinecartChest, Category, Entry, OldStack, Chest

### Community 94 - "Decorate"
Cohesion: 0.13
Nodes (15): Decorate, BIG_SHROOM, CACTUS, CLAY, DEAD_BUSH, FLOWERS, GRASS, LAKE (+7 more)

### Community 95 - "ArtificeBlocks"
Cohesion: 0.11
Nodes (8): ArtificeBlocks, Tier, ADVANCED, BASIC, INDUSTRIAL, REINFORCED, FrameBlock, ScaffoldingBlock

### Community 96 - "Wood"
Cohesion: 0.10
Nodes (14): Wood, AMARANTH, BLOODWOOD, DARKWOOD, EUCALYPTUS, FUSEWOOD, GHOSTWOOD, HOPSEED (+6 more)

### Community 97 - "InvTweaksHandlerShortcuts"
Cohesion: 0.10
Nodes (16): Action, DROP, MOVE, InvTweaksHandlerShortcuts, Scope, ALL_ITEMS, EVERYTHING, ONE_ITEM (+8 more)

### Community 101 - ".column"
Cohesion: 0.10
Nodes (4): ArtificeWorldGen, Pos, XSRandom, Column

### Community 104 - "Mobs, Spawn-Eier und Boote auf 26.3"
Cohesion: 0.25
Nodes (7): Boote, Client, Drops, Mobs, Spawn-Eier und Boote auf 26.3, Registrieren, Spawn-Eier, Spawns

### Community 105 - ".setBlock"
Cohesion: 0.18
Nodes (3): Teil 5 – Hexxit Gear, More Bows, Enchanting Plus, offLawn, Wild Caves 3, WildCavesGen, Skull

### Community 108 - "convert_tree.py"
Cohesion: 0.22
Nodes (8): camel(), convert(), emit_additions(), walk(), main(), Out, resolve_tags(), get()

### Community 109 - "Extrabiomes"
Cohesion: 0.21
Nodes (6): Bark, NE, NW, SE, SW, Extrabiomes

### Community 114 - "Type"
Cohesion: 0.20
Nodes (9): Type, DROP, MOVE_ALL_ITEMS, MOVE_DOWN, MOVE_EVERYTHING, MOVE_ONE_ITEM, MOVE_ONE_STACK, MOVE_TO_SPECIFIC_HOTBAR_SLOT (+1 more)

### Community 116 - "ArmorSet"
Cohesion: 0.19
Nodes (4): ArmorSet, SCALE, THIEF, TRIBAL

### Community 118 - "Bäume und Äxte bei TreeCapitator anmelden"
Cohesion: 0.40
Nodes (4): Bäume: Daten-Registry `treecapitator:tree`, Bäume und Äxte bei TreeCapitator anmelden, Prüfen, Äxte und Scheren: Item-Tags

### Community 121 - "NaturaGen"
Cohesion: 0.08
Nodes (6): BaseTreeWorldgen, BushTreeGen, DarkwoodGen, FusewoodGen, NaturaGen, WhiteTreeGen

### Community 128 - "OldGameIds"
Cohesion: 0.18
Nodes (10): Biome und Dekoratoren, Blöcke anmelden, Bäume im Spiel (Setzlinge), Generator anmelden, Nether, Prüfen, Regel 1: Kern-Klassen ohne Spielklassen, Stolperfallen (+2 more)

### Community 132 - "InvTweaksItemTree"
Cohesion: 0.06
Nodes (4): InvTweaksItemTree, InvTweaksItemTreeCategory, InvTweaksItemTreeItem, InvTweaksItemTreeLoader

### Community 134 - ".populate"
Cohesion: 0.07
Nodes (7): Lake, Fire, Glowshroom, Glowstone, HellLava, OldNetherGens, Listener

### Community 135 - "NaturaEntities.java"
Cohesion: 0.09
Nodes (3): MoreBows, BloodwoodBoat, NaturaEntities

### Community 136 - "Ore"
Cohesion: 0.15
Nodes (10): OldTerrainGen.Listener, Ore, COAL, DIAMOND, DIRT, GOLD, GRAVEL, IRON (+2 more)

### Community 138 - "Populate"
Cohesion: 0.22
Nodes (8): Populate, DUNGEON, FIRE, GLOWSTONE, ICE, LAKE, LAVA, NETHER_LAVA

### Community 139 - "HeroMC – Hexxit 1:1 für Minecraft 26.3"
Cohesion: 0.18
Nodes (9): Arbeitsplan, Aufbau, Bauen und Starten, HeroMC – Hexxit 1:1 für Minecraft 26.3, Referenzquellen des Originals, Status, Vorgaben, Wann ein Mod als portiert gilt (+1 more)

### Community 142 - "json"
Cohesion: 0.29
Nodes (5): check_model(), van(), check_model(), modelpath(), tinted()

### Community 147 - "net.minecraft.world.level.block.RotatedPillarBlock"
Cohesion: 0.12
Nodes (7): QuarterLogBlock, Bark, NE, NW, SE, SW, BloodwoodLogBlock

### Community 151 - "StructureComponent"
Cohesion: 0.09
Nodes (7): ComponentMineshaftCorridor, ComponentMineshaftCross, ComponentMineshaftRoom, ComponentMineshaftStairs, StructureComponent, StructureMineshaftPieces, StructureVillagePieces

### Community 154 - "Starting the test client without a screen"
Cohesion: 0.50
Nodes (3): Recipe, Starting the test client without a screen, What does not work and why

## Knowledge Gaps
- **179 isolated node(s):** `NE`, `NW`, `SE`, `SW`, `SCALE` (+174 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 915 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **65 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `OldWorld` connect `OldWorld` to `OldGen`, `.column`, `.populate`, `OldRails`, `.setBlock`, `Ore`, `Populate`, `hexxit.worldgen152.core.populate.OldGen`, `random`, `OldBiome`, `BigTree`, `EbxlWorldGen`, `OldDecoratedChunk`, `.isAirBlock`, `OldTerrainGen`, `NaturaGen`, `.generate`, `OldStructures`?**
  _High betweenness centrality (0.119) - this node is a cross-community bridge._
- **What connects `NE`, `NW`, `SE` to the rest of the system?**
  _179 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `OldWorld` be split into smaller, more focused modules?**
  _Cohesion score 0.09172724125995153 - nodes in this community are weakly interconnected._
- **Why does `InvTweaksContainerManager` connect `InvTweaksContainerManager` to `InvTweaksHandlerShortcuts`, `InvTweaksHandlerSorting`, `EnchantTableScreen.java`, `InvTweaksConfig`, `InvTweaksClient.java`?**
  _High betweenness centrality (0.032) - this node is a cross-community bridge._
- **Should `Layer` be split into smaller, more focused modules?**
  _Cohesion score 0.08831306187202538 - nodes in this community are weakly interconnected._
- **Why does `NaturaBlocks` connect `net.minecraft.world.level.block.Block` to `Wood`, `net.minecraft.world.level.Level`, `net.minecraft.resources.Identifier`, `InvTweaksHandlerSorting`, `NaturaEntities.java`, `net.neoforged.bus.api.IEventBus`, `SoulCageBlockEntity.java`, `net.minecraft.world.item.Item`, `net.minecraft.world.level.block.RotatedPillarBlock`, `net.minecraft.world.level.block.state.BlockState`, `net.minecraft.world.level.Level`, `net.minecraft.world.level.block.Block`, `net.minecraft.core.Direction`, `net.minecraft.core.BlockPos`, `net.minecraft.server.level.ServerLevel`?**
  _High betweenness centrality (0.025) - this node is a cross-community bridge._
- **Should `OldGen` be split into smaller, more focused modules?**
  _Cohesion score 0.06787330316742081 - nodes in this community are weakly interconnected._