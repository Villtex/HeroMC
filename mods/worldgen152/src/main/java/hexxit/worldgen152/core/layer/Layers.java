package hexxit.worldgen152.core.layer;

import hexxit.worldgen152.core.OldBiome;

/**
 * The biome layout chain of Minecraft 1.5.2 (GenLayer.initializeAllBiomeGenerators and its stages).
 * Every stage consumes its pseudo-random numbers in the original order, so a seed yields the original map.
 */
public final class Layers {
    private static final int OCEAN = 0;
    private static final int PLAINS = 1;
    private static final int DESERT = 2;
    private static final int EXTREME_HILLS = 3;
    private static final int FOREST = 4;
    private static final int TAIGA = 5;
    private static final int SWAMPLAND = 6;
    private static final int RIVER = 7;
    private static final int FROZEN_OCEAN = 10;
    private static final int FROZEN_RIVER = 11;
    private static final int ICE_PLAINS = 12;
    private static final int ICE_MOUNTAINS = 13;
    private static final int MUSHROOM_ISLAND = 14;
    private static final int MUSHROOM_ISLAND_SHORE = 15;
    private static final int BEACH = 16;
    private static final int DESERT_HILLS = 17;
    private static final int FOREST_HILLS = 18;
    private static final int TAIGA_HILLS = 19;
    private static final int EXTREME_HILLS_EDGE = 20;
    private static final int JUNGLE = 21;
    private static final int JUNGLE_HILLS = 22;

    private Layers() {
    }

    /** The two layers a world samples: [0] at 1:4 scale for terrain shaping, [1] per block column. */
    public record Stack(Layer quarter, Layer block) {
    }

    /**
     * @param landBiomes the biomes land is filled with, in world-type order
     * @param biomeSize  4 for the default world type, 6 for large biomes
     */
    public static Stack build(long seed, OldBiome[] landBiomes, int biomeSize) {
        Layer layer = new Island(1L);
        layer = new FuzzyZoom(2000L, layer);
        layer = new AddIsland(1L, layer);
        layer = new Zoom(2001L, layer);
        layer = new AddIsland(2L, layer);
        layer = new AddSnow(2L, layer);
        layer = new Zoom(2002L, layer);
        layer = new AddIsland(3L, layer);
        layer = new Zoom(2003L, layer);
        layer = new AddIsland(4L, layer);
        Layer continents = new AddMushroomIsland(5L, layer);

        Layer rivers = Zoom.magnify(1000L, continents, 0);
        rivers = new RiverInit(100L, rivers);
        rivers = Zoom.magnify(1000L, rivers, biomeSize + 2);
        rivers = new River(1L, rivers);
        rivers = new Smooth(1000L, rivers);

        Layer biomes = Zoom.magnify(1000L, continents, 0);
        biomes = new Biome(200L, biomes, landBiomes);
        biomes = Zoom.magnify(1000L, biomes, 2);
        biomes = new Hills(1000L, biomes);
        for (int i = 0; i < biomeSize; i++) {
            biomes = new Zoom(1000 + i, biomes);
            if (i == 0) {
                biomes = new AddIsland(3L, biomes);
            }
            if (i == 1) {
                biomes = new Shore(1000L, biomes);
                biomes = new SwampRivers(1000L, biomes);
            }
        }
        biomes = new Smooth(1000L, biomes);

        Layer mixed = new RiverMix(100L, biomes, rivers);
        Layer voronoi = new VoronoiZoom(10L, mixed);
        mixed.initWorldSeed(seed);
        voronoi.initWorldSeed(seed);
        return new Stack(mixed, voronoi);
    }

    static final class Island extends Layer {
        Island(long salt) {
            super(salt);
        }

        @Override
        public int[] sample(int x, int z, int width, int height) {
            int[] out = new int[width * height];
            for (int dz = 0; dz < height; dz++) {
                for (int dx = 0; dx < width; dx++) {
                    this.initChunkSeed(x + dx, z + dz);
                    out[dx + dz * width] = this.nextInt(10) == 0 ? 1 : 0;
                }
            }
            if (x > -width && x <= 0 && z > -height && z <= 0) {
                out[-x + -z * width] = 1;
            }
            return out;
        }
    }

