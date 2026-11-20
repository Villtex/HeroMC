package twilightforest.block;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import twilightforest.TFBlocks;

/** BlockTFFireflyJar: a glass jar 3/16..13/16 wide with fireflies, light 8; two little fireflies per display tick. */
public class FireflyJarBlock extends Block {
    private static final VoxelShape SHAPE = Block.box(3.0, 0.0, 3.0, 13.0, 16.0, 13.0);

    public FireflyJarBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource rand) {
        for (int i = 0; i < 2; i++) {
            double dx = pos.getX() + ((rand.nextFloat() - rand.nextFloat()) * 0.3F + 0.5F);
            double dy = pos.getY() - 0.1F + (rand.nextFloat() - rand.nextFloat()) * 0.4F;
            double dz = pos.getZ() + ((rand.nextFloat() - rand.nextFloat()) * 0.3F + 0.5F);
            level.addParticle(TFBlocks.FIREFLY_PARTICLE.get(), dx, dy, dz, 0.0, 0.0, 0.0);
        }
    }
}
