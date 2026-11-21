package twilightforest.entity.boss;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import twilightforest.TFBosses;
import twilightforest.entity.projectile.TFThrowable;

/**
 * EntityTFLichBomb (drawn as magma cream, always burning): gravity 0.001, explodes with power 2 (no fire, no broken
 * blocks) where it hits or when something hits it; passes through bolts, bombs and the lich.
 */
public class LichBomb extends TFThrowable {
    public LichBomb(EntityType<? extends ThrowableItemProjectile> type, Level level) {
        super(type, level);
    }

    public LichBomb(Level level, LivingEntity owner) {
        super(TFBosses.LICH_BOMB.get(), owner, level);
    }

    @Override
    protected Item getDefaultItem() {
        return Items.MAGMA_CREAM;
    }

    @Override
    protected float gravity() {
        return 0.001F;
    }

    @Override
    public boolean isOnFire() {
        return true;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            Vec3 m = this.getDeltaMovement();
            double sx = 0.5 * (this.random.nextDouble() - this.random.nextDouble()) + m.x;
            double sy = 0.5 * (this.random.nextDouble() - this.random.nextDouble()) + m.y;
            double sz = 0.5 * (this.random.nextDouble() - this.random.nextDouble()) + m.z;
            this.level().addParticle(ParticleTypes.FLAME, this.getX() + sx, this.getY() + sy, this.getZ() + sz, sx * -0.25, sy * -0.25, sz * -0.25);
        }
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public float getPickRadius() {
        return 1.0F;
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        this.markHurt();
        if (source.getEntity() != null) {
            this.explode(level);
            return true;
        }
        return false;
    }

    private void explode(ServerLevel level) {
        level.explode(this, this.getX(), this.getY(), this.getZ(), 2.0F, false, Level.ExplosionInteraction.NONE);
        this.discard();
    }

    @Override
    protected boolean canHitEntity(Entity target) {
        return super.canHitEntity(target) && !(target instanceof LichBolt) && !(target instanceof LichBomb) && !(target instanceof Lich);
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (this.level() instanceof ServerLevel level) {
            this.explode(level);
        }
    }
}
