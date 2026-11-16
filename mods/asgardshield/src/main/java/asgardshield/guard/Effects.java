package asgardshield.guard;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.cubemob.Slime;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.LevelEvent;

/** Particles and the teleport of the ender items (ItemAsgardShield.enderFx, teleportRandomly, teleportTo). */
public final class Effects {
    private Effects() {
    }

    /** enderFx: three portal particles around a block position. */
    public static void enderFx(ServerLevel level, int x, int y, int z, RandomSource random) {
        for (int i = 0; i < 3; i++) {
            double py = y + random.nextFloat();
            int sx = random.nextInt(2) * 2 - 1;
            int sz = random.nextInt(2) * 2 - 1;
            double vy = (random.nextFloat() - 0.5) * 0.125;
            double pz = z + 0.5 + 0.25 * sz;
            double vz = random.nextFloat() * sz;
            double px = x + 0.5 + 0.25 * sx;
            double vx = random.nextFloat() * sx;
            level.sendParticles(ParticleTypes.PORTAL, px, py, pz, 0, vx, vy, vz, 1.0);
        }
    }

    /** teleportRandomly: the attacker of an ender shield jumps up to 12 blocks away. */
    public static boolean teleportRandomly(Entity entity, Player player) {
        RandomSource random = player.getRandom();
        double x = entity.getX() + (random.nextDouble() - 0.5) * 24.0;
        double y = entity.getY() + (random.nextInt(24) - 12);
        double z = entity.getZ() + (random.nextDouble() - 0.5) * 24.0;
        return teleportTo(entity, player, x, y, z);
    }

    private static boolean teleportTo(Entity entity, Player player, double x, double y, double z) {
        if (!(entity.level() instanceof ServerLevel level)) {
            return false;
        }
        double oldX = entity.getX();
        double oldY = entity.getY();
        double oldZ = entity.getZ();
        entity.setPos(x, y, z);
        boolean success = false;
        BlockPos.MutableBlockPos pos = BlockPos.containing(x, y, z).mutable();
        if (level.hasChunkAt(pos)) {
            boolean ground = false;
            while (!ground && pos.getY() > level.getMinY()) {
                if (level.getBlockState(pos.below()).isSolid()) {
                    ground = true;
                } else {
                    y--;
                    pos.move(0, -1, 0);
                }
            }
            if (ground) {
                entity.setPos(x, y, z);
                // slimes always land (no room check)
                success = entity instanceof Slime || level.noCollision(entity) && !level.containsAnyLiquid(entity.getBoundingBox());
            }
        }
        entity.setPos(oldX, oldY, oldZ);
        if (!success) {
            return false;
        }
        entity.teleportTo(x, y, z);
        RandomSource random = player.getRandom();
        for (int i = 0; i < 128; i++) {
            double t = i / 127.0;
            float vx = (random.nextFloat() - 0.5F) * 0.2F;
            float vy = (random.nextFloat() - 0.5F) * 0.2F;
            float vz = (random.nextFloat() - 0.5F) * 0.2F;
            double px = oldX + (x - oldX) * t + (random.nextDouble() - 0.5) * entity.getBbWidth() * 2.0;
            double py = oldY + (y - oldY) * t + random.nextDouble() * entity.getBbHeight();
            double pz = oldZ + (z - oldZ) * t + (random.nextDouble() - 0.5) * entity.getBbWidth() * 2.0;
            level.sendParticles(ParticleTypes.PORTAL, px, py, pz, 0, vx, vy, vz, 1.0);
        }
        level.playSound(null, oldX, oldY, oldZ, SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 1.0F, 1.0F);
        level.playSound(null, entity, SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 1.0F, 1.0F);
        level.levelEvent(LevelEvent.PARTICLES_EYE_OF_ENDER_DEATH, BlockPos.containing(Math.round(player.getX() - 0.5), Math.round(player.getY() + 0.5),
                Math.round(player.getZ())), 0);
        return true;
    }

    static float rad(float degrees) {
        return degrees * Mth.DEG_TO_RAD;
    }
}
