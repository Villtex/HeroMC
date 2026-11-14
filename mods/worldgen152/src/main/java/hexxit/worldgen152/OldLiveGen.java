package hexxit.worldgen152;

import hexxit.worldgen152.core.OldChunk;
import hexxit.worldgen152.core.populate.OldGen;
import hexxit.worldgen152.core.populate.OldWorld;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
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

    /** The 1.5.2 id a generator should see for a block of today. */
    static int oldId(BlockState state) {
        if (state.isAir()) {
            return 0;
        }
        FluidState fluid = state.getFluidState();
        if (!fluid.isEmpty() && state.getBlock() instanceof net.minecraft.world.level.block.LiquidBlock) {
            return fluid.is(FluidTags.LAVA) ? 11 : 9;
        }
        if (state.is(Blocks.GRASS_BLOCK)) {
            return 2;
        }
        if (state.is(Blocks.FARMLAND)) {
            return 60;
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
        OldWorld world = new OldWorld(level.getSeed(), (cx, cz) -> new OldChunk(cx, cz, NO_BLOCKS, NO_BIOMES));
        int minY = Math.max(level.getMinY(), y - 2);
        int maxY = Math.min(level.getMaxY(), y + height);
        int size = 2 * radius + 1;
        int[] before = new int[size * size * (maxY - minY + 1)];
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int i = 0;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                for (int by = minY; by <= maxY; by++, i++) {
                    int id = oldId(level.getBlockState(pos.set(x + dx, by, z + dz)));
                    before[i] = id << 4;
                    if (id != 0 && by >= 0 && by < OldWorld.HEIGHT) {
                        world.setBlock(x + dx, by, z + dz, id, 0);
                    }
                }
            }
        }
        if (!gen.generate(world, random, x, y, z)) {
            return false;
        }
        List<BlockPos> leaves = new ArrayList<>();
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
                        level.setBlock(at, state, Block.UPDATE_CLIENTS);
                    } else {
                        level.setBlock(at, state, Block.UPDATE_ALL);
                    }
                }
            }
        }
        // leaves last, so that they find the new logs for their distance
        for (BlockPos at : leaves) {
            BlockState state = level.getBlockState(at);
            level.setBlock(at, Block.updateFromNeighbourShapes(state, level, at), Block.UPDATE_ALL);
        }
        return true;
    }
}
