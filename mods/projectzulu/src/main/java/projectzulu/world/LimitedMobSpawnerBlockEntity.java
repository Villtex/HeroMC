package projectzulu.world;

import hexxit.worldgen152.OldGameIds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Spawner;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import org.jspecify.annotations.Nullable;
import projectzulu.ZuluWorld;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * TileEntityLimitedMobSpawner: a mob spawner that stops after {@code MaxSpawnableEntities} mobs (0 = never) and then
 * turns into air. While a player who can be hurt (any player in debug mode) is within {@code RequiredPlayerRange} it
 * counts its delay down, then tries {@code SpawnCount} mobs within {@code SpawnRange} of its corner, unless
 * {@code MaxNearbyEntities} of that kind are around. A mob gets the NBT of {@code SpawnData} written over it, or
 * initCreature (finalizeSpawn) without. {@code SpawnPotentials} picks the next SpawnData by weight. The saved keys are
 * those of 1.5.2; {@code EntityId} is an entity type id now (1.5.2 names such as "Chicken" are still understood).
 * Not ported: the spawn sound of a SpawnData (a file from the config folder) and the editor.
 */
public class LimitedMobSpawnerBlockEntity extends BlockEntity implements Spawner {
    /** TileEntityLimitedMobSpawnData: weight, entity type, NBT written over the mob, spawn sound. */
    public record SpawnEntry(int weight, String type, CompoundTag properties, String spawnSound) {
        static SpawnEntry of(CompoundTag tag) {
            return new SpawnEntry(tag.getIntOr("Weight", 0), tag.getStringOr("Type", ""), tag.getCompoundOrEmpty("Properties").copy(),
                    tag.getStringOr("SpawnSound", ""));
        }

        CompoundTag toTag() {
            CompoundTag tag = new CompoundTag();
            tag.put("Properties", this.properties.copy());
            tag.putString("Type", this.type);
            tag.putInt("Weight", this.weight);
            tag.putString("SpawnSound", this.spawnSound);
            return tag;
        }
    }

    public int delay = 20;
    private CompoundTag debugSavedSetup = new CompoundTag();
    private String mobID = "minecraft:chicken";
    private @Nullable List<SpawnEntry> spawnList;
    private @Nullable SpawnEntry spawnerTags;
    public double yaw;
    public double yaw2;
    private int minSpawnDelay = 200;
    private int maxSpawnDelay = 800;
    private int spawnCount = 4;
    private @Nullable Entity displayEntity;
    private int maxSpawnableEntities = 5;
    /** Not saved, as in the original: after a reload the spawner counts from 0 again. */
    private int spawnedEntities;
    private int maxNearbyEntities = 6;
    private int requiredPlayerRange = 16;
    private int spawnRange = 4;

    public LimitedMobSpawnerBlockEntity(BlockPos pos, BlockState state) {
        super(ZuluWorld.LIMITED_MOB_SPAWNER_ENTITY.get(), pos, state);
    }

    public boolean isDebugEnabled() {
        return !this.debugSavedSetup.isEmpty();
    }

    public String getEntityName() {
        return this.spawnerTags == null ? this.mobID : this.spawnerTags.type();
    }

    /** The entity type of an id of today or a 1.5.2 entity name, or {@code null}. */
    static @Nullable EntityType<?> entityType(String name) {
        if (name.indexOf(':') >= 0) {
            Identifier id = Identifier.tryParse(name);
            return id == null ? null : BuiltInRegistries.ENTITY_TYPE.getOptional(id).orElse(null);
        }
        try {
            return OldGameIds.entity(name);
        } catch (IllegalStateException e) {
            return null;
        }
    }