    static class Zoom extends Layer {
        Zoom(long salt, Layer parent) {
            super(salt);
            this.parent = parent;
        }

        static Layer magnify(long salt, Layer parent, int times) {
            Layer layer = parent;
            for (int i = 0; i < times; i++) {
                layer = new Zoom(salt + i, layer);
            }
            return layer;
        }

        @Override
        public int[] sample(int x, int z, int width, int height) {
            int px = x >> 1;
            int pz = z >> 1;
            int pw = (width >> 1) + 3;
            int ph = (height >> 1) + 3;
            int[] in = this.parent.sample(px, pz, pw, ph);
            int[] zoomed = new int[pw * 2 * ph * 2];
            int stride = pw << 1;
            for (int dz = 0; dz < ph - 1; dz++) {
                int index = (dz << 1) * stride;
                int a = in[0 + (dz + 0) * pw];
                int c = in[0 + (dz + 1) * pw];
                for (int dx = 0; dx < pw - 1; dx++) {
                    this.initChunkSeed(dx + px << 1, dz + pz << 1);
                    int b = in[dx + 1 + (dz + 0) * pw];
                    int d = in[dx + 1 + (dz + 1) * pw];
                    zoomed[index] = a;
                    zoomed[index++ + stride] = this.choose(a, c);
                    zoomed[index] = this.choose(a, b);
                    zoomed[index++ + stride] = this.corner(a, b, c, d);
                    a = b;
                    c = d;
                }
            }
            int[] out = new int[width * height];
            for (int dz = 0; dz < height; dz++) {
                System.arraycopy(zoomed, (dz + (z & 1)) * (pw << 1) + (x & 1), out, dz * width, width);
            }
            return out;
        }

        final int choose(int a, int b) {
            return this.nextInt(2) == 0 ? a : b;
        }

        final int random(int a, int b, int c, int d) {
            int pick = this.nextInt(4);
            if (pick == 0) {
                return a;
            } else if (pick == 1) {
                return b;
            } else {
                return pick == 2 ? c : d;
            }
        }

        /** Most common of the four corners where there is one, otherwise a random corner. */
        int corner(int a, int b, int c, int d) {
            if (b == c && c == d) {
                return b;
            } else if (a == b && a == c) {
                return a;
            } else if (a == b && a == d) {
                return a;
            } else if (a == c && a == d) {
                return a;
            } else if (a == b && c != d) {
                return a;
            } else if (a == c && b != d) {
                return a;
            } else if (a == d && b != c) {
                return a;
            } else if (b == a && c != d) {
                return b;
            } else if (b == c && a != d) {
                return b;
            } else if (b == d && a != c) {
                return b;
            } else if (c == a && b != d) {
                return c;
            } else if (c == b && a != d) {
                return c;
            } else if (c == d && a != b) {
                return c;
            } else if (d == a && b != c) {
                return c;
            } else if (d == b && a != c) {
                return c;
            } else if (d == c && a != b) {
                return c;
            } else {
                return this.random(a, b, c, d);
            }
        }
    }

    static final class FuzzyZoom extends Zoom {
        FuzzyZoom(long salt, Layer parent) {
            super(salt, parent);
        }

        @Override
        int corner(int a, int b, int c, int d) {
            return this.random(a, b, c, d);
        }
    }

    static final class AddIsland extends Layer {
        AddIsland(long salt, Layer parent) {
            super(salt);
            this.parent = parent;
        }

