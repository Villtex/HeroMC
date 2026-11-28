package slimeknights.tconstruct.smeltery;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import slimeknights.mantle.recipe.helper.LoadableRecipeSerializer;
import slimeknights.mantle.registration.RegistryObject;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.TinkerModule;
import slimeknights.tconstruct.library.recipe.TinkerRecipeTypes;
import slimeknights.tconstruct.library.recipe.alloying.AlloyRecipe;
import slimeknights.tconstruct.library.recipe.casting.CastDuplicationRecipe;
import slimeknights.tconstruct.library.recipe.casting.ItemCastingRecipe;
import slimeknights.tconstruct.library.recipe.casting.PotionCastingRecipe;
import slimeknights.tconstruct.library.recipe.casting.RetexturedCastingRecipe;
import slimeknights.tconstruct.library.recipe.casting.TipClearingCastingRecipe;
import slimeknights.tconstruct.library.recipe.casting.TippingCastingRecipe;
import slimeknights.tconstruct.library.recipe.casting.container.ContainerFillingRecipe;
import slimeknights.tconstruct.library.recipe.casting.material.CompositeCastingRecipe;
import slimeknights.tconstruct.library.recipe.casting.material.MaterialCastingRecipe;
import slimeknights.tconstruct.library.recipe.casting.material.MaterialFluidRecipe;
import slimeknights.tconstruct.library.recipe.casting.material.PartSwapCastingRecipe;
import slimeknights.tconstruct.library.recipe.casting.material.ToolCastingRecipe;
import slimeknights.tconstruct.library.recipe.entitymelting.EntityMeltingRecipe;
import slimeknights.tconstruct.library.recipe.fuel.MeltingFuel;
import slimeknights.tconstruct.library.recipe.melting.DamageableMeltingRecipe;
import slimeknights.tconstruct.library.recipe.melting.MaterialMeltingRecipe;
import slimeknights.tconstruct.library.recipe.melting.MeltingRecipe;
import slimeknights.tconstruct.library.recipe.melting.OreMeltingRecipe;
import slimeknights.tconstruct.library.recipe.molding.MoldingRecipe;

/**
 * Teil 27f: the recipe serializers of 1.20.1 TinkerSmeltery (all of them are library recipes) and references to the
 * smeltery blocks the recipe library shows as catalysts ({@link RegistryObject#create}). Teil 27i replaces this file with
 * the full original and keeps the serializers from here (1.20.1 {@code TypeAwareRecipeSerializer} is a plain
 * {@link RecipeSerializer} in 26.3, the type is in {@link LoadableRecipeSerializer#TYPE}).
 */
public final class TinkerSmeltery extends TinkerModule {
  private TinkerSmeltery() {}

  /** Loads the class, which registers the serializers into {@link TinkerModule#RECIPE_SERIALIZERS} */
  public static void init() {}

  /* Blocks shown as recipe catalysts, registered in Teil 27i */
  public static final RegistryObject<Item> searedMelter = item("seared_melter");
  public static final RegistryObject<Item> searedTable = item("seared_table");
  public static final RegistryObject<Item> searedBasin = item("seared_basin");
  /** 1.20.1 {@code searedTank.get(TankType.FUEL_TANK)} */
  public static final RegistryObject<Item> searedFuelTank = item("seared_fuel_tank");

  private static RegistryObject<Item> item(String name) {
    return RegistryObject.create(TConstruct.getResource(name), Registries.ITEM);
  }

