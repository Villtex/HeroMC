package hexxit.worldgen152;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.dimension.LevelStem;
import net.neoforged.neoforge.common.conditions.ICondition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Load condition {@code {"type": "worldgen152:world_type", "legacy": true}} for data of the pack that changes vanilla
 * behaviour only in Hexxit Legacy (Teil 26e): true when the overworld of the world being loaded runs the 1.5.2
 * generator ({@link OldChunkGenerator}). Dimensions are part of the registries a data pack reload sees, so a
 * dedicated server decides right at the first load. A client creating a world loads its data before the world type
 * is chosen; {@link #checkLoaded} then reloads once when the world type differs from the one the data was loaded for.
 */
public record WorldTypeCondition(boolean legacy) implements ICondition {
    private static final Logger LOGGER = LoggerFactory.getLogger(WorldTypeCondition.class);
    public static final MapCodec<WorldTypeCondition> CODEC = RecordCodecBuilder.mapCodec(b -> b.group(
            Codec.BOOL.fieldOf("legacy").forGetter(WorldTypeCondition::legacy)).apply(b, WorldTypeCondition::new));

    /** World type the last data load saw (null: no condition evaluated yet). */
    private static volatile Boolean loadedFor;

    @Override
    public boolean test(IContext context) {
        boolean isLegacy = context.registries().lookup(Registries.LEVEL_STEM)
                .flatMap(stems -> stems.get(LevelStem.OVERWORLD))
                .map(Holder::value)
                .map(stem -> stem.generator() instanceof OldChunkGenerator)
                .orElse(false);
        loadedFor = isLegacy;
        return isLegacy == legacy;
    }

    @Override
    public MapCodec<? extends ICondition> codec() {
        return CODEC;
    }

    /** After the server started: reload the data once if it was loaded for the other world type. */
    static void checkLoaded(MinecraftServer server) {
        boolean isLegacy = server.overworld().getChunkSource().getGenerator() instanceof OldChunkGenerator;
        Boolean seen = loadedFor;
        if (seen != null && seen != isLegacy) {
            LOGGER.info("Data was loaded for world type {}, reloading for {}", seen ? "Hexxit Legacy" : "Integrated",
                    isLegacy ? "Hexxit Legacy" : "Integrated");
            loadedFor = isLegacy;
            server.reloadResources(server.getPackRepository().getSelectedIds());
        }
    }
}
