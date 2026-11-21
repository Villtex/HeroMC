package tconstruct.world;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import tconstruct.TSmeltery;
import tconstruct.TWorld;

/**
 * SlimeGrass (metadata 0 on blue slime dirt, 1 on vanilla dirt). In the dark under an opaque block it turns into blue
 * slime dirt; at light 9 it spreads to dirt (as the dirt kind) and to blue slime dirt (as the slime kind).
 */
public class SlimeGrassBlock extends Block {
    public SlimeGrassBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        BlockPos above = pos.above();
        if (level.getMaxLocalRawBrightness(above) < 4 && level.getBlockState(above).getLightDampening() > 2) {
            level.setBlock(pos, TSmeltery.SOIL_BLOCKS.get(5).get().defaultBlockState(), Block.UPDATE_ALL);
        } else if (level.getMaxLocalRawBrightness(above) >= 9) {
            for (int i = 0; i < 4; i++) {
                BlockPos target = pos.offset(random.nextInt(3) - 1, random.nextInt(5) - 3, random.nextInt(3) - 1);
                BlockPos targetAbove = target.above();
                if (level.getMaxLocalRawBrightness(targetAbove) >= 4 && level.getBlockState(targetAbove).getLightDampening() <= 2) {
                    BlockState found = level.getBlockState(target);
                    if (found.is(Blocks.DIRT)) {
                        level.setBlock(target, TWorld.SLIME_GRASS_DIRT.get().defaultBlockState(), Block.UPDATE_ALL);
                        return;
                    }
                    if (found.is(TSmeltery.SOIL_BLOCKS.get(5).get())) {
                        level.setBlock(target, TWorld.SLIME_GRASS.get().defaultBlockState(), Block.UPDATE_ALL);
                    }
                }
            }
        }
    }
}
