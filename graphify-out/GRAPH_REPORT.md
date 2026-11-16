# Graph Report - HeroMC  (2026-11-16)

## Corpus Check
- 2948 files · ~312,973 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 25 file(s) not represented in the graph (top: .properties 21, (none) 1, .toml 1)

## Summary
- 4204 nodes · 13357 edges · 158 communities (82 shown, 76 thin omitted)
- Extraction: 92% EXTRACTED · 8% INFERRED · 0% AMBIGUOUS · INFERRED: 1048 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `5ec74f0b`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- StructureBoundingBox
- Layer
- reference.py
- OldGen
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
- net.minecraft.world.entity.EntityType
- EbxlWorldGen
- StructureComponent
- random
- OldTerrainGen
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
- BeanstalkBlock.java
- NaturaBlocks.java
- net.minecraft.server.level.ServerLevel
- BackToolsClient
- EbxlClient.java
- original_server.sh
- EbxlPlantGens
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
- Material
- ChestTransporterItem.java
- net.minecraft.world.entity.player.Player
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
- OldWorld
- OldCarver
- Decorate
- convert_tree.py
- QuarterLogBlock
- .copyVanilla
- OldChunkWriter.java
- NaturaGen
- SakuraTreeGen
- HexxitGear.java
- GuiCustomLanPermissions
- ScarecrowEntity
- .setBlock
- Bäume und Äxte bei TreeCapitator anmelden
- net.minecraft.world.level.block.Block
- .mayPlace
- BaseTreeWorldgen
- BackItemLayer.java
- EnchantTableScreen.java
- hexxit.worldgen152.core.OldChunk
- MapGenNetherBridge
- .generate
- SoulEvents.java
- OldGameIds
- InvTweaksItemTreeLoader.java
- ObsidiPlates
- NitroCreeper
- InvTweaksItemTreeItem
- .nextInt
- OldWorld.java
- NaturaEntities.java
- InvTweaksConfigInventoryRuleset
- HeroMC – Hexxit 1:1 für Minecraft 26.3
- Natura
- TableEvents
- check.py
- BloodTreeLargeGen
- EucalyptusTreeGenShort
- SaguaroGen
- NitroCreeperRenderer.java
- .generate
- .generate
- OffLawnGen
- net.minecraft.world.entity.LivingEntity
- .func_74971_a
- HGWorldGen
- .grow
- OldPopulator.java
- Dispenser

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
- `Aufbau` --references--> `World`  [INFERRED]
  .claude/skills/worldgen152/SKILL.md → mods/worldgen152/src/main/java/hexxit/worldgen152/OldNetherChunkGenerator.java
- `Teil 1 – Vanilla-1.5.2-Dekoration` --references--> `World`  [INFERRED]
  docs/PLAN.md → mods/worldgen152/src/main/java/hexxit/worldgen152/OldNetherChunkGenerator.java
- `Aufbau` --references--> `WorldGenerator`  [INFERRED]
  .claude/skills/worldgen152/SKILL.md → mods/worldgen152/src/main/java/hexxit/worldgen152/core/populate/OldTerrainGen.java
- `Teil 7b – Natura-Nether` --references--> `StructureNetherBridgePieces`  [INFERRED]
  docs/PLAN.md → mods/worldgen152/src/main/java/hexxit/worldgen152/core/structure/StructureNetherBridgePieces.java

## Import Cycles
- None detected.

## Communities (158 total, 76 thin omitted)

### Community 0 - "StructureBoundingBox"
Cohesion: 0.12
Nodes (3): Witch, Spawner, StructureBoundingBox

### Community 1 - "Layer"
Cohesion: 0.10
Nodes (15): Layer, AddIsland, AddMushroomIsland, AddSnow, Hills, Island, Layers, River (+7 more)

### Community 2 - "reference.py"
Cohesion: 0.18
Nodes (10): build_mappings(), digest(), download(), inner_archives(), java(), main(), minecraft_reference(), process() (+2 more)

