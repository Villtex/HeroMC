package mapwriter.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * MwGui: the full screen map. Left drag or arrow keys pan, mouse wheel or Page Up/Down zoom, right click makes or edits
 * a marker, a selected marker can be dragged. Keys: Space next group, Delete, C colour, Home/End centre, N select
 * marker, T teleport, P save a PNG of the view, R regenerate the view.
 */
final class MwGui extends Screen {
    private static final String[] HELP_KEYS = {"Space", "Delete", "C", "Home", "End", "N", "T", "P", "R"};
    private static final String[] HELP_TEXT = {"Next marker group", "Delete selected marker", "Cycle selected marker colour", "Centre map on player",
            "Centre map on selected marker", "Select next marker", "Teleport to cursor or selected marker", "Save PNG of visible map area",
            "Regenerate visible map area"};

    private final Mw mw;
    private final MapMode mode = new MapMode(mapwriter.MapWriterMod.FULL);
    private final MapView view = new MapView();
    private final MapPainter painter = new MapPainter();
    private final Label help = new Label(), options = new Label(), dimensionLabel = new Label(), groupLabel = new Label();

    private boolean leftHeld;
    private double dragStartX, dragStartY, viewXStart, viewZStart;
    private boolean dragged;
    private Marker movingMarker;
    private int movingXStart, movingZStart;
    private int mouseBlockX, mouseBlockY, mouseBlockZ;
    private double[] playerArrow = {0, 0};

    private static final class Label {
        int x, y, w = 1, h = 12;

        void draw(GuiGraphicsExtractor g, net.minecraft.client.gui.Font font, int x, int y, String s) {
            this.x = x;
            this.y = y;
            this.w = font.width(s) + 4;
            g.fill(x, y, x + w, y + h, 0x80000000);
            g.text(font, s, x + 2, y + 2, 0xFFFFFFFF, false);
        }

        void drawRightOf(GuiGraphicsExtractor g, net.minecraft.client.gui.Font font, Label l, String s) {
            draw(g, font, l.x + l.w + 5, l.y, s);
        }

        boolean within(double px, double py) {
            return px >= x && py >= y && px <= x + w && py <= y + h;
        }
    }

    MwGui(Mw mw) {
        super(Component.literal("MapWriter"));
        this.mw = mw;
        view.dimension = mw.overlayView.dimension;
        view.setViewCentre(mw.playerX * view.scaleFrom(mw.dimension), mw.playerZ * view.scaleFrom(mw.dimension));
        view.setZoomLevel(0);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        painter.close();
        mw.markers.save(mw.worldDir.resolve("markers.txt"));
        mapwriter.MapWriterMod.saveConfig();
        super.onClose();
    }

    private Marker markerNear(double x, double y) {
        Marker near = null;
        for (Marker m : mw.markers.visible) {
            double dx = m.screenX - x, dy = m.screenY - y;
            if (dx * dx + dy * dy < 6.0) {
                near = m;
            }
        }
        return near;
    }

    private boolean playerNear(double x, double y) {
        double dx = playerArrow[0] - x, dy = playerArrow[1] - y;
        return dx * dx + dy * dy < 9.0;
    }

    private int heightAt(int bx, int bz) {
        if (mw.mc.level == null || !view.dimension.equals(mw.dimension) || mw.mc.level.dimensionType().hasCeiling()) {
            return 0;
        }
        LevelChunk chunk = mw.mc.level.getChunkSource().getChunkNow(bx >> 4, bz >> 4);
        return chunk == null ? 0 : chunk.getHeight(Heightmap.Types.WORLD_SURFACE, bx & 15, bz & 15);
    }

    // ------------------------------------------------------------------ input

