package projectzulu.network.gear;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import projectzulu.ProjectZulu;

/** Server to client: open the tombstone editor (openGui(…, 0, …) of BlockTombstone) for the tombstone at {@code pos}. */
public record OpenTombstonePayload(BlockPos pos) implements CustomPacketPayload {
    public static final Type<OpenTombstonePayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(ProjectZulu.MOD_ID, "open_tombstone"));
    public static final StreamCodec<ByteBuf, OpenTombstonePayload> CODEC = BlockPos.STREAM_CODEC.map(OpenTombstonePayload::new, OpenTombstonePayload::pos);

    @Override
    public Type<OpenTombstonePayload> type() {
        return TYPE;
    }
}
