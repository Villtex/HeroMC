package hexxit.worldgen152.core.populate;

import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Where mods hook into chunk population, as they did in Forge 7.8 for Minecraft 1.5.2:
 * <ul>
 * <li>{@link #registerWorldGenerator} is {@code GameRegistry.registerWorldGenerator}: the generators run
 * after the vanilla population of a chunk, in the order they were registered, and share one random
 * sequence per chunk.</li>
 * <li>{@link #addListener} receives what was posted as {@code PopulateChunkEvent}, {@code DecorateBiomeEvent}
 * and {@code OreGenEvent}. Returning {@code false} from one of the questions is the old
 * {@code Result.DENY}: the vanilla step is skipped (and draws no random numbers), so the mod can do its
 * own.</li>
 * </ul>
 * Register in the same order as the original mods loaded, otherwise the shared random sequences differ.
 */
public final class OldTerrainGen {
    /** IWorldGenerator. Chunk coordinates, not block coordinates. */
    public interface WorldGenerator {
        void generate(Random random, int chunkX, int chunkZ, OldWorld world);
    }

    /** PopulateChunkEvent.Populate.EventType, as far as the overworld uses it. */
    public enum Populate { LAKE, LAVA, DUNGEON, ICE }

    /** DecorateBiomeEvent.Decorate.EventType. */
    public enum Decorate { SAND, CLAY, SAND_PASS2, TREE, BIG_SHROOM, FLOWERS, GRASS, DEAD_BUSH, LILYPAD, SHROOM, REED, PUMPKIN, CACTUS, LAKE }

    /** OreGenEvent.GenerateMinable.EventType. */
    public enum Ore { DIRT, GRAVEL, COAL, IRON, GOLD, REDSTONE, DIAMOND, LAPIS }

    /** The terrain events of one chunk. {@code x}/{@code z} of the decorate and ore events are block coordinates of the chunk corner. */
    public interface Listener {
        default void populatePre(OldWorld world, Random random, int chunkX, int chunkZ) {
        }

        default boolean populate(OldWorld world, Random random, int chunkX, int chunkZ, boolean villageGenerated, Populate type) {
            return true;
        }

        default void populatePost(OldWorld world, Random random, int chunkX, int chunkZ, boolean villageGenerated) {
        }

        default void decoratePre(OldWorld world, Random random, int x, int z) {
        }

        default boolean decorate(OldWorld world, Random random, int x, int z, Decorate type) {
            return true;
        }

        default void decoratePost(OldWorld world, Random random, int x, int z) {
        }

        default void oreGenPre(OldWorld world, Random random, int x, int z) {
        }

        default boolean generateOre(OldWorld world, Random random, int x, int z, Ore type) {
            return true;
        }

        default void oreGenPost(OldWorld world, Random random, int x, int z) {
        }
    }

    private static final List<WorldGenerator> GENERATORS = new CopyOnWriteArrayList<>();
    private static final List<Listener> LISTENERS = new CopyOnWriteArrayList<>();

    private OldTerrainGen() {
    }

    public static void registerWorldGenerator(WorldGenerator generator) {
        GENERATORS.add(generator);
    }

    public static void addListener(Listener listener) {
        LISTENERS.add(listener);
    }

    static List<WorldGenerator> generators() {
        return GENERATORS;
    }

    static List<Listener> listeners() {
        return LISTENERS;
    }

    static boolean populate(OldWorld world, Random random, int chunkX, int chunkZ, boolean village, Populate type) {
        boolean allowed = true;
        for (Listener listener : LISTENERS) {
            allowed &= listener.populate(world, random, chunkX, chunkZ, village, type);
        }
        return allowed;
    }

    static boolean decorate(OldWorld world, Random random, int x, int z, Decorate type) {
        boolean allowed = true;
        for (Listener listener : LISTENERS) {
            allowed &= listener.decorate(world, random, x, z, type);
        }
        return allowed;
    }

    static boolean generateOre(OldWorld world, Random random, int x, int z, Ore type) {
        boolean allowed = true;
        for (Listener listener : LISTENERS) {
            allowed &= listener.generateOre(world, random, x, z, type);
        }
        return allowed;
    }
}
