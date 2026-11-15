package customlan;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import net.neoforged.fml.loading.FMLPaths;

/**
 * CLPermissions.txt in the game directory (GuiCustomLanPermissions of 1.5.2): per command 0 = everyone may use it,
 * 2 = only operators. The order is the order of the file and of the two GUI pages.
 */
public final class CLPermissions {
    private CLPermissions() {
    }

    /** key, default value, button label (in GUI order: page 1 = the first 14). */
    public static final String[][] KEYS = {
            {"permBack", "2", "/Back"}, {"permBan", "2", "/Ban|/BanIp"}, {"permUnban", "2", "/Unban|/UnbanIp"},
            {"permBanList", "2", "/BanList"}, {"permKick", "2", "/Kick"}, {"permFireball", "2", "/Fireball"},
            {"permLightning", "2", "/Lightning"}, {"permGod", "2", "/God"}, {"permHat", "0", "/Hat"}, {"permHeal", "2", "/Heal"},
            {"permViewMOTD", "0", "/ViewMOTD"}, {"permSpawnMob", "2", "/SpawnMob"}, {"permGetTime", "0", "/GetTime"}, {"permTP", "2", "/TP"},
            {"permPM", "0", "/PM"}, {"permSetHome", "0", "/SetHome"}, {"permHome", "0", "/Home"}, {"permSetWarp", "2", "/SetWarp"},
            {"permWarp", "0", "/Warp"}, {"permRemoveWarp", "2", "/RemoveWarp"}, {"permWarpList", "0", "/WarpList"},
            {"permSpawn", "0", "/Spawn"}, {"permSetSpawn", "2", "/SetSpawn"}, {"permBroadcast", "2", "/Broadcast"}, {"permFly", "2", "/Fly"},
    };

    private static final Map<String, Integer> VALUES = new LinkedHashMap<>();

    static {
        for (String[] k : KEYS) {
            VALUES.put(k[0], Integer.parseInt(k[1]));
        }
    }

    private static Path file() {
        return FMLPaths.GAMEDIR.get().resolve("CLPermissions.txt");
    }

    public static synchronized int get(String key) {
        return VALUES.getOrDefault(key, 2);
    }

    /** The GUI toggles with 2 - value. */
    public static synchronized void toggle(String key) {
        VALUES.put(key, 2 - get(key));
    }

    /** canSenderUse: the command's key is 0. Command names without a key answer false. */
    public static synchronized boolean canSenderUse(String command) {
        checkOptions();
        String key = switch (command) {
            case "ban", "banip", "ban-ip" -> "permBan";
            case "unban", "unbanip", "unban-ip", "pardon", "pardon-ip" -> "permUnban";
            case "back" -> "permBack";
            case "banlist" -> "permBanList";
            case "kick" -> "permKick";
            case "lightning" -> "permLightning";
            case "fireball" -> "permFireball";
            case "god" -> "permGod";
            case "hat" -> "permHat";
            case "heal" -> "permHeal";
            case "viewmotd" -> "permViewMOTD";
            case "gettime" -> "permGetTime";
            case "pm" -> "permPM";
            case "sethome" -> "permSetHome";
            case "home" -> "permHome";
            case "setwarp" -> "permSetWarp";
            case "warp" -> "permWarp";
            case "removewarp" -> "permRemoveWarp";
            case "warplist" -> "permWarpList";
            case "spawn" -> "permSpawn";
            case "spawnmob" -> "permSpawnMob";
            case "setspawn" -> "permSetSpawn";
            case "broadcast" -> "permBroadcast";
            case "fly" -> "permFly";
            case "tp" -> "permTP";
            default -> null;
        };
        return key != null && get(key) == 0;
    }

    public static synchronized void checkOptions() {
        if (!Files.exists(file())) {
            saveOptions();
        }
        loadOptions();
    }

    public static synchronized void saveOptions() {
        try (PrintWriter w = new PrintWriter(Files.newBufferedWriter(file(), StandardCharsets.UTF_8))) {
            VALUES.forEach((k, v) -> w.println(k + ":" + v));
        } catch (IOException e) {
            CustomLAN.LOGGER.warn("Failed to save the permissions file.", e);
        }
    }

    public static synchronized void loadOptions() {
        try (BufferedReader r = Files.newBufferedReader(file(), StandardCharsets.UTF_8)) {
            String line;
            while ((line = r.readLine()) != null) {
                String[] kv = line.split(":", 2);
                try {
                    if (kv.length == 2 && VALUES.containsKey(kv[0])) {
                        VALUES.put(kv[0], Integer.parseInt(kv[1].trim()));
                    }
                } catch (NumberFormatException e) {
                    CustomLAN.LOGGER.info("Skipping bad option: {}", kv[0]);
                }
            }
        } catch (IOException e) {
            CustomLAN.LOGGER.warn("Failed to load the permissions file.", e);
        }
    }
}
