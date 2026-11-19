package betterstorage.crate;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.nio.ByteBuffer;
import net.minecraft.core.BlockPos;

/**
 * CratePileMap: which positions of a pile's bounding {@link #region} hold one of its crates, one bit per block
 * (eight y positions per byte), in a map region with some room around. Ported as it was; the y divisions by 8 are
 * floor divisions here, since worlds go below y 0 now (1.5.2 never had negative y).
 */
public final class CratePileMap {
    private static final Codec<byte[]> BYTES = Codec.BYTE_BUFFER.xmap(b -> {
        byte[] a = new byte[b.remaining()];
        b.duplicate().get(a);
        return a;
    }, ByteBuffer::wrap);
    public static final Codec<CratePileMap> CODEC = RecordCodecBuilder.create(i -> i.group(
            Region.CODEC.fieldOf("region").forGetter(m -> m.region),
            BYTES.fieldOf("map").forGetter(m -> m.map),
            Region.CODEC.fieldOf("mapRegion").forGetter(m -> m.mapRegion)).apply(i, CratePileMap::new));

    public Region region;
    private byte[] map;
    private Region mapRegion;

    private CratePileMap(Region region, byte[] map, Region mapRegion) {
        this.region = region;
        this.map = map;
        this.mapRegion = mapRegion;
    }

    public CratePileMap(BlockPos pos) {
        this.region = new Region(pos);
        this.map = new byte[1];
        this.mapRegion = this.region.copy();
        this.mapRegion.minY = Math.floorDiv(this.mapRegion.minY, 8);
        this.mapRegion.maxY = Math.floorDiv(this.mapRegion.maxY, 8);
        this.resize();
        this.add(pos);
    }

    private static int getIndex(int width, int depth, int x, int y, int z) {
        return y + x * depth + z * depth * width;
    }

    private int getIndex(int x, int y, int z) {
        return getIndex(this.mapRegion.width(), this.mapRegion.depth(), x, y, z);
    }

    private boolean internalGet(int x, int y, int z) {
        byte b = this.map[this.getIndex(x, Math.floorDiv(y, 8), z)];
        return (b >> Math.floorMod(y, 8) & 1) > 0;
    }

    private void internalSet(int x, int y, int z, boolean value) {
        int index = this.getIndex(x, Math.floorDiv(y, 8), z);
        if (value) {
            this.map[index] = (byte) (this.map[index] | 1 << Math.floorMod(y, 8));
        } else {
            this.map[index] = (byte) (this.map[index] & ~(1 << Math.floorMod(y, 8)));
        }
    }

