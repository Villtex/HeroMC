# Graph Report - HeroMC  (2026-11-17)

## Corpus Check
- 4438 files · ~449,474 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 31 file(s) not represented in the graph (top: .properties 27, (none) 1, .toml 1)

## Summary
- 6459 nodes · 21431 edges · 194 communities (103 shown, 91 thin omitted)
- Extraction: 95% EXTRACTED · 5% INFERRED · 0% AMBIGUOUS · INFERRED: 1176 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `52c670fd`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- ZuluLandAnimal
- Layer
- reference.py
- OldGen
- SpectralPig
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
- net.minecraft.world.entity.EntityType
- net.minecraft.client.model.geom.builders.LayerDefinition
- OldStructures
- .findIntersecting
- InvTweaksClient.java
- net.minecraft.world.level.block.state.BlockState
- SpectralHumanoid
- minecraft-dev
- InvTweaksContainerManager
- Override
- net.minecraft.world.item.ItemStack
- list
- TreeDefinition
- SpectralWolf
- .opaqueCube
- net.minecraft.core.Holder
- OldRails
- EplusNetwork.java
- net.neoforged.bus.api.IEventBus
- gen.py
- biomes.py
- PerlinNoise
- StructurePieceBlockSelector
- OldBiomeSource.java
- net.minecraft.world.item.ItemStack
- hexxit.worldgen152.core.populate.OldGen
- BigTree
- InvTweaksConfig
- NaturaLeavesBlock
- compare_original.py
- net.minecraft.world.item.ItemStack
- verify_blocks.py
- NaturaClient.java
- artifice/tools/gen.py
- FoxEntity
- SoulEntity
- NaturaBlocks
- net.minecraft.server.level.ServerLevel
- BackToolsClient
- EvilSpectralMiner
- original_server.sh
- EssenceSlime
- net.minecraft.world.item.ItemStack
- ZuluEntityRenderers.java
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
- ZuluEntities.java
- InvTweaks
- SledgeParts
- net.minecraft.client.renderer.entity.state.LivingEntityRenderState
- net.minecraft.client.gui.GuiGraphicsExtractor
- Client-Darstellung in 26.3 (NeoForge 26.3.0.102)
- ScaffoldingBlock.java
- net.minecraft.client.model.geom.builders.LayerDefinition
- minecraft
- Material
- .appendHoverText
- HSEvents
- ArmorBarHud
- ScarecrowEntity
- .generate
- Override
- FrostArrow
- Kind
- Wood
- LanCommands
- EbxlBlocks
- SakuraTreeGen
- InvTweaksHandlerSorting
- Mobs, Spawn-Eier und Boote auf 26.3
- OldWorld
- OldCarver
- json
- Extrabiomes
- net.minecraft.world.level.block.RotatedPillarBlock
- OldChunkWriter.java
- OldBlockStates
- .floor
- InvTweaksHandlerShortcuts
- GuiCustomLanPermissions
- AltarMenu
- HSSkullRenderer.java
- Bäume und Äxte bei TreeCapitator anmelden
- ArmadilloEntity
- SpectralBat
- Natura.java
- Natura
- Decorate
- hexxit.worldgen152.core.OldChunk
- Harbinger
- .generate
- EntityState
- net.minecraft.client.gui.GuiGraphicsExtractor
- FrogModel
- SpectralSheep
- NitroCreeper
- InvTweaksItemTree
- net.minecraft.world.damagesource.DamageSource
- ArmorSet
- NaturaEntities
- Weltgenerierung eines 1.5.2-Mods in worldgen152 einhängen
- Kind
- GuardEvents
- HeroMC – Hexxit 1:1 für Minecraft 26.3
- CoFHCommand.java
- TableEvents
- harkenscythe/tools/gen.py
- net.neoforged.bus.api.SubscribeEvent
- HSBlocks.java
- ArtificeBlocks
- FinchEntity
- Bark
- OldChunkGenerator
- net.neoforged.neoforge.registries.DeferredItem
- SkullType
- SpectralGhast
- VultureEntity
- SaguaroGen
- Starting the test client without a screen
- PenguinEntity
- OldDecoration
- EucalyptusTreeGenShort
- .generate
- AerialGoals.java
- OldPopulator.java
- BeanstalkBlock
- EbxlBiomes.java
- Gen
- org.jspecify.annotations.Nullable
- CloudBlock
- NitroCreeperRenderer.java
- Growth
- SpectralRenderState
- arraylist
- Holzsets und Pflanzen in 26.3
- Tier
- AltarScreen
- VultureEntity
- SoulEvents
- ColormapTint
- HSWorldGen.java
- ArmadilloRenderer
- Dispenser
- StructureBoundingBox
- Verhalten ohne Spieler prüfen (26.3)
- Bark
- AerialGoals
- HornBillEntity
- .generate
- VillageHandler
- ThornVinesBlock
- OldRavines

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
- `Teil 1 – Vanilla-1.5.2-Dekoration` --references--> `OldDecorator`  [INFERRED]
  docs/PLAN.md → mods/worldgen152/src/main/java/hexxit/worldgen152/core/populate/OldDecorator.java
