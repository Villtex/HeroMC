package slimeknights.tconstruct.tables;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import slimeknights.mantle.recipe.helper.LoadableRecipeSerializer;
import slimeknights.mantle.recipe.helper.SimpleRecipeSerializer;
import slimeknights.mantle.registration.RegistryObject;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.TinkerModule;
import slimeknights.tconstruct.library.recipe.material.MaterialRecipe;
import slimeknights.tconstruct.library.recipe.material.ShapedMaterialRecipe;
import slimeknights.tconstruct.library.recipe.material.ShapedMaterialsRecipe;
import slimeknights.tconstruct.library.recipe.material.ShapelessMaterialsRecipe;
import slimeknights.tconstruct.library.recipe.partbuilder.ItemPartRecipe;
import slimeknights.tconstruct.library.recipe.partbuilder.PartRecipe;
import slimeknights.tconstruct.library.recipe.partbuilder.recycle.PartBuilderRecycle;
import slimeknights.tconstruct.library.recipe.tinkerstation.building.FixedMaterialSwappingRecipe;
import slimeknights.tconstruct.library.recipe.tinkerstation.building.MaterialValueSwappingRecipe;
import slimeknights.tconstruct.library.recipe.tinkerstation.building.PartSwappingOverrideRecipe;
import slimeknights.tconstruct.library.recipe.tinkerstation.building.ToolBuildingRecipe;
import slimeknights.tconstruct.library.recipe.tinkerstation.building.ToolMaterialSwappingRecipe;
import slimeknights.tconstruct.tables.recipe.CraftingTableRepairKitRecipe;
import slimeknights.tconstruct.tables.recipe.PartBuilderToolRecycle;
import slimeknights.tconstruct.tables.recipe.TinkerStationDamagingRecipe;
import slimeknights.tconstruct.tables.recipe.TinkerStationPartSwapping;
import slimeknights.tconstruct.tables.recipe.TinkerStationRepairRecipe;

/**
 * Teil 27f: the recipe serializers of 1.20.1 TinkerTables and references to the tables the recipe library shows
 * ({@link RegistryObject#create}). The tables register their blocks in Teil 27g, which replaces this file with the full
 * original and keeps the serializers from here.
 */
public final class TinkerTables extends TinkerModule {
  private TinkerTables() {}

  /** Loads the class, which registers the serializers into {@link TinkerModule#RECIPE_SERIALIZERS} */
  public static void init() {}

  public static final RegistryObject<Item> partBuilder = RegistryObject.create(TConstruct.getResource("part_builder"), Registries.ITEM);

  public static final RegistryObject<RecipeSerializer<MaterialRecipe>> materialRecipeSerializer = RECIPE_SERIALIZERS.register("material", () -> LoadableRecipeSerializer.of(MaterialRecipe.LOADER));
  public static final RegistryObject<RecipeSerializer<ToolBuildingRecipe>> toolBuildingRecipeSerializer = RECIPE_SERIALIZERS.register("tool_building", () -> LoadableRecipeSerializer.of(ToolBuildingRecipe.LOADER));
  public static final RegistryObject<RecipeSerializer<TinkerStationPartSwapping>> tinkerStationPartSwappingSerializer = RECIPE_SERIALIZERS.register("tinker_station_part_swapping", () -> LoadableRecipeSerializer.of(TinkerStationPartSwapping.LOADER));
  public static final RegistryObject<RecipeSerializer<TinkerStationDamagingRecipe>> tinkerStationDamagingSerializer = RECIPE_SERIALIZERS.register("tinker_station_damaging", () -> LoadableRecipeSerializer.of(TinkerStationDamagingRecipe.LOADER));
  public static final RegistryObject<RecipeSerializer<FixedMaterialSwappingRecipe>> fixedMaterialSwapping = RECIPE_SERIALIZERS.register("fixed_material_swapping", () -> LoadableRecipeSerializer.of(FixedMaterialSwappingRecipe.LOADER));
  public static final RegistryObject<RecipeSerializer<MaterialValueSwappingRecipe>> materialValueSwapping = RECIPE_SERIALIZERS.register("material_value_swapping", () -> LoadableRecipeSerializer.of(MaterialValueSwappingRecipe.LOADER));
  public static final RegistryObject<RecipeSerializer<PartSwappingOverrideRecipe>> partSwappingOverride = RECIPE_SERIALIZERS.register("part_swapping_override", () -> LoadableRecipeSerializer.of(PartSwappingOverrideRecipe.LOADER));
  public static final RegistryObject<RecipeSerializer<ToolMaterialSwappingRecipe>> toolMaterialSwapping = RECIPE_SERIALIZERS.register("tool_material_swapping", () -> LoadableRecipeSerializer.of(ToolMaterialSwappingRecipe.LOADER));
  public static final RegistryObject<RecipeSerializer<ShapedMaterialRecipe>> shapedMaterialRecipeSerializer = RECIPE_SERIALIZERS.register("crafting_shaped_material", () -> new RecipeSerializer<>(ShapedMaterialRecipe.CODEC, ShapedMaterialRecipe.STREAM_CODEC));
  public static final RegistryObject<RecipeSerializer<ShapedMaterialsRecipe>> shapedMaterialsRecipeSerializer = RECIPE_SERIALIZERS.register("crafting_shaped_materials", () -> new RecipeSerializer<>(ShapedMaterialsRecipe.CODEC, ShapedMaterialsRecipe.STREAM_CODEC));
  public static final RegistryObject<RecipeSerializer<ShapelessMaterialsRecipe>> shapelessMaterialsRecipeSerializer = RECIPE_SERIALIZERS.register("crafting_shapeless_materials", () -> new RecipeSerializer<>(ShapelessMaterialsRecipe.CODEC, ShapelessMaterialsRecipe.STREAM_CODEC));
  public static final RegistryObject<RecipeSerializer<PartRecipe>> partRecipeSerializer = RECIPE_SERIALIZERS.register("part_builder", () -> LoadableRecipeSerializer.of(PartRecipe.LOADER));
  public static final RegistryObject<RecipeSerializer<ItemPartRecipe>> itemPartBuilderSerializer = RECIPE_SERIALIZERS.register("item_part_builder", () -> LoadableRecipeSerializer.of(ItemPartRecipe.LOADER));
  public static final RegistryObject<RecipeSerializer<PartBuilderToolRecycle>> partBuilderToolRecycling = RECIPE_SERIALIZERS.register("part_builder_tool_recycling", () -> LoadableRecipeSerializer.of(PartBuilderToolRecycle.LOADER));
  public static final RegistryObject<RecipeSerializer<PartBuilderRecycle>> partBuilderDamageableRecycling = RECIPE_SERIALIZERS.register("part_builder_recycling", () -> LoadableRecipeSerializer.of(PartBuilderRecycle.LOADER));
  public static final RegistryObject<RecipeSerializer<TinkerStationRepairRecipe>> tinkerStationRepairSerializer = RECIPE_SERIALIZERS.register("tinker_station_repair", () -> SimpleRecipeSerializer.of(TinkerStationRepairRecipe::new));
  public static final RegistryObject<RecipeSerializer<CraftingTableRepairKitRecipe>> craftingTableRepairSerializer = RECIPE_SERIALIZERS.register("crafting_table_repair", () -> SimpleRecipeSerializer.of(CraftingTableRepairKitRecipe::new));
}
