package twilightforest.world;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import hexxit.worldgen152.core.OldBiome;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeResolver;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import twilightforest.gen.TFBiomes;
import twilightforest.gen.TFLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * The biomes of the Twilight Forest (TFWorldChunkManager). Each 4x4x4 cell of today takes the 1.5.2 biome of its
 * centre column; the terrain uses the exact per-column layout.
 */
public final class TFBiomeSource extends BiomeSource {
    public static final MapCodec<TFBiomeSource> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
            .group(RegistryOps.retrieveGetter(Registries.BIOME))
            .apply(instance, TFBiomeSource::new));

    @SuppressWarnings("unchecked")
    private final Holder<Biome>[] byOldId = new Holder[256];
    private final List<Holder<Biome>> possible = new ArrayList<>();
    private volatile TFLayout layout;

    private TFBiomeSource(HolderGetter<Biome> biomes) {
        for (OldBiome old : TFBiomes.ALL) {
            Holder<Biome> holder = biomes.getOrThrow(ResourceKey.create(Registries.BIOME, Identifier.parse(TFBiomes.key(old.id))));
            this.byOldId[old.id] = holder;
            this.possible.add(holder);
        }
    }

    /** Fixes the world seed; the generator calls it before chunks are made. */
    public void bind(TFLayout layout) {
        this.layout = layout;
    }

    public TFLayout layout() {
        TFLayout current = this.layout;
        if (current == null) {
            throw new IllegalStateException("Twilight Forest biomes used before the world seed was known");
        }
        return current;
    }

    public Holder<Biome> holder(int oldId) {
        Holder<Biome> holder = this.byOldId[oldId & 0xFF];
        return holder != null ? holder : this.byOldId[TFBiomes.TWILIGHT_FOREST.id];
    }

    @Override
    protected MapCodec<? extends BiomeSource> codec() {
        return CODEC;
    }

    @Override
    protected Stream<Holder<Biome>> collectPossibleBiomes() {
        return this.possible.stream();
    }

    @Override
    public BiomeResolver createResolver(Climate.Sampler sampler) {
        return (quartX, quartY, quartZ) -> {
            int x = (quartX << 2) + 2;
            int z = (quartZ << 2) + 2;
            return this.holder(this.layout().chunkIds(x >> 4, z >> 4)[(x & 15) + (z & 15) * 16]);
        };
    }
}
