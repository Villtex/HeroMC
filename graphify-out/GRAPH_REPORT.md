# Graph Report - HeroMC  (2026-11-16)

## Corpus Check
- 3397 files · ~340,759 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 27 file(s) not represented in the graph (top: .properties 23, (none) 1, .toml 1)

## Summary
- 4519 nodes · 14440 edges · 161 communities (84 shown, 77 thin omitted)
- Extraction: 93% EXTRACTED · 7% INFERRED · 0% AMBIGUOUS · INFERRED: 1072 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `b132a9e7`
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
- BoxRecipe.java
- EbxlWorldGen
- StructureComponent
- random
- OldTerrainGen
- OldStructures
- .findIntersecting
- InvTweaksClient.java
- net.minecraft.world.level.block.Block
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
- OldRails
- EnchantingPlus.java
- net.neoforged.bus.api.IEventBus
- gen.py
- biomes.py
- PerlinNoise
- StructurePieceBlockSelector
- EplusConfig
- hexxit.worldgen152.core.populate.OldGen
- .floor
- InvTweaksConfig
- SoulCageBlock.java
- compare_original.py
- net.minecraft.world.level.block.Block
- verify_blocks.py
- NaturaClient.java
- artifice/tools/gen.py
- BeanstalkBlock
- NaturaGen
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
- .isAboveGround
- InvTweaksConfigSortingRule
- EnchantingCategory.java
- JEI-Plugin für einen portierten Mod
- SoulCageBlockEntity.java
- OldDecoration
- LegendOak
- hexxit.worldgen152.core.populate.OldWorld
- net.minecraft.world.item.Item
- InvTweaks
- HexxitGearClient.java
- net.minecraft.client.renderer.entity.state.LivingEntityRenderState
- net.minecraft.client.gui.screens.Screen
- Client-Darstellung in 26.3 (NeoForge 26.3.0.102)
- net.minecraft.world.level.Level
- ImpModel.java
- CLOptions
- Material
- net.neoforged.bus.api.SubscribeEvent
- net.minecraft.resources.Identifier
- ScarecrowEntity
- OldStack
- OldChunkGenerator
- ScaffoldingBlock
- Wood
- InvTweaksHandlerShortcuts
- LanCommands
- ImpEntity.java
- ArtificeWorldGen
- InvTweaksHandlerSorting
- BackItemLayer.java
- Mobs, Spawn-Eier und Boote auf 26.3
- OldWorld
- OldCarver
- .register
- convert_tree.py
- Extrabiomes
- OldBlockStates
- SakuraTreeGen
- net.minecraft.client.model.EntityModel
- GuiCustomLanPermissions
- ArmorSet
- BabyHeatscarSpider
- Bäume und Äxte bei TreeCapitator anmelden
- net.minecraft.world.level.block.Block
- SledgeParts
- BaseTreeWorldgen
- Natura
- UpgradeRecipe
- hexxit.worldgen152.core.OldChunk
- MapGenNetherBridge
- .generate
- .appendHoverText
- OldGameIds
- BloodTreeLargeGen
- DispenseBox.java
- NitroCreeper
- InvTweaksItemTree
- .nextInt
- OldWorld.java
- net.minecraft.world.entity.EntityType
- Ore
- EucalyptusTreeGenShort
- BoxItem
- HeroMC – Hexxit 1:1 für Minecraft 26.3
- CoFHCommand.java
- TableEvents
- json
- CoFHWorldGen
- HexbiscusBlock
- HeatscarSpider
- NitroCreeperRenderer.java
- Bark
- .generate
- ComponentNetherBridgeCorridor2
- ComponentNetherBridgeCorridor
- StructureBoundingBox
- ComponentNetherBridgeEntrance
- Upgrade
- .generate
- OldRavines
- MoreBowsJeiPlugin
- NaturaIds.java

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
- `Teil 7b – Natura-Nether` --references--> `StructureNetherBridgePieces`  [INFERRED]
  docs/PLAN.md → mods/worldgen152/src/main/java/hexxit/worldgen152/core/structure/StructureNetherBridgePieces.java
