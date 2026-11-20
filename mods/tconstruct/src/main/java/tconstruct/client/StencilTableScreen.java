package tconstruct.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import tconstruct.TConstruct;
import tconstruct.menu.StencilTableMenu;

/** {@code StencilTableGui}: the buttons "Next Pattern" and "Previous Pattern" left of the GUI. */
public class StencilTableScreen extends AbstractContainerScreen<StencilTableMenu> {
    private static final Identifier TEXTURE = TConstruct.id("textures/gui/patternshaper.png");

    public StencilTableScreen(StencilTableMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected void init() {
        super.init();
        this.addRenderableWidget(Button.builder(Component.translatable("gui.tconstruct.next_pattern"),
                b -> this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 0)).bounds(this.leftPos - 120, this.topPos, 120, 20).build());
        this.addRenderableWidget(Button.builder(Component.translatable("gui.tconstruct.previous_pattern"),
                b -> this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 1)).bounds(this.leftPos - 120, this.topPos + 20, 120, 20)
                .build());
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(g, mouseX, mouseY, partialTick);
        g.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, 256, 256);
        if (!this.menu.slots.get(0).hasItem()) {
            g.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, this.leftPos + 47, this.topPos + 34, 176, 0, 18, 18, 256, 256);
        }
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        g.text(this.font, this.title, 50, 6, 0xFF404040, false);
        g.text(this.font, Component.translatable("container.inventory"), 8, this.imageHeight - 96 + 2, 0xFF404040, false);
    }
}
