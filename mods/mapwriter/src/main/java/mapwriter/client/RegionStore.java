package mapwriter.client;

import com.mojang.blaze3d.platform.NativeImage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

/**
 * RegionManager: the regions of one dimension, loaded from and saved to {@code r.<x>.<z>.png} in the dimension folder
 * of the world's map directory. At most {@link #MAX_OPEN} regions stay in memory; the dirty ones are written on a
 * background thread when they are pushed out, every half minute and when the world is left.
 */
final class RegionStore {
    private static final int MAX_OPEN = 48;

    private final Path dir;
    private final ExecutorService io;
    /** Counts every change, so the painters know the map moved on. */
    long version;
    private final LinkedHashMap<Long, Region> open = new LinkedHashMap<>(64, 0.75F, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<Long, Region> eldest) {
            if (size() > MAX_OPEN) {
                save(eldest.getValue());
                return true;
            }
            return false;
        }
    };

    RegionStore(Path dir, ExecutorService io) {
        this.dir = dir;
        this.io = io;
    }

    private static long key(int rx, int rz) {
        return (long) rx << 32 | rz & 0xFFFFFFFFL;
    }

    private Path file(int rx, int rz) {
        return dir.resolve("r." + rx + "." + rz + ".png");
    }

    /** The region at region coordinates; {@code create} makes an empty one when there is none yet. */
    Region get(int rx, int rz, boolean create) {
        long k = key(rx, rz);
        Region r = open.get(k);
        if (r != null) {
            return r;
        }
        Path f = file(rx, rz);
        if (Files.isRegularFile(f)) {
            r = new Region(rx, rz);
            try (var in = Files.newInputStream(f); NativeImage img = NativeImage.read(in)) {
                for (int z = 0; z < Math.min(img.getHeight(), Region.SIZE); z++) {
                    for (int x = 0; x < Math.min(img.getWidth(), Region.SIZE); x++) {
                        r.argb[z * Region.SIZE + x] = img.getPixel(x, z);
                    }
                }
            } catch (IOException | RuntimeException e) {
                // an unreadable image is a region without data
            }
        } else if (create) {
            r = new Region(rx, rz);
        } else {
            return null;
        }
        open.put(k, r);
        return r;
    }

    /** The colour at block coordinates, 0 when not seen. */
    int pixel(int bx, int bz) {
        Region r = get(bx >> 9, bz >> 9, false);
        return r == null ? 0 : r.argb[(bz & 511) * Region.SIZE + (bx & 511)];
    }

    short heightAt(int bx, int bz) {
        Region r = get(bx >> 9, bz >> 9, false);
        return r == null ? Region.NO_HEIGHT : r.height[(bz & 511) * Region.SIZE + (bx & 511)];
    }

    void markChanged(Region r) {
        r.dirty = true;
        version++;
    }

    void clearArea(int minX, int minZ, int maxX, int maxZ) {
        for (int rx = minX >> 9; rx <= maxX >> 9; rx++) {
            for (int rz = minZ >> 9; rz <= maxZ >> 9; rz++) {
                Region r = get(rx, rz, false);
                if (r != null) {
                    r.clearArea(minX, minZ, maxX, maxZ);
                }
            }
        }
        version++;
    }

    private void save(Region r) {
        if (!r.dirty) {
            return;
        }
        r.dirty = false;
        int[] copy = r.argb.clone();
        Path f = file(r.rx, r.rz);
        io.execute(() -> {
            try (NativeImage img = new NativeImage(Region.SIZE, Region.SIZE, false)) {
                for (int z = 0; z < Region.SIZE; z++) {
                    for (int x = 0; x < Region.SIZE; x++) {
                        img.setPixel(x, z, copy[z * Region.SIZE + x]);
                    }
                }
                Files.createDirectories(dir);
                img.writeToFile(f);
            } catch (IOException | RuntimeException e) {
                // a map that cannot be saved is lost on the next start, nothing else breaks
            }
        });
    }

    void saveAll() {
        for (Region r : new ArrayList<>(open.values())) {
            save(r);
        }
    }

    /** Regions of this dimension that exist on disk or in memory (for the merged image). */
    List<Region> all() {
        return new ArrayList<>(open.values());
    }
}
