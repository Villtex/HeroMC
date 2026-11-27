package slimeknights.tconstruct.library.recipe.worktable;

import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import slimeknights.tconstruct.library.modifiers.ModifierId;
import slimeknights.tconstruct.library.tools.nbt.IModDataView;

import java.util.Iterator;

/**
 * Sets of modifiers stored in tool data as a list of modifier IDs (for example which modifiers react to left or right
 * click). The static helpers of {@code ModifierSetWorktableRecipe} (27f), here so the tool library can use them first.
 */
public final class ModifierSets {
  private ModifierSets() {}

  /** Checks if the given modifier is in the set under the key */
  public static boolean isInSet(IModDataView modData, Identifier key, ModifierId modifier) {
    if (!modData.contains(key, Tag.TAG_LIST)) {
      return false;
    }
    return isInSet(modData.getList(key, Tag.TAG_STRING), modifier, false);
  }

  /** Checks if the given modifier is in the set. Removes the modifier if requested. */
  public static boolean isInSet(ListTag list, ModifierId modifier, boolean remove) {
    String modifierStr = modifier.toString();
    Iterator<Tag> iterator = list.iterator();
    while (iterator.hasNext()) {
      if (iterator.next().asString().filter(modifierStr::equals).isPresent()) {
        if (remove) {
          iterator.remove();
        }
        return true;
      }
    }
    return false;
  }
}
