package hexxit.worldgen152.core.populate;

/** An entity a 1.5.2 structure put into the world while it was built. */
public sealed interface OldEntity {
    double x();

    double y();

    double z();

    /** Villager with the profession number of 1.5.2: 0 farmer, 1 librarian, 2 priest, 3 smith, 4 butcher. */
    record Villager(double x, double y, double z, int profession) implements OldEntity {
    }

    record Witch(double x, double y, double z) implements OldEntity {
    }

    /** Minecart with chest (mineshafts), 27 slots, empty slots are {@code null}. */
    record MinecartChest(double x, double y, double z, OldStack[] slots) implements OldEntity {
    }

    /**
     * Painting: {@code tileX/Y/Z} is the wall block it hangs on (EntityHanging.xPosition ...), {@code direction}
     * the side it faces (0 south, 1 west, 2 north, 3 east), {@code motive} the EnumArt title ("Kebab", ...).
     */
    record Painting(int tileX, int tileY, int tileZ, int direction, String motive) implements OldEntity {
        public double x() {
            return this.tileX + 0.5;
        }

        public double y() {
            return this.tileY + 0.5;
        }

        public double z() {
            return this.tileZ + 0.5;
        }
    }

    /** Item frame like {@link Painting}, with the item shown ({@code null} for none) and its rotation 0 to 3. */
    record ItemFrame(int tileX, int tileY, int tileZ, int direction, OldStack item, int rotation) implements OldEntity {
        public double x() {
            return this.tileX + 0.5;
        }

        public double y() {
            return this.tileY + 0.5;
        }

        public double z() {
            return this.tileZ + 0.5;
        }
    }

    /**
     * A mob or other entity by its 1.5.2 name (EntityList: "Cow", "Zombie", "Skeleton", "Minecart", ...), with
     * its yaw and what it holds and wears (Equipment: 0 hand, 1 boots ... 4 helmet; {@code null} when empty).
     */
    record Mob(String name, double x, double y, double z, float yaw, OldStack[] equipment) implements OldEntity {
    }

    /** A dropped item lying in the world. */
    record Item(double x, double y, double z, OldStack stack) implements OldEntity {
    }
}
