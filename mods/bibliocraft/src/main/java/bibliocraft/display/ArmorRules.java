package bibliocraft.display;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.level.block.AbstractSkullBlock;

/**
 * The armor tests of the armor stand. "ItemArmor with armorType t" of 1.5.2 is an item (no block item, so no carved
 * pumpkin or head) that equips into a humanoid armor slot; armorType 0 helmet, 1 chestplate, 2 leggings, 3 boots is
 * also the stand's slot number. ItemSkull is a mob head.
 */
public final class ArmorRules {
    /** Stand slot / armorType -> equipment slot. */
    public static final EquipmentSlot[] SLOTS = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

    private ArmorRules() {
    }

    /** ItemArmor.armorType, or -1 for anything that is no ItemArmor. */
    public static int armorType(ItemStack stack) {
        if (stack.isEmpty() || stack.getItem() instanceof BlockItem) {
            return -1;
        }
        Equippable equippable = stack.get(DataComponents.EQUIPPABLE);
        if (equippable == null) {
            return -1;
        }
        for (int t = 0; t < 4; t++) {
            if (equippable.slot() == SLOTS[t]) {
                return t;
            }
        }
        return -1;
    }

    /** instanceof ItemSkull. */
    public static boolean isSkull(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof BlockItem bi && bi.getBlock() instanceof AbstractSkullBlock;
    }

    /** SlotArmorHelm/Cuirass/Greaves/Boots.isItemValid: armor of that type; the helmet slot also takes heads. */
    public static boolean fitsSlot(ItemStack stack, int slot) {
        return armorType(stack) == slot || (slot == 0 && isSkull(stack));
    }
}
