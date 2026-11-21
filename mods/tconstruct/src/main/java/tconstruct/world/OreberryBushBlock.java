package tconstruct.world;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

import java.util.function.Supplier;

/**
 * OreberryBush / OreberryBushEssence. 1.5.2 metadata = berry type + 4 * age; here every berry is a block and {@code age}
 * is 0 (small), 1 (medium), 2 (full bush) and 3 (ripe). Bushes grow in the dark (light below 10, 1 in 20 random ticks),
 * the essence bush at any light. A ripe bush gives one berry when clicked (left or right) and goes back to age 2. They
 * prick every entity but items, and stay when their support goes.
 */
public class OreberryBushBlock extends Block {
    public static final IntegerProperty AGE = BlockStateProperties.AGE_3;
    private static final VoxelShape SMALL = Block.box(4, 0, 4, 12, 8, 12);
    private static final VoxelShape MEDIUM = Block.box(2, 0, 2, 14, 12, 14);
    private static final VoxelShape FULL_COLLISION = Block.box(1, 0, 1, 15, 15, 15);

    private final boolean essence;
    private final Supplier<? extends Item> berry;

    public OreberryBushBlock(boolean essence, Supplier<? extends Item> berry, Properties properties) {
        super(properties);
        this.essence = essence;
        this.berry = berry;
        this.registerDefaultState(this.stateDefinition.any().setValue(AGE, 0));
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

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(AGE) >= 2 ? FULL_COLLISION : this.getShape(state, level, pos, context);
    }

    /** OreberryBushItem: only on the top of a block that is solid there, or of a full bush, with air above. */
    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        if (context.getClickedFace() != Direction.UP) {
            return null;
        }
        BlockPos below = context.getClickedPos().below();
        BlockState soil = context.getLevel().getBlockState(below);
        boolean sustains = soil.getBlock() instanceof OreberryBushBlock ? soil.getValue(AGE) >= 2
                : soil.isFaceSturdy(context.getLevel(), below, Direction.UP);
        return sustains ? this.defaultBlockState() : null;
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return state.getValue(AGE) < 3;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (random.nextInt(20) == 0 && (this.essence || level.getRawBrightness(pos, 0) < 10)) {
            int age = state.getValue(AGE);
            if (age < 3) {
                level.setBlock(pos, state.setValue(AGE, age + 1), Block.UPDATE_ALL);
            }
        }
    }

    private boolean harvest(BlockState state, Level level, BlockPos pos, Player player) {
        if (state.getValue(AGE) < 3) {
            return false;
        }
        if (!level.isClientSide()) {
            level.setBlock(pos, state.setValue(AGE, 2), Block.UPDATE_ALL);
            // AbilityHelper.spawnItemAtPlayer
            ItemEntity item = new ItemEntity(level, player.getX() + 0.5, player.getY() + 0.5, player.getZ() + 0.5, new ItemStack(this.berry.get()));
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

    /** onEntityCollidedWithBlock: 1 damage like a cactus, items are spared. */
    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effects, boolean intersects) {
        if (!(entity instanceof ItemEntity) && level instanceof ServerLevel server) {
            entity.hurtServer(server, level.damageSources().cactus(), 1.0F);
        }
    }
}
