package hexxit.worldgen152;

import hexxit.worldgen152.core.OldChunk;
import hexxit.worldgen152.core.populate.OldBlockInfo;
import hexxit.worldgen152.core.populate.OldGen;
import hexxit.worldgen152.core.populate.OldTile;
import hexxit.worldgen152.core.populate.OldWorld;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.material.FluidState;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Runs a 1.5.2 generator ({@link OldGen}) in a live world: a sapling growing into one of the mod trees. The
 * blocks around the spot are copied into an {@link OldWorld} as the 1.5.2 ids the generators ask about (air,
 * water, grass, dirt, sand, logs, leaves, plants, otherwise stone), the generator runs there, and every block it
 * changed is written back with {@link OldBlockStates}.
 */
public final class OldLiveGen {
    private static final byte[] NO_BLOCKS = new byte[256 * OldChunk.HEIGHT];
    private static final byte[] NO_BIOMES = new byte[256];

    private OldLiveGen() {
    }

    /**
     * The 1.5.2 id and metadata ({@code id << 4 | meta}) a generator should see for a block of today: the mod blocks
     * as the mods mapped them, otherwise air, water, lava, the ground blocks the generators ask about, logs, leaves,
     * plants, and stone for the rest.
     */
    static int oldCell(BlockState state) {
        if (state.isAir()) {
            return 0;
        }
        FluidState fluid = state.getFluidState();
        if (!fluid.isEmpty() && state.getBlock() instanceof net.minecraft.world.level.block.LiquidBlock) {
            return (fluid.is(FluidTags.LAVA) ? 11 : 9) << 4;
        }
        int mod = OldBlockStates.modCell(state);
        if (mod >= 0) {
            return mod;
        }
        return oldId(state) << 4;
    }

    private static int oldId(BlockState state) {
        if (state.is(Blocks.GRASS_BLOCK)) {
            return 2;
        }
        if (state.is(Blocks.FARMLAND)) {
            return 60;
        }
        if (state.is(Blocks.MYCELIUM)) {
            return 110;
        }
        if (state.is(Blocks.NETHERRACK)) {
            return 87;
        }
        if (state.is(Blocks.SOUL_SAND) || state.is(Blocks.SOUL_SOIL)) {
            return 88;
        }
        if (state.is(Blocks.BEDROCK)) {
            return 7;
        }
        if (state.is(Blocks.CACTUS)) {
            return 81;
        }
        if (state.is(Blocks.SUGAR_CANE)) {
            return 83;
        }
        // blocks the generators of 1.5.2 tell apart; anything else solid counts as stone, and so would these: marble
        // and ore clusters turned the ice of frozen oceans to stone, ruins took the ice for rock to stand on
        if (state.is(BlockTags.ICE)) {
            return 79;
        }
        if (state.is(Blocks.SNOW_BLOCK) || state.is(Blocks.POWDER_SNOW)) {
            return 80;
        }
        if (state.is(Blocks.SNOW)) {
            return 78;
        }
        if (state.is(Blocks.GRAVEL) || state.is(Blocks.SUSPICIOUS_GRAVEL)) {
            return 13;
        }
        if (state.is(Blocks.CLAY)) {
            return 82;
        }
        if (state.is(Blocks.SANDSTONE) || state.is(Blocks.RED_SANDSTONE)) {
            return 24;
        }
        if (state.is(BlockTags.DIRT)) {
            return 3;
        }
        if (state.is(BlockTags.SAND)) {
            return 12;
        }
        if (state.is(BlockTags.LOGS)) {
            return 17;
        }
        if (state.is(BlockTags.LEAVES)) {
            return 18;
        }
        if (state.canBeReplaced()) {
            return 31;
        }
        if (state.getCollisionShape(net.minecraft.world.level.EmptyBlockGetter.INSTANCE, BlockPos.ZERO).isEmpty()) {
            return 37;
        }
        return 1;
    }

    /**
     * Grows {@code gen} at x/y/z. {@code radius} and {@code height} bound the area the generator may touch.
     *
     * @return what the generator returned; on {@code false} nothing is written
     */
    public static boolean generate(ServerLevel level, OldGen gen, Random random, int x, int y, int z, int radius, int height) {
        return generate(level, gen, random, x, y, z, radius, 2, height);
    }

