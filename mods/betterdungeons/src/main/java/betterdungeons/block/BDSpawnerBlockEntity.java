package betterdungeons.block;

import betterdungeons.BetterDungeons;
import betterdungeons.api.BaseDungeonMonsters;
import betterdungeons.api.RegisterDungeonMobs;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import org.jspecify.annotations.Nullable;

/**
 * BlockTileEntityMobSpawner: a one-use spawner. While a player who is not in creative mode is within 16 blocks it
 * counts down (20 ticks after loading), then spawns numMobsPerSpawner (1 in the pack) dungeon mobs at its centre and
 * turns into air. With 6 or more mobs of that kind within 3 blocks it waits 200..799 ticks instead.
 *
 * <p>The mob: family {@link #mob} of {@link RegisterDungeonMobs} (index as in the dungeon configuration), metadata 0..4
 * warrior of level metadata, 5..9 ranged, 10..14 warrior again, 15 the family's boss. Without a family (or while its
 * mobs are not ported) the 1.5.2 fallback: zombie (0..4), skeleton (5..9), spider (10..15); a missing boss is a
 * zombie as well (BlockTileEntityMobSpawner.updateEntity).
 */
public class BDSpawnerBlockEntity extends BlockEntity {
    /** numMobsPerSpawner of the pack's Chocolate/config.prop; below 1 only vanilla mobs (one). */
    public static final int NUM_MOBS_PER_SPAWNER = 1;

    private int mob = 1;
    private int metadata;
    private int delay = 20;

    public BDSpawnerBlockEntity(BlockPos pos, BlockState state) {
        super(BetterDungeons.SPAWNER_ENTITY.get(), pos, state);
    }

    public void set(int mob, int metadata) {
        this.mob = mob;
        this.metadata = metadata;
        this.setChanged();
    }

    public int mob() {
        return this.mob;
    }

    public int metadata() {
        return this.metadata;
    }

    private boolean anyPlayerInRange(Level level) {
        Player player = level.getNearestPlayer(this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 0.5, this.worldPosition.getZ() + 0.5, 16.0,
                false);
        return player != null && !player.isCreative();
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BDSpawnerBlockEntity spawner) {
        if (!(level instanceof ServerLevel server) || !spawner.anyPlayerInRange(level)) {
            return;
        }
        if (spawner.delay == -1) {
            spawner.updateDelay(level);
        }
        if (spawner.delay > 0) {
            spawner.delay--;
            return;
        }
        int numMobs = NUM_MOBS_PER_SPAWNER;
        boolean vanillaOnly = numMobs < 1;
        if (spawner.metadata == 15 || numMobs < 1) {
            numMobs = 1;
        }
        for (int i = 0; i < numMobs; i++) {
            int mob = spawner.metadata != 15 && vanillaOnly ? -1 : spawner.mob;
            Entity entity = getEntity(spawner.metadata, mob, server, pos.getX(), pos.getY(), pos.getZ());
            if (entity == null) {
                entity = EntityTypes.ZOMBIE.create(server, EntitySpawnReason.SPAWNER);
                if (entity == null) {
                    return;
                }
            }
            AABB box = new AABB(pos).inflate(3.0, 1.0, 3.0);
            Class<? extends Entity> kind = entity.getClass();
            if (server.getEntitiesOfClass(kind, box).size() >= 6) {
                spawner.updateDelay(level);
                return;
            }
            entity.snapTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, server.getRandom().nextFloat() * 360.0F, 0.0F);
            if (entity instanceof Mob m && !(entity instanceof betterdungeons.api.BDDungeonMob)) {
                m.finalizeSpawn(server, server.getCurrentDifficultyAt(pos), EntitySpawnReason.SPAWNER, null);
            }
            server.addFreshEntity(entity);
            server.levelEvent(LevelEvent.PARTICLES_MOBBLOCK_SPAWN, pos, 0);
            if (entity instanceof Mob m) {
                m.spawnAnim();
            }
        }
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
    }

    private void updateDelay(Level level) {
        this.delay = 200 + level.getRandom().nextInt(600);
    }

    /** BlockTileEntityMobSpawner.getEntity. */
    public static @Nullable Entity getEntity(int metadata, int mob, ServerLevel level, int x, int y, int z) {
        BaseDungeonMonsters family = mob >= 0 ? RegisterDungeonMobs.get(mob) : null;
        Entity e = null;
        if (family != null) {
            if (metadata <= 4) {
                e = family.getWarriorMob(level, metadata % 5, x, y, z);
            } else if (metadata <= 9) {
                e = family.getRangedMob(level, metadata % 5, x, y, z);
            } else if (metadata <= 14) {
                e = family.getWarriorMob(level, metadata % 5, x, y, z);
            } else {
                e = family.getBoss(level, x, y, z);
            }
            if (e == null && metadata == 15) {
                return null;
            }
        }
        if (e == null) {
            if (metadata < 5) {
                e = EntityTypes.ZOMBIE.create(level, EntitySpawnReason.SPAWNER);
            } else if (metadata <= 9) {
                e = EntityTypes.SKELETON.create(level, EntitySpawnReason.SPAWNER);
            } else {
                e = EntityTypes.SPIDER.create(level, EntitySpawnReason.SPAWNER);
            }
        }
        return e;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("mob", this.mob);
        output.putInt("metadata", this.metadata);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.mob = input.getIntOr("mob", 1);
        this.metadata = input.getIntOr("metadata", 0);
    }
}