- `Teil 7b – Natura-Nether` --references--> `StructureNetherBridgePieces`  [INFERRED]
  docs/PLAN.md → mods/worldgen152/src/main/java/hexxit/worldgen152/core/structure/StructureNetherBridgePieces.java
- `Aufbau` --references--> `World`  [INFERRED]
  .claude/skills/worldgen152/SKILL.md → mods/worldgen152/src/main/java/hexxit/worldgen152/OldNetherChunkGenerator.java

## Import Cycles
- None detected.

## Communities (194 total, 91 thin omitted)

### Community 0 - "ZuluLandAnimal"
Cohesion: 0.07
Nodes (4): FrogEntity, OstrichEntity, FleeGoal, ZuluLandAnimal

### Community 1 - "Layer"
Cohesion: 0.11
Nodes (16): Layer, AddIsland, AddMushroomIsland, AddSnow, Biome, Hills, Island, Layers (+8 more)

### Community 2 - "reference.py"
Cohesion: 0.13
Nodes (10): build_mappings(), digest(), download(), inner_archives(), java(), main(), minecraft_reference(), process() (+2 more)

### Community 3 - "OldGen"
Cohesion: 0.05
Nodes (33): Aufbau, TreeChooser, OldDecorator, OldDungeonGen, OldGen, DesertWell, Lake, OldLakeGens (+25 more)

### Community 7 - "OldTerrain"
Cohesion: 0.16
Nodes (3): BiomeLayout, OctaveNoise, OldTerrain

### Community 8 - "Arbeitsplan: Hexxit 1.0.11 → NeoForge 26.3"
Cohesion: 0.11
Nodes (19): Arbeitsplan: Hexxit 1.0.11 → NeoForge 26.3, Regeln für jeden Teil, Teil 10 und 11 – Project Zulu, Teil 14 – Battle Towers, Ruins, Teil 15 – Dimensional Doors, Teil 16 – BiblioCraft, Teil 17 – BetterStorage, Backpacks, qCraft, Teil 18 – ChocoCraft, Infernal Mobs (+11 more)

### Community 11 - "HeroMC – Hinweise für Claude"
Cohesion: 0.29
Nodes (6): Arbeitsweise beim Portieren, Branches, graphify, HeroMC – Hinweise für Claude, Skills anlegen, Tokens sparen

### Community 17 - "OldBiome"
Cohesion: 0.11
Nodes (5): EbxlBiomes, Decoration, GrassChooser, OldBiome, OldBiomes

### Community 18 - "OldBiomes.java"
Cohesion: 0.19
Nodes (6): OldBlockInfo, OldEntity, Villager, Witch, OldLoot, OldTile

### Community 20 - "ArtificeJeiPlugin.java"
Cohesion: 0.11
Nodes (4): BoxRecipe, UpgradeRecipe, GildShieldRecipe, SpectralDyeRecipe

