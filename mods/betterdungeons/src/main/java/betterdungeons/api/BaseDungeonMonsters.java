package betterdungeons.api;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import org.jspecify.annotations.Nullable;

import java.util.Random;

/**
 * API.BaseDungeonMonsters: one dungeon mob family as the spawner of the mod (and the dungeon builders) ask for it. The
 * lvl is 0..4 (the spawner's metadata % 5). Null means "not available": the spawner then makes a vanilla mob.
 */
public abstract class BaseDungeonMonsters {
    int id;

    public void setID(int id) {
        this.id = id;
    }

    public int getID() {
        return this.id;
    }

    public abstract @Nullable Entity getWarriorMob(ServerLevel level, int lvl, int x, int y, int z);

    public abstract @Nullable Entity getRangedMob(ServerLevel level, int lvl, int x, int y, int z);

    public @Nullable Entity getSpecialMob(ServerLevel level, int lvl, int x, int y, int z) {
        return this.getWarriorMob(level, lvl, x, y, z);
    }

    public abstract @Nullable Entity getBoss(ServerLevel level, int x, int y, int z);

    /** The entity name for vanilla spawners ("BetterDungeons.Pirate"), null for none. */
    public @Nullable String getSpawnerName(int x, int y, int z, Random random) {
        return null;
    }

    public abstract String getEntityName();

    public int getWeight() {
        return 0;
    }
}
