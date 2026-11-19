package bibliocraft.common;

import java.util.Arrays;
import java.util.List;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PotionItem;
import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Config: the "general" and "text colors" settings of BiblioCraft.cfg, defaults from the pack (z-hexxit-configs).
 * Block and item ids, the enchantment id and IsOptifineInstalled have no meaning any more.
 *
 * <p>The keyword tests of the original compared against {@code ItemStack.toString()} ("1xitem.book@0", block items
 * "1xtile.stone@0") or {@code Item.toString()} (the class name). Here {@link #codeName} stands for both: description id
 * and class name of the item, so "item.minecraft.writable_book|net.minecraft.world.item.writablebookitem".
 */
public final class BCConfig {
    private static final ModConfigSpec.Builder B = new ModConfigSpec.Builder();
    private static final String BOOKS = "book, map, journal, plan, thaumonomicon, necronomicon, lexicon, print, notes, spell, library, tome, encyclopedia";
    private static final String TOOLS = "sprayer, wand, rod, scepter, wrench, screwdriver, meter, handsaw, gun, cutter, scoop, soldering, painter, reader, "
            + "shovel, grafter, pickaxe, pipette, magnifying, sword, axe, hammer";
    private static final String POTIONS = "essence, mead, bottle, test, element, molecule, can, capsule, cell, catalyst, ambrosia, honey pot, dissipation, "
            + "vial, juice";
    static {
        B.push("general");
    }
    private static final ModConfigSpec.ConfigValue<String> ADDITIONAL_POTIONS = B
            .comment("These are keywords that add additional support for more types of potions and items. Add more keyworks if you wish to allow more types of items to be displayed.")
            .define("AdditionalPotions", POTIONS);
    private static final ModConfigSpec.ConfigValue<String> ADDITIONAL_TOOLS = B
            .comment("These are the names of additional tools that can be added to the Tool Rack. Added keywords will allow additional items to be placed on this block.")
            .define("AdditionalTools", TOOLS);
    private static final ModConfigSpec.BooleanValue ALLOW_EMPTY_BOOKS = B
            .comment("Should blank books be allowed on Bookcases? (true or false)").define("AllowEmptyBooks", true);
    private static final ModConfigSpec.ConfigValue<String> ALLOWED_BOOKS = B
            .comment("These are the keywords that are compared against the item names to determine if the item can be placed on a Bookcase. Add more keywords if needed.")
            .define("AllowedBooks", BOOKS);
    private static final ModConfigSpec.BooleanValue TEXT_SHADOW = B
            .comment("Setting to true renders a shadow behind the text seen with the reading glasses.").define("RenderTextShadow", false);
    static {
        B.pop();
        B.push("text colors");
    }
    private static final ModConfigSpec.IntValue TEXT_COLOR = B
            .comment("Color of the text rendered on blocks when using the reading glasses, a combined RGB value (16777215 = white).")
            .defineInRange("ReadingGlassesTextColor", 16777215, 0, 0xFFFFFF);
    static {
        B.pop();
    }
    public static final ModConfigSpec SPEC = B.build();

    private BCConfig() {
    }

    public static boolean allowEmptyBooks() {
        return SPEC.isLoaded() ? ALLOW_EMPTY_BOOKS.get() : true;
    }

    public static boolean textShadow() {
        return SPEC.isLoaded() && TEXT_SHADOW.get();
    }

    /** Config.color, as ARGB. */
    public static int textColor() {
        return 0xFF000000 | (SPEC.isLoaded() ? TEXT_COLOR.get() : 0xFFFFFF);
    }

    public static List<String> books() {
        return split(SPEC.isLoaded() ? ALLOWED_BOOKS.get() : BOOKS);
    }

    public static List<String> tools() {
        return split(SPEC.isLoaded() ? ADDITIONAL_TOOLS.get() : TOOLS);
    }

    public static List<String> potions() {
        return split(SPEC.isLoaded() ? ADDITIONAL_POTIONS.get() : POTIONS);
    }

    private static List<String> split(String s) {
        return Arrays.stream(s.split(",")).map(k -> k.trim().toLowerCase()).toList();
    }

    /** Stand-in for ItemStack.toString() / Item.toString() of 1.5.2, see the class comment. Lower case. */
    public static String codeName(ItemStack stack) {
        return (stack.getItem().getDescriptionId() + "|" + stack.getItem().getClass().getName()).toLowerCase();
    }

    public static String displayName(ItemStack stack) {
        return stack.getHoverName().getString();
    }

    /** Config.isBlock: the stack's name has no "item" in it (block items were "tile.*"). */
    public static boolean isBlock(ItemStack stack) {
        return stack.getItem() instanceof BlockItem && !stack.getItem().getDescriptionId().contains("item");
    }

    /** Config.testBookValidity(stack.toString(), displayName) as the bookcase and the desk call it. */
    public static boolean isBook(ItemStack stack) {
        String testName = codeName(stack);
        String display = displayName(stack).trim().toLowerCase();
        boolean match = false;
        for (String k : books()) {
            if (testName.contains(k) || display.contains(k)) {
                if (!testName.contains("shelf") && !testName.contains("case") && !testName.contains("stand")) {
                    match = true;
                }
                break;
            }
        }
        if (!allowEmptyBooks() && stack.is(Items.BOOK)) {
            match = false;
        }
        // "ub@", "vv@", "vw@": the obfuscated ItemBook, ItemWritableBook and ItemEditableBook
        if (stack.is(Items.WRITABLE_BOOK) || stack.is(Items.WRITTEN_BOOK) || (stack.is(Items.BOOK) && allowEmptyBooks())
                || testName.contains("manual") || testName.contains("catalogue") || testName.contains("railcraft.routing.table")) {
            match = true;
        }
        return match;
    }

    /** SlotBook.isItemValid / TileEntityBookcase.isStackValidForSlot: a book that is no block item. */
    public static boolean isShelvableBook(ItemStack stack) {
        return !stack.isEmpty() && !isBlock(stack) && isBook(stack);
    }

    /** Config.testPotionValidity(name, displayName, item): a potion item, or a keyword in name or display name. */
    public static boolean isPotion(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        String testName = codeName(stack);
        String display = displayName(stack).trim().toLowerCase();
        if (stack.getItem() instanceof PotionItem || testName.contains("potion")) {
            return true;
        }
        for (String k : potions()) {
            if (testName.contains(k) || display.contains(k)) {
                return true;
            }
        }
        return false;
    }

    /** Config.testToolValidity(toolName, toolcodeName): a keyword in {@code name}, or a tool class in {@code codeName}. */
    public static boolean testToolValidity(String name, String codeName) {
        String testName = name.trim().toLowerCase();
        String code = codeName.toLowerCase();
        for (String k : tools()) {
            if (testName.contains(k)) {
                return true;
            }
        }
        return code.contains("tconstruct.items.tools") || code.contains("item.tool") || code.contains("hammer") || code.contains("wrench");
    }
}
