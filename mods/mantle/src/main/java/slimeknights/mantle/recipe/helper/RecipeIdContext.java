package slimeknights.mantle.recipe.helper;

import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import slimeknights.mantle.Mantle;

import javax.annotation.Nullable;

/**
 * Port: ID of the registry element (recipe, loot table) being decoded on this thread, set by
 * {@code slimeknights.mantle.mixin.PendingRegistrationMixin}. 26.3 decodes recipes through codecs without their ID;
 * {@link LoadableRecipeSerializer} puts this ID into {@link slimeknights.mantle.data.loadable.field.ContextKey#ID}.
 */
public final class RecipeIdContext {
  /** Used when a recipe is decoded outside of a registry load (for instance a codec test) */
  public static final Identifier UNKNOWN = Mantle.getResource("unknown_recipe");
  private static final ThreadLocal<Identifier> CURRENT = new ThreadLocal<>();

  private RecipeIdContext() {}

  public static void start(ResourceKey<?> key) {
    CURRENT.set(key.identifier());
  }

  public static void end() {
    CURRENT.remove();
  }

  /** ID being decoded, or null outside of a registry load */
  @Nullable
  public static Identifier get() {
    return CURRENT.get();
  }

  /** ID being decoded, or {@link #UNKNOWN} */
  public static Identifier getOrUnknown() {
    Identifier id = CURRENT.get();
    return id != null ? id : UNKNOWN;
  }
}
