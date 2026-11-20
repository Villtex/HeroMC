package legendgear.recipe;

import com.mojang.serialization.MapCodec;
import legendgear.LGItems;
import legendgear.item.AmmoBagItem;
import net.minecraft.core.NonNullList;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

/**
 * QuiverEmptyingRecipe (crafty loading is off in the pack, so only emptying): a quiver or bomb bag that is not empty,
 * alone in the grid, gives one stack of its arrows (64) or bombs (16) and stays in the grid with that many less.
 */
public class EmptyAmmoBagRecipe extends CustomRecipe {
    public static final EmptyAmmoBagRecipe INSTANCE = new EmptyAmmoBagRecipe();
    public static final RecipeSerializer<EmptyAmmoBagRecipe> SERIALIZER = new RecipeSerializer<>(MapCodec.unit(INSTANCE), StreamCodec.unit(INSTANCE));

    private static int bag(CraftingInput input) {
        int found = -1;
        for (int i = 0; i < input.size(); i++) {
            ItemStack s = input.getItem(i);
            if (s.isEmpty()) {
                continue;
            }
            if (!(s.getItem() instanceof AmmoBagItem) || found >= 0) {
                return -1;
            }
            found = i;
        }
        return found >= 0 && AmmoBagItem.ammo(input.getItem(found)) > 0 ? found : -1;
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        return bag(input) >= 0;
    }

    @Override
    public ItemStack assemble(CraftingInput input) {
        int i = bag(input);
        if (i < 0) {
            return ItemStack.EMPTY;
        }
        ItemStack bag = input.getItem(i);
        boolean bombs = ((AmmoBagItem) bag.getItem()).bombs();
        int limit = bombs ? 16 : 64;
        return new ItemStack(bombs ? LGItems.BOMB.get() : Items.ARROW, Math.min(AmmoBagItem.ammo(bag), limit));
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
        NonNullList<ItemStack> rest = NonNullList.withSize(input.size(), ItemStack.EMPTY);
        int i = bag(input);
        if (i >= 0) {
            ItemStack bag = input.getItem(i).copyWithCount(1);
            int limit = ((AmmoBagItem) bag.getItem()).bombs() ? 16 : 64;
            AmmoBagItem.setAmmo(bag, Math.max(AmmoBagItem.ammo(bag) - limit, 0));
            rest.set(i, bag);
        }
        return rest;
    }

    @Override
    public CraftingBookCategory category() {
        return CraftingBookCategory.EQUIPMENT;
    }

    @Override
    public RecipeSerializer<EmptyAmmoBagRecipe> getSerializer() {
        return SERIALIZER;
    }
}
