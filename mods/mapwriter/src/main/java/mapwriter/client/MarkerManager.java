package mapwriter.client;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/** MarkerManager: the markers of a world in groups; one group (or "all") is visible at a time. Saved in {@code markers.txt}. */
final class MarkerManager {
    private static final Pattern INVALID = Pattern.compile("[^a-zA-Z0-9_]");

    final List<Marker> markers = new ArrayList<>();
    final List<String> groups = new ArrayList<>();
    final List<Marker> visible = new ArrayList<>();
    private String visibleGroup = "none";
    Marker selected;
    /** Counts every change for the painters. */
    int version;

    static String munge(String s) {
        return INVALID.matcher(s).replaceAll("_");
    }

    String visibleGroup() {
        return visibleGroup;
    }

    void setVisibleGroup(String group) {
        visibleGroup = group != null ? munge(group) : "none";
        version++;
    }

    void load(Path file) {
        markers.clear();
        try {
            if (Files.isRegularFile(file)) {
                for (String line : Files.readAllLines(file)) {
                    String[] s = line.trim().split(" ");
                    if (s.length == 6) {
                        try {
                            markers.add(new Marker(s[0], s[5], Integer.parseInt(s[1]), Integer.parseInt(s[2]), Integer.parseInt(s[3]),
                                    0xFF000000 | Integer.parseInt(s[4], 16)));
                        } catch (NumberFormatException e) {
                            // a damaged line is skipped
                        }
                    } else if (s.length == 2 && s[0].equals("visibleGroup")) {
                        visibleGroup = s[1];
                    }
                }
            }
        } catch (IOException e) {
            // no markers
        }
        update();
    }

    void save(Path file) {
        List<String> lines = new ArrayList<>();
        lines.add("visibleGroup " + visibleGroup);
        for (Marker m : markers) {
            lines.add(String.format("%s %d %d %d %06x %s", m.name, m.x, m.y, m.z, m.colour & 0xFFFFFF, m.group));
        }
        try {
            Files.createDirectories(file.getParent());
            Files.write(file, lines);
        } catch (IOException e) {
            // markers are kept in memory until the next save
        }
    }

    void add(String name, String group, int x, int y, int z, int colour) {
        markers.add(new Marker(munge(name), munge(group), x, y, z, colour));
        version++;
    }

    void delete(Marker m) {
        markers.remove(m);
        version++;
    }

    void update() {
        visible.clear();
        groups.clear();
        groups.add("none");
        groups.add("all");
        for (Marker m : markers) {
            if (m.group.equals(visibleGroup) || visibleGroup.equals("all")) {
                visible.add(m);
            }
            if (!groups.contains(m.group)) {
                groups.add(m.group);
            }
        }
        if (!groups.contains(visibleGroup)) {
            visibleGroup = "none";
        }
        version++;
    }

    void nextGroup(int n) {
        int i = groups.indexOf(visibleGroup);
        int size = groups.size();
        visibleGroup = groups.get(i == -1 ? 0 : (i + size + n) % size);
        update();
    }

    void selectNext() {
        if (visible.isEmpty()) {
            selected = null;
            return;
        }
        int i = selected == null ? 0 : Math.max(0, visible.indexOf(selected));
        selected = visible.get((i + 1) % visible.size());
        version++;
    }

    /** The visible marker in the direction the player looks (cos of the angle > 0.8), nearest first. */
    Marker nearestInDirection(int x, int z, double angle) {
        int nearest = 100000000;
        Marker best = null;
        for (Marker m : visible) {
            int dx = m.x - x;
            int dz = m.z - z;
            int d = dx * dx + dz * dz;
            if (Math.cos(angle - Math.atan2(dz, dx)) > 0.8 && d < nearest && d > 4) {
                best = m;
                nearest = d;
            }
        }
        return best;
    }
}
