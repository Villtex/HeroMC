package twilightforest.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import twilightforest.TFEntities;
import twilightforest.TFTower;
import twilightforest.entity.MiniGhast;

import java.util.Random;

/**
 * TileEntityTFCReactorActive: the running carminite reactor. At tick 5 it puts fake gold and diamond around itself,
 * then grows blobs of debris and holes (one around itself, two at random corners 3 away, which turn into netherrack
 * and fire), and at tick 350 it lets out six mini ghasts and explodes. Not saved, as in 1.5.2.
 */
public class ReactorBlockEntity extends BlockEntity {
    int counter;
    final int secX;
    final int secY;
    final int secZ;
    int terX;
    int terY;
    int terZ;

    public ReactorBlockEntity(BlockPos pos, BlockState state) {
        super(TFTower.REACTOR_ENTITY.get(), pos, state);
        Random rand = new Random();
        this.secX = 3 * (rand.nextBoolean() ? 1 : -1);
        this.secY = 3 * (rand.nextBoolean() ? 1 : -1);
        this.secZ = 3 * (rand.nextBoolean() ? 1 : -1);
        this.terX = 3 * (rand.nextBoolean() ? 1 : -1);
        this.terY = 3 * (rand.nextBoolean() ? 1 : -1);
        this.terZ = 3 * (rand.nextBoolean() ? 1 : -1);
        if (this.secX == this.terX && this.secY == this.terY && this.secZ == this.terZ) {
            this.terX = -this.terX;
            this.terY = -this.terY;
            this.terZ = -this.terZ;
        }
    }

    void tick(Level level, BlockPos pos) {
        this.counter++;
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        if (!(level instanceof ServerLevel server)) {
            if (this.counter % 5 == 0 && this.counter <= 250) {
                level.playLocalSound(x + 0.5, y + 0.5, z + 0.5, SoundEvents.PORTAL_AMBIENT, SoundSource.BLOCKS, this.counter / 100.0F,
                        this.counter / 100.0F, false);
            }
            return;
        }
        int offset = 10;
        if (this.counter % 5 == 0) {
            if (this.counter == 5) {
                BlockState gold = TowerTranslucentBlock.translucent(TowerTranslucentBlock.FAKE_GOLD);
                BlockState diamond = TowerTranslucentBlock.translucent(TowerTranslucentBlock.FAKE_DIAMOND);
                for (int dy = 1; dy >= -1; dy--) {
                    // the corners of the 3 x 3 rings are diamond above and below, gold in the middle ring; the edges the other way
                    BlockState corner = dy == 0 ? gold : diamond;
                    BlockState edge = dy == 0 ? diamond : gold;
                    level.setBlock(pos.offset(1, dy, 1), corner, Block.UPDATE_CLIENTS);
                    level.setBlock(pos.offset(1, dy, -1), corner, Block.UPDATE_CLIENTS);
                    level.setBlock(pos.offset(-1, dy, 1), corner, Block.UPDATE_CLIENTS);
                    level.setBlock(pos.offset(-1, dy, -1), corner, Block.UPDATE_CLIENTS);
                    level.setBlock(pos.offset(0, dy, 1), edge, Block.UPDATE_CLIENTS);
                    level.setBlock(pos.offset(0, dy, -1), edge, Block.UPDATE_CLIENTS);
                    level.setBlock(pos.offset(1, dy, 0), edge, Block.UPDATE_CLIENTS);
                    level.setBlock(pos.offset(-1, dy, 0), edge, Block.UPDATE_CLIENTS);
                }
            }
            int primary = this.counter - 80;
            if (primary >= offset && primary <= 249) {
                this.drawBlob(server, x, y, z, (primary - offset) / 40, null, primary - offset, false);
            }
            if (primary <= 200) {
                this.drawBlob(server, x, y, z, primary / 40, TowerTranslucentBlock.translucent(TowerTranslucentBlock.REACTOR_DEBRIS), this.counter, false);
            }
            int secondary = this.counter - 120;
            if (secondary >= offset && secondary <= 129) {
                this.drawBlob(server, x + this.secX, y + this.secY, z + this.secZ, (secondary - offset) / 40, null, secondary - offset, false);
            }
            if (secondary >= 0 && secondary <= 160) {
                this.drawBlob(server, x + this.secX, y + this.secY, z + this.secZ, secondary / 40, null, secondary, true);
            }
            int tertiary = this.counter - 160;
            if (tertiary >= offset && tertiary <= 129) {
                this.drawBlob(server, x + this.terX, y + this.terY, z + this.terZ, (tertiary - offset) / 40, null, tertiary - offset, false);
            }
            if (tertiary >= 0 && tertiary <= 160) {
                this.drawBlob(server, x + this.terX, y + this.terY, z + this.terZ, tertiary / 40, null, tertiary, true);
            }
        }
        if (this.counter >= 350) {
            for (int i = 0; i < 3; i++) {
                this.spawnGhastNear(server, x + this.secX, y + this.secY, z + this.secZ);
                this.spawnGhastNear(server, x + this.terX, y + this.terY, z + this.terZ);
            }
            // createExplosion(null, x, y, z, 2, true): breaks blocks, no fire
            level.explode(null, x, y, z, 2.0F, Level.ExplosionInteraction.TNT);
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        }
    }

