package xreliquary.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import xreliquary.XRData;

/**
 * ItemSojournerStaff: stores torches (513) and places them on the block looked at up to 32 blocks away; one torch
 * plus one per 6 blocks of distance, cooldown 10. The placing goes through the vanilla torch item, which knows
 * standing and wall torches.
 */
public class SojournerStaffItem extends ChargeItem {
    public SojournerStaffItem(Properties properties) {
        super(properties, 2);
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity owner, @Nullable EquipmentSlot slot) {
        if (owner instanceof Player player) {
            refuel(stack, player, Items.TORCH, 1, stack.getMaxDamage() - 1);
            int cooldown = XRData.getShort(stack, "cooldown");
            if (cooldown > 0) {
                XRData.setShort(stack, "cooldown", cooldown - 1);
            }
        }
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (context.getPlayer() == null) {
            return InteractionResult.PASS;
        }
        BlockHitResult hit = new BlockHitResult(context.getClickLocation(), context.getClickedFace(), context.getClickedPos(), context.isInside());
        return this.place(context.getLevel(), context.getPlayer(), context.getHand(), hit);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (XRData.getShort(player.getItemInHand(hand), "cooldown") > 0) {
            return InteractionResult.PASS;
        }
        Vec3 eye = player.getEyePosition();
        BlockHitResult hit = level.clip(new ClipContext(eye, eye.add(player.getViewVector(1.0F).scale(32.0)), ClipContext.Block.OUTLINE,
                ClipContext.Fluid.SOURCE_ONLY, player));
        if (hit.getType() == HitResult.Type.BLOCK) {
            return this.place(level, player, hand, hit);
        }
        return InteractionResult.PASS;
    }

    private InteractionResult place(Level level, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack stack = player.getItemInHand(hand);
        if (XRData.getShort(stack, "cooldown") > 0 || !player.mayUseItemAt(hit.getBlockPos(), hit.getDirection(), stack)) {
            return InteractionResult.PASS;
        }
        ItemStack torch = new ItemStack(Items.TORCH);
        BlockPlaceContext ctx = new BlockPlaceContext(level, player, hand, torch, hit);
        BlockPos pos = ctx.getClickedPos();
        boolean creative = player.getAbilities().instabuild;
        int cost = 1;
        if (!creative) {
            if (stack.getDamageValue() == 0) {
                return InteractionResult.PASS;
            }
            for (int distance = (int) Math.sqrt(player.distanceToSqr(pos.getX(), pos.getY(), pos.getZ())); distance > 6; distance -= 6) {
                cost++;
            }
            if (stack.getDamageValue() >= stack.getMaxDamage() - cost) {
                return InteractionResult.PASS;
            }
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        InteractionResult result = ((BlockItem) Items.TORCH).place(ctx);
        if (result.consumesAction()) {
            if (!creative) {
                stack.setDamageValue(stack.getDamageValue() + cost);
            }
            double gauss = 0.5 + level.getRandom().nextFloat() / 2.0F;
            ((ServerLevel) level).sendParticles(ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, (float) gauss, (float) gauss, 0.0F),
                    pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 1, 0, 0, 0, 0);
            player.swing(hand, net.minecraft.world.item.component.SwingAnimation.DEFAULT, true);
            XRData.setShort(stack, "cooldown", 10);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }
}
