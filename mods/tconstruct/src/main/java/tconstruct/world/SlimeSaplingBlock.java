package tconstruct.world;

import hexxit.worldgen152.OldLiveGen;
import hexxit.worldgen152.core.populate.OldGen;
import hexxit.worldgen152.core.populate.OldWorld;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import tconstruct.gen.SlimeIslandGen;

import java.util.Random;

/**
 * SlimeSapling: on random ticks with light 9 above, 1 in 10, it is first marked ({@code stage} = metadata bit 8), then
 * grows a slime tree (SlimeTreeGen with notify, where it stands). The calls of BlockSapling and bone meal did nothing
 * for it in survival (boneFertilize needs metadata % 8 != 0), so it takes no bone meal.
 */
public class SlimeSaplingBlock extends VegetationBlock {
    public static final IntegerProperty STAGE = BlockStateProperties.STAGE;
    private static final VoxelShape SHAPE = Block.box(1.6, 0, 1.6, 14.4, 12.8, 14.4);
    private static final OldGen TREE = new OldGen() {
        @Override
        public boolean generate(OldWorld world, Random random, int x, int y, int z) {
            return SlimeIslandGen.slimeTree(world, random, x, y, z, false);
        }
    };

    public SlimeSaplingBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(STAGE, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(STAGE);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(BlockTags.SUPPORTS_VEGETATION);
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return super.canSurvive(state, level, pos) && (level.getRawBrightness(pos, 0) >= 8 || level.canSeeSky(pos));
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (random.nextInt(10) == 0 && level.getMaxLocalRawBrightness(pos.above()) >= 9) {
            if (state.getValue(STAGE) == 0) {
                level.setBlock(pos, state.setValue(STAGE, 1), Block.UPDATE_INVISIBLE);
            } else {
                grow(level, pos, random);
            }
        }
    }

    /** growTree: the sapling goes, the tree grows; if it does not fit, the sapling comes back marked. */
    public static void grow(ServerLevel level, BlockPos pos, RandomSource random) {
        BlockState sapling = level.getBlockState(pos);
        level.setBlock(pos, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        if (!OldLiveGen.generate(level, TREE, new Random(random.nextLong()), pos.getX(), pos.getY(), pos.getZ(), 4, 1, 11)) {
            level.setBlock(pos, sapling.setValue(STAGE, 1), Block.UPDATE_ALL);
        }
    }
}
