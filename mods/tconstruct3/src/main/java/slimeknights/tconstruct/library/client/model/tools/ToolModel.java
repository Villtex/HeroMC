package slimeknights.tconstruct.library.client.model.tools;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableList;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.math.Axis;
import com.mojang.math.Transformation;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelDebugName;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.cuboid.CuboidModel;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.ItemQuads;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.geometry.UnbakedGeometry;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.client.resources.model.sprite.TextureSlots;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec2;
import net.neoforged.neoforge.client.model.DelegateUnbakedModel;
import net.neoforged.neoforge.client.model.ExtendedUnbakedGeometry;
import net.neoforged.neoforge.client.model.UnbakedModelLoader;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import slimeknights.mantle.client.model.util.ColoredBlockModel;
import slimeknights.mantle.client.model.util.MantleItemLayerModel;
import slimeknights.mantle.data.loadable.Loadable;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.data.loadable.mapping.CompactLoadable;
import slimeknights.mantle.data.loadable.primitive.BooleanLoadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.util.ItemLayerPixels;
import slimeknights.mantle.util.ItemNbt;
import slimeknights.mantle.util.JsonHelper;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.client.TinkerSprites;
import slimeknights.tconstruct.library.client.materials.MaterialRenderInfo.TintedSprite;
import slimeknights.tconstruct.library.client.materials.MaterialRenderInfoLoader;
import slimeknights.tconstruct.library.client.model.TinkerItemModel;
import slimeknights.tconstruct.library.client.modifiers.IBakedModifierModel;
import slimeknights.tconstruct.library.client.modifiers.ModifierModelMap;
import slimeknights.tconstruct.library.client.modifiers.ModifierModelMapManager;
import slimeknights.tconstruct.library.client.modifiers.model.ModifierModel;
import slimeknights.tconstruct.library.materials.definition.MaterialId;
import slimeknights.tconstruct.library.materials.definition.MaterialVariantId;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierId;
import slimeknights.tconstruct.library.recipe.worktable.ModifierSetWorktableRecipe;
import slimeknights.tconstruct.library.tools.nbt.IModDataView;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.MaterialIdNBT;
import slimeknights.tconstruct.library.tools.nbt.ModifierNBT;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Model handling all tools, both multipart and non.
 * <p>
 * Port (Teil 27f2): this class is the block model loader {@code tconstruct3:tool} (keeps the settings of the 1.20.1
 * JSON, bakes the untextured parts as a plain fallback), {@link Item} is the 26.3 item model {@code tconstruct3:tool}
 * that builds the quads for the materials and modifiers of the stack (see {@link TinkerItemModel}). The 1.20.1 item
 * color handler became the tints of the item layer, the model overrides became item model definitions with the
 * condition {@code tconstruct3:predicate}, ammo is drawn as the layers of the ammo's own item model.
 */
public class ToolModel extends DelegateUnbakedModel {
  /** Shared loader instance */
  public static final UnbakedModelLoader<ToolModel> LOADER = ToolModel::deserialize;
  /** Set of transform types that make tools render small */
  private static final BitSet SMALL_TOOL_TYPES = new BitSet();

  /** Registers a new small tool transform type */
  public static synchronized void registerSmallTool(ItemDisplayContext type) {
    SMALL_TOOL_TYPES.set(type.ordinal());
  }

  /** Gets an offset from JSON, or the default value if absent */
  private static Vec2 getOffset(JsonObject parent, String key) {
    if (parent.has(key)) {
      return MaterialModel.getVec2(parent, key);
    }
    return Vec2.ZERO;
  }

