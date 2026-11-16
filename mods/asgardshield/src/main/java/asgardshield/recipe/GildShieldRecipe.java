package asgardshield.recipe;

import asgardshield.item.ItemAsgardShield;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

/**
 * RecipesASShieldDye: six gold ingots around a shield turn it into its gilded version, keeping durability,
 * enchantments and colour.
 * <pre>
 * G G G
 * G S G
 *   G
 * </pre>
 * 1.5.2 did not look at the two bottom corners (whatever lay there was used up); here they must be empty. The
 * gilded shield was "item id + 1", which only worked with the default ids of the mod: with the ids of the pack most
 * shields turned into another item (stone into the wooden shield, nether quartz into the diamond giant sword). The
 * port always gives the gilded version of the shield.
 */
public class GildShieldRecipe extends CustomRecipe {
    public static final GildShieldRecipe INSTANCE = new GildShieldRecipe();
    public static final RecipeSerializer<GildShieldRecipe> SERIALIZER = new RecipeSerializer<>(MapCodec.unit(INSTANCE), StreamCodec.unit(INSTANCE));
    private static final int[] GOLD = {0, 1, 2, 3, 5, 7};

    @Override
    public boolean matches(CraftingInput input, Level level) {
        return !assemble(input).isEmpty();
    }

    @Override
    public ItemStack assemble(CraftingInput input) {
        if (input.width() != 3 || input.height() != 3) {
            return ItemStack.EMPTY;
        }
        for (int slot : GOLD) {
            if (!input.getItem(slot).is(Items.GOLD_INGOT)) {
                return ItemStack.EMPTY;
            }
        }
        if (!input.getItem(6).isEmpty() || !input.getItem(8).isEmpty()) {
            return ItemStack.EMPTY;
        }
        return gild(input.getItem(4));
    }

    /** The gilded version of a (non-gilded) shield, or empty. */
    public static ItemStack gild(ItemStack shield) {
        if (!(shield.getItem() instanceof ItemAsgardShield item)) {
            return ItemStack.EMPTY;
        }
        Item gilded = item.gildedVariant();
        if (gilded == null) {
            return ItemStack.EMPTY;
        }
        ItemStack result = shield.transmuteCopy(gilded, 1);
        DyedItemColor dyed = shield.get(DataComponents.DYED_COLOR);
        if (dyed != null) {
            result.set(DataComponents.DYED_COLOR, new DyedItemColor(recolor(dyed.rgb())));
        }
        return result;
    }

    /** The colour mixing of 1.5.2 with the shield's own colour only (rounds the channels a little). */
    static int recolor(int l) {
        float f = (l >> 16 & 0xFF) / 255.0F;
        float f1 = (l >> 8 & 0xFF) / 255.0F;
        float f2 = (l & 0xFF) / 255.0F;
        int i = (int) (Math.max(f, Math.max(f1, f2)) * 255.0F);
        int k = (int) (f * 255.0F);
        int l1 = (int) (f1 * 255.0F);
        int b = (int) (f2 * 255.0F);
        float ff = (float) i;
        float ff1 = Math.max(k, Math.max(l1, b));
        k = (int) (k * ff / ff1);
        l1 = (int) (l1 * ff / ff1);
        b = (int) (b * ff / ff1);
        return ((k << 8) + l1 << 8) + b;
    }

    @Override
    public CraftingBookCategory category() {
        return CraftingBookCategory.EQUIPMENT;
    }

    @Override
    public RecipeSerializer<GildShieldRecipe> getSerializer() {
        return SERIALIZER;
    }
}
