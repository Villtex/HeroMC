package betterdungeons.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

/**
 * BlockCapsule (CapsuleStation, block id 2001 of the pack): one block per 1.5.2 metadata (0 editable, 1 storage, 2
 * small, 3 medium, 4 big); placing it sets the area of its block entity (ItemCapsuleStation.onItemUse). Each drops
 * itself (damageDropped = metadata). The editable station opened GuiCapsule (width, height, name) in 1.5.2.
 */
public class CapsuleStationBlock extends BaseEntityBlock {
    private final int kind;

    public CapsuleStationBlock(Properties properties, int kind) {
        super(properties);
        this.kind = kind;
    }

    public int kind() {
        return this.kind;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CapsuleStationBlockEntity(pos, state);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof CapsuleStationBlockEntity station) {
            station.setCapsuleSize(this.kind);
        }
    }
}
