# Graph Report - HeroMC  (2026-11-16)

## Corpus Check
- 2920 files · ~295,772 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 25 file(s) not represented in the graph (top: .properties 21, (none) 1, .toml 1)

## Summary
- 3985 nodes · 12598 edges · 144 communities (79 shown, 65 thin omitted)
- Extraction: 92% EXTRACTED · 8% INFERRED · 0% AMBIGUOUS · INFERRED: 968 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `f9b94a12`
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
- BackItemLayer.java
- net.minecraft.world.item.ItemStack
- Material
- TreeCapitator
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
- .getBlockId
- OldTreeGens
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
- EnchantPayload
- BackToolsClient
- EbxlClient.java
- original_server.sh
- .setBlock
- net.minecraft.world.item.ItemStack
- EnchantTableScreen
- StructureComponent
- net.minecraft.world.level.Level
- net.minecraft.resources.Identifier
- .addComponentParts
- InvTweaksConfigSortingRule
- EnchantingCategory.java
- JEI-Plugin für einen portierten Mod
- InvTweaksHandlerShortcuts
- net.minecraft.world.entity.player.Player
- OffLawnGen
- hexxit.worldgen152.core.populate.OldWorld
- NaturaItems.java
- InvTweaks
- HexxitGearClient.java
- net.minecraft.client.renderer.entity.state.LivingEntityRenderState
- .get
- Client-Darstellung in 26.3 (NeoForge 26.3.0.102)
- net.minecraft.world.level.Level
- ImpModel.java
- CLOptions
- NaturaWorkbenchBlock.java
- NaturaGen
- OldStack
- WorldGen152.java
- InvTweaksItemTree
- net.minecraft.world.level.block.state.BlockState
- Type
- LanCommands
- ImpEntity.java
- Holzsets und Pflanzen in 26.3
- InvTweaksHandlerSorting
- BlockID
- NaturaTree
- OldCarver
- Expect.java
- convert_tree.py
- Extrabiomes
- .copyVanilla
- OldChunkWriter.java
- Wood
- SakuraTreeGen
- HexxitGear.java
- GuiCustomLanPermissions
- ScarecrowEntity
- OldDecoration
- Bäume und Äxte bei TreeCapitator anmelden
- HeroMC – Hexxit 1:1 für Minecraft 26.3
- EnchantTableMenu
- BaseTreeWorldgen
- OldGen
- EnchantTableScreen.java
- TreeDefinition
- .generate
- .generate
- SoulEvents.java
- net.minecraft.resources.ResourceKey
- Natura.java
- ObsidiPlates
- NitroCreeper
- TreeRegistry
- .nextInt
- BloodwoodBoat.java
- BloodTreeLargeGen
- EucalyptusTreeGenShort
- GuiCustomLan
- NitroCreeperRenderer.java
- .generate
- .generate
- check.py
- EbxlBiomes.java

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
- `Teil 12 und 13 – Better Dungeons (Chocolate Quest)` --references--> `VillageHandler`  [INFERRED]
  docs/PLAN.md → mods/worldgen152/src/main/java/hexxit/worldgen152/core/structure/OldStructures.java
- `Teil 1 – Vanilla-1.5.2-Dekoration` --references--> `OldDecorator`  [INFERRED]
  docs/PLAN.md → mods/worldgen152/src/main/java/hexxit/worldgen152/core/populate/OldDecorator.java
- `Bäume im Spiel (Setzlinge)` --references--> `OldGen`  [INFERRED]
  .claude/skills/worldgen152/SKILL.md → mods/worldgen152/src/main/java/hexxit/worldgen152/core/populate/OldGen.java
- `Aufbau` --references--> `WorldGenerator`  [INFERRED]
  .claude/skills/worldgen152/SKILL.md → mods/worldgen152/src/main/java/hexxit/worldgen152/core/populate/OldTerrainGen.java
