package bibliocraft.shelf;

import bibliocraft.common.BiblioBlock;
import bibliocraft.common.BiblioMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.common.world.AuxiliaryLightManager;
import org.jspecify.annotations.Nullable;

/**
 * BlockGenericShelf: two shelves with two places each. Right click with an item on a place puts it there (or, if the
 * place is taken, drops what lies there); with an empty hand the item of the place drops, on an empty place the GUI
 * opens. Sneaking opens the GUI. Glows with the brightest block on it.
 */
public class ShelfBlock extends BiblioBlock {
    /** setBlockBoundsBasedOnState: the back half of the block. */
    private static final VoxelShape[] SHAPES = rotations(8, 0, 0, 16, 16, 16);
    /** Metadata of the original: 0 oak, 1 spruce, 2 birch, 3 jungle (texture genericshelf1..4). */
    public final int wood;

    public ShelfBlock(int wood, Properties properties) {
        super(true, properties);
        this.wood = wood;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ShelfBlockEntity(pos, state);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES[angle(state)];
    }

    /** getLightValue, kept by the block entity in the chunk's light manager. */
    @Override
    public boolean hasDynamicLightEmission(BlockState state) {
        return true;
    }

    @Override
    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
        AuxiliaryLightManager lights = level.getAuxLightManager(pos);
        return lights != null ? lights.getLightAt(pos) : 0;
    }

    /**
     * The place under the hit as onBlockActivated tested it ({@code check} = (int) (hit * 2)): upper shelf 0, 1, lower
     * shelf 2, 3, left to right seen from the front; -1 off the places (hit = 1 on an axis). Also used by the tool rack.
     */
    public static int whatSlot(int angle, int xCheck, int yCheck, int zCheck) {
        int row = switch (yCheck) {
            case 1 -> 0;
            case 0 -> 2;
            default -> -1;
        };
        int across = switch (angle) {
            case 0 -> zCheck;
            case 1 -> 1 - xCheck;
            case 2 -> 1 - zCheck;
            default -> xCheck;
        };
        return row < 0 || across < 0 || across > 1 ? -1 : row + across;
    }

    /** whatSlot for a hit inside the block. */
    public static int whatSlot(int angle, float hitX, float hitY, float hitZ) {
        return whatSlot(angle, (int) (hitX * 2.0F), (int) (hitY * 2.0F), (int) (hitZ * 2.0F));
    }

    @Override
    protected InteractionResult useItemOn(ItemStack held, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof ShelfBlockEntity be) {
            this.activate(level, pos, state, player, held, be, hit);
        }
        return InteractionResult.SUCCESS;
    }

    /** onBlockActivated (server). */
    private void activate(Level level, BlockPos pos, BlockState state, Player player, ItemStack held, ShelfBlockEntity be, BlockHitResult hit) {
        if (player.isShiftKeyDown()) {
            this.openGui(player, be);
            return;
        }
        int angle = angle(state);
        int slot = whatSlot(angle, (float) (hit.getLocation().x - pos.getX()), (float) (hit.getLocation().y - pos.getY()),
                (float) (hit.getLocation().z - pos.getZ()));
        if (!held.isEmpty() && slot >= 0) {
            if (be.addStuff(slot, held)) {
                player.getInventory().setItem(player.getInventory().getSelectedSlot(), ItemStack.EMPTY);
                return;
            }
            Shelves.dropInFront(level, pos, angle, be.getItem(slot));
            be.removeStuff(slot);
            return;
        }
        boolean dropped = false;
        if (slot >= 0) {
            dropped = Shelves.dropInFront(level, pos, angle, be.getItem(slot));
            be.removeStuff(slot);
        }
        if (!dropped) {
            this.openGui(player, be);
        }
    }

    private void openGui(Player player, ShelfBlockEntity be) {
        if (player instanceof ServerPlayer sp) {
            BiblioMenu.open(sp, be, Component.literal("Shelf"), ShelfMenu::new);
        }
    }

    /** onBlockPlacedBy: also notes whether a block stands on top. */
    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        this.updateTop(level, pos);
    }

    /** onNeighborBlockChange: a block above (any id but 0) shows the upper centre board. */
    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, @Nullable Orientation orientation, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, block, orientation, movedByPiston);
        this.updateTop(level, pos);
    }

    private void updateTop(Level level, BlockPos pos) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof ShelfBlockEntity be) {
            be.setTop(!level.getBlockState(pos.above()).isAir());
        }
    }
}
