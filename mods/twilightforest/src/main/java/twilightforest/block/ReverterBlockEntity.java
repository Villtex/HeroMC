package twilightforest.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;
import twilightforest.TFTower;

import java.util.Random;

/**
 * TileEntityTFReverter of the antibuilder: while a player is within 16 it remembers the blocks within 4 and puts back,
 * 1 in 10 per check, what was changed (a block taken away comes back as the antibuilt block). Checks every 20 ticks,
 * every tick while it is reverting. Not saved: a reloaded antibuilder takes the blocks as they are then.
 */
public class ReverterBlockEntity extends BlockEntity {
    private static final DustParticleOptions RED_DUST = new DustParticleOptions(0xFF0000, 1.0F);
    public final int radius = 4;
    public final int diameter = 2 * this.radius + 1;
    public final double requiredPlayerRange = 16.0;
    public final Random rand = new Random();
    private int tickCount;
    private boolean slowScan;
    private int ticksSinceChange;
    private BlockState @Nullable [] blockData;

    public ReverterBlockEntity(BlockPos pos, BlockState state) {
        super(TFTower.REVERTER_ENTITY.get(), pos, state);
    }

    void tick(Level level, BlockPos pos) {
        if (level.getNearestPlayer(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, this.requiredPlayerRange, false) != null) {
            this.tickCount++;
            if (level.isClientSide()) {
                level.addParticle(RED_DUST, pos.getX() + level.getRandom().nextFloat(), pos.getY() + level.getRandom().nextFloat(),
                        pos.getZ() + level.getRandom().nextFloat(), 0.0, 0.0, 0.0);
                if (this.rand.nextInt(10) == 0) {
                    this.makeOutline(level, this.rand.nextInt(12));
                    this.makeOutline(level, this.rand.nextInt(12));
                    this.makeOutline(level, this.rand.nextInt(12));
                }
            } else {
                if (this.blockData == null) {
                    this.captureBlockData(level);
                    this.slowScan = true;
                }
                if (!this.slowScan || this.tickCount % 20 == 0) {
                    if (this.scanAndRevertChanges((ServerLevel) level)) {
                        this.slowScan = false;
                        this.ticksSinceChange = 0;
                    } else {
                        this.ticksSinceChange++;
                        if (this.ticksSinceChange > 20) {
                            this.slowScan = true;
                        }
                    }
                }
            }
        } else {
            this.blockData = null;
            this.tickCount = 0;
        }
    }

    /** makeOutline: red dust along one of the 12 edges of the box, and from one end to the block. */
    private void makeOutline(Level level, int outline) {
        double cx = this.worldPosition.getX();
        double cy = this.worldPosition.getY();
        double cz = this.worldPosition.getZ();
        double sx = cx, sy = cy, sz = cz, dx = cx, dy = cy, dz = cz;
        int r = this.radius;
        switch (outline) {
            case 0, 8 -> { sx -= r; dx += r + 1; sz -= r; dz -= r; }
            case 1, 9 -> { sx -= r; dx -= r; sz -= r; dz += r + 1; }
            case 2, 10 -> { sx -= r; dx += r + 1; sz += r + 1; dz += r + 1; }
            case 3, 11 -> { sx += r + 1; dx += r + 1; sz -= r; dz += r + 1; }
            case 4 -> { sx -= r; dx -= r; sz -= r; dz -= r; }
            case 5 -> { sx += r + 1; dx += r + 1; sz -= r; dz -= r; }
            case 6 -> { sx += r + 1; dx += r + 1; sz += r + 1; dz += r + 1; }
            case 7 -> { sx -= r; dx -= r; sz += r + 1; dz += r + 1; }
            default -> {
            }
        }
        switch (outline) {
            case 0, 1, 2, 3 -> { sy += r + 1; dy += r + 1; }
            case 4, 5, 6, 7 -> { sy -= r; dy += r + 1; }
            default -> { sy -= r; dy -= r; }
        }
        if (this.rand.nextBoolean()) {
            this.drawParticleLine(level, cx + 0.5, cy + 0.5, cz + 0.5, dx, dy, dz);
        } else {
            this.drawParticleLine(level, sx, sy, sz, cx + 0.5, cy + 0.5, cz + 0.5);
        }
        this.drawParticleLine(level, sx, sy, sz, dx, dy, dz);
    }

