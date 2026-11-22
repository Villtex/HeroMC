package hexxit.worldgen152.core.populate;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

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
 * Each registration names the FML mod id of the original mod. Generators and listeners run in the mod
 * load order of the Hexxit 1.0.11 server ({@link #LOAD_ORDER}), and in registration order within one mod,
 * whatever order the NeoForge mod constructors (which run in parallel) happen to call in.
 */
public final class OldTerrainGen {
    /** IWorldGenerator. Chunk coordinates, not block coordinates. */
    public interface WorldGenerator {
        void generate(Random random, int chunkX, int chunkZ, OldWorld world);
    }

    /** PopulateChunkEvent.Populate.EventType: the first four in the overworld, the others in the Nether. */
    public enum Populate { LAKE, LAVA, DUNGEON, ICE, NETHER_LAVA, FIRE, GLOWSTONE }

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

    /** FML log of the Hexxit 1.0.11 server: the order in which the mods loaded and registered their generators. */
    public static final List<String> LOAD_ORDER = List.of("mod_bspkrsCore", "CoFHCore", "CoFHWorld", "TreeCapitator",
            "ImmibisMicroblocksInstallCheck", "DamageIndicatorsMod", "AsgardShield_Core", "ASAddon_Hallowed",
            "HarkenScythe_Core", "ASAddon_HarkenScythe", "ASAddon_NetherEnder", "ASAddon_ColorAPI", "Artifice",
            "ArtificeCompat|Chisel", "ArtificeCompat|EE3", "ArtificeCompat|Forestry", "ArtificeCompat|MFR",
            "ArtificeCompat|Railcraft", "ArtificeCompat|Vanilla", "BattleTowers", "BetterDungeons", "BiblioCraft",
            "DimDoors", "eplus", "ExtrabiomesXL", "hexxitgear", "InfernalMobs", "Meteors", "More Bows", "LegendGear",
            "Natura", "Natura|CompatMineFactoryReloaded", "OffLawn", "ProjectZulu|Core", "ProjectZulu|Blocks",
            "ProjectZulu|Dungeon", "ProjectZulu|Mobs", "ProjectZulu|World", "xreliquary", "rftl", "AS_Ruins",
            "secretroomsmod", "SoulShards", "TConstruct", "TConstruct|CompatMineFactoryReloaded", "WildCaves3",
            "BetterStorage", "Backpack", "ChestTransporter", "ImmibisCore", "ObsidiPlates", "QuantumCraft",
            "TwilightForest", "chococraft");

    /** Chunks a generator may write into, relative to the chunk it runs for: x from minX to maxX, z from minZ to maxZ. */
    public record Reach(int minX, int minZ, int maxX, int maxZ) {
    }

    /**
     * For generators that sometimes write farther than the margin {@link OldDecoration} populates around a tile (a
     * structure of 100 blocks from the corner of its chunk): where they do it, known from the seed and the biome
     * layout alone, without populating anything.
     */
    public interface ReachProvider {
        /** The reach of the generator in that chunk, or {@code null} when it stays within the usual margin there. */
        Reach reach(long seed, hexxit.worldgen152.core.BiomeLayout layout, int chunkX, int chunkZ);
    }

    /** A provider and how many chunks from a tile it has to be asked (the largest reach it can return). */
    record ReachEntry(int distance, ReachProvider provider) {
    }

    private record Entry<T>(int rank, int seq, T value) {
    }

    private static final List<Entry<WorldGenerator>> GENERATOR_ENTRIES = new ArrayList<>();
    private static final List<Entry<Listener>> LISTENER_ENTRIES = new ArrayList<>();
    private static volatile List<WorldGenerator> GENERATORS = List.of();
    private static volatile List<Listener> LISTENERS = List.of();
    private static volatile List<ReachEntry> REACHES = List.of();
    private static final java.util.Map<String, WorldGenerator> NAMED = new java.util.concurrent.ConcurrentHashMap<>();

    private OldTerrainGen() {
    }

    private static int rank(String originalModId) {
        int rank = LOAD_ORDER.indexOf(originalModId);
        if (rank < 0) {
            throw new IllegalArgumentException("Unknown original mod id " + originalModId + " (see OldTerrainGen.LOAD_ORDER)");
        }
        return rank;
    }

    private static <T> List<T> insert(List<Entry<T>> entries, String originalModId, T value) {
        entries.add(new Entry<>(rank(originalModId), entries.size(), value));
        entries.sort(Comparator.<Entry<T>>comparingInt(Entry::rank).thenComparingInt(Entry::seq));
        return entries.stream().map(Entry::value).toList();
    }

    /**
     * {@code GameRegistry.registerWorldGenerator} of the original mod {@code originalModId} (its FML mod id).
     * {@code name} ({@code "natura:trees"}) names the generator on its own: in the world type Hexxit Integrated it
     * runs as the feature of that name, which data packs and other mods can move or remove.
     */
    public static synchronized void registerWorldGenerator(String originalModId, String name, WorldGenerator generator) {
        if (NAMED.putIfAbsent(name, generator) != null) {
            throw new IllegalArgumentException("World generator " + name + " is registered twice");
        }
        GENERATORS = insert(GENERATOR_ENTRIES, originalModId, generator);
    }

    /**
     * A generator that exists only as a feature of the world type Hexxit Integrated (a piece of a 1.5.2 generator
     * with heights for the deeper world of today); it never runs in the 1.5.2 population.
     */
    public static void registerFeature(String name, WorldGenerator generator) {
        if (NAMED.putIfAbsent(name, generator) != null) {
            throw new IllegalArgumentException("World generator " + name + " is registered twice");
        }
    }

    /**
     * For features with {@code "y_offset": -64} (old y = real y + 64): where a 1.5.2 height {@code y} goes. The
     * underground of 1.5.2 (y 0-63) is stretched over today's -64 to 63, above it heights stay (real y = y).
     */
    public static int deepY(int y) {
        return y < 64 ? 2 * y : y + 64;
    }

    /** The generator registered under {@code name}, or {@code null}. */
    public static WorldGenerator generator(String name) {
        return NAMED.get(name);
    }

    /** The names of all registered generators. */
    public static java.util.Set<String> names() {
        return java.util.Collections.unmodifiableSet(NAMED.keySet());
    }

    /** A terrain-event handler of the original mod {@code originalModId} (its FML mod id). */
    public static synchronized void addListener(String originalModId, Listener listener) {
        LISTENERS = insert(LISTENER_ENTRIES, originalModId, listener);
    }

    /**
     * Declares where the generator of {@code originalModId} writes beyond the usual margin; {@code distance} is the
     * largest distance in chunks between a chunk it runs for and a chunk it writes into. The tiles near such a
     * chunk are populated with a margin wide enough to run it, so its structure is not cut at a tile border.
     */
    public static synchronized void registerReach(String originalModId, int distance, ReachProvider provider) {
        rank(originalModId);
        List<ReachEntry> reaches = new ArrayList<>(REACHES);
        reaches.add(new ReachEntry(distance, provider));
        REACHES = List.copyOf(reaches);
    }

    static List<ReachEntry> reaches() {
        return REACHES;
    }

    public static List<WorldGenerator> generators() {
        return GENERATORS;
    }

    static List<Listener> listeners() {
        return LISTENERS;
    }

    static boolean populate(OldWorld world, Random random, int chunkX, int chunkZ, boolean village, Populate type) {
        boolean allowed = true;
        for (Listener listener : listeners()) {
            allowed &= listener.populate(world, random, chunkX, chunkZ, village, type);
        }
        return allowed;
    }

    static boolean decorate(OldWorld world, Random random, int x, int z, Decorate type) {
        boolean allowed = true;
        for (Listener listener : listeners()) {
            allowed &= listener.decorate(world, random, x, z, type);
        }
        return allowed;
    }

    static boolean generateOre(OldWorld world, Random random, int x, int z, Ore type) {
        boolean allowed = true;
        for (Listener listener : listeners()) {
            allowed &= listener.generateOre(world, random, x, z, type);
        }
        return allowed;
    }
}