### Community 3 - "OldGen"
Cohesion: 0.05
Nodes (20): Aufbau, CatTail, BaseCropWorldgen, OldDecorator, OldDungeonGen, OldGen, Lake, Spring (+12 more)

### Community 4 - "net.minecraft.world.item.ItemStack"
Cohesion: 0.16
Nodes (5): EnchantHelper, OldCost, Restriction, EnchantTableMenu, Settings

### Community 8 - "Arbeitsplan: Hexxit 1.0.11 → NeoForge 26.3"
Cohesion: 0.10
Nodes (20): Arbeitsplan: Hexxit 1.0.11 → NeoForge 26.3, Regeln für jeden Teil, Teil 10 und 11 – Project Zulu, Teil 14 – Battle Towers, Ruins, Teil 15 – Dimensional Doors, Teil 16 – BiblioCraft, Teil 17 – BetterStorage, Backpacks, qCraft, Teil 18 – ChocoCraft, Infernal Mobs (+12 more)

### Community 9 - "OldChunkGenerator.java"
Cohesion: 0.08
Nodes (3): OldChunkGenerator, OldNetherChunkGenerator, World

### Community 11 - "HeroMC – Hinweise für Claude"
Cohesion: 0.29
Nodes (6): Arbeitsweise beim Portieren, Branches, graphify, HeroMC – Hinweise für Claude, Skills anlegen, Tokens sparen

### Community 17 - "OldBiome"
Cohesion: 0.11
Nodes (6): EbxlBiomes, Decoration, GrassChooser, OldBiome, TreeChooser, OldBiomes

### Community 18 - "OldBiomes.java"
Cohesion: 0.21
Nodes (4): OldBlockInfo, OldEntity, OldLoot, OldTile

### Community 19 - "OldDecoratedChunk"
Cohesion: 0.09
Nodes (4): OldChunk, OldDecoratedChunk, OldDecoration, TerrainSource

### Community 21 - "EbxlWorldGen"
Cohesion: 0.14
Nodes (5): Choices, EbxlWorldGen, Jungle, Weighted, DesertWell

### Community 22 - "StructureComponent"
Cohesion: 0.06
Nodes (19): Teil 12 und 13 – Better Dungeons (Chocolate Quest), ComponentVillage, ComponentVillageChurch, ComponentVillageField2, ComponentVillageField, ComponentVillageHall, ComponentVillageHouse1, ComponentVillageHouse2 (+11 more)

### Community 24 - "OldTerrainGen"
Cohesion: 0.05
Nodes (26): Teil 1 – Vanilla-1.5.2-Dekoration, Fire, Glowshroom, Glowstone, HellLava, OldNetherGens, Entry, OldTerrainGen (+18 more)

### Community 25 - "OldStructures"
Cohesion: 0.10
Nodes (8): Found, OldStructures, StructureMineshaftStart, StructureNetherBridgeStart, StructureScatteredFeatureStart, StructureStart, StructureStrongholdStart, StructureVillageStart

### Community 26 - ".findIntersecting"
Cohesion: 0.05
Nodes (21): ComponentStronghold, ComponentStrongholdChestCorridor, ComponentStrongholdCorridor, ComponentStrongholdCrossing, ComponentStrongholdLeftTurn, ComponentStrongholdLibrary, ComponentStrongholdPortalRoom, ComponentStrongholdPrison (+13 more)

### Community 27 - "net.minecraft.client.gui.screens.inventory.AbstractContainerScreen"
Cohesion: 0.06
Nodes (3): InvTweaksGuiIconButton, InvTweaksGuiSettingsButton, InvTweaksGuiSortingButton

### Community 28 - "net.minecraft.core.BlockPos"
Cohesion: 0.09
Nodes (5): HexbiscusBlock, GlowshroomBlock, NaturaPlantBlock, IcicleBlock, StalactiteBlock

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
Cohesion: 0.08
Nodes (8): Kind, ENDER, FLAME, FROST, MULTI, PLAIN, MoreBowItem, MoreBows

