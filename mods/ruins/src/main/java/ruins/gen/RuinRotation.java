package ruins.gen;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * CustomRotationMapping of Ruins 10.3: rotation_mappings.txt of the templates folder, the metadata a block gets when
 * a template is turned east (1), south (2) or west (3). Also the block names the original could look up.
 */
final class RuinRotation {
    private static volatile Map<Integer, TreeMap<Integer, Integer>[]> mappings;

    private RuinRotation() {
    }

    /** Block.getUnlocalizedName of the blocks the files of the pack name (only the tripwire hook). */
    static int blockOfName(String name) {
        return name.equals("tile.tripWireSource") ? 131 : -1;
    }

    /** Whether 1.5.2 had a block with this id (Block.blocksList): all of 1 to 158; templates use no mod blocks. */
    static boolean blockExists(int id) {
        return id >= 1 && id <= 158;
    }

    static int getMapping(int blockID, int metadata, int dir) {
        TreeMap<Integer, Integer>[] map = mappings().get(blockID);
        if (map == null) {
            return metadata;
        }
        Integer i = map[dir].get(metadata);
        return i == null ? metadata : i;
    }

    private static Map<Integer, TreeMap<Integer, Integer>[]> mappings() {
        Map<Integer, TreeMap<Integer, Integer>[]> map = mappings;
        if (map == null) {
            map = load();
            mappings = map;
        }
        return map;
    }

    @SuppressWarnings("unchecked")
    private static Map<Integer, TreeMap<Integer, Integer>[]> load() {
        Map<Integer, TreeMap<Integer, Integer>[]> map = new TreeMap<>();
        if (!RuinsFiles.exists("rotation_mappings.txt")) {
            return map;
        }
        List<Integer> current = new ArrayList<>();
        try {
            for (String line : RuinsFiles.lines("ruins/rotation_mappings.txt")) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                if (line.startsWith("BlockID=")) {
                    current.clear();
                    for (String s : line.split("=")[1].split(";")) {
                        int i;
                        try {
                            i = Integer.parseInt(s);
                        } catch (NumberFormatException e) {
                            i = blockOfName(s);
                        }
                        if (i > 0) {
                            current.add(i);
                        }
                    }
                } else {
                    String[] val = line.split("-");
                    int dir = switch (val[0]) {
                        case "EAST" -> 1;
                        case "SOUTH" -> 2;
                        case "WEST" -> 3;
                        default -> 0;
                    };
                    int metadata = Integer.parseInt(val[1]);
                    int result = Integer.parseInt(val[2]);
                    for (int id : current) {
                        TreeMap<Integer, Integer>[] maps = map.computeIfAbsent(id, k -> {
                            TreeMap<Integer, Integer>[] m = new TreeMap[4];
                            for (int n = 0; n < 4; n++) {
                                m[n] = new TreeMap<>();
                            }
                            return m;
                        });
                        maps[dir].put(metadata, result);
                    }
                }
            }
        } catch (IOException | RuntimeException e) {
            // "Error parsing custom mappings": what was read so far stays
        }
        return map;
    }
}
