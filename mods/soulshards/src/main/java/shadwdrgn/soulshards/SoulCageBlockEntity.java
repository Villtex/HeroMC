package shadwdrgn.soulshards;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * TESoulCage. Tier 1 spawns 2 mobs every 20 s, tiers 2 and 3 four mobs every 10 s and 5 s, tiers 4 and 5 six mobs
 * every 5 s and 2 s, as long as fewer than twice that many mobs of the kind are near. Mobs need the conditions of a
 * natural spawn (tier 5: only room and no liquid) and are marked so that they give no souls. Redstone switches only
 * a tier 5 cage off. From tier 3 on the mobs cannot despawn for their first 30 seconds.
 */
public class SoulCageBlockEntity extends BlockEntity {
    /** NBT mark of a caged mob ("mobcage" in getEntityData of 1.5.2). */
    public static final String CAGED = "soulshards:mobcage";
    private static final Field PERSISTENCE = persistenceField();

    private ItemStack shard = ItemStack.EMPTY;
    private String mobType = "";
    private int mobCount;
    private int delay;
    private int count;
    private int tier;
    private boolean powered;
    private int persistCheck;
    private final List<Mob> persistent = new ArrayList<>();

    public SoulCageBlockEntity(BlockPos pos, BlockState state) {
        super(SoulShards.SOUL_CAGE_ENTITY.get(), pos, state);
    }

    private static Field persistenceField() {
        try {
            Field field = Mob.class.getDeclaredField("persistenceRequired");
            field.setAccessible(true);
            return field;
        } catch (ReflectiveOperationException e) {
            return null;
        }
    }

    public void bind(ItemStack shard, EntityType<?> type, int tier, boolean powered) {
        this.shard = shard;
        this.mobType = SoulShardItem.id(type);
        this.tier = tier;
        this.powered = powered;
        if (tier == 1) {
            this.mobCount = 2;
            this.delay = 20 * 20;
        } else if (tier < 4) {
            this.mobCount = 4;
            this.delay = (tier == 2 ? 10 : 5) * 20;
        } else {
            this.mobCount = 6;
            this.delay = (tier == 4 ? 5 : 2) * 20;
        }
        this.count = this.delay;
        this.setChanged();
    }

    public void setPowered(boolean powered) {
        if (this.powered != powered) {
            this.powered = powered;
            this.setChanged();
        }
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, SoulCageBlockEntity cage) {
        cage.tick((ServerLevel) level, pos);
    }

    private void tick(ServerLevel level, BlockPos pos) {
        if (++this.persistCheck >= 200) {
            this.persistCheck = 0;
            this.persistent.removeIf(mob -> {
                if (mob.isRemoved()) {
                    return true;
                }
                if (mob.tickCount > 600) {
                    setPersistent(mob, false);
                    return true;
                }
                return false;
            });
        }
        if (this.count >= this.delay) {
            this.count = 0;
            EntityType<?> type = HomeDimensions.type(this.mobType);
            for (int i = 0; i < this.mobCount; i++) {
                if (type == null) {
                    return;
                }
                Entity entity = type.create(level, EntitySpawnReason.SPAWNER);
                if (entity == null) {
                    return;
                }
                int near = level.getEntitiesOfClass(entity.getClass(), new AABB(pos).inflate(8.0, 4.0, 8.0)).size();
                if (near >= 2 * this.mobCount) {
                    this.count = 0;
                    return;
                }
                double x = pos.getX() + (level.getRandom().nextDouble() - level.getRandom().nextDouble()) * 4.0;
                double y = pos.getY() + level.getRandom().nextInt(3) - 1;
                double z = pos.getZ() + (level.getRandom().nextDouble() - level.getRandom().nextDouble()) * 4.0;
                entity.snapTo(x, y, z, level.getRandom().nextFloat() * 360.0F, 0.0F);
                Mob mob = entity instanceof Mob m ? m : null;
                if ((mob == null || canSpawnHere(level, type, mob) || this.tier == 5 && hasRoom(level, mob))
                        && (this.tier < 5 || !this.powered)) {
                    entity.getPersistentData().putBoolean(CAGED, true);
                    if (mob != null) {
                        mob.finalizeSpawn(level, level.getCurrentDifficultyAt(entity.blockPosition()), EntitySpawnReason.SPAWNER, null);
                    }
                    level.addFreshEntity(entity);
                    level.levelEvent(2004, pos, 0);
                    if (mob != null) {
                        if (this.tier >= 3) {
                            setPersistent(mob, true);
                            this.persistent.add(mob);
                        }
                        mob.spawnAnim();
                    }
                    this.count = 0;
                }
            }
        }
        this.count++;
    }

    /** getCanSpawnHere of 1.5.2: the checks of a natural spawn (monsters need darkness, animals grass and light). */
    private static boolean canSpawnHere(ServerLevel level, EntityType<?> type, Mob mob) {
        return SpawnPlacements.checkSpawnRules(type, level, EntitySpawnReason.NATURAL, mob.blockPosition(), level.getRandom())
                && mob.checkSpawnRules(level, EntitySpawnReason.NATURAL) && mob.checkSpawnObstruction(level);
    }

    private static boolean hasRoom(ServerLevel level, Mob mob) {
        return level.noCollision(mob, mob.getBoundingBox()) && !level.containsAnyLiquid(mob.getBoundingBox());
    }

    private static void setPersistent(Mob mob, boolean persistent) {
        if (persistent) {
            mob.setPersistenceRequired();
        } else if (PERSISTENCE != null) {
            try {
                PERSISTENCE.setBoolean(mob, false);
            } catch (IllegalAccessException ignored) {
                // stays persistent
            }
        }
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        if (this.level != null && !this.shard.isEmpty()) {
            Containers.dropItemStack(this.level, pos.getX(), pos.getY(), pos.getZ(), this.shard);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.shard = input.read("shard", ItemStack.CODEC).orElse(ItemStack.EMPTY);
        this.mobType = input.getStringOr("mobType", "");
        this.mobCount = input.getIntOr("mobCount", 0);
        this.delay = input.getIntOr("delay", 0);
        this.count = input.getIntOr("count", 0);
        this.tier = input.getIntOr("tier", 0);
        this.powered = input.getBooleanOr("signal", false);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (!this.shard.isEmpty()) {
            output.store("shard", ItemStack.CODEC, this.shard);
        }
        output.putString("mobType", this.mobType);
        output.putInt("mobCount", this.mobCount);
        output.putInt("delay", this.delay);
        output.putInt("count", this.count);
        output.putInt("tier", this.tier);
        output.putBoolean("signal", this.powered);
    }
}