        @Override
        public int[] sample(int x, int z, int width, int height) {
            int pw = width + 2;
            int[] in = this.parent.sample(x - 1, z - 1, pw, height + 2);
            int[] out = new int[width * height];
            for (int dz = 0; dz < height; dz++) {
                for (int dx = 0; dx < width; dx++) {
                    int nw = in[dx + 0 + (dz + 0) * pw];
                    int ne = in[dx + 2 + (dz + 0) * pw];
                    int sw = in[dx + 0 + (dz + 2) * pw];
                    int se = in[dx + 2 + (dz + 2) * pw];
                    int center = in[dx + 1 + (dz + 1) * pw];
                    this.initChunkSeed(dx + x, dz + z);
                    if (center != 0 || nw == 0 && ne == 0 && sw == 0 && se == 0) {
                        if (center > 0 && (nw == 0 || ne == 0 || sw == 0 || se == 0)) {
                            if (this.nextInt(5) == 0) {
                                out[dx + dz * width] = center == ICE_PLAINS ? FROZEN_OCEAN : 0;
                            } else {
                                out[dx + dz * width] = center;
                            }
                        } else {
                            out[dx + dz * width] = center;
                        }
                    } else {
                        int bound = 1;
                        int pick = 1;
                        if (nw != 0 && this.nextInt(bound++) == 0) {
                            pick = nw;
                        }
                        if (ne != 0 && this.nextInt(bound++) == 0) {
                            pick = ne;
                        }
                        if (sw != 0 && this.nextInt(bound++) == 0) {
                            pick = sw;
                        }
                        if (se != 0 && this.nextInt(bound++) == 0) {
                            pick = se;
                        }
                        if (this.nextInt(3) == 0) {
                            out[dx + dz * width] = pick;
                        } else if (pick == ICE_PLAINS) {
                            out[dx + dz * width] = FROZEN_OCEAN;
                        } else {
                            out[dx + dz * width] = 0;
                        }
                    }
                }
            }
            return out;
        }
    }

    static final class AddSnow extends Layer {
        AddSnow(long salt, Layer parent) {
            super(salt);
            this.parent = parent;
        }

        @Override
        public int[] sample(int x, int z, int width, int height) {
            int pw = width + 2;
            int[] in = this.parent.sample(x - 1, z - 1, pw, height + 2);
            int[] out = new int[width * height];
            for (int dz = 0; dz < height; dz++) {
                for (int dx = 0; dx < width; dx++) {
                    int center = in[dx + 1 + (dz + 1) * pw];
                    this.initChunkSeed(dx + x, dz + z);
                    if (center == 0) {
                        out[dx + dz * width] = 0;
                    } else {
                        out[dx + dz * width] = this.nextInt(5) == 0 ? ICE_PLAINS : 1;
                    }
                }
            }
            return out;
        }
    }

    static final class AddMushroomIsland extends Layer {
        AddMushroomIsland(long salt, Layer parent) {
            super(salt);
            this.parent = parent;
        }

        @Override
        public int[] sample(int x, int z, int width, int height) {
            int pw = width + 2;
            int[] in = this.parent.sample(x - 1, z - 1, pw, height + 2);
            int[] out = new int[width * height];
            for (int dz = 0; dz < height; dz++) {
                for (int dx = 0; dx < width; dx++) {
                    int nw = in[dx + 0 + (dz + 0) * pw];
                    int ne = in[dx + 2 + (dz + 0) * pw];
                    int sw = in[dx + 0 + (dz + 2) * pw];
                    int se = in[dx + 2 + (dz + 2) * pw];
                    int center = in[dx + 1 + (dz + 1) * pw];
                    this.initChunkSeed(dx + x, dz + z);
                    if (center == 0 && nw == 0 && ne == 0 && sw == 0 && se == 0 && this.nextInt(100) == 0) {
                        out[dx + dz * width] = MUSHROOM_ISLAND;
                    } else {
                        out[dx + dz * width] = center;
                    }
                }
            }
            return out;
        }
    }

    static final class RiverInit extends Layer {
        RiverInit(long salt, Layer parent) {
            super(salt);
            this.parent = parent;
        }

        @Override
        public int[] sample(int x, int z, int width, int height) {
            int[] in = this.parent.sample(x, z, width, height);
            int[] out = new int[width * height];
            for (int dz = 0; dz < height; dz++) {
                for (int dx = 0; dx < width; dx++) {
                    this.initChunkSeed(dx + x, dz + z);
                    out[dx + dz * width] = in[dx + dz * width] > 0 ? this.nextInt(2) + 2 : 0;
                }
            }
            return out;
        }
    }

    static final class River extends Layer {
        River(long salt, Layer parent) {
            super(salt);
            this.parent = parent;
        }