    /**
     * Grows {@code gen} at x/y/z. The generator may touch {@code radius} blocks to the sides, {@code depth} blocks
     * below y (roots, trees that hang from a ceiling) and {@code height} blocks above.
     *
     * @return what the generator returned; on {@code false} nothing is written
     */
    public static boolean generate(ServerLevel level, OldGen gen, Random random, int x, int y, int z, int radius, int depth, int height) {
        OldWorld world = new OldWorld(level.getSeed(), (cx, cz) -> new OldChunk(cx, cz, NO_BLOCKS, NO_BIOMES));
        int minY = Math.max(level.getMinY(), y - depth);
        int maxY = Math.min(level.getMaxY(), y + height);
        int size = 2 * radius + 1;
        int[] before = new int[size * size * (maxY - minY + 1)];
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int i = 0;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                for (int by = minY; by <= maxY; by++, i++) {
                    int cell = oldCell(level.getBlockState(pos.set(x + dx, by, z + dz)));
                    before[i] = cell;
                    if (cell != 0 && by >= 0 && by < OldWorld.HEIGHT) {
                        world.setBlock(x + dx, by, z + dz, cell >> 4, cell & 15);
                    }
                }
            }
        }
        if (!gen.generate(world, random, x, y, z)) {
            return false;
        }
        // leaf distance as the chunk writer sets it: steps through leaves to the nearest log, 7 = none in reach
        int height2 = maxY - minY + 1;
        int[] distance = new int[size * size * height2];
        java.util.ArrayDeque<Integer> queue = new java.util.ArrayDeque<>();
        i = 0;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                for (int by = minY; by <= maxY; by++, i++) {
                    int id = world.getBlockId(x + dx, by, z + dz);
                    distance[i] = OldBlockInfo.wood(id) ? 0 : 7;
                    if (distance[i] == 0) {
                        queue.add(i);
                    }
                }
            }
        }
        int strideX = size * height2;
        while (!queue.isEmpty()) {
            int at = queue.poll();
            int d = distance[at] + 1;
            if (d >= 7) {
                continue;
            }
            int ax = at / strideX;
            int az = at / height2 % size;
            int ay = at % height2;
            for (int[] step : new int[][] {{1, 0, 0}, {-1, 0, 0}, {0, 1, 0}, {0, -1, 0}, {0, 0, 1}, {0, 0, -1}}) {
                int nx = ax + step[0];
                int ny = ay + step[1];
                int nz = az + step[2];
                if (nx < 0 || nx >= size || nz < 0 || nz >= size || ny < 0 || ny >= height2) {
                    continue;
                }
                int n = nx * strideX + nz * height2 + ny;
                if (distance[n] > d && OldBlockInfo.leaves(world.getBlockId(x + nx - radius, minY + ny, z + nz - radius))) {
                    distance[n] = d;
                    queue.add(n);
                }
            }
        }
        List<BlockPos> leaves = new ArrayList<>();
        List<BlockState> leafStates = new ArrayList<>();
        List<BlockPos> written = new ArrayList<>();
        i = 0;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                for (int by = minY; by <= maxY; by++, i++) {
                    if (by < 0 || by >= OldWorld.HEIGHT) {
                        continue;
                    }
                    int id = world.getBlockId(x + dx, by, z + dz);
                    int meta = world.getBlockMetadata(x + dx, by, z + dz);
                    if ((id << 4 | meta) == before[i]) {
                        continue;
                    }
                    BlockPos at = new BlockPos(x + dx, by, z + dz);
                    BlockState state = id == 0 ? Blocks.AIR.defaultBlockState() : OldBlockStates.get(id, meta);
                    if (state.getBlock() instanceof LeavesBlock) {
                        leaves.add(at);
                        leafStates.add(state.setValue(LeavesBlock.DISTANCE, Math.max(1, distance[i])));
                    } else {
                        level.setBlock(at, state, Block.UPDATE_ALL);
                        written.add(at);
                    }
                }
            }
        }
        // states that depend on their neighbours (connections): the last block of a group never got its update
        for (BlockPos at : written) {
            BlockState state = level.getBlockState(at);
            BlockState shaped = Block.updateFromNeighbourShapes(state, level, at);
            if (shaped != state) {
                level.setBlock(at, shaped, Block.UPDATE_CLIENTS);
            }
        }
        // leaves last and without neighbour updates, as the 1.5.2 generators left them (see the chunk writer)
        for (int n = 0; n < leaves.size(); n++) {
            level.setBlock(leaves.get(n), leafStates.get(n), Block.UPDATE_CLIENTS);
        }
        return true;
    }

    /**
     * Writes what a 1.5.2 builder put into {@code world} (a world that was empty before) into the live level: every
     * block that is not air in the box, then what the tile entities held, then lets the blocks find their neighbours
     * (stairs, fences, leaves, door halves). Like setBlockDirectly of the mods, no block reacts to being placed.
     */
    public static void place(ServerLevel level, OldWorld world, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        minY = Math.max(minY, Math.max(0, level.getMinY()));
        maxY = Math.min(maxY, Math.min(OldWorld.HEIGHT - 1, level.getMaxY()));
        List<BlockPos> written = new ArrayList<>();
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                for (int y = minY; y <= maxY; y++) {
                    int id = world.getBlockId(x, y, z);
                    if (id == 0) {
                        continue;
                    }
                    BlockState state = OldBlockStates.get(id, world.getBlockMetadata(x, y, z));
                    if (state.getBlock() instanceof DoorBlock) {
                        state = door(world, state, x, y, z);
                    }
                    state = chest(state, x, y, z, (cx, cy, cz) -> OldBlockStates.get(world.getBlockId(cx, cy, cz), world.getBlockMetadata(cx, cy, cz)));
                    BlockPos at = new BlockPos(x, y, z);
                    level.setBlock(at, state, Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
                    written.add(at);
                }
            }
        }
        for (BlockPos at : written) {
            OldTile tile = world.getTile(at.getX(), at.getY(), at.getZ());
            BlockEntity blockEntity = tile == null ? null : level.getBlockEntity(at);
            if (blockEntity != null) {
                OldChunkWriter.fillTile(blockEntity, tile, level.registryAccess(), at);
                blockEntity.setChanged();
                level.sendBlockUpdated(at, level.getBlockState(at), level.getBlockState(at), Block.UPDATE_CLIENTS);
            }
        }
        for (BlockPos at : written) {
            BlockState state = level.getBlockState(at);
            BlockState shaped = Block.updateFromNeighbourShapes(state, level, at);
            if (shaped != state && !shaped.isAir()) {
                level.setBlock(at, shaped, Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
            }
        }
    }

    /** A door half with what 1.5.2 kept in the other half (facing and "open" below, the hinge above). */
    static BlockState door(OldWorld world, BlockState state, int x, int y, int z) {
        boolean upper = state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.UPPER;
        int otherY = upper ? y - 1 : y + 1;
        if (world.getBlockId(x, otherY, z) != world.getBlockId(x, y, z)) {
            return state;
        }
        BlockState other = OldBlockStates.get(world.getBlockId(x, otherY, z), world.getBlockMetadata(x, otherY, z));
        if (other.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF)) {
            return state;
        }
        BlockState lower = upper ? other : state;
        BlockState top = upper ? state : other;
        return state.setValue(BlockStateProperties.HORIZONTAL_FACING, lower.getValue(BlockStateProperties.HORIZONTAL_FACING))
                .setValue(BlockStateProperties.OPEN, lower.getValue(BlockStateProperties.OPEN))
                .setValue(BlockStateProperties.DOOR_HINGE, top.getValue(BlockStateProperties.DOOR_HINGE));
    }

    /** The 1.5.2 state of a block next to a chest, or null where it is not known. */
    @FunctionalInterface
    interface StateAt {
        BlockState at(int x, int y, int z);
    }

    private static final Direction[] CHEST_SIDES = {Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST};

    /**
     * BlockChest.unifyAdjacentChests: two chests side by side were one double chest in 1.5.2, today the halves say so in
     * their state. Both halves face the same way, across the line they form: the facing of the western/northern half
     * if it fits, else that of the other, else south (pair along x) or east (pair along z), the default of 1.5.2.
     */
    static BlockState chest(BlockState state, int x, int y, int z, StateAt old) {
        if (!(state.getBlock() instanceof ChestBlock) || !state.hasProperty(BlockStateProperties.CHEST_TYPE)) {
            return state;
        }
        for (Direction side : CHEST_SIDES) {
            BlockState other = old.at(x + side.getStepX(), y, z + side.getStepZ());
            if (other == null || !other.is(state.getBlock())) {
                continue;
            }
            Direction.Axis line = side.getAxis();
            boolean first = side.getAxisDirection() == Direction.AxisDirection.POSITIVE;
            Direction a = (first ? state : other).getValue(BlockStateProperties.HORIZONTAL_FACING);
            Direction b = (first ? other : state).getValue(BlockStateProperties.HORIZONTAL_FACING);
            Direction facing = a.getAxis() != line ? a : b.getAxis() != line ? b : line == Direction.Axis.X ? Direction.SOUTH : Direction.EAST;
            ChestType type = side == facing.getClockWise() ? ChestType.LEFT : ChestType.RIGHT;
            return state.setValue(BlockStateProperties.HORIZONTAL_FACING, facing).setValue(BlockStateProperties.CHEST_TYPE, type);
        }
        return state;
    }
}
