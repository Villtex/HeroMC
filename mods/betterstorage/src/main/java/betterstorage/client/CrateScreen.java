package betterstorage.client;

import betterstorage.BetterStorage;
import betterstorage.menu.CrateMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

/** GuiCrate: the crate texture and the fullness bar of the pile (54 pixels at 255) next to the title. */
public class CrateScreen extends BSScreen<CrateMenu> {
    private static final Identifier TEXTURE = BetterStorage.id("textures/gui/crate.png");

    public CrateScreen(CrateMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected Identifier texture() {
        return TEXTURE;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        int width = this.menu.getFullness() * 54 / 255;
        if (width > 0) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, this.leftPos + 115, this.topPos + 7, 176.0F, 0.0F, width, 6, 256, 256);
        }
    }
}
