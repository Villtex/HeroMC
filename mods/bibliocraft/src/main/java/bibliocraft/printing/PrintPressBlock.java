package bibliocraft.printing;

import bibliocraft.common.BiblioBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

/**
 * BlockPrintPress "Printing Press" (the angle was the metadata): a full block. On the top face (by angle) the ink
 * plate takes ink sacs, the plate place a plate, the left front blank books, the left back holds the printed book.
 * A click with something else on the ink, plate or blank book place drops what is there (and starts the press
 * animation if it was not running). The GUI (ContainerPrintPress) was never opened by the original block.
 */
public class PrintPressBlock extends BiblioBlock {
    public PrintPressBlock(Properties properties) {
        super(true, properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PrintPressBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide() ? null : createTickerHelper(type, Presses.PRESS_ENTITY.get(), PrintPressBlockEntity::serverTick);
    }

    /** The zones of the top face: 0 ink, 1 plate, 2 blank books, 3 printed book, -1 none. */
    public static int zone(int angle, double hitx, double hitz) {
        int iCheck = (int) (hitx * 3.0F);
        int iCheck2 = (int) (hitx * 2.0F);
        int kCheck = (int) (hitz * 3.0F);
        int kCheck2 = (int) (hitz * 2.0F);
        switch (angle) {
            case 0:
                if (iCheck == 2 && kCheck == 1) {
                    return 0;
                }
                if (iCheck == 1 && kCheck == 1) {
                    return 1;
                }
                if (iCheck == 0 && kCheck2 == 0) {
                    return 2;
                }
                if (iCheck == 0 && kCheck2 == 1) {
                    return 3;
                }
                break;
            case 1:
                if (iCheck == 1 && kCheck == 2) {
                    return 0;
                }
                if (iCheck == 1 && kCheck == 1) {
                    return 1;
                }
                if (iCheck2 == 1 && kCheck == 0) {
                    return 2;
                }
                if (iCheck2 == 0 && kCheck == 0) {
                    return 3;
                }
                break;
            case 2:
                if (iCheck == 0 && kCheck == 1) {
                    return 0;
                }
                if (iCheck == 1 && kCheck == 1) {
                    return 1;
                }
                if (iCheck == 2 && kCheck2 == 1) {
                    return 2;
                }
                if (iCheck == 2 && kCheck2 == 0) {
                    return 3;
                }
                break;
            case 3:
                if (iCheck == 1 && kCheck == 0) {
                    return 0;
                }
                if (iCheck == 1 && kCheck == 1) {
                    return 1;
                }
                if (iCheck2 == 0 && kCheck == 2) {
                    return 2;
                }
                if (iCheck2 == 1 && kCheck == 2) {
                    return 3;
                }
                break;
            default:
                break;
        }
        return -1;
    }

    @Override
    protected InteractionResult useItemOn(ItemStack held, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide() && hit.getDirection() == Direction.UP && level.getBlockEntity(pos) instanceof PrintPressBlockEntity tile) {
            int zone = zone(angle(state), hit.getLocation().x - pos.getX(), hit.getLocation().z - pos.getZ());
            if (zone == 3) {
                TypesettingBlock.dropSlot(level, pos, tile, 3);
            } else if (zone >= 0) {
                boolean added = switch (zone) {
                    case 0 -> placeInk(player, held, tile);
                    case 1 -> placePlate(player, held, tile);
                    default -> placeBooks(player, held, tile);
                };
                if (!added) {
                    if (!tile.pressAnimation()) {
                        tile.setPressAnimation(true);
                    }
                    TypesettingBlock.dropSlot(level, pos, tile, zone);
                }
            }
        }
        return InteractionResult.SUCCESS;
    }

    private static void setHeld(Player player, ItemStack stack) {
        player.getInventory().setItem(player.getInventory().getSelectedSlot(), stack);
    }

    private static boolean placePlate(Player player, ItemStack plate, PrintPressBlockEntity tile) {
        if ((plate.getItem() instanceof PlateItem || plate.getItem() instanceof EnchantedPlateItem) && tile.addPlate(plate)) {
            setHeld(player, ItemStack.EMPTY);
            return true;
        }
        return false;
    }

    /** placeInk: any dye is tried, only ink sacs go in. */
    private static boolean placeInk(Player player, ItemStack ink, PrintPressBlockEntity tile) {
        if (PrintPressBlockEntity.isDye(ink)) {
            int inkReturn = tile.addInk(ink);
            if (inkReturn == 0) {
                setHeld(player, ItemStack.EMPTY);
                return true;
            }
            if (inkReturn > 0) {
                ink.setCount(inkReturn);
                setHeld(player, ink);
                return true;
            }
        }
        return false;
    }

    private static boolean placeBooks(Player player, ItemStack book, PrintPressBlockEntity tile) {
        if (book.is(Items.BOOK)) {
            int bookReturn = tile.addBlank(book);
            if (bookReturn == 0) {
                setHeld(player, ItemStack.EMPTY);
            } else {
                book.setCount(bookReturn);
                setHeld(player, book);
            }
            return true;
        }
        return false;
    }
}
