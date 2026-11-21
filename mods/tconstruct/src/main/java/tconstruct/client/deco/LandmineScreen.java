package tconstruct.client.deco;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import tconstruct.TConstruct;
import tconstruct.landmine.LandmineMenu;

/** GuiLandmine: landmine.png, the name (or the item's custom name) and the inventory label. Client check open. */
public class LandmineScreen extends AbstractContainerScreen<LandmineMenu> {
    private static final Identifier TEXTURE = TConstruct.id("textures/gui/landmine.png");

    public LandmineScreen(LandmineMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 176, 166);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(g, mouseX, mouseY, partialTick);
        g.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, 256, 256);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        g.text(this.font, this.title, 8, 5, 0xFF404040, false);
        g.text(this.font, Component.translatable("container.inventory"), 8, this.imageHeight - 96 + 3, 0xFF404040, false);
    }
}
