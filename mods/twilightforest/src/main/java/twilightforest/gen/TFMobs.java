package twilightforest.gen;

import hexxit.worldgen152.core.populate.OldWorld;

/**
 * Mobs the decoration spawns (TFGenPenguins, the ravens of TFGenMonolith). The mobs themselves come with Teil 24c;
 * until then the generators draw their random numbers and spawn nothing.
 */
public final class TFMobs {
    private TFMobs() {
    }

    /** world.spawnEntityInWorld of a mob set to (x, y, z) with the given yaw (setLocationAndAngles). */
    static void spawn(OldWorld world, String name, int x, int y, int z, float yaw) {
        // Teil 24c: world.spawnEntity(...) with the mob of this name
    }
}
