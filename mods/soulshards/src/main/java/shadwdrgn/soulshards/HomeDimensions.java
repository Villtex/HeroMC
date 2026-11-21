package shadwdrgn.soulshards;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.level.Level;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * ItemShard.homeDims: the mobs a shard can take, with the dimension a cage below tier 4 spawns them in.
 * 1.5.2 had wither skeletons and zombie villagers as "special" skeletons and zombies; today they are mobs of their
 * own. The Twilight Forest mobs of the list count as overworld mobs; they are looked up by id, so Soul Shards does not
 * need the Twilight Forest. "TwilightForest.Tower Broodling" of the original matched no mob (the broodling's name is
 * "Redscale Broodling") and stays out; "entBrainyZombie" and "entFirebat" belong to mods not in the pack.
 */
public final class HomeDimensions {
    private static final Map<EntityType<?>, ResourceKey<Level>> HOME = new LinkedHashMap<>();
    /** homeDims entries "TwilightForest.<name>" (all dimension 0) as entity ids of the port. */
    private static final java.util.Set<Identifier> TWILIGHT = java.util.Set.of(
            "wild_boar", "bighorn_sheep", "wild_deer", "redcap", "swarm_spider", "skeleton_druid", "hostile_wolf", "wraith", "hedge_spider",
            "penguin", "kobold", "minotaur", "fire_beetle", "slime_beetle", "pinch_beetle", "maze_slime", "redcap_sapper", "mini_ghast",
            "tower_ghast", "tower_termite").stream().map(id -> Identifier.fromNamespaceAndPath("twilightforest", id))
            .collect(java.util.stream.Collectors.toUnmodifiableSet());

    static {
        for (EntityType<?> type : new EntityType<?>[] {EntityTypes.BLAZE, EntityTypes.GHAST, EntityTypes.ZOMBIFIED_PIGLIN,
                EntityTypes.MAGMA_CUBE, EntityTypes.WITHER_SKELETON}) {
            HOME.put(type, Level.NETHER);
        }
        HOME.put(EntityTypes.ENDERMAN, Level.END);
        for (EntityType<?> type : new EntityType<?>[] {EntityTypes.CAVE_SPIDER, EntityTypes.SILVERFISH, EntityTypes.SKELETON,
                EntityTypes.SPIDER, EntityTypes.SLIME, EntityTypes.ZOMBIE, EntityTypes.ZOMBIE_VILLAGER, EntityTypes.CREEPER, EntityTypes.WITCH,
                EntityTypes.SHEEP, EntityTypes.COW, EntityTypes.MOOSHROOM, EntityTypes.CHICKEN, EntityTypes.BAT, EntityTypes.OCELOT,
                EntityTypes.WOLF, EntityTypes.PIG}) {
            HOME.put(type, Level.OVERWORLD);
        }
    }

    private HomeDimensions() {
    }

    public static boolean accepts(EntityType<?> type) {
        return home(type) != null;
    }

    private static ResourceKey<Level> home(EntityType<?> type) {
        ResourceKey<Level> home = HOME.get(type);
        if (home == null && TWILIGHT.contains(BuiltInRegistries.ENTITY_TYPE.getKey(type))) {
            return Level.OVERWORLD;
        }
        return home;
    }

    public static EntityType<?> type(String id) {
        Identifier key = Identifier.tryParse(id);
        return key == null ? null : BuiltInRegistries.ENTITY_TYPE.getOptional(key).orElse(null);
    }

    /**
     * canSpawnhere: nether and end mobs only in their dimension, overworld mobs anywhere else (other dimensions
     * counted as overworld); wither skeletons only in the nether.
     */
    public static boolean canSpawnIn(EntityType<?> type, ResourceKey<Level> dimension) {
        ResourceKey<Level> home = home(type);
        if (home == null) {
            return false;
        }
        if (dimension == Level.NETHER || dimension == Level.END) {
            return home == dimension;
        }
        return home == Level.OVERWORLD;
    }
}