### Community 21 - "EbxlWorldGen"
Cohesion: 0.16
Nodes (4): Choices, EbxlWorldGen, Jungle, Weighted

### Community 22 - "StructureComponent"
Cohesion: 0.05
Nodes (17): ComponentVillage, ComponentVillageChurch, ComponentVillageField2, ComponentVillageField, ComponentVillageHall, ComponentVillageHouse1, ComponentVillageHouse2, ComponentVillageHouse3 (+9 more)

### Community 23 - "net.minecraft.world.entity.EntityType"
Cohesion: 0.05
Nodes (13): Abweichungen (bewusst), Harken Scythe: Mobs (Teil 9, Teilauftrag 3) – Stand, Offen, HSMobs, Kind, CHICKEN, COW, SILVERFISH (+5 more)

### Community 25 - "OldStructures"
Cohesion: 0.08
Nodes (12): Found, MapGenNetherBridge, Region, SpawnEntry, Found, OldStructures, StructureMineshaftStart, StructureNetherBridgeStart (+4 more)

### Community 26 - ".findIntersecting"
Cohesion: 0.06
Nodes (20): ComponentStronghold, ComponentStrongholdChestCorridor, ComponentStrongholdCrossing, ComponentStrongholdLeftTurn, ComponentStrongholdLibrary, ComponentStrongholdPortalRoom, ComponentStrongholdPrison, ComponentStrongholdRightTurn (+12 more)

### Community 27 - "InvTweaksClient.java"
Cohesion: 0.05
Nodes (3): InvTweaksGuiIconButton, InvTweaksGuiSettingsButton, InvTweaksGuiSortingButton

### Community 28 - "net.minecraft.world.level.block.state.BlockState"
Cohesion: 0.03
Nodes (25): DetectorBlock, FloraBlock, CattailBlock, EbxlLeavesBlock, EbxlPlantBlock, EbxlSaplingBlock, LeafPileBlock, QuicksandBlock (+17 more)

### Community 30 - "SpectralHumanoid"
Cohesion: 0.11
Nodes (8): Kind, MINER, PIG_ZOMBIE, SKELETON, WITCH, WITHER_SKELETON, ZOMBIE, SpectralHumanoid

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
Cohesion: 0.06
Nodes (6): Teil 6 – bspkrsCore, TreeCapitator, Inventory Tweaks, Custom LAN Ports, BlockID, TreeCapitator, Data, TreeDefinition, TreeRegistry

### Community 37 - "SpectralWolf"
Cohesion: 0.09
Nodes (5): DispenseBox, DispenseSledge, Entry, SledgeParts, SpectralWolf

### Community 38 - ".opaqueCube"
Cohesion: 0.09
Nodes (5): BerryBushGen, GlowshroomGenBlueGreen, NaturaGen, NetherBerryBushGen, WillowGen

### Community 39 - "net.minecraft.core.Holder"
Cohesion: 0.05
Nodes (11): HarkenScythe, EnchantTableScreen, GuiItem, EnchantHelper, OldCost, Restriction, EnchantTableMenu, Settings (+3 more)

### Community 40 - "OldRails"
Cohesion: 0.08
Nodes (5): ArtificeWorldGen, Pos, XSRandom, OldRails, Column

### Community 42 - "net.neoforged.bus.api.IEventBus"
Cohesion: 0.03
Nodes (25): ArmorBarMod, Artifice, ArtificeRecipes, ArtificeIds, AsgardShield, ASClient, BackTools, bspkrsCoreMod (+17 more)

### Community 43 - "gen.py"
Cohesion: 0.09
Nodes (22): age(), ages(), bitag(), blockstate(), btag(), cube_all(), flam(), generated_item() (+14 more)

### Community 44 - "biomes.py"
Cohesion: 0.15
Nodes (9): ColorMaps, f32(), fixed(), hexcolor(), main(), multiply(), sky_color(), tinted() (+1 more)

