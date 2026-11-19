package backpack.client;

import backpack.inventory.BackpackMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

/**
 * GuiBackpack: /gui/container.png (the large chest, generic_54 now) cut to the backpack's rows, the backpack's name at
 * (8, 6) and "Inventory" at (8, ySize - 96 + 2).
 */
public class BackpackScreen extends AbstractContainerScreen<BackpackMenu> {
    private static final Identifier CONTAINER = Identifier.withDefaultNamespace("textures/gui/container/generic_54.png");

    public BackpackScreen(BackpackMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 176, 114 + menu.rows() * 18);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(g, mouseX, mouseY, partialTick);
        int rows = this.menu.rows();
        g.blit(RenderPipelines.GUI_TEXTURED, CONTAINER, this.leftPos, this.topPos, 0.0F, 0.0F, this.imageWidth, rows * 18 + 17, 256, 256);
        g.blit(RenderPipelines.GUI_TEXTURED, CONTAINER, this.leftPos, this.topPos + rows * 18 + 17, 0.0F, 126.0F, this.imageWidth, 96, 256, 256);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        g.text(this.font, this.title, 8, 6, 0xFF404040, false);
        g.text(this.font, Component.translatable("container.inventory"), 8, this.imageHeight - 96 + 2, 0xFF404040, false);
    }
}