        @Override
        public int[] sample(int x, int z, int width, int height) {
            int pw = width + 2;
            int[] in = this.parent.sample(x - 1, z - 1, pw, height + 2);
            int[] out = new int[width * height];
            for (int dz = 0; dz < height; dz++) {
                for (int dx = 0; dx < width; dx++) {
                    int west = in[dx + 0 + (dz + 1) * pw];
                    int east = in[dx + 2 + (dz + 1) * pw];
                    int north = in[dx + 1 + (dz + 0) * pw];
                    int south = in[dx + 1 + (dz + 2) * pw];
                    int center = in[dx + 1 + (dz + 1) * pw];
                    if (center != 0 && west != 0 && east != 0 && north != 0 && south != 0
                            && center == west && center == north && center == east && center == south) {
                        out[dx + dz * width] = -1;
                    } else {
                        out[dx + dz * width] = RIVER;
                    }
                }
            }
            return out;
        }
    }

    static final class Smooth extends Layer {
        Smooth(long salt, Layer parent) {
            super(salt);
            this.parent = parent;
        }

        @Override
        public int[] sample(int x, int z, int width, int height) {
            int pw = width + 2;
            int[] in = this.parent.sample(x - 1, z - 1, pw, height + 2);
            int[] out = new int[width * height];
            for (int dz = 0; dz < height; dz++) {
                for (int dx = 0; dx < width; dx++) {
                    int west = in[dx + 0 + (dz + 1) * pw];
                    int east = in[dx + 2 + (dz + 1) * pw];
                    int north = in[dx + 1 + (dz + 0) * pw];
                    int south = in[dx + 1 + (dz + 2) * pw];
                    int center = in[dx + 1 + (dz + 1) * pw];
                    if (west == east && north == south) {
                        this.initChunkSeed(dx + x, dz + z);
                        center = this.nextInt(2) == 0 ? west : north;
                    } else {
                        if (west == east) {
                            center = west;
                        }
                        if (north == south) {
                            center = north;
                        }
                    }
                    out[dx + dz * width] = center;
                }
            }
            return out;
        }
    }

    static final class Biome extends Layer {
        private final OldBiome[] allowed;

        Biome(long salt, Layer parent, OldBiome[] allowed) {
            super(salt);
            this.parent = parent;
            this.allowed = allowed;
        }

        @Override
        public int[] sample(int x, int z, int width, int height) {
            int[] in = this.parent.sample(x, z, width, height);
            int[] out = new int[width * height];
            for (int dz = 0; dz < height; dz++) {
                for (int dx = 0; dx < width; dx++) {
                    this.initChunkSeed(dx + x, dz + z);
                    int land = in[dx + dz * width];
                    if (land == 0) {
                        out[dx + dz * width] = 0;
                    } else if (land == MUSHROOM_ISLAND) {
                        out[dx + dz * width] = land;
                    } else if (land == 1) {
                        out[dx + dz * width] = this.allowed[this.nextInt(this.allowed.length)].id;
                    } else {
                        // Snowy land: only taiga survives, everything else becomes ice plains.
                        int pick = this.allowed[this.nextInt(this.allowed.length)].id;
                        out[dx + dz * width] = pick == TAIGA ? pick : ICE_PLAINS;
                    }
                }
            }
            return out;
        }
    }

    static final class Hills extends Layer {
        Hills(long salt, Layer parent) {
            super(salt);
            this.parent = parent;
        }

        @Override
        public int[] sample(int x, int z, int width, int height) {
            int pw = width + 2;
            int[] in = this.parent.sample(x - 1, z - 1, pw, height + 2);
            int[] out = new int[width * height];
            for (int dz = 0; dz < height; dz++) {
                for (int dx = 0; dx < width; dx++) {
                    this.initChunkSeed(dx + x, dz + z);
                    int center = in[dx + 1 + (dz + 1) * pw];
                    if (this.nextInt(3) == 0) {
                        int hills = center;
                        if (center == DESERT) {
                            hills = DESERT_HILLS;
                        } else if (center == FOREST) {
                            hills = FOREST_HILLS;
                        } else if (center == TAIGA) {
                            hills = TAIGA_HILLS;
                        } else if (center == PLAINS) {
                            hills = FOREST;
                        } else if (center == ICE_PLAINS) {
                            hills = ICE_MOUNTAINS;
                        } else if (center == JUNGLE) {
                            hills = JUNGLE_HILLS;
                        }
                        if (hills == center) {
                            out[dx + dz * width] = center;
                        } else {
                            int north = in[dx + 1 + (dz + 1 - 1) * pw];
                            int east = in[dx + 1 + 1 + (dz + 1) * pw];
                            int west = in[dx + 1 - 1 + (dz + 1) * pw];
                            int south = in[dx + 1 + (dz + 1 + 1) * pw];
                            if (north == center && east == center && west == center && south == center) {
                                out[dx + dz * width] = hills;
                            } else {
                                out[dx + dz * width] = center;
                            }
                        }
                    } else {
                        out[dx + dz * width] = center;
                    }
                }
            }
            return out;
        }
    }

