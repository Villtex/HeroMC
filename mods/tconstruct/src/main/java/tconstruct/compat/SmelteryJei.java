package tconstruct.compat;

import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import tconstruct.smeltery.SmelteryRecipes;

import java.util.ArrayList;
import java.util.List;

/** The rows of the smeltery categories, straight from {@link SmelteryRecipes}. */
final class SmelteryJei {
    private SmelteryJei() {
    }

    static void register(IRecipeRegistration registration, IRecipeType<LiquidCategory.Row> melting, IRecipeType<LiquidCategory.Row> alloying,
                         IRecipeType<LiquidCategory.Row> table, IRecipeType<LiquidCategory.Row> basin, IRecipeType<LiquidCategory.Row> drying) {
        List<LiquidCategory.Row> rows = new ArrayList<>();
        for (SmelteryRecipes.MeltingEntry entry : SmelteryRecipes.meltingEntries()) {
            if (!entry.inputs().isEmpty()) {
                rows.add(new LiquidCategory.Row(List.of(entry.inputs()), List.of(), List.of(), List.of(entry.melting().result()),
                        Component.translatable("jei.tconstruct.temperature", entry.melting().temperature())));
            }
        }
        registration.addRecipes(melting, rows);
        rows = new ArrayList<>();
        for (SmelteryRecipes.Alloy alloy : SmelteryRecipes.alloys()) {
            rows.add(new LiquidCategory.Row(List.of(), alloy.mixers(), List.of(), List.of(alloy.result()), null));
        }
        registration.addRecipes(alloying, rows);
        registration.addRecipes(table, casting(SmelteryRecipes.table()));
        registration.addRecipes(basin, casting(SmelteryRecipes.basin()));
        rows = new ArrayList<>();
        for (SmelteryRecipes.Drying recipe : SmelteryRecipes.dryingRecipes()) {
            rows.add(new LiquidCategory.Row(List.of(List.of(new ItemStack(recipe.input()))), List.of(), List.of(List.of(recipe.output().get())), List.of(),
                    Component.translatable("jei.tconstruct.time", recipe.time() / 20)));
        }
        registration.addRecipes(drying, rows);
    }

    private static List<LiquidCategory.Row> casting(List<SmelteryRecipes.Casting> recipes) {
        List<LiquidCategory.Row> rows = new ArrayList<>();
        for (SmelteryRecipes.Casting recipe : recipes) {
            List<List<ItemStack>> casts = new ArrayList<>();
            List<ItemStack> castItems = recipe.castItems().get();
            if (!castItems.isEmpty()) {
                casts.add(castItems);
            }
            int amount = recipe.amount();
            if (!castItems.isEmpty() && castItems.get(0).getItem() instanceof tconstruct.items.PatternItem pattern && pattern.cost() > 0) {
                amount = pattern.cost() * 72;
            }
            Component note = Component.translatable("jei.tconstruct.cool", recipe.coolTime() / 20.0F);
            if (recipe.consumeCast() && !castItems.isEmpty()) {
                note = note.copy().append(", ").append(Component.translatable("jei.tconstruct.consumed"));
            }
            rows.add(new LiquidCategory.Row(casts, List.of(new net.neoforged.neoforge.fluids.FluidStack(recipe.fluid(), amount)),
                    List.of(List.of(recipe.result())), List.of(), note));
        }
        return rows;
    }
}
