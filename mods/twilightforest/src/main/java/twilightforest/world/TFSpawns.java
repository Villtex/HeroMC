package twilightforest.world;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.WeightedList;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import org.jspecify.annotations.Nullable;
import twilightforest.TFEntities;
import twilightforest.TwilightForest;
import twilightforest.gen.TFFeature;
import twilightforest.gen.TFTerrain;
import twilightforest.gen.TFWorld;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * ChunkProviderTwilightForest.getPossibleCreatures: inside a major feature its spawn list (by the part of the structure,
 * Teil 24d/24e), below the sea level (y 32) the underground monsters, else the biome's lists. The swamp and the dark
 * forest offer their monsters only 1 time in 20 (TFBiomeSwamp / TFBiomeDarkForest.getSpawnableList).
 */
public final class TFSpawns {
    private TFSpawns() {
    }

    private static final ResourceKey<Biome> SWAMP = ResourceKey.create(Registries.BIOME, TwilightForest.id("twilight_swamp"));
    private static final ResourceKey<Biome> DARK_FOREST = ResourceKey.create(Registries.BIOME, TwilightForest.id("dark_forest"));
    private static final RandomSource MONSTER_RNG = RandomSource.create(53439L);

    /** A spawn entry of 1.5.2 (class, weight, min group, max group). */
    private record Entry(Supplier<? extends EntityType<?>> type, int weight, int min, int max) {
        MobSpawnSettings.SpawnerData data() {
            IntProvider count = this.min == this.max ? ConstantInt.of(this.min) : UniformInt.of(this.min, this.max);
            return new MobSpawnSettings.SpawnerData(this.type.get(), count);
        }
    }

    private static Entry e(Supplier<? extends EntityType<?>> type, int weight, int min, int max) {
        return new Entry(type, weight, min, max);
    }

    private static Supplier<EntityType<?>> v(EntityType<?> type) {
        return () -> type;
    }

    /** TFFeature: the monster lists of each feature (index 0, 1, ...), filled in its static block. */
    private static Map<TFFeature, List<List<Entry>>> monsterLists() {
        Map<TFFeature, List<List<Entry>>> m = new HashMap<>();
        m.put(TFFeature.WIZARD_TOWER, List.of(List.of(e(v(EntityTypes.ZOMBIE), 10, 4, 4), e(v(EntityTypes.SKELETON), 10, 4, 4),
                e(v(EntityTypes.CREEPER), 1, 4, 4), e(v(EntityTypes.ENDERMAN), 1, 1, 4), e(TFEntities.DEATH_TOME, 10, 4, 4))));
        m.put(TFFeature.HILL1, List.of(List.of(e(v(EntityTypes.SPIDER), 10, 4, 4), e(v(EntityTypes.ZOMBIE), 10, 4, 4),
                e(TFEntities.REDCAP, 10, 4, 4), e(TFEntities.SWARM_SPIDER, 10, 4, 4), e(TFEntities.KOBOLD, 10, 4, 8))));
        m.put(TFFeature.HILL2, List.of(List.of(e(TFEntities.REDCAP, 10, 4, 4), e(TFEntities.KOBOLD, 10, 4, 8),
                e(v(EntityTypes.SKELETON), 10, 4, 4), e(TFEntities.SWARM_SPIDER, 10, 4, 4), e(v(EntityTypes.SPIDER), 10, 4, 4),
                e(v(EntityTypes.CREEPER), 10, 4, 4), e(TFEntities.FIRE_BEETLE, 5, 4, 4))));
        m.put(TFFeature.HILL3, List.of(List.of(e(TFEntities.REDCAP_SAPPER, 1, 1, 4), e(TFEntities.SLIME_BEETLE, 5, 4, 4),
                e(TFEntities.REDCAP, 10, 4, 4), e(TFEntities.REDCAP_SAPPER, 2, 1, 4), e(v(EntityTypes.SKELETON), 10, 4, 4),
                e(v(EntityTypes.CAVE_SPIDER), 10, 4, 4), e(v(EntityTypes.CREEPER), 10, 4, 4), e(v(EntityTypes.ENDERMAN), 1, 1, 4),
                e(TFEntities.WRAITH, 2, 1, 4), e(TFEntities.FIRE_BEETLE, 10, 4, 4), e(TFEntities.SLIME_BEETLE, 10, 4, 4),
                e(TFEntities.PINCH_BEETLE, 10, 2, 4))));
        m.put(TFFeature.LABYRINTH, List.of(List.of(e(TFEntities.MINOTAUR, 20, 2, 4), e(v(EntityTypes.CAVE_SPIDER), 10, 4, 4),
                e(v(EntityTypes.CREEPER), 10, 4, 4), e(TFEntities.MAZE_SLIME, 10, 4, 4), e(v(EntityTypes.ENDERMAN), 1, 1, 4),
                e(TFEntities.FIRE_BEETLE, 10, 4, 4), e(TFEntities.SLIME_BEETLE, 10, 4, 4), e(TFEntities.PINCH_BEETLE, 10, 2, 4))));
        // the dark tower and the knight stronghold: towerAndStronghold
        towerAndStronghold(m);
        return m;
    }

