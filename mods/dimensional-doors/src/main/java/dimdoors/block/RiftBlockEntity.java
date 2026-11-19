package dimdoors.block;

import dimdoors.DimDoors;
import dimdoors.world.DDData.LinkData;
import dimdoors.world.DDWorld;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.monster.Enderman;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;

import java.util.Random;

/**
 * TileEntityRift: every 200 ticks it looks for the nearest rift within 5 blocks; near one it may grow a new rift of the
 * same link and spawns endermen; without a link it goes. A rift told to close (rift remover) tells its neighbour
 * after a second and vanishes after two.
 */
public class RiftBlockEntity extends BlockEntity {
    public int xOffset;
    public int yOffset;
    public int zOffset;
    public int distance;
    public boolean hasGrownRifts;
    public boolean shouldClose;
    public boolean isNearRift;
    private int count = 200;
    private int count2;
    private final Random rand = new Random();

    public RiftBlockEntity(BlockPos pos, BlockState state) {
        super(DimDoors.RIFT_ENTITY.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, RiftBlockEntity rift) {
        if (level instanceof ServerLevel server) {
            rift.serverTick(server);
        }
    }

    private void serverTick(ServerLevel level) {
        BlockPos pos = this.worldPosition;
        int dim = DDWorld.dimId(level, pos);
        if (this.count > 200 && DDWorld.data().dim(dim) != null) {
            LinkData nearest = nearestRift(level, dim, 5);
            int oldX = this.xOffset;
            int oldY = this.yOffset;
            int oldZ = this.zOffset;
            if (nearest != null) {
                this.xOffset = pos.getX() - nearest.locX;
                this.yOffset = pos.getY() - nearest.locY;
                this.zOffset = pos.getZ() - nearest.locZ;
                this.distance = Math.abs(this.xOffset) + Math.abs(this.yOffset) + Math.abs(this.zOffset);
                this.isNearRift = true;
                if (this.distance > 1) {
                    this.grow(level, dim, this.distance);
                }
                if (this.rand.nextInt(30) == 0) {
                    AABB box = new AABB(pos.getX() - 9, pos.getY() - 3, pos.getZ() - 9, pos.getX() + 9, pos.getY() + 3, pos.getZ() + 9);
                    if (level.getEntitiesOfClass(Enderman.class, box).isEmpty()) {
                        Enderman enderman = EntityTypes.ENDERMAN.create(level, EntitySpawnReason.EVENT);
                        if (enderman != null) {
                            enderman.snapTo(pos.getX() + 0.5, pos.getY() - 1, pos.getZ() + 0.5, 5.0F, 6.0F);
                            level.addFreshEntity(enderman);
                        }
                    }
                }
            } else {
                this.isNearRift = false;
            }
            this.count = 0;
            if (oldX != this.xOffset || oldY != this.yOffset || oldZ != this.zOffset) {
                level.sendBlockUpdated(pos, this.getBlockState(), this.getBlockState(), Block.UPDATE_CLIENTS);
            }
            this.setChanged();
        } else if (DDWorld.link(dim, pos.getX(), pos.getY(), pos.getZ()) == null) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            return;
        }
        this.count++;
        if (this.shouldClose) {
            if (this.count2 > 20 && this.count2 < 22) {
                LinkData nearest = nearestRift(level, dim, 10);
                if (nearest != null && level.getBlockEntity(new BlockPos(nearest.locX, nearest.locY, nearest.locZ)) instanceof RiftBlockEntity other) {
                    other.close();
                }
            }
            if (this.count2 > 40) {
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                if (DDWorld.link(dim, pos.getX(), pos.getY(), pos.getZ()) != null) {
                    DDWorld.removeLink(dim, pos.getX(), pos.getY(), pos.getZ());
                    level.playSound(null, pos, DimDoors.RIFT_CLOSE.get(), SoundSource.BLOCKS, 0.7F, 1.0F);
                }
                return;
            }
            this.count2++;
        }
    }

    public void close() {
        if (!this.shouldClose) {
            this.shouldClose = true;
            this.setChanged();
            if (this.level != null) {
                this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), Block.UPDATE_CLIENTS);
            }
        }
    }

    /** DimData.findNearestRift: the nearest other rift with a link in the cube of {@code range} (to range - 1). */
    private LinkData nearestRift(ServerLevel level, int dim, int range) {
        LinkData nearest = null;
        float distance = range + 1;
        BlockPos pos = this.worldPosition;
        BlockPos.MutableBlockPos at = new BlockPos.MutableBlockPos();
        for (int i = -range; i < range; i++) {
            for (int j = -range; j < range; j++) {
                for (int k = -range; k < range; k++) {
                    int d = Math.abs(i) + Math.abs(j) + Math.abs(k);
                    if (d < distance && d != 0 && level.getBlockState(at.set(pos.getX() + i, pos.getY() + j, pos.getZ() + k)).is(DimDoors.RIFT.get())) {
                        nearest = DDWorld.link(dim, at.getX(), at.getY(), at.getZ());
                        distance = d;
                    }
                }
            }
        }
        return nearest;
    }

    /** grow: the original read neither "Enable Rift Spread" nor "Rift Spread Modifier" here. */
    private void grow(ServerLevel level, int dim, int distance) {
        if (this.rand.nextInt(distance * 2) != 0) {
            return;
        }
        int count = 0;
        int x = 0;
        int y = 0;
        int z = 0;
        BlockPos pos = this.worldPosition;
        while (count < 100) {
            count++;
            x = pos.getX() + (1 - this.rand.nextInt(2) * 2 * this.rand.nextInt(6));
            y = pos.getY() + (1 - this.rand.nextInt(2) * 2 * this.rand.nextInt(4));
            z = pos.getZ() + (1 - this.rand.nextInt(2) * 2 * this.rand.nextInt(6));
            if (level.getBlockState(new BlockPos(x, y, z)).isAir()) {
                break;
            }
        }
        if (count < 100) {
            LinkData link = DDWorld.link(dim, pos.getX(), pos.getY(), pos.getZ());
            if (link != null && !this.hasGrownRifts && this.rand.nextInt(3) == 0) {
                link.children++;
                DDWorld.createLink(dim, link.destDim, x, y, z, link.destX, link.destY, link.destZ).children = link.children + 1;
                this.hasGrownRifts = true;
            }
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("xOffset", this.xOffset);
        output.putInt("yOffset", this.yOffset);
        output.putInt("zOffset", this.zOffset);
        output.putBoolean("grownRifts", this.hasGrownRifts);
        output.putInt("count", this.count);
        output.putInt("count2", this.count2);
        output.putBoolean("shouldClose", this.shouldClose);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.xOffset = input.getIntOr("xOffset", 0);
        this.yOffset = input.getIntOr("yOffset", 0);
        this.zOffset = input.getIntOr("zOffset", 0);
        this.hasGrownRifts = input.getBooleanOr("grownRifts", false);
        this.count = input.getIntOr("count", 200);
        this.count2 = input.getIntOr("count2", 0);
        this.shouldClose = input.getBooleanOr("shouldClose", false);
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