- `Aufbau` --references--> `OldDecorator`  [INFERRED]
  .claude/skills/worldgen152/SKILL.md → mods/worldgen152/src/main/java/hexxit/worldgen152/core/populate/OldDecorator.java
- `Teil 1 – Vanilla-1.5.2-Dekoration` --references--> `OldDecorator`  [INFERRED]
  docs/PLAN.md → mods/worldgen152/src/main/java/hexxit/worldgen152/core/populate/OldDecorator.java
- `Aufbau` --references--> `OldGen`  [INFERRED]
  .claude/skills/worldgen152/SKILL.md → mods/worldgen152/src/main/java/hexxit/worldgen152/core/populate/OldGen.java

## Import Cycles
- None detected.

## Communities (161 total, 77 thin omitted)

### Community 0 - "StructureBoundingBox"
Cohesion: 0.11
Nodes (4): Witch, Category, Entry, Spawner

### Community 1 - "Layer"
Cohesion: 0.09
Nodes (16): Layer, AddIsland, AddMushroomIsland, AddSnow, Biome, Hills, Island, Layers (+8 more)

### Community 2 - "reference.py"
Cohesion: 0.18
Nodes (10): build_mappings(), digest(), download(), inner_archives(), java(), main(), minecraft_reference(), process() (+2 more)

### Community 3 - ".setBlock"
Cohesion: 0.05
Nodes (32): TreeChooser, OldDecorator, OldDungeonGen, OldGen, DesertWell, OldLakeGens, Spring, Fire (+24 more)

### Community 4 - "net.minecraft.world.item.ItemStack"
Cohesion: 0.10
Nodes (6): Container → `AbstractContainerMenu`, EnchantHelper, OldCost, EnchantTableMenu, Slot, Settings

### Community 8 - "Arbeitsplan: Hexxit 1.0.11 → NeoForge 26.3"
Cohesion: 0.10
Nodes (20): Arbeitsplan: Hexxit 1.0.11 → NeoForge 26.3, Regeln für jeden Teil, Teil 10 und 11 – Project Zulu, Teil 14 – Battle Towers, Ruins, Teil 15 – Dimensional Doors, Teil 16 – BiblioCraft, Teil 17 – BetterStorage, Backpacks, qCraft, Teil 18 – ChocoCraft, Infernal Mobs (+12 more)

### Community 11 - "HeroMC – Hinweise für Claude"
Cohesion: 0.29
Nodes (6): Arbeitsweise beim Portieren, Branches, graphify, HeroMC – Hinweise für Claude, Skills anlegen, Tokens sparen

### Community 17 - "OldBiome"
Cohesion: 0.13
Nodes (5): EbxlBiomes, Decoration, GrassChooser, OldBiome, OldBiomes

### Community 18 - "OldBiomes.java"
Cohesion: 0.21
Nodes (4): OldBlockInfo, OldEntity, OldLoot, OldTile

### Community 19 - "OldDecoratedChunk"
Cohesion: 0.12
Nodes (3): OldChunk, OldDecoratedChunk, OldChunkWriter

### Community 21 - "EbxlWorldGen"
Cohesion: 0.08
Nodes (7): Choices, EbxlWorldGen, Jungle, Weighted, OffLawnGen, Lake, OldPopulator

### Community 22 - "StructureComponent"
Cohesion: 0.05
Nodes (19): Teil 12 und 13 – Better Dungeons (Chocolate Quest), ComponentVillage, ComponentVillageChurch, ComponentVillageField2, ComponentVillageField, ComponentVillageHall, ComponentVillageHouse1, ComponentVillageHouse2 (+11 more)

