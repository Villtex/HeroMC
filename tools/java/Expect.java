import hexxit.worldgen152.core.*;
import hexxit.worldgen152.core.populate.*;
import java.util.*;

/**
 * Predicts the populated 1.5.2 world for tools/verify_blocks.py. Per chosen chunk it prints
 *   "x y z id meta"   four full columns, and every block population changed or added
 *   "D dimension"     first line when the chunks are not in the overworld
 *   "B x z biomeKey"  biome samples
 *   "S x y z Mob"     mob spawners, "C x y z n" chests and dispensers with n filled slots
 *   "K x y z type rotation" floor skulls (type as TileEntitySkull, rotation 0 to 15)
 *   "E x y z Kind"    entities structures spawned (Villager, Witch, MinecartChest)
 * With "find <radius>" it picks one chunk per biome found on a coarse scan around 0,0, with
 * "structures <radius>" chunks of the structures starting within that many chunks of 0,0; "spawn" prints
 * the world spawn. "vanilla" as second argument leaves out the biomes of mods; "nether" (after it) predicts the
 * Nether, where "structures" picks the nearest fortresses.
 */
public class Expect {
    public static void main(String[] a) {
        long seed = Long.parseLong(a[0]);
        boolean vanilla = a.length > 1 && a[1].equals("vanilla");   // without the biomes of mods, to compare with a vanilla 1.5.2 world
        OldDungeonGen.vanillaLoot = vanilla;
        OldLoot.vanillaDraws = vanilla;
        if (vanilla) a = java.util.stream.Stream.concat(java.util.stream.Stream.of(a[0]), Arrays.stream(a).skip(2)).toArray(String[]::new);
        else {
            extrabiomes.EbxlBiomes.register();
            sct.hexxitgear.gen.HGWorldGen.register();
            natura.gen.NaturaWorldGen.register();
            offlawn.gen.OffLawnGen.register();
            wildcaves.gen.WildCavesGen.register();
        }
        // "nether" (after "vanilla", if both are given): the Nether of the seed instead of the overworld
        boolean nether = a.length > 1 && a[1].equals("nether");
        if (nether) a = java.util.stream.Stream.concat(java.util.stream.Stream.of(a[0]), Arrays.stream(a).skip(2)).toArray(String[]::new);
        BiomeLayout layout = nether ? null : new BiomeLayout(seed);
        OldTerrain terrain = nether ? null : new OldTerrain(layout);
        hexxit.worldgen152.core.nether.NetherTerrain netherTerrain = nether ? new hexxit.worldgen152.core.nether.NetherTerrain(seed) : null;
        Map<Long, OldChunk> raw = new HashMap<>();
        OldWorld.TerrainSource source = (x, z) -> {
            synchronized (raw) {
                return raw.computeIfAbsent((long) x << 32 | z & 0xFFFFFFFFL, k -> nether ? netherTerrain.generate(x, z) : terrain.generate(x, z));
            }
        };
        OldDecoration decoration = nether ? OldDecoration.nether(seed, source) : new OldDecoration(seed, source, layout);
        List<int[]> chunks = new ArrayList<>();
        if (nether && a[1].equals("structures")) {
            // two by two chunks at the first piece of the two fortresses nearest to 0,0
            List<hexxit.worldgen152.core.structure.MapGenNetherBridge.Found> found = new hexxit.worldgen152.core.structure.MapGenNetherBridge(seed).find(Integer.parseInt(a[2]));
            found.sort(Comparator.comparingInt(f -> Math.max(Math.abs(f.chunkX()), Math.abs(f.chunkZ()))));
            for (var f : found.subList(0, Math.min(2, found.size()))) {
                System.err.println(f);
                int cx = f.x() - 8 >> 4, cz = f.z() - 8 >> 4;
                for (int dx = 0; dx < 2; dx++) for (int dz = 0; dz < 2; dz++) chunks.add(new int[]{cx + dx, cz + dz});
            }
        } else if (nether && (a[1].equals("find") || a[1].equals("spawn"))) {
            throw new IllegalArgumentException("the Nether has one biome and no spawn: give chunks or \"structures <radius>\"");
        } else if (a[1].equals("find")) {
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
        } else if (a[1].equals("structures")) {
            // two by two chunks at the first piece of the structures nearest to 0,0
            Map<String, Integer> perKind = new HashMap<>();
            List<hexxit.worldgen152.core.structure.OldStructures.Found> found = new hexxit.worldgen152.core.structure.OldStructures(seed, layout).find(Integer.parseInt(a[2]));
            found.sort(Comparator.comparingInt(f -> Math.max(Math.abs(f.chunkX()), Math.abs(f.chunkZ()))));
            for (var f : found) {
                if (perKind.merge(f.kind(), 1, Integer::sum) > 2) continue;   // the two nearest of every kind
                System.err.println(f);
                int cx = f.x() - 8 >> 4, cz = f.z() - 8 >> 4;
                for (int dx = 0; dx < 2; dx++) for (int dz = 0; dz < 2; dz++) chunks.add(new int[]{cx + dx, cz + dz});
            }
        } else if (a[1].equals("spawn")) {
            int[] spawn = hexxit.worldgen152.core.structure.OldStructures.spawnPoint(seed, layout, source);
            System.out.println("spawn " + spawn[0] + " " + spawn[1]);
            return;
        } else if (!a[1].equals("raw")) {
            for (int i = 1; i + 1 < a.length; i += 2) chunks.add(new int[]{Integer.parseInt(a[i]), Integer.parseInt(a[i + 1])});
        }
        if (a[1].equals("raw")) {
            // every block of the chunks, for tools/compare_original.py: "R cx cz" and 16*16*256 cells (id << 4 | meta) as base64
            chunks.clear();
            for (int i = 2; i + 1 < a.length; i += 2) chunks.add(new int[]{Integer.parseInt(a[i]), Integer.parseInt(a[i + 1])});
            StringBuilder dump = new StringBuilder();
            for (int[] ch : chunks) {
                OldDecoratedChunk c = decoration.chunk(ch[0], ch[1]);
                byte[] cells = new byte[16 * 16 * 256 * 2];
                int n = 0;
                for (int x = 0; x < 16; x++) for (int z = 0; z < 16; z++) for (int y = 0; y < 256; y++) {
                    int cell = c.cell(x, y, z);
                    cells[n++] = (byte) (cell >> 8);
                    cells[n++] = (byte) cell;
                }
                dump.append("R ").append(ch[0]).append(' ').append(ch[1]).append(' ').append(Base64.getEncoder().encodeToString(cells)).append('\n');
                byte[] biomes = new byte[256];
                for (int x = 0; x < 16; x++) for (int z = 0; z < 16; z++) biomes[z * 16 + x] = (byte) c.biomeId(x, z);
                dump.append("G ").append(ch[0]).append(' ').append(ch[1]).append(' ').append(Base64.getEncoder().encodeToString(biomes)).append('\n');
                for (OldEntity e : c.entities()) dump.append("E ").append(e.x()).append(' ').append(e.y()).append(' ').append(e.z()).append(' ').append(e.getClass().getSimpleName()).append('\n');
                if (dump.length() > 1 << 24) { System.out.print(dump); dump.setLength(0); }
            }
            System.out.print(dump);
            return;
        }
        StringBuilder out = new StringBuilder();
        if (nether) out.append("D minecraft:the_nether\n");
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
                out.append("B ").append(cx * 16 + x).append(' ').append(cz * 16 + z).append(' ').append(nether ? "worldgen152:hell" : OldBiomes.byId(c.biomeId(x, z)).key).append('\n');
            for (var e : c.tiles().entrySet()) {
                int k = e.getKey();
                String pos = (cx * 16 + (k >> 12)) + " " + (k & 255) + " " + (cz * 16 + (k >> 8 & 15));
                if (e.getValue() instanceof OldTile.Skull sk) out.append("K ").append(pos).append(' ').append(sk.type()).append(' ').append(sk.rotation() & 15).append('\n');
                if (e.getValue() instanceof OldTile.Spawner s) out.append("S ").append(pos).append(' ').append(s.mob()).append('\n');
                if (e.getValue() instanceof OldTile.Dispenser t) out.append("C ").append(pos).append(' ').append(Arrays.stream(t.slots()).filter(Objects::nonNull).count()).append('\n');
                if (e.getValue() instanceof OldTile.Chest t) out.append("C ").append(pos).append(' ').append(Arrays.stream(t.slots()).filter(Objects::nonNull).count()).append('\n');
            }
            for (OldEntity e : c.entities())
                out.append("E ").append(e.x()).append(' ').append(e.y()).append(' ').append(e.z()).append(' ').append(e.getClass().getSimpleName()).append('\n');
        }
        System.out.print(out);
        System.err.println(chunks.size() + " chunks in " + (System.nanoTime() - start) / 1000000 + " ms; blocks changed by population, by id: " + changed);
    }
}
