package hexxit.worldgen152;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.LevelStem;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;

/**
 * The overworld and the Nether of Minecraft 1.5.2 as chunk generators for current versions.
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
    private static final DeferredRegister<MapCodec<? extends net.minecraft.world.level.levelgen.feature.Feature>> FEATURE_TYPES =
            DeferredRegister.create(Registries.FEATURE_TYPE, MOD_ID);
    private static final DeferredRegister<MapCodec<? extends net.neoforged.neoforge.common.world.BiomeModifier>> BIOME_MODIFIERS =
            DeferredRegister.create(net.neoforged.neoforge.registries.NeoForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, MOD_ID);
    private static final DeferredRegister<MapCodec<? extends BiomeSource>> BIOME_SOURCES =
            DeferredRegister.create(Registries.BIOME_SOURCE, MOD_ID);

    private static final DeferredRegister<net.minecraft.world.level.levelgen.structure.StructureType<?>> STRUCTURE_TYPES =
            DeferredRegister.create(Registries.STRUCTURE_TYPE, MOD_ID);
    private static final DeferredRegister<net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType> STRUCTURE_PIECES =
            DeferredRegister.create(Registries.STRUCTURE_PIECE, MOD_ID);

    private static final DeferredRegister<net.minecraft.world.level.levelgen.structure.pools.StructurePoolElementType<?>> POOL_ELEMENTS =
            DeferredRegister.create(Registries.STRUCTURE_POOL_ELEMENT, MOD_ID);

    private static final DeferredRegister<MapCodec<? extends net.neoforged.neoforge.common.conditions.ICondition>> CONDITIONS =
            DeferredRegister.create(net.neoforged.neoforge.registries.NeoForgeRegistries.Keys.CONDITION_CODECS, MOD_ID);

    public WorldGen152(IEventBus modBus, net.neoforged.fml.ModContainer container) {
        container.registerConfig(net.neoforged.fml.config.ModConfig.Type.STARTUP, IntegratedConfig.SPEC);
        CHUNK_GENERATORS.register("overworld", () -> OldChunkGenerator.CODEC);
        CHUNK_GENERATORS.register("nether", () -> OldNetherChunkGenerator.CODEC);
        BIOME_SOURCES.register("overworld", () -> OldBiomeSource.CODEC);
        // a 1.5.2 world generator as a feature of today (world type Hexxit Integrated), see OldGeneratorFeature
        FEATURE_TYPES.register("old_generator", () -> OldGeneratorFeature.CODEC);
        FEATURE_TYPES.register(modBus);
        // a 1.5.2 structure as a structure of today (world type Hexxit Integrated), see OldStructure
        OldStructure.TYPE = STRUCTURE_TYPES.register("old_structure", () -> () -> OldStructure.CODEC);
        OldStructure.PIECE = STRUCTURE_PIECES.register("old_piece",
                () -> (net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType.ContextlessType) OldStructure.Piece::new);
        OldVillagePiece.TYPE = POOL_ELEMENTS.register("old_village_piece", () -> () -> OldVillagePiece.CODEC);
        POOL_ELEMENTS.register(modBus);
        STRUCTURE_TYPES.register(modBus);
        STRUCTURE_PIECES.register(modBus);
        NeoForge.EVENT_BUS.addListener(LevelEvent.Load.class, event -> {
            if (event.getLevel() instanceof ServerLevel level) {
                OldStructure.levelLoaded(level);
            }
        });
        NeoForge.EVENT_BUS.addListener(net.neoforged.neoforge.event.server.ServerStoppedEvent.class, event -> OldStructure.serverStopped());
        BIOME_MODIFIERS.register("replace_vanilla_ores", () -> ReplaceVanillaOresModifier.CODEC);
        BIOME_MODIFIERS.register(modBus);
        CHUNK_GENERATORS.register(modBus);
        BIOME_SOURCES.register(modBus);
        NeoForge.EVENT_BUS.addListener(WorldGen152::onServerAboutToStart);
        if (net.neoforged.fml.loading.FMLEnvironment.getDist() == net.neoforged.api.distmarker.Dist.CLIENT) {
            hexxit.worldgen152.client.DefaultWorldType.register();
        }
        // load condition worldgen152:world_type (data that differs between Hexxit Legacy and other world types)
        CONDITIONS.register("world_type", () -> WorldTypeCondition.CODEC);
        CONDITIONS.register(modBus);
        NeoForge.EVENT_BUS.addListener(net.neoforged.neoforge.event.server.ServerStartedEvent.class,
                event -> WorldTypeCondition.checkLoaded(event.getServer()));
        NeoForge.EVENT_BUS.addListener(WorldGen152::onCreateSpawnPosition);
    }

    /** A new world gets the spawn point 1.5.2 would have chosen for its seed. */
    private static void onCreateSpawnPosition(LevelEvent.CreateSpawnPosition event) {
        if (event.getLevel() instanceof ServerLevel level && level.dimension() == Level.OVERWORLD
                && level.getChunkSource().getGenerator() instanceof OldChunkGenerator generator
                && generator.getBiomeSource() instanceof OldBiomeSource source) {
            source.bind(level.getSeed());
            BlockPos spawn = generator.spawnPoint();
            event.getSettings().setSpawn(LevelData.RespawnData.of(Level.OVERWORLD, spawn, 0.0F, 0.0F));
            event.setCanceled(true);
            LOGGER.info("1.5.2 spawn point: {}", spawn);
        }
    }

    /** Biome sources are created without knowing the world seed; hand it over before any chunk is generated. */
    private static void onServerAboutToStart(ServerAboutToStartEvent event) {
        MinecraftServer server = event.getServer();
        OldVillagePiece.addToVillages(server);
        long seed = server.getWorldGenSettings().options().seed();
        for (LevelStem stem : server.getWorldGenSettings().dimensions().dimensions().values()) {
            if (stem.generator() instanceof OldNetherChunkGenerator nether) {
                nether.bind(seed);
                LOGGER.info("1.5.2 Nether generation active, seed {}", seed);
            }
            if (stem.generator().getBiomeSource() instanceof OldBiomeSource source) {
                source.bind(seed);
                LOGGER.info("1.5.2 world generation active, seed {}", seed);
            }
        }
    }
}
