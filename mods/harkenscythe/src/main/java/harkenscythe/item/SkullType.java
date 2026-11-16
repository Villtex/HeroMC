package harkenscythe.item;

/**
 * The sixteen skulls of ItemHSSkull in damage-value order: item id ({@code <id>_skull}), name ("<name> Head"), texture
 * of the head (textures/entity/skull/...) and the scale of RenderHSSkull.
 */
public enum SkullType {
    SPIDER("spider", "spider", 1.0F, 1.0F),
    CAVE_SPIDER("cave_spider", "cavespider", 1.0F, 1.0F),
    ENDERMAN("enderman", "enderman", 1.0F, 1.0F),
    PIGMAN_ZOMBIE("pigman_zombie", "pigmanzombie", 1.0F, 1.0F),
    VILLAGER_ZOMBIE("villager_zombie", "villagerzombie", 1.0F, 1.0F),
    VILLAGER("villager", "villager", 1.0F, 1.0F),
    COW("cow", "cow", 1.0F, 1.0F),
    MOOSHROOM("mooshroom", "mooshroom", 1.0F, 1.0F),
    PIG("pig", "pig", 1.0F, 1.0F),
    SQUID("squid", "squid", 1.0F, 1.0F),
    SHEEP("sheep", "sheep", 1.0F, 1.0F),
    WOLF("wolf", "wolf", 0.8F, 0.8F),
    OCELOT("ocelot", "ocelot", 0.7F, 0.7F),
    ENDER_DRAGON("ender_dragon", "enderdragon", 2.0F, 2.0F),
    BAT("bat", "bat", 0.35F, 0.35F),
    CHICKEN("chicken", "chicken", 0.6F, 0.65F);

    private final String id;
    private final String texture;
    /** glScalef(-x, -y, z) of RenderHSSkull: x, and y = z. */
    public final float scaleX;
    public final float scaleYZ;

    SkullType(String id, String texture, float scaleX, float scaleYZ) {
        this.id = id;
        this.texture = texture;
        this.scaleX = scaleX;
        this.scaleYZ = scaleYZ;
    }

    public String id() {
        return this.id;
    }

    public String texture() {
        return this.texture;
    }
}
