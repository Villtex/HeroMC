package betterdungeons.api;

/**
 * IEntityClass: the "kind" of a mob of the mod ("dwarf", "goblin", "skeleton", ...; a "kind.sub" name counts as its
 * part before the dot). AITargetNearestRival attacks the humans (EntityHuman) whose kind is in its list, and the humans
 * spare mobs of their own kind. Two bosses have a kind without being humans: the golem boss ("dwarf") and the spider
 * boss ("goblin"); they return false from {@link #isHuman()}, so the rival search of the other bosses skips them as it
 * skipped everything but EntityHuman in 1.5.2.
 */
public interface BDEntityClass {
    /** IEntityClass.getType (named differently here: Entity.getType is the entity type today). */
    String getEntityClass();

    /** Whether this is an EntityHuman of 1.5.2 (the only class AITargetNearestRival looked for). */
    default boolean isHuman() {
        return true;
    }
}
