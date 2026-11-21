package twilightforest.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;
import twilightforest.TFTower;

/**
 * TileEntityTFTowerBuilder of the active carminite builder: every 10 ticks one built block further in the direction the
 * nearest player (within 16) looks, at most 17 blocks; when the way is blocked it stops, and 60 ticks later the builder
 * times out. Not saved, as in 1.5.2 (an unloaded builder starts again stopped).
 */
public class TowerBuilderBlockEntity extends BlockEntity {
    int ticksRunning;
    int blockedCounter;
    int ticksStopped;
    public boolean makingBlocks;
    int blocksMade;
    private @Nullable BlockPos lastBlockCoords;
    private int nextFacing;
    private @Nullable Player trackedPlayer;

    public TowerBuilderBlockEntity(BlockPos pos, BlockState state) {
        super(TFTower.BUILDER_ENTITY.get(), pos, state);
    }

    public void startBuilding() {
        this.makingBlocks = true;
        this.blocksMade = 0;
        this.lastBlockCoords = this.worldPosition;
    }

    void tick(Level level, BlockPos pos) {
        if (this.makingBlocks) {
            if (this.trackedPlayer == null) {
                this.trackedPlayer = level.getNearestPlayer(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 16.0, false);
            }
            this.nextFacing = this.findNextFacing();
            this.ticksRunning++;
            if (this.ticksRunning % 10 == 0 && this.lastBlockCoords != null && this.nextFacing != -1) {
                BlockPos next = this.lastBlockCoords.relative(Direction.from3DDataValue(this.nextFacing));
                if (this.blocksMade <= 16 && level.isEmptyBlock(next)) {
                    level.setBlock(next, TowerTranslucentBlock.translucent(TowerTranslucentBlock.BUILT_INACTIVE), Block.UPDATE_ALL);
                    level.levelEvent(LevelEvent.SOUND_DISPENSER_FAIL, next, 0);
                    this.lastBlockCoords = next;
                    this.blockedCounter = 0;
                    this.blocksMade++;
                } else {
                    this.blockedCounter++;
                }
            }
            if (this.blockedCounter > 0) {
                this.makingBlocks = false;
                this.trackedPlayer = null;
                this.ticksStopped = 0;
            }
        } else {
            this.trackedPlayer = null;
            if (++this.ticksStopped == 60) {
                level.setBlock(pos, TowerDeviceBlock.device(TowerDeviceBlock.BUILDER_TIMEOUT), Block.UPDATE_ALL);
                level.scheduleTick(pos, TFTower.DEVICES.get(TowerDeviceBlock.BUILDER_TIMEOUT).get(), 4);
            }
        }
    }

    /** Facing of 1.5.2 (0 down, 1 up, 2 north, 3 south, 4 west, 5 east) from the player's pitch and yaw. */
    private int findNextFacing() {
        if (this.trackedPlayer != null) {
            int pitch = Mth.floor(this.trackedPlayer.getXRot() * 4.0F / 360.0F + 1.5) & 3;
            if (pitch == 0) {
                return 1;
            }
            if (pitch == 2) {
                return 0;
            }
            int direction = Mth.floor(this.trackedPlayer.getYRot() * 4.0F / 360.0F + 0.5) & 3;
            return switch (direction) {
                case 0 -> 3;
                case 1 -> 4;
                case 2 -> 2;
                default -> 5;
            };
        }
        return -1;
    }
}