### Community 36 - "TreeDefinition"
Cohesion: 0.05
Nodes (7): Teil 6 – bspkrsCore, TreeCapitator, Inventory Tweaks, Custom LAN Ports, BlockID, TreeCapitator, TreeCapitatorMod, Data, TreeDefinition, TreeRegistry

### Community 41 - "EnchantingPlus.java"
Cohesion: 0.09
Nodes (6): EnchantingPlus, ClientOptionPayload, EnchantPayload, EplusNetwork, OpenModTablePayload, UseModPayload

### Community 42 - "net.neoforged.bus.api.IEventBus"
Cohesion: 0.10
Nodes (4): ArmorBarMod, BackTools, BuffBarMod, BuffBarHud

### Community 43 - "gen.py"
Cohesion: 0.09
Nodes (23): age(), ages(), bitag(), blockstate(), btag(), cube_all(), flam(), generated_item() (+15 more)

### Community 44 - "biomes.py"
Cohesion: 0.15
Nodes (9): ColorMaps, f32(), fixed(), hexcolor(), main(), multiply(), sky_color(), tinted() (+1 more)

### Community 46 - "StructurePieceBlockSelector"
Cohesion: 0.09
Nodes (7): ComponentScatteredFeature, ComponentScatteredFeatureDesertPyramid, ComponentScatteredFeatureJunglePyramid, ComponentScatteredFeatureSwampHut, StructurePieceBlockSelector, StructureScatteredFeatureStones, StructureStrongholdStones

### Community 47 - "EplusConfig"
Cohesion: 0.08
Nodes (7): Konfiguration und Spielerdaten, Menü, Screen und Pakete in 26.3 (NeoForge 26.3.0.102), Pakete → Custom Payloads, Truhen-GUIs und Inventory Tweaks, Vanilla-Block ersetzen (Mod hat Block-ID überschrieben), EplusConfig, TCSettings

### Community 49 - "EbxlTreeGens"
Cohesion: 0.12
Nodes (8): Acacia, Autumn, BigAutumn, CustomSwamp, EbxlTreeGens, Fir, FirHuge, Redwood

### Community 50 - "OldGen"
Cohesion: 0.09
Nodes (10): BigMushroom, BigTree, Birch, Huge, OldTreeGens, Pine, Shrub, Spruce (+2 more)

### Community 51 - "InvTweaksConfig"
Cohesion: 0.09
Nodes (4): InvTweaksConfig, OrderedProperties, InvTweaksConfigManager, InvTweaksHandlerAutoRefill

### Community 53 - "compare_original.py"
Cohesion: 0.19
Nodes (7): cells(), main(), read_nbt(), payload(), string(), take(), region_chunks()

### Community 54 - "net.minecraft.world.level.block.Block"
Cohesion: 0.06
Nodes (5): LogTurnerItem, ScarecrowItem, BoneBagItem, SunflowerSeedItem, ShadowPickaxeItem

### Community 55 - "verify_blocks.py"
Cohesion: 0.14
Nodes (15): main(), packet(), read(), block(), ebxl(), flag(), main(), saguaro() (+7 more)

### Community 58 - "BeanstalkBlock.java"
Cohesion: 0.10
Nodes (3): EbxlLeavesBlock, SaguaroFruitBlock, BeanstalkBlock

### Community 60 - "NaturaBlocks.java"
Cohesion: 0.04
Nodes (20): CattailBlock, EbxlPlantBlock, LeafPileBlock, EbxlBlocks, NaturaLeavesBlock, NaturaBlocks, Wood, AMARANTH (+12 more)

### Community 61 - "net.minecraft.server.level.ServerLevel"
Cohesion: 0.08
Nodes (5): EbxlSaplingBlock, NaturaIds, LawnBlock, SunflowerBlock, OffLawn

### Community 62 - "BackToolsClient"
Cohesion: 0.10
Nodes (4): Back, BackItem, BackToolsClient, EplusClient

