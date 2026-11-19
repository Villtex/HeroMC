package chococraft.network;

import chococraft.ChocoCraft;
import chococraft.entity.AbstractChocobo;
import chococraft.entity.Chocobo;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * The client-to-server packets of the Chocopedia page (PacketChocoboAttribute, PacketChocoboDropGear,
 * PacketChocoboChangeOwner) as one payload. Everything else of the original channel is today's entity sync. The
 * server only takes it from the owner within 8 blocks.
 */
public final class ChocoNetwork {
    public static final int RENAME = 0;
    public static final int TOGGLE_HIDE_NAME = 1;
    public static final int TOGGLE_FOLLOW = 2;
    public static final int DROP_GEAR = 3;
    public static final int CHANGE_OWNER = 4;
    /** GuiNameChocobo: setMaxStringLength(20). */
    public static final int MAX_NAME = 20;

    private ChocoNetwork() {
    }

    public record Action(int entityId, int action, String text) implements CustomPacketPayload {
        public static final Type<Action> TYPE = new Type<>(ChocoCraft.id("chocobo_action"));
        public static final StreamCodec<ByteBuf, Action> CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, Action::entityId, ByteBufCodecs.VAR_INT, Action::action, ByteBufCodecs.stringUtf8(64), Action::text,
                Action::new);

        @Override
        public Type<Action> type() {
            return TYPE;
        }
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToServer(Action.TYPE, Action.CODEC, ChocoNetwork::handle);
    }

    private static void handle(Action payload, IPayloadContext ctx) {
        if (!(ctx.player() instanceof ServerPlayer player)
                || !(player.level().getEntity(payload.entityId()) instanceof AbstractChocobo chocobo)
                || !chocobo.isOwner(player) || player.distanceToSqr(chocobo) > 64.0) {
            return;
        }
        switch (payload.action()) {
            case RENAME -> {
                String name = payload.text().trim();
                if (!name.isEmpty() && name.length() <= MAX_NAME) {
                    chocobo.setChocoboName(name);
                }
            }
            case TOGGLE_HIDE_NAME -> chocobo.setHidename(!chocobo.isHidename());
            case TOGGLE_FOLLOW -> chocobo.toggleFollowWanderStay();
            case DROP_GEAR -> {
                if (chocobo instanceof Chocobo rideable) {
                    rideable.dropGear();
                }
            }
            case CHANGE_OWNER -> {
                ServerPlayer target = player.level().getServer().getPlayerList().getPlayerByName(payload.text());
                if (target != null) {
                    chocobo.setOwner(target);
                    chocobo.setOwnerName(target.getGameProfile().name());
                }
            }
            default -> {
            }
        }
    }
}
