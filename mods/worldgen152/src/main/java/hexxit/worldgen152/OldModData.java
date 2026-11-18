package hexxit.worldgen152;

import hexxit.worldgen152.core.populate.OldTile;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * What mods tell the chunk writer about their own generated data: how a mod's tile entity data
 * ({@link OldTile.Mod}) goes into the block entity of its block.
 */
public final class OldModData {
    /** Fills the block entity the chunk writer created for the block at that place. */
    public interface TileWriter<T extends OldTile.Mod> {
        void write(T tile, BlockEntity blockEntity, RegistryAccess registries);
    }

    private static final Map<Class<?>, TileWriter<?>> TILES = new ConcurrentHashMap<>();

    private OldModData() {
    }

    public static <T extends OldTile.Mod> void registerTile(Class<T> type, TileWriter<T> writer) {
        TILES.put(type, writer);
    }

    @SuppressWarnings("unchecked")
    static <T extends OldTile.Mod> boolean write(T tile, BlockEntity blockEntity, RegistryAccess registries) {
        TileWriter<T> writer = (TileWriter<T>) TILES.get(tile.getClass());
        if (writer == null) {
            return false;
        }
        writer.write(tile, blockEntity, registries);
        return true;
    }
}
