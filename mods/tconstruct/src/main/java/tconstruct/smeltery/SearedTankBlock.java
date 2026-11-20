package tconstruct.smeltery;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jspecify.annotations.Nullable;

/**
 * LavaTankBlock (seared tank, seared glass, seared window): a seared block of the smeltery with a tank of 4000 mB. It
 * glows with the light of its liquid, takes full buckets and fills empty ones, and keeps its liquid when broken (the
 * item carries it, {@code tconstruct:liquid}).
 */
public class SearedTankBlock extends BaseEntityBlock {
    public SearedTankBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TankBlockEntity(pos, state);
    }

    @Override
    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof TankBlockEntity tank ? tank.brightness() : 0;
    }

    @Override
    protected boolean skipRendering(BlockState state, BlockState adjacent, Direction direction) {
        return adjacent.is(this) || super.skipRendering(state, adjacent, direction);
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, @Nullable Orientation orientation, boolean movedByPiston) {
        if (!level.isClientSide()) {
            SearedBrickBlock.notify(level, pos);
        }
    }

    /** onBlockActivated: a full bucket goes in if all of it fits; an empty bucket takes 1000 mB of the liquid. */
    @Override
    protected InteractionResult useItemOn(ItemStack held, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
                                          BlockHitResult hit) {
        if (held.isEmpty() || !(level.getBlockEntity(pos) instanceof TankBlockEntity tank)) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        if (held.getItem() instanceof BucketItem bucket && bucket.getContent() != Fluids.EMPTY) {
            FluidStack liquid = new FluidStack(bucket.getContent(), 1000);
            if (tank.fill(null, liquid, false) == liquid.getAmount()) {
                if (!level.isClientSide()) {
                    tank.fill(null, liquid, true);
                    if (!player.getAbilities().instabuild) {
                        player.setItemInHand(hand, consume(held));
                    }
                }
            }
            return InteractionResult.SUCCESS;
        }
        if (held.is(Items.BUCKET)) {
            FluidStack liquid = tank.liquid();
            Fluid fluid = liquid.getFluid();
            if (!liquid.isEmpty() && liquid.getAmount() >= 1000 && fluid.getBucket() != Items.AIR) {
                if (!level.isClientSide()) {
                    ItemStack filled = new ItemStack(fluid.getBucket());
                    tank.drain(null, 1000, true);
                    if (!player.getAbilities().instabuild) {
                        if (held.getCount() == 1) {
                            player.setItemInHand(hand, filled);
                        } else {
                            held.shrink(1);
                            if (!player.getInventory().add(filled)) {
                                player.drop(filled, false, net.minecraft.util.Prediction.SERVER_ONLY);
                            }
                        }
                    }
                }
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.TRY_WITH_EMPTY_HAND;
    }

    /** consumeItem: one used up, a single bucket turns into its container. */
    static ItemStack consume(ItemStack stack) {
        if (stack.getCount() == 1) {
            ItemStack remainder = stack.getItem().getCraftingRemainder() == null ? ItemStack.EMPTY : stack.getItem().getCraftingRemainder().create();
            return remainder.isEmpty() && stack.getItem() instanceof BucketItem ? new ItemStack(Items.BUCKET) : remainder;
        }
        stack.shrink(1);
        return stack;
    }
}
