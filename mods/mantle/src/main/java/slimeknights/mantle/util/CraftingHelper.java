package slimeknights.mantle.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.JsonOps;
import net.minecraft.util.GsonHelper;
import net.neoforged.neoforge.common.conditions.ConditionalOps;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.conditions.ICondition.IContext;

/**
 * Port: the condition helpers of Forge's {@code slimeknights.mantle.util.CraftingHelper}, gone in NeoForge,
 * on top of NeoForge's {@link ICondition#CODEC}. Mantle and Tinkers loaders call these where 1.20.1 called Forge.
 *
 * <p>Data converted by {@code convert_data.py} keeps its conditions under {@code "neoforge:conditions"}; asking for the
 * member {@code "conditions"} therefore also checks that key. Tag and world type conditions ({@code worldgen152:world_type})
 * need the real reload context ({@code AddServerReloadListenersEvent#getConditionContext()}); with
 * {@link IContext#TAGS_INVALID} the world type condition sees no registries.
 */
public final class CraftingHelper {
  private CraftingHelper() {}

  /** Parses a single condition */
  public static ICondition getCondition(JsonObject json) {
    return ICondition.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow(JsonSyntaxException::new);
  }

  /** Checks the conditions in the given array */
  public static boolean processConditions(JsonArray conditions, IContext context) {
    for (int i = 0; i < conditions.size(); i++) {
      if (!getCondition(GsonHelper.convertToJsonObject(conditions.get(i), "conditions[" + i + "]")).test(context)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Checks the conditions in the given member of the object, true if the member is missing.
   * For {@code "conditions"}, the NeoForge key {@code "neoforge:conditions"} counts as well.
   */
  public static boolean processConditions(JsonObject json, String memberName, IContext context) {
    if (json.has(memberName) && !processConditions(GsonHelper.getAsJsonArray(json, memberName), context)) {
      return false;
    }
    String neoKey = ConditionalOps.DEFAULT_CONDITIONS_KEY;
    if ("conditions".equals(memberName) && json.has(neoKey)) {
      return processConditions(GsonHelper.getAsJsonArray(json, neoKey), context);
    }
    return true;
  }

  /** Checks all conditions */
  public static boolean processConditions(ICondition[] conditions, IContext context) {
    for (ICondition condition : conditions) {
      if (!condition.test(context)) {
        return false;
      }
    }
    return true;
  }

  /** Writes a condition to JSON */
  public static JsonElement serialize(ICondition condition) {
    return ICondition.CODEC.encodeStart(JsonOps.INSTANCE, condition).getOrThrow(IllegalStateException::new);
  }

  /** Writes a list of conditions to JSON */
  public static JsonArray serialize(ICondition... conditions) {
    JsonArray array = new JsonArray();
    for (ICondition condition : conditions) {
      array.add(serialize(condition));
    }
    return array;
  }
}
