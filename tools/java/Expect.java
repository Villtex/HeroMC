import hexxit.worldgen152.core.*;
import hexxit.worldgen152.core.populate.*;
import java.util.*;

/**
 * Predicts the populated 1.5.2 world for tools/verify_blocks.py. Per chosen chunk it prints
 *   "x y z id meta"   four full columns, and every block population changed or added
 *   "B x z biomeKey"  biome samples
 *   "S x y z Mob"     mob spawners, "C x y z n" chests with n filled slots
 * With "find <radius>" it picks one chunk per biome found on a coarse scan around 0,0.
 */
public class Expect {
    public static void main(String[] a) {
        long seed = Long.parseLong(a[0]);
        extrabiomes.EbxlBiomes.register();
        BiomeLayout layout = new BiomeLayout(seed);
        OldTerrain terrain = new OldTerrain(layout);
        Map<Long, OldChunk> raw = new HashMap<>();
        OldWorld.TerrainSource source = (x, z) -> {
            synchronized (raw) {
                return raw.computeIfAbsent((long) x << 32 | z & 0xFFFFFFFFL, k -> terrain.generate(x, z));
            }
        };
        OldDecoration decoration = new OldDecoration(seed, source);
        List<int[]> chunks = new ArrayList<>();
        if (a[1].equals("find")) {
            Map<Integer, int[]> first = new TreeMap<>();
            int r = Integer.parseInt(a[2]);
            for (int d = 0; d <= r; d++) for (int cx = -d; cx <= d; cx++) for (int cz = -d; cz <= d; cz++) {
                if (Math.max(Math.abs(cx), Math.abs(cz)) != d || (cx & 3) != 0 || (cz & 3) != 0) continue;
                int id = layout.chunkIds(cx, cz)[8 + 8 * 16] & 0xFF;
                first.putIfAbsent(id, new int[]{cx, cz});
            }
            StringBuilder sb = new StringBuilder();
            for (var e : first.entrySet()) { chunks.add(e.getValue()); sb.append(OldBiomes.byId(e.getKey()).name).append("@").append(e.getValue()[0]).append(",").append(e.getValue()[1]).append("  "); }
            System.err.println(first.size() + " biomes found: " + sb);
        } else {
            for (int i = 1; i + 1 < a.length; i += 2) chunks.add(new int[]{Integer.parseInt(a[i]), Integer.parseInt(a[i + 1])});
        }
        StringBuilder out = new StringBuilder();
        Map<Integer, Integer> changed = new TreeMap<>();
        long start = System.nanoTime();
        for (int[] ch : chunks) {
            int cx = ch[0], cz = ch[1];
            OldChunk bare = source.chunk(cx, cz);
            OldDecoratedChunk c = decoration.chunk(cx, cz);
            int top = Math.max(c.height(), 128);
            for (int x = 0; x < 16; x++) for (int z = 0; z < 16; z++) {
                boolean column = (x == 1 || x == 8 || x == 15) && (z == 2 || z == 9);
                for (int y = 0; y < top; y++) {
                    int cell = c.cell(x, y, z);
                    boolean differs = cell != (y < 128 ? bare.blockId(x, y, z) << 4 : 0);
                    if (differs) changed.merge(cell >> 4, 1, Integer::sum);
                    if (column || differs) out.append(cx * 16 + x).append(' ').append(y).append(' ').append(cz * 16 + z).append(' ').append(cell >> 4).append(' ').append(cell & 15).append('\n');
                }
            }
            for (int x = 2; x < 16; x += 4) for (int z = 2; z < 16; z += 4)
                out.append("B ").append(cx * 16 + x).append(' ').append(cz * 16 + z).append(' ').append(OldBiomes.byId(c.biomeId(x, z)).key).append('\n');
            for (var e : c.tiles().entrySet()) {
                int k = e.getKey();
                String pos = (cx * 16 + (k >> 12)) + " " + (k & 255) + " " + (cz * 16 + (k >> 8 & 15));
                if (e.getValue() instanceof OldTile.Spawner s) out.append("S ").append(pos).append(' ').append(s.mob()).append('\n');
                if (e.getValue() instanceof OldTile.Chest t) out.append("C ").append(pos).append(' ').append(Arrays.stream(t.slots()).filter(Objects::nonNull).count()).append('\n');
            }
        }
        System.out.print(out);
        System.err.println(chunks.size() + " chunks in " + (System.nanoTime() - start) / 1000000 + " ms; blocks changed by population, by id: " + changed);
    }
}
