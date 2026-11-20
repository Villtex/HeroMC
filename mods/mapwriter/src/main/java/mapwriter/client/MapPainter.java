package mapwriter.client;

import com.mojang.blaze3d.platform.NativeImage;
import mapwriter.MapWriterMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;

import java.util.Objects;

/**
 * StandardMapRenderer / MapTexture / Render: the map of one {@link MapMode} drawn on the CPU into one image (map
 * pixels sampled from the regions, border, markers, trail and player arrow) that is shown as a single texture. The
 * image is only recomputed when something it shows has changed.
 */
final class MapPainter {
    private static int counter;

    private final Identifier id = Identifier.fromNamespaceAndPath(MapWriterMod.MOD_ID, "map_" + counter++);
    private DynamicTexture texture;
    private int tw, th;
    private int[] buf = new int[0];
    private int lastKey = 0;
    private boolean painted;

    void close() {
        if (texture != null) {
            Minecraft.getInstance().getTextureManager().release(id);
            texture = null;
        }
        painted = false;
    }

    /** Draws the map; returns the player arrow position (GUI pixels) for the mouse. */
    double[] draw(GuiGraphicsExtractor g, Mw mw, MapMode mode, MapView view) {
        Minecraft mc = Minecraft.getInstance();
        int s = Math.max(1, (int) mc.getWindow().getGuiScale());
        mode.setScreen(g.guiWidth(), g.guiHeight());
        view.setAspect(mode.w, mode.h);
        boolean border = mode.cfg.borderWidth.get() > 0 && (argb(mode.cfg.borderColour.get()) >>> 24) != 0;
        int padGui = border ? mode.cfg.borderWidth.get() : 0;
        int pad = padGui * s;
        int pw = mode.w * s;
        int ph = mode.h * s;
        int w = pw + 2 * pad;
        int h = ph + 2 * pad;
        double rot = mode.rotate() ? Math.toRadians(mw.rotationDegrees) : 0.0;
        RegionStore store = mw.store(view.dimension);
        double scaleFrom = view.scaleFrom(mw.dimension);
        double playerX = mw.playerX * scaleFrom;
        double playerZ = mw.playerZ * scaleFrom;
        int key = Objects.hash(view.x, view.z, view.zoomLevel(), view.dimension, mode.w, mode.h, s, rot, store.version, mw.markers.version,
                mw.markers.selected, mw.trail.version, playerX, playerZ, mw.playerHeading, mode.circular(), mode.cfg.alphaPercent.get(),
                mode.cfg.borderWidth.get(), mode.cfg.markerSize.get(), mode.cfg.playerArrowSize.get(), mode.cfg.playerArrowColour.get());
        if (texture == null || w != tw || h != th) {
            close();
            tw = w;
            th = h;
            texture = new DynamicTexture(() -> id.toString(), new NativeImage(tw, th, true));
            mc.getTextureManager().register(id, texture);
            buf = new int[tw * th];
            painted = false;
        }
        double[] arrow = mode.blockToScreen(view, playerX, playerZ, rot, true);
        if (!painted || key != lastKey) {
            paint(mw, mode, view, store, s, pad, pw, ph, rot, playerX, playerZ);
            NativeImage img = texture.getPixels();
            for (int y = 0; y < th; y++) {
                for (int x = 0; x < tw; x++) {
                    img.setPixel(x, y, buf[y * tw + x]);
                }
            }
            texture.upload();
            lastKey = key;
            painted = true;
        }
        g.blit(RenderPipelines.GUI_TEXTURED, id, mode.xTranslation + mode.x - padGui, mode.yTranslation + mode.y - padGui, 0, 0,
                mode.w + 2 * padGui, mode.h + 2 * padGui, tw, th, tw, th);
        return new double[]{mode.xTranslation + arrow[0], mode.yTranslation + arrow[1]};
    }

    static int argb(String hex) {
        try {
            return (int) Long.parseLong(hex.trim(), 16);
        } catch (NumberFormatException e) {
            return 0xFF000000;
        }
    }