### Community 63 - "EbxlClient.java"
Cohesion: 0.08
Nodes (5): EbxlClient, FrostArrowRenderer, MoreBowsClient, NaturaEntityRenderers, OffLawnClient

### Community 65 - "EbxlPlantGens"
Cohesion: 0.11
Nodes (10): EbxlPlantGens, Flowers, LeafPile, Marsh, Melon, Oasis, Quicksand, Root (+2 more)

### Community 67 - "EnchantTableScreen"
Cohesion: 0.14
Nodes (3): GuiContainer → `AbstractContainerScreen`, EnchantTableScreen, GuiItem

### Community 68 - "StructureComponent"
Cohesion: 0.04
Nodes (24): Teil 7b – Natura-Nether, ComponentMineshaftCorridor, ComponentMineshaftCross, ComponentMineshaftRoom, ComponentMineshaftStairs, ComponentNetherBridgeCorridor2, ComponentNetherBridgeCorridor3, ComponentNetherBridgeCorridor4 (+16 more)

### Community 69 - "net.minecraft.server.level.ServerLevel"
Cohesion: 0.06
Nodes (29): Growth, FLORA, NETHER, RARE, REDWOOD, NaturaSaplingBlock, Soil, CEILING (+21 more)

### Community 70 - "net.minecraft.resources.Identifier"
Cohesion: 0.12
Nodes (6): ChestTransporterJeiPlugin, EbxlJeiPlugin, HexxitGearJeiPlugin, MoreBowsJeiPlugin, ObsidiPlatesJeiPlugin, SoulShardsJeiPlugin

### Community 72 - "InvTweaksConfigSortingRule"
Cohesion: 0.13
Nodes (6): InvTweaksConfigSortingRule, Type, COLUMN, RECTANGLE, ROW, SLOT

### Community 74 - "JEI-Plugin für einen portierten Mod"
Cohesion: 0.40
Nodes (4): Einbindung (schon erledigt, in `build.gradle`), JEI-Plugin für einen portierten Mod, Prüfen, Vorlage

### Community 75 - "InvTweaksHandlerShortcuts"
Cohesion: 0.10
Nodes (15): Action, DROP, MOVE, InvTweaksHandlerShortcuts, Scope, ALL_ITEMS, EVERYTHING, ONE_ITEM (+7 more)

### Community 77 - "LegendOak"
Cohesion: 0.24
Nodes (9): Acuteness, LOOSE, TIGHT, TIGHTER, Bend, LEFT, RIGHT, STRAIGHT (+1 more)

### Community 79 - "NaturaItems.java"
Cohesion: 0.10
Nodes (4): HexxitGear, KamaItem, NaturaItems, ToolSet

### Community 80 - "InvTweaks"
Cohesion: 0.10
Nodes (3): InvTweaks, Scheduled, InvTweaksClient

### Community 81 - "HexxitGearClient.java"
Cohesion: 0.13
Nodes (3): HelmetModels, HexxitGearClient, IClientItemExtensions

### Community 82 - "net.minecraft.client.renderer.entity.state.LivingEntityRenderState"
Cohesion: 0.17
Nodes (3): ScarecrowRenderer, HeatscarSpiderRenderer, ImpRenderer

### Community 83 - ".get"
Cohesion: 0.06
Nodes (9): InvTweaksGuiModNotWorking, InvTweaksGuiSettings, InvTweaksGuiSettingsAbstract, TextButton, InvTweaksGuiSettingsAdvanced, InvTweaksGuiShortcutsHelp, InvTweaksConst, InvTweaksKeys (+1 more)

### Community 84 - "Client-Darstellung in 26.3 (NeoForge 26.3.0.102)"
Cohesion: 0.22
Nodes (8): Client-Darstellung in 26.3 (NeoForge 26.3.0.102), Etwas am Spielermodell zeichnen (Rücken, Schultern), Halbdurchsichtige Blöcke (Alpha im Renderer), HUD (statt `ITickHandler` mit `TickType.RENDER`), Nur-Client-Mod sauber laden, Rechtsklick vor dem Block (Original: `PlayerInteractEvent` mit `RIGHT_CLICK_BLOCK`), Rüstung: Texturen und eigene Helm-Modelle (`ItemArmor`, `getArmorTexture`, `getArmorModel`), Stolperfallen

