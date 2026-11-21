package twilightforest;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

/**
 * TFEnchantment / EnchantmentTFFireReact "Fiery Aura" (id 206 of the pack, weight 2, armour; data file
 * enchantment/fiery_aura.json). TFEventListener.entityHurts: a player hit by a mob sets it on fire for level / 2
 * seconds with a chance of level in 25, the level being the sum of (6 + l * l) / 2 over the armour pieces.
 */
public final class TFEnchantment {
    public static final int OLD_ID = 206;
    public static final ResourceKey<Enchantment> FIERY_AURA = ResourceKey.create(Registries.ENCHANTMENT, TwilightForest.id("fiery_aura"));

    private TFEnchantment() {
    }

    public static int getReactFireLevel(Player player) {
        int modifier = 0;
        var holder = player.level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).get(FIERY_AURA);
        if (holder.isEmpty()) {
            return 0;
        }
        for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD}) {
            ItemStack armor = player.getItemBySlot(slot);
            int level = EnchantmentHelper.getItemEnchantmentLevel(holder.get(), armor);
            if (level > 0) {
                modifier += (6 + level * level) / 2;
            }
        }
        return modifier;
    }

    /** damageType "mob": the melee attack of a mob (not a player, not an arrow). */
    static void onHurt(LivingIncomingDamageEvent event) {
        if (event.getEntity() instanceof Player player && event.getSource().is(DamageTypes.MOB_ATTACK) && event.getSource().getEntity() != null
                && !event.getSource().is(DamageTypeTags.IS_PROJECTILE)) {
            int fireLevel = getReactFireLevel(player);
            if (fireLevel > 0 && player.getRandom().nextInt(25) < fireLevel) {
                event.getSource().getEntity().igniteForSeconds(fireLevel / 2);
            }
        }
    }
}
