package meteors.block;

import meteors.MeteorsMod;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;

/**
 * BlockFrezarite: slippery like ice, glowing (0.25); from a crash it carries a charge 1..4 (metadata) that melts away
 * by one per random tick where the biome is warmer than 0.15, and the block is water at 0. Placed Frezarite (0) stays.
 */
public class FrezariteBlock extends Block {
    public FrezariteBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(MeteorBlock.HEAT, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(MeteorBlock.HEAT);
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int heat = state.getValue(MeteorBlock.HEAT);
        float temp = level.getBiome(pos).value().getBaseTemperature();
        if (heat > 0 && temp > 0.15F) {
            heat--;
            level.setBlock(pos, state.setValue(MeteorBlock.HEAT, heat), 2);
            if (heat <= 0) {
                level.setBlock(pos, Blocks.WATER.defaultBlockState(), 2);
            }
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (state.getValue(MeteorBlock.HEAT) > 0) {
            MeteorBlock.glowParticles(level, pos, random, MeteorsMod.FREZA_DUST.get());
        }
    }
}
