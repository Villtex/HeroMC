package slimeknights.tconstruct.library.utils;

import net.minecraft.resources.Identifier;
import slimeknights.tconstruct.TConstruct;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * Port (Teil 27e): attribute modifiers are identified by an {@link Identifier} in 26.3 instead of a UUID. Modules
 * keep the UUIDs of 1.20.1 (derived from their unique name and slot) and use them as path, so IDs stay unique.
 */
public final class AttributeIds {
  private AttributeIds() {}

  /** ID of the attribute modifier for the UUID of 1.20.1, null for null */
  @Nullable
  public static Identifier of(@Nullable UUID uuid) {
    return uuid == null ? null : TConstruct.getResource("attribute/" + uuid);
  }
}
