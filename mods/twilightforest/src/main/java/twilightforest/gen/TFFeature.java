package twilightforest.gen;

import hexxit.worldgen152.core.OldBiome;

import java.util.Random;

/**
 * The major features of the Twilight Forest (TFFeature): one sits at the centre of every chunk whose x and z are
 * multiples of 16, its kind chosen from the biome there. The terrain raises hills and flattens courtyards around
 * them; the buildings follow in Teil 24d/24e. Mob lists of the features come with Teil 24c.
 */
public final class TFFeature {
    private static final TFFeature[] FEATURE_LIST = new TFFeature[256];

    public static final TFFeature NOTHING = new TFFeature(0, 0, "No Feature").enableDecorations().disableStructure();
    public static final TFFeature HILL1 = new TFFeature(1, 1, "Small Hollow Hill").enableDecorations();
    public static final TFFeature HILL2 = new TFFeature(2, 2, "Medium Hollow Hill").enableDecorations();
    public static final TFFeature HILL3 = new TFFeature(3, 3, "Large Hollow Hill").enableDecorations();
    public static final TFFeature HEDGE_MAZE = new TFFeature(4, 2, "Hedge Maze");
    public static final TFFeature NAGA_LAIR = new TFFeature(5, 3, "Naga Courtyard");
    public static final TFFeature WIZARD_TOWER = new TFFeature(6, 1, "Wizard Tower");
    public static final TFFeature GLACIER_MAZE = new TFFeature(7, 2, "Glacier Maze").disableStructure();
    public static final TFFeature QUEST_ISLAND = new TFFeature(8, 1, "Quest Island").disableStructure();
    public static final TFFeature QUEST_GROVE = new TFFeature(9, 1, "Quest Grove");
    public static final TFFeature DRUID_GROVE = new TFFeature(10, 1, "Druid Grove").disableStructure();
    public static final TFFeature FLOAT_RUINS = new TFFeature(11, 3, "Floating Ruins").disableStructure();
    public static final TFFeature HYDRA_LAIR = new TFFeature(12, 2, "Hydra Lair");
    public static final TFFeature LABYRINTH = new TFFeature(13, 3, "Labyrinth").enableDecorations();
    public static final TFFeature DARK_TOWER = new TFFeature(14, 1, "Dark Tower");
    public static final TFFeature STRONGHOLD = new TFFeature(15, 3, "Knight Stronghold").enableDecorations();
    public static final TFFeature WORLD_TREE = new TFFeature(16, 3, "World Tree").disableStructure();
    public static final TFFeature YETI_CAVE = new TFFeature(17, 2, "Yeti Lairs").disableStructure().enableDecorations();
    public static final TFFeature TROLL_CAVE = new TFFeature(18, 2, "Troll Lairs").disableStructure().enableDecorations();
    public static final TFFeature FINAL_CASTLE = new TFFeature(19, 2, "Final Castle").disableStructure();
    public static final TFFeature UNDERGROUND = new TFFeature(255, 0, "Underground");

    public final int featureID;
    public final int size;
    public final String name;
    public boolean chunkDecorationsEnabled;
    public boolean structureEnabled = true;

    private TFFeature(int id, int size, String name) {
        this.featureID = id;
        this.size = size;
        this.name = name;
        FEATURE_LIST[id] = this;
    }

    private TFFeature enableDecorations() {
        this.chunkDecorationsEnabled = true;
        return this;
    }

    private TFFeature disableStructure() {
        this.structureEnabled = false;
        return this;
    }

    public static TFFeature byId(int id) {
        TFFeature feature = FEATURE_LIST[id & 255];
        return feature != null ? feature : NOTHING;
    }

    /** TFWorldChunkManager.isInFeatureChunk: chunk x and z multiples of 16. */
    public static boolean isInFeatureChunk(int chunkX, int chunkZ) {
        return chunkX % 16 == 0 && chunkZ % 16 == 0;
    }

