package twilightforest.gen.structure;

/** ChunkCoordinates of 1.5.2: a block position. */
public class ChunkCoordinates {
    public int posX;
    public int posY;
    public int posZ;

    public ChunkCoordinates(int x, int y, int z) {
        this.posX = x;
        this.posY = y;
        this.posZ = z;
    }

    public ChunkCoordinates(ChunkCoordinates other) {
        this(other.posX, other.posY, other.posZ);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof ChunkCoordinates c && c.posX == this.posX && c.posY == this.posY && c.posZ == this.posZ;
    }

    @Override
    public int hashCode() {
        return this.posX + this.posZ << 8 + this.posY << 16;
    }
}
