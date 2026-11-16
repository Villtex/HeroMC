package harkenscythe.recipe;

import com.mojang.serialization.MapCodec;
import harkenscythe.HSItems;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * RecipesHSArmorDye: a piece of leather, soulweave or bloodweave armour with one or more spectral dyes anywhere in the
 * grid becomes the spectral piece. As in 1.5.2 the new piece keeps only the damage (no enchantments, no colour).
 */
public class SpectralDyeRecipe extends CustomRecipe {
    public static final SpectralDyeRecipe INSTANCE = new SpectralDyeRecipe();
    public static final RecipeSerializer<SpectralDyeRecipe> SERIALIZER = new RecipeSerializer<>(MapCodec.unit(INSTANCE), StreamCodec.unit(INSTANCE));

    /** customSpectralRecipesInput -> customSpectralRecipesOutput. */
    public static Map<Item, Item> pairs() {
        Map<Item, Item> map = new LinkedHashMap<>();
        Object[][] list = {
                {Items.LEATHER_HELMET, HSItems.SPECTRAL_LEATHER_CAP}, {Items.LEATHER_CHESTPLATE, HSItems.SPECTRAL_LEATHER_TUNIC},
                {Items.LEATHER_LEGGINGS, HSItems.SPECTRAL_LEATHER_PANTS}, {Items.LEATHER_BOOTS, HSItems.SPECTRAL_LEATHER_SHOES},
                {HSItems.SOULWEAVE_HOOD, HSItems.SPECTRAL_SOULWEAVE_HOOD}, {HSItems.SOULWEAVE_ROBE, HSItems.SPECTRAL_SOULWEAVE_ROBE},
                {HSItems.SOULWEAVE_PANTS, HSItems.SPECTRAL_SOULWEAVE_PANTS}, {HSItems.SOULWEAVE_SHOES, HSItems.SPECTRAL_SOULWEAVE_SHOES},
                {HSItems.BLOODWEAVE_HOOD, HSItems.SPECTRAL_BLOODWEAVE_HOOD}, {HSItems.BLOODWEAVE_ROBE, HSItems.SPECTRAL_BLOODWEAVE_ROBE},
                {HSItems.BLOODWEAVE_PANTS, HSItems.SPECTRAL_BLOODWEAVE_PANTS}, {HSItems.BLOODWEAVE_SHOES, HSItems.SPECTRAL_BLOODWEAVE_SHOES}};
        for (Object[] pair : list) {
            map.put(item(pair[0]), item(pair[1]));
        }
        return map;
    }

    private static Item item(Object o) {
        return o instanceof Supplier<?> s ? (Item) s.get() : (Item) o;
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        return !this.assemble(input).isEmpty();
    }

    @Override
    public ItemStack assemble(CraftingInput input) {
        Map<Item, Item> pairs = pairs();
        ItemStack armor = ItemStack.EMPTY;
        int dyes = 0;
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) {
                continue;
            }
            if (stack.is(HSItems.SPECTRAL_DYE.get())) {
                dyes++;
            } else if (pairs.containsKey(stack.getItem()) && armor.isEmpty()) {
                armor = stack;
            } else {
                return ItemStack.EMPTY;
            }
        }
        if (armor.isEmpty() || dyes == 0) {
            return ItemStack.EMPTY;
        }
        ItemStack result = new ItemStack(pairs.get(armor.getItem()));
        result.setDamageValue(armor.getDamageValue());
        return result;
    }

    @Override
    public CraftingBookCategory category() {
        return CraftingBookCategory.EQUIPMENT;
    }

    @Override
    public RecipeSerializer<SpectralDyeRecipe> getSerializer() {
        return SERIALIZER;
    }
}