  /** Deserializes the model from JSON */
  public static ToolModel deserialize(JsonObject json, JsonDeserializationContext context) {
    List<ToolPart> parts = Collections.emptyList();
    if (json.has("parts")) {
      parts = JsonHelper.parseList(json, "parts", ToolPart::read);
    }
    boolean isLarge = GsonHelper.getAsBoolean(json, "large", false);
    boolean showTraits = GsonHelper.getAsBoolean(json, "show_traits", false);
    Vec2 offset = getOffset(json, "large_offset");
    // modifier root fetching
    List<Identifier> modifierModels = List.of();
    if (json.has("modifier_maps")) {
      modifierModels = JsonHelper.parseList(json, "modifier_maps", Loadables.RESOURCE_LOCATION);
    }
    List<Identifier> smallModifierRoots = List.of();
    List<Identifier> largeModifierRoots = List.of();
    if (json.has("modifier_roots")) {
      // large model requires an object
      if (isLarge) {
        JsonObject modifierRoots = GsonHelper.getAsJsonObject(json, "modifier_roots");
        smallModifierRoots = JsonHelper.parseList(modifierRoots, "small", Loadables.RESOURCE_LOCATION);
        largeModifierRoots = JsonHelper.parseList(modifierRoots, "large", Loadables.RESOURCE_LOCATION);
      } else {
        // small requires an array
        smallModifierRoots = JsonHelper.parseList(json, "modifier_roots", Loadables.RESOURCE_LOCATION);
      }
    }
    // fetch data related to ammo display
    Identifier ammoKey = null;
    Vec2 smallAmmoOffset = Vec2.ZERO;
    Vec2 largeAmmoOffset = Vec2.ZERO;
    boolean flipAmmo = false;
    boolean leftAmmo = false;
    if (json.has("ammo")) {
      JsonObject ammo = GsonHelper.getAsJsonObject(json, "ammo");
      ammoKey = JsonHelper.getResourceLocation(ammo, "key");
      flipAmmo = GsonHelper.getAsBoolean(ammo, "flip");
      leftAmmo = GsonHelper.getAsBoolean(ammo, "left");
      // large has an offset for both models
      if (isLarge) {
        if (!ammo.has("small_offset") && !ammo.has("large_offset")) {
          throw new JsonSyntaxException("Ammo must either have a small or large offset provided");
        }
        smallAmmoOffset = getOffset(ammo, "small_offset");
        largeAmmoOffset = getOffset(ammo, "large_offset");
      } else {
        // no large is just a single offset, and is required
        smallAmmoOffset = MaterialModel.getVec2(ammo, "offset");
      }
    }

    // modifiers first
    List<FirstModifier> firstModifiers = FirstModifier.LOADABLE.getOrDefault(json, "first_modifiers", List.of());
    return new ToolModel(ColoredBlockModel.readVanilla(json, context), parts, isLarge, offset, modifierModels, smallModifierRoots, largeModifierRoots, firstModifiers, ammoKey, flipAmmo, leftAmmo, smallAmmoOffset, largeAmmoOffset, showTraits);
  }

  /** List of tool parts in this model */
  private final List<ToolPart> toolParts;
  /** If true, this is a large tool and uses double resolution textures in hand */
  private final boolean isLarge;
  /** Transform matrix to apply to child parts */
  private final Vec2 offset;
  /** List of modifier models to read for this tool. Separate from the tool model for the sake of reuse. */
  private final List<Identifier> modifierModels;
  /** Location to fetch modifier textures for small variant (deprecated in 3.12, still used by the data) */
  private final List<Identifier> smallModifierRoots;
  /** Location to fetch modifier textures for large variant (deprecated in 3.12, still used by the data) */
  private final List<Identifier> largeModifierRoots;
  /** Modifiers that show first on tools, bypassing normal sort order */
  private final List<FirstModifier> firstModifiers;
  /** Location in tool NBT to find the ammo */
  @Nullable
  private final Identifier ammoKey;
  /** If true, flips the ammo horizontally, as most bows have opposite orientation from ammo */
  private final boolean flipAmmo;
  /** If true, left handed models should have the ammo shifted to the left */
  private final boolean leftAmmo;
  /** Offset to apply to ammo quads for the small model */
  private final Vec2 smallAmmoOffset;
  /** Offset to apply to ammo quads for the large model */
  private final Vec2 largeAmmoOffset;
  /** If true, traits are displayed on the tool model. If false, just modifiers. */
  private final boolean showTraits;

  private ToolModel(CuboidModel delegate, List<ToolPart> toolParts, boolean isLarge, Vec2 offset, List<Identifier> modifierModels, List<Identifier> smallModifierRoots, List<Identifier> largeModifierRoots,
                    List<FirstModifier> firstModifiers, @Nullable Identifier ammoKey, boolean flipAmmo, boolean leftAmmo, Vec2 smallAmmoOffset, Vec2 largeAmmoOffset, boolean showTraits) {
    super(delegate);
    this.toolParts = toolParts;
    this.isLarge = isLarge;
    this.offset = offset;
    this.modifierModels = modifierModels;
    this.smallModifierRoots = smallModifierRoots;
    this.largeModifierRoots = largeModifierRoots;
    this.firstModifiers = firstModifiers;
    this.ammoKey = ammoKey;
    this.flipAmmo = flipAmmo;
    this.leftAmmo = leftAmmo;
    this.smallAmmoOffset = smallAmmoOffset;
    this.largeAmmoOffset = largeAmmoOffset;
    this.showTraits = showTraits;
  }

