package mapwriter;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * MapWriter 2.0.5: minimap (small and large), full screen map with markers and teleport, player trail. Only the client
 * does anything. Standard values are those of the pack's MapWriter.cfg. The map is read from the chunks the client has
 * loaded and saved per world as 512 x 512 block images (see {@code mapwriter.client.RegionStore}).
 */
@Mod(MapWriterMod.MOD_ID)
public final class MapWriterMod {
    public static final String MOD_ID = "mapwriter";

    private static final ModConfigSpec.Builder B = new ModConfigSpec.Builder();

    /** One map mode category of the config (smallMap, largeMap, fullScreenMap). */
    public static final class Mode {
        public final ModConfigSpec.BooleanValue enabled;
        public final ModConfigSpec.IntValue alphaPercent, borderWidth, heightPercent, marginTop, marginBottom, marginLeft, marginRight, markerSize, playerArrowSize;
        public final ModConfigSpec.ConfigValue<String> borderColour, playerArrowColour;
        public final ModConfigSpec.BooleanValue circular, rotate, coordsEnabled;

        Mode(String name, boolean enabled, int heightPercent, int top, int bottom, int left, int right, int borderWidth, int arrow, int marker,
             boolean circular, boolean coords) {
            B.push(name);
            this.enabled = B.define("enabled", enabled);
            this.alphaPercent = B.defineInRange("alphaPercent", 100, 0, 100);
            this.borderWidth = B.defineInRange("borderWidth", borderWidth, 0, 50);
            this.borderColour = B.comment("ARGB hex").define("borderColour", borderWidth > 0 ? "ff000000" : "00000000");
            this.heightPercent = B.defineInRange("heightPercent", heightPercent, -1, 100);
            this.marginTop = B.defineInRange("marginTop", top, -1, 2000);
            this.marginBottom = B.defineInRange("marginBottom", bottom, -1, 2000);
            this.marginLeft = B.defineInRange("marginLeft", left, -1, 2000);
            this.marginRight = B.defineInRange("marginRight", right, -1, 2000);
            this.markerSize = B.defineInRange("markerSize", marker, 1, 20);
            this.playerArrowSize = B.defineInRange("playerArrowSize", arrow, 1, 20);
            this.playerArrowColour = B.comment("ARGB hex").define("playerArrowColour", "ffff0000");
            this.circular = B.define("circular", circular);
            this.rotate = B.define("rotate", false);
            this.coordsEnabled = B.define("coordsEnabled", coords);
            B.pop();
        }
    }

    public static final ModConfigSpec.BooleanValue COORDS;
    public static final ModConfigSpec.IntValue OVERLAY_INDEX, CHUNKS_PER_TICK, ZOOM_IN_LEVELS, ZOOM_OUT_LEVELS, TEXTURE_SIZE, TRAIL_LENGTH, TRAIL_INTERVAL;
    public static final ModConfigSpec.BooleanValue TELEPORT, TRAIL;
    public static final ModConfigSpec.ConfigValue<String> TELEPORT_COMMAND;
    public static final Mode SMALL, LARGE, FULL;

    static {
        B.push("options");
        COORDS = B.define("coordsEnabled", true);
        OVERLAY_INDEX = B.comment("0 = small map, 1 = large map, 2 = no map").defineInRange("overlayModeIndex", 0, 0, 1000);
        CHUNKS_PER_TICK = B.defineInRange("chunksPerTick", 3, 1, 64);
        ZOOM_IN_LEVELS = B.defineInRange("zoomInLevels", 5, 1, 256);
        ZOOM_OUT_LEVELS = B.defineInRange("zoomOutLevels", 5, 1, 256);
        TEXTURE_SIZE = B.comment("Blocks across the view at zoom level 0 are half of this").defineInRange("textureSize", 2048, 1024, 8192);
        TELEPORT = B.comment("The map GUI teleports with a chat command").define("teleportEnabled", false);
        TELEPORT_COMMAND = B.define("teleportCommand", "tp");
        TRAIL = B.define("playerTrailEnabled", false);
        TRAIL_LENGTH = B.defineInRange("playerTrailMaxLength", 30, 1, 200);
        TRAIL_INTERVAL = B.defineInRange("playerTrailMarkerIntervalMillis", 5000, 100, 360000);
        B.pop();
        // enabled, height%, top, bottom, left, right, border, arrow, marker, circular, coords
        SMALL = new Mode("smallMap", true, 30, 10, -1, -1, 10, 1, 4, 3, true, true);
        LARGE = new Mode("largeMap", true, -1, 10, 40, 40, 40, 1, 5, 5, false, true);
        FULL = new Mode("fullScreenMap", true, -1, 0, 0, 0, 0, 0, 5, 5, false, false);
    }

    private static final ModConfigSpec SPEC = B.build();

    public static void saveConfig() {
        SPEC.save();
    }

    public MapWriterMod(IEventBus modBus, ModContainer container) {
        container.registerConfig(ModConfig.Type.CLIENT, SPEC);
        if (FMLEnvironment.getDist() == Dist.CLIENT) {
            mapwriter.client.MwClient.register(modBus);
        }
    }
}