    private void spawnGhastNear(ServerLevel level, int x, int y, int z) {
        MiniGhast ghast = TFEntities.MINI_GHAST.get().create(level, EntitySpawnReason.MOB_SUMMONED);
        if (ghast != null) {
            ghast.snapTo(x - 1.5 + level.getRandom().nextFloat() * 3.0, y - 1.5 + level.getRandom().nextFloat() * 3.0,
                    z - 1.5 + level.getRandom().nextFloat() * 3.0, level.getRandom().nextFloat() * 360.0F, 0.0F);
            level.addFreshEntity(ghast);
        }
    }

    /** drawBlob: the shell of radius {@code rad} (a rough distance) in one of the eight octants, chosen by {@code fuzz}. */
    private void drawBlob(ServerLevel level, int sx, int sy, int sz, int rad, BlockState block, int fuzz, boolean netherTransform) {
        for (byte dx = 0; dx <= rad; dx++) {
            int fuzzX = (fuzz + dx) % 8;
            for (byte dy = 0; dy <= rad; dy++) {
                int fuzzY = (fuzz + dy) % 8;
                for (byte dz = 0; dz <= rad; dz++) {
                    byte dist;
                    if (dx >= dy && dx >= dz) {
                        dist = (byte) (dx + (byte) (Math.max(dy, dz) * 0.5 + Math.min(dy, dz) * 0.25));
                    } else if (dy >= dx && dy >= dz) {
                        dist = (byte) (dy + (byte) (Math.max(dx, dz) * 0.5 + Math.min(dx, dz) * 0.25));
                    } else {
                        dist = (byte) (dz + (byte) (Math.max(dx, dy) * 0.5 + Math.min(dx, dy) * 0.25));
                    }
                    if (dist == rad && (dx != 0 || dy != 0 || dz != 0)) {
                        int ox = (fuzzX & 2) == 0 ? dx : -dx;
                        int oy = (fuzzX & 4) == 0 ? dy : -dy;
                        int oz = (fuzzX & 1) == 0 ? dz : -dz;
                        this.transformBlock(level, new BlockPos(sx + ox, sy + oy, sz + oz), block, fuzzY, netherTransform);
                    }
                }
            }
        }
    }

    private void transformBlock(ServerLevel level, BlockPos pos, BlockState block, int fuzz, boolean netherTransform) {
        BlockState there = level.getBlockState(pos);
        if (there.isAir() || there.getDestroySpeed(level, pos) != -1.0F) {
            if (fuzz == 0 && !there.isAir()) {
                level.levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, pos, Block.getId(there));
            }
            if (netherTransform && !there.isAir()) {
                level.setBlock(pos, Blocks.NETHERRACK.defaultBlockState(), Block.UPDATE_ALL);
                if (level.isEmptyBlock(pos.above()) && fuzz % 3 == 0) {
                    level.setBlock(pos.above(), Blocks.FIRE.defaultBlockState(), Block.UPDATE_ALL);
                }
            } else {
                level.setBlock(pos, block == null ? Blocks.AIR.defaultBlockState() : block, Block.UPDATE_ALL);
            }
        }
    }
}
