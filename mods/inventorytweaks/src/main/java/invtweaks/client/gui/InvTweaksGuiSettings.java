package invtweaks.client.gui;

import com.mojang.blaze3d.Blaze3D;
import com.mojang.blaze3d.platform.InputConstants;
import invtweaks.client.InvTweaks;
import invtweaks.client.InvTweaksConfig;
import invtweaks.client.InvTweaksConst;
import invtweaks.client.InvTweaksKeys;
import invtweaks.client.InvTweaksLocalization;
import java.net.URI;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;

/** InvTweaksGuiSettings: shortcuts, sort key, auto-refill, middle click, the config files and help. */
public final class InvTweaksGuiSettings extends InvTweaksGuiSettingsAbstract {
    private Button sortMappingButton;
    private boolean sortMappingEdition = false;

    public InvTweaksGuiSettings(Screen parentScreen, InvTweaksConfig config) {
        super(parentScreen, config);
    }

    @Override
    protected void init() {
        super.init();
        int[] point = this.moveToButtonCoords(1);
        int index = 0;
        this.button(point[0] + 55, this.height / 6 + 96, 200, InvTweaksLocalization.get("invtweaks.settings.rulesfile"), null,
                b -> openFile(InvTweaksConst.CONFIG_RULES_FILE, "invtweaks.settings.rulesfile.error"));
        this.button(point[0] + 55, this.height / 6 + 120, 200, InvTweaksLocalization.get("invtweaks.settings.treefile"), null,
                b -> openFile(InvTweaksConst.CONFIG_TREE_FILE, "invtweaks.settings.treefile.error"));
        this.button(point[0] + 55, this.height / 6 + 144, 200, InvTweaksLocalization.get("invtweaks.settings.onlinehelp"), null, b -> {
            try {
                Blaze3D.openUri(new URI(InvTweaksConst.HELP_URL));
            } catch (Exception e) {
                InvTweaks.logInGameErrorStatic("invtweaks.settings.onlinehelp.error", e);
            }
        });

        point = this.moveToButtonCoords(index++);
        this.button(point[0] + 130, point[1], 20, "?", InvTweaksLocalization.get("invtweaks.help.shortcuts.title"),
                b -> open(new InvTweaksGuiShortcutsHelp(this.config)));
        int[] shortcutsPoint = point;
        Button shortcuts = this.toggle(index - 1, InvTweaksConfig.PROP_ENABLE_SHORTCUTS, InvTweaksLocalization.get("invtweaks.settings.shortcuts"),
                InvTweaksLocalization.get("invtweaks.settings.shortcuts.tooltip"));
        shortcuts.setX(shortcutsPoint[0]);
        shortcuts.setWidth(130);

        point = this.moveToButtonCoords(index++);
        this.sortMappingButton = this.button(point[0], point[1], 150, this.sortKeyLabel(this.config.getProperty(InvTweaksConfig.PROP_KEY_SORT_INVENTORY)),
                null, b -> {
                    b.setMessage(Component.literal(InvTweaksLocalization.get("invtweaks.settings.key") + " > ??? <"));
                    this.sortMappingEdition = true;
                });

        this.toggle(index++, InvTweaksConfig.PROP_AUTO_REFILL_BEFORE_BREAK, InvTweaksLocalization.get("invtweaks.settings.beforebreak"),
                InvTweaksLocalization.get("invtweaks.settings.beforebreak.tooltip"));
        this.toggle(index++, InvTweaksConfig.PROP_ENABLE_AUTO_REFILL, InvTweaksLocalization.get("invtweaks.settings.autorefill"),
                InvTweaksLocalization.get("invtweaks.settings.autorefill.tooltip"));

        point = this.moveToButtonCoords(index++);
        this.button(point[0], point[1], 150, InvTweaksLocalization.get("invtweaks.settings.moreoptions"),
                InvTweaksLocalization.get("invtweaks.settings.moreoptions.tooltip"), b -> open(new InvTweaksGuiSettingsAdvanced(this.parentScreen, this.config)));
        this.addRenderableWidget(new TextButton(5, this.height - 20, 100, 20, Component.literal(InvTweaksLocalization.get("invtweaks.help.bugsorting")),
                () -> open(new InvTweaksGuiModNotWorking(this.parentScreen, this.config))));

        this.toggle(index++, InvTweaksConfig.PROP_ENABLE_MIDDLE_CLICK, InvTweaksLocalization.get("invtweaks.settings.middleclick"),
                InvTweaksLocalization.get("invtweaks.settings.middleclick.tooltip"));
    }

    private String sortKeyLabel(String key) {
        return InvTweaksLocalization.get("invtweaks.settings.key") + " " + key;
    }

    static void openFile(java.nio.file.Path file, String errorKey) {
        try {
            Blaze3D.openPath(file);
        } catch (Exception e) {
            InvTweaks.logInGameErrorStatic(errorKey, e);
        }
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (this.sortMappingEdition) {
            this.sortMappingEdition = false;
            int code = InputConstants.getKey(event).getValue();
            String name = InvTweaksKeys.getKeyName(code);
            if (code != InputConstants.KEY_ESCAPE && name != null) {
                this.config.setProperty(InvTweaksConfig.PROP_KEY_SORT_INVENTORY, name);
            }
            this.sortMappingButton.setMessage(Component.literal(this.sortKeyLabel(this.config.getProperty(InvTweaksConfig.PROP_KEY_SORT_INVENTORY))));
            return true;
        }
        return super.keyPressed(event);
    }
}
