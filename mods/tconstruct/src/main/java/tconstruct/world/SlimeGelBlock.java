package tconstruct.world;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * SlimeGel (congealed slime): entities sink in (collision box 0.625 high), a falling entity bounces back with 1.2 times
 * its speed, and nothing takes fall damage on it.
 */
public class SlimeGelBlock extends Block {
    private static final VoxelShape COLLISION = Block.box(0, 0, 0, 16, 10, 16);

    public SlimeGelBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return COLLISION;
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effects, boolean intersects) {
        Vec3 motion = entity.getDeltaMovement();
        if (motion.y < 0.0) {
            if (motion.y < -0.08F && !level.isClientSide()) {
                SoundType sound = state.getSoundType();
                level.playSound(null, pos, sound.getStepSound(), SoundSource.BLOCKS, sound.getVolume() / 2.0F, sound.getPitch() * 0.65F);
            }
            entity.setDeltaMovement(motion.x, motion.y * -1.2F, motion.z);
            entity.needsSync = true;
        }
        entity.resetFallDistance();
    }

    @Override
    public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, double fallDistance) {
        entity.resetFallDistance();
    }
}
