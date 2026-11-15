package offlawn.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealSource;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import offlawn.OffLawn;

/**
 * LawnBlock: grass coloured like the grass of the biome that neither spreads nor turns to dirt. Plants grow on it
 * (block tags), trees and huge mushrooms leave it in place, a hoe tills it (LawnHoe, tag turns_into_farmland) and bone meal grows grass and
 * flowers around it (LawnBonemeal), on lawn only.
 */
public class LawnBlock extends Block implements BonemealableBlock {
    public LawnBlock(Properties properties) {
        super(properties);
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
    public Type getType() {
        return Type.NEIGHBOR_SPREADER;
    }

    /** LawnBonemeal.onUseBonemeal, with the random draws of the original. */
    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state, BonemealSource source) {
        Block lawn = OffLawn.LAWN.get();
        for (int i = 0; i < 128; i++) {
            int x = pos.getX();
            int y = pos.getY() + 1;
            int z = pos.getZ();
            boolean blocked = false;
            for (int j = 0; j < i / 16; j++) {
                x += random.nextInt(3) - 1;
                y += (random.nextInt(3) - 1) * random.nextInt(3) / 2;
                z += random.nextInt(3) - 1;
                BlockPos at = new BlockPos(x, y, z);
                if (!level.getBlockState(at.below()).is(lawn) || level.getBlockState(at).isRedstoneConductor(level, at)) {
                    blocked = true;
                    break;
                }
            }
            BlockPos at = new BlockPos(x, y, z);
            if (!blocked && level.getBlockState(at).isAir()) {
                // always on lawn here: the start is above this block and every step checked the soil
                BlockState plant;
                if (random.nextInt(10) != 0) {
                    plant = Blocks.SHORT_GRASS.defaultBlockState();
                } else {
                    plant = (random.nextInt(2) == 0 ? Blocks.POPPY : Blocks.DANDELION).defaultBlockState();
                }
                if (plant.canSurvive(level, at)) {
                    level.setBlock(at, plant, Block.UPDATE_ALL);
                }
            }
        }
    }
}