### Community 46 - "StructurePieceBlockSelector"
Cohesion: 0.09
Nodes (7): ComponentScatteredFeature, ComponentScatteredFeatureDesertPyramid, ComponentScatteredFeatureJunglePyramid, ComponentScatteredFeatureSwampHut, StructurePieceBlockSelector, StructureScatteredFeatureStones, StructureStrongholdStones

### Community 47 - "OldBiomeSource.java"
Cohesion: 0.05
Nodes (13): Container → `AbstractContainerMenu`, GuiContainer → `AbstractContainerScreen`, Konfiguration und Spielerdaten, Menü, Screen und Pakete in 26.3 (NeoForge 26.3.0.102), Pakete → Custom Payloads, Truhen-GUIs und Inventory Tweaks, Vanilla-Block ersetzen (Mod hat Block-ID überschrieben), ASConfig (+5 more)

### Community 48 - "net.minecraft.world.item.ItemStack"
Cohesion: 0.03
Nodes (12): BoxItem, Effects, AbstractGuardItem, AmuletItem, Essence, KeeperItem, NecronomiconItem, ReapingWeaponItem (+4 more)

### Community 49 - "hexxit.worldgen152.core.populate.OldGen"
Cohesion: 0.10
Nodes (16): Acacia, Acuteness, LOOSE, TIGHT, TIGHTER, Autumn, Bend, LEFT (+8 more)

### Community 51 - "InvTweaksConfig"
Cohesion: 0.08
Nodes (4): InvTweaksConfig, OrderedProperties, InvTweaksConfigManager, InvTweaksHandlerAutoRefill

### Community 53 - "compare_original.py"
Cohesion: 0.20
Nodes (7): cells(), main(), read_nbt(), payload(), string(), take(), region_chunks()

### Community 54 - "net.minecraft.world.item.ItemStack"
Cohesion: 0.03
Nodes (14): LogTurnerItem, ScarecrowItem, HSItems, BookItem, GlaiveItem, ScytheItem, SeedItem, SkullItem (+6 more)

### Community 55 - "verify_blocks.py"
Cohesion: 0.14
Nodes (15): main(), packet(), read(), block(), ebxl(), flag(), main(), saguaro() (+7 more)

### Community 56 - "NaturaClient.java"
Cohesion: 0.10
Nodes (3): ArtificeClient, NaturaClient, OffLawnClient

### Community 57 - "artifice/tools/gen.py"
Cohesion: 0.11
Nodes (21): bitag(), blockstate(), btag(), cube_all(), cube_element(), face(), generated_item(), handheld_item() (+13 more)

### Community 58 - "FoxEntity"
Cohesion: 0.11
Nodes (7): FoxEntity, FollowOwnerGoal, Mode, FOLLOW, GUARD, SIT, STAY

### Community 59 - "SoulEntity"
Cohesion: 0.06
Nodes (3): LostSoulEntity, SoulEntity, HSEntities

### Community 61 - "net.minecraft.server.level.ServerLevel"
Cohesion: 0.04
Nodes (9): LotusBlock, LotusLilyBlock, SickleItem, SledgeItem, BiomassPlantBlock, FallenSoul, Hooks, SpectralPets (+1 more)

### Community 62 - "BackToolsClient"
Cohesion: 0.27
Nodes (3): Back, BackItem, BackToolsClient

### Community 67 - "ZuluEntityRenderers.java"
Cohesion: 0.10
Nodes (4): EbxlClient, NaturaEntityRenderers, ZuluEntityRenderers, ZuluClient

### Community 68 - "ComponentNetherBridgePiece"
Cohesion: 0.04
Nodes (20): Teil 7b – Natura-Nether, ComponentMineshaftStairs, ComponentNetherBridgeCorridor2, ComponentNetherBridgeCorridor3, ComponentNetherBridgeCorridor4, ComponentNetherBridgeCorridor5, ComponentNetherBridgeCorridor, ComponentNetherBridgeCrossing2 (+12 more)

