package slimeknights.tconstruct.library.tools.capability;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;
import net.neoforged.neoforge.registries.DeferredHolder;
import slimeknights.tconstruct.library.tools.nbt.ModifierNBT;
import slimeknights.tconstruct.library.utils.LazyOptional;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/** Capability to allow an entity to store modifiers, used on projectiles fired from modifiable items */
public class EntityModifierCapability {
  /** Default instance to use with orElse */
  public static final EntityModifiers EMPTY = new EntityModifiers() {
    @Override
    public ModifierNBT getModifiers() {
      return ModifierNBT.EMPTY;
    }

    @Override
    public void setModifiers(ModifierNBT nbt) {}

    @Override
    public void addModifiers(ModifierNBT nbt) {}
  };

  private EntityModifierCapability() {}

  /* Static helpers */

  /** List of predicates to check if the entity supports this capability */
  private static final List<Predicate<Entity>> ENTITY_PREDICATES = new ArrayList<>();

  /** Attachment type, saved as the modifier list */
  public static final DeferredHolder<AttachmentType<?>, AttachmentType<Data>> ATTACHMENT = TinkerDataCapability.ATTACHMENT_TYPES.register("modifiers", () ->
    AttachmentType.builder(() -> new Data()).serialize(new IAttachmentSerializer<Data>() {
      @Override
      public Data read(IAttachmentHolder holder, ValueInput input) {
        Data data = new Data();
        input.read("modifiers", ModifierNBT.CODEC).ifPresent(data::setModifiers);
        return data;
      }

      @Override
      public boolean write(Data attachment, ValueOutput output) {
        if (attachment.getModifiers().isEmpty()) {
          return false;
        }
        output.store("modifiers", ModifierNBT.CODEC, attachment.getModifiers());
        return true;
      }
    }).build());

  /** Gets the data of the entity, empty if the entity does not support it. Replaces {@code entity.getCapability(CAPABILITY)} */
  public static LazyOptional<EntityModifiers> get(@Nullable Entity entity) {
    if (entity != null && supportCapability(entity)) {
      return LazyOptional.of(() -> entity.getData(ATTACHMENT));
    }
    return LazyOptional.empty();
  }

  /** Gets the capability for the entity or an empty instance if missing */
  public static EntityModifiers getCapability(Entity entity) {
    return get(entity).orElse(EMPTY);
  }

  /** Gets the data or an empty instance if missing */
  public static ModifierNBT getOrEmpty(Entity entity) {
    return getCapability(entity).getModifiers();
  }

  /** Checks if the given entity supports this capability */
  public static boolean supportCapability(Entity entity) {
    for (Predicate<Entity> entityPredicate : ENTITY_PREDICATES) {
      if (entityPredicate.test(entity)) {
        return true;
      }
    }
    return false;
  }

  /** Registers a predicate of entites that need this capability */
  public static void registerEntityPredicate(Predicate<Entity> predicate) {
    ENTITY_PREDICATES.add(predicate);
  }

  /** Stored data */
  private static class Data implements EntityModifiers {
    @Getter @Setter
    private ModifierNBT modifiers = ModifierNBT.EMPTY;
  }

  /** Interface for callers to use */
  public interface EntityModifiers {
    /** Gets the stored modifiers */
    ModifierNBT getModifiers();

    /** Sets the stored modifiers */
    void setModifiers(ModifierNBT nbt);

    /** Adds additional modifiers to the stored modifiers */
    default void addModifiers(ModifierNBT nbt) {
      ModifierNBT existing = getModifiers();
      if (existing.isEmpty()) {
        setModifiers(nbt);
      } else {
        setModifiers(ModifierNBT.builder().add(existing).add(nbt).build());
      }
    }
  }
}
