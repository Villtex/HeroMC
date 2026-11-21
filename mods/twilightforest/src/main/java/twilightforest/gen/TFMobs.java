package twilightforest.gen;

import hexxit.worldgen152.core.populate.OldEntity;
import hexxit.worldgen152.core.populate.OldWorld;

/**
 * Mobs the decoration spawns (TFGenPenguins, the ravens of TFGenMonolith): world.spawnEntityInWorld of a mob set to
 * (x, y, z) with the given yaw. The old name ("Penguin") resolves through OldGameIds (TFEntities.registerOld).
 */
public final class TFMobs {
    private TFMobs() {
    }

    static void spawn(OldWorld world, String name, int x, int y, int z, float yaw) {
        world.spawnEntity(new OldEntity.Mob(TFTreasure.SPAWNER_PREFIX + name, x, y, z, yaw, null));
    }
}
