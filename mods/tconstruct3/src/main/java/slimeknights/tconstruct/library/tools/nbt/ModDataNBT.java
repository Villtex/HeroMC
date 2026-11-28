package slimeknights.tconstruct.library.tools.nbt;

import javax.annotation.Nullable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;

import java.util.function.BiFunction;

/**
 * NBT representing extra data on the tool for modifiers, with a wrapper around the compound for to enforce namespacing data.
 * On a typical tool, there are two copies of this class, one for persistent data, and one that rebuilds when the modifiers refresh.
 * Note unlike other NBT classes, the data inside this one is mutable as most of it is directly used by the tools.
 */
@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class ModDataNBT implements IModDataView {
  /** Compound representing modifier data */
  @Getter(AccessLevel.PROTECTED)
  private final CompoundTag data;
  /**
   * 26.3: called after every change, set by the tool stack to write the data back into its data component
   * (components are immutable, 1.20.1 changed the NBT of the stack in place). Not part of equality.
   */
  @Nullable
  @EqualsAndHashCode.Exclude
  private Runnable onChange;

  /**
   * Creates a new mod data containing empty data
   */
  public ModDataNBT() {
    this(new CompoundTag(), null);
  }

  protected ModDataNBT(CompoundTag data) {
    this(data, null);
  }

  /** Sets the listener called after each change (tool stacks write the data component with it) */
  public void setChangeListener(@Nullable Runnable onChange) {
    this.onChange = onChange;
  }

  /** Notifies the listener of a change */
  protected void changed() {
    if (onChange != null) {
      onChange.run();
    }
  }

  @Override
  public <T> T get(Identifier name, BiFunction<CompoundTag,String,T> function) {
    return function.apply(data, name.toString());
  }

  @Override
  public ListTag getList(Identifier name, int type) {
    // save generation of the extra lambda object
    return slimeknights.mantle.util.TagHelper.getList(data, name.toString(), type);
  }

  @Override
  public boolean contains(Identifier name) {
    return data.contains(name.toString());
  }

  @Override
  public boolean contains(Identifier name, int type) {
    return slimeknights.mantle.util.TagHelper.contains(data, name.toString(), type);
  }

  /**
   * Sets the given NBT into the data
   * @param name  Key name
   * @param nbt   NBT value
   */
  public void put(Identifier name, Tag nbt) {
    data.put(name.toString(), nbt);
    changed();
  }

  /**
   * Sets an integer from the mod data
   * @param name  Name
   * @param value  Integer value
   */
  public void putInt(Identifier name, int value) {
    data.putInt(name.toString(), value);
    changed();
  }

  /**
   * Sets an boolean from the mod data
   * @param name  Name
   * @param value  Boolean value
   */
  public void putBoolean(Identifier name, boolean value) {
    data.putBoolean(name.toString(), value);
    changed();
  }

  /**
   * Sets an float from the mod data
   * @param name  Name
   * @param value  Float value
   */
  public void putFloat(Identifier name, float value) {
    data.putFloat(name.toString(), value);
    changed();
  }

  /**
   * Reads a string from the mod data
   * @param name  Name
   * @param value  String value
   */
  public void putString(Identifier name, String value) {
    data.putString(name.toString(), value);
    changed();
  }

  /**
   * Removes the given key from the NBT
   * @param name  Key to remove
   */
  public void remove(Identifier name) {
    data.remove(name.toString());
    changed();
  }


  /* Networking */

  /** Gets a copy of the internal data, generally should only be used for syncing, no reason to call directly */
  public CompoundTag getCopy() {
    return data.copy();
  }

  /**
   * Called to merge this NBT data from another
   * @param data  data
   */
  public void copyFrom(CompoundTag data) {
    for (String key : java.util.List.copyOf(this.data.keySet())) {
      this.data.remove(key);
    }
    this.data.merge(data);
    changed();
  }

  /**
   * Parses the data from NBT
   * @param data  data
   * @return  Parsed mod data
   */
  public static ModDataNBT readFromNBT(CompoundTag data) {
    return new ModDataNBT(data);
  }
}
