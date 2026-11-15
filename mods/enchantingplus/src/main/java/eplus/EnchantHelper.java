package eplus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.IntUnaryOperator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantable;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;

/**
 * EnchantHelper, EnchantmentHelp and the item rules of 1.5.2 (Item.isItemTool, getItemEnchantability) on top of the
 * 26.3 data-driven enchantments.
 */
public final class EnchantHelper {
    private EnchantHelper() {
    }

    /** The enchantments the table offers, in the order of their 1.5.2 effect ids (the old enchantmentsList). */
    public static final TagKey<Enchantment> TABLE = TagKey.create(Registries.ENCHANTMENT, Identifier.fromNamespaceAndPath(EnchantingPlus.MOD_ID, "table"));
    /** Items the table ignores (IMC "blacklist-item" of 1.5.2). */
    public static final TagKey<Item> BLACKLIST = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(EnchantingPlus.MOD_ID, "blacklist"));

    /** getMinEnchantability / getMaxEnchantability of the 22 enchantments of 1.5.2; the cost formulas use them. */
    private record OldCost(IntUnaryOperator min, IntUnaryOperator max) {
    }

    private static final Map<ResourceKey<Enchantment>, OldCost> OLD_COSTS = new HashMap<>();

    /** Enchantment.getMinEnchantability of 1.5.2, used by the subclasses that call super. */
    private static int baseMin(int level) {
        return 1 + level * 10;
    }

    private static void protection(ResourceKey<Enchantment> key, int base, int perLevel, int threshold) {
        OLD_COSTS.put(key, new OldCost(l -> base + (l - 1) * perLevel, l -> base + (l - 1) * perLevel + threshold));
    }

    static {
        // EnchantmentProtection: base, level and threshold enchantability per protection type
        protection(Enchantments.PROTECTION, 1, 11, 20);
        protection(Enchantments.FIRE_PROTECTION, 10, 8, 12);
        protection(Enchantments.FEATHER_FALLING, 5, 6, 10);
        protection(Enchantments.BLAST_PROTECTION, 5, 8, 12);
        protection(Enchantments.PROJECTILE_PROTECTION, 3, 6, 15);
        OLD_COSTS.put(Enchantments.RESPIRATION, new OldCost(l -> 10 * l, l -> 10 * l + 30));
        OLD_COSTS.put(Enchantments.AQUA_AFFINITY, new OldCost(l -> 1, l -> 41));
        OLD_COSTS.put(Enchantments.THORNS, new OldCost(l -> 10 + 20 * (l - 1), l -> baseMin(l) + 50));
        // EnchantmentDamage
        protection(Enchantments.SHARPNESS, 1, 11, 20);
        protection(Enchantments.SMITE, 5, 8, 20);
        protection(Enchantments.BANE_OF_ARTHROPODS, 5, 8, 20);
        OLD_COSTS.put(Enchantments.KNOCKBACK, new OldCost(l -> 5 + 20 * (l - 1), l -> baseMin(l) + 50));
        OLD_COSTS.put(Enchantments.FIRE_ASPECT, new OldCost(l -> 10 + 20 * (l - 1), l -> baseMin(l) + 50));
        OLD_COSTS.put(Enchantments.LOOTING, new OldCost(l -> 15 + (l - 1) * 9, l -> baseMin(l) + 50));
        OLD_COSTS.put(Enchantments.EFFICIENCY, new OldCost(l -> 1 + 10 * (l - 1), l -> baseMin(l) + 50));
        OLD_COSTS.put(Enchantments.SILK_TOUCH, new OldCost(l -> 15, l -> baseMin(l) + 50));
        OLD_COSTS.put(Enchantments.UNBREAKING, new OldCost(l -> 5 + (l - 1) * 8, l -> baseMin(l) + 50));
        OLD_COSTS.put(Enchantments.FORTUNE, new OldCost(l -> 15 + (l - 1) * 9, l -> baseMin(l) + 50));
        OLD_COSTS.put(Enchantments.POWER, new OldCost(l -> 1 + (l - 1) * 10, l -> 1 + (l - 1) * 10 + 15));
        OLD_COSTS.put(Enchantments.PUNCH, new OldCost(l -> 12 + (l - 1) * 20, l -> 12 + (l - 1) * 20 + 25));
        OLD_COSTS.put(Enchantments.FLAME, new OldCost(l -> 20, l -> 50));
        OLD_COSTS.put(Enchantments.INFINITY, new OldCost(l -> 20, l -> 50));
    }

    /** getMinEnchantability(level): the 1.5.2 value for the old enchantments, the data value (min_cost) for all others. */
    public static int minEnchantability(Holder<Enchantment> enchantment, int level) {
        OldCost old = enchantment.unwrapKey().map(OLD_COSTS::get).orElse(null);
        return old != null ? old.min().applyAsInt(level) : enchantment.value().getMinCost(level);
    }

    public static int maxEnchantability(Holder<Enchantment> enchantment, int level) {
        OldCost old = enchantment.unwrapKey().map(OLD_COSTS::get).orElse(null);
        return old != null ? old.max().applyAsInt(level) : enchantment.value().getMaxCost(level);
    }

    /** Item.getItemEnchantability: tools 5..22, armour 9..25, bow and book 1, everything else 0. */
    public static int enchantability(ItemStack stack) {
        Enchantable enchantable = stack.get(DataComponents.ENCHANTABLE);
        return enchantable == null ? 0 : enchantable.value();
    }

    /** ItemStack.isItemEnchanted of 1.5.2: the item (not a book) carries enchantments. */
    public static boolean isEnchanted(ItemStack stack) {
        return !stack.getTagEnchantments().isEmpty();
    }

    /** Item.isItemTool: single item that takes damage (a book counts as well). */
    private static boolean isTool(ItemStack stack) {
        return stack.is(Items.BOOK) ? stack.getCount() == 1 : stack.getMaxStackSize() == 1 && stack.isDamageableItem();
    }

    /** EnchantHelper.isItemEnchantable: a book, or a tool that is not enchanted yet. */
    public static boolean isItemEnchantable(ItemStack stack) {
        return stack.is(Items.BOOK) || isTool(stack) && !isEnchanted(stack);
    }

    /** EnchantHelper.isItemEnchanted: the item has enchantments, an enchanted book stored ones. */
    public static boolean isItemEnchanted(ItemStack stack) {
        return stack.is(Items.ENCHANTED_BOOK) ? !EnchantmentHelper.getEnchantmentsForCrafting(stack).isEmpty() : isEnchanted(stack);
    }

    /** EnchantHelper.canEnchantItem: a plain book takes everything, an enchanted book nothing new. */
    public static boolean canEnchantItem(ItemStack stack, Holder<Enchantment> enchantment) {
        if (stack.is(Items.BOOK)) {
            return true;
        }
        return !stack.is(Items.ENCHANTED_BOOK) && stack.supportsEnchantment(enchantment);
    }

    /** SlotEnchant.isItemValid. */
    public static boolean mayPlace(ItemStack stack) {
        return isItemEnchantable(stack) || isEnchanted(stack) || stack.is(Items.BOOK) || stack.is(Items.ENCHANTED_BOOK);
    }

    public static boolean isBlackListed(Holder<Enchantment> enchantment, EplusConfig.Settings settings) {
        return enchantment.unwrapKey().map(k -> settings.disabled().contains(k.identifier().toString())).orElse(false);
    }

    /** The enchantments of the table, in order. */
    public static List<Holder<Enchantment>> tableEnchantments(HolderLookup.Provider registries) {
        List<Holder<Enchantment>> list = new ArrayList<>();
        registries.lookupOrThrow(Registries.ENCHANTMENT).get(TABLE).ifPresent(set -> set.forEach(list::add));
        return list;
    }

    /**
     * ContainerEnchantTable.readItems: the enchantments of the item with their levels, then every enchantment the
     * table offers for it at level 0 (for an enchanted item only those that go with all it has).
     */
    public static Map<Holder<Enchantment>, Integer> readItems(ItemStack stack, HolderLookup.Provider registries, EplusConfig.Settings settings) {
        Map<Holder<Enchantment>, Integer> temp = new LinkedHashMap<>();
        if (stack.isEmpty() || stack.is(BLACKLIST)) {
            return temp;
        }
        if (isItemEnchanted(stack)) {
            for (var entry : EnchantmentHelper.getEnchantmentsForCrafting(stack).entrySet()) {
                temp.put(entry.getKey(), entry.getIntValue());
            }
        }
        List<Holder<Enchantment>> all = tableEnchantments(registries);
        if (isItemEnchantable(stack)) {
            for (Holder<Enchantment> obj : all) {
                if (canEnchantItem(stack, obj) && !isBlackListed(obj, settings)) {
                    temp.put(obj, 0);
                }
            }
        } else {
            for (Holder<Enchantment> obj : all) {
                boolean add = true;
                for (Holder<Enchantment> enc : temp.keySet()) {
                    if (!Enchantment.areCompatible(obj, enc) || !Enchantment.areCompatible(enc, obj)) {
                        add = false;
                    }
                }
                if (canEnchantItem(stack, obj) && add && !isBlackListed(obj, settings)) {
                    temp.put(obj, 0);
                }
            }
        }
        return temp;
    }

    /** ForgeHooks.getEnchantPower of every block at distance 2 (both heights) with air in between, plus minimumBook, doubled. */
    public static float bookCases(Level level, BlockPos pos, int minimumBook) {
        float temp = minimumBook;
        for (int j = -1; j <= 1; j++) {
            for (int k = -1; k <= 1; k++) {
                if ((j != 0 || k != 0) && level.isEmptyBlock(pos.offset(k, 0, j)) && level.isEmptyBlock(pos.offset(k, 1, j))) {
                    temp += power(level, pos.offset(k * 2, 0, j * 2));
                    temp += power(level, pos.offset(k * 2, 1, j * 2));
                    if (k != 0 && j != 0) {
                        temp += power(level, pos.offset(k * 2, 0, j));
                        temp += power(level, pos.offset(k * 2, 1, j));
                        temp += power(level, pos.offset(k, 0, j * 2));
                        temp += power(level, pos.offset(k, 1, j * 2));
                    }
                }
            }
        }
        return temp * 2.0F;
    }

    private static float power(Level level, BlockPos pos) {
        return level.getBlockState(pos).getEnchantPowerBonus(level, pos);
    }

    /** "restrictions" NBT: the levels an enchantment reached on this table; only those can be taken back. */
    public static boolean containsKey(List<EnchantingPlus.Restriction> restrictions, Holder<Enchantment> enchantment, int level) {
        for (EnchantingPlus.Restriction restriction : restrictions) {
            if (restriction.level() == level && restriction.enchantment().equals(enchantment)) {
                return true;
            }
        }
        return false;
    }

    public static List<EnchantingPlus.Restriction> restrictions(ItemStack stack) {
        return stack.getOrDefault(EnchantingPlus.RESTRICTIONS.get(), List.of());
    }

    /**
     * EnchantHelper.setEnchantments: writes the enchantments (a book becomes an enchanted book, an enchanted book
     * without any becomes a book again) and records the levels passed on this table for the player.
     *
     * @param levels the level each changed enchantment had before
     * @return the new stack (a different item for books)
     */
    public static ItemStack setEnchantments(Map<Holder<Enchantment>, Integer> map, ItemStack stack, Map<Holder<Enchantment>, Integer> levels,
                                            String player) {
        ItemEnchantments.Mutable list = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
        List<EnchantingPlus.Restriction> restrictions = new ArrayList<>(restrictions(stack));
        for (Map.Entry<Holder<Enchantment>, Integer> entry : map.entrySet()) {
            Holder<Enchantment> enchantment = entry.getKey();
            int level = entry.getValue();
            list.set(enchantment, level);
            int startLevel = levels.getOrDefault(enchantment, level);
            for (int y = startLevel; y <= level; y++) {
                if (!containsKey(restrictions, enchantment, y)) {
                    restrictions.add(new EnchantingPlus.Restriction(enchantment, y, player));
                }
            }
        }
        ItemStack result = stack;
        if (stack.is(Items.BOOK)) {
            result = stack.transmuteCopy(Items.ENCHANTED_BOOK);
        }
        ItemEnchantments enchantments = list.toImmutable();
        if (!enchantments.isEmpty()) {
            if (result.is(Items.ENCHANTED_BOOK)) {
                result.set(DataComponents.STORED_ENCHANTMENTS, enchantments);
            } else {
                result.set(DataComponents.ENCHANTMENTS, enchantments);
            }
            result.set(EnchantingPlus.RESTRICTIONS.get(), List.copyOf(restrictions));
        } else if (result.is(Items.ENCHANTED_BOOK)) {
            result = new ItemStack(Items.BOOK, result.getCount());
        } else {
            result.set(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        }
        return result;
    }
}
