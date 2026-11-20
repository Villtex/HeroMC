package xreliquary.item;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.Nullable;
import xreliquary.XRData;

import java.util.Set;

/**
 * ItemMidasTouchstone: four charges per glowstone dust (257); every 5 ticks it repairs one point of each damaged gold
 * armour piece worn and each gold sword or tool in the main inventory, one charge each.
 */
public class MidasTouchstoneItem extends ChargeItem {
    private static final Set<Item> GOLD = Set.of(Items.GOLDEN_HELMET, Items.GOLDEN_CHESTPLATE, Items.GOLDEN_LEGGINGS, Items.GOLDEN_BOOTS,
            Items.GOLDEN_SWORD, Items.GOLDEN_PICKAXE, Items.GOLDEN_AXE, Items.GOLDEN_SHOVEL, Items.GOLDEN_HOE);

    public MidasTouchstoneItem(Properties properties) {
        super(properties, 2);
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity owner, @Nullable EquipmentSlot slot) {
        if (!(owner instanceof Player player)) {
            return;
        }
        refuel(stack, player, Items.GLOWSTONE_DUST, 4, 251);
        int cooldown = XRData.getShort(stack, "cooldown");
        if (cooldown == 0) {
            for (EquipmentSlot armor : new EquipmentSlot[]{EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD}) {
                this.repair(stack, player.getItemBySlot(armor));
            }
            for (int s = 0; s < 36; s++) {
                this.repair(stack, player.getInventory().getItem(s));
            }
            XRData.setShort(stack, "cooldown", 4);
        } else {
            XRData.setShort(stack, "cooldown", cooldown - 1);
        }
    }

    private void repair(ItemStack touchstone, ItemStack item) {
        if (!item.isEmpty() && GOLD.contains(item.getItem()) && item.getDamageValue() > 0 && this.use(touchstone)) {
            item.setDamageValue(item.getDamageValue() - 1);
        }
    }

    /** decrementTouchStoneCharge. */
    private boolean use(ItemStack stack) {
        int damage = stack.getDamageValue();
        if (damage != 0 && damage < stack.getMaxDamage() - 1) {
            stack.setDamageValue(damage + 1);
            return true;
        }
        return false;
    }
}
