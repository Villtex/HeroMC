# Graph Report - HeroMC  (2026-11-16)

## Corpus Check
- 3397 files · ~340,853 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 27 file(s) not represented in the graph (top: .properties 23, (none) 1, .toml 1)

## Summary
- 4519 nodes · 14446 edges · 159 communities (87 shown, 72 thin omitted)
- Extraction: 93% EXTRACTED · 7% INFERRED · 0% AMBIGUOUS · INFERRED: 1072 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `bb957407`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- StructureBoundingBox
- Layer
- reference.py
- .setBlock
- net.minecraft.world.item.ItemStack
- expect.sh
- ImportOriginalAssets.groovy
- OldTerrain
- Arbeitsplan: Hexxit 1.0.11 → NeoForge 26.3
- OldChunkGenerator.java
- HeroMC – Hinweise für Claude
- .claude/CLAUDE.md
- OldBiome
- OldBiomes.java
- OldDecoratedChunk
- UpgradeRecipe.java
- EbxlWorldGen
- StructureComponent
- random
- OldTerrainGen
- OldStructures
- .findIntersecting
- net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
- StalactiteBlock
- ArmorBarHud
- minecraft-dev
- InvTweaksContainerManager
- Override
- net.minecraft.world.item.ItemStack
- EplusJeiPlugin.java
- TreeDefinition
- OldBiomeSource.java
- .opaqueCube
- FrostArrow.java
- OldWorld
- EnchantingPlus.java
- net.neoforged.bus.api.IEventBus
- gen.py
- biomes.py
- PerlinNoise
- StructurePieceBlockSelector
- EplusConfig
- net.minecraft.world.level.Level
- hexxit.worldgen152.core.populate.OldGen
- .floor
- InvTweaksConfig
- net.minecraft.server.level.ServerLevel
- compare_original.py
- net.minecraft.world.level.block.Block
- verify_blocks.py
- NaturaClient.java
- artifice/tools/gen.py
- BeanstalkBlock
- BlockID
- NaturaBlocks.java
- net.minecraft.world.level.block.state.BlockState
- BackToolsClient.java
- EbxlClient.java
- original_server.sh
- ArtificeJeiPlugin.java
- net.minecraft.world.item.ItemStack
- EnchantTableScreen
- StructureComponent
- net.minecraft.server.level.ServerLevel
- net.minecraft.resources.Identifier
- component
- InvTweaksConfigSortingRule
- EnchantingCategory.java
- JEI-Plugin für einen portierten Mod
- Target
- ArtificeEvents
- Populate
- hexxit.worldgen152.core.populate.OldWorld
- NaturaItems.java
- InvTweaks
- HexxitGearClient.java
- net.minecraft.client.renderer.entity.state.LivingEntityRenderState
- net.minecraft.client.gui.screens.Screen
- Client-Darstellung in 26.3 (NeoForge 26.3.0.102)
- net.minecraft.world.level.Level
- ImpModel.java
- minecraft
- Material
- ChestTransporterItem.java
- net.neoforged.bus.api.SubscribeEvent
- ScarecrowEntity
- OldStack
- OldChunkGenerator
- ScaffoldingBlock
- Wood
- Type
- LanCommands
- ImpEntity.java
- ArtificeWorldGen
- InvTweaksHandlerSorting
- Mobs, Spawn-Eier und Boote auf 26.3
- OldWorld
- OldCarver
- Decorate
- convert_tree.py
- Extrabiomes
- .copyVanilla
- OldChunkWriter.java
- .get
- SakuraTreeGen
- HexxitGear.java
- GuiCustomLanPermissions
- ArmorSet
- .setBlock
- Bäume und Äxte bei TreeCapitator anmelden
- net.minecraft.world.level.block.Block
- EnchantTableMenu
- Natura.java
- OldBlockStates
- net.minecraft.network.chat.Component
- hexxit.worldgen152.core.OldChunk
- MapGenNetherBridge
- .generate
- SoulEvents.java
- OldGameIds
- InvTweaksItemTreeLoader.java
- DispenseBox.java
- NitroCreeper
- InvTweaksItemTree
- .nextInt
- OldWorld.java
- NaturaEntities.java
- Ore
- StructureNetherBridgePieceWeight
- HeroMC – Hexxit 1:1 für Minecraft 26.3
- CoFHCommand
- TableEvents
- json
- CoFHWorldGen
- Listener
- NitroCreeperRenderer.java
- StructurePieceBlockSelector
- Menü, Screen und Pakete in 26.3 (NeoForge 26.3.0.102)
- SaguaroFruitBlock
- .ComponentVillagePathGen
- NaturaLeavesBlock
- HexxitGear.java
- CattailBlock
- OldRavines
- .populate
- Biome

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
- `Aufbau` --references--> `OldDungeonGen`  [INFERRED]
  .claude/skills/worldgen152/SKILL.md → mods/worldgen152/src/main/java/hexxit/worldgen152/core/populate/OldDungeonGen.java
