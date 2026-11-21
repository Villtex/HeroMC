package twilightforest.entity.projectile;

import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import twilightforest.TFEntities;

/** EntityTFTomeBolt (looks like paper): 6 thrown damage and slowness II (3 / 7 / 9 s by difficulty). */
public class TomeBolt extends TFThrowable {
    public TomeBolt(EntityType<? extends ThrowableItemProjectile> type, Level level) {
        super(type, level);
    }

    public TomeBolt(Level level, LivingEntity owner) {
        super(TFEntities.TOME_BOLT.get(), owner, level);
    }

    @Override
    protected Item getDefaultItem() {
        return Items.PAPER;
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
                this.level().addParticle(ParticleTypes.CRIT, this.getX() + 0.5 * (this.random.nextDouble() - this.random.nextDouble()),
                        this.getY() + 0.5 * (this.random.nextDouble() - this.random.nextDouble()),
                        this.getZ() + 0.5 * (this.random.nextDouble() - this.random.nextDouble()), 0.0, 0.0, 0.0);
            }
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!(this.level() instanceof ServerLevel level)) {
            return;
        }
        if (result instanceof EntityHitResult hit && hit.getEntity() instanceof LivingEntity living
                && living.hurtServer(level, this.damageSources().thrown(this, this.getOwner()), 6.0F)) {
            int seconds = level.getDifficulty() == Difficulty.PEACEFUL ? 3 : level.getDifficulty() == Difficulty.NORMAL ? 7 : 9;
            living.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, seconds * 20, 1));
        }
        for (int i = 0; i < 8; i++) {
            level.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, Items.FIRE_CHARGE), this.getX(), this.getY(), this.getZ(),
                    0, this.random.nextGaussian() * 0.05, this.random.nextDouble() * 0.2, this.random.nextGaussian() * 0.05, 1.0);
        }
        this.discard();
    }
}
