package betterdungeons.item;

import betterdungeons.BDFx;
import betterdungeons.projectile.BDProjectile;
import betterdungeons.projectile.ProjectileType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * ItemBigSword (iron / diamond bigsword, earth crusher, Monking sword): slows its holder (landMovementFactor - 0.01, a
 * movement speed modifier on the main hand); hold use and let go to sweep everything within 40 degrees in front with
 * damage * min(ticks + 1, 60) / 30 + 1. The Monking sword always dealt 6 (its getDamageVsEntity ignored the charge).
 */
public class BigSwordItem extends Item {
    private final int damage;

    public BigSwordItem(Properties properties, int damage) {
        super(properties);
        this.damage = damage;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        player.startUsingItem(hand);
        return InteractionResult.CONSUME;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity user) {
        return 72000;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.BOW;
    }

    /** The damage of a charged sweep; the Monking sword returns 6 regardless. */
    protected int chargedDamage(int useTime) {
        return this.damage * useTime / 30 + 1;
    }

    @Override
    public boolean releaseUsing(ItemStack stack, Level level, LivingEntity entity, int remainingTime) {
        if (!(entity instanceof Player player)) {
            return false;
        }
        int useTime = Math.min(this.getUseDuration(stack, entity) - remainingTime + 1, 60);
        int cached = this.chargedDamage(useTime);
        player.swing(player.getUsedItemHand(), net.minecraft.world.item.component.SwingAnimation.DEFAULT, true);
        if (!level.isClientSide()) {
            Vec3 look = player.getLookAngle();
            AABB box = player.getBoundingBox().expandTowards(look).inflate(2.0);
            for (Entity e : level.getEntities(player, box)) {
                if (e instanceof LivingEntity) {
                    double rot = Math.abs(Math.abs(BDItemUtil.angleBetween(player, e)) - Math.abs(Mth.wrapDegrees(player.getYRot())));
                    if (rot < 40.0) {
                        BDItemUtil.attackWithDamage(player, e, cached);
                        stack.hurtAndBreak(1, player, player.getUsedItemHand());
                    }
                }
            }
        } else {
            float x = (float) -Math.sin(Math.toRadians(player.getYRot()));
            float z = (float) Math.cos(Math.toRadians(player.getYRot()));
            float y = (float) -Math.sin(Math.toRadians(player.getXRot()));
            x *= 1.0F - Math.abs(y);
            z *= 1.0F - Math.abs(y);
            level.addParticle(ParticleTypes.EXPLOSION, player.getX() + x - 0.5, player.getEyeY() + y, player.getZ() + z, 0.0, 0.0, 0.0);
        }
        return true;
    }

    /** ItemBigSwordEarth (earth crusher): a hit sends an earthquake (level 1) along the ground. */
    public static class Earth extends BigSwordItem {
        public Earth(Properties properties, int damage) {
            super(properties, damage);
        }

        @Override
        public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
            if (!target.level().isClientSide()) {
                BDProjectile.spawn(attacker, ProjectileType.EARTHQUAKE, 1);
            }
        }
    }

    /** ItemBigSwordMonking: a hit also hurts every living thing within 1.5 blocks of the target by 3. */
    public static class Monking extends BigSwordItem {
        public Monking(Properties properties, int damage) {
            super(properties, damage);
        }

        @Override
        protected int chargedDamage(int useTime) {
            return 6;
        }

        @Override
        public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
            float range = 1.5F;
            AABB box = new AABB(target.getX() - range, target.getY() - range, target.getZ() - range, target.getX() + range,
                    target.getY() + range, target.getZ() + range);
            for (Entity e : target.level().getEntities(target, box)) {
                if (e instanceof LivingEntity && e != attacker) {
                    BDFx.hurt(e, BDFx.direct(attacker), 3);
                    BDFx.particle(e.level(), ParticleTypes.EXPLOSION, e.getX() + BDItemUtil.RAND.nextFloat() - 0.5, e.getY() + BDItemUtil.RAND.nextFloat(),
                            e.getZ() + BDItemUtil.RAND.nextFloat() - 0.5, 0.0, 0.0, 0.0);
                }
            }
            BDFx.particle(target.level(), ParticleTypes.EXPLOSION, target.getX() + BDItemUtil.RAND.nextFloat() - 0.5,
                    target.getY() + BDItemUtil.RAND.nextFloat(), target.getZ() + BDItemUtil.RAND.nextFloat() - 0.5, 0.0, 0.0, 0.0);
        }
    }
}
