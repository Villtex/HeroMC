package twilightforest.item;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import twilightforest.TFBlocks;

/**
 * ItemTFCrumbleHorn (1024 uses): blown (sheep sound) for more than 10 ticks, every 5 ticks the blocks in the 4-block
 * box 3 blocks ahead crumble 1 in 5: stone to cobblestone, stone bricks to cracked ones, maze stone bricks to cracked
 * maze stone, cobblestone to gravel; gravel and dirt break 1 in 20. One use per crumbled block.
 */
public class CrumbleHornItem extends Item {
    public CrumbleHornItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        player.startUsingItem(hand);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.SHEEP_AMBIENT, player.getSoundSource(), 1.0F, 0.8F);
        return InteractionResult.CONSUME;
    }

    @Override
    public void onUseTick(Level level, LivingEntity user, ItemStack stack, int count) {
        if (count > 10 && count % 5 == 0 && level instanceof ServerLevel server && user instanceof Player player) {
            int crumbled = crumble(server, player, TFItemUtil.effectBox(player, 3.0, 2.0));
            if (crumbled > 0) {
                TFItemUtil.damage(stack, crumbled, player);
            }
            level.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.SHEEP_AMBIENT, user.getSoundSource(), 1.0F, 0.8F);
        }
    }

    private static int crumble(ServerLevel level, Player player, AABB box) {
        int crumbled = 0;
        for (int x = Mth.floor(box.minX); x <= Mth.floor(box.maxX); x++) {
            for (int y = Mth.floor(box.minY); y <= Mth.floor(box.maxY); y++) {
                for (int z = Mth.floor(box.minZ); z <= Mth.floor(box.maxZ); z++) {
                    crumbled += crumbleBlock(level, player, new BlockPos(x, y, z));
                }
            }
        }
        return crumbled;
    }

    private static int crumbleBlock(ServerLevel level, Player player, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.isAir()) {
            return 0;
        }
        int cost = 0;
        if (state.is(Blocks.STONE) && level.getRandom().nextInt(5) == 0) {
            cost += replace(level, pos, state, Blocks.COBBLESTONE.defaultBlockState());
        }
        if (state.is(Blocks.STONE_BRICKS) && level.getRandom().nextInt(5) == 0) {
            cost += replace(level, pos, state, Blocks.CRACKED_STONE_BRICKS.defaultBlockState());
        }
        if (state.is(TFBlocks.MAZESTONES.get(1).get()) && level.getRandom().nextInt(5) == 0) {
            cost += replace(level, pos, state, TFBlocks.MAZESTONES.get(4).get().defaultBlockState());
        }
        if (state.is(Blocks.COBBLESTONE) && level.getRandom().nextInt(5) == 0) {
            cost += replace(level, pos, state, Blocks.GRAVEL.defaultBlockState());
        }
        if ((state.is(Blocks.GRAVEL) || state.is(Blocks.DIRT)) && level.getRandom().nextInt(20) == 0) {
            level.destroyBlock(pos, true, player);
            cost++;
        }
        return cost;
    }

    private static int replace(ServerLevel level, BlockPos pos, BlockState old, BlockState state) {
        level.setBlock(pos, state, 3);
        level.levelEvent(2001, pos, Block.getId(old));
        return 1;
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
