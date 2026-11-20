package tconstruct.compat;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * A row of input slots, an arrow and the outputs; used for the part builder, tool building and the modifiers. The
 * slots of one recipe hold aligned lists (the n-th entries belong together) and are focus-linked.
 */
public class TinkerCategory implements IRecipeCategory<TinkerCategory.Row> {
    /** One recipe: the stacks of every input slot and of every output slot, and a note under it. */
    public record Row(List<List<ItemStack>> inputs, List<List<ItemStack>> outputs, @Nullable Component note) {
    }

    private final IRecipeType<Row> type;
    private final Component title;
    private final IDrawable icon;
    private final IDrawableStatic arrow;
    private final int inputs;
    private final int outputs;

    public TinkerCategory(IGuiHelper gui, IRecipeType<Row> type, Component title, ItemLike icon, int inputs, int outputs) {
        this.type = type;
        this.title = title;
        this.icon = gui.createDrawableItemLike(icon);
        this.arrow = gui.getRecipeArrow();
        this.inputs = inputs;
        this.outputs = outputs;
    }

    @Override
    public IRecipeType<Row> getRecipeType() {
        return this.type;
    }

    @Override
    public Component getTitle() {
        return this.title;
    }

    @Override
    public int getWidth() {
        return Math.max(150, this.inputs * 18 + 30 + this.outputs * 18);
    }

    @Override
    public int getHeight() {
        return 32;
    }

    @Override
    public IDrawable getIcon() {
        return this.icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, Row row, IFocusGroup focuses) {
        List<IRecipeSlotBuilder> slots = new ArrayList<>();
        for (int i = 0; i < row.inputs().size(); i++) {
            IRecipeSlotBuilder slot = builder.addInputSlot(1 + i * 18, 1).setStandardSlotBackground();
            slot.addItemStacks(row.inputs().get(i));
            slots.add(slot);
        }
        int x = this.inputs * 18 + 30;
        for (int i = 0; i < row.outputs().size(); i++) {
            IRecipeSlotBuilder slot = builder.addOutputSlot(x + i * 18, 1).setOutputSlotBackground();
            slot.addItemStacks(row.outputs().get(i));
            slots.add(slot);
        }
        boolean aligned = slots.size() > 1;
        int size = -1;
        for (List<ItemStack> list : row.inputs()) {
            aligned &= size < 0 || list.size() == size;
            size = list.size();
        }
        for (List<ItemStack> list : row.outputs()) {
            aligned &= list.size() == size;
        }
        if (aligned && size > 1) {
            builder.createFocusLink(slots);
        }
    }

    @Override
    public void draw(Row row, IRecipeSlotsView slots, GuiGraphicsExtractor graphics, double mouseX, double mouseY) {
        this.arrow.draw(graphics, this.inputs * 18 + 4, 1);
        if (row.note() != null) {
            graphics.text(Minecraft.getInstance().font, row.note(), 0, 22, 0xFF404040, false);
        }
    }
}