  /** Parts of the model, the default part if none are set */
  private List<ToolPart> parts() {
    return toolParts.isEmpty() ? ToolPart.DEFAULT_PARTS : toolParts;
  }

  @Override
  public UnbakedGeometry geometry() {
    // fallback when used as a plain model: the small part textures without materials
    List<ToolPart> parts = parts();
    return (ExtendedUnbakedGeometry) (TextureSlots slots, ModelBaker baker, ModelState state, ModelDebugName name, ContextMap properties) -> {
      QuadCollection.Builder builder = new QuadCollection.Builder();
      ItemLayerPixels pixels = new ItemLayerPixels();
      List<List<BakedQuad>> layers = new ArrayList<>();
      for (int i = parts.size() - 1; i >= 0; i--) {
        TextureAtlasSprite sprite = baker.materials().resolveSlot(slots, parts.get(i).getName(false), name).sprite();
        layers.add(MantleItemLayerModel.getQuadsForSprite(-1, -1, sprite, Transformation.IDENTITY, 0, pixels));
      }
      for (int i = layers.size() - 1; i >= 0; i--) {
        layers.get(i).forEach(builder::addUnculledFace);
      }
      return builder.build();
    };
  }

  /**
   * adds quads for relevant modifiers
   * @param spriteGetter    Sprite getter instance
   * @param modifierModels  Map of modifier models
   * @param tool            Tool instance
   * @param quadConsumer    Consumer for finished quads
   * @param transforms      Transforms to apply
   * @param isLarge         If true, the quads are for a large tool
   */
  private static void addModifierQuads(Function<Material, TextureAtlasSprite> spriteGetter, ModifierModelMap modifierModels, List<FirstModifier> firstModifiers, boolean showTraits, IToolStackView tool, Consumer<Collection<BakedQuad>> quadConsumer, @Nullable ItemLayerPixels pixels, Transformation transforms, boolean isLarge) {
    if (!modifierModels.isEmpty()) {
      // keep a running tint index so models know where they should start, currently starts at 0 as the main model does not use tint indexes
      int modelIndex = 0;
      // reversed order to ensure the pixels is updated correctly
      List<ModifierEntry> modifiers = (showTraits ? tool.getModifiers() : tool.getUpgrades()).getModifiers();
      // keep track of the entry for each first modifier, as that may impact how it renders
      ModifierEntry[] firsts = new ModifierEntry[firstModifiers.size()];
      if (!modifiers.isEmpty()) {
        // last, add all regular modifiers
        Set<ModifierId> hidden = ModifierSetWorktableRecipe.getModifierSet(tool.getPersistentData(), TConstruct.getResource("invisible_modifiers"));
        for (int i = modifiers.size() - 1; i >= 0; i--) {
          ModifierEntry entry = modifiers.get(i);
          ModifierId modifier = entry.getModifier().getId();
          int index = FirstModifier.indexOf(firstModifiers, modifier);
          if (index != -1) {
            // handle first modifiers later
            firsts[index] = entry;
          } else if (!hidden.contains(modifier)) {
            IBakedModifierModel model = modifierModels.get(modifier);
            if (model != null) {
              // if the modifier is in the list, delay adding its quads, but keep the expected tint index
              model.addQuads(tool, entry, spriteGetter, transforms, isLarge, modelIndex, quadConsumer, pixels);
              modelIndex += model.getTintIndexes();
            }
          }
        }
      }
      // first, add the first modifiers
      for (int i = firsts.length - 1; i >= 0; i--) {
        ModifierEntry entry = firsts[i];
        FirstModifier first = firstModifiers.get(i);
        if (entry != null || first.forced) {
          IBakedModifierModel model = modifierModels.get(first.id);
          if (model != null) {
            if (entry == null) {
              entry = new ModifierEntry(first.id, 0);
            }
            model.addQuads(tool, entry, spriteGetter, transforms, isLarge, modelIndex, quadConsumer, pixels);
            modelIndex += model.getTintIndexes();
          }
        }
      }
      // iterate the constant in order of key (already reverse alphabetical)
      for (ModifierModel model : modifierModels.sortedConstant()) {
        model.addQuads(tool, ModifierEntry.EMPTY, spriteGetter, transforms, isLarge, modelIndex, quadConsumer, pixels);
        modelIndex += model.getTintIndexes();
      }
    }
  }

