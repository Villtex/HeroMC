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
 * "dungeons <radius>" chunks of the nearest Better Dungeons dungeon of each type, chunk cave and wall castle, with
 * "structures <radius>" chunks of the structures starting within that many chunks of 0,0; "spawn" prints
 * the world spawn. "vanilla" as second argument leaves out the biomes of mods; "nether" (after it) predicts the
 * Nether, where "structures" picks the nearest fortresses.
 */
public class Expect {
    /** The Better Dungeons mobs whose entity type exists (tools/verify_blocks.py ENTITIES has their ids). */
    static final java.util.Set<String> PORTED_BD_MOBS = java.util.Set.of("IceBull", "SlimeBoss", "TurtleBoss", "SpiderBoss", "Monking",
            // strand b1: the humans, their bosses, the mages and the mercenary
            "ArmoredSkeleton", "ArmoredZombie", "Dwarf", "Pirate", "ArmoredPigZombie", "Walker", "Specter", "Goblin", "SnowGoblin", "Triton",
            "Monkey", "Gremlin", "GolemSmall", "OgreArmored", "MinotaurArmored", "OrcArmored", "ArmoredEnderman", "PirateBoss", "WalkerBoss",
            "Necromancer", "Liche", "PigMage", "SpecterBoss", "GremlinBoss", "MinotaurBoss", "goblinBoss", "FriendlyHuman");

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
            cofh.gen.CoFHWorldGen.register();
            artifice.gen.ArtificeWorldGen.register();
            harkenscythe.gen.HSWorldGen.register();
            projectzulu.gen.ZuluWorldGen.register();
            betterdungeons.gen.BDWorldGen.register();
            ruins.gen.RuinsGen.register();
            battletowers.gen.BTWorldGen.register();
            dimdoors.gen.RiftGen.register();
            qcraft.gen.QCraftWorldGen.register();
            chococraft.gen.ChocoWorldGen.register();
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
        } else if (a[1].equals("dungeons")) {
            // Better Dungeons: per dungeon type the nearest dungeon within <radius> chunks, with the chunks that show it
            // (dungeons start at the corner of their chunk; the ice cave reaches 6 chunks towards +x/+z, the volcano 4
            // back and 5 forward, its dungeon east of it), the nearest jungle and swamp chunk cave, and the nearest
            // piece of the great wall at chunk z = -500 with a castle, snow castle or ship
            int r = Integer.parseInt(a[2]);
            Map<String, int[]> nearest = new TreeMap<>();
            for (int d = 0; d <= r; d += 5) for (int cx = -d; cx <= d; cx += 5) for (int cz = -d; cz <= d; cz += 5) {
                if (Math.max(Math.abs(cx), Math.abs(cz)) != d) continue;
                String biome = OldBiomes.byId(layout.chunkIds(cx, cz)[0] & 0xFF).name;
                String kind = betterdungeons.gen.BDWorldGen.dungeonAt(seed, biome, cx, cz);
                if (kind != null) nearest.putIfAbsent(kind, new int[]{cx, cz});
            }
            String[] caves = {"jungle chunk cave", "swamp chunk cave"};
            search:
            for (int d = 0; d <= r; d++) for (int cx = -d; cx <= d; cx++) for (int cz = -d; cz <= d; cz++) {
                if (Math.max(Math.abs(cx), Math.abs(cz)) != d) continue;
                int cave = betterdungeons.gen.BDWorldGen.chunkCaveAt(OldBiomes.byId(layout.chunkIds(cx, cz)[0] & 0xFF));
                // a cave only where the chunk and its neighbours are all of the biome: look at the middle of a patch
                if (cave > 0 && betterdungeons.gen.BDWorldGen.chunkCaveAt(OldBiomes.byId(layout.chunkIds(cx - 1, cz - 1)[0] & 0xFF)) == cave
                        && betterdungeons.gen.BDWorldGen.chunkCaveAt(OldBiomes.byId(layout.chunkIds(cx + 1, cz + 1)[0] & 0xFF)) == cave)
                    nearest.putIfAbsent(caves[cave - 1], new int[]{cx, cz});
                if (nearest.containsKey(caves[0]) && nearest.containsKey(caves[1])) break search;
            }
            for (int d = 0; d <= 3000; d += 10) for (int cx : new int[]{-d, d}) {   // (the wall line is mostly ocean)
                String wall = betterdungeons.gen.BDWorldGen.wallCastleAt(layout.biomeAt(cx * 16, -500 * 16 - 70), cx);
                if (wall != null) nearest.putIfAbsent("wall with " + wall, new int[]{cx, -500});
            }
            for (var e : nearest.entrySet()) {
                int cx = e.getValue()[0], cz = e.getValue()[1];
                String kind = e.getKey();
                System.err.println(kind + " at chunk " + cx + ", " + cz);
                int[][] offsets;
                if (kind.contains("template")) offsets = new int[][]{{1, 1}, {1, 2}, {2, 1}, {2, 2}};
                else if (kind.contains("iceCave")) offsets = new int[][]{{2, 2}, {2, 3}, {3, 2}, {3, 3}, {6, 6}};
                else if (kind.contains("volcano")) offsets = new int[][]{{-1, -1}, {-1, 0}, {0, -1}, {0, 0}, {-4, -4}, {3, 3}, {4, -1}, {5, -2}};
                else if (kind.contains("linearDungeon")) offsets = new int[][]{{-1, -1}, {-1, 0}, {0, -1}, {0, 0}, {1, 1}};
                else if (kind.contains("chunk cave")) offsets = new int[][]{{0, 0}, {1, 0}};
                else if (kind.startsWith("wall")) offsets = new int[][]{{0, 0}, {1, 0}, {1, -4}, {2, -4}, {1, -3}, {2, -3}, {0, -5}};
                else offsets = new int[][]{{0, 0}, {1, 0}, {0, 1}, {1, 1}};
                for (int[] o : offsets) chunks.add(new int[]{cx + o[0], cz + o[1]});
            }
        } else if (a[1].equals("ruins")) {
            // Ruins: populate the chunks around 0,0 (or the chunk given after the count) ring by ring and take the chunks of the first <count> ruins built
            // (their centre chunk and the three towards +x/+z), and every chunk a site's later ruins stand in
            int count = Integer.parseInt(a[2]);
            List<String> found = Collections.synchronizedList(new ArrayList<>());
            ruins.gen.RuinsGen.built = found::add;
            System.err.println(ruins.gen.RuinsGen.templateCount() + " Ruins templates");
            Set<Long> picked = new LinkedHashSet<>();
            int ox = a.length > 4 ? Integer.parseInt(a[3]) : 0, oz = a.length > 4 ? Integer.parseInt(a[4]) : 0;   // centre chunk
            for (int d = 0; d <= 200 && found.size() < count; d += 8) for (int tx = -d; tx <= d; tx += 8) for (int tz = -d; tz <= d; tz += 8) {
                if (Math.max(Math.abs(tx), Math.abs(tz)) != d) continue;
                decoration.chunk(ox + tx, oz + tz);
            }
            ruins.gen.RuinsGen.built = null;
            Set<String> seen = new HashSet<>();
            for (String r : found) {
                if (!seen.add(r) || seen.size() > count) continue;
                String[] p = r.split(" ", 5);
                int x = Integer.parseInt(p[0]), z = Integer.parseInt(p[2]);
                System.err.println("ruin " + r);
                for (int dx = 0; dx < 2; dx++) for (int dz = 0; dz < 2; dz++) picked.add((long) ((x >> 4) - dx) << 32 | ((z >> 4) - dz) & 0xFFFFFFFFL);
            }
            for (long k : picked) chunks.add(new int[]{(int) (k >> 32), (int) k});
        } else if (a[1].equals("towers")) {
            // Battle Towers: the chunks of the towers built at the candidates of the cells nearest to 0,0, up to <count>
            // towers (a tower covers its chunk corner and the chunks towards -x/-z); prints how many tries succeeded
            int count = Integer.parseInt(a[2]);
            List<String> found = Collections.synchronizedList(new ArrayList<>());
            battletowers.gen.BTWorldGen.built = found::add;
            int tried = 0;
            for (int d = 0; d <= 4000 && found.size() < count; d++) for (int cx = -d; cx <= d && found.size() < count; cx++) for (int cz = -d; cz <= d && found.size() < count; cz++) {
                if (Math.max(Math.abs(cx), Math.abs(cz)) != d || !battletowers.gen.BTWorldGen.triedAt(seed, layout, cx, cz)) continue;
                tried++;
                int before = found.size();
                decoration.chunk(cx, cz);
                if (found.size() > before) for (int dx = -1; dx <= 0; dx++) for (int dz = -1; dz <= 0; dz++) chunks.add(new int[]{cx + dx, cz + dz});
                else System.err.println("no tower at chunk " + cx + ", " + cz + " (" + layout.biomeAt(cx * 16, cz * 16).name + ")");
            }
            battletowers.gen.BTWorldGen.built = null;
            for (String t : new LinkedHashSet<>(found)) System.err.println("tower " + t);
            System.err.println(tried + " towers tried, " + new LinkedHashSet<>(found).size() + " built");
        } else if (a[1].equals("rifts")) {
            // Dimensional Doors: populate the tiles around 0,0 ring by ring until <count> rift gateways and clusters were made;
            // their chunk and its neighbours towards -x/-z (the ruin of a gateway reaches 4 blocks out)
            int count = Integer.parseInt(a[2]);
            List<String> found = Collections.synchronizedList(new ArrayList<>());
            dimdoors.gen.RiftGen.built = found::add;
            Set<Long> picked = new LinkedHashSet<>();
            for (int d = 0; d <= 400 && found.size() < count; d += 8) for (int tx = -d; tx <= d && found.size() < count; tx += 8) for (int tz = -d; tz <= d && found.size() < count; tz += 8) {
                if (Math.max(Math.abs(tx), Math.abs(tz)) != d) continue;
                decoration.chunk(tx, tz);
            }
            dimdoors.gen.RiftGen.built = null;
            for (String r : new LinkedHashSet<>(found)) {
                System.err.println("rift " + r);
                String[] p = r.split(" ");
                int x = Integer.parseInt(p[1]), z = Integer.parseInt(p[3]);
                for (int dx = -1; dx <= 1; dx++) for (int dz = -1; dz <= 1; dz++) picked.add((long) (((x + dx * 4) >> 4)) << 32 | ((z + dz * 4) >> 4) & 0xFFFFFFFFL);
            }
            for (long k : picked) chunks.add(new int[]{(int) (k >> 32), (int) k});
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
                if (e.getValue() instanceof OldTile.Skull sk && c.metadata(k >> 12, k & 255, k >> 8 & 15) == 1) out.append("K ").append(pos).append(' ').append(sk.type()).append(' ').append(sk.rotation() & 15).append('\n');
                if (e.getValue() instanceof OldTile.Spawner s) out.append("S ").append(pos).append(' ').append(s.mob()).append('\n');
                if (e.getValue() instanceof OldTile.Dispenser t) out.append("C ").append(pos).append(' ').append(Arrays.stream(t.slots()).filter(Objects::nonNull).count()).append('\n');
                // chests: filled slots, and how many of them hold vanilla items (items of mods not ported yet stay out in the game)
                if (e.getValue() instanceof OldTile.Chest t) out.append("C ").append(pos).append(' ').append(Arrays.stream(t.slots()).filter(Objects::nonNull).count())
                        .append(' ').append(Arrays.stream(t.slots()).filter(st -> st != null && (st.itemId() <= 158 || st.itemId() >= 256 && st.itemId() <= 422 || st.itemId() >= 2256 && st.itemId() <= 2267)).count())
                        .append(Arrays.stream(t.slots()).filter(Objects::nonNull).map(st -> " " + st.itemId() + ":" + st.damage() + "x" + st.count()).reduce("", String::concat)).append('\n');
            }
            for (OldEntity e : c.entities()) {
                String kind = e instanceof OldEntity.Mob m ? "Mob:" + m.name() : e.getClass().getSimpleName();
                // Better Dungeons mobs: only the ones that are ported (the bosses of betterdungeons.mob)
                if (kind.startsWith("Mob:BetterDungeons.") && !PORTED_BD_MOBS.contains(kind.substring("Mob:BetterDungeons.".length()))) continue;
                out.append("E ").append(e.x()).append(' ').append(e.y()).append(' ').append(e.z()).append(' ').append(kind).append('\n');
            }
        }
        System.out.print(out);
        System.err.println(chunks.size() + " chunks in " + (System.nanoTime() - start) / 1000000 + " ms; blocks changed by population, by id: " + changed);
    }
}
