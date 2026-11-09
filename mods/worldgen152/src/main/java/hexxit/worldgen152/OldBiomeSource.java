package hexxit.worldgen152;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import hexxit.worldgen152.core.BiomeLayout;
import hexxit.worldgen152.core.OldBiome;
import hexxit.worldgen152.core.OldBiomes;
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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Biome placement of 1.5.2. The old game stored one biome per block column; today biomes live on a 4x4x4
 * grid, so each cell takes the old biome at its centre column. Terrain itself is generated from the exact
 * per-column layout and does not depend on this coarser view.
 */
public final class OldBiomeSource extends BiomeSource {
    public static final MapCodec<OldBiomeSource> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
            .group(RegistryOps.retrieveGetter(Registries.BIOME))
            .apply(instance, OldBiomeSource::new));

    @SuppressWarnings("unchecked")
    private final Holder<Biome>[] byOldId = new Holder[256];
    private final List<Holder<Biome>> possible = new ArrayList<>();
    private final Holder<Biome> fallback;
    private volatile BiomeLayout layout;

    private OldBiomeSource(HolderGetter<Biome> biomes) {
        for (OldBiome old : OldBiomes.all()) {
            if (old.key != null) {
                // While registries load this hands out a reference that must exist once loading finishes,
                // so a biome without its JSON file fails the load with the missing key named.
                Holder<Biome> holder = biomes.getOrThrow(ResourceKey.create(Registries.BIOME, Identifier.parse(old.key)));
                this.byOldId[old.id] = holder;
                this.possible.add(holder);
            }
        }
        this.fallback = this.byOldId[OldBiomes.PLAINS.id];
    }

    /** Fixes the world seed. Called once per server start, before chunks are generated. */
    public void bind(long seed) {
        BiomeLayout current = this.layout;
        if (current == null || current.seed() != seed) {
            this.layout = new BiomeLayout(seed);
        }
    }

    public BiomeLayout layout() {
        BiomeLayout current = this.layout;
        if (current == null) {
            throw new IllegalStateException("1.5.2 biome layout used before the world seed was known");
        }
        return current;
    }

    public Holder<Biome> holder(int oldId) {
        Holder<Biome> holder = this.byOldId[oldId & 0xFF];
        return holder != null ? holder : this.fallback;
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
