package bspkrs.treecapitator;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * TreeCapitator.cfg, sections general_settings.global_settings and general_settings.per_tree_defaults, with the
 * values of the Hexxit pack. A SYNCED config: the client needs breakSpeedModifier and sneakAction for the same break
 * speed as the server. The axe and shears lists of the old file are the item tags #treecapitator:axes and
 * #treecapitator:shears, the tree definitions are data files (registry treecapitator:tree).
 */
public final class TCSettings {
    private TCSettings() {
    }

    private static final ModConfigSpec.Builder B = new ModConfigSpec.Builder();

    static {
        B.push("global_settings");
    }

    public static final ModConfigSpec.BooleanValue ALLOW_DEBUG_LOGGING = B.comment("Set to true if you want TreeCapitator to tell you what kind of block you are clicking when sneaking, false to disable")
            .define("allowDebugLogging", false);
    public static final ModConfigSpec.BooleanValue ALLOW_DEBUG_OUTPUT = B.comment("Set to true if you want TreeCapitator to tell you what kind of block you are clicking on when sneaking, false to disable")
            .define("allowDebugOutput", false);
    public static final ModConfigSpec.BooleanValue ALLOW_ITEM_DAMAGE = B.comment("Enable to cause item damage based on number of blocks destroyed")
            .define("allowItemDamage", true);
    public static final ModConfigSpec.BooleanValue ALLOW_MORE_BLOCKS_THAN_DAMAGE = B.comment("Enable to allow chopping down the entire tree even if your item does not have enough damage remaining to cover the number of blocks.")
            .define("allowMoreBlocksThanDamage", false);
    public static final ModConfigSpec.DoubleValue DAMAGE_INCREASE_AMOUNT = B.comment("Amount to increase the damage multiplier by every increaseDamageEveryXBlocks blocks (if useIncreasingItemDamage=true)")
            .defineInRange("damageIncreaseAmount", 1.0, 0.1, 100.0);
    public static final ModConfigSpec.DoubleValue DAMAGE_MULTIPLIER = B.comment("Axes and shears will take damage this many times for each log broken. Remaining damage is rounded and applied to tools when a tree is finished.")
            .defineInRange("damageMultiplier", 1.0, 0.1, 50.0);
    public static final ModConfigSpec.BooleanValue DESTROY_LEAVES = B.comment("Enabling this option will cause destruction of leaves on trees.")
            .define("destroyLeaves", true);
    public static final ModConfigSpec.BooleanValue DISABLE_CREATIVE_DROPS = B.comment("Enabling this option will disable log and leaf drops while in creative mode.")
            .define("disableCreativeDrops", false);
    public static final ModConfigSpec.BooleanValue DISABLE_IN_CREATIVE = B.comment("Enabling this option will disable tree chopping while in creative mode.")
            .define("disableInCreative", false);
    public static final ModConfigSpec.IntValue INCREASE_DAMAGE_EVERY_X_BLOCKS = B.comment("When useIncreasingItemDamage=true the damage applied per block broken will increase each time X blocks are broken in a tree.")
            .defineInRange("increaseDamageEveryXBlocks", 8, 1, 500);
    public static final ModConfigSpec.BooleanValue NEED_ITEM = B.comment("Whether you need an item from the axe list (#treecapitator:axes) to chop down a tree.")
            .define("needItem", true);
    public static final ModConfigSpec.BooleanValue SHEAR_LEAVES = B.comment("Enabling this option will shear leaves when you have shears in your hotbar.")
            .define("shearLeaves", false);
    public static final ModConfigSpec.BooleanValue SHEAR_VINES = B.comment("Enabling this option will shear vines when you have shears in your hotbar.")
            .define("shearVines", false);
    public static final ModConfigSpec.ConfigValue<String> SNEAK_ACTION = B.comment("Set sneakAction = \"disable\" to disable tree chopping while sneaking,",
                    "set sneakAction = \"enable\" to only enable tree chopping while sneaking,",
                    "set sneakAction = \"none\" to have tree chopping enabled regardless of sneaking.")
            .define("sneakAction", "disable", o -> o instanceof String s && (s.equalsIgnoreCase("disable") || s.equalsIgnoreCase("enable") || s.equalsIgnoreCase("none")));
    public static final ModConfigSpec.BooleanValue TREE_HEIGHT_DECIDES_BREAK_SPEED = B.comment("When set to true, the breakSpeedModifier values are ignored and the time it takes to chop a tree is",
                    "the original speed divided by twice the number of logs above the one you are chopping.")
            .define("treeHeightDecidesBreakSpeed", false);
    public static final ModConfigSpec.BooleanValue USE_INCREASING_ITEM_DAMAGE = B.comment("Set to true to have the damage applied to axes and shears increase as more blocks are broken in a tree.")
            .define("useIncreasingItemDamage", false);
    public static final ModConfigSpec.BooleanValue USE_STRICT_BLOCK_PAIRING = B.comment("Set to true to only break the logs and leaves of the tree definition the chopped log belongs to,",
                    "false to treat every registered log and leaf as one tree.")
            .define("useStrictBlockPairing", true);

    static {
        B.pop().comment("Defaults of the per-tree settings; a tree definition can override each one.").push("per_tree_defaults");
    }

    public static final ModConfigSpec.BooleanValue ALLOW_SMART_TREE_DETECTION = B.comment("Set to false to disable TreeCapitator Smart Tree Detection.",
                    "Smart Tree Detection counts the leaves around the top log of a tree to decide whether it is a tree.")
            .define("allowSmartTreeDetection", true);
    public static final ModConfigSpec.DoubleValue BREAK_SPEED_MODIFIER = B.comment("When using an item that can chop trees, the break speed will by multiplied by this value.")
            .defineInRange("breakSpeedModifier", 0.256, 0.01, 1.0);
    public static final ModConfigSpec.IntValue MAX_HOR_LEAF_BREAK_DIST = B.comment("Maximum horizontal distance from the top logs of a tree at which leaves are destroyed (-1 = no limit).")
            .defineInRange("maxHorLeafBreakDist", 4, -1, 100);
    public static final ModConfigSpec.IntValue MAX_HOR_LOG_BREAK_DIST = B.comment("Maximum horizontal distance from the chopped log at which logs are destroyed (-1 = no limit).")
            .defineInRange("maxHorLogBreakDist", 16, -1, 100);
    public static final ModConfigSpec.IntValue MAX_LEAF_ID_DIST = B.comment("Distance from the top log at which leaves are counted to identify a tree.")
            .defineInRange("maxLeafIDDist", 1, 1, 8);
    public static final ModConfigSpec.IntValue MAX_VER_LOG_BREAK_DIST = B.comment("Maximum vertical distance from the chopped log at which logs are destroyed (-1 = no limit).")
            .defineInRange("maxVerLogBreakDist", -1, -1, 255);
    public static final ModConfigSpec.IntValue MIN_LEAVES_TO_ID = B.comment("Minimum number of leaves that have to be near the top log to identify a tree.")
            .defineInRange("minLeavesToID", 3, 0, 8);
    public static final ModConfigSpec.BooleanValue ONLY_DESTROY_UPWARDS = B.comment("Set to true to only destroy logs at or above the chopped one.")
            .define("onlyDestroyUpwards", true);
    public static final ModConfigSpec.BooleanValue REQUIRE_LEAF_DECAY_CHECK = B.comment("When true, only leaves that would decay (natural leaves next to a broken log) are destroyed.")
            .define("requireLeafDecayCheck", true);

    public static final ModConfigSpec SPEC = B.pop().build();
}
