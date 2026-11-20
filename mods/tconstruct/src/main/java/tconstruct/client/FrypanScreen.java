package tconstruct.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import tconstruct.TConstruct;
import tconstruct.menu.FrypanMenu;

/** FrypanGui: the fire below the fuel slot and the cooking arrow at the top right. Client check open. */
public class FrypanScreen extends AbstractContainerScreen<FrypanMenu> {
    private static final Identifier TEXTURE = TConstruct.id("textures/gui/frypan.png");

    public FrypanScreen(FrypanMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 176, 166);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(g, mouseX, mouseY, partialTick);
        int x = this.leftPos;
        int y = this.topPos;
        g.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight, 256, 256);
        int fuel = this.menu.fuelScaled(12);
        if (fuel > 0) {
            g.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x + 27, y + 40 - fuel, 176, 12 - fuel, 14, fuel + 2, 256, 256);
        }
        int progress = this.menu.progressScaled(24);
        g.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x + 140, y + 8, 176, 14, progress, 16, 256, 256);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        g.text(this.font, this.title, 60, 6, 0xFF404040, false);
        g.text(this.font, Component.translatable("container.inventory"), 8, this.imageHeight - 96 + 2, 0xFF404040, false);
    }
}
