package slimeknights.tconstruct.library.tools.capability;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import slimeknights.mantle.registration.object.IdAwareObject;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.utils.LazyOptional;

import javax.annotation.Nullable;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Capability to make it easy for Tinkers to store common data on the player, primarily used for armor
 * Data stored in this capability is not saved to NBT, most often its filled by the relevant equipment events.
 * <p>26.3: a data attachment ({@code tconstruct3:modifier_data}) on living entities instead of a Forge capability.
 */
public class TinkerDataCapability {
  private TinkerDataCapability() {}

  /** Register for all Tinkers data attachments (entity data that used to be Forge capabilities) */
  public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, TConstruct.MOD_ID);
  /** Attachment type, not saved */
  public static final DeferredHolder<AttachmentType<?>, AttachmentType<Holder>> ATTACHMENT = ATTACHMENT_TYPES.register("modifier_data", () -> AttachmentType.builder(Holder::new).build());

  /** Registers the attachments (this one, {@link PersistentDataCapability}, {@link EntityModifierCapability}) */
  public static void register(IEventBus modBus) {
    ATTACHMENT_TYPES.register(modBus);
  }

  /** Gets the data of a living entity, empty for other entities. Replaces {@code entity.getCapability(CAPABILITY)} */
  public static LazyOptional<Holder> get(@Nullable Entity entity) {
    if (entity instanceof LivingEntity living) {
      return LazyOptional.of(() -> living.getData(ATTACHMENT));
    }
    return LazyOptional.empty();
  }

  /** Gets the data capability from an entity, or null if missing */
  @Nullable
  public static TinkerDataCapability.Holder getData(LivingEntity entity) {
    return entity.getData(ATTACHMENT);
  }

  /** Class for generic keys */
  @SuppressWarnings("unused")
  @RequiredArgsConstructor(staticName = "of")
  public static class TinkerDataKey<T> implements IdAwareObject {
    /** Name for debug */
    @Getter
    private final Identifier id;

    @Override
    public String toString() {
      return "TinkerDataKey{" + id + '}';
    }
  }

  /** Extension key that can automatically create an instance if missing */
  public static class ComputableDataKey<T> extends TinkerDataKey<T> implements Function<TinkerDataKey<?>, T> {
    private final Supplier<T> constructor;
    private ComputableDataKey(Identifier name, Supplier<T> constructor) {
      super(name);
      this.constructor = constructor;
    }

    /** Creates a new instance */
    public static <T> ComputableDataKey<T> of(Identifier name, Supplier<T> constructor) {
      return new ComputableDataKey<>(name, constructor);
    }

    @Override
    public T apply(TinkerDataKey<?> tinkerDataKey) {
      return constructor.get();
    }
  }


  /** Data class holding the tinker data */
  public static class Holder {
    private final Map<TinkerDataKey<?>, Object> data = new IdentityHashMap<>();

    /**
     * Adds a value to the holder
     * @param key    Key to add
     * @param value  Value to add
     * @param <T>    Data type
     */
    public <T> void put(TinkerDataKey<T> key, T value) {
      data.put(key, value);
    }

    /**
     * Adds the given value to the float data key
     * @param key    Key to add
     * @param value  Value to add
     */
    public void add(TinkerDataKey<Float> key, float value) {
      float newValue = get(key, 0f) + value;
      if (newValue == 0) {
        data.remove(key);
      } else {
        data.put(key, newValue);
      }
    }

    /**
     * Removes a value to the holder
     * @param key  Key to remove
     */
    public void remove(TinkerDataKey<?> key) {
      data.remove(key);
    }

    /**
     * Gets a value from the holder, or a default if missing
     * @param key           Holder key
     * @param defaultValue  Value
     * @param <T>           Data type
     * @return  Data or default
     */
    @SuppressWarnings("unchecked")
    public <S, T extends S> S get(TinkerDataKey<T> key, S defaultValue) {
      return (T) data.getOrDefault(key, defaultValue);
    }

    /**
     * Gets a value from the holder, or null if missing
     * @param key           Holder key
     * @param <T>           Data type
     * @return  Data or default
     */
    @Nullable
    @SuppressWarnings("unchecked")
    public <T> T get(TinkerDataKey<T> key) {
      return (T) data.get(key);
    }

    /** Gets the value from the holder, creating it if missing */
    @SuppressWarnings("unchecked")
    public <T> T computeIfAbsent(TinkerDataKey<T> key, Function<TinkerDataKey<?>,T> constructor) {
      return (T) data.computeIfAbsent(key, constructor);
    }

    /** Gets the value from the holder, creating it if missing */
    public <T, U extends TinkerDataKey<T> & Function<TinkerDataKey<?>,T>> T computeIfAbsent(U key) {
      return computeIfAbsent(key, key);
    }

    /**
     * Checks if the given key is present
     * @param key  Key to check
     * @return  true if present
     */
    public boolean contains(TinkerDataKey<?> key) {
      return data.containsKey(key);
    }
  }
}
