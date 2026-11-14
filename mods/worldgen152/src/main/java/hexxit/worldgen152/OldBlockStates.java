package hexxit.worldgen152;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.level.block.state.properties.SlabType;

import java.util.function.IntFunction;
import java.util.function.Supplier;

/**
 * Translates the numeric block ids and metadata the 1.5.2 generator writes into block states.
 * Mods whose blocks appear in generated chunks add theirs with {@link #register}; the same blocks need
 * their generation properties in {@code OldBlockInfo}.
 *
 * <p>States that depend on neighbours (leaf distance, snowy grass, the top face of vines, door halves, the
 * connections of fences, panes, stairs and wires) are finished by
 * {@link OldChunkWriter}.
 */
public final class OldBlockStates {
    private static final int MAX_ID = 4096;
    @SuppressWarnings("unchecked")
    private static final IntFunction<BlockState>[] MAPPERS = new IntFunction[MAX_ID];
    private static final BlockState[][] RESOLVED = new BlockState[MAX_ID][];

    private static final Direction[] CHEST_FACING = {Direction.NORTH, Direction.NORTH, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST};
    private static final Direction[] FACING = {Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST};
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
        registerMeta(97, meta -> (meta == 1 ? Blocks.INFESTED_COBBLESTONE : meta == 2 ? Blocks.INFESTED_STONE_BRICKS : Blocks.INFESTED_STONE)
                .defaultBlockState());
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
        structureBlocks();
    }

    /** Blocks only structures place (villages, mineshafts, strongholds, temples, witch huts). */
    private static void structureBlocks() {
        registerMeta(5, meta -> new Block[] {Blocks.OAK_PLANKS, Blocks.SPRUCE_PLANKS, Blocks.BIRCH_PLANKS, Blocks.JUNGLE_PLANKS}[meta & 3]
                .defaultBlockState());
        registerMeta(23, meta -> Blocks.DISPENSER.defaultBlockState().setValue(BlockStateProperties.FACING, FACING[Math.min(meta & 7, 5)]));
        registerMeta(29, meta -> piston(Blocks.STICKY_PISTON, meta));
        registerMeta(33, meta -> piston(Blocks.PISTON, meta));
        block(30, Blocks.COBWEB);
        // wool colours in metadata order: white, orange, magenta, light blue, yellow, lime, pink, gray, ...
        registerMeta(35, meta -> Blocks.WOOL.asList().get(meta).defaultBlockState());
        registerMeta(43, meta -> {
            Block[] slabs = {Blocks.SMOOTH_STONE_SLAB, Blocks.SANDSTONE_SLAB, Blocks.PETRIFIED_OAK_SLAB, Blocks.COBBLESTONE_SLAB,
                    Blocks.BRICK_SLAB, Blocks.STONE_BRICK_SLAB, Blocks.NETHER_BRICK_SLAB, Blocks.QUARTZ_SLAB};
            return slabs[meta & 7].defaultBlockState().setValue(BlockStateProperties.SLAB_TYPE, SlabType.DOUBLE);
        });
        block(46, Blocks.TNT);
        block(47, Blocks.BOOKSHELF);
        registerMeta(50, meta -> switch (meta) {
            case 1 -> Blocks.WALL_TORCH.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.EAST);
            case 2 -> Blocks.WALL_TORCH.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.WEST);
            case 3 -> Blocks.WALL_TORCH.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.SOUTH);
            case 4 -> Blocks.WALL_TORCH.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH);
            default -> Blocks.TORCH.defaultBlockState();
        });
        stairs(53, Blocks.OAK_STAIRS);
        stairs(67, Blocks.COBBLESTONE_STAIRS);
        stairs(108, Blocks.BRICK_STAIRS);
        stairs(109, Blocks.STONE_BRICK_STAIRS);
        stairs(114, Blocks.NETHER_BRICK_STAIRS);
        stairs(128, Blocks.SANDSTONE_STAIRS);
        stairs(134, Blocks.SPRUCE_STAIRS);
        stairs(135, Blocks.BIRCH_STAIRS);
        stairs(136, Blocks.JUNGLE_STAIRS);
        registerMeta(55, meta -> Blocks.REDSTONE_WIRE.defaultBlockState().setValue(BlockStateProperties.POWER, meta));
        block(58, Blocks.CRAFTING_TABLE);
        registerMeta(59, meta -> Blocks.WHEAT.defaultBlockState().setValue(BlockStateProperties.AGE_7, meta & 7));
        registerMeta(60, meta -> Blocks.FARMLAND.defaultBlockState().setValue(BlockStateProperties.MOISTURE, meta & 7));
        registerMeta(61, meta -> Blocks.FURNACE.defaultBlockState()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, CHEST_FACING[meta < 6 ? meta : 0]));
        registerMeta(64, meta -> door(Blocks.OAK_DOOR, meta));
        registerMeta(71, meta -> door(Blocks.IRON_DOOR, meta));
        registerMeta(65, meta -> Blocks.LADDER.defaultBlockState()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, CHEST_FACING[meta < 6 ? meta : 0]));
        registerMeta(66, meta -> Blocks.RAIL.defaultBlockState().setValue(BlockStateProperties.RAIL_SHAPE, new RailShape[] {
                RailShape.NORTH_SOUTH, RailShape.EAST_WEST, RailShape.ASCENDING_EAST, RailShape.ASCENDING_WEST, RailShape.ASCENDING_NORTH,
                RailShape.ASCENDING_SOUTH, RailShape.SOUTH_EAST, RailShape.SOUTH_WEST, RailShape.NORTH_WEST, RailShape.NORTH_EAST}[meta < 10 ? meta : 0]));
        registerMeta(69, meta -> {
            BlockState lever = Blocks.LEVER.defaultBlockState().setValue(BlockStateProperties.POWERED, (meta & 8) != 0);
            return switch (meta & 7) {
                case 1 -> lever.setValue(BlockStateProperties.ATTACH_FACE, AttachFace.WALL).setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.EAST);
                case 2 -> lever.setValue(BlockStateProperties.ATTACH_FACE, AttachFace.WALL).setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.WEST);
                case 3 -> lever.setValue(BlockStateProperties.ATTACH_FACE, AttachFace.WALL).setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.SOUTH);
                case 4 -> lever.setValue(BlockStateProperties.ATTACH_FACE, AttachFace.WALL).setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH);
                case 5 -> lever.setValue(BlockStateProperties.ATTACH_FACE, AttachFace.FLOOR).setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH);
                case 6 -> lever.setValue(BlockStateProperties.ATTACH_FACE, AttachFace.FLOOR).setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.EAST);
                case 7 -> lever.setValue(BlockStateProperties.ATTACH_FACE, AttachFace.CEILING).setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH);
                default -> lever.setValue(BlockStateProperties.ATTACH_FACE, AttachFace.CEILING).setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.EAST);
            };
        });
        block(70, Blocks.STONE_PRESSURE_PLATE);
        block(72, Blocks.OAK_PRESSURE_PLATE);
        registerMeta(77, meta -> Blocks.STONE_BUTTON.defaultBlockState()
                .setValue(BlockStateProperties.ATTACH_FACE, AttachFace.WALL)
                .setValue(BlockStateProperties.POWERED, (meta & 8) != 0)
                .setValue(BlockStateProperties.HORIZONTAL_FACING,
                        new Direction[] {Direction.NORTH, Direction.EAST, Direction.WEST, Direction.SOUTH, Direction.NORTH}[Math.min(meta & 7, 4)]));
        block(85, Blocks.OAK_FENCE);
        registerMeta(93, meta -> Blocks.REPEATER.defaultBlockState()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, DIRECTION[meta & 3])
                .setValue(BlockStateProperties.DELAY, (meta >> 2) + 1));
        registerMeta(98, meta -> new Block[] {Blocks.STONE_BRICKS, Blocks.MOSSY_STONE_BRICKS, Blocks.CRACKED_STONE_BRICKS,
                Blocks.CHISELED_STONE_BRICKS}[meta & 3].defaultBlockState());
        block(101, Blocks.IRON_BARS);
        block(102, Blocks.GLASS_PANE);
        registerMeta(118, meta -> (meta & 3) == 0 ? Blocks.CAULDRON.defaultBlockState()
                : Blocks.WATER_CAULDRON.defaultBlockState().setValue(BlockStateProperties.LEVEL_CAULDRON, meta & 3));
        registerMeta(120, meta -> Blocks.END_PORTAL_FRAME.defaultBlockState()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, DIRECTION[meta & 3])
                .setValue(BlockStateProperties.EYE, (meta & 4) != 0));
        registerMeta(126, meta -> new Block[] {Blocks.OAK_SLAB, Blocks.SPRUCE_SLAB, Blocks.BIRCH_SLAB, Blocks.JUNGLE_SLAB}[meta & 3]
                .defaultBlockState().setValue(BlockStateProperties.SLAB_TYPE, (meta & 8) != 0 ? SlabType.TOP : SlabType.BOTTOM));
        registerMeta(131, meta -> Blocks.TRIPWIRE_HOOK.defaultBlockState()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, DIRECTION[meta & 3])
                .setValue(BlockStateProperties.ATTACHED, (meta & 4) != 0)
                .setValue(BlockStateProperties.POWERED, (meta & 8) != 0));
        registerMeta(132, meta -> Blocks.TRIPWIRE.defaultBlockState()
                .setValue(BlockStateProperties.POWERED, (meta & 1) != 0)
                .setValue(BlockStateProperties.ATTACHED, (meta & 4) != 0)
                .setValue(BlockStateProperties.DISARMED, (meta & 8) != 0));
        registerMeta(140, meta -> new Block[] {Blocks.FLOWER_POT, Blocks.POTTED_POPPY, Blocks.POTTED_DANDELION, Blocks.POTTED_OAK_SAPLING,
                Blocks.POTTED_SPRUCE_SAPLING, Blocks.POTTED_BIRCH_SAPLING, Blocks.POTTED_JUNGLE_SAPLING, Blocks.POTTED_RED_MUSHROOM,
                Blocks.POTTED_BROWN_MUSHROOM, Blocks.POTTED_CACTUS, Blocks.POTTED_DEAD_BUSH, Blocks.POTTED_FERN}[meta < 12 ? meta : 0]
                .defaultBlockState());
        registerMeta(141, meta -> Blocks.CARROTS.defaultBlockState().setValue(BlockStateProperties.AGE_7, meta & 7));
        registerMeta(142, meta -> Blocks.POTATOES.defaultBlockState().setValue(BlockStateProperties.AGE_7, meta & 7));
    }

    private static void stairs(int oldId, Block block) {
        // metadata 0-3: the side the steps rise towards (east, west, south, north); bit 4: upside down
        registerMeta(oldId, meta -> block.defaultBlockState()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, new Direction[] {Direction.EAST, Direction.WEST, Direction.SOUTH, Direction.NORTH}[meta & 3])
                .setValue(BlockStateProperties.HALF, (meta & 4) != 0 ? Half.TOP : Half.BOTTOM));
    }

    private static BlockState piston(Block block, int meta) {
        return block.defaultBlockState()
                .setValue(BlockStateProperties.FACING, FACING[Math.min(meta & 7, 5)])
                .setValue(BlockStateProperties.EXTENDED, (meta & 8) != 0);
    }

    /**
     * One half of a door. 1.5.2 kept facing and "open" in the lower half and the hinge side in the upper half;
     * {@link OldChunkWriter} copies what the other half knows.
     */
    private static BlockState door(Block block, int meta) {
        BlockState state = block.defaultBlockState();
        if ((meta & 8) != 0) {
            return state.setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.UPPER)
                    .setValue(BlockStateProperties.DOOR_HINGE, (meta & 1) != 0 ? DoorHingeSide.RIGHT : DoorHingeSide.LEFT);
        }
        return state.setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.LOWER)
                .setValue(BlockStateProperties.HORIZONTAL_FACING, new Direction[] {Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.NORTH}[meta & 3])
                .setValue(BlockStateProperties.OPEN, (meta & 4) != 0);
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
