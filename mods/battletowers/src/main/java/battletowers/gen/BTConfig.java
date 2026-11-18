package battletowers.gen;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * The pack's config/BattleTowers.cfg (a Forge 1.5.2 Configuration file), which the build copies into the jar as
 * battletowers-original/BattleTowers.cfg. Missing values take the defaults of AS_BattleTowersCore.loadForgeConfig.
 */
public final class BTConfig {
    private static final String[] DEFAULT_FLOORS = {
            "280-0-75-6-5;295-0-75-3-5;5-0-75-6-5;83-0-75-3-5",
            "274-0-50-1-1;275-0-50-1-1;50-0-80-3-3;77-0-50-2-2",
            "281-0-75-2-4;263-0-90-4-4;287-0-80-5-5;35-0-75-2-2",
            "20-0-75-3-3;288-0-75-4-4;297-0-75-2-2;260-0-75-2-2",
            "39-0-75-2-2;40-0-75-2-2;6-0-90-3-3;296-0-75-4-4",
            "323-0-50-1-2;346-0-75-1-1;361-0-60-2-2;362-0-60-3-3",
            "267-0-60-1-1;289-0-75-3-3;334-0-75-4-4;349-0-75-3-3;351-0-60-1-2",
            "302-0-40-1-1;303-0-40-1-1;304-0-40-1-1;305-0-40-1-1",
            "57-0-70-1-3;123-0-60-2-2;379-0-75-3-3;379-0-50-1-1",
            "368-0-50-2-2;264-0-70-2-2;331-0-75-5-5;266-0-90-8-8"};
    private static final String[] FLOOR_KEYS = {"Floor 1", "Floor 2", "Floor 3", "Floor 4", "Floor 5", "Floor 6", "Floor 7",
            "Floor 8", "Floor 9", "Top Floor"};

    public final double minDistanceBetweenTowers;
    public final int towerDestroyerEnabled;
    public final int itemGenerateAttemptsPerFloor;
    public final int chanceTowerIsUnderGround;
    final BTItems[] floorItemManagers = new BTItems[10];
    private final Map<String, String> biomes;

    private static volatile BTConfig instance;

    private BTConfig(Map<String, Map<String, String>> file) {
        Map<String, String> main = file.getOrDefault("mainoptions", Map.of());
        Map<String, String> chest = file.getOrDefault("battletowerchestitems", Map.of());
        this.minDistanceBetweenTowers = Integer.parseInt(main.getOrDefault("Minimum Distance between 2 BattleTowers", "196"));
        this.towerDestroyerEnabled = Integer.parseInt(main.getOrDefault("Tower Destroying Enabled", "1"));
        this.itemGenerateAttemptsPerFloor = Integer.parseInt(chest.getOrDefault("Item Generations per Floot", "7"));
        this.chanceTowerIsUnderGround = Integer.parseInt(main.getOrDefault("chanceTowerIsUnderGround", "15"));
        for (int i = 0; i < 10; i++) {
            this.floorItemManagers[i] = new BTItems(chest.getOrDefault(FLOOR_KEYS[i], DEFAULT_FLOORS[i]));
        }
        this.biomes = file.getOrDefault("biomespawnallowed", Map.of());
    }

    public static BTConfig get() {
        BTConfig config = instance;
        if (config == null) {
            synchronized (BTConfig.class) {
                config = instance;
                if (config == null) {
                    config = new BTConfig(read());
                    instance = config;
                }
            }
        }
        return config;
    }

    /** BiomeSpawnAllowed: a biome not in the file is allowed (Property.getBoolean(true); " false" is no boolean either). */
    public boolean biomeAllowed(String biomeName) {
        String value = this.biomes.get(biomeName);
        if (value == null) {
            return true;
        }
        String lower = value.toLowerCase();
        return lower.equals("true") || !lower.equals("false");
    }

    private static Map<String, Map<String, String>> read() {
        Map<String, Map<String, String>> out = new HashMap<>();
        InputStream in = BTConfig.class.getClassLoader().getResourceAsStream("battletowers-original/BattleTowers.cfg");
        if (in == null) {
            in = BTConfig.class.getResourceAsStream("/battletowers-original/BattleTowers.cfg");
        }
        if (in == null) {
            return out;
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            Map<String, String> category = null;
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                String t = line.trim();
                if (t.isEmpty() || t.startsWith("#")) {
                    continue;
                }
                if (t.endsWith("{")) {
                    category = out.computeIfAbsent(t.substring(0, t.length() - 1).trim().toLowerCase(), k -> new HashMap<>());
                } else if (t.equals("}")) {
                    category = null;
                } else if (category != null && t.length() > 2 && t.charAt(1) == ':') {
                    int eq = t.indexOf('=', t.startsWith("\"", 2) ? t.indexOf('"', 3) : 2);
                    String key = t.substring(2, eq);
                    if (key.startsWith("\"") && key.endsWith("\"")) {
                        key = key.substring(1, key.length() - 1);
                    }
                    category.put(key, t.substring(eq + 1));
                }
            }
        } catch (IOException e) {
            // defaults
        }
        return out;
    }
}
