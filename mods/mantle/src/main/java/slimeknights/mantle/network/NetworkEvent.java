package slimeknights.mantle.network;

import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;

/**
 * Port: stands in for Forge's {@code slimeknights.mantle.network.NetworkEvent}, so packets written against
 * {@link slimeknights.mantle.network.packet.ISimplePacket} keep their handler signature. Wraps NeoForge's
 * {@link IPayloadContext}.
 */
public final class NetworkEvent {
  private NetworkEvent() {}

  /** Packet context as in Forge 1.20.1 */
  public static final class Context {
    private final IPayloadContext context;

    public Context(IPayloadContext context) {
      this.context = context;
    }

    /** NeoForge context, for anything not covered here */
    public IPayloadContext getPayloadContext() {
      return context;
    }

    /** Sending player on the server, null on the client */
    @Nullable
    public ServerPlayer getSender() {
      return context.flow() == PacketFlow.SERVERBOUND && context.player() instanceof ServerPlayer player ? player : null;
    }

    /** Direction the packet travelled */
    public NetworkDirection getDirection() {
      return context.flow() == PacketFlow.SERVERBOUND ? NetworkDirection.PLAY_TO_SERVER : NetworkDirection.PLAY_TO_CLIENT;
    }

    /** Runs the task on the main thread */
    public CompletableFuture<Void> enqueueWork(Runnable runnable) {
      return context.enqueueWork(runnable);
    }

    /** No effect on NeoForge, kept for the original packet code */
    public void setPacketHandled(boolean handled) {}
  }
}