    private boolean anyPlayerInRange(Level level) {
        double x = this.worldPosition.getX() + 0.5;
        double y = this.worldPosition.getY() + 0.5;
        double z = this.worldPosition.getZ() + 0.5;
        Player player = level.getNearestPlayer(x, y, z, this.requiredPlayerRange, this.isDebugEnabled() ? null : EntitySelector.NO_CREATIVE_OR_SPECTATOR);
        return player != null;
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, LimitedMobSpawnerBlockEntity spawner) {
        if (!spawner.anyPlayerInRange(level)) {
            return;
        }
        RandomSource random = level.getRandom();
        double x = pos.getX() + random.nextFloat();
        double y = pos.getY() + random.nextFloat();
        double z = pos.getZ() + random.nextFloat();
        level.addParticle(ParticleTypes.SMOKE, x, y, z, 0.0, 0.0, 0.0);
        level.addParticle(ParticleTypes.FLAME, x, y, z, 0.0, 0.0, 0.0);
        if (spawner.delay > 0) {
            spawner.delay--;
        }
        spawner.yaw2 = spawner.yaw;
        spawner.yaw = (spawner.yaw + 1000.0F / (spawner.delay + 200.0F)) % 360.0;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, LimitedMobSpawnerBlockEntity spawner) {
        if (!(level instanceof ServerLevel server)) {
            return;
        }
        if (spawner.maxSpawnableEntities > 0 && spawner.spawnedEntities >= spawner.maxSpawnableEntities) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        }
        if (!spawner.anyPlayerInRange(level)) {
            return;
        }
        if (spawner.delay == -1) {
            spawner.updateDelay(server);
        }
        if (spawner.delay > 0) {
            spawner.delay--;
            return;
        }
        RandomSource random = server.getRandom();
        boolean spawned = false;
        for (int n = 0; n < spawner.spawnCount; n++) {
            EntityType<?> type = entityType(spawner.getEntityName());
            Entity entity = type == null ? null : type.create(server, EntitySpawnReason.SPAWNER);
            if (entity == null) {
                return;
            }
            AABB around = new AABB(pos).inflate(spawner.spawnRange * 2, 4.0, spawner.spawnRange * 2);
            if (server.getEntitiesOfClass(entity.getClass(), around).size() >= spawner.maxNearbyEntities) {
                spawner.updateDelay(server);
                return;
            }
            double x = pos.getX() + (random.nextDouble() - random.nextDouble()) * spawner.spawnRange;
            double y = pos.getY() + random.nextInt(3) - 1;
            double z = pos.getZ() + (random.nextDouble() - random.nextDouble()) * spawner.spawnRange;
            spawner.writeNBTTagsTo(entity, server);
            entity.snapTo(x, y, z, random.nextFloat() * 360.0F, 0.0F);
            if (spawner.spawnerTags == null && entity instanceof Mob mob) {
                // initCreature
                mob.finalizeSpawn(server, server.getCurrentDifficultyAt(entity.blockPosition()), EntitySpawnReason.SPAWNER, null);
            }
            Mob living = entity instanceof Mob mob ? mob : null;
            if (living == null || living.checkSpawnRules(server, EntitySpawnReason.SPAWNER) && living.checkSpawnObstruction(server)) {
                server.addFreshEntity(entity);
                server.levelEvent(LevelEvent.PARTICLES_MOBBLOCK_SPAWN, pos, 0);
                if (!spawner.isDebugEnabled()) {
                    spawner.spawnedEntities++;
                }
                if (living != null) {
                    living.spawnAnim();
                }
                spawned = true;
            }
        }
        if (spawned) {
            spawner.updateDelay(server);
        }
    }

    /** writeNBTTagsTo: the SpawnData properties go over what the entity saves of itself. */
    private void writeNBTTagsTo(Entity entity, Level level) {
        if (this.spawnerTags == null || this.spawnerTags.properties().isEmpty()) {
            return;
        }
        HolderLookup.Provider registries = level.registryAccess();
        TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, registries);
        entity.saveWithoutId(output);
        CompoundTag tag = output.buildResult();
        tag.merge(this.spawnerTags.properties());
        entity.load(TagValueInput.create(ProblemReporter.DISCARDING, registries, tag));
    }

    private void updateDelay(ServerLevel level) {
        RandomSource random = level.getRandom();
        if (this.maxSpawnDelay <= this.minSpawnDelay) {
            this.delay = this.minSpawnDelay;
        } else {
            this.delay = this.minSpawnDelay + random.nextInt(this.maxSpawnDelay - this.minSpawnDelay);
        }
        if (this.spawnList != null && !this.spawnList.isEmpty()) {
            int total = 0;
            for (SpawnEntry entry : this.spawnList) {
                total += entry.weight();
            }
            if (total > 0) {
                int pick = random.nextInt(total);
                for (SpawnEntry entry : this.spawnList) {
                    pick -= entry.weight();
                    if (pick < 0) {
                        this.spawnerTags = entry;
                        break;
                    }
                }
            }
            this.setChanged();
            level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), Block.UPDATE_ALL);
        }
        level.blockEvent(this.worldPosition, this.getBlockState().getBlock(), 1, 0);
    }

    /** receiveClientEvent 1: the client restarts its delay. */
    @Override
    public boolean triggerEvent(int id, int param) {
        if (id == 1 && this.level != null && this.level.isClientSide()) {
            this.delay = this.minSpawnDelay;
            return true;
        }
        return super.triggerEvent(id, param);
    }

    /** The mob the renderer shows turning inside the cage (getMobEntity). */
    public @Nullable Entity getMobEntity() {
        if (this.displayEntity == null && this.level != null) {
            EntityType<?> type = entityType(this.getEntityName());
            Entity entity = type == null ? null : type.create(this.level, EntitySpawnReason.SPAWNER);
            if (entity != null) {
                this.writeNBTTagsTo(entity, this.level);
            }
            this.displayEntity = entity;
        }
        return this.displayEntity;
    }

    // ---- Spawner: a spawn egg sets the mob (instead of the editor of the original)

    @Override
    public void setEntityId(EntityType<?> type, RandomSource random) {
        this.mobID = BuiltInRegistries.ENTITY_TYPE.getKey(type).toString();
        this.spawnerTags = null;
        this.spawnList = null;
        this.displayEntity = null;
        this.setChanged();
    }

    @Override
    public void setEntityData(net.minecraft.world.item.component.TypedEntityData<EntityType<?>> entityData, RandomSource random) {
        this.mobID = BuiltInRegistries.ENTITY_TYPE.getKey(entityData.type()).toString();
        this.spawnerTags = new SpawnEntry(1, this.mobID, entityData.copyTagWithoutId(), "");
        this.spawnList = null;
        this.displayEntity = null;
        this.setChanged();
    }

    // ---- saved data, keys of 1.5.2

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.mobID = input.getStringOr("EntityId", "");
        this.delay = input.getShortOr("Delay", (short) 0);
        Optional<ValueInput.TypedInputList<CompoundTag>> potentials = input.list("SpawnPotentials", CompoundTag.CODEC);
        if (potentials.isPresent()) {
            List<SpawnEntry> list = new ArrayList<>();
            potentials.get().stream().forEach(tag -> list.add(SpawnEntry.of(tag)));
            this.spawnList = list;
        } else {
            this.spawnList = null;
        }
        this.spawnerTags = input.read("SpawnData", CompoundTag.CODEC).map(tag -> new SpawnEntry(1, this.mobID, tag, "")).orElse(null);
        if (input.getInt("MinSpawnDelay").isPresent()) {
            this.minSpawnDelay = input.getShortOr("MinSpawnDelay", (short) this.minSpawnDelay);
            this.maxSpawnDelay = input.getShortOr("MaxSpawnDelay", (short) this.maxSpawnDelay);
            this.spawnCount = input.getShortOr("SpawnCount", (short) this.spawnCount);
            this.maxSpawnableEntities = input.getShortOr("MaxSpawnableEntities", (short) this.maxSpawnableEntities);
        }
        this.maxNearbyEntities = input.getShortOr("MaxNearbyEntities", (short) this.maxNearbyEntities);
        this.requiredPlayerRange = input.getShortOr("RequiredPlayerRange", (short) this.requiredPlayerRange);
        this.spawnRange = input.getShortOr("SpawnRange", (short) this.spawnRange);
        this.debugSavedSetup = input.read("DebugSavedSetup", CompoundTag.CODEC).orElseGet(CompoundTag::new);
        this.displayEntity = null;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.store("DebugSavedSetup", CompoundTag.CODEC, this.debugSavedSetup);
        output.putString("EntityId", this.getEntityName());
        output.putShort("Delay", (short) this.delay);
        output.putShort("MinSpawnDelay", (short) this.minSpawnDelay);
        output.putShort("MaxSpawnDelay", (short) this.maxSpawnDelay);
        output.putShort("SpawnCount", (short) this.spawnCount);
        output.putShort("MaxNearbyEntities", (short) this.maxNearbyEntities);
        output.putShort("RequiredPlayerRange", (short) this.requiredPlayerRange);
        output.putShort("SpawnRange", (short) this.spawnRange);
        output.putShort("MaxSpawnableEntities", (short) this.maxSpawnableEntities);
        if (this.spawnerTags != null) {
            output.store("SpawnData", CompoundTag.CODEC, this.spawnerTags.properties().copy());
        }
        if (this.spawnerTags != null || this.spawnList != null && !this.spawnList.isEmpty()) {
            ValueOutput.TypedOutputList<CompoundTag> list = output.list("SpawnPotentials", CompoundTag.CODEC);
            if (this.spawnList != null && !this.spawnList.isEmpty()) {
                for (SpawnEntry entry : this.spawnList) {
                    list.add(entry.toTag());
                }
            } else {
                list.add(this.spawnerTags.toTag());
            }
        }
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveCustomOnly(registries);
    }
}