### Community 69 - "NaturaTree"
Cohesion: 0.10
Nodes (18): NaturaTree, AMARANTH, BLOODWOOD, BLUE_GLOWSHROOM, DARKWOOD, EUCALYPTUS, FUSEWOOD, GHOSTWOOD (+10 more)

### Community 70 - "net.minecraft.resources.Identifier"
Cohesion: 0.05
Nodes (13): ArtificeJeiPlugin, AsgardShieldJeiPlugin, ChestTransporterJeiPlugin, EbxlJeiPlugin, EplusJeiPlugin, HarkenScytheJeiPlugin, HexxitGearJeiPlugin, MoreBowsJeiPlugin (+5 more)

### Community 72 - "InvTweaksConfigSortingRule"
Cohesion: 0.08
Nodes (7): InvTweaksConfigInventoryRuleset, InvTweaksConfigSortingRule, Type, COLUMN, RECTANGLE, ROW, SLOT

### Community 73 - "AltarRecipe"
Cohesion: 0.05
Nodes (17): Eigener Rezepttyp für eine Maschine (Vorbild: Harken-Scythe-Altäre), EnchantingCategory, TableRecipe, Harken Scythe: die Altäre (Teil 9, Teilauftrag 2), Action, ATTUNE, AUGMENT, ENTHRALL (+9 more)

### Community 74 - "JEI-Plugin für einen portierten Mod"
Cohesion: 0.40
Nodes (4): Einbindung (schon erledigt, in `build.gradle`), JEI-Plugin für einen portierten Mod, Prüfen, Vorlage

### Community 77 - "net.minecraft.world.item.Item"
Cohesion: 0.05
Nodes (22): ASAddons, Material, ASGiantSwordPerks, ASShieldPerks, ASItems, GuardSound, BONE, CRYSTAL (+14 more)

### Community 79 - "ZuluEntities.java"
Cohesion: 0.05
Nodes (8): BeaverEntity, BoarEntity, RabbitEntity, ZuluEntities, Kanonische Registrierungsnamen, Konventionen, Nicht portieren, Project Zulu Teil 10 – Portierungsnotizen (Kreaturen)

### Community 80 - "InvTweaks"
Cohesion: 0.09
Nodes (3): InvTweaks, Scheduled, InvTweaksClient

### Community 81 - "SledgeParts"
Cohesion: 0.10
Nodes (4): HelmetModels, HexxitGearClient, IClientItemExtensions, PenguinModel

### Community 82 - "net.minecraft.client.renderer.entity.state.LivingEntityRenderState"
Cohesion: 0.06
Nodes (8): ScarecrowModel, ScarecrowRenderer, HeatscarSpiderRenderer, ImpModel, ImpRenderer, LandAnimalRenderer, OstrichModel, ProjectZulu

### Community 83 - "net.minecraft.client.gui.GuiGraphicsExtractor"
Cohesion: 0.06
Nodes (9): InvTweaksGuiModNotWorking, InvTweaksGuiSettings, InvTweaksGuiSettingsAbstract, TextButton, InvTweaksGuiSettingsAdvanced, InvTweaksGuiShortcutsHelp, InvTweaksConst, InvTweaksKeys (+1 more)

### Community 84 - "Client-Darstellung in 26.3 (NeoForge 26.3.0.102)"
Cohesion: 0.15
Nodes (9): Client-Darstellung in 26.3 (NeoForge 26.3.0.102), Etwas am Spielermodell zeichnen (Rücken, Schultern), Halbdurchsichtige Blöcke (Alpha im Renderer), HUD (statt `ITickHandler` mit `TickType.RENDER`), Nur-Client-Mod sauber laden, Rechtsklick vor dem Block (Original: `PlayerInteractEvent` mit `RIGHT_CLICK_BLOCK`), Rüstung: Texturen und eigene Helm-Modelle (`ItemArmor`, `getArmorTexture`, `getArmorModel`), Rüstungstextur je Färbefarbe (+1 more)