- `Teil 5 – Hexxit Gear, More Bows, Enchanting Plus, offLawn, Wild Caves 3` --references--> `IClientItemExtensions`  [INFERRED]
  docs/PLAN.md → mods/hexxitgear/src/main/java/sct/hexxitgear/client/HexxitGearClient.java

## Import Cycles
- None detected.

## Communities (144 total, 65 thin omitted)

### Community 0 - "StructureBoundingBox"
Cohesion: 0.15
Nodes (3): Witch, Spawner, StructureBoundingBox

### Community 1 - "Layer"
Cohesion: 0.10
Nodes (15): Layer, AddIsland, AddMushroomIsland, AddSnow, Hills, Island, Layers, River (+7 more)

### Community 2 - "reference.py"
Cohesion: 0.18
Nodes (10): build_mappings(), digest(), download(), inner_archives(), java(), main(), minecraft_reference(), process() (+2 more)

### Community 3 - "Menü, Screen und Pakete in 26.3 (NeoForge 26.3.0.102)"
Cohesion: 0.18
Nodes (6): Container → `AbstractContainerMenu`, Konfiguration und Spielerdaten, Menü, Screen und Pakete in 26.3 (NeoForge 26.3.0.102), Pakete → Custom Payloads, Truhen-GUIs und Inventory Tweaks, Slot

### Community 4 - "net.minecraft.world.item.ItemStack"
Cohesion: 0.14
Nodes (4): EnchantHelper, OldCost, Restriction, Settings

### Community 7 - "OldTerrain"
Cohesion: 0.17
Nodes (3): BiomeLayout, OctaveNoise, OldTerrain

### Community 8 - "Arbeitsplan: Hexxit 1.0.11 → NeoForge 26.3"
Cohesion: 0.10
Nodes (21): Arbeitsplan: Hexxit 1.0.11 → NeoForge 26.3, Regeln für jeden Teil, Teil 10 und 11 – Project Zulu, Teil 14 – Battle Towers, Ruins, Teil 15 – Dimensional Doors, Teil 16 – BiblioCraft, Teil 17 – BetterStorage, Backpacks, qCraft, Teil 18 – ChocoCraft, Infernal Mobs (+13 more)

### Community 11 - "HeroMC – Hinweise für Claude"
Cohesion: 0.29
Nodes (6): Arbeitsweise beim Portieren, Branches, graphify, HeroMC – Hinweise für Claude, Skills anlegen, Tokens sparen

### Community 17 - "OldBiome"
Cohesion: 0.11
Nodes (5): EbxlBiomes, Decoration, GrassChooser, OldBiome, OldBiomes

### Community 18 - "OldBlockInfo"
Cohesion: 0.17
Nodes (4): OldBlockInfo, OldEntity, OldLoot, OldTile

### Community 20 - "net.minecraft.world.entity.EntityType"
Cohesion: 0.11
Nodes (3): NaturaEntities, HomeDimensions, SoulShardItem

### Community 21 - "EbxlWorldGen"
Cohesion: 0.15
Nodes (5): Choices, EbxlWorldGen, Jungle, Weighted, DesertWell

### Community 22 - "StructureComponent"
Cohesion: 0.07
Nodes (18): Teil 12 und 13 – Better Dungeons (Chocolate Quest), ComponentMineshaftStairs, ComponentVillage, ComponentVillageChurch, ComponentVillageField2, ComponentVillageField, ComponentVillageHall, ComponentVillageHouse1 (+10 more)

### Community 24 - "EbxlWorldGen.java"
Cohesion: 0.05
Nodes (33): Teil 1 – Vanilla-1.5.2-Dekoration, Decorate, BIG_SHROOM, CACTUS, CLAY, DEAD_BUSH, FLOWERS, GRASS (+25 more)

### Community 25 - "OldStructures"
Cohesion: 0.13
Nodes (7): Found, OldStructures, StructureMineshaftStart, StructureScatteredFeatureStart, StructureStart, StructureStrongholdStart, StructureVillageStart

