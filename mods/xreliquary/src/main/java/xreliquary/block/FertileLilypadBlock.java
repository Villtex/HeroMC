package xreliquary.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CactusBlock;
import net.minecraft.world.level.block.LilyPadBlock;
import net.minecraft.world.level.block.SugarCaneBlock;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * BlockFertileLilypad: a full-width lily pad (0.25 px high) that on every random tick gives each plant (IPlantable of
 * 1.5.2: vegetation, sugar cane, cactus) of the 9 x 6 x 9 around it (one below to four above) a random tick.
 */
public class FertileLilypadBlock extends LilyPadBlock {
    private static final VoxelShape SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 0.25, 16.0);

    public FertileLilypadBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        for (BlockPos p : BlockPos.betweenClosed(pos.offset(-4, -1, -4), pos.offset(4, 4, 4))) {
            BlockState s = level.getBlockState(p);
            Block b = s.getBlock();
            if (b != this && (b instanceof VegetationBlock || b instanceof SugarCaneBlock || b instanceof CactusBlock) && s.isRandomlyTicking()) {
                s.randomTick(level, p.immutable(), level.getRandom());
            }
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        level.addParticle(ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, 0.0F, 0.9F, 0.5F), pos.getX() + 0.5 + random.nextGaussian() / 8.0,
                pos.getY(), pos.getZ() + 0.5 + random.nextGaussian() / 8.0, 0.0, 0.9, 0.5);
    }
}
