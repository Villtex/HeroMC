package mapwriter.client;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;

/**
 * ChunkToPixels / MwChunk: the colour of every column of a chunk. From the top of the column the colours of the blocks
 * down to the first opaque one are blended (translucent water over the sea floor); the height differences to the
 * west and north neighbour brighten or darken the pixel. In dimensions with a ceiling (the Nether) the column starts
 * below the first free block, as the original did for dimension -1.
 */
final class ChunkScanner {
    private static final double BRIGHTEN_EXPONENT = 0.35, DARKEN_EXPONENT = 0.35, BRIGHTEN_AMPLITUDE = 0.7, DARKEN_AMPLITUDE = 1.4;

    private ChunkScanner() {
    }

    static void scan(Level level, LevelChunk chunk, RegionStore store) {
        int cx = chunk.getPos().x();
        int cz = chunk.getPos().z();
        boolean cave = level.dimensionType().hasCeiling();
        int minY = level.getMinY();
        int top = cave ? minY + level.dimensionType().logicalHeight() - 1 : level.getMaxY();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        Region region = null;
        for (int dz = 0; dz < 16; dz++) {
            for (int dx = 0; dx < 16; dx++) {
                int bx = cx * 16 + dx;
                int bz = cz * 16 + dz;
                if (region == null || region.rx != bx >> 9 || region.rz != bz >> 9) {
                    region = store.get(bx >> 9, bz >> 9, true);
                }
                int yEnd;
                if (cave) {
                    yEnd = firstNonOpaque(chunk, dx, top, dz, minY, pos);
                } else {
                    yEnd = Math.min(top, chunk.getHeight(Heightmap.Types.WORLD_SURFACE, dx, dz) - 1);
                }
                int yStart = firstOpaque(chunk, dx, yEnd, dz, minY, pos);
                double r = 0, g = 0, b = 0;
                for (int y = yStart; y <= yEnd; y++) {
                    pos.set(bx, y, bz);
                    BlockState state = chunk.getBlockState(pos);
                    BlockColours.Entry e = BlockColours.get(state);
                    double a = (e.argb() >>> 24) / 255.0;
                    if (a == 0.0) {
                        continue;
                    }
                    int tint = BlockColours.tint(e, state, level, pos);
                    r = r * (1.0 - a) + (e.argb() >> 16 & 0xFF) / 255.0 * (tint >> 16 & 0xFF) / 255.0 * a;
                    g = g * (1.0 - a) + (e.argb() >> 8 & 0xFF) / 255.0 * (tint >> 8 & 0xFF) / 255.0 * a;
                    b = b * (1.0 - a) + (e.argb() & 0xFF) / 255.0 * (tint & 0xFF) / 255.0 * a;
                }
                int i = (bz & 511) * Region.SIZE + (bx & 511);
                region.height[i] = (short) yStart;
                double shading = 1.0 + heightShading(store, region, bx, bz, yStart);
                int rr = (int) (Math.min(Math.max(0.0, r * shading), 1.0) * 255.0);
                int gg = (int) (Math.min(Math.max(0.0, g * shading), 1.0) * 255.0);
                int bb = (int) (Math.min(Math.max(0.0, b * shading), 1.0) * 255.0);
                region.argb[i] = 0xFF000000 | rr << 16 | gg << 8 | bb;
            }
        }
        if (region != null) {
            for (int rx = (cx * 16) >> 9; rx <= (cx * 16 + 15) >> 9; rx++) {
                for (int rz = (cz * 16) >> 9; rz <= (cz * 16 + 15) >> 9; rz++) {
                    store.markChanged(store.get(rx, rz, true));
                }
            }
        }
    }

    private static int firstOpaque(LevelChunk chunk, int dx, int y, int dz, int minY, BlockPos.MutableBlockPos pos) {
        while (y >= minY) {
            pos.set(chunk.getPos().x() * 16 + dx, y, chunk.getPos().z() * 16 + dz);
            if ((BlockColours.get(chunk.getBlockState(pos)).argb() >>> 24) == 255) {
                return y;
            }
            y--;
        }
        return minY;
    }

    private static int firstNonOpaque(LevelChunk chunk, int dx, int y, int dz, int minY, BlockPos.MutableBlockPos pos) {
        while (y >= minY) {
            pos.set(chunk.getPos().x() * 16 + dx, y, chunk.getPos().z() * 16 + dz);
            if ((BlockColours.get(chunk.getBlockState(pos)).argb() >>> 24) != 255) {
                return y;
            }
            y--;
        }
        return minY;
    }

    private static double heightShading(RegionStore store, Region region, int bx, int bz, int height) {
        int samples = 0;
        int diff = 0;
        short w = store.heightAt(bx - 1, bz);
        if (w != Region.NO_HEIGHT) {
            diff += height - w;
            samples++;
        }
        short n = store.heightAt(bx, bz - 1);
        if (n != Region.NO_HEIGHT) {
            diff += height - n;
            samples++;
        }
        if (samples == 0) {
            return 0.0;
        }
        double h = diff / (samples * 255.0);
        return h >= 0.0 ? Math.pow(h, BRIGHTEN_EXPONENT) * BRIGHTEN_AMPLITUDE : -Math.pow(-h, DARKEN_EXPONENT) * DARKEN_AMPLITUDE;
    }
}
