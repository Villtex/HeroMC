# Graph Report - HeroMC  (2026-11-16)

## Corpus Check
- 2921 files · ~296,850 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 25 file(s) not represented in the graph (top: .properties 21, (none) 1, .toml 1)

## Summary
- 3993 nodes · 12606 edges · 130 communities (77 shown, 53 thin omitted)
- Extraction: 92% EXTRACTED · 8% INFERRED · 0% AMBIGUOUS · INFERRED: 969 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `f2e27f99`
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
- EbxlWorldGen.java
- OldStructures
- .findIntersecting
- net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
- net.minecraft.core.BlockPos
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
- HeatscarSpider.java
- EbxlTreeGens
- OldGen
- InvTweaksConfig
- net.minecraft.server.level.ServerLevel
- compare_original.py
- net.minecraft.world.level.block.Block
- verify_blocks.py
- NaturaClient.java
- OldBlockStates
- BeanstalkBlock
- TableEvents.java
- NaturaBlocks.java
- EbxlLeavesBlock.java
- BackToolsClient
- EbxlClient.java
- original_server.sh
- .setBlock
- net.minecraft.world.item.ItemStack
- EnchantTableScreen
- StructureComponent
- net.minecraft.server.level.ServerLevel
- net.minecraft.resources.Identifier
- .addComponentParts
- InvTweaksConfigSortingRule
- EnchantingCategory.java
- JEI-Plugin für einen portierten Mod
- InvTweaksHandlerShortcuts
- net.neoforged.bus.api.SubscribeEvent
- LegendOak
- hexxit.worldgen152.core.populate.OldWorld
- NaturaItems.java
- InvTweaks
- HexxitGearClient.java
- net.minecraft.client.renderer.entity.state.LivingEntityRenderState
- .get
- Client-Darstellung in 26.3 (NeoForge 26.3.0.102)
- net.minecraft.world.level.Level
- ImpModel.java
- minecraft
- StalactiteBlock
- net.minecraft.world.entity.player.Player
- map
- ScarecrowEntity
- OldStack
- WorldGen152.java
- InvTweaksItemTree
- net.minecraft.world.level.block.state.BlockState
- Type
- LanCommands
- ImpEntity.java
- Holzsets und Pflanzen in 26.3
- InvTweaksHandlerSorting
- Mobs, Spawn-Eier und Boote auf 26.3
- HeatscarSpider
- OldCarver
- Expect.java
- convert_tree.py
- QuarterLogBlock
- .copyVanilla
- Wood
- SakuraTreeGen
- HexxitGear.java
- GuiCustomLanPermissions
- OldDecoration
- Bäume und Äxte bei TreeCapitator anmelden
- EnchantTableMenu
- BaseTreeWorldgen
- EnchantTableScreen.java
- .generate
- SoulEvents.java
- EnchantHelper.java
- ObsidiPlates
- NitroCreeper
- .nextInt
- NaturaEntities.java
- check.py

## God Nodes (most connected - your core abstractions)
1. `OldWorld` - 249 edges
2. `StructureBoundingBox` - 111 edges
3. `OldBlockInfo` - 75 edges
4. `StructureComponent` - 67 edges
5. `OldLoot` - 65 edges
6. `OldTile` - 64 edges
7. `OldEntity` - 63 edges
8. `NaturaBlocks` - 60 edges
9. `RedwoodTreeGen` - 55 edges
10. `InvTweaksContainerManager` - 48 edges

## Surprising Connections (you probably didn't know these)
- `Bäume im Spiel (Setzlinge)` --references--> `OldGen`  [INFERRED]
  .claude/skills/worldgen152/SKILL.md → mods/worldgen152/src/main/java/hexxit/worldgen152/core/populate/OldGen.java
- `Aufbau` --references--> `OldGen`  [INFERRED]
  .claude/skills/worldgen152/SKILL.md → mods/worldgen152/src/main/java/hexxit/worldgen152/core/populate/OldGen.java
