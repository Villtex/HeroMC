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
}
