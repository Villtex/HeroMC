package artifice.item;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;

/**
 * EnumUpgrades: the enchantment of each upgrade and its highest level with "Limit Upgrades" (on in the pack).
 * The sharpening kit gives efficiency to items that cannot take sharpness.
 */
public enum Upgrade {
    SHARPENING_KIT(Enchantments.SHARPNESS, 3),
    REINFORCEMENT(Enchantments.UNBREAKING, 2),
    REINFORCED_LIMBS(Enchantments.PUNCH, 2),
    PLAITED_STRING(Enchantments.POWER, 3),
    COUNTERWEIGHT(Enchantments.KNOCKBACK, 2),
    ARMOR_SPIKES(Enchantments.THORNS, 2),
    LAMINATED_PADDING(Enchantments.PROTECTION, 3),
    QUILTED_COVER(Enchantments.PROJECTILE_PROTECTION, 3),
    ELASTIC_SOLES(Enchantments.FEATHER_FALLING, 3),
    FIREDAMP(Enchantments.FIRE_PROTECTION, 3),
    ELASTIC_LAYERING(Enchantments.BLAST_PROTECTION, 3),
    SCUBA_TANK(Enchantments.RESPIRATION, 3),
    DIVE_KIT(Enchantments.AQUA_AFFINITY, 1);

    public final ResourceKey<Enchantment> enchantment;
    public final int maxLevel;
    public final String id = name().toLowerCase(java.util.Locale.ROOT);

    Upgrade(ResourceKey<Enchantment> enchantment, int maxLevel) {
        this.enchantment = enchantment;
        this.maxLevel = maxLevel;
    }
}
