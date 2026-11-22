package extrabiomes.integrated;

import com.mojang.datafixers.util.Pair;
import extrabiomes.Extrabiomes;
import hexxit.worldgen152.IntegratedConfig;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.levelgen.material.rule.MaterialRule;
import terrablender.api.MaterialRuleManager;
import terrablender.api.ModifiedVanillaOverworldBuilder;
import terrablender.api.Region;
import terrablender.api.RegionType;
import terrablender.api.Regions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * The ExtrabiomesXL biomes in the world type Hexxit Integrated: a TerraBlender region, the same way biome mods of
 * today add theirs, so the biomes of other mods (their own regions) and the vanilla region stay. The region is the
 * vanilla overworld layout with land biomes swapped for the ExtrabiomesXL biome of similar climate and shape;
 * where a vanilla biome stands for several of them, the temperature/humidity cell picks one, so each covers whole
 * patches. Oceans, rivers, beaches, caves and the biomes without a counterpart stay vanilla.
 */
public final class EbxlRegion extends Region {
    private static final Map<ResourceKey<Biome>, List<ResourceKey<Biome>>> REPLACEMENTS = new HashMap<>();

    static {
        put(Biomes.PLAINS, "meadow", "shrubland", "green_hills");
        put(Biomes.SUNFLOWER_PLAINS, "meadow");
        put(Biomes.FOREST, "autumn_woods", "woodlands", "forested_hills");
        put(Biomes.FLOWER_FOREST, "green_hills");
        put(Biomes.BIRCH_FOREST, "birch_forest");
        put(Biomes.DARK_FOREST, "temperate_rainforest");
        put(Biomes.TAIGA, "pine_forest", "mountain_taiga");
        put(Biomes.OLD_GROWTH_PINE_TAIGA, "redwood_lush");
        put(Biomes.OLD_GROWTH_SPRUCE_TAIGA, "redwood_forest");
        put(Biomes.SNOWY_TAIGA, "snowy_forest", "snowy_rainforest");
        put(Biomes.SNOWY_PLAINS, "tundra", "ice_wasteland");
        put(Biomes.FROZEN_PEAKS, "glacier");
        put(Biomes.JAGGED_PEAKS, "alpine");
        put(Biomes.WINDSWEPT_FOREST, "forested_hills");
        put(Biomes.SWAMP, "green_swamp", "marsh");
        put(Biomes.JUNGLE, "mini_jungle", "extreme_jungle");
        put(Biomes.SPARSE_JUNGLE, "rainforest");
        put(Biomes.SAVANNA, "savanna");
        put(Biomes.DESERT, "wasteland", "mountain_desert");
        put(Biomes.BADLANDS, "mountain_ridge");
        put(Biomes.SAVANNA_PLATEAU, "mountain_ridge");
        put(Biomes.MUSHROOM_FIELDS, "forested_island");
    }

    private static void put(ResourceKey<Biome> vanilla, String... ebxl) {
        REPLACEMENTS.put(vanilla, java.util.Arrays.stream(ebxl).map(EbxlRegion::key).toList());
    }

    private static ResourceKey<Biome> key(String name) {
        return ResourceKey.create(Registries.BIOME, Identifier.fromNamespaceAndPath(Extrabiomes.MOD_ID, name));
    }

    private EbxlRegion(int weight) {
        super(Identifier.fromNamespaceAndPath(Extrabiomes.MOD_ID, "overworld"), RegionType.OVERWORLD, weight);
    }

    @Override
    public void addBiomes(Registry<Biome> registry, Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> mapper) {
        for (Pair<Climate.ParameterPoint, ResourceKey<Biome>> pair : new ModifiedVanillaOverworldBuilder().build()) {
            Climate.ParameterPoint point = pair.getFirst();
            ResourceKey<Biome> biome = pair.getSecond();
            List<ResourceKey<Biome>> choices = REPLACEMENTS.get(biome);
            if (choices != null) {
                biome = choices.get(Math.floorMod(cell(point), choices.size()));
            }
            mapper.accept(Pair.of(point, biome));
        }
    }

    /** Same number for all points of one temperature/humidity/weirdness-sign cell. */
    private static int cell(Climate.ParameterPoint point) {
        long h = point.temperature().min() * 31 + point.humidity().min();
        h = h * 31 + (point.weirdness().min() < 0 ? 1 : 0);
        return Long.hashCode(h * 0x9E3779B97F4A7C15L);
    }

    /** Material rules for the surface of the biomes (TerraBlender applies them to extrabiomesxl:* biomes only). */
    private static final ResourceKey<MaterialRule> SURFACE = ResourceKey.create(Registries.MATERIAL_RULE,
            Identifier.fromNamespaceAndPath(Extrabiomes.MOD_ID, "overworld_surface"));

    /** Called once at common setup. */
    public static void register() {
        int weight = IntegratedConfig.extrabiomesRegionWeight();
        if (weight > 0) {
            Regions.register(new EbxlRegion(weight));
        }
        MaterialRuleManager.addRules(MaterialRuleManager.RuleCategory.OVERWORLD, Extrabiomes.MOD_ID, SURFACE);
    }
}
