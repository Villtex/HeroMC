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
 * BlockPotionShelf: three shelves of four potions. Right click on a place puts a held potion there, sneaking takes the
 * potion out in front of the shelf, everything else opens the GUI.
 */
public class PotionShelfBlock extends BiblioBlock {
    /** setBlockBoundsBasedOnState: the back half of the block. */
    private static final VoxelShape[] SHAPES = rotations(8, 0, 0, 16, 16, 16);
    /** Metadata of the original: 0 oak, 1 spruce, 2 birch, 3 jungle (texture potionshelf1..4). */
    public final int wood;

    public PotionShelfBlock(int wood, Properties properties) {
        super(true, properties);
        this.wood = wood;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PotionShelfBlockEntity(pos, state);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES[angle(state)];
    }

    /** isWhatShelf: first slot of the shelf at hitY (8 bottom, 4 middle, 0 top), -1 at hitY = 1. */
    public static int whatShelf(double hitY) {
        return switch ((int) (hitY * 3.0)) {
            case 0 -> 8;
            case 1 -> 4;
            case 2 -> 0;
            default -> -1;
        };
    }

    /**
     * isWhatPot: the place 0..3 on a shelf, with the fall-through of the original switch (angle 1 with xt = 4 falls
     * into the angle 2 case, and on into angle 3).
     */
    public static int whatPot(int angle, double hitX, double hitZ) {
        int xt = (int) (hitX * 4.0);
        int zt = (int) (hitZ * 4.0);
        switch (angle) {
            case 0:
                return zt;
            case 1:
                if (xt >= 0 && xt <= 3) {
                    return 3 - xt;
                }
            case 2:
                if (zt >= 0 && zt <= 3) {
                    return 3 - zt;
                }
            case 3:
                return xt;
            default:
                return -1;
        }
    }

    @Override
    protected InteractionResult useItemOn(ItemStack held, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof PotionShelfBlockEntity be) {
            this.activate(level, pos, state, player, held, be, hit);
        }
        return InteractionResult.SUCCESS;
    }

    /**
     * onBlockActivated (server). The original indexed the inventory with the slot unchecked (hitY = 1 gives -1 + n)
     * and threw; such hits do nothing when sneaking and open the GUI otherwise.
     */
    private void activate(Level level, BlockPos pos, BlockState state, Player player, ItemStack held, PotionShelfBlockEntity be, BlockHitResult hit) {
        float hitX = (float) (hit.getLocation().x - pos.getX());
        float hitY = (float) (hit.getLocation().y - pos.getY());
        float hitZ = (float) (hit.getLocation().z - pos.getZ());
        int angle = angle(state);
        int slot = whatShelf(hitY) + whatPot(angle, hitX, hitZ);
        boolean valid = slot >= 0 && slot < 12;
        if (player.isShiftKeyDown()) {
            if (valid) {
                Shelves.dropInFront(level, pos, angle, be.getItem(slot));
                be.addPotion(ItemStack.EMPTY, slot);
            }
            return;
        }
        if (!held.isEmpty() && BCConfig.isPotion(held) && valid && be.addPotion(held, slot)) {
            player.getInventory().setItem(player.getInventory().getSelectedSlot(), ItemStack.EMPTY);
            return;
        }
        if (player instanceof ServerPlayer sp) {
            BiblioMenu.open(sp, be, Component.literal("Potion Shelf"), PotionShelfMenu::new);
        }
    }
}