### Community 24 - "OldTerrainGen"
Cohesion: 0.18
Nodes (6): Aufbau, Teil 1 – Vanilla-1.5.2-Dekoration, HGWorldGen, Entry, OldTerrainGen, WorldGenerator

### Community 25 - "OldStructures"
Cohesion: 0.11
Nodes (8): Found, OldStructures, StructureMineshaftStart, StructureNetherBridgeStart, StructureScatteredFeatureStart, StructureStart, StructureStrongholdStart, StructureVillageStart

### Community 26 - ".findIntersecting"
Cohesion: 0.07
Nodes (20): ComponentMineshaftStairs, ComponentStronghold, ComponentStrongholdChestCorridor, ComponentStrongholdCrossing, ComponentStrongholdLeftTurn, ComponentStrongholdLibrary, ComponentStrongholdPortalRoom, ComponentStrongholdPrison (+12 more)

### Community 27 - "InvTweaksClient.java"
Cohesion: 0.04
Nodes (8): GuiCustomLan, InvTweaksGuiIconButton, InvTweaksGuiModNotWorking, TextButton, InvTweaksGuiSettingsButton, InvTweaksGuiSortingButton, InvTweaksConst, InvTweaksLocalization

### Community 28 - "net.minecraft.world.level.block.Block"
Cohesion: 0.05
Nodes (8): FloraBlock, CloudBlock, NaturaPlantBlock, SaguaroFruitBlock, SeedBagItem, FloraBlock, IcicleBlock, StalactiteBlock

### Community 30 - "ArmorBarHud"
Cohesion: 0.08
Nodes (11): Kind, AMOUNT, CHEST, DURABILITY, FEET, HEAD, LEGS, Unit (+3 more)

### Community 31 - "minecraft-dev"
Cohesion: 0.50
Nodes (3): npx, minecraft-dev, @mcdxai/minecraft-dev-mcp

### Community 32 - "InvTweaksContainerManager"
Cohesion: 0.08
Nodes (18): ContainerSection, ARMOR, BREWING_BOTTLES, BREWING_INGREDIENT, CHEST, CRAFTING_IN, CRAFTING_IN_PERSISTENT, CRAFTING_OUT (+10 more)

### Community 33 - "Override"
Cohesion: 0.15
Nodes (3): Barley, Cotton, NaturaCropBlock

### Community 34 - "net.minecraft.world.item.ItemStack"
Cohesion: 0.12
Nodes (7): Kind, ENDER, FLAME, FROST, MULTI, PLAIN, MoreBowItem

### Community 36 - "TreeDefinition"
Cohesion: 0.06
Nodes (5): BlockID, TreeCapitator, Data, TreeDefinition, TreeRegistry

### Community 40 - "OldRails"
Cohesion: 0.06
Nodes (26): OldNetherPopulator, OldRails, Decorate, BIG_SHROOM, CACTUS, CLAY, DEAD_BUSH, FLOWERS (+18 more)

### Community 41 - "EnchantingPlus.java"
Cohesion: 0.07
Nodes (8): EnchantException, EnchantingPlus, Restriction, ClientOptionPayload, EnchantPayload, EplusNetwork, OpenModTablePayload, UseModPayload

### Community 42 - "net.neoforged.bus.api.IEventBus"
Cohesion: 0.05
Nodes (13): ArmorBarMod, Artifice, ArtificeIds, BackTools, bspkrsCoreMod, BuffBarMod, ChestTransporter, CoFHCore (+5 more)

### Community 43 - "gen.py"
Cohesion: 0.09
Nodes (22): age(), ages(), bitag(), blockstate(), btag(), cube_all(), flam(), generated_item() (+14 more)

### Community 44 - "biomes.py"
Cohesion: 0.15
Nodes (9): ColorMaps, f32(), fixed(), hexcolor(), main(), multiply(), sky_color(), tinted() (+1 more)

