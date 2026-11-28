package slimeknights.tconstruct.library.tools.part;

import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.library.client.materials.MaterialTooltipCache;
import slimeknights.tconstruct.library.materials.MaterialRegistry;
import slimeknights.tconstruct.library.materials.definition.IMaterial;
import slimeknights.tconstruct.library.materials.definition.MaterialId;
import slimeknights.tconstruct.library.materials.definition.MaterialVariantId;
import slimeknights.tconstruct.library.tools.helper.TooltipUtil;
import slimeknights.tconstruct.library.utils.DomainDisplayName;
import slimeknights.tconstruct.library.utils.Util;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Represents an item that has a Material associated with it. The NBT of the itemstack identifies which material the
 * itemstack of this item has.
 */
public class MaterialItem extends Item implements IMaterialItem {
  private static final String ADDED_BY = TConstruct.makeTranslationKey("tooltip", "part.added_by");

  public MaterialItem(Properties properties) {
    super(properties);
  }

  /** Gets the material ID for the given NBT compound */
  public static MaterialVariantId getMaterialId(@Nullable CompoundTag nbt) {
    if (nbt != null) {
      String str = nbt.getStringOr(MATERIAL_TAG, "");
      if (!str.isEmpty()) {
        MaterialVariantId id = MaterialVariantId.tryParse(str);
        if (id != null) {
          return id;
        }
      }
    }
    return MaterialId.UNKNOWN;
  }

  @Override
  public MaterialVariantId getMaterial(ItemStack stack) {
    return getMaterialId(stack);
  }

  /** Gets the material ID of the given stack (26.3: data component tconstruct3:material) */
  public static MaterialVariantId getMaterialId(ItemStack stack) {
    MaterialVariantId id = stack.get(slimeknights.tconstruct.library.tools.nbt.ToolDataComponents.MATERIAL.get());
    return id == null ? MaterialId.UNKNOWN : id;
  }

  @Nullable
  private static Component getName(String baseKey, MaterialVariantId material) {
    // if there is a specific name, use that
    Identifier location = material.getLocation('.');
    String fullKey = String.format("%s.%s.%s", baseKey, location.getNamespace(), location.getPath());
    if (Util.canTranslate(fullKey)) {
      return Component.translatable(fullKey);
    }
    // try material name prefix next
    String materialKey = MaterialTooltipCache.getKey(material);
    String materialPrefix = materialKey + ".format";
    if (Util.canTranslate(materialPrefix)) {
      return Component.translatable(materialPrefix, Component.translatable(baseKey));
    }
    // format as "<material> <item name>"
    if (Util.canTranslate(materialKey)) {
      return Component.translatable(TooltipUtil.KEY_FORMAT, Component.translatable(materialKey), Component.translatable(baseKey));
    }
    return null;
  }

  /** Static helper to get part name, used also by {@code slimeknights.tconstruct.library.tools.part.block.MaterialBlockItem} */
  public static Component getName(IMaterialItem self, ItemStack stack) {
    // if no material, return part name directly
    MaterialVariantId material = self.getMaterial(stack);
    String key = self.asItem().getDescriptionId();
    if (material.equals(MaterialId.UNKNOWN)) {
      return Component.translatable(key);
    }
    // try variant first
    if (material.hasVariant()) {
      Component component = getName(key, material);
      if (component != null) {
        return component;
      }
    }
    // if variant did not work, do base material
    Component component = getName(key, material.getId());
    if (component != null) {
      return component;
    }
    // if neither worked, format directly
    return Component.translatable(key);
  }

  @Override
  public Component getName(ItemStack stack) {
    return getName(this, stack);
  }

  /** Rarity of the material (26.3: items have a rarity component; TODO 27f: show it in the name color) */
  public Rarity getRarity(ItemStack stack) {
    return MaterialRegistry.getMaterial(getMaterial(stack).getId()).getRarity();
  }

  @Override
  public boolean isFoil(ItemStack stack) {
    return MaterialRegistry.getInstance().isInTag(getMaterial(stack).getId(), TinkerTags.Materials.SHINY);
  }

  /** Helper to append the material ID to the tool part. */
  public static void appendHoverText(IMaterialItem self, ItemStack stack, List<Component> tooltip, TooltipFlag flag) {
    if (flag.isAdvanced() && !TooltipUtil.isDisplay(stack)) {
      MaterialVariantId materialVariant = self.getMaterial(stack);
      if (!materialVariant.equals(MaterialId.UNKNOWN)) {
        tooltip.add((Component.translatable(ToolPartItem.MATERIAL_KEY, materialVariant.toString())).withStyle(ChatFormatting.DARK_GRAY));
      }
    }
  }

  @Override
  public void appendHoverText(ItemStack stack, TooltipContext context, net.minecraft.world.item.component.TooltipDisplay display, java.util.function.Consumer<Component> tooltip, TooltipFlag flag) {
    List<Component> lines = new java.util.ArrayList<>();
    appendHoverText(stack, context.level(), lines, flag);
    lines.forEach(tooltip);
    super.appendHoverText(stack, context, display, tooltip, flag);
  }

  /** 1.20.1 tooltip form, kept for subclasses */
  public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flag) {
    appendHoverText(this, stack, tooltip, flag);
  }

  /** Gets the creator mod ID based on the material. */
  @SuppressWarnings("deprecation")  // deprecation? more like not deprecation
  public static String getCreatorModId(IMaterialItem self, ItemStack stack) {
    MaterialVariantId material = self.getMaterial(stack);
    if (!MaterialId.UNKNOWN.equals(material)) {
      String namespace = material.getId().getNamespace();
      // skip if it's a tinkers material; we want addon tool parts to prefer showing their mod ID as end users mistake those for us
      if (!TConstruct.MOD_ID.equals(namespace)) {
        return namespace;
      }
    }
    return BuiltInRegistries.ITEM.getKey(stack.getItem()).getNamespace();
  }

  @Nullable
  @Override
  public String getCreatorModId(net.minecraft.core.HolderLookup.Provider registries, ItemStack stack) {
    return getCreatorModId(this, stack);
  }


  /**
   * Adds the mod that added the material to the tooltip
   * @param tooltip   Tooltip list
   * @param material  Material to add
   */
  @Deprecated(forRemoval = true)
  protected static void addModTooltip(IMaterial material, List<Component> tooltip) {
    if (material != IMaterial.UNKNOWN) {
      tooltip.add(Component.empty());
      tooltip.add(Component.translatable(ADDED_BY, DomainDisplayName.nameFor(material.getIdentifier().getNamespace())));
    }
  }

  /** Resolves material redirects of the stack (1.20.1 verifyTagAfterLoad; TODO 27f: call it when stacks load) */
  public static void verifyTag(ItemStack stack) {
    // if the material exists and was changed, update it
    MaterialVariantId id = getMaterialId(stack);
    if (!id.equals(MaterialId.UNKNOWN)) {
      MaterialId original = id.getId();
      MaterialId resolved = MaterialRegistry.getInstance().resolve(original);
      if (original != resolved) {
        stack.set(slimeknights.tconstruct.library.tools.nbt.ToolDataComponents.MATERIAL.get(), MaterialVariantId.create(resolved, id.getVariant()));
      }
    }
  }
}