    private void paint(Mw mw, MapMode mode, MapView view, RegionStore store, int s, int pad, int pw, int ph, double rot, double playerX, double playerZ) {
        java.util.Arrays.fill(buf, 0);
        double ppbX = pw / view.w;
        double ppbZ = ph / view.h;
        boolean circular = mode.circular();
        double radius = Math.min(pw, ph) / 2.0;
        int alpha = mode.cfg.alphaPercent.get() * 255 / 100;
        double c = Math.cos(-rot);
        double sn = Math.sin(-rot);
        boolean zoomedOut = view.w / pw > 1.5;
        Region cached = null;
        int crx = Integer.MIN_VALUE, crz = Integer.MIN_VALUE;
        for (int j = 0; j < ph; j++) {
            for (int i = 0; i < pw; i++) {
                double dx = i + 0.5 - pw / 2.0;
                double dy = j + 0.5 - ph / 2.0;
                if (circular && dx * dx + dy * dy > radius * radius) {
                    continue;
                }
                int sr = 0, sg = 0, sb = 0, n = 0;
                int samples = zoomedOut ? 2 : 1;
                for (int sj = 0; sj < samples; sj++) {
                    for (int si = 0; si < samples; si++) {
                        double fx = dx + (si + 0.5) / samples - 0.5;
                        double fy = dy + (sj + 0.5) / samples - 0.5;
                        double px = fx * c - fy * sn;
                        double py = fx * sn + fy * c;
                        int bx = (int) Math.floor(view.x + px / ppbX);
                        int bz = (int) Math.floor(view.z + py / ppbZ);
                        int rx = bx >> 9;
                        int rz = bz >> 9;
                        if (rx != crx || rz != crz) {
                            cached = store.get(rx, rz, false);
                            crx = rx;
                            crz = rz;
                        }
                        if (cached != null) {
                            int p = cached.argb[(bz & 511) * Region.SIZE + (bx & 511)];
                            if ((p >>> 24) != 0) {
                                sr += p >> 16 & 0xFF;
                                sg += p >> 8 & 0xFF;
                                sb += p & 0xFF;
                                n++;
                            }
                        }
                    }
                }
                if (n > 0) {
                    int cover = alpha * n / (samples * samples);
                    buf[(j + pad) * tw + i + pad] = cover << 24 | (sr / n) << 16 | (sg / n) << 8 | sb / n;
                }
            }
        }
        // border
        int borderColour = argb(mode.cfg.borderColour.get());
        if (pad > 0 && (borderColour >>> 24) != 0) {
            double cx = tw / 2.0, cy = th / 2.0;
            for (int y = 0; y < th; y++) {
                for (int x = 0; x < tw; x++) {
                    boolean on;
                    if (circular) {
                        double d = Math.hypot(x + 0.5 - cx, y + 0.5 - cy);
                        on = d >= radius && d < radius + pad;
                    } else {
                        on = x < pad || y < pad || x >= tw - pad || y >= th - pad;
                    }
                    if (on) {
                        buf[y * tw + x] = borderColour;
                    }
                }
            }
        }
        double cx = tw / 2.0;
        double cy = th / 2.0;
        // trail
        int n = mw.trail.points.size();
        int index = 0;
        for (Trail.Point p : mw.trail.points) {
            index++;
            if (!p.dimension().equals(view.dimension)) {
                continue;
            }
            double sc = view.scaleFrom(p.dimension());
            double[] pos = mode.blockToScreen(view, p.x() * sc, p.z() * sc, rot, false);
            if (Math.abs(pos[0]) > mode.w / 2.0 || Math.abs(pos[1]) > mode.h / 2.0 || circular && Math.hypot(pos[0], pos[1]) > mode.h / 2.0) {
                continue;
            }
            int a = 255 * index / Math.max(1, n);
            double len = Math.max(1, mode.cfg.markerSize.get() - 1) * s;
            arrow(cx + pos[0] * s, cy + pos[1] * s, p.heading() + rot, len, a << 24);
            arrow(cx + pos[0] * s, cy + pos[1] * s, p.heading() + rot, len - s, a << 24 | 0x00FFFF);
        }
        // markers
        for (Marker m : mw.markers.visible) {
            marker(mw, mode, view, m, 0xFF000000, rot, s, cx, cy);
        }
        if (mw.markers.selected != null) {
            marker(mw, mode, view, mw.markers.selected, 0xFFFFFFFF, rot, s, cx, cy);
        }
        // player arrow
        double[] pos = mode.blockToScreen(view, playerX, playerZ, rot, true);
        arrow(cx + pos[0] * s, cy + pos[1] * s, mw.playerHeading + rot, mode.cfg.playerArrowSize.get() * s, argb(mode.cfg.playerArrowColour.get()));
    }

