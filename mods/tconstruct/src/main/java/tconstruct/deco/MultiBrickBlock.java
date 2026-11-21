package tconstruct.deco;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * MultiBrick / MultiBrickFancy: decorative bricks with the hardness and blast resistance of what they were chiseled from
 * (given in the properties). The slime bricks (10, 11) are 10 pixels high and bounce whatever touches them back up (motion
 * y times -1.2, no fall damage); the redstone bricks (8) give a weak signal of 4.
 */
public class MultiBrickBlock extends Block {
    private static final VoxelShape SLIME = Block.box(0, 0, 0, 16, 10, 16);
    private final boolean slime;
    private final boolean redstone;

    public MultiBrickBlock(boolean slime, boolean redstone, Properties properties) {
        super(properties);
        this.slime = slime;
        this.redstone = redstone;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return this.slime ? SLIME : super.getCollisionShape(state, level, pos, context);
    }

    /** onEntityCollidedWithBlock of 1.5.2 ran for every entity in the block's cell, also one standing on the lower box. */
    @Override
    protected VoxelShape getEntityInsideCollisionShape(BlockState state, BlockGetter level, BlockPos pos, Entity entity) {
        return this.slime ? Shapes.block() : super.getEntityInsideCollisionShape(state, level, pos, entity);
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effects, boolean intersects) {
        if (this.slime) {
            Vec3 motion = entity.getDeltaMovement();
            if (motion.y < 0.0) {
                entity.setDeltaMovement(motion.x, motion.y * -1.2F, motion.z);
            }
            entity.resetFallDistance();
        }
    }

    @Override
    protected boolean isSignalSource(BlockState state) {
        return this.redstone;
    }

    @Override
    protected int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return this.redstone ? 4 : 0;
    }
}
