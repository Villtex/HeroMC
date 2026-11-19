package bibliocraft.printing;

import bibliocraft.common.BiblioBlockEntity;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * TileEntityTypeMachine: slot 0 the book or plate on the left (a signed book to save, an enchanted book to make an
 * enchanted plate, or an old plate to melt back into a chase), slot 1 the stack of chases, slot 2 the new plate.
 * It keeps the chosen book ("SavedBookName", "Select a book." at first), whether the book in slot 0 is saved, the
 * level cost of the enchanted book and the list of saved books (names joined with "_") that the GUI shows.
 */
public class TypesettingBlockEntity extends BiblioBlockEntity {
    private String nameofbook = "Select a book.";
    private boolean bookIsSaved;
    private String listofbooks = "";
    private int requiredlevels;

    public TypesettingBlockEntity(BlockPos pos, BlockState state) {
        super(Presses.TYPESETTING_ENTITY.get(), pos, state, 3);
    }

    private MinecraftServer server() {
        return this.level == null ? null : this.level.getServer();
    }

    /** booklistset: the server's current list of saved books. */
    public void booklistset() {
        MinecraftServer server = this.server();
        if (server != null) {
            this.setBookList(BookFiles.listString(server));
        }
    }

    public void setBookList(String books) {
        if (!books.equals(this.listofbooks)) {
            this.listofbooks = books;
            this.sync();
        }
    }

    /** getbookList: the list split at "_" (an empty list gives one empty name, as String.split did). */
    public String[] bookList() {
        return this.listofbooks.split("_");
    }

    public String bookname() {
        return this.nameofbook;
    }

    public void setBookname(String name) {
        this.nameofbook = name;
        this.sync();
    }

    public boolean hasSavedBook() {
        return this.bookIsSaved;
    }

    public int levels() {
        return this.requiredlevels;
    }

    public boolean signedBookCheck() {
        return this.getItem(0).is(Items.WRITTEN_BOOK);
    }

    public boolean enchantedBookCheck() {
        return this.getItem(0).is(Items.ENCHANTED_BOOK);
    }

    public boolean chaseCheck() {
        return !this.getItem(1).isEmpty();
    }

    /** getChaseNum: 1 per 16 chases (1..16 -> 1, ..., 49+ -> 4). */
    public int chaseNum() {
        if (!this.chaseCheck()) {
            return 0;
        }
        int size = this.getItem(1).getCount();
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

    public boolean hasNewPlate() {
        return !this.getItem(2).isEmpty();
    }

    public boolean hasEnchantedPlate() {
        return this.getItem(2).getItem() instanceof EnchantedPlateItem;
    }

    public boolean hasOldPlate() {
        return this.getItem(0).getItem() instanceof PlateItem;
    }

    /** hasLowerChase: a chase in slot 0 (only resetPlate puts one there, when the chase stack is full). */
    public boolean hasLowerChase() {
        return this.getItem(0).getItem() instanceof ChaseItem;
    }

    /** addChase: onto the chase stack (up to 64); returns how many are left over. */
    public int addChase(ItemStack chaseStack) {
        ItemStack stack = this.getItem(1);
        if (stack.isEmpty()) {
            this.setItem(1, chaseStack);
            return 0;
        }
        int total = stack.getCount() + chaseStack.getCount();
        if (total < 65) {
            stack.setCount(total);
            this.sync();
            return 0;
        }
        stack.setCount(64);
        this.sync();
        return total - 64;
    }

    /** The level cost of an enchanted book: per enchantment (level + (5 - max level), for max level 1..4)^2 + 15. */
    public static int levelCost(ItemEnchantments enchantments) {
        int levelcost = 0;
        for (Object2IntMap.Entry<Holder<Enchantment>> e : enchantments.entrySet()) {
            int enchlvl = e.getIntValue();
            switch (e.getKey().value().getMaxLevel()) {
                case 1 -> enchlvl += 4;
                case 2 -> enchlvl += 3;
                case 3 -> enchlvl += 2;
                case 4 -> enchlvl++;
                default -> {
                }
            }
            levelcost = levelcost + enchlvl * enchlvl + 15;
        }
        return levelcost;
    }

    public static ItemEnchantments stored(ItemStack stack) {
        return stack.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);
    }

