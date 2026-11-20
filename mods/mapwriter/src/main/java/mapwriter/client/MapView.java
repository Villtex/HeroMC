package mapwriter.client;

import mapwriter.MapWriterMod;

/** MapView: which part of which dimension is shown, with the zoom level (blocks across: half the texture size, shifted). */
final class MapView {
    private double aspect = 1.0;
    private int zoomLevel;
    String dimension = "minecraft:overworld";
    double x, z;
    int aspectW = 1, aspectH = 1;
    double w = 1.0, h = 1.0;

    static int minZoom() {
        return -MapWriterMod.ZOOM_IN_LEVELS.get();
    }

    static int maxZoom() {
        return MapWriterMod.ZOOM_OUT_LEVELS.get();
    }

    void setViewCentre(double vx, double vz) {
        this.x = vx;
        this.z = vz;
    }

    void panView(double relX, double relZ) {
        setViewCentre(x + relX * w, z + relZ * h);
    }

    int setZoomLevel(int level) {
        int prev = zoomLevel;
        zoomLevel = Math.min(Math.max(minZoom(), level), maxZoom());
        if (prev != zoomLevel) {
            update();
        }
        return zoomLevel;
    }

    int adjustZoomLevel(int n) {
        return setZoomLevel(zoomLevel + n);
    }

    int zoomLevel() {
        return zoomLevel;
    }

    void zoomToPoint(int newLevel, double bx, double bz) {
        int prev = zoomLevel;
        newLevel = setZoomLevel(newLevel);
        double f = Math.pow(2.0, newLevel - prev);
        setViewCentre(bx - (bx - x) * f, bz - (bz - z) * f);
    }

    /** Switching between dimensions with different coordinate scales (Nether 1:8) keeps the view over the same place. */
    void setDimension(String dim) {
        double ratio = Mw.scale(dimension) / Mw.scale(dim);
        dimension = dim;
        if (ratio != 1.0) {
            setZoomLevel(zoomLevel + (int) Math.round(Math.log(ratio) / Math.log(2.0)));
            setViewCentre(x * ratio, z * ratio);
        }
    }

    void setAspect(int pw, int ph) {
        aspect = (double) pw / ph;
        if (aspectW != pw || aspectH != ph) {
            aspectW = pw;
            aspectH = ph;
            update();
        }
    }

    void update() {
        int texture = MapWriterMod.TEXTURE_SIZE.get();
        double viewSize = zoomLevel >= 0 ? (double) (texture >> 1 << zoomLevel) : (double) (texture >> 1 >> -zoomLevel);
        w = aspect < 1.0 ? viewSize * aspect : viewSize;
        h = aspect > 1.0 ? viewSize / aspect : viewSize;
    }

    double minX() {
        return x - w / 2.0;
    }

    double minZ() {
        return z - h / 2.0;
    }

    /** Factor from coordinates of the dimension the player is in to this view's coordinates. */
    double scaleFrom(String playerDimension) {
        return Mw.scale(playerDimension) / Mw.scale(dimension);
    }
}
