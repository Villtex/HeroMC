package projectzulu.block.gear;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;
import projectzulu.ZuluGear;

import java.util.List;

/**
 * BlockMobHeads: one block for all 18 mob heads; the kind and the rotation on the floor live in the block entity,
 * the side it was placed on is {@link #FACING} (metadata 1 = floor is UP, 2-5 = the four walls, the clicked face).
 * Drops the head item of its kind (also when blown up); a block entity renderer draws it.
 */
public class MobHeadBlock extends BaseEntityBlock {
    public static final EnumProperty<Direction> FACING = EnumProperty.create("facing", Direction.class, d -> d != Direction.DOWN);
    private static final VoxelShape FLOOR = Block.box(4, 0, 4, 12, 8, 12);
    private static final VoxelShape NORTH = Block.box(4, 4, 8, 12, 12, 16);
    private static final VoxelShape SOUTH = Block.box(4, 4, 0, 12, 12, 8);
    private static final VoxelShape WEST = Block.box(8, 4, 4, 16, 12, 12);
    private static final VoxelShape EAST = Block.box(0, 4, 4, 8, 12, 12);
    private static final Direction[] BY_META = {Direction.UP, Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST};

    public MobHeadBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.UP));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    /** The block state of 1.5.2 metadata (bits 0-2 the side, bit 8 the creative-break flag). */
    public BlockState byMeta(int meta) {
        int side = meta & 7;
        return this.defaultBlockState().setValue(FACING, side < BY_META.length ? BY_META[side] : Direction.UP);
    }

    /** The side index of 1.5.2 for the renderer's offsets (1 floor, 2-5 walls). */
    public static int side(Direction facing) {
        return switch (facing) {
            case NORTH -> 2;
            case SOUTH -> 3;
            case WEST -> 4;
            case EAST -> 5;
            default -> 1;
        };
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MobHeadBlockEntity(pos, state);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case NORTH -> NORTH;
            case SOUTH -> SOUTH;
            case WEST -> WEST;
            case EAST -> EAST;
            default -> FLOOR;
        };
    }

    private static ItemStack item(@Nullable BlockEntity entity) {
        MobHead head = entity instanceof MobHeadBlockEntity mobHead ? mobHead.getHead() : MobHead.RED_FINCH;
        return new ItemStack(ZuluGear.MOB_HEADS.get(head).get());
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        return List.of(item(params.getOptionalParameter(LootContextParams.BLOCK_ENTITY)));
    }

    @Override
    protected ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData) {
        return item(level.getBlockEntity(pos));
    }
}
