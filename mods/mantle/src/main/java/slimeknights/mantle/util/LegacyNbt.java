package slimeknights.mantle.util;

import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.CustomData;

import javax.annotation.Nullable;

/**
 * Port: turns item/fluid NBT of the 1.20.1 data format into 26.3 data components, for data that still carries an
 * {@code "nbt"} object (Mantle fluid outputs, old item stacks). Known keys get their component ({@code Potion} becomes
 * {@code minecraft:potion_contents}); everything else is kept as {@code minecraft:custom_data}.
 */
public final class LegacyNbt {
  private LegacyNbt() {}

  /** Components for the legacy NBT */
  public static DataComponentPatch toComponents(@Nullable CompoundTag nbt) {
    if (nbt == null || nbt.isEmpty()) {
      return DataComponentPatch.EMPTY;
    }
    CompoundTag rest = nbt.copy();
    DataComponentPatch.Builder builder = DataComponentPatch.builder();
    String potion = rest.getStringOr("Potion", "");
    if (!potion.isEmpty()) {
      rest.remove("Potion");
      Identifier id = Identifier.tryParse(potion);
      if (id != null) {
        BuiltInRegistries.POTION.get(id).ifPresent(holder -> builder.set(DataComponents.POTION_CONTENTS, new PotionContents(holder)));
      }
    }
    if (!rest.isEmpty()) {
      builder.set(DataComponents.CUSTOM_DATA, CustomData.of(rest));
    }
    return builder.build();
  }
}
