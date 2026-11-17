package projectzulu.block.plant;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import projectzulu.ZuluPlants;

/**
 * BlockAloeVera: metadata 0-3 is the growing base, 4-8 the flower stalk on top of a grown base (meta 3). The original
 * re-scheduled itself every tick, so the growth below runs every tick once the plant has ticked (placed or random
 * tick). Chances are "brightness - rand(200) >= 0", i.e. 1/200, 2/200 in full light. A wet surrounding (water within
 * 4 blocks) lets the stalk grow and turns the soil below into watered dirt; a full flower (meta 8) blows away as a
 * tumbleweed next to the plant.
 */
public class AloeVeraBlock extends ZuluFlowerBlock {
    public static final IntegerProperty AGE = IntegerProperty.create("age", 0, 8);
    private static final int PROBABILITY = 200;

    public AloeVeraBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(AGE, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE);
    }

    /** canThisPlantGrowOnThisBlockID: watered dirt, grass, dirt, farmland, sand, aloe vera. */
    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(ZuluPlants.WATERED_DIRT.get()) || state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.DIRT) || state.is(Blocks.FARMLAND)
                || state.is(Blocks.SAND) || state.is(this);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        level.scheduleTick(pos, this, 4);
        super.onPlace(state, level, pos, oldState, movedByPiston);
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        this.tick(state, level, pos, random);
    }

    /** brightness - rand(200) >= 0 with the 1.5.2 brightness 0..1: only rand 0, or rand 1 in full light. */
    private static boolean lightChance(ServerLevel level, BlockPos pos, RandomSource random) {
        int roll = random.nextInt(PROBABILITY);
        return roll == 0 || roll == 1 && level.getMaxLocalRawBrightness(pos) >= 15;
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int meta = state.getValue(AGE);
        int waterRate = waterRate(level, pos, 1);
        Block aloe = this;
        if (meta < 3 && lightChance(level, pos, random)) {
            level.setBlock(pos, state.setValue(AGE, meta + 1), Block.UPDATE_ALL);
        }
        if (meta == 3 && waterRate > 1 && level.getBlockState(pos.above()).isAir() && !level.getBlockState(pos.below()).is(aloe)
                && lightChance(level, pos.above(), random)) {
            level.setBlock(pos.above(), this.defaultBlockState().setValue(AGE, 4), Block.UPDATE_ALL);
        }
        waterRate = waterRate(level, pos.below(), 1);
        int weedRate = waterRate(level, pos.below(), 0);
        if (meta > 3 && meta < 8 && waterRate > 1 && level.getBlockState(pos.below()).is(aloe) && lightChance(level, pos, random)) {
            level.setBlock(pos, state.setValue(AGE, meta + 1), Block.UPDATE_ALL);
        }
        if (meta == 8 && level.getBlockState(pos).is(aloe)) {
            int roll = random.nextInt(PROBABILITY);
            if (weedRate - roll >= 0) {
                BlockPos spot = findSuitableBlockLoc(level, pos.below());
                if (spot != null) {
                    level.setBlock(spot, ZuluPlants.TUMBLEWEED.get().defaultBlockState(), Block.UPDATE_ALL);
                    level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                }
            }
        }
        int roll = random.nextInt(PROBABILITY / 2);
        if (waterRate - roll >= 0) {
            // three checks in a row as in the original: dirt turns into watered dirt and moistens once in the same tick
            if (level.getBlockState(pos.below()).is(Blocks.DIRT)) {
                level.setBlock(pos.below(), WateredDirtBlock.state(0), Block.UPDATE_ALL);
            }
            if (level.getBlockState(pos.below()).is(Blocks.SAND)) {
                level.setBlock(pos.below(), WateredDirtBlock.state(4), Block.UPDATE_ALL);
            }
            BlockState below = level.getBlockState(pos.below());
            if (below.is(ZuluPlants.WATERED_DIRT.get()) && below.getValue(WateredDirtBlock.MOISTURE) != 3) {
                level.setBlock(pos.below(), below.setValue(WateredDirtBlock.MOISTURE, below.getValue(WateredDirtBlock.MOISTURE) + 1), Block.UPDATE_ALL);
            }
        }
        // BlockFlower.updateTick -> checkFlowerChange
        BlockState now = level.getBlockState(pos);
        if (now.is(aloe) && !now.canSurvive(level, pos)) {
            level.destroyBlock(pos, true);
            return;
        }
        if (now.is(aloe)) {
            level.scheduleTick(pos, this, 1);
        }
    }

    /** A free spot for the tumbleweed: air above solid ground that is not water, around and one below x/y/z. */
    private static BlockPos findSuitableBlockLoc(ServerLevel level, BlockPos base) {
        for (int i = -1; i < 2; i++) {
            for (int j = -1; j < 1; j++) {
                for (int k = -1; k < 2; k++) {
                    BlockPos p = base.offset(i, j, k);
                    BlockState under = level.getBlockState(p.below());
                    if (level.getBlockState(p).isAir() && !under.is(Blocks.WATER) && !under.isAir()) {
                        return p;
                    }
                }
            }
        }
        return null;
    }

    /**
     * waterGrowthRate (start 1) / weedGrowthRate (start 0): one per column of the 9x9 around x/z that has water at y or
     * y-1, at most 30.
     */
    private static int waterRate(ServerLevel level, BlockPos pos, int start) {
        int rate = start;
        BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
        for (int i = -4; i < 5; i++) {
            for (int j = -4; j < 5; j++) {
                for (int k = -1; k < 1; k++) {
                    if (level.getBlockState(p.set(pos.getX() + i, pos.getY() + k, pos.getZ() + j)).is(Blocks.WATER)) {
                        rate++;
                        break;
                    }
                }
            }
        }
        return Math.min(rate, 30);
    }
}
