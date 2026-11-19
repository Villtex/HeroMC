package bibliocraft;

/** The metadata variants of the original: wood types 0..3, dye colours 0..15 (BlockLoader.dyes), weapon case cloth. */
public final class Woods {
    public static final String[] NAMES = {"oak", "spruce", "birch", "jungle"};
    public static final String[] COLORS = {"white", "light_gray", "gray", "black", "red", "orange", "yellow", "lime", "green", "cyan",
            "light_blue", "blue", "purple", "magenta", "pink", "brown"};
    /** Weapon case metadata = wood * 4 + cloth: red, blue, green and black wool. */
    public static final String[] CASE_CLOTH = {"red", "blue", "green", "black"};

    private Woods() {
    }
}
