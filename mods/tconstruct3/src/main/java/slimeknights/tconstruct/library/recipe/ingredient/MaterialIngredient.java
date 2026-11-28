package slimeknights.tconstruct.library.recipe.ingredient;

import slimeknights.mantle.util.LazyTagHolderSet;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.crafting.IngredientType;
import slimeknights.mantle.data.loadable.common.IngredientLoadable;
import slimeknights.mantle.data.loadable.field.LoadableField;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.data.predicate.IJsonPredicate;
import slimeknights.mantle.util.typed.TypedMap;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.json.TinkerLoadables;
import slimeknights.tconstruct.library.json.predicate.material.MaterialPredicate;
import slimeknights.tconstruct.library.json.predicate.material.MaterialPredicateField;
import slimeknights.tconstruct.library.materials.MaterialRegistry;
import slimeknights.tconstruct.library.materials.definition.IMaterial;
import slimeknights.tconstruct.library.materials.definition.MaterialVariantId;
import slimeknights.tconstruct.library.recipe.material.MaterialRecipeCache;
import slimeknights.tconstruct.library.tools.part.IMaterialItem;
import slimeknights.tconstruct.library.tools.nbt.ToolDataComponents;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Extension of the vanilla ingredient to display materials on items and support matching by materials.
 * <p>Port: {@link net.neoforged.neoforge.common.crafting.ICustomIngredient}; the material variants are only built for
 * {@link #display()} (recipe viewers), matching reads the material component.
 */
public class MaterialIngredient extends NestedIngredient {
  private final IJsonPredicate<MaterialVariantId> material;
  protected MaterialIngredient(Ingredient nested, IJsonPredicate<MaterialVariantId> material) {
    super(nested);
    this.material = material;
  }

  /** @deprecated use {@link #MaterialIngredient(Ingredient, IJsonPredicate)} */
  @Deprecated(forRemoval = true)
  protected MaterialIngredient(Ingredient nested, MaterialVariantId material, @Nullable TagKey<IMaterial> tag) {
    this(nested, makePredicate(material, tag));
  }

  /** Converts the legacy material and tag into a predicate */
  private static IJsonPredicate<MaterialVariantId> makePredicate(MaterialVariantId material, @Nullable TagKey<IMaterial> tag) {
    // UNKNOWN is the legacy way to express any material
    IJsonPredicate<MaterialVariantId> predicate = material.equals(IMaterial.UNKNOWN.getIdentifier()) ? MaterialPredicate.ANY : MaterialPredicate.variant(material);
    if (tag != null) {
      IJsonPredicate<MaterialVariantId> tagPredicate = MaterialPredicate.tag(tag);
      if (predicate == MaterialPredicate.ANY) {
        predicate = tagPredicate;
      } else {
        predicate = MaterialPredicate.and(predicate, tagPredicate);
      }
    }
    return predicate;
  }

  /** Creates an ingredient matching the given materials */
  public static MaterialIngredient of(Ingredient ingredient, IJsonPredicate<MaterialVariantId> material) {
    return new MaterialIngredient(ingredient, material);
  }

  /** Creates an ingredient matching the given materials */
  public static MaterialIngredient of(ItemLike item, IJsonPredicate<MaterialVariantId> material) {
    return of(Ingredient.of(item), material);
  }

  /** Creates an ingredient matching a specific material */
  public static MaterialIngredient of(Ingredient ingredient) {
    return new MaterialIngredient(ingredient, MaterialPredicate.ANY);
  }

  /** Creates an ingredient matching a single material */
  public static MaterialIngredient of(Ingredient ingredient, MaterialVariantId material) {
    return of(ingredient, MaterialPredicate.variant(material));
  }

  /** Creates an ingredient matching a material tag */
  public static MaterialIngredient of(Ingredient ingredient, TagKey<IMaterial> tag) {
    return of(ingredient, MaterialPredicate.tag(tag));
  }

  /**
   * Creates a new instance from an item with a fixed material
   * @param item      Material item
   * @param material  Material ID
   * @return  Material ingredient instance
   */
  public static MaterialIngredient of(ItemLike item, MaterialVariantId material) {
    return of(Ingredient.of(item), material);
  }

  /**
   * Creates a new instance from an item with a tagged material
   * @param item      Material item
   * @param tag   Material tag
   * @return  Material ingredient instance
   */
  public static MaterialIngredient of(ItemLike item, TagKey<IMaterial> tag) {
    return of(Ingredient.of(item), tag);
  }

  /**
   * Creates a new ingredient matching any material from items
   * @param item  Material item
   * @return  Material ingredient instance
   */
  public static MaterialIngredient of(ItemLike item) {
    return of(Ingredient.of(item));
  }

  /**
   * Creates a new ingredient from a tag
   * @param tag       Tag instance
   * @param material  Material value
   * @return  Material with tag
   */
  public static MaterialIngredient of(TagKey<Item> tag, MaterialVariantId material) {
    return of(LazyTagHolderSet.ingredient(tag), material);
  }

  /**
   * Creates a new ingredient matching any material from a tag
   * @param tag       Tag instance
   * @return  Material with tag
   */
  public static MaterialIngredient of(TagKey<Item> tag) {
    return of(LazyTagHolderSet.ingredient(tag));
  }

  @Override
  public boolean test(ItemStack stack) {
    // check super first, should be faster
    if (stack.isEmpty() || !super.test(stack)) {
      return false;
    }
    // no need to read material NBT if the material is the any predicate
    if (material != MaterialPredicate.ANY) {
      return material.matches(IMaterialItem.getMaterialFromStack(stack));
    }
    return true;
  }

  @Override
  public SlotDisplay display() {
    if (!MaterialRegistry.isFullyLoaded()) {
      return nested.display();
    }
    // find all materials matching the filter; note this only shows craftable material variants
    List<SlotDisplay> displays = nested.items()
      .flatMap(item -> MaterialRecipeCache.getAllVariants().stream()
        .filter(material::matches)
        .map(mat -> IMaterialItem.withMaterial(new ItemStack(item), mat))
        .filter(stack -> stack.has(ToolDataComponents.MATERIAL.get())))
      .distinct()
      .map(stack -> (SlotDisplay) new SlotDisplay.ItemStackSlotDisplay(ItemStackTemplate.fromNonEmptyStack(stack)))
      .toList();
    return displays.isEmpty() ? nested.display() : new SlotDisplay.Composite(displays);
  }

  @Override
  public boolean isSimple() {
    return material == MaterialPredicate.ANY && nested.isSimple();
  }

  @Override
  public IngredientType<?> getType() {
    return TinkerIngredients.MATERIAL.get();
  }

  public static final Identifier ID = TConstruct.getResource("material");
  private static final LoadableField<IJsonPredicate<MaterialVariantId>,MaterialIngredient> MATERIAL_FIELD = new MaterialPredicateField<>("material", i -> i.material);
  /** Loader instance */
  public static final RecordLoadable<MaterialIngredient> LOADABLE = new RecordLoadable<>() {
    @Override
    public MaterialIngredient deserialize(JsonObject json, TypedMap context) {
      Ingredient ingredient = parseNested(json, context);
      IJsonPredicate<MaterialVariantId> material = MATERIAL_FIELD.get(json, context);
      // deprecated tag field
      if (json.has("material_tag")) {
        TConstruct.LOG.warn("Using deprecated tag field on material ingredient");
        IJsonPredicate<MaterialVariantId> tagPredicate = MaterialPredicate.tag(TinkerLoadables.MATERIAL_TAGS.getIfPresent(json, "material_tag"));
        if (material == MaterialPredicate.ANY) {
          material = tagPredicate;
        } else {
          material = MaterialPredicate.and(material, tagPredicate);
        }
      }
      return new MaterialIngredient(ingredient, material);
    }

    @Override
    public void serialize(MaterialIngredient object, JsonObject json) {
      serializeNested(object.nested, json);
      MATERIAL_FIELD.serialize(object, json);
    }

    @Override
    public MaterialIngredient decode(FriendlyByteBuf buffer, TypedMap context) {
      return new MaterialIngredient(IngredientLoadable.DISALLOW_EMPTY.decode(buffer, context), MATERIAL_FIELD.decode(buffer, context));
    }

    @Override
    public void encode(FriendlyByteBuf buffer, MaterialIngredient object) {
      IngredientLoadable.DISALLOW_EMPTY.encode(buffer, object.nested);
      MATERIAL_FIELD.encode(buffer, object);
    }
  };
}
