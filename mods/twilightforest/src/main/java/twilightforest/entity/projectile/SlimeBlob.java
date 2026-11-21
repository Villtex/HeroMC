package twilightforest.entity.projectile;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import twilightforest.TFEntities;

/** EntityTFSlimeProjectile (looks like a slimeball): 8 thrown damage; it pops when it hits or is hit. */
public class SlimeBlob extends TFThrowable {
    public SlimeBlob(EntityType<? extends ThrowableItemProjectile> type, Level level) {
        super(type, level);
    }

    public SlimeBlob(Level level, LivingEntity owner) {
        super(TFEntities.SLIME_BLOB.get(), owner, level);
    }

    @Override
    protected Item getDefaultItem() {
        return Items.SLIME_BALL;
    }

    @Override
    protected float gravity() {
        return 0.006F;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            for (int i = 0; i < 2; i++) {
                this.level().addParticle(ParticleTypes.ITEM_SLIME, this.getX() + 0.5 * (this.random.nextDouble() - this.random.nextDouble()),
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
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        this.markHurt();
        this.pop(level);
        return true;
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (this.level() instanceof ServerLevel level) {
            if (result instanceof EntityHitResult hit && hit.getEntity() instanceof LivingEntity living) {
                living.hurtServer(level, this.damageSources().thrown(this, this.getOwner()), 8.0F);
            }
            this.pop(level);
        }
    }

    private void pop(ServerLevel level) {
        for (int i = 0; i < 8; i++) {
            level.sendParticles(ParticleTypes.ITEM_SLIME, this.getX(), this.getY(), this.getZ(), 0, this.random.nextGaussian() * 0.05,
                    this.random.nextDouble() * 0.2, this.random.nextGaussian() * 0.05, 1.0);
        }
        this.playSound(SoundEvents.SLIME_SQUISH, 1.0F, 1.0F / (this.random.nextFloat() * 0.4F + 0.8F));
        this.discard();
    }
}