### Community 86 - "net.minecraft.client.model.geom.builders.LayerDefinition"
Cohesion: 0.08
Nodes (11): SoulModel, NaturaBoatModel, NaturaBoatRenderer, BeaverModel, BirdRenderState, BoarModel, EagleModel, FinchModel (+3 more)

### Community 87 - "minecraft"
Cohesion: 0.17
Nodes (3): CustomLanClient, GuiCustomLan, CLOptions

### Community 88 - "Material"
Cohesion: 0.06
Nodes (29): HGWorldGen, NaturaWorldGen, Material, AIR, CACTUS, CIRCUITS, CLAY, CLOTH (+21 more)

### Community 91 - "ArmorBarHud"
Cohesion: 0.13
Nodes (10): Kind, AMOUNT, CHEST, DURABILITY, FEET, HEAD, LEGS, Unit (+2 more)

### Community 93 - ".generate"
Cohesion: 0.32
Nodes (3): MinecartChest, OldStack, Chest

### Community 95 - "FrostArrow"
Cohesion: 0.13
Nodes (3): FrostArrowRenderer, MoreBowsClient, FrostArrow

### Community 96 - "Kind"
Cohesion: 0.40
Nodes (4): Kind, ETHEREAL, NETHER, OVERWORLD

### Community 97 - "Wood"
Cohesion: 0.09
Nodes (14): Wood, AMARANTH, BLOODWOOD, DARKWOOD, EUCALYPTUS, FUSEWOOD, GHOSTWOOD, HOPSEED (+6 more)

### Community 104 - "Mobs, Spawn-Eier und Boote auf 26.3"
Cohesion: 0.22
Nodes (8): Boote, Client, Drops, Mobs, Spawn-Eier und Boote auf 26.3, Registrieren, Spawn-Eier, Spawns, Stolperfallen (aus Teil 9, Harken Scythe)

### Community 105 - "OldWorld"
Cohesion: 0.04
Nodes (18): Teil 5 – Hexxit Gear, More Bows, Enchanting Plus, offLawn, Wild Caves 3, CatTail, EbxlPlantGens, Flowers, LeafPile, Marsh, Melon, Oasis (+10 more)

### Community 108 - "json"
Cohesion: 0.08
Nodes (18): check_model(), van(), check_model(), camel(), convert(), emit_additions(), walk(), main() (+10 more)

### Community 110 - "net.minecraft.world.level.block.RotatedPillarBlock"
Cohesion: 0.13
Nodes (3): QuarterLogBlock, BloodwoodLogBlock, FusewoodLogBlock

### Community 113 - ".floor"
Cohesion: 0.16
Nodes (3): BigAutumn, NetherCaves, OldMath

### Community 114 - "InvTweaksHandlerShortcuts"
Cohesion: 0.07
Nodes (24): Action, DROP, MOVE, InvTweaksHandlerShortcuts, Scope, ALL_ITEMS, EVERYTHING, ONE_ITEM (+16 more)

### Community 116 - "AltarMenu"
Cohesion: 0.07
Nodes (4): Slot, AltarMenu, Menu, NaturaWorkbenchBlock

### Community 117 - "HSSkullRenderer.java"
Cohesion: 0.07
Nodes (7): HSSkullBlockEntity, AltarRenderer, State, HSSkullRenderer, State, SoulRenderer, SoulRenderState

### Community 118 - "Bäume und Äxte bei TreeCapitator anmelden"
Cohesion: 0.40
Nodes (4): Bäume: Daten-Registry `treecapitator:tree`, Bäume und Äxte bei TreeCapitator anmelden, Prüfen, Äxte und Scheren: Item-Tags

### Community 121 - "Natura.java"
Cohesion: 0.05
Nodes (9): BaseTreeWorldgen, BloodTreeLargeGen, BushTreeGen, DarkwoodGen, FusewoodGen, GlowshroomGenPurple, NaturaIds, RareTreeGen (+1 more)

### Community 123 - "Decorate"
Cohesion: 0.04
Nodes (37): Teil 1 – Vanilla-1.5.2-Dekoration, OldNetherPopulator, Decorate, BIG_SHROOM, CACTUS, CLAY, DEAD_BUSH, FLOWERS (+29 more)

