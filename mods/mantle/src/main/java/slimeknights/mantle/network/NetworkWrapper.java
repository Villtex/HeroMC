package slimeknights.mantle.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.network.packet.ISimplePacket;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A small network implementation/wrapper using AbstractPackets instead of IMessages.
 * Instantiate in your mod class and register your packets accordingly.
 *
 * <p>Port: Forge's SimpleChannel became NeoForge payloads. Every registered packet class gets its own payload type
 * {@code <channel namespace>:<channel path>/<class name>}; the object is wrapped in a {@link Payload} for sending.
 * Packets must be registered before {@link RegisterPayloadHandlersEvent} (call {@code registerPackets()} from the mod
 * constructor, not from common setup). The channel's namespace must be the ID of the mod creating the wrapper.
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class NetworkWrapper {
  private final Identifier channelName;
  private final String version;
  private final List<Registration<?>> registrations = new ArrayList<>();
  private final Map<Class<?>, Registration<?>> byClass = new IdentityHashMap<>();
  private boolean registered = false;

  /**
   * Creates a new network wrapper
   * @param channelName  Unique packet channel name
   * @deprecated Give your channel a version number.
   */
  @Deprecated
  public NetworkWrapper(Identifier channelName) {
    this(channelName, "1");
  }

  public NetworkWrapper(Identifier channelName, String version) {
    this.channelName = channelName;
    this.version = version;
    IEventBus bus = ModList.get().getModContainerById(channelName.getNamespace())
      .map(container -> container.getEventBus())
      .orElseThrow(() -> new IllegalStateException("No mod " + channelName.getNamespace() + " for network channel " + channelName));
    bus.addListener(RegisterPayloadHandlersEvent.class, this::registerPayloads);
  }

  /**
   * Registers a new {@link ISimplePacket}
   * @param clazz    Packet class
   * @param decoder  Packet decoder, typically the constructor
   * @param <MSG>  Packet class type
   */
  public <MSG extends ISimplePacket> void registerPacket(Class<MSG> clazz, Function<FriendlyByteBuf, MSG> decoder, @Nullable NetworkDirection direction) {
    registerPacket(clazz, ISimplePacket::encode, decoder, ISimplePacket::handle, direction);
  }

  /**
   * Registers a new generic packet
   * @param clazz      Packet class
   * @param encoder    Encodes a packet to the buffer
   * @param decoder    Packet decoder, typically the constructor
   * @param consumer   Logic to handle a packet
   * @param direction  Network direction for validation. Pass null for no direction
   * @param <MSG>  Packet class type
   */
  public <MSG> void registerPacket(Class<MSG> clazz, BiConsumer<MSG, FriendlyByteBuf> encoder, Function<FriendlyByteBuf, MSG> decoder, BiConsumer<MSG,Supplier<NetworkEvent.Context>> consumer, @Nullable NetworkDirection direction) {
    registerPacketNoLogger(clazz, wrapLogger(clazz, encoder), wrapLogger(clazz, decoder), consumer, direction);
  }

  /**
   * Registers a new packet without the automatic logging if the decoder fails
   * @param clazz      Packet class
   * @param encoder    Encodes a packet to the buffer
   * @param decoder    Packet decoder, typically the constructor
   * @param consumer   Logic to handle a packet
   * @param direction  Network direction for validation. Pass null for no direction
   * @param <MSG>  Packet class type
   */
  public <MSG> void registerPacketNoLogger(Class<MSG> clazz, BiConsumer<MSG, FriendlyByteBuf> encoder, Function<FriendlyByteBuf, MSG> decoder, BiConsumer<MSG,Supplier<NetworkEvent.Context>> consumer, @Nullable NetworkDirection direction) {
    if (registered) {
      throw new IllegalStateException("Packet " + clazz.getName() + " registered after the payload registration of " + channelName);
    }
    String name = clazz.getSimpleName().replaceAll("([a-z0-9])([A-Z])", "$1_$2").toLowerCase(Locale.ROOT);
    CustomPacketPayload.Type<Payload<MSG>> type = new CustomPacketPayload.Type<>(channelName.withSuffix("/" + name));
    Registration<MSG> registration = new Registration<>(type, encoder, decoder, consumer, direction);
    if (byClass.put(clazz, registration) != null) {
      throw new IllegalArgumentException("Duplicate packet class " + clazz.getName() + " in " + channelName);
    }
    registrations.add(registration);
  }

  private void registerPayloads(RegisterPayloadHandlersEvent event) {
    registered = true;
    PayloadRegistrar registrar = event.registrar(version);
    for (Registration<?> registration : registrations) {
      registration.register(registrar);
    }
  }

  /** Wraps the given encoder function */
  private static <MSG> BiConsumer<MSG, FriendlyByteBuf> wrapLogger(Class<MSG> clazz, BiConsumer<MSG, FriendlyByteBuf> encoder) {
    return (message, buffer) -> {
      try {
        encoder.accept(message, buffer);
      } catch (Exception e) {
        Mantle.logger.error("Exception while encoding packet of class {}", clazz.getName(), e);
        throw e;
      }
    };
  }

  /** Wraps the given decoder function */
  private static <MSG> Function<FriendlyByteBuf,MSG> wrapLogger(Class<MSG> clazz, Function<FriendlyByteBuf,MSG> decoder) {
    return buffer -> {
      try {
        return decoder.apply(buffer);
      } catch (Exception e) {
        Mantle.logger.error("Exception while decoding packet of class {}", clazz.getName(), e);
        throw e;
      }
    };
  }

  /** Wraps a packet object into its payload */
  @SuppressWarnings("unchecked")
  private <MSG> Payload<MSG> wrap(MSG msg) {
    Registration<MSG> registration = (Registration<MSG>) byClass.get(msg.getClass());
    if (registration == null) {
      throw new IllegalArgumentException("Packet " + msg.getClass().getName() + " is not registered in " + channelName);
    }
    return new Payload<>(registration.type, msg);
  }


  /* Sending packets */

  /**
   * Sends a packet to the server
   * @param msg  Packet to send
   */
  public void sendToServer(Object msg) {
    net.neoforged.neoforge.client.network.ClientPacketDistributor.sendToServer(wrap(msg));
  }

  /**
   * Sends a vanilla packet to the given entity
   * @param player  Player receiving the packet
   * @param packet  Packet
   */
  public void sendVanillaPacket(Packet<?> packet, Entity player) {
    if (player instanceof ServerPlayer sPlayer) {
      sPlayer.connection.send(packet);
    }
  }

  /**
   * Sends a packet to a player
   * @param msg     Packet
   * @param player  Player to send
   */
  public void sendTo(Object msg, Player player) {
    if (player instanceof ServerPlayer) {
      sendTo(msg, (ServerPlayer) player);
    }
  }

  /**
   * Sends a packet to a player
   * @param msg     Packet
   * @param player  Player to send
   */
  public void sendTo(Object msg, ServerPlayer player) {
    if (!(player instanceof FakePlayer)) {
      PacketDistributor.sendToPlayer(player, wrap(msg));
    }
  }

  /** Sends a packet to all players (port: replaces {@code send(PacketDistributor.ALL.noArg(), msg)}) */
  public void sendToAll(Object msg) {
    PacketDistributor.sendToAllPlayers(wrap(msg));
  }

  /**
   * Sends a packet to players near a location
   * @param msg          Packet to send
   * @param serverWorld  World instance
   * @param position     Position within range
   */
  public void sendToClientsAround(Object msg, ServerLevel serverWorld, BlockPos position) {
    PacketDistributor.sendToPlayersTrackingChunk(serverWorld, ChunkPos.containing(position), wrap(msg));
  }

  /**
   * Sends a packet to all entities tracking the given entity
   * @param msg     Packet
   * @param entity  Entity to check
   */
  public void sendToTrackingAndSelf(Object msg, Entity entity) {
    PacketDistributor.sendToPlayersTrackingEntityAndSelf(entity, wrap(msg));
  }

  /**
   * Sends a packet to all entities tracking the given entity
   * @param msg     Packet
   * @param entity  Entity to check
   */
  public void sendToTracking(Object msg, Entity entity) {
    PacketDistributor.sendToPlayersTrackingEntity(entity, wrap(msg));
  }

  /** Payload carrying one packet object */
  public record Payload<MSG>(Type<Payload<MSG>> type, MSG msg) implements CustomPacketPayload {}

  /** One registered packet class */
  private record Registration<MSG>(CustomPacketPayload.Type<Payload<MSG>> type, BiConsumer<MSG, FriendlyByteBuf> encoder, Function<FriendlyByteBuf, MSG> decoder, BiConsumer<MSG,Supplier<NetworkEvent.Context>> consumer, @Nullable NetworkDirection direction) {
    void register(PayloadRegistrar registrar) {
      StreamCodec<RegistryFriendlyByteBuf, Payload<MSG>> codec = StreamCodec.of(
        (buffer, payload) -> encoder.accept(payload.msg(), buffer),
        buffer -> new Payload<>(type, decoder.apply(buffer)));
      IPayloadHandler<Payload<MSG>> handler = (payload, context) -> consumer.accept(payload.msg(), () -> new NetworkEvent.Context(context));
      if (direction == NetworkDirection.PLAY_TO_CLIENT) {
        registrar.playToClient(type, codec, handler);
      } else if (direction == NetworkDirection.PLAY_TO_SERVER) {
        registrar.playToServer(type, codec, handler);
      } else {
        registrar.playBidirectional(type, codec, handler);
      }
    }
  }
}
