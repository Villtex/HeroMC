package twilightforest.entity.boss;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.hurtingprojectile.AbstractHurtingProjectile;
import net.minecraft.world.entity.projectile.hurtingprojectile.LargeFireball;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/** EntityTFUrGhastFireball: a large fireball doing 16 fireball damage to what it hits; explosion power 1 with fire. */
public class UrGhastFireball extends LargeFireball {
    public UrGhastFireball(EntityType<? extends LargeFireball> type, Level level) {
        super(type, level);
    }

    public UrGhastFireball(Level level, LivingEntity owner, Vec3 direction) {
        super(level, owner, direction, 1);
    }

    @Override
    protected void onHit(HitResult result) {
        if (this.level() instanceof ServerLevel level && !(result instanceof EntityHitResult hit && hit.getEntity() instanceof AbstractHurtingProjectile)) {
            if (result instanceof EntityHitResult hit) {
                hit.getEntity().hurtServer(level, this.damageSources().fireball(this, this.getOwner()), 16.0F);
            }
            boolean griefing = level.getGameRules().get(GameRules.MOB_GRIEFING);
            level.explode(null, this.getX(), this.getY(), this.getZ(), 1.0F, true, griefing ? Level.ExplosionInteraction.MOB : Level.ExplosionInteraction.NONE);
            this.discard();
        }
    }
}
