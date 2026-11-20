package projectzulu.gen;

import hexxit.worldgen152.core.populate.OldLoot;
import hexxit.worldgen152.core.populate.OldStack;

/**
 * The chest loot Project Zulu|Blocks added in postInit (ChestGenHooks in ItemBlockRecipeManager), as OldLoot
 * entries. No game classes (tools/expect.sh compiles this). Called from {@link ZuluWorldGen#register()}.
 */
public final class ZuluLoot {
    /** FML mod id that added the entries (ordering of OldLoot entries). */
    public static final String FML_ID = "ProjectZulu|Blocks";
    /** ItemGenerics.Properties.ShinyBauble.meta(). */
    private static final int SHINY_BAUBLE = 24;

    private ZuluLoot() {
    }

    public static void register() {
        // order as in setupBlockModuleRecipies: jasper (1-1, weight 5), then the shiny bauble (3-8, weight 35)
        OldLoot.DUNGEON_CHEST.add(FML_ID, new OldStack(ZuluIds.JASPER, 0, 1), 1, 1, 5);
        OldLoot.DUNGEON_CHEST.add(FML_ID, new OldStack(ZuluIds.GENERIC_CRAFTING_ITEMS, SHINY_BAUBLE, 1), 3, 8, 35);
    }
}
