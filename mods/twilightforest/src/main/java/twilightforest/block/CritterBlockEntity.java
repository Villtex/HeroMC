package twilightforest.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import twilightforest.TFBlocks;

/**
 * TileEntityTFFirefly, TileEntityTFCicada and TileEntityTFMoonworm: the turning (yaw), the glow of the firefly and
 * the song of the cicada. Only the client ticks it (the server keeps no state). Pack config SilentCicadas=false.
 */
public class CritterBlockEntity extends BlockEntity {
    public int yawDelay;
    public int currentYaw;
    public int desiredYaw;
    // firefly
    public float glowIntensity;
    public boolean glowing;
    public int glowDelay;
    // cicada
    public int singDuration;
    public boolean singing;
    public int singDelay;
    private boolean started;

    public CritterBlockEntity(BlockPos pos, BlockState state) {
        super(TFBlocks.CRITTER_ENTITY.get(), pos, state);
    }

    public CritterBlock.Kind kind() {
        return this.getBlockState().getBlock() instanceof CritterBlock critter ? critter.kind() : CritterBlock.Kind.FIREFLY;
    }

    void clientTick() {
        Level level = this.level;
        if (level == null) {
            return;
        }
        RandomSource rand = level.getRandom();
        switch (this.kind()) {
            case FIREFLY -> this.tickFirefly(level, rand);
            case CICADA -> this.tickCicada(level, rand);
            case MOONWORM -> this.tickMoonworm(rand);
        }
    }

    private void turn(RandomSource rand) {
        if (this.yawDelay > 0) {
            this.yawDelay--;
        } else {
            if (this.currentYaw == 0 && this.desiredYaw == 0) {
                this.yawDelay = 200 + rand.nextInt(200);
                this.desiredYaw = rand.nextInt(15) - rand.nextInt(15);
            }
            if (this.currentYaw < this.desiredYaw) {
                this.currentYaw++;
            }
            if (this.currentYaw > this.desiredYaw) {
                this.currentYaw--;
            }
            if (this.currentYaw == this.desiredYaw) {
                this.desiredYaw = 0;
            }
        }
    }

    private void tickFirefly(Level level, RandomSource rand) {
        BlockPos pos = this.worldPosition;
        if (level.getNearestPlayer(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 16.0, false) != null && rand.nextInt(20) == 0) {
            level.addParticle(TFBlocks.FIREFLY_PARTICLE.get(), pos.getX() + rand.nextFloat(), pos.getY() + rand.nextFloat(),
                    pos.getZ() + rand.nextFloat(), 0.0, 0.0, 0.0);
        }
        this.turn(rand);
        if (this.glowDelay > 0) {
            this.glowDelay--;
        } else {
            if (this.glowing && this.glowIntensity >= 1.0) {
                this.glowing = false;
            }
            if (this.glowing && this.glowIntensity < 1.0) {
                this.glowIntensity = (float) (this.glowIntensity + 0.05);
            }
            if (!this.glowing && this.glowIntensity > 0.0F) {
                this.glowIntensity = (float) (this.glowIntensity - 0.05);
            }
            if (!this.glowing && this.glowIntensity <= 0.0F) {
                this.glowing = true;
                this.glowDelay = rand.nextInt(50);
            }
        }
    }

    private void tickCicada(Level level, RandomSource rand) {
        BlockPos pos = this.worldPosition;
        this.turn(rand);
        if (this.singDelay > 0) {
            this.singDelay--;
        } else {
            if (this.singing && this.singDuration == 0) {
                level.playLocalSound(pos.getX(), pos.getY(), pos.getZ(), TFBlocks.CICADA_SOUND.get(), SoundSource.AMBIENT, 1.0F,
                        (rand.nextFloat() - rand.nextFloat()) * 0.2F + 1.0F, false);
            }
            if (this.singing && this.singDuration >= 100) {
                this.singing = false;
                this.singDuration = 0;
            }
            if (this.singing && this.singDuration < 100) {
                this.singDuration++;
                if (rand.nextInt(5) == 0) {
                    level.addParticle(ParticleTypes.NOTE, pos.getX() + rand.nextFloat(), pos.getY() + rand.nextFloat(), pos.getZ() + rand.nextFloat(),
                            0.0, 0.0, 0.0);
                }
            }
            if (!this.singing && this.singDuration <= 0) {
                this.singing = true;
                this.singDelay = 100 + rand.nextInt(100);
            }
        }
    }

    private void tickMoonworm(RandomSource rand) {
        if (!this.started) {
            this.started = true;
            this.currentYaw = rand.nextInt(4) * 90;
        }
        if (this.yawDelay > 0) {
            this.yawDelay--;
        } else {
            if (this.desiredYaw == 0) {
                this.yawDelay = 200 + rand.nextInt(200);
                this.desiredYaw = rand.nextInt(4) * 90;
            }
            this.currentYaw++;
            if (this.currentYaw > 360) {
                this.currentYaw = 0;
            }
            if (this.currentYaw == this.desiredYaw) {
                this.desiredYaw = 0;
            }
        }
    }
}
