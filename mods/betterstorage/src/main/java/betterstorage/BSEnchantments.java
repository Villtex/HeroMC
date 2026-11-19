package betterstorage;

import java.util.function.Consumer;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.enchantment.ItemEnchantments;

/**
 * EnchantmentBetterStorage / BetterStorageEnchantment: the seven enchantments of keys and locks. They are data
 * (data/betterstorage/enchantment/*.json, written by gen.py with the weights, levels and costs of the original) and
 * have no effects of their own: keys, locks and the reinforced chest ask for their level, like getLevel did.
 */
public final class BSEnchantments {
    public static final ResourceKey<Enchantment> UNLOCKING = key("unlocking");
    public static final ResourceKey<Enchantment> LOCKPICKING = key("lockpicking");
    public static final ResourceKey<Enchantment> MORPHING = key("morphing");
    public static final ResourceKey<Enchantment> PERSISTANCE = key("persistance");
    public static final ResourceKey<Enchantment> SECURITY = key("security");
    public static final ResourceKey<Enchantment> SHOCK = key("shock");
    public static final ResourceKey<Enchantment> TRIGGER = key("trigger");
    /** Key enchantments first, then the lock ones (CreativeTabBetterStorage: type key, then type lock). */
    public static final java.util.List<ResourceKey<Enchantment>> ALL = java.util.List.of(UNLOCKING, LOCKPICKING, MORPHING, PERSISTANCE, SECURITY, SHOCK, TRIGGER);

    private BSEnchantments() {
    }

    private static ResourceKey<Enchantment> key(String name) {
        return ResourceKey.create(Registries.ENCHANTMENT, BetterStorage.id(name));
    }

    /** BetterStorageEnchantment.getLevel: 0 for an empty stack. */
    public static int level(ItemStack stack, ResourceKey<Enchantment> enchantment) {
        if (stack.isEmpty()) {
            return 0;
        }
        for (var entry : stack.getEnchantments().entrySet()) {
            if (entry.getKey().is(enchantment)) {
                return entry.getIntValue();
            }
        }
        return 0;
    }

    /** Sets the level of an enchantment already on the stack; 0 removes it (the empty "ench" list goes with it). */
    public static void setLevel(ItemStack stack, ResourceKey<Enchantment> enchantment, int level) {
        EnchantmentHelper.updateEnchantments(stack, mutable -> {
            Holder<Enchantment> holder = null;
            for (Holder<Enchantment> h : mutable.keySet()) {
                if (h.is(enchantment)) {
                    holder = h;
                }
            }
            if (holder != null) {
                mutable.set(holder, level);
            }
        });
    }

    /** CreativeTabs.func_92116_a: one enchanted book per enchantment at its highest level. */
    public static void books(HolderLookup.Provider registries, Consumer<ItemStack> output) {
        HolderLookup.RegistryLookup<Enchantment> lookup = registries.lookupOrThrow(Registries.ENCHANTMENT);
        for (ResourceKey<Enchantment> key : ALL) {
            lookup.get(key).ifPresent(holder -> output.accept(EnchantmentHelper.createBook(new EnchantmentInstance(holder, holder.value().getMaxLevel()))));
        }
    }

    static ItemEnchantments none() {
        return ItemEnchantments.EMPTY;
    }
}
