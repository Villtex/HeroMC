package tconstruct.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import tconstruct.smeltery.TFluids;

import java.util.ArrayList;
import java.util.List;

/** The sprites of liquids (their fluid models) and the liquid tooltip of the smeltery GUI (SmelteryGui.getLiquidTooltip). */
public final class LiquidRender {
    private LiquidRender() {
    }

    public static TextureAtlasSprite still(Fluid fluid) {
        return Minecraft.getInstance().getModelManager().getFluidStateModelSet().get(fluid.defaultFluidState()).stillMaterial().sprite();
    }

    public static TextureAtlasSprite flowing(Fluid fluid) {
        return Minecraft.getInstance().getModelManager().getFluidStateModelSet().get(fluid.defaultFluidState()).flowingMaterial().sprite();
    }

    public static List<Component> tooltip(FluidStack liquid) {
        List<Component> list = new ArrayList<>();
        int amount = liquid.getAmount();
        if (liquid.getFluid() == Fluids.LAVA) {
            list.add(Component.translatable("gui.tconstruct.smeltery.fuel"));
            if (amount > 0) {
                list.add(grey("mb", amount));
            }
            return list;
        }
        list.add(liquid.getHoverName());
        TFluids.Liquid kind = TFluids.of(liquid.getFluid());
        int index = kind == null ? -1 : kind.index();
        if (index == TFluids.EMERALD) {
            list.add(grey("emeralds", amount / 320.0F));
        } else if (index >= 0 && index <= TFluids.GLASS || index == TFluids.ENDER) {
            if (amount / 144 > 0) {
                list.add(grey("ingots", amount / 144));
            }
            int mb = amount % 144;
            if (mb > 0) {
                if (mb % 72 == 0) {
                    list.add(grey("chunks", mb / 72));
                } else if (mb % 16 == 0) {
                    list.add(grey("nuggets", mb / 16));
                } else {
                    list.add(grey("mb", mb));
                }
            }
        } else if (index == TFluids.STONE) {
            if (amount / 144 > 0) {
                list.add(grey("blocks", amount / 144));
            }
            if (amount % 144 > 0) {
                list.add(grey("mb", amount % 144));
            }
        } else {
            list.add(grey("mb", amount));
        }
        return list;
    }

    private static Component grey(String key, Object value) {
        return Component.translatable("gui.tconstruct.smeltery." + key, value).withStyle(net.minecraft.ChatFormatting.GRAY);
    }
}
