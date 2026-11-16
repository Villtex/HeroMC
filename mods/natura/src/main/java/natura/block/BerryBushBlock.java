package natura.block;

import natura.NaturaBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealSource;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.function.Supplier;

/**
 * BerryBush / NetherBerryBush. 1.5.2 metadata = berry type + 4 * age; here every berry is a block and {@code age} is
 * 0 (small), 1 (medium), 2 (full bush) and 3 (ripe). A ripe bush gives one berry when clicked (left or right) and
 * goes back to age 2. Full bushes grow a new small bush on top, up to three high. Overworld bushes grow at light 8
 * (1/20 per random tick), nether bushes at any light (1/75). Bone meal: overworld bushes grow one or two ages up to
 * a full bush (or a new bush on top, 1/3); nether bushes only half as often.
 */
public class BerryBushBlock extends Block implements BonemealableBlock {
    public static final IntegerProperty AGE = BlockStateProperties.AGE_3;
    private static final VoxelShape SMALL = Block.box(4, 0, 4, 12, 8, 12);
    private static final VoxelShape MEDIUM = Block.box(2, 0, 2, 14, 12, 14);

    private final boolean nether;
    private final Supplier<? extends Item> berry;

    public BerryBushBlock(boolean nether, Supplier<? extends Item> berry, Properties properties) {
        super(properties);
        this.nether = nether;
        this.berry = berry;
        this.registerDefaultState(this.stateDefinition.any().setValue(AGE, 0));
    }

    /** 1.5.2 metadata to the age (meta / 4). */
    public static int age(int meta) {
        return (meta >> 2) & 3;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(AGE)) {
            case 0 -> SMALL;
            case 1 -> MEDIUM;
            default -> Shapes.block();
        };
    }

    /**
     * BerryBushItem / NetherBerryBushItem: on soil that carries plains (nether) plants, or on a full bush of the same
     * kind. Placed bushes do not break when the soil goes, as in 1.5.2.
     */
    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockState below = level.getBlockState(pos.below());
        if (below.getBlock() instanceof BerryBushBlock bush && bush.nether == this.nether) {
            return below.getValue(AGE) >= 2;
        }
        if (this.nether) {
            return below.is(Blocks.SOUL_SAND) || below.is(Blocks.NETHERRACK) || below.is(NaturaBlocks.TAINTED_SOIL.get());
        }
        return below.is(BlockTags.SUPPORTS_VEGETATION);
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int height = 1;
        while (level.getBlockState(pos.below(height)).is(this)) {
            height++;
        }
        boolean grows = this.nether ? random.nextInt(75) == 0 : random.nextInt(20) == 0 && level.getMaxLocalRawBrightness(pos) >= 8;
        if (grows) {
            int age = state.getValue(AGE);
            if (age < 3) {
                level.setBlock(pos, state.setValue(AGE, age + 1), Block.UPDATE_ALL);
            }
            if (random.nextInt(3) == 0 && height < 3 && level.isEmptyBlock(pos.above()) && age >= 2) {
                level.setBlock(pos.above(), this.defaultBlockState(), Block.UPDATE_ALL);
            }
        }
    }

    private boolean harvest(BlockState state, Level level, BlockPos pos, Player player) {
        if (state.getValue(AGE) < 3) {
            return false;
        }
        if (!level.isClientSide()) {
            level.setBlock(pos, state.setValue(AGE, 2), Block.UPDATE_ALL);
            ItemEntity item = new ItemEntity(level, player.getX(), player.getY() - 1.0, player.getZ(), new ItemStack(this.berry.get()));
            level.addFreshEntity(item);
            item.playerTouch(player);
        }
        return true;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        return this.harvest(state, level, pos, player) ? InteractionResult.SUCCESS : InteractionResult.PASS;
    }

    @Override
    protected void attack(BlockState state, Level level, BlockPos pos, Player player) {
        this.harvest(state, level, pos, player);
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state, BonemealSource source) {
        return state.getValue(AGE) < 2 || level.isEmptyBlock(pos.above());
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state, BonemealSource source) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state, BonemealSource source) {
        int age = state.getValue(AGE);
        if (age < 2) {
            if (!this.nether || random.nextBoolean()) {
                level.setBlock(pos, state.setValue(AGE, Math.min(2, random.nextInt(2) + 1 + age)), Block.UPDATE_ALL);
            }
        } else if ((!this.nether || random.nextBoolean()) && random.nextInt(3) == 0) {
            level.setBlock(pos.above(), this.defaultBlockState(), Block.UPDATE_ALL);
        }
    }
}