### Community 26 - ".findIntersecting"
Cohesion: 0.06
Nodes (20): ComponentStronghold, ComponentStrongholdChestCorridor, ComponentStrongholdCrossing, ComponentStrongholdLeftTurn, ComponentStrongholdLibrary, ComponentStrongholdPortalRoom, ComponentStrongholdPrison, ComponentStrongholdRightTurn (+12 more)

### Community 27 - "net.minecraft.client.gui.screens.inventory.AbstractContainerScreen"
Cohesion: 0.07
Nodes (6): InvTweaksGuiIconButton, InvTweaksGuiSettingsButton, InvTweaksGuiSortingButton, InvTweaksConst, InvTweaksKeys, InvTweaksLocalization

### Community 28 - "net.minecraft.core.BlockPos"
Cohesion: 0.08
Nodes (5): HexbiscusBlock, NaturaPlantBlock, SaguaroFruitBlock, IcicleBlock, StalactiteBlock

### Community 30 - "ArmorBarHud"
Cohesion: 0.14
Nodes (10): Kind, AMOUNT, CHEST, DURABILITY, FEET, HEAD, LEGS, Unit (+2 more)

### Community 31 - "minecraft-dev"
Cohesion: 0.50
Nodes (3): npx, minecraft-dev, @mcdxai/minecraft-dev-mcp

### Community 32 - "InvTweaksContainerManager"
Cohesion: 0.08
Nodes (17): ContainerSection, ARMOR, BREWING_BOTTLES, BREWING_INGREDIENT, CHEST, CRAFTING_IN, CRAFTING_IN_PERSISTENT, CRAFTING_OUT (+9 more)

### Community 34 - "net.minecraft.world.item.ItemStack"
Cohesion: 0.12
Nodes (7): Kind, ENDER, FLAME, FROST, MULTI, PLAIN, MoreBowItem

### Community 35 - "Material"
Cohesion: 0.06
Nodes (29): HGWorldGen, NaturaWorldGen, Material, AIR, CACTUS, CIRCUITS, CLAY, CLOTH (+21 more)

### Community 38 - ".opaqueCube"
Cohesion: 0.12
Nodes (3): BaseCropWorldgen, BerryBushGen, NetherBerryBushGen

### Community 40 - "OldWorld"
Cohesion: 0.06
Nodes (5): OldPopulator, OldRails, Listener, Column, OldWorld

### Community 41 - "EnchantingPlus.java"
Cohesion: 0.13
Nodes (4): EnchantingPlus, ClientOptionPayload, OpenModTablePayload, UseModPayload

### Community 42 - "net.neoforged.bus.api.IEventBus"
Cohesion: 0.06
Nodes (5): ArmorBarMod, BackTools, BuffBarMod, BuffBarHud, TreeCapitatorMod

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
Cohesion: 0.10
Nodes (4): Vanilla-Block ersetzen (Mod hat Block-ID überschrieben), EplusClient, EplusConfig, TCSettings

### Community 49 - ".getBlockId"
Cohesion: 0.05
Nodes (28): CatTail, EbxlPlantGens, Flowers, LeafPile, Marsh, Melon, Oasis, Quicksand (+20 more)

### Community 50 - "OldTreeGens"
Cohesion: 0.17
Nodes (8): BigMushroom, Birch, Huge, OldTreeGens, Pine, Spruce, Swamp, Trees

### Community 51 - "InvTweaksConfig"
Cohesion: 0.09
Nodes (4): InvTweaksConfig, OrderedProperties, InvTweaksConfigManager, InvTweaksHandlerAutoRefill

### Community 53 - "compare_original.py"
Cohesion: 0.19
Nodes (7): cells(), main(), read_nbt(), payload(), string(), take(), region_chunks()

### Community 54 - "net.minecraft.world.level.block.Block"
Cohesion: 0.05
Nodes (12): QuarterLogBlock, LogTurnerItem, ScarecrowItem, Bark, NE, NW, SE, SW (+4 more)

### Community 55 - "verify_blocks.py"
Cohesion: 0.14
Nodes (15): main(), packet(), read(), block(), ebxl(), flag(), main(), saguaro() (+7 more)

