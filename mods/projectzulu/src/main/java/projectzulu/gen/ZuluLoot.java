package projectzulu.gen;

/**
 * The chest loot Project Zulu|Blocks added in postInit (ChestGenHooks in ItemBlockRecipeManager), as OldLoot
 * entries. No game classes (tools/expect.sh compiles this). Called from {@link ZuluWorldGen#register()}.
 */
public final class ZuluLoot {
    /** FML mod id that added the entries (ordering of OldLoot entries). */
    public static final String FML_ID = "ProjectZulu|Blocks";

    private ZuluLoot() {
    }

    public static void register() {
        // TODO Teil 11, Batch Gear
    }
}
