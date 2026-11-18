package ruins.gen;

import hexxit.worldgen152.core.OldBiome;
import hexxit.worldgen152.core.OldBiomes;
import hexxit.worldgen152.core.populate.OldTerrainGen;
import hexxit.worldgen152.core.populate.OldWorld;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.logging.Logger;

/**
 * The world generation of Ruins 10.3 (RuinsMod.RuinsWorldGenerator, RuinHandler, RuinGenerator) without game
 * classes, so that tools/expect.sh can run it. Options are the pack's mods/ruins.txt (3 % per try, 6 tries per chunk,
 * 75 % for another ruin nearby), templates those of the pack's ruins-data archives.
 *
 * <p>Two things of the original depended on what had been generated before and are bound to the chunk here: the
 * last ten ruins whose bounding boxes a new ruin must not touch (here only those of the same chunk and its site),
 * and unique templates, which the original took out of the world for good after building them once (here only
 * for the rest of the chunk). A site that would reach further from its chunk than the population of a tile covers
 * (3 chunks towards -x/-z, 4 towards +x/+z) is not built, so that no ruin is cut at a tile border.
 */
public final class RuinsGen {
    public static final String FML_ID = "AS_Ruins";
    private static final Logger LOGGER = Logger.getLogger("Ruins");
    private static final int BIOME_NONE = 500;
    /** Where the original found its templates: mods/resources/ruins below the installation (its canonical path). */
    private static final String PATH = "mods/resources/ruins/";
    private static volatile Handler handler;
    /** Told of every ruin built ("x y z rotation template"), for tools/expect.sh; {@code null} in the game. */
    public static volatile java.util.function.Consumer<String> built;

    private RuinsGen() {
    }

    public static void register() {
        OldTerrainGen.registerWorldGenerator(FML_ID, RuinsGen::generate);
    }

    private static void generate(Random random, int chunkX, int chunkZ, OldWorld world) {
        if (Math.abs(chunkX) >= 3 || Math.abs(chunkZ) >= 3) {
            Handler h = handler();
            if (h.loaded) {
                new Generator(h, world, chunkX * 16, chunkZ * 16).generate(random, world.isHellWorld());
            }
        }
    }

    /** How many templates were loaded (loads them on first use). */
    public static int templateCount() {
        return handler().count;
    }

    static Handler handler() {
        Handler h = handler;
        if (h == null) {
            synchronized (RuinsGen.class) {
                h = handler;
                if (h == null) {
                    h = new Handler();
                    handler = h;
                }
            }
        }
        return h;
    }

    /** RuinHandler: the templates of each biome (500 = generic) and the global options. Read once, then unchanged. */
    static final class Handler {
        final List<Set<RuinTemplate>> templates = new ArrayList<>();
        /** vars[0] count, vars[1] weight, vars[2] chance of a biome-specific template. */
        final int[][] vars = new int[3][501];
        int triesPerChunkNormal = 6;
        int chanceToSpawnNormal = 10;
        int chanceForSiteNormal = 15;
        int triesPerChunkNether = 6;
        int chanceToSpawnNether = 10;
        int chanceForSiteNether = 15;
        boolean loaded;
        int count;

        Handler() {
            for (int j = 0; j < this.vars[0].length; j++) {
                this.vars[2][j] = 75;
            }
            for (int fill = 0; fill < 501; fill++) {
                // HashSet as in the original: its iteration order (by the hash of the path) decides the weighted pick
                this.templates.add(new HashSet<>());
            }
            if (RuinsFiles.list("generic") == null && RuinsFiles.list("forest") == null) {
                LOGGER.severe("Ruins templates missing in the jar; the Ruins mod could not be loaded");
                return;
            }
            List<OldBiome> biomes = OldBiomes.all();
            this.addRuins("generic", BIOME_NONE, biomes);
            this.vars[0][BIOME_NONE] = this.templates.get(BIOME_NONE).size();
            this.recalcBiomeWeight(BIOME_NONE);
            for (OldBiome biome : biomes) {
                this.addRuins(biome.name.toLowerCase(), biome.id, biomes);
                this.vars[0][biome.id] = this.templates.get(biome.id).size();
                this.recalcBiomeWeight(biome.id);
            }
            try {
                this.readGlobalOptions(biomes);
            } catch (IOException | RuntimeException e) {
                LOGGER.warning("There was an error when loading the Ruins options file. Defaults will be used instead: " + e);
            }
            this.loaded = true;
            LOGGER.info("Ruins mod loaded " + this.count + " templates");
        }

        private void recalcBiomeWeight(int biome) {
            int weight = 0;
            for (RuinTemplate r : this.templates.get(biome)) {
                weight += r.getWeight();
            }
            this.vars[1][biome] = weight;
        }