### Community 127 - "EntityState"
Cohesion: 0.12
Nodes (13): EntityState, ATTACKING, FLEEING, FOLLOWING, IDLE, IN_COVER, IN_LOVE, LOOKING (+5 more)

### Community 128 - "net.minecraft.client.gui.GuiGraphicsExtractor"
Cohesion: 0.05
Nodes (7): GuardHud, GuardState, GuardStatePayload, BuffBarHud, DrinkHud, HSClient, IClientItemExtensions

### Community 129 - "FrogModel"
Cohesion: 0.15
Nodes (5): State, FrogModel, State, RabbitModel, State

### Community 132 - "InvTweaksItemTree"
Cohesion: 0.07
Nodes (4): InvTweaksItemTree, InvTweaksItemTreeCategory, InvTweaksItemTreeItem, InvTweaksItemTreeLoader

### Community 134 - "ArmorSet"
Cohesion: 0.21
Nodes (4): ArmorSet, SCALE, THIEF, TRIBAL

### Community 135 - "NaturaEntities"
Cohesion: 0.06
Nodes (5): BabyHeatscarSpider, BloodwoodBoat, HeatscarSpider, ImpEntity, NaturaEntities

### Community 136 - "Weltgenerierung eines 1.5.2-Mods in worldgen152 einhängen"
Cohesion: 0.10
Nodes (16): Biome und Dekoratoren, Blöcke anmelden, Bäume im Spiel (Setzlinge), Generator anmelden, Nether, Prüfen, Regel 1: Kern-Klassen ohne Spielklassen, Stolperfallen (+8 more)

### Community 137 - "Kind"
Cohesion: 0.17
Nodes (7): Kind, BLOOD_VESSEL, BLOODKEEPER, ESSENCE_KEEPER, ESSENCE_VESSEL, SOUL_VESSEL, SOULKEEPER

### Community 139 - "HeroMC – Hexxit 1:1 für Minecraft 26.3"
Cohesion: 0.18
Nodes (9): Arbeitsplan, Aufbau, Bauen und Starten, HeroMC – Hexxit 1:1 für Minecraft 26.3, Referenzquellen des Originals, Status, Vorgaben, Wann ein Mod als portiert gilt (+1 more)

### Community 142 - "harkenscythe/tools/gen.py"
Cohesion: 0.14
Nodes (20): altar_recipe(), block_item(), block_model(), blockstate(), book_rows(), damaged(), flat(), full() (+12 more)

### Community 143 - "net.neoforged.bus.api.SubscribeEvent"
Cohesion: 0.14
Nodes (3): ArtificeEvents, SetBonuses, State

### Community 144 - "HSBlocks.java"
Cohesion: 0.04
Nodes (14): AltarBlock, CrucibleVisitor, AltarBlockEntity, BiomassBlock, BloodColor, GREEN, PURPLE, RED (+6 more)

### Community 147 - "Bark"
Cohesion: 0.29
Nodes (5): Bark, NE, NW, SE, SW

### Community 149 - "net.neoforged.neoforge.registries.DeferredItem"
Cohesion: 0.03
Nodes (24): ArtificeItems, Upgrade, ARMOR_SPIKES, COUNTERWEIGHT, DIVE_KIT, ELASTIC_LAYERING, ELASTIC_SOLES, FIREDAMP (+16 more)

### Community 150 - "SkullType"
Cohesion: 0.09
Nodes (17): SkullType, BAT, CAVE_SPIDER, CHICKEN, COW, ENDER_DRAGON, ENDERMAN, MOOSHROOM (+9 more)

### Community 154 - "Starting the test client without a screen"
Cohesion: 0.50
Nodes (3): Recipe, Starting the test client without a screen, What does not work and why

### Community 166 - "org.jspecify.annotations.Nullable"
Cohesion: 0.08
Nodes (3): SpectralEnderman, SpectralOcelot, SpectralPet

