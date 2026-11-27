package slimeknights.tconstruct.tools;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import slimeknights.mantle.registration.RegistryObject;
import slimeknights.tconstruct.TConstruct;

/**
 * Teil 27d: only the entries the tool library refers to, as references to the registry entries
 * ({@link RegistryObject#create}). The tools, their entities and loot functions are registered by the full
 * TinkerTools of Teil 27h, which replaces this file.
 */
public final class TinkerTools {
  private TinkerTools() {}

  public static final RegistryObject<EntityType<?>> fishingHook = RegistryObject.create(TConstruct.getResource("fishing_hook"), Registries.ENTITY_TYPE);
  public static final RegistryObject<EntityType<?>> indestructibleItem = RegistryObject.create(TConstruct.getResource("indestructible_item"), Registries.ENTITY_TYPE);
}
