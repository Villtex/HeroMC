package bibliocraft.reading;

import bibliocraft.BiblioCraft;
import java.util.List;
import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.core.component.DataComponents;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;

/**
 * ItemReadingGlasses (reading glasses, tinted glasses, monocle: metadata 0..2 of one item) and EnchantmentReading.
 * A player wearing glasses, or a helmet with Reading, sees the names of the items on shelves, cases and racks
 * (HighlightHandler); {@link #canRead(Player)} is that test.
 */
public final class Reading {
    public static final ResourceKey<Enchantment> READING = ResourceKey.create(Registries.ENCHANTMENT, BiblioCraft.id("reading"));
    /** biblioArmorEnum: durability 5, reduction 1, enchantability 1; the glasses have no durability (setMaxDamage(0)). */
    private static final ArmorMaterial GLASSES = material("glasses");

    public static final DeferredItem<Item> READING_GLASSES = glasses("reading_glasses", "glasses");
    public static final DeferredItem<Item> TINTED_GLASSES = glasses("tinted_glasses", "glasses_darktint");
    public static final DeferredItem<Item> MONOCLE = glasses("monocle", "glasses_mono");
    public static final List<DeferredItem<Item>> ALL = List.of(READING_GLASSES, TINTED_GLASSES, MONOCLE);

    private Reading() {
    }

    public static void init(IEventBus modBus) {
        // the items register themselves through the static fields
    }

    private static ArmorMaterial material(String asset) {
        return new ArmorMaterial(5, Map.of(ArmorType.HELMET, 1, ArmorType.CHESTPLATE, 1, ArmorType.LEGGINGS, 1, ArmorType.BOOTS, 1), 1,
                SoundEvents.ARMOR_EQUIP_GENERIC, 0.0F, 0.0F, ItemTags.REPAIRS_LEATHER_ARMOR,
                ResourceKey.create(EquipmentAssets.ROOT_ID, BiblioCraft.id(asset)));
    }

    /** getArmorTexture: glasses.png, glasses_darktint.png, glasses_mono.png. */
    private static DeferredItem<Item> glasses(String name, String asset) {
        ResourceKey<EquipmentAsset> key = ResourceKey.create(EquipmentAssets.ROOT_ID, BiblioCraft.id(asset));
        ItemAttributeModifiers attributes = GLASSES.createAttributes(ArmorType.HELMET);
        return BiblioCraft.ITEMS.registerItem(name, Item::new, p -> p.stacksTo(1).attributes(attributes).enchantable(1)
                .component(DataComponents.EQUIPPABLE, Equippable.builder(EquipmentSlot.HEAD).setEquipSound(SoundEvents.ARMOR_EQUIP_GENERIC)
                        .setAsset(key).build()));
    }

    public static boolean isGlasses(ItemStack stack) {
        return ALL.stream().anyMatch(g -> stack.is(g.get()));
    }

    /** HighlightHandler "playerready": glasses on the head, or a head item with the Reading enchantment. */
    public static boolean canRead(Player player) {
        ItemStack head = player.getItemBySlot(EquipmentSlot.HEAD);
        if (head.isEmpty()) {
            return false;
        }
        if (isGlasses(head)) {
            return true;
        }
        ItemEnchantments enchantments = head.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        for (Holder<Enchantment> e : enchantments.keySet()) {
            if (e.is(READING)) {
                return true;
            }
        }
        return false;
    }
}