### Community 56 - "NaturaClient.java"
Cohesion: 0.11
Nodes (3): ColormapTint, NaturaClient, OffLawnClient

### Community 57 - "OldBlockStates"
Cohesion: 0.15
Nodes (3): Grower, Natura, OldBlockStates

### Community 60 - "NaturaBlocks.java"
Cohesion: 0.05
Nodes (9): CattailBlock, EbxlLeavesBlock, EbxlPlantBlock, LeafPileBlock, QuicksandBlock, EbxlBlocks, FusewoodLogBlock, LargeGlowshroomBlock (+1 more)

### Community 62 - "BackToolsClient"
Cohesion: 0.27
Nodes (3): Back, BackItem, BackToolsClient

### Community 63 - "EbxlClient.java"
Cohesion: 0.10
Nodes (4): EbxlClient, FrostArrowRenderer, MoreBowsClient, NaturaEntityRenderers

### Community 65 - ".setBlock"
Cohesion: 0.09
Nodes (4): Generator anmelden, Teil 5 – Hexxit Gear, More Bows, Enchanting Plus, offLawn, Wild Caves 3, WildCavesGen, Skull

### Community 67 - "EnchantTableScreen"
Cohesion: 0.15
Nodes (3): GuiContainer → `AbstractContainerScreen`, EnchantTableScreen, GuiItem

### Community 68 - "StructureComponent"
Cohesion: 0.06
Nodes (8): ComponentMineshaftCorridor, ComponentMineshaftCross, ComponentMineshaftRoom, ComponentStrongholdCorridor, ComponentVillagePathGen, ComponentVillageRoadPiece, StructureComponent, StructureMineshaftPieces

### Community 69 - "net.minecraft.world.level.Level"
Cohesion: 0.08
Nodes (6): EbxlSaplingBlock, BerryBushBlock, GlowshroomBlock, SaplingGrower, LawnBlock, SunflowerBlock

### Community 70 - "net.minecraft.resources.Identifier"
Cohesion: 0.09
Nodes (8): ChestTransporterJeiPlugin, EbxlJeiPlugin, EplusJeiPlugin, HexxitGearJeiPlugin, MoreBowsJeiPlugin, NaturaJeiPlugin, ObsidiPlatesJeiPlugin, SoulShardsJeiPlugin

### Community 72 - "InvTweaksConfigSortingRule"
Cohesion: 0.08
Nodes (7): InvTweaksConfigInventoryRuleset, InvTweaksConfigSortingRule, Type, COLUMN, RECTANGLE, ROW, SLOT

### Community 74 - "JEI-Plugin für einen portierten Mod"
Cohesion: 0.40
Nodes (4): Einbindung (schon erledigt, in `build.gradle`), JEI-Plugin für einen portierten Mod, Prüfen, Vorlage

### Community 75 - "InvTweaksHandlerShortcuts"
Cohesion: 0.10
Nodes (16): Action, DROP, MOVE, InvTweaksHandlerShortcuts, Scope, ALL_ITEMS, EVERYTHING, ONE_ITEM (+8 more)

### Community 76 - "net.minecraft.world.entity.player.Player"
Cohesion: 0.12
Nodes (6): ArmorSet, SCALE, THIEF, TRIBAL, SetBonuses, State

### Community 79 - "NaturaItems.java"
Cohesion: 0.06
Nodes (5): HexxitGear, ImpMeatItem, KamaItem, NaturaItems, ToolSet

### Community 80 - "InvTweaks"
Cohesion: 0.10
Nodes (3): InvTweaks, Scheduled, InvTweaksClient

### Community 81 - "HexxitGearClient.java"
Cohesion: 0.14
Nodes (3): HelmetModels, HexxitGearClient, IClientItemExtensions

### Community 82 - "net.minecraft.client.renderer.entity.state.LivingEntityRenderState"
Cohesion: 0.13
Nodes (3): ScarecrowRenderer, HeatscarSpiderRenderer, ImpRenderer

