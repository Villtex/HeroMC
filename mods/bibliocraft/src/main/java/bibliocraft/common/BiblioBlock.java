package bibliocraft.common;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

/**
 * Base of the BiblioCraft blocks: a BlockContainer that is no full cube (renderAsNormalBlock/isOpaqueCube false) and is
 * drawn by its block entity renderer. The block model only carries the breaking particles.
 *
 * <p>Most blocks kept their rotation in the tile entity ("angle" 0..3, set in onBlockPlacedBy); here it is the block
 * state property {@link #ANGLE}, with the same numbers. Blocks that use it pass {@code true} for {@code hasAngle}.
 * The contents of a block entity that is a {@link net.minecraft.world.Container} drop when the block is broken
 * (BlockEntity.preRemoveSideEffects, the same scatter as dropItems of the original).
 */
public abstract class BiblioBlock extends BaseEntityBlock {
    /** TileEntity*.getAngle(): 0 = player looked east (+x), 1 south, 2 west, 3 north when placing. */
    public static final IntegerProperty ANGLE = IntegerProperty.create("angle", 0, 3);
    private final boolean hasAngle;

    protected BiblioBlock(boolean hasAngle, Properties properties) {
        super(properties);
        this.hasAngle = hasAngle;
        if (hasAngle) {
            this.registerDefaultState(this.stateDefinition.any().setValue(ANGLE, 0));
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        // called from the Block constructor, before hasAngle is set: subclasses without the angle override this
        builder.add(ANGLE);
    }

    /** onBlockPlacedBy: {@code (floor(yaw * 4 / 360 + 0.5) & 3) + 1) % 4}. */
    public static int angle(LivingEntity placer) {
        int angle = Mth.floor(placer.getYRot() * 4.0F / 360.0F + 0.5) & 3;
        return (angle + 1) % 4;
    }

    public static int angle(BlockState state) {
        return state.hasProperty(ANGLE) ? state.getValue(ANGLE) : 0;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (this.hasAngle && placer != null) {
            level.setBlock(pos, state.setValue(ANGLE, angle(placer)), 2);
        }
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    /** A box in pixels rotated for angle 0..3 as the original setBlockBounds switches did: give the box for angle 0. */
    public static VoxelShape[] rotations(double x1, double y1, double z1, double x2, double y2, double z2) {
        VoxelShape[] shapes = new VoxelShape[4];
        for (int a = 0; a < 4; a++) {
            double ax1 = x1, az1 = z1, ax2 = x2, az2 = z2;
            for (int r = 0; r < a; r++) {
                // angle a+1 is angle a turned 90 degrees: (x, z) -> (16 - z, x) applied to both corners
                double nx1 = 16 - az2, nz1 = ax1, nx2 = 16 - az1, nz2 = ax2;
                ax1 = nx1;
                az1 = nz1;
                ax2 = nx2;
                az2 = nz2;
            }
            shapes[a] = Block.box(ax1, y1, az1, ax2, y2, az2);
        }
        return shapes;
    }
}
