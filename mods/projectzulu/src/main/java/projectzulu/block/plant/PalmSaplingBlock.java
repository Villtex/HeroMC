package projectzulu.block.plant;

import hexxit.worldgen152.OldLiveGen;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealSource;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Random;

/**
 * BlockPalmTreeSapling: on sand, dirt or grass; a random tick with light 9 above grows the palm with chance 1/7, bone
 * meal grows it at once (onBlockActivated with bone meal). There is no growth stage. The tree is {@link PalmTreeGen},
 * run in the live world with {@link OldLiveGen}.
 */
public class PalmSaplingBlock extends ZuluFlowerBlock implements BonemealableBlock {
    private static final VoxelShape SHAPE = Block.box(1.6, 0.0, 1.6, 14.4, 12.8, 14.4);

    public PalmSaplingBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(Blocks.SAND) || state.is(Blocks.DIRT) || state.is(Blocks.GRASS_BLOCK) || state.is(BlockTags.SAND)
                || state.is(BlockTags.DIRT);
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!state.canSurvive(level, pos)) {
            level.destroyBlock(pos, true);
            return;
        }
        if (level.getMaxLocalRawBrightness(pos.above()) >= 9 && random.nextInt(7) == 0) {
            growTree(level, pos, random);
        }
    }

    public static void growTree(ServerLevel level, BlockPos pos, RandomSource source) {
        Random random = new Random(source.nextLong());
        OldLiveGen.generate(level, new PalmTreeGen(), random, pos.getX(), pos.getY(), pos.getZ(), PalmTreeGen.RADIUS, PalmTreeGen.DEPTH,
                PalmTreeGen.HEIGHT);
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state, BonemealSource source) {
        return true;
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state, BonemealSource source) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state, BonemealSource source) {
        growTree(level, pos, random);
    }
}
