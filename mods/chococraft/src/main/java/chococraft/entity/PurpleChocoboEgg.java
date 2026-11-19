package chococraft.entity;

import chococraft.ChocoConfig;
import chococraft.ChocoCraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * EntityPurpleChocoboEgg: thrown like a snowball; a hit entity takes 0 damage, and a purple chicobo hatches where the
 * egg lands (8 x 7 snowball puffs). The original never registered the entity, so clients did not see it fly.
 */
public class PurpleChocoboEgg extends ThrowableItemProjectile {
    public PurpleChocoboEgg(EntityType<? extends PurpleChocoboEgg> type, Level level) {
        super(type, level);
    }

    public PurpleChocoboEgg(Level level, LivingEntity thrower, ItemStack stack) {
        super(ChocoCraft.PURPLE_CHOCOBO_EGG_ENTITY.get(), thrower, level, stack);
    }

    @Override
    protected Item getDefaultItem() {
        return ChocoCraft.PURPLE_CHOCOBO_EGG.get();
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (this.level() instanceof ServerLevel level) {
            result.getEntity().hurtServer(level, this.damageSources().thrown(this, this.getOwner()), 0.0F);
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (this.level() instanceof ServerLevel level) {
            Chicobo baby = ChocoCraft.CHICOBO.get().create(level, EntitySpawnReason.BREEDING);
            if (baby != null) {
                baby.setColor(ChocoboColor.PURPLE);
                baby.setTimeUntilAdult(this.random.nextInt(ChocoConfig.GROWUP_DELAY_RANDOM.get()) + ChocoConfig.GROWUP_DELAY_STATIC.get());
                baby.snapTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), this.getXRot());
                level.addFreshEntity(baby);
                baby.particles(ParticleTypes.ITEM_SNOWBALL, 56);
            }
            this.discard();
        }
    }
}
