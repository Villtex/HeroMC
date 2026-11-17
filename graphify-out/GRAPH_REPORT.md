# Graph Report - HeroMC  (2026-11-17)

## Corpus Check
- 4422 files · ~442,532 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 31 file(s) not represented in the graph (top: .properties 27, (none) 1, .toml 1)

## Summary
- 6268 nodes · 20703 edges · 192 communities (107 shown, 85 thin omitted)
- Extraction: 94% EXTRACTED · 6% INFERRED · 0% AMBIGUOUS · INFERRED: 1168 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `0ed95ed9`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- OldWorld
- Layer
- reference.py
- .isAirBlock
- SpectralPet
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
- ArtificeJeiPlugin.java
- EbxlWorldGen
- StructureComponent
- SpectralBat
- net.minecraft.client.model.geom.builders.LayerDefinition
- OldStructures
- .findIntersecting
- InvTweaksClient.java
- net.minecraft.core.BlockPos
- SpectralHumanoid
- minecraft-dev
- InvTweaksContainerManager
- Override
- net.minecraft.world.item.ItemStack
- list
- TreeDefinition
- OldBiomeSource.java
- .opaqueCube
- net.minecraft.core.Holder
- OldRails
- EnchantingPlus.java
- net.neoforged.bus.api.IEventBus
- gen.py
- biomes.py
- PerlinNoise
- StructurePieceBlockSelector
- EplusClient
- net.minecraft.world.InteractionResult
- hexxit.worldgen152.core.populate.OldGen
- BigTree
- InvTweaksConfig
- net.minecraft.world.level.block.state.BlockState
- compare_original.py
- net.minecraft.world.item.ItemStack
- verify_blocks.py
- NaturaClient.java
- artifice/tools/gen.py
- StalactiteBlock
- SoulEntity
- net.minecraft.world.level.block.Block
- net.minecraft.server.level.ServerLevel
- BackToolsClient.java
- EvilSpectralMiner
- original_server.sh
- EssenceSlime
- net.minecraft.world.item.ItemStack
- NaturaItems
- ComponentNetherBridgePiece
- NaturaTree
- net.minecraft.resources.Identifier
- SpectralCreeper
- InvTweaksConfigSortingRule
- AltarRecipe
- JEI-Plugin für einen portierten Mod
- SoulCageBlockEntity.java
- AerialAnimal
- net.minecraft.world.item.Item
- .getBlockId
- net.neoforged.neoforge.registries.DeferredItem
- InvTweaks
- SledgeParts
- net.minecraft.client.renderer.entity.state.LivingEntityRenderState
- net.minecraft.client.gui.GuiGraphicsExtractor
- Client-Darstellung in 26.3 (NeoForge 26.3.0.102)
- net.minecraft.world.level.Level
- net.minecraft.client.model.geom.builders.LayerDefinition
- minecraft
- Material
- .appendHoverText
- HSEvents
- net.minecraft.client.gui.GuiGraphicsExtractor
- ScarecrowEntity
- .generate
- Override
- NaturaSaplingBlock
- Essence
- Wood
- LanCommands
- EbxlBlocks
- .column
- InvTweaksHandlerSorting
- Chase
- Mobs, Spawn-Eier und Boote auf 26.3
- .setBlock
- OldCarver
- json
- Extrabiomes
- LostSoulEntity
- OldBlockStates
- SakuraTreeGen
- Type
- GuiCustomLanPermissions
- net.minecraft.world.entity.player.Player
- SpectralRenderers.java
- Bäume und Äxte bei TreeCapitator anmelden
- ArmadilloEntity
- NaturaWorkbenchBlock.java
- NaturaGen
- Natura
- Decorate
- hexxit.worldgen152.core.OldChunk
- net.minecraft.world.entity.LivingEntity
- .generate
- EntityState
- HSClient.java
- SpectralBlaze
- ArtificeEvents
- NitroCreeper
- InvTweaksItemTree
- EagleEntity
- ArmorSet
- net.minecraft.world.entity.EntityType
- Weltgenerierung eines 1.5.2-Mods in worldgen152 einhängen
- KeeperItem
- net.minecraft.world.level.block.BonemealSource
- HeroMC – Hexxit 1:1 für Minecraft 26.3
- CoFHCommand.java
- TableEvents
- harkenscythe/tools/gen.py
- net.neoforged.bus.api.SubscribeEvent
- HSBlocks.java
- ObsidiPlates
- FinchEntity
- Bark
- OldChunkGenerator
- ArtificeItems.java
- SkullType
- StructureComponent
- BloodSplashBlock
- Starting the test client without a screen
- BiomassPlantBlock
- TerrainSource
- MapGenNetherBridge
- AerialGoals.java
- AltarBlockEntity
- BeanstalkBlock
- EbxlBiomes.java
- Gen
- SpectralOcelot
- HSSkullBlockEntity
- SoulRenderer
- Growth
- SpectralRenderState
- net.minecraft.world.item.enchantment.Enchantment
- Holzsets und Pflanzen in 26.3
- FlyingWander
- AltarScreen
- VultureEntity
- SoulEvents
- ComponentNetherBridgeEntrance
- HSWorldGen.java
- AerialRenderer
- Dispenser
- StructureBoundingBox
- Verhalten ohne Spieler prüfen (26.3)
- Bark
- AerialGoals
- HornBillEntity
- SoulShards
- SickleItem
- Scope
- OldRavines
- HeadDrops
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
9. `AerialAnimal` - 55 edges
10. `RedwoodTreeGen` - 55 edges

