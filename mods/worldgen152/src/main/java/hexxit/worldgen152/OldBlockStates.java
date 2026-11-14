package hexxit.worldgen152;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.SlabType;

import java.util.function.IntFunction;
import java.util.function.Supplier;

/**
 * Translates the numeric block ids and metadata the 1.5.2 generator writes into block states.
 * Mods whose blocks appear in generated chunks add theirs with {@link #register}; the same blocks need
 * their generation properties in {@code OldBlockInfo}.
 *
 * <p>States that depend on neighbours (leaf distance, snowy grass, the top face of vines) are finished by
 * {@link OldChunkWriter}.
 */
public final class OldBlockStates {
    private static final int MAX_ID = 4096;
    @SuppressWarnings("unchecked")
    private static final IntFunction<BlockState>[] MAPPERS = new IntFunction[MAX_ID];
    private static final BlockState[][] RESOLVED = new BlockState[MAX_ID][];

    private static final Direction[] CHEST_FACING = {Direction.NORTH, Direction.NORTH, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST};
    private static final Direction[] DIRECTION = {Direction.SOUTH, Direction.WEST, Direction.NORTH, Direction.EAST};

    static {
        block(1, Blocks.STONE);
        block(2, Blocks.GRASS_BLOCK);
        block(3, Blocks.DIRT);
        block(4, Blocks.COBBLESTONE);
        block(7, Blocks.BEDROCK);
        // 8/10 are the "flowing" ids: a source that should start to flow (see OldChunkWriter)
        block(8, Blocks.WATER);
        block(9, Blocks.WATER);
        block(10, Blocks.LAVA);
        block(11, Blocks.LAVA);
        block(12, Blocks.SAND);
        block(13, Blocks.GRAVEL);
        block(14, Blocks.GOLD_ORE);
        block(15, Blocks.IRON_ORE);
        block(16, Blocks.COAL_ORE);
        registerMeta(17, meta -> {
            Block[] logs = {Blocks.OAK_LOG, Blocks.SPRUCE_LOG, Blocks.BIRCH_LOG, Blocks.JUNGLE_LOG};
            Block[] wood = {Blocks.OAK_WOOD, Blocks.SPRUCE_WOOD, Blocks.BIRCH_WOOD, Blocks.JUNGLE_WOOD};
            return switch (meta & 12) {
                case 4 -> logs[meta & 3].defaultBlockState().setValue(BlockStateProperties.AXIS, Direction.Axis.X);
                case 8 -> logs[meta & 3].defaultBlockState().setValue(BlockStateProperties.AXIS, Direction.Axis.Z);
                case 12 -> wood[meta & 3].defaultBlockState();
                default -> logs[meta & 3].defaultBlockState();
            };
        });
        registerMeta(18, meta -> new Block[] {Blocks.OAK_LEAVES, Blocks.SPRUCE_LEAVES, Blocks.BIRCH_LEAVES, Blocks.JUNGLE_LEAVES}[meta & 3]
                .defaultBlockState());
        block(21, Blocks.LAPIS_ORE);
        block(24, Blocks.SANDSTONE);
        // tall grass: 0 was the dead shrub, 1 grass, 2 fern
        registerMeta(31, meta -> (meta == 2 ? Blocks.FERN : meta == 1 ? Blocks.SHORT_GRASS : Blocks.DEAD_BUSH).defaultBlockState());
        block(32, Blocks.DEAD_BUSH);
        block(37, Blocks.DANDELION);
        block(38, Blocks.POPPY);
        block(39, Blocks.BROWN_MUSHROOM);
        block(40, Blocks.RED_MUSHROOM);
        registerMeta(44, meta -> {
            Block[] slabs = {Blocks.SMOOTH_STONE_SLAB, Blocks.SANDSTONE_SLAB, Blocks.PETRIFIED_OAK_SLAB, Blocks.COBBLESTONE_SLAB,
                    Blocks.BRICK_SLAB, Blocks.STONE_BRICK_SLAB, Blocks.NETHER_BRICK_SLAB, Blocks.QUARTZ_SLAB};
            return slabs[meta & 7].defaultBlockState().setValue(BlockStateProperties.SLAB_TYPE, (meta & 8) != 0 ? SlabType.TOP : SlabType.BOTTOM);
        });
        block(48, Blocks.MOSSY_COBBLESTONE);
        block(49, Blocks.OBSIDIAN);
        block(52, Blocks.SPAWNER);
        registerMeta(54, meta -> Blocks.CHEST.defaultBlockState()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, CHEST_FACING[meta < 6 ? meta : 0]));
        block(56, Blocks.DIAMOND_ORE);
        block(73, Blocks.REDSTONE_ORE);
        registerMeta(78, meta -> Blocks.SNOW.defaultBlockState().setValue(BlockStateProperties.LAYERS, (meta & 7) + 1));
        block(79, Blocks.ICE);
        block(80, Blocks.SNOW_BLOCK);
        block(81, Blocks.CACTUS);
        block(82, Blocks.CLAY);
        block(83, Blocks.SUGAR_CANE);
        // every 1.5.2 pumpkin had a face; the metadata is where it looks
        registerMeta(86, meta -> Blocks.CARVED_PUMPKIN.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, DIRECTION[meta & 3]));
        block(97, Blocks.INFESTED_STONE);
        registerMeta(99, meta -> hugeMushroom(Blocks.BROWN_MUSHROOM_BLOCK, meta));
        registerMeta(100, meta -> hugeMushroom(Blocks.RED_MUSHROOM_BLOCK, meta));
        // vine metadata: bit 1 south, 2 west, 4 north, 8 east - the side of the block it hangs on
        registerMeta(106, meta -> Blocks.VINE.defaultBlockState()
                .setValue(BlockStateProperties.SOUTH, (meta & 1) != 0)
                .setValue(BlockStateProperties.WEST, (meta & 2) != 0)
                .setValue(BlockStateProperties.NORTH, (meta & 4) != 0)
                .setValue(BlockStateProperties.EAST, (meta & 8) != 0));
        block(110, Blocks.MYCELIUM);
        block(111, Blocks.LILY_PAD);
        registerMeta(127, meta -> Blocks.COCOA.defaultBlockState()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, DIRECTION[meta & 3])
                .setValue(BlockStateProperties.AGE_2, Math.min(meta >> 2, 2)));
        block(129, Blocks.EMERALD_ORE);
    }

    private OldBlockStates() {
    }

    private static void block(int oldId, Block block) {
        register(oldId, block::defaultBlockState);
    }

    /** Cap blocks: metadata 1-9 is the position in the cap (which sides show the cap), 10 the stem, 0 all pores. */
    private static BlockState hugeMushroom(Block cap, int meta) {
        if (meta == 10 || meta == 15) {
            return Blocks.MUSHROOM_STEM.defaultBlockState()
                    .setValue(BlockStateProperties.UP, meta == 15)
                    .setValue(BlockStateProperties.DOWN, meta == 15);
        }
        boolean all = meta == 14;
        boolean top = meta >= 1 && meta <= 9;
        return cap.defaultBlockState()
                .setValue(BlockStateProperties.UP, all || top)
                .setValue(BlockStateProperties.DOWN, all)
                .setValue(BlockStateProperties.NORTH, all || meta >= 1 && meta <= 3)
                .setValue(BlockStateProperties.SOUTH, all || meta >= 7 && meta <= 9)
                .setValue(BlockStateProperties.WEST, all || meta == 1 || meta == 4 || meta == 7)
                .setValue(BlockStateProperties.EAST, all || meta == 3 || meta == 6 || meta == 9);
    }

    /** Maps an old block id to one state, whatever its metadata. */
    public static void register(int oldId, Supplier<BlockState> state) {
        registerMeta(oldId, meta -> state.get());
    }

    /** Maps an old block id to a state per metadata value (0-15). The function runs once per value, on first use. */
    public static synchronized void registerMeta(int oldId, IntFunction<BlockState> states) {
        if (oldId <= 0 || oldId >= MAX_ID) {
            throw new IllegalArgumentException("Block ids must be 1.." + (MAX_ID - 1) + ", got " + oldId);
        }
        if (MAPPERS[oldId] != null) {
            throw new IllegalArgumentException("Block id " + oldId + " is already mapped");
        }
        MAPPERS[oldId] = states;
    }

    /** State for an old block id with metadata 0, or {@code null} for air. */
    public static BlockState get(int oldId) {
        return get(oldId, 0);
    }

    /** State for an old block id and metadata, or {@code null} for air. */
    public static BlockState get(int oldId, int meta) {
        if (oldId == 0) {
            return null;
        }
        BlockState[] states = RESOLVED[oldId];
        if (states == null) {
            states = new BlockState[16];
            RESOLVED[oldId] = states;
        }
        BlockState state = states[meta];
        if (state == null) {
            IntFunction<BlockState> mapper = MAPPERS[oldId];
            if (mapper == null) {
                throw new IllegalStateException("1.5.2 world generation uses block id " + oldId + ", which no mod has mapped to a block");
            }
            state = mapper.apply(meta);
            states[meta] = state;
        }
        return state;
    }
}