    private void marker(Mw mw, MapMode mode, MapView view, Marker m, int border, double rot, int s, double cx, double cy) {
        double scale = Mw.scale(view.dimension);
        double[] p = mode.blockToScreen(view, m.x / scale, m.z / scale, rot, true);
        m.screenX = mode.xTranslation + p[0];
        m.screenY = mode.yTranslation + p[1];
        double size = mode.cfg.markerSize.get() * s;
        double half = size / 2.0;
        rect(cx + p[0] * s - half, cy + p[1] * s - half, size, size, border);
        rect(cx + p[0] * s - half + 0.5 * s, cy + p[1] * s - half + 0.5 * s, size - s, size - s, m.colour);
    }

    private void rect(double x, double y, double w, double h, int argb) {
        int x0 = (int) Math.round(x), y0 = (int) Math.round(y);
        int x1 = (int) Math.round(x + w), y1 = (int) Math.round(y + h);
        for (int yy = Math.max(0, y0); yy < Math.min(th, y1); yy++) {
            for (int xx = Math.max(0, x0); xx < Math.min(tw, x1); xx++) {
                buf[yy * tw + xx] = argb;
            }
        }
    }

    /** Render.drawArrow: tip at the heading, two back corners and a notch, as a triangle fan. */
    private void arrow(double x, double y, double angle, double length, int argb) {
        double back = Math.PI * 3.0 / 4.0;
        double[][] v = {
                {x + length * Math.cos(angle), y + length * Math.sin(angle)},
                {x + length * 0.5 * Math.cos(angle - back), y + length * 0.5 * Math.sin(angle - back)},
                {x + length * 0.3 * Math.cos(angle + Math.PI), y + length * 0.3 * Math.sin(angle + Math.PI)},
                {x + length * 0.5 * Math.cos(angle + back), y + length * 0.5 * Math.sin(angle + back)}};
        triangle(v[0], v[1], v[2], argb);
        triangle(v[0], v[2], v[3], argb);
    }

    private void triangle(double[] a, double[] b, double[] c, int argb) {
        int minX = (int) Math.floor(Math.min(a[0], Math.min(b[0], c[0])));
        int maxX = (int) Math.ceil(Math.max(a[0], Math.max(b[0], c[0])));
        int minY = (int) Math.floor(Math.min(a[1], Math.min(b[1], c[1])));
        int maxY = (int) Math.ceil(Math.max(a[1], Math.max(b[1], c[1])));
        double det = (b[1] - c[1]) * (a[0] - c[0]) + (c[0] - b[0]) * (a[1] - c[1]);
        if (Math.abs(det) < 1e-9) {
            return;
        }
        for (int yy = Math.max(0, minY); yy <= Math.min(th - 1, maxY); yy++) {
            for (int xx = Math.max(0, minX); xx <= Math.min(tw - 1, maxX); xx++) {
                double px = xx + 0.5, py = yy + 0.5;
                double l1 = ((b[1] - c[1]) * (px - c[0]) + (c[0] - b[0]) * (py - c[1])) / det;
                double l2 = ((c[1] - a[1]) * (px - c[0]) + (a[0] - c[0]) * (py - c[1])) / det;
                double l3 = 1 - l1 - l2;
                if (l1 >= 0 && l2 >= 0 && l3 >= 0) {
                    buf[yy * tw + xx] = argb | (argb >>> 24 == 0 ? 0xFF000000 : 0);
                }
            }
        }
    }
}
