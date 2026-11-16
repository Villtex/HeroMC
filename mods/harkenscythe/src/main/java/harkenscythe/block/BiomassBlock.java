package harkenscythe.block;

import harkenscythe.HSBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * BlockHSBiomass: grows dried blood on the air above it, and turns dried blood on it back into fresh red blood
 * (stage 3 or 2), which then stays (blood on biomass does not dry). Chance 1 in 7, 1 in 3 next to other biomass blocks.
 */
public class BiomassBlock extends Block {
    public BiomassBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        BlockPos above = pos.above();
        BlockState blood = level.getBlockState(above);
        if (level.isEmptyBlock(above)) {
            if (random.nextInt((int) (25.0F / this.growthRate(level, pos)) + 1) == 0) {
                level.setBlock(above, HSBlocks.BLOOD_SPLASH_RESIDUE.get().fromMeta(4), Block.UPDATE_CLIENTS);
                return;
            }
        }
        if (BloodSplashBlock.isDried(blood) && blood.getValue(BloodSplashBlock.COLOR) == BloodSplashBlock.BloodColor.RED) {
            if (random.nextInt((int) (25.0F / this.growthRate(level, pos)) + 1) == 0) {
                int stage = 4 - 1 - random.nextInt(2);
                level.setBlock(above, blood.setValue(BloodSplashBlock.STAGE, stage), Block.UPDATE_CLIENTS);
            }
        }
    }

    /** Always 4, halved next to biomass blocks on both axes or a diagonal. */
    private float growthRate(ServerLevel level, BlockPos pos) {
        float rate = 1.0F + 1.0F + 8 * 0.25F;
        boolean x = this.is(level, pos.west()) || this.is(level, pos.east());
        boolean z = this.is(level, pos.north()) || this.is(level, pos.south());
        boolean diagonal = this.is(level, pos.offset(-1, 0, -1)) || this.is(level, pos.offset(1, 0, -1)) || this.is(level, pos.offset(1, 0, 1))
                || this.is(level, pos.offset(-1, 0, 1));
        if (diagonal || x && z) {
            rate /= 2.0F;
        }
        return rate;
    }

    private boolean is(ServerLevel level, BlockPos pos) {
        return level.getBlockState(pos).is(this);
    }
}
