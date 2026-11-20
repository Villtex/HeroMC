package projectzulu.item.gear;

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
import projectzulu.ZuluGear;
import projectzulu.block.gear.MobHead;
import projectzulu.block.gear.MobHeadBlock;
import projectzulu.block.gear.MobHeadBlockEntity;

/**
 * ItemMobHeads: placed on the top or a side of a solid block (not from below); on the floor it turns in sixteen
 * steps with the player's yaw (floor(yaw * 16 / 360 + 0.5) & 15 like the original). Stacks to 8.
 */
public class MobHeadItem extends Item {
    private final MobHead head;

    public MobHeadItem(MobHead head, Properties properties) {
        super(properties);
        this.head = head;
    }

    public MobHead head() {
        return this.head;
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
        if (!level.getBlockState(pos).canBeReplaced(new BlockPlaceContext(context))) {
            return InteractionResult.FAIL;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        BlockState state = ZuluGear.MOB_HEAD.get().defaultBlockState().setValue(MobHeadBlock.FACING, face);
        level.setBlock(pos, state, Block.UPDATE_ALL);
        int rotation = 0;
        if (face == Direction.UP && player != null) {
            rotation = Mth.floor(player.getYRot() * 16.0F / 360.0F + 0.5) & 15;
        }
        if (level.getBlockEntity(pos) instanceof MobHeadBlockEntity entity) {
            entity.setHead(this.head, rotation);
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        }
        level.gameEvent(player, GameEvent.BLOCK_PLACE, pos);
        stack.consume(1, player);
        return InteractionResult.SUCCESS;
    }
}
