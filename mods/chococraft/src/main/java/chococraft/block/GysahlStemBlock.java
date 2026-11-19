package chococraft.block;

import chococraft.ChocoCraft;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealSource;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.FarmlandBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * BlockGysahlStem: the planted gysahl seed on farmland, stages 0-4 (metadata), 4 px high. Grows at light 9 above with a
 * chance of 1 / (25 / rate + 1) per random tick; rate = 1 + the farmland around (3 when wet, a quarter for the eight
 * neighbours), halved by a neighbouring stem diagonally or on both axes. Bone meal makes it ripe at once. Drops
 * (loot table): ripe a gysahl green, mutating into a loverly (gysahlGreenMutationRate 100 per mille) or golden
 * gysahl (gysahlLoverlyMutationRate 100 per mille of those); and 3 tries for a seed with (stage + 1) / 15 each.
 */
public class GysahlStemBlock extends CropBlock {
    public static final IntegerProperty AGE = BlockStateProperties.AGE_4;
    private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 4, 16);

    public GysahlStemBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected IntegerProperty getAgeProperty() {
        return AGE;
    }

    @Override
    public int getMaxAge() {
        return 4;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE);
    }

    @Override
    protected ItemLike getBaseSeedId() {
        return ChocoCraft.GYSAHL_SEEDS.get();
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        // BlockFlower.updateTick first: a stem that cannot stay drops
        if (!state.canSurvive(level, pos)) {
            level.destroyBlock(pos, true);
            return;
        }
        if (level.getMaxLocalRawBrightness(pos.above()) >= 9 && !this.isMaxAge(state)) {
            float rate = growthRate(level, pos);
            if (random.nextInt((int) (25.0F / rate) + 1) == 0) {
                level.setBlock(pos, this.getStateForAge(this.getAge(state) + 1), Block.UPDATE_CLIENTS);
            }
        }
    }

    private float growthRate(ServerLevel level, BlockPos pos) {
        Block self = this;
        boolean leftRight = level.getBlockState(pos.west()).is(self) || level.getBlockState(pos.east()).is(self);
        boolean frontBack = level.getBlockState(pos.north()).is(self) || level.getBlockState(pos.south()).is(self);
        boolean corner = level.getBlockState(pos.offset(-1, 0, -1)).is(self) || level.getBlockState(pos.offset(1, 0, -1)).is(self)
                || level.getBlockState(pos.offset(1, 0, 1)).is(self) || level.getBlockState(pos.offset(-1, 0, 1)).is(self);
        float rate = 1.0F;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                BlockState soil = level.getBlockState(pos.offset(dx, -1, dz));
                float r = 0.0F;
                if (soil.getBlock() instanceof FarmlandBlock) {
                    r = soil.getValue(FarmlandBlock.MOISTURE) > 0 ? 3.0F : 1.0F;
                }
                if (dx != 0 || dz != 0) {
                    r /= 4.0F;
                }
                rate += r;
            }
        }
        if (corner || leftRight && frontBack) {
            rate /= 2.0F;
        }
        return rate;
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        // BlockFlower.canBlockStay: light 8 or open sky, on farmland
        return (level.getRawBrightness(pos, 0) >= 8 || level.canSeeSky(pos)) && this.mayPlaceOn(level.getBlockState(pos.below()), level, pos.below());
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state, BonemealSource source) {
        return !this.isMaxAge(state);
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state, BonemealSource source) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state, BonemealSource source) {
        level.setBlock(pos, this.getStateForAge(this.getMaxAge()), Block.UPDATE_CLIENTS);
    }
}