- `Aufbau` --references--> `WorldGenerator`  [INFERRED]
  .claude/skills/worldgen152/SKILL.md → mods/worldgen152/src/main/java/hexxit/worldgen152/core/populate/OldTerrainGen.java
- `Aufbau` --references--> `World`  [INFERRED]
  .claude/skills/worldgen152/SKILL.md → mods/worldgen152/src/main/java/hexxit/worldgen152/OldNetherChunkGenerator.java
- `Teil 1 – Vanilla-1.5.2-Dekoration` --references--> `OldDecorator`  [INFERRED]
  docs/PLAN.md → mods/worldgen152/src/main/java/hexxit/worldgen152/core/populate/OldDecorator.java

## Import Cycles
- None detected.

## Communities (159 total, 72 thin omitted)

### Community 0 - "StructureBoundingBox"
Cohesion: 0.10
Nodes (5): Villager, Witch, Spawner, OldWorld, StructureBoundingBox

### Community 1 - "Layer"
Cohesion: 0.10
Nodes (15): Layer, AddIsland, AddMushroomIsland, AddSnow, Hills, Island, Layers, River (+7 more)

### Community 2 - "reference.py"
Cohesion: 0.18
Nodes (10): build_mappings(), digest(), download(), inner_archives(), java(), main(), minecraft_reference(), process() (+2 more)

### Community 3 - ".setBlock"
Cohesion: 0.04
Nodes (32): Aufbau, BaseCropWorldgen, OldDecorator, OldGen, DesertWell, Lake, Spring, Fire (+24 more)

### Community 4 - "net.minecraft.world.item.ItemStack"
Cohesion: 0.18
Nodes (3): EnchantHelper, OldCost, Restriction

### Community 8 - "Arbeitsplan: Hexxit 1.0.11 → NeoForge 26.3"
Cohesion: 0.08
Nodes (23): Arbeitsplan: Hexxit 1.0.11 → NeoForge 26.3, Regeln für jeden Teil, Teil 10 und 11 – Project Zulu, Teil 12 und 13 – Better Dungeons (Chocolate Quest), Teil 14 – Battle Towers, Ruins, Teil 15 – Dimensional Doors, Teil 16 – BiblioCraft, Teil 17 – BetterStorage, Backpacks, qCraft (+15 more)

### Community 11 - "HeroMC – Hinweise für Claude"
Cohesion: 0.29
Nodes (6): Arbeitsweise beim Portieren, Branches, graphify, HeroMC – Hinweise für Claude, Skills anlegen, Tokens sparen

### Community 17 - "OldBiome"
Cohesion: 0.12
Nodes (6): EbxlBiomes, Decoration, GrassChooser, OldBiome, TreeChooser, OldBiomes

### Community 18 - "OldBiomes.java"
Cohesion: 0.22
Nodes (4): OldBlockInfo, OldEntity, OldLoot, OldTile

### Community 19 - "OldDecoratedChunk"
Cohesion: 0.09
Nodes (4): OldChunk, OldDecoratedChunk, OldDecoration, TerrainSource

