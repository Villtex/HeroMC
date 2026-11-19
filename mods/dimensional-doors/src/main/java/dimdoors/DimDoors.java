package dimdoors;

import dimdoors.block.DimDoorBlock;
import dimdoors.block.DimDoorBlockEntity;
import dimdoors.block.DimHatchBlock;
import dimdoors.block.EternalFabricBlock;
import dimdoors.block.FabricBlock;
import dimdoors.block.RiftBlock;
import dimdoors.block.RiftBlockEntity;
import dimdoors.block.UnraveledFabricBlock;
import dimdoors.entity.Monolith;
import dimdoors.gen.DDLoot;
import dimdoors.gen.Dungeons;
import dimdoors.gen.RiftGen;
import dimdoors.item.DimDoorItem;
import dimdoors.item.RiftBladeItem;
import dimdoors.item.RiftRemoverItem;
import dimdoors.item.RiftSignatureItem;
import dimdoors.world.LimboChunkGenerator;
import hexxit.worldgen152.OldBlockStates;
import hexxit.worldgen152.OldGameIds;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.component.Tool;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;
import java.util.function.Function;
import com.mojang.serialization.MapCodec;

/**
 * Dimensional Doors 1.5.2R1.4.1RC1 (FML id "DimDoors") for NeoForge: doors into pocket dimensions, rifts, dungeons
 * from schematics, Limbo and its monoliths. Settings are those of the pack's DimDoors.cfg.
 */
@Mod(DimDoors.MOD_ID)
public final class DimDoors {
    public static final String MOD_ID = "dimdoors";
    public static final ResourceKey<Level> LIMBO = ResourceKey.create(Registries.DIMENSION, id("limbo"));
    public static final ResourceKey<Level> POCKET = ResourceKey.create(Registries.DIMENSION, id("pocket"));
    /** "Limbo Dimension ID". */
    public static final int LIMBO_ID = -23;

