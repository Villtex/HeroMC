package betterdungeons.projectile;

import betterdungeons.BDEntities;
import betterdungeons.BDFx;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * EntityArrowMagic ("MagicArrow"): the elemental arrows (types 0..7 like the essences: water, poison, ice, fire,
 * darkness, electric, wind, life) and the plain arrows of the archer mobs (type 11). Flight, sticking, pick-up (a plain
 * arrow, as in 1.5.2) and crits are vanilla's arrow; the element adds its effect after a hit and its particles.
 *
 * <p>Firing: {@code BDMagicArrow.shoot(player, speed, type)} (the bow, EventHandlerBD.shootMagicArrow) or, for mobs,
 * {@code BDMagicArrow.shootAt(mob, target, speed, inaccuracy)} (EntityArrowMagic(world, shooter, target, f, f1)) and
 * {@code BDMagicArrow.shoot(mob, 1.0F, BDMagicArrow.PLAIN)} (EntityHuman); add the result with {@code addFreshEntity}.
 */
public class BDMagicArrow extends AbstractArrow {
    public static final int WATER = 0;
    public static final int POISON = 1;
    public static final int ICE = 2;
    public static final int FIRE = 3;
    public static final int DARKNESS = 4;
    public static final int ELECTRIC = 5;
    public static final int WIND = 6;
    public static final int LIFE = 7;
    /** The default of the data watcher: a plain arrow. */
    public static final int PLAIN = 11;

    private static final EntityDataAccessor<Byte> DATA_TYPE = SynchedEntityData.defineId(BDMagicArrow.class, EntityDataSerializers.BYTE);
    private int knockbackStrength;
    private double damage = 2.0;

    public BDMagicArrow(EntityType<? extends BDMagicArrow> type, Level level) {
        super(type, level);
    }

    private BDMagicArrow(Level level, LivingEntity shooter, double x, double y, double z) {
        super(BDEntities.MAGIC_ARROW.get(), x, y, z, level, new ItemStack(Items.ARROW), null);
        this.setOwner(shooter);
        if (shooter instanceof Player) {
            this.pickup = Pickup.ALLOWED;
        }
    }

    /** EntityArrowMagic(world, shooter, speed, type): from the eyes along the shooter's view, at speed * 1.5. */
    public static BDMagicArrow shoot(LivingEntity shooter, float speed, int type) {
        float yaw = shooter.getYRot();
        float pitch = shooter.getXRot();
        double x = shooter.getX() - Mth.cos(yaw / 180.0F * (float) Math.PI) * 0.16F;
        double y = shooter.getEyeY() - 0.1F;
        double z = shooter.getZ() - Mth.sin(yaw / 180.0F * (float) Math.PI) * 0.16F;
        BDMagicArrow arrow = new BDMagicArrow(shooter.level(), shooter, x, y, z);
        arrow.setYRot(yaw);
        arrow.setXRot(pitch);
        double mx = -Mth.sin(yaw / 180.0F * (float) Math.PI) * Mth.cos(pitch / 180.0F * (float) Math.PI);
        double mz = Mth.cos(yaw / 180.0F * (float) Math.PI) * Mth.cos(pitch / 180.0F * (float) Math.PI);
        double my = -Mth.sin(pitch / 180.0F * (float) Math.PI);
        arrow.heading(mx, my, mz, speed * 1.5F, 1.0F);
        arrow.setArrowType(type);
        return arrow;
    }

    /** EntityArrowMagic(world, shooter, target, speed, inaccuracy): aimed at the target's eyes with a little lift. */
    public static BDMagicArrow shootAt(LivingEntity shooter, LivingEntity target, float speed, float inaccuracy) {
        double y = shooter.getEyeY() - 0.1F;
        double dx = target.getX() - shooter.getX();
        double dy = target.getEyeY() - 0.7F - y;
        double dz = target.getZ() - shooter.getZ();
        double horizontal = Math.sqrt(dx * dx + dz * dz);
        BDMagicArrow arrow = new BDMagicArrow(shooter.level(), shooter, shooter.getX(), y, shooter.getZ());
        if (horizontal >= 1.0E-7) {
            arrow.snapTo(shooter.getX() + dx / horizontal, y, shooter.getZ() + dz / horizontal,
                    (float) (Math.atan2(dz, dx) * 180.0 / Math.PI) - 90.0F, (float) -(Math.atan2(dy, horizontal) * 180.0 / Math.PI));
            arrow.heading(dx, dy + horizontal * 0.2F, dz, speed, inaccuracy);
        }
        return arrow;
    }

