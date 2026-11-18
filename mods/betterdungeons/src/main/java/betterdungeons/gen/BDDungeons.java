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
            case "iceCave", "jungleCave", "lavaCity", "linearDungeon", "dungeon", "volcano", "skyscraper", "skyscraperGrid", "emptyCave":
                // the generated dungeons follow in part 13 of the port; they are registered (and drawn) like in the original
                return () -> NOT_YET;
            default:
                return null;
        }
    }

    /** Placeholder for the dungeon types that are not ported yet: builds nothing. */
    static final Builder NOT_YET = (random, world, x, z, mob) -> {
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
