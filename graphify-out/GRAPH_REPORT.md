# Graph Report - HeroMC  (2026-11-16)

## Corpus Check
- 4343 files · ~422,027 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 31 file(s) not represented in the graph (top: .properties 27, (none) 1, .toml 1)

## Summary
- 5757 nodes · 18887 edges · 182 communities (112 shown, 70 thin omitted)
- Extraction: 94% EXTRACTED · 6% INFERRED · 0% AMBIGUOUS · INFERRED: 1148 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `f77cc585`
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
- OldBiomes.java
- OldDecoratedChunk
- net.minecraft.world.item.crafting.CraftingInput
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
- Override
- net.minecraft.world.item.ItemStack
- list
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
- .floor
- InvTweaksConfig
- net.minecraft.world.level.block.state.BlockState
- compare_original.py
- net.minecraft.world.level.block.Block
- verify_blocks.py
- NaturaClient.java
- artifice/tools/gen.py
- StalactiteBlock
- SoulEntity.java
- net.minecraft.world.level.block.Block
- net.minecraft.server.level.ServerLevel
- BackToolsClient.java
- OldBlockStates.java
- original_server.sh
- net.minecraft.world.level.block.Block
- net.minecraft.world.item.ItemStack
- EnchantTableScreen
- ComponentNetherBridgePiece
- NaturaTree
- net.minecraft.resources.Identifier
- EplusNetwork.java
- InvTweaksConfigSortingRule
- AltarCategory.java
- JEI-Plugin für einen portierten Mod
- SoulCageBlockEntity.java
- AerialAnimal
- net.minecraft.world.item.Item
- .getBlockId
- net.neoforged.neoforge.registries.DeferredItem
- InvTweaks
- .getHumanoidArmorModel
- net.minecraft.client.renderer.entity.state.LivingEntityRenderState
- net.minecraft.client.gui.GuiGraphicsExtractor
- Client-Darstellung in 26.3 (NeoForge 26.3.0.102)
- net.minecraft.world.level.Level
- net.minecraft.client.model.geom.builders.LayerDefinition
- minecraft
- Material
- net.minecraft.world.item.ItemStack
- net.neoforged.bus.api.SubscribeEvent
- net.minecraft.resources.Identifier
- ScarecrowEntity
- .generate
- Decorate
- net.minecraft.core.BlockPos
- Essence
- Target
- LanCommands
- ImpEntity.java
- .column
- InvTweaksHandlerSorting
- BackItemLayer.java
- Mobs, Spawn-Eier und Boote auf 26.3
- .setBlock
- HSEvents.java
- AltarRecipe
- json
- Extrabiomes
- OldBlockStates
- SakuraTreeGen
- Type
- GuiCustomLanPermissions
- net.minecraft.world.entity.player.Player
- HSSkullRenderer.java
- Bäume und Äxte bei TreeCapitator anmelden
- ArmadilloRenderer
- EnchantTableMenu.java
- NaturaGen
- Natura
- Populate
- hexxit.worldgen152.core.OldChunk
- net.minecraft.world.entity.LivingEntity
- .generate
- EntityState
- OldGameIds
- GuardSound
- ArtificeEvents.java
- NitroCreeper
- InvTweaksItemTree
- net.minecraft.world.entity.EntityType
- .populate
- NaturaEntities.java
- Ore
- KeeperItem
- net.minecraft.world.level.block.BonemealSource
- HeroMC – Hexxit 1:1 für Minecraft 26.3
- CoFHCommand.java
- TableEvents
- harkenscythe/tools/gen.py
- CoFHWorldGen
- HSBlocks.java
- ArmadilloEntity
- NitroCreeperRenderer.java
- Bark
- OldChunkGenerator
- map
- SkullType
- StructureComponent
- VultureEntity
- BloodSplashBlock
- Starting the test client without a screen
- BiomassPlantBlock
- BirdRenderState
- MapGenNetherBridge
- AerialGoals.java
- AltarBlockEntity
- BeanstalkBlock
- OldWorld.java
- Gen
- CrucibleBlock
- HSSkullBlockEntity
- SoulRenderer
- Menü, Screen und Pakete in 26.3 (NeoForge 26.3.0.102)
- FrostArrowRenderer
- OldGameIds
- .column
- AltarScreen
- Listener
- HelmetModels
- .calculate
- HSWorldGen.java
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
9. `AerialAnimal` - 55 edges
10. `RedwoodTreeGen` - 55 edges

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