- `Teil 5 – Hexxit Gear, More Bows, Enchanting Plus, offLawn, Wild Caves 3` --references--> `IClientItemExtensions`  [INFERRED]
  docs/PLAN.md → mods/hexxitgear/src/main/java/sct/hexxitgear/client/HexxitGearClient.java
- `Teil 12 und 13 – Better Dungeons (Chocolate Quest)` --references--> `VillageHandler`  [INFERRED]
  docs/PLAN.md → mods/worldgen152/src/main/java/hexxit/worldgen152/core/structure/OldStructures.java
- `Aufbau` --references--> `OldDecorator`  [INFERRED]
  .claude/skills/worldgen152/SKILL.md → mods/worldgen152/src/main/java/hexxit/worldgen152/core/populate/OldDecorator.java

## Import Cycles
- None detected.

## Communities (130 total, 53 thin omitted)

### Community 0 - "StructureBoundingBox"
Cohesion: 0.13
Nodes (5): Listener, Spawner, OldWorld, ComponentStrongholdRightTurn, StructureBoundingBox

### Community 1 - "Layer"
Cohesion: 0.10
Nodes (15): Layer, AddIsland, AddMushroomIsland, AddSnow, Biome, Hills, Layers, River (+7 more)

### Community 2 - "reference.py"
Cohesion: 0.18
Nodes (10): build_mappings(), digest(), download(), inner_archives(), java(), main(), minecraft_reference(), process() (+2 more)

### Community 3 - "Menü, Screen und Pakete in 26.3 (NeoForge 26.3.0.102)"
Cohesion: 0.18
Nodes (7): Container → `AbstractContainerMenu`, GuiContainer → `AbstractContainerScreen`, Konfiguration und Spielerdaten, Menü, Screen und Pakete in 26.3 (NeoForge 26.3.0.102), Pakete → Custom Payloads, Truhen-GUIs und Inventory Tweaks, Vanilla-Block ersetzen (Mod hat Block-ID überschrieben)

### Community 4 - "net.minecraft.world.item.ItemStack"
Cohesion: 0.19
Nodes (3): EnchantHelper, OldCost, Restriction

### Community 7 - "OldTerrain"
Cohesion: 0.19
Nodes (3): BiomeLayout, OctaveNoise, OldTerrain

### Community 8 - "Arbeitsplan: Hexxit 1.0.11 → NeoForge 26.3"
Cohesion: 0.06
Nodes (31): Arbeitsplan: Hexxit 1.0.11 → NeoForge 26.3, Regeln für jeden Teil, Teil 10 und 11 – Project Zulu, Teil 14 – Battle Towers, Ruins, Teil 15 – Dimensional Doors, Teil 16 – BiblioCraft, Teil 17 – BetterStorage, Backpacks, qCraft, Teil 18 – ChocoCraft, Infernal Mobs (+23 more)

### Community 11 - "HeroMC – Hinweise für Claude"
Cohesion: 0.29
Nodes (6): Arbeitsweise beim Portieren, Branches, graphify, HeroMC – Hinweise für Claude, Skills anlegen, Tokens sparen

### Community 17 - "OldBiome"
Cohesion: 0.12
Nodes (6): EbxlBiomes, Decoration, GrassChooser, OldBiome, TreeChooser, OldBiomes

### Community 18 - "OldBlockInfo"
Cohesion: 0.06
Nodes (36): NaturaWorldGen, OldBlocks, Material, AIR, CACTUS, CIRCUITS, CLAY, CLOTH (+28 more)

### Community 19 - "OldDecoratedChunk"
Cohesion: 0.12
Nodes (3): OldChunk, OldDecoratedChunk, OldChunkWriter

### Community 21 - "EbxlWorldGen"
Cohesion: 0.10
Nodes (8): Choices, EbxlWorldGen, Jungle, Weighted, DesertWell, Lake, OldLakeGens, Spring

### Community 22 - "StructureComponent"
Cohesion: 0.06
Nodes (19): Teil 12 und 13 – Better Dungeons (Chocolate Quest), ComponentVillage, ComponentVillageChurch, ComponentVillageField2, ComponentVillageField, ComponentVillageHall, ComponentVillageHouse1, ComponentVillageHouse2 (+11 more)

