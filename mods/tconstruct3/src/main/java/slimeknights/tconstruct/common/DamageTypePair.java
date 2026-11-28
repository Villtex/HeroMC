package slimeknights.tconstruct.common;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageType;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.data.loadable.record.RecordLoadable;

/** Melee and ranged damage type of a fluid effect (in 3.12.1 nested in DamageFluidEffect, moved out for Teil 27d) */
public record DamageTypePair(ResourceKey<DamageType> melee, ResourceKey<DamageType> ranged) {
  public static final RecordLoadable<DamageTypePair> LOADER = RecordLoadable.create(
    Loadables.DAMAGE_TYPE_KEY.requiredField("melee", DamageTypePair::melee),
    Loadables.DAMAGE_TYPE_KEY.requiredField("ranged", DamageTypePair::ranged),
    DamageTypePair::new);

  /** Gets all pair values, useful for datagen */
  @SuppressWarnings("unchecked")
  public ResourceKey<DamageType>[] values() {
    return new ResourceKey[] { melee, ranged };
  }
}