    private void resize(Region region) {
        byte[] newMap = new byte[region.width() * region.depth() * region.height()];
        int minX = Math.max(this.mapRegion.minX, region.minX);
        int maxX = Math.min(this.mapRegion.maxX, region.maxX);
        int minY = Math.max(this.mapRegion.minY, region.minY);
        int maxY = Math.min(this.mapRegion.maxY, region.maxY);
        int minZ = Math.max(this.mapRegion.minZ, region.minZ);
        int maxZ = Math.min(this.mapRegion.maxZ, region.maxZ);
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                int srcPos = this.getIndex(x - this.mapRegion.minX, minY - this.mapRegion.minY, z - this.mapRegion.minZ);
                int destPos = getIndex(region.width(), region.depth(), x - region.minX, minY - region.minY, z - region.minZ);
                int length = maxY - minY + 1;
                if (length > 0) {
                    System.arraycopy(this.map, srcPos, newMap, destPos, length);
                }
            }
        }
        this.map = newMap;
        this.mapRegion = region;
    }

    private void resize() {
        Region newRegion = this.region.copy();
        newRegion.expand(2, 0, 2, 2, 2, 2);
        newRegion.minY = Math.floorDiv(newRegion.minY, 8);
        newRegion.maxY = Math.floorDiv(newRegion.maxY, 8);
        this.resize(newRegion);
    }

    private boolean checkCuboid(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    if (this.get(x, y, z)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public boolean get(int x, int y, int z) {
        return this.region.contains(x, y, z) && this.internalGet(x - this.mapRegion.minX, y - this.mapRegion.minY * 8, z - this.mapRegion.minZ);
    }

    public void set(int x, int y, int z, boolean value) {
        if (this.region.contains(x, y, z)) {
            this.internalSet(x - this.mapRegion.minX, y - this.mapRegion.minY * 8, z - this.mapRegion.minZ, value);
            if (!value) {
                int minX = this.region.minX;
                int minY = this.region.minY;
                int minZ = this.region.minZ;
                int maxX = this.region.maxX;
                int maxY = this.region.maxY;
                int maxZ = this.region.maxZ;
                if (x == minX && this.checkCuboid(minX, minY, minZ, minX, maxY, maxZ)) {
                    this.region.minX++;
                }
                if (y == minY && this.checkCuboid(minX, minY, minZ, maxX, minY, maxZ)) {
                    this.region.minY++;
                }
                if (z == minZ && this.checkCuboid(minX, minY, minZ, maxX, maxY, minZ)) {
                    this.region.minZ++;
                }
                if (x == maxX && this.checkCuboid(maxX, minY, minZ, maxX, maxY, maxZ)) {
                    this.region.maxX--;
                }
                if (y == maxY && this.checkCuboid(minX, maxY, minZ, maxX, maxY, maxZ)) {
                    this.region.maxY--;
                }
                if (z == maxZ && this.checkCuboid(minX, minY, maxZ, maxX, maxY, maxZ)) {
                    this.region.maxZ--;
                }
                if (this.region.minX - this.mapRegion.minX > 4 || this.mapRegion.maxX - this.region.maxX > 4
                        || Math.floorDiv(this.region.minY, 8) - this.mapRegion.minY > 2 || this.mapRegion.maxY - Math.floorDiv(this.region.maxY, 8) > 2
                        || this.region.minZ - this.mapRegion.minZ > 4 || this.mapRegion.maxZ - this.region.maxZ > 4) {
                    this.resize();
                }
            }
        } else if (value) {
            this.region.expandToContain(x, y, z);
            if (!this.mapRegion.contains(x, Math.floorDiv(y, 8), z)) {
                this.resize();
            }
            this.internalSet(x - this.mapRegion.minX, y - this.mapRegion.minY * 8, z - this.mapRegion.minZ, value);
        }
    }

    public void add(BlockPos pos) {
        this.set(pos.getX(), pos.getY(), pos.getZ(), true);
    }

    public void remove(BlockPos pos) {
        this.set(pos.getX(), pos.getY(), pos.getZ(), false);
    }

    /** Shrinks the region to the crates in it. */
    public void trim() {
        int minX = this.region.minX;
        int minY = this.region.minY;
        int minZ = this.region.minZ;
        int maxX = this.region.maxX;
        int maxY = this.region.maxY;
        int maxZ = this.region.maxZ;
        while (minX <= maxX && this.checkCuboid(minX, minY, minZ, minX, maxY, maxZ)) {
            minX++;
        }
        while (minY <= maxY && this.checkCuboid(minX, minY, minZ, maxX, minY, maxZ)) {
            minY++;
        }
        while (minZ <= maxZ && this.checkCuboid(minX, minY, minZ, maxX, maxY, minZ)) {
            minZ++;
        }
        while (maxX >= minX && this.checkCuboid(maxX, minY, minZ, maxX, maxY, maxZ)) {
            maxX--;
        }
        while (maxY >= minY && this.checkCuboid(minX, maxY, minZ, maxX, maxY, maxZ)) {
            maxY--;
        }
        while (maxZ >= minZ && this.checkCuboid(minX, minY, maxZ, maxX, maxY, maxZ)) {
            maxZ--;
        }
        this.region.set(minX, minY, minZ, maxX, maxY, maxZ);
    }
}
