package mapwriter.client;

import mapwriter.MapWriterMod;

/** MapMode: where on the screen a map is drawn (the margins, height percentage and shape of one config category). */
final class MapMode {
    final MapWriterMod.Mode cfg;
    int sw = 320, sh = 240;
    /** Centre of the map on the screen and the rectangle around that centre, in GUI pixels. */
    int xTranslation, yTranslation, x, y, w = 50, h = 50;
    int textX, textY;

    MapMode(MapWriterMod.Mode cfg) {
        this.cfg = cfg;
    }

    boolean circular() {
        return cfg.circular.get();
    }

    boolean rotate() {
        return cfg.rotate.get();
    }

    void setScreen(int sw, int sh) {
        if (sw != this.sw || sh != this.sh || w <= 50 && h <= 50) {
            this.sw = sw;
            this.sh = sh;
        }
        update();
    }

    /** The same arithmetic as MapMode.update of the original. */
    void update() {
        int size = sh * cfg.heightPercent.get() / 100;
        int top = cfg.marginTop.get(), bottom = cfg.marginBottom.get(), left = cfg.marginLeft.get(), right = cfg.marginRight.get();
        int px;
        if (left >= 0 && right >= 0) {
            px = left;
            w = sw - left - right;
        } else if (left >= 0) {
            px = left;
            w = size;
        } else if (right >= 0) {
            px = sw - size - right;
            w = size;
        } else {
            px = (sw - size) / 2;
            w = size;
        }
        int py;
        if (top >= 0 && bottom >= 0) {
            py = top;
            h = sh - top - bottom;
        } else if (top >= 0) {
            py = top;
            h = size;
        } else if (bottom >= 0) {
            py = sh - size - bottom;
            h = size;
        } else {
            py = (sh - size) / 2;
            h = size;
        }
        w = Math.max(w, 8);
        h = Math.max(h, 8);
        int halfW = w / 2;
        int halfH = h / 2;
        xTranslation = px + halfW;
        yTranslation = py + halfH;
        x = -halfW;
        y = -halfH;
        if (circular()) {
            w = h;
            x = -halfH;
        }
        textX = 0;
        textY = y + h + 4;
    }

    /** Block (x, z) to a position relative to the map centre, rotated by {@code rot} radians; clamped to the map when asked. */
    double[] blockToScreen(MapView view, double bx, double bz, double rot, boolean clamp) {
        double xr = (bx - view.x) / view.w;
        double zr = (bz - view.z) / view.h;
        if (clamp) {
            double limit = 0.49;
            if (!circular()) {
                for (int i = 0; i < 2; i++) {
                    if (xr < -limit) {
                        zr = -limit * zr / xr;
                        xr = -limit;
                    }
                    if (xr > limit) {
                        zr = limit * zr / xr;
                        xr = limit;
                    }
                    if (zr < -limit) {
                        xr = -limit * xr / zr;
                        zr = -limit;
                    }
                    if (zr > limit) {
                        xr = limit * xr / zr;
                        zr = limit;
                    }
                }
            } else if (xr * xr + zr * zr > limit * limit) {
                double a = Math.atan2(zr, xr);
                xr = limit * Math.cos(a);
                zr = limit * Math.sin(a);
            }
        }
        double px = w * xr;
        double py = h * zr;
        double c = Math.cos(rot);
        double s = Math.sin(rot);
        return new double[]{px * c - py * s, px * s + py * c};
    }

    /** Screen position (GUI pixels, absolute) back to block coordinates, undoing the rotation. */
    double[] screenToBlock(MapView view, double sx, double sy, double rot) {
        double dx = sx - xTranslation;
        double dy = sy - yTranslation;
        double c = Math.cos(-rot);
        double s = Math.sin(-rot);
        double px = dx * c - dy * s;
        double py = dx * s + dy * c;
        return new double[]{view.x + px / w * view.w, view.z + py / h * view.h};
    }
}