### Community 46 - "StructurePieceBlockSelector"
Cohesion: 0.09
Nodes (7): ComponentScatteredFeature, ComponentScatteredFeatureDesertPyramid, ComponentScatteredFeatureJunglePyramid, ComponentScatteredFeatureSwampHut, StructurePieceBlockSelector, StructureScatteredFeatureStones, StructureStrongholdStones

### Community 47 - "EplusConfig"
Cohesion: 0.08
Nodes (8): Konfiguration und Spielerdaten, Menü, Screen und Pakete in 26.3 (NeoForge 26.3.0.102), Pakete → Custom Payloads, Truhen-GUIs und Inventory Tweaks, Vanilla-Block ersetzen (Mod hat Block-ID überschrieben), EplusClient, EplusConfig, TCSettings

### Community 49 - "hexxit.worldgen152.core.populate.OldGen"
Cohesion: 0.07
Nodes (19): CatTail, EbxlPlantGens, Flowers, LeafPile, Marsh, Melon, Oasis, Quicksand (+11 more)

### Community 50 - ".floor"
Cohesion: 0.11
Nodes (3): BushTreeGen, OldMath, BigTree

### Community 51 - "InvTweaksConfig"
Cohesion: 0.08
Nodes (4): InvTweaksConfig, OrderedProperties, InvTweaksConfigManager, InvTweaksHandlerAutoRefill

### Community 53 - "compare_original.py"
Cohesion: 0.19
Nodes (7): cells(), main(), read_nbt(), payload(), string(), take(), region_chunks()

### Community 54 - "net.minecraft.world.level.block.Block"
Cohesion: 0.03
Nodes (6): ScarecrowItem, BloodwoodBoat, BoneBagItem, ImpMeatItem, OffLawn, SunflowerSeedItem

### Community 55 - "verify_blocks.py"
Cohesion: 0.14
Nodes (15): main(), packet(), read(), block(), ebxl(), flag(), main(), saguaro() (+7 more)

### Community 56 - "NaturaClient.java"
Cohesion: 0.08
Nodes (4): ArtificeClient, ColormapTint, NaturaClient, OffLawnClient

### Community 57 - "artifice/tools/gen.py"
Cohesion: 0.11
Nodes (21): bitag(), blockstate(), btag(), cube_all(), cube_element(), face(), generated_item(), handheld_item() (+13 more)

### Community 59 - "NaturaGen"
Cohesion: 0.07
Nodes (6): DarkwoodGen, GlowshroomGenBlueGreen, GlowshroomGenPurple, NaturaGen, RareTreeGen, WillowGen

### Community 60 - "NaturaBlocks.java"
Cohesion: 0.04
Nodes (14): ArtificeBlocks, FrameBlock, CattailBlock, EbxlLeavesBlock, EbxlPlantBlock, LeafPileBlock, QuarterLogBlock, EbxlBlocks (+6 more)

### Community 61 - "net.minecraft.world.level.block.state.BlockState"
Cohesion: 0.05
Nodes (11): LotusBlock, LotusLilyBlock, EbxlSaplingBlock, BerryBushBlock, GlowshroomBlock, NaturaSaplingBlock, SaguaroBlock, SaguaroSproutBlock (+3 more)

### Community 62 - "BackToolsClient.java"
Cohesion: 0.14
Nodes (3): Back, BackItem, BackToolsClient

### Community 63 - "EbxlClient.java"
Cohesion: 0.10
Nodes (4): EbxlClient, FrostArrowRenderer, MoreBowsClient, NaturaEntityRenderers

### Community 67 - "EnchantTableScreen"
Cohesion: 0.14
Nodes (3): GuiContainer → `AbstractContainerScreen`, EnchantTableScreen, GuiItem

### Community 68 - "StructureComponent"
Cohesion: 0.05
Nodes (15): Teil 7b – Natura-Nether, ComponentMineshaftCorridor, ComponentMineshaftRoom, ComponentNetherBridgeCorridor3, ComponentNetherBridgeCorridor4, ComponentNetherBridgeCrossing3, ComponentNetherBridgeNetherStalkRoom, ComponentNetherBridgePiece (+7 more)