    public static TFFeature getFeatureDirectlyAt(int chunkX, int chunkZ, TFLayout layout) {
        return isInFeatureChunk(chunkX, chunkZ) ? generateFeatureFor(chunkX, chunkZ, layout) : NOTHING;
    }

    /** The feature of a feature chunk, from the biome at its centre and a random number of the chunk. */
    public static TFFeature generateFeatureFor(int chunkX, int chunkZ, TFLayout layout) {
        OldBiome biomeAt = layout.biomeAt((chunkX << 4) + 8, (chunkZ << 4) + 8);
        // int arithmetic as in the original
        Random hillRNG = new Random(layout.seed() + chunkX * 25117 + chunkZ * 151121);
        int randnum = hillRNG.nextInt(16);
        if (biomeAt == TFBiomes.GLACIER) {
            return GLACIER_MAZE;
        }
        if (biomeAt == TFBiomes.LAKE) {
            return QUEST_ISLAND;
        }
        if (biomeAt == TFBiomes.ENCHANTED_FOREST) {
            return QUEST_GROVE;
        }
        if (biomeAt == TFBiomes.FIRE_SWAMP) {
            return HYDRA_LAIR;
        }
        if (biomeAt == TFBiomes.CLEARING || biomeAt == TFBiomes.CLEARING_BORDER) {
            return LABYRINTH;
        }
        if (biomeAt == TFBiomes.DARK_FOREST) {
            switch (randnum % 3) {
                case 1:
                    return DARK_TOWER;
                case 2:
                    return STRONGHOLD;
                default:
                    break;
            }
        }
        switch (randnum) {
            case 7, 8, 9:
                return HILL2;
            case 10:
                return HILL3;
            case 11, 12:
                return HEDGE_MAZE;
            case 13:
                return biomeAt != TFBiomes.SWAMP ? NAGA_LAIR : HYDRA_LAIR;
            case 14, 15:
                return WIZARD_TOWER;
            default:
                return HILL1;
        }
    }

    /** The feature within as many chunks as it is large, nearest rings first. */
    public static TFFeature getNearestFeature(int cx, int cz, TFLayout layout) {
        for (int rad = 1; rad <= 3; rad++) {
            for (int x = -rad; x <= rad; x++) {
                for (int z = -rad; z <= rad; z++) {
                    TFFeature directlyAt = getFeatureDirectlyAt(x + cx, z + cz, layout);
                    if (directlyAt.size == rad) {
                        return directlyAt;
                    }
                }
            }
        }
        return NOTHING;
    }

    /** The feature of the feature chunk nearest to a chunk (for mob spawns). */
    public static TFFeature getNearestFeatureIncludeMore(int chunkX, int chunkZ, TFLayout layout) {
        int featureX = Math.round(chunkX / 16.0F) * 16;
        int featureZ = Math.round(chunkZ / 16.0F) * 16;
        return getFeatureDirectlyAt(featureX, featureZ, layout);
    }

    /** Centre of the nearest feature relative to the chunk corner, block x/z; {0, 0} if there is none. */
    public static int[] getNearestCenter(int cx, int cz, TFLayout layout) {
        for (int rad = 1; rad <= 3; rad++) {
            for (int x = -rad; x <= rad; x++) {
                for (int z = -rad; z <= rad; z++) {
                    if (getFeatureDirectlyAt(x + cx, z + cz, layout).size == rad) {
                        return new int[]{x * 16 + 8, z * 16 + 8};
                    }
                }
            }
        }
        return new int[]{0, 0};
    }

    /** TFFeature.getNearestCenterXYZ: centre of the feature cell a block position lies in. */
    public static int[] getNearestCenterXYZ(int x, int z) {
        int fx = (int) (Math.round(x / 256.0) * 256L + 8L);
        int fz = (int) (Math.round(z / 256.0) * 256L + 8L);
        return new int[]{fx, TFTerrain.SEALEVEL, fz};
    }

    @Override
    public String toString() {
        return this.name;
    }
}