## Surprising Connections (you probably didn't know these)
- `Bäume im Spiel (Setzlinge)` --references--> `OldGen`  [INFERRED]
  .claude/skills/worldgen152/SKILL.md → mods/worldgen152/src/main/java/hexxit/worldgen152/core/populate/OldGen.java
- `Aufbau` --references--> `WorldGenerator`  [INFERRED]
  .claude/skills/worldgen152/SKILL.md → mods/worldgen152/src/main/java/hexxit/worldgen152/core/populate/OldTerrainGen.java
- `Aufbau` --references--> `World`  [INFERRED]
  .claude/skills/worldgen152/SKILL.md → mods/worldgen152/src/main/java/hexxit/worldgen152/OldNetherChunkGenerator.java
- `Teil 1 – Vanilla-1.5.2-Dekoration` --references--> `OldDecorator`  [INFERRED]
  docs/PLAN.md → mods/worldgen152/src/main/java/hexxit/worldgen152/core/populate/OldDecorator.java
- `Teil 1 – Vanilla-1.5.2-Dekoration` --references--> `World`  [INFERRED]
  docs/PLAN.md → mods/worldgen152/src/main/java/hexxit/worldgen152/OldNetherChunkGenerator.java

## Import Cycles
- None detected.

## Communities (192 total, 85 thin omitted)

### Community 0 - "OldWorld"
Cohesion: 0.12
Nodes (4): Villager, Listener, Spawner, OldWorld

### Community 1 - "Layer"
Cohesion: 0.11
Nodes (16): Layer, AddIsland, AddMushroomIsland, AddSnow, Biome, Hills, Island, Layers (+8 more)

### Community 2 - "reference.py"
Cohesion: 0.13
Nodes (10): build_mappings(), digest(), download(), inner_archives(), java(), main(), minecraft_reference(), process() (+2 more)

### Community 3 - ".isAirBlock"
Cohesion: 0.04
Nodes (22): Aufbau, BaseCropWorldgen, OldDecorator, OldDungeonGen, OldGen, DesertWell, Lake, Spring (+14 more)

### Community 4 - "SpectralPet"
Cohesion: 0.05
Nodes (5): SpectralGhast, SpectralPet, SpectralPig, SpectralSpider, SpectralWolf

### Community 8 - "Arbeitsplan: Hexxit 1.0.11 → NeoForge 26.3"
Cohesion: 0.10
Nodes (20): Arbeitsplan: Hexxit 1.0.11 → NeoForge 26.3, Regeln für jeden Teil, Teil 10 und 11 – Project Zulu, Teil 14 – Battle Towers, Ruins, Teil 15 – Dimensional Doors, Teil 16 – BiblioCraft, Teil 17 – BetterStorage, Backpacks, qCraft, Teil 18 – ChocoCraft, Infernal Mobs (+12 more)

### Community 11 - "HeroMC – Hinweise für Claude"
Cohesion: 0.29
Nodes (6): Arbeitsweise beim Portieren, Branches, graphify, HeroMC – Hinweise für Claude, Skills anlegen, Tokens sparen

### Community 17 - "OldBiome"
Cohesion: 0.11
Nodes (7): Teil 1 – Vanilla-1.5.2-Dekoration, EbxlBiomes, Decoration, GrassChooser, OldBiome, TreeChooser, OldBiomes

