package slimeknights.tconstruct.library.recipe.ingredient;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import slimeknights.mantle.data.codec.LoadableMapCodec;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.util.typed.TypedMap;
import slimeknights.tconstruct.TConstruct;

/**
 * Port: the ingredient types of Tinkers (1.20.1 {@code CraftingHelper.register} in TinkerMaterials, TinkerCommons and
 * TinkerTools). In JSON they are {@code {"neoforge:ingredient_type": "tconstruct3:<name>", ...}} with the fields of 1.20.1.
 */
public final class TinkerIngredients {
  private TinkerIngredients() {}

  private static final DeferredRegister<IngredientType<?>> INGREDIENTS = DeferredRegister.create(NeoForgeRegistries.Keys.INGREDIENT_TYPES, TConstruct.MOD_ID);

  public static final DeferredHolder<IngredientType<?>,IngredientType<MaterialIngredient>> MATERIAL = register("material", MaterialIngredient.LOADABLE);
  public static final DeferredHolder<IngredientType<?>,IngredientType<MaterialValueIngredient>> MATERIAL_VALUE = register("material_value", MaterialValueIngredient.LOADABLE);
  public static final DeferredHolder<IngredientType<?>,IngredientType<NoContainerIngredient>> NO_CONTAINER = register("no_container", NoContainerIngredient.LOADABLE);
  public static final DeferredHolder<IngredientType<?>,IngredientType<BlockTagIngredient>> BLOCK_TAG = register("block_tag", BlockTagIngredient.LOADABLE);
  public static final DeferredHolder<IngredientType<?>,IngredientType<InstrumentIngredient>> INSTRUMENT = register("instrument", InstrumentIngredient.LOADABLE);
  public static final DeferredHolder<IngredientType<?>,IngredientType<ToolHookIngredient>> TOOL_HOOK = register("tool_hook", ToolHookIngredient.LOADABLE);

  public static void init(IEventBus bus) {
    INGREDIENTS.register(bus);
  }

  private static <T extends ICustomIngredient> DeferredHolder<IngredientType<?>,IngredientType<T>> register(String name, RecordLoadable<T> loadable) {
    return INGREDIENTS.register(name, () -> new IngredientType<>(codec(loadable), streamCodec(loadable)));
  }

  /** Map codec for an ingredient loadable */
  public static <T> MapCodec<T> codec(RecordLoadable<T> loadable) {
    return new LoadableMapCodec<>(loadable);
  }

  /** Stream codec for an ingredient loadable */
  public static <T> StreamCodec<RegistryFriendlyByteBuf,T> streamCodec(RecordLoadable<T> loadable) {
    return StreamCodec.of((buf, value) -> loadable.encode(buf, value), buf -> loadable.decode(buf, TypedMap.EMPTY));
  }
}
