package offlawn.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BonemealSource;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import offlawn.OffLawn;

/**
 * SunflowerBlock: two blocks high (metadata 0 = lower, 1 = upper half), on dirt, grass, farmland, lawn or a
 * beanstalk, where it is light (8 or more) or under the open sky. Breaking it gives one sunflower seed; bone meal
 * makes it drop another (SunflowerBonemeal).
 */
public class SunflowerBlock extends DoublePlantBlock implements BonemealableBlock {
    public SunflowerBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(Blocks.DIRT) || state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.FARMLAND)
                || state.is(OffLawn.LAWN.get()) || state.is(OffLawn.BEANSTALK.get());
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        if (state.getValue(HALF) == DoubleBlockHalf.UPPER) {
            return super.canSurvive(state, level, pos);
        }
        // getFullBlockLightValue < 8 && !canBlockSeeTheSky -> no
        if (level.getRawBrightness(pos, 0) < 8 && !level.canSeeSky(pos)) {
            return false;
        }
        return super.canSurvive(state, level, pos);
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state, BonemealSource source) {
        return true;
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state, BonemealSource source) {
        return true;
    }

    /** A seed pops out of the flower, as from a block that drops an item (delay 10 ticks before pick-up). */
    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state, BonemealSource source) {
        float drift = 0.7F;
        double x = pos.getX() + random.nextFloat() * drift + (1.0F - drift) * 0.5;
        double y = pos.getY() + random.nextFloat() * drift + (1.0F - drift) * 0.2 + 0.6;
        double z = pos.getZ() + random.nextFloat() * drift + (1.0F - drift) * 0.5;
        ItemEntity item = new ItemEntity(level, x, y, z, new ItemStack(OffLawn.SUNFLOWER_SEED.get()));
        item.setPickUpDelay(10);
        level.addFreshEntity(item);
    }
}