### Community 18 - "OldBiomes.java"
Cohesion: 0.22
Nodes (4): OldBlockInfo, OldEntity, OldLoot, OldTile

### Community 19 - "OldDecoratedChunk"
Cohesion: 0.12
Nodes (3): OldDecoratedChunk, OldDecoration, OldChunkWriter

### Community 20 - "ArtificeJeiPlugin.java"
Cohesion: 0.07
Nodes (7): ArtificeRecipes, ArtificeJeiPlugin, BoxItem, BoxRecipe, UpgradeRecipe, GildShieldRecipe, SpectralDyeRecipe

### Community 21 - "EbxlWorldGen"
Cohesion: 0.16
Nodes (4): Choices, EbxlWorldGen, Jungle, Weighted

### Community 22 - "StructureComponent"
Cohesion: 0.06
Nodes (19): Teil 12 und 13 – Better Dungeons (Chocolate Quest), ComponentVillage, ComponentVillageChurch, ComponentVillageField2, ComponentVillageField, ComponentVillageHall, ComponentVillageHouse1, ComponentVillageHouse2 (+11 more)

### Community 23 - "SpectralBat"
Cohesion: 0.05
Nodes (10): Kind, CHICKEN, COW, SILVERFISH, VILLAGER, SimpleSpectral, SpectralBat, Sounds (+2 more)

### Community 25 - "OldStructures"
Cohesion: 0.10
Nodes (7): OldStructures, StructureMineshaftStart, StructureNetherBridgeStart, StructureScatteredFeatureStart, StructureStart, StructureStrongholdStart, StructureVillageStart

### Community 26 - ".findIntersecting"
Cohesion: 0.09
Nodes (19): ComponentStronghold, ComponentStrongholdChestCorridor, ComponentStrongholdCorridor, ComponentStrongholdCrossing, ComponentStrongholdLeftTurn, ComponentStrongholdLibrary, ComponentStrongholdPortalRoom, ComponentStrongholdPrison (+11 more)

### Community 27 - "InvTweaksClient.java"
Cohesion: 0.05
Nodes (3): InvTweaksGuiIconButton, InvTweaksGuiSettingsButton, InvTweaksGuiSortingButton

### Community 28 - "net.minecraft.core.BlockPos"
Cohesion: 0.06
Nodes (8): CattailBlock, EbxlPlantBlock, QuicksandBlock, HexbiscusBlock, NaturaPlantBlock, SaguaroFruitBlock, FloraBlock, IcicleBlock

### Community 30 - "SpectralHumanoid"
Cohesion: 0.11
Nodes (8): Kind, MINER, PIG_ZOMBIE, SKELETON, WITCH, WITHER_SKELETON, ZOMBIE, SpectralHumanoid

### Community 31 - "minecraft-dev"
Cohesion: 0.50
Nodes (3): npx, minecraft-dev, @mcdxai/minecraft-dev-mcp

### Community 32 - "InvTweaksContainerManager"
Cohesion: 0.06
Nodes (27): ContainerSection, ARMOR, BREWING_BOTTLES, BREWING_INGREDIENT, CHEST, CRAFTING_IN, CRAFTING_IN_PERSISTENT, CRAFTING_OUT (+19 more)

### Community 33 - "Override"
Cohesion: 0.15
Nodes (3): Barley, Cotton, NaturaCropBlock

### Community 34 - "net.minecraft.world.item.ItemStack"
Cohesion: 0.08
Nodes (8): FrostArrow, Kind, ENDER, FLAME, FROST, MULTI, PLAIN, MoreBowItem

### Community 35 - "list"
Cohesion: 0.06
Nodes (4): EbxlIds, OldLakeGens, OldOreGens, VillageBlocks

### Community 36 - "TreeDefinition"
Cohesion: 0.06
Nodes (7): Teil 6 – bspkrsCore, TreeCapitator, Inventory Tweaks, Custom LAN Ports, BlockID, TreeCapitator, TreeCapitatorMod, Data, TreeDefinition, TreeRegistry

### Community 39 - "net.minecraft.core.Holder"
Cohesion: 0.06
Nodes (7): EnchantTableScreen, GuiItem, EnchantException, EnchantHelper, EnchantTableMenu, Slot, Settings

### Community 40 - "OldRails"
Cohesion: 0.08
Nodes (3): OffLawnGen, OldRails, Column

