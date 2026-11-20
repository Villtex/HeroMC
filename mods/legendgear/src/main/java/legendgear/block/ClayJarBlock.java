package legendgear.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

/**
 * BlockJar: a clay jar (hardness 0, drops nothing). An item that falls into it is kept; breaking the jar, or an arrow
 * flying into it, smashes it and lets the item out.
 */
public class ClayJarBlock extends BaseEntityBlock {
    private static final VoxelShape SHAPE = Block.box(1, 0, 1, 15, 16, 15);

    public ClayJarBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new JarBlockEntity(pos, state);
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effects, boolean intersects) {
        if (!(level instanceof ServerLevel server)) {
            return;
        }
        if (entity instanceof ItemEntity item && item.isAlive() && level.getBlockEntity(pos) instanceof JarBlockEntity jar && jar.contents.isEmpty()) {
            jar.setContents(item.getItem().copy());
            item.discard();
            level.playSound(null, item.getX(), item.getY(), item.getZ(), SoundEvents.CHICKEN_EGG, SoundSource.BLOCKS, 0.3F,
                    0.5F + (float) Math.random() * 0.2F);
        }
        if (entity instanceof AbstractArrow) {
            server.destroyBlock(pos, false);
        }
    }
}
