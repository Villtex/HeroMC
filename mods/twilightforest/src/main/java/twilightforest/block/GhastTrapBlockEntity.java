package twilightforest.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import twilightforest.TFTower;
import twilightforest.entity.MiniGhast;
import twilightforest.entity.TowerGhast;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

/**
 * TileEntityTFGhastTrapInactive / TileEntityTFGhastTrapActive. The inactive trap counts the mini ghasts that die within
 * 10 (16 up and down) of it; with three it is charged, and redstone then switches it on. The active trap pulls every
 * ghast in the 13 × 33 × 13 box above it towards itself, hurts it 1 in 10 ticks (3 for the ur-ghast, 10 for the others),
 * holds tower ghasts in the trap, and switches off after 120 ticks. Not saved, as in 1.5.2.
 */
public class GhastTrapBlockEntity extends BlockEntity {
    /** The ur-ghast (Teil 24f): stopTantrum and the smaller damage. */
    public static Consumer<Entity> UR_GHAST_IN_TRAP = ghast -> {
    };
    public static java.util.function.Predicate<Entity> IS_UR_GHAST = ghast -> false;

    int counter;
    final Random rand = new Random();
    final List<MiniGhast> dyingGhasts = new ArrayList<>();

    public GhastTrapBlockEntity(BlockPos pos, BlockState state) {
        super(TFTower.GHAST_TRAP_ENTITY.get(), pos, state);
    }

    public boolean isCharged() {
        return this.dyingGhasts.size() >= 3;
    }

    void tick(Level level, BlockPos pos, boolean active) {
        if (active) {
            this.tickActive(level, pos);
        } else {
            this.tickInactive(level, pos);
        }
    }

    private void tickInactive(Level level, BlockPos pos) {
        AABB box = new AABB(pos).inflate(10.0, 16.0, 10.0);
        List<MiniGhast> nearbyGhasts = level.getEntitiesOfClass(MiniGhast.class, box);
        for (MiniGhast ghast : nearbyGhasts) {
            if (ghast.deathTime > 0) {
                this.makeParticlesTo(level, ghast);
                if (!this.dyingGhasts.contains(ghast)) {
                    this.dyingGhasts.add(ghast);
                }
            }
        }
        int chargeLevel = Math.min(3, this.dyingGhasts.size());
        this.counter++;
        if (level.isClientSide()) {
            double x = pos.getX();
            double y = pos.getY();
            double z = pos.getZ();
            if (this.counter % 20 == 0 && !nearbyGhasts.isEmpty()) {
                this.makeParticlesTo(level, nearbyGhasts.get(this.rand.nextInt(nearbyGhasts.size())));
            }
            if (chargeLevel >= 1 && this.counter % 10 == 0) {
                TowerDeviceBlock.sparkle(level, pos, level.getRandom());
                level.playLocalSound(x + 0.5, y + 1.5, z + 0.5, SoundEvents.NOTE_BLOCK_HARP.value(), SoundSource.BLOCKS, 1.0F, 1.0F, false);
            }
            if (chargeLevel >= 2) {
                level.addParticle(ParticleTypes.SMOKE, x + 0.1 + this.rand.nextFloat() * 0.8, y + 1.05, z + 0.1 + this.rand.nextFloat() * 0.8,
                        (this.rand.nextFloat() - this.rand.nextFloat()) * 0.05, 0.0, (this.rand.nextFloat() - this.rand.nextFloat()) * 0.05);
                if (this.counter % 10 == 0) {
                    level.playLocalSound(x + 0.5, y + 1.5, z + 0.5, SoundEvents.NOTE_BLOCK_HARP.value(), SoundSource.BLOCKS, 1.2F, 0.8F, false);
                }
            }
            if (chargeLevel >= 3) {
                level.addParticle(ParticleTypes.LARGE_SMOKE, x + 0.1 + this.rand.nextFloat() * 0.8, y + 1.05, z + 0.1 + this.rand.nextFloat() * 0.8,
                        (this.rand.nextFloat() - this.rand.nextFloat()) * 0.05, 0.05, (this.rand.nextFloat() - this.rand.nextFloat()) * 0.05);
                TowerDeviceBlock.sparkle(level, pos, level.getRandom());
                if (this.counter % 5 == 0) {
                    level.playLocalSound(x + 0.5, y + 1.5, z + 0.5, SoundEvents.NOTE_BLOCK_HARP.value(), SoundSource.BLOCKS, 1.5F, 2.0F, false);
                }
            }
        }
    }

