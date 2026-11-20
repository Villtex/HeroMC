package legendgear.block;

import legendgear.entity.BombEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * BlockBombFlower: grows on stone or netherrack next to lava (an air block over lava in the 3x3 around it) with air
 * above. Picking or blowing up a grown flower leaves the cut stalk (hardness 1) and a lit bomb (fuse 80 minus up to 4,
 * or 10 by an explosion); the stalk grows back (1 in 5 random ticks). Losing its place sets the bomb off as well. Shears
 * take the whole flower. Drops nothing else.
 */
public class BombFlowerBlock extends Block {
    public static final BooleanProperty CUT = BooleanProperty.create("cut");
    private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 14, 16);
    private static final VoxelShape CUT_SHAPE = Block.box(0, 0, 0, 16, 3, 16);

    public BombFlowerBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(CUT, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(CUT);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(CUT) ? CUT_SHAPE : SHAPE;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    protected float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
        return state.getValue(CUT) ? super.getDestroyProgress(state, player, level, pos) : 1.0F;
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockState below = level.getBlockState(pos.below());
        if (!below.is(Blocks.STONE) && !below.is(Blocks.NETHERRACK)) {
            return false;
        }
        if (!level.getBlockState(pos.above()).isAir()) {
            return false;
        }
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                BlockPos p = pos.offset(x, 0, z);
                if (level.getBlockState(p).isAir() && level.getFluidState(p.below()).is(net.minecraft.tags.FluidTags.LAVA)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block,
            net.minecraft.world.level.redstone.@org.jspecify.annotations.Nullable Orientation orientation, boolean moved) {
        this.checkFlowerChange(state, level, pos);
    }

    private boolean checkFlowerChange(BlockState state, Level level, BlockPos pos) {
        if (state.canSurvive(level, pos)) {
            return true;
        }
        if (!level.isClientSide() && !state.getValue(CUT)) {
            bomb(level, pos, 80 - level.getRandom().nextInt(5));
        }
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
        return false;
    }

    private static void bomb(Level level, BlockPos pos, int fuse) {
        BombEntity bomb = BombEntity.at(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, fuse);
        bomb.setDeltaMovement(0.0, 0.1, 0.0);
        level.addFreshEntity(bomb);
    }

    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, ItemStack tool, boolean willHarvest, FluidState fluid) {
        this.playerWillDestroy(level, pos, state, player);
        if (state.getValue(CUT)) {
            level.setBlock(pos, fluid.createLegacyBlock(), 3);
        } else if (tool.is(Items.SHEARS)) {
            // IShearable.onSheared: the flower itself, the block gone
            if (!level.isClientSide()) {
                Block.popResource(level, pos, new ItemStack(this));
            }
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        } else {
            level.setBlock(pos, state.setValue(CUT, true), 3);
            if (!level.isClientSide()) {
                bomb(level, pos, 80 - level.getRandom().nextInt(5));
            }
        }
        return false;
    }

    /** onBlockDestroyedByExplosion and the bomb blasts: a grown flower becomes a stalk and a bomb with fuse 10. */
    public void explode(Level level, BlockPos pos, BlockState state) {
        if (!state.getValue(CUT)) {
            level.setBlock(pos, state.setValue(CUT, true), 2);
            if (!level.isClientSide()) {
                bomb(level, pos, 10);
            }
        }
    }

    @Override
    public void onBlockExploded(BlockState state, ServerLevel level, BlockPos pos, Explosion explosion) {
        if (!state.getValue(CUT)) {
            this.explode(level, pos, state);
        } else {
            super.onBlockExploded(state, level, pos, explosion);
        }
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!this.checkFlowerChange(state, level, pos)) {
            return;
        }
        if (random.nextInt(5) == 0 && state.getValue(CUT)) {
            level.setBlock(pos, state.setValue(CUT, false), 2);
        }
    }
}