    @Override
    public boolean keyPressed(KeyEvent event) {
        int key = event.key();
        if (key == InputConstants.KEY_ESCAPE) {
            onClose();
            return true;
        }
        MarkerManager mm = mw.markers;
        switch (key) {
            case InputConstants.KEY_R -> {
                regenerate();
                onClose();
            }
            case InputConstants.KEY_T -> {
                if (mm.selected != null) {
                    mw.teleportToOverworldPos(mm.selected.x, mm.selected.y, mm.selected.z);
                    onClose();
                } else {
                    int bx = mouseBlockX, bz = mouseBlockZ;
                    minecraft.gui.setScreen(new PromptScreen(this, "Teleport Height:", "" + mw.defaultTeleportHeight,
                            s -> parseInt(s) == null ? "invalid height" : null, s -> {
                                int h = Math.min(Math.max(-64, parseInt(s)), 319);
                                mw.defaultTeleportHeight = h;
                                mw.teleportToMapPos(view, bx, h, bz);
                                onClose();
                            }));
                }
            }
            case InputConstants.KEY_P -> {
                mergeToImage();
                onClose();
            }
            case InputConstants.KEY_C -> {
                if (mm.selected != null) {
                    mm.selected.colourNext();
                    mm.version++;
                }
            }
            case InputConstants.KEY_N -> mm.selectNext();
            case InputConstants.KEY_SPACE -> mm.nextGroup(1);
            case InputConstants.KEY_HOME -> view.setViewCentre(mw.playerX * view.scaleFrom(mw.dimension), mw.playerZ * view.scaleFrom(mw.dimension));
            case InputConstants.KEY_UP -> view.panView(0.0, -0.3);
            case InputConstants.KEY_LEFT -> view.panView(-0.3, 0.0);
            case InputConstants.KEY_RIGHT -> view.panView(0.3, 0.0);
            case InputConstants.KEY_DOWN -> view.panView(0.0, 0.3);
            case InputConstants.KEY_END -> {
                if (mm.selected != null) {
                    double s = Mw.scale(view.dimension);
                    view.setViewCentre(mm.selected.x / s, mm.selected.z / s);
                }
            }
            case InputConstants.KEY_DELETE -> {
                if (mm.selected != null) {
                    mm.delete(mm.selected);
                    mm.update();
                    mm.selected = null;
                }
            }
            default -> {
                if (MwClient.GUI.matches(event)) {
                    onClose();
                } else if (MwClient.ZOOM_IN.matches(event)) {
                    view.adjustZoomLevel(-1);
                } else if (MwClient.ZOOM_OUT.matches(event)) {
                    view.adjustZoomLevel(1);
                } else if (MwClient.GROUP.matches(event)) {
                    mm.nextGroup(1);
                } else {
                    return super.keyPressed(event);
                }
            }
        }
        return true;
    }

    private static Integer parseInt(String s) {
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double x = event.x(), y = event.y();
        Marker marker = markerNear(x, y);
        Marker prev = mw.markers.selected;
        if (event.button() == 0) {
            if (dimensionLabel.within(x, y)) {
                minecraft.gui.setScreen(new PromptScreen(this, "Set dimension to:", view.dimension, s -> s.isBlank() ? "invalid dimension" : null, s -> {
                    String dim = s.trim().contains(":") ? s.trim() : "minecraft:" + s.trim();
                    view.setDimension(dim);
                    mw.overlayView.setDimension(dim);
                    mw.addDimension(dim);
                }));
            } else if (options.within(x, y)) {
                minecraft.gui.setScreen(new MwOptionsScreen(mw, this));
            } else {
                leftHeld = true;
                dragged = false;
                dragStartX = x;
                dragStartY = y;
                mw.markers.selected = marker;
                mw.markers.version++;
                if (marker != null && prev == marker) {
                    movingMarker = marker;
                    movingXStart = marker.x;
                    movingZStart = marker.z;
                }
            }
        } else if (event.button() == 1) {
            if (marker != null && prev == marker) {
                editMarker(marker);
            } else if (marker == null) {
                newMarker(x, y);
            }
        }
        viewXStart = view.x;
        viewZStart = view.z;
        return true;
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (event.button() == 0) {
            leftHeld = false;
            movingMarker = null;
        }
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
        if (leftHeld && event.button() == 0) {
            double offX = (dragStartX - event.x()) * view.w / mode.w;
            double offZ = (dragStartY - event.y()) * view.h / mode.h;
            if (!dragged && Math.abs(dragStartX - event.x()) + Math.abs(dragStartY - event.y()) < 3) {
                return true;
            }
            dragged = true;
            if (movingMarker != null) {
                double s = Mw.scale(view.dimension);
                movingMarker.x = movingXStart - (int) (offX * s);
                movingMarker.z = movingZStart - (int) (offZ * s);
                mw.markers.version++;
            } else {
                view.setViewCentre(viewXStart + offX, viewZStart + offZ);
            }
            return true;
        }
        return super.mouseDragged(event, dx, dy);
    }

