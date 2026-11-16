package natura.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealSource;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Random;

/**
 * Glowshroom (green, purple, blue): a small glowing mushroom. Spreads like a mushroom (1/25 per random tick, at
 * most 5 in the 9x3x9 around). Stands on mycelium-like soil, or in light below 13 on any solid block. Bone meal grows
 * the large glowshroom through the generator of the original ({@link NaturaTree}).
 */
public class GlowshroomBlock extends VegetationBlock implements BonemealableBlock {
    private static final VoxelShape SHAPE = Block.box(5, 0, 5, 11, 6, 11);
    private final NaturaTree tree;

    public GlowshroomBlock(NaturaTree tree, Properties properties) {
        super(properties);
        this.tree = tree;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.isSolidRender();
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos below = pos.below();
        BlockState soil = level.getBlockState(below);
        return soil.is(BlockTags.OVERRIDES_MUSHROOM_LIGHT_REQUIREMENT) || level.getRawBrightness(pos, 0) < 13 && soil.isFaceSturdy(level, below, Direction.UP);
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (random.nextInt(25) != 0) {
            return;
        }
        int left = 5;
        for (BlockPos p : BlockPos.betweenClosed(pos.offset(-4, -1, -4), pos.offset(4, 1, 4))) {
            if (level.getBlockState(p).is(this) && --left <= 0) {
                return;
            }
        }
        BlockPos target = pos.offset(random.nextInt(3) - 1, random.nextInt(2) - random.nextInt(2), random.nextInt(3) - 1);
        for (int i = 0; i < 4; i++) {
            if (level.isEmptyBlock(target) && state.canSurvive(level, target)) {
                pos = target;
            }
            target = pos.offset(random.nextInt(3) - 1, random.nextInt(2) - random.nextInt(2), random.nextInt(3) - 1);
        }
        if (level.isEmptyBlock(target) && state.canSurvive(level, target)) {
            level.setBlock(target, state, Block.UPDATE_ALL);
        }
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state, BonemealSource source) {
        return true;
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state, BonemealSource source) {
        return true;
    }

    /** Glowshroom.fertilizeMushroom: remove, generate, put back when the generator fails. */
    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state, BonemealSource source) {
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_NONE);
        if (!this.tree.grow(level, pos, new Random(random.nextLong()))) {
            level.setBlock(pos, state, Block.UPDATE_ALL);
        }
    }
}
