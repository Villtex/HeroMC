package eplus.network;

import eplus.EnchantingPlus;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/** Client to server: the "Mod" button of the vanilla enchanting GUI (GuiPacket with id 0). */
public record OpenModTablePayload() implements CustomPacketPayload {
    public static final OpenModTablePayload INSTANCE = new OpenModTablePayload();
    public static final Type<OpenModTablePayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(EnchantingPlus.MOD_ID, "open_mod_table"));
    public static final StreamCodec<ByteBuf, OpenModTablePayload> CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public Type<OpenModTablePayload> type() {
        return TYPE;
    }
}
