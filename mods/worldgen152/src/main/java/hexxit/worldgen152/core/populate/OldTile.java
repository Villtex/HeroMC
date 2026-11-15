package hexxit.worldgen152.core.populate;

/** Tile entity data a generator attached to a block. */
public sealed interface OldTile {
    /** Chest inventory, 27 slots, empty slots are {@code null}. */
    record Chest(OldStack[] slots) implements OldTile {
        public Chest() {
            this(new OldStack[27]);
        }
    }

    /** Dispenser inventory, 9 slots. */
    record Dispenser(OldStack[] slots) implements OldTile {
        public Dispenser() {
            this(new OldStack[9]);
        }
    }

    /** TileEntitySkull: skull type (0 skeleton, 1 wither skeleton, 2 zombie, 3 player, 4 creeper) and rotation. */
    record Skull(int type, int rotation) implements OldTile {
    }

    /** Mob spawner with the entity name of 1.5.2 ("Zombie", "Skeleton", ...). */
    record Spawner(String mob) implements OldTile {
    }
}
