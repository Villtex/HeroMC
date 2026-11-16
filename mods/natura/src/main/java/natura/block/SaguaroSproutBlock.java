package natura.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Random;

/**
 * SaguaroBlock metadata 1 and 2: the small sprout planted with a saguaro fruit ({@code age} 0 and 1). Each random
 * tick 1/200 it ripens, a ripe sprout grows into a cactus through the saguaro generator ({@link NaturaTree#SAGUARO}).
 * Planted on sand (or on a saguaro).
 */
public class SaguaroSproutBlock extends Block {
    public static final IntegerProperty AGE = BlockStateProperties.AGE_1;
    private static final VoxelShape SHAPE = Block.box(5.2, 0, 5.2, 10.8, 8, 10.8);

    public SaguaroSproutBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(AGE, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    /** SeedFood.onItemUse: the soil must carry a desert plant (sand); a sprout on a saguaro also stays. */
    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockState below = level.getBlockState(pos.below());
        return below.is(BlockTags.SAND) || SaguaroBlock.canStandOn(below) && !below.isAir();
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction direction,
            BlockPos neighborPos, BlockState neighborState, RandomSource random) {
        if (direction == Direction.DOWN && !SaguaroBlock.canStandOn(neighborState)) {
            ticks.scheduleTick(pos, this, 1);
        }
        return state;
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!SaguaroBlock.canStandOn(level.getBlockState(pos.below()))) {
            level.destroyBlock(pos, true);
        }
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (random.nextInt(200) != 0) {
            return;
        }
        if (state.getValue(AGE) == 0) {
            level.setBlock(pos, state.setValue(AGE, 1), Block.UPDATE_ALL);
        } else {
            NaturaTree.SAGUARO.grow(level, pos, new Random(random.nextLong()));
        }
    }
}
