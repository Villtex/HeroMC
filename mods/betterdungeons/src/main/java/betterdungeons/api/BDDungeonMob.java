package betterdungeons.api;

/**
 * Implemented by the mobs of the port that come in levels and kinds (EntityHuman and its subclasses). The spawner,
 * the spawn eggs and the default {@link RegisterDungeonMobs} entries create the type by its id and call this instead
 * of the 1.5.2 constructors.
 */
public interface BDDungeonMob {
    /**
     * The constructors Mob(world, lvl) ({@code type < 0}: no kind given) and Mob(world, lvl, type) of 1.5.2; the level is
     * 0..4 as in the spawner's metadata.
     */
    void initDungeonMob(int level, int type);
}
