package betterdungeons.item;

import betterdungeons.BDFx;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
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
import net.minecraft.world.phys.Vec3;

/**
 * ItemSpear (ItemSword damage minus 1, two uses per hit): a hit knocks the target off its mount. Hold use and let go
 * to thrust at the target within 5 blocks with damage * min(ticks + 1, 90) / 30 + 1; held longer than 50 ticks the
 * dwarf spear breathes fire as well (ItemSpearDwarf).
 */
public class SpearItem extends Item {
    private final int damage;

    public SpearItem(Properties properties, int damage) {
        super(properties);
        this.damage = damage;
    }

    @Override
    public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (target.getVehicle() != null) {
            target.stopRiding();
        }
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

    @Override
    public boolean releaseUsing(ItemStack stack, Level level, LivingEntity entity, int remainingTime) {
        if (!(entity instanceof Player player)) {
            return false;
        }
        player.swing(player.getUsedItemHand(), net.minecraft.world.item.component.SwingAnimation.DEFAULT, true);
        Entity target = BDItemUtil.getTarget(player, 5.0);
        int held = this.getUseDuration(stack, entity) - remainingTime;
        if (held > 50) {
            this.doSpecialSkill(stack, level, player);
        }
        if (target != null && !level.isClientSide()) {
            int useTime = Math.min(held + 1, 90);
            BDItemUtil.attackWithDamage(player, target, this.damage * useTime / 30 + 1);
        }
        return true;
    }

    protected void doSpecialSkill(ItemStack stack, Level level, Player player) {
    }

    /** ItemSpearDwarf (fire spear): a cone of fire 15 blocks along the view, 4 fire damage and 6 s of burning. */
    public static class Dwarf extends SpearItem {
        public Dwarf(Properties properties, int damage) {
            super(properties, damage);
        }

        @Override
        protected void doSpecialSkill(ItemStack stack, Level level, Player player) {
            // getMaxItemUseDuration() % 40 == 0 never held (72000 % 40 == 0 always did): the fire sounds every time
            BDFx.sound(level, player.getX(), player.getY(), player.getZ(), SoundEvents.FIRE_AMBIENT, 4.0F, BDFx.pitch(level));
            float x = (float) -Math.sin(Math.toRadians(player.getYRot()));
            float z = (float) Math.cos(Math.toRadians(player.getYRot()));
            float y = (float) -Math.sin(Math.toRadians(player.getXRot()));
            x *= 1.0F - Math.abs(y);
            z *= 1.0F - Math.abs(y);
            if (level.isClientSide()) {
                for (int i = 0; i < 50; i++) {
                    double speed = BDItemUtil.RAND.nextDouble() + 0.2;
                    level.addParticle(ParticleTypes.FLAME, player.getX(), player.getY() - 1.0 + BDItemUtil.RAND.nextFloat() + player.getEyeHeight(),
                            player.getZ(), (x + (BDItemUtil.RAND.nextDouble() - 0.5) / 3.0) * speed, (y + (BDItemUtil.RAND.nextDouble() - 0.5) / 3.0) * speed,
                            (z + (BDItemUtil.RAND.nextDouble() - 0.5) / 3.0) * speed);
                }
            }
            int dist = 15;
            Vec3 look = player.getLookAngle();
            for (Entity e : level.getEntities(player, player.getBoundingBox().expandTowards(look.scale(dist)).inflate(1.0))) {
                if (e instanceof LivingEntity) {
                    double rot = Math.abs(Math.abs(BDItemUtil.angleBetween(player, e)) - Math.abs(Mth.wrapDegrees(player.getYRot())));
                    if (rot < 10.0) {
                        e.igniteForSeconds(6);
                        BDFx.hurt(e, BDFx.direct(player), 4);
                    }
                }
            }
            stack.hurtAndBreak(1, player, player.getUsedItemHand());
        }
    }
}
