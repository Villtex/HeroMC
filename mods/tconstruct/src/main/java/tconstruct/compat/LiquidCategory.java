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
import net.neoforged.neoforge.fluids.FluidStack;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * The smeltery recipes in JEI: item and liquid inputs, an arrow, item and liquid outputs and a note (temperature,
 * cooling time). Melting, alloying, casting table and basin, drying rack.
 */
public class LiquidCategory implements IRecipeCategory<LiquidCategory.Row> {
    public record Row(List<List<ItemStack>> items, List<FluidStack> liquids, List<List<ItemStack>> results, List<FluidStack> liquidResults,
                      @Nullable Component note) {
    }

    private final IRecipeType<Row> type;
    private final Component title;
    private final IDrawable icon;
    private final IDrawableStatic arrow;
    private final int inputs;

    public LiquidCategory(IGuiHelper gui, IRecipeType<Row> type, Component title, ItemLike icon, int inputs) {
        this.type = type;
        this.title = title;
        this.icon = gui.createDrawableItemLike(icon);
        this.arrow = gui.getRecipeArrow();
        this.inputs = inputs;
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
        return 150;
    }

    @Override
    public int getHeight() {
        return 32;
    }

    @Override
    public IDrawable getIcon() {
        return this.icon;
    }

    private static int capacity(FluidStack liquid) {
        return Math.max(144, liquid.getAmount());
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, Row row, IFocusGroup focuses) {
        int x = 1;
        for (List<ItemStack> items : row.items()) {
            builder.addInputSlot(x, 1).setStandardSlotBackground().addItemStacks(items);
            x += 18;
        }
        for (FluidStack liquid : row.liquids()) {
            IRecipeSlotBuilder slot = builder.addInputSlot(x, 1).setStandardSlotBackground();
            slot.addFluidStack(liquid.getFluid(), liquid.getAmount()).setFluidRenderer(capacity(liquid), false, 16, 16);
            x += 18;
        }
        x = this.inputs * 18 + 30;
        for (List<ItemStack> items : row.results()) {
            builder.addOutputSlot(x, 1).setOutputSlotBackground().addItemStacks(items);
            x += 18;
        }
        for (FluidStack liquid : row.liquidResults()) {
            IRecipeSlotBuilder slot = builder.addOutputSlot(x, 1).setOutputSlotBackground();
            slot.addFluidStack(liquid.getFluid(), liquid.getAmount()).setFluidRenderer(capacity(liquid), false, 16, 16);
            x += 18;
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
