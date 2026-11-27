package slimeknights.mantle.data.loadable.field;

import com.google.gson.JsonElement;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.RegistryOps;
import net.neoforged.neoforge.common.conditions.ConditionalOps;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.common.conditions.ICondition.IContext;
import slimeknights.mantle.data.loadable.ErrorFactory;
import slimeknights.mantle.util.typed.TypedMap;
import slimeknights.mantle.util.typed.TypedMap.Key;

import javax.annotation.Nullable;
import java.util.function.BiFunction;

/**
 * Key for fetching properties from a loadable context. This key doubles as a record field for a required context key.
 * @param <T>  Field type
 */
@RequiredArgsConstructor
public class ContextKey<T> implements Key<T> {
  /** Context field representing the object's ID */
  public static final ContextKey<Identifier> ID = new ContextKey<>("id");
  /** Key for adding debug info to log messages. Generally not useful as a field */
  public static final ContextKey<String> DEBUG = new ContextKey<>("debug info");
  /** Key for adding condition context, used in {@link slimeknights.mantle.data.loadable.mapping.ConditionalLoadable} */
  public static final ContextKey<IContext> CONDITION_CONTEXT = new ContextKey<>("condition context");

  /** Port (from Mantle 1.21): registry lookup for parsing registry based values. If missing, {@link #registryOps(TypedMap)} uses the lookup of the running data reload. */
  public static final ContextKey<HolderLookup.Provider> REGISTRY_LOOKUP = new ContextKey<>("registry lookup");

  /** Port: registry ops of the codec that is decoding (recipes, loot), preferred by {@link #registryOps(TypedMap)} */
  public static final ContextKey<RegistryOps<?>> REGISTRY_OPS = new ContextKey<>("registry ops");

  /** Name of the field, used primarily for debug */
  @Getter
  private final String name;



  @Override
  public String toString() {
    return "ContextKey('" + name + "')'";
  }


  /* Fields */
  private RecordField<T,Object> requiredField;
  private RecordField<T,Object> nullableField;

  /** Gets a field requiring this context parameter */
  public RecordField<T,Object> requiredField() {
    if (requiredField == null) {
      requiredField = new Required<>(this, (t,e) -> t);
    }
    return requiredField;
  }

  /** Gets a field requiring this context parameter, but mapped using the passed function */
  public <M> RecordField<M,Object> mappedField(BiFunction<T,ErrorFactory,M> mapper) {
    return new Required<>(this, mapper);
  }

  /** Field using null in place of this parameter if missing */
  public RecordField<T,Object> nullableField() {
    if (nullableField == null) {
      nullableField = new Defaulting<>(this, null);
    }
    return nullableField;
  }

  /** Creates a defaulting field for this key */
  public RecordField<T,Object> defaultField(T defaultValue) {
    return new Defaulting<>(this, defaultValue);
  }


  /** Field instance for making the key required */
  private record Required<T,M>(ContextKey<T> key, BiFunction<T,ErrorFactory,M> mapper) implements ContextField<M> {
    @Override
    public M get(TypedMap context, ErrorFactory error) {
      T value = context.get(key);
      if (value != null) {
        return mapper.apply(value, error);
      }
      throw error.create("Unable to fetch " + key.name + " from context, this usually implements a broken JSON deserializer");
    }
  }

  /** Field instance that defaults to a given value */
  private record Defaulting<T>(ContextKey<T> key, @Nullable T defaultValue) implements ContextField<T> {
    @Override
    public T get(TypedMap context, ErrorFactory error) {
      // it is potentially faster to use get over getOrDefault so call it directly if we have no default value
      if (defaultValue == null) {
        return context.get(key);
      }
      return context.getOrDefault(key, defaultValue);
    }
  }

  /* Port: registry ops helpers */

  /** Registry lookup for the context: the context key, else the running data reload, else the client level */
  public static HolderLookup.Provider registryLookup(TypedMap context) {
    HolderLookup.Provider lookup = context.get(REGISTRY_LOOKUP);
    if (lookup != null) {
      return lookup;
    }
    lookup = slimeknights.mantle.data.listener.JsonReloadListener.currentRegistryLookup();
    if (lookup == RegistryAccess.EMPTY) {
      RegistryAccess client = slimeknights.mantle.client.SafeClientAccess.getRegistryAccess();
      if (client != null) {
        return client;
      }
    }
    return lookup;
  }

  /** JSON ops with registry access (and the condition context, if given) */
  public static DynamicOps<JsonElement> registryOps(TypedMap context) {
    RegistryOps<?> parent = context.get(REGISTRY_OPS);
    RegistryOps<JsonElement> ops = parent != null ? parent.withParent(JsonOps.INSTANCE) : registryLookup(context).createSerializationContext(JsonOps.INSTANCE);
    IContext conditionContext = context.get(CONDITION_CONTEXT);
    if (conditionContext != null) {
      return new ConditionalOps<>(ops, conditionContext);
    }
    return ops;
  }
}
