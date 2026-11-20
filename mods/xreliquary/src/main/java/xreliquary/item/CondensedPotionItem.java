package xreliquary.item;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import xreliquary.XRFx;
import xreliquary.XRItems;
import xreliquary.entity.ThrownEntity;

/**
 * ItemCondensedPotion, one item per damage value: the empty vial fills from a water source, the splash serums (1..10)
 * are thrown, the elixirs (13..27) are drunk (the {@code minecraft:consumable} component set in {@link XRItems}).
 */
public class CondensedPotionItem extends XRItem {
    public final PotionKind kind;

    public CondensedPotionItem(Properties properties, PotionKind kind) {
        super(properties, 2);
        this.kind = kind;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return this.kind.hasEffect();
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (this.kind == PotionKind.EMPTY_VIAL) {
            BlockHitResult hit = getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);
            if (hit.getType() == HitResult.Type.BLOCK && level.getFluidState(hit.getBlockPos()).is(FluidTags.WATER)
                    && level.getFluidState(hit.getBlockPos()).isSource()) {
                ItemStack water = new ItemStack(XRItems.potion(PotionKind.WATER));
                stack.shrink(1);
                if (stack.isEmpty()) {
                    player.setItemInHand(hand, water);
                } else {
                    XRFx.give(player, water);
                }
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.PASS;
        }
        if (this.kind.isSplash()) {
            if (!level.isClientSide()) {
                if (!XRFx.creative(player)) {
                    stack.shrink(1);
                }
                XRFx.sound(player, SoundEvents.ARROW_SHOOT, 0.5F, 0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F));
                level.addFreshEntity(new ThrownEntity(level, player, ThrownEntity.Kind.splash(this.kind)));
            }
            return InteractionResult.SUCCESS;
        }
        return super.use(level, player, hand);
    }
}