    /** setArrowHeading of 1.5.2 (its spread was 0.0075 per unit of inaccuracy). */
    public void heading(double x, double y, double z, float speed, float inaccuracy) {
        double length = Math.sqrt(x * x + y * y + z * z);
        x = x / length + this.random.nextGaussian() * 0.0075F * inaccuracy;
        y = y / length + this.random.nextGaussian() * 0.0075F * inaccuracy;
        z = z / length + this.random.nextGaussian() * 0.0075F * inaccuracy;
        x *= speed;
        y *= speed;
        z *= speed;
        this.setDeltaMovement(x, y, z);
        double horizontal = Math.sqrt(x * x + z * z);
        this.setYRot((float) (Math.atan2(x, z) * 180.0 / Math.PI));
        this.setXRot((float) (Math.atan2(y, horizontal) * 180.0 / Math.PI));
        this.yRotO = this.getYRot();
        this.xRotO = this.getXRot();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_TYPE, (byte) PLAIN);
    }

    public void setArrowType(int type) {
        this.entityData.set(DATA_TYPE, (byte) type);
    }

    public int getArrowType() {
        return this.entityData.get(DATA_TYPE);
    }

    /** setKnockbackStrength (Punch of the bow). */
    public void setKnockbackStrength(int strength) {
        this.knockbackStrength = strength;
    }

    /** The arrow's damage (2, Power adds 0.5 + 0.5 per level); the life arrow heals this much. */
    public double getDamage() {
        return this.damage;
    }

    @Override
    public void setBaseDamage(double baseDamage) {
        super.setBaseDamage(baseDamage);
        this.damage = baseDamage;
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return new ItemStack(Items.ARROW);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            ParticleOptions particle = this.particle();
            if (particle != null) {
                Vec3 m = this.getDeltaMovement();
                for (int i = 0; i < 4; i++) {
                    this.level().addParticle(particle, this.getX() + m.x * i / 4.0, this.getY() + m.y * i / 4.0, this.getZ() + m.z * i / 4.0,
                            0.0, this.getArrowType() == POISON ? 1.0 : 0.0, 0.0);
                }
            }
        }
    }

    /** The particle every ProjectileArrowX left behind ("splash", "mobSpell", "snowballpoof", ...). */
    private @Nullable ParticleOptions particle() {
        return switch (this.getArrowType()) {
            case WATER -> ParticleTypes.SPLASH;
            case POISON -> ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, MobEffects.POISON.value().getColor());
            case ICE -> ParticleTypes.ITEM_SNOWBALL;
            case FIRE -> ParticleTypes.FLAME;
            case DARKNESS -> ParticleTypes.PORTAL;
            case ELECTRIC -> ParticleTypes.ENCHANTED_HIT;
            case WIND -> ParticleTypes.MYCELIUM;
            case LIFE -> new DustParticleOptions(0xFF0000, 1.0F);
            default -> null;
        };
    }

    /** knockbackStrength of 1.5.2 (Punch; the wind arrow adds 2 before the push). */
    @Override
    protected void doKnockback(LivingEntity mob, DamageSource damageSource) {
        int strength = this.knockbackStrength + (this.getArrowType() == WIND ? 2 : 0);
        if (strength > 0) {
            Vec3 m = this.getDeltaMovement();
            double horizontal = Math.sqrt(m.x * m.x + m.z * m.z);
            if (horizontal > 0.0) {
                mob.push(m.x * strength * 0.6F / horizontal, 0.1, m.z * strength * 0.6F / horizontal);
            }
        }
    }

    /** ArrowBase.applyDamage of the element. */
    @Override
    protected void doPostHurtEffects(LivingEntity e) {
        switch (this.getArrowType()) {
            case WATER -> {
                e.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 200, 0));
                e.clearFire();
            }
            case POISON -> e.addEffect(new MobEffectInstance(MobEffects.POISON, 200, 0));
            case ICE -> e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 200, 1));
            case FIRE -> {
                BDFx.hurt(e, e.damageSources().inFire(), 4);
                e.igniteForSeconds(2);
            }
            case DARKNESS -> {
                e.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 1));
                BDFx.hurt(e, BDFx.magic(this, this.getOwner()), 1);
            }
            case ELECTRIC -> BDFx.hurt(e, BDFx.magic(this, this.getOwner()), e.getEffect(MobEffects.NAUSEA) == null ? 2 : 6);
            case LIFE -> e.heal((int) this.damage);
            default -> {
            }
        }
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putByte("type", (byte) this.getArrowType());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.setArrowType(input.getByteOr("type", (byte) PLAIN));
    }
}
