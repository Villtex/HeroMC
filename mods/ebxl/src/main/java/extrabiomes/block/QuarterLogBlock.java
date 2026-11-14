package extrabiomes.block;

import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;

/**
 * BlockQuarterLog: one quarter of a 2x2 trunk (redwood, huge fir, legend oak). 1.5.2 used four block ids for the
 * corner with the bark; here it is the property {@code bark}. Placed by hand, the bark faces the quarter of the
 * block face that was clicked.
 */
public class QuarterLogBlock extends RotatedPillarBlock {
    public enum Bark implements StringRepresentable {
        NW, NE, SW, SE;

        @Override
        public String getSerializedName() {
            return this.name().toLowerCase(java.util.Locale.ROOT);
        }
    }

    public static final EnumProperty<Bark> BARK = EnumProperty.create("bark", Bark.class);

    public QuarterLogBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(BARK, Bark.NW));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(BARK);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        double x = context.getClickLocation().x - context.getClickedPos().getX();
        double z = context.getClickLocation().z - context.getClickedPos().getZ();
        boolean north = z < 0.5;
        boolean west = x < 0.5;
        if (context.getClickedFace().getAxis() != Direction.Axis.Y) {
            Direction facing = context.getHorizontalDirection();
            north = facing == Direction.SOUTH || facing.getAxis() == Direction.Axis.X && z < 0.5;
            west = facing == Direction.EAST || facing.getAxis() == Direction.Axis.Z && x < 0.5;
        }
        Bark bark = north ? (west ? Bark.NW : Bark.NE) : (west ? Bark.SW : Bark.SE);
        return state == null ? null : state.setValue(BARK, bark);
    }
}
