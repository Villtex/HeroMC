package dimdoors.block;

import dimdoors.DimDoors;
import dimdoors.world.DDData.DimData;
import dimdoors.world.DDData.LinkData;
import dimdoors.world.DDWorld;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Random;

/**
 * dimDoor, ExitDoor (warp door), ChaosDoor (unstable door) and TransientDoor of Dimensional Doors 1.4.1: doors whose
 * upper half holds a rift. Walking through the open door in the direction it faces sends you along the link. All of
 * them open by hand, iron or not; a rift blade turns them.
 */
public class DimDoorBlock extends DoorBlock implements EntityBlock {
    public enum Kind {
        /** dimDoor: a new pocket behind it, or the rift it was placed on. */
        DIMENSIONAL,
        /** ExitDoor: in a pocket a way out to where the chain of pockets began. */
        WARP,
        /** ChaosDoor: to the place of a random link. */
        UNSTABLE,
        /** TransientDoor: opened by a rift blade on a rift; walking in closes it again. */
        TRANSIENT
    }

    private final Kind kind;

    public DimDoorBlock(Kind kind, BlockSetType type, Properties properties) {
        super(type, properties);
        this.kind = kind;
    }

    public Kind kind() {
        return this.kind;
    }

    /** generateDoor makes a door on the other side for dimensional, warp and transient doors. */
    public boolean makesDoorOnOtherSide() {
        return this.kind != Kind.UNSTABLE;
    }

    /** The state of 1.5.2 metadata: lower half facing and open, upper half (bit 8) the hinge. */
    public static BlockState state(Block block, int meta, boolean powered) {
        BlockState state = block.defaultBlockState();
        if ((meta & 8) != 0) {
            return state.setValue(HALF, DoubleBlockHalf.UPPER).setValue(HINGE, (meta & 1) != 0 ? DoorHingeSide.RIGHT : DoorHingeSide.LEFT);
        }
        return state.setValue(HALF, DoubleBlockHalf.LOWER).setValue(FACING, DDWorld.direction(meta)).setValue(OPEN, (meta & 4) != 0)
                .setValue(POWERED, powered);
    }

    /** The 1.5.2 metadata of the lower half: facing and open. */
    public static int lowerMeta(BlockState lower) {
        return DDWorld.meta(lower.getValue(FACING)) | (lower.getValue(OPEN) ? 4 : 0);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new DimDoorBlockEntity(pos, state);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return this.kind == Kind.TRANSIENT ? RenderShape.INVISIBLE : super.getRenderShape(state);
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return this.kind == Kind.TRANSIENT ? Shapes.empty() : super.getCollisionShape(state, level, pos, context);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return this.kind == Kind.TRANSIENT ? Shapes.empty() : super.getShape(state, level, pos, context);
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        // setBlockDirectly in the dungeons and the doors of generateDoor stand where they are put
        return state.getValue(HALF) == DoubleBlockHalf.UPPER ? level.getBlockState(pos.below()).is(this) : true;
    }

