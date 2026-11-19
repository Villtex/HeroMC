package betterstorage;

/**
 * ChestMaterial: the materials of the reinforced chest with the metadata ids of the original (4 was never used). Each
 * one is its own block now. The recipes (gen.py) take the ingot and block of the material; copper, tin, silver and
 * zinc came from the ore dictionary and use the common tags {@code c:ingots/*} and {@code c:storage_blocks/*}.
 */
public enum ChestMaterial {
    IRON(0, "iron"),
    GOLD(1, "gold"),
    DIAMOND(2, "diamond"),
    EMERALD(3, "emerald"),
    COPPER(5, "copper"),
    TIN(6, "tin"),
    SILVER(7, "silver"),
    ZINC(8, "zinc");

    public final int id;
    public final String name;

    ChestMaterial(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public String blockName() {
        return "reinforced_" + this.name + "_chest";
    }
}
