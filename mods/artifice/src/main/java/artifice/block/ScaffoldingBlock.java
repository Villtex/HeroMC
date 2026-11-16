package artifice.block;

import artifice.ArtificeBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.redstone.Orientation;
import org.jspecify.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;

/**
 * BlockFrameScaffold: a scaffold stays (and can be placed) where it is rooted - a column of the same scaffolding
 * down to a block that is solid on top - or next to the same scaffolding that is rooted or connected to a rooted
 * scaffold within its overhang (4/8/12/16). It drops when that support goes. Left-clicking it with the same
 * scaffolding stacks one on top of the column. Climbing is in {@code ArtificeEvents} (ScaffoldTicker).
 */
public class ScaffoldingBlock extends FrameBlock {
    /** checkStay looks at these neighbours, in this order. */
    private static final Direction[] SIDES = {Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST, Direction.DOWN};
    /** Safety limit for the search of connected scaffolding (isConnected of 1.5.2 had none). */
    private static final int MAX_CONNECTED = 100_000;

    private final ArtificeBlocks.Tier tier;

    public ScaffoldingBlock(Properties properties, ArtificeBlocks.Tier tier) {
        super(properties, false, true);
        this.tier = tier;
    }

    /** getOverhang: 4, 8, 12, 16 by tier. */
    public int overhang() {
        return 4 * (this.tier.ordinal() + 1);
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effectApplier, boolean isPrecise) {
        if (entity instanceof ServerPlayer) {
            entity.resetFallDistance();
        }
    }

    @Override
    protected void attack(BlockState state, Level level, BlockPos pos, Player player) {
        if (level.isClientSide()) {
            return;
        }
        ItemStack held = player.getMainHandItem();
        if (!held.is(this.asItem())) {
            return;
        }
        BlockPos top = pos;
        while (level.getBlockState(top).getBlock() instanceof ScaffoldingBlock) {
            top = top.above();
        }
        if (level.isInsideBuildHeight(top) && this.checkStay(level, top) && level.getBlockState(top).isAir()) {
            level.setBlock(top, this.defaultBlockState(), Block.UPDATE_ALL);
            if (!player.getAbilities().instabuild) {
                held.shrink(1);
            }
        }
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return this.checkStay(level, pos);
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, @Nullable Orientation orientation, boolean movedByPiston) {
        if (!level.isClientSide() && !this.checkStay(level, pos)) {
            level.destroyBlock(pos, true);
        }
    }

    /** checkStay of 1.5.2. */
    public boolean checkStay(LevelReader level, BlockPos pos) {
        if (this.isRooted(level, pos)) {
            return true;
        }
        for (Direction side : SIDES) {
            BlockPos t = pos.relative(side);
            if (level.getBlockState(t).getBlock() instanceof ScaffoldingBlock) {
                if (level.getBlockState(t).is(this)) {
                    if (this.isRooted(level, t)) {
                        return true;
                    }
                    return this.rootedWithinOverhang(level, pos);
                }
                return false;
            }
        }
        return false;
    }

    /** getRadiusMatches(overhang) that are rooted, connected to {@code pos} and within the overhang. */
    private boolean rootedWithinOverhang(LevelReader level, BlockPos pos) {
        Set<BlockPos> connected = this.connected(level, pos);
        int r = this.overhang();
        for (BlockPos match : connected) {
            if (Math.abs(match.getX() - pos.getX()) <= r && Math.abs(match.getY() - pos.getY()) <= r && Math.abs(match.getZ() - pos.getZ()) <= r
                    && Math.sqrt(match.distSqr(pos)) <= r && this.isRooted(level, match)) {
                return true;
            }
        }
        return false;
    }

    /** isConnected: the scaffolding of this kind reachable from {@code pos} over its six neighbours. */
    private Set<BlockPos> connected(LevelReader level, BlockPos start) {
        Set<BlockPos> seen = new HashSet<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        queue.add(start);
        seen.add(start);
        Set<BlockPos> found = new HashSet<>();
        while (!queue.isEmpty() && found.size() < MAX_CONNECTED) {
            BlockPos p = queue.poll();
            for (Direction d : Direction.values()) {
                BlockPos q = p.relative(d);
                if (!seen.contains(q) && level.hasChunkAt(q) && level.getBlockState(q).is(this)) {
                    seen.add(q);
                    found.add(q);
                    queue.add(q);
                }
            }
        }
        return found;
    }

    /** isRooted: going down, the same scaffolding until a block that is solid on top (any other block fails). */
    public boolean isRooted(LevelReader level, BlockPos pos) {
        for (BlockPos p = pos.below(); p.getY() > level.getMinY(); p = p.below()) {
            if (!FrameBlock.isSolidUp(level, p)) {
                return false;
            }
            BlockState state = level.getBlockState(p);
            if (!(state.getBlock() instanceof ScaffoldingBlock)) {
                return true;
            }
            if (!state.is(this)) {
                return false;
            }
        }
        return false;
    }
}
