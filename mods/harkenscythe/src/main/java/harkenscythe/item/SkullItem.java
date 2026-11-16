package harkenscythe.item;

import harkenscythe.HSBlocks;
import harkenscythe.block.HSSkullBlock;
import harkenscythe.block.HSSkullBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

/**
 * ItemHSSkull: placed like a vanilla skull on the top or a side of a solid block (not from below); on the floor it
 * looks at the player in sixteen steps. It can be worn as a helmet (no armour).
 */
public class SkullItem extends Item {
    private final SkullType type;

    public SkullItem(SkullType type, Properties properties) {
        super(properties);
        this.type = type;
    }

    public SkullType type() {
        return this.type;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Direction face = context.getClickedFace();
        Level level = context.getLevel();
        if (face == Direction.DOWN || !level.getBlockState(context.getClickedPos()).isSolid()) {
            return InteractionResult.FAIL;
        }
        BlockPos pos = context.getClickedPos().relative(face);
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();
        if (player != null && !player.mayUseItemAt(pos, face, stack)) {
            return InteractionResult.FAIL;
        }
        BlockState existing = level.getBlockState(pos);
        if (!existing.canBeReplaced(new BlockPlaceContext(context))) {
            return InteractionResult.FAIL;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        BlockState state = HSBlocks.SKULL.get().defaultBlockState().setValue(HSSkullBlock.FACING, face);
        level.setBlock(pos, state, Block.UPDATE_CLIENTS);
        int rotation = 0;
        if (face == Direction.UP && player != null) {
            // RotationSegment of today's skulls (the same direction as the yaw * 16 / 360 of 1.5.2's renderer)
            rotation = Mth.floor((player.getYRot() + 180.0F) * 16.0F / 360.0F + 0.5) & 15;
        }
        if (level.getBlockEntity(pos) instanceof HSSkullBlockEntity skull) {
            skull.setSkull(this.type.ordinal(), rotation);
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        }
        level.gameEvent(player, GameEvent.BLOCK_PLACE, pos);
        stack.consume(1, player);
        return InteractionResult.SUCCESS;
    }
}
