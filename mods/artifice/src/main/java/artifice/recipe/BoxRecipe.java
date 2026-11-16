package artifice.recipe;

import artifice.ArtificeItems;
import artifice.ArtificeRecipes;
import artifice.item.BoxItem;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

/**
 * RecipeBox and BoxCraftingHandler. If the first box in the grid is empty, every other stack must be the same item
 * (with the same components) and the result is one box holding one item of each of those stacks (so up to 8). If it
 * holds something and is the only stack in the grid, the result is its contents, at most a full stack; the rest stays
 * in a box that goes back to the player (BoxCraftingHandler). Shift-clicking the result unpacks the boxes of the slot
 * one after the other. (1.5.2 took one box too many per unpacking when the slot held several boxes; not ported.)
 */
public class BoxRecipe extends CustomRecipe {
    public static final BoxRecipe INSTANCE = new BoxRecipe();
    public static final RecipeSerializer<BoxRecipe> SERIALIZER = new RecipeSerializer<>(MapCodec.unit(INSTANCE), StreamCodec.unit(INSTANCE));

    private static boolean isBox(ItemStack stack) {
        return stack.is(ArtificeItems.BOX.get());
    }

    /** The first box of the grid (slot order), or empty. */
    private static ItemStack firstBox(CraftingInput input) {
        for (int i = 0; i < input.size(); i++) {
            if (isBox(input.getItem(i))) {
                return input.getItem(i);
            }
        }
        return ItemStack.EMPTY;
    }

    /** The slot of the filled box when unpacking, or -1. */
    private static int unpackSlot(CraftingInput input) {
        ItemStack first = firstBox(input);
        if (first.isEmpty() || BoxItem.contents(first) == null) {
            return -1;
        }
        int slot = -1;
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) {
                continue;
            }
            if (!isBox(stack) || slot >= 0) {
                return -1;
            }
            slot = i;
        }
        return slot;
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        return !this.assemble(input).isEmpty();
    }

    @Override
    public ItemStack assemble(CraftingInput input) {
        ItemStack first = firstBox(input);
        if (first.isEmpty()) {
            return ItemStack.EMPTY;
        }
        int slot = unpackSlot(input);
        if (slot >= 0) {
            ItemStackTemplate contents = BoxItem.contents(input.getItem(slot));
            ItemStack thing = contents.create();
            thing.setCount(Math.min(contents.count(), thing.getMaxStackSize()));
            return thing;
        }
        if (BoxItem.contents(first) != null) {
            return ItemStack.EMPTY;
        }
        // packing: every stack that is not a box must equal the first of them
        ItemStack thing = ItemStack.EMPTY;
        int num = 0;
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty() || isBox(stack)) {
                continue;
            }
            if (thing.isEmpty()) {
                thing = stack;
            } else if (!ItemStack.isSameItemSameComponents(thing, stack)) {
                return ItemStack.EMPTY;
            }
            num++;
        }
        return thing.isEmpty() ? ItemStack.EMPTY : BoxItem.packed(ArtificeItems.BOX.get(), thing, num);
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
        NonNullList<ItemStack> remaining = CraftingRecipe.defaultCraftingReminder(input);
        int slot = unpackSlot(input);
        if (slot >= 0) {
            // BoxCraftingHandler: a box that held more than a stack keeps the rest
            ItemStack box = input.getItem(slot);
            ItemStackTemplate contents = BoxItem.contents(box);
            int max = contents.create().getMaxStackSize();
            if (contents.count() > max) {
                ItemStack rest = box.copyWithCount(1);
                rest.set(ArtificeRecipes.BOX_CONTENTS.get(), contents.withCount(contents.count() - max));
                remaining.set(slot, rest);
            }
        }
        return remaining;
    }

    @Override
    public RecipeSerializer<BoxRecipe> getSerializer() {
        return SERIALIZER;
    }
}
