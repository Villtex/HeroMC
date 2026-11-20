package legendgear.item;

import legendgear.LGFx;
import legendgear.LGParticles;
import legendgear.LGSounds;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * ItemTitanBand (50 uses): right click on a mob lifts it onto the player's head (one use); used again after 5 ticks of
 * carrying it throws the mob where the player looks. A carried chicken slows the fall. Taking damage, or not holding
 * the band, drops the mob (LGEvents).
 */
public class TitanBandItem extends Item {
    public TitanBandItem(Properties properties) {
        super(properties);
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.BLOCK;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    /** HandleLift (EntityInteractEvent). */
    public static boolean lift(Player player, ItemStack band, Entity target) {
        if (player.level().isClientSide() || !(target instanceof LivingEntity) || !player.getPassengers().isEmpty()) {
            return false;
        }
        target.startRiding(player, true, true);
        LGFx.damage(band, 1, player);
        if (target instanceof Mob mob) {
            mob.playAmbientSound();
        }
        player.getPersistentData().putInt("carryingTime", 0);
        LGFx.sound(player, LGSounds.LIFT.get(), 0.3F, 1.0F);
        player.getPersistentData().putBoolean("TitanLift", true);
        return true;
    }

    public static void drop(Player player) {
        for (Entity rider : player.getPassengers()) {
            rider.stopRiding();
        }
        LGFx.sound(player, LGSounds.THROW.get(), 0.3F, 1.0F);
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity owner, @Nullable EquipmentSlot slot) {
        int carry = owner.getPersistentData().getIntOr("carryingTime", 0);
        if (owner instanceof Player player) {
            Entity rider = player.getFirstPassenger();
            if (player.getMainHandItem().getItem() == this && rider != null) {
                carry++;
                LGParticles.rune(level, rider.getX(), rider.getY(), rider.getZ(), level.getRandom().nextGaussian() * 0.1,
                        level.getRandom().nextGaussian() * 0.1, level.getRandom().nextGaussian() * 0.1, 1.0F);
            } else {
                carry = 0;
            }
        }
        owner.getPersistentData().putInt("carryingTime", carry);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        Entity toThrow = player.getFirstPassenger();
        if (toThrow != null && !level.isClientSide() && player.getPersistentData().getIntOr("carryingTime", 0) > 5) {
            toThrow.stopRiding();
            float f = 0.8F;
            float yaw = player.getYRot() / 180.0F * (float) Math.PI;
            float pitch = player.getXRot() / 180.0F * (float) Math.PI;
            toThrow.setDeltaMovement(new Vec3(-Mth.sin(yaw) * Mth.cos(pitch) * f, -Mth.sin(pitch) * f + 0.2, Mth.cos(yaw) * Mth.cos(pitch) * f));
            toThrow.needsSync = true;
            if (toThrow instanceof Mob mob) {
                mob.playAmbientSound();
            }
            toThrow.setYRot(player.getYRot());
            LGFx.sound(player, LGSounds.THROW.get(), 0.3F, 1.0F);
            player.getPersistentData().putBoolean("TitanLift", false);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    /** onUpdate: a carried chicken lets its carrier glide (both sides, the client moves the player). */
    public static void glide(Player player) {
        if (player.getMainHandItem().getItem() instanceof TitanBandItem && player.getFirstPassenger() instanceof Chicken && !player.onGround()
                && player.getDeltaMovement().y < 0.0) {
            player.setDeltaMovement(player.getDeltaMovement().multiply(1.0, 0.6, 1.0));
            player.resetFallDistance();
        }
    }
}
