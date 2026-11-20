package tconstruct.smeltery;

import net.minecraft.core.BlockPos;

/** IMasterLogic: told when one of its blocks changes. */
public interface MasterLogic {
    void notifyChange(BlockPos pos);
}
