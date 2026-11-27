package slimeknights.mantle.data.listener;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.neoforge.common.conditions.ConditionalOps;
import net.neoforged.neoforge.common.conditions.ICondition.IContext;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.util.CraftingHelper;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Port: Minecraft 1.20.1's Gson based {@code SimpleJsonResourceReloadListener} (26.3 only has a codec based one), so
 * Mantle and Tinkers loaders keep their {@code apply(Map<Identifier,JsonElement>, ...)} form.
 *
 * <p>Files whose {@code "neoforge:conditions"} fail (for instance {@code worldgen152:world_type} in the wrong world type)
 * are left out, using the reload context NeoForge injects into server listeners. The registry lookup of the running
 * reload is kept in {@link #currentRegistryLookup()} for parsing data components and other registry based values.
 */
public abstract class JsonReloadListener extends SimplePreparableReloadListener<Map<Identifier,JsonElement>> {
  private static volatile HolderLookup.Provider currentLookup = RegistryAccess.EMPTY;
  private static volatile IContext currentContext = IContext.EMPTY;

  private final Gson gson;
  private final String directory;

  protected JsonReloadListener(Gson gson, String directory) {
    this.gson = gson;
    this.directory = directory;
  }

  /** Registry lookup of the latest server data reload (empty before the first one) */
  public static HolderLookup.Provider currentRegistryLookup() {
    return currentLookup;
  }

  /** Condition context of the latest server data reload */
  public static IContext currentConditionContext() {
    return currentContext;
  }

  /** Condition context for this reload */
  protected IContext getConditionContext() {
    return getContext();
  }

  /** Same as 1.20.1's {@code SimpleJsonResourceReloadListener.scanDirectory}: reads all JSON files of the folder into the map */
  public static void scanDirectory(ResourceManager manager, String directory, Gson gson, Map<Identifier,JsonElement> output) {
    FileToIdConverter lister = FileToIdConverter.json(directory);
    for (Entry<Identifier,Resource> entry : lister.listMatchingResources(manager).entrySet()) {
      Identifier file = entry.getKey();
      Identifier id = lister.fileToId(file);
      try (Reader reader = entry.getValue().openAsReader()) {
        JsonElement json = GsonHelper.fromJson(gson, reader, JsonElement.class);
        if (output.put(id, json) != null) {
          throw new IllegalStateException("Duplicate data file ignored with ID " + id);
        }
      } catch (IllegalArgumentException | IOException | JsonParseException ex) {
        Mantle.logger.error("Couldn't parse data file {} from {}", id, file, ex);
      }
    }
  }

  @Override
  protected Map<Identifier,JsonElement> prepare(ResourceManager manager, ProfilerFiller profiler) {
    if (getRegistryLookup() != RegistryAccess.EMPTY) {
      currentLookup = getRegistryLookup();
      currentContext = getContext();
    }
    Map<Identifier,JsonElement> map = new HashMap<>();
    FileToIdConverter lister = FileToIdConverter.json(directory);
    for (Entry<Identifier,Resource> entry : lister.listMatchingResources(manager).entrySet()) {
      Identifier file = entry.getKey();
      Identifier id = lister.fileToId(file);
      try (Reader reader = entry.getValue().openAsReader()) {
        JsonElement json = GsonHelper.fromJson(gson, reader, JsonElement.class);
        if (json instanceof JsonObject object && object.has(ConditionalOps.DEFAULT_CONDITIONS_KEY)
            && !CraftingHelper.processConditions(GsonHelper.getAsJsonArray(object, ConditionalOps.DEFAULT_CONDITIONS_KEY), getContext())) {
          continue;
        }
        if (map.put(id, json) != null) {
          throw new IllegalStateException("Duplicate data file ignored with ID " + id);
        }
      } catch (IllegalArgumentException | IOException | JsonParseException ex) {
        Mantle.logger.error("Couldn't parse data file {} from {}", id, file, ex);
      }
    }
    return map;
  }
}
