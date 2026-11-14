package shadwdrgn.soulshards;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/** ItemShadowPickaxe: unless sneaking, every block of the 3x3x3 around the mined one breaks too, with a bang. */
public class ShadowPickaxeItem extends Item {
    public ShadowPickaxeItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity miner) {
        if (miner.isShiftKeyDown() || !(level instanceof ServerLevel server)) {
            return super.mineBlock(stack, level, state, pos, miner);
        }
        for (BlockPos p : BlockPos.betweenClosed(pos.offset(-1, -1, -1), pos.offset(1, 1, 1))) {
            BlockState around = level.getBlockState(p);
            if (!around.isAir()) {
                // 1.5.2 broke everything here, bedrock included, and dropped it as if mined
                Block.dropResources(around, level, p, level.getBlockEntity(p), miner, stack);
                level.removeBlock(p, false);
                stack.hurtAndBreak(1, miner, EquipmentSlot.MAINHAND);
            }
        }
        level.playSound(null, pos, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.BLOCKS, 4.0F,
                (1.0F + (level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 0.2F) * 0.7F);
        server.sendParticles(ParticleTypes.EXPLOSION_EMITTER, pos.getX(), pos.getY(), pos.getZ(), 1, 0.0, 0.0, 0.0, 0.0);
        return true;
    }
}
