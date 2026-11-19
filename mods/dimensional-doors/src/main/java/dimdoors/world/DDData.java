package dimdoors.world;

import com.mojang.serialization.Codec;
import dimdoors.DimDoors;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * What dimHelper of Dimensional Doors 1.4.1 kept in the file DimensionalDoorsData: every dimension that has links
 * ({@link DimData} by its 1.5.2 dimension id), the blocks of Limbo that decay, and which ids the other levels got.
 *
 * <p>The pocket dimensions of the original were dimensions of their own. Here they all live in the level
 * {@code dimdoors:pocket}, each in a square of {@value #CELL} blocks of its own ({@link DimData#cellX}); its blocks
 * stand where the original put them, moved by {@link DimData#offsetX}/{@link DimData#offsetZ}.
 */
public final class DDData extends SavedData {
    /** Edge of the square each pocket gets in the pocket level. */
    public static final int CELL = 512;
    /** Cells per row of the grid. */
    static final int ROW = 2048;

    public static final SavedDataType<DDData> TYPE = new SavedDataType<>(DimDoors.id("dimensional_doors"), DDData::new,
            CompoundTag.CODEC.xmap(DDData::load, DDData::save));

    /** LinkData: a rift at loc leading to dest. Coordinates are those of the levels (the pocket level for pockets). */
    public static final class LinkData {
        public int locDim;
        public int locX;
        public int locY;
        public int locZ;
        public int destDim;
        public int destX;
        public int destY;
        public int destZ;
        public boolean isLocPocket;
        public int orientation;
        public boolean hasGennedDoor;
        public int children;

        public LinkData() {
        }

        public LinkData(int locDim, int destDim, int locX, int locY, int locZ, int destX, int destY, int destZ, boolean isLocPocket, int orientation) {
            this.locDim = locDim;
            this.destDim = destDim;
            this.locX = locX;
            this.locY = locY;
            this.locZ = locZ;
            this.destX = destX;
            this.destY = destY;
            this.destZ = destZ;
            this.isLocPocket = isLocPocket;
            this.orientation = orientation;
        }

        CompoundTag save() {
            CompoundTag tag = new CompoundTag();
            tag.putIntArray("loc", new int[] {this.locDim, this.locX, this.locY, this.locZ});
            tag.putIntArray("dest", new int[] {this.destDim, this.destX, this.destY, this.destZ});
            tag.putBoolean("pocket", this.isLocPocket);
            tag.putInt("orientation", this.orientation);
            tag.putBoolean("door", this.hasGennedDoor);
            tag.putInt("children", this.children);
            return tag;
        }

        static LinkData load(CompoundTag tag) {
            LinkData link = new LinkData();
            int[] loc = tag.getIntArray("loc").orElse(new int[4]);
            int[] dest = tag.getIntArray("dest").orElse(new int[4]);
            link.locDim = loc[0];
            link.locX = loc[1];
            link.locY = loc[2];
            link.locZ = loc[3];
            link.destDim = dest[0];
            link.destX = dest[1];
            link.destY = dest[2];
            link.destZ = dest[3];
            link.isLocPocket = tag.getBooleanOr("pocket", false);
            link.orientation = tag.getIntOr("orientation", 0);
            link.hasGennedDoor = tag.getBooleanOr("door", false);
            link.children = tag.getIntOr("children", 0);
            return link;
        }
    }

    /** DimData: a dimension with links; for pockets its depth, where its chain leaves, its dungeon and its cell. */
    public static final class DimData {
        public final int id;
        public final boolean isPocket;
        public int depth;
        /** exitDimLink: only dest counts, the dimension and place the chain of pockets started from. */
        public int exitDim;
        public int exitX;
        public int exitY;
        public int exitZ;
        public boolean hasBeenFilled;
        public boolean isDimRandomRift;
        /** The schematic path of the dungeon (DungeonGenerator), or {@code null}. */
        public String dungeon;
        public int cellX;
        public int cellZ;
        /** Where the original had the pocket: level coordinates minus these. */
        public int offsetX;
        public int offsetZ;
        /** The box the dungeon was built in (minX, minY, minZ, maxX, maxY, maxZ), for dd-resetdungeons; {@code null} if none. */
        public int[] box;
        final Map<Long, LinkData> links = new LinkedHashMap<>();

        DimData(int id, boolean isPocket, int depth, int exitDim, int exitX, int exitY, int exitZ) {
            this.id = id;
            this.isPocket = isPocket;
            this.depth = depth;
            this.exitDim = exitDim;
            this.exitX = exitX;
            this.exitY = exitY;
            this.exitZ = exitZ;
        }

        public List<LinkData> links() {
            return new ArrayList<>(this.links.values());
        }

        public LinkData link(int x, int y, int z) {
            return this.links.get(key(x, y, z));
        }

        CompoundTag save() {
            CompoundTag tag = new CompoundTag();
            tag.putInt("id", this.id);
            tag.putBoolean("pocket", this.isPocket);
            tag.putInt("depth", this.depth);
            tag.putIntArray("exit", new int[] {this.exitDim, this.exitX, this.exitY, this.exitZ});
            tag.putBoolean("filled", this.hasBeenFilled);
            tag.putBoolean("random", this.isDimRandomRift);
            if (this.dungeon != null) {
                tag.putString("dungeon", this.dungeon);
            }
            tag.putIntArray("cell", new int[] {this.cellX, this.cellZ, this.offsetX, this.offsetZ});
            if (this.box != null) {
                tag.putIntArray("box", this.box);
            }
            ListTag list = new ListTag();
            for (LinkData link : this.links.values()) {
                list.add(link.save());
            }
            tag.put("links", list);
            return tag;
        }

        static DimData load(CompoundTag tag) {
            int[] exit = tag.getIntArray("exit").orElse(new int[4]);
            DimData data = new DimData(tag.getIntOr("id", 0), tag.getBooleanOr("pocket", false), tag.getIntOr("depth", 0), exit[0], exit[1],
                    exit[2], exit[3]);
            data.hasBeenFilled = tag.getBooleanOr("filled", false);
            data.isDimRandomRift = tag.getBooleanOr("random", false);
            data.dungeon = tag.getString("dungeon").orElse(null);
            int[] cell = tag.getIntArray("cell").orElse(new int[4]);
            data.cellX = cell[0];
            data.cellZ = cell[1];
            data.offsetX = cell[2];
            data.offsetZ = cell[3];
            data.box = tag.getIntArray("box").orElse(null);
            for (Tag entry : tag.getListOrEmpty("links")) {
                if (entry instanceof CompoundTag linkTag) {
                    LinkData link = LinkData.load(linkTag);
                    data.links.put(key(link.locX, link.locY, link.locZ), link);
                }
            }
            return data;
        }
    }

    /** dimList; a HashMap like the original's, so that random picks go over the same kind of order. */
    final Map<Integer, DimData> dims = new HashMap<>();
    /** Pocket cells in use: cell key to dimension id. */
    final Map<Long, Integer> cells = new HashMap<>();
    /** Levels other than the four of the pack: their name to the id they got. */
    final Map<String, Integer> otherLevels = new LinkedHashMap<>();
    /** blocksToDecay: blocks placed in Limbo that crumble. */
    final List<int[]> blocksToDecay = new ArrayList<>();
    int nextCell = 1;

    public DDData() {
    }

    static long key(int x, int y, int z) {
        return ((long) x & 0x3FFFFFF) << 38 | ((long) z & 0x3FFFFFF) << 12 | (long) y & 0xFFF;
    }

    static long cellKey(int cellX, int cellZ) {
        return (long) cellX << 32 | cellZ & 0xFFFFFFFFL;
    }

    public static DDData get(MinecraftServer server) {
        return server.getDataStorage().computeIfAbsent(TYPE);
    }

    private static DDData load(CompoundTag tag) {
        DDData data = new DDData();
        for (Tag entry : tag.getListOrEmpty("dims")) {
            if (entry instanceof CompoundTag dimTag) {
                DimData dim = DimData.load(dimTag);
                data.dims.put(dim.id, dim);
                if (dim.isPocket) {
                    data.cells.put(cellKey(dim.cellX, dim.cellZ), dim.id);
                }
            }
        }
        CompoundTag others = tag.getCompoundOrEmpty("levels");
        for (String name : others.keySet()) {
            data.otherLevels.put(name, others.getIntOr(name, 0));
        }
        for (Tag entry : tag.getListOrEmpty("decay")) {
            if (entry instanceof CompoundTag point) {
                data.blocksToDecay.add(point.getIntArray("p").orElse(new int[3]));
            }
        }
        data.nextCell = tag.getIntOr("nextCell", 1);
        return data;
    }

    private CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        ListTag list = new ListTag();
        for (DimData dim : this.dims.values()) {
            list.add(dim.save());
        }
        tag.put("dims", list);
        CompoundTag others = new CompoundTag();
        this.otherLevels.forEach(others::putInt);
        tag.put("levels", others);
        ListTag decay = new ListTag();
        for (int[] point : this.blocksToDecay) {
            CompoundTag p = new CompoundTag();
            p.putIntArray("p", point);
            decay.add(p);
        }
        tag.put("decay", decay);
        tag.putInt("nextCell", this.nextCell);
        return tag;
    }

    /** The data of a dimension that is not a pocket, made when missing (the Limbo entry of serverStarting). */
    public DimData ensure(int id) {
        DimData data = this.dims.get(id);
        if (data == null) {
            data = new DimData(id, false, 0, 0, 0, 0, 0);
            this.dims.put(id, data);
            this.setDirty();
        }
        return data;
    }

    public DimData dim(int id) {
        return this.dims.get(id);
    }

    public Map<Integer, DimData> dims() {
        return this.dims;
    }

    /** The pocket whose cell holds x/z of the pocket level, or {@code null}. */
    public DimData pocketAt(int x, int z) {
        int cellX = Math.floorDiv(x + CELL / 2, CELL);
        int cellZ = Math.floorDiv(z + CELL / 2, CELL);
        Integer id = this.cells.get(cellKey(cellX, cellZ));
        return id == null ? null : this.dims.get(id);
    }

    /** Gives a new pocket the next free cell. */
    void assignCell(DimData pocket) {
        int index = this.nextCell++;
        pocket.cellX = index % ROW;
        pocket.cellZ = index / ROW;
        this.cells.put(cellKey(pocket.cellX, pocket.cellZ), pocket.id);
    }

    void releaseCell(DimData pocket) {
        this.cells.remove(cellKey(pocket.cellX, pocket.cellZ));
    }

    /** DimensionManager.getNextFreeDimId: the lowest id no dimension uses. */
    int nextFreeId() {
        int id = 0;
        while (this.dims.containsKey(id) || id == 0 || id == 1 || id == -1 || id == DimDoors.LIMBO_ID || this.otherLevels.containsValue(id)) {
            id++;
        }
        return id;
    }

    public List<int[]> blocksToDecay() {
        return this.blocksToDecay;
    }
}
