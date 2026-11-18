package betterdungeons.api;

import org.jspecify.annotations.Nullable;

/**
 * IEntityClass of 1.5.2: a mob with a Better Dungeons team ("pirate", "zombie", "dwarf", "goblin", "beast", "walker",
 * "friend.&lt;player&gt;" ...). Mobs of the same type do not fight each other, humans follow a leader of their type, and
 * the rival lists of the humans name these types. The humans implement it; bosses with a type (spider boss, golem boss)
 * may implement it too.
 */
public interface BDTeamMob {
    @Nullable String getBDType();
}
