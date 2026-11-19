package betterstorage.recipe;

import betterstorage.BS;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

/**
 * ComboRecipe: a shaped pattern that can sit anywhere in the grid (mirrored too if {@link #canMirror}); every other
 * item in the grid is a "shapeless" extra that {@link #checkShapelessItems} has to accept. The pattern compares items
 * only (no damage values: the key's id was a wildcard). Keys used in the grid stay there (ItemKey was its own
 * container item) unless {@link #consumesKey} says otherwise (CraftingHandler of the original).
 */
public abstract class ComboRecipe extends CustomRecipe {
    protected final List<String> pattern;
    protected final Map<String, Item> key;
    protected final int width;
    protected final int height;
    private final @Nullable Item[] items;

    protected ComboRecipe(List<String> pattern, Map<String, Item> key) {
        this.pattern = List.copyOf(pattern);
        this.key = Map.copyOf(key);
        this.height = pattern.size();
        this.width = pattern.stream().mapToInt(String::length).max().orElse(0);
        this.items = new Item[this.width * this.height];
        for (int y = 0; y < this.height; y++) {
            String row = pattern.get(y);
            for (int x = 0; x < this.width; x++) {
                this.items[x + y * this.width] = x < row.length() ? key.get(String.valueOf(row.charAt(x))) : null;
            }
        }
    }

    protected static <R extends ComboRecipe> MapCodec<R> codec(java.util.function.BiFunction<List<String>, Map<String, Item>, R> factory) {
        return RecordCodecBuilder.mapCodec(i -> i.group(
                Codec.STRING.listOf(1, 3).fieldOf("pattern").forGetter(r -> r.pattern),
                Codec.unboundedMap(Codec.string(1, 1), BuiltInRegistries.ITEM.byNameCodec()).fieldOf("key").forGetter(r -> r.key))
                .apply(i, factory));
    }

    protected static <R extends ComboRecipe> StreamCodec<RegistryFriendlyByteBuf, R> streamCodec(MapCodec<R> codec) {
        return ByteBufCodecs.fromCodecWithRegistries(codec.codec());
    }

    public List<String> pattern() {
        return this.pattern;
    }

    public Map<String, Item> key() {
        return this.key;
    }

    public int getRecipeSize() {
        return this.width * this.height;
    }

    public boolean canMirror() {
        return false;
    }

    public boolean consumesKey() {
        return false;
    }

    /** The other items of the grid, in the order of the original (column by column). */
    public abstract boolean checkShapelessItems(CraftingInput input, List<ItemStack> shapeless);

    @Override
    public boolean matches(CraftingInput input, Level level) {
        // the input is cut down to the items' bounding box; the pattern can not start before it (its first row and
        // column hold items), so trying all offsets inside a 3 x 3 grid is the same as on the real grid
        if (input.width() > 3 || input.height() > 3 || input.isEmpty()) {
            return false;
        }
        for (int x = 0; x <= 3 - this.width; x++) {
            for (int y = 0; y <= 3 - this.height; y++) {
                if (this.checkMatch(input, x, y, false) || this.canMirror() && this.checkMatch(input, x, y, true)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static ItemStack at(CraftingInput input, int x, int y) {
        return x < input.width() && y < input.height() ? input.getItem(x, y) : ItemStack.EMPTY;
    }

    private boolean checkMatch(CraftingInput input, int startX, int startY, boolean mirror) {
        List<ItemStack> shapeless = new ArrayList<>();
        for (int x = 0; x < 3; x++) {
            for (int y = 0; y < 3; y++) {
                int rx = x - startX;
                int ry = y - startY;
                Item wanted = null;
                if (rx >= 0 && ry >= 0 && rx < this.width && ry < this.height) {
                    wanted = this.items[(mirror ? this.width - rx - 1 : rx) + ry * this.width];
                }
                ItemStack stack = at(input, x, y);
                if (stack.isEmpty() && wanted == null) {
                    continue;
                }
                if (stack.isEmpty()) {
                    return false;
                }
                if (wanted == null) {
                    shapeless.add(stack);
                } else if (!stack.is(wanted)) {
                    return false;
                }
            }
        }
        return shapeless.isEmpty() || this.checkShapelessItems(input, shapeless);
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
        NonNullList<ItemStack> result = NonNullList.withSize(input.size(), ItemStack.EMPTY);
        for (int i = 0; i < result.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.is(BS.KEY.get()) && !this.consumesKey()) {
                result.set(i, stack.copyWithCount(1));
            } else if (stack.getItem().getCraftingRemainder() != null) {
                result.set(i, stack.getItem().getCraftingRemainder().create());
            }
        }
        return result;
    }

    @Override
    public CraftingBookCategory category() {
        return CraftingBookCategory.MISC;
    }

    static int count(CraftingInput input, Item item) {
        int n = 0;
        for (ItemStack stack : input.items()) {
            if (stack.is(item)) {
                n++;
            }
        }
        return n;
    }

    static ItemStack find(CraftingInput input, Item item) {
        for (ItemStack stack : input.items()) {
            if (stack.is(item)) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

}
