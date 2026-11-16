package harkenscythe.mob;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

/** The enderman teleporting of 1.5.2 (EntityEnderman.teleportRandomly/teleportToEntity/shouldAttackPlayer). */
final class Teleports {
    private Teleports() {
    }

    /** shouldAttackPlayer: the player looks at it (no carved pumpkin on his head) and can see it. */
    static boolean isLookedAt(LivingEntity mob, Player player) {
        if (player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.HEAD).is(Items.CARVED_PUMPKIN.asItem())
                || player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.HEAD).is(Items.PUMPKIN)) {
            return false;
        }
        Vec3 look = player.getViewVector(1.0F).normalize();
        Vec3 to = new Vec3(mob.getX() - player.getX(), mob.getBoundingBox().minY + mob.getBbHeight() / 2.0F - player.getEyeY(),
                mob.getZ() - player.getZ());
        double dist = to.length();
        double dot = look.dot(to.normalize());
        return dot > 1.0 - 0.025 / dist && player.hasLineOfSight(mob);
    }

    static boolean randomly(LivingEntity mob) {
        double x = mob.getX() + (mob.getRandom().nextDouble() - 0.5) * 64.0;
        double y = mob.getY() + (mob.getRandom().nextInt(64) - 32);
        double z = mob.getZ() + (mob.getRandom().nextDouble() - 0.5) * 64.0;
        return to(mob, x, y, z);
    }

    static boolean towards(LivingEntity mob, Entity target) {
        Vec3 v = new Vec3(mob.getX() - target.getX(), mob.getBoundingBox().minY + mob.getBbHeight() / 2.0F - target.getY() + target.getEyeHeight(),
                mob.getZ() - target.getZ()).normalize();
        double x = mob.getX() + (mob.getRandom().nextDouble() - 0.5) * 8.0 - v.x * 16.0;
        double y = mob.getY() + (mob.getRandom().nextInt(16) - 8) - v.y * 16.0;
        double z = mob.getZ() + (mob.getRandom().nextDouble() - 0.5) * 8.0 - v.z * 16.0;
        return to(mob, x, y, z);
    }

    /** teleportTo: down to the first solid block, only into free space out of liquids; portal particles and sound. */
    static boolean to(LivingEntity mob, double x, double y, double z) {
        double ox = mob.getX();
        double oy = mob.getY();
        double oz = mob.getZ();
        if (!mob.randomTeleport(x, y, z, true, s -> false)) {
            return false;
        }
        mob.level().playSound(null, ox, oy, oz, SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 1.0F, 1.0F);
        mob.playSound(SoundEvents.ENDERMAN_TELEPORT, 1.0F, 1.0F);
        return true;
    }
}