    @Override
    public boolean mouseScrolled(double x, double y, double scrollX, double scrollY) {
        int direction = scrollY > 0 ? 1 : scrollY < 0 ? -1 : 0;
        if (direction == 0) {
            return true;
        }
        Marker marker = markerNear(x, y);
        if (marker != null && marker == mw.markers.selected) {
            if (direction > 0) {
                marker.colourNext();
            } else {
                marker.colourPrev();
            }
            mw.markers.version++;
        } else if (dimensionLabel.within(x, y)) {
            int i = Math.max(0, mw.dimensions.indexOf(view.dimension));
            String dim = mw.dimensions.get((i + mw.dimensions.size() + direction) % mw.dimensions.size());
            view.setDimension(dim);
            mw.overlayView.setDimension(dim);
        } else if (groupLabel.within(x, y)) {
            mw.markers.nextGroup(direction);
        } else {
            view.zoomToPoint(view.zoomLevel() - direction, mouseBlockX, mouseBlockZ);
        }
        return true;
    }

    // ------------------------------------------------------------------ markers

    private void newMarker(double sx, double sy) {
        String group = mw.markers.visibleGroup().equals("none") ? "group" : mw.markers.visibleGroup();
        int mx, my, mz;
        if (playerNear(sx, sy)) {
            mx = mw.playerXInt;
            my = mw.playerYInt;
            mz = mw.playerZInt;
            double s = Mw.scale(mw.dimension);
            mx = (int) (mx * s);
            mz = (int) (mz * s);
        } else {
            double s = Mw.scale(view.dimension);
            mx = (int) (mouseBlockX * s);
            my = mouseBlockY > 0 ? mouseBlockY : mw.defaultTeleportHeight;
            mz = (int) (mouseBlockZ * s);
        }
        markerDialog(null, "", group, mx, my, mz);
    }

    private void editMarker(Marker m) {
        markerDialog(m, m.name, m.group, m.x, m.y, m.z);
    }

    /** The five questions of MarkerTextDialog: name, group, x, y, z. */
    private void markerDialog(Marker editing, String name, String group, int x, int y, int z) {
        String titleName = editing != null ? "Edit Marker Name:" : "Marker Name:";
        minecraft.gui.setScreen(new PromptScreen(this, titleName, name, s -> s.isBlank() ? "marker must have a name" : null, n ->
                minecraft.gui.setScreen(new PromptScreen(this, "Marker Group:", group, s -> s.isBlank() ? "marker must have a group name" : null, gr ->
                        minecraft.gui.setScreen(new PromptScreen(this, "Marker X:", "" + x, s -> parseInt(s) == null ? "invalid value" : null, sx ->
                                minecraft.gui.setScreen(new PromptScreen(this, "Marker Y:", "" + y, s -> parseInt(s) == null ? "invalid value" : null, sy ->
                                        minecraft.gui.setScreen(new PromptScreen(this, "Marker Z:", "" + z, s -> parseInt(s) == null ? "invalid value" : null, sz -> {
                                            int colour = Marker.currentColour();
                                            if (editing != null) {
                                                colour = editing.colour;
                                                mw.markers.delete(editing);
                                            }
                                            mw.markers.add(n.trim(), gr.trim(), parseInt(sx), parseInt(sy), parseInt(sz), colour);
                                            mw.markers.setVisibleGroup(gr.trim());
                                            mw.markers.update();
                                        }))))))))));
    }

    // ------------------------------------------------------------------ view actions

    private void regenerate() {
        RegionStore store = mw.store(view.dimension);
        store.clearArea((int) view.minX(), (int) view.minZ(), (int) (view.minX() + view.w), (int) (view.minZ() + view.h));
        BlockColours.clear();
        mw.message(String.format("regenerating %dx%d blocks starting from (%d, %d)", (int) view.w, (int) view.h, (int) view.minX(), (int) view.minZ()));
    }

    /** MergeTask: the visible area of the map as one PNG in the png folder of the world. */
    private void mergeToImage() {
        int w = (int) Math.min(view.w, 8192), h = (int) Math.min(view.h, 8192);
        int minX = (int) view.minX(), minZ = (int) view.minZ();
        RegionStore store = mw.store(view.dimension);
        mw.io.execute(() -> {
            try (NativeImage img = new NativeImage(w, h, true)) {
                for (int z = 0; z < h; z++) {
                    for (int x = 0; x < w; x++) {
                        img.setPixel(x, z, store.pixel(minX + x, minZ + z));
                    }
                }
                Path dir = mw.worldDir.resolve("png");
                Files.createDirectories(dir);
                int n = 0;
                Path out;
                do {
                    out = dir.resolve(String.format("%s_%03d.png", mw.worldDir.getFileName(), n++));
                } while (Files.exists(out));
                img.writeToFile(out);
            } catch (IOException | RuntimeException e) {
                // reported below as not saved
            }
        });
        mw.message("saving the visible map to the png folder of the world");
    }

