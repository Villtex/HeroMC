package harkenscythe.compat;

import harkenscythe.HarkenScythe;
import harkenscythe.altar.AltarRecipe;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeHolderType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.ItemLike;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * One category per altar: the item in the altar's slot, what it turns into, the cost in souls or blood and, for the
 * augments, the chance of every augment (all its levels together).
 */
public class AltarCategory implements IRecipeCategory<RecipeHolder<AltarRecipe>> {
    public static final IRecipeHolderType<AltarRecipe> SOULS = IRecipeHolderType.create(HarkenScythe.id("altar_of_souls"));
    public static final IRecipeHolderType<AltarRecipe> BLOOD = IRecipeHolderType.create(HarkenScythe.id("altar_of_blood"));
    private static final int GREY = 0xFF404040;
    private final AltarRecipe.Kind kind;
    private final IDrawable icon;
    private final IDrawableStatic arrow;
    private final Component title;

    public AltarCategory(IGuiHelper guiHelper, AltarRecipe.Kind kind, ItemLike altar) {
        this.kind = kind;
        this.icon = guiHelper.createDrawableItemLike(altar);
        this.arrow = guiHelper.getRecipeArrow();
        this.title = Component.translatable(altar.asItem().getDescriptionId());
    }

    @Override
    public IRecipeHolderType<AltarRecipe> getRecipeType() {
        return this.kind == AltarRecipe.Kind.SOUL ? SOULS : BLOOD;
    }

    @Override
    public Component getTitle() {
        return this.title;
    }

    @Override
    public int getWidth() {
        return 160;
    }

    @Override
    public int getHeight() {
        return 24 + 10 * 7;
    }

    @Override
    public IDrawable getIcon() {
        return this.icon;
    }

    /** The items of the recipe, a half-empty one for a refill. */
    static List<ItemStack> inputs(AltarRecipe recipe) {
        List<ItemStack> list = new ArrayList<>();
        recipe.ingredient().items().forEach(item -> {
            ItemStack stack = new ItemStack(item);
            if (recipe.exclude().isPresent() && recipe.exclude().get().test(stack)) {
                return;
            }
            if (recipe.recharge()) {
                stack.setDamageValue(stack.getMaxDamage() / 2);
            }
            list.add(stack);
        });
        return list;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<AltarRecipe> holder, IFocusGroup focuses) {
        AltarRecipe recipe = holder.value();
        List<ItemStack> inputs = inputs(recipe);
        IRecipeSlotBuilder in = builder.addInputSlot(1, 1).setStandardSlotBackground();
        IRecipeSlotBuilder out = builder.addOutputSlot(61, 1).setOutputSlotBackground();
        for (ItemStack stack : inputs) {
            in.add(stack);
            out.add(recipe.preview(stack));
        }
        builder.createFocusLink(in, out);
    }

    @Override
    public void draw(RecipeHolder<AltarRecipe> holder, IRecipeSlotsView slots, GuiGraphicsExtractor graphics, double mouseX, double mouseY) {
        AltarRecipe recipe = holder.value();
        Font font = Minecraft.getInstance().font;
        this.arrow.draw(graphics, 26, 1);
        String ns = "jei." + HarkenScythe.MOD_ID + ".altar.";
        String essence = this.kind.getSerializedName();
        Component cost = recipe.recharge() ? Component.translatable(ns + "recharge." + essence, recipe.cost(), recipe.cost())
                : Component.translatable(ns + "cost." + essence, recipe.cost());
        int y = 24;
        graphics.text(font, cost, 0, y, GREY, false);
        y += 10;
        if (!recipe.isAugment() && !recipe.single()) {
            graphics.text(font, Component.translatable(ns + "per_item"), 0, y, GREY, false);
            y += 10;
        }
        if (!recipe.isAugment() || Minecraft.getInstance().level == null) {
            return;
        }
        graphics.text(font, Component.translatable(ns + "unenchanted"), 0, y, GREY, false);
        y += 10;
        HolderLookup.RegistryLookup<Enchantment> lookup = Minecraft.getInstance().level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        Map<ResourceKey<Enchantment>, double[]> chances = new LinkedHashMap<>();
        for (AltarRecipe.Augment augment : recipe.augments()) {
            double[] c = chances.computeIfAbsent(augment.enchantment(), k -> new double[]{0, Integer.MAX_VALUE, 0});
            c[0] += augment.chance();
            c[1] = Math.min(c[1], augment.level());
            c[2] = Math.max(c[2], augment.level());
        }
        for (Map.Entry<ResourceKey<Enchantment>, double[]> e : chances.entrySet()) {
            Optional<Holder.Reference<Enchantment>> enchantment = lookup.get(e.getKey());
            if (enchantment.isEmpty()) {
                continue;
            }
            double[] c = e.getValue();
            MutableComponent name = enchantment.get().value().description().copy().append(" ")
                    .append(Component.translatable("enchantment.level." + (int) c[1]));
            if (c[2] > c[1]) {
                name.append("-").append(Component.translatable("enchantment.level." + (int) c[2]));
            }
            String pct = c[0] == Math.rint(c[0]) ? String.valueOf((int) c[0]) : String.format("%.2f", c[0]);
            graphics.text(font, Component.translatable(ns + "chance", name, pct), 0, y, GREY, false);
            y += 10;
        }
    }
}
