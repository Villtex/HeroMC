package sct.hexxitgear.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * BlockHexbiscus, a BlockFlower that drops hexical essence (loot table). Stands on grass, dirt and farmland;
 * like a 1.5.2 flower it pops off on a random tick when it has neither light 8 nor open sky.
 */
public class HexbiscusBlock extends VegetationBlock {
    /** BlockFlower bounds: 0.2 around the centre, 0.6 high. */
    private static final VoxelShape SHAPE = Block.box(4.8, 0.0, 4.8, 11.2, 9.6, 11.2);

    public HexbiscusBlock(Properties properties) {
        super(properties);
    }

    /** BlockFlower.canThisPlantGrowOnThisBlockID: grass, dirt, tilled field. */
    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.DIRT) || state.is(Blocks.FARMLAND);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    /** BlockFlower.updateTick -> checkFlowerChange: drops when canBlockStay fails (light or soil). */
    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!hasLight(level, pos) || !this.canSurvive(state, level, pos)) {
            level.destroyBlock(pos, true);
        }
    }

    /** getFullBlockLightValue >= 8 || canBlockSeeTheSky. */
    private static boolean hasLight(LevelReader level, BlockPos pos) {
        return level.getRawBrightness(pos, 0) >= 8 || level.canSeeSky(pos);
    }
}
