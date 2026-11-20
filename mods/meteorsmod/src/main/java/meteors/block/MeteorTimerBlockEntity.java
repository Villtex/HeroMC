package meteors.block;

import meteors.MeteorData;
import meteors.MeteorsMod;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * TileEntityMeteorTimer: power 15 - ticksLeft / 800 (0..15); in quick mode only 15 or 0. As in the original, with no
 * meteor announced the ticks left are 0 and the timer gives 15. The ticks come from the server's announced meteors
 * (in 1.5.2 from the client's copy, so on a dedicated server the timer always gave 15).
 */
public class MeteorTimerBlockEntity extends BlockEntity {
    public boolean quickMode;

    public MeteorTimerBlockEntity(BlockPos pos, BlockState state) {
        super(MeteorsMod.METEOR_TIMER_ENTITY.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, MeteorTimerBlockEntity be) {
        if (!(level instanceof ServerLevel server)) {
            return;
        }
        MeteorData.Ghost nearest = MeteorData.get(server.getServer()).nearestTimeMeteor();
        int ticksLeft = nearest != null ? nearest.remainingTicks() : 0;
        int meta = Mth.clamp(15 - Math.min(ticksLeft / 800, 15), 0, 15);
        if (be.quickMode && meta != 15) {
            meta = 0;
        }
        if (state.getValue(MeteorTimerBlock.POWER) != meta) {
            level.setBlock(pos, state.setValue(MeteorTimerBlock.POWER, meta), 3);
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putBoolean("mode", this.quickMode);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.quickMode = input.getBooleanOr("mode", false);
    }
}
