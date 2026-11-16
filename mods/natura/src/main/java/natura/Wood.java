package natura;

import net.minecraft.world.level.material.MapColor;

import java.util.Locale;

/**
 * The 13 woods of Natura in the order of the 1.5.2 planks metadata (Planks.textureNames, also sticks, workbenches,
 * bookshelves, fences). Ghostwood, bloodwood, darkwood and fusewood do not burn (Planks.getFlammability).
 */
public enum Wood {
    EUCALYPTUS("Eucalyptus", MapColor.COLOR_PINK, true),
    SAKURA("Sakura", MapColor.COLOR_PINK, true),
    GHOSTWOOD("Ghostwood", MapColor.QUARTZ, false),
    REDWOOD("Redwood", MapColor.COLOR_RED, true),
    BLOODWOOD("Bloodwood", MapColor.COLOR_ORANGE, false),
    HOPSEED("Hopseed", MapColor.SAND, true),
    MAPLE("Maple", MapColor.COLOR_BROWN, true),
    SILVERBELL("Silverbell", MapColor.SAND, true),
    AMARANTH("Amaranth", MapColor.COLOR_PURPLE, true),
    TIGER("Tigerwood", MapColor.COLOR_ORANGE, true),
    WILLOW("Willow", MapColor.COLOR_GREEN, true),
    DARKWOOD("Darkwood", MapColor.COLOR_BLUE, false),
    FUSEWOOD("Fusewood", MapColor.COLOR_GREEN, false);

    /** Name for registry ids, e.g. {@code eucalyptus_planks}. */
    public final String id;
    /** English name of the original ("Tigerwood Planks", "Amaranth Stairs"). */
    public final String title;
    public final MapColor color;
    public final boolean flammable;

    Wood(String title, MapColor color, boolean flammable) {
        this.id = this.name().toLowerCase(Locale.ROOT);
        this.title = title;
        this.color = color;
        this.flammable = flammable;
    }

    /** Planks metadata of 1.5.2. */
    public int meta() {
        return this.ordinal();
    }
}
