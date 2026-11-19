package dimdoors.block;

import dimdoors.world.DDData.DimData;
import dimdoors.world.DDData.LinkData;
import dimdoors.world.DDWorld;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.phys.BlockHitResult;

/** dimHatch, the Transdimensional Trapdoor: opened by hand, iron or not; falling through it open in a pocket leads out of the chain. */
public class DimHatchBlock extends TrapDoorBlock {
    public DimHatchBlock(BlockSetType type, Properties properties) {
        super(type, properties);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        BlockState toggled = state.cycle(OPEN);
        level.setBlock(pos, toggled, Block.UPDATE_CLIENTS);
        level.playSound(player, pos, toggled.getValue(OPEN) ? SoundEvents.WOODEN_DOOR_OPEN : SoundEvents.WOODEN_DOOR_CLOSE, SoundSource.BLOCKS, 1.0F,
                level.getRandom().nextFloat() * 0.1F + 0.9F);
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effectApplier, boolean isPrecise) {
        if (level.isClientSide() || !state.getValue(OPEN) || !DDWorld.isPocket(level)) {
            return;
        }
        int dim = DDWorld.dimId(level, pos);
        DimData data = DDWorld.data().dim(dim);
        if (data == null) {
            return;
        }
        level.setBlock(pos, state.setValue(OPEN, false), Block.UPDATE_CLIENTS);
        LinkData exit = new LinkData(dim, data.exitDim, 0, 0, 0, data.exitX, data.exitY, data.exitZ, false, 0);
        DDWorld.teleport(level, exit, entity);
    }
}
