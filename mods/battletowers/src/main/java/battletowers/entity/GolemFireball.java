package battletowers.entity;

import battletowers.BattleTowers;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.entity.projectile.hurtingprojectile.AbstractHurtingProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * AS_EntityGolemFireball: flies towards where the golem aimed (acceleration 0.1 along the aim, scattered by a
 * gaussian 0.4 per axis; inertia 0.95, 0.8 in water), burns, and explodes with power 1, setting fire, on the first
 * block or entity it touches; the hit itself does no damage. The golem cannot hit itself in the first 25 ticks.
 * Hitting it sends it off where the attacker looks. 0.3 x 0.3, drawn as a fire charge at half size.
 */
public class GolemFireball extends AbstractHurtingProjectile implements ItemSupplier {
    public GolemFireball(EntityType<? extends GolemFireball> type, Level level) {
        super(type, level);
    }

    /** {@code diff}: from the golem to the middle of its target; the scatter is drawn here, as in the original. */
    public GolemFireball(Level level, LivingEntity shooter, Vec3 diff) {
        super(BattleTowers.GOLEM_FIREBALL.get(), shooter, diff.add(shooter.getRandom().nextGaussian() * 0.4,
                shooter.getRandom().nextGaussian() * 0.4, shooter.getRandom().nextGaussian() * 0.4), level);
    }

    @Override
    protected void onHit(HitResult hitResult) {
        super.onHit(hitResult);
        if (!this.level().isClientSide()) {
            // newExplosion(null, ..., 1.0F, flaming, smoking): no exploder, so it breaks blocks whatever mobGriefing says
            this.level().explode(null, this.getX(), this.getY(), this.getZ(), 1.0F, true, Level.ExplosionInteraction.TNT);
            this.discard();
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
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        Entity attacker = source.getEntity();
        if (attacker == null) {
            return false;
        }
        this.markHurt();
        Vec3 look = attacker.getLookAngle();
        this.setDeltaMovement(look);
        this.accelerationPower = 0.1;
        // wasDeflected: from now on it can hit the golem too
        this.setOwner(attacker);
        return true;
    }

    @Override
    public ItemStack getItem() {
        return new ItemStack(Items.FIRE_CHARGE);
    }
}
