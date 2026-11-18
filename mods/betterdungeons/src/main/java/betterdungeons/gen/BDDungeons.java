package betterdungeons.gen;

import hexxit.worldgen152.core.populate.OldWorld;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.StringTokenizer;

/**
 * DungeonRegister with the dungeon types of Chocolate/DungeonConfig (DungeonBase.readData and the readSpecialData of
 * each type), in the order of the folder listing.
 */
public final class BDDungeons {
    /** BuilderBase: builds a dungeon at the corner of a chunk ({@code generate(random, world, x, z, mob)}). */
    public interface Builder {
        void generate(Random random, OldWorld world, int x, int z, int mob);
    }

    /** A registered dungeon: DungeonBase with the builder its "builder" line chose. */
    public record Dungeon(String name, String type, String[] biomes, int chance, int mobID, int dimensionID, int icon,
                          java.util.function.Supplier<Builder> builder) {
    }

    private static volatile List<Dungeon> list;

    private BDDungeons() {
    }

    public static List<Dungeon> list() {
        List<Dungeon> dungeons = list;
        if (dungeons == null) {
            synchronized (BDDungeons.class) {
                dungeons = list;
                if (dungeons == null) {
                    dungeons = new ArrayList<>();
                    for (String file : BDFiles.list("DungeonConfig")) {
                        Dungeon dungeon = read(file, BDFiles.properties("DungeonConfig/" + file));
                        if (dungeon != null) {
                            dungeons.add(dungeon);
                        }
                    }
                    dungeons = Collections.unmodifiableList(dungeons);
                    list = dungeons;
                }
            }
        }
        return dungeons;
    }

    /** DungeonBase.readData. */
    static Dungeon read(String name, Properties prop) {
        String dungeonType = prop.getProperty("builder");
        if (dungeonType == null) {
            return null;
        }
        // (the original trimmed into nothing: the type has to match without the spaces around it anyway)
        java.util.function.Supplier<Builder> builder = builder(dungeonType, prop);
        if (builder == null) {
            return null;
        }
        // "for (i = 0; i < stkn.countTokens(); i++) ret[i] = stkn.nextToken()": countTokens() shrinks with every token
        // taken, so only the first half of the biomes (rounded up) is read; the other entries stay null
        StringTokenizer stkn = new StringTokenizer(prop.getProperty("biomes"), ",");
        String[] biomes = new String[stkn.countTokens()];
        for (int i = 0; i < stkn.countTokens(); i++) {
            biomes[i] = stkn.nextToken().trim();
        }
        return new Dungeon(name, dungeonType, biomes, integer(prop, "chance", 10), BDMobs.byName(prop.getProperty("mob").trim()),
                integer(prop, "dimensionID", 0), integer(prop, "icon", 10), builder);
    }

    /** The builder of a dungeon type with its readSpecialData; {@code null} for a type the mod does not know. */
    private static java.util.function.Supplier<Builder> builder(String type, Properties prop) {
        switch (type) {
            case "templateSurface": {
                String folder = prop.getProperty("folder").trim();
                int offset = integer(prop, "underGroundOffset", -10);
                return () -> new BuilderTemplateSurface(folder, offset);
            }
            case "templateFloating": {
                String folder = prop.getProperty("folder").trim();
                int posY = integer(prop, "posY", 10);
                return () -> new BuilderTemplate(folder, posY);
            }
            case "iceCave": {
                // DungeonIceCave.readSpecialData
                int size = Math.max(16, integer(prop, "size", 100));
                int caveSpread = Math.min(90, Math.max(10, integer(prop, "caveSpread", 45)));
                boolean spawnBoss = bool(prop, "spawnBoss", true);
                int floorBlock = integer(prop, "floorBlock", 79);
                int posY = integer(prop, "posY", 10);
                int heightVariation = integer(prop, "heightVariation", 40);
                return () -> new BuilderIceCave(size, caveSpread, floorBlock, spawnBoss, posY, heightVariation);
            }
            case "jungleCave": {
                int size = integer(prop, "size", 48);
                int height = integer(prop, "height", 40);
                int floorBlock = integer(prop, "floorBlock", 2);
                boolean vines = bool(prop, "vines", true);
                boolean vegetation = bool(prop, "vegetation", true);
                return () -> new BuilderJungleCave(size, height, floorBlock, vines, vegetation);
            }
            case "linearDungeon": {
                int rooms = Math.max(1, integer(prop, "rooms", 25));
                return () -> new BuilderStronghold(rooms);
            }
            case "dungeon": {
                // DungeonBetterDungeon.readSpecialData: the size is read from "rows" (the pack's "size = 40" is not read)
                int size = Math.max(16, integer(prop, "rows", 40));
                int roomSize = Math.min(size / 2, integer(prop, "roomSize", 12));
                int posY = integer(prop, "posY", 10);
                int heightVariation = integer(prop, "heightVariation", 40);
                int wallBlock = integer(prop, "wallBlock", 4);
                return () -> new BuilderDungeon(size, roomSize, posY, heightVariation, wallBlock);
            }
            case "volcano": {
                // DungeonVolcano.readSpecialData: "oreChance" is read from "size"
                int oreChance = integer(prop, "size", 8);
                int blockWalls = integer(prop, "blockWalls", 1);
                int blockPath = integer(prop, "blockPath", 87);
                int dungeonRooms = integer(prop, "dungeonRooms", 25);
                boolean generatePath = bool(prop, "generatePath", true);
                boolean generateDungeon = bool(prop, "generateDungeon", false);
                return () -> new BuilderVolcano(blockWalls, blockPath, oreChance, generatePath, generateDungeon, dungeonRooms);
            }
            case "emptyCave": {
                // DungeonCave.readSpecialData (initChance and spawnBoss are read, the builder ignores them)
                int caveBlock = integer(prop, "caveBlock", 0);
                int size = integer(prop, "size", 32);
                int height = integer(prop, "height", 28);
                int posY = integer(prop, "posY", 20);
                int boss = integer(prop, "boss", 3);
                return () -> new BuilderEmptyCave(caveBlock, size, height, posY, boss);
            }
            case "lavaCity", "skyscraper", "skyscraperGrid":
                // BuilderNether (the nether city: dimension 0 and biome "Hell" in the pack, so it never appears),
                // BuilderSkyscraper and BuilderCity (no configuration of the pack uses them): registered and drawn
                // like in the original, not built
                return () -> NOT_BUILT;
            default:
                return null;
        }
    }

    /** The dungeon types that never get built in the pack: builds nothing. */
    static final Builder NOT_BUILT = (random, world, x, z, mob) -> {
    };

    static int integer(Properties prop, String name, int defaultValue) {
        String s = prop.getProperty(name);
        return s == null ? defaultValue : Integer.parseInt(s.trim());
    }

    static boolean bool(Properties prop, String name, boolean defaultValue) {
        String s = prop.getProperty(name);
        return s == null ? defaultValue : s.trim().equals("true");
    }
}
