package harkenscythe.item;

import harkenscythe.HSItems;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.function.Supplier;

/** ItemHSSoulweaveArmor.soulweaveArmorCheck / ItemHSBloodweaveArmor.bloodweaveArmorCheck: worn pieces, spectral ones included. */
public final class WeaveArmor {
    private static final EquipmentSlot[] SLOTS = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
    private static final List<Supplier<? extends Item>> SOULWEAVE = List.of(HSItems.SOULWEAVE_HOOD, HSItems.SOULWEAVE_ROBE, HSItems.SOULWEAVE_PANTS,
            HSItems.SOULWEAVE_SHOES, HSItems.SPECTRAL_SOULWEAVE_HOOD, HSItems.SPECTRAL_SOULWEAVE_ROBE, HSItems.SPECTRAL_SOULWEAVE_PANTS,
            HSItems.SPECTRAL_SOULWEAVE_SHOES);
    private static final List<Supplier<? extends Item>> BLOODWEAVE = List.of(HSItems.BLOODWEAVE_HOOD, HSItems.BLOODWEAVE_ROBE,
            HSItems.BLOODWEAVE_PANTS, HSItems.BLOODWEAVE_SHOES, HSItems.SPECTRAL_BLOODWEAVE_HOOD, HSItems.SPECTRAL_BLOODWEAVE_ROBE,
            HSItems.SPECTRAL_BLOODWEAVE_PANTS, HSItems.SPECTRAL_BLOODWEAVE_SHOES);

    private WeaveArmor() {
    }

    public static int count(Player player, boolean blood) {
        int count = 0;
        for (EquipmentSlot slot : SLOTS) {
            ItemStack stack = player.getItemBySlot(slot);
            for (Supplier<? extends Item> item : blood ? BLOODWEAVE : SOULWEAVE) {
                if (stack.is(item.get())) {
                    count++;
                }
            }
        }
        return count;
    }
}