  /**
   * Gets the tint for the given index, the 1.20.1 item color handler of tools.
   * @return  Tint color, -1 for none
   */
  private static int getTint(ModifierModelMap modifierModels, List<FirstModifier> firstModifiers, boolean showTraits, IToolStackView tool, int index) {
    // modifier model indexes start at the last part
    List<ModifierEntry> modifiers = (showTraits ? tool.getModifiers() : tool.getUpgrades()).getModifiers();
    ModifierEntry[] firsts = new ModifierEntry[firstModifiers.size()];
    for (int i = modifiers.size() - 1; i >= 0; i--) {
      ModifierEntry entry = modifiers.get(i);
      ModifierId id = entry.getId();
      int firstIndex = FirstModifier.indexOf(firstModifiers, id);
      if (firstIndex != -1) {
        firsts[firstIndex] = entry;
      } else {
        // colors are assumed to not be sensitive to the model's large status
        IBakedModifierModel modifierModel = modifierModels.get(entry.getId());
        if (modifierModel != null) {
          // indexes from [0,modelIndexes) are passed to this model
          // if below the range, make the index model relative
          // if above the range, add the count and let the next model handle it
          int modelIndexes = modifierModel.getTintIndexes();
          if (index < modelIndexes) {
            return modifierModel.getTint(tool, entry, index);
          }
          index -= modelIndexes;
        }
      }
    }
    // first, add the first modifier tints
    for (int i = firsts.length - 1; i >= 0; i--) {
      ModifierEntry entry = firsts[i];
      FirstModifier first = firstModifiers.get(i);
      if (entry != null || first.forced) {
        IBakedModifierModel model = modifierModels.get(first.id);
        if (model != null) {
          int modelIndexes = model.getTintIndexes();
          if (index < modelIndexes) {
            if (entry == null) {
              entry = new ModifierEntry(first.id, 0);
            }
            return model.getTint(tool, entry, index);
          }
          index -= modelIndexes;
        }
      }
    }
    // iterate the constant in order of key (already reverse alphabetical)
    for (ModifierModel model : modifierModels.sortedConstant()) {
      int modelIndexes = model.getTintIndexes();
      if (index < modelIndexes) {
        return model.getTint(tool, ModifierEntry.EMPTY, index);
      }
      index -= modelIndexes;
    }
    return -1;
  }

  /** Modifier that may be forced */
  private record FirstModifier(ModifierId id, boolean forced) {
    private static final Loadable<List<FirstModifier>> LOADABLE = CompactLoadable.of(
      RecordLoadable.create(ModifierId.PARSER.requiredField("name", FirstModifier::id), BooleanLoadable.INSTANCE.defaultField("forced", false, false, FirstModifier::forced), FirstModifier::new),
      ModifierId.PARSER.flatXmap(id -> new FirstModifier(id, false), FirstModifier::id),
      f -> !f.forced).list(0);

    /** Gets the index of a modifier in the list */
    public static int indexOf(List<FirstModifier> list, ModifierId id) {
      for (int i = 0; i < list.size(); i++) {
        if (list.get(i).id.equals(id)) {
          return i;
        }
      }
      return -1;
    }
  }

  /** Adds the layers in reverse order (they are built top down for the pixel tracking) */
  private static List<BakedQuad> reversed(List<Collection<BakedQuad>> layers) {
    List<BakedQuad> quads = new ArrayList<>();
    for (int i = layers.size() - 1; i >= 0; i--) {
      quads.addAll(layers.get(i));
    }
    return quads;
  }