### Community 20 - "UpgradeRecipe.java"
Cohesion: 0.10
Nodes (4): ArtificeRecipes, BoxItem, BoxRecipe, UpgradeRecipe

### Community 21 - "EbxlWorldGen"
Cohesion: 0.12
Nodes (6): Choices, EbxlWorldGen, Jungle, Weighted, BaseCloudWorldgen, CloudGen

### Community 22 - "StructureComponent"
Cohesion: 0.05
Nodes (18): ComponentVillage, ComponentVillageChurch, ComponentVillageField2, ComponentVillageField, ComponentVillageHall, ComponentVillageHouse1, ComponentVillageHouse2, ComponentVillageHouse3 (+10 more)

### Community 24 - "OldTerrainGen"
Cohesion: 0.31
Nodes (4): Teil 1 – Vanilla-1.5.2-Dekoration, Entry, OldTerrainGen, WorldGenerator

### Community 25 - "OldStructures"
Cohesion: 0.12
Nodes (7): OldStructures, StructureMineshaftStart, StructureNetherBridgeStart, StructureScatteredFeatureStart, StructureStart, StructureStrongholdStart, StructureVillageStart

### Community 26 - ".findIntersecting"
Cohesion: 0.05
Nodes (20): ComponentStronghold, ComponentStrongholdChestCorridor, ComponentStrongholdCrossing, ComponentStrongholdLeftTurn, ComponentStrongholdLibrary, ComponentStrongholdPortalRoom, ComponentStrongholdPrison, ComponentStrongholdRightTurn (+12 more)

### Community 27 - "net.minecraft.client.gui.screens.inventory.AbstractContainerScreen"
Cohesion: 0.06
Nodes (3): InvTweaksGuiIconButton, InvTweaksGuiSettingsButton, InvTweaksGuiSortingButton

### Community 30 - "ArmorBarHud"
Cohesion: 0.13
Nodes (10): Kind, AMOUNT, CHEST, DURABILITY, FEET, HEAD, LEGS, Unit (+2 more)

### Community 31 - "minecraft-dev"
Cohesion: 0.50
Nodes (3): npx, minecraft-dev, @mcdxai/minecraft-dev-mcp

### Community 32 - "InvTweaksContainerManager"
Cohesion: 0.07
Nodes (18): ContainerSection, ARMOR, BREWING_BOTTLES, BREWING_INGREDIENT, CHEST, CRAFTING_IN, CRAFTING_IN_PERSISTENT, CRAFTING_OUT (+10 more)

### Community 33 - "Override"
Cohesion: 0.15
Nodes (3): Barley, Cotton, NaturaCropBlock

### Community 34 - "net.minecraft.world.item.ItemStack"
Cohesion: 0.07
Nodes (9): SickleItem, Kind, ENDER, FLAME, FROST, MULTI, PLAIN, MoreBowItem (+1 more)

### Community 38 - ".opaqueCube"
Cohesion: 0.12
Nodes (3): BerryBushGen, BloodTreeLargeGen, NetherBerryBushGen

### Community 41 - "EnchantingPlus.java"
Cohesion: 0.09
Nodes (7): EnchantingPlus, ClientOptionPayload, EnchantPayload, OpenModTablePayload, UseModPayload, RFTL, TCLog

### Community 42 - "net.neoforged.bus.api.IEventBus"
Cohesion: 0.06
Nodes (11): ArmorBarMod, Artifice, ArtificeIds, BackTools, bspkrsCoreMod, BuffBarMod, CoFHCore, CustomLAN (+3 more)

### Community 43 - "gen.py"
Cohesion: 0.09
Nodes (22): age(), ages(), bitag(), blockstate(), btag(), cube_all(), flam(), generated_item() (+14 more)

### Community 44 - "biomes.py"
Cohesion: 0.15
Nodes (9): ColorMaps, f32(), fixed(), hexcolor(), main(), multiply(), sky_color(), tinted() (+1 more)

