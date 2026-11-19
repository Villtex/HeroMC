package betterstorage.item;

import betterstorage.BS;
import betterstorage.backpack.BackpackData;
import betterstorage.backpack.Backpacks;
import betterstorage.backpack.ListContainer;
import betterstorage.block.BackpackBlock;
import betterstorage.block.BackpackBlockEntity;
import betterstorage.block.ContainerWrapper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

/**
 * ItemBackpack and ItemEnderBackpack: worn in the chest slot (only by picking up a placed backpack, see
 * {@link BackpackBlock}), set down on top of a solid block. The ender backpack holds nothing itself: it opens the
 * ender chest of whoever opens it, can not be taken out of the armor slot and is repaired with obsidian.
 */
public class BackpackItem extends Item {
    public final boolean ender;

    public BackpackItem(boolean ender, Properties properties) {
        super(properties);
        this.ender = ender;
    }

    public Block block() {
        return this.ender ? BS.ENDER_BACKPACK_BLOCK.get() : BS.BACKPACK_BLOCK.get();
    }

    public String getName() {
        return this.ender ? "container.betterstorage.ender_backpack" : "container.betterstorage.backpack";
    }

    public int getColumns() {
        return 9;
    }

    public int getRows() {
        return this.ender ? 3 : betterstorage.BSConfig.backpackRows();
    }

    /** getBackpackItemsInternal. */
    public @Nullable Container getBackpackItems(LivingEntity carrier, @Nullable Player player) {
        if (this.ender) {
            return player == null ? null : new ContainerWrapper(player.getEnderChestInventory());
        }
        BackpackData data = Backpacks.data(carrier);
        if (data.contents == null) {
            data.contents = NonNullList.withSize(this.getColumns() * this.getRows(), ItemStack.EMPTY);
        }
        return new ListContainer(data.contents);
    }

    /** canTake: the ender backpack stays on until it is set down. */
    public boolean canTake(ItemStack backpack) {
        return !this.ender;
    }

    public boolean containsItems(LivingEntity carrier) {
        return !this.ender && (Backpacks.state(carrier).hasItems() || Backpacks.data(carrier).hasItems());
    }

    /** isValidArmor false: never put on by hand. */
    @Override
    public boolean canEquip(ItemStack stack, EquipmentSlot armorType, LivingEntity entity) {
        return false;
    }

    /** onItemRightClick did nothing (ItemArmor would have put it on). */
    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        return InteractionResult.PASS;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        return player != null && place(context.getItemInHand(), player, context.getLevel(), context.getClickedPos(), context.getClickedFace())
                ? InteractionResult.SUCCESS : InteractionResult.FAIL;
    }

    /** Where onItemUse would put the backpack, or null. */
    public static @Nullable BlockPos placePos(Player player, Level level, BlockPos pos, Direction side) {
        BlockState clicked = level.getBlockState(pos);
        if (!clicked.is(Blocks.SNOW) && !clicked.is(Blocks.VINE) && !clicked.is(Blocks.SHORT_GRASS) && !clicked.is(Blocks.FERN)
                && !clicked.is(Blocks.DEAD_BUSH) && !clicked.canBeReplaced()) {
            if (side != Direction.UP) {
                return null;
            }
            pos = pos.above();
        }
        if (pos.getY() <= level.getMinY() || pos.getY() >= level.getMaxY()) {
            return null;
        }
        BlockPos below = pos.below();
        if (!level.getBlockState(below).isFaceSturdy(level, below, Direction.UP)) {
            return null;
        }
        if (!player.mayUseItemAt(pos, side, ItemStack.EMPTY)) {
            return null;
        }
        BlockState there = level.getBlockState(pos);
        if (!there.isAir() && !there.canBeReplaced() && !there.is(Blocks.SNOW)) {
            return null;
        }
        return pos;
    }

    /** onItemUse: sets the backpack down facing the player; a worn one takes its contents along. */
    public static boolean place(ItemStack stack, Player player, Level level, BlockPos clicked, Direction side) {
        if (stack.isEmpty() || !(stack.getItem() instanceof BackpackItem item)) {
            return false;
        }
        BlockPos pos = placePos(player, level, clicked, side);
        if (pos == null) {
            return false;
        }
        Block block = item.block();
        BlockState state = block.defaultBlockState().setValue(BackpackBlock.FACING, player.getDirection().getOpposite());
        if (!state.canSurvive(level, pos) || !level.isUnobstructed(state, pos, net.minecraft.world.phys.shapes.CollisionContext.of(player))) {
            return false;
        }
        if (level.isClientSide()) {
            return true;
        }
        if (!level.setBlock(pos, state, 3) || !level.getBlockState(pos).is(block)) {
            return false;
        }
        if (level.getBlockEntity(pos) instanceof BackpackBlockEntity backpack) {
            backpack.stack = stack.copyWithCount(1);
            Component name = stack.get(DataComponents.CUSTOM_NAME);
            if (name != null) {
                backpack.setCustomTitle(name);
            }
            if (Backpacks.getBackpack(player) == stack) {
                backpack.unequip(player);
            }
            backpack.sync();
        }
        SoundType sound = state.getSoundType();
        level.playSound(null, pos, sound.getPlaceSound(), SoundSource.BLOCKS, (sound.getVolume() + 1.0F) / 2.0F, sound.getPitch() * 0.8F);
        stack.shrink(1);
        return true;
    }
}