## Communities (182 total, 70 thin omitted)

### Community 0 - "OldWorld"
Cohesion: 0.10
Nodes (5): Villager, Witch, Spawner, OldWorld, StructureBoundingBox

### Community 1 - "Layer"
Cohesion: 0.07
Nodes (20): HSClient, IClientItemExtensions, Layer, AddIsland, AddMushroomIsland, AddSnow, Biome, FuzzyZoom (+12 more)

### Community 2 - "reference.py"
Cohesion: 0.13
Nodes (10): build_mappings(), digest(), download(), inner_archives(), java(), main(), minecraft_reference(), process() (+2 more)

### Community 3 - "OldGen"
Cohesion: 0.06
Nodes (23): OldDecorator, OldGen, Spring, Clay, Sand, Cactus, DeadBush, Flowers (+15 more)

### Community 4 - "net.minecraft.core.Holder"
Cohesion: 0.15
Nodes (3): EnchantHelper, OldCost, Restriction

### Community 7 - "OldTerrain"
Cohesion: 0.21
Nodes (3): BiomeLayout, OctaveNoise, OldTerrain

### Community 8 - "Arbeitsplan: Hexxit 1.0.11 → NeoForge 26.3"
Cohesion: 0.10
Nodes (20): Arbeitsplan: Hexxit 1.0.11 → NeoForge 26.3, Regeln für jeden Teil, Teil 10 und 11 – Project Zulu, Teil 14 – Battle Towers, Ruins, Teil 15 – Dimensional Doors, Teil 16 – BiblioCraft, Teil 17 – BetterStorage, Backpacks, qCraft, Teil 18 – ChocoCraft, Infernal Mobs (+12 more)

### Community 11 - "HeroMC – Hinweise für Claude"
Cohesion: 0.29
Nodes (6): Arbeitsweise beim Portieren, Branches, graphify, HeroMC – Hinweise für Claude, Skills anlegen, Tokens sparen

### Community 17 - "OldBiome"
Cohesion: 0.07
Nodes (9): EbxlBiomes, Decoration, GrassChooser, OldBiome, TreeChooser, OldBiomes, OldCarver, OldCaves (+1 more)

### Community 18 - "OldBiomes.java"
Cohesion: 0.22
Nodes (4): OldBlockInfo, OldEntity, OldLoot, OldTile

### Community 19 - "OldDecoratedChunk"
Cohesion: 0.09
Nodes (4): OldChunk, OldDecoratedChunk, OldDecoration, OldChunkWriter

### Community 20 - "net.minecraft.world.item.crafting.CraftingInput"
Cohesion: 0.12
Nodes (4): BoxRecipe, UpgradeRecipe, GildShieldRecipe, SpectralDyeRecipe

### Community 21 - "EbxlWorldGen"
Cohesion: 0.14
Nodes (5): Choices, EbxlWorldGen, Jungle, Weighted, DesertWell

### Community 22 - "StructureComponent"
Cohesion: 0.06
Nodes (19): Teil 12 und 13 – Better Dungeons (Chocolate Quest), ComponentVillage, ComponentVillageChurch, ComponentVillageField2, ComponentVillageField, ComponentVillageHall, ComponentVillageHouse1, ComponentVillageHouse2 (+11 more)

### Community 23 - ".isAirBlock"
Cohesion: 0.09
Nodes (12): CatTail, EbxlPlantGens, Flowers, LeafPile, Marsh, Melon, Oasis, Quicksand (+4 more)

