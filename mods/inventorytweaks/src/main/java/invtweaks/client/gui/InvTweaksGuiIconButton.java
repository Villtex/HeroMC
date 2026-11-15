package invtweaks.client.gui;

import invtweaks.client.InvTweaksConst;
import java.time.Duration;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;

/**
 * InvTweaksGuiIconButton: a 10x10 button at the top right of a container GUI. Without the optional
 * /gui/button10px.png of a texture pack the original drew the corners of the vanilla button texture; here the vanilla
 * button sprite is drawn at that size. Position is relative to the right edge and the top of the GUI.
 */
public abstract class InvTweaksGuiIconButton extends AbstractButton {
    private final int offsetX;
    private final int offsetY;

    protected InvTweaksGuiIconButton(AbstractContainerScreen<?> screen, int offsetX, int offsetY, int width, int height, String label, String tooltip) {
        super(screen.getLeftPos() + screen.getImageWidth() + offsetX, screen.getTopPos() + offsetY, width, height, Component.literal(label));
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        if (tooltip != null) {
            this.setTooltip(Tooltip.create(Component.literal(tooltip)));
            this.setTooltipDelay(Duration.ofMillis(InvTweaksConst.TOOLTIP_DELAY));
        }
    }

    public void reposition(AbstractContainerScreen<?> screen) {
        this.setX(screen.getLeftPos() + screen.getImageWidth() + this.offsetX);
        this.setY(screen.getTopPos() + this.offsetY);
    }

    /** InvTweaksGuiTooltipButton.getTextColor. */
    protected int getTextColor() {
        if (!this.active) {
            return 0xFFA0A0A0;
        }
        return this.isHoveredOrFocused() ? 0xFFFFFFA0 : 0xFFE0E0E0;
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        this.extractDefaultSprite(graphics);
        this.drawIcon(graphics, this.getTextColor());
    }

    protected abstract void drawIcon(GuiGraphicsExtractor graphics, int color);

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        this.defaultButtonNarrationText(output);
    }
}
