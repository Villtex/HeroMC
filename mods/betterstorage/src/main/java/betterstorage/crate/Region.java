package betterstorage.crate;

import com.mojang.serialization.Codec;
import java.util.List;
import net.minecraft.core.BlockPos;

/**
 * misc.Region: a box of block positions, bounds inclusive. Mind the names of the original: width is x, depth is y,
 * height is z (CratePileMap and CratePileData rely on them).
 */
public final class Region {
    public static final Codec<Region> CODEC = Codec.INT.listOf(6, 6).xmap(
            l -> new Region(l.get(0), l.get(1), l.get(2), l.get(3), l.get(4), l.get(5)),
            r -> List.of(r.minX, r.minY, r.minZ, r.maxX, r.maxY, r.maxZ));
    public int minX;
    public int minY;
    public int minZ;
    public int maxX;
    public int maxY;
    public int maxZ;

    public Region(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        this.set(minX, minY, minZ, maxX, maxY, maxZ);
    }

    public Region(BlockPos pos) {
        this(pos.getX(), pos.getY(), pos.getZ(), pos.getX(), pos.getY(), pos.getZ());
    }

    public int width() {
        return this.maxX - this.minX + 1;
    }

    public int depth() {
        return this.maxY - this.minY + 1;
    }

    public int height() {
        return this.maxZ - this.minZ + 1;
    }

    public int volume() {
        return this.width() * this.depth() * this.height();
    }

    public void set(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        this.minX = minX;
        this.minY = minY;
        this.minZ = minZ;
        this.maxX = maxX;
        this.maxY = maxY;
        this.maxZ = maxZ;
    }

    public void expand(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        this.minX -= minX;
        this.minY -= minY;
        this.minZ -= minZ;
        this.maxX += maxX;
        this.maxY += maxY;
        this.maxZ += maxZ;
    }

    public boolean contains(int x, int y, int z) {
        return x >= this.minX && y >= this.minY && z >= this.minZ && x <= this.maxX && y <= this.maxY && z <= this.maxZ;
    }

    public boolean contains(BlockPos pos) {
        return this.contains(pos.getX(), pos.getY(), pos.getZ());
    }

    public void expandToContain(int x, int y, int z) {
        this.minX = Math.min(this.minX, x);
        this.minY = Math.min(this.minY, y);
        this.minZ = Math.min(this.minZ, z);
        this.maxX = Math.max(this.maxX, x);
        this.maxY = Math.max(this.maxY, y);
        this.maxZ = Math.max(this.maxZ, z);
    }

    public Region copy() {
        return new Region(this.minX, this.minY, this.minZ, this.maxX, this.maxY, this.maxZ);
    }

    @Override
    public String toString() {
        return "[ " + this.minX + "," + this.minY + "," + this.minZ + " : " + this.maxX + "," + this.maxY + "," + this.maxZ + " ]";
    }
}