    private void drawParticleLine(Level level, double srcX, double srcY, double srcZ, double destX, double destY, double destZ) {
        int particles = 16;
        for (int i = 0; i < particles; i++) {
            double trailFactor = i / (particles - 1.0);
            double tx = srcX + (destX - srcX) * trailFactor + this.rand.nextFloat() * 0.005;
            double ty = srcY + (destY - srcY) * trailFactor + this.rand.nextFloat() * 0.005;
            double tz = srcZ + (destZ - srcZ) * trailFactor + this.rand.nextFloat() * 0.005;
            level.addParticle(RED_DUST, tx, ty, tz, 0.0, 0.0, 0.0);
        }
    }

    private boolean scanAndRevertChanges(ServerLevel level) {
        int index = 0;
        boolean reverted = false;
        BlockPos.MutableBlockPos at = new BlockPos.MutableBlockPos();
        for (int x = -this.radius; x <= this.radius; x++) {
            for (int y = -this.radius; y <= this.radius; y++) {
                for (int z = -this.radius; z <= this.radius; z++) {
                    at.setWithOffset(this.worldPosition, x, y, z);
                    BlockState there = level.getBlockState(at);
                    // 1.5.2 compared the block ids only
                    if (this.blockData[index].getBlock() != there.getBlock()) {
                        if (this.revertBlock(level, at.immutable(), there, this.blockData[index])) {
                            reverted = true;
                        } else {
                            this.blockData[index] = there;
                        }
                    }
                    index++;
                }
            }
        }
        return reverted;
    }

    private boolean revertBlock(ServerLevel level, BlockPos pos, BlockState there, BlockState replace) {
        // Block.getBlocksMovement is true for blocks one can walk through: those taken away do not come back
        if (there.isAir() && replace.getCollisionShape(level, pos).isEmpty()) {
            return false;
        }
        if (isUnrevertable(there, replace)) {
            return false;
        }
        if (this.rand.nextInt(10) == 0) {
            if (!replace.isAir()) {
                replace = TowerTranslucentBlock.translucent(TowerTranslucentBlock.REVERTER_REPLACEMENT);
            }
            level.setBlock(pos, replace, Block.UPDATE_ALL);
            if (there.isAir()) {
                level.levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, pos, Block.getId(replace));
            } else if (replace.isAir()) {
                level.levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, pos, Block.getId(there));
                Block.dropResources(there, level, pos);
            }
        }
        return true;
    }

    private static boolean isUnrevertable(BlockState there, BlockState replace) {
        if (there.getBlock() instanceof TowerDeviceBlock || replace.getBlock() instanceof TowerDeviceBlock) {
            return true;
        }
        // 1.5.2: "replaceBlockID == 4" (cobblestone) where the metadata 4 was meant
        if (there.getBlock() instanceof TowerTranslucentBlock && !replace.is(Blocks.COBBLESTONE)) {
            return true;
        }
        if (replace.getBlock() instanceof TowerTranslucentBlock translucent && translucent.meta != TowerTranslucentBlock.REVERTER_REPLACEMENT) {
            return true;
        }
        if (there.is(Blocks.REDSTONE_LAMP) && replace.is(Blocks.REDSTONE_LAMP)) {
            return true;
        }
        if (there.is(Blocks.WATER) || replace.is(Blocks.WATER)) {
            return true;
        }
        return replace.is(Blocks.TNT);
    }

    private void captureBlockData(Level level) {
        this.blockData = new BlockState[this.diameter * this.diameter * this.diameter];
        int index = 0;
        for (int x = -this.radius; x <= this.radius; x++) {
            for (int y = -this.radius; y <= this.radius; y++) {
                for (int z = -this.radius; z <= this.radius; z++) {
                    this.blockData[index++] = level.getBlockState(this.worldPosition.offset(x, y, z));
                }
            }
        }
    }
}
