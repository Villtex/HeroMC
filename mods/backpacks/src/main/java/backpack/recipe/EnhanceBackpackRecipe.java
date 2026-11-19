package backpack.recipe;

import backpack.Backpack;
import backpack.BackpackItem;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

/**
 * RecipeEnhanceBackpack: a small backpack (any colour, or the workbench backpack; damage below 19, so not the ender
 * backpack) in the middle of a full 3x3 grid of tanned leather gives the big backpack (damage + 32) with all its
 * components, so the contents stay.
 */
public class EnhanceBackpackRecipe extends CustomRecipe {
    public static final EnhanceBackpackRecipe INSTANCE = new EnhanceBackpackRecipe();
    public static final RecipeSerializer<EnhanceBackpackRecipe> SERIALIZER = new RecipeSerializer<>(MapCodec.unit(INSTANCE), StreamCodec.unit(INSTANCE));

    @Override
    public boolean matches(CraftingInput input, Level level) {
        return !apply(input).isEmpty();
    }

    @Override
    public ItemStack assemble(CraftingInput input) {
        return apply(input);
    }

    public static ItemStack apply(CraftingInput input) {
        if (input.width() != 3 || input.height() != 3) {
            return ItemStack.EMPTY;
        }
        for (int i = 0; i < 9; i++) {
            ItemStack slot = input.getItem(i);
            if (i == 4) {
                if (!(slot.getItem() instanceof BackpackItem item) || item.big() || item.kind() == BackpackItem.Kind.ENDER) {
                    return ItemStack.EMPTY;
                }
            } else if (!slot.is(Backpack.TANNED_LEATHER.get())) {
                return ItemStack.EMPTY;
            }
        }
        ItemStack backpack = input.getItem(4);
        BackpackItem item = (BackpackItem) backpack.getItem();
        return backpack.transmuteCopy(Backpack.backpack(item.kind(), item.color(), true), 1);
    }

    @Override
    public CraftingBookCategory category() {
        return CraftingBookCategory.EQUIPMENT;
    }

    @Override
    public RecipeSerializer<EnhanceBackpackRecipe> getSerializer() {
        return SERIALIZER;
    }
}
