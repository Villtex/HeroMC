package projectzulu.entity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.hurtingprojectile.AbstractHurtingProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import projectzulu.ZuluMonsters;

/**
 * EntityLizardSpit: a fireball-like projectile (acceleration 0.1 towards the target, inertia 0.95, 0.8 in water, a
 * smoke trail) that poisons (40 ticks, level II) and slows (100 ticks, level III) a living entity it hits and vanishes
 * on any hit. It does not burn. Hitting it sends it off where the attacker looks. Full bright, 1.0 x 1.0.
 */
public class LizardSpitEntity extends AbstractHurtingProjectile {
    public LizardSpitEntity(EntityType<? extends LizardSpitEntity> type, Level level) {
        super(type, level);
    }

    public LizardSpitEntity(Level level, LivingEntity shooter, Vec3 direction) {
        super(ZuluMonsters.LIZARD_SPIT.get(), shooter, direction, level);
    }

    @Override
    protected boolean shouldBurn() {
        return false;
    }

    @Override
    protected void onHitEntity(EntityHitResult hitResult) {
        super.onHitEntity(hitResult);
        if (!this.level().isClientSide() && hitResult.getEntity() instanceof LivingEntity living) {
            living.addEffect(new MobEffectInstance(MobEffects.POISON, 40, 1), this.getEffectSource());
            living.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 100, 2), this.getEffectSource());
        }
    }

    @Override
    protected void onHit(HitResult hitResult) {
        super.onHit(hitResult);
        if (!this.level().isClientSide()) {
            this.discard();
        }
    }

    /** canBeCollidedWith / getCollisionBorderSize 1.0: it can be hit. */
    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public float getPickRadius() {
        return 1.0F;
    }

    /** attackEntityFrom: the attacker's look direction becomes the new course, the attacker the new shooter. */
    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        Entity attacker = source.getEntity();
        if (attacker == null) {
            return false;
        }
        this.markHurt();
        Vec3 look = attacker.getLookAngle();
        this.setDeltaMovement(look);
        this.accelerationPower = 0.1;
        if (attacker instanceof LivingEntity) {
            this.setOwner(attacker);
        }
        return true;
    }
}