### Community 24 - "EbxlWorldGen.java"
Cohesion: 0.03
Nodes (50): Aufbau, Biome und Dekoratoren, Blöcke anmelden, Bäume im Spiel (Setzlinge), Generator anmelden, Prüfen, Regel 1: Kern-Klassen ohne Spielklassen, Stolperfallen (+42 more)

### Community 25 - "OldStructures"
Cohesion: 0.07
Nodes (8): TerrainSource, Found, OldStructures, StructureMineshaftStart, StructureScatteredFeatureStart, StructureStart, StructureStrongholdStart, StructureVillageStart

### Community 26 - ".findIntersecting"
Cohesion: 0.08
Nodes (18): ComponentStrongholdChestCorridor, ComponentStrongholdCorridor, ComponentStrongholdCrossing, ComponentStrongholdLeftTurn, ComponentStrongholdLibrary, ComponentStrongholdPortalRoom, ComponentStrongholdPrison, ComponentStrongholdRoomCrossing (+10 more)

### Community 27 - "net.minecraft.client.gui.screens.inventory.AbstractContainerScreen"
Cohesion: 0.06
Nodes (4): InvTweaksGuiIconButton, InvTweaksGuiSortingButton, InvTweaksConst, InvTweaksLocalization

### Community 28 - "net.minecraft.core.BlockPos"
Cohesion: 0.08
Nodes (7): CattailBlock, EbxlPlantBlock, HexbiscusBlock, NaturaPlantBlock, SaguaroFruitBlock, FloraBlock, IcicleBlock

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
Cohesion: 0.12
Nodes (7): Kind, ENDER, FLAME, FROST, MULTI, PLAIN, MoreBowItem

### Community 36 - "TreeDefinition"
Cohesion: 0.05
Nodes (6): BlockID, TreeCapitator, TreeCapitatorMod, Data, TreeDefinition, TreeRegistry

### Community 38 - ".opaqueCube"
Cohesion: 0.10
Nodes (5): BaseCloudWorldgen, BaseCropWorldgen, BerryBushGen, CloudGen, NetherBerryBushGen

### Community 41 - "EnchantingPlus.java"
Cohesion: 0.13
Nodes (5): EnchantingPlus, ClientOptionPayload, EnchantPayload, OpenModTablePayload, UseModPayload

### Community 42 - "net.neoforged.bus.api.IEventBus"
Cohesion: 0.08
Nodes (5): ArmorBarMod, BackTools, BuffBarMod, BuffBarHud, TCSettings

### Community 43 - "gen.py"
Cohesion: 0.09
Nodes (23): age(), ages(), bitag(), blockstate(), btag(), cube_all(), flam(), generated_item() (+15 more)

### Community 44 - "biomes.py"
Cohesion: 0.15
Nodes (9): ColorMaps, f32(), fixed(), hexcolor(), main(), multiply(), sky_color(), tinted() (+1 more)

### Community 46 - "StructurePieceBlockSelector"
Cohesion: 0.09
Nodes (7): ComponentScatteredFeature, ComponentScatteredFeatureDesertPyramid, ComponentScatteredFeatureJunglePyramid, ComponentScatteredFeatureSwampHut, StructurePieceBlockSelector, StructureScatteredFeatureStones, StructureStrongholdStones

### Community 49 - "EbxlTreeGens"
Cohesion: 0.12
Nodes (8): Acacia, Autumn, BigAutumn, CustomSwamp, EbxlTreeGens, Fir, FirHuge, Redwood

### Community 50 - "OldGen"
Cohesion: 0.08
Nodes (20): OldGen, Clay, Sand, Cactus, Flowers, OldPlantGens, Reed, TallGrass (+12 more)

### Community 51 - "InvTweaksConfig"
Cohesion: 0.08
Nodes (4): InvTweaksConfig, OrderedProperties, InvTweaksConfigManager, InvTweaksHandlerAutoRefill

