package asgardshield.guard;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

/**
 * Support for Harken Scythe ("Special Mod Support" of the config) without depending on it: its items and augments
 * are looked up by id in the namespace {@code harkenscythe_core}; whatever is missing counts as absent / level 0.
 * The augment ids of the config (206, 207, 210, 211; pack 208, 207, 218, 217) are the data-driven enchantments
 * {@code harkenscythe_core:vitality}, {@code exude}, {@code ward} and {@code sanguinary}.
 */
public final class HarkenScythe {
    public static final String MOD_ID = "harkenscythe_core";
    public static final ResourceKey<Enchantment> VITALITY = enchantment("vitality");
    public static final ResourceKey<Enchantment> EXUDE = enchantment("exude");
    public static final ResourceKey<Enchantment> WARD = enchantment("ward");
    public static final ResourceKey<Enchantment> SANGUINARY = enchantment("sanguinary");
    /** Essence a keeper or vessel holds, if Harken Scythe keeps it in a data component (else its durability is used). */
    public static final Identifier ESSENCE = id("essence");

    private HarkenScythe() {
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    private static ResourceKey<Enchantment> enchantment(String path) {
        return ResourceKey.create(Registries.ENCHANTMENT, id(path));
    }

    /** The level of an enchantment on a stack; 0 if the enchantment does not exist. */
    public static int level(Level level, ItemStack stack, ResourceKey<Enchantment> key) {
        return level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).get(key)
                .map(holder -> EnchantmentHelper.getItemEnchantmentLevel(holder, stack)).orElse(0);
    }

    public static @Nullable Item item(String path) {
        return BuiltInRegistries.ITEM.getOptional(id(path)).orElse(null);
    }

    @SuppressWarnings("unchecked")
    static @Nullable DataComponentType<Integer> essenceComponent() {
        DataComponentType<?> type = BuiltInRegistries.DATA_COMPONENT_TYPE.getValue(ESSENCE);
        return (DataComponentType<Integer>) type;
    }
}