        private void addRuins(String folder, int biomeID, List<OldBiome> biomes) {
            List<String> files = RuinsFiles.list(folder);
            if (files == null) {
                return;
            }
            Set<RuinTemplate> target = this.templates.get(biomeID);
            for (String file : files) {
                if (!file.substring(file.lastIndexOf('.') + 1).equals("tml")) {
                    continue;
                }
                try {
                    RuinTemplate r = new RuinTemplate(PATH + folder + "/" + file, RuinsFiles.lines("ruins/" + folder + "/" + file));
                    target.add(r);
                    this.count++;
                    for (String biomeName : r.getBiomesToSpawnIn()) {
                        for (OldBiome b : biomes) {
                            if (b.name.toLowerCase().equals(biomeName)) {
                                if (b.id != biomeID) {
                                    this.templates.get(b.id).add(r);
                                }
                                break;
                            }
                        }
                    }
                } catch (Exception e) {
                    LOGGER.warning("There was a problem loading the Ruins file " + folder + "/" + file + ": " + e);
                }
            }
        }

        private void readGlobalOptions(List<OldBiome> biomes) throws IOException {
            for (String read : RuinsFiles.lines("ruins.txt")) {
                String[] check = read.split("=");
                switch (check[0]) {
                    case "tries_per_chunk_normal" -> this.triesPerChunkNormal = Integer.parseInt(check[1]);
                    case "chance_to_spawn_normal" -> this.chanceToSpawnNormal = Integer.parseInt(check[1]);
                    case "chance_for_site_normal" -> this.chanceForSiteNormal = Integer.parseInt(check[1]);
                    case "tries_per_chunk_nether" -> this.triesPerChunkNether = Integer.parseInt(check[1]);
                    case "chance_to_spawn_nether" -> this.chanceToSpawnNether = Integer.parseInt(check[1]);
                    case "chance_for_site_nether" -> this.chanceForSiteNether = Integer.parseInt(check[1]);
                    default -> {
                    }
                }
                if (read.startsWith("specific_")) {
                    check = read.split("_")[1].split("=");
                    for (OldBiome b : biomes) {
                        if (b.name.equalsIgnoreCase(check[0])) {
                            this.vars[2][b.id] = Integer.parseInt(check[1]);
                            break;
                        }
                    }
                }
            }
        }

        boolean useGeneric(Random random, int biome) {
            return biome == BIOME_NONE || random.nextInt(100) + 1 >= this.vars[2][biome];
        }

        /** getTemplate: a weighted pick; {@code removed} are the unique templates this chunk took out of the biome. */
        RuinTemplate getTemplate(Random random, int biome, Set<RuinTemplate> removed) {
            Set<RuinTemplate> set = this.templates.get(biome);
            int weight = this.vars[1][biome];
            if (!removed.isEmpty()) {
                // removeTemplate recalculated the weight from the remaining templates
                weight = 0;
                for (RuinTemplate r : set) {
                    if (!removed.contains(r)) {
                        weight += r.getWeight();
                    }
                }
            }
            if (weight <= 0) {
                return null; // nextInt threw, the original caught it
            }
            int rand = random.nextInt(weight);
            int oldval = 0;
            int increment = 0;
            RuinTemplate retval = null;
            for (RuinTemplate next : set) {
                if (removed.contains(next)) {
                    continue;
                }
                retval = next;
                increment += retval.getWeight();
                if (oldval <= rand && rand < increment) {
                    return retval;
                }
                oldval += retval.getWeight();
            }
            return retval;
        }
    }

    /** RuinGenerator for one chunk. */
    static final class Generator {
        private final Handler handler;
        private final OldWorld world;
        private final int originX;
        private final int originZ;
        private final List<int[]> lastTen = new ArrayList<>();
        private final java.util.Map<Integer, Set<RuinTemplate>> removed = new java.util.HashMap<>();

        Generator(Handler handler, OldWorld world, int originX, int originZ) {
            this.handler = handler;
            this.world = world;
            this.originX = originX;
            this.originZ = originZ;
        }

        void generate(Random random, boolean nether) {
            Handler h = this.handler;
            int tries = nether ? h.triesPerChunkNether : h.triesPerChunkNormal;
            int chance = nether ? h.chanceToSpawnNether : h.chanceToSpawnNormal;
            for (int c = 0; c < tries; c++) {
                if (random.nextInt(100) < chance) {
                    int xMod;
                    int zMod;
                    if (nether) {
                        xMod = random.nextInt(2) == 1 ? random.nextInt(16) : -random.nextInt(16);
                        zMod = random.nextInt(2) == 1 ? random.nextInt(16) : -random.nextInt(16);
                    } else {
                        xMod = random.nextInt(16);
                        zMod = random.nextInt(16);
                    }
                    this.createBuilding(random, this.originX + xMod, this.originZ + zMod, 0, nether);
                }
            }
        }