### Community 85 - "net.minecraft.world.level.Level"
Cohesion: 0.09
Nodes (5): QuicksandBlock, CloudBlock, HeatSandBlock, LargeGlowshroomBlock, ThornVinesBlock

### Community 86 - "ImpModel.java"
Cohesion: 0.13
Nodes (4): ScarecrowModel, ImpModel, NaturaBoatModel, NaturaBoatRenderer

### Community 87 - "minecraft"
Cohesion: 0.11
Nodes (3): CustomLanClient, GuiCustomLan, CLOptions

### Community 88 - "Material"
Cohesion: 0.06
Nodes (28): NaturaWorldGen, Material, AIR, CACTUS, CIRCUITS, CLAY, CLOTH, CRAFTED_SNOW (+20 more)

### Community 90 - "net.minecraft.world.entity.player.Player"
Cohesion: 0.14
Nodes (6): ArmorSet, SCALE, THIEF, TRIBAL, SetBonuses, State

### Community 93 - "OldStack"
Cohesion: 0.19
Nodes (5): MinecartChest, Category, Entry, OldStack, Chest

### Community 94 - "WorldGen152.java"
Cohesion: 0.14
Nodes (3): CustomLAN, RFTL, TCLog

### Community 96 - "net.minecraft.world.level.block.state.BlockState"
Cohesion: 0.11
Nodes (4): BerryBushBlock, SaguaroBlock, SaguaroSproutBlock, OldLiveGen

### Community 97 - "Type"
Cohesion: 0.20
Nodes (9): Type, DROP, MOVE_ALL_ITEMS, MOVE_DOWN, MOVE_EVERYTHING, MOVE_ONE_ITEM, MOVE_ONE_STACK, MOVE_TO_SPECIFIC_HOTBAR_SLOT (+1 more)

### Community 101 - "Holzsets und Pflanzen in 26.3"
Cohesion: 0.17
Nodes (7): Blöcke (Java), Brennwert und Brennbarkeit, Holzsets und Pflanzen in 26.3, Modelle und Blockstates aus Vanilla erzeugen, Prüfen, Rezeptkonflikte mit Vanilla, Tags (Block + Item)

### Community 104 - "Mobs, Spawn-Eier und Boote auf 26.3"
Cohesion: 0.25
Nodes (7): Boote, Client, Drops, Mobs, Spawn-Eier und Boote auf 26.3, Registrieren, Spawn-Eier, Spawns

### Community 105 - "OldWorld"
Cohesion: 0.10
Nodes (3): Listener, Column, OldWorld

### Community 106 - "OldCarver"
Cohesion: 0.15
Nodes (3): OldCarver, OldCaves, OldRavines

### Community 107 - "Decorate"
Cohesion: 0.08
Nodes (17): OldNetherPopulator, Decorate, BIG_SHROOM, CACTUS, CLAY, DEAD_BUSH, FLOWERS, GRASS (+9 more)

### Community 108 - "convert_tree.py"
Cohesion: 0.22
Nodes (8): camel(), convert(), emit_additions(), walk(), main(), Out, resolve_tags(), get()

### Community 109 - "QuarterLogBlock"
Cohesion: 0.08
Nodes (13): Bark, NE, NW, SE, SW, QuarterLogBlock, Bark, NE (+5 more)

### Community 112 - "NaturaGen"
Cohesion: 0.09
Nodes (5): BushTreeGen, GlowshroomGenBlueGreen, GlowshroomGenPurple, NaturaGen, RareTreeGen

### Community 114 - "HexxitGear.java"
Cohesion: 0.06
Nodes (8): bspkrsCoreMod, ChestTransporter, EbxlIds, InvTweaksMod, ObsidiPlates, Soul, SoulShards, WildCaves

