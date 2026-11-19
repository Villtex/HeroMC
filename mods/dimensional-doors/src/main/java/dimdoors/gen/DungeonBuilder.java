package dimdoors.gen;

import hexxit.worldgen152.core.populate.OldStack;
import hexxit.worldgen152.core.populate.OldTile;
import hexxit.worldgen152.core.populate.OldWorld;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.zip.GZIPInputStream;

/**
 * SchematicLoader.generateSchematic of Dimensional Doors 1.4.1 without game classes: builds a dungeon schematic into an
 * {@link OldWorld}, turned so that its entrance (the wooden door) stands at the rift of the incoming link facing its
 * orientation. Iron doors become dimensional doors (links deeper into new dungeons), wooden doors warp doors; a
 * wooden door over sandstone is an exit, end portal frames are where monoliths stand. Blocks of mods become Fabric of
 * Reality. Empty chests get the loot of {@link DDLoot}, empty dispensers 64 arrows.
 *
 * <p>The game takes the result ({@link Result}) to create the links and put the blocks into the pocket.
 */
public final class DungeonBuilder {
    private static final int MAX_VANILLA_BLOCK_ID = 158;

    /** A door found in the schematic, already moved into the world: its upper half, and the facing of its lower half. */
    public record Door(int x, int y, int z, int direction, int destX, int destY, int destZ) {
    }

    /** What the game needs after the build. */
    public record Result(Random random, List<Door> sideDoors, List<int[]> exitDoors, List<int[]> monoliths, int[] entrance,
                         int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
    }

    /** A schematic as SchematicLoader read it: block ids (with AddBlocks), metadata, tile entities by position. */
    record Schematic(int width, int height, int length, short[] blocks, byte[] data, Map<Long, Map<String, Object>> tiles) {
    }

    private static final Map<String, Schematic> CACHE = new HashMap<>();

    private DungeonBuilder() {
    }

    private static long key(int x, int y, int z) {
        return ((long) x & 0xFFFFF) << 40 | ((long) y & 0xFFFFF) << 20 | (long) z & 0xFFFFF;
    }

    /** Where a built-in schematic ("/schematics/x.schematic") lies in the jar. */
    public static String resource(String path) {
        return "dimdoors-original" + path;
    }

    private static InputStream open(String path) throws IOException {
        File file = new File(path);
        if (file.isFile()) {
            return new FileInputStream(file);
        }
        InputStream in = DungeonBuilder.class.getClassLoader().getResourceAsStream(resource(path));
        if (in == null) {
            throw new IOException("Schematic not found: " + path);
        }
        return in;
    }

    @SuppressWarnings("unchecked")
    static synchronized Schematic schematic(String path) {
        Schematic cached = CACHE.get(path);
        if (cached != null) {
            return cached;
        }
        Schematic schematic;
        try (DataInputStream in = new DataInputStream(new GZIPInputStream(open(path)))) {
            String[] name = new String[1];
            int type = DDNbt.readHeader(in, name);
            Map<String, Object> tag = (Map<String, Object>) DDNbt.read(in, type);
            int width = DDNbt.getInt(tag, "Width");
            int height = DDNbt.getInt(tag, "Height");
            int length = DDNbt.getInt(tag, "Length");
            byte[] blockId = tag.get("Blocks") instanceof byte[] b ? b : new byte[0];
            byte[] data = tag.get("Data") instanceof byte[] b ? b : new byte[0];
            byte[] add = tag.get("AddBlocks") instanceof byte[] b ? b : new byte[0];
            short[] blocks = new short[blockId.length];
            for (int i = 0; i < blockId.length; i++) {
                if (i >> 1 >= add.length) {
                    blocks[i] = (short) (blockId[i] & 0xFF);
                } else if ((i & 1) == 0) {
                    blocks[i] = (short) (((add[i >> 1] & 15) << 8) + (blockId[i] & 255));
                } else {
                    blocks[i] = (short) (((add[i >> 1] & 240) << 4) + (blockId[i] & 255));
                }
            }
            Map<Long, Map<String, Object>> tiles = new HashMap<>();
            for (Object entry : DDNbt.getList(tag, "TileEntities")) {
                Map<String, Object> tile = (Map<String, Object>) entry;
                tiles.put(key(DDNbt.getInt(tile, "x"), DDNbt.getInt(tile, "y"), DDNbt.getInt(tile, "z")), tile);
            }
            schematic = new Schematic(width, height, length, blocks, data, tiles);
        } catch (IOException e) {
            // "Error- could not find file": nothing is built
            schematic = new Schematic(0, 0, 0, new short[0], new byte[0], Map.of());
        }
        CACHE.put(path, schematic);
        return schematic;
    }

