package twilightforest.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import twilightforest.TFBlocks;

/**
 * TileEntityTFSmoker, TileEntityTFPoppingJet and TileEntityTFFlameJet in one: what it does follows the block state.
 * Both sides count; the server switches the states, plays the sounds and burns, the client makes the particles.
 */
public class FireJetBlockEntity extends BlockEntity {
    private long counter;
    private Object phase;

    public FireJetBlockEntity(BlockPos pos, BlockState state) {
        super(TFBlocks.FIRE_JET_ENTITY.get(), pos, state);
    }

    void tick(Level level, BlockPos pos, BlockState state) {
        if (!(state.getBlock() instanceof FireJetBlock block)) {
            return;
        }
        Object now = state.hasProperty(FireJetBlock.PHASE) ? state.getValue(FireJetBlock.PHASE)
                : state.hasProperty(FireJetBlock.ACTIVE) ? state.getValue(FireJetBlock.ACTIVE) : Boolean.TRUE;
        if (now != this.phase) {
            // a new tile entity in 1.5.2
            this.phase = now;
            this.counter = 0;
        }
        if (now == Boolean.TRUE) {
            this.smoke(level, pos);
        } else if (now == FireJetBlock.Phase.POPPING) {
            this.popping(level, pos, state);
        } else if (now == FireJetBlock.Phase.FLAME) {
            this.flame(level, pos, state);
        }
    }

    /** TileEntityTFSmoker: "hugesmoke" every 4 ticks, turning around. */
    private void smoke(Level level, BlockPos pos) {
        if (++this.counter % 4L == 0L && level.isClientSide()) {
            level.addParticle(ParticleTypes.LARGE_SMOKE, pos.getX() + 0.5, pos.getY() + 0.95, pos.getZ() + 0.5,
                    Math.cos(this.counter / 10.0) * 0.05, 0.25, Math.sin(this.counter / 10.0) * 0.05);
        }
    }

    /** TileEntityTFPoppingJet: 80 ticks of lava pops, then the flame. */
    private void popping(Level level, BlockPos pos, BlockState state) {
        if (++this.counter >= 80) {
            this.counter = 0;
            if (!level.isClientSide()) {
                level.setBlock(pos, state.setValue(FireJetBlock.PHASE, FireJetBlock.Phase.FLAME), Block.UPDATE_ALL);
            }
        } else if (this.counter % 20 == 0) {
            if (level.isClientSide()) {
                level.addParticle(ParticleTypes.LAVA, pos.getX() + 0.5, pos.getY() + 1.5, pos.getZ() + 0.5, 0.0, 0.0, 0.0);
            } else {
                level.playSound(null, pos.getX(), pos.getY(), pos.getZ(), SoundEvents.LAVA_POP, SoundSource.BLOCKS,
                        0.2F + level.getRandom().nextFloat() * 0.2F, 0.9F + level.getRandom().nextFloat() * 0.15F);
            }
        }
    }

    /** TileEntityTFFlameJet: 60 ticks of flames that set everything around (x and z +-2, 4 up) on fire, then idle. */
    private void flame(Level level, BlockPos pos, BlockState state) {
        if (++this.counter > 60) {
            this.counter = 0;
            if (!level.isClientSide()) {
                level.setBlock(pos, state.setValue(FireJetBlock.PHASE, FireJetBlock.Phase.IDLE), Block.UPDATE_ALL);
            }
            return;
        }
        double x = pos.getX();
        double y = pos.getY();
        double z = pos.getZ();
        if (this.counter % 2 == 0 && level.isClientSide()) {
            level.addParticle(ParticleTypes.LARGE_SMOKE, x + 0.5, y + 1.0, z + 0.5, 0.0, 0.0, 0.0);
            // "largeflame" of the original: vanilla flames with its speed
            level.addParticle(ParticleTypes.FLAME, x + 0.5, y + 1.0, z + 0.5, 0.0, 0.5, 0.0);
            level.addParticle(ParticleTypes.FLAME, x - 0.5, y + 1.0, z + 0.5, 0.05, 0.5, 0.0);
            level.addParticle(ParticleTypes.FLAME, x + 0.5, y + 1.0, z - 0.5, 0.0, 0.5, 0.05);
            level.addParticle(ParticleTypes.FLAME, x + 1.5, y + 1.0, z + 0.5, -0.05, 0.5, 0.0);
            level.addParticle(ParticleTypes.FLAME, x + 0.5, y + 1.0, z + 1.5, 0.0, 0.5, -0.05);
        }
        if (level instanceof ServerLevel server) {
            if (this.counter % 4 == 0) {
                level.playSound(null, x + 0.5, y + 0.5, z + 0.5, SoundEvents.GHAST_SHOOT, SoundSource.BLOCKS,
                        1.0F + level.getRandom().nextFloat(), level.getRandom().nextFloat() * 0.7F + 0.3F);
            } else if (this.counter == 1) {
                level.playSound(null, x + 0.5, y + 0.5, z + 0.5, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS,
                        1.0F + level.getRandom().nextFloat(), level.getRandom().nextFloat() * 0.7F + 0.3F);
            }
            if (this.counter % 5 == 0) {
                for (Entity entity : level.getEntitiesOfClass(Entity.class, new AABB(x - 2, y, z - 2, x + 2, y + 4, z + 2))) {
                    if (!entity.fireImmune()) {
                        entity.hurtServer(server, level.damageSources().inFire(), 2.0F);
                        entity.igniteForSeconds(15.0F);
                    }
                }
            }
        }
    }
}
