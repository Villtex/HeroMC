package xreliquary.recipe;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.NonNullList;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import xreliquary.XRItems;
import xreliquary.item.VoidContents;

/**
 * The two void satchel recipes with VoidSatchelHandler: leather around string and a void tear (lsl / ltl / lll) makes
 * a satchel with the tear's item (at most 64 of it, capacity 64; the rest spills, see XREvents); three empty void
 * tears and a satchel raise its capacity by 192 (up to 32000).
 */
public class SatchelRecipe extends CustomRecipe {
    public static final SatchelRecipe INSTANCE = new SatchelRecipe();
    public static final RecipeSerializer<SatchelRecipe> SERIALIZER = new RecipeSerializer<>(MapCodec.unit(INSTANCE), StreamCodec.unit(INSTANCE));

    private static final String[] PATTERN = {"lsl", "ltl", "lll"};

    private static boolean shaped(CraftingInput input) {
        if (input.width() != 3 || input.height() != 3) {
            return false;
        }
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                ItemStack s = input.getItem(x, y);
                Item want = switch (PATTERN[y].charAt(x)) {
                    case 'l' -> Items.LEATHER;
                    case 's' -> Items.STRING;
                    default -> XRItems.VOID_TEAR.get();
                };
                if (!s.is(want)) {
                    return false;
                }
            }
        }
        return true;
    }

    private static ItemStack upgradeSatchel(CraftingInput input) {
        int tears = 0;
        ItemStack satchel = ItemStack.EMPTY;
        for (int i = 0; i < input.size(); i++) {
            ItemStack s = input.getItem(i);
            if (s.isEmpty()) {
                continue;
            }
            if (s.is(XRItems.EMPTY_VOID_TEAR.get())) {
                tears++;
            } else if (s.is(XRItems.VOID_SATCHEL.get()) && satchel.isEmpty()) {
                satchel = s;
            } else {
                return ItemStack.EMPTY;
            }
        }
        return tears == 3 ? satchel : ItemStack.EMPTY;
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        return shaped(input) || !upgradeSatchel(input).isEmpty();
    }

    @Override
    public ItemStack assemble(CraftingInput input) {
        ItemStack result = new ItemStack(XRItems.VOID_SATCHEL.get());
        if (shaped(input)) {
            ItemStack tear = input.getItem(1, 1);
            Item item = VoidContents.item(tear);
            if (item != Items.AIR) {
                VoidContents.set(result, item, Math.min(VoidContents.quantity(tear), 64), 64);
            }
            return result;
        }
        ItemStack old = upgradeSatchel(input);
        Item item = VoidContents.item(old);
        if (item != Items.AIR) {
            int capacity = VoidContents.capacity(old);
            VoidContents.set(result, item, VoidContents.quantity(old), capacity >= 32000 ? capacity : capacity + 192);
        }
        return result;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
        return NonNullList.withSize(input.size(), ItemStack.EMPTY);
    }

    @Override
    public CraftingBookCategory category() {
        return CraftingBookCategory.MISC;
    }

    @Override
    public RecipeSerializer<SatchelRecipe> getSerializer() {
        return SERIALIZER;
    }
}
