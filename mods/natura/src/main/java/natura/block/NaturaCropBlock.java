package natura.block;

import natura.NaturaItems;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.FarmlandBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * CropBlock of Natura: barley (1.5.2 metadata 0-3) and cotton (4-8) on farmland. Growth as in the original: at light
 * 8 or more, chance 1 / (60 / rate + 1) with rate = 1 + 0.25 * (light - 7), +2 under open sky, doubled on wet
 * farmland. Bone meal adds 1-3 (barley) or 1-4 (cotton) stages. Ripe cotton is picked by clicking and goes back two
 * stages.
 */
public abstract class NaturaCropBlock extends CropBlock {
    private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 4, 16);

    protected NaturaCropBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int light = level.getMaxLocalRawBrightness(pos);
        if (light < 8 || this.isMaxAge(state)) {
            return;
        }
        float rate = 0.25F * (light - 7);
        if (level.canSeeSky(pos)) {
            rate += 2.0F;
        }
        BlockState soil = level.getBlockState(pos.below());
        if (soil.hasProperty(FarmlandBlock.MOISTURE) && soil.getValue(FarmlandBlock.MOISTURE) > 0) {
            rate *= 2.0F;
        }
        rate += 1.0F;
        if (random.nextInt((int) (60.0F / rate) + 1) == 0) {
            level.setBlock(pos, this.getStateForAge(this.getAge(state) + 1), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    protected int getBonemealAgeIncrease(Level level) {
        return level.getRandom().nextInt(this.getMaxAge()) + 1;
    }

    public static final class Barley extends NaturaCropBlock {
        public static final IntegerProperty AGE = BlockStateProperties.AGE_3;

        public Barley(Properties properties) {
            super(properties);
        }

        @Override
        protected IntegerProperty getAgeProperty() {
            return AGE;
        }

        @Override
        public int getMaxAge() {
            return 3;
        }

        @Override
        protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
            builder.add(AGE);
        }

        @Override
        protected ItemLike getBaseSeedId() {
            return NaturaItems.BARLEY_SEEDS.get();
        }
    }

    public static final class Cotton extends NaturaCropBlock {
        public static final IntegerProperty AGE = BlockStateProperties.AGE_4;

        public Cotton(Properties properties) {
            super(properties);
        }

        @Override
        protected IntegerProperty getAgeProperty() {
            return AGE;
        }

        @Override
        public int getMaxAge() {
            return 4;
        }

        @Override
        protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
            builder.add(AGE);
        }

        @Override
        protected ItemLike getBaseSeedId() {
            return NaturaItems.COTTON_SEEDS.get();
        }

        private boolean pick(BlockState state, Level level, BlockPos pos, Player player) {
            if (!this.isMaxAge(state)) {
                return false;
            }
            if (!level.isClientSide()) {
                level.setBlock(pos, this.getStateForAge(2), Block.UPDATE_ALL);
                ItemEntity item = new ItemEntity(level, player.getX(), player.getY() - 1.0, player.getZ(), new ItemStack(NaturaItems.COTTON.get()));
                level.addFreshEntity(item);
                item.playerTouch(player);
            }
            return true;
        }

        @Override
        protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
            return this.pick(state, level, pos, player) ? InteractionResult.SUCCESS : InteractionResult.PASS;
        }

        @Override
        protected void attack(BlockState state, Level level, BlockPos pos, Player player) {
            this.pick(state, level, pos, player);
        }
    }
}
