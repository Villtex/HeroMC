package bibliocraft.shelf;

import bibliocraft.common.BCConfig;
import bibliocraft.common.BiblioBlock;
import bibliocraft.common.BiblioMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * BlockWeaponRack ("Tool Rack"): four pegs, the places as on the shelf. Right click with a tool (no block) on a place
 * hangs it there or, if the place is taken, drops what hangs there; anything else in the hand, or an empty hand, drops
 * the tool of the place. Only sneaking opens the GUI.
 */
public class ToolRackBlock extends BiblioBlock {
    /** setBlockBoundsBasedOnState: the back half of the block. */
    private static final VoxelShape[] SHAPES = rotations(8, 0, 0, 16, 16, 16);
    /** Metadata of the original: 0 oak, 1 spruce, 2 birch, 3 jungle (texture toolrack1..4). */
    public final int wood;

    public ToolRackBlock(int wood, Properties properties) {
        super(true, properties);
        this.wood = wood;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ToolRackBlockEntity(pos, state);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES[angle(state)];
    }

    @Override
    protected InteractionResult useItemOn(ItemStack held, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof ToolRackBlockEntity be) {
            this.activate(level, pos, state, player, held, be, hit);
        }
        return InteractionResult.SUCCESS;
    }

    /** onBlockActivated (server). */
    private void activate(Level level, BlockPos pos, BlockState state, Player player, ItemStack held, ToolRackBlockEntity be, BlockHitResult hit) {
        if (player.isShiftKeyDown()) {
            if (player instanceof ServerPlayer sp) {
                BiblioMenu.open(sp, be, Component.literal("Tool Rack"), ToolRackMenu::new);
            }
            return;
        }
        int angle = angle(state);
        int slot = ShelfBlock.whatSlot(angle, (float) (hit.getLocation().x - pos.getX()), (float) (hit.getLocation().y - pos.getY()),
                (float) (hit.getLocation().z - pos.getZ()));
        if (slot < 0) {
            return;
        }
        if (!held.isEmpty() && !BCConfig.isBlock(held) && ToolRackBlockEntity.isItemTool(held) && be.addTool(slot, held)) {
            player.getInventory().setItem(player.getInventory().getSelectedSlot(), ItemStack.EMPTY);
            return;
        }
        Shelves.dropInFront(level, pos, angle, be.getItem(slot));
        be.removeTool(slot);
    }
}
