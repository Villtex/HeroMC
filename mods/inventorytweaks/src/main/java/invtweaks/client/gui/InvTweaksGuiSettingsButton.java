package invtweaks.client.gui;

import invtweaks.client.ContainerSection;
import invtweaks.client.InvTweaks;
import invtweaks.client.InvTweaksConfigManager;
import invtweaks.client.InvTweaksContainerSectionManager;
import invtweaks.client.InvTweaksLocalization;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.InputWithModifiers;

/** InvTweaksGuiSettingsButton: "..." opens the settings over the container GUI. */
public final class InvTweaksGuiSettingsButton extends InvTweaksGuiIconButton {
    private final AbstractContainerScreen<?> screen;

    public InvTweaksGuiSettingsButton(AbstractContainerScreen<?> screen, int offsetX, int offsetY, int width, int height) {
        super(screen, offsetX, offsetY, width, height, "...", InvTweaksLocalization.get("invtweaks.button.settings.tooltip"));
        this.screen = screen;
    }

    @Override
    public void onPress(InputWithModifiers input) {
        Minecraft mc = Minecraft.getInstance();
        InvTweaksConfigManager manager = InvTweaks.getConfigManager();
        try {
            InvTweaksContainerSectionManager container = new InvTweaksContainerSectionManager(mc, ContainerSection.INVENTORY);
            if (!mc.player.containerMenu.getCarried().isEmpty()) {
                for (int i = container.getSize() - 1; i >= 0; i--) {
                    if (container.getItemStack(i) == null) {
                        container.leftClick(i);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            InvTweaks.logInGameErrorStatic("invtweaks.sort.releaseitem.error", e);
        }
        if (manager.makeSureConfigurationIsLoaded()) {
            InvTweaksGuiSettingsAbstract.open(new InvTweaksGuiSettings(this.screen, manager.getConfig()));
        }
    }

    @Override
    protected void drawIcon(GuiGraphicsExtractor g, int color) {
        g.centeredText(Minecraft.getInstance().font, this.getMessage(), this.getX() + 5, this.getY() - 1, color);
    }
}
