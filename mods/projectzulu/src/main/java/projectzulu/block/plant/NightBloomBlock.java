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
 * BlockNightBloom: opens one stage (metadata 0-4) per 20 ticks after time 13000 of the day and closes again during
 * the day. Glows with light 9 from stage 2 on (getLightValue).
 */
public class NightBloomBlock extends ZuluFlowerBlock {
    public static final IntegerProperty STAGE = IntegerProperty.create("stage", 0, 4);

    public NightBloomBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(STAGE, 0));
    }

    public static int light(BlockState state) {
        return state.getValue(STAGE) > 1 ? 9 : 0;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(STAGE);
    }

    /** grass, dirt, farmland, watered dirt. */
    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.DIRT) || state.is(Blocks.FARMLAND) || state.is(ZuluPlants.WATERED_DIRT.get());
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        level.scheduleTick(pos, this, 2);
        super.onPlace(state, level, pos, oldState, movedByPiston);
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        this.tick(state, level, pos, random);
    }

    /** mapTimeTo24000. */
    private static long dayTime(long worldTime) {
        long t = worldTime;
        while (t > 24000L) {
            t -= 24000L;
        }
        return t <= 0L ? 0L : t;
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        long time = dayTime(level.getOverworldClockTime());
        int stage = state.getValue(STAGE);
        if (time > 13000L && stage != 4) {
            state = state.setValue(STAGE, stage + 1);
            level.setBlock(pos, state, Block.UPDATE_ALL);
            level.scheduleTick(pos, this, 20);
        }
        stage = state.getValue(STAGE);
        if (time < 13000L && stage != 0) {
            state = state.setValue(STAGE, stage - 1);
            level.setBlock(pos, state, Block.UPDATE_ALL);
            level.scheduleTick(pos, this, 20);
        }
        if (!state.canSurvive(level, pos)) {
            level.destroyBlock(pos, true);
            return;
        }
        // a second schedule is ignored while one is pending, as in 1.5.2
        level.scheduleTick(pos, this, 100 + random.nextInt(200));
    }
}