### Community 24 - "OldTerrainGen"
Cohesion: 0.22
Nodes (5): Aufbau, Teil 1 – Vanilla-1.5.2-Dekoration, Entry, OldTerrainGen, WorldGenerator

### Community 25 - "OldStructures"
Cohesion: 0.08
Nodes (8): TerrainSource, OldStructures, StructureMineshaftStart, StructureNetherBridgeStart, StructureScatteredFeatureStart, StructureStart, StructureStrongholdStart, StructureVillageStart

### Community 26 - ".findIntersecting"
Cohesion: 0.07
Nodes (19): ComponentStronghold, ComponentStrongholdChestCorridor, ComponentStrongholdCrossing, ComponentStrongholdLeftTurn, ComponentStrongholdLibrary, ComponentStrongholdPortalRoom, ComponentStrongholdPrison, ComponentStrongholdRightTurn (+11 more)

### Community 27 - "InvTweaksClient.java"
Cohesion: 0.05
Nodes (5): InvTweaksGuiIconButton, InvTweaksGuiSettingsButton, InvTweaksGuiSortingButton, InvTweaksConst, InvTweaksLocalization

### Community 28 - "net.minecraft.core.BlockPos"
Cohesion: 0.07
Nodes (8): FloraBlock, CattailBlock, EbxlPlantBlock, HexbiscusBlock, NaturaPlantBlock, SaguaroFruitBlock, NaturaIds, FloraBlock

### Community 30 - "ArmorBarHud"
Cohesion: 0.14
Nodes (10): Kind, AMOUNT, CHEST, DURABILITY, FEET, HEAD, LEGS, Unit (+2 more)

### Community 31 - "minecraft-dev"
Cohesion: 0.50
Nodes (3): npx, minecraft-dev, @mcdxai/minecraft-dev-mcp

### Community 32 - "InvTweaksContainerManager"
Cohesion: 0.06
Nodes (27): ContainerSection, ARMOR, BREWING_BOTTLES, BREWING_INGREDIENT, CHEST, CRAFTING_IN, CRAFTING_IN_PERSISTENT, CRAFTING_OUT (+19 more)

### Community 33 - "Override"
Cohesion: 0.16
Nodes (3): Barley, Cotton, NaturaCropBlock

### Community 34 - "net.minecraft.world.item.ItemStack"
Cohesion: 0.08
Nodes (8): FrostArrow, Kind, ENDER, FLAME, FROST, MULTI, PLAIN, MoreBowItem

### Community 36 - "TreeDefinition"
Cohesion: 0.06
Nodes (7): Teil 6 – bspkrsCore, TreeCapitator, Inventory Tweaks, Custom LAN Ports, BlockID, TreeCapitator, TreeCapitatorMod, Data, TreeDefinition, TreeRegistry

### Community 39 - "EnchantTableMenu"
Cohesion: 0.19
Nodes (4): Container → `AbstractContainerMenu`, EnchantTableMenu, Slot, Settings

### Community 41 - "EnchantingPlus.java"
Cohesion: 0.07
Nodes (6): GuardState, GuardStatePayload, ClientOptionPayload, EnchantPayload, OpenModTablePayload, UseModPayload

### Community 42 - "net.neoforged.bus.api.IEventBus"
Cohesion: 0.04
Nodes (21): ArmorBarMod, Artifice, ArtificeRecipes, ArtificeIds, AsgardShield, BackTools, bspkrsCoreMod, BuffBarMod (+13 more)

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
Cohesion: 0.10
Nodes (3): EplusClient, EplusConfig, TCSettings

### Community 49 - "hexxit.worldgen152.core.populate.OldGen"
Cohesion: 0.08
Nodes (17): Acacia, Acuteness, LOOSE, TIGHT, TIGHTER, Autumn, Bend, LEFT (+9 more)

### Community 50 - ".floor"
Cohesion: 0.13
Nodes (4): Generator anmelden, OldMath, Minable, BigTree

### Community 51 - "InvTweaksConfig"
Cohesion: 0.09
Nodes (4): InvTweaksConfig, OrderedProperties, InvTweaksConfigManager, InvTweaksHandlerAutoRefill

