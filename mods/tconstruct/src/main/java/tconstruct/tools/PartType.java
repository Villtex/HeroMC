package tconstruct.tools;

/**
 * The tool parts of 1.5.2 ({@code ToolPart} items of TContent.registerItems), one item per part and material today
 * ({@code tconstruct:<material>_<key>}). {@code pattern} is the damage of the wooden pattern / metal cast that makes the
 * part (TContent.patternOutputs + 1), -1 for parts without one (shards). {@code costValue} is not needed here: the
 * pattern carries the cost.
 *
 * @param key        registry name part
 * @param tex        texture suffix of 1.5.2 ({@code parts/iron<tex>.png})
 * @param oldId      config id of 1.5.2 (TinkersWorkshop.txt), the game id is +256
 */
public enum PartType {
    TOOL_ROD("tool_rod", "_rod", 1, 6041),
    PICKAXE_HEAD("pickaxe_head", "_pickaxe_head", 2, 6053),
    SHOVEL_HEAD("shovel_head", "_shovel_head", 3, 6054),
    AXE_HEAD("axe_head", "_axe_head", 4, 6044),
    SWORD_BLADE("sword_blade", "_sword_blade", 5, 6056),
    WIDE_GUARD("wide_guard", "_large_guard", 6, 6050),
    HAND_GUARD("hand_guard", "_medium_guard", 7, 6052),
    CROSSBAR("crossbar", "_crossbar", 8, 6046),
    BINDING("binding", "_binding", 9, 6057),
    PAN("pan", "_frypan_head", 10, 6047),
    SIGN_HEAD("sign_head", "_battlesign_head", 11, 6055),
    KNIFE_BLADE("knife_blade", "_knife_blade", 12, 6049),
    CHISEL_HEAD("chisel_head", "_chisel_head", 13, 6045),
    TOUGH_ROD("tough_rod", "_toughrod", 14, 6086),
    TOUGH_BINDING("tough_binding", "_toughbind", 15, 6085),
    LARGE_PLATE("large_plate", "_largeplate", 16, 6082),
    BROAD_AXE_HEAD("broad_axe_head", "_lumberaxe_head", 17, 6051),
    SCYTHE_BLADE("scythe_blade", "_scythe_head", 18, 6084),
    EXCAVATOR_HEAD("excavator_head", "_excavator_head", 19, 6078),
    LARGE_SWORD_BLADE("large_sword_blade", "_large_sword_blade", 20, 6083),
    HAMMER_HEAD("hammer_head", "_hammer_head", 21, 6081),
    FULL_GUARD("full_guard", "_full_guard", 22, 6080),
    ARROWHEAD("arrowhead", "_arrowhead", 25, 6076),
    /** toolShard: the leftover of the part builder and the repair material; not for wood, bone and paper. */
    SHARD("shard", "_chunk", -1, 6042),
    /** Bowstring and Fletching: parts with materials of their own (CustomMaterial), not one item per tool material. */
    BOWSTRING("bowstring", "_bowstring", 23, 6077),
    FLETCHING("fletching", "_fletching", 24, 6079);

    public final String key;
    public final String tex;
    public final int pattern;
    public final int oldId;

    PartType(String key, String tex, int pattern, int oldId) {
        this.key = key;
        this.tex = tex;
        this.pattern = pattern;
        this.oldId = oldId;
    }

    public boolean exists(TMaterial material) {
        return this != BOWSTRING && this != FLETCHING && (this != SHARD || material.hasShard());
    }

    public String itemName(TMaterial material) {
        return material.key + "_" + this.key;
    }

    public static PartType byPattern(int pattern) {
        for (PartType type : values()) {
            if (type.pattern == pattern) {
                return type;
            }
        }
        return null;
    }
}
