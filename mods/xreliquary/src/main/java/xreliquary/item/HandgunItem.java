package xreliquary.item;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;
import xreliquary.XRData;
import xreliquary.XRFx;
import xreliquary.XRItems;
import xreliquary.XRSounds;
import xreliquary.entity.ShotEntity;

/**
 * ItemHandgun: fires the loaded shot on right click (cooldown 12), held with an empty gun it reloads from a filled
 * magazine of the main inventory after 12..32 ticks (fewer with more levels; cooldown 24), giving back the empty
 * magazine; every shot leaves an empty casing. "cooldownTime", "bulletCount", "bulletType" in custom data, the item
 * damage ((8 - count) << 5) + type as in 1.5.2 (max 267), the overlay colour by the shot.
 */
public class HandgunItem extends XRItem {
    private static final int USE_DURATION = 256;

    public HandgunItem(Properties properties) {
        super(properties, 2);
    }

    public static ShotType loaded(ItemStack stack) {
        return ShotType.of(XRData.getShort(stack, "bulletType"));
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity owner, @Nullable EquipmentSlot slot) {
        int cooldown = XRData.getShort(stack, "cooldownTime");
        if (cooldown > 0) {
            XRData.setShort(stack, "cooldownTime", cooldown - 1);
        }
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (XRData.getShort(stack, "cooldownTime") <= 0) {
            if (XRData.getShort(stack, "bulletCount") <= 0 && XRData.getShort(stack, "bulletType") <= 0) {
                player.startUsingItem(hand);
                return InteractionResult.CONSUME;
            }
            this.fire(stack, level, player);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int count) {
        if (!(entity instanceof Player player) || level.isClientSide()) {
            return;
        }
        if (!hasSpareClip(player)) {
            XRData.setShort(stack, "cooldownTime", 12);
            player.stopUsingItem();
            return;
        }
        if (USE_DURATION - count >= skillTimer(player) - 1) {
            XRData.setShort(stack, "cooldownTime", 24);
            int type = takeClip(player);
            XRData.setShort(stack, "bulletType", type);
            if (type != 0) {
                player.swing(InteractionHand.MAIN_HAND, net.minecraft.world.item.component.SwingAnimation.DEFAULT, true);
                XRFx.give(player, new ItemStack(XRItems.MAGAZINES.get(0).get()));
                XRData.setShort(stack, "bulletCount", 8);
                XRFx.sound(player, XRSounds.LOAD.get(), 0.25F, 1.0F);
            }
            if (XRData.getShort(stack, "bulletCount") == 0) {
                XRData.setShort(stack, "bulletType", 0);
            }
            setDamageByContents(stack);
            player.stopUsingItem();
        }
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.BLOCK;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return USE_DURATION;
    }

    private void fire(ItemStack stack, Level level, Player player) {
        if (level.isClientSide()) {
            return;
        }
        XRData.setShort(stack, "cooldownTime", 12);
        ShotType type = loaded(stack);
        if (type == ShotType.EMPTY) {
            return;
        }
        level.addFreshEntity(new ShotEntity(level, player, type));
        XRFx.sound(player, XRSounds.SHOT.get(), 0.2F, 1.2F);
        int count = XRData.getShort(stack, "bulletCount") - 1;
        XRData.setShort(stack, "bulletCount", count);
        if (count == 0) {
            XRData.setShort(stack, "bulletType", 0);
        }
        XRFx.give(player, new ItemStack(XRItems.BULLETS.get(0).get()));
        setDamageByContents(stack);
    }

    private static void setDamageByContents(ItemStack stack) {
        int count = XRData.getShort(stack, "bulletCount");
        int type = XRData.getShort(stack, "bulletType");
        stack.setDamageValue(((8 - count) << 5) + type);
        stack.set(net.minecraft.core.component.DataComponents.CUSTOM_MODEL_DATA, new net.minecraft.world.item.component.CustomModelData(
                java.util.List.of(), java.util.List.of(type != 0), java.util.List.of(), java.util.List.of(ShotType.of(type).color)));
    }

    private static boolean hasSpareClip(Player player) {
        return clipSlot(player) >= 0;
    }

    private static int clipSlot(Player player) {
        for (int slot = 0; slot < 36; slot++) {
            if (player.getInventory().getItem(slot).getItem() instanceof AmmoItem ammo && ammo.magazine && ammo.shot != ShotType.EMPTY) {
                return slot;
            }
        }
        return -1;
    }

    private static int takeClip(Player player) {
        int slot = clipSlot(player);
        if (slot < 0) {
            return 0;
        }
        int type = ((AmmoItem) player.getInventory().getItem(slot).getItem()).shot.ordinal();
        player.getInventory().removeItem(slot, 1);
        return type;
    }

    private static int skillTimer(Player player) {
        return player.experienceLevel >= 20 ? 12 : 20 - player.experienceLevel + 12;
    }

    @Override
    public Component getName(ItemStack stack) {
        return super.getName(stack);
    }
}
