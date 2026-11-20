package twilightforest.block;

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
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import twilightforest.TFBlocks;

import java.util.Random;

/**
 * BlockTFSapling (metadata 0..9). A random tick with light 9 above and 1 in 7 grows the tree at once (no marking
 * stage as vanilla saplings have); bone meal grows it always (TFEventListener.bonemealUsed). The tree comes from
 * {@link TFBlocks#GROWER}; if it does not grow, the sapling is put back.
 */
public class TFSaplingBlock extends VegetationBlock implements BonemealableBlock {
    private static final VoxelShape SHAPE = Block.box(1.6, 0.0, 1.6, 14.4, 12.8, 14.4);
    private final int meta;

    public TFSaplingBlock(int meta, Properties properties) {
        super(properties);
        this.meta = meta;
    }

    public int meta() {
        return this.meta;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    /** BlockSapling.canThisPlantGrowOnThisBlockID: grass, dirt, farmland. */
    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(BlockTags.DIRT) || state.is(Blocks.FARMLAND);
    }

    /** BlockFlower.canBlockStay: light 8 or the sky, and the soil. */
    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return (level.getRawBrightness(pos, 0) >= 8 || level.canSeeSky(pos)) && this.mayPlaceOn(level.getBlockState(pos.below()), level, pos.below());
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (level.getMaxLocalRawBrightness(pos.above()) >= 9 && random.nextInt(7) == 0) {
            this.growTree(level, pos, random);
        }
    }

    /** growTree: remove the sapling, run the generator, put the sapling back when it fails. */
    public void growTree(ServerLevel level, BlockPos pos, RandomSource random) {
        BlockState sapling = level.getBlockState(pos);
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_INVISIBLE);
        if (!TFBlocks.GROWER.grow(level, pos, this.meta, new Random(random.nextLong()))) {
            level.setBlock(pos, sapling, Block.UPDATE_INVISIBLE);
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

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state, BonemealSource source) {
        this.growTree(level, pos, random);
    }
}
