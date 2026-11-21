package twilightforest.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import twilightforest.TFBosses;
import twilightforest.TwilightForest;

/**
 * TileEntityTFBossSpawner: while a player is in range (see {@link BossSpawnerBlock.Kind}) it smokes and burns on the
 * client; on the server (not on peaceful) it summons its boss at its centre with a random heading, gives it its home
 * here and turns into air.
 */
public class BossSpawnerBlockEntity extends BlockEntity {
    public BossSpawnerBlockEntity(BlockPos pos, BlockState state) {
        super(TFBosses.BOSS_SPAWNER_ENTITY.get(), pos, state);
    }

    private boolean anyPlayerInRange(Level level, BossSpawnerBlock.Kind kind) {
        BlockPos pos = this.getBlockPos();
        Player player = level.getNearestPlayer(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, kind.range, false);
        return player != null && (!kind.above || player.getY() > pos.getY() - 2);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, BossSpawnerBlockEntity spawner) {
        if (!(state.getBlock() instanceof BossSpawnerBlock block) || !spawner.anyPlayerInRange(level, block.kind())) {
            return;
        }
        if (level.isClientSide()) {
            double rx = pos.getX() + level.getRandom().nextFloat();
            double ry = pos.getY() + level.getRandom().nextFloat();
            double rz = pos.getZ() + level.getRandom().nextFloat();
            level.addParticle(ParticleTypes.SMOKE, rx, ry, rz, 0.0, 0.0, 0.0);
            level.addParticle(ParticleTypes.FLAME, rx, ry, rz, 0.0, 0.0, 0.0);
        } else if (level.getDifficulty() != Difficulty.PEACEFUL && level instanceof ServerLevel server) {
            EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.getValue(TwilightForest.id(block.kind().mob));
            Entity entity = type.create(server, EntitySpawnReason.SPAWNER);
            if (entity == null) {
                return;
            }
            entity.snapTo(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, level.getRandom().nextFloat() * 360.0F, 0.0F);
            if (entity instanceof Mob mob) {
                mob.setHomeTo(pos, block.kind().home);
                mob.setPersistenceRequired();
            }
            server.addFreshEntity(entity);
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
        }
    }
}
