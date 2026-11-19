package bibliocraft.decor;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** BlockLantern "Golden Lantern": a candle of the block's colour in a gold lantern, with a flame (EntityCandleFX). */
public class LanternBlock extends LightBlock {
    /** setBlockBounds(0.3, 0, 0.3, 0.7, 0.7, 0.7) of the constructor, for every style (also when hanging). */
    private static final VoxelShape SHAPE = Block.box(4.8, 0.0, 4.8, 11.2, 11.2, 11.2);

    public LanternBlock(int color, Properties properties) {
        super(color, properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new LightBlockEntity(Decor.LANTERN_ENTITY.get(), pos, state);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    /** randomDisplayTick: a candle flame at (0.5, 0.3, 0.5), rising with 0.001. */
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        level.addParticle(Decor.CANDLE_FLAME.get(), pos.getX() + 0.5, pos.getY() + 0.3, pos.getZ() + 0.5, 0.0, 0.001, 0.0);
    }
}