    static final class Shore extends Layer {
        Shore(long salt, Layer parent) {
            super(salt);
            this.parent = parent;
        }

        @Override
        public int[] sample(int x, int z, int width, int height) {
            int pw = width + 2;
            int[] in = this.parent.sample(x - 1, z - 1, pw, height + 2);
            int[] out = new int[width * height];
            for (int dz = 0; dz < height; dz++) {
                for (int dx = 0; dx < width; dx++) {
                    this.initChunkSeed(dx + x, dz + z);
                    int center = in[dx + 1 + (dz + 1) * pw];
                    int north = in[dx + 1 + (dz + 1 - 1) * pw];
                    int east = in[dx + 1 + 1 + (dz + 1) * pw];
                    int west = in[dx + 1 - 1 + (dz + 1) * pw];
                    int south = in[dx + 1 + (dz + 1 + 1) * pw];
                    if (center == MUSHROOM_ISLAND) {
                        if (north != OCEAN && east != OCEAN && west != OCEAN && south != OCEAN) {
                            out[dx + dz * width] = center;
                        } else {
                            out[dx + dz * width] = MUSHROOM_ISLAND_SHORE;
                        }
                    } else if (center != OCEAN && center != RIVER && center != SWAMPLAND && center != EXTREME_HILLS) {
                        if (north != OCEAN && east != OCEAN && west != OCEAN && south != OCEAN) {
                            out[dx + dz * width] = center;
                        } else {
                            out[dx + dz * width] = BEACH;
                        }
                    } else if (center == EXTREME_HILLS) {
                        if (north == EXTREME_HILLS && east == EXTREME_HILLS && west == EXTREME_HILLS && south == EXTREME_HILLS) {
                            out[dx + dz * width] = center;
                        } else {
                            out[dx + dz * width] = EXTREME_HILLS_EDGE;
                        }
                    } else {
                        out[dx + dz * width] = center;
                    }
                }
            }
            return out;
        }
    }

    static final class SwampRivers extends Layer {
        SwampRivers(long salt, Layer parent) {
            super(salt);
            this.parent = parent;
        }

        @Override
        public int[] sample(int x, int z, int width, int height) {
            int pw = width + 2;
            int[] in = this.parent.sample(x - 1, z - 1, pw, height + 2);
            int[] out = new int[width * height];
            for (int dz = 0; dz < height; dz++) {
                for (int dx = 0; dx < width; dx++) {
                    this.initChunkSeed(dx + x, dz + z);
                    int center = in[dx + 1 + (dz + 1) * pw];
                    if ((center != SWAMPLAND || this.nextInt(6) != 0)
                            && (center != JUNGLE && center != JUNGLE_HILLS || this.nextInt(8) != 0)) {
                        out[dx + dz * width] = center;
                    } else {
                        out[dx + dz * width] = RIVER;
                    }
                }
            }
            return out;
        }
    }

    static final class RiverMix extends Layer {
        private final Layer biomes;
        private final Layer rivers;

        RiverMix(long salt, Layer biomes, Layer rivers) {
            super(salt);
            this.biomes = biomes;
            this.rivers = rivers;
        }

        @Override
        public void initWorldSeed(long seed) {
            this.biomes.initWorldSeed(seed);
            this.rivers.initWorldSeed(seed);
            super.initWorldSeed(seed);
        }