### Community 52 - "net.minecraft.world.level.block.state.BlockState"
Cohesion: 0.07
Nodes (3): BerryBushBlock, SaguaroBlock, SaguaroSproutBlock

### Community 53 - "compare_original.py"
Cohesion: 0.20
Nodes (7): cells(), main(), read_nbt(), payload(), string(), take(), region_chunks()

### Community 54 - "net.minecraft.world.level.block.Block"
Cohesion: 0.06
Nodes (6): LogTurnerItem, ScarecrowItem, SeedItem, BoneBagItem, SeedBagItem, SunflowerSeedItem

### Community 55 - "verify_blocks.py"
Cohesion: 0.14
Nodes (15): main(), packet(), read(), block(), ebxl(), flag(), main(), saguaro() (+7 more)

### Community 56 - "NaturaClient.java"
Cohesion: 0.08
Nodes (4): ArtificeClient, ColormapTint, NaturaClient, OffLawnClient

### Community 57 - "artifice/tools/gen.py"
Cohesion: 0.11
Nodes (21): bitag(), blockstate(), btag(), cube_all(), cube_element(), face(), generated_item(), handheld_item() (+13 more)

### Community 59 - "SoulEntity.java"
Cohesion: 0.06
Nodes (5): Entity-Tags (leer, von Teilauftrag 3 zu füllen), Harken Scythe: Schnittstellen für die Mobs (Teil 9, Teilauftrag 3), Schnittstelle `harkenscythe.entity.SpectralPets`, LostSoulEntity, SoulEntity

### Community 60 - "net.minecraft.world.level.block.Block"
Cohesion: 0.04
Nodes (25): ArtificeBlocks, LotusLilyBlock, EbxlLeavesBlock, LeafPileBlock, QuarterLogBlock, EbxlBlocks, EbxlIds, BloodwoodLogBlock (+17 more)

### Community 61 - "net.minecraft.server.level.ServerLevel"
Cohesion: 0.05
Nodes (5): LotusBlock, EbxlSaplingBlock, Hooks, SpectralPets, NaturaSaplingBlock

### Community 62 - "BackToolsClient.java"
Cohesion: 0.27
Nodes (3): Back, BackItem, BackToolsClient

### Community 63 - "OldBlockStates.java"
Cohesion: 0.05
Nodes (6): EbxlClient, MoreBowsClient, NaturaEntityRenderers, ZuluEntityRenderers, ZuluClient, ProjectZulu

### Community 65 - "net.minecraft.world.level.block.Block"
Cohesion: 0.12
Nodes (4): DetectorBlock, Entry, SledgeParts, ClothBlock

### Community 68 - "ComponentNetherBridgePiece"
Cohesion: 0.04
Nodes (20): Teil 7b – Natura-Nether, ComponentMineshaftStairs, ComponentNetherBridgeCorridor2, ComponentNetherBridgeCorridor3, ComponentNetherBridgeCorridor4, ComponentNetherBridgeCorridor5, ComponentNetherBridgeCorridor, ComponentNetherBridgeCrossing2 (+12 more)

### Community 69 - "NaturaTree"
Cohesion: 0.05
Nodes (34): Blöcke (Java), Brennwert und Brennbarkeit, Holzsets und Pflanzen in 26.3, Modelle und Blockstates aus Vanilla erzeugen, Prüfen, Rezeptkonflikte mit Vanilla, Tags (Block + Item), Growth (+26 more)

### Community 70 - "net.minecraft.resources.Identifier"
Cohesion: 0.04
Nodes (14): ArtificeJeiPlugin, AsgardShieldJeiPlugin, ChestTransporter, ChestTransporterJeiPlugin, EbxlJeiPlugin, EplusJeiPlugin, HarkenScytheJeiPlugin, HexxitGearJeiPlugin (+6 more)

