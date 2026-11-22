package hexxit.worldgen152;

import hexxit.worldgen152.core.OldBiome;
import hexxit.worldgen152.core.OldBiomes;
import net.minecraft.core.Holder;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.neoforge.common.Tags;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Which 1.5.2 biome a generator sees for a biome of today (world type Hexxit Integrated). The 1.5.2 generators ask
 * for biome names ("Forest", "Desert", "Swampland"); the answer comes from the {@code c:} and {@code minecraft:}
 * biome tags, so biomes of other mods with those tags get the same content. A biome that is itself one of the old
 * biomes (worldgen152, ExtrabiomesXL) keeps its id.
 */
public final class OldBiomeTags {
    private record Rule(TagKey<Biome> tag, OldBiome biome, OldBiome cold) {
    }

    /** First match wins: the narrow tags before the wide ones. */
    private static final List<Rule> RULES = List.of(
            new Rule(Tags.Biomes.IS_MUSHROOM, OldBiomes.MUSHROOM_ISLAND, OldBiomes.MUSHROOM_ISLAND),
            new Rule(BiomeTags.IS_DEEP_OCEAN, OldBiomes.OCEAN, OldBiomes.FROZEN_OCEAN),
            new Rule(BiomeTags.IS_OCEAN, OldBiomes.OCEAN, OldBiomes.FROZEN_OCEAN),
            new Rule(BiomeTags.IS_RIVER, OldBiomes.RIVER, OldBiomes.FROZEN_RIVER),
            new Rule(BiomeTags.IS_BEACH, OldBiomes.BEACH, OldBiomes.BEACH),
            new Rule(Tags.Biomes.IS_SWAMP, OldBiomes.SWAMPLAND, OldBiomes.SWAMPLAND),
            new Rule(Tags.Biomes.IS_DESERT, OldBiomes.DESERT, OldBiomes.DESERT),
            new Rule(BiomeTags.IS_BADLANDS, OldBiomes.DESERT, OldBiomes.DESERT),
            new Rule(BiomeTags.IS_JUNGLE, OldBiomes.JUNGLE, OldBiomes.JUNGLE),
            new Rule(BiomeTags.IS_TAIGA, OldBiomes.TAIGA, OldBiomes.TAIGA),
            new Rule(BiomeTags.IS_MOUNTAIN, OldBiomes.EXTREME_HILLS, OldBiomes.ICE_MOUNTAINS),
            new Rule(Tags.Biomes.IS_MOUNTAIN, OldBiomes.EXTREME_HILLS, OldBiomes.ICE_MOUNTAINS),
            new Rule(BiomeTags.IS_HILL, OldBiomes.EXTREME_HILLS, OldBiomes.ICE_MOUNTAINS),
            new Rule(BiomeTags.IS_FOREST, OldBiomes.FOREST, OldBiomes.TAIGA),
            new Rule(Tags.Biomes.IS_FOREST, OldBiomes.FOREST, OldBiomes.TAIGA),
            new Rule(Tags.Biomes.IS_ICY, OldBiomes.ICE_PLAINS, OldBiomes.ICE_PLAINS),
            new Rule(Tags.Biomes.IS_SNOWY_PLAINS, OldBiomes.ICE_PLAINS, OldBiomes.ICE_PLAINS),
            new Rule(Tags.Biomes.IS_SAVANNA, OldBiomes.PLAINS, OldBiomes.PLAINS),
            new Rule(Tags.Biomes.IS_PLAINS, OldBiomes.PLAINS, OldBiomes.ICE_PLAINS));

    private static final Map<Holder<Biome>, Integer> CACHE = new ConcurrentHashMap<>();

    private OldBiomeTags() {
    }

    /** The 1.5.2 biome id for {@code biome}; in the Nether always Hell. */
    public static int oldId(Holder<Biome> biome, boolean nether) {
        if (nether) {
            return OldBiomes.HELL.id;
        }
        Integer id = CACHE.get(biome);
        if (id == null) {
            id = compute(biome);
            CACHE.put(biome, id);
        }
        return id;
    }

    private static int compute(Holder<Biome> biome) {
        String key = biome.unwrapKey().map(k -> k.identifier().toString()).orElse(null);
        if (key != null) {
            for (OldBiome old : OldBiomes.all()) {
                if (key.equals(old.key)) {
                    return old.id;
                }
            }
        }
        if (biome.is(BiomeTags.IS_END)) {
            return OldBiomes.SKY.id;
        }
        boolean cold = biome.is(Tags.Biomes.IS_SNOWY) || biome.is(Tags.Biomes.IS_ICY);
        for (Rule rule : RULES) {
            if (biome.is(rule.tag())) {
                return (cold ? rule.cold() : rule.biome()).id;
            }
        }
        return (cold ? OldBiomes.ICE_PLAINS : OldBiomes.PLAINS).id;
    }
}
