package slimeknights.mantle.util;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import slimeknights.mantle.recipe.helper.RecipeHelper;

import javax.annotation.Nullable;
import java.util.function.Consumer;

/**
 * Port: helpers for code that kept data in item NBT in 1.20.1. Free NBT lives in {@code minecraft:custom_data} in 26.3
 * (the data converter puts unknown NBT keys there, so the keys stay the same); item stacks are saved with the
 * 26.3 codec ({@code id}, {@code count}, {@code components}).
 */
public final class ItemNbt {
  private ItemNbt() {}

  /** Copy of the custom data of the stack (1.20.1 {@code getTag()}), null if it has none */
  @Nullable
  public static CompoundTag getTag(ItemStack stack) {
    CustomData data = stack.get(DataComponents.CUSTOM_DATA);
    return data == null ? null : data.copyTag();
  }

  /** Changes the custom data of the stack (1.20.1 {@code getOrCreateTag()} and then changing it) */
  public static void update(ItemStack stack, Consumer<CompoundTag> change) {
    CustomData.update(DataComponents.CUSTOM_DATA, stack, change);
  }

  /** Saves the stack into the tag (1.20.1 {@code stack.save(tag)}), keeping other keys of the tag */
  public static CompoundTag save(ItemStack stack, CompoundTag tag) {
    if (!stack.isEmpty()) {
      ItemStack.CODEC.encodeStart(RegistryOps.create(NbtOps.INSTANCE, RecipeHelper.registryAccess()), stack).result().ifPresent(saved -> {
        if (saved instanceof CompoundTag compound) {
          tag.merge(compound);
        }
      });
    }
    return tag;
  }

  /** Reads a stack saved by {@link #save(ItemStack, CompoundTag)} (1.20.1 {@code ItemStack.of(tag)}), empty if invalid */
  public static ItemStack read(Tag tag) {
    return ItemStack.OPTIONAL_CODEC.parse(RegistryOps.create(NbtOps.INSTANCE, RecipeHelper.registryAccess()), tag).result().orElse(ItemStack.EMPTY);
  }
}
