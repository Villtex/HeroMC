package betterstorage.recipe;

import betterstorage.BS;
import betterstorage.item.KeyItem;
import com.mojang.serialization.MapCodec;
import java.util.List;
import java.util.Map;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

/**
 * KeyRecipe. Two recipes of the mod use it:
 * <ul>
 * <li>a new key ({@code .o / .o /  o}, nuggets and gold ingots, mirrored too); an extra key in the grid gives the new
 * key the same id (and stays in the grid), an iron ingot plates it, dyes colour it;</li>
 * <li>a single key ({@code k}) with an iron ingot and/or dyes: the key itself is changed (and used up).</li>
 * </ul>
 * At most one key and one iron ingot; a key that is already iron plated can not be plated again. The colour is the
 * dye mix of 1.5.2 with its fleece colour table.
 */
public class KeyRecipe extends ComboRecipe {
    public static final MapCodec<KeyRecipe> CODEC = codec(KeyRecipe::new);
    public static final StreamCodec<RegistryFriendlyByteBuf, KeyRecipe> STREAM_CODEC = streamCodec(CODEC);
    public static final RecipeSerializer<KeyRecipe> SERIALIZER = new RecipeSerializer<>(CODEC, STREAM_CODEC);

    /** EntitySheep.fleeceColorTable of 1.5.2, by wool colour (DyeColor id). */
    private static final float[][] FLEECE_COLORS = {
            {1.0F, 1.0F, 1.0F}, {0.85F, 0.5F, 0.2F}, {0.7F, 0.3F, 0.85F}, {0.4F, 0.6F, 0.85F},
            {0.9F, 0.9F, 0.2F}, {0.5F, 0.8F, 0.1F}, {0.95F, 0.5F, 0.65F}, {0.3F, 0.3F, 0.3F},
            {0.6F, 0.6F, 0.6F}, {0.3F, 0.5F, 0.6F}, {0.5F, 0.25F, 0.7F}, {0.2F, 0.3F, 0.7F},
            {0.4F, 0.3F, 0.2F}, {0.4F, 0.5F, 0.2F}, {0.6F, 0.2F, 0.2F}, {0.1F, 0.1F, 0.1F}};

    public KeyRecipe(List<String> pattern, Map<String, Item> key) {
        super(pattern, key);
    }

    private boolean modifiesKey() {
        return this.getRecipeSize() == 1;
    }

    @Override
    public boolean canMirror() {
        return this.width > 1;
    }

    /** CraftingHandler: without a gold ingot (the modifying recipe) the key is used up. */
    @Override
    public boolean consumesKey() {
        return this.modifiesKey();
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        return super.matches(input, level) && input.ingredientCount() > 1;
    }

    static boolean isDye(ItemStack stack) {
        return stack.has(DataComponents.DYE) && stack.is(net.minecraft.tags.ItemTags.DYES);
    }

    @Override
    public boolean checkShapelessItems(CraftingInput input, List<ItemStack> shapeless) {
        if (count(input, BS.KEY.get()) > 1) {
            return false;
        }
        ItemStack key = find(input, BS.KEY.get());
        int iron = count(input, Items.IRON_INGOT);
        if (iron > 1) {
            return false;
        }
        if (this.modifiesKey() && iron > 0 && key.has(BS.IRON_PLATED.get())) {
            return false;
        }
        for (ItemStack stack : shapeless) {
            if (!stack.is(BS.KEY.get()) && !stack.is(Items.IRON_INGOT) && !isDye(stack)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack assemble(CraftingInput input) {
        ItemStack key = find(input, BS.KEY.get());
        ItemStack result = this.modifiesKey() && !key.isEmpty() ? key.copyWithCount(1) : new ItemStack(BS.KEY.get());
        if (!key.isEmpty() && KeyItem.keyId(key) != 0) {
            result.set(BS.KEY_ID.get(), KeyItem.keyId(key));
        }
        if (count(input, Items.IRON_INGOT) > 0) {
            result.set(BS.IRON_PLATED.get(), net.minecraft.util.Unit.INSTANCE);
        }
        float r = 0.0F;
        float g = 0.0F;
        float b = 0.0F;
        int[] colors = new int[16];
        int amount = 0;
        for (ItemStack stack : input.items()) {
            if (!isDye(stack)) {
                continue;
            }
            int index = stack.getOrDefault(DataComponents.DYE, DyeColor.WHITE).getId();
            int intensity = colors[index];
            float[] color = FLEECE_COLORS[index];
            float f = (float) Math.pow(2.0, intensity);
            r += color[0] / f;
            g += color[1] / f;
            b += color[2] / f;
            colors[index]++;
            if (intensity <= 0) {
                amount++;
            }
        }
        if (amount > 0) {
            int rr = (int) (Math.min(1.0F, r / amount) * 255.0F);
            int gg = (int) (Math.min(1.0F, g / amount) * 255.0F);
            int bb = (int) (Math.min(1.0F, b / amount) * 255.0F);
            result.set(DataComponents.DYED_COLOR, new DyedItemColor(rr << 16 | gg << 8 | bb));
        }
        return result;
    }

    @Override
    public RecipeSerializer<KeyRecipe> getSerializer() {
        return SERIALIZER;
    }
}