### Community 69 - "net.minecraft.server.level.ServerLevel"
Cohesion: 0.05
Nodes (34): Blöcke (Java), Brennwert und Brennbarkeit, Holzsets und Pflanzen in 26.3, Modelle und Blockstates aus Vanilla erzeugen, Prüfen, Rezeptkonflikte mit Vanilla, Tags (Block + Item), Growth (+26 more)

### Community 70 - "net.minecraft.resources.Identifier"
Cohesion: 0.14
Nodes (5): ChestTransporterJeiPlugin, EbxlJeiPlugin, HexxitGearJeiPlugin, ObsidiPlatesJeiPlugin, SoulShardsJeiPlugin

### Community 71 - ".isAboveGround"
Cohesion: 0.08
Nodes (6): ComponentNetherBridgeCorridor5, ComponentNetherBridgeCrossing2, ComponentNetherBridgeCrossing, ComponentNetherBridgeEnd, ComponentNetherBridgeStraight, ComponentNetherBridgeThrone

### Community 72 - "InvTweaksConfigSortingRule"
Cohesion: 0.08
Nodes (7): InvTweaksConfigInventoryRuleset, InvTweaksConfigSortingRule, Type, COLUMN, RECTANGLE, ROW, SLOT

### Community 74 - "JEI-Plugin für einen portierten Mod"
Cohesion: 0.40
Nodes (4): Einbindung (schon erledigt, in `build.gradle`), JEI-Plugin für einen portierten Mod, Prüfen, Vorlage

### Community 76 - "OldDecoration"
Cohesion: 0.19
Nodes (3): OldDecoration, TerrainSource, World

### Community 77 - "LegendOak"
Cohesion: 0.25
Nodes (9): Acuteness, LOOSE, TIGHT, TIGHTER, Bend, LEFT, RIGHT, STRAIGHT (+1 more)

### Community 79 - "net.minecraft.world.item.Item"
Cohesion: 0.05
Nodes (11): ArtificeRecipes, SickleItem, SledgeItem, HexxitGear, KamaItem, NaturaItems, ToolSet, ObsidiPlates (+3 more)

### Community 80 - "InvTweaks"
Cohesion: 0.09
Nodes (3): InvTweaks, Scheduled, InvTweaksClient

### Community 81 - "HexxitGearClient.java"
Cohesion: 0.13
Nodes (3): HelmetModels, HexxitGearClient, IClientItemExtensions

### Community 82 - "net.minecraft.client.renderer.entity.state.LivingEntityRenderState"
Cohesion: 0.15
Nodes (3): ScarecrowRenderer, HeatscarSpiderRenderer, ImpRenderer

### Community 83 - "net.minecraft.client.gui.screens.Screen"
Cohesion: 0.12
Nodes (4): InvTweaksGuiSettings, InvTweaksGuiSettingsAbstract, InvTweaksGuiSettingsAdvanced, InvTweaksGuiShortcutsHelp

### Community 84 - "Client-Darstellung in 26.3 (NeoForge 26.3.0.102)"
Cohesion: 0.22
Nodes (8): Client-Darstellung in 26.3 (NeoForge 26.3.0.102), Etwas am Spielermodell zeichnen (Rücken, Schultern), Halbdurchsichtige Blöcke (Alpha im Renderer), HUD (statt `ITickHandler` mit `TickType.RENDER`), Nur-Client-Mod sauber laden, Rechtsklick vor dem Block (Original: `PlayerInteractEvent` mit `RIGHT_CLICK_BLOCK`), Rüstung: Texturen und eigene Helm-Modelle (`ItemArmor`, `getArmorTexture`, `getArmorModel`), Stolperfallen