### Community 83 - ".get"
Cohesion: 0.09
Nodes (6): InvTweaksGuiModNotWorking, InvTweaksGuiSettings, InvTweaksGuiSettingsAbstract, TextButton, InvTweaksGuiSettingsAdvanced, InvTweaksGuiShortcutsHelp

### Community 84 - "Client-Darstellung in 26.3 (NeoForge 26.3.0.102)"
Cohesion: 0.22
Nodes (8): Client-Darstellung in 26.3 (NeoForge 26.3.0.102), Etwas am Spielermodell zeichnen (Rücken, Schultern), Halbdurchsichtige Blöcke (Alpha im Renderer), HUD (statt `ITickHandler` mit `TickType.RENDER`), Nur-Client-Mod sauber laden, Rechtsklick vor dem Block (Original: `PlayerInteractEvent` mit `RIGHT_CLICK_BLOCK`), Rüstung: Texturen und eigene Helm-Modelle (`ItemArmor`, `getArmorTexture`, `getArmorModel`), Stolperfallen

### Community 85 - "net.minecraft.world.level.Level"
Cohesion: 0.05
Nodes (7): CloudBlock, HeatSandBlock, Barley, Cotton, NaturaCropBlock, ThornVinesBlock, ShadowPickaxeItem

### Community 86 - "ImpModel.java"
Cohesion: 0.13
Nodes (4): ScarecrowModel, ImpModel, NaturaBoatModel, NaturaBoatRenderer

### Community 92 - "NaturaGen"
Cohesion: 0.09
Nodes (5): DarkwoodGen, GlowshroomGenBlueGreen, GlowshroomGenPurple, NaturaGen, RareTreeGen

### Community 93 - "OldStack"
Cohesion: 0.18
Nodes (6): MinecartChest, Category, Entry, OldStack, Chest, Dispenser

### Community 94 - "WorldGen152.java"
Cohesion: 0.11
Nodes (4): CustomLAN, RFTL, TCLog, WorldGen152

### Community 95 - "InvTweaksItemTree"
Cohesion: 0.07
Nodes (4): InvTweaksItemTree, InvTweaksItemTreeCategory, InvTweaksItemTreeItem, InvTweaksItemTreeLoader

### Community 96 - "net.minecraft.world.level.block.state.BlockState"
Cohesion: 0.09
Nodes (4): NaturaLeavesBlock, SaguaroBlock, SaguaroSproutBlock, FloraBlock

### Community 97 - "Type"
Cohesion: 0.20
Nodes (9): Type, DROP, MOVE_ALL_ITEMS, MOVE_DOWN, MOVE_EVERYTHING, MOVE_ONE_ITEM, MOVE_ONE_STACK, MOVE_TO_SPECIFIC_HOTBAR_SLOT (+1 more)

### Community 101 - "Holzsets und Pflanzen in 26.3"
Cohesion: 0.22
Nodes (7): Blöcke (Java), Brennwert und Brennbarkeit, Holzsets und Pflanzen in 26.3, Modelle und Blockstates aus Vanilla erzeugen, Prüfen, Rezeptkonflikte mit Vanilla, Tags (Block + Item)

### Community 105 - "NaturaTree"
Cohesion: 0.06
Nodes (28): Growth, FLORA, NETHER, RARE, REDWOOD, NaturaSaplingBlock, Soil, CEILING (+20 more)

### Community 106 - "OldCarver"
Cohesion: 0.15
Nodes (3): OldCarver, OldCaves, OldRavines

### Community 108 - "convert_tree.py"
Cohesion: 0.22
Nodes (8): camel(), convert(), emit_additions(), walk(), main(), Out, resolve_tags(), get()

### Community 109 - "Extrabiomes"
Cohesion: 0.21
Nodes (6): Bark, NE, NW, SE, SW, Extrabiomes

### Community 112 - "Wood"
Cohesion: 0.10
Nodes (14): Wood, AMARANTH, BLOODWOOD, DARKWOOD, EUCALYPTUS, FUSEWOOD, GHOSTWOOD, HOPSEED (+6 more)