### Community 72 - "InvTweaksConfigSortingRule"
Cohesion: 0.08
Nodes (7): InvTweaksConfigInventoryRuleset, InvTweaksConfigSortingRule, Type, COLUMN, RECTANGLE, ROW, SLOT

### Community 73 - "AltarCategory.java"
Cohesion: 0.10
Nodes (4): EnchantingCategory, TableRecipe, AltarRecipesClient, AltarCategory

### Community 74 - "JEI-Plugin für einen portierten Mod"
Cohesion: 0.40
Nodes (4): Einbindung (schon erledigt, in `build.gradle`), JEI-Plugin für einen portierten Mod, Prüfen, Vorlage

### Community 76 - "AerialAnimal"
Cohesion: 0.10
Nodes (3): AerialAnimal, Chase, FlyingWander

### Community 77 - "net.minecraft.world.item.Item"
Cohesion: 0.09
Nodes (6): ASAddons, Material, ASItems, ItemAsgardShield, ItemZedSword, ZuluItems

### Community 78 - ".getBlockId"
Cohesion: 0.05
Nodes (10): BaseCloudWorldgen, BloodTreeLargeGen, BushTreeGen, CloudGen, EucalyptusTreeGenShort, GlowshroomGenBlueGreen, NaturaGen, RareTreeGen (+2 more)

### Community 79 - "net.neoforged.neoforge.registries.DeferredItem"
Cohesion: 0.05
Nodes (8): HSItems, ScytheItem, SpectralArmorItem, HexxitGear, KamaItem, NaturaItems, ToolSet, WildCaves

### Community 80 - "InvTweaks"
Cohesion: 0.09
Nodes (3): InvTweaks, Scheduled, InvTweaksClient

### Community 82 - "net.minecraft.client.renderer.entity.state.LivingEntityRenderState"
Cohesion: 0.13
Nodes (4): ScarecrowModel, ScarecrowRenderer, HeatscarSpiderRenderer, ImpRenderer

### Community 83 - "net.minecraft.client.gui.GuiGraphicsExtractor"
Cohesion: 0.08
Nodes (6): InvTweaksGuiModNotWorking, InvTweaksGuiSettings, InvTweaksGuiSettingsAbstract, TextButton, InvTweaksGuiSettingsAdvanced, InvTweaksGuiShortcutsHelp

### Community 84 - "Client-Darstellung in 26.3 (NeoForge 26.3.0.102)"
Cohesion: 0.22
Nodes (8): Client-Darstellung in 26.3 (NeoForge 26.3.0.102), Etwas am Spielermodell zeichnen (Rücken, Schultern), Halbdurchsichtige Blöcke (Alpha im Renderer), HUD (statt `ITickHandler` mit `TickType.RENDER`), Nur-Client-Mod sauber laden, Rechtsklick vor dem Block (Original: `PlayerInteractEvent` mit `RIGHT_CLICK_BLOCK`), Rüstung: Texturen und eigene Helm-Modelle (`ItemArmor`, `getArmorTexture`, `getArmorModel`), Stolperfallen

### Community 85 - "net.minecraft.world.level.Level"
Cohesion: 0.07
Nodes (5): QuicksandBlock, CloudBlock, HeatSandBlock, LargeGlowshroomBlock, ThornVinesBlock

### Community 86 - "net.minecraft.client.model.geom.builders.LayerDefinition"
Cohesion: 0.10
Nodes (8): SoulModel, ImpModel, NaturaBoatModel, NaturaBoatRenderer, ArmadilloModel, EagleModel, FinchModel, PelicanModel

### Community 87 - "minecraft"
Cohesion: 0.11
Nodes (3): CustomLanClient, GuiCustomLan, CLOptions

### Community 88 - "Material"
Cohesion: 0.05
Nodes (30): HGWorldGen, NaturaWorldGen, OffLawnGen, Material, AIR, CACTUS, CIRCUITS, CLAY (+22 more)

### Community 89 - "net.minecraft.world.item.ItemStack"
Cohesion: 0.07
Nodes (5): EnchantException, BookItem, TalismanItem, HomeDimensions, SoulShardItem

