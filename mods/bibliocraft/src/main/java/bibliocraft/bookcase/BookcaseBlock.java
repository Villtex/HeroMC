package bibliocraft.bookcase;

import bibliocraft.common.BCConfig;
import bibliocraft.common.BiblioBlock;
import bibliocraft.common.BiblioMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

/**
 * BlockBookcase: 16 books in two rows of eight. Right click on a book place puts the held book there, sneaking with
 * an empty hand takes it out, everything else opens the GUI. Each book adds 1/8 enchanting power; a Redstone book
 * powers the neighbours (except behind the back) with the number of its slot.
 */
public class BookcaseBlock extends BiblioBlock {
    /** setBlockBoundsBasedOnState: the half of the block at the back. */
    private static final VoxelShape[] SHAPES = rotations(8, 0, 0, 16, 16, 16);
    /** Metadata of the original: 0 oak, 1 spruce, 2 birch, 3 jungle (texture bookcase_empty1..4). */
    public final int wood;

    public BookcaseBlock(int wood, Properties properties) {
        super(true, properties);
        this.wood = wood;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BookcaseBlockEntity(pos, state);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES[angle(state)];
    }

    /** getEnchantPowerBonus: 0.125 per filled slot. */
    @Override
    public float getEnchantPowerBonus(BlockState state, BlockGetter level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof BookcaseBlockEntity be ? 0.125F * be.filledSlots() : 0.0F;
    }

    @Override
    protected boolean isSignalSource(BlockState state) {
        return true;
    }

    /** isProvidingWeakPower: the slot of the redstone book, not to the side behind the back (face 5, 3, 4, 2 by angle). */
    @Override
    protected int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        if (!(level.getBlockEntity(pos) instanceof BookcaseBlockEntity be) || !be.hasRedstoneBook()) {
            return 0;
        }
        int face = direction.get3DDataValue();
        int back = switch (angle(state)) {
            case 0 -> 5;
            case 1 -> 3;
            case 2 -> 4;
            default -> 2;
        };
        return face == back ? 0 : be.redstoneBookSlot();
    }

    @Override
    protected int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return this.getSignal(state, level, pos, direction);
    }

    /**
     * isWhatBook: the book place under the hit (0..7 in a row), with the fall-through of the original switch (angle 1
     * with hitX = 1 falls into the angle 2 case, and on into angle 3).
     */
    public static int whatBook(int angle, double hitX, double hitZ) {
        int xt = (int) (hitX * 8.0);
        int zt = (int) (hitZ * 8.0);
        switch (angle) {
            case 0:
                return zt;
            case 1:
                if (xt >= 0 && xt <= 7) {
                    return 7 - xt;
                }
            case 2:
                if (zt >= 0 && zt <= 7) {
                    return 7 - zt;
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
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof BookcaseBlockEntity be) {
            this.activate(level, pos, state, player, held, be, hit);
        }
        return InteractionResult.SUCCESS;
    }

    /** onBlockActivated (server). */
    private void activate(Level level, BlockPos pos, BlockState state, Player player, ItemStack held, BookcaseBlockEntity be, BlockHitResult hit) {
        double hitX = hit.getLocation().x - pos.getX();
        double hitY = hit.getLocation().y - pos.getY();
        double hitZ = hit.getLocation().z - pos.getZ();
        int yCheck = (int) (hitY * 2.0F);
        int angle = angle(state);
        if (yCheck == 0 || yCheck == 1) {
            int book = whatBook(angle, hitX, hitZ) + (yCheck == 0 ? 8 : 0);
            if (player.isShiftKeyDown()) {
                if (book >= 0 && book < 16) {
                    this.dropBook(level, pos, angle, be, book);
                    be.setBook(book, ItemStack.EMPTY);
                }
                return;
            }
            if (!held.isEmpty()) {
                if (BCConfig.isBlock(held) || !BCConfig.isBook(held)) {
                    this.openGui(player, be);
                    return;
                }
                if (book >= 0 && book < 16 && be.setBook(book, held)) {
                    player.getInventory().setItem(player.getInventory().getSelectedSlot(), ItemStack.EMPTY);
                    return;
                }
            }
        }
        this.openGui(player, be);
    }

    private void openGui(Player player, BookcaseBlockEntity be) {
        if (player instanceof ServerPlayer sp) {
            BiblioMenu.open(sp, be, Component.literal("Bookcase"), BookcaseMenu::new);
        }
    }

    /** dropBook: the book falls out in front, without motion. */
    private void dropBook(Level level, BlockPos pos, int angle, BookcaseBlockEntity be, int slot) {
        ItemStack book = be.getItem(slot);
        if (book.isEmpty()) {
            return;
        }
        float ai = switch (angle) {
            case 0 -> -0.2F;
            case 2 -> 0.2F;
            default -> 0.0F;
        };
        float ak = switch (angle) {
            case 1 -> -0.2F;
            case 3 -> 0.2F;
            default -> 0.0F;
        };
        ItemEntity entity = new ItemEntity(level, pos.getX() + 0.5F + ai, pos.getY() + 0.5F, pos.getZ() + 0.5F + ak, book.copy(), 0.0, 0.0, 0.0);
        level.addFreshEntity(entity);
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, net.minecraft.server.level.ServerLevel level, BlockPos pos, boolean movedByPiston) {
        super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston);
        BookcaseBlockEntity.notifyAround(level, pos, this);
    }

    public static @Nullable BookcaseBlockEntity entity(BlockGetter level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof BookcaseBlockEntity be ? be : null;
    }
}
