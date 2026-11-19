package backpack.network;

import backpack.Backpack;
import backpack.BackpackItem;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/** PacketHandlerBackpack: the channels "OpenBackpack" (key binding) and "BackpackRename" (rename screen). */
public final class BackpackNetwork {
    public static final int MAX_NAME = 32;

    private BackpackNetwork() {
    }

    /** Client to server: open the backpack worn in the chest slot. */
    public record OpenBackpack() implements CustomPacketPayload {
        public static final OpenBackpack INSTANCE = new OpenBackpack();
        public static final Type<OpenBackpack> TYPE = new Type<>(Identifier.fromNamespaceAndPath(Backpack.MOD_ID, "open_backpack"));
        public static final StreamCodec<ByteBuf, OpenBackpack> CODEC = StreamCodec.unit(INSTANCE);

        @Override
        public Type<OpenBackpack> type() {
            return TYPE;
        }
    }

    /** Client to server: the new name of the backpack in the main hand. */
    public record Rename(String name) implements CustomPacketPayload {
        public static final Type<Rename> TYPE = new Type<>(Identifier.fromNamespaceAndPath(Backpack.MOD_ID, "rename"));
        public static final StreamCodec<ByteBuf, Rename> CODEC = ByteBufCodecs.stringUtf8(256).map(Rename::new, Rename::name);

        @Override
        public Type<Rename> type() {
            return TYPE;
        }
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(OpenBackpack.TYPE, OpenBackpack.CODEC, (p, ctx) -> {
            if (ctx.player() instanceof ServerPlayer player) {
                BackpackItem.openWorn(player);
            }
        });
        registrar.playToServer(Rename.TYPE, Rename.CODEC, BackpackNetwork::rename);
    }

    /** handlePacket: the held backpack gets the name (trimmed, 1..32 characters; not the ender backpack). */
    private static void rename(Rename payload, IPayloadContext ctx) {
        if (!(ctx.player() instanceof ServerPlayer player)) {
            return;
        }
        String name = payload.name().trim();
        ItemStack stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof BackpackItem)) {
            stack = player.getOffhandItem();
        }
        if (name.isEmpty() || name.length() > MAX_NAME || !(stack.getItem() instanceof BackpackItem item) || item.kind() == BackpackItem.Kind.ENDER) {
            return;
        }
        stack.set(DataComponents.CUSTOM_NAME, Component.literal(name));
    }
}
