package legendgear.block;

import legendgear.LGItems;
import legendgear.LGSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;

/**
 * BlockChained: unbreakable wood. Kind 0 chains, 2/3/4 an iron, gold or diamond lock that the matching one-use key opens.
 * An opened block (kind 1) vanishes after 10 ticks and opens every chained block next to it, so a whole wall of chains
 * comes down from one lock.
 */
public class ChainedBlock extends Block {
    public static final IntegerProperty KIND = IntegerProperty.create("kind", 0, 4);
    public static final int UNCHAINING = 1;
    private static final int TICK_RATE = 10;

    public ChainedBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(KIND, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(KIND);
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return state.getValue(KIND) == UNCHAINING;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        this.tick(state, level, pos, random);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (state.getValue(KIND) == UNCHAINING) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
        }
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block,
            net.minecraft.world.level.redstone.@org.jspecify.annotations.Nullable Orientation orientation, boolean moved) {
        if (state.getValue(KIND) == UNCHAINING || level.isClientSide()) {
            return;
        }
        for (Direction d : Direction.values()) {
            BlockState n = level.getBlockState(pos.relative(d));
            if (n.is(this) && n.getValue(KIND) == UNCHAINING) {
                level.setBlock(pos, state.setValue(KIND, UNCHAINING), 3);
                level.scheduleTick(pos, this, TICK_RATE);
                return;
            }
        }
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        int kind = state.getValue(KIND);
        if (kind > 1 && stack.is(LGItems.KEYS.get(kind - 2).get())) {
            if (!level.isClientSide()) {
                level.setBlock(pos, state.setValue(KIND, UNCHAINING), 3);
                level.scheduleTick(pos, this, TICK_RATE);
                level.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, LGSounds.UNCHAIN.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
                stack.shrink(1);
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.TRY_WITH_EMPTY_HAND;
    }
}