### Community 41 - "EnchantingPlus.java"
Cohesion: 0.06
Nodes (12): AsgardShield, GuardState, GuardStatePayload, EnchantingPlus, Restriction, ClientOptionPayload, EnchantPayload, EplusNetwork (+4 more)

### Community 42 - "net.neoforged.bus.api.IEventBus"
Cohesion: 0.03
Nodes (16): Artifice, ArtificeIds, BackTools, bspkrsCoreMod, BuffBarMod, ChestTransporter, CoFHCore, EbxlClient (+8 more)

### Community 43 - "gen.py"
Cohesion: 0.09
Nodes (22): age(), ages(), bitag(), blockstate(), btag(), cube_all(), flam(), generated_item() (+14 more)

### Community 44 - "biomes.py"
Cohesion: 0.15
Nodes (9): ColorMaps, f32(), fixed(), hexcolor(), main(), multiply(), sky_color(), tinted() (+1 more)

### Community 46 - "StructurePieceBlockSelector"
Cohesion: 0.15
Nodes (5): ComponentScatteredFeature, ComponentScatteredFeatureDesertPyramid, ComponentScatteredFeatureJunglePyramid, ComponentScatteredFeatureSwampHut, StructureScatteredFeatureStones

### Community 47 - "EplusClient"
Cohesion: 0.11
Nodes (8): Container → `AbstractContainerMenu`, GuiContainer → `AbstractContainerScreen`, Konfiguration und Spielerdaten, Menü, Screen und Pakete in 26.3 (NeoForge 26.3.0.102), Pakete → Custom Payloads, Truhen-GUIs und Inventory Tweaks, Vanilla-Block ersetzen (Mod hat Block-ID überschrieben), EplusClient

### Community 49 - "hexxit.worldgen152.core.populate.OldGen"
Cohesion: 0.08
Nodes (17): Acacia, Acuteness, LOOSE, TIGHT, TIGHTER, Autumn, Bend, LEFT (+9 more)

### Community 50 - "BigTree"
Cohesion: 0.09
Nodes (11): TallGrass, BigMushroom, BigTree, Birch, Huge, OldTreeGens, Pine, Shrub (+3 more)

### Community 51 - "InvTweaksConfig"
Cohesion: 0.09
Nodes (4): InvTweaksConfig, OrderedProperties, InvTweaksConfigManager, InvTweaksHandlerAutoRefill

### Community 52 - "net.minecraft.world.level.block.state.BlockState"
Cohesion: 0.07
Nodes (5): DetectorBlock, BerryBushBlock, NaturaLeavesBlock, SaguaroBlock, SaguaroSproutBlock

### Community 53 - "compare_original.py"
Cohesion: 0.20
Nodes (7): cells(), main(), read_nbt(), payload(), string(), take(), region_chunks()

### Community 54 - "net.minecraft.world.item.ItemStack"
Cohesion: 0.04
Nodes (8): DispenseBox, DispenseSledge, LogTurnerItem, ScarecrowItem, SeedItem, BoneBagItem, SeedBagItem, SunflowerSeedItem

### Community 55 - "verify_blocks.py"
Cohesion: 0.14
Nodes (15): main(), packet(), read(), block(), ebxl(), flag(), main(), saguaro() (+7 more)

### Community 56 - "NaturaClient.java"
Cohesion: 0.10
Nodes (3): ColormapTint, NaturaClient, OffLawnClient

### Community 57 - "artifice/tools/gen.py"
Cohesion: 0.11
Nodes (21): bitag(), blockstate(), btag(), cube_all(), cube_element(), face(), generated_item(), handheld_item() (+13 more)

### Community 60 - "net.minecraft.world.level.block.Block"
Cohesion: 0.06
Nodes (12): ArtificeBlocks, Tier, ADVANCED, BASIC, INDUSTRIAL, REINFORCED, FloraBlock, FrameBlock (+4 more)

### Community 61 - "net.minecraft.server.level.ServerLevel"
Cohesion: 0.06
Nodes (5): LotusLilyBlock, EbxlLeavesBlock, LivingmetalBlock, Hooks, SpectralPets

### Community 62 - "BackToolsClient.java"
Cohesion: 0.10
Nodes (4): ShieldColorTint, Back, BackItem, BackToolsClient

### Community 68 - "ComponentNetherBridgePiece"
Cohesion: 0.04
Nodes (18): Teil 7b – Natura-Nether, ComponentNetherBridgeCorridor2, ComponentNetherBridgeCorridor3, ComponentNetherBridgeCorridor4, ComponentNetherBridgeCorridor5, ComponentNetherBridgeCorridor, ComponentNetherBridgeCrossing2, ComponentNetherBridgeCrossing3 (+10 more)