### Community 46 - "StructurePieceBlockSelector"
Cohesion: 0.15
Nodes (5): ComponentScatteredFeature, ComponentScatteredFeatureDesertPyramid, ComponentScatteredFeatureJunglePyramid, ComponentScatteredFeatureSwampHut, StructureScatteredFeatureStones

### Community 47 - "EplusConfig"
Cohesion: 0.09
Nodes (5): Vanilla-Block ersetzen (Mod hat Block-ID überschrieben), EplusClient, EplusConfig, Settings, TCSettings

### Community 48 - "net.minecraft.world.level.Level"
Cohesion: 0.05
Nodes (5): BabyHeatscarSpider, HeatscarSpider, ImpMeatItem, PlayerMobPlateBlock, HomeDimensions

### Community 49 - "hexxit.worldgen152.core.populate.OldGen"
Cohesion: 0.07
Nodes (27): CatTail, EbxlPlantGens, Flowers, LeafPile, Marsh, Melon, Oasis, Quicksand (+19 more)

### Community 50 - ".floor"
Cohesion: 0.10
Nodes (4): BigAutumn, OldMath, Minable, BigTree

### Community 51 - "InvTweaksConfig"
Cohesion: 0.06
Nodes (14): InvTweaksConfig, OrderedProperties, InvTweaksConfigManager, InvTweaksHandlerAutoRefill, Action, DROP, MOVE, InvTweaksHandlerShortcuts (+6 more)

### Community 52 - "net.minecraft.server.level.ServerLevel"
Cohesion: 0.08
Nodes (3): DetectorBlock, SoulCageBlock, SoulCageBlockEntity

### Community 53 - "compare_original.py"
Cohesion: 0.19
Nodes (7): cells(), main(), read_nbt(), payload(), string(), take(), region_chunks()

### Community 54 - "net.minecraft.world.level.block.Block"
Cohesion: 0.05
Nodes (5): LogTurnerItem, ScarecrowItem, BoneBagItem, SunflowerSeedItem, ShadowPickaxeItem

### Community 55 - "verify_blocks.py"
Cohesion: 0.14
Nodes (15): main(), packet(), read(), block(), ebxl(), flag(), main(), saguaro() (+7 more)

### Community 57 - "artifice/tools/gen.py"
Cohesion: 0.11
Nodes (21): bitag(), blockstate(), btag(), cube_all(), cube_element(), face(), generated_item(), handheld_item() (+13 more)

### Community 59 - "BlockID"
Cohesion: 0.11
Nodes (4): Teil 6 – bspkrsCore, TreeCapitator, Inventory Tweaks, Custom LAN Ports, BlockID, Data, TreeRegistry

### Community 60 - "NaturaBlocks.java"
Cohesion: 0.04
Nodes (12): ArtificeBlocks, FrameBlock, LotusLilyBlock, EbxlPlantBlock, LeafPileBlock, QuarterLogBlock, QuicksandBlock, EbxlBlocks (+4 more)

### Community 61 - "net.minecraft.world.level.block.state.BlockState"
Cohesion: 0.04
Nodes (16): FloraBlock, LotusBlock, EbxlLeavesBlock, EbxlSaplingBlock, HexbiscusBlock, GlowshroomBlock, NaturaPlantBlock, NaturaSaplingBlock (+8 more)

### Community 63 - "EbxlClient.java"
Cohesion: 0.08
Nodes (5): EbxlClient, FrostArrowRenderer, MoreBowsClient, NaturaEntityRenderers, OffLawnClient

### Community 65 - "ArtificeJeiPlugin.java"
Cohesion: 0.12
Nodes (3): ArtificeJeiPlugin, Entry, SledgeParts

### Community 67 - "EnchantTableScreen"
Cohesion: 0.14
Nodes (3): GuiContainer → `AbstractContainerScreen`, EnchantTableScreen, GuiItem

### Community 68 - "StructureComponent"
Cohesion: 0.05
Nodes (21): ComponentMineshaftCorridor, ComponentMineshaftCross, ComponentMineshaftRoom, ComponentMineshaftStairs, ComponentNetherBridgeCorridor2, ComponentNetherBridgeCorridor3, ComponentNetherBridgeCorridor4, ComponentNetherBridgeCorridor5 (+13 more)