    /** Five "ghasttrap" particles from the trap to the ghast's eyes (here the portal particle, which moves the same way). */
    private void makeParticlesTo(Level level, Entity highlight) {
        if (!level.isClientSide()) {
            return;
        }
        double sx = this.worldPosition.getX() + 0.5;
        double sy = this.worldPosition.getY() + 1.0;
        double sz = this.worldPosition.getZ() + 0.5;
        double dx = sx - highlight.getX();
        double dy = sy - highlight.getY() - highlight.getEyeHeight();
        double dz = sz - highlight.getZ();
        for (int i = 0; i < 5; i++) {
            level.addParticle(ParticleTypes.PORTAL, sx, sy, sz, -dx, -dy, -dz);
        }
    }

    private void tickActive(Level level, BlockPos pos) {
        this.counter++;
        double x = pos.getX();
        double y = pos.getY();
        double z = pos.getZ();
        if (level.isClientSide()) {
            if (this.counter > 100 && this.counter % 4 == 0) {
                level.addParticle(ParticleTypes.LARGE_SMOKE, x + 0.5, y + 0.95, z + 0.5, Math.cos(this.counter / 10.0) * 0.05, 0.25,
                        Math.sin(this.counter / 10.0) * 0.05);
            } else if (this.counter < 100) {
                double dx = Math.cos(this.counter / 10.0) * 2.5;
                double dy = 20.0;
                double dz = Math.sin(this.counter / 10.0) * 2.5;
                level.addParticle(ParticleTypes.PORTAL, x + 0.5, y + 1.0, z + 0.5, dx, dy, dz);
                level.addParticle(ParticleTypes.PORTAL, x + 0.5, y + 1.0, z + 0.5, -dx, dy, -dz);
                level.addParticle(ParticleTypes.PORTAL, x + 0.5, y + 1.0, z + 0.5, -dx, dy / 2.0, dz);
                level.addParticle(ParticleTypes.PORTAL, x + 0.5, y + 1.0, z + 0.5, dx, dy / 2.0, -dz);
                level.addParticle(ParticleTypes.PORTAL, x + 0.5, y + 1.0, z + 0.5, dx / 2.0, dy / 4.0, dz / 2.0);
                level.addParticle(ParticleTypes.PORTAL, x + 0.5, y + 1.0, z + 0.5, -dx / 2.0, dy / 4.0, -dz / 2.0);
            }
            // played every tick in 1.5.2, where a sound already playing was not started again
            if (this.counter == 1) {
                level.playLocalSound(x + 0.5, y + 1.5, z + 0.5, TFTower.TRAP_WARMUP.get(), SoundSource.BLOCKS, 1.0F, 4.0F, false);
            } else if (this.counter == 30) {
                level.playLocalSound(x + 0.5, y + 1.5, z + 0.5, TFTower.TRAP_ON.get(), SoundSource.BLOCKS, 1.0F, 4.0F, false);
            } else if (this.counter == 80) {
                level.playLocalSound(x + 0.5, y + 1.5, z + 0.5, TFTower.TRAP_SPINDOWN.get(), SoundSource.BLOCKS, 1.0F, 4.0F, false);
            }
            return;
        }
        AABB box = new AABB(x, y + 16.0, z, x + 1, y + 16 + 1, z + 1).inflate(6.0, 16.0, 6.0);
        for (Entity ghast : level.getEntitiesOfClass(Entity.class, box, e -> e instanceof Ghast || e instanceof TowerGhast)) {
            boolean urGhast = IS_UR_GHAST.test(ghast);
            if (urGhast) {
                UR_GHAST_IN_TRAP.accept(ghast);
            }
            ghast.setDeltaMovement(new Vec3((ghast.getX() - x - 0.5) * -0.1, (ghast.getY() - y - (urGhast ? 2.5 : 1.5)) * -0.1,
                    (ghast.getZ() - z - 0.5) * -0.1));
            ghast.needsSync = true;
            if (this.rand.nextInt(10) == 0) {
                ghast.hurtServer((ServerLevel) level, level.damageSources().generic(), urGhast ? 3.0F : 10.0F);
            }
            if (ghast instanceof TowerGhast towerGhast) {
                towerGhast.setInTrap();
            }
        }
        if (this.counter >= 120) {
            level.setBlock(pos, TowerDeviceBlock.device(TowerDeviceBlock.GHASTTRAP_INACTIVE), Block.UPDATE_ALL);
        }
    }
}