### Community 69 - "NaturaTree"
Cohesion: 0.10
Nodes (18): NaturaTree, AMARANTH, BLOODWOOD, BLUE_GLOWSHROOM, DARKWOOD, EUCALYPTUS, FUSEWOOD, GHOSTWOOD (+10 more)

### Community 70 - "net.minecraft.resources.Identifier"
Cohesion: 0.06
Nodes (11): AsgardShieldJeiPlugin, ChestTransporterJeiPlugin, EbxlJeiPlugin, EplusJeiPlugin, HarkenScytheJeiPlugin, HexxitGearJeiPlugin, MoreBowsJeiPlugin, NitroCreeperRenderer (+3 more)

### Community 72 - "InvTweaksConfigSortingRule"
Cohesion: 0.08
Nodes (7): InvTweaksConfigInventoryRuleset, InvTweaksConfigSortingRule, Type, COLUMN, RECTANGLE, ROW, SLOT

### Community 73 - "AltarRecipe"
Cohesion: 0.04
Nodes (18): Eigener Rezepttyp für eine Maschine (Vorbild: Harken-Scythe-Altäre), EnchantingCategory, TableRecipe, Harken Scythe: die Altäre (Teil 9, Teilauftrag 2), Action, ATTUNE, AUGMENT, ENTHRALL (+10 more)

### Community 74 - "JEI-Plugin für einen portierten Mod"
Cohesion: 0.40
Nodes (4): Einbindung (schon erledigt, in `build.gradle`), JEI-Plugin für einen portierten Mod, Prüfen, Vorlage

### Community 77 - "net.minecraft.world.item.Item"
Cohesion: 0.06
Nodes (23): ASAddons, Material, AbstractGuardItem, ASItems, GuardSound, BONE, CRYSTAL, FLESH (+15 more)

### Community 78 - ".getBlockId"
Cohesion: 0.05
Nodes (11): BaseCloudWorldgen, BloodTreeLargeGen, BushTreeGen, CloudGen, EucalyptusTreeGenShort, GlowshroomGenBlueGreen, GlowshroomGenPurple, NaturaGen (+3 more)

### Community 79 - "net.neoforged.neoforge.registries.DeferredItem"
Cohesion: 0.05
Nodes (8): HSItems, BookItem, GlaiveItem, ScytheItem, SpectralArmorItem, HexxitGear, KamaItem, WildCaves

### Community 80 - "InvTweaks"
Cohesion: 0.10
Nodes (3): InvTweaks, Scheduled, InvTweaksClient

### Community 81 - "SledgeParts"
Cohesion: 0.09
Nodes (6): Entry, SledgeParts, Biped, HelmetModels, HexxitGearClient, IClientItemExtensions

### Community 82 - "net.minecraft.client.renderer.entity.state.LivingEntityRenderState"
Cohesion: 0.11
Nodes (4): ScarecrowModel, ScarecrowRenderer, HeatscarSpiderRenderer, ImpRenderer

### Community 83 - "net.minecraft.client.gui.GuiGraphicsExtractor"
Cohesion: 0.06
Nodes (9): InvTweaksGuiModNotWorking, InvTweaksGuiSettings, InvTweaksGuiSettingsAbstract, TextButton, InvTweaksGuiSettingsAdvanced, InvTweaksGuiShortcutsHelp, InvTweaksConst, InvTweaksKeys (+1 more)

### Community 84 - "Client-Darstellung in 26.3 (NeoForge 26.3.0.102)"
Cohesion: 0.17
Nodes (9): Client-Darstellung in 26.3 (NeoForge 26.3.0.102), Etwas am Spielermodell zeichnen (Rücken, Schultern), Halbdurchsichtige Blöcke (Alpha im Renderer), HUD (statt `ITickHandler` mit `TickType.RENDER`), Nur-Client-Mod sauber laden, Rechtsklick vor dem Block (Original: `PlayerInteractEvent` mit `RIGHT_CLICK_BLOCK`), Rüstung: Texturen und eigene Helm-Modelle (`ItemArmor`, `getArmorTexture`, `getArmorModel`), Rüstungstextur je Färbefarbe (+1 more)

### Community 85 - "net.minecraft.world.level.Level"
Cohesion: 0.05
Nodes (5): ScaffoldingBlock, CloudBlock, HeatSandBlock, LargeGlowshroomBlock, ThornVinesBlock

