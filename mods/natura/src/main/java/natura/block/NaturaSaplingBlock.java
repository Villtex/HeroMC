package natura.block;

import natura.NaturaBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealSource;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Random;

/**
 * NSaplingBlock (redwood, eucalyptus, hopseed, sakura, ghostwood, bloodwood, darkwood, fusewood) and
 * OverworldSapling (maple, silverbell, amaranth, tiger, willow). {@code stage} is the metadata bit 8 of 1.5.2: a
 * sapling is first marked, the next step grows the tree through {@link NaturaTree#grow}.
 */
public class NaturaSaplingBlock extends VegetationBlock implements BonemealableBlock {
    public static final IntegerProperty STAGE = BlockStateProperties.STAGE;
    private static final VoxelShape SHAPE = Block.box(1.6, 0, 1.6, 14.4, 12.8, 14.4);

    /** Where a sapling stands (canBlockStay of the original). */
    public enum Soil {
        /** Grass, dirt, farmland; light 8 or open sky. */
        PLAINS,
        /** Netherrack, soul sand, tainted soil. */
        NETHER,
        /** Bloodwood: hangs under netherrack, soul sand or tainted soil. */
        CEILING
    }

    /** How a sapling advances on random ticks. */
    public enum Growth {
        /** Redwood: 1/120 with light 9, then grows only with 40 redwood saplings in the 7x7 around it. */
        REDWOOD,
        /** Eucalyptus, hopseed, sakura: 1/7 (BlockSapling) and 1/10 with light 9. */
        FLORA,
        /** Ghostwood, bloodwood, darkwood, fusewood: 1/7 with light 9 (BlockSapling), and 1/10 at any light. */
        NETHER,
        /** OverworldSapling: 1/7 (BlockSapling) and 1/120 with light 9. */
        RARE
    }

    private final NaturaTree tree;
    private final Soil soil;
    private final Growth growth;

    public NaturaSaplingBlock(NaturaTree tree, Soil soil, Growth growth, Properties properties) {
        super(properties);
        this.tree = tree;
        this.soil = soil;
        this.growth = growth;
        this.registerDefaultState(this.stateDefinition.any().setValue(STAGE, 0));
    }

    public NaturaTree tree() {
        return this.tree;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(STAGE);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    private static boolean netherSoil(BlockState state) {
        return state.is(Blocks.NETHERRACK) || state.is(Blocks.SOUL_SAND) || state.is(NaturaBlocks.TAINTED_SOIL.get());
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return switch (this.soil) {
            case PLAINS -> state.is(BlockTags.SUPPORTS_VEGETATION);
            case NETHER -> netherSoil(state);
            case CEILING -> false;
        };
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return switch (this.soil) {
            case PLAINS -> this.mayPlaceOn(level.getBlockState(pos.below()), level, pos.below())
                    && (level.getRawBrightness(pos, 0) >= 8 || level.canSeeSky(pos));
            case NETHER -> netherSoil(level.getBlockState(pos.below()));
            case CEILING -> netherSoil(level.getBlockState(pos.above()));
        };
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        boolean bright = level.getMaxLocalRawBrightness(pos.above()) >= 9;
        // BlockSapling.updateTick: light 9 and 1/7 -> markOrGrowMarked (NSaplingBlock ignores it for redwood)
        if (bright && random.nextInt(7) == 0 && this.growth != Growth.REDWOOD) {
            this.advance(level, pos, state, random);
            return;
        }
        switch (this.growth) {
            case REDWOOD -> {
                if (bright && random.nextInt(120) == 0) {
                    if (state.getValue(STAGE) == 0) {
                        level.setBlock(pos, state.setValue(STAGE, 1), Block.UPDATE_NONE);
                    } else {
                        this.growRedwood(level, pos, random);
                    }
                }
            }
            case FLORA -> {
                if (random.nextInt(10) == 0 && bright) {
                    this.advance(level, pos, state, random);
                }
            }
            case NETHER -> {
                if (random.nextInt(10) == 0) {
                    this.advance(level, pos, state, random);
                }
            }
            case RARE -> {
                if (bright && random.nextInt(120) == 0) {
                    this.advance(level, pos, state, random);
                }
            }
        }
    }

    /** Redwood: 40 unmarked-or-marked redwood saplings in the 7x7 are removed (9x9) and the tree grows. */
    private void growRedwood(ServerLevel level, BlockPos pos, RandomSource random) {
        int count = 0;
        for (BlockPos p : BlockPos.betweenClosed(pos.offset(-3, 0, -3), pos.offset(3, 0, 3))) {
            if (level.getBlockState(p).is(this)) {
                count++;
            }
        }
        if (count < 40) {
            return;
        }
        for (BlockPos p : BlockPos.betweenClosed(pos.offset(-4, 0, -4), pos.offset(4, 0, 4))) {
            if (level.getBlockState(p).is(this)) {
                level.setBlock(p, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
            }
        }
        this.growTree(level, pos, random);
    }

    /** markOrGrowMarked / boneFertilize: mark first, then grow. */
    public void advance(ServerLevel level, BlockPos pos, BlockState state, RandomSource random) {
        if (state.getValue(STAGE) == 0) {
            level.setBlock(pos, state.setValue(STAGE, 1), Block.UPDATE_NONE);
        } else {
            this.growTree(level, pos, random);
        }
    }

    /** growTree: remove the sapling, run the generator, put the marked sapling back when it fails. */
    public void growTree(ServerLevel level, BlockPos pos, RandomSource random) {
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_NONE);
        if (!this.tree.grow(level, pos, new Random(random.nextLong()))) {
            level.setBlock(pos, this.defaultBlockState().setValue(STAGE, 1), Block.UPDATE_ALL);
        }
    }

    /** NSaplingBlock.boneFertilize: bone meal does nothing to redwood saplings. */
    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state, BonemealSource source) {
        return this.growth != Growth.REDWOOD;
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state, BonemealSource source) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state, BonemealSource source) {
        this.advance(level, pos, state, random);
    }
}
