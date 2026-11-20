package xreliquary.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import xreliquary.XRBlocks;

/** TEAltar: "cycleTime", "redstoneCount", "isActive". */
public class AltarBlockEntity extends BlockEntity {
    private int cycleTime;
    private boolean active;
    private int redstoneCount;

    public AltarBlockEntity(BlockPos pos, BlockState state) {
        super(XRBlocks.ALTAR_ENTITY.get(), pos, state);
    }

    public int redstoneCount() {
        return this.redstoneCount;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, AltarBlockEntity altar) {
        if (altar.active && level.getOverworldClockTime() % 24000L < 12000L && level.canSeeSky(pos.above())) {
            if (altar.cycleTime > 0) {
                altar.cycleTime--;
            } else {
                altar.active = false;
                level.setBlock(pos.above(), Blocks.GLOWSTONE.defaultBlockState(), 3);
                altar.updateState();
            }
            altar.setChanged();
        }
    }

    public void addRedstone() {
        this.redstoneCount++;
        if (this.redstoneCount > 2) {
            this.cycleTime = (int) (24000.0 + 6000.0 * this.level.getRandom().nextGaussian());
            this.active = true;
            this.redstoneCount = 0;
            this.updateState();
        }
        this.setChanged();
    }

    private void updateState() {
        if (this.level != null && this.getBlockState().getValue(AltarBlock.ACTIVE) != this.active) {
            this.level.setBlock(this.worldPosition, this.getBlockState().setValue(AltarBlock.ACTIVE, this.active), 3);
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putShort("cycleTime", (short) this.cycleTime);
        output.putShort("redstoneCount", (short) this.redstoneCount);
        output.putBoolean("isActive", this.active);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.cycleTime = input.getShortOr("cycleTime", (short) 0);
        this.redstoneCount = input.getShortOr("redstoneCount", (short) 0);
        this.active = input.getBooleanOr("isActive", false);
    }
}
