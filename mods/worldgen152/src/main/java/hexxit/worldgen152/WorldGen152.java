package hexxit.worldgen152;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.LevelStem;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;

/**
 * The overworld of Minecraft 1.5.2 as a chunk generator for current versions.
 *
 * <p>{@code hexxit.worldgen152.core} holds the generator itself (plain Java, no game classes); this package
 * plugs it into the game: generator and biome source types, and the world preset / dimension data in
 * {@code data/}.
 */
@Mod(WorldGen152.MOD_ID)
public final class WorldGen152 {
    public static final String MOD_ID = "worldgen152";
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final DeferredRegister<MapCodec<? extends ChunkGenerator>> CHUNK_GENERATORS =
            DeferredRegister.create(Registries.CHUNK_GENERATOR, MOD_ID);
    private static final DeferredRegister<MapCodec<? extends BiomeSource>> BIOME_SOURCES =
            DeferredRegister.create(Registries.BIOME_SOURCE, MOD_ID);

    public WorldGen152(IEventBus modBus) {
        CHUNK_GENERATORS.register("overworld", () -> OldChunkGenerator.CODEC);
        BIOME_SOURCES.register("overworld", () -> OldBiomeSource.CODEC);
        CHUNK_GENERATORS.register(modBus);
        BIOME_SOURCES.register(modBus);
        NeoForge.EVENT_BUS.addListener(WorldGen152::onServerAboutToStart);
    }

    /** Biome sources are created without knowing the world seed; hand it over before any chunk is generated. */
    private static void onServerAboutToStart(ServerAboutToStartEvent event) {
        MinecraftServer server = event.getServer();
        long seed = server.getWorldGenSettings().options().seed();
        for (LevelStem stem : server.getWorldGenSettings().dimensions().dimensions().values()) {
            if (stem.generator().getBiomeSource() instanceof OldBiomeSource source) {
                source.bind(seed);
                LOGGER.info("1.5.2 world generation active, seed {}", seed);
            }
        }
    }
}