### Community 114 - "HexxitGear.java"
Cohesion: 0.06
Nodes (9): bspkrsCoreMod, ChestTransporter, EbxlIds, InvTweaksMod, MoreBows, OffLawn, Soul, SoulShards (+1 more)

### Community 118 - "Bäume und Äxte bei TreeCapitator anmelden"
Cohesion: 0.40
Nodes (4): Bäume: Daten-Registry `treecapitator:tree`, Bäume und Äxte bei TreeCapitator anmelden, Prüfen, Äxte und Scheren: Item-Tags

### Community 119 - "HeroMC – Hexxit 1:1 für Minecraft 26.3"
Cohesion: 0.18
Nodes (9): Arbeitsplan, Aufbau, Bauen und Starten, HeroMC – Hexxit 1:1 für Minecraft 26.3, Referenzquellen des Originals, Status, Vorgaben, Wann ein Mod als portiert gilt (+1 more)

### Community 121 - "BaseTreeWorldgen"
Cohesion: 0.14
Nodes (3): BaseTreeWorldgen, SaguaroGen, WhiteTreeGen

### Community 122 - "OldGen"
Cohesion: 0.05
Nodes (30): Aufbau, Biome und Dekoratoren, Blöcke anmelden, Bäume im Spiel (Setzlinge), Prüfen, Regel 1: Kern-Klassen ohne Spielklassen, Stolperfallen, Strukturen (+22 more)

### Community 129 - "Natura.java"
Cohesion: 0.08
Nodes (4): BushTreeGen, FusewoodGen, NaturaIds, OldMath

### Community 132 - "TreeRegistry"
Cohesion: 0.26
Nodes (3): Teil 6 – bspkrsCore, TreeCapitator, Inventory Tweaks, Custom LAN Ports, Data, TreeRegistry

### Community 133 - ".nextInt"
Cohesion: 0.24
Nodes (3): Biome, FuzzyZoom, Zoom

## Knowledge Gaps
- **159 isolated node(s):** `FLORA`, `NETHER`, `RARE`, `REDWOOD`, `CEILING` (+154 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 821 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **65 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `OldWorld` connect `OldWorld` to `StructureBoundingBox`, `.setBlock`, `Natura.java`, `StructureComponent`, `.addComponentParts`, `hexxit.worldgen152.core.populate.OldWorld`, `.getBlockId`, `OldBlockInfo`, `OldBiome`, `OldTreeGens`, `OldDecoration`, `EbxlWorldGen`, `BigTree`, `EbxlWorldGen.java`, `BaseTreeWorldgen`, `OldGen`?**
  _High betweenness centrality (0.104) - this node is a cross-community bridge._
- **What connects `FLORA`, `NETHER`, `RARE` to the rest of the system?**
  _159 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Layer` be split into smaller, more focused modules?**
  _Cohesion score 0.10040816326530612 - nodes in this community are weakly interconnected._
- **Why does `OldStructures` connect `OldStructures` to `StructureComponent`, `.addComponentParts`, `OldTerrain`, `OldChunkGenerator.java`, `OldBlockInfo`, `OldDecoration`, `StructureComponent`?**
  _High betweenness centrality (0.039) - this node is a cross-community bridge._
- **Should `net.minecraft.world.item.ItemStack` be split into smaller, more focused modules?**
  _Cohesion score 0.14204545454545456 - nodes in this community are weakly interconnected._
- **Why does `OldBiome` connect `OldBiome` to `Layer`, `.nextInt`, `OldBiomeSource.java`, `OldTerrain`, `OldCarver`, `OldBlockInfo`, `EbxlWorldGen.java`, `OldGen`?**
  _High betweenness centrality (0.027) - this node is a cross-community bridge._
- **Should `ImportOriginalAssets.groovy` be split into smaller, more focused modules?**
  _Cohesion score 0.09333333333333334 - nodes in this community are weakly interconnected._