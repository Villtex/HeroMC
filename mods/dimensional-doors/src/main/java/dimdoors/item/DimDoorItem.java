package dimdoors.item;

import dimdoors.block.DimDoorBlock;
import dimdoors.world.DDWorld;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * itemDimDoor, itemExitDoor and ItemChaosDoor: placed on the top of a block like a door, or right-clicked at a rift to
 * put the door into it (its upper half where the rift is).
 */
public class DimDoorItem extends Item {
    private final Supplier<? extends Block> door;
    private final String[] lines;

    public DimDoorItem(Supplier<? extends Block> door, Properties properties, String... lines) {
        super(properties);
        this.door = door;
        this.lines = lines;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (context.getClickedFace() != Direction.UP || context.getPlayer() == null) {
            return InteractionResult.PASS;
        }
        Level level = context.getLevel();
        Player player = context.getPlayer();
        BlockPos pos = context.getClickedPos().above();
        if (!player.mayUseItemAt(pos, Direction.UP, context.getItemInHand()) || !player.mayUseItemAt(pos.above(), Direction.UP, context.getItemInHand())) {
            return InteractionResult.PASS;
        }
        if (!DDItems.canPlace(level, pos) || !DDItems.canPlace(level, pos.above())) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide()) {
            int offset = level.getBlockState(pos.below()).canBeReplaced() && !level.getBlockState(pos.below()).isAir() ? 1 : 0;
            DimDoorBlock.placeDoorBlock(level, pos.below(offset), DDWorld.placeFacing(player), this.door.get());
            context.getItemInHand().shrink(1);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide()) {
            return InteractionResult.PASS;
        }
        BlockPos hit = DDItems.target(level, player, 4.0);
        if (hit == null || !level.getBlockState(hit).is(dimdoors.DimDoors.RIFT.get()) || DDWorld.link(level, hit) == null) {
            return InteractionResult.PASS;
        }
        ItemStack stack = player.getItemInHand(hand);
        if (!player.mayUseItemAt(hit, Direction.UP, stack) || !player.mayUseItemAt(hit.above(), Direction.UP, stack)
                || !DDItems.canPlace(level, hit) || !DDItems.canPlace(level, hit.below())) {
            return InteractionResult.PASS;
        }
        DimDoorBlock.placeDoorBlock(level, hit.below(), DDWorld.placeFacing(player), this.door.get());
        stack.shrink(1);
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        for (String line : this.lines) {
            tooltip.accept(Component.literal(line));
        }
    }
}
