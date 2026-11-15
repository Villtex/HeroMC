package invtweaks.client.gui;

import invtweaks.client.InvTweaksConfig;
import invtweaks.client.InvTweaksLocalization;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;

/** InvTweaksGuiModNotWorking: the NoCheatPlus hint. */
public final class InvTweaksGuiModNotWorking extends InvTweaksGuiSettingsAbstract {
    public InvTweaksGuiModNotWorking(Screen parentScreen, InvTweaksConfig config) {
        super(parentScreen, config);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        int x = this.width / 2;
        graphics.centeredText(this.font, InvTweaksLocalization.get("invtweaks.help.bugsorting.pt1"), x, 80, 0xFFBBBBBB);
        graphics.centeredText(this.font, InvTweaksLocalization.get("invtweaks.help.bugsorting.pt2"), x, 95, 0xFFBBBBBB);
        graphics.centeredText(this.font, InvTweaksLocalization.get("invtweaks.help.bugsorting.pt3"), x, 110, 0xFFBBBBBB);
        graphics.centeredText(this.font, InvTweaksLocalization.get("invtweaks.help.bugsorting.pt4"), x, 150, 0xFFFFFF99);
    }
}