    /** addBookorPlate: a plate, signed book or enchanted book into the empty slot 0. */
    public boolean addBookorPlate(ItemStack playerstack) {
        if (!this.getItem(0).isEmpty() || playerstack.isEmpty()) {
            return false;
        }
        if (playerstack.getItem() instanceof PlateItem || playerstack.is(Items.WRITTEN_BOOK) || playerstack.is(Items.ENCHANTED_BOOK)) {
            MinecraftServer server = this.server();
            this.bookIsSaved = server != null && BookFiles.isSaved(playerstack, server);
            if (playerstack.is(Items.ENCHANTED_BOOK)) {
                this.requiredlevels = levelCost(stored(playerstack));
            }
            this.setItem(0, playerstack);
            return true;
        }
        return false;
    }

    /**
     * resetPlate: an old plate in slot 0 becomes a chase again, added to the chase stack. With a full stack the new
     * chase lands in slot 0 instead (where it is drawn as the "lower chase").
     */
    public boolean resetPlate() {
        if (!this.hasOldPlate()) {
            return false;
        }
        ItemStack newChase = new ItemStack(Presses.CHASE.get());
        ItemStack chaseStack = this.getItem(1);
        this.setItem(0, ItemStack.EMPTY);
        if (!chaseStack.isEmpty()) {
            if (chaseStack.getCount() < 64) {
                chaseStack.grow(1);
            } else {
                this.setItem(0, newChase);
            }
        } else {
            this.setItem(1, newChase);
        }
        this.changed();
        return true;
    }

    /** saveBook: writes the signed book in slot 0 to its file. */
    public boolean saveBook() {
        ItemStack book = this.getItem(0);
        MinecraftServer server = this.server();
        if (book.is(Items.WRITTEN_BOOK) && server != null) {
            BookFiles.save(book, server);
            this.bookIsSaved = true;
            this.sync();
            return true;
        }
        return false;
    }

    /**
     * enchantPlate: an enchanted book in slot 0, a chase and no new plate: if the player has the levels, the book and
     * one chase become an enchanted plate with the book's enchantments, and the levels are taken.
     */
    public boolean enchantPlate(Player player) {
        ItemStack stack = this.getItem(0);
        if (stack.isEmpty() || !this.chaseCheck() || !this.getItem(2).isEmpty() || !stack.is(Items.ENCHANTED_BOOK)) {
            return false;
        }
        int levelcost = levelCost(stored(stack));
        if (player.experienceLevel < levelcost) {
            return false;
        }
        ItemStack enchplate = new ItemStack(Presses.ENCHANTED_PLATE.get());
        enchplate.set(DataComponents.STORED_ENCHANTMENTS, stored(stack));
        this.items.set(2, enchplate);
        this.getItem(1).shrink(1);
        if (this.getItem(1).isEmpty()) {
            this.items.set(1, ItemStack.EMPTY);
        }
        this.items.set(0, ItemStack.EMPTY);
        player.giveExperienceLevels(-levelcost);
        this.changed();
        return true;
    }

    /** setPlate (redstone): one chase becomes a plate named after the chosen book, if there is no new plate yet. */
    public void setPlate() {
        if (this.chaseCheck() && this.getItem(2).isEmpty()) {
            ItemStack newPlate = new ItemStack(Presses.PLATE.get());
            newPlate.set(Presses.PLATE_BOOK.get(), this.nameofbook);
            this.items.set(2, newPlate);
            this.getItem(1).shrink(1);
            if (this.getItem(1).isEmpty()) {
                this.items.set(1, ItemStack.EMPTY);
            }
            this.changed();
        }
    }

    /** isStackValidForSlot: a plate or signed book in slot 0, chases in slot 1. */
    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        if (slot == 0) {
            return stack.getItem() instanceof PlateItem || stack.is(Items.WRITTEN_BOOK);
        }
        return slot == 1 && stack.getItem() instanceof ChaseItem;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putBoolean("savedbook", this.bookIsSaved);
        output.putString("SavedBookName", this.nameofbook);
        output.putString("ListOfBooks", this.listofbooks);
        output.putInt("levelreq", this.requiredlevels);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.bookIsSaved = input.getBooleanOr("savedbook", false);
        this.nameofbook = input.getStringOr("SavedBookName", "");
        this.listofbooks = input.getStringOr("ListOfBooks", "");
        this.requiredlevels = input.getIntOr("levelreq", 0);
    }
}
