package tconstruct.client.deco;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import tconstruct.TConstruct;
import tconstruct.machine.DrawbridgeMenu;

import java.util.ArrayList;
import java.util.List;

/**
 * DrawbridgeGui: drawbridge.png and the five placement buttons (DrawbridgeButton: arrows up, right, down, left and the
 * centre; the chosen one is disabled, v + 46 disabled, v + 92 hovered). Client check open.
 */
public class DrawbridgeScreen extends AbstractContainerScreen<DrawbridgeMenu> {
    private static final Identifier TEXTURE = TConstruct.id("textures/gui/drawbridge.png");
    /** id, x, y, u, v, width, height. */
    private static final int[][] BUTTONS = {{0, 131, 18, 176, 0, 21, 22}, {1, 146, 34, 199, 23, 22, 21}, {2, 132, 48, 199, 0, 21, 22},
            {3, 117, 34, 178, 23, 22, 21}, {4, 135, 40, 217, 0, 10, 10}};
    private final List<PlacementButton> buttons = new ArrayList<>();

    public DrawbridgeScreen(DrawbridgeMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 176, 166);
    }

    @Override
    protected void init() {
        super.init();
        this.buttons.clear();
        for (int[] b : BUTTONS) {
            PlacementButton button = new PlacementButton(b);
            this.buttons.add(button);
            this.addRenderableWidget(button);
        }
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(g, mouseX, mouseY, partialTick);
        for (PlacementButton button : this.buttons) {
            button.active = button.spec[0] != this.menu.placement();
        }
        g.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, 256, 256);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        g.text(this.font, this.title, 8, 6, 0xFF404040, false);
        g.text(this.font, Component.translatable("container.inventory"), 8, this.imageHeight - 96 + 2, 0xFF404040, false);
    }

    private class PlacementButton extends AbstractButton {
        final int[] spec;

        PlacementButton(int[] spec) {
            super(DrawbridgeScreen.this.leftPos + spec[1], DrawbridgeScreen.this.topPos + spec[2], spec[5], spec[6], Component.empty());
            this.spec = spec;
        }

        @Override
        public void onPress(InputWithModifiers input) {
            DrawbridgeScreen.this.minecraft.gameMode.handleInventoryButtonClick(DrawbridgeScreen.this.menu.containerId, this.spec[0]);
        }

        @Override
        protected void extractContents(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
            int offset = !this.active ? 46 : this.isHoveredOrFocused() ? 92 : 0;
            g.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, this.getX(), this.getY(), this.spec[3], this.spec[4] + offset, this.width, this.height, 256, 256);
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput output) {
            this.defaultButtonNarrationText(output);
        }
    }
}
