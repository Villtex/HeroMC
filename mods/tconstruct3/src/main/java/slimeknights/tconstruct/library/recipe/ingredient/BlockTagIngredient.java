package slimeknights.tconstruct.library.recipe.ingredient;

import lombok.RequiredArgsConstructor;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.util.RegistryHelper;
import slimeknights.tconstruct.TConstruct;

import java.util.stream.Stream;

/**
 * Item ingredient matching items with a block form in the given tag.
 * <p>Port: {@link ICustomIngredient}; the matching items are looked up each time from the current block tag (1.20.1
 * cached them until the tags reloaded).
 */
@RequiredArgsConstructor
public class BlockTagIngredient implements ICustomIngredient {
  public static final Identifier ID = TConstruct.getResource("block_tag");
  /** Loader instance */
  public static final RecordLoadable<BlockTagIngredient> LOADABLE = RecordLoadable.create(
    Loadables.BLOCK_TAG.requiredField("tag", i -> i.tag), BlockTagIngredient::new);

  private final TagKey<Block> tag;

  @Override
  public boolean test(ItemStack stack) {
    return !stack.isEmpty() && stack.getItem() instanceof net.minecraft.world.item.BlockItem blockItem && blockItem.getBlock().builtInRegistryHolder().is(tag);
  }

  @SuppressWarnings("deprecation")
  @Override
  public Stream<Holder<Item>> items() {
    return RegistryHelper.getTagValueStream(BuiltInRegistries.BLOCK, tag)
                         .map(Block::asItem)
                         .filter(item -> item != Items.AIR)
                         .distinct()
                         .map(Item::builtInRegistryHolder);
  }

  @Override
  public boolean isSimple() {
    return true;
  }

  @Override
  public IngredientType<?> getType() {
    return TinkerIngredients.BLOCK_TAG.get();
  }
}
