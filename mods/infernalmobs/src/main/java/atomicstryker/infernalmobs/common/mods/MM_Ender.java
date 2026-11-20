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
 * MM_Ender of Infernal Mobs 1.3.1, ported 1:1.
 */
public class MM_Ender extends MobModifier {
    private long nextAbilityUse = 0L;
    private static final long coolDown = 15000L;
    private static final String[] suffix = {" the Enderborn", " the Trickster"};
    private static final String[] prefix = {" enderborn ", " tricky "};

    public MM_Ender(LivingEntity mob, MobModifier prevMod) {
        this.modName = "Ender";
        this.nextMod = prevMod;
    }

    @Override
    public float onHurt(LivingEntity mob, DamageSource source, float damage) {
        long time = System.currentTimeMillis();
        if (time > this.nextAbilityUse && source.getEntity() != null && source.getEntity() != mob && this.teleportToEntity(mob, source.getEntity())) {
            this.nextAbilityUse = time + coolDown;
            hurt(source.getEntity(), mob.damageSources().mobAttack(mob), damage);
            return super.onHurt(mob, source, 0);
        }
        return super.onHurt(mob, source, damage);
    }

    private boolean teleportToEntity(LivingEntity mob, Entity par1Entity) {
        Vec3 vector = new Vec3(mob.getX() - par1Entity.getX(),
                mob.getBoundingBox().minY + mob.getBbHeight() / 2.0F - par1Entity.getY() + par1Entity.getEyeHeight(),
                mob.getZ() - par1Entity.getZ()).normalize();
        double telDist = 16.0;
        double destX = mob.getX() + (mob.getRandom().nextDouble() - 0.5) * 8.0 - vector.x * telDist;
        double destY = mob.getY() + (mob.getRandom().nextInt(16) - 8) - vector.y * telDist;
        double destZ = mob.getZ() + (mob.getRandom().nextDouble() - 0.5) * 8.0 - vector.z * telDist;
        return this.teleportTo(mob, destX, destY, destZ);
    }

    private boolean teleportTo(LivingEntity mob, double destX, double destY, double destZ) {
        double oldX = mob.getX();
        double oldY = mob.getY();
        double oldZ = mob.getZ();
        boolean success = false;
        double posY = destY;
        int x = Mth.floor(destX);
        int y = Mth.floor(destY);
        int z = Mth.floor(destZ);
        if (mob.level().hasChunkAt(new BlockPos(x, y, z))) {
            boolean hitGround = false;
            while (!hitGround && y < 96) {
                if (mob.level().getBlockState(new BlockPos(x, y - 1, z)).isSolid()) {
                    hitGround = true;
                } else {
                    posY--;
                    y--;
                }
            }
            if (!hitGround) {
                return false;
            }
            mob.setPos(destX, posY, destZ);
            if (mob.level().noCollision(mob, mob.getBoundingBox()) && !mob.level().containsAnyLiquid(mob.getBoundingBox())) {
                success = true;
            }
        }
        if (!success) {
            mob.setPos(oldX, oldY, oldZ);
            return false;
        }
        int range = 128;
        for (int i = 0; i < range; i++) {
            double var19 = i / (range - 1.0);
            float var21 = (mob.getRandom().nextFloat() - 0.5F) * 0.2F;
            float var22 = (mob.getRandom().nextFloat() - 0.5F) * 0.2F;
            float var23 = (mob.getRandom().nextFloat() - 0.5F) * 0.2F;
            double var24 = oldX + (mob.getX() - oldX) * var19 + (mob.getRandom().nextDouble() - 0.5) * mob.getBbWidth() * 2.0;
            double var26 = oldY + (mob.getY() - oldY) * var19 + mob.getRandom().nextDouble() * mob.getBbHeight();
            double var28 = oldZ + (mob.getZ() - oldZ) * var19 + (mob.getRandom().nextDouble() - 0.5) * mob.getBbWidth() * 2.0;
            serverLevel(mob).sendParticles(ParticleTypes.PORTAL, var24, var26, var28, 0, var21, var22, var23, 1.0);
        }
        mob.level().playSound(null, oldX, oldY, oldZ, SoundEvents.ENDERMAN_TELEPORT, mob.getSoundSource(), 1.0F, 1.0F);
        playSound(mob, SoundEvents.ENDERMAN_TELEPORT, 1.0F, 1.0F);
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
