package slimeknights.mantle.registration;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import javax.annotation.Nullable;
import java.util.function.Supplier;

/**
 * Port: in 26.3 {@link Item.Properties} and block properties need the registry key ({@code setId}) before the item or
 * block is constructed, else "Item id not set". Mantle's registers keep the 1.20.1 API ({@code register(name, () -> new Item(props))}),
 * so they run each supplier inside {@link #create(ResourceKey, boolean, Supplier)}; the mixins
 * {@code ItemPropertiesMixin} and {@code BlockPropertiesMixin} use the key of the entry being created when the
 * properties have none. Shared properties objects keep working that way, as nothing is written to them.
 */
public final class RegistrationContext {
  private RegistrationContext() {}

  /** Entry being created */
  private record Entry(ResourceKey<?> key, boolean blockItem) {}

  private static final ThreadLocal<Entry> CURRENT = new ThreadLocal<>();

  /**
   * Runs the supplier with the key as the entry being created
   * @param key        Registry key of the entry
   * @param blockItem  If true, the entry is the item of a block: it uses the block description ID ({@code block.ns.name}) as in 1.20.1
   * @param supplier   Constructor of the entry
   */
  public static <T> T create(ResourceKey<?> key, boolean blockItem, Supplier<T> supplier) {
    Entry previous = CURRENT.get();
    CURRENT.set(new Entry(key, blockItem));
    try {
      return supplier.get();
    } finally {
      if (previous == null) {
        CURRENT.remove();
      } else {
        CURRENT.set(previous);
      }
    }
  }

  /** Same as {@link #create(ResourceKey, boolean, Supplier)} for anything but block items */
  public static <T> T create(ResourceKey<?> key, Supplier<T> supplier) {
    return create(key, false, supplier);
  }

  /** Key of the item being created, null if none */
  @SuppressWarnings("unchecked")
  @Nullable
  public static ResourceKey<Item> currentItem() {
    Entry entry = CURRENT.get();
    return entry != null && entry.key.isFor(Registries.ITEM) ? (ResourceKey<Item>) entry.key : null;
  }

  /** True if the item being created is the item of a block */
  public static boolean isBlockItem() {
    Entry entry = CURRENT.get();
    return entry != null && entry.blockItem && entry.key.isFor(Registries.ITEM);
  }

  /** Key of the block being created, null if none */
  @SuppressWarnings("unchecked")
  @Nullable
  public static ResourceKey<Block> currentBlock() {
    Entry entry = CURRENT.get();
    return entry != null && entry.key.isFor(Registries.BLOCK) ? (ResourceKey<Block>) entry.key : null;
  }
}
