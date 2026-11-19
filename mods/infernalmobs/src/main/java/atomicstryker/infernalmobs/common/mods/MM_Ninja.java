package atomicstryker.infernalmobs.common.mods;

import atomicstryker.infernalmobs.common.MobModifier;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

/**
 * MM_Ninja of Infernal Mobs 1.3.1, ported 1:1.
 * Like the original the teleport never fails: the mob only climbs out of solid blocks above the destination.
 */
public class MM_Ninja extends MobModifier {
    private long nextAbilityUse = 0L;
    private static final long coolDown = 15000L;
    private static final String[] suffix = {" the Zen Master", " of Equilibrium", " of Inner Peace"};
    private static final String[] prefix = {" totally zen ", " innerly peaceful ", " Ronin "};

    public MM_Ninja(LivingEntity mob, MobModifier prevMod) {
        this.modName = "Ninja";
        this.nextMod = prevMod;
    }

    @Override
    public float onHurt(LivingEntity mob, DamageSource source, float damage) {
        long time = System.currentTimeMillis();
        if (time > this.nextAbilityUse && source.getEntity() != null && source.getEntity() != mob && this.teleportToEntity(mob, source.getEntity())) {
            this.nextAbilityUse = time + coolDown;
            hurt(source.getEntity(), mob.damageSources().mobAttack(mob), Math.min(damage, 15));
            return super.onHurt(mob, source, 0);
        }
        return super.onHurt(mob, source, damage);
    }

    private boolean teleportToEntity(LivingEntity mob, Entity par1Entity) {
        Vec3 vector = new Vec3(mob.getX() - par1Entity.getX(),
                mob.getBoundingBox().minY + mob.getBbHeight() / 2.0F - par1Entity.getY() + par1Entity.getEyeHeight(),
                mob.getZ() - par1Entity.getZ()).normalize();
        double telDist = 8.0;
        double destX = mob.getX() + (mob.getRandom().nextDouble() - 0.5) * 4.0 - vector.x * telDist;
        double destY = mob.getY() + (mob.getRandom().nextInt(16) - 4) - vector.y * telDist;
        double destZ = mob.getZ() + (mob.getRandom().nextDouble() - 0.5) * 4.0 - vector.z * telDist;
        return this.teleportTo(mob, destX, destY, destZ);
    }

    private boolean teleportTo(LivingEntity mob, double destX, double destY, double destZ) {
        double oldX = mob.getX();
        double oldY = mob.getY();
        double oldZ = mob.getZ();
        double posY = destY;
        int x = Mth.floor(destX);
        int y = Mth.floor(destY);
        int z = Mth.floor(destZ);
        if (mob.level().hasChunkAt(new BlockPos(x, y, z))) {
            boolean hitGround = false;
            while (!hitGround && y < 96) {
                if (mob.level().getBlockState(new BlockPos(x, y + 1, z)).isSolid()) {
                    posY++;
                    y++;
                } else {
                    hitGround = true;
                }
            }
            if (!hitGround) {
                return false;
            }
            mob.setPos(destX, posY, destZ);
            mob.level().playSound(null, oldX, oldY, oldZ, SoundEvents.GENERIC_EXPLODE, mob.getSoundSource(), 2.0F,
                    (1.0F + (mob.getRandom().nextFloat() - mob.getRandom().nextFloat()) * 0.2F) * 0.7F);
            serverLevel(mob).sendParticles(ParticleTypes.EXPLOSION_EMITTER, oldX, oldY, oldZ, 1, 0.0, 0.0, 0.0, 0.0);
        } else {
            // original: the position stays set to the (unloaded) destination
            mob.setPos(destX, destY, destZ);
        }
        return true;
    }

    @Override
    protected String[] getModNameSuffix() {
        return suffix;
    }

    @Override
    protected String[] getModNamePrefix() {
        return prefix;
    }
}
