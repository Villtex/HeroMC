package legendgear.block;

import legendgear.LGBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * TileEntityPedestal: the sword in the pedestal. Over a skybeam block (two below) and with redstone power the sword is
 * repaired by one point per 6000 ticks, also for the time the chunk was not loaded ("lastWorldTime", "storedCharge").
 */
public class PedestalBlockEntity extends JarBlockEntity {
    public static final int CHARGE_PER_POINT = 6000;
    public long lastActiveWorldTime;
    public long storedCharge;

    public PedestalBlockEntity(BlockPos pos, BlockState state) {
        super(LGBlocks.PEDESTAL_ENTITY.get(), pos, state);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putLong("lastWorldTime", this.lastActiveWorldTime);
        output.putLong("storedCharge", this.storedCharge);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.lastActiveWorldTime = input.getLongOr("lastWorldTime", 0L);
        this.storedCharge = input.getLongOr("storedCharge", 0L);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, PedestalBlockEntity be) {
        long worldAge = level.getGameTime();
        if (be.lastActiveWorldTime == 0L) {
            be.lastActiveWorldTime = worldAge;
        }
        boolean skybeam = level.getBlockState(pos.below(2)).is(LGBlocks.SKYBEAM.get()) && level.hasNeighborSignal(pos);
        if (skybeam && !be.contents.isEmpty() && !level.isClientSide()) {
            if (be.lastActiveWorldTime < worldAge - 2L) {
                be.storedCharge += worldAge - be.lastActiveWorldTime;
            } else if (level.getNearestPlayer(pos.getX(), pos.getY(), pos.getZ(), 128.0, false) == null) {
                be.storedCharge++;
            }
        }
        be.lastActiveWorldTime = worldAge;
        int points = (int) (be.storedCharge / CHARGE_PER_POINT);
        be.storedCharge -= (long) points * CHARGE_PER_POINT;
        if (points > 0 && !be.contents.isEmpty() && be.contents.isDamageableItem() && !level.isClientSide()) {
            be.contents.setDamageValue(Math.max(0, be.contents.getDamageValue() - points));
            be.setContents(be.contents);
        }
        if (!level.isClientSide()) {
            be.setChanged();
        }
    }
}
