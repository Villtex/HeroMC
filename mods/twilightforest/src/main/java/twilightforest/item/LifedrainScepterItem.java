package twilightforest.item;

import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

/**
 * ItemTFLifeDrainWand: 99 charges, every 5 ticks of holding it down on the living thing looked at within 20 blocks
 * (through walls, as in 1.5.2): a fresh target takes 1 damage and slowness III for a second; a slowed one takes 3
 * and feeds the player (1 health and 1 food every 10 ticks); one with 3 health or less shatters and dies. Targets no
 * bigger than the player are held up (motion 0, 0.2, 0). A red trail of spell particles shows the drain.
 */
public class LifedrainScepterItem extends Item {
    public LifedrainScepterItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (TFItemUtil.hasCharge(player.getItemInHand(hand))) {
            player.startUsingItem(hand);
            return InteractionResult.CONSUME;
        }
        return InteractionResult.FAIL;
    }

    /** getPlayerLookTarget: the nearest entity whose box (+ border) the look line of 20 blocks hits. */
    private static @Nullable Entity lookTarget(LivingEntity player) {
        double range = 20.0;
        Vec3 src = player.getEyePosition();
        Vec3 look = player.getViewVector(1.0F);
        Vec3 dest = src.add(look.scale(range));
        AABB box = player.getBoundingBox().expandTowards(look.scale(range)).inflate(1.0);
        EntityHitResult hit = ProjectileUtil.getEntityHitResult(player, src, dest, box, Entity::isPickable, range * range);
        return hit == null ? null : hit.getEntity();
    }

    @Override
    public void onUseTick(Level level, LivingEntity user, ItemStack stack, int count) {
        if (!TFItemUtil.hasCharge(stack)) {
            user.releaseUsingItem();
            return;
        }
        if (count % 5 != 0 || !(lookTarget(user) instanceof LivingEntity target) || !(level instanceof ServerLevel server)) {
            return;
        }
        DamageSource source = user.damageSources().indirectMagic(user, user);
        if (target.getEffect(MobEffects.SLOWNESS) == null && target.getHealth() >= 1.0F) {
            trail(server, user, target);
            server.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.FLINTANDSTEEL_USE, user.getSoundSource(), 1.0F,
                    (server.getRandom().nextFloat() - server.getRandom().nextFloat()) * 0.2F + 1.0F);
            target.hurtServer(server, source, 1.0F);
            hold(target, user);
            target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 20, 2));
        } else if (target.getHealth() <= 3.0F) {
            trail(server, user, target);
            server.broadcastEntityEvent(target, (byte) 60);
            server.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.PLAYER_BIG_FALL, target.getSoundSource(), 1.0F,
                    ((server.getRandom().nextFloat() - server.getRandom().nextFloat()) * 0.7F + 1.0F) * 2.0F);
            animateTargetShatter(server, target);
            target.die(source);
            target.discard();
            user.releaseUsingItem();
        } else {
            target.hurtServer(server, source, 3.0F);
            hold(target, user);
            target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 20, 2));
            if (count % 10 == 0 && user instanceof Player player) {
                player.heal(1.0F);
                player.getFoodData().eat(1, 0.1F);
            }
        }
        TFItemUtil.useCharge(stack, 1);
    }

    private static void hold(LivingEntity target, LivingEntity user) {
        if (target.getMaxHealth() <= user.getMaxHealth()) {
            target.setDeltaMovement(0.0, 0.2, 0.0);
            target.needsSync = true;
        }
    }

    /** iconcrack of rotten flesh all over the target. */
    public static void animateTargetShatter(ServerLevel level, LivingEntity target) {
        for (int i = 0; i < 50; i++) {
            double gx = level.getRandom().nextGaussian() * 0.02;
            double gy = level.getRandom().nextGaussian() * 0.02;
            double gz = level.getRandom().nextGaussian() * 0.02;
            level.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, Items.ROTTEN_FLESH),
                    target.getX() + level.getRandom().nextFloat() * target.getBbWidth() * 2.0F - target.getBbWidth() - gx * 10.0,
                    target.getY() + level.getRandom().nextFloat() * target.getBbHeight() - gy * 10.0,
                    target.getZ() + level.getRandom().nextFloat() * target.getBbWidth() * 2.0F - target.getBbWidth() - gz * 10.0,
                    0, gx, gy, gz, 1.0);
        }
    }

    /** makeRedMagicTrail: 32 spell particles (1.0, 0.5, 0.5) from the eyes to the target's eyes. */
    private static void trail(ServerLevel level, LivingEntity from, LivingEntity to) {
        Vec3 src = from.getEyePosition();
        Vec3 dest = to.getEyePosition();
        for (int i = 0; i < 32; i++) {
            double f = i / 31.0;
            level.sendParticles(ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, 1.0F, 0.5F, 0.5F),
                    src.x + (dest.x - src.x) * f + level.getRandom().nextGaussian() * 0.005,
                    src.y + (dest.y - src.y) * f + level.getRandom().nextGaussian() * 0.005,
                    src.z + (dest.z - src.z) * f + level.getRandom().nextGaussian() * 0.005, 1, 0.0, 0.0, 0.0, 0.0);
        }
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.BOW;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        TFItemUtil.chargesTooltip(stack, tooltip);
    }
}
