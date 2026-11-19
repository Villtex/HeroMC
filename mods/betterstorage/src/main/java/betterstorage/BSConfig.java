package betterstorage;

import java.util.List;
import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Config "settings" of BetterStorage.cfg with the values of the pack as defaults. The block/item ids, the
 * enchantment base id and the data watcher id of the original are not needed any more. enableCrateInventoryInterface
 * is false in the pack (the original disabled it because of dupe issues): crates offer no inventory to hoppers and
 * other machines, which is all this port does.
 */
public final class BSConfig {
    public static final ModConfigSpec SPEC;
    private static final ModConfigSpec.IntValue REINFORCED_CHEST_COLUMNS;
    private static final ModConfigSpec.IntValue BACKPACK_ROWS;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.push("settings");
        REINFORCED_CHEST_COLUMNS = builder.comment("Number of colums in reinforced chests. Valid values are 9, 11 and 13.")
                .defineInRange("reinforcedChestColumns", 13, 9, 13);
        BACKPACK_ROWS = builder.comment("Number of rows in backpacks. Valid values are 1 to 6.").defineInRange("backpackRows", 3, 1, 6);
        builder.pop();
        SPEC = builder.build();
    }

    private BSConfig() {
    }

    /** Config.validateColumnAmount: 9, 11 or 13, anything else gives the default 13. */
    public static int reinforcedChestColumns() {
        int columns = SPEC.isLoaded() ? REINFORCED_CHEST_COLUMNS.get() : 13;
        return List.of(9, 11, 13).contains(columns) ? columns : 13;
    }

    public static int backpackRows() {
        return SPEC.isLoaded() ? BACKPACK_ROWS.get() : 3;
    }
}
