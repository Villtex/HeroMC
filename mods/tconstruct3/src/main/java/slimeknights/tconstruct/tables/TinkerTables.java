package slimeknights.tconstruct.tables;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import slimeknights.mantle.registration.RegistryObject;
import slimeknights.tconstruct.TConstruct;

/**
 * Teil 27e: only the entries the recipe library refers to, as references to the registry entries
 * ({@link RegistryObject#create}). The tables register them in Teil 27g, which replaces this file.
 */
public final class TinkerTables {
  private TinkerTables() {}

  public static final RegistryObject<Item> partBuilder = RegistryObject.create(TConstruct.getResource("part_builder"), Registries.ITEM);
  public static final RegistryObject<RecipeSerializer<?>> materialRecipeSerializer = RegistryObject.create(TConstruct.getResource("material"), Registries.RECIPE_SERIALIZER);
}
