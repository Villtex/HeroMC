package tconstruct.recipe;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.NonNullList;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import tconstruct.tools.ToolCoreItem;
import tconstruct.tools.ToolType;

/**
 * Detailing.addShapelessToolRecipe (ShapelessToolRecipe): the chisel and one block of a {@link Detailing} step anywhere
 * in the grid give the detailed block; the chisel stays and wears by one (Chisel.getContainerItemStack, with the
 * reinforced chance).
 */
public class ChiselRecipe extends CustomRecipe {
    public static final ChiselRecipe INSTANCE = new ChiselRecipe();
    public static final RecipeSerializer<ChiselRecipe> SERIALIZER = new RecipeSerializer<>(MapCodec.unit(INSTANCE), StreamCodec.unit(INSTANCE));

    @Override
    public boolean matches(CraftingInput input, Level level) {
        return !assemble(input).isEmpty();
    }

    @Override
    public ItemStack assemble(CraftingInput input) {
        ItemStack chisel = ItemStack.EMPTY;
        ItemStack block = ItemStack.EMPTY;
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) {
                continue;
            }
            if (stack.getItem() instanceof ToolCoreItem tool && tool.type == ToolType.CHISEL && chisel.isEmpty()) {
                chisel = stack;
            } else if (block.isEmpty()) {
                block = stack;
            } else {
                return ItemStack.EMPTY;
            }
        }
        if (chisel.isEmpty() || block.isEmpty()) {
            return ItemStack.EMPTY;
        }
        net.minecraft.world.level.block.state.BlockState out = Detailing.output(block);
        return out == null ? ItemStack.EMPTY : new ItemStack(out.getBlock());
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
        NonNullList<ItemStack> rest = NonNullList.withSize(input.size(), ItemStack.EMPTY);
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.getItem() instanceof ToolCoreItem tool && tool.type == ToolType.CHISEL) {
                ItemStack chisel = stack.copyWithCount(1);
                ToolCoreItem.onBlockChanged(chisel, null);
                rest.set(i, chisel);
                break;
            }
        }
        return rest;
    }

    @Override
    public CraftingBookCategory category() {
        return CraftingBookCategory.BUILDING;
    }

    @Override
    public RecipeSerializer<ChiselRecipe> getSerializer() {
        return SERIALIZER;
    }
}
