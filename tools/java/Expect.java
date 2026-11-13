import hexxit.worldgen152.core.*;
import java.util.*;

/** Prints "x y z blockId" for sample columns and "B x z biomeKey" for their biomes. With "find", picks chunks itself. */
public class Expect {
    public static void main(String[] a) {
        long seed = Long.parseLong(a[0]);
        extrabiomes.EbxlBiomes.register();
        BiomeLayout layout = new BiomeLayout(seed);
        OldTerrain terrain = new OldTerrain(layout);
        List<int[]> chunks = new ArrayList<>();
        if (a[1].equals("find")) {
            // one chunk per biome id found on a coarse scan, nearest to the origin first
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
        for (int[] ch : chunks) {
            int cx = ch[0], cz = ch[1];
            OldChunk c = terrain.generate(cx, cz);
            for (int x = 1; x < 16; x += 7) for (int z = 2; z < 16; z += 7) {
                for (int y = 0; y < 128; y++) System.out.println((cx * 16 + x) + " " + y + " " + (cz * 16 + z) + " " + c.blockId(x, y, z));
            }
            for (int x = 2; x < 16; x += 4) for (int z = 2; z < 16; z += 4)
                System.out.println("B " + (cx * 16 + x) + " " + (cz * 16 + z) + " " + OldBiomes.byId(c.biomeId(x, z)).key);
        }
    }
}
