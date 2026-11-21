package twilightforest;

import hexxit.worldgen152.OldBlockStates;
import hexxit.worldgen152.OldGameIds;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import twilightforest.block.GhastTrapBlockEntity;
import twilightforest.block.ReactorBlockEntity;
import twilightforest.block.ReverterBlockEntity;
import twilightforest.block.TowerBuilderBlockEntity;
import twilightforest.block.TowerDeviceBlock;
import twilightforest.block.TowerTranslucentBlock;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Teil 24e: the devices of the dark tower (BlockTFTowerDevice 1370, BlockTFTowerTranslucent 1371), their tile
 * entities, and the tower key (item 7321).
 */
public final class TFTower {
    static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(TwilightForest.MOD_ID);
    static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(TwilightForest.MOD_ID);
    static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, TwilightForest.MOD_ID);
    static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, TwilightForest.MOD_ID);
    static final List<Supplier<ItemStack>> TAB_ITEMS = new ArrayList<>();

    public static final int ID_TOWER_DEVICE = 1370;
    public static final int ID_TOWER_TRANSLUCENT = 1371;
    public static final int ID_TOWER_KEY = 7321 + 256;

    /** Names by metadata of BlockTFTowerDevice. */
    public static final String[] DEVICE_NAMES = {"reappearing_block", "reappearing_block_active", "vanishing_block", "vanishing_block_active",
            "locked_vanishing_block", "unlocked_vanishing_block", "carminite_builder", "carminite_builder_active", "carminite_builder_timeout",
            "antibuilder", "ghast_trap", "ghast_trap_active", "carminite_reactor", "carminite_reactor_active"};
    /** getSubBlocks: the creative tab shows these. */
    private static final Set<Integer> DEVICE_TAB = Set.of(0, 2, 4, 5, 6, 9, 10, 12);
    /** Names by metadata of BlockTFTowerTranslucent. */
    public static final String[] TRANSLUCENT_NAMES = {"reappearing_trace", "reappearing_trace_active", "built_block", "built_block_active",
            "antibuilt_block", "reactor_debris", "fake_gold", "fake_diamond"};

    public static final List<DeferredBlock<TowerDeviceBlock>> DEVICES = new ArrayList<>();
    public static final List<DeferredBlock<TowerTranslucentBlock>> TRANSLUCENTS = new ArrayList<>();

    static {
        for (int meta = 0; meta < DEVICE_NAMES.length; meta++) {
            int m = meta;
            DeferredBlock<TowerDeviceBlock> block = BLOCKS.registerBlock(DEVICE_NAMES[meta], p -> new TowerDeviceBlock(m, p),
                    p -> p.mapColor(MapColor.WOOD).instrument(NoteBlockInstrument.BASS).sound(SoundType.WOOD)
                            .strength(TowerDeviceBlock.hardness(m), TowerDeviceBlock.resistance(m)).lightLevel(s -> TowerDeviceBlock.light(m))
                            .ignitedByLava());
            DEVICES.add(block);
            if (DEVICE_TAB.contains(meta)) {
                DeferredItem<?> item = ITEMS.registerSimpleBlockItem(block);
                TAB_ITEMS.add(() -> new ItemStack(item.get()));
            }
        }
        for (int meta = 0; meta < TRANSLUCENT_NAMES.length; meta++) {
            int m = meta;
            TRANSLUCENTS.add(BLOCKS.registerBlock(TRANSLUCENT_NAMES[meta], p -> new TowerTranslucentBlock(m, p),
                    p -> p.mapColor(MapColor.NONE).sound(SoundType.METAL).strength(TowerTranslucentBlock.hardness(m), 1200.0F).noOcclusion()
                            .noLootTable().isRedstoneConductor((s, l, b) -> false).isSuffocating((s, l, b) -> false)
                            .isViewBlocking((s, l, b, e) -> false)));
        }
    }

    /** ItemTFTowerKey: unlocks a locked vanishing block and is used up. */
    public static final DeferredItem<Item> TOWER_KEY = ITEMS.registerItem("tower_key", p -> new Item(p) {
        @Override
        public InteractionResult useOn(UseOnContext context) {
            if (!TowerDeviceBlock.isDevice(context.getLevel().getBlockState(context.getClickedPos()), TowerDeviceBlock.VANISH_LOCKED)) {
                return InteractionResult.PASS;
            }
            if (!context.getLevel().isClientSide()) {
                TowerDeviceBlock.unlockBlock(context.getLevel(), context.getClickedPos());
                context.getItemInHand().shrink(1);
            }
            return InteractionResult.SUCCESS;
        }
    });

    /** Carminite (item 7253, rare): shown in the item frames of the dark tower; the recipe comes with Teil 24f. */
    public static final DeferredItem<Item> CARMINITE = ITEMS.registerItem("carminite", Item::new, p -> p.rarity(Rarity.RARE));
    public static final int ID_CARMINITE = 7253 + 256;

    static {
        TAB_ITEMS.add(() -> new ItemStack(TOWER_KEY.get()));
        TAB_ITEMS.add(() -> new ItemStack(CARMINITE.get()));
    }

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TowerBuilderBlockEntity>> BUILDER_ENTITY = BLOCK_ENTITIES.register(
            "carminite_builder", () -> new BlockEntityType<>(TowerBuilderBlockEntity::new, Set.of(DEVICES.get(TowerDeviceBlock.BUILDER_ACTIVE).get())));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ReverterBlockEntity>> REVERTER_ENTITY = BLOCK_ENTITIES.register(
            "antibuilder", () -> new BlockEntityType<>(ReverterBlockEntity::new, Set.of(DEVICES.get(TowerDeviceBlock.ANTIBUILDER).get())));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<GhastTrapBlockEntity>> GHAST_TRAP_ENTITY = BLOCK_ENTITIES.register(
            "ghast_trap", () -> new BlockEntityType<>(GhastTrapBlockEntity::new,
                    Set.of(DEVICES.get(TowerDeviceBlock.GHASTTRAP_INACTIVE).get(), DEVICES.get(TowerDeviceBlock.GHASTTRAP_ACTIVE).get())));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ReactorBlockEntity>> REACTOR_ENTITY = BLOCK_ENTITIES.register(
            "carminite_reactor", () -> new BlockEntityType<>(ReactorBlockEntity::new, Set.of(DEVICES.get(TowerDeviceBlock.REACTOR_ACTIVE).get())));

    /** mob.tf.urghast.trapwarmup, trapon (5 files), trapspindown. */
    public static final DeferredHolder<SoundEvent, SoundEvent> TRAP_WARMUP = sound("ghast_trap.warmup");
    public static final DeferredHolder<SoundEvent, SoundEvent> TRAP_ON = sound("ghast_trap.on");
    public static final DeferredHolder<SoundEvent, SoundEvent> TRAP_SPINDOWN = sound("ghast_trap.spindown");

    private TFTower() {
    }

    private static DeferredHolder<SoundEvent, SoundEvent> sound(String name) {
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(TwilightForest.id(name)));
    }

    static void register(IEventBus modBus) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        BLOCK_ENTITIES.register(modBus);
        SOUNDS.register(modBus);
    }

    /** Old ids: every metadata value is its block; items as damageDropped (the antibuilder drops nothing). */
    static void registerOldBlocks() {
        OldBlockStates.registerMeta(ID_TOWER_DEVICE, meta -> DEVICES.get(meta < DEVICES.size() ? meta : 0).get().defaultBlockState());
        OldBlockStates.registerMeta(ID_TOWER_TRANSLUCENT, meta -> TRANSLUCENTS.get(meta < TRANSLUCENTS.size() ? meta : 0).get().defaultBlockState());
        int[] dropped = {0, 0, 2, 2, 4, 5, 6, 6, 6, 9, 10, 10, 12, 12};
        for (int meta = 0; meta < dropped.length; meta++) {
            OldGameIds.registerItem(ID_TOWER_DEVICE, meta, DEVICES.get(dropped[meta]).getId().toString());
        }
        OldGameIds.registerItem(ID_TOWER_KEY, TOWER_KEY.getId().toString());
        OldGameIds.registerItem(ID_CARMINITE, CARMINITE.getId().toString());
    }
}
