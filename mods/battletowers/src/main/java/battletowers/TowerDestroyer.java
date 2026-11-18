package battletowers;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * AS_TowerDestroyer: after the guardian's death the tower collapses from the top, one floor of 7 blocks every 5
 * seconds, 15 seconds after the death; each floor is blown up (power 10, by the golem's killer) and the blocks left in
 * the 16 x 8 x 16 box above it are cleared. It stops below y 70 and after six floors. In between, fizzing and
 * crumbling sounds. Like the original, the collapses in progress are not saved.
 */
public final class TowerDestroyer {
    private static final List<TowerDestroyer> ACTIVE = new ArrayList<>();
    private static final long INITIAL_DELAY = 300;
    private static final long FLOOR_DELAY = 100;

    private final ServerLevel level;
    private final Entity player;
    private final int x;
    private final int y;
    private final int z;
    private long triggerTime;
    private long lastExplosionSoundTime;
    private int floor = 6;
    private boolean deleteMe;

    private TowerDestroyer(ServerLevel level, int x, int y, int z, Entity killer) {
        this.level = level;
        this.player = killer;
        this.x = x;
        this.y = y;
        this.z = z;
        this.triggerTime = level.getGameTime();
        this.lastExplosionSoundTime = this.triggerTime;
        level.playSound(null, x, y, z, BattleTowers.TOWER_BREAK_START.get(), SoundSource.HOSTILE, 4.0F, 1.0F);
    }

    /** onBattleTowerDestroyed: tells the players within 100 blocks and starts the collapse. */
    public static synchronized void start(ServerLevel level, int x, int y, int z, Entity killer) {
        for (ServerPlayer p : level.players()) {
            if (p.distanceToSqr(killer) < 100.0 * 100.0) {
                p.sendSystemMessage(Component.literal("A Battletower's Guardian has fallen! Without it's power, the Tower will collapse..."));
            }
        }
        ACTIVE.add(new TowerDestroyer(level, x, y, z, killer));
    }

    /** ServerTickHandler: once a second. */
    static synchronized void tick(ServerTickEvent.Post event) {
        if (event.getServer().getTickCount() % 20 != 0) {
            return;
        }
        ACTIVE.removeIf(td -> {
            if (td.deleteMe) {
                return true;
            }
            td.update();
            return false;
        });
    }

    private double yCoord() {
        return this.y - 7 * Math.abs(6 - this.floor);
    }

    private void update() {
        if (this.deleteMe || this.yCoord() < 70.0) {
            return;
        }
        long now = this.level.getGameTime();
        if (this.floor == 6 && now > this.triggerTime + INITIAL_DELAY) {
            this.triggerTime = now;
            this.blowUpFloor();
        } else if (this.floor < 6 && now > this.triggerTime + FLOOR_DELAY) {
            if (this.floor < 1) {
                this.deleteMe = true;
                return;
            }
            this.triggerTime = now;
            this.blowUpFloor();
        } else if (now > this.lastExplosionSoundTime + 80) {
            int sx = this.x - 7 + this.level.getRandom().nextInt(15);
            int sz = this.z - 7 + this.level.getRandom().nextInt(15);
            float pitch = (1.0F + (this.level.getRandom().nextFloat() - this.level.getRandom().nextFloat()) * 0.2F) * 0.7F;
            switch (this.level.getRandom().nextInt(4)) {
                case 0 -> this.level.playSound(null, sx, this.yCoord(), sz, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 4.0F, pitch);
                case 1 -> this.level.playSound(null, sx, this.yCoord(), sz, BattleTowers.TOWER_CRUMBLE.get(), SoundSource.BLOCKS, 4.0F, pitch);
                default -> {
                }
            }
            this.lastExplosionSoundTime = now;
        }
    }

    private void blowUpFloor() {
        this.level.explode(this.player, this.x, this.yCoord(), this.z, 10.0F, false, Level.ExplosionInteraction.TNT);
        int ytemp = (int) this.yCoord();
        for (int xi = -8; xi < 8; xi++) {
            for (int zi = -8; zi < 8; zi++) {
                for (int yi = 1; yi < 9; yi++) {
                    BlockPos pos = new BlockPos(this.x + xi, ytemp + yi, this.z + zi);
                    if (!this.level.getBlockState(pos).isAir()) {
                        this.level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                    }
                }
            }
        }
        this.floor--;
    }
}
