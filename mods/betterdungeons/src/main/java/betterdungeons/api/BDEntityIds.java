package betterdungeons.api;

import hexxit.worldgen152.OldGameIds;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import org.jspecify.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The entities of BetterDungeons.registerEntities with their 1.5.2 names ("BetterDungeons.&lt;name&gt;" in EntityList) and
 * the entity type ids the port uses for them. The items, spawn eggs, the spawner and the projectiles look the mobs up
 * here by id, so the mob port only has to register its types under these ids.
 *
 * <p>A type that is not registered yet is simply missing: {@link #type} returns null, eggs do nothing, the spawner
 * falls back to the vanilla mobs of 1.5.2 (zombie / skeleton / spider).
 */
public final class BDEntityIds {
    /** 1.5.2 name (without "BetterDungeons.") -> path of the entity type id in the betterdungeons namespace. */
    public static final Map<String, String> NAMES = new LinkedHashMap<>();

    static {
        String[][] names = {
                {"MagicArrow", "magic_arrow"}, {"ArmoredSkeleton", "armored_skeleton"}, {"ArmoredZombie", "armored_zombie"},
                {"Dwarf", "dwarf"}, {"Pirate", "pirate"}, {"ArmoredPigZombie", "armored_pig_zombie"}, {"Walker", "walker"},
                {"Specter", "specter"}, {"Goblin", "goblin"}, {"SnowGoblin", "snow_goblin"}, {"Triton", "triton"},
                {"Monkey", "monkey"}, {"Gremlin", "gremlin"}, {"GolemSmall", "golem_small"}, {"OgreArmored", "ogre_armored"},
                {"MinotaurArmored", "minotaur_armored"}, {"OrcArmored", "orc_armored"}, {"ArmoredEnderman", "armored_enderman"},
                {"PirateBoss", "pirate_boss"}, {"WalkerBoss", "walker_boss"}, {"GolemBoss", "golem_boss"}, {"IceBull", "ice_bull"},
                {"SpiderBoss", "spider_boss"}, {"Monking", "monking"}, {"SlimeBoss", "slime_boss"}, {"TurtleBoss", "turtle_boss"},
                {"TurtleBossPart", "turtle_boss_part"}, {"Necromancer", "necromancer"}, {"Liche", "liche"}, {"PigMage", "pig_mage"},
                {"SummonedUndead", "summoned_undead"}, {"SummonedBait", "summoned_bait"}, {"SpecterBoss", "specter_boss"},
                {"GremlinBoss", "gremlin_boss"}, {"MinotaurBoss", "minotaur_boss"}, {"goblinBoss", "goblin_boss"},
                {"Wyvern", "wyvern"}, {"BlockProjectile", "block_projectile"}, {"BDProjectile", "projectile"},
                {"FriendlyHuman", "friendly_human"}};
        for (String[] n : names) {
            NAMES.put(n[0], n[1]);
        }
    }

    private BDEntityIds() {
    }

    public static Identifier id(String oldName) {
        String path = NAMES.get(oldName);
        if (path == null) {
            throw new IllegalArgumentException("No Better Dungeons entity " + oldName);
        }
        return Identifier.fromNamespaceAndPath("betterdungeons", path);
    }

    /** The registered type of a 1.5.2 name ("Pirate" or "BetterDungeons.Pirate"), null while the mob is not ported. */
    public static @Nullable EntityType<?> type(String oldName) {
        String name = oldName.startsWith("BetterDungeons.") ? oldName.substring("BetterDungeons.".length()) : oldName;
        if (!NAMES.containsKey(name)) {
            return null;
        }
        return BuiltInRegistries.ENTITY_TYPE.getOptional(id(name)).orElse(null);
    }

    public static boolean is(Entity entity, String oldName) {
        return BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).equals(id(oldName));
    }

    /**
     * Points the 1.5.2 names that the templates and the vanilla spawners of the dungeons use (OldGameIds, read by the
     * world generator) to the ported types. Runs at common setup, after all registries are filled; names whose type is
     * missing keep the vanilla stand-in registered in BetterDungeons. Misspelled names of the
     * configuration ("triton", "minotaurArmored", "GremlinArmored") found no mob in 1.5.2 and are left alone.
     */
    public static void registerOldNames() {
        for (String name : NAMES.keySet()) {
            if (BuiltInRegistries.ENTITY_TYPE.containsKey(id(name))) {
                OldGameIds.registerEntity("BetterDungeons." + name, id(name).toString());
            }
        }
    }
}
