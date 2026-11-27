package slimeknights.tconstruct.library.tools.capability;

import com.mojang.serialization.Codec;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.network.SyncPersistentDataPacket;
import slimeknights.tconstruct.common.network.TinkerNetwork;
import slimeknights.tconstruct.library.tools.nbt.ModDataNBT;
import slimeknights.tconstruct.library.utils.LazyOptional;

import javax.annotation.Nullable;

/**
 * Persistent modifier data on entities (players keep it through death and dimension changes).
 * <p>26.3: a saved data attachment ({@code tconstruct3:persistent_data}) instead of a Forge capability.
 */
public class PersistentDataCapability {
  private PersistentDataCapability() {}

  /** Attachment type, saved as the compound tag of the data, copied on death */
  public static final DeferredHolder<AttachmentType<?>, AttachmentType<ModDataNBT>> ATTACHMENT = TinkerDataCapability.ATTACHMENT_TYPES.register("persistent_data", () ->
    AttachmentType.builder(() -> new ModDataNBT()).serialize(new IAttachmentSerializer<ModDataNBT>() {
      @Override
      public ModDataNBT read(IAttachmentHolder holder, ValueInput input) {
        return ModDataNBT.readFromNBT(input.read("data", CompoundTag.CODEC).orElseGet(CompoundTag::new));
      }

      @Override
      public boolean write(ModDataNBT attachment, ValueOutput output) {
        CompoundTag tag = attachment.getCopy();
        if (tag.isEmpty()) {
          return false;
        }
        output.store("data", CompoundTag.CODEC, tag);
        return true;
      }
    }).copyOnDeath().build());

  /** Checks if the entity may hold this data */
  private static boolean supports(Entity entity) {
    // must be on living entities as we use this for potions, but also support anything else with modifiers, this is their data
    return entity instanceof LivingEntity || EntityModifierCapability.supportCapability(entity);
  }

  /** Gets the data of the entity, empty if the entity does not support it. Replaces {@code entity.getCapability(CAPABILITY)} */
  public static LazyOptional<ModDataNBT> get(@Nullable Entity entity) {
    if (entity != null && supports(entity)) {
      return LazyOptional.of(() -> entity.getData(ATTACHMENT));
    }
    return LazyOptional.empty();
  }

  /** Gets the data or warns if its missing */
  public static ModDataNBT getOrWarn(Entity entity) {
    if (!supports(entity)) {
      TConstruct.LOG.warn("Missing Tinkers NBT on entity {}, this should not happen", entity.getType());
      return new ModDataNBT();
    }
    return entity.getData(ATTACHMENT);
  }

  /** Registers the sync listeners (the attachment itself is registered by {@link TinkerDataCapability#register}) */
  public static void register() {
    NeoForge.EVENT_BUS.addListener(EventPriority.NORMAL, false, PlayerEvent.PlayerRespawnEvent.class, event -> sync(event.getEntity()));
    NeoForge.EVENT_BUS.addListener(EventPriority.NORMAL, false, PlayerEvent.PlayerChangedDimensionEvent.class, event -> sync(event.getEntity()));
    NeoForge.EVENT_BUS.addListener(EventPriority.NORMAL, false, PlayerEvent.PlayerLoggedInEvent.class, event -> sync(event.getEntity()));
  }

  /** Syncs the data to the given player */
  private static void sync(Player player) {
    TinkerNetwork.getInstance().sendTo(new SyncPersistentDataPacket(player.getData(ATTACHMENT).getCopy()), player);
  }
}
