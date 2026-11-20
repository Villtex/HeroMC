package mapwriter.client;

import mapwriter.MapWriterMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.chunk.LevelChunk;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Mw: the state of MapWriter in one world (single-player save or server): the maps of its dimensions, markers, trail,
 * the overlay maps, and the work done every tick (updating the player, scanning chunks into the regions).
 */
final class Mw {
    static final String OVERWORLD = "minecraft:overworld";
    private static final Map<String, Double> SCALES = new HashMap<>();

    static Mw current;

    final Minecraft mc = Minecraft.getInstance();
    final Path worldDir;
    final ExecutorService io = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "MapWriter-IO");
        t.setDaemon(true);
        return t;
    });
    private final Map<String, RegionStore> stores = new HashMap<>();
    final MarkerManager markers = new MarkerManager();
    final Trail trail = new Trail();
    final List<String> dimensions = new ArrayList<>();
    final MapView overlayView = new MapView();
    final MapMode smallMode = new MapMode(MapWriterMod.SMALL);
    final MapMode largeMode = new MapMode(MapWriterMod.LARGE);
    private final MapPainter smallPainter = new MapPainter();
    private final MapPainter largePainter = new MapPainter();

    String dimension = OVERWORLD;
    double playerX, playerY, playerZ, playerHeading, rotationDegrees;
    int playerXInt, playerYInt, playerZInt;
    int defaultTeleportHeight = 80;
    private int tickCounter;
    private int cursor;
    private int cursorRadius = -1;
    private int[][] offsets = new int[0][];

    private Mw(Path worldDir) {
        this.worldDir = worldDir;
    }

    static double scale(String dimension) {
        return SCALES.getOrDefault(dimension, dimension.equals("minecraft:the_nether") ? 8.0 : 1.0);
    }

    static void open(Minecraft mc) {
        String name;
        if (mc.getSingleplayerServer() != null) {
            name = MarkerManager.munge(mc.getSingleplayerServer().getWorldData().getLevelName());
        } else {
            name = "mp_" + MarkerManager.munge(mc.getCurrentServer() != null ? mc.getCurrentServer().ip : "server");
        }
        Mw mw = new Mw(mc.gameDirectory.toPath().resolve("mapwriter").resolve(name.isEmpty() ? "default" : name));
        mw.markers.load(mw.worldDir.resolve("markers.txt"));
        mw.loadDimensions();
        mw.overlayView.setZoomLevel(-1);
        BlockColours.clear();
        current = mw;
    }

    static void close() {
        Mw mw = current;
        current = null;
        if (mw != null) {
            mw.shutdown();
        }
    }

    private void shutdown() {
        for (RegionStore s : stores.values()) {
            s.saveAll();
        }
        markers.save(worldDir.resolve("markers.txt"));
        saveDimensions();
        MapWriterMod.saveConfig();
        smallPainter.close();
        largePainter.close();
        io.shutdown();
        try {
            io.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    RegionStore store(String dim) {
        return stores.computeIfAbsent(dim, d -> new RegionStore(worldDir.resolve("dim_" + MarkerManager.munge(d)), io));
    }

    private void loadDimensions() {
        dimensions.clear();
        dimensions.add(OVERWORLD);
        try {
            Path f = worldDir.resolve("dimensions.txt");
            if (Files.isRegularFile(f)) {
                for (String line : Files.readAllLines(f)) {
                    if (!line.isBlank() && !dimensions.contains(line.trim())) {
                        dimensions.add(line.trim());
                    }
                }
            }
        } catch (IOException e) {
            // only the overworld is known
        }
    }

    private void saveDimensions() {
        try {
            Files.createDirectories(worldDir);
            Files.write(worldDir.resolve("dimensions.txt"), dimensions);
        } catch (IOException e) {
            // found again by visiting
        }
    }

    void addDimension(String dim) {
        if (!dimensions.contains(dim)) {
            dimensions.add(dim);
        }
    }

    void message(String text) {
        if (mc.player != null) {
            mc.player.sendOverlayMessage(Component.literal(text));
        }
    }

    // ------------------------------------------------------------------ per tick

    void tick() {
        ClientLevel level = mc.level;
        if (level == null || mc.player == null) {
            return;
        }
        String dim = level.dimension().identifier().toString();
        if (!dim.equals(dimension)) {
            dimension = dim;
            SCALES.put(dim, level.dimensionType().coordinateScale());
            addDimension(dim);
            overlayView.setDimension(dim);
        }
        playerX = mc.player.getX();
        playerY = mc.player.getY();
        playerZ = mc.player.getZ();
        playerXInt = (int) Math.floor(playerX);
        playerYInt = (int) Math.floor(playerY);
        playerZInt = (int) Math.floor(playerZ);
        playerHeading = Math.toRadians(mc.player.getYRot()) + Math.PI / 2;
        rotationDegrees = -mc.player.getYRot() + 180.0;
        if (mc.gui.screen() == null) {
            overlayView.setViewCentre(playerX * overlayView.scaleFrom(dimension), playerZ * overlayView.scaleFrom(dimension));
        }
        scanChunks(level);
        trail.tick(this);
        if (++tickCounter % 600 == 0) {
            for (RegionStore s : stores.values()) {
                s.saveAll();
            }
        }
    }

    /** RegionManager.onTick: a few chunks around the player per tick, nearest first, round and round. */
    private void scanChunks(ClientLevel level) {
        int radius = Math.max(2, Math.min(mc.options.renderDistance().get(), 24));
        if (radius != cursorRadius) {
            cursorRadius = radius;
            List<int[]> list = new ArrayList<>();
            for (int dz = -radius; dz <= radius; dz++) {
                for (int dx = -radius; dx <= radius; dx++) {
                    if (dx * dx + dz * dz <= radius * radius) {
                        list.add(new int[]{dx, dz});
                    }
                }
            }
            list.sort((a, b) -> Integer.compare(a[0] * a[0] + a[1] * a[1], b[0] * b[0] + b[1] * b[1]));
            offsets = list.toArray(new int[0][]);
            cursor = 0;
        }
        RegionStore store = store(dimension);
        int pcx = playerXInt >> 4;
        int pcz = playerZInt >> 4;
        int todo = MapWriterMod.CHUNKS_PER_TICK.get();
        int attempts = 20;
        while (attempts-- > 0 && todo > 0) {
            int[] o = offsets[cursor];
            cursor = (cursor + 1) % offsets.length;
            int cx = pcx + o[0];
            int cz = pcz + o[1];
            LevelChunk chunk = level.getChunkSource().getChunkNow(cx, cz);
            if (chunk == null || level.getChunkSource().getChunkNow(cx - 1, cz) == null || level.getChunkSource().getChunkNow(cx + 1, cz) == null
                    || level.getChunkSource().getChunkNow(cx, cz - 1) == null || level.getChunkSource().getChunkNow(cx, cz + 1) == null) {
                continue;
            }
            ChunkScanner.scan(level, chunk, store);
            todo--;
        }
    }

    // ------------------------------------------------------------------ overlay

    /** The map the N key selected: small map, large map or none. */
    private MapMode currentMode() {
        List<MapMode> list = new ArrayList<>();
        if (smallMode.cfg.enabled.get()) {
            list.add(smallMode);
        }
        if (largeMode.cfg.enabled.get()) {
            list.add(largeMode);
        }
        list.add(null);
        return list.get(MapWriterMod.OVERLAY_INDEX.get() % list.size());
    }

    void nextOverlayMode(int n) {
        int size = (smallMode.cfg.enabled.get() ? 1 : 0) + (largeMode.cfg.enabled.get() ? 1 : 0) + 1;
        MapWriterMod.OVERLAY_INDEX.set((MapWriterMod.OVERLAY_INDEX.get() + size + n) % size);
    }

    void renderOverlay(GuiGraphicsExtractor g) {
        MapMode mode = currentMode();
        if (mode == null || mc.level == null) {
            return;
        }
        MapPainter painter = mode == smallMode ? smallPainter : largePainter;
        painter.draw(g, this, mode, overlayView);
        drawCoords(g, mode);
    }

    void drawCoords(GuiGraphicsExtractor g, MapMode mode) {
        if (MapWriterMod.COORDS.get() && mode.cfg.coordsEnabled.get()) {
            g.centeredText(mc.font, String.format("%d %d %d", playerXInt, playerYInt, playerZInt), mode.xTranslation + mode.textX,
                    mode.yTranslation + mode.textY, 0xFFFFFFFF);
        }
    }

    void toggleRotating() {
        boolean rotate = !MapWriterMod.SMALL.rotate.get();
        for (MapWriterMod.Mode m : new MapWriterMod.Mode[]{MapWriterMod.SMALL, MapWriterMod.LARGE}) {
            m.rotate.set(rotate);
            m.circular.set(rotate);
        }
    }

    // ------------------------------------------------------------------ teleport

    void teleportTo(int x, int y, int z) {
        if (MapWriterMod.TELEPORT.get() && mc.player != null) {
            mc.player.connection.sendCommand(String.format("%s %d %d %d", MapWriterMod.TELEPORT_COMMAND.get(), x, y, z));
        } else {
            message("teleportation is disabled in the MapWriter config");
        }
    }

    void teleportToOverworldPos(int x, int y, int z) {
        double s = scale(dimension);
        teleportTo((int) (x / s), y, (int) (z / s));
    }

    void teleportToMapPos(MapView view, int x, int y, int z) {
        teleportTo((int) (x * Mw.scale(view.dimension) / scale(dimension)), y, (int) (z * Mw.scale(view.dimension) / scale(dimension)));
    }
}
