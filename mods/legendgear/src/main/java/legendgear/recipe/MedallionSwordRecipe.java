package legendgear.recipe;

import com.mojang.serialization.MapCodec;
import legendgear.item.MedallionItem;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

/**
 * MedallionSwordRecipe: one charged medallion and one sword that is enchantable, damageable and not yet augmented give
 * the sword with the medallion as its augment (custom data "medallion" = the medallion's item id, the lore line
 * "Augment: ..."). Blocking with it charges the augment's ability (LGEvents.swordTick).
 */
public class MedallionSwordRecipe extends CustomRecipe {
    public static final MedallionSwordRecipe INSTANCE = new MedallionSwordRecipe();
    public static final RecipeSerializer<MedallionSwordRecipe> SERIALIZER = new RecipeSerializer<>(MapCodec.unit(INSTANCE), StreamCodec.unit(INSTANCE));

    public static @Nullable String augment(ItemStack sword) {
        CustomData data = sword.get(DataComponents.CUSTOM_DATA);
        return data == null ? null : data.copyTag().getString("medallion").orElse(null);
    }

    private static boolean sword(ItemStack s) {
        return s.is(ItemTags.SWORDS) && augment(s) == null && s.isDamageableItem() && s.has(DataComponents.ENCHANTABLE);
    }

    private static ItemStack[] parts(CraftingInput input) {
        ItemStack medallion = null;
        ItemStack sword = null;
        for (int i = 0; i < input.size(); i++) {
            ItemStack s = input.getItem(i);
            if (s.isEmpty()) {
                continue;
            }
            if (s.getItem() instanceof MedallionItem && s.getDamageValue() == 0 && medallion == null) {
                medallion = s;
            } else if (sword(s) && sword == null) {
                sword = s;
            } else {
                return null;
            }
        }
        return medallion == null || sword == null ? null : new ItemStack[]{medallion, sword};
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        return parts(input) != null;
    }

    @Override
    public ItemStack assemble(CraftingInput input) {
        ItemStack[] parts = parts(input);
        if (parts == null) {
            return ItemStack.EMPTY;
        }
        ItemStack result = parts[1].copyWithCount(1);
        CustomData data = result.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = data.copyTag();
        tag.putString("medallion", BuiltInRegistries.ITEM.getKey(parts[0].getItem()).toString());
        result.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        ItemLore lore = result.getOrDefault(DataComponents.LORE, ItemLore.EMPTY);
        result.set(DataComponents.LORE, lore.withLineAdded(Component.translatable("item.legendgear.augment", parts[0].getHoverName())
                .withStyle(ChatFormatting.GOLD)));
        return result;
    }

    @Override
    public CraftingBookCategory category() {
        return CraftingBookCategory.EQUIPMENT;
    }

    @Override
    public RecipeSerializer<MedallionSwordRecipe> getSerializer() {
        return SERIALIZER;
    }
}
