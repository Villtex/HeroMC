package hexxit.worldgen152;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.ComparatorMode;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.PistonType;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.level.block.state.properties.SlabType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
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
    /** Highest block id of vanilla 1.5.2; everything above belongs to a mod. */
    private static final int LAST_VANILLA_ID = 158;
    private static final List<Supplier<? extends Block>> CONNECTING = new CopyOnWriteArrayList<>();
    /** Mod states and blocks back to their 1.5.2 id and metadata ({@code id << 4 | meta}), built on first use. */
    private static volatile Map<Object, Integer> reverse;

    private static final Direction[] CHEST_FACING = {Direction.NORTH, Direction.NORTH, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST};
    private static final Direction[] FACING = {Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST};
    private static final Direction[] DIRECTION = {Direction.SOUTH, Direction.WEST, Direction.NORTH, Direction.EAST};
    /** BlockSkull metadata 2 to 5: the side of the wall block it was put on = the direction it faces. */
    private static final Direction[] SKULL_WALL = {Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST};

    static {
        block(1, Blocks.STONE);
        block(2, Blocks.GRASS_BLOCK);
        block(3, Blocks.DIRT);
        block(4, Blocks.COBBLESTONE);
        block(7, Blocks.BEDROCK);
        // 8/10 are the "flowing" ids: a source that should start to flow (see OldChunkWriter)
        block(8, Blocks.WATER);
        block(9, Blocks.WATER);
        // lava with metadata: the flowing lava the fortress entrance leaves below its source (8 = falling)
        registerMeta(10, meta -> Blocks.LAVA.defaultBlockState().setValue(BlockStateProperties.LEVEL, meta));
        registerMeta(11, meta -> Blocks.LAVA.defaultBlockState().setValue(BlockStateProperties.LEVEL, meta));
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
        // sandstone: 0 plain, 1 chiseled, 2 smooth (today cut); other values drew the plain texture
        registerMeta(24, meta -> (meta == 1 ? Blocks.CHISELED_SANDSTONE : meta == 2 ? Blocks.CUT_SANDSTONE : Blocks.SANDSTONE).defaultBlockState());
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
        // the Nether
        block(51, Blocks.FIRE);
        block(87, Blocks.NETHERRACK);
        block(88, Blocks.SOUL_SAND);
        block(89, Blocks.GLOWSTONE);
        block(112, Blocks.NETHER_BRICKS);
        block(113, Blocks.NETHER_BRICK_FENCE);
        registerMeta(115, meta -> Blocks.NETHER_WART.defaultBlockState().setValue(BlockStateProperties.AGE_3, Math.min(meta, 3)));
        block(153, Blocks.NETHER_QUARTZ_ORE);
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
        // skulls: metadata 1 stands on the floor (kind and rotation come from the tile, OldChunkWriter), 2 to 5 hang on walls
        registerMeta(144, meta -> meta >= 2 && meta <= 5
                ? Blocks.SKELETON_WALL_SKULL.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, SKULL_WALL[meta - 2])
                : Blocks.SKELETON_SKULL.defaultBlockState());
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
        block(103, Blocks.MELON);   // ExtrabiomesXL melon patches
        structureBlocks();
        otherVanillaBlocks();
    }

    /** The remaining vanilla blocks of 1.5.2, which templates (Better Dungeons, Ruins) place. */
    private static void otherVanillaBlocks() {
        // saplings: metadata 0-3 the wood, bit 8 the second growth stage
        registerMeta(6, meta -> new Block[] {Blocks.OAK_SAPLING, Blocks.SPRUCE_SAPLING, Blocks.BIRCH_SAPLING, Blocks.JUNGLE_SAPLING}[meta & 3]
                .defaultBlockState().setValue(BlockStateProperties.STAGE, (meta & 8) != 0 ? 1 : 0));
        block(19, Blocks.SPONGE);
        block(20, Blocks.GLASS);
        block(22, Blocks.LAPIS_BLOCK);
        block(25, Blocks.NOTE_BLOCK);
        // bed: metadata 0-3 the direction from foot to head (south, west, north, east), bit 4 occupied, bit 8 the head
        registerMeta(26, meta -> Blocks.BED.red().defaultBlockState()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, DIRECTION[meta & 3])
                .setValue(BlockStateProperties.OCCUPIED, (meta & 4) != 0)
                .setValue(BlockStateProperties.BED_PART, (meta & 8) != 0 ? BedPart.HEAD : BedPart.FOOT));
        // powered, detector and activator rails: metadata 0-5 the straight shapes of the rail, bit 8 powered
        registerMeta(27, meta -> straightRail(Blocks.POWERED_RAIL, meta));
        registerMeta(28, meta -> straightRail(Blocks.DETECTOR_RAIL, meta));
        registerMeta(157, meta -> straightRail(Blocks.ACTIVATOR_RAIL, meta));
        // piston head: metadata 0-5 facing, bit 8 sticky
        registerMeta(34, meta -> Blocks.PISTON_HEAD.defaultBlockState()
                .setValue(BlockStateProperties.FACING, FACING[Math.min(meta & 7, 5)])
                .setValue(BlockStateProperties.PISTON_TYPE, (meta & 8) != 0 ? PistonType.STICKY : PistonType.DEFAULT));
        // a block being moved by a piston only exists with its tile entity, which generation never writes
        register(36, Blocks.AIR::defaultBlockState);
        block(41, Blocks.GOLD_BLOCK);
        block(42, Blocks.IRON_BLOCK);
        block(45, Blocks.BRICKS);
        block(57, Blocks.DIAMOND_BLOCK);
        registerMeta(62, meta -> Blocks.FURNACE.defaultBlockState()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, CHEST_FACING[meta < 6 ? meta : 0])
                .setValue(BlockStateProperties.LIT, true));
        // signs: standing ones with the rotation in sixteenths, wall signs with the side they face (2-5)
        registerMeta(63, meta -> Blocks.OAK_SIGN.defaultBlockState().setValue(BlockStateProperties.ROTATION_16, meta));
        registerMeta(68, meta -> Blocks.OAK_WALL_SIGN.defaultBlockState()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, CHEST_FACING[meta < 6 ? meta : 0]));
        registerMeta(74, meta -> Blocks.REDSTONE_ORE.defaultBlockState().setValue(BlockStateProperties.LIT, true));
        registerMeta(75, meta -> torch(Blocks.REDSTONE_TORCH, Blocks.REDSTONE_WALL_TORCH, meta).setValue(BlockStateProperties.LIT, false));
        registerMeta(76, meta -> torch(Blocks.REDSTONE_TORCH, Blocks.REDSTONE_WALL_TORCH, meta).setValue(BlockStateProperties.LIT, true));
        registerMeta(84, meta -> Blocks.JUKEBOX.defaultBlockState().setValue(BlockStateProperties.HAS_RECORD, meta != 0));
        // 1.5.2 portals kept no metadata; their plane came from the obsidian around them
        block(90, Blocks.NETHER_PORTAL);
        registerMeta(91, meta -> Blocks.JACK_O_LANTERN.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, DIRECTION[meta & 3]));
        registerMeta(92, meta -> Blocks.CAKE.defaultBlockState().setValue(BlockStateProperties.BITES, Math.min(meta, 6)));
        registerMeta(94, meta -> Blocks.REPEATER.defaultBlockState()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, DIRECTION[meta & 3])
                .setValue(BlockStateProperties.DELAY, (meta >> 2) + 1)
                .setValue(BlockStateProperties.POWERED, true));
        // the April fools' locked chest became an ordinary chest
        block(95, Blocks.CHEST);
        // trapdoor: metadata 0-3 the side of the block its hinge is on (south, north, east, west), bit 4 open, bit 8 upper half
        registerMeta(96, meta -> Blocks.OAK_TRAPDOOR.defaultBlockState()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, new Direction[] {Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST}[meta & 3])
                .setValue(BlockStateProperties.OPEN, (meta & 4) != 0)
                .setValue(BlockStateProperties.HALF, (meta & 8) != 0 ? Half.TOP : Half.BOTTOM));
        registerMeta(104, meta -> Blocks.PUMPKIN_STEM.defaultBlockState().setValue(BlockStateProperties.AGE_7, meta & 7));
        registerMeta(105, meta -> Blocks.MELON_STEM.defaultBlockState().setValue(BlockStateProperties.AGE_7, meta & 7));
        // fence gate: metadata 0-3 the direction (south, west, north, east), bit 4 open
        registerMeta(107, meta -> Blocks.OAK_FENCE_GATE.defaultBlockState()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, DIRECTION[meta & 3])
                .setValue(BlockStateProperties.OPEN, (meta & 4) != 0));
        block(116, Blocks.ENCHANTING_TABLE);
        // brewing stand: bits 1, 2, 4 show a bottle in slot 0, 1, 2
        registerMeta(117, meta -> Blocks.BREWING_STAND.defaultBlockState()
                .setValue(BlockStateProperties.HAS_BOTTLE_0, (meta & 1) != 0)
                .setValue(BlockStateProperties.HAS_BOTTLE_1, (meta & 2) != 0)
                .setValue(BlockStateProperties.HAS_BOTTLE_2, (meta & 4) != 0));
        block(119, Blocks.END_PORTAL);
        block(121, Blocks.END_STONE);
        block(122, Blocks.DRAGON_EGG);
        registerMeta(123, meta -> Blocks.REDSTONE_LAMP.defaultBlockState().setValue(BlockStateProperties.LIT, false));
        registerMeta(124, meta -> Blocks.REDSTONE_LAMP.defaultBlockState().setValue(BlockStateProperties.LIT, true));
        // double wooden slab: metadata 0-3 the wood; other values drew oak planks
        registerMeta(125, meta -> new Block[] {Blocks.OAK_SLAB, Blocks.SPRUCE_SLAB, Blocks.BIRCH_SLAB, Blocks.JUNGLE_SLAB}[(meta & 7) < 4 ? meta & 7 : 0]
                .defaultBlockState().setValue(BlockStateProperties.SLAB_TYPE, SlabType.DOUBLE));
        registerMeta(130, meta -> Blocks.ENDER_CHEST.defaultBlockState()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, CHEST_FACING[meta < 6 ? meta : 0]));
        block(133, Blocks.EMERALD_BLOCK);
        block(137, Blocks.COMMAND_BLOCK);
        block(138, Blocks.BEACON);
        registerMeta(139, meta -> (meta == 1 ? Blocks.MOSSY_COBBLESTONE_WALL : Blocks.COBBLESTONE_WALL).defaultBlockState());
        registerMeta(143, meta -> button(Blocks.OAK_BUTTON, meta));
        // anvil: metadata 0-3 the direction, the bits above it the damage (0 whole, 1 chipped, 2 damaged)
        registerMeta(145, meta -> new Block[] {Blocks.ANVIL, Blocks.CHIPPED_ANVIL, Blocks.DAMAGED_ANVIL}[Math.min(meta >> 2, 2)]
                .defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, DIRECTION[meta & 3]));
        registerMeta(146, meta -> Blocks.TRAPPED_CHEST.defaultBlockState()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, CHEST_FACING[meta < 6 ? meta : 0]));
        // weighted pressure plates and the daylight sensor keep their output power in the metadata
        registerMeta(147, meta -> Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE.defaultBlockState().setValue(BlockStateProperties.POWER, meta));
        registerMeta(148, meta -> Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE.defaultBlockState().setValue(BlockStateProperties.POWER, meta));
        // comparator: metadata 0-3 the direction (as the repeater), bit 4 subtract mode, bit 8 powered (always for id 150)
        registerMeta(149, meta -> comparator(meta, (meta & 8) != 0));
        registerMeta(150, meta -> comparator(meta, true));
        registerMeta(151, meta -> Blocks.DAYLIGHT_DETECTOR.defaultBlockState().setValue(BlockStateProperties.POWER, meta));
        block(152, Blocks.REDSTONE_BLOCK);
        // hopper: metadata 0 and 2-5 the side it feeds (1, up, does not exist), bit 8 switched off by redstone
        registerMeta(154, meta -> Blocks.HOPPER.defaultBlockState()
                .setValue(BlockStateProperties.FACING_HOPPER, (meta & 7) >= 2 && (meta & 7) <= 5 ? FACING[meta & 7] : Direction.DOWN)
                .setValue(BlockStateProperties.ENABLED, (meta & 8) == 0));
        // quartz: 0 plain, 1 chiseled, 2-4 pillar along y, x, z
        registerMeta(155, meta -> switch (meta) {
            case 1 -> Blocks.CHISELED_QUARTZ_BLOCK.defaultBlockState();
            case 2 -> Blocks.QUARTZ_PILLAR.defaultBlockState().setValue(BlockStateProperties.AXIS, Direction.Axis.Y);
            case 3 -> Blocks.QUARTZ_PILLAR.defaultBlockState().setValue(BlockStateProperties.AXIS, Direction.Axis.X);
            case 4 -> Blocks.QUARTZ_PILLAR.defaultBlockState().setValue(BlockStateProperties.AXIS, Direction.Axis.Z);
            default -> Blocks.QUARTZ_BLOCK.defaultBlockState();
        });
        stairs(156, Blocks.QUARTZ_STAIRS);
        registerMeta(158, meta -> Blocks.DROPPER.defaultBlockState().setValue(BlockStateProperties.FACING, FACING[Math.min(meta & 7, 5)]));
    }

    /** Torch metadata: 1-4 on a wall, facing east, west, south, north (away from the block it is on); else standing. */
    private static BlockState torch(Block standing, Block wall, int meta) {
        return switch (meta) {
            case 1 -> wall.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.EAST);
            case 2 -> wall.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.WEST);
            case 3 -> wall.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.SOUTH);
            case 4 -> wall.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH);
            default -> standing.defaultBlockState();
        };
    }

    /** Button metadata: 1-4 the side it faces (east, west, south, north), bit 8 pressed; 1.5.2 buttons only sat on walls. */
    private static BlockState button(Block block, int meta) {
        return block.defaultBlockState()
                .setValue(BlockStateProperties.ATTACH_FACE, AttachFace.WALL)
                .setValue(BlockStateProperties.POWERED, (meta & 8) != 0)
                .setValue(BlockStateProperties.HORIZONTAL_FACING,
                        new Direction[] {Direction.NORTH, Direction.EAST, Direction.WEST, Direction.SOUTH, Direction.NORTH}[Math.min(meta & 7, 4)]);
    }

    private static BlockState straightRail(Block block, int meta) {
        RailShape[] shapes = {RailShape.NORTH_SOUTH, RailShape.EAST_WEST, RailShape.ASCENDING_EAST, RailShape.ASCENDING_WEST,
                RailShape.ASCENDING_NORTH, RailShape.ASCENDING_SOUTH};
        return block.defaultBlockState()
                .setValue(BlockStateProperties.RAIL_SHAPE_STRAIGHT, shapes[(meta & 7) < 6 ? meta & 7 : 0])
                .setValue(BlockStateProperties.POWERED, (meta & 8) != 0);
    }

    private static BlockState comparator(int meta, boolean powered) {
        return Blocks.COMPARATOR.defaultBlockState()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, DIRECTION[meta & 3])
                .setValue(BlockStateProperties.MODE_COMPARATOR, (meta & 4) != 0 ? ComparatorMode.SUBTRACT : ComparatorMode.COMPARE)
                .setValue(BlockStateProperties.POWERED, powered);
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
            // bit 8 drew the top texture on every side: smooth stone, smooth sandstone, smooth quartz
            if (meta == 8) {
                return Blocks.SMOOTH_STONE.defaultBlockState();
            }
            if (meta == 9) {
                return Blocks.SMOOTH_SANDSTONE.defaultBlockState();
            }
            if (meta == 15) {
                return Blocks.SMOOTH_QUARTZ.defaultBlockState();
            }
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
        // pressure plates: metadata 1 is pressed
        registerMeta(70, meta -> Blocks.STONE_PRESSURE_PLATE.defaultBlockState().setValue(BlockStateProperties.POWERED, meta == 1));
        registerMeta(72, meta -> Blocks.OAK_PRESSURE_PLATE.defaultBlockState().setValue(BlockStateProperties.POWERED, meta == 1));
        registerMeta(77, meta -> button(Blocks.STONE_BUTTON, meta));
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
    public static BlockState door(Block block, int meta) {
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

    /**
     * A mod block whose state depends on its neighbours (connections, like the arms of the saguaro). The chunk writer
     * lets it update its shape once the chunk is part of the world, as it does for fences.
     */
    public static void registerConnecting(Supplier<? extends Block> block) {
        CONNECTING.add(block);
    }

    static boolean connecting(Block block) {
        for (Supplier<? extends Block> b : CONNECTING) {
            if (b.get() == block) {
                return true;
            }
        }
        return false;
    }

    /**
     * The 1.5.2 id and metadata ({@code id << 4 | meta}) of a mod block (an id above the vanilla ones), or -1. A state
     * that no metadata value maps to exactly (leaf distance, connections) falls back to the first metadata value of its
     * block.
     */
    static int modCell(BlockState state) {
        Map<Object, Integer> map = reverse;
        if (map == null) {
            map = buildReverse();
        }
        Integer cell = map.get(state);
        if (cell == null) {
            cell = map.get(state.getBlock());
        }
        return cell == null ? -1 : cell;
    }

    private static synchronized Map<Object, Integer> buildReverse() {
        if (reverse != null) {
            return reverse;
        }
        Map<Object, Integer> map = new HashMap<>();
        for (int id = LAST_VANILLA_ID + 1; id < MAX_ID; id++) {
            if (MAPPERS[id] == null || !hexxit.worldgen152.core.populate.OldBlockInfo.known(id)) {
                continue;
            }
            for (int meta = 0; meta < 16; meta++) {
                BlockState state = get(id, meta);
                if (state == null || state.isAir()) {
                    continue;
                }
                map.putIfAbsent(state, id << 4 | meta);
                map.putIfAbsent(state.getBlock(), id << 4 | meta);
            }
        }
        reverse = map;
        return map;
    }
}