### Community 53 - "compare_original.py"
Cohesion: 0.19
Nodes (7): cells(), main(), read_nbt(), payload(), string(), take(), region_chunks()

### Community 54 - "net.minecraft.world.level.block.Block"
Cohesion: 0.07
Nodes (6): LogTurnerItem, ScarecrowItem, BoneBagItem, SeedBagItem, SunflowerSeedItem, ShadowPickaxeItem

### Community 55 - "verify_blocks.py"
Cohesion: 0.14
Nodes (15): main(), packet(), read(), block(), ebxl(), flag(), main(), saguaro() (+7 more)

### Community 57 - "OldBlockStates"
Cohesion: 0.11
Nodes (4): Extrabiomes, Grower, Natura, OldBlockStates

### Community 60 - "NaturaBlocks.java"
Cohesion: 0.05
Nodes (8): LeafPileBlock, EbxlBlocks, BloodwoodLogBlock, FusewoodLogBlock, Menu, NaturaWorkbenchBlock, NaturaIds, NaturaBlocks

### Community 62 - "BackToolsClient"
Cohesion: 0.14
Nodes (3): Back, BackItem, BackToolsClient

### Community 63 - "EbxlClient.java"
Cohesion: 0.08
Nodes (5): EbxlClient, FrostArrowRenderer, MoreBowsClient, NaturaEntityRenderers, OffLawnClient

### Community 65 - ".setBlock"
Cohesion: 0.07
Nodes (12): CatTail, EbxlPlantGens, Flowers, LeafPile, Marsh, Melon, Oasis, Quicksand (+4 more)

### Community 68 - "StructureComponent"
Cohesion: 0.07
Nodes (8): ComponentMineshaftCorridor, ComponentMineshaftCross, ComponentMineshaftRoom, ComponentMineshaftStairs, ComponentStronghold, ComponentStrongholdStairs2, StructureComponent, StructureStrongholdPieceWeight

### Community 69 - "net.minecraft.server.level.ServerLevel"
Cohesion: 0.04
Nodes (34): EbxlSaplingBlock, GlowshroomBlock, Growth, FLORA, NETHER, RARE, REDWOOD, NaturaSaplingBlock (+26 more)

### Community 70 - "net.minecraft.resources.Identifier"
Cohesion: 0.12
Nodes (6): ChestTransporterJeiPlugin, EbxlJeiPlugin, HexxitGearJeiPlugin, MoreBowsJeiPlugin, ObsidiPlatesJeiPlugin, SoulShardsJeiPlugin

### Community 71 - ".addComponentParts"
Cohesion: 0.13
Nodes (3): Villager, Witch, VillageBlocks

### Community 72 - "InvTweaksConfigSortingRule"
Cohesion: 0.08
Nodes (7): InvTweaksConfigInventoryRuleset, InvTweaksConfigSortingRule, Type, COLUMN, RECTANGLE, ROW, SLOT

### Community 74 - "JEI-Plugin für einen portierten Mod"
Cohesion: 0.40
Nodes (4): Einbindung (schon erledigt, in `build.gradle`), JEI-Plugin für einen portierten Mod, Prüfen, Vorlage

### Community 75 - "InvTweaksHandlerShortcuts"
Cohesion: 0.10
Nodes (15): Action, DROP, MOVE, InvTweaksHandlerShortcuts, Scope, ALL_ITEMS, EVERYTHING, ONE_ITEM (+7 more)

### Community 77 - "LegendOak"
Cohesion: 0.25
Nodes (9): Acuteness, LOOSE, TIGHT, TIGHTER, Bend, LEFT, RIGHT, STRAIGHT (+1 more)

### Community 78 - "hexxit.worldgen152.core.populate.OldWorld"
Cohesion: 0.05
Nodes (9): BloodTreeLargeGen, BushTreeGen, EucalyptusTreeGenShort, GlowshroomGenBlueGreen, GlowshroomGenPurple, NaturaGen, RareTreeGen, RedwoodTreeGen (+1 more)

