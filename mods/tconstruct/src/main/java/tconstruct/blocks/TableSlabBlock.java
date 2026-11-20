package tconstruct.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

/**
 * {@code CraftingSlab}: a half-high table (crafting station, tool station, part builder, stencil table, pattern chest
 * or tool forge). It sits at the top when placed from below or into the upper half of the clicked block; as in 1.5.2
 * the hit is measured from the clicked block, so placing it onto the top face of a block puts it at the top as well.
 */
public class TableSlabBlock extends TableBlock {
    public static final BooleanProperty TOP = BooleanProperty.create("top");
    private static final VoxelShape BOTTOM = Block.box(0, 0, 0, 16, 8, 16);
    private static final VoxelShape UPPER = Block.box(0, 8, 0, 16, 16, 16);

    public TableSlabBlock(Kind kind, Properties properties) {
        super(kind, true, properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(TOP, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(TOP);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction face = context.getClickedFace();
        BlockPos clicked = context.getClickedPos().relative(face.getOpposite());
        double hitY = context.getClickLocation().y - clicked.getY();
        return this.defaultBlockState().setValue(TOP, face == Direction.DOWN || hitY >= 0.5);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(TOP) ? UPPER : BOTTOM;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return this.getShape(state, level, pos, context);
    }

    @Override
    protected boolean useShapeForLightOcclusion(BlockState state) {
        return true;
    }
}
