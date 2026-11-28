package slimeknights.tconstruct.shared;

import net.minecraft.world.entity.EntityTypes;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import slimeknights.mantle.registration.RegistryObject;
import slimeknights.tconstruct.TConstruct;

import javax.annotation.Nullable;

/**
 * Teil 27d: only the effects and helpers the tool library uses, as references to the registry entries
 * ({@link RegistryObject#create}). The effects themselves (and potions, brewing) are registered by the full
 * TinkerEffects of Teil 27g/27h, which replaces this file.
 */
public final class TinkerEffects {
  private TinkerEffects() {}

  public static final RegistryObject<MobEffect> enderference = RegistryObject.create(TConstruct.getResource("enderference"), Registries.MOB_EFFECT);
  public static final RegistryObject<MobEffect> bleeding = RegistryObject.create(TConstruct.getResource("bleeding"), Registries.MOB_EFFECT);

  /** Holder of a referenced effect, for {@link LivingEntity#hasEffect(Holder)} */
  public static Holder<MobEffect> holder(RegistryObject<? extends MobEffect> effect) {
    return BuiltInRegistries.MOB_EFFECT.wrapAsHolder(effect.get());
  }

  /** Checks if the given entity can be hit considering enderman enderference */
  public static boolean canHitWithProjectile(@Nullable LivingEntity living) {
    return living == null || living.getType() != EntityTypes.ENDERMAN || (enderference.isPresent() && living.hasEffect(holder(enderference)));
  }

  /** Checks if the given entity needs special casing for enderference */
  public static boolean needsEnderferenceOverride(@Nullable Entity entity) {
    return entity instanceof LivingEntity living && needsEnderferenceOverride(living);
  }

  /** Checks if the given entity needs special casing for enderference */
  public static boolean needsEnderferenceOverride(@Nullable LivingEntity living) {
    return living != null && living.getType() == EntityTypes.ENDERMAN && enderference.isPresent() && living.hasEffect(holder(enderference));
  }
}