    // ---- onBlockAdded ----

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (level.isClientSide() || oldState.is(this) || state.getValue(HALF) != DoubleBlockHalf.UPPER) {
            return;
        }
        BlockState lower = level.getBlockState(pos.below());
        if (!lower.is(this)) {
            return;
        }
        int dim = DDWorld.dimId(level, pos);
        int orientation = lowerMeta(lower);
        switch (this.kind) {
            case DIMENSIONAL -> {
                if (DDWorld.link(dim, pos.getX(), pos.getY(), pos.getZ()) == null) {
                    DDWorld.createPocket(new LinkData(dim, 0, pos.getX(), pos.getY(), pos.getZ(), pos.getX(), pos.getY(), pos.getZ(), true,
                            orientation), true, false);
                }
                LinkData link = DDWorld.link(dim, pos.getX(), pos.getY(), pos.getZ());
                if (link != null) {
                    link.orientation = orientation;
                    link.hasGennedDoor = false;
                }
            }
            case WARP, TRANSIENT -> {
                DimData data = DDWorld.data().dim(dim);
                LinkData link = DDWorld.link(dim, pos.getX(), pos.getY(), pos.getZ());
                if (data != null && link == null) {
                    if (data.isPocket) {
                        ServerLevel exit = DDWorld.level(data.exitDim);
                        if (exit != null) {
                            int x = DDWorld.virtualX(dim, pos.getX());
                            int z = DDWorld.virtualZ(dim, pos.getZ());
                            int y = DDWorld.firstUncovered(exit, x, pos.getY(), z);
                            DDWorld.createLink(dim, data.exitDim, pos.getX(), pos.getY(), pos.getZ(), x, y, z, orientation);
                            DDWorld.createLink(data.exitDim, dim, x, y, z, pos.getX(), pos.getY(), pos.getZ(), DDWorld.flipDoorMetadata(orientation));
                        }
                    }
                } else if (link != null) {
                    link.orientation = orientation;
                    link.hasGennedDoor = false;
                }
            }
            case UNSTABLE -> {
                if (DDWorld.link(dim, pos.getX(), pos.getY(), pos.getZ()) == null) {
                    DDWorld.createLink(new LinkData(dim, DimDoors.LIMBO_ID, pos.getX(), pos.getY(), pos.getZ(), pos.getX(), pos.getY() + 500,
                            pos.getZ(), false, orientation), true);
                }
                LinkData link = DDWorld.link(dim, pos.getX(), pos.getY(), pos.getZ());
                if (link != null) {
                    link.orientation = orientation;
                }
            }
        }
        DDWorld.data().setDirty();
        DDWorld.refreshDoor((ServerLevel) level, pos);
    }

    // ---- onEntityCollidedWithBlock ----

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effectApplier, boolean isPrecise) {
        if (level.isClientSide() || !entity.getBoundingBox().intersects(new AABB(pos))) {
            return;
        }
        int facing = DDWorld.facing(entity);
        int dim = DDWorld.dimId(level, pos);
        switch (this.kind) {
            case TRANSIENT -> {
                int num = state.getValue(HALF) == DoubleBlockHalf.UPPER ? 8 | (state.getValue(HINGE) == DoorHingeSide.RIGHT ? 1 : 0)
                        : DDWorld.meta(state.getValue(FACING));
                if (num == facing || !(entity instanceof Player)) {
                    LinkData link = DDWorld.link(dim, pos.getX(), pos.getY(), pos.getZ());
                    if (link != null) {
                        if (DDWorld.data().dim(link.destDim) != null) {
                            level.setBlock(pos.below(), net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                            level.setBlock(pos, DimDoors.RIFT.get().defaultBlockState(), Block.UPDATE_ALL);
                            DDWorld.teleport(level, link, entity);
                        }
                    } else {
                        link = DDWorld.link(dim, pos.getX(), pos.getY() + 1, pos.getZ());
                        if (link != null && DDWorld.data().dim(link.destDim) != null) {
                            level.setBlock(pos, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                            level.setBlock(pos.above(), DimDoors.RIFT.get().defaultBlockState(), Block.UPDATE_ALL);
                            DDWorld.teleport(level, link, entity);
                        }
                    }
                }
            }
            case UNSTABLE -> {
                BlockState lower = level.getBlockState(pos.below());
                if (!lower.is(this)) {
                    return;
                }
                int num = lowerMeta(lower);
                if (num >= 4 && num <= 7 && num - 4 == facing) {
                    this.setOpen(null, level, lower, pos.below(), false);
                    for (int i = 0; i < 100; i++) {
                        LinkData link = DDWorld.randomLink(false);
                        if (link != null && !link.isLocPocket && link.orientation != -10 && link.destDim != DimDoors.LIMBO_ID) {
                            DDWorld.teleport(level, new LinkData(link.destDim, link.locDim, link.destX, link.destY, link.destZ, link.locX, link.locY,
                                    link.locZ, link.isLocPocket, 0), entity);
                            ServerLevel there = DDWorld.level(link.locDim);
                            BlockPos at = new BlockPos(link.locX, link.locY, link.locZ);
                            if (there != null && there.getBlockState(at).isAir()) {
                                there.setBlock(at, DimDoors.RIFT.get().defaultBlockState(), Block.UPDATE_ALL);
                            }
                            break;
                        }
                    }
                }
            }
            default -> {
                int num = 0;
                LinkData link = null;
                BlockPos lowerPos = null;
                if (level.getBlockState(pos.below()).is(this)) {
                    lowerPos = pos.below();
                    num = lowerMeta(level.getBlockState(lowerPos));
                    link = DDWorld.link(dim, pos.getX(), pos.getY(), pos.getZ());
                }
                if (level.getBlockState(pos.above()).is(this)) {
                    lowerPos = pos;
                    num = lowerMeta(state);
                    link = DDWorld.link(dim, pos.getX(), pos.getY() + 1, pos.getZ());
                }
                if (lowerPos == null) {
                    return;
                }
                if (!(entity instanceof Player) && num > 3 || num >= 4 && num <= 7 && num - 4 == facing) {
                    this.setOpen(null, level, level.getBlockState(lowerPos), lowerPos, false);
                    DDWorld.teleport(level, link, entity);
                }
            }
        }
    }

    // ---- onBlockActivated ----

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
                                          BlockHitResult hitResult) {
        if (!stack.is(DimDoors.RIFT_BLADE.get())) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        // the rift blade turns the door to where the player looks, or the other way when it looks that way already
        if (!level.isClientSide()) {
            int dim = DDWorld.dimId(level, pos);
            BlockPos lowerPos = state.getValue(HALF) == DoubleBlockHalf.UPPER ? pos.below() : pos;
            BlockState lower = level.getBlockState(lowerPos);
            if (lower.is(this) && level.getBlockState(lowerPos.above()).is(this)) {
                int facing = DDWorld.facing(player);
                if (lowerMeta(lower) == facing) {
                    facing = DDWorld.flipDoorMetadata(facing);
                }
                level.setBlock(lowerPos, state(this, facing, lower.getValue(POWERED)), Block.UPDATE_CLIENTS);
                LinkData link = DDWorld.link(dim, lowerPos.getX(), lowerPos.getY() + 1, lowerPos.getZ());
                if (link != null) {
                    link.orientation = facing;
                    DDWorld.data().setDirty();
                }
            }
            level.levelEvent(null, 1001, pos, 0);
            stack.hurtAndBreak(5, player, hand);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        BlockPos lowerPos = state.getValue(HALF) == DoubleBlockHalf.UPPER ? pos.below() : pos;
        BlockState lower = level.getBlockState(lowerPos);
        if (!lower.is(this)) {
            return InteractionResult.PASS;
        }
        BlockState toggled = lower.cycle(OPEN);
        level.setBlock(lowerPos, toggled, Block.UPDATE_CLIENTS | Block.UPDATE_IMMEDIATE);
        level.playSound(player, pos, toggled.getValue(OPEN) ? SoundEvents.WOODEN_DOOR_OPEN : SoundEvents.WOODEN_DOOR_CLOSE, SoundSource.BLOCKS, 1.0F,
                level.getRandom().nextFloat() * 0.1F + 0.9F);
        return InteractionResult.SUCCESS;
    }

    // ---- onBlockDestroyedByPlayer: a linked door leaves its rift behind ----

    @Override
    public void destroy(LevelAccessor level, BlockPos pos, BlockState state) {
        super.destroy(level, pos, state);
        if (!(level instanceof ServerLevel server) || this.kind == Kind.TRANSIENT) {
            return;
        }
        int dim = DDWorld.dimId(server, pos);
        for (BlockPos at : new BlockPos[] {pos, pos.below(), pos.above()}) {
            if (DDWorld.link(dim, at.getX(), at.getY(), at.getZ()) != null) {
                server.setBlock(at, DimDoors.RIFT.get().defaultBlockState(), Block.UPDATE_ALL);
            }
        }
    }

    /** ItemDoor.placeDoorBlock: both halves, the hinge towards the side with more solid blocks or another door. */
    public static void placeDoorBlock(Level level, BlockPos pos, int facing, Block door) {
        int dx = 0;
        int dz = 0;
        switch (facing) {
            case 0 -> dz = 1;
            case 1 -> dx = -1;
            case 2 -> dz = -1;
            default -> dx = 1;
        }
        BlockPos left = pos.offset(-dx, 0, -dz);
        BlockPos right = pos.offset(dx, 0, dz);
        int leftSolid = (level.getBlockState(left).isRedstoneConductor(level, left) ? 1 : 0)
                + (level.getBlockState(left.above()).isRedstoneConductor(level, left.above()) ? 1 : 0);
        int rightSolid = (level.getBlockState(right).isRedstoneConductor(level, right) ? 1 : 0)
                + (level.getBlockState(right.above()).isRedstoneConductor(level, right.above()) ? 1 : 0);
        boolean doorLeft = level.getBlockState(left).is(door) || level.getBlockState(left.above()).is(door);
        boolean doorRight = level.getBlockState(right).is(door) || level.getBlockState(right.above()).is(door);
        boolean mirrored = doorLeft && !doorRight || rightSolid > leftSolid;
        level.setBlock(pos, state(door, facing, false), Block.UPDATE_CLIENTS);
        level.setBlock(pos.above(), state(door, 8 | (mirrored ? 1 : 0), false), Block.UPDATE_CLIENTS);
        level.updateNeighborsAt(pos, door);
        level.updateNeighborsAt(pos.above(), door);
    }

    static Random random() {
        return DDWorld.RAND;
    }
}