    // ------------------------------------------------------------------ drawing

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        g.fill(0, 0, width, height, 0xC0101010);
        double scaleFrom = view.scaleFrom(mw.dimension);
        playerArrow = painter.draw(g, mw, mode, view);
        double[] b = mode.screenToBlock(view, mouseX, mouseY, 0.0);
        mouseBlockX = (int) Math.floor(b[0]);
        mouseBlockZ = (int) Math.floor(b[1]);
        mouseBlockY = heightAt(mouseBlockX, mouseBlockZ);
        Marker marker = markerNear(mouseX, mouseY);
        if (marker != null) {
            hint(g, mouseX, mouseY, marker.name, marker.x, marker.y, marker.z);
        }
        if (playerNear(mouseX, mouseY)) {
            hint(g, mouseX, mouseY, minecraft.player.getName().getString(), mw.playerXInt, mw.playerYInt, mw.playerZInt);
        }
        status(g);
        help.draw(g, font, 5, 5, "[help]");
        options.drawRightOf(g, font, help, "[options]");
        dimensionLabel.drawRightOf(g, font, options, "[dimension: " + view.dimension + "]");
        groupLabel.drawRightOf(g, font, dimensionLabel, "[group: " + mw.markers.visibleGroup() + "]");
        if (help.within(mouseX, mouseY)) {
            drawHelp(g);
        }
        super.extractRenderState(g, mouseX, mouseY, partialTick);
    }

    private void status(GuiGraphicsExtractor g) {
        String s = mouseBlockY != 0 ? String.format("cursor: (%d, %d, %d)", mouseBlockX, mouseBlockY, mouseBlockZ)
                : String.format("cursor: (%d, ?, %d)", mouseBlockX, mouseBlockZ);
        if (mw.mc.level != null && view.dimension.equals(mw.dimension) && mw.mc.level.getChunkSource().getChunkNow(mouseBlockX >> 4, mouseBlockZ >> 4) != null) {
            var biome = mw.mc.level.getBiome(new BlockPos(mouseBlockX, mouseBlockY != 0 ? mouseBlockY : 64, mouseBlockZ));
            s += ", biome: " + biome.unwrapKey().map(k -> k.identifier().getPath()).orElse("?");
        }
        g.fill(10, height - 21, width - 10, height - 6, 0x80000000);
        g.centeredText(font, s, width / 2, height - 18, 0xFFFFFFFF);
    }

    private void hint(GuiGraphicsExtractor g, int x, int y, String title, int mx, int my, int mz) {
        String desc = String.format("(%d, %d, %d)", mx, my, mz);
        int w = Math.max(font.width(title), font.width(desc));
        x = Math.min(x, width - (w + 16));
        y = Math.min(Math.max(10, y), height - 14);
        g.fill(x + 8, y - 10, x + w + 16, y + 14, 0x80000000);
        g.text(font, title, x + 10, y - 8, 0xFFFFFFFF, false);
        g.text(font, desc, x + 10, y + 4, 0xFFCCCCCC, false);
    }

    private void drawHelp(GuiGraphicsExtractor g) {
        int lines = HELP_KEYS.length + 7;
        g.fill(10, 20, 288, 20 + lines * 11 + 12, 0x80000000);
        g.text(font, "Keys:", 14, 24, 0xFFFFFFFF, false);
        int y = 38;
        for (int i = 0; i < HELP_KEYS.length; i++) {
            g.text(font, HELP_KEYS[i], 14, y, 0xFFFFFFFF, false);
            g.text(font, "| " + HELP_TEXT[i], 70, y, 0xFFFFFFFF, false);
            y += 11;
        }
        y += 5;
        String[] more = {"Left click drag or arrow keys pan the map.", "Mouse wheel or Page Up/Down zooms map.", "Right click map to create a new marker.",
                "Left click drag a selected marker to move it.", "Mouse wheel over selected marker to cycle colour.", "Mouse wheel over dimension or group box to cycle."};
        for (String s : more) {
            g.text(font, s, 14, y, 0xFFFFFFFF, false);
            y += 11;
        }
    }
}
