package harkenscythe.item;

import harkenscythe.HarkenScythe;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * SpecialTierAbilities: taking in souls repairs livingmetal gear, taking in blood repairs biomass gear, in the main
 * inventory (tag {@code livingmetal_equipment} / {@code biomass_equipment}: tools, weapons, armour and the shields of
 * the Asgard Shield addon) and in the armour slots (tag {@code livingmetal_armor} / {@code biomass_armor}). The giant
 * swords of the addon were listed under a misspelt name and never repaired.
 */
public final class SpecialTierAbilities {
    private static final EquipmentSlot[] ARMOR = {EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD};

    private SpecialTierAbilities() {
    }

    /** sTALivingmetal. */
    public static void livingmetal(Player player, int amount) {
        repair(player, amount, HarkenScythe.LIVINGMETAL_EQUIPMENT, HarkenScythe.LIVINGMETAL_ARMOR);
    }

    /** sTABiomass. */
    public static void biomass(Player player, int amount) {
        repair(player, amount, HarkenScythe.BIOMASS_EQUIPMENT, HarkenScythe.BIOMASS_ARMOR);
    }

    private static void repair(Player player, int amount, TagKey<Item> equipment, TagKey<Item> armor) {
        for (ItemStack stack : player.getInventory().getNonEquipmentItems()) {
            if (stack.is(equipment)) {
                repair(stack, amount);
            }
        }
        for (EquipmentSlot slot : ARMOR) {
            ItemStack stack = player.getItemBySlot(slot);
            if (stack.is(armor)) {
                repair(stack, amount);
            }
        }
    }

    private static void repair(ItemStack stack, int amount) {
        if (stack.isDamageableItem()) {
            stack.setDamageValue(Math.max(0, stack.getDamageValue() - amount));
        }
    }
}