### Community 69 - "net.minecraft.server.level.ServerLevel"
Cohesion: 0.05
Nodes (34): Blöcke (Java), Brennwert und Brennbarkeit, Holzsets und Pflanzen in 26.3, Modelle und Blockstates aus Vanilla erzeugen, Prüfen, Rezeptkonflikte mit Vanilla, Tags (Block + Item), Growth (+26 more)

### Community 70 - "net.minecraft.resources.Identifier"
Cohesion: 0.12
Nodes (6): ChestTransporterJeiPlugin, EbxlJeiPlugin, HexxitGearJeiPlugin, MoreBowsJeiPlugin, ObsidiPlatesJeiPlugin, SoulShardsJeiPlugin

### Community 72 - "InvTweaksConfigSortingRule"
Cohesion: 0.08
Nodes (7): InvTweaksConfigInventoryRuleset, InvTweaksConfigSortingRule, Type, COLUMN, RECTANGLE, ROW, SLOT

### Community 74 - "JEI-Plugin für einen portierten Mod"
Cohesion: 0.40
Nodes (4): Einbindung (schon erledigt, in `build.gradle`), JEI-Plugin für einen portierten Mod, Prüfen, Vorlage

### Community 75 - "Target"
Cohesion: 0.40
Nodes (5): Target, DOWN, HOTBAR_SLOT, UNSPECIFIED, UP

### Community 77 - "Populate"
Cohesion: 0.11
Nodes (10): OldNetherPopulator, Populate, DUNGEON, FIRE, GLOWSTONE, ICE, LAKE, LAVA (+2 more)

### Community 78 - "hexxit.worldgen152.core.populate.OldWorld"
Cohesion: 0.07
Nodes (4): BushTreeGen, RareTreeGen, RedwoodTreeGen, WillowGen

### Community 79 - "NaturaItems.java"
Cohesion: 0.08
Nodes (6): ArtificeItems, SledgeItem, UpgradeItem, KamaItem, NaturaItems, ToolSet

### Community 80 - "InvTweaks"
Cohesion: 0.09
Nodes (3): InvTweaks, Scheduled, InvTweaksClient

### Community 81 - "HexxitGearClient.java"
Cohesion: 0.14
Nodes (3): HelmetModels, HexxitGearClient, IClientItemExtensions

### Community 82 - "net.minecraft.client.renderer.entity.state.LivingEntityRenderState"
Cohesion: 0.14
Nodes (3): ScarecrowRenderer, HeatscarSpiderRenderer, ImpRenderer

### Community 83 - "net.minecraft.client.gui.screens.Screen"
Cohesion: 0.12
Nodes (5): InvTweaksGuiModNotWorking, InvTweaksGuiSettings, InvTweaksGuiSettingsAbstract, TextButton, InvTweaksGuiSettingsAdvanced

### Community 84 - "Client-Darstellung in 26.3 (NeoForge 26.3.0.102)"
Cohesion: 0.22
Nodes (8): Client-Darstellung in 26.3 (NeoForge 26.3.0.102), Etwas am Spielermodell zeichnen (Rücken, Schultern), Halbdurchsichtige Blöcke (Alpha im Renderer), HUD (statt `ITickHandler` mit `TickType.RENDER`), Nur-Client-Mod sauber laden, Rechtsklick vor dem Block (Original: `PlayerInteractEvent` mit `RIGHT_CLICK_BLOCK`), Rüstung: Texturen und eigene Helm-Modelle (`ItemArmor`, `getArmorTexture`, `getArmorModel`), Stolperfallen

### Community 85 - "net.minecraft.world.level.Level"
Cohesion: 0.08
Nodes (4): CloudBlock, HeatSandBlock, LargeGlowshroomBlock, ThornVinesBlock

### Community 86 - "ImpModel.java"
Cohesion: 0.13
Nodes (4): ScarecrowModel, ImpModel, NaturaBoatModel, NaturaBoatRenderer