  /**
   * Builds the quads for one material and modifier combination, the 1.20.1 {@code bakeInternal}
   * @param settings        Model settings with the textures
   * @param spriteGetter    Sprite getter function
   * @param largeTransforms Transform to apply to the large parts. If null, only generates small parts
   * @param parts           List of tool parts in this tool
   * @param modifierModels  Map of modifier models for this tool
   * @param firstModifiers  List of modifiers to show first on the tool
   * @param showTraits      If true, shows the traits on the tool. False shows just crafted modifiers.
   * @param materials       Materials to use for the parts
   * @param tool            Tool instance for modifier parsing
   * @return  Quads for each display context
   */
  private static TinkerItemModel.Variant bakeInternal(TinkerItemModel.Settings settings, Function<Material, TextureAtlasSprite> spriteGetter, @Nullable Transformation largeTransforms,
                                                      List<ToolPart> parts, ModifierModelMap modifierModels, List<FirstModifier> firstModifiers, boolean showTraits,
                                                      List<MaterialVariantId> materials, @Nullable IToolStackView tool) {
    Transformation smallTransforms = Transformation.IDENTITY;

    // small model is used in GUIs (though that will be filtered to just front faces) and in small tool contexts like casting blocks
    List<Collection<BakedQuad>> smallQuads = new ArrayList<>();
    ItemLayerPixels smallPixels = new ItemLayerPixels();
    // large model is used in all other places
    List<Collection<BakedQuad>> largeQuads = largeTransforms != null ? new ArrayList<>() : smallQuads;
    ItemLayerPixels largePixels = largeTransforms != null ? new ItemLayerPixels() : smallPixels;

    // add quads for all modifiers first, for the sake of the item layer pixels
    if (tool != null && !modifierModels.isEmpty()) {
      addModifierQuads(spriteGetter, modifierModels, firstModifiers, showTraits, tool, smallQuads::add, smallPixels, smallTransforms, false);
      // if we have a large model, that means we will fetch models twice, once for large then again for small
      if (largeTransforms != null) {
        addModifierQuads(spriteGetter, modifierModels, firstModifiers, showTraits, tool, largeQuads::add, largePixels, largeTransforms, true);
      }
    }

    // add quads for all parts
    TextureAtlasSprite particle = null;
    for (int i = parts.size() - 1; i >= 0; i--) {
      ToolPart part = parts.get(i);

      // part with materials
      if (part.hasMaterials()) {
        // start by fetching the material we are rendering at this position, should only be null on invalid tools or during the initial bake
        int index = part.index();
        MaterialVariantId material = index < materials.size() ? materials.get(index) : MaterialId.UNKNOWN;
        TintedSprite materialSprite = MaterialModel.getMaterialSprite(spriteGetter, settings.material(part.getName(false)), material);
        particle = materialSprite.sprite();

        // need full quads for both as small is directly rendered in a few non-GUI cases
        smallQuads.add(MantleItemLayerModel.getQuadsForSprite(materialSprite.color(), -1, materialSprite.sprite(), smallTransforms, materialSprite.emissivity(), smallPixels));
        if (largeTransforms != null) {
          largeQuads.add(MaterialModel.getQuadsForMaterial(spriteGetter, settings.material(part.getName(true)), material, -1, largeTransforms, largePixels));
        }
      } else {
        // part without materials
        particle = spriteGetter.apply(settings.material(part.getName(false)));
        // same drill as above
        smallQuads.add(MantleItemLayerModel.getQuadsForSprite(-1, -1, particle, smallTransforms, 0, smallPixels));
        if (largeTransforms != null) {
          largeQuads.add(MantleItemLayerModel.getQuadsForSprite(-1, -1, spriteGetter.apply(settings.material(part.getName(true))), largeTransforms, 0, largePixels));
        }
      }
    }
    // should never happen, but just in case prevents a NPE
    if (particle == null) {
      particle = TinkerSprites.missing();
      TConstruct.LOG.error("Created tool model without a particle sprite, this means it somehow has no parts. This should not be possible");
    }

    List<BakedQuad> small = reversed(smallQuads);
    List<BakedQuad> right = largeTransforms != null ? reversed(largeQuads) : small;
    ItemQuads smallItem = ItemQuads.split(small);
    ItemQuads rightItem = largeTransforms != null ? ItemQuads.split(right) : smallItem;
    // left handed models only differ with ammo, which is drawn as its own layers
    return new TinkerItemModel.Variant(rightItem, rightItem, smallItem, ItemQuads.split(TinkerItemModel.front(small)),
      new Material.Baked(particle, false), TinkerItemModel.extents(right), TinkerItemModel.animated(right) || TinkerItemModel.animated(small));
  }

  /**
   * Data class for a single tool part
   */
  private record ToolPart(String name, int index) {
    /** Default tool part instance for breakable textures */
    public static final ToolPart DEFAULT = new ToolPart("tool", -1);
    /** Default tool part list if one is not defined */
    public static final List<ToolPart> DEFAULT_PARTS = List.of(DEFAULT);

    /**
     * If true, this part has material variants
     */
    public boolean hasMaterials() {
      return index >= 0;
    }

    /**
     * Gets the name for this part
     * @param isLarge  If true, rendering a large tool
     * @return Texture name for this part
     */
    public String getName(boolean isLarge) {
      if (isLarge) {
        return "large_" + name;
      }
      return name;
    }

