package twilightforest.item;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;
import twilightforest.TFBlocks;
import twilightforest.entity.projectile.MoonwormShot;

/**
 * ItemTFMoonwormQueen (256 uses, recharged with torchberries): placed on a block it sets a moonworm there (one use);
 * held down for more than 12 ticks and released it shoots a moonworm that sticks where it lands (two uses).
 */
public class MoonwormQueenItem extends Item {
    public static final int FIRING_TIME = 12;

    public MoonwormQueenItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (player.getItemInHand(hand).getDamageValue() < player.getItemInHand(hand).getMaxDamage()) {
            player.startUsingItem(hand);
            return InteractionResult.CONSUME;
        }
        return InteractionResult.FAIL;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        ItemStack stack = context.getItemInHand();
        if (context.getLevel().getBlockState(context.getClickedPos()).is(TFBlocks.MOONWORM.get()) || stack.getDamageValue() >= stack.getMaxDamage()) {
            return InteractionResult.PASS;
        }
        if (place(context.getLevel(), context.getPlayer(), new BlockPlaceContext(context))) {
            if (context.getPlayer() != null) {
                TFItemUtil.damage(stack, 1, context.getPlayer());
                context.getPlayer().stopUsingItem();
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    /** onItemUse without a stack: the moonworm shot landing. */
    public static boolean place(Level level, @Nullable Player player, BlockHitResult hit) {
        return place(level, player, new BlockPlaceContext(level, player, InteractionHand.MAIN_HAND, ItemStack.EMPTY, hit));
    }

    private static boolean place(Level level, @Nullable Player player, BlockPlaceContext context) {
        if (!context.canPlace()) {
            return false;
        }
        BlockState state = TFBlocks.MOONWORM.get().getStateForPlacement(context);
        BlockPos pos = context.getClickedPos();
        if (state == null || !state.canSurvive(level, pos) || !level.setBlock(pos, state, 3)) {
            return false;
        }
        SoundType sound = state.getSoundType();
        level.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.SLIME_SQUISH, net.minecraft.sounds.SoundSource.BLOCKS,
                sound.getVolume() / 2.0F, sound.getPitch() * 0.8F);
        return true;
    }

    @Override
    public boolean releaseUsing(ItemStack stack, Level level, LivingEntity entity, int remaining) {
        int useTime = this.getUseDuration(stack, entity) - remaining;
        if (level instanceof ServerLevel server && useTime > FIRING_TIME && stack.getDamageValue() + 1 < stack.getMaxDamage()) {
            MoonwormShot shot = new MoonwormShot(server, entity);
            shot.shootFromRotation(entity, entity.getXRot(), entity.getYRot(), 0.0F, 1.5F, 1.0F);
            if (server.addFreshEntity(shot)) {
                TFItemUtil.damage(stack, 2, entity);
                server.playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.SLIME_SQUISH, entity.getSoundSource(), 1.0F, 1.0F);
            }
        }
        return true;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.BOW;
    }
}
