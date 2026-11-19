package bibliocraft.printing;

import bibliocraft.common.BiblioBlockEntity;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * TileEntityPrintPress: slot 0 ink (ink sacs), 1 the plate, 2 blank books, 3 the printed book. Works like a furnace:
 * an ink sac burns 200 ticks, a print takes 200 ticks ("CookTime"), only with a plate, blank books and an empty
 * output slot. A plate prints the saved book whose file name contains the plate's book name; an enchanted plate
 * (that matches no file) prints an enchanted book with its enchantments and wears out after three prints.
 *
 * <p>"pressani" (sent to the clients) is false while a print runs; the press and roller animation itself
 * (move1/move2/...) lived in the client copy of the tile entity and is moved by the renderer every frame, so it is
 * kept here as client-only fields and not saved (the original saved the server's never-changed values).
 */
public class PrintPressBlockEntity extends BiblioBlockEntity {
    private int furnaceCookTime;
    private int furnaceBurnTime;
    private int currentItemBurnTime;
    private boolean pressani = true;
    // client-side animation state of TileEntityPrintPressRenderer
    public float move1;
    public boolean movetest1;
    public float move2;
    public boolean movetest2;
    public boolean plateturn;
    public boolean rollerturn = true;

    public PrintPressBlockEntity(BlockPos pos, BlockState state) {
        super(Presses.PRESS_ENTITY.get(), pos, state, 4);
    }

    /** "instanceof ItemDye" of 1.5.2: the dyes, ink sac, cocoa beans, lapis lazuli and bone meal. */
    public static boolean isDye(ItemStack stack) {
        return stack.getItem() instanceof DyeItem || stack.is(Items.INK_SAC) || stack.is(Items.COCOA_BEANS) || stack.is(Items.LAPIS_LAZULI)
                || stack.is(Items.BONE_MEAL);
    }

    /** Dye metadata 0: the ink sac. */
    public static boolean isInk(ItemStack stack) {
        return stack.is(Items.INK_SAC);
    }

    public boolean pressAnimation() {
        return this.pressani;
    }

    public void setPressAnimation(boolean on) {
        this.pressani = on;
    }

    public boolean hasPlate() {
        ItemStack stack = this.getItem(1);
        return stack.getItem() instanceof PlateItem || stack.getItem() instanceof EnchantedPlateItem;
    }

    /** hasEnchantedPlate: an enchanted plate that carries at least one enchantment. */
    public boolean hasEnchantedPlate() {
        ItemStack stack = this.getItem(1);
        return stack.getItem() instanceof EnchantedPlateItem && !EnchantedPlateItem.enchantments(stack).isEmpty();
    }

    public boolean hasEnchantedBook() {
        return this.getItem(3).is(Items.ENCHANTED_BOOK);
    }

    public boolean hasBlank() {
        return !this.getItem(2).isEmpty();
    }

    /** numberBlanks: 1 per 16 blank books. */
    public int numberBlanks() {
        if (!this.hasBlank()) {
            return 0;
        }
        int size = this.getItem(2).getCount();
        if (size > 48) {
            return 4;
        }
        if (size > 32) {
            return 3;
        }
        if (size > 16) {
            return 2;
        }
        return size > 0 ? 1 : 0;
    }

    public boolean hasSigned() {
        return !this.getItem(3).isEmpty();
    }

    public boolean hasInk() {
        return !this.getItem(0).isEmpty();
    }

    /** getinkStackSize: the ink plate texture 1..8 (one step per 8 ink sacs), 0 without ink. */
    public int inkStackSize() {
        int size = this.getItem(0).getCount();
        if (size <= 0) {
            return 0;
        }
        return Math.min(8, size / 8 + 1);
    }

    /**
     * plateName: the plate's book name; for an enchanted plate the name of its first enchantment with the level
     * ("none" without a plate).
     */
    public String plateName() {
        if (!this.hasPlate()) {
            return "none";
        }
        ItemStack stack = this.getItem(1);
        String bookname = PlateItem.bookName(stack);
        if (this.hasEnchantedPlate()) {
            Object2IntMap.Entry<Holder<Enchantment>> first = EnchantedPlateItem.enchantments(stack).entrySet().iterator().next();
            bookname = Enchantment.getFullname(first.getKey(), first.getIntValue()).getString();
        }
        return bookname;
    }

    /** addPlate: a plate or enchanted plate into the empty plate slot. */
    public boolean addPlate(ItemStack plate) {
        if (!this.hasPlate() && (plate.getItem() instanceof PlateItem || plate.getItem() instanceof EnchantedPlateItem)) {
            this.setItem(1, plate);
            return true;
        }
        return false;
    }

    /** addInk: ink sacs only (-1 for other dyes); returns how many are left over. */
    public int addInk(ItemStack ink) {
        if (!isInk(ink)) {
            return -1;
        }
        return this.addTo(0, ink);
    }

    /** addBlank: blank books; returns how many are left over. */
    public int addBlank(ItemStack books) {
        return this.addTo(2, books);
    }

    private int addTo(int slot, ItemStack add) {
        ItemStack in = this.getItem(slot);
        if (in.isEmpty()) {
            this.setItem(slot, add);
            return 0;
        }
        int total = add.getCount() + in.getCount();
        if (total < 65) {
            in.setCount(total);
            this.sync();
            return 0;
        }
        in.setCount(64);
        this.sync();
        return total - 64;
    }

    private static int itemBurnTime(ItemStack stack) {
        return isDye(stack) ? 200 : 0;
    }

    private boolean canSmelt() {
        return this.items.get(3).isEmpty() && this.hasPlate() && !this.items.get(2).isEmpty();
    }

    /** updateEntity (server). */
    public static void serverTick(Level level, BlockPos pos, BlockState state, PrintPressBlockEntity be) {
        be.tick();
    }

    private void tick() {
        boolean wasBurning = this.furnaceBurnTime > 0;
        boolean changed = false;
        if (this.furnaceBurnTime > 0) {
            this.furnaceBurnTime--;
        }
        if (this.furnaceBurnTime == 0 && this.canSmelt()) {
            this.currentItemBurnTime = this.furnaceBurnTime = itemBurnTime(this.items.get(0));
            if (this.furnaceBurnTime > 0) {
                changed = true;
                if (!this.items.get(0).isEmpty()) {
                    this.setPressAnimation(false);
                    this.items.get(0).shrink(1);
                    if (this.items.get(0).isEmpty()) {
                        this.items.set(0, ItemStack.EMPTY);
                    }
                    this.changed();
                }
            }
        }
        if (this.furnaceBurnTime > 0 && this.canSmelt()) {
            this.furnaceCookTime++;
            if (this.furnaceCookTime == 200) {
                this.furnaceCookTime = 0;
                this.smeltItem();
                changed = true;
            }
        } else {
            this.furnaceCookTime = 0;
        }
        if (wasBurning != this.furnaceBurnTime > 0) {
            changed = true;
        }
        if (changed) {
            this.setChanged();
        }
    }

    /** smeltItem: one blank book becomes the printed book. */
    private void smeltItem() {
        MinecraftServer server = this.level == null ? null : this.level.getServer();
        if (!this.canSmelt() || server == null) {
            return;
        }
        int booknum = BookFiles.bookNumber(server, this.plateName());
        if (booknum == -1 && !this.hasEnchantedPlate()) {
            // "This is not a valid book name!"
            this.setPressAnimation(true);
            this.sync();
            return;
        }
        ItemStack newstack = ItemStack.EMPTY;
        if (booknum == -1) {
            ItemStack plate = this.getItem(1);
            ItemEnchantments enchantments = EnchantedPlateItem.enchantments(plate);
            ItemStack book = new ItemStack(Items.ENCHANTED_BOOK);
            book.set(DataComponents.STORED_ENCHANTMENTS, enchantments);
            newstack = book;
            plate.setDamageValue(plate.getDamageValue() + 1);
            if (plate.getDamageValue() > 2) {
                this.items.set(1, ItemStack.EMPTY);
            }
        } else {
            newstack = BookFiles.load(server, booknum);
            if (newstack.isEmpty()) {
                // the original crashed here (copy of null) when the file could not be read
                return;
            }
        }
        this.items.set(3, newstack.copy());
        this.items.get(2).shrink(1);
        if (this.items.get(2).isEmpty()) {
            this.items.set(2, ItemStack.EMPTY);
        }
        this.setPressAnimation(true);
        this.changed();
    }

    /** isStackValidForSlot: ink sacs, plates (not enchanted ones) and blank books (hoppers). */
    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return switch (slot) {
            case 0 -> isInk(stack);
            case 1 -> stack.getItem() instanceof PlateItem;
            case 2 -> stack.is(Items.BOOK);
            default -> false;
        };
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putShort("BurnTime", (short) this.furnaceBurnTime);
        output.putShort("CookTime", (short) this.furnaceCookTime);
        output.putBoolean("pressani", this.pressani);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.furnaceBurnTime = input.getShortOr("BurnTime", (short) 0);
        this.furnaceCookTime = input.getShortOr("CookTime", (short) 0);
        this.currentItemBurnTime = itemBurnTime(this.items.get(0));
        this.pressani = input.getBooleanOr("pressani", true);
    }
}
