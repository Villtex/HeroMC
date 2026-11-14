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
 * own. Twilight Forest mobs (and the ones of mods not in the pack) are left out until those mods are ported.
 */
public final class HomeDimensions {
    private static final Map<EntityType<?>, ResourceKey<Level>> HOME = new LinkedHashMap<>();

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
        return HOME.containsKey(type);
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
        ResourceKey<Level> home = HOME.get(type);
        if (home == null) {
            return false;
        }
        if (dimension == Level.NETHER || dimension == Level.END) {
            return home == dimension;
        }
        return home == Level.OVERWORLD;
    }
}
