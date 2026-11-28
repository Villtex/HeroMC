package slimeknights.tconstruct.library.tools.nbt;

import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Unit;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.materials.definition.MaterialVariantId;

import java.util.function.Function;

/**
 * Data components of Tinkers' tools and parts (Teil 27d). In 1.20.1 all of this was NBT of the stack
 * ({@code tic_materials}, {@code tic_modifiers} ...), now each value is a component, stored in the same NBT shape as
 * before (so data and saved items convert one to one, see {@code tools/convert_data.py}):
 * <ul>
 *   <li>{@link #MATERIALS} {@code tconstruct3:materials}: material variants of the parts ({@code tic_materials})</li>
 *   <li>{@link #UPGRADES} {@code tconstruct3:upgrades}: modifiers added by recipes ({@code tic_upgrades})</li>
 *   <li>{@link #MODIFIERS} {@code tconstruct3:modifiers}: all modifiers incl. traits, calculated ({@code tic_modifiers})</li>
 *   <li>{@link #STATS} {@code tconstruct3:stats}, {@link #MULTIPLIERS} {@code tconstruct3:multipliers}: calculated stats</li>
 *   <li>{@link #PERSISTENT_DATA} {@code tconstruct3:persistent_data} ({@code tic_persistent}),
 *       {@link #VOLATILE_DATA} {@code tconstruct3:volatile_data} ({@code tic_volatile_data}): modifier data</li>
 *   <li>{@link #BROKEN} {@code tconstruct3:broken} ({@code tic_broken}); damage is {@code minecraft:damage}</li>
 *   <li>{@link #DISPLAY} {@code tconstruct3:display}: display only tool or part ({@code tic_display})</li>
 *   <li>{@link #MATERIAL} {@code tconstruct3:material}: material of a part or material item ({@code Material})</li>
 * </ul>
 * Components are immutable: {@link ToolStack} writes them back after each change.
 */
public final class ToolDataComponents {
  private ToolDataComponents() {}

  public static final DeferredRegister.DataComponents REGISTER = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, TConstruct.MOD_ID);

  public static final DeferredHolder<DataComponentType<?>, DataComponentType<MaterialNBT>> MATERIALS = nbt("materials", MaterialNBT::readFromNBT, MaterialNBT::serializeToNBT);
  public static final DeferredHolder<DataComponentType<?>, DataComponentType<ModifierNBT>> UPGRADES = nbt("upgrades", ModifierNBT::readFromNBT, ModifierNBT::serializeToNBT);
  public static final DeferredHolder<DataComponentType<?>, DataComponentType<ModifierNBT>> MODIFIERS = nbt("modifiers", ModifierNBT::readFromNBT, ModifierNBT::serializeToNBT);
  public static final DeferredHolder<DataComponentType<?>, DataComponentType<StatsNBT>> STATS = nbt("stats", StatsNBT::readFromNBT, StatsNBT::serializeToNBT);
  public static final DeferredHolder<DataComponentType<?>, DataComponentType<MultiplierNBT>> MULTIPLIERS = nbt("multipliers", MultiplierNBT::readFromNBT, MultiplierNBT::serializeToNBT);
  public static final DeferredHolder<DataComponentType<?>, DataComponentType<CompoundTag>> PERSISTENT_DATA = REGISTER.registerComponentType("persistent_data", builder -> builder.persistent(CompoundTag.CODEC).networkSynchronized(ByteBufCodecs.COMPOUND_TAG));
  public static final DeferredHolder<DataComponentType<?>, DataComponentType<CompoundTag>> VOLATILE_DATA = REGISTER.registerComponentType("volatile_data", builder -> builder.persistent(CompoundTag.CODEC).networkSynchronized(ByteBufCodecs.COMPOUND_TAG));
  public static final DeferredHolder<DataComponentType<?>, DataComponentType<Unit>> BROKEN = REGISTER.registerComponentType("broken", builder -> builder.persistent(Unit.CODEC).networkSynchronized(net.minecraft.network.codec.StreamCodec.unit(Unit.INSTANCE)));
  /** Marks a tool or part only shown in a book or JEI, never used ({@code tic_display}): its stats are not rebuilt */
  public static final DeferredHolder<DataComponentType<?>, DataComponentType<Unit>> DISPLAY = REGISTER.registerComponentType("display", builder -> builder.persistent(Unit.CODEC).networkSynchronized(net.minecraft.network.codec.StreamCodec.unit(Unit.INSTANCE)));
  public static final DeferredHolder<DataComponentType<?>, DataComponentType<MaterialVariantId>> MATERIAL = REGISTER.registerComponentType("material", builder -> builder.persistent(MaterialVariantId.CODEC).networkSynchronized(ByteBufCodecs.fromCodec(MaterialVariantId.CODEC)));

  /** Registers the components */
  public static void register(IEventBus modBus) {
    REGISTER.register(modBus);
  }

  /** Codec of a value stored as NBT in 1.20.1, keeping that NBT shape */
  public static <T> Codec<T> nbtCodec(Function<Tag, T> read, Function<T, ? extends Tag> write) {
    return ExtraCodecs.NBT.xmap(read, write::apply);
  }

  /** Registers a component stored as NBT in 1.20.1, keeping that NBT shape */
  private static <T> DeferredHolder<DataComponentType<?>, DataComponentType<T>> nbt(String name, Function<Tag, T> read, Function<T, ? extends Tag> write) {
    Codec<T> codec = nbtCodec(read, write);
    return REGISTER.registerComponentType(name, builder -> builder.persistent(codec).networkSynchronized(ByteBufCodecs.fromCodec(codec)));
  }
}
