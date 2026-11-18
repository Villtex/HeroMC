package betterdungeons.item;

import betterdungeons.BDFx;
import betterdungeons.api.BDEntityIds;
import betterdungeons.api.BDSummonedMob;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * ItemDagger (iron and diamond dagger; ItemSword damage minus 2): a hit from behind (attacker and target facing within
 * 50 degrees of the same way) hurts once more with double the dagger's damage. Subclasses: ItemDaggerEnd (ninja
 * dagger) and ItemDaggerNinja (trickster dagger).
 */
public class DaggerItem extends Item {
    private final int damage;

    public DaggerItem(Properties properties, int damage) {
        super(properties);
        this.damage = damage;
    }

    @Override
    public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        double angle = attacker.getYRot() - target.getYRot();
        while (angle > 360.0) {
            angle -= 360.0;
        }
        while (angle < 0.0) {
            angle += 360.0;
        }
        angle = Math.abs(angle - 180.0);
        if (angle > 130.0 && attacker instanceof Player player) {
            if (target.level() instanceof ServerLevel server) {
                for (int i = 0; i < 5; i++) {
                    server.sendParticles(ParticleTypes.CRIT, target.getX(), target.getY() + 1.0 + BDItemUtil.RAND.nextDouble(), target.getZ(), 0,
                            BDItemUtil.RAND.nextDouble() - 0.5, -0.5 + BDItemUtil.RAND.nextDouble(), BDItemUtil.RAND.nextDouble() - 0.5, 1.0);
                }
            }
            BDFx.hurt(target, target.damageSources().playerAttack(player), this.damage * 2);
        }
    }

    /** ItemDaggerEnd (ninja dagger, 2048 uses): sneak + use teleports 4 blocks; any use makes the player invisible. */
    public static class Ninja extends DaggerItem {
        public Ninja(Properties properties, int damage) {
            super(properties, damage);
        }

        @Override
        public InteractionResult use(Level level, Player player, InteractionHand hand) {
            ItemStack stack = player.getItemInHand(hand);
            if (player.isShiftKeyDown()) {
                BDItemUtil.teleportForward(player);
                stack.hurtAndBreak(2, player, hand);
            } else {
                stack.hurtAndBreak(1, player, hand);
            }
            player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 100, 5));
            return InteractionResult.SUCCESS;
        }
    }

    /** ItemDaggerNinja (trickster dagger): sneak + use leaves a bait (EntityBaiter) behind; any use makes invisible. */
    public static class Trickster extends DaggerItem {
        public Trickster(Properties properties, int damage) {
            super(properties, damage);
        }

        @Override
        public InteractionResult use(Level level, Player player, InteractionHand hand) {
            ItemStack stack = player.getItemInHand(hand);
            if (player.isShiftKeyDown()) {
                EntityType<?> type = BDEntityIds.type("SummonedBait");
                if (level instanceof ServerLevel server && type != null) {
                    Entity bait = type.create(server, EntitySpawnReason.MOB_SUMMONED);
                    if (bait != null) {
                        if (bait instanceof BDSummonedMob summoned) {
                            summoned.initSummoned(player, 0, 0);
                        }
                        bait.setPos(player.getX(), player.getY(), player.getZ());
                        server.addFreshEntity(bait);
                    }
                }
                player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 100, 5));
                stack.hurtAndBreak(20, player, hand);
            } else {
                player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 100, 5));
                stack.hurtAndBreak(1, player, hand);
            }
            return InteractionResult.SUCCESS;
        }
    }
}
