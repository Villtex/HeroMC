package invtweaks.client.gui;

import invtweaks.client.InvTweaks;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.InputWithModifiers;

/** InvTweaksGuiSortingButton: sorts the chest with one algorithm; "h" rows, "v" columns, "s" default. */
public final class InvTweaksGuiSortingButton extends InvTweaksGuiIconButton {
    private final AbstractContainerScreen<?> screen;
    private final String icon;
    private final int algorithm;

    public InvTweaksGuiSortingButton(AbstractContainerScreen<?> screen, int offsetX, int offsetY, int width, int height, String icon,
            String tooltip, int algorithm) {
        super(screen, offsetX, offsetY, width, height, icon, tooltip);
        this.screen = screen;
        this.icon = icon;
        this.algorithm = algorithm;
    }

    @Override
    public void onPress(InputWithModifiers input) {
        if (InvTweaks.getInstance() != null) {
            InvTweaks.getInstance().sortChest(this.screen, this.algorithm);
        }
    }

    @Override
    protected void drawIcon(GuiGraphicsExtractor g, int color) {
        int x = this.getX();
        int y = this.getY();
        int w = this.getWidth();
        int h = this.getHeight();
        if (this.icon.equals("h")) {
            g.fill(x + 3, y + 3, x + w - 3, y + 4, color);
            g.fill(x + 3, y + 6, x + w - 3, y + 7, color);
        } else if (this.icon.equals("v")) {
            g.fill(x + 3, y + 3, x + 4, y + h - 3, color);
            g.fill(x + 6, y + 3, x + 7, y + h - 3, color);
        } else {
            g.fill(x + 3, y + 3, x + w - 3, y + 4, color);
            g.fill(x + 5, y + 4, x + 6, y + 5, color);
            g.fill(x + 4, y + 5, x + 5, y + 6, color);
            g.fill(x + 3, y + 6, x + w - 3, y + 7, color);
        }
    }
}