### Community 87 - "minecraft"
Cohesion: 0.11
Nodes (3): CustomLanClient, GuiCustomLan, CLOptions

### Community 88 - "Material"
Cohesion: 0.06
Nodes (29): NaturaWorldGen, OffLawnGen, Material, AIR, CACTUS, CIRCUITS, CLAY, CLOTH (+21 more)

### Community 90 - "net.neoforged.bus.api.SubscribeEvent"
Cohesion: 0.21
Nodes (3): HeadDrops, SetBonuses, State

### Community 93 - "OldStack"
Cohesion: 0.11
Nodes (7): OldDungeonGen, MinecartChest, Category, Entry, OldStack, Chest, Dispenser

### Community 95 - "ScaffoldingBlock"
Cohesion: 0.19
Nodes (6): Tier, ADVANCED, BASIC, INDUSTRIAL, REINFORCED, ScaffoldingBlock

### Community 96 - "Wood"
Cohesion: 0.11
Nodes (14): Wood, AMARANTH, BLOODWOOD, DARKWOOD, EUCALYPTUS, FUSEWOOD, GHOSTWOOD, HOPSEED (+6 more)

### Community 97 - "Type"
Cohesion: 0.20
Nodes (9): Type, DROP, MOVE_ALL_ITEMS, MOVE_DOWN, MOVE_EVERYTHING, MOVE_ONE_ITEM, MOVE_ONE_STACK, MOVE_TO_SPECIFIC_HOTBAR_SLOT (+1 more)

### Community 101 - "ArtificeWorldGen"
Cohesion: 0.25
Nodes (3): ArtificeWorldGen, Pos, XSRandom

### Community 104 - "Mobs, Spawn-Eier und Boote auf 26.3"
Cohesion: 0.25
Nodes (7): Boote, Client, Drops, Mobs, Spawn-Eier und Boote auf 26.3, Registrieren, Spawn-Eier, Spawns

### Community 107 - "Decorate"
Cohesion: 0.13
Nodes (15): Decorate, BIG_SHROOM, CACTUS, CLAY, DEAD_BUSH, FLOWERS, GRASS, LAKE (+7 more)

### Community 108 - "convert_tree.py"
Cohesion: 0.22
Nodes (8): camel(), convert(), emit_additions(), walk(), main(), Out, resolve_tags(), get()

### Community 109 - "Extrabiomes"
Cohesion: 0.23
Nodes (6): Bark, NE, NW, SE, SW, Extrabiomes

### Community 114 - "HexxitGear.java"
Cohesion: 0.07
Nodes (5): ChestTransporter, ObsidiPlates, Soul, SoulShards, WildCaves

### Community 116 - "ArmorSet"
Cohesion: 0.19
Nodes (4): ArmorSet, SCALE, THIEF, TRIBAL

### Community 118 - "Bäume und Äxte bei TreeCapitator anmelden"
Cohesion: 0.40
Nodes (4): Bäume: Daten-Registry `treecapitator:tree`, Bäume und Äxte bei TreeCapitator anmelden, Prüfen, Äxte und Scheren: Item-Tags

### Community 121 - "Natura.java"
Cohesion: 0.04
Nodes (11): BaseTreeWorldgen, DarkwoodGen, EucalyptusTreeGenShort, FusewoodGen, GlowshroomGenBlueGreen, GlowshroomGenPurple, NaturaGen, NaturaIds (+3 more)

### Community 122 - "OldBlockStates"
Cohesion: 0.06
Nodes (13): Teil 7 – Natura, BackItemLayer, Back, BackItem, BackToolsClient, Bark, NE, NW (+5 more)

### Community 125 - "MapGenNetherBridge"
Cohesion: 0.19
Nodes (5): Found, MapGenNetherBridge, Region, SpawnEntry, Found

### Community 128 - "OldGameIds"
Cohesion: 0.17
Nodes (11): Biome und Dekoratoren, Blöcke anmelden, Bäume im Spiel (Setzlinge), Generator anmelden, Nether, Prüfen, Regel 1: Kern-Klassen ohne Spielklassen, Stolperfallen (+3 more)

