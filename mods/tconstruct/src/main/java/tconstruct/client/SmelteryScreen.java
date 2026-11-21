package tconstruct.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jspecify.annotations.Nullable;
import tconstruct.TConstruct;
import tconstruct.smeltery.SmelteryBlockEntity;
import tconstruct.smeltery.SmelteryMenu;

import java.lang.reflect.Field;
import java.util.Optional;

/**
 * SmelteryGui: the slots of the smeltery on a side panel (scrolled when there are more than two layers, a heat bar next
 * to each slot), the molten metal stacked bottom up in the middle and the lava of the active tank on the right. Client
 * check open.
 */
public class SmelteryScreen extends AbstractContainerScreen<SmelteryMenu> {
    private static final Identifier TEXTURE = TConstruct.id("textures/gui/smeltery.png");
    private static final Identifier SIDE = TConstruct.id("textures/gui/smelteryside.png");
    private static @Nullable Field slotY;
    private boolean isScrolling;
    private float currentScroll;
    private int slotPos;

    public SmelteryScreen(SmelteryMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 248, 166);
    }

    private @Nullable SmelteryBlockEntity smeltery() {
        return this.minecraft.level != null && this.minecraft.level.getBlockEntity(this.menu.pos) instanceof SmelteryBlockEntity s ? s : null;
    }

    private int layers() {
        return this.menu.size / 9;
    }

    @Override
    protected void init() {
        super.init();
        this.placeSlots();
    }

    /** updateRows: the visible rows move to the top (only the client draws them there). */
    private void placeSlots() {
        try {
            if (slotY == null) {
                slotY = Slot.class.getDeclaredField("y");
                slotY.setAccessible(true);
            }
            for (int i = 0; i < this.menu.size; i++) {
                slotY.setInt(this.menu.slots.get(i), 8 + 18 * (i / 3 - this.menu.slotRow));
            }
        } catch (ReflectiveOperationException e) {
            com.mojang.logging.LogUtils.getLogger().warn("cannot move the slots of the smeltery", e);
        }
    }

    private void scroll(double mouseY) {
        int yScroll = this.topPos + 8;
        int scrollHeight = yScroll + 144;
        this.currentScroll = Math.clamp(((float) mouseY - yScroll - 7.5F) / (scrollHeight - yScroll - 15.0F), 0.0F, 1.0F);
        int s = this.menu.scrollTo(this.currentScroll);
        if (s != -1) {
            this.slotPos = s;
            this.placeSlots();
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        int xScroll = this.leftPos + 67;
        int yScroll = this.topPos + 8;
        if (this.layers() > 2 && event.button() == 0 && event.x() >= xScroll && event.y() >= yScroll && event.x() < xScroll + 14
                && event.y() < yScroll + 144) {
            this.isScrolling = true;
            this.scroll(event.y());
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
        if (this.isScrolling) {
            this.scroll(event.y());
            return true;
        }
        return super.mouseDragged(event, dx, dy);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        this.isScrolling = false;
        return super.mouseReleased(event);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(g, mouseX, mouseY, partialTick);
        int cornerX = this.leftPos + 36;
        int cornerY = this.topPos;
        g.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, cornerX + 46, cornerY, 0, 0, 176, this.imageHeight, 256, 256);
        int fuelGague = this.menu.fuelGague();
        if (fuelGague > 0) {
            TextureAtlasSprite lava = LiquidRender.still(Fluids.LAVA);
            int fuel = Math.max(1, fuelGague * 52 / 52);
            for (int count = 0; fuel > 0; count++) {
                int size = Math.min(16, fuel);
                fuel -= size;
                g.blitSprite(RenderPipelines.GUI_TEXTURED, lava, cornerX + 117, cornerY + 68 - size - 16 * count, 12, size);
            }
        }
        SmelteryBlockEntity smeltery = this.smeltery();
        if (smeltery != null && smeltery.getCapacity() > 0) {
            int base = 0;
            int liquidLayers = (smeltery.getTotalLiquid() / 20000 + 1) * 20000;
            for (FluidStack liquid : smeltery.moltenMetal) {
                TextureAtlasSprite sprite = LiquidRender.still(liquid.getFluid());
                int liquidSize = Math.max(1, liquid.getAmount() * 52 / liquidLayers);
                while (liquidSize > 0) {
                    int size = Math.min(16, liquidSize);
                    int y = cornerY + 68 - size - base;
                    g.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, cornerX + 54, y, 16, size);
                    g.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, cornerX + 70, y, 16, size);
                    g.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, cornerX + 86, y, 16, size);
                    g.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, cornerX + 102, y, 4, size);
                    liquidSize -= size;
                    base += size;
                }
            }
        }
        g.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, cornerX + 54, cornerY + 16, 176, 76, 52, 52, 256, 256);
        int layers = this.layers();
        if (layers > 0) {
            if (layers == 1) {
                g.blit(RenderPipelines.GUI_TEXTURED, SIDE, cornerX - 46, cornerY, 0, 0, 98, 43, 256, 256);
                g.blit(RenderPipelines.GUI_TEXTURED, SIDE, cornerX - 46, cornerY + 43, 0, 133, 98, 25, 256, 256);
            } else if (layers == 2) {
                g.blit(RenderPipelines.GUI_TEXTURED, SIDE, cornerX - 46, cornerY, 0, 0, 98, 61, 256, 256);
                g.blit(RenderPipelines.GUI_TEXTURED, SIDE, cornerX - 46, cornerY + 61, 0, 97, 98, 61, 256, 256);
            } else {
                g.blit(RenderPipelines.GUI_TEXTURED, SIDE, cornerX - 46, cornerY, 0, 0, 98, this.imageHeight - 8, 256, 256);
            }
            g.blit(RenderPipelines.GUI_TEXTURED, SIDE, cornerX + 32, (int) (cornerY + 8 + 127.0F * this.currentScroll), 98, 0, 12, 15, 256, 256);
        }
        int slotSize = Math.min(24, layers * 9);
        for (int iter = 0; iter < slotSize; iter++) {
            int slot = iter + this.slotPos * 3;
            if (slot >= this.menu.size) {
                break;
            }
            int slotTemp = this.menu.activeTemp(slot) - 20;
            int maxTemp = this.menu.meltingTemp(slot) - 20;
            if (slotTemp > 0 && maxTemp > 0) {
                int size = 16 * slotTemp / maxTemp + 1;
                g.blit(RenderPipelines.GUI_TEXTURED, SIDE, cornerX - 38 + iter % 3 * 22, cornerY + 8 + iter / 3 * 18 + 16 - size, 98, 31 - size, 5, size,
                        256, 256);
            }
        }
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        g.text(this.font, this.title, 86, 5, 0xFF404040, false);
        g.text(this.font, Component.translatable("container.inventory"), 90, this.imageHeight - 96 + 2, 0xFF404040, false);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(g, mouseX, mouseY, partialTick);
        int cornerX = this.leftPos + 36;
        int cornerY = this.topPos;
        SmelteryBlockEntity smeltery = this.smeltery();
        if (smeltery != null && smeltery.getCapacity() > 0) {
            int base = 0;
            int liquidLayers = (smeltery.getTotalLiquid() / 20000 + 1) * 20000;
            for (FluidStack liquid : smeltery.moltenMetal) {
                int liquidSize = Math.max(1, liquid.getAmount() * 52 / liquidLayers);
                base += liquidSize;
                int leftX = cornerX + 54;
                int topY = cornerY + 68 - base;
                if (mouseX >= leftX && mouseX <= leftX + 52 && mouseY >= topY && mouseY < topY + liquidSize) {
                    g.setTooltipForNextFrame(this.font, LiquidRender.tooltip(liquid), Optional.empty(), mouseX, mouseY);
                }
            }
        }
        int fuelGague = this.menu.fuelGague();
        if (fuelGague > 0) {
            int size = Math.max(1, fuelGague * 52 / 52);
            int leftX = cornerX + 117;
            int topY = cornerY + 68 - size;
            if (mouseX >= leftX && mouseX <= leftX + 12 && mouseY >= topY && mouseY < topY + size) {
                g.setTooltipForNextFrame(this.font, LiquidRender.tooltip(new FluidStack(Fluids.LAVA, this.menu.fuelAmount())), Optional.empty(), mouseX,
                        mouseY);
            }
        }
    }
}
