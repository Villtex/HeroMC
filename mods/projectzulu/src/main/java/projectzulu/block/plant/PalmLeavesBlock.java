package projectzulu.block.plant;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.TintedParticleLeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;

/**
 * BlockPalmTreeLeaves. Not the distance of vanilla leaves: as in 1.5.2 a leaf only checks for decay after a log within
 * 4 or a leaf next to it was removed (metadata bit 8 = {@link #CHECK_DECAY}); then, on a random tick, it stays when a
 * log is reachable through at most 4 leaves within 4 blocks, otherwise it drops its items and vanishes. Player-placed
 * leaves (bit 4) are {@code persistent}. The fronds of a palm end in leaves that only touch the others diagonally;
 * they stay until a decay check reaches them, as in the original.
 */
public class PalmLeavesBlock extends TintedParticleLeavesBlock {
    public static final BooleanProperty CHECK_DECAY = BooleanProperty.create("check_decay");
    private static final int RANGE = 4;

    public PalmLeavesBlock(Properties properties) {
        super(0.01F, properties);
        this.registerDefaultState(this.defaultBlockState().setValue(CHECK_DECAY, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(CHECK_DECAY);
    }

    /** beginLeavesDecay on every palm leaf within {@code range} of pos. */
    public static void beginDecay(ServerLevel level, BlockPos pos, int range) {
        for (BlockPos p : BlockPos.betweenClosed(pos.offset(-range, -range, -range), pos.offset(range, range, range))) {
            BlockState s = level.getBlockState(p);
            if (s.getBlock() instanceof PalmLeavesBlock && !s.getValue(CHECK_DECAY)) {
                level.setBlock(p, s.setValue(CHECK_DECAY, true), Block.UPDATE_CLIENTS);
            }
        }
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return !state.getValue(PERSISTENT) && state.getValue(CHECK_DECAY);
    }

    @Override
    protected boolean decaying(BlockState state) {
        return false;
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
        super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston);
        beginDecay(level, pos, 1);
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (state.getValue(PERSISTENT) || !state.getValue(CHECK_DECAY)) {
            return;
        }
        if (nearLog(level, pos)) {
            level.setBlock(pos, state.setValue(CHECK_DECAY, false), Block.UPDATE_ALL);
        } else {
            Block.dropResources(state, level, pos);
            level.removeBlock(pos, false);
        }
    }

    /** The search of updateTick: a log next to a chain of at most 4 leaves (this one included) inside the 9x9x9 cube. */
    private static boolean nearLog(ServerLevel level, BlockPos pos) {
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        ArrayDeque<Integer> depths = new ArrayDeque<>();
        Set<BlockPos> seen = new HashSet<>();
        queue.add(pos);
        depths.add(1);
        seen.add(pos);
        while (!queue.isEmpty()) {
            BlockPos p = queue.poll();
            int depth = depths.poll();
            for (Direction d : Direction.values()) {
                BlockPos q = p.relative(d);
                if (Math.abs(q.getX() - pos.getX()) > RANGE || Math.abs(q.getY() - pos.getY()) > RANGE
                        || Math.abs(q.getZ() - pos.getZ()) > RANGE) {
                    continue;
                }
                BlockState s = level.getBlockState(q);
                if (s.is(BlockTags.LOGS)) {
                    return true;
                }
                if (depth < RANGE && s.is(BlockTags.LEAVES) && seen.add(q)) {
                    queue.add(q);
                    depths.add(depth + 1);
                }
            }
        }
        return false;
    }
}
