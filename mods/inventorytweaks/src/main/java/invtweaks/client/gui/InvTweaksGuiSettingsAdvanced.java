package invtweaks.client.gui;

import invtweaks.client.InvTweaksConfig;
import invtweaks.client.InvTweaksConst;
import invtweaks.client.InvTweaksLocalization;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;

/**
 * InvTweaksGuiSettingsAdvanced: sort on pickup, sounds, chest buttons, auto-equip armour and the mappings file. The
 * "Server Item Movement" toggle is gone (there is no server part).
 */
public final class InvTweaksGuiSettingsAdvanced extends InvTweaksGuiSettingsAbstract {
    public InvTweaksGuiSettingsAdvanced(Screen parentScreen, InvTweaksConfig config) {
        super(parentScreen, config);
    }

    @Override
    protected void init() {
        super.init();
        int[] point = this.moveToButtonCoords(1);
        this.button(point[0] + 55, this.height / 6 + 144, 200, InvTweaksLocalization.get("invtweaks.settings.advanced.mappingsfile"), null,
                b -> InvTweaksGuiSettings.openFile(InvTweaksConst.CONFIG_PROPS_FILE, "invtweaks.settings.advanced.mappingsfile.error"));
        int index = 2;
        this.toggle(index++, InvTweaksConfig.PROP_ENABLE_SORTING_ON_PICKUP, InvTweaksLocalization.get("invtweaks.settings.advanced.sortonpickup"),
                InvTweaksLocalization.get("invtweaks.settings.advanced.sortonpickup.tooltip"));
        this.toggle(index++, InvTweaksConfig.PROP_ENABLE_SOUNDS, InvTweaksLocalization.get("invtweaks.settings.advanced.sounds"),
                InvTweaksLocalization.get("invtweaks.settings.advanced.sounds.tooltip"));
        this.toggle(index++, InvTweaksConfig.PROP_SHOW_CHEST_BUTTONS, InvTweaksLocalization.get("invtweaks.settings.chestbuttons"),
                InvTweaksLocalization.get("invtweaks.settings.chestbuttons.tooltip"));
        this.toggle(index, InvTweaksConfig.PROP_ENABLE_AUTO_EQUIP_ARMOR, InvTweaksLocalization.get("invtweaks.settings.advanced.autoequip"),
                InvTweaksLocalization.get("invtweaks.settings.advanced.autoequip.tooltip"));
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        int x = this.width / 2;
        graphics.centeredText(this.font, InvTweaksLocalization.get("invtweaks.settings.pvpwarning.pt1"), x, 40, 0xFF999999);
        graphics.centeredText(this.font, InvTweaksLocalization.get("invtweaks.settings.pvpwarning.pt2"), x, 50, 0xFF999999);
    }
}