    // the pack's DimDoors.cfg ("general")
    public static final boolean RIFT_GRIEFING = true;
    public static final boolean LIMBO_ENABLED = true;
    public static final boolean HARDCORE_LIMBO = false;
    public static final boolean LIMBO_RETURNS_INVENTORY = true;
    public static final int LIMBO_RETURN_RANGE = 2000;
    public static final int MONOLITH_SPAWNING_CHANCE = 28;

    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MOD_ID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MOD_ID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MOD_ID);
    private static final DeferredRegister.Entities ENTITIES = DeferredRegister.createEntities(MOD_ID);
    private static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, MOD_ID);
    private static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MOD_ID);
    private static final DeferredRegister<MapCodec<? extends ChunkGenerator>> GENERATORS = DeferredRegister.create(Registries.CHUNK_GENERATOR, MOD_ID);

    /** Material.iron, hardness 1, resistance 2000; opens by hand. Drops an iron door. */
    public static final DeferredBlock<DimDoorBlock> DIMENSIONAL_DOOR = BLOCKS.registerBlock("dimensional_door",
            p -> new DimDoorBlock(DimDoorBlock.Kind.DIMENSIONAL, BlockSetType.IRON, p),
            p -> p.mapColor(MapColor.METAL).strength(1.0F, 1200.0F).requiresCorrectToolForDrops().noOcclusion().sound(SoundType.METAL)
                    .pushReaction(PushReaction.IMMOVEABLE));
    /** Material.wood, hardness 1. Drops a wooden door. */
    public static final DeferredBlock<DimDoorBlock> WARP_DOOR = BLOCKS.registerBlock("warp_door",
            p -> new DimDoorBlock(DimDoorBlock.Kind.WARP, BlockSetType.OAK, p),
            p -> p.mapColor(MapColor.WOOD).strength(1.0F).noOcclusion().sound(SoundType.WOOD).pushReaction(PushReaction.IMMOVEABLE));
    /** Material.iron, hardness 0.2. */
    public static final DeferredBlock<DimDoorBlock> UNSTABLE_DOOR = BLOCKS.registerBlock("unstable_door",
            p -> new DimDoorBlock(DimDoorBlock.Kind.UNSTABLE, BlockSetType.IRON, p),
            p -> p.mapColor(MapColor.METAL).strength(0.2F).requiresCorrectToolForDrops().noOcclusion().sound(SoundType.METAL)
                    .pushReaction(PushReaction.IMMOVEABLE));
    /** Material.grass, hardness 1, no collision, nothing dropped. */
    public static final DeferredBlock<DimDoorBlock> TRANSIENT_DOOR = BLOCKS.registerBlock("transient_door",
            p -> new DimDoorBlock(DimDoorBlock.Kind.TRANSIENT, BlockSetType.OAK, p),
            p -> p.mapColor(MapColor.GRASS).strength(1.0F).noOcclusion().noCollision().sound(SoundType.GRASS).noLootTable()
                    .pushReaction(PushReaction.IMMOVEABLE));
    public static final DeferredBlock<DimHatchBlock> TRANSDIMENSIONAL_TRAPDOOR = BLOCKS.registerBlock("transdimensional_trapdoor",
            p -> new DimHatchBlock(BlockSetType.IRON, p),
            p -> p.mapColor(MapColor.METAL).strength(1.0F).requiresCorrectToolForDrops().noOcclusion().sound(SoundType.METAL));
    /** Material.ground, hardness 0.1, light 15. */
    public static final DeferredBlock<FabricBlock> FABRIC = BLOCKS.registerBlock("fabric_of_reality", p -> new FabricBlock(false, p),
            p -> p.mapColor(MapColor.COLOR_BLACK).strength(0.1F).lightLevel(s -> 15).sound(SoundType.GRAVEL));
    /** Metadata 1 of Fabric of Reality: hardness 10^13. */
    public static final DeferredBlock<FabricBlock> ANCIENT_FABRIC = BLOCKS.registerBlock("ancient_fabric", p -> new FabricBlock(true, p),
            p -> p.mapColor(MapColor.COLOR_BLACK).strength(1.0E13F, 0.0F).lightLevel(s -> 15).sound(SoundType.GRAVEL));
    /** Unbreakable, light 15. */
    public static final DeferredBlock<EternalFabricBlock> ETERNAL_FABRIC = BLOCKS.registerBlock("eternal_fabric", EternalFabricBlock::new,
            p -> p.mapColor(MapColor.COLOR_BLACK).strength(-1.0F, 3600000.0F).lightLevel(s -> 15).sound(SoundType.GRAVEL).noLootTable());
    /** Material.ground, hardness 0.2. */
    public static final DeferredBlock<UnraveledFabricBlock> UNRAVELED_FABRIC = BLOCKS.registerBlock("unraveled_fabric", UnraveledFabricBlock::new,
            p -> p.mapColor(MapColor.COLOR_GRAY).strength(0.2F).sound(SoundType.GRAVEL));
    public static final DeferredBlock<RiftBlock> RIFT = BLOCKS.registerBlock("rift", RiftBlock::new,
            p -> p.mapColor(MapColor.NONE).strength(1.0F).noCollision().noOcclusion().replaceable().randomTicks().noLootTable()
                    .pushReaction(PushReaction.IMMOVEABLE));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DimDoorBlockEntity>> DOOR_ENTITY = BLOCK_ENTITIES.register("door",
            () -> new BlockEntityType<>(DimDoorBlockEntity::new, DIMENSIONAL_DOOR.get(), WARP_DOOR.get(), UNSTABLE_DOOR.get(), TRANSIENT_DOOR.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<RiftBlockEntity>> RIFT_ENTITY = BLOCK_ENTITIES.register("rift",
            () -> new BlockEntityType<>(RiftBlockEntity::new, RIFT.get()));

    /** registerModEntity(MobObelisk, "Monolith", 72, 70, 1, true): 3 by 9. */
    public static final DeferredHolder<EntityType<?>, EntityType<Monolith>> MONOLITH = ENTITIES.registerEntityType("monolith", Monolith::new,
            MobCategory.MONSTER, b -> b.sized(3.0F, 9.0F).fireImmune().clientTrackingRange(5).updateInterval(1));

    public static final DeferredItem<DimDoorItem> DIMENSIONAL_DOOR_ITEM = ITEMS.registerItem("dimensional_door",
            p -> new DimDoorItem(DIMENSIONAL_DOOR, p, "Place on the block under a rift", "to activate that rift or place",
                    "anywhere else to create a", "pocket dimension."));
    public static final DeferredItem<DimDoorItem> WARP_DOOR_ITEM = ITEMS.registerItem("warp_door",
            p -> new DimDoorItem(WARP_DOOR, p, "Place on the block under a rift", "in any dimension,", "or place anywhere in pocket dim",
                    "to approach surface"));
    public static final DeferredItem<DimDoorItem> UNSTABLE_DOOR_ITEM = ITEMS.registerItem("unstable_door",
            p -> new DimDoorItem(UNSTABLE_DOOR, p, "Caution: Leads to random destination"));
    public static final DeferredItem<RiftSignatureItem> RIFT_SIGNATURE = ITEMS.registerItem("rift_signature",
            p -> new RiftSignatureItem(false, p), p -> p.stacksTo(1));
    public static final DeferredItem<RiftSignatureItem> STABILIZED_RIFT_SIGNATURE = ITEMS.registerItem("stabilized_rift_signature",
            p -> new RiftSignatureItem(true, p), p -> p.stacksTo(1));
    public static final DeferredItem<RiftRemoverItem> RIFT_REMOVER = ITEMS.registerItem("rift_remover", RiftRemoverItem::new, p -> p.durability(5));
    public static final DeferredItem<Item> STABLE_FABRIC = ITEMS.registerItem("stable_fabric", Item::new);
    /** ItemSword of EnumToolMaterial.GOLD with 500 uses and 7 damage; repaired with stable fabric. */
    public static final DeferredItem<RiftBladeItem> RIFT_BLADE = ITEMS.registerItem("rift_blade", RiftBladeItem::new, p -> {
        TagKey<Item> repair = TagKey.create(Registries.ITEM, id("repairs/rift_blade"));
        ToolMaterial gold = new ToolMaterial(BlockTags.INCORRECT_FOR_GOLD_TOOL, 500, 1.0F, 0.0F, 22, repair);
        var blocks = BuiltInRegistries.acquireBootstrapRegistrationLookup(BuiltInRegistries.BLOCK);
        return p.sword(gold, 6.0F, -2.4F)
                .component(DataComponents.TOOL, new Tool(List.of(
                        Tool.Rule.minesAndDrops(HolderSet.direct(Blocks.COBWEB.builtInRegistryHolder()), 15.0F),
                        Tool.Rule.overrideSpeed(blocks.getOrThrow(BlockTags.SWORD_EFFICIENT), 1.5F)), 1.0F, 2, false))
                .component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    });
    public static final DeferredItem<?> TRAPDOOR_ITEM = ITEMS.registerSimpleBlockItem(TRANSDIMENSIONAL_TRAPDOOR);
    public static final DeferredItem<?> FABRIC_ITEM = ITEMS.registerSimpleBlockItem(FABRIC);
    public static final DeferredItem<?> ANCIENT_FABRIC_ITEM = ITEMS.registerSimpleBlockItem(ANCIENT_FABRIC);
    public static final DeferredItem<?> ETERNAL_FABRIC_ITEM = ITEMS.registerSimpleBlockItem(ETERNAL_FABRIC);
    public static final DeferredItem<?> UNRAVELED_FABRIC_ITEM = ITEMS.registerSimpleBlockItem(UNRAVELED_FABRIC);
    public static final DeferredItem<SpawnEggItem> MONOLITH_SPAWN_EGG = ITEMS.registerItem("monolith_spawn_egg", SpawnEggItem::new,
            p -> p.spawnEgg(MONOLITH.get()));

    public static final DeferredHolder<SoundEvent, SoundEvent> MONK = sound("monk");
    public static final DeferredHolder<SoundEvent, SoundEvent> CRACK = sound("crack");
    public static final DeferredHolder<SoundEvent, SoundEvent> TEARING = sound("tearing");
    public static final DeferredHolder<SoundEvent, SoundEvent> RIFT_SOUND = sound("rift");
    public static final DeferredHolder<SoundEvent, SoundEvent> RIFT_START = sound("rift_start");
    public static final DeferredHolder<SoundEvent, SoundEvent> RIFT_END = sound("rift_end");
    public static final DeferredHolder<SoundEvent, SoundEvent> RIFT_CLOSE = sound("rift_close");
    public static final DeferredHolder<SoundEvent, SoundEvent> RIFT_DOOR = sound("rift_door");

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TAB = TABS.register("dimdoors", () -> CreativeModeTab.builder()
            .title(Component.literal("Dimensional Doors"))
            .icon(() -> new ItemStack(DIMENSIONAL_DOOR_ITEM.get()))
            .displayItems((params, output) -> {
                output.accept(DIMENSIONAL_DOOR_ITEM.get());
                output.accept(WARP_DOOR_ITEM.get());
                output.accept(UNSTABLE_DOOR_ITEM.get());
                output.accept(TRAPDOOR_ITEM.get());
                output.accept(RIFT_SIGNATURE.get());
                output.accept(STABILIZED_RIFT_SIGNATURE.get());
                output.accept(RIFT_REMOVER.get());
                output.accept(RIFT_BLADE.get());
                output.accept(STABLE_FABRIC.get());
                output.accept(FABRIC_ITEM.get());
                output.accept(ANCIENT_FABRIC_ITEM.get());
                output.accept(ETERNAL_FABRIC_ITEM.get());
                output.accept(UNRAVELED_FABRIC_ITEM.get());
                output.accept(MONOLITH_SPAWN_EGG.get());
            }).build());

    public DimDoors(IEventBus modBus) {
        RiftGen.register();
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        BLOCK_ENTITIES.register(modBus);
        ENTITIES.register(modBus);
        SOUNDS.register(modBus);
        TABS.register(modBus);
        GENERATORS.register("limbo", () -> LimboChunkGenerator.CODEC);
        GENERATORS.register(modBus);
        modBus.addListener(EntityAttributeCreationEvent.class, event -> event.put(MONOLITH.get(), Monolith.createAttributes().build()));
        registerOldBlocks();
        Dungeons.initialize(FMLPaths.CONFIGDIR.get().resolve("DimDoors_Custom_schematics").toFile());
        DDEvents.register(NeoForge.EVENT_BUS);
        if (FMLEnvironment.getDist() == Dist.CLIENT) {
            dimdoors.client.DDClient.register(modBus);
        }
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    private static DeferredHolder<SoundEvent, SoundEvent> sound(String name) {
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(id(name)));
    }

    /** The ids of the pack's DimDoors.cfg, for worldgen152 and the dungeon schematics. */
    private static void registerOldBlocks() {
        OldBlockStates.registerMeta(RiftGen.DIMENSIONAL_DOOR, meta -> DimDoorBlock.state(DIMENSIONAL_DOOR.get(), meta, false));
        OldBlockStates.registerMeta(RiftGen.WARP_DOOR, meta -> DimDoorBlock.state(WARP_DOOR.get(), meta, false));
        OldBlockStates.registerMeta(RiftGen.UNSTABLE_DOOR, meta -> DimDoorBlock.state(UNSTABLE_DOOR.get(), meta, false));
        OldBlockStates.registerMeta(RiftGen.TRANSIENT_DOOR, meta -> DimDoorBlock.state(TRANSIENT_DOOR.get(), meta, false));
        OldBlockStates.registerMeta(RiftGen.FABRIC, meta -> (meta == 1 ? ANCIENT_FABRIC : FABRIC).get().defaultBlockState());
        OldBlockStates.register(RiftGen.RIFT, () -> RIFT.get().defaultBlockState());
        OldBlockStates.register(RiftGen.UNRAVELED_FABRIC, () -> UNRAVELED_FABRIC.get().defaultBlockState());
        OldBlockStates.register(RiftGen.ETERNAL_FABRIC, () -> ETERNAL_FABRIC.get().defaultBlockState());
        OldGameIds.registerItem(RiftGen.FABRIC, id("fabric_of_reality").toString());
        OldGameIds.registerItem(RiftGen.TRANSDIMENSIONAL_TRAPDOOR, id("transdimensional_trapdoor").toString());
        OldGameIds.registerItem(DDLoot.DIMENSIONAL_DOOR_ITEM, id("dimensional_door").toString());
        OldGameIds.registerItem(DDLoot.WARP_DOOR_ITEM, id("warp_door").toString());
        OldGameIds.registerItem(DDLoot.UNSTABLE_DOOR_ITEM, id("unstable_door").toString());
        OldGameIds.registerItem(DDLoot.RIFT_SIGNATURE, id("rift_signature").toString());
        OldGameIds.registerItem(DDLoot.STABILIZED_RIFT_SIGNATURE, id("stabilized_rift_signature").toString());
        OldGameIds.registerItem(DDLoot.RIFT_REMOVER, id("rift_remover").toString());
        OldGameIds.registerItem(DDLoot.RIFT_BLADE, id("rift_blade").toString());
        OldGameIds.registerItem(DDLoot.STABLE_FABRIC, id("stable_fabric").toString());
        OldGameIds.registerEntity("Monolith", id("monolith").toString());
    }

    /** mod_pocketDim.blocksImmuneToRift. */
    public static boolean immuneToRift(BlockState state) {
        Block block = state.getBlock();
        return block == FABRIC.get() || block == ANCIENT_FABRIC.get() || block == ETERNAL_FABRIC.get() || block instanceof DimDoorBlock
                || block == TRANSDIMENSIONAL_TRAPDOOR.get() || block == RIFT.get() || block == Blocks.IRON_BLOCK || block == Blocks.DIAMOND_BLOCK
                || block == Blocks.EMERALD_BLOCK || block == Blocks.GOLD_BLOCK || block == Blocks.LAPIS_BLOCK || block == Blocks.BEDROCK;
    }

    static Function<Item.Properties, Item.Properties> none() {
        return p -> p;
    }
}