    /** The lists of the dark tower (index 1: the tower ghasts of the top) and the knight stronghold. */
    private static void towerAndStronghold(Map<TFFeature, List<List<Entry>>> m) {
    }

    private static final List<Entry> UNDERGROUND = List.of(e(v(EntityTypes.SPIDER), 10, 4, 4), e(v(EntityTypes.ZOMBIE), 10, 4, 4),
            e(v(EntityTypes.SKELETON), 10, 4, 4), e(v(EntityTypes.CREEPER), 1, 4, 4), e(v(EntityTypes.SLIME), 10, 4, 4),
            e(v(EntityTypes.ENDERMAN), 1, 1, 4), e(TFEntities.KOBOLD, 10, 4, 8));
    /** Every feature's ambient list: bats; the dark tower's water list: squids. */
    private static final List<Entry> FEATURE_AMBIENT = List.of(e(v(EntityTypes.BAT), 10, 8, 8));
    private static final List<Entry> DARK_TOWER_WATER = List.of(e(v(EntityTypes.SQUID), 10, 4, 4));

    private static volatile @Nullable Map<TFFeature, List<WeightedList<MobSpawnSettings.SpawnerData>>> features;
    private static volatile @Nullable WeightedList<MobSpawnSettings.SpawnerData> underground;
    private static volatile @Nullable WeightedList<MobSpawnSettings.SpawnerData> featureAmbient;
    private static volatile @Nullable WeightedList<MobSpawnSettings.SpawnerData> darkTowerWater;

    private static WeightedList<MobSpawnSettings.SpawnerData> list(List<Entry> entries) {
        WeightedList.Builder<MobSpawnSettings.SpawnerData> builder = WeightedList.builder();
        for (Entry entry : entries) {
            builder.add(entry.data(), entry.weight());
        }
        return builder.build();
    }

    private static void init() {
        if (features == null) {
            Map<TFFeature, List<WeightedList<MobSpawnSettings.SpawnerData>>> m = new HashMap<>();
            monsterLists().forEach((feature, lists) -> m.put(feature, lists.stream().map(TFSpawns::list).toList()));
            underground = list(UNDERGROUND);
            featureAmbient = list(FEATURE_AMBIENT);
            darkTowerWater = list(DARK_TOWER_WATER);
            features = m;
        }
    }

    /**
     * The index of the feature's spawn list at a position (MapGenTFMajorFeature.getSpawnListIndexAt): -1 outside the
     * structure. The structures come with Teil 24d/24e; until then nothing is inside one.
     */
    public static int spawnListIndexAt(TFWorld world, int x, int y, int z) {
        return -1;
    }

    /** The gate of the swamp and dark forest stays open for the rest of the tick once it opened (see the class doc). */
    private static long openTick = Long.MIN_VALUE;
    private static long openChunk;

    /** getPossibleCreatures; null means the biome's own lists. */
    public static synchronized @Nullable WeightedList<MobSpawnSettings.SpawnerData> mobsAt(TFWorld world, Level level, MobCategory category,
            BlockPos pos, Holder<Biome> biome) {
        init();
        TFFeature nearest = TFFeature.getNearestFeatureIncludeMore(pos.getX() >> 4, pos.getZ() >> 4, world.layout());
        if (nearest != TFFeature.NOTHING) {
            int index = spawnListIndexAt(world, pos.getX(), pos.getY(), pos.getZ());
            if (index >= 0) {
                if (category == MobCategory.MONSTER) {
                    List<WeightedList<MobSpawnSettings.SpawnerData>> lists = features.get(nearest);
                    return lists != null && index < lists.size() ? lists.get(index) : WeightedList.of();
                }
                if (category == MobCategory.AMBIENT) {
                    return featureAmbient;
                }
                if (category == MobCategory.WATER_CREATURE) {
                    return nearest == TFFeature.DARK_TOWER ? darkTowerWater : WeightedList.of();
                }
                return WeightedList.of();
            }
        }
        if (pos.getY() < TFTerrain.SEALEVEL && category == MobCategory.MONSTER) {
            return underground;
        }
        if (category == MobCategory.MONSTER && (biome.is(SWAMP) || biome.is(DARK_FOREST))) {
            long tick = level.getGameTime();
            long chunk = ((long) (pos.getX() >> 4) << 32) ^ (pos.getZ() >> 4);
            if (tick == openTick && chunk == openChunk) {
                return null;
            }
            if (MONSTER_RNG.nextInt(20) == 0) {
                openTick = tick;
                openChunk = chunk;
                return null;
            }
            return WeightedList.of();
        }
        return null;
    }
}
