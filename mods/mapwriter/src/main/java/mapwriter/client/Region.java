package mapwriter.client;

import java.util.Arrays;

/** A 512 x 512 block area of the map of one dimension: a colour per block (alpha 0 = not seen) and its height. */
final class Region {
    static final int SIZE = 512;
    static final short NO_HEIGHT = Short.MIN_VALUE;

    final int rx, rz;
    final int[] argb = new int[SIZE * SIZE];
    final short[] height = new short[SIZE * SIZE];
    boolean dirty;

    Region(int rx, int rz) {
        this.rx = rx;
        this.rz = rz;
        Arrays.fill(height, NO_HEIGHT);
    }

    void clearArea(int minX, int minZ, int maxX, int maxZ) {
        for (int z = Math.max(minZ, rz * SIZE); z <= Math.min(maxZ, rz * SIZE + SIZE - 1); z++) {
            for (int x = Math.max(minX, rx * SIZE); x <= Math.min(maxX, rx * SIZE + SIZE - 1); x++) {
                int i = (z & (SIZE - 1)) * SIZE + (x & (SIZE - 1));
                argb[i] = 0;
                height[i] = NO_HEIGHT;
            }
        }
        dirty = true;
    }
}