        private void createBuilding(Random random, int x, int z, int minDistance, boolean nether) {
            OldWorld world = this.world;
            int rotate = random.nextInt(4);
            int biomeID = world.getBiome(x, z).id;
            if (this.handler.useGeneric(random, biomeID)) {
                biomeID = BIOME_NONE;
            }
            RuinTemplate ruin = this.handler.getTemplate(random, biomeID, this.removed.getOrDefault(biomeID, Set.of()));
            if (ruin == null) {
                return;
            }
            if (minDistance != 0) {
                minDistance += random.nextInt(3) + ruin.getMinDistance();
                x += random.nextInt(2) == 1 ? -minDistance : minDistance;
                z += random.nextInt(2) == 1 ? -minDistance : minDistance;
            }
            if (this.willOverlap(ruin, x, z, rotate)) {
                int xTemp = getRandomAdjustment(random, minDistance);
                int zTemp = getRandomAdjustment(random, minDistance);
                if (this.willOverlap(ruin, xTemp, zTemp, rotate)) {
                    xTemp = getRandomAdjustment(random, minDistance);
                    zTemp = getRandomAdjustment(random, minDistance);
                    if (this.willOverlap(ruin, xTemp, zTemp, rotate)) {
                        return;
                    }
                }
                // the original's getRandomAdjustment ignored its base: the ruin jumps near 0, 0
                x = xTemp;
                z = zTemp;
            }
            if (!this.inReach(ruin.extent(x, z, rotate))) {
                return;
            }
            int y = findSuitableY(world, ruin, x, z, nether);
            if (y > 0) {
                if (!ruin.checkArea(world, x, y, z, rotate)) {
                    return;
                }
                ruin.doBuild(world, random, x, y, z, rotate);
                java.util.function.Consumer<String> report = built;
                if (report != null) {
                    report.accept(x + " " + y + " " + z + " " + rotate + " " + ruin.getName().substring(PATH.length()));
                }
                this.lastTen.add(ruin.getBoundingBox(x, z, rotate));
                if (this.lastTen.size() > 10) {
                    this.lastTen.remove(0);
                }
                int nextMinDistance = ruin.getMinDistance();
                if (ruin.isUnique()) {
                    this.removed.computeIfAbsent(biomeID, k -> new HashSet<>()).add(ruin);
                }
                if (random.nextInt(100) < (nether ? this.handler.chanceForSiteNether : this.handler.chanceForSiteNormal)) {
                    this.createBuilding(random, x, z, nextMinDistance, nether);
                }
            }
        }

        /** Whether the site stays inside what the population of a tile covers around this chunk. */
        private boolean inReach(int[] extent) {
            return extent[0] >= this.originX - 48 && extent[1] >= this.originZ - 48
                    && extent[2] < this.originX + 80 && extent[3] < this.originZ + 80;
        }

        private static int getRandomAdjustment(Random random, int minDistance) {
            return random.nextInt(8) - random.nextInt(8) + (random.nextInt(2) == 1 ? -minDistance : minDistance);
        }

        /** RuinBoundingBox.collides of the original: touching in x or in z is enough. */
        private boolean willOverlap(RuinTemplate r, int x, int z, int rotate) {
            int[] current = r.getBoundingBox(x, z, rotate);
            for (int i = this.lastTen.size() - 1; i >= 0; i--) {
                int[] box = this.lastTen.get(i);
                if (current[0] >= box[0] && current[0] <= box[1] || current[1] >= box[0] && current[1] <= box[1]
                        || current[2] >= box[2] && current[2] <= box[3] || current[3] >= box[2] && current[3] <= box[3]) {
                    return true;
                }
            }
            return false;
        }

        private static int findSuitableY(OldWorld world, RuinTemplate r, int x, int z, boolean nether) {
            if (nether) {
                if (x % 2 == 1 ^ z % 2 == 1) {
                    for (int y = 255; y > -1; y--) {
                        if (world.getBlockId(x, y, z) == 0) {
                            while (y > -1) {
                                if (!r.isAir(world.getBlockId(x, y, z)) && r.isAcceptable(world, x, y, z)) {
                                    return y;
                                }
                                y--;
                            }
                        }
                    }
                } else {
                    for (int y = 0; y < 256; y++) {
                        if (!r.isAir(world.getBlockId(x, y, z)) && r.isAcceptable(world, x, y - 1, z)) {
                            return y - 1;
                        }
                    }
                }
                return -1;
            }
            for (int y = 255; y > -1; y--) {
                if (r.isAcceptable(world, x, y, z)) {
                    return y;
                }
                if (!r.isAir(world.getBlockId(x, y, z))) {
                    return -1;
                }
            }
            return -1;
        }
    }
}
