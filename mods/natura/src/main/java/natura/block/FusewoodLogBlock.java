package natura.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;

/** DarkTreeBlock metadata 1: fusewood explodes when a player breaks it (strength 2, 1.75 on hard). */
public class FusewoodLogBlock extends RotatedPillarBlock {
    public FusewoodLogBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide()) {
            float power = level.getDifficulty() == Difficulty.HARD ? 1.75F : 2.0F;
            level.explode(null, pos.getX(), pos.getY(), pos.getZ(), power, false, Level.ExplosionInteraction.BLOCK);
        }
        return super.playerWillDestroy(level, pos, state, player);
    }
}
