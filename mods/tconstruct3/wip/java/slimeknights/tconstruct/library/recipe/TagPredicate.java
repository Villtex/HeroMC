package slimeknights.tconstruct.library.recipe;

import net.neoforged.neoforge.fluids.FluidStack;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import slimeknights.mantle.data.loadable.common.NBTLoadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;

import javax.annotation.Nullable;
import java.util.function.Predicate;

/** Extended implementation of {@link net.minecraft.advancements.predicates.NbtPredicate} that supports syncing over the network */
public record TagPredicate(@Nullable CompoundTag tag) implements Predicate<CompoundTag> {
  /** Loadable instance */
  public static final RecordLoadable<TagPredicate> LOADABLE = NBTLoadable.ALLOW_STRING.flatXmap(TagPredicate::new, p -> p.tag);
  /** Instance that matches any NBT */
  public static final TagPredicate ANY = new TagPredicate(null);

  @Override
  public boolean test(@Nullable CompoundTag toTest) {
    return NbtUtils.compareNbt(this.tag, toTest, true);
  }

  /** Teil 27e: matches the fluid; 26.3 fluids have components instead of NBT, see {@link #legacyTag(FluidStack)} */
  public boolean test(FluidStack fluid) {
    return tag == null || test(legacyTag(fluid));
  }

  /**
   * NBT of a fluid in the form of 1.20.1 for the predicates in the data: {@code minecraft:custom_data} at the root,
   * the potion as {@code Potion}. TODO 27g: the bottle type of potion fluids ({@code Bottle}) once the fluids exist.
   */
  public static CompoundTag legacyTag(FluidStack fluid) {
    CompoundTag nbt = new CompoundTag();
    net.minecraft.world.item.component.CustomData custom = fluid.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA);
    if (custom != null) {
      nbt.merge(custom.copyTag());
    }
    net.minecraft.world.item.alchemy.PotionContents potion = fluid.get(net.minecraft.core.component.DataComponents.POTION_CONTENTS);
    if (potion != null) {
      potion.potion().flatMap(net.minecraft.core.Holder::unwrapKey).ifPresent(key -> nbt.putString("Potion", key.identifier().toString()));
    }
    return nbt;
  }
}
