package betterdungeons.gen;

import java.io.DataInput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Reads the uncompressed named NBT of 1.5.2 (NBTTagCompound.readNamedTag) into plain Java values: compounds as
 * {@code Map<String, Object>}, lists as {@code List<Object>}, numbers boxed, strings, byte and int arrays.
 */
public final class BDNbt {
    public static final int COMPOUND = 10;
    public static final int LIST = 9;

    private BDNbt() {
    }

    /** Reads the type and name of the next named tag; returns the type (0 for the end tag) and puts the name into {@code name[0]}. */
    public static int readHeader(DataInput in, String[] name) throws IOException {
        int type = in.readByte();
        name[0] = type == 0 ? "" : in.readUTF();
        return type;
    }

    public static Object read(DataInput in, int type) throws IOException {
        switch (type) {
            case 1:
                return in.readByte();
            case 2:
                return in.readShort();
            case 3:
                return in.readInt();
            case 4:
                return in.readLong();
            case 5:
                return in.readFloat();
            case 6:
                return in.readDouble();
            case 7: {
                byte[] bytes = new byte[in.readInt()];
                in.readFully(bytes);
                return bytes;
            }
            case 8:
                return in.readUTF();
            case LIST: {
                int elementType = in.readByte();
                int length = in.readInt();
                List<Object> list = new ArrayList<>(Math.max(0, length));
                for (int i = 0; i < length; i++) {
                    list.add(read(in, elementType));
                }
                return list;
            }
            case COMPOUND: {
                Map<String, Object> compound = new LinkedHashMap<>();
                String[] name = new String[1];
                int tag;
                while ((tag = readHeader(in, name)) != 0) {
                    compound.put(name[0], read(in, tag));
                }
                return compound;
            }
            case 11: {
                int[] ints = new int[in.readInt()];
                for (int i = 0; i < ints.length; i++) {
                    ints[i] = in.readInt();
                }
                return ints;
            }
            default:
                throw new IOException("Unknown NBT tag type " + type);
        }
    }

    // ---- NBTTagCompound getters: missing or of another type gives 0 / "" / empty, as in 1.5.2 ----

    public static int getInt(Map<String, Object> nbt, String key) {
        return nbt.get(key) instanceof Number n ? n.intValue() : 0;
    }

    public static double getDouble(Map<String, Object> nbt, String key) {
        return nbt.get(key) instanceof Number n ? n.doubleValue() : 0.0;
    }

    public static String getString(Map<String, Object> nbt, String key) {
        return nbt.get(key) instanceof String s ? s : "";
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> getCompound(Map<String, Object> nbt, String key) {
        return nbt.get(key) instanceof Map<?, ?> m ? (Map<String, Object>) m : Map.of();
    }

    @SuppressWarnings("unchecked")
    public static List<Object> getList(Map<String, Object> nbt, String key) {
        return nbt.get(key) instanceof List<?> l ? (List<Object>) l : List.of();
    }
}
