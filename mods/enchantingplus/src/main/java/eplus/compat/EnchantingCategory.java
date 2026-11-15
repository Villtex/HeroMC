package eplus.compat;

import eplus.EnchantTableMenu;
import eplus.EnchantingPlus;
import eplus.EplusConfig;
import java.util.List;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;

/** One page per enchantment: the items it can go on (cycling) and, for the shown item, the price of every level. */
public class EnchantingCategory implements IRecipeCategory<EnchantingCategory.TableRecipe> {
    public record TableRecipe(Holder<Enchantment> enchantment, List<ItemStack> items) {
    }

    public static final IRecipeType<TableRecipe> TYPE = IRecipeType.create(EnchantingPlus.MOD_ID, "enchanting", TableRecipe.class);
    /** All 32 bookshelf places filled. */
    private static final int MAX_SHELVES = 32;

    private final IDrawable icon;

    public EnchantingCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableItemLike(Items.ENCHANTING_TABLE);
    }

    @Override
    public IRecipeType<TableRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.eplus.category");
    }

    @Override
    public int getWidth() {
        return 160;
    }

    @Override
    public int getHeight() {
        return 22 + 10 * 5;
    }

    @Override
    public IDrawable getIcon() {
        return this.icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, TableRecipe recipe, IFocusGroup focuses) {
        var slot = builder.addInputSlot(1, 1).setStandardSlotBackground().setSlotName("item");
        for (ItemStack stack : recipe.items()) {
            slot.add(stack);
        }
    }

    @Override
    public void draw(TableRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphicsExtractor graphics, double mouseX, double mouseY) {
        Font font = Minecraft.getInstance().font;
        graphics.text(font, recipe.enchantment().value().description(), 22, 6, 0xFF404040, false);
        ItemStack stack = recipeSlotsView.findSlotByName("item").flatMap(s -> s.getDisplayedItemStack()).orElse(ItemStack.EMPTY);
        EplusConfig.Settings settings = EplusConfig.server();
        float bookCases = (settings.minimumBook() + MAX_SHELVES) * 2.0F;
        int max = Math.min(5, recipe.enchantment().value().getMaxLevel());
        for (int level = 1; level <= max; level++) {
            int cost = EnchantTableMenu.enchantmentCost(stack, recipe.enchantment(), level, 0, settings, bookCases);
            Component line;
            if (settings.needsBookShelves()) {
                int shelves = Math.max(0, (int) Math.ceil(cost / 2.0 - settings.minimumBook()));
                line = shelves > MAX_SHELVES
                        ? Component.translatable("jei.eplus.cost.impossible", Enchantment.getFullname(recipe.enchantment(), level), cost)
                        : Component.translatable("jei.eplus.cost.shelves", Enchantment.getFullname(recipe.enchantment(), level), cost, shelves);
            } else {
                line = Component.translatable("jei.eplus.cost", Enchantment.getFullname(recipe.enchantment(), level), cost);
            }
            graphics.text(font, line, 0, 22 + 10 * (level - 1), 0xFF404040, false);
        }
    }
}
