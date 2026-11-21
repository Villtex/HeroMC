package twilightforest.gen.structure;

import hexxit.worldgen152.core.populate.OldWorld;
import hexxit.worldgen152.core.structure.StructureBoundingBox;
import hexxit.worldgen152.core.structure.StructureComponent;
import hexxit.worldgen152.core.structure.StructureStart;
import twilightforest.gen.TFFeature;
import twilightforest.gen.TFLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * MapGenTFMajorFeature: the structures of the Twilight Forest's feature chunks (MapGenStructure with
 * canSpawnStructureAtCoords = TFFeature.getFeatureDirectlyAt(...).structureEnabled).
 *
 * <p>An instance belongs to one {@link OldWorld}, because the pieces remember what they have built (the quest grove's
 * dispenser, the minoshroom) and pieces that fail are dropped. In 1.5.2 starts within 8 chunks of a generated chunk
 * existed, and overlapping starts were built in the order of a hash table; here every start exists from the
 * beginning, and overlapping starts are built ordered by chunk x, then z.
 */
public final class MapGenTFMajorFeature {
    /**
     * How far (chunks) from its start chunk a structure may reach (MapGenBase.range is 8; the lower pieces of the knight
     * stronghold go up to 112 blocks from the entrance, plus their own size).
     */
    public static final int REACH = 10;

    private final TFLayout layout;
    private final long rand1;
    private final long rand2;
    private final Map<Long, StructureStart> starts = new HashMap<>();

    public MapGenTFMajorFeature(TFLayout layout) {
        this.layout = layout;
        Random rand = new Random(layout.seed());
        this.rand1 = rand.nextLong();
        this.rand2 = rand.nextLong();
    }

    /** The structures of a world, made once per world. */
    public static MapGenTFMajorFeature of(OldWorld world, TFLayout layout) {
        return world.worldState(MapGenTFMajorFeature.class, () -> new MapGenTFMajorFeature(layout));
    }

    /** The start in a chunk, or null if no structure starts there. */
    public synchronized StructureStart start(int chunkX, int chunkZ) {
        long key = (long) chunkX << 32 | chunkZ & 0xFFFFFFFFL;
        if (this.starts.containsKey(key)) {
            return this.starts.get(key);
        }
        StructureStart start = null;
        if (TFFeature.getFeatureDirectlyAt(chunkX, chunkZ, this.layout).structureEnabled) {
            // getStructureStart: the seed of the chunk from the world seed
            Random rand = new Random(chunkX * this.rand1 ^ chunkZ * this.rand2 ^ this.layout.seed());
            rand.nextInt();
            start = new StructureTFMajorFeatureStart(this.layout, rand, chunkX, chunkZ);
        }
        this.starts.put(key, start);
        return start;
    }

    /** generateStructuresInChunk: builds what lies in the population area of the chunk; true if anything did. */
    public boolean generateStructuresInChunk(OldWorld world, Random random, int chunkX, int chunkZ) {
        int minX = (chunkX << 4) + 8;
        int minZ = (chunkZ << 4) + 8;
        boolean generated = false;
        for (StructureStart start : this.near(chunkX, chunkZ)) {
            if (start.isSizeableStructure() && start.getBoundingBox().intersectsWith(minX, minZ, minX + 15, minZ + 15)) {
                start.generateStructure(world, random, new StructureBoundingBox(minX, minZ, minX + 15, minZ + 15));
                generated = true;
            }
        }
        return generated;
    }

    private List<StructureStart> near(int chunkX, int chunkZ) {
        List<StructureStart> list = new ArrayList<>();
        for (int x = chunkX - REACH; x <= chunkX + REACH; x++) {
            for (int z = chunkZ - REACH; z <= chunkZ + REACH; z++) {
                StructureStart start = this.start(x, z);
                if (start != null) {
                    list.add(start);
                }
            }
        }
        return list;
    }

    /**
     * getSpawnListIndexAt: the highest spawn list index of the pieces holding the position, -1 outside all of them (0
     * for a piece that is not a Twilight Forest one).
     */
    public synchronized int getSpawnListIndexAt(int x, int y, int z) {
        int highestFoundIndex = -1;
        for (StructureStart start : this.near(x >> 4, z >> 4)) {
            if (start.isSizeableStructure() && start.getBoundingBox().intersectsWith(x, z, x, z)) {
                for (StructureComponent component : start.getComponents()) {
                    if (component.getBoundingBox().isVecInside(x, y, z)) {
                        if (!(component instanceof StructureTFComponent tfComponent)) {
                            return 0;
                        }
                        if (tfComponent.spawnListIndex > highestFoundIndex) {
                            highestFoundIndex = tfComponent.spawnListIndex;
                        }
                    }
                }
            }
        }
        return highestFoundIndex;
    }
}
