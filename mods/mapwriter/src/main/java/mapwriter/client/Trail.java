package mapwriter.client;

import mapwriter.MapWriterMod;

import java.util.LinkedList;

/** Trail: arrows where the player has been, the older the fainter. */
final class Trail {
    record Point(double x, double z, double heading, String dimension) {
    }

    final LinkedList<Point> points = new LinkedList<>();
    private long last;
    int version;

    void tick(Mw mw) {
        long now = System.currentTimeMillis();
        if (MapWriterMod.TRAIL.get() && now - last > MapWriterMod.TRAIL_INTERVAL.get()) {
            last = now;
            points.add(new Point(mw.playerX, mw.playerZ, mw.playerHeading, mw.dimension));
            while (points.size() > MapWriterMod.TRAIL_LENGTH.get()) {
                points.poll();
            }
            version++;
        }
    }
}
