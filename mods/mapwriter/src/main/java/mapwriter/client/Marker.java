package mapwriter.client;

/** Marker: a named point of a group with a colour; coordinates are those of the overworld. */
final class Marker {
    private static final int[] COLOURS = {0xFF0000, 0x00FF00, 0x0000FF, 0xFFFF00, 0xFF00FF, 0x00FFFF, 0xFF8000, 0x7F00FF};
    private static int colourIndex;

    String name;
    int x, y, z;
    String group;
    int colour;
    /** Where the marker was drawn last, in GUI coordinates, for the mouse. */
    double screenX, screenY;

    Marker(String name, String group, int x, int y, int z, int colour) {
        this.name = name;
        this.group = group;
        this.x = x;
        this.y = y;
        this.z = z;
        this.colour = colour;
    }

    static int currentColour() {
        return 0xFF000000 | COLOURS[colourIndex];
    }

    void colourNext() {
        colourIndex = (colourIndex + 1) % COLOURS.length;
        colour = currentColour();
    }

    void colourPrev() {
        colourIndex = (colourIndex + COLOURS.length - 1) % COLOURS.length;
        colour = currentColour();
    }
}
