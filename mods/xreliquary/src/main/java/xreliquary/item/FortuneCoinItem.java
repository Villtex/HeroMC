package xreliquary.item;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import xreliquary.XRData;
import xreliquary.XRFx;

/**
 * ItemFortuneCoin: switched on (item damage 1, the overlay flag) it teleports one item and one XP orb per tick from
 * within 5 blocks to its holder, 15 while held up (sneak + right click). Toggling plays the orb sound three times
 * ("soundTimer"); disableCoinAudio is false in the pack.
 */
public class FortuneCoinItem extends XRItem {
    public FortuneCoinItem(Properties properties) {
        super(properties, 2);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity owner, @Nullable EquipmentSlot slot) {
        int timer = XRData.getShort(stack, "soundTimer");
        if (timer > 0) {
            if (timer % 2 == 0) {
                XRFx.sound(owner, net.minecraft.sounds.SoundEvents.EXPERIENCE_ORB_PICKUP, 0.1F,
                        0.5F * ((level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 0.7F + 1.8F));
            }
            XRData.setShort(stack, "soundTimer", timer - 1);
        }
        if (XRData.flag(stack) && owner instanceof Player player) {
            scan(level, player, 5.0);
        }
    }

    private static void scan(Level level, Player player, double d) {
        AABB box = new AABB(player.getX() - d, player.getY() - d, player.getZ() - d, player.getX() + d, player.getY() + d, player.getZ() + d);
        for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, box)) {
            if (room(item.getItem(), player)) {
                item.setNoPickUpDelay();
                if (!(player.distanceTo(item) < 1.5)) {
                    pull(item, player);
                    break;
                }
            }
        }
        for (ExperienceOrb orb : level.getEntitiesOfClass(ExperienceOrb.class, box)) {
            player.takeXpDelay = 0;
            if (!(player.distanceTo(orb) < 1.5)) {
                pull(orb, player);
                break;
            }
        }
    }

    private static void pull(Entity item, Player player) {
        Level level = player.level();
        XRFx.particle(level, ParticleTypes.PORTAL, item.getX(), item.getY(), item.getZ(), 0.0, 0.1, 0.0);
        Vec3 look = player.getLookAngle();
        item.setPos(player.getX() + look.x * 0.2, player.getY(), player.getZ() + look.z * 0.2);
        XRFx.particle(level, ParticleTypes.PORTAL, item.getX(), item.getY(), item.getZ(), 0.0, 0.1, 0.0);
        XRFx.orb(player);
    }

    /** checkForRoom. */
    private static boolean room(ItemStack item, Player player) {
        int remaining = item.getCount();
        for (int slot = 0; slot < 36; slot++) {
            ItemStack s = player.getInventory().getItem(slot);
            if (s.isEmpty()) {
                return true;
            }
            if (ItemStack.isSameItemSameComponents(s, item)) {
                if (s.getCount() + remaining <= s.getMaxStackSize()) {
                    return true;
                }
                remaining -= s.getMaxStackSize() - s.getCount();
                if (remaining <= 0) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int count) {
        if (!level.isClientSide() && XRData.flag(stack) && entity instanceof Player player) {
            scan(level, player, 15.0);
        }
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 64;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.BLOCK;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isShiftKeyDown()) {
            player.startUsingItem(hand);
            return InteractionResult.CONSUME;
        }
        if (!level.isClientSide()) {
            XRData.setShort(stack, "soundTimer", 6);
            XRData.setFlag(stack, !XRData.flag(stack));
        }
        return InteractionResult.SUCCESS;
    }
}
