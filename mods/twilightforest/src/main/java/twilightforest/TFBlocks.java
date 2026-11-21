package twilightforest;

import hexxit.worldgen152.OldBlockStates;
import hexxit.worldgen152.OldGameIds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import twilightforest.block.CritterBlock;
import twilightforest.block.CritterBlockEntity;
import twilightforest.block.FireJetBlock;
import twilightforest.block.FireJetBlockEntity;
import twilightforest.block.FireflyJarBlock;
import twilightforest.block.MagicLogSpecialBlock;
import twilightforest.block.MazestoneBlock;
import twilightforest.block.NagastoneBlock;
import twilightforest.block.TFLeavesBlock;
import twilightforest.block.TFPlantBlock;
import twilightforest.block.TFSaplingBlock;
import twilightforest.block.TowerWoodBlock;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

/**
 * The blocks and items of Teil 24b (TFBlocks of 1.5.2 without portal, hedge, boss spawners, uncrafting table, tower
 * devices and trophies). 1.5.2 kept the variants as metadata of one block id; here every variant is a block of its
 * own, the comments give id:meta of the pack's TwilightForest.cfg.
 */
public final class TFBlocks {
    static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(TwilightForest.MOD_ID);
    static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(TwilightForest.MOD_ID);
    static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, TwilightForest.MOD_ID);
    static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, TwilightForest.MOD_ID);
    static final DeferredRegister<ParticleType<?>> PARTICLES = DeferredRegister.create(Registries.PARTICLE_TYPE, TwilightForest.MOD_ID);
    /** Creative tab order = order of registration. */
    static final List<Supplier<ItemStack>> TAB_ITEMS = new ArrayList<>();

    // pack config block ids (TwilightForest.cfg, section block)
    public static final int ID_CICADA = 1352;
    public static final int ID_FIREFLY = 1353;
    public static final int ID_FIRE_JET = 1354;
    public static final int ID_FIREFLY_JAR = 1355;
    public static final int ID_HEDGE = 1356;
    public static final int ID_LEAVES = 1357;
    public static final int ID_LOG = 1358;
    public static final int ID_MAGIC_LEAVES = 1359;
    public static final int ID_MAGIC_LOG = 1360;
    public static final int ID_MAGIC_LOG_SPECIAL = 1361;
    public static final int ID_MAZESTONE = 1362;
    public static final int ID_MOONWORM = 1363;
    public static final int ID_NAGASTONE = 1364;
    public static final int ID_PLANT = 1365;
    public static final int ID_ROOTS = 1367;
    public static final int ID_SAPLING = 1368;
    public static final int ID_TOWER_WOOD = 1372;
    // item ids = config value + 256
    public static final int ID_CHARM_OF_KEEPING_1 = 7254 + 256;
    public static final int ID_CHARM_OF_LIFE_1 = 7257 + 256;
    public static final int ID_LIVEROOT = 7288 + 256;
    public static final int ID_TORCHBERRIES = 7320 + 256;

    /**
     * What a TF sapling grows into (BlockTFSapling.growTree). Called after the sapling was removed; returns true if a
     * tree grew, otherwise the sapling is put back. {@code meta} is the sapling metadata of 1.5.2 (0..9). The 1.5.2
     * generators are plugged in by the world generation (worldgen152 OldLiveGen).
     */
    @FunctionalInterface
    public interface TreeGrower {
        boolean grow(ServerLevel level, BlockPos pos, int meta, Random random);
    }

    public static TreeGrower GROWER = (level, pos, meta, random) -> false;

    private TFBlocks() {
    }

    // ---------------------------------------------------------------- helpers

    private static <B extends Block> DeferredBlock<B> block(String name, Function<BlockBehaviour.Properties, B> factory,
            UnaryOperator<BlockBehaviour.Properties> properties, UnaryOperator<Item.Properties> itemProperties) {
        DeferredBlock<B> block = BLOCKS.registerBlock(name, factory, properties);
        DeferredItem<BlockItem> item = ITEMS.registerSimpleBlockItem(block, itemProperties);
        TAB_ITEMS.add(() -> new ItemStack(item.get()));
        return block;
    }

    private static <B extends Block> DeferredBlock<B> block(String name, Function<BlockBehaviour.Properties, B> factory,
            UnaryOperator<BlockBehaviour.Properties> properties) {
        return block(name, factory, properties, p -> p);
    }

    private static DeferredItem<Item> item(String name) {
        DeferredItem<Item> item = ITEMS.registerSimpleItem(name, p -> p.rarity(net.minecraft.world.item.Rarity.UNCOMMON));
        TAB_ITEMS.add(() -> new ItemStack(item.get()));
        return item;
    }

    /** TileEntityFurnace: every ItemBlock of Material.wood burns 300 ticks. */
    private static final UnaryOperator<Item.Properties> WOOD_FUEL = p -> p.cookingFuel(ContextIntProviders.COOKING_TIME_WOOD_BLOCKS);

    /** Material.wood with the given hardness; resistance as 1.5.2 (setResistance(r) = r * 0.6). */
    private static UnaryOperator<BlockBehaviour.Properties> wood(float hardness, float resistance, boolean flammable) {
        return p -> {
            BlockBehaviour.Properties q = p.mapColor(MapColor.WOOD).instrument(NoteBlockInstrument.BASS).strength(hardness, resistance).sound(SoundType.WOOD);
            return flammable ? q.ignitedByLava() : q;
        };
    }

    /** Material.leaves: hardness 0.2, grass sound, see-through, random ticks for decay. */
    private static UnaryOperator<BlockBehaviour.Properties> leaves(boolean flammable) {
        return p -> {
            BlockBehaviour.Properties q = p.mapColor(MapColor.PLANT).strength(0.2F).randomTicks().sound(SoundType.GRASS).noOcclusion()
                    .isValidSpawn(Blocks::ocelotOrParrot).isSuffocating((s, l, b) -> false).isViewBlocking((s, l, b, e) -> false)
                    .pushReaction(PushReaction.POPPED).isRedstoneConductor((s, l, b) -> false);
            return flammable ? q.ignitedByLava() : q;
        };
    }

    /** Material.plants: no collision, breaks at once. */
    private static UnaryOperator<BlockBehaviour.Properties> plant() {
        return p -> p.mapColor(MapColor.PLANT).noCollision().instabreak().sound(SoundType.GRASS).pushReaction(PushReaction.POPPED);
    }

    /** Material.rock: needs a pickaxe to drop. */
    private static UnaryOperator<BlockBehaviour.Properties> rock(float hardness, float resistance, SoundType sound) {
        return p -> p.mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).strength(hardness, resistance).sound(sound)
                .requiresCorrectToolForDrops();
    }

    /** StepSoundTFInsect("squish", 0.25, 0.6): mob.slime.big for breaking, stepping and placing. */
    public static final SoundType INSECT = new SoundType(0.25F, 0.6F, net.minecraft.sounds.SoundEvents.SLIME_SQUISH,
            net.minecraft.sounds.SoundEvents.SLIME_SQUISH, net.minecraft.sounds.SoundEvents.SLIME_SQUISH,
            net.minecraft.sounds.SoundEvents.SLIME_SQUISH, net.minecraft.sounds.SoundEvents.SLIME_SQUISH);

    /** Material.circuits critters: no collision, no hardness, insect sound. */
    private static UnaryOperator<BlockBehaviour.Properties> critter(int light) {
        return p -> p.mapColor(MapColor.NONE).noCollision().instabreak().sound(INSECT).noOcclusion().lightLevel(s -> light)
                .pushReaction(PushReaction.POPPED);
    }

    // ---------------------------------------------------------------- logs (1358)
    // meta & 3 = type, & 12 = axis (0 y, 4 x, 8 z, 12 bark all round)
    public static final DeferredBlock<RotatedPillarBlock> TWILIGHT_OAK_LOG = block("twilight_oak_log", RotatedPillarBlock::new, wood(2.0F, 2.0F, true), WOOD_FUEL);
    public static final DeferredBlock<RotatedPillarBlock> CANOPY_LOG = block("canopy_log", RotatedPillarBlock::new, wood(2.0F, 2.0F, true), WOOD_FUEL);
    public static final DeferredBlock<RotatedPillarBlock> MANGROVE_LOG = block("mangrove_log", RotatedPillarBlock::new, wood(2.0F, 2.0F, true), WOOD_FUEL);
    public static final DeferredBlock<RotatedPillarBlock> DARKWOOD_LOG = block("darkwood_log", RotatedPillarBlock::new, wood(2.0F, 2.0F, true), WOOD_FUEL);
    public static final DeferredBlock<RotatedPillarBlock> TWILIGHT_OAK_WOOD = block("twilight_oak_wood", RotatedPillarBlock::new, wood(2.0F, 2.0F, true), WOOD_FUEL);
    public static final DeferredBlock<RotatedPillarBlock> CANOPY_WOOD = block("canopy_wood", RotatedPillarBlock::new, wood(2.0F, 2.0F, true), WOOD_FUEL);
    public static final DeferredBlock<RotatedPillarBlock> MANGROVE_WOOD = block("mangrove_wood", RotatedPillarBlock::new, wood(2.0F, 2.0F, true), WOOD_FUEL);
    public static final DeferredBlock<RotatedPillarBlock> DARKWOOD_WOOD = block("darkwood_wood", RotatedPillarBlock::new, wood(2.0F, 2.0F, true), WOOD_FUEL);

    // ---------------------------------------------------------------- leaves (1357)
    public static final DeferredBlock<TFLeavesBlock> TWILIGHT_OAK_LEAVES = block("twilight_oak_leaves", p -> new TFLeavesBlock(TFLeavesBlock.Kind.PLAIN, p), leaves(true));
    public static final DeferredBlock<TFLeavesBlock> CANOPY_LEAVES = block("canopy_leaves", p -> new TFLeavesBlock(TFLeavesBlock.Kind.PLAIN, p), leaves(true));
    public static final DeferredBlock<TFLeavesBlock> MANGROVE_LEAVES = block("mangrove_leaves", p -> new TFLeavesBlock(TFLeavesBlock.Kind.PLAIN, p), leaves(true));
    public static final DeferredBlock<TFLeavesBlock> RAINBOW_OAK_LEAVES = block("rainbow_oak_leaves", p -> new TFLeavesBlock(TFLeavesBlock.Kind.PLAIN, p), leaves(true));

    // ---------------------------------------------------------------- saplings (1368)
    public static final List<DeferredBlock<TFSaplingBlock>> SAPLINGS = new ArrayList<>();
    private static final String[] SAPLING_NAMES = {"twilight_oak_sapling", "canopy_sapling", "mangrove_sapling", "darkwood_sapling",
            "hollow_oak_sapling", "time_sapling", "transformation_sapling", "mining_sapling", "sorting_sapling", "rainbow_oak_sapling"};

    static {
        for (int i = 0; i < SAPLING_NAMES.length; i++) {
            int meta = i;
            SAPLINGS.add(block(SAPLING_NAMES[i], p -> new TFSaplingBlock(meta, p), p -> plant().apply(p).randomTicks()));
        }
    }

    // ---------------------------------------------------------------- plants (1365)
    public static final DeferredBlock<TFPlantBlock> MOSS_PATCH = plant("moss_patch", TFPlantBlock.Kind.MOSS_PATCH);
    public static final DeferredBlock<TFPlantBlock> MAYAPPLE = plant("mayapple", TFPlantBlock.Kind.MAYAPPLE);
    public static final DeferredBlock<TFPlantBlock> CLOVER_PATCH = plant("clover_patch", TFPlantBlock.Kind.CLOVER_PATCH);
    public static final DeferredBlock<TFPlantBlock> FIDDLEHEAD = plant("fiddlehead", TFPlantBlock.Kind.FIDDLEHEAD);
    public static final DeferredBlock<TFPlantBlock> MUSHGLOOM = plant("mushgloom", TFPlantBlock.Kind.MUSHGLOOM);
    public static final DeferredBlock<TFPlantBlock> TORCHBERRY_PLANT = plant("torchberry_plant", TFPlantBlock.Kind.TORCHBERRY);
    public static final DeferredBlock<TFPlantBlock> ROOT_STRAND = plant("root_strand", TFPlantBlock.Kind.ROOT_STRAND);

    private static DeferredBlock<TFPlantBlock> plant(String name, TFPlantBlock.Kind kind) {
        return block(name, p -> new TFPlantBlock(kind, p), p -> plant().apply(p).lightLevel(s -> kind.light));
    }

    // ---------------------------------------------------------------- roots (1367)
    public static final DeferredBlock<Block> ROOT = block("root", Block::new, wood(2.0F, 2.0F, false), WOOD_FUEL);
    public static final DeferredBlock<Block> LIVEROOT_BLOCK = block("liveroot_block", Block::new, wood(2.0F, 2.0F, false), WOOD_FUEL);

    // ---------------------------------------------------------------- critters (1353, 1352, 1363) and the jar (1355)
    public static final DeferredBlock<CritterBlock> FIREFLY = block("firefly", p -> new CritterBlock(CritterBlock.Kind.FIREFLY, p), critter(15));
    public static final DeferredBlock<CritterBlock> CICADA = block("cicada", p -> new CritterBlock(CritterBlock.Kind.CICADA, p), critter(0));
    public static final DeferredBlock<CritterBlock> MOONWORM = block("moonworm", p -> new CritterBlock(CritterBlock.Kind.MOONWORM, p),
            p -> critter(14).apply(p).noLootTable());
    /** Material.glass, hardness 0.3, wood step sound, light 8. */
    public static final DeferredBlock<FireflyJarBlock> FIREFLY_JAR = block("firefly_jar", FireflyJarBlock::new,
            p -> p.mapColor(MapColor.NONE).strength(0.3F).sound(SoundType.WOOD).noOcclusion().lightLevel(s -> 8)
                    .isRedstoneConductor((s, l, b) -> false).isSuffocating((s, l, b) -> false).isViewBlocking((s, l, b, e) -> false));

    // ---------------------------------------------------------------- fire jets (1354)
    public static final DeferredBlock<FireJetBlock> SMOKER = fireJet("smoker", FireJetBlock.Kind.SMOKER);
    public static final DeferredBlock<FireJetBlock> FIRE_JET = fireJet("fire_jet", FireJetBlock.Kind.FIRE_JET);
    public static final DeferredBlock<FireJetBlock> ENCASED_SMOKER = fireJet("encased_smoker", FireJetBlock.Kind.ENCASED_SMOKER);
    public static final DeferredBlock<FireJetBlock> ENCASED_FIRE_JET = fireJet("encased_fire_jet", FireJetBlock.Kind.ENCASED_FIRE_JET);

    /** Material.rock, hardness 1.5, resistance 10, wood step sound, light 15 while flaming. */
    private static DeferredBlock<FireJetBlock> fireJet(String name, FireJetBlock.Kind kind) {
        return block(name, p -> new FireJetBlock(kind, p), p -> rock(1.5F, 6.0F, SoundType.WOOD).apply(p).randomTicks()
                .lightLevel(s -> s.hasProperty(FireJetBlock.PHASE) && s.getValue(FireJetBlock.PHASE) == FireJetBlock.Phase.FLAME ? 15 : 0));
    }

    // ---------------------------------------------------------------- maze stone (1362)
    public static final List<DeferredBlock<MazestoneBlock>> MAZESTONES = new ArrayList<>();
    private static final String[] MAZESTONE_NAMES = {"maze_stone", "maze_stone_brick", "maze_stone_pillar", "maze_stone_decorative",
            "maze_stone_cracked", "maze_stone_mossy", "maze_stone_mosaic", "maze_stone_border"};

    static {
        for (String name : MAZESTONE_NAMES) {
            MAZESTONES.add(block(name, MazestoneBlock::new, rock(100.0F, 3.0F, SoundType.STONE)));
        }
    }

    // ---------------------------------------------------------------- naga stone (1364)
    public static final DeferredBlock<NagastoneBlock> NAGASTONE_HEAD = block("nagastone_head", p -> new NagastoneBlock(true, p), rock(1.5F, 6.0F, SoundType.STONE));
    public static final DeferredBlock<NagastoneBlock> NAGASTONE_BODY = block("nagastone_body", p -> new NagastoneBlock(false, p), rock(1.5F, 6.0F, SoundType.STONE));

    // ---------------------------------------------------------------- boss spawners (1351)
    public static final int ID_BOSS_SPAWNER = 1351;
    /**
     * BlockTFBossSpawner by metadata (0 naga, 1 lich, 2 hydra, 3 tower): hardness 20, resistance 10, drops nothing. Teil 24d
     * places them in the buildings; they summon their boss with Teil 24f.
     */
    public static final List<DeferredBlock<Block>> BOSS_SPAWNERS = List.of(bossSpawner("naga_boss_spawner"),
            bossSpawner("lich_boss_spawner"), bossSpawner("hydra_boss_spawner"), bossSpawner("tower_boss_spawner"));

    private static DeferredBlock<Block> bossSpawner(String name) {
        return block(name, Block::new, p -> p.mapColor(MapColor.STONE).strength(20.0F, 10.0F).sound(SoundType.METAL).noOcclusion().noLootTable()
                .requiresCorrectToolForDrops());
    }

    // ---------------------------------------------------------------- tower wood (1372)
    /** Material.wood, hardness 40, resistance 10; flammability 1 (data map), spreads no fire. */
    public static final DeferredBlock<Block> TOWER_WOOD = block("tower_wood", Block::new, wood(40.0F, 6.0F, true), WOOD_FUEL);
    public static final DeferredBlock<Block> ENCASED_TOWER_WOOD = block("encased_tower_wood", Block::new, wood(40.0F, 6.0F, true), WOOD_FUEL);
    public static final DeferredBlock<Block> TOWER_WOOD_CRACKED = block("tower_wood_cracked", Block::new, wood(40.0F, 6.0F, true), WOOD_FUEL);
    public static final DeferredBlock<Block> TOWER_WOOD_MOSSY = block("tower_wood_mossy", Block::new, wood(40.0F, 6.0F, true), WOOD_FUEL);
    /** Metadata 4: hardness 0.75, drops nothing, a termite comes out (Teil 24c, {@link TowerWoodBlock#TERMITE}). */
    public static final DeferredBlock<TowerWoodBlock> TOWER_WOOD_INFESTED = block("tower_wood_infested", TowerWoodBlock::new,
            p -> wood(0.75F, 6.0F, true).apply(p).noLootTable(), WOOD_FUEL);

    // ---------------------------------------------------------------- magic trees (1360, 1359, 1361)
    public static final DeferredBlock<RotatedPillarBlock> TIME_LOG = block("time_log", RotatedPillarBlock::new, wood(2.0F, 2.0F, false), WOOD_FUEL);
    public static final DeferredBlock<RotatedPillarBlock> TRANSFORMATION_LOG = block("transformation_log", RotatedPillarBlock::new, wood(2.0F, 2.0F, false), WOOD_FUEL);
    public static final DeferredBlock<RotatedPillarBlock> MINING_LOG = block("mining_log", RotatedPillarBlock::new, wood(2.0F, 2.0F, false), WOOD_FUEL);
    public static final DeferredBlock<RotatedPillarBlock> SORTING_LOG = block("sorting_log", RotatedPillarBlock::new, wood(2.0F, 2.0F, false), WOOD_FUEL);
    public static final DeferredBlock<RotatedPillarBlock> TIME_WOOD = block("time_wood", RotatedPillarBlock::new, wood(2.0F, 2.0F, false), WOOD_FUEL);
    public static final DeferredBlock<RotatedPillarBlock> TRANSFORMATION_WOOD = block("transformation_wood", RotatedPillarBlock::new, wood(2.0F, 2.0F, false), WOOD_FUEL);
    public static final DeferredBlock<RotatedPillarBlock> MINING_WOOD = block("mining_wood", RotatedPillarBlock::new, wood(2.0F, 2.0F, false), WOOD_FUEL);
    public static final DeferredBlock<RotatedPillarBlock> SORTING_WOOD = block("sorting_wood", RotatedPillarBlock::new, wood(2.0F, 2.0F, false), WOOD_FUEL);

    public static final DeferredBlock<TFLeavesBlock> TIME_LEAVES = block("time_leaves", p -> new TFLeavesBlock(TFLeavesBlock.Kind.MAGIC, p), leaves(false));
    public static final DeferredBlock<TFLeavesBlock> TRANSFORMATION_LEAVES = block("transformation_leaves", p -> new TFLeavesBlock(TFLeavesBlock.Kind.RUNES, p), leaves(false));
    public static final DeferredBlock<TFLeavesBlock> MINING_LEAVES = block("mining_leaves", p -> new TFLeavesBlock(TFLeavesBlock.Kind.MAGIC, p), leaves(false));
    public static final DeferredBlock<TFLeavesBlock> SORTING_LEAVES = block("sorting_leaves", p -> new TFLeavesBlock(TFLeavesBlock.Kind.MAGIC, p), leaves(false));

    public static final DeferredBlock<MagicLogSpecialBlock> TIME_CLOCK = magicCore("time_clock", MagicLogSpecialBlock.Effect.TIME);
    public static final DeferredBlock<MagicLogSpecialBlock> TRANSFORMATION_HEART = magicCore("transformation_heart", MagicLogSpecialBlock.Effect.TRANSFORMATION);
    public static final DeferredBlock<MagicLogSpecialBlock> MINING_CORE = magicCore("mining_core", MagicLogSpecialBlock.Effect.MINING);
    public static final DeferredBlock<MagicLogSpecialBlock> SORTING_ENGINE = magicCore("sorting_engine", MagicLogSpecialBlock.Effect.SORTING);

    /** BlockTFMagicLogSpecial: like the magic log, light 15, ticks every 20 ticks while active. */
    private static DeferredBlock<MagicLogSpecialBlock> magicCore(String name, MagicLogSpecialBlock.Effect effect) {
        return block(name, p -> new MagicLogSpecialBlock(effect, p), p -> wood(2.0F, 2.0F, false).apply(p).lightLevel(s -> 15), WOOD_FUEL);
    }

    // ---------------------------------------------------------------- items
    public static final DeferredItem<Item> LIVEROOT = item("liveroot");
    public static final DeferredItem<Item> TORCHBERRIES = item("torchberries");
    /** Function with Teil 24f (TFEventListener: saves the player from death). */
    public static final DeferredItem<Item> CHARM_OF_LIFE_1 = item("charm_of_life_1");
    /** Function with Teil 24f (TFEventListener: keeps armour and the held item on death). */
    public static final DeferredItem<Item> CHARM_OF_KEEPING_1 = item("charm_of_keeping_1");

    // ---------------------------------------------------------------- block entities, sounds, particles
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CritterBlockEntity>> CRITTER_ENTITY = BLOCK_ENTITIES.register("critter",
            () -> new BlockEntityType<>(CritterBlockEntity::new, Set.of(FIREFLY.get(), CICADA.get(), MOONWORM.get())));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FireJetBlockEntity>> FIRE_JET_ENTITY = BLOCK_ENTITIES.register("fire_jet",
            () -> new BlockEntityType<>(FireJetBlockEntity::new, Set.of(SMOKER.get(), FIRE_JET.get(), ENCASED_SMOKER.get(), ENCASED_FIRE_JET.get())));

    /** mob.tf.cicada (cicada2.ogg, cicada3.ogg). */
    public static final DeferredHolder<SoundEvent, SoundEvent> CICADA_SOUND = SOUNDS.register("cicada",
            () -> SoundEvent.createVariableRangeEvent(TwilightForest.id("cicada")));
    /** EntityTFTinyFirefly: the little glow the fireflies and the firefly jar give off. */
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> FIREFLY_PARTICLE = PARTICLES.register("firefly",
            () -> new SimpleParticleType(false));

    static void register(IEventBus modBus) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        BLOCK_ENTITIES.register(modBus);
        SOUNDS.register(modBus);
        PARTICLES.register(modBus);
    }

    // ---------------------------------------------------------------- old ids

    private static Direction.Axis axis(int meta) {
        return switch (meta & 12) {
            case 4 -> Direction.Axis.X;
            case 8 -> Direction.Axis.Z;
            default -> Direction.Axis.Y;
        };
    }

    @SafeVarargs
    private static BlockState pick(int index, Supplier<? extends Block>... blocks) {
        return blocks[index < blocks.length ? index : 0].get().defaultBlockState();
    }

    private static String name(Supplier<? extends Block> block) {
        return ((DeferredBlock<?>) block).getId().toString();
    }

    private static void items(int id, int meta, Supplier<? extends Block> block) {
        OldGameIds.registerItem(id, meta, name(block));
    }

    /**
     * The block ids of the pack's TwilightForest.cfg for worldgen152, and the item ids of these blocks (block id, damage
     * = metadata) and of the TF items (config value + 256) for generated chests.
     */
    public static void registerOldBlocks() {
        List<Supplier<? extends Block>> logs = List.of(TWILIGHT_OAK_LOG, CANOPY_LOG, MANGROVE_LOG, DARKWOOD_LOG);
        List<Supplier<? extends Block>> woods = List.of(TWILIGHT_OAK_WOOD, CANOPY_WOOD, MANGROVE_WOOD, DARKWOOD_WOOD);
        OldBlockStates.registerMeta(ID_LOG, meta -> (meta & 12) == 12 ? woods.get(meta & 3).get().defaultBlockState()
                : logs.get(meta & 3).get().defaultBlockState().setValue(RotatedPillarBlock.AXIS, axis(meta)));
        List<Supplier<? extends Block>> magicLogs = List.of(TIME_LOG, TRANSFORMATION_LOG, MINING_LOG, SORTING_LOG);
        List<Supplier<? extends Block>> magicWoods = List.of(TIME_WOOD, TRANSFORMATION_WOOD, MINING_WOOD, SORTING_WOOD);
        OldBlockStates.registerMeta(ID_MAGIC_LOG, meta -> (meta & 12) == 12 ? magicWoods.get(meta & 3).get().defaultBlockState()
                : magicLogs.get(meta & 3).get().defaultBlockState().setValue(RotatedPillarBlock.AXIS, axis(meta)));
        for (int meta = 0; meta < 16; meta++) {
            items(ID_LOG, meta, ((meta & 12) == 12 ? woods : logs).get(meta & 3));
            items(ID_MAGIC_LOG, meta, ((meta & 12) == 12 ? magicWoods : magicLogs).get(meta & 3));
        }

        // leaves: not persistent, the chunk writer works out the distance
        List<Supplier<? extends Block>> leaves = List.of(TWILIGHT_OAK_LEAVES, CANOPY_LEAVES, MANGROVE_LEAVES, RAINBOW_OAK_LEAVES);
        List<Supplier<? extends Block>> magicLeaves = List.of(TIME_LEAVES, TRANSFORMATION_LEAVES, MINING_LEAVES, SORTING_LEAVES);
        OldBlockStates.registerMeta(ID_LEAVES, meta -> leaves.get(meta & 3).get().defaultBlockState());
        OldBlockStates.registerMeta(ID_MAGIC_LEAVES, meta -> magicLeaves.get(meta & 3).get().defaultBlockState());
        for (int meta = 0; meta < 16; meta++) {
            items(ID_LEAVES, meta, leaves.get(meta & 3));
            items(ID_MAGIC_LEAVES, meta, magicLeaves.get(meta & 3));
        }

        // magic log cores: meta & 3 the tree, meta & 12 == 12 switched off
        List<Supplier<? extends Block>> cores = List.of(TIME_CLOCK, TRANSFORMATION_HEART, MINING_CORE, SORTING_ENGINE);
        OldBlockStates.registerMeta(ID_MAGIC_LOG_SPECIAL, meta -> cores.get(meta & 3).get().defaultBlockState()
                .setValue(MagicLogSpecialBlock.ACTIVE, (meta & 12) != 12));
        for (int meta = 0; meta < 16; meta++) {
            items(ID_MAGIC_LOG_SPECIAL, meta, cores.get(meta & 3));
        }

        OldBlockStates.registerMeta(ID_SAPLING, meta -> SAPLINGS.get(meta < SAPLINGS.size() ? meta : 0).get().defaultBlockState());
        for (int meta = 0; meta < SAPLINGS.size(); meta++) {
            items(ID_SAPLING, meta, SAPLINGS.get(meta));
        }

        // plants: metas without a plant are air
        OldBlockStates.registerMeta(ID_PLANT, meta -> {
            Supplier<? extends Block> plant = plantByMeta(meta);
            return plant == null ? Blocks.AIR.defaultBlockState() : plant.get().defaultBlockState();
        });
        for (int meta = 0; meta < 16; meta++) {
            Supplier<? extends Block> plant = plantByMeta(meta);
            if (plant != null) {
                items(ID_PLANT, meta, plant);
            }
        }

        OldBlockStates.registerMeta(ID_ROOTS, meta -> pick(meta == 1 ? 1 : 0, ROOT, LIVEROOT_BLOCK));
        items(ID_ROOTS, 0, ROOT);
        items(ID_ROOTS, 1, LIVEROOT_BLOCK);

        // critters: 1 west wall .. 6 ceiling, 0 = find a support like onBlockAdded (the chunk writer updates the shape)
        critter(ID_FIREFLY, FIREFLY);
        critter(ID_CICADA, CICADA);
        critter(ID_MOONWORM, MOONWORM);
        OldBlockStates.register(ID_FIREFLY_JAR, () -> FIREFLY_JAR.get().defaultBlockState());
        OldGameIds.registerItem(ID_FIREFLY_JAR, name(FIREFLY_JAR));

        OldBlockStates.registerMeta(ID_FIRE_JET, FireJetBlock::fromMeta);
        // damageDropped: 0 smoker, 1/2 encased smoker, 11..13 encased fire jet, everything else a fire jet
        for (int meta = 0; meta < 16; meta++) {
            items(ID_FIRE_JET, meta, meta == 0 ? SMOKER : meta == 1 || meta == 2 ? ENCASED_SMOKER : meta >= 11 && meta <= 13 ? ENCASED_FIRE_JET : FIRE_JET);
        }

        OldBlockStates.registerMeta(ID_MAZESTONE, meta -> MAZESTONES.get(meta & 7).get().defaultBlockState());
        for (int meta = 0; meta < 16; meta++) {
            items(ID_MAZESTONE, meta, MAZESTONES.get(meta & 7));
        }

        OldBlockStates.registerMeta(ID_NAGASTONE, NagastoneBlock::fromMeta);
        for (int meta = 0; meta < 16; meta++) {
            items(ID_NAGASTONE, meta, meta < 4 ? NAGASTONE_HEAD : NAGASTONE_BODY);
        }

        OldBlockStates.registerMeta(ID_BOSS_SPAWNER, meta -> BOSS_SPAWNERS.get(meta < BOSS_SPAWNERS.size() ? meta : 0).get().defaultBlockState());
        for (int meta = 0; meta < BOSS_SPAWNERS.size(); meta++) {
            items(ID_BOSS_SPAWNER, meta, BOSS_SPAWNERS.get(meta));
        }
        List<Supplier<? extends Block>> tower = List.of(TOWER_WOOD, ENCASED_TOWER_WOOD, TOWER_WOOD_CRACKED, TOWER_WOOD_MOSSY, TOWER_WOOD_INFESTED);
        OldBlockStates.registerMeta(ID_TOWER_WOOD, meta -> tower.get(meta < tower.size() ? meta : 0).get().defaultBlockState());
        for (int meta = 0; meta < tower.size(); meta++) {
            items(ID_TOWER_WOOD, meta, tower.get(meta));
        }

        // the hedge block itself is mapped in TwilightForest.registerOldBlocks; its items
        OldGameIds.registerItem(ID_HEDGE, 0, "twilightforest:hedge");
        OldGameIds.registerItem(ID_HEDGE, 1, "twilightforest:darkwood_leaves");

        OldGameIds.registerItem(ID_LIVEROOT, name(LIVEROOT));
        OldGameIds.registerItem(ID_TORCHBERRIES, name(TORCHBERRIES));
        OldGameIds.registerItem(ID_CHARM_OF_LIFE_1, name(CHARM_OF_LIFE_1));
        OldGameIds.registerItem(ID_CHARM_OF_KEEPING_1, name(CHARM_OF_KEEPING_1));
    }

    private static String name(DeferredItem<?> item) {
        return item.getId().toString();
    }

    private static void critter(int id, DeferredBlock<CritterBlock> block) {
        OldBlockStates.registerMeta(id, meta -> block.get().defaultBlockState().setValue(CritterBlock.FACING, CritterBlock.facing(meta)));
        OldBlockStates.registerConnecting(block);
        OldGameIds.registerItem(id, name(block));
    }

    /** BlockTFPlant metadata: 3 moss, 4 mayapple, 5 clover, 8 fiddlehead, 9 mushgloom, 13 torchberry, 14 root strand. */
    public static Supplier<? extends Block> plantByMeta(int meta) {
        return switch (meta) {
            case 3 -> MOSS_PATCH;
            case 4 -> MAYAPPLE;
            case 5 -> CLOVER_PATCH;
            case 8 -> FIDDLEHEAD;
            case 9 -> MUSHGLOOM;
            case 13 -> TORCHBERRY_PLANT;
            case 14 -> ROOT_STRAND;
            default -> null;
        };
    }
}
