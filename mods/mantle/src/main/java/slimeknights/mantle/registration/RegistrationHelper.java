package slimeknights.mantle.registration;

import com.mojang.brigadier.arguments.ArgumentType;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.minecraft.core.DefaultedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.WoodType;
import slimeknights.mantle.util.RegistryHelper;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RegistrationHelper {
  /** Wood types to register with the texture atlas */
  private static final List<WoodType> WOOD_TYPES = new ArrayList<>();

  /**
   * Properties for a standard bucket item.
   * Port: shared properties are fine for Mantle's registers, the item ID comes from {@link RegistrationContext}.
   */
  public static final Item.Properties BUCKET_PROPS = new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1);

  /**
   * Used to mark injected registry objects, as despite being set to null they will be nonnull at runtime.
   * @param <T>  Class type
   * @return  Null, its a lie
   */
  @SuppressWarnings("ConstantConditions")
  public static <T> T injected() {
    return null;
  }

  /**
   * Gets a holder for a registry object
   * @param registry  Registry instance
   * @param entry     Entry to fetch holder
   * @param <T>       Registry type
   * @param <R>       Return type, typically but not strictly registry type
   * @return  Supplier for the given registry casted to the requested type
   */
  @SuppressWarnings("unchecked")  // we know the entry is the given type
  public static <T, R extends T> Supplier<R> getCastedHolder(DefaultedRegistry<T> registry, T entry) {
    Supplier<T> holder = RegistryHelper.getHolder(registry, entry);
    return () -> (R) holder.get();
  }

  /** Registers a wood type to be injected into the atlas, should be called before client setup */
  public static void registerWoodType(WoodType type) {
    synchronized (WOOD_TYPES) {
      WOOD_TYPES.add(type);
      WoodType.register(type);
    }
  }

  /** Runs the given consumer for each wood type registered */
  public static void forEachWoodType(Consumer<WoodType> consumer) {
    WOOD_TYPES.forEach(consumer);
  }

  /* Sign blocks */

  /** Sign blocks to use for the block entity valid blocks */
  private static final List<Supplier<? extends Block>> SIGN_BLOCKS = new ArrayList<>();
  /** Hanging sign blocks to use for the block entity valid blocks */
  private static final List<Supplier<? extends Block>> HANGING_SIGN_BLOCKS = new ArrayList<>();

  /**
   * Registers a sign block to be added to the vanilla sign block entity, should be called during registration.
   * Port: as in Mantle 1.21, signs use vanilla's sign block entity ({@code BlockEntityTypeAddBlocksEvent})
   */
  public static void registerSignBlock(Supplier<? extends Block> sign) {
    synchronized (SIGN_BLOCKS) {
      SIGN_BLOCKS.add(sign);
    }
  }

  /** Builds the list of sign blocks for the block entity */
  public static Block[] buildSignBlocks() {
    return SIGN_BLOCKS.stream().map(Supplier::get).toArray(Block[]::new);
  }

  /** Registers a hanging sign block to be added to the vanilla hanging sign block entity, should be called during registration */
  public static void registerHangingSignBlock(Supplier<? extends Block> sign) {
    synchronized (HANGING_SIGN_BLOCKS) {
      HANGING_SIGN_BLOCKS.add(sign);
    }
  }

  /** Builds the list of hanging sign blocks for the block entity */
  public static Block[] buildHangingSignBlocks() {
    return HANGING_SIGN_BLOCKS.stream().map(Supplier::get).toArray(Block[]::new);
  }

  /** Casts the class type to make it a valid argument type */
  @SuppressWarnings("unchecked")
  public static <T extends ArgumentType<?>> Class<T> genericArgumentType(Class<? super T> type) {
    return (Class<T>) type;
  }
}