### Community 86 - "net.minecraft.client.model.geom.builders.LayerDefinition"
Cohesion: 0.08
Nodes (12): SoulModel, ImpModel, NaturaBoatModel, NaturaBoatRenderer, ArmadilloModel, BirdRenderState, EagleModel, FinchModel (+4 more)

### Community 87 - "minecraft"
Cohesion: 0.11
Nodes (4): CustomLanClient, GuiCustomLan, CLOptions, CustomLAN

### Community 88 - "Material"
Cohesion: 0.06
Nodes (29): HGWorldGen, NaturaWorldGen, Material, AIR, CACTUS, CIRCUITS, CLAY, CLOTH (+21 more)

### Community 89 - ".appendHoverText"
Cohesion: 0.15
Nodes (3): HomeDimensions, SoulShardItem, Soul

### Community 91 - "net.minecraft.client.gui.GuiGraphicsExtractor"
Cohesion: 0.05
Nodes (18): ArmorBarMod, Kind, AMOUNT, CHEST, DURABILITY, FEET, HEAD, LEGS (+10 more)

### Community 93 - ".generate"
Cohesion: 0.21
Nodes (5): MinecartChest, Category, Entry, OldStack, Chest

### Community 96 - "Essence"
Cohesion: 0.08
Nodes (9): AmuletItem, Kind, ETHEREAL, NETHER, OVERWORLD, Essence, NecronomiconItem, Ritual (+1 more)

### Community 97 - "Wood"
Cohesion: 0.11
Nodes (14): Wood, AMARANTH, BLOODWOOD, DARKWOOD, EUCALYPTUS, FUSEWOOD, GHOSTWOOD, HOPSEED (+6 more)

### Community 101 - ".column"
Cohesion: 0.25
Nodes (3): ArtificeWorldGen, Pos, XSRandom

### Community 104 - "Mobs, Spawn-Eier und Boote auf 26.3"
Cohesion: 0.22
Nodes (8): Boote, Client, Drops, Mobs, Spawn-Eier und Boote auf 26.3, Registrieren, Spawn-Eier, Spawns, Stolperfallen (aus Teil 9, Harken Scythe)

### Community 105 - ".setBlock"
Cohesion: 0.07
Nodes (16): Generator anmelden, Teil 5 – Hexxit Gear, More Bows, Enchanting Plus, offLawn, Wild Caves 3, CatTail, EbxlPlantGens, Flowers, LeafPile, Marsh, Melon (+8 more)

### Community 108 - "json"
Cohesion: 0.08
Nodes (18): check_model(), van(), check_model(), camel(), convert(), emit_additions(), walk(), main() (+10 more)

### Community 113 - "SakuraTreeGen"
Cohesion: 0.09
Nodes (4): BigAutumn, SakuraTreeGen, OldMath, Minable

### Community 114 - "Type"
Cohesion: 0.12
Nodes (10): InvTweaksShortcutMapping, Type, DROP, MOVE_ALL_ITEMS, MOVE_DOWN, MOVE_EVERYTHING, MOVE_ONE_ITEM, MOVE_ONE_STACK (+2 more)

### Community 116 - "net.minecraft.world.entity.player.Player"
Cohesion: 0.06
Nodes (6): ASGiantSwordPerks, ASShieldPerks, Effects, GuardEvents, ItemZedSword, AltarMenu

### Community 117 - "SpectralRenderers.java"
Cohesion: 0.08
Nodes (5): BackItemLayer, HSSkullRenderer, State, Slime, HarkenScythe

### Community 118 - "Bäume und Äxte bei TreeCapitator anmelden"
Cohesion: 0.40
Nodes (4): Bäume: Daten-Registry `treecapitator:tree`, Bäume und Äxte bei TreeCapitator anmelden, Prüfen, Äxte und Scheren: Item-Tags

### Community 119 - "ArmadilloEntity"
Cohesion: 0.05
Nodes (9): State, ArmadilloRenderer, Easing, Eased, EasingCache, Easing, PingPong, VultureRenderer (+1 more)

### Community 121 - "NaturaGen"
Cohesion: 0.08
Nodes (5): BaseTreeWorldgen, DarkwoodGen, FusewoodGen, SaguaroGen, WhiteTreeGen

