package betterstorage.block;

import betterstorage.BSEnchantments;
import betterstorage.ChestMaterial;
import betterstorage.item.KeyLike;
import betterstorage.item.LockItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

/**
 * BlockReinforcedChest: a chest of one {@link ChestMaterial} (the metadata of the original). A lock makes it much
 * harder to break (15 times, and persistance + 2 times more) and persistance raises the explosion resistance.
 * Right click opens it if it has no lock, someone has it open already or the held key unlocks the lock; a failed
 * attempt and every left click apply the lock's shock and trigger enchantments. Trigger powers it for 10 ticks.
 */
public class ReinforcedChestBlock extends ConnectableBlock {
    private static final VoxelShape SINGLE = box(1, 0, 1, 15, 14, 15);
    private static final VoxelShape NORTH = box(1, 0, 0, 15, 14, 15);
    private static final VoxelShape SOUTH = box(1, 0, 1, 15, 14, 16);
    private static final VoxelShape WEST = box(0, 0, 1, 15, 14, 15);
    private static final VoxelShape EAST = box(1, 0, 1, 16, 14, 15);
    public final ChestMaterial material;

    public ReinforcedChestBlock(ChestMaterial material, Properties properties) {
        super(properties);
        this.material = material;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ReinforcedChestBlockEntity(pos, state);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(CONNECTION)) {
            case NORTH -> NORTH;
            case SOUTH -> SOUTH;
            case WEST -> WEST;
            case EAST -> EAST;
            default -> SINGLE;
        };
    }

    private static @Nullable ReinforcedChestBlockEntity chest(BlockGetter level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof ReinforcedChestBlockEntity chest ? chest : null;
    }

    /** getBlockHardness: locked 15 times harder, persistance multiplies by its level + 2 on top. */
    @Override
    protected float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
        float progress = super.getDestroyProgress(state, player, level, pos);
        ReinforcedChestBlockEntity chest = chest(level, pos);
        if (chest != null && !chest.getLock().isEmpty()) {
            float factor = 15.0F;
            int persistance = BSEnchantments.level(chest.getLock(), BSEnchantments.PERSISTANCE);
            if (persistance > 0) {
                factor *= persistance + 2;
            }
            progress /= factor;
        }
        return progress;
    }

    /** getExplosionResistance: times 1 + 2^persistance. */
    @Override
    public float getExplosionResistance(BlockState state, BlockGetter level, BlockPos pos, Explosion explosion) {
        float modifier = 1.0F;
        ReinforcedChestBlockEntity chest = chest(level, pos);
        if (chest != null) {
            int persistance = BSEnchantments.level(chest.getLock(), BSEnchantments.PERSISTANCE);
            if (persistance > 0) {
                modifier += (float) Math.pow(2.0, persistance);
            }
        }
        return super.getExplosionResistance(state, level, pos, explosion) * modifier;
    }

    /**
     * onBlockActivated. With a lock in hand on a chest without one it does nothing, so the lock's onItemUse puts it on
     * (PASS goes on to the item). Sneaking with an item in hand skips this, as in 1.5.2, so the key can take a lock off.
     */
    @Override
    protected InteractionResult useItemOn(ItemStack held, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }
        ReinforcedChestBlockEntity chest = chest(level, pos);
        if (chest == null) {
            return InteractionResult.PASS;
        }
        ItemStack lock = chest.getLock();
        if (lock.isEmpty() && held.getItem() instanceof LockItem) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        ItemStack key = held.getItem() instanceof KeyLike ? held : ItemStack.EMPTY;
        boolean success = lock.isEmpty() || chest.canUse(player) || !key.isEmpty() && ((KeyLike) key.getItem()).unlock(key, lock, true);
        if (!lock.isEmpty() && lock.getItem() instanceof LockItem lockType) {
            lockType.onUnlock(lock, key, chest, player, success);
        }
        if (success && player instanceof ServerPlayer sp) {
            chest.openGui(sp);
        }
        return InteractionResult.SUCCESS;
    }

    /** onBlockClicked: the lock's effects at power 3. */
    @Override
    protected void attack(BlockState state, Level level, BlockPos pos, Player player) {
        if (!level.isClientSide()) {
            ReinforcedChestBlockEntity chest = chest(level, pos);
            if (chest != null && chest.getLock().getItem() instanceof LockItem lockType) {
                lockType.applyEffects(chest.getLock(), chest, player, 3);
            }
        }
    }

    @Override
    protected boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    protected int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        ReinforcedChestBlockEntity chest = chest(level, pos);
        return chest != null && chest.isPowered() ? 15 : 0;
    }

    @Override
    protected int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return this.getSignal(state, level, pos, direction);
    }

    /** updateTick: the trigger pulse ends. */
    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        ReinforcedChestBlockEntity chest = chest(level, pos);
        if (chest != null) {
            chest.setPowered(false);
        }
    }
}
