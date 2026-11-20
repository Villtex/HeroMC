package legendgear.entity;

import legendgear.LGBlocks;
import legendgear.LGConfig;
import legendgear.LGEntities;
import legendgear.LGFx;
import legendgear.block.BombFlowerBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.TntBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * EntityBombBlast: one tick after it appears everything within 3.5 blocks takes the bomb damage (5, an explosion) and is
 * thrown away by 4 / (2 + distance), at least 0.6 upwards; in the 3x3x3 around it grown bomb flowers go off and the
 * bombable blocks of the pack (cobblestone, TNT; its 2461 was the jar's old id) break, TNT lit.
 */
public class BombBlastEntity extends LGEffectEntity {
    public BombBlastEntity(EntityType<? extends BombBlastEntity> type, Level level) {
        super(type, level);
    }

    public BombBlastEntity(Level level, @Nullable Entity owner, double x, double y, double z) {
        this(LGEntities.BOMB_BLAST.get(), level);
        this.thrower = owner;
        this.setPos(x, y, z);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.tickCount == 1) {
            Level level = this.level();
            if (level.isClientSide()) {
                for (int i = 0; i < 8; i++) {
                    level.addParticle(ParticleTypes.EXPLOSION, this.getX() + this.random.nextGaussian(), this.getY() + this.random.nextGaussian(),
                            this.getZ() + this.random.nextGaussian(), 0.0, 0.0, 0.0);
                    level.addParticle(ParticleTypes.LAVA, this.getX(), this.getY() + 0.25, this.getZ(), this.random.nextGaussian(),
                            this.random.nextGaussian(), this.random.nextGaussian());
                    level.addParticle(ParticleTypes.POOF, this.getX() + this.random.nextGaussian(), this.getY() + this.random.nextGaussian(),
                            this.getZ() + this.random.nextGaussian(), 0.0, 0.0, 0.0);
                }
            }
            if (level instanceof ServerLevel server) {
                LGFx.sound(this, SoundEvents.GENERIC_EXPLODE.value(), 3.0F, 0.7F + (float) Math.random() * 0.2F);
                DamageSource damage = this.damageSources().explosion(this, this.thrower);
                double blastRadius = 3.5;
                for (Entity e : level.getEntities(this, this.getBoundingBox().inflate(4.0))) {
                    Vec3 away = new Vec3(e.getX() - this.getX(), e.getY() - this.getY(), e.getZ() - this.getZ());
                    double dist = away.length();
                    if (dist < blastRadius) {
                        away = away.normalize();
                        if (away.y < 0.6) {
                            away = new Vec3(away.x, 0.6, away.z);
                        }
                        double force = 4.0 / (2.0 + dist);
                        if (e instanceof LivingEntity) {
                            e.hurtServer(server, damage, LGConfig.BOMB_DAMAGE);
                        }
                        if (!(e instanceof BombEntity bomb) || !bomb.unblastable) {
                            e.setDeltaMovement(away.scale(force));
                            e.needsSync = true;
                        }
                        e.resetFallDistance();
                    }
                }
                BlockPos c = BlockPos.containing(this.getX(), this.getY(), this.getZ());
                for (BlockPos p : BlockPos.betweenClosed(c.offset(-1, -1, -1), c.offset(1, 1, 1))) {
                    BlockState state = level.getBlockState(p);
                    if (state.is(LGBlocks.BOMB_FLOWER.get()) && !state.getValue(BombFlowerBlock.CUT)) {
                        LGBlocks.BOMB_FLOWER.get().explode(level, p.immutable(), state);
                    } else if (state.is(Blocks.TNT)) {
                        level.removeBlock(p, false);
                        TntBlock.prime(level, p.immutable());
                    } else if (state.is(Blocks.COBBLESTONE)) {
                        level.destroyBlock(p, false);
                    }
                }
            }
        }
        if (this.tickCount > 3 && !this.level().isClientSide()) {
            this.discard();
        }
    }
}
