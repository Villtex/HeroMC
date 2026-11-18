package betterdungeons.item;

import betterdungeons.BDFx;
import betterdungeons.BDSounds;
import betterdungeons.projectile.BDProjectile;
import betterdungeons.projectile.ProjectileType;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * ItemStaffBase and the eight staffs (one class, the element decides). Use: water a bubble, dark a vampiric ball, wind
 * a wind ball, electric a dash, fire a flame thrower (held), ice slows everything around the target, poison poisons
 * the target (HelperPlayer.getTarget, 30 blocks). A melee hit: fire sets on fire, ice slows, poison poisons, the
 * healing staff heals by 2 instead of hurting ({@link #onLeftClickEntity}, IItemExtension).
 */
public class StaffItem extends Item {
    public enum Element { LIFE, WATER, POISON, ICE, FIRE, DARK, ELECTRIC, WIND }

    private final Element element;

    public StaffItem(Properties properties, Element element) {
        super(properties);
        this.element = element;
    }

    public Element element() {
        return this.element;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        switch (this.element) {
            case DARK -> {
                if (!level.isClientSide()) {
                    BDProjectile.spawn(player, ProjectileType.VAMPIRE, 1);
                }
                stack.hurtAndBreak(1, player, hand);
            }
            case WATER -> {
                if (!level.isClientSide()) {
                    BDProjectile.spawn(player, ProjectileType.BUBBLE);
                }
                stack.hurtAndBreak(1, player, hand);
            }
            case WIND -> {
                if (!level.isClientSide()) {
                    BDProjectile.spawn(player, ProjectileType.WINDBALL);
                }
                stack.hurtAndBreak(1, player, hand);
            }
            case ELECTRIC -> {
                BDFx.sound(level, player.getX(), player.getY(), player.getZ(), SoundEvents.FIREWORK_ROCKET_LAUNCH, 4.0F, BDFx.pitch(level));
                double x = -Math.sin(Math.toRadians(player.getYRot()));
                double z = Math.cos(Math.toRadians(player.getYRot()));
                double y = -Math.sin(Math.toRadians(player.getXRot()));
                x *= 1.0 - Math.abs(y);
                z *= 1.0 - Math.abs(y);
                player.setDeltaMovement(x * 2.0, y * 1.5, z * 2.0);
                player.fallDistance = -25.0;
                stack.hurtAndBreak(1, player, hand);
            }
            case FIRE -> {
                BDFx.sound(level, player.getX(), player.getY(), player.getZ(), SoundEvents.GHAST_SHOOT, 4.0F, BDFx.pitch(level));
                this.flames(level, player, stack, 4, 24, false);
                player.startUsingItem(hand);
                return InteractionResult.CONSUME;
            }
            case ICE -> {
                Entity entity = BDItemUtil.getTarget(player, 30.0);
                if (entity instanceof LivingEntity) {
                    if (!level.isClientSide()) {
                        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, entity.getBoundingBox().inflate(4.0, 2.0, 4.0))) {
                            if (e != player && entity.distanceToSqr(e) < 16.0) {
                                e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 70, 0));
                            }
                        }
                    }
                    BDFx.splash(level, entity.getX(), entity.getY(), entity.getZ(), 2);
                    stack.hurtAndBreak(1, player, hand);
                    BDFx.sound(level, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.SNOW_BREAK, 4.0F, BDFx.pitch(level));
                }
            }
            case POISON -> {
                Entity entity = BDItemUtil.getTarget(player, 30.0);
                if (entity instanceof LivingEntity living) {
                    stack.hurtAndBreak(1, player, hand);
                    if (!level.isClientSide()) {
                        living.addEffect(new MobEffectInstance(MobEffects.POISON, 300, 1));
                    }
                    spell(level, entity);
                    BDFx.sound(level, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.PLAYER_BREATH, 4.0F, BDFx.pitch(level));
                }
            }
            case LIFE -> {
            }
        }
        return InteractionResult.PASS;
    }

    /** The fire staff's flames 4 (on use) or 5 (held) blocks along the view; each entity hit costs a use. */
    private void flames(Level level, Player player, ItemStack stack, int dist, int particles, boolean livingOnly) {
        float x = (float) -Math.sin(Math.toRadians(player.getYRot()));
        float z = (float) Math.cos(Math.toRadians(player.getYRot()));
        double y = -Math.sin(Math.toRadians(player.getXRot()));
        x = (float) (x * (1.0 - Math.abs(y)));
        z = (float) (z * (1.0 - Math.abs(y)));
        if (!level.isClientSide()) {
            Vec3 look = player.getLookAngle();
            for (Entity e : level.getEntities(player, player.getBoundingBox().expandTowards(look.scale(dist)).inflate(1.0))) {
                if (!livingOnly || e instanceof LivingEntity) {
                    e.igniteForSeconds(4);
                    BDFx.hurt(e, BDFx.direct(player), 1);
                    stack.hurtAndBreak(1, player, player.getUsedItemHand() != null ? player.getUsedItemHand() : InteractionHand.MAIN_HAND);
                }
            }
        }
        for (int i = 0; i < particles; i++) {
            level.addParticle(ParticleTypes.FLAME, player.getX(), player.getEyeY() - 1.0 + BDItemUtil.RAND.nextFloat(), player.getZ(),
                    (x + BDItemUtil.RAND.nextFloat() - 0.5) / 2.0, (y + BDItemUtil.RAND.nextFloat() - 0.5) / 8.0, (z + BDItemUtil.RAND.nextFloat() - 0.5) / 2.0);
        }
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int ticksRemaining) {
        if (this.element == Element.FIRE && entity instanceof Player player) {
            // the 1.5.2 test getMaxItemUseDuration() % 40 == 0 was always true: the fire crackles every tick
            BDFx.sound(level, player.getX(), player.getY(), player.getZ(), SoundEvents.FIRE_AMBIENT, 4.0F, BDFx.pitch(level));
            this.flames(level, player, stack, 5, 8, true);
        }
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity user) {
        return this.element == Element.FIRE ? 72000 : 0;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.NONE;
    }

    private static void spell(Level level, Entity entity) {
        ParticleOptions particle = ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, MobEffects.POISON.value().getColor());
        for (int i = 0; i < 6; i++) {
            BDFx.particle(level, particle, entity.getX() + BDItemUtil.RAND.nextFloat() - 0.5, entity.getY() + 1.0 + BDItemUtil.RAND.nextFloat(),
                    entity.getZ() + BDItemUtil.RAND.nextFloat() - 0.5, 0.0, 0.4F, 0.0);
        }
    }

    /**
     * onLeftClickEntity (IItemExtension, both sides): true cancels the attack (the healing staff heals instead).
     */
    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
        if (!(entity instanceof LivingEntity living)) {
            return false;
        }
        Level level = entity.level();
        switch (this.element) {
            case LIFE -> {
                if (!level.isClientSide()) {
                    living.heal(2);
                }
                stack.hurtAndBreak(1, player, InteractionHand.MAIN_HAND);
                for (int i = 0; i < 5; i++) {
                    BDFx.particle(level, ParticleTypes.HEART, entity.getX() + BDItemUtil.RAND.nextFloat() - 0.5, entity.getY() + 1.0 + BDItemUtil.RAND.nextFloat(),
                            entity.getZ() + BDItemUtil.RAND.nextFloat() - 0.5, BDItemUtil.RAND.nextFloat() - 0.5F, BDItemUtil.RAND.nextFloat() - 0.5F,
                            BDItemUtil.RAND.nextFloat() - 0.5F);
                }
                BDFx.sound(level, entity.getX(), entity.getY(), entity.getZ(), BDSounds.MAGIC.get(), 4.0F, BDFx.pitch(level));
                return true;
            }
            case FIRE -> {
                living.igniteForSeconds(3);
                stack.hurtAndBreak(1, player, InteractionHand.MAIN_HAND);
                for (int i = 0; i < 6; i++) {
                    BDFx.particle(level, ParticleTypes.FLAME, entity.getX() + BDItemUtil.RAND.nextFloat() - 0.5, entity.getY() + 1.0 + BDItemUtil.RAND.nextFloat(),
                            entity.getZ() + BDItemUtil.RAND.nextFloat() - 0.5, BDItemUtil.RAND.nextFloat() - 0.5F, BDItemUtil.RAND.nextFloat() - 0.5F,
                            BDItemUtil.RAND.nextFloat() - 0.5F);
                }
                return false;
            }
            case ICE -> {
                if (!level.isClientSide()) {
                    living.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 200, 1));
                }
                stack.hurtAndBreak(1, player, InteractionHand.MAIN_HAND);
                for (int i = 0; i < 6; i++) {
                    BDFx.particle(level, ParticleTypes.ITEM_SNOWBALL, entity.getX() + BDItemUtil.RAND.nextFloat() - 0.5, entity.getY() + 1.0 + BDItemUtil.RAND.nextFloat(),
                            entity.getZ() + BDItemUtil.RAND.nextFloat() - 0.5, BDItemUtil.RAND.nextFloat() - 0.5F, BDItemUtil.RAND.nextFloat() - 0.5F,
                            BDItemUtil.RAND.nextFloat() - 0.5F);
                }
                return false;
            }
            case POISON -> {
                if (!level.isClientSide()) {
                    living.addEffect(new MobEffectInstance(MobEffects.POISON, 300, 1));
                }
                stack.hurtAndBreak(1, player, InteractionHand.MAIN_HAND);
                spell(level, entity);
                return false;
            }
            default -> {
                return false;
            }
        }
    }
}