### Community 90 - "net.neoforged.bus.api.SubscribeEvent"
Cohesion: 0.08
Nodes (5): HeadDrops, HSEvents, SetBonuses, State, SoulEvents

### Community 91 - "net.minecraft.resources.Identifier"
Cohesion: 0.09
Nodes (5): ASConfig, ASClient, GuardHud, BuffBarHud, DrinkHud

### Community 93 - ".generate"
Cohesion: 0.14
Nodes (6): OldDungeonGen, MinecartChest, Category, Entry, OldStack, Chest

### Community 94 - "Decorate"
Cohesion: 0.13
Nodes (15): Decorate, BIG_SHROOM, CACTUS, CLAY, DEAD_BUSH, FLOWERS, GRASS, LAKE (+7 more)

### Community 95 - "net.minecraft.core.BlockPos"
Cohesion: 0.07
Nodes (10): Tier, ADVANCED, BASIC, INDUSTRIAL, REINFORCED, FrameBlock, ScaffoldingBlock, GlowshroomBlock (+2 more)

### Community 96 - "Essence"
Cohesion: 0.09
Nodes (8): AmuletItem, Kind, ETHEREAL, NETHER, OVERWORLD, Essence, NecronomiconItem, Ritual

### Community 97 - "Target"
Cohesion: 0.40
Nodes (5): Target, DOWN, HOTBAR_SLOT, UNSPECIFIED, UP

### Community 101 - ".column"
Cohesion: 0.15
Nodes (3): ArtificeWorldGen, Pos, XSRandom

### Community 104 - "Mobs, Spawn-Eier und Boote auf 26.3"
Cohesion: 0.25
Nodes (7): Boote, Client, Drops, Mobs, Spawn-Eier und Boote auf 26.3, Registrieren, Spawn-Eier, Spawns

### Community 105 - ".setBlock"
Cohesion: 0.12
Nodes (3): Teil 5 – Hexxit Gear, More Bows, Enchanting Plus, offLawn, Wild Caves 3, WildCavesGen, Skull

### Community 107 - "AltarRecipe"
Cohesion: 0.07
Nodes (13): Harken Scythe: die Altäre (Teil 9, Teilauftrag 2), Action, ATTUNE, AUGMENT, ENTHRALL, ENTWINE, GERMINATE, IMBUE (+5 more)

### Community 108 - "json"
Cohesion: 0.08
Nodes (18): check_model(), van(), check_model(), camel(), convert(), emit_additions(), walk(), main() (+10 more)

### Community 109 - "Extrabiomes"
Cohesion: 0.26
Nodes (6): Bark, NE, NW, SE, SW, Extrabiomes

### Community 114 - "Type"
Cohesion: 0.11
Nodes (11): InvTweaksKeys, InvTweaksShortcutMapping, Type, DROP, MOVE_ALL_ITEMS, MOVE_DOWN, MOVE_EVERYTHING, MOVE_ONE_ITEM (+3 more)

### Community 116 - "net.minecraft.world.entity.player.Player"
Cohesion: 0.07
Nodes (9): ASGiantSwordPerks, ASShieldPerks, Effects, GuardEvents, SpecialTierAbilities, ArmorSet, SCALE, THIEF (+1 more)

### Community 117 - "HSSkullRenderer.java"
Cohesion: 0.10
Nodes (4): AltarRenderer, State, HSSkullRenderer, State

### Community 118 - "Bäume und Äxte bei TreeCapitator anmelden"
Cohesion: 0.40
Nodes (4): Bäume: Daten-Registry `treecapitator:tree`, Bäume und Äxte bei TreeCapitator anmelden, Prüfen, Äxte und Scheren: Item-Tags

### Community 119 - "ArmadilloRenderer"
Cohesion: 0.08
Nodes (9): AerialRenderer, State, ArmadilloRenderer, Easing, Eased, EasingCache, Easing, PingPong (+1 more)

