package atomicstryker.infernalmobs.common.mods;

import atomicstryker.infernalmobs.common.MobModifier;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.core.component.DataComponents;

/**
 * MM_Rust of Infernal Mobs 1.3.1, ported 1:1.
 * InventoryPlayer.damageArmor(damage * 3): a quarter of it (at least 1) on every worn armour piece.
 */
public class MM_Rust extends MobModifier {
    private static final String[] suffix = {" of Decay", " the Equipment Haunter"};
    private static final String[] prefix = {" rusting ", " decaying "};

    public MM_Rust(LivingEntity mob, MobModifier prevMod) {
        this.modName = "Rust";
        this.nextMod = prevMod;
    }

    @Override
    public float onHurt(LivingEntity mob, DamageSource source, float damage) {
        if (source.getEntity() instanceof Player p) {
            ItemStack current = p.getMainHandItem();
            if (!current.isEmpty()) {
                current.hurtAndBreak(4, p, EquipmentSlot.MAINHAND);
            }
        }
        return super.onHurt(mob, source, damage);
    }

    @Override
    public float onAttack(LivingEntity entity, DamageSource source, float damage) {
        if (entity instanceof Player p) {
            int amount = Math.max((int) (damage * 3) / 4, 1);
            for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD}) {
                ItemStack armor = p.getItemBySlot(slot);
                Equippable equippable = armor.get(DataComponents.EQUIPPABLE);
                if (!armor.isEmpty() && armor.isDamageableItem() && equippable != null && equippable.slot() == slot) {
                    armor.hurtAndBreak(amount, p, slot);
                }
            }
        }
        return super.onAttack(entity, source, damage);
    }

    @Override
    protected String[] getModNameSuffix() {
        return suffix;
    }

    @Override
    protected String[] getModNamePrefix() {
        return prefix;
    }
}
