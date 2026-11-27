package slimeknights.tconstruct.tools.modules;

import net.minecraft.world.item.equipment.ArmorType;


/**
 * Interface for armor module builders, which are builders designed to create slightly varied modules based on the armor slot
 */
public interface ArmorModuleBuilder<T> {
  /**
   * Array of the durability multiplier for use in durability factor creation, in order helmet, chestplate, leggings, boot.
   * This is identical values to those used in {@code net.minecraft.world.item.ArmorMaterials}{@code #HEALTH_FUNCTION_FOR_TYPE},
   * though as a public array (using ordinals from {@link ArmorType} as the indices.
   */
  int[] MAX_DAMAGE_ARRAY = {11, 16, 15, 13};
  /** The four armor pieces in the order of {@link #MAX_DAMAGE_ARRAY} (26.3 {@link ArmorType#values()} also has the animal armor {@code BODY}) */
  ArmorType[] TYPES = {ArmorType.HELMET, ArmorType.CHESTPLATE, ArmorType.LEGGINGS, ArmorType.BOOTS};
  /** Factor for use alongside {@link #MAX_DAMAGE_ARRAY} for shields. */
  int SHIELD_DAMAGE = 22;

  /**
   * Builds the object for the given slot
   */
  T build(ArmorType slot);

  /**
   * Builder for an object that also includes shields
   */
  interface ArmorShieldModuleBuilder<T> extends ArmorModuleBuilder<T> {
    /** Builds the object for the shield */
    T buildShield();
  }
}
