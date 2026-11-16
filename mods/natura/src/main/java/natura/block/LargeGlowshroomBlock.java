package natura.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HugeMushroomBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * LargeGlowshroom (blue, green, purple): cap and stem of the large glowshrooms. 1.5.2 kept both in one block with
 * the metadata of the huge mushrooms (0 inside, 1-9 cap, 10 stem, 14 all cap, 15 all stem); here the cap and the
 * stem are two blocks with the six faces of {@link HugeMushroomBlock} ({@code true} = skin or stem texture). Only the
 * top block of a column has a thin surface (1/16) to land on; whoever goes through is slowed and gets nausea.
 * Blue and purple glowshrooms are see-through to each other.
 */
public class LargeGlowshroomBlock extends HugeMushroomBlock {
    private static final VoxelShape TOP = Block.box(0, 15, 0, 16, 16, 16);
    private final boolean cullsSame;

    public LargeGlowshroomBlock(boolean cullsSame, Properties properties) {
        super(properties);
        this.cullsSame = cullsSame;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return level.getBlockState(pos.above()).getBlock() instanceof LargeGlowshroomBlock ? Shapes.empty() : TOP;
    }

    @Override
    protected boolean skipRendering(BlockState state, BlockState adjacent, Direction direction) {
        return adjacent.getBlock() instanceof LargeGlowshroomBlock other && other.cullsSame || super.skipRendering(state, adjacent, direction);
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effects, boolean intersects) {
        Vec3 motion = entity.getDeltaMovement();
        if (motion.y < 0.0) {
            motion = new Vec3(motion.x, motion.y * 0.25, motion.z);
        }
        entity.fallDistance = Math.max(0.0, entity.fallDistance - 0.25);
        if (motion.y == 0.0) {
            motion = new Vec3(motion.x * 0.25, motion.y, motion.z * 0.25);
        }
        entity.setDeltaMovement(motion);
        if (!level.isClientSide() && entity instanceof LivingEntity living) {
            living.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 100, 0));
        }
    }
}