### Community 85 - "net.minecraft.world.level.Level"
Cohesion: 0.08
Nodes (5): QuicksandBlock, HeatSandBlock, LargeGlowshroomBlock, ThornVinesBlock, PlayerMobPlateBlock

### Community 88 - "Material"
Cohesion: 0.07
Nodes (27): Material, AIR, CACTUS, CIRCUITS, CLAY, CLOTH, CRAFTED_SNOW, FIRE (+19 more)

### Community 90 - "net.neoforged.bus.api.SubscribeEvent"
Cohesion: 0.19
Nodes (3): HeadDrops, SetBonuses, State

### Community 93 - "OldStack"
Cohesion: 0.22
Nodes (4): MinecartChest, OldStack, Chest, Dispenser

### Community 95 - "ScaffoldingBlock"
Cohesion: 0.22
Nodes (6): Tier, ADVANCED, BASIC, INDUSTRIAL, REINFORCED, ScaffoldingBlock

### Community 96 - "Wood"
Cohesion: 0.11
Nodes (14): Wood, AMARANTH, BLOODWOOD, DARKWOOD, EUCALYPTUS, FUSEWOOD, GHOSTWOOD, HOPSEED (+6 more)

### Community 97 - "InvTweaksHandlerShortcuts"
Cohesion: 0.06
Nodes (25): Action, DROP, MOVE, InvTweaksHandlerShortcuts, Scope, ALL_ITEMS, EVERYTHING, ONE_ITEM (+17 more)

### Community 101 - "ArtificeWorldGen"
Cohesion: 0.20
Nodes (3): ArtificeWorldGen, Pos, XSRandom

### Community 104 - "Mobs, Spawn-Eier und Boote auf 26.3"
Cohesion: 0.25
Nodes (7): Boote, Client, Drops, Mobs, Spawn-Eier und Boote auf 26.3, Registrieren, Spawn-Eier, Spawns

### Community 105 - "OldWorld"
Cohesion: 0.05
Nodes (9): Generator anmelden, Teil 5 – Hexxit Gear, More Bows, Enchanting Plus, offLawn, Wild Caves 3, BaseCropWorldgen, WildCavesGen, Glowshroom, Listener, Skull, Column (+1 more)

### Community 108 - "convert_tree.py"
Cohesion: 0.22
Nodes (8): camel(), convert(), emit_additions(), walk(), main(), Out, resolve_tags(), get()

### Community 109 - "Extrabiomes"
Cohesion: 0.23
Nodes (6): Bark, NE, NW, SE, SW, Extrabiomes

### Community 116 - "ArmorSet"
Cohesion: 0.19
Nodes (4): ArmorSet, SCALE, THIEF, TRIBAL

### Community 118 - "Bäume und Äxte bei TreeCapitator anmelden"
Cohesion: 0.40
Nodes (4): Bäume: Daten-Registry `treecapitator:tree`, Bäume und Äxte bei TreeCapitator anmelden, Prüfen, Äxte und Scheren: Item-Tags

### Community 121 - "BaseTreeWorldgen"
Cohesion: 0.11
Nodes (4): BaseTreeWorldgen, FusewoodGen, SaguaroGen, WhiteTreeGen

### Community 125 - "MapGenNetherBridge"
Cohesion: 0.22
Nodes (4): Found, MapGenNetherBridge, Region, SpawnEntry

### Community 127 - ".appendHoverText"
Cohesion: 0.11
Nodes (4): HomeDimensions, SoulEvents, SoulShardItem, Soul

### Community 128 - "OldGameIds"
Cohesion: 0.18
Nodes (10): Biome und Dekoratoren, Blöcke anmelden, Bäume im Spiel (Setzlinge), Nether, Prüfen, Regel 1: Kern-Klassen ohne Spielklassen, Stolperfallen, Strukturen (+2 more)

### Community 130 - "DispenseBox.java"
Cohesion: 0.17
Nodes (3): ArtificeEvents, DispenseBox, DispenseSledge

