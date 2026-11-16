package artifice.recipe;

import artifice.item.Upgrade;
import artifice.item.UpgradeItem;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.enchantment.Enchantable;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

/**
 * RecipeUpgrade: exactly one tool (stack size 1 and damageable: Item.isItemTool) and at least one upgrade, nothing
 * else. Every upgrade raises its enchantment by one level, in slot order, up to the limited level of
 * {@link Upgrade}; the craft fails if an upgrade cannot apply to the item, the item has no enchantability, the level
 * is already at the limit, or a new enchantment clashes with one the item has. The sharpening kit gives efficiency
 * where sharpness does not apply.
 */
public class UpgradeRecipe extends CustomRecipe {
    public static final UpgradeRecipe INSTANCE = new UpgradeRecipe();
    public static final RecipeSerializer<UpgradeRecipe> SERIALIZER = new RecipeSerializer<>(MapCodec.unit(INSTANCE), StreamCodec.unit(INSTANCE));

    /** The registries of the last {@link #matches} (assemble has none; recipes only run on the server). */
    private static volatile HolderLookup.@Nullable Provider registries;

    private static boolean isTool(ItemStack stack) {
        return stack.getMaxStackSize() == 1 && stack.isDamageableItem();
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        registries = level.registryAccess();
        return !apply(input, level.registryAccess()).isEmpty();
    }

    @Override
    public ItemStack assemble(CraftingInput input) {
        HolderLookup.Provider lookup = registries;
        return lookup == null ? ItemStack.EMPTY : apply(input, lookup);
    }

    /** The upgraded tool, or empty if the grid is no valid upgrade. */
    public static ItemStack apply(CraftingInput input, HolderLookup.Provider lookup) {
        ItemStack output = ItemStack.EMPTY;
        int tools = 0;
        int upgrades = 0;
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) {
                continue;
            }
            boolean upgrade = stack.getItem() instanceof UpgradeItem;
            if (!isTool(stack) && !upgrade) {
                return ItemStack.EMPTY;
            }
            if (isTool(stack)) {
                tools++;
                output = stack.copy();
            } else {
                upgrades++;
            }
        }
        if (tools != 1 || upgrades == 0) {
            return ItemStack.EMPTY;
        }
        HolderLookup.RegistryLookup<Enchantment> enchantments = lookup.lookupOrThrow(Registries.ENCHANTMENT);
        for (int i = 0; i < input.size(); i++) {
            if (!(input.getItem(i).getItem() instanceof UpgradeItem item)) {
                continue;
            }
            Upgrade upgrade = item.upgrade();
            Holder<Enchantment> enchant = enchantments.getOrThrow(upgrade.enchantment);
            if (upgrade == Upgrade.SHARPENING_KIT && !enchant.value().canEnchant(output)) {
                enchant = enchantments.getOrThrow(Enchantments.EFFICIENCY);
                if (!enchant.value().canEnchant(output)) {
                    return ItemStack.EMPTY;
                }
            }
            Enchantable enchantable = output.get(DataComponents.ENCHANTABLE);
            if (!enchant.value().canEnchant(output) || enchantable == null || enchantable.value() <= 0) {
                return ItemStack.EMPTY;
            }
            int level = EnchantmentHelper.getItemEnchantmentLevel(enchant, output);
            if (level >= upgrade.maxLevel) {
                return ItemStack.EMPTY;
            }
            if (level == 0) {
                ItemEnchantments present = output.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
                for (Holder<Enchantment> other : present.keySet()) {
                    if (!Enchantment.areCompatible(enchant, other)) {
                        return ItemStack.EMPTY;
                    }
                }
            }
            Holder<Enchantment> chosen = enchant;
            EnchantmentHelper.updateEnchantments(output, m -> m.set(chosen, level + 1));
        }
        return output;
    }

    @Override
    public CraftingBookCategory category() {
        return CraftingBookCategory.EQUIPMENT;
    }

    @Override
    public RecipeSerializer<UpgradeRecipe> getSerializer() {
        return SERIALIZER;
    }
}