        @Override
        public int[] sample(int x, int z, int width, int height) {
            int[] biome = this.biomes.sample(x, z, width, height);
            int[] river = this.rivers.sample(x, z, width, height);
            int[] out = new int[width * height];
            for (int i = 0; i < width * height; i++) {
                if (biome[i] == OCEAN) {
                    out[i] = biome[i];
                } else if (river[i] >= 0) {
                    if (biome[i] == ICE_PLAINS) {
                        out[i] = FROZEN_RIVER;
                    } else if (biome[i] != MUSHROOM_ISLAND && biome[i] != MUSHROOM_ISLAND_SHORE) {
                        out[i] = river[i];
                    } else {
                        out[i] = MUSHROOM_ISLAND_SHORE;
                    }
                } else {
                    out[i] = biome[i];
                }
            }
            return out;
        }
    }

    static final class VoronoiZoom extends Layer {
        VoronoiZoom(long salt, Layer parent) {
            super(salt);
            this.parent = parent;
        }

        @Override
        public int[] sample(int x, int z, int width, int height) {
            x -= 2;
            z -= 2;
            int shift = 2;
            int cell = 1 << shift;
            int px = x >> shift;
            int pz = z >> shift;
            int pw = (width >> shift) + 3;
            int ph = (height >> shift) + 3;
            int[] in = this.parent.sample(px, pz, pw, ph);
            int zw = pw << shift;
            int zh = ph << shift;
            int[] zoomed = new int[zw * zh];
            for (int dz = 0; dz < ph - 1; dz++) {
                int a = in[0 + (dz + 0) * pw];
                int c = in[0 + (dz + 1) * pw];
                for (int dx = 0; dx < pw - 1; dx++) {
                    double spread = cell * 0.9;
                    this.initChunkSeed(dx + px << shift, dz + pz << shift);
                    double ax = (this.nextInt(1024) / 1024.0 - 0.5) * spread;
                    double az = (this.nextInt(1024) / 1024.0 - 0.5) * spread;
                    this.initChunkSeed(dx + px + 1 << shift, dz + pz << shift);
                    double bx = (this.nextInt(1024) / 1024.0 - 0.5) * spread + cell;
                    double bz = (this.nextInt(1024) / 1024.0 - 0.5) * spread;
                    this.initChunkSeed(dx + px << shift, dz + pz + 1 << shift);
                    double cx = (this.nextInt(1024) / 1024.0 - 0.5) * spread;
                    double cz = (this.nextInt(1024) / 1024.0 - 0.5) * spread + cell;
                    this.initChunkSeed(dx + px + 1 << shift, dz + pz + 1 << shift);
                    double ex = (this.nextInt(1024) / 1024.0 - 0.5) * spread + cell;
                    double ez = (this.nextInt(1024) / 1024.0 - 0.5) * spread + cell;
                    int b = in[dx + 1 + (dz + 0) * pw];
                    int d = in[dx + 1 + (dz + 1) * pw];
                    for (int iz = 0; iz < cell; iz++) {
                        int index = ((dz << shift) + iz) * zw + (dx << shift);
                        for (int ix = 0; ix < cell; ix++) {
                            double da = (iz - az) * (iz - az) + (ix - ax) * (ix - ax);
                            double db = (iz - bz) * (iz - bz) + (ix - bx) * (ix - bx);
                            double dc = (iz - cz) * (iz - cz) + (ix - cx) * (ix - cx);
                            double de = (iz - ez) * (iz - ez) + (ix - ex) * (ix - ex);
                            if (da < db && da < dc && da < de) {
                                zoomed[index++] = a;
                            } else if (db < da && db < dc && db < de) {
                                zoomed[index++] = b;
                            } else if (dc < da && dc < db && dc < de) {
                                zoomed[index++] = c;
                            } else {
                                zoomed[index++] = d;
                            }
                        }
                    }
                    a = b;
                    c = d;
                }
            }
            int[] out = new int[width * height];
            for (int dz = 0; dz < height; dz++) {
                System.arraycopy(zoomed, (dz + (z & cell - 1)) * (pw << shift) + (x & cell - 1), out, dz * width, width);
            }
            return out;
        }
    }
}