    /** SchematicLoader.transformPoint: turns a point around {@code src} by quarter turns and moves it to {@code dest}. */
    public static int[] transformPoint(int x, int y, int z, int[] src, int angle, int[] dest) {
        int tx = x - src[0];
        int ty = y - src[1];
        int tz = z - src[2];
        if (angle < 0) {
            int correction = -(angle / 4);
            angle += 4 * (correction + 1);
        }
        angle %= 4;
        int rx;
        int rz;
        switch (angle) {
            case 0 -> {
                rx = tx;
                rz = tz;
            }
            case 1 -> {
                rx = -tz;
                rz = tx;
            }
            case 2 -> {
                rx = -tx;
                rz = -tz;
            }
            default -> {
                rx = tz;
                rz = -tx;
            }
        }
        return new int[] {rx + dest[0], ty + dest[1], rz + dest[2]};
    }

    /** MathHelper.getRandomIntegerInRange. */
    public static int randomInRange(Random random, int min, int max) {
        return min >= max ? min : random.nextInt(max - min + 1) + min;
    }

    /**
     * Builds the dungeon of {@code path} into {@code world} with its entrance at riftX/Y/Z (in the world) facing
     * {@code orientation}. {@code seedX}/{@code seedZ} are the rift's coordinates in its pocket dimension of the
     * original, which seeded the chest loot together with the world seed. {@code depth} is the depth of the dimension
     * the dungeon is entered from plus one (the noise of the side doors).
     */
    public static Result build(OldWorld world, String path, int riftX, int riftY, int riftZ, int orientation, int seedX, int seedZ, int depth) {
        Schematic s = schematic(path);
        Random rand = new Random(world.seed());
        long factorA = rand.nextLong() / 2L * 2L + 1L;
        long factorB = rand.nextLong() / 2L * 2L + 1L;
        rand.setSeed((seedX >> 4) * factorA + (seedZ >> 4) * factorB ^ world.seed());
        int[] pocketCenter = {riftX, riftY, riftZ};
        int[] entrance = {0, 0, 0};
        int entryDirection = 0;
        List<int[]> sidePoints = new ArrayList<>();
        List<int[]> exitPoints = new ArrayList<>();
        List<int[]> monolithPoints = new ArrayList<>();
        int w = s.width();
        int l = s.length();
        for (int y = 0; y < s.height(); y++) {
            for (int z = 0; z < l; z++) {
                for (int x = 0; x < w; x++) {
                    int index = y * w * l + z * w + x;
                    int below = (y - 1) * w * l + z * w + x;
                    int doubleBelow = (y - 2) * w * l + z * w + x;
                    int block = s.blocks()[index];
                    if (block == 71 && below >= 0 && s.blocks()[below] == 71) {
                        sidePoints.add(new int[] {x, y, z});
                    } else if (block == 64) {
                        if (doubleBelow >= 0 && s.blocks()[doubleBelow] == 24 && s.blocks()[below] == 64) {
                            exitPoints.add(new int[] {x, y, z});
                        } else if (below >= 0 && s.blocks()[below] == 64) {
                            entrance = new int[] {x, y, z};
                            entryDirection = Math.abs(s.data()[below] + 2) % 4;
                        }
                    } else if (block == 120) {
                        monolithPoints.add(new int[] {x, y, z});
                    }
                }
            }
        }
        int angle = orientation - entryDirection;
        int metaTurn = (orientation + 3 - entryDirection + 16) % 4;
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;
        int maxZ = Integer.MIN_VALUE;
        for (int y = 0; y < s.height(); y++) {
            for (int z = 0; z < l; z++) {
                for (int x = 0; x < w; x++) {
                    int[] real = transformPoint(x, y, z, entrance, angle, pocketCenter);
                    int index = y * w * l + z * w + x;
                    int block = s.blocks()[index];
                    int meta = s.data()[index];
                    boolean changed = false;
                    if (block == 120) {
                        block = 0;
                        changed = true;
                    } else if (block == 1973) {
                        block = RiftGen.FABRIC;
                    } else if (block == 220) {
                        block = RiftGen.ETERNAL_FABRIC;
                    } else if (block > MAX_VANILLA_BLOCK_ID) {
                        // every id up to 158 was a block in 1.5.2
                        block = RiftGen.FABRIC;
                        changed = true;
                    }
                    if (block <= 0) {
                        continue;
                    }
                    int fixed = changed ? 0 : Rotation.transformMetadata(meta, metaTurn, block);
                    int placed = block == 71 ? RiftGen.DIMENSIONAL_DOOR : block == 64 ? RiftGen.WARP_DOOR : block;
                    world.setBlockDirect(real[0], real[1], real[2], placed, fixed);
                    minX = Math.min(minX, real[0]);
                    minY = Math.min(minY, real[1]);
                    minZ = Math.min(minZ, real[2]);
                    maxX = Math.max(maxX, real[0]);
                    maxY = Math.max(maxY, real[1]);
                    maxZ = Math.max(maxZ, real[2]);
                    if (block == 54 || block == 146) {
                        Map<String, Object> nbt = changed ? null : s.tiles().get(key(x, y, z));
                        OldTile.Chest chest = new OldTile.Chest(slots(nbt, 27));
                        if (isEmpty(chest.slots())) {
                            DDLoot.fill(rand, chest);
                        }
                        world.setTile(real[0], real[1], real[2], chest);
                    } else if (block == 23 || block == 158) {
                        Map<String, Object> nbt = changed ? null : s.tiles().get(key(x, y, z));
                        OldTile.Dispenser dispenser = new OldTile.Dispenser(slots(nbt, 9));
                        if (isEmpty(dispenser.slots())) {
                            // TileEntityDispenser.addItem: the first empty slot
                            dispenser.slots()[0] = new OldStack(262, 0, 64);
                        }
                        world.setTile(real[0], real[1], real[2], dispenser);
                    }
                }
            }
        }
        int[] entranceRift = transformPoint(entrance[0], entrance[1], entrance[2], entrance, angle, pocketCenter);
        List<Door> sideDoors = new ArrayList<>();
        for (int[] point : sidePoints) {
            int forward = randomInRange(rand, -50 * depth, 150 * depth);
            int sideways = randomInRange(rand, -10 * depth, 10 * depth);
            int[] door = transformPoint(point[0], point[1], point[2], entrance, angle, pocketCenter);
            int direction = world.getBlockMetadata(door[0], door[1] - 1, door[2]);
            int[] destination = transformPoint(forward, 0, sideways, new int[] {0, 0, 0}, direction, door);
            sideDoors.add(new Door(door[0], door[1], door[2], direction, destination[0], destination[1], destination[2]));
        }
        List<int[]> exitDoors = new ArrayList<>();
        for (int[] point : exitPoints) {
            exitDoors.add(transformPoint(point[0], point[1], point[2], entrance, angle, pocketCenter));
        }
        List<int[]> monoliths = new ArrayList<>();
        for (int[] point : monolithPoints) {
            monoliths.add(transformPoint(point[0], point[1], point[2], entrance, angle, pocketCenter));
        }
        return new Result(rand, sideDoors, exitDoors, monoliths, entranceRift, minX, minY, minZ, maxX, maxY, maxZ);
    }

    private static boolean isEmpty(OldStack[] slots) {
        for (OldStack stack : slots) {
            if (stack != null) {
                return false;
            }
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    private static OldStack[] slots(Map<String, Object> nbt, int size) {
        OldStack[] slots = new OldStack[size];
        if (nbt == null) {
            return slots;
        }
        for (Object entry : DDNbt.getList(nbt, "Items")) {
            Map<String, Object> item = (Map<String, Object>) entry;
            int slot = DDNbt.getInt(item, "Slot") & 255;
            if (slot < size && DDNbt.getInt(item, "id") > 0 && DDNbt.getInt(item, "Count") > 0) {
                slots[slot] = new OldStack(DDNbt.getInt(item, "id"), DDNbt.getInt(item, "Damage"), DDNbt.getInt(item, "Count"));
            }
        }
        return slots;
    }
}