### Community 132 - "InvTweaksItemTree"
Cohesion: 0.07
Nodes (4): InvTweaksItemTree, InvTweaksItemTreeCategory, InvTweaksItemTreeItem, InvTweaksItemTreeLoader

### Community 136 - "Ore"
Cohesion: 0.14
Nodes (10): OldTerrainGen.Listener, Ore, COAL, DIAMOND, DIRT, GOLD, GRAVEL, IRON (+2 more)

### Community 139 - "HeroMC – Hexxit 1:1 für Minecraft 26.3"
Cohesion: 0.18
Nodes (9): Arbeitsplan, Aufbau, Bauen und Starten, HeroMC – Hexxit 1:1 für Minecraft 26.3, Referenzquellen des Originals, Status, Vorgaben, Wann ein Mod als portiert gilt (+1 more)

### Community 142 - "json"
Cohesion: 0.29
Nodes (5): check_model(), van(), check_model(), modelpath(), tinted()

### Community 147 - "Bark"
Cohesion: 0.29
Nodes (5): Bark, NE, NW, SE, SW

### Community 151 - "StructureBoundingBox"
Cohesion: 0.10
Nodes (5): Villager, ComponentMineshaftCross, ComponentStrongholdCorridor, VillageBlocks, StructureBoundingBox

### Community 153 - "Upgrade"
Cohesion: 0.10
Nodes (16): ArtificeItems, Upgrade, ARMOR_SPIKES, COUNTERWEIGHT, DIVE_KIT, ELASTIC_LAYERING, ELASTIC_SOLES, FIREDAMP (+8 more)

## Knowledge Gaps
- **179 isolated node(s):** `BIG_SHROOM`, `CACTUS`, `CLAY`, `DEAD_BUSH`, `FLOWERS` (+174 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 912 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **77 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `OldWorld` connect `OldWorld` to `StructureBoundingBox`, `.setBlock`, `OldWorld.java`, `Ore`, `OldBiome`, `OldBiomes.java`, `EbxlWorldGen`, `ComponentNetherBridgeCorridor2`, `random`, `OldTerrainGen`, `StructureBoundingBox`, `ComponentNetherBridgeCorridor`, `ComponentNetherBridgeEntrance`, `StructureComponent`, `.spawnEntity`, `OldRails`, `hexxit.worldgen152.core.populate.OldGen`, `.floor`, `StructureComponent`, `.isAboveGround`, `OldDecoration`, `LegendOak`, `hexxit.worldgen152.core.populate.OldWorld`, `ArtificeWorldGen`, `BaseTreeWorldgen`, `MapGenNetherBridge`?**
  _High betweenness centrality (0.147) - this node is a cross-community bridge._
- **What connects `BIG_SHROOM`, `CACTUS`, `CLAY` to the rest of the system?**
  _179 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `StructureBoundingBox` be split into smaller, more focused modules?**
  _Cohesion score 0.11210317460317461 - nodes in this community are weakly interconnected._
- **Why does `OldBiome` connect `OldBiome` to `Layer`, `.setBlock`, `OldBiomeSource.java`, `OldTerrain`, `OldCarver`, `OldBiomes.java`, `.floor`, `random`, `OldRavines`?**
  _High betweenness centrality (0.043) - this node is a cross-community bridge._
- **Should `Layer` be split into smaller, more focused modules?**
  _Cohesion score 0.09294199860237597 - nodes in this community are weakly interconnected._
- **Why does `OldStructures` connect `OldStructures` to `StructureComponent`, `OldChunkGenerator.java`, `OldDecoration`, `EbxlWorldGen`, `StructureComponent`, `random`, `StructureBoundingBox`, `MapGenNetherBridge`?**
  _High betweenness centrality (0.016) - this node is a cross-community bridge._
- **Should `.setBlock` be split into smaller, more focused modules?**
  _Cohesion score 0.048771929824561404 - nodes in this community are weakly interconnected._