package extrabiomes.block;

import extrabiomes.gen.EbxlWorldGen;
import hexxit.worldgen152.OldLiveGen;
import hexxit.worldgen152.core.populate.OldGen;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealSource;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Random;

/**
 * BlockCustomSapling: grows the ExtrabiomesXL trees with the 1.5.2 generators. Fir and redwood grow into the huge
 * 2x2 trees when four saplings stand in a square; a lone redwood sapling does not grow.
 */
public class EbxlSaplingBlock extends VegetationBlock implements BonemealableBlock {
    public static final IntegerProperty STAGE = BlockStateProperties.STAGE;
    private static final VoxelShape SHAPE = Block.box(1.6, 0, 1.6, 14.4, 12.8, 14.4);
    /** BlockCustomSapling.BlockType metadata. */
    private final int type;

    public EbxlSaplingBlock(int type, Properties properties) {
        super(properties);
        this.type = type;
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

    /** TreeSoilRegistry: grass, dirt, farmland. */
    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(BlockTags.GRASS_BLOCKS) || state.is(BlockTags.DIRT) || state.is(Blocks.FARMLAND);
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (level.getMaxLocalRawBrightness(pos.above()) >= 9 && random.nextInt(7) == 0) {
            this.advanceTree(level, pos, state, random);
        }
    }

    /** First a mark (the metadata bit 8 of the original), then the tree. */
    public void advanceTree(ServerLevel level, BlockPos pos, BlockState state, RandomSource random) {
        if (state.getValue(STAGE) == 0) {
            level.setBlock(pos, state.cycle(STAGE), Block.UPDATE_NONE);
        } else {
            this.growTree(level, pos, random);
        }
    }

    private boolean isSame(Level level, BlockPos pos) {
        return level.getBlockState(pos).is(this);
    }

    private void growTree(ServerLevel level, BlockPos pos, RandomSource source) {
        Random rand = new Random(source.nextLong());
        int dx = 0;
        int dz = 0;
        boolean huge = false;
        if (this.type == extrabiomes.gen.EbxlIds.SAPLING_FIR || this.type == extrabiomes.gen.EbxlIds.SAPLING_REDWOOD) {
            search:
            for (dx = 0; dx >= -1; dx--) {
                for (dz = 0; dz >= -1; dz--) {
                    BlockPos corner = pos.offset(dx, 0, dz);
                    if (this.isSame(level, corner) && this.isSame(level, corner.east()) && this.isSame(level, corner.south())
                            && this.isSame(level, corner.south().east())) {
                        huge = true;
                        break search;
                    }
                }
            }
            if (!huge) {
                dx = 0;
                dz = 0;
            }
        }
        OldGen tree = EbxlWorldGen.saplingTree(this.type, huge, rand);
        if (tree == null) {
            return;
        }
        BlockPos corner = pos.offset(dx, 0, dz);
        BlockPos[] spots = huge ? new BlockPos[] {corner, corner.east(), corner.south(), corner.south().east()} : new BlockPos[] {pos};
        BlockState[] saplings = new BlockState[spots.length];
        for (int i = 0; i < spots.length; i++) {
            saplings[i] = level.getBlockState(spots[i]);
            level.setBlock(spots[i], Blocks.AIR.defaultBlockState(), Block.UPDATE_NONE);
        }
        int offset = huge ? 1 : 0;
        if (!OldLiveGen.generate(level, tree, rand, corner.getX() + offset, pos.getY(), corner.getZ() + offset, 14, 66)) {
            for (int i = 0; i < spots.length; i++) {
                level.setBlock(spots[i], saplings[i], Block.UPDATE_ALL);
            }
        }
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state, BonemealSource source) {
        return true;
    }

    /** SaplingBonemealEventHandler: bone meal always tries to grow the tree at once. */
    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state, BonemealSource source) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state, BonemealSource source) {
        this.growTree(level, pos, random);
    }
}
