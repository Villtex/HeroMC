package xreliquary.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import xreliquary.XRData;
import xreliquary.XRFx;

/**
 * ItemEmperorChalice: picks up a water source (item damage 1 = filled, the overlay flag here) and then places water
 * without ever running dry; sneaking while placing empties it. Filled and aimed at nothing it is drunk: 1 food,
 * 0.5 saturation and 1 drowning damage.
 */
public class EmperorChaliceItem extends XRItem {
    public EmperorChaliceItem(Properties properties) {
        super(properties, 3);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 16;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.DRINK;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide() && entity instanceof Player player) {
            player.getFoodData().eat(1, 0.5F);
            player.hurtServer((net.minecraft.server.level.ServerLevel) level, level.damageSources().drown(), 1.0F);
            ItemStack empty = stack.copy();
            XRData.setFlag(empty, false);
            return empty;
        }
        return stack;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        boolean filled = XRData.flag(stack);
        BlockHitResult hit = getPlayerPOVHitResult(level, player, filled ? ClipContext.Fluid.NONE : ClipContext.Fluid.SOURCE_ONLY);
        if (hit.getType() != HitResult.Type.BLOCK) {
            if (filled) {
                player.startUsingItem(hand);
                return InteractionResult.CONSUME;
            }
            return InteractionResult.PASS;
        }
        BlockPos pos = hit.getBlockPos();
        if (!level.mayInteract(player, pos) || !player.mayUseItemAt(pos, hit.getDirection(), stack)) {
            return InteractionResult.PASS;
        }
        if (level.getFluidState(pos).is(FluidTags.WATER) && level.getFluidState(pos).isSource()) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 11);
            XRData.setFlag(stack, true);
            return InteractionResult.SUCCESS;
        }
        if (filled) {
            BlockPos place = pos.relative(hit.getDirection());
            if (!player.mayUseItemAt(place, hit.getDirection(), stack)) {
                return InteractionResult.PASS;
            }
            if (place(level, place) && !XRFx.creative(player)) {
                XRData.setFlag(stack, !player.isShiftKeyDown());
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    /** tryPlaceContainedLiquid: flowing water level 0, evaporates in the Nether. */
    private static boolean place(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (!state.isAir() && state.isSolid()) {
            return false;
        }
        if (level.environmentAttributes().getValue(net.minecraft.world.attribute.EnvironmentAttributes.WATER_EVAPORATES, pos)) {
            XRFx.sound(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.FIRE_EXTINGUISH, 0.5F,
                    2.6F + (level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 0.8F);
            for (int i = 0; i < 8; i++) {
                XRFx.particle(level, ParticleTypes.LARGE_SMOKE, pos.getX() + Math.random(), pos.getY() + Math.random(), pos.getZ() + Math.random(), 0, 0, 0);
            }
        } else if (!level.isClientSide()) {
            level.setBlock(pos, Blocks.WATER.defaultBlockState(), 3);
        }
        return true;
    }
}
