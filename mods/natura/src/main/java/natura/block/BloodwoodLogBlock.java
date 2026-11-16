package natura.block;

import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;

import java.util.Locale;

/**
 * LogTwoxTwo: one quarter of the 2x2 bloodwood trunk. 1.5.2 metadata 0-3 were the quarters with the bark on the
 * north-west, north-east, south-west and south-east (4-7 and 8-11 the same lying along x and z); here they are
 * {@code bark} and {@code axis}. Metadata 15 (bark on all sides) is the block {@code bloodwood_bark}. Placed by hand,
 * the quarter follows the part of the face that was clicked, as LogTwoxTwo.onBlockPlaced did.
 */
public class BloodwoodLogBlock extends RotatedPillarBlock {
    public enum Bark implements StringRepresentable {
        NW, NE, SW, SE;

        @Override
        public String getSerializedName() {
            return this.name().toLowerCase(Locale.ROOT);
        }
    }

    public static final EnumProperty<Bark> BARK = EnumProperty.create("bark", Bark.class);

    public BloodwoodLogBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(BARK, Bark.NW));
    }

    /** Quarter of a 1.5.2 metadata value (meta &amp; 3). */
    public static Bark bark(int meta) {
        return Bark.values()[meta & 3];
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(BARK);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        if (state == null) {
            return null;
        }
        double x = context.getClickLocation().x - context.getClickedPos().getX();
        double y = context.getClickLocation().y - context.getClickedPos().getY();
        double z = context.getClickLocation().z - context.getClickedPos().getZ();
        Direction face = context.getClickedFace();
        int add = switch (face.getAxis()) {
            case Y -> (x > 0.5 ? 1 : 0) + (z > 0.5 ? 2 : 0);
            case Z -> (x > 0.5 ? 1 : 0) + (y < 0.5 ? 2 : 0);
            case X -> (z < 0.5 ? 1 : 0) + (y < 0.5 ? 2 : 0);
        };
        return state.setValue(BARK, Bark.values()[add]);
    }
}
