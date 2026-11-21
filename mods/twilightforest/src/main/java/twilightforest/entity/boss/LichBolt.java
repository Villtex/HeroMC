package twilightforest.entity.boss;

import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ItemParticleOption;
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
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import twilightforest.TFBosses;
import twilightforest.entity.projectile.TFThrowable;

/**
 * EntityTFLichBolt (drawn as an ender pearl): no gravity, 6 indirect magic damage. It can be hit: then it flies along
 * the hitter's look (1.5, spread 0.1) and counts as his. It passes through other bolts, bombs and shadow clones.
 */
public class LichBolt extends TFThrowable {
    public LichBolt(EntityType<? extends ThrowableItemProjectile> type, Level level) {
        super(type, level);
    }

    public LichBolt(Level level, LivingEntity owner) {
        super(TFBosses.LICH_BOLT.get(), owner, level);
    }

    @Override
    protected Item getDefaultItem() {
        return Items.ENDER_PEARL;
    }

    @Override
    protected float gravity() {
        return 0.0F;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            for (int i = 0; i < 5; i++) {
                float s1 = (this.random.nextFloat() * 0.5F + 0.5F) * 0.17F;
                float s2 = (this.random.nextFloat() * 0.5F + 0.5F) * 0.8F;
                float s3 = (this.random.nextFloat() * 0.5F + 0.5F) * 0.69F;
                this.level().addParticle(ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, s1, s2, s3),
                        this.getX() + 0.5 * (this.random.nextDouble() - this.random.nextDouble()),
                        this.getY() + 0.5 * (this.random.nextDouble() - this.random.nextDouble()),
                        this.getZ() + 0.5 * (this.random.nextDouble() - this.random.nextDouble()), 0.0, 0.0, 0.0);
            }
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
            Vec3 look = source.getEntity().getLookAngle();
            this.shootOld(look.x, look.y, look.z, 1.5F, 0.1F);
            this.setOwner(source.getEntity());
            return true;
        }
        return false;
    }

    @Override
    protected boolean canHitEntity(Entity target) {
        return super.canHitEntity(target) && !(target instanceof LichBolt) && !(target instanceof LichBomb)
                && !(target instanceof Lich lich && lich.isShadowClone());
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!(this.level() instanceof ServerLevel level)) {
            return;
        }
        if (result instanceof EntityHitResult hit && hit.getEntity() instanceof LivingEntity living) {
            living.hurtServer(level, this.damageSources().indirectMagic(this, this.getOwner()), 6.0F);
        }
        level.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, Items.ENDER_PEARL), this.getX(), this.getY(), this.getZ(), 8, 0.05, 0.1, 0.05, 0.1);
        this.discard();
    }
}