### Community 169 - "Growth"
Cohesion: 0.20
Nodes (9): Growth, FLORA, NETHER, RARE, REDWOOD, Soil, CEILING, NETHER (+1 more)

### Community 170 - "SpectralRenderState"
Cohesion: 0.05
Nodes (16): BackItemLayer, Anim, Biped, Model, Bat, Biped, Blaze, Eyes (+8 more)

### Community 172 - "Holzsets und Pflanzen in 26.3"
Cohesion: 0.18
Nodes (8): Blöcke (Java), Brennwert und Brennbarkeit, Holzsets und Pflanzen in 26.3, Modelle und Blockstates aus Vanilla erzeugen, Prüfen, Rezeptkonflikte mit Vanilla, Tags (Block + Item), SaplingGrower

### Community 173 - "Tier"
Cohesion: 0.33
Nodes (5): Tier, ADVANCED, BASIC, INDUSTRIAL, REINFORCED

### Community 179 - "ArmadilloRenderer"
Cohesion: 0.07
Nodes (12): AerialRenderer, ArmadilloModel, State, ArmadilloRenderer, Easing, Eased, EasingCache, State (+4 more)

### Community 181 - "StructureBoundingBox"
Cohesion: 0.07
Nodes (10): Category, Entry, Spawner, ComponentMineshaftCorridor, ComponentMineshaftCross, ComponentMineshaftRoom, ComponentStrongholdCorridor, StructureBoundingBox (+2 more)

### Community 182 - "Verhalten ohne Spieler prüfen (26.3)"
Cohesion: 0.33
Nodes (5): Fremde, unfertige Teilprojekte, Rezepte über einen Crafter, Temporärer Testbefehl mit FakePlayer, Ticken ohne Spieler, Verhalten ohne Spieler prüfen (26.3)

### Community 183 - "Bark"
Cohesion: 0.33
Nodes (5): Bark, NE, NW, SE, SW

### Community 184 - "AerialGoals"
Cohesion: 0.22
Nodes (3): AerialGoals, FlyingWander, GroundWander

## Knowledge Gaps
- **252 isolated node(s):** `SIT`, `FOLLOW`, `GUARD`, `STAY`, `DROP` (+247 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 1234 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **91 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `OldWorld` connect `OldWorld` to `OldGen`, `OldBiome`, `OldBiomes.java`, `EbxlWorldGen`, `StructureComponent`, `OldStructures`, `OldDecoration`, `list`, `EbxlBiomes.java`, `OldRails`, `arraylist`, `hexxit.worldgen152.core.populate.OldGen`, `BigTree`, `StructureBoundingBox`, `ComponentNetherBridgePiece`, `.getBlockId`, `.floor`, `Natura.java`, `Decorate`?**
  _High betweenness centrality (0.083) - this node is a cross-community bridge._
- **What connects `SIT`, `FOLLOW`, `GUARD` to the rest of the system?**
  _252 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `ZuluLandAnimal` be split into smaller, more focused modules?**
  _Cohesion score 0.07088989441930618 - nodes in this community are weakly interconnected._
- **Why does `StructureComponent` connect `StructureBoundingBox` to `ComponentNetherBridgePiece`, `StructurePieceBlockSelector`, `OldBiomes.java`, `StructureComponent`, `OldStructures`, `.findIntersecting`?**
  _High betweenness centrality (0.029) - this node is a cross-community bridge._
- **Should `Layer` be split into smaller, more focused modules?**
  _Cohesion score 0.10897435897435898 - nodes in this community are weakly interconnected._
- **Why does `OldBiome` connect `OldBiome` to `Layer`, `OldGen`, `list`, `OldTerrain`, `OldCarver`, `OldBiomeSource.java`, `.floor`, `OldBiomes.java`, `OldRavines`?**
  _High betweenness centrality (0.023) - this node is a cross-community bridge._
- **Should `reference.py` be split into smaller, more focused modules?**
  _Cohesion score 0.1265597147950089 - nodes in this community are weakly interconnected._