  /* casting */
  public static final RegistryObject<RecipeSerializer<ItemCastingRecipe>> basinRecipeSerializer = RECIPE_SERIALIZERS.register("casting_basin", () -> LoadableRecipeSerializer.of(ItemCastingRecipe.LOADER, TinkerRecipeTypes.CASTING_BASIN));
  public static final RegistryObject<RecipeSerializer<ItemCastingRecipe>> tableRecipeSerializer = RECIPE_SERIALIZERS.register("casting_table", () -> LoadableRecipeSerializer.of(ItemCastingRecipe.LOADER, TinkerRecipeTypes.CASTING_TABLE));
  public static final RegistryObject<RecipeSerializer<ContainerFillingRecipe>> basinFillingRecipeSerializer = RECIPE_SERIALIZERS.register("basin_filling", () -> LoadableRecipeSerializer.of(ContainerFillingRecipe.LOADER, TinkerRecipeTypes.CASTING_BASIN));
  public static final RegistryObject<RecipeSerializer<ContainerFillingRecipe>> tableFillingRecipeSerializer = RECIPE_SERIALIZERS.register("table_filling", () -> LoadableRecipeSerializer.of(ContainerFillingRecipe.LOADER, TinkerRecipeTypes.CASTING_TABLE));
  public static final RegistryObject<RecipeSerializer<CastDuplicationRecipe>> basinDuplicationRecipeSerializer = RECIPE_SERIALIZERS.register("basin_duplication", () -> LoadableRecipeSerializer.of(CastDuplicationRecipe.LOADER, TinkerRecipeTypes.CASTING_BASIN));
  public static final RegistryObject<RecipeSerializer<CastDuplicationRecipe>> tableDuplicationRecipeSerializer = RECIPE_SERIALIZERS.register("table_duplication", () -> LoadableRecipeSerializer.of(CastDuplicationRecipe.LOADER, TinkerRecipeTypes.CASTING_TABLE));
  public static final RegistryObject<RecipeSerializer<PotionCastingRecipe>> basinPotionRecipeSerializer = RECIPE_SERIALIZERS.register("casting_basin_potion", () -> LoadableRecipeSerializer.of(PotionCastingRecipe.LOADER, TinkerRecipeTypes.CASTING_BASIN));
  public static final RegistryObject<RecipeSerializer<PotionCastingRecipe>> tablePotionRecipeSerializer = RECIPE_SERIALIZERS.register("casting_table_potion", () -> LoadableRecipeSerializer.of(PotionCastingRecipe.LOADER, TinkerRecipeTypes.CASTING_TABLE));
  public static final RegistryObject<RecipeSerializer<TippingCastingRecipe>> basinTippingRecipeSerializer = RECIPE_SERIALIZERS.register("casting_basin_tipping", () -> LoadableRecipeSerializer.of(TippingCastingRecipe.LOADER, TinkerRecipeTypes.CASTING_BASIN));
  public static final RegistryObject<RecipeSerializer<TippingCastingRecipe>> tableTippingRecipeSerializer = RECIPE_SERIALIZERS.register("casting_table_tipping", () -> LoadableRecipeSerializer.of(TippingCastingRecipe.LOADER, TinkerRecipeTypes.CASTING_TABLE));
  public static final RegistryObject<RecipeSerializer<TipClearingCastingRecipe>> basinTipClearingRecipeSerializer = RECIPE_SERIALIZERS.register("casting_basin_tipped_clearing", () -> LoadableRecipeSerializer.of(TipClearingCastingRecipe.LOADER, TinkerRecipeTypes.CASTING_BASIN));
  public static final RegistryObject<RecipeSerializer<TipClearingCastingRecipe>> tableTipClearingRecipeSerializer = RECIPE_SERIALIZERS.register("casting_table_tipped_clearing", () -> LoadableRecipeSerializer.of(TipClearingCastingRecipe.LOADER, TinkerRecipeTypes.CASTING_TABLE));
  public static final RegistryObject<RecipeSerializer<RetexturedCastingRecipe>> retexturedBasinRecipeSerializer = RECIPE_SERIALIZERS.register("retextured_casting_basin", () -> LoadableRecipeSerializer.of(RetexturedCastingRecipe.LOADER, TinkerRecipeTypes.CASTING_BASIN));
  public static final RegistryObject<RecipeSerializer<RetexturedCastingRecipe>> retexturedTableRecipeSerializer = RECIPE_SERIALIZERS.register("retextured_casting_table", () -> LoadableRecipeSerializer.of(RetexturedCastingRecipe.LOADER, TinkerRecipeTypes.CASTING_TABLE));
  public static final RegistryObject<RecipeSerializer<MaterialCastingRecipe>> basinMaterialSerializer = RECIPE_SERIALIZERS.register("basin_casting_material", () -> LoadableRecipeSerializer.of(MaterialCastingRecipe.LOADER, TinkerRecipeTypes.CASTING_BASIN));
  public static final RegistryObject<RecipeSerializer<MaterialCastingRecipe>> tableMaterialSerializer = RECIPE_SERIALIZERS.register("table_casting_material", () -> LoadableRecipeSerializer.of(MaterialCastingRecipe.LOADER, TinkerRecipeTypes.CASTING_TABLE));
  public static final RegistryObject<RecipeSerializer<CompositeCastingRecipe>> basinCompositeSerializer = RECIPE_SERIALIZERS.register("basin_casting_composite", () -> LoadableRecipeSerializer.of(CompositeCastingRecipe.LOADER, TinkerRecipeTypes.CASTING_BASIN));
  public static final RegistryObject<RecipeSerializer<CompositeCastingRecipe>> tableCompositeSerializer = RECIPE_SERIALIZERS.register("table_casting_composite", () -> LoadableRecipeSerializer.of(CompositeCastingRecipe.LOADER, TinkerRecipeTypes.CASTING_TABLE));
  public static final RegistryObject<RecipeSerializer<ToolCastingRecipe>> basinToolSerializer = RECIPE_SERIALIZERS.register("basin_tool_casting", () -> LoadableRecipeSerializer.of(ToolCastingRecipe.LOADER, TinkerRecipeTypes.CASTING_BASIN));
  public static final RegistryObject<RecipeSerializer<ToolCastingRecipe>> tableToolSerializer = RECIPE_SERIALIZERS.register("table_tool_casting", () -> LoadableRecipeSerializer.of(ToolCastingRecipe.LOADER, TinkerRecipeTypes.CASTING_TABLE));
  public static final RegistryObject<RecipeSerializer<MaterialFluidRecipe>> materialFluidRecipe = RECIPE_SERIALIZERS.register("material_fluid", () -> LoadableRecipeSerializer.of(MaterialFluidRecipe.LOADER));
  public static final RegistryObject<RecipeSerializer<PartSwapCastingRecipe>> basinPartSwappingSerializer = RECIPE_SERIALIZERS.register("basin_casting_part_swapping", () -> LoadableRecipeSerializer.of(PartSwapCastingRecipe.LOADER, TinkerRecipeTypes.CASTING_BASIN));
  public static final RegistryObject<RecipeSerializer<PartSwapCastingRecipe>> tablePartSwappingSerializer = RECIPE_SERIALIZERS.register("table_casting_part_swapping", () -> LoadableRecipeSerializer.of(PartSwapCastingRecipe.LOADER, TinkerRecipeTypes.CASTING_TABLE));
  /* molding */
  public static final RegistryObject<RecipeSerializer<MoldingRecipe>> moldingBasinSerializer = RECIPE_SERIALIZERS.register("molding_basin", () -> LoadableRecipeSerializer.of(MoldingRecipe.LOADER, TinkerRecipeTypes.MOLDING_BASIN));
  public static final RegistryObject<RecipeSerializer<MoldingRecipe>> moldingTableSerializer = RECIPE_SERIALIZERS.register("molding_table", () -> LoadableRecipeSerializer.of(MoldingRecipe.LOADER, TinkerRecipeTypes.MOLDING_TABLE));
  /* melting */
  public static final RegistryObject<RecipeSerializer<MeltingRecipe>> meltingSerializer = RECIPE_SERIALIZERS.register("melting", () -> LoadableRecipeSerializer.of(MeltingRecipe.LOADER));
  public static final RegistryObject<RecipeSerializer<OreMeltingRecipe>> oreMeltingSerializer = RECIPE_SERIALIZERS.register("ore_melting", () -> LoadableRecipeSerializer.of(OreMeltingRecipe.LOADER));
  public static final RegistryObject<RecipeSerializer<DamageableMeltingRecipe>> damagableMeltingSerializer = RECIPE_SERIALIZERS.register("damagable_melting", () -> LoadableRecipeSerializer.of(DamageableMeltingRecipe.LOADER));
  public static final RegistryObject<RecipeSerializer<MaterialMeltingRecipe>> materialMeltingSerializer = RECIPE_SERIALIZERS.register("material_melting", () -> LoadableRecipeSerializer.of(MaterialMeltingRecipe.LOADER));
  public static final RegistryObject<RecipeSerializer<MeltingFuel>> fuelSerializer = RECIPE_SERIALIZERS.register("melting_fuel", () -> LoadableRecipeSerializer.of(MeltingFuel.LOADER));
  public static final RegistryObject<RecipeSerializer<EntityMeltingRecipe>> entityMeltingSerializer = RECIPE_SERIALIZERS.register("entity_melting", () -> LoadableRecipeSerializer.of(EntityMeltingRecipe.LOADER));
  /* alloying */
  public static final RegistryObject<RecipeSerializer<AlloyRecipe>> alloyingSerializer = RECIPE_SERIALIZERS.register("alloy", () -> LoadableRecipeSerializer.of(AlloyRecipe.LOADER));
}
