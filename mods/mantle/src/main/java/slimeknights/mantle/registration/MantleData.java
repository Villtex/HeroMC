package slimeknights.mantle.registration;

import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import slimeknights.mantle.Mantle;

/**
 * Port: data components of Mantle, replacing the item NBT keys of 1.20.1 (as in Mantle 1.21).
 */
public final class MantleData {
  private static final DeferredRegister.DataComponents DATA_COMPONENTS = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, Mantle.modId);

  private MantleData() {}

  /** Registers this to the bus */
  public static void init(IEventBus bus) {
    DATA_COMPONENTS.register(bus);
  }

  /** Block used as texture by {@link slimeknights.mantle.util.RetexturedHelper} (NBT {@code texture} in 1.20.1) */
  public static final DeferredHolder<DataComponentType<?>, DataComponentType<Block>> BLOCK_TEXTURE = DATA_COMPONENTS.registerComponentType("block_texture",
    builder -> builder.persistent(BuiltInRegistries.BLOCK.byNameCodec()).networkSynchronized(ByteBufCodecs.registry(Registries.BLOCK)));

  /** Soulbound slot, kept on an item between death and respawn by {@code MantleEvents}. Not saved. */
  public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> SOULBOUND_SLOT = DATA_COMPONENTS.registerComponentType("soulbound_slot",
    builder -> builder.networkSynchronized(ByteBufCodecs.VAR_INT));

  /** Last page viewed in a book (book engine, NBT {@code mantle:book_page} in 1.20.1) */
  public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> BOOK_PAGE = DATA_COMPONENTS.registerComponentType("book_page",
    builder -> builder.persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8));
}
