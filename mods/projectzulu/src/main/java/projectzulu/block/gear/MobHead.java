package projectzulu.block.gear;

/**
 * BlockMobHeads.Head: the 18 mob heads by their metadata of 1.5.2. {@link #id} is the item suffix
 * ({@code mob_head_<id>}), {@link #texture} the creature texture the head model uses
 * ({@code textures/entity/<texture>.png}), {@link #icon} the 1.5.2 icon {@code mobskulls_<icon>.png}.
 */
public enum MobHead {
    RED_FINCH("red_finch", "Stuffed Finch", "finch_red", "redfinch"),
    ALLIGATOR("alligator", "Alligator Head", "alligator", "crocodile"),
    ARMADILLO("armadillo", "Armadillo Head", "armadillo", "armadillo"),
    BLACK_BEAR("black_bear", "Black Bear Head", "black_bear", "blackbear"),
    BROWN_BEAR("brown_bear", "Brown Bear Head", "brown_bear", "brownbear"),
    POLAR_BEAR("polar_bear", "Polar Bear Head", "polar_bear", "polarbear"),
    BEAVER("beaver", "Beaver Head", "beaver", "beaver"),
    BOAR("boar", "Boar Head", "boar", "boar"),
    GIRAFFE("giraffe", "Giraffe Head", "giraffe", "giraffe"),
    GORILLA("gorilla", "Gorilla Head", "gorilla", "gorilla"),
    LIZARD("lizard", "Lizard Head", "lizard", "lizard"),
    MAMMOTH("mammoth", "Mammoth Head", "mammoth", "mammoth"),
    OSTRICH("ostrich", "Ostrich Head", "ostrich", "ostrich"),
    PENGUIN("penguin", "Penguin Head", "penguin", "penguin"),
    RHINO("rhino", "Rhino Head", "rhino", "rhino"),
    TREE_ENT("tree_ent", "TreeEnt Head", "tree_ent", "treeent"),
    VULTURE("vulture", "Vulture Head", "vulture", "vulture"),
    ELEPHANT("elephant", "Elephant Head", "elephant", "elephant");

    public final String id;
    public final String displayName;
    public final String texture;
    public final String icon;

    MobHead(String id, String displayName, String texture, String icon) {
        this.id = id;
        this.displayName = displayName;
        this.texture = texture;
        this.icon = icon;
    }

    /** The metadata (skull type) of 1.5.2. */
    public int meta() {
        return this.ordinal();
    }

    public static MobHead byMeta(int meta) {
        MobHead[] values = values();
        return meta >= 0 && meta < values.length ? values[meta] : RED_FINCH;
    }
}
