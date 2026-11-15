package eplus.network;

import eplus.EnchantingPlus;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.enchantment.Enchantment;

/**
 * Client to server: EnchantPacket of 1.5.2, the new level of every changed enchantment and the cost the client
 * computed. (The old levels the original also sent are taken from the server's own menu.)
 */
public record EnchantPayload(Map<Holder<Enchantment>, Integer> levels, int cost) implements CustomPacketPayload {
    public static final Type<EnchantPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(EnchantingPlus.MOD_ID, "enchant"));
    public static final StreamCodec<RegistryFriendlyByteBuf, EnchantPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.map(HashMap::new, Enchantment.STREAM_CODEC, ByteBufCodecs.VAR_INT, 256), EnchantPayload::levels,
            ByteBufCodecs.INT, EnchantPayload::cost, EnchantPayload::new);

    @Override
    public Type<EnchantPayload> type() {
        return TYPE;
    }
}
