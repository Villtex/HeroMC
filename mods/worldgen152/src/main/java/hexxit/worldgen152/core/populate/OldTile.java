package hexxit.worldgen152.core.populate;

/** Tile entity data a generator attached to a block. */
public sealed interface OldTile {
    /** Chest inventory (also the trapped chest), 27 slots, empty slots are {@code null}. */
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

    /** TileEntityFurnace: slots 0 input, 1 fuel, 2 output; burn and cook time in ticks. */
    record Furnace(OldStack[] slots, int burnTime, int cookTime) implements OldTile {
        public Furnace() {
            this(new OldStack[3], 0, 0);
        }
    }

    /** TileEntitySign: four lines of text. */
    record Sign(String[] lines) implements OldTile {
    }

    /** TileEntityRecordPlayer: the record inside, or {@code null}. */
    record Jukebox(OldStack record) implements OldTile {
    }

    /** TileEntityBrewingStand: slots 0 to 2 bottles, 3 ingredient. */
    record BrewingStand(OldStack[] slots, int brewTime) implements OldTile {
    }

    /**
     * Tile entity of a mod block. The mod tells the game side how to write it
     * ({@code hexxit.worldgen152.OldModData.registerTile}).
     */
    non-sealed interface Mod extends OldTile {
    }
}
