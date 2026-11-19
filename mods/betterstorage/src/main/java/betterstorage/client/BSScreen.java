package betterstorage.client;

import betterstorage.BetterStorage;
import betterstorage.menu.BSMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

/**
 * GuiBetterStorage: 14 + 18 per column wide, 98 + 18 per row + 14 high. The top (rows) and the bottom (player
 * inventory, from v = 115) of the texture are drawn separately: 9 columns use the vanilla chest texture, more the
 * reinforced chest's 256 x 256 texture.
 */
public class BSScreen<M extends BSMenu> extends AbstractContainerScreen<M> {
    private static final Identifier CONTAINER = Identifier.withDefaultNamespace("textures/gui/container/generic_54.png");
    private static final Identifier REINFORCED = BetterStorage.id("textures/gui/reinforced_chest.png");

    public BSScreen(M menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 14 + menu.columns * 18, 98 + menu.rows * 18 + BSMenu.SEPARATION);
    }

    protected Identifier texture() {
        return this.menu.columns <= 9 ? CONTAINER : REINFORCED;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        int m = 91 + BSMenu.SEPARATION;
        Identifier texture = this.texture();
        graphics.blit(RenderPipelines.GUI_TEXTURED, texture, this.leftPos, this.topPos, 0.0F, 0.0F, this.imageWidth, this.imageHeight - m, 256, 256);
        graphics.blit(RenderPipelines.GUI_TEXTURED, texture, this.leftPos, this.topPos + this.imageHeight - m, 0.0F, 115.0F, this.imageWidth, m, 256, 256);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        graphics.text(this.font, this.title, 8, 6, 0xFF404040, false);
        graphics.text(this.font, Component.translatable("container.inventory"), 8 + (this.imageWidth - 176) / 2, this.imageHeight - 92, 0xFF404040,
                false);
    }
}