### Community 120 - "EnchantTableMenu.java"
Cohesion: 0.09
Nodes (3): AltarMenu, Menu, NaturaWorkbenchBlock

### Community 121 - "NaturaGen"
Cohesion: 0.07
Nodes (6): BaseTreeWorldgen, DarkwoodGen, FusewoodGen, GlowshroomGenPurple, SaguaroGen, WhiteTreeGen

### Community 123 - "Populate"
Cohesion: 0.11
Nodes (10): OldNetherPopulator, Populate, DUNGEON, FIRE, GLOWSTONE, ICE, LAKE, LAVA (+2 more)

### Community 125 - "net.minecraft.world.entity.LivingEntity"
Cohesion: 0.10
Nodes (4): FallenSoul, GlaiveItem, ReapingWeaponItem, ImpMeatItem

### Community 127 - "EntityState"
Cohesion: 0.09
Nodes (14): EntityState, ATTACKING, FLEEING, FOLLOWING, IDLE, IN_COVER, IN_LOVE, LOOKING (+6 more)

### Community 128 - "OldGameIds"
Cohesion: 0.18
Nodes (10): Biome und Dekoratoren, Blöcke anmelden, Bäume im Spiel (Setzlinge), Nether, Prüfen, Regel 1: Kern-Klassen ohne Spielklassen, Stolperfallen, Strukturen (+2 more)

### Community 129 - "GuardSound"
Cohesion: 0.09
Nodes (16): AbstractGuardItem, GuardSound, BONE, CRYSTAL, FLESH, METAL, STONE, WOOD (+8 more)

### Community 130 - "ArtificeEvents.java"
Cohesion: 0.12
Nodes (4): ArtificeEvents, BoxItem, DispenseBox, DispenseSledge

### Community 132 - "InvTweaksItemTree"
Cohesion: 0.07
Nodes (4): InvTweaksItemTree, InvTweaksItemTreeCategory, InvTweaksItemTreeItem, InvTweaksItemTreeLoader

### Community 133 - "net.minecraft.world.entity.EntityType"
Cohesion: 0.14
Nodes (3): EagleEntity, HornBillEntity, ZuluSounds

### Community 134 - ".populate"
Cohesion: 0.08
Nodes (6): Lake, Fire, Glowshroom, Glowstone, HellLava, OldNetherGens

### Community 135 - "NaturaEntities.java"
Cohesion: 0.06
Nodes (8): HSEntities, BloodwoodBoat, NaturaEntities, ZuluEntities, Kanonische Registrierungsnamen, Konventionen, Nicht portieren, Project Zulu Teil 10 – Portierungsnotizen (Kreaturen)

### Community 136 - "Ore"
Cohesion: 0.14
Nodes (10): OldTerrainGen.Listener, Ore, COAL, DIAMOND, DIRT, GOLD, GRAVEL, IRON (+2 more)

### Community 137 - "KeeperItem"
Cohesion: 0.11
Nodes (8): KeeperItem, Kind, BLOOD_VESSEL, BLOODKEEPER, ESSENCE_KEEPER, ESSENCE_VESSEL, SOUL_VESSEL, SOULKEEPER

### Community 139 - "HeroMC – Hexxit 1:1 für Minecraft 26.3"
Cohesion: 0.18
Nodes (9): Arbeitsplan, Aufbau, Bauen und Starten, HeroMC – Hexxit 1:1 für Minecraft 26.3, Referenzquellen des Originals, Status, Vorgaben, Wann ein Mod als portiert gilt (+1 more)

### Community 142 - "harkenscythe/tools/gen.py"
Cohesion: 0.16
Nodes (19): altar_recipe(), block_item(), block_model(), blockstate(), book_rows(), damaged(), flat(), full() (+11 more)

### Community 144 - "HSBlocks.java"
Cohesion: 0.06
Nodes (8): AltarBlock, CrucibleVisitor, BiomassBlock, HSSkullBlock, LivingmetalBlock, SoulLightBlock, HSBlocks, SoulCageBlock

