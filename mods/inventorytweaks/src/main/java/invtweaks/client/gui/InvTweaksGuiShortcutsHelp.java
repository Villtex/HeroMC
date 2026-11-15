package invtweaks.client.gui;

import invtweaks.client.InvTweaksConfig;
import invtweaks.client.InvTweaksKeys;
import invtweaks.client.InvTweaksLocalization;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import com.mojang.blaze3d.platform.InputConstants;

/**
 * InvTweaksGuiShortcutsHelp: the list of shortcuts with the configured keys. The red "since 1.3.1 shortcuts won't
 * work" warning of the original is left out: they work here.
 */
public final class InvTweaksGuiShortcutsHelp extends Screen {
    private final InvTweaksConfig config;

    public InvTweaksGuiShortcutsHelp(InvTweaksConfig config) {
        super(Component.literal(InvTweaksLocalization.get("invtweaks.help.shortcuts.title")));
        this.config = config;
    }

    @Override
    protected void init() {
        this.addRenderableWidget(Button.builder(Component.literal(InvTweaksLocalization.get("invtweaks.settings.exit")),
                b -> Minecraft.getInstance().gui.popScreenLayer()).bounds(this.width / 2 - 100, this.height / 6 + 168, 200, 20).build());
    }

    @Override
    public void onClose() {
        Minecraft mc = Minecraft.getInstance();
        int guard = 0;
        do {
            mc.gui.popScreenLayer();
        } while (InvTweaksGuiSettingsAbstract.isOwnScreen(mc.gui.screen()) && guard++ < 10);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(g, mouseX, mouseY, partialTick);
        g.centeredText(this.font, this.title, this.width / 2, 20, 0xFFFFFFFF);
        String click = InvTweaksLocalization.get("invtweaks.help.shortcuts.click");
        String or = InvTweaksLocalization.get("invtweaks.help.shortcuts.or");
        int y = this.height / 6 - 2;
        this.line(g, InvTweaksLocalization.get("invtweaks.help.shortcuts.onestack"), "LSHIFT " + or + " RSHIFT + " + click, 0xFFFF00, y);
        y += 12;
        this.line(g, "", this.upOrDown(InvTweaksConfig.PROP_SHORTCUT_UP, Minecraft.getInstance().options.keyUp,
                InvTweaksLocalization.get("invtweaks.help.shortcuts.forward")) + " + " + click, 0xFFFF00, y);
        y += 12;
        this.line(g, "", this.upOrDown(InvTweaksConfig.PROP_SHORTCUT_DOWN, Minecraft.getInstance().options.keyDown,
                InvTweaksLocalization.get("invtweaks.help.shortcuts.backward")) + " + " + click, 0xFFFF00, y);
        y += 12;
        this.line(g, InvTweaksLocalization.get("invtweaks.help.shortcuts.oneitem"),
                this.config.getProperty(InvTweaksConfig.PROP_SHORTCUT_ONE_ITEM) + " + " + click, 0xFFFF00, y);
        y += 12;
        this.line(g, InvTweaksLocalization.get("invtweaks.help.shortcuts.allitems"),
                this.config.getProperty(InvTweaksConfig.PROP_SHORTCUT_ALL_ITEMS) + " + " + click, 0xFFFF00, y);
        y += 12;
        this.line(g, InvTweaksLocalization.get("invtweaks.help.shortcuts.everything"),
                this.config.getProperty(InvTweaksConfig.PROP_SHORTCUT_EVERYTHING) + " + " + click, 0xFFFF00, y);
        y += 19;
        this.line(g, InvTweaksLocalization.get("invtweaks.help.shortcuts.hotbar"), "0-9 + " + click, 0x00FF33, y);
        y += 12;
        this.line(g, InvTweaksLocalization.get("invtweaks.help.shortcuts.emptyslot"), InvTweaksLocalization.get("invtweaks.help.shortcuts.rightclick"),
                0x00FF33, y);
        y += 12;
        this.line(g, InvTweaksLocalization.get("invtweaks.help.shortcuts.drop"),
                this.config.getProperty(InvTweaksConfig.PROP_SHORTCUT_DROP) + " + " + click, 0x00FF33, y);
        y += 19;
        this.line(g, InvTweaksLocalization.get("invtweaks.help.shortcuts.craftall"), "LSHIFT, RSHIFT + " + click, 0xFF8800, y);
        y += 12;
        this.line(g, InvTweaksLocalization.get("invtweaks.help.shortcuts.craftone"),
                this.config.getProperty(InvTweaksConfig.PROP_SHORTCUT_ONE_ITEM) + " + " + click, 0xFF8800, y);
        y += 19;
        String sortKey = InvTweaksKeys.getKeyName(this.config.getSortKeyCode());
        this.line(g, InvTweaksLocalization.get("invtweaks.help.shortcuts.selectconfig"), "0-9 + " + (sortKey != null ? sortKey : "(Sort Key)"),
                0x88FFFF, y);
    }

    private String upOrDown(String property, KeyMapping mapping, String fallback) {
        InputConstants.Key key = mapping.getKey();
        String name = key.getType() == InputConstants.Type.KEYBOARD ? InvTweaksKeys.getKeyName(key.getValue()) : null;
        if (name == null) {
            name = fallback;
        }
        String configured = this.config.getProperty(property);
        return name.equals(configured) ? name : name + "/" + configured;
    }

    private void line(GuiGraphicsExtractor g, String label, String value, int color, int y) {
        g.text(this.font, label, 30, y, 0xFFFFFFFF);
        if (value != null) {
            String or = InvTweaksLocalization.get("invtweaks.help.shortcuts.or");
            g.text(this.font, value.contains("DEFAULT") ? "-" : value.replaceAll(", ", " " + or + " "), this.width / 2 - 30, y, 0xFF000000 | color);
        }
    }
}
