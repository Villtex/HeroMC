package tconstruct.items;

import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.Nullable;
import tconstruct.TItems;

/**
 * The custom materials of TContent.registerMaterials: the bowstring (string) and the fletchings (feather, leaves,
 * slime crystal, blue slime crystal), with their part value (2) and their stats for the bow and the arrow.
 */
public final class CustomMaterials {
    /** BowstringMaterial: durability, draw speed and flight speed modifiers. */
    public record Bowstring(int id, float durability, float drawSpeed, float flightSpeed) {
    }

    /** FletchingMaterial: accuracy, break chance and mass. */
    public record Fletching(int id, float accuracy, float breakChance, float mass) {
    }

    public static final Bowstring STRING = new Bowstring(0, 1.0F, 1.0F, 1.0F);
    public static final Fletching[] FLETCHINGS = {
            new Fletching(0, 100.0F, 0.0F, 0.05F),
            new Fletching(1, 75.0F, 0.0F, 0.2F),
            new Fletching(2, 100.0F, 0.0F, 0.12F),
            new Fletching(3, 100.0F, 0.0F, 0.12F)
    };

    private CustomMaterials() {
    }

    public static @Nullable Bowstring bowstring(int id) {
        return id == 0 ? STRING : null;
    }

    public static @Nullable Fletching fletching(int id) {
        return id >= 0 && id < FLETCHINGS.length ? FLETCHINGS[id] : null;
    }

    /** The fletching a material makes (TEventHandler.craftFletching; any leaves count as the leaves of 1.5.2). */
    public static int fletchingId(ItemStack stack) {
        if (stack.is(Items.FEATHER)) {
            return 0;
        }
        if (stack.is(ItemTags.LEAVES)) {
            return 1;
        }
        if (stack.is(TItems.SLIME_CRYSTAL.get())) {
            return 2;
        }
        if (stack.is(TItems.BLUE_SLIME_CRYSTAL.get())) {
            return 3;
        }
        return -1;
    }

    /** CustomMaterial.value for PatternBuilder.getPartValue: string, feather and leaves are worth 2. */
    public static int value(ItemStack stack) {
        return stack.is(Items.STRING) || stack.is(Items.FEATHER) || stack.is(ItemTags.LEAVES) ? 2 : 0;
    }
}