### Community 123 - "Decorate"
Cohesion: 0.05
Nodes (29): OldNetherPopulator, OldPopulator, Decorate, BIG_SHROOM, CACTUS, CLAY, DEAD_BUSH, FLOWERS (+21 more)

### Community 125 - "net.minecraft.world.entity.LivingEntity"
Cohesion: 0.07
Nodes (5): FallenSoul, ReapingWeaponItem, Harbinger, Teleports, ImpMeatItem

### Community 127 - "EntityState"
Cohesion: 0.13
Nodes (12): EntityState, ATTACKING, FLEEING, FOLLOWING, IDLE, IN_COVER, IN_LOVE, LOOKING (+4 more)

### Community 132 - "InvTweaksItemTree"
Cohesion: 0.07
Nodes (4): InvTweaksItemTree, InvTweaksItemTreeCategory, InvTweaksItemTreeItem, InvTweaksItemTreeLoader

### Community 134 - "ArmorSet"
Cohesion: 0.19
Nodes (4): ArmorSet, SCALE, THIEF, TRIBAL

### Community 135 - "net.minecraft.world.entity.EntityType"
Cohesion: 0.03
Nodes (15): Abweichungen (bewusst), Harken Scythe: Mobs (Teil 9, Teilauftrag 3) – Stand, Offen, HSEntities, HSMobs, BloodwoodBoat, HeatscarSpider, ImpEntity (+7 more)

### Community 136 - "Weltgenerierung eines 1.5.2-Mods in worldgen152 einhängen"
Cohesion: 0.07
Nodes (23): Biome und Dekoratoren, Blöcke anmelden, Bäume im Spiel (Setzlinge), Nether, Prüfen, Regel 1: Kern-Klassen ohne Spielklassen, Stolperfallen, Strukturen (+15 more)

### Community 137 - "KeeperItem"
Cohesion: 0.11
Nodes (8): KeeperItem, Kind, BLOOD_VESSEL, BLOODKEEPER, ESSENCE_KEEPER, ESSENCE_VESSEL, SOUL_VESSEL, SOULKEEPER

### Community 138 - "net.minecraft.world.level.block.BonemealSource"
Cohesion: 0.09
Nodes (4): EbxlSaplingBlock, GlowshroomBlock, LawnBlock, SunflowerBlock

### Community 139 - "HeroMC – Hexxit 1:1 für Minecraft 26.3"
Cohesion: 0.18
Nodes (9): Arbeitsplan, Aufbau, Bauen und Starten, HeroMC – Hexxit 1:1 für Minecraft 26.3, Referenzquellen des Originals, Status, Vorgaben, Wann ein Mod als portiert gilt (+1 more)

### Community 142 - "harkenscythe/tools/gen.py"
Cohesion: 0.14
Nodes (20): altar_recipe(), block_item(), block_model(), blockstate(), book_rows(), damaged(), flat(), full() (+12 more)

### Community 144 - "HSBlocks.java"
Cohesion: 0.05
Nodes (8): AltarBlock, CrucibleVisitor, BiomassBlock, ClothBlock, CrucibleBlock, HSSkullBlock, HSBlocks, SoulCageBlock

### Community 147 - "Bark"
Cohesion: 0.19
Nodes (6): Bark, NE, NW, SE, SW, BloodwoodLogBlock

### Community 149 - "ArtificeItems.java"
Cohesion: 0.07
Nodes (17): ArtificeItems, SledgeItem, Upgrade, ARMOR_SPIKES, COUNTERWEIGHT, DIVE_KIT, ELASTIC_LAYERING, ELASTIC_SOLES (+9 more)

### Community 150 - "SkullType"
Cohesion: 0.09
Nodes (18): SkullItem, SkullType, BAT, CAVE_SPIDER, CHICKEN, COW, ENDER_DRAGON, ENDERMAN (+10 more)

### Community 151 - "StructureComponent"
Cohesion: 0.06
Nodes (6): ComponentMineshaftCorridor, ComponentMineshaftCross, ComponentMineshaftStairs, ComponentStrongholdStairs2, StructureComponent, StructureStrongholdPieceWeight

### Community 153 - "BloodSplashBlock"
Cohesion: 0.11
Nodes (6): BloodColor, GREEN, PURPLE, RED, BloodSplashBlock, SoulLightBlock

### Community 154 - "Starting the test client without a screen"
Cohesion: 0.50
Nodes (3): Recipe, Starting the test client without a screen, What does not work and why

