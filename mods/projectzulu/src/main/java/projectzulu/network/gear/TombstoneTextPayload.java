package projectzulu.network.gear;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringUtil;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import projectzulu.ProjectZulu;
import projectzulu.block.gear.TombstoneBlockEntity;

import java.util.List;

/**
 * Client to server: the four lines typed into the tombstone editor (packet id 2 on "Channel_Zulu" of the original).
 * The server checks distance and length again.
 */
public record TombstoneTextPayload(BlockPos pos, List<String> lines) implements CustomPacketPayload {
    public static final Type<TombstoneTextPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(ProjectZulu.MOD_ID, "tombstone_text"));
    public static final StreamCodec<ByteBuf, TombstoneTextPayload> CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, TombstoneTextPayload::pos,
            ByteBufCodecs.stringUtf8(64).apply(ByteBufCodecs.list(TombstoneBlockEntity.LINES)), TombstoneTextPayload::lines,
            TombstoneTextPayload::new);

    @Override
    public Type<TombstoneTextPayload> type() {
        return TYPE;
    }

    public static void handle(TombstoneTextPayload payload, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player) || !player.isWithinBlockInteractionRange(payload.pos(), 1.0)) {
            return;
        }
        if (player.level().getBlockEntity(payload.pos()) instanceof TombstoneBlockEntity tombstone) {
            for (int i = 0; i < TombstoneBlockEntity.LINES && i < payload.lines().size(); i++) {
                tombstone.setLine(i, StringUtil.filterText(payload.lines().get(i)));
            }
        }
    }
}