### Community 147 - "Bark"
Cohesion: 0.29
Nodes (5): Bark, NE, NW, SE, SW

### Community 149 - "map"
Cohesion: 0.04
Nodes (19): ArtificeItems, SickleItem, SledgeItem, Upgrade, ARMOR_SPIKES, COUNTERWEIGHT, DIVE_KIT, ELASTIC_LAYERING (+11 more)

### Community 150 - "SkullType"
Cohesion: 0.09
Nodes (18): SkullItem, SkullType, BAT, CAVE_SPIDER, CHICKEN, COW, ENDER_DRAGON, ENDERMAN (+10 more)

### Community 151 - "StructureComponent"
Cohesion: 0.07
Nodes (7): ComponentMineshaftCorridor, ComponentMineshaftCross, ComponentMineshaftRoom, ComponentStrongholdCorridor, ComponentStrongholdStairs2, StructureComponent, StructureMineshaftPieces

### Community 153 - "BloodSplashBlock"
Cohesion: 0.13
Nodes (5): BloodColor, GREEN, PURPLE, RED, BloodSplashBlock

### Community 154 - "Starting the test client without a screen"
Cohesion: 0.50
Nodes (3): Recipe, Starting the test client without a screen, What does not work and why

### Community 157 - "BirdRenderState"
Cohesion: 0.10
Nodes (4): BirdRenderState, HornBillModel, State, VultureModel

### Community 158 - "MapGenNetherBridge"
Cohesion: 0.21
Nodes (5): Found, MapGenNetherBridge, Region, SpawnEntry, Found

### Community 161 - "AerialGoals.java"
Cohesion: 0.16
Nodes (3): AerialGoals, GroundWander, PelicanEntity

### Community 169 - "Menü, Screen und Pakete in 26.3 (NeoForge 26.3.0.102)"
Cohesion: 0.22
Nodes (6): GuiContainer → `AbstractContainerScreen`, Konfiguration und Spielerdaten, Menü, Screen und Pakete in 26.3 (NeoForge 26.3.0.102), Pakete → Custom Payloads, Truhen-GUIs und Inventory Tweaks, Vanilla-Block ersetzen (Mod hat Block-ID überschrieben)

## Knowledge Gaps
- **238 isolated node(s):** `WOOD`, `STONE`, `METAL`, `CRYSTAL`, `FLESH` (+233 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 1144 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **70 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `OldWorld` connect `OldWorld` to `OldGen`, `.populate`, `Ore`, `OldBiome`, `OldBiomes.java`, `OldDecoratedChunk`, `EbxlWorldGen`, `.isAirBlock`, `OldTerrainGen`, `OldStructures`, `StructureComponent`, `MapGenNetherBridge`, `list`, `OldWorld.java`, `OldRails`, `.column`, `Listener`, `hexxit.worldgen152.core.populate.OldGen`, `.floor`, `ComponentNetherBridgePiece`, `.getBlockId`, `.generate`, `.column`, `.setBlock`, `NaturaGen`, `Populate`?**
  _High betweenness centrality (0.098) - this node is a cross-community bridge._
- **What connects `WOOD`, `STONE`, `METAL` to the rest of the system?**
  _238 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `OldWorld` be split into smaller, more focused modules?**
  _Cohesion score 0.09823335274703941 - nodes in this community are weakly interconnected._
- **Why does `OldBiome` connect `OldBiome` to `Layer`, `OldGen`, `list`, `OldBiomeSource.java`, `OldTerrain`, `OldBiomes.java`, `OldStructures`?**
  _High betweenness centrality (0.030) - this node is a cross-community bridge._
- **Should `Layer` be split into smaller, more focused modules?**
  _Cohesion score 0.0684931506849315 - nodes in this community are weakly interconnected._
- **Should `reference.py` be split into smaller, more focused modules?**
  _Cohesion score 0.1265597147950089 - nodes in this community are weakly interconnected._
- **Should `OldGen` be split into smaller, more focused modules?**
  _Cohesion score 0.0579476861167002 - nodes in this community are weakly interconnected._