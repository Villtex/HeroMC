package betterdungeons.gen;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A building template of Chocolate/Building: the NBT the exporter block wrote ({"data": {sx, sy, sz}, "blocks": one
 * compound per block in x, y, z order with "blockid", "metadata" and the tile entity data, "entities"}). Kept as
 * arrays; only the blocks that carry more than id and metadata keep their compound.
 */
public final class BDTemplate {
    private static final Map<String, BDTemplate> CACHE = new ConcurrentHashMap<>();
    private static final Map<String, float[][][]> MAPS = new ConcurrentHashMap<>();

    public final String path;
    public final int sx;
    public final int sy;
    public final int sz;
    private final short[] ids;
    private final byte[] metas;
    private final Map<Integer, Map<String, Object>> data;
    public final List<Object> entities;

    private BDTemplate(String path, int sx, int sy, int sz, short[] ids, byte[] metas, Map<Integer, Map<String, Object>> data,
            List<Object> entities) {
        this.path = path;
        this.sx = sx;
        this.sy = sy;
        this.sz = sz;
        this.ids = ids;
        this.metas = metas;
        this.data = data;
        this.entities = entities;
    }

    /** Number of block entries (the "blocks" list may be shorter than sx * sy * sz in a broken file). */
    public int size() {
        return this.ids.length;
    }

    /** "blockid" of entry {@code index} (index = (x * sy + y) * sz + z). */
    public int id(int index) {
        return this.ids[index];
    }

    public int metadata(int index) {
        return this.metas[index];
    }

    /** The whole compound of the entry: blockid, metadata and the tile entity data. */
    public Map<String, Object> nbt(int index) {
        Map<String, Object> nbt = this.data.get(index);
        if (nbt == null) {
            nbt = new HashMap<>();
            nbt.put("blockid", (int) this.ids[index]);
            nbt.put("metadata", (int) this.metas[index]);
        }
        return nbt;
    }

    /** BuilderHelper.getRandomNBTMap: one template of the folder, picked with {@code random} if there is more than one. */
    public static BDTemplate random(String folder, java.util.Random random) {
        List<String> files = BDFiles.list("Building/" + folder);
        int s = 0;
        if (files.size() > 1) {
            s = random.nextInt(files.size());
        }
        return get("Building/" + folder + "/" + files.get(s));
    }

    /** The template at a path below Chocolate/, read once. */
    public static BDTemplate get(String path) {
        return CACHE.computeIfAbsent(path, BDTemplate::load);
    }

    @SuppressWarnings("unchecked")
    private static BDTemplate load(String path) {
        try (DataInputStream in = new DataInputStream(new BufferedInputStream(BDFiles.open(path), 1 << 16))) {
            String[] name = new String[1];
            if (BDNbt.readHeader(in, name) != BDNbt.COMPOUND) {
                throw new IOException("Not an NBT compound");
            }
            Map<String, Object> dimensions = Map.of();
            short[] ids = new short[0];
            byte[] metas = new byte[0];
            Map<Integer, Map<String, Object>> data = new HashMap<>();
            List<Object> entities = List.of();
            int tag;
            while ((tag = BDNbt.readHeader(in, name)) != 0) {
                if (name[0].equals("blocks") && tag == BDNbt.LIST) {
                    int elementType = in.readByte();
                    int length = in.readInt();
                    ids = new short[length];
                    metas = new byte[length];
                    for (int i = 0; i < length; i++) {
                        Map<String, Object> block = (Map<String, Object>) BDNbt.read(in, elementType);
                        ids[i] = (short) BDNbt.getInt(block, "blockid");
                        metas[i] = (byte) BDNbt.getInt(block, "metadata");
                        if (block.size() > 3 || block.size() == 3 && !block.containsKey("pos")) {
                            data.put(i, block);
                        }
                    }
                } else {
                    Object value = BDNbt.read(in, tag);
                    if (name[0].equals("data")) {
                        dimensions = (Map<String, Object>) value;
                    } else if (name[0].equals("entities")) {
                        entities = (List<Object>) value;
                    }
                }
            }
            return new BDTemplate(path, BDNbt.getInt(dimensions, "sx"), BDNbt.getInt(dimensions, "sy"), BDNbt.getInt(dimensions, "sz"),
                    ids, metas, data, entities);
        } catch (IOException | ClassCastException e) {
            throw new IllegalStateException("Corrupted Better Dungeons template " + path, e);
        }
    }

    /**
     * BuilderHelper.getMap: the older text format ("#Exporter v1.0", sx, sy, sz, then per x and y one line of sz
     * numbers "id.meta", the metadata in the hundredths).
     */
    public static float[][][] map(String path) {
        return MAPS.computeIfAbsent(path, BDTemplate::loadMap);
    }

    /** BuilderHelper.getRandomMap: one text template of the folder, always drawing from {@code random}. */
    public static float[][][] randomMap(String folder, java.util.Random random) {
        List<String> files = BDFiles.list("Building/" + folder);
        return map("Building/" + folder + "/" + files.get(random.nextInt(files.size())));
    }

    private static float[][][] loadMap(String path) {
        try (InputStream stream = BDFiles.open(path);
             BufferedReader read = new BufferedReader(new InputStreamReader(stream, StandardCharsets.ISO_8859_1))) {
            read.readLine();
            int sx = Integer.parseInt(read.readLine().trim());
            int sy = Integer.parseInt(read.readLine().trim());
            int sz = Integer.parseInt(read.readLine().trim());
            float[][][] map = new float[sx][sy][sz];
            for (int x = 0; x < sx; x++) {
                for (int y = 0; y < sy; y++) {
                    String line = read.readLine();
                    int cont = 0;
                    for (int z = 0; z < sz; z++) {
                        StringBuilder a = new StringBuilder();
                        while (cont < line.length() && line.charAt(cont) != ' ') {
                            a.append(line.charAt(cont));
                            cont++;
                        }
                        map[x][y][z] = Float.parseFloat(a.toString().trim());
                        cont++;
                    }
                }
            }
            return map;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
