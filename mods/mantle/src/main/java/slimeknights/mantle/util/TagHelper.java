package slimeknights.mantle.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NumericTag;

import javax.annotation.Nullable;

/**
 * Helpers to aid in reading and writing of NBT
 */
@SuppressWarnings("unused")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TagHelper {
  /* BlockPos */

  /** @deprecated use {@link net.minecraft.nbt.NbtUtils#writeBlockPos(BlockPos)} */
  @Deprecated(forRemoval = true)
  public static CompoundTag writePos(BlockPos pos) {
    CompoundTag tag = new CompoundTag();
    tag.putInt("X", pos.getX());
    tag.putInt("Y", pos.getY());
    tag.putInt("Z", pos.getZ());
    return tag;
  }

  /** @deprecated use {@link net.minecraft.nbt.NbtUtils#readBlockPos(CompoundTag)} */
  @Nullable
  @Deprecated(forRemoval = true)
  public static BlockPos readPos(CompoundTag tag) {
    if (tag.get("X") instanceof NumericTag x && tag.get("Y") instanceof NumericTag y && tag.get("Z") instanceof NumericTag z) {
      return new BlockPos(x.intValue(), y.intValue(), z.intValue());
    }
    return null;
  }

  /* 1.20.1 typed lookups, gone in 26.3 (Teil 27d) */

  /** Tag type of any number in 1.20.1 ({@code Tag.TAG_ANY_NUMERIC}) */
  public static final int TAG_ANY_NUMERIC = 99;

  /**
   * 1.20.1 {@code tag.contains(key, type)}: true if the key holds a tag of the given type ({@link net.minecraft.nbt.Tag}
   * {@code TAG_*} constants, {@link #TAG_ANY_NUMERIC} for any number)
   */
  public static boolean contains(@Nullable CompoundTag tag, String key, int type) {
    if (tag == null) {
      return false;
    }
    net.minecraft.nbt.Tag value = tag.get(key);
    if (value == null) {
      return false;
    }
    return type == TAG_ANY_NUMERIC ? value instanceof NumericTag : value.getId() == type;
  }

  /** 1.20.1 {@code tag.getList(key, type)}: the list under the key if its elements have the given type (or it is empty), else an empty list */
  public static net.minecraft.nbt.ListTag getList(@Nullable CompoundTag tag, String key, int type) {
    if (tag != null && tag.get(key) instanceof net.minecraft.nbt.ListTag list && (list.isEmpty() || list.get(0).getId() == type)) {
      return list;
    }
    return new net.minecraft.nbt.ListTag();
  }
}
