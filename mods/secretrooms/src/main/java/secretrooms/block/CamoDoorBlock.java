package secretrooms.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import org.jspecify.annotations.Nullable;

/**
 * BlockCamoDoor: a door (wood opens by hand, iron only by redstone) whose two halves wear the block below it
 * (grass under it turns to dirt when it is placed, as ItemCamoDoor did).
 */
public class CamoDoorBlock extends DoorBlock implements EntityBlock, CamoSource {
    public CamoDoorBlock(BlockSetType type, Properties properties) {
        super(type, properties);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CamoBlockEntity(pos, state);
    }

    @Override
    public @Nullable BlockState chooseCamo(Level level, BlockPos pos, BlockState state) {
        BlockPos floor = state.getValue(HALF) == DoubleBlockHalf.LOWER ? pos.below() : pos.below(2);
        BlockState below = level.getBlockState(floor);
        return below.isAir() ? null : below;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if (level.getBlockState(pos.below()).is(Blocks.GRASS_BLOCK)) {
            level.setBlock(pos.below(), Blocks.DIRT.defaultBlockState(), 3);
        }
        super.setPlacedBy(level, pos, state, placer, stack);
    }
}