### Community 79 - "NaturaItems.java"
Cohesion: 0.08
Nodes (5): HexxitGear, ImpMeatItem, KamaItem, NaturaItems, ToolSet

### Community 80 - "InvTweaks"
Cohesion: 0.09
Nodes (4): InvTweaksGuiSettingsButton, InvTweaks, Scheduled, InvTweaksClient

### Community 81 - "HexxitGearClient.java"
Cohesion: 0.13
Nodes (3): HelmetModels, HexxitGearClient, IClientItemExtensions

### Community 82 - "net.minecraft.client.renderer.entity.state.LivingEntityRenderState"
Cohesion: 0.10
Nodes (4): ScarecrowRenderer, HeatscarSpiderRenderer, ImpRenderer, NitroCreeperRenderer

### Community 83 - ".get"
Cohesion: 0.10
Nodes (6): InvTweaksGuiModNotWorking, InvTweaksGuiSettings, InvTweaksGuiSettingsAbstract, TextButton, InvTweaksGuiSettingsAdvanced, InvTweaksGuiShortcutsHelp

### Community 84 - "Client-Darstellung in 26.3 (NeoForge 26.3.0.102)"
Cohesion: 0.22
Nodes (8): Client-Darstellung in 26.3 (NeoForge 26.3.0.102), Etwas am Spielermodell zeichnen (Rücken, Schultern), Halbdurchsichtige Blöcke (Alpha im Renderer), HUD (statt `ITickHandler` mit `TickType.RENDER`), Nur-Client-Mod sauber laden, Rechtsklick vor dem Block (Original: `PlayerInteractEvent` mit `RIGHT_CLICK_BLOCK`), Rüstung: Texturen und eigene Helm-Modelle (`ItemArmor`, `getArmorTexture`, `getArmorModel`), Stolperfallen

### Community 85 - "net.minecraft.world.level.Level"
Cohesion: 0.07
Nodes (5): QuicksandBlock, CloudBlock, HeatSandBlock, LargeGlowshroomBlock, ThornVinesBlock

### Community 86 - "ImpModel.java"
Cohesion: 0.15
Nodes (4): ScarecrowModel, ImpModel, NaturaBoatModel, NaturaBoatRenderer

### Community 87 - "minecraft"
Cohesion: 0.11
Nodes (3): CustomLanClient, GuiCustomLan, CLOptions

### Community 90 - "net.minecraft.world.entity.player.Player"
Cohesion: 0.21
Nodes (4): ArmorSet, SCALE, THIEF, TRIBAL

### Community 93 - "OldStack"
Cohesion: 0.18
Nodes (5): MinecartChest, Category, Entry, OldStack, Chest

### Community 94 - "WorldGen152.java"
Cohesion: 0.12
Nodes (4): BackItemLayer, CustomLAN, RFTL, TCLog

### Community 95 - "InvTweaksItemTree"
Cohesion: 0.08
Nodes (4): InvTweaksItemTree, InvTweaksItemTreeCategory, InvTweaksItemTreeItem, InvTweaksItemTreeLoader

### Community 96 - "net.minecraft.world.level.block.state.BlockState"
Cohesion: 0.10
Nodes (3): BerryBushBlock, SaguaroBlock, SaguaroSproutBlock

### Community 97 - "Type"
Cohesion: 0.20
Nodes (9): Type, DROP, MOVE_ALL_ITEMS, MOVE_DOWN, MOVE_EVERYTHING, MOVE_ONE_ITEM, MOVE_ONE_STACK, MOVE_TO_SPECIFIC_HOTBAR_SLOT (+1 more)

### Community 101 - "Holzsets und Pflanzen in 26.3"
Cohesion: 0.22
Nodes (7): Blöcke (Java), Brennwert und Brennbarkeit, Holzsets und Pflanzen in 26.3, Modelle und Blockstates aus Vanilla erzeugen, Prüfen, Rezeptkonflikte mit Vanilla, Tags (Block + Item)