### Community 132 - "InvTweaksItemTree"
Cohesion: 0.10
Nodes (3): InvTweaksItemTree, InvTweaksItemTreeCategory, InvTweaksItemTreeItem

### Community 135 - "NaturaEntities.java"
Cohesion: 0.09
Nodes (3): MoreBows, BloodwoodBoat, NaturaEntities

### Community 136 - "Ore"
Cohesion: 0.15
Nodes (10): OldTerrainGen.Listener, Ore, COAL, DIAMOND, DIRT, GOLD, GRAVEL, IRON (+2 more)

### Community 138 - "StructureNetherBridgePieceWeight"
Cohesion: 0.27
Nodes (3): Teil 7b – Natura-Nether, StructureNetherBridgePieces, StructureNetherBridgePieceWeight

### Community 139 - "HeroMC – Hexxit 1:1 für Minecraft 26.3"
Cohesion: 0.18
Nodes (9): Arbeitsplan, Aufbau, Bauen und Starten, HeroMC – Hexxit 1:1 für Minecraft 26.3, Referenzquellen des Originals, Status, Vorgaben, Wann ein Mod als portiert gilt (+1 more)

### Community 142 - "json"
Cohesion: 0.29
Nodes (5): check_model(), van(), check_model(), modelpath(), tinted()

### Community 149 - "Menü, Screen und Pakete in 26.3 (NeoForge 26.3.0.102)"
Cohesion: 0.29
Nodes (5): Container → `AbstractContainerMenu`, Konfiguration und Spielerdaten, Menü, Screen und Pakete in 26.3 (NeoForge 26.3.0.102), Pakete → Custom Payloads, Truhen-GUIs und Inventory Tweaks

### Community 153 - "HexxitGear.java"
Cohesion: 0.05
Nodes (18): ArtificeClient, Upgrade, ARMOR_SPIKES, COUNTERWEIGHT, DIVE_KIT, ELASTIC_LAYERING, ELASTIC_SOLES, FIREDAMP (+10 more)

## Knowledge Gaps
- **179 isolated node(s):** `BASIC`, `REINFORCED`, `INDUSTRIAL`, `ADVANCED`, `SHARPENING_KIT` (+174 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 913 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **72 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `OldWorld` connect `StructureBoundingBox` to `.setBlock`, `OldWorld.java`, `Ore`, `Listener`, `OldBiome`, `OldBiomes.java`, `OldDecoratedChunk`, `EbxlWorldGen`, `StructureComponent`, `random`, `OldTerrainGen`, `.populate`, `OldWorld`, `hexxit.worldgen152.core.populate.OldGen`, `.floor`, `StructureComponent`, `Populate`, `hexxit.worldgen152.core.populate.OldWorld`, `map`, `OldStack`, `OldWorld`, `.setBlock`, `Natura.java`, `MapGenNetherBridge`?**
  _High betweenness centrality (0.122) - this node is a cross-community bridge._
- **What connects `BASIC`, `REINFORCED`, `INDUSTRIAL` to the rest of the system?**
  _179 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `StructureBoundingBox` be split into smaller, more focused modules?**
  _Cohesion score 0.1049863244266779 - nodes in this community are weakly interconnected._
- **Why does `OldBiome` connect `OldBiome` to `Layer`, `.setBlock`, `OldBiomeSource.java`, `OldTerrain`, `OldCarver`, `OldBiomes.java`, `.floor`, `random`, `OldRavines`, `Biome`?**
  _High betweenness centrality (0.038) - this node is a cross-community bridge._
- **Should `Layer` be split into smaller, more focused modules?**
  _Cohesion score 0.09725490196078432 - nodes in this community are weakly interconnected._
- **Should `.setBlock` be split into smaller, more focused modules?**
  _Cohesion score 0.03983200108386398 - nodes in this community are weakly interconnected._
- **Should `ImportOriginalAssets.groovy` be split into smaller, more focused modules?**
  _Cohesion score 0.09333333333333334 - nodes in this community are weakly interconnected._