    /**
     * Reads a part from JSON
     */
    public static ToolPart read(JsonObject json) {
      String name = GsonHelper.getAsString(json, "name");
      int index = GsonHelper.getAsInt(json, "index", -1);
      return new ToolPart(name, index);
    }
  }


  /* 26.3 item model */

  /** Item model definition {@code {"type": "tconstruct3:tool", "model": "<1.20.1 model>"}} */
  public record Unbaked(Identifier model) implements ItemModel.Unbaked {
    public static final MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
      Identifier.CODEC.fieldOf("model").forGetter(Unbaked::model)
    ).apply(inst, Unbaked::new));

    @Override
    public MapCodec<Unbaked> type() {
      return MAP_CODEC;
    }

    @Override
    public void resolveDependencies(ResolvableModel.Resolver resolver) {
      resolver.markDependency(model);
    }

    @Override
    public ItemModel bake(ItemModel.BakingContext context, Matrix4fc transformation) {
      ResolvedModel resolved = context.blockModelBaker().getModel(model);
      ToolModel config = TinkerItemModel.find(resolved, ToolModel.class);
      if (config == null) {
        TConstruct.LOG.error("Item model {} needs a model with loader tconstruct3:tool", model);
        return context.missingItemModel(transformation);
      }
      // warn on deprecated keys
      if (config.showTraits) {
        TConstruct.LOG.warn("Using deprecated key 'show_traits' in tool model {}, use 'constant' in modifier model maps with 'tconstruct:trait' instead", model);
      }
      if (!config.firstModifiers.isEmpty()) {
        TConstruct.LOG.warn("Using deprecated key 'first_modifiers' in tool model {}, use 'constant' in modifier model maps with 'tconstruct:crafted' and an early ID instead", model);
      }
      return new Item(TinkerItemModel.Settings.of(model, resolved, transformation), config);
    }
  }

  /** Cache key for a built variant, contains everything unique in the textures */
  private record ToolCacheKey(List<MaterialVariantId> materials, List<Object> modifierData) {}

  /** Item model building the quads for the materials and modifiers of the stack */
  public static class Item implements ItemModel {
    private final TinkerItemModel.Settings settings;
    private final ToolModel config;
    private final List<ToolPart> parts;
    /** Large tools are stretched in X and Y by 200%, and get a special offset */
    @Nullable
    private final Transformation largeTransforms;
    /** Ammo transforms (pixel offsets in model space) */
    @Nullable
    private final Matrix4f smallAmmoTransform, largeAmmoTransform, leftAmmoTransform;
    /** Modifier models, fetched when first drawn so all textures are on the atlas */
    @Nullable
    private volatile ModifierModelMap modifierModels;
    /** Variant without materials and modifiers */
    @Nullable
    private volatile TinkerItemModel.Variant base;
    /** contains all the built variants, discarded with the model on resource reload */
    private final Cache<ToolCacheKey, TinkerItemModel.Variant> cache = CacheBuilder.newBuilder()
      // ensure we can display every single tool that shows in JEI, plus a couple extra
      .maximumSize(Math.max(64, MaterialRenderInfoLoader.INSTANCE.getAllRenderInfos().size() * 3L / 2))
      .build();

    Item(TinkerItemModel.Settings settings, ToolModel config) {
      this.settings = settings;
      this.config = config;
      this.parts = config.parts();
      Vec2 offset = config.offset;
      this.largeTransforms = config.isLarge ? new Transformation(new Vector3f((offset.x - 8) / 32, (-offset.y - 8) / 32, 0), null, new Vector3f(2, 2, 1), null) : null;
      // arrow quads are flipped, and also shifted
      if (config.ammoKey != null) {
        // flipping rotates it 180 degrees, but because the origin is 0,0 instead of 0.5,0,5 it gets shifted
        Quaternionf ammoRotation = config.flipAmmo ? Axis.YP.rotationDegrees(-180) : null;
        float flipOffset = config.flipAmmo ? 1 : 0;
        Vector3f translation = new Vector3f(config.smallAmmoOffset.x / 16 + flipOffset, -config.smallAmmoOffset.y / 16, 1f / 16 + flipOffset);
        this.smallAmmoTransform = new Transformation(translation, ammoRotation, null, null).getMatrixCopy();
        if (config.isLarge) {
          translation = new Vector3f(
            (offset.x / 2 + config.largeAmmoOffset.x + 4f) / 16 + flipOffset,
            (-offset.y / 2 - config.largeAmmoOffset.y + 4f) / 16,
            1f / 16 + flipOffset);
          this.largeAmmoTransform = new Transformation(translation, ammoRotation, null, null).getMatrixCopy();
        } else {
          this.largeAmmoTransform = null;
        }
        // if we want left ammo, it copies whichever is the most recent from the two offsets
        if (config.leftAmmo) {
          translation = new Vector3f(translation);
          translation.z = -1f / 16 + flipOffset;
          this.leftAmmoTransform = new Transformation(translation, ammoRotation, null, null).getMatrixCopy();
        } else {
          this.leftAmmoTransform = null;
        }
      } else {
        this.smallAmmoTransform = null;
        this.largeAmmoTransform = null;
        this.leftAmmoTransform = null;
      }
    }

    /** Gets the modifier models for this tool */
    private ModifierModelMap modifierModels() {
      ModifierModelMap models = modifierModels;
      if (models == null) {
        models = ModifierModelMapManager.INSTANCE.getModelsForTool(TinkerSprites.GETTER, config.modifierModels, config.smallModifierRoots, config.largeModifierRoots, settings.id());
        modifierModels = models;
      }
      return models;
    }

    /** Gets the variant without materials and modifiers */
    private TinkerItemModel.Variant base() {
      TinkerItemModel.Variant variant = base;
      if (variant == null) {
        variant = bakeInternal(settings, TinkerSprites.GETTER, largeTransforms, parts, modifierModels(), config.firstModifiers, config.showTraits, List.of(), null);
        base = variant;
      }
      return variant;
    }

    /** Checks if any first modifier is forced */
    private boolean hasForced() {
      for (FirstModifier modifier : config.firstModifiers) {
        if (modifier.forced) {
          return true;
        }
      }
      return false;
    }

    @Override
    public void update(ItemStackRenderState output, ItemStack stack, ItemModelResolver resolver, ItemDisplayContext context, @Nullable ClientLevel level, @Nullable ItemOwner owner, int seed) {
      boolean useSmall = largeTransforms != null && SMALL_TOOL_TYPES.get(context.ordinal());
      ModifierModelMap modifierModels = modifierModels();

      // use material IDs for the sake of internal rendering materials
      List<MaterialVariantId> materialIds = MaterialIdNBT.from(stack).getMaterials();
      IToolStackView tool = ToolStack.from(stack);

      // if nothing unique, render original
      ModifierNBT modifiers = config.showTraits ? tool.getModifiers() : tool.getUpgrades();
      if (materialIds.isEmpty() && modifiers.isEmpty() && modifierModels.sortedConstant().isEmpty() && !hasForced()) {
        TinkerItemModel.addLayer(output, stack, context, settings, base(), useSmall, this);
        return;
      }

      // build the cache key for the modifiers, based on what the modifier requests
      // for many, it is just the modifier entry, but they can have more complex keys if needed
      ImmutableList.Builder<Object> builder = ImmutableList.builder();
      Set<ModifierId> hidden = ModifierSetWorktableRecipe.getModifierSet(tool.getPersistentData(), TConstruct.getResource("invisible_modifiers"));
      List<FirstModifier> firstModifiers = config.firstModifiers;
      ModifierEntry[] firstEntries = new ModifierEntry[firstModifiers.size()];
      int tintIndexes = 0;
      for (ModifierEntry entry : modifiers) {
        ModifierId id = entry.getId();
        int index = FirstModifier.indexOf(firstModifiers, id);
        if (index != -1) {
          // handle all the first entries together, keeps their order consistent
          firstEntries[index] = entry;
        } else if (!hidden.contains(id)) {
          IBakedModifierModel model = modifierModels.get(id);
          if (model != null) {
            tintIndexes += model.getTintIndexes();
            Object cacheKey = model.getCacheKey(tool, entry);
            if (cacheKey != null) {
              builder.add(cacheKey);
            }
          }
        }
      }
      for (int i = 0; i < firstModifiers.size(); i++) {
        FirstModifier modifier = firstModifiers.get(i);
        ModifierEntry entry = firstEntries[i];
        if (entry != null || modifier.forced) {
          IBakedModifierModel model = modifierModels.get(modifier.id);
          if (model != null) {
            tintIndexes += model.getTintIndexes();
            if (entry == null) {
              entry = new ModifierEntry(modifier.id, 0);
            }
            Object cacheKey = model.getCacheKey(tool, entry);
            if (cacheKey != null) {
              builder.add(cacheKey);
            }
          }
        }
      }
      for (ModifierModel model : modifierModels.constant().values()) {
        tintIndexes += model.getTintIndexes();
        Object cacheKey = model.getCacheKey(tool, ModifierEntry.EMPTY);
        if (cacheKey != null) {
          builder.add(cacheKey);
        }
      }

      // render special model
      ToolCacheKey key = new ToolCacheKey(materialIds, builder.build());
      TinkerItemModel.Variant variant;
      try {
        variant = cache.get(key, () -> bakeInternal(settings, TinkerSprites.GETTER, largeTransforms, parts, modifierModels, firstModifiers, config.showTraits, materialIds, tool));
      } catch (ExecutionException e) {
        TConstruct.LOG.error("Failed to get tool model from cache", e);
        variant = base();
      }
      ItemStackRenderState.LayerRenderState layer = TinkerItemModel.addLayer(output, stack, context, settings, variant, useSmall, key);

      // tints for modifiers with dynamic colors (1.20.1 item color handler)
      if (tintIndexes > 0) {
        int[] tints = new int[tintIndexes];
        for (int i = 0; i < tintIndexes; i++) {
          tints[i] = getTint(modifierModels, firstModifiers, config.showTraits, tool, i);
        }
        TinkerItemModel.addTints(output, layer, tints);
      }

      // ammo, drawn with its own item model
      if (config.ammoKey != null) {
        addAmmo(output, tool.getPersistentData(), resolver, context, level, owner, seed);
      }
    }

    /** Adds the layers of the loaded ammo, moved onto the tool */
    private void addAmmo(ItemStackRenderState output, IModDataView persistentData, ItemModelResolver resolver, ItemDisplayContext context, @Nullable ClientLevel level, @Nullable ItemOwner owner, int seed) {
      Identifier ammoKey = config.ammoKey;
      if (ammoKey == null || !persistentData.contains(ammoKey, Tag.TAG_COMPOUND)) {
        return;
      }
      CompoundTag tag = persistentData.getCompound(ammoKey);
      ItemStack ammo = ItemNbt.read(tag);
      if (ammo.isEmpty()) {
        return;
      }
      boolean left = context == ItemDisplayContext.FIRST_PERSON_LEFT_HAND || context == ItemDisplayContext.THIRD_PERSON_LEFT_HAND;
      Matrix4f transform;
      if (left && leftAmmoTransform != null) {
        transform = leftAmmoTransform;
      } else if (context != ItemDisplayContext.GUI && largeAmmoTransform != null && !SMALL_TOOL_TYPES.get(context.ordinal())) {
        transform = largeAmmoTransform;
      } else {
        transform = smallAmmoTransform;
      }
      if (transform == null) {
        return;
      }
      output.appendModelIdentityElement(ammo.getItem());
      output.appendModelIdentityElement(tag);
      // the ammo's layers use the display transform of the tool, moved by the ammo transform
      int start = AmmoLayers.count(output);
      resolver.appendItemLayers(output, ammo, context, level, owner, seed);
      AmmoLayers.retransform(output, start, settings.transforms().getTransform(context), new Matrix4f(settings.transform()).mul(transform));
    }
  }

  /** Access to the layers of a render state, to move ammo layers onto the tool */
  private static final class AmmoLayers {
    private static final Field ACTIVE = field("activeLayerCount");
    private static final Field LAYERS = field("layers");

    private static Field field(String name) {
      try {
        Field field = ItemStackRenderState.class.getDeclaredField(name);
        field.setAccessible(true);
        return field;
      } catch (ReflectiveOperationException e) {
        throw new IllegalStateException("Missing field " + name + " in ItemStackRenderState", e);
      }
    }

    /** Number of layers so far */
    static int count(ItemStackRenderState state) {
      try {
        return ACTIVE.getInt(state);
      } catch (IllegalAccessException e) {
        throw new IllegalStateException(e);
      }
    }

    /** Sets the item and local transform of the layers added after {@code start} */
    static void retransform(ItemStackRenderState state, int start, net.minecraft.client.resources.model.cuboid.ItemTransform itemTransform, Matrix4fc local) {
      try {
        int end = ACTIVE.getInt(state);
        ItemStackRenderState.LayerRenderState[] layers = (ItemStackRenderState.LayerRenderState[]) LAYERS.get(state);
        for (int i = start; i < end; i++) {
          layers[i].setItemTransform(itemTransform);
          layers[i].setLocalTransform(local);
        }
      } catch (IllegalAccessException e) {
        throw new IllegalStateException(e);
      }
    }
  }
}