### Community 104 - "Mobs, Spawn-Eier und Boote auf 26.3"
Cohesion: 0.25
Nodes (7): Boote, Client, Drops, Mobs, Spawn-Eier und Boote auf 26.3, Registrieren, Spawn-Eier, Spawns

### Community 106 - "OldCarver"
Cohesion: 0.10
Nodes (5): OldCarver, OldCaves, OldMath, OldRavines, Minable

### Community 108 - "convert_tree.py"
Cohesion: 0.22
Nodes (8): camel(), convert(), emit_additions(), walk(), main(), Out, resolve_tags(), get()

### Community 109 - "QuarterLogBlock"
Cohesion: 0.10
Nodes (11): Bark, NE, NW, SE, SW, QuarterLogBlock, Bark, NE (+3 more)

### Community 112 - "Wood"
Cohesion: 0.09
Nodes (14): Wood, AMARANTH, BLOODWOOD, DARKWOOD, EUCALYPTUS, FUSEWOOD, GHOSTWOOD, HOPSEED (+6 more)

### Community 114 - "HexxitGear.java"
Cohesion: 0.06
Nodes (7): bspkrsCoreMod, ChestTransporter, EbxlIds, InvTweaksMod, Soul, SoulShards, WildCaves

### Community 118 - "Bäume und Äxte bei TreeCapitator anmelden"
Cohesion: 0.40
Nodes (4): Bäume: Daten-Registry `treecapitator:tree`, Bäume und Äxte bei TreeCapitator anmelden, Prüfen, Äxte und Scheren: Item-Tags

### Community 120 - "EnchantTableMenu"
Cohesion: 0.18
Nodes (3): EnchantTableMenu, Slot, Settings

### Community 121 - "BaseTreeWorldgen"
Cohesion: 0.08
Nodes (5): BaseTreeWorldgen, DarkwoodGen, FusewoodGen, SaguaroGen, WhiteTreeGen

### Community 128 - "EnchantHelper.java"
Cohesion: 0.09
Nodes (4): Truhen und Spawner, HGWorldGen, MoreBows, OldGameIds

### Community 133 - ".nextInt"
Cohesion: 0.24
Nodes (3): FuzzyZoom, Island, Zoom

## Knowledge Gaps
- **159 isolated node(s):** `NE`, `NW`, `SE`, `SW`, `NE` (+154 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 827 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **53 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `OldWorld` connect `StructureBoundingBox` to `.setBlock`, `.addComponentParts`, `OldWorld`, `OldCarver`, `LegendOak`, `hexxit.worldgen152.core.populate.OldWorld`, `EbxlTreeGens`, `OldBlockInfo`, `OldBiome`, `OldGen`, `OldDecoration`, `EbxlWorldGen`, `BigTree`, `EbxlWorldGen.java`, `BaseTreeWorldgen`, `OldStructures`?**
  _High betweenness centrality (0.112) - this node is a cross-community bridge._
- **What connects `NE`, `NW`, `SE` to the rest of the system?**
  _159 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `StructureBoundingBox` be split into smaller, more focused modules?**
  _Cohesion score 0.12844611528822056 - nodes in this community are weakly interconnected._
- **Why does `NaturaTree` connect `net.minecraft.server.level.ServerLevel` to `NaturaBlocks.java`, `Holzsets und Pflanzen in 26.3`?**
  _High betweenness centrality (0.026) - this node is a cross-community bridge._
- **Should `Layer` be split into smaller, more focused modules?**
  _Cohesion score 0.10204081632653061 - nodes in this community are weakly interconnected._
- **Why does `OldBiome` connect `OldBiome` to `Layer`, `OldBiomeSource.java`, `OldTerrain`, `OldCarver`, `OldBlockInfo`, `OldGen`, `OldDecoration`, `EbxlWorldGen.java`?**
  _High betweenness centrality (0.025) - this node is a cross-community bridge._
- **Should `ImportOriginalAssets.groovy` be split into smaller, more focused modules?**
  _Cohesion score 0.09333333333333334 - nodes in this community are weakly interconnected._