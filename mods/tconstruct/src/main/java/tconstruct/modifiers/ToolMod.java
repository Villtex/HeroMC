package tconstruct.modifiers;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;
import tconstruct.tools.InfiTool;
import tconstruct.tools.ToolType;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * {@code library.tools.ToolMod}: a modifier the tool station / tool forge applies when its items lie in the slots next
 * to the tool. {@link #matches} wants exactly these items (one per slot, in any order); {@link #modify} changes the tool.
 * Only the applying is ported here (part 25a); what the modifiers do while the tool is used follows in 25b.
 */
public abstract class ToolMod {
    /** The key of the tag of 1.5.2 ("Redstone", "Lapis", ...), shown in the JEI category. */
    public final String key;
    /** The items, each a predicate (an item, or item with damage of 1.5.2). */
    public final List<Supplier<Ingredient>> stacks;
    /** The render index of the effect icon (Effect1..6), see TProxyClient.effectTypes. */
    public final int effectIndex;

    /** One required item. */
    public record Ingredient(Predicate<ItemStack> test, Supplier<List<ItemStack>> examples) {
        public static Ingredient of(Supplier<? extends Item> item) {
            return new Ingredient(stack -> stack.is(item.get()), () -> List.of(new ItemStack(item.get())));
        }
    }

    protected ToolMod(List<Supplier<Ingredient>> items, int effect, String key) {
        this.stacks = items;
        this.effectIndex = effect;
        this.key = key;
    }

    /** ToolMod.matches: every non-empty input slot takes one required item, and every required item is used. */
    public boolean matches(ItemStack[] input, InfiTool tool, ToolType type) {
        if (!this.canModify(tool, type, input)) {
            return false;
        }
        List<Ingredient> list = new ArrayList<>();
        for (Supplier<Ingredient> s : this.stacks) {
            list.add(s.get());
        }
        for (ItemStack stack : input) {
            if (stack.isEmpty()) {
                continue;
            }
            boolean found = false;
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).test().test(stack)) {
                    list.remove(i);
                    found = true;
                    break;
                }
            }
            if (!found) {
                return false;
            }
        }
        return list.isEmpty();
    }

    protected boolean canModify(InfiTool tool, ToolType type, ItemStack[] input) {
        return tool.modifiers > 0;
    }

    /**
     * Changes the tool. {@code stack} is the tool item (for enchantments), {@code registries} null where the
     * enchantments cannot be looked up (they are then left out).
     */
    public abstract void modify(ItemStack[] input, ItemStack stack, InfiTool tool, ToolType type, HolderLookup.@Nullable Provider registries);

    /** addMatchingEffect: the next free effect layer gets this modifier's icon, once per modifier. */
    public void addMatchingEffect(InfiTool tool) {
        if (tool.effects.size() < 6 && !tool.has(this.key)) {
            tool.effects.add(this.effectIndex);
        }
    }

    /** addModifierTip: a new tooltip line with an empty tooltip. */
    protected int addModifierTip(InfiTool tool, String modifierTip) {
        tool.tips.add(new String[]{"", modifierTip});
        return tool.tips.size();
    }

    /**
     * addToolTip: a new line, or the existing line of this tooltip raised by one level ("Haste" - "Haste II" ...).
     * Returns the line number (1-based).
     */
    protected int addToolTip(InfiTool tool, String tooltip, String modifierTip) {
        for (int i = 0; i < tool.tips.size(); i++) {
            String[] tip = tool.tips.get(i);
            if (tip[0].contains(tooltip)) {
                tip[0] = properName(tooltip, tip[0]);
                tip[1] = properName(modifierTip, tip[1]);
                return i + 1;
            }
        }
        tool.tips.add(new String[]{tooltip, modifierTip});
        return tool.tips.size();
    }

    protected static String properName(String tooltip, String tag) {
        String[] levels = {"", " II", " III", " IV", " V", " VI", " VII", " VIII", " IX", " X"};
        for (int i = 0; i < levels.length - 1; i++) {
            if (tag.equals(tooltip + levels[i])) {
                return tooltip + levels[i + 1];
            }
        }
        return tooltip + " X+";
    }

    public boolean validType(ToolType type) {
        return true;
    }
}
