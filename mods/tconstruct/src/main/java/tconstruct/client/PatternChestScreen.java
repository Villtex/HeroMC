package tconstruct.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import tconstruct.TConstruct;
import tconstruct.menu.PatternChestMenu;

/** {@code PatternChestGui}: 194 x 168. */
public class PatternChestScreen extends AbstractContainerScreen<PatternChestMenu> {
    private static final Identifier TEXTURE = TConstruct.id("textures/gui/patternchest.png");

    public PatternChestScreen(PatternChestMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 194, 168);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(g, mouseX, mouseY, partialTick);
        g.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, 256, 256);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        g.text(this.font, this.title, 60, 6, 0xFF404040, false);
        g.text(this.font, Component.translatable("container.inventory"), 17, this.imageHeight - 96 + 2, 0xFF404040, false);
    }
}
