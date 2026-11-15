package customlan;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import net.neoforged.fml.loading.FMLPaths;

/**
 * optionsCLP.txt in the game directory (GuiCustomLan of 1.5.2): the LAN settings, "key:value" per line. Shared by the
 * screens and the integrated server (same JVM).
 */
public final class CLOptions {
    private CLOptions() {
    }

    public static String serverMOTD = "Minecraft Server";
    public static String serverIP = "0.0.0.0";
    public static int serverPort = 25565;
    public static int serverMaxPlayers = 16;
    public static boolean serverOnlineMode = false;
    public static boolean serverCanSpawnAnimals = true;
    public static boolean serverCanSpawnNPCs = true;
    public static boolean serverAllowPvP = true;
    public static boolean serverAllowFlight = true;

    private static Path file() {
        return FMLPaths.GAMEDIR.get().resolve("optionsCLP.txt");
    }

    /** checkOptions: creates the file with the defaults, then loads it. */
    public static synchronized void checkOptions() {
        if (!Files.exists(file())) {
            saveOptions();
        }
        loadOptions();
    }

    public static synchronized void saveOptions() {
        try (PrintWriter w = new PrintWriter(Files.newBufferedWriter(file(), StandardCharsets.UTF_8))) {
            w.println("serverMOTD:" + serverMOTD);
            w.println("serverIP:" + serverIP);
            w.println("serverPort:" + serverPort);
            w.println("serverMaxPlayers:" + serverMaxPlayers);
            w.println("serverOnlineMode:" + serverOnlineMode);
            w.println("serverAllowPvP:" + serverAllowPvP);
            w.println("serverCanSpawnNPCs:" + serverCanSpawnNPCs);
            w.println("serverCanSpawnAnimals:" + serverCanSpawnAnimals);
            w.println("serverAllowFlight:" + serverAllowFlight);
        } catch (IOException e) {
            CustomLAN.LOGGER.warn("[CustomLAN]: Cannot save Custom LAN Port configs.", e);
        }
    }

    public static synchronized void loadOptions() {
        if (!Files.exists(file())) {
            return;
        }
        try (BufferedReader r = Files.newBufferedReader(file(), StandardCharsets.UTF_8)) {
            String line;
            while ((line = r.readLine()) != null) {
                String[] kv = line.split(":", 2);
                if (kv.length != 2) {
                    continue;
                }
                try {
                    switch (kv[0]) {
                        case "serverMOTD" -> serverMOTD = kv[1];
                        case "serverIP" -> serverIP = kv[1];
                        case "serverPort" -> serverPort = Integer.parseInt(kv[1].trim());
                        case "serverMaxPlayers" -> serverMaxPlayers = Integer.parseInt(kv[1].trim());
                        case "serverOnlineMode" -> serverOnlineMode = kv[1].equals("true");
                        case "serverAllowPvP" -> serverAllowPvP = kv[1].equals("true");
                        case "serverCanSpawnNPCs" -> serverCanSpawnNPCs = kv[1].equals("true");
                        case "serverCanSpawnAnimals" -> serverCanSpawnAnimals = kv[1].equals("true");
                        case "serverAllowFlight" -> serverAllowFlight = kv[1].equals("true");
                        default -> {
                        }
                    }
                } catch (NumberFormatException e) {
                    CustomLAN.LOGGER.info("Skipping bad option: {}", kv[0]);
                }
            }
        } catch (IOException e) {
            CustomLAN.LOGGER.warn("Failed to load options", e);
        }
    }
}