### Community 158 - "MapGenNetherBridge"
Cohesion: 0.19
Nodes (5): Found, MapGenNetherBridge, Region, SpawnEntry, Found

### Community 162 - "AltarBlockEntity"
Cohesion: 0.14
Nodes (3): AltarBlockEntity, AltarRenderer, State

### Community 169 - "Growth"
Cohesion: 0.20
Nodes (9): Growth, FLORA, NETHER, RARE, REDWOOD, Soil, CEILING, NETHER (+1 more)

### Community 170 - "SpectralRenderState"
Cohesion: 0.08
Nodes (12): Anim, Model, Bat, Biped, Eyes, Overlay, Power, RenderLayer<> (+4 more)

### Community 171 - "net.minecraft.world.item.enchantment.Enchantment"
Cohesion: 0.05
Nodes (5): ArtificeClient, HarkenScythe, OldCost, Augment, OldGameIds

### Community 172 - "Holzsets und Pflanzen in 26.3"
Cohesion: 0.22
Nodes (7): Blöcke (Java), Brennwert und Brennbarkeit, Holzsets und Pflanzen in 26.3, Modelle und Blockstates aus Vanilla erzeugen, Prüfen, Rezeptkonflikte mit Vanilla, Tags (Block + Item)

### Community 181 - "StructureBoundingBox"
Cohesion: 0.10
Nodes (6): Witch, ComponentMineshaftRoom, ComponentStrongholdRightTurn, StructureBoundingBox, StructurePieceBlockSelector, StructureStrongholdStones

### Community 182 - "Verhalten ohne Spieler prüfen (26.3)"
Cohesion: 0.33
Nodes (5): Fremde, unfertige Teilprojekte, Rezepte über einen Crafter, Temporärer Testbefehl mit FakePlayer, Ticken ohne Spieler, Verhalten ohne Spieler prüfen (26.3)

### Community 183 - "Bark"
Cohesion: 0.33
Nodes (5): Bark, NE, NW, SE, SW

### Community 188 - "Scope"
Cohesion: 0.40
Nodes (5): Scope, ALL_ITEMS, EVERYTHING, ONE_ITEM, ONE_STACK

## Knowledge Gaps
- **248 isolated node(s):** `CHICKEN`, `COW`, `VILLAGER`, `SILVERFISH`, `ZOMBIE` (+243 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 1216 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **85 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `OldWorld` connect `OldWorld` to `.isAirBlock`, `Weltgenerierung eines 1.5.2-Mods in worldgen152 einhängen`, `OldBiome`, `OldBiomes.java`, `OldDecoratedChunk`, `EbxlWorldGen`, `StructureComponent`, `TerrainSource`, `MapGenNetherBridge`, `list`, `EbxlBiomes.java`, `OldRails`, `hexxit.worldgen152.core.populate.OldGen`, `BigTree`, `ComponentNetherBridgeEntrance`, `StructureBoundingBox`, `ComponentNetherBridgePiece`, `.getBlockId`, `.setBlock`, `SakuraTreeGen`, `NaturaGen`, `Decorate`?**
  _High betweenness centrality (0.093) - this node is a cross-community bridge._
- **What connects `CHICKEN`, `COW`, `VILLAGER` to the rest of the system?**
  _248 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `OldWorld` be split into smaller, more focused modules?**
  _Cohesion score 0.11581173982020095 - nodes in this community are weakly interconnected._
- **Why does `OldBiome` connect `OldBiome` to `Layer`, `.isAirBlock`, `list`, `OldBiomeSource.java`, `OldTerrain`, `OldCarver`, `SakuraTreeGen`, `OldBiomes.java`, `OldStructures`, `OldRavines`?**
  _High betweenness centrality (0.023) - this node is a cross-community bridge._
- **Should `Layer` be split into smaller, more focused modules?**
  _Cohesion score 0.10897435897435898 - nodes in this community are weakly interconnected._
- **Why does `Arbeitsplan: Hexxit 1.0.11 → NeoForge 26.3` connect `Arbeitsplan: Hexxit 1.0.11 → NeoForge 26.3` to `TreeDefinition`, `ComponentNetherBridgePiece`, `.setBlock`, `HeroMC – Hexxit 1:1 für Minecraft 26.3`, `OldBiome`, `StructureComponent`?**
  _High betweenness centrality (0.016) - this node is a cross-community bridge._
- **Should `reference.py` be split into smaller, more focused modules?**
  _Cohesion score 0.1265597147950089 - nodes in this community are weakly interconnected._