### Community 117 - ".setBlock"
Cohesion: 0.26
Nodes (3): Teil 5 – Hexxit Gear, More Bows, Enchanting Plus, offLawn, Wild Caves 3, WildCavesGen, Skull

### Community 118 - "Bäume und Äxte bei TreeCapitator anmelden"
Cohesion: 0.40
Nodes (4): Bäume: Daten-Registry `treecapitator:tree`, Bäume und Äxte bei TreeCapitator anmelden, Prüfen, Äxte und Scheren: Item-Tags

### Community 119 - "net.minecraft.world.level.block.Block"
Cohesion: 0.14
Nodes (4): Menu, NaturaWorkbenchBlock, SeedBagItem, FloraBlock

### Community 121 - "BaseTreeWorldgen"
Cohesion: 0.11
Nodes (4): BaseTreeWorldgen, DarkwoodGen, FusewoodGen, WhiteTreeGen

### Community 125 - "MapGenNetherBridge"
Cohesion: 0.22
Nodes (4): Found, MapGenNetherBridge, Region, SpawnEntry

### Community 128 - "OldGameIds"
Cohesion: 0.10
Nodes (12): Biome und Dekoratoren, Blöcke anmelden, Bäume im Spiel (Setzlinge), Generator anmelden, Nether, Prüfen, Regel 1: Kern-Klassen ohne Spielklassen, Stolperfallen (+4 more)

### Community 133 - ".nextInt"
Cohesion: 0.24
Nodes (3): Biome, FuzzyZoom, Zoom

### Community 139 - "HeroMC – Hexxit 1:1 für Minecraft 26.3"
Cohesion: 0.18
Nodes (9): Arbeitsplan, Aufbau, Bauen und Starten, HeroMC – Hexxit 1:1 für Minecraft 26.3, Referenzquellen des Originals, Status, Vorgaben, Wann ein Mod als portiert gilt (+1 more)

## Knowledge Gaps
- **162 isolated node(s):** `AIR`, `GRASS`, `GROUND`, `WOOD`, `ROCK` (+157 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 846 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **76 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `OldWorld` connect `OldWorld` to `StructureBoundingBox`, `OldGen`, `OldWorld.java`, `OldBiome`, `OldBiomes.java`, `OldDecoratedChunk`, `EbxlWorldGen`, `StructureComponent`, `random`, `OldTerrainGen`, `.func_74971_a`, `OldWorld`, `EbxlTreeGens`, `OldGen`, `EbxlPlantGens`, `StructureComponent`, `.addComponentParts`, `LegendOak`, `.setBlock`, `BaseTreeWorldgen`, `MapGenNetherBridge`?**
  _High betweenness centrality (0.167) - this node is a cross-community bridge._
- **What connects `AIR`, `GRASS`, `GROUND` to the rest of the system?**
  _162 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `StructureBoundingBox` be split into smaller, more focused modules?**
  _Cohesion score 0.117102615694165 - nodes in this community are weakly interconnected._
- **Why does `InvTweaksContainerManager` connect `InvTweaksContainerManager` to `InvTweaksHandlerSorting`, `net.minecraft.client.gui.screens.inventory.AbstractContainerScreen`, `EnchantTableScreen.java`, `InvTweaksHandlerShortcuts`, `InvTweaks`, `InvTweaksConfig`, `map`?**
  _High betweenness centrality (0.033) - this node is a cross-community bridge._
- **Should `Layer` be split into smaller, more focused modules?**
  _Cohesion score 0.10040816326530612 - nodes in this community are weakly interconnected._
- **Why does `OldBiome` connect `OldBiome` to `Layer`, `OldGen`, `.nextInt`, `OldBiomeSource.java`, `OldTerrain`, `OldCarver`, `OldBiomes.java`, `random`?**
  _High betweenness centrality (0.033) - this node is a cross-community bridge._
- **Should `OldGen` be split into smaller, more focused modules?**
  _Cohesion score 0.0518326545723806 - nodes in this community are weakly interconnected._