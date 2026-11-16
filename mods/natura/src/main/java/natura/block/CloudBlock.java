package natura.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * CloudBlock (white, dark, ash and sulfur cloud). Entities sink slowly through clouds and take no fall damage;
 * only the topmost cloud of a stack has a thin floor (1/16) to stand on. A sulfur cloud explodes when a burning
 * arrow flies into it or when it is right-clicked with flint and steel. Clouds never drop from explosions.
 */
public class CloudBlock extends Block {
    private static final VoxelShape FLOOR = Block.box(0, 0, 0, 16, 1, 16);
    private final boolean sulfur;

    public CloudBlock(boolean sulfur, Properties properties) {
        super(properties);
        this.sulfur = sulfur;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return level.getBlockState(pos.below()).getBlock() instanceof CloudBlock ? Shapes.empty() : FLOOR;
    }

    @Override
    protected boolean skipRendering(BlockState state, BlockState adjacent, Direction direction) {
        return adjacent.getBlock() instanceof CloudBlock || super.skipRendering(state, adjacent, direction);
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effects, boolean intersects) {
        if (this.sulfur && entity instanceof AbstractArrow arrow && arrow.isOnFire() && !level.isClientSide()) {
            level.removeBlock(pos, false);
            this.explode(level, pos, arrow.getOwner());
            return;
        }
        Vec3 motion = entity.getDeltaMovement();
        if (motion.y < 0.0) {
            entity.setDeltaMovement(motion.x, motion.y * 0.005, motion.z);
        }
        entity.resetFallDistance();
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        if (this.sulfur && stack.is(Items.FLINT_AND_STEEL)) {
            if (!level.isClientSide()) {
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                this.explode(level, pos, player);
            }
            return InteractionResult.SUCCESS;
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hit);
    }

    private void explode(Level level, BlockPos pos, Entity source) {
        level.explode(source, pos.getX(), pos.getY(), pos.getZ(), 1.0F, false, Level.ExplosionInteraction.BLOCK);
    }

    @Override
    public boolean dropFromExplosion(Explosion explosion) {
        return false;
    }
}
