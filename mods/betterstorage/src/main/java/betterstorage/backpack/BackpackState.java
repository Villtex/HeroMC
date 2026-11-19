package betterstorage.backpack;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * The data watcher entry backpackOpenDataWatcherId (open) and the "backpackHasItems" packet of the original, synced
 * to every client that sees the entity.
 */
public record BackpackState(boolean open, boolean hasItems) {
    public static final BackpackState CLOSED = new BackpackState(false, false);
    public static final StreamCodec<ByteBuf, BackpackState> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, BackpackState::open, ByteBufCodecs.BOOL, BackpackState::hasItems, BackpackState::new);
}
