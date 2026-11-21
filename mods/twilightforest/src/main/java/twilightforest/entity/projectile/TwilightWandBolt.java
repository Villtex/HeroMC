package twilightforest.entity.projectile;

import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import twilightforest.TFEntities;

/**
 * EntityTFTwilightWandBolt (drawn as an ender pearl): 6 indirect magic damage, a purple trail ("mobSpell" 0.17/0.8/0.69
 * scaled), ender pearl crumbs where it hits.
 */
public class TwilightWandBolt extends TFThrowable {
    public TwilightWandBolt(EntityType<? extends ThrowableItemProjectile> type, Level level) {
        super(type, level);
    }

    public TwilightWandBolt(Level level, LivingEntity owner) {
        super(TFEntities.TWILIGHT_WAND_BOLT.get(), owner, level);
    }

    /** EntityThrowable(world, thrower): along the look of the thrower at 1.5 blocks per tick, spread 1. */
    public void shootOld(float pitch, float yaw) {
        float f = 0.4F;
        double x = -Math.sin(yaw / 180.0F * Math.PI) * Math.cos(pitch / 180.0F * Math.PI) * f;
        double z = Math.cos(yaw / 180.0F * Math.PI) * Math.cos(pitch / 180.0F * Math.PI) * f;
        double y = -Math.sin(pitch / 180.0F * Math.PI) * f;
        this.shootOld(x, y, z, 1.5F, 1.0F);
    }

    @Override
    protected Item getDefaultItem() {
        return Items.ENDER_PEARL;
    }

    @Override
    protected float gravity() {
        return 0.003F;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            for (int i = 0; i < 5; i++) {
                double dx = this.getX() + 0.5 * (this.random.nextDouble() - this.random.nextDouble());
                double dy = this.getY() + 0.5 * (this.random.nextDouble() - this.random.nextDouble());
                double dz = this.getZ() + 0.5 * (this.random.nextDouble() - this.random.nextDouble());
                float s1 = (this.random.nextFloat() * 0.5F + 0.5F) * 0.17F;
                float s2 = (this.random.nextFloat() * 0.5F + 0.5F) * 0.8F;
                float s3 = (this.random.nextFloat() * 0.5F + 0.5F) * 0.69F;
                this.level().addParticle(ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, s1, s2, s3), dx, dy, dz, 0.0, 0.0, 0.0);
            }
        }
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
        level.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, Items.ENDER_PEARL), this.getX(), this.getY(), this.getZ(), 8,
                0.0, 0.0, 0.0, 0.1);
        this.discard();
    }
}
