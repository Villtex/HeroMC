package backpack.recipe;

import backpack.Backpack;
import backpack.BackpackItem;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

/**
 * RecipeRecolorBackpack: one backpack (not the ender or workbench backpack) and one dye anywhere in the grid give
 * the backpack in the dye's colour with its contents. Leather (small backpack) or tanned leather (big backpack)
 * removes the colour. The original ignored all other items in the grid (and used them up); here they make the
 * recipe fail.
 */
public class RecolorBackpackRecipe extends CustomRecipe {
    public static final RecolorBackpackRecipe INSTANCE = new RecolorBackpackRecipe();
    public static final RecipeSerializer<RecolorBackpackRecipe> SERIALIZER = new RecipeSerializer<>(MapCodec.unit(INSTANCE), StreamCodec.unit(INSTANCE));

    /** The colour index of the original (1.5.2 dye damage, 16 = no colour) for a dye or leather, or -1. */
    public static int colorOf(ItemStack stack) {
        if (stack.is(Items.LEATHER) || stack.is(Backpack.TANNED_LEATHER.get())) {
            return 16;
        }
        DyeColor dye = DyeColor.getColor(stack);
        return dye == null ? -1 : 15 - dye.getId();
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        return !apply(input).isEmpty();
    }

    @Override
    public ItemStack assemble(CraftingInput input) {
        return apply(input);
    }

    public static ItemStack apply(CraftingInput input) {
        ItemStack backpack = ItemStack.EMPTY;
        ItemStack dye = ItemStack.EMPTY;
        for (int i = 0; i < input.size(); i++) {
            ItemStack slot = input.getItem(i);
            if (slot.isEmpty()) {
                continue;
            }
            if (slot.getItem() instanceof BackpackItem item) {
                if (item.kind() != BackpackItem.Kind.NORMAL || !backpack.isEmpty()) {
                    return ItemStack.EMPTY;
                }
                backpack = slot;
            } else if (colorOf(slot) >= 0) {
                if (!dye.isEmpty()) {
                    return ItemStack.EMPTY;
                }
                dye = slot;
            } else {
                return ItemStack.EMPTY;
            }
        }
        if (backpack.isEmpty() || dye.isEmpty()) {
            return ItemStack.EMPTY;
        }
        boolean big = ((BackpackItem) backpack.getItem()).big();
        if (big && dye.is(Items.LEATHER) || !big && dye.is(Backpack.TANNED_LEATHER.get())) {
            return ItemStack.EMPTY;
        }
        return backpack.transmuteCopy(Backpack.backpack(BackpackItem.Kind.NORMAL, colorOf(dye), big), 1);
    }

    @Override
    public CraftingBookCategory category() {
        return CraftingBookCategory.EQUIPMENT;
    }

    @Override
    public RecipeSerializer<RecolorBackpackRecipe> getSerializer() {
        return SERIALIZER;
    }
}
