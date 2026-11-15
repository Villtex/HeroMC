package invtweaks.client.gui;

import invtweaks.client.InvTweaksConfig;
import invtweaks.client.InvTweaksConst;
import invtweaks.client.InvTweaksLocalization;
import java.time.Duration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.network.chat.Component;

/**
 * InvTweaksGuiSettingsAbstract: title, "Done" and the two-column button grid of the settings screens. The screens
 * are opened as a layer over the container GUI (the original replaced it and reopened it afterwards; in 26.3 that
 * would close the container on the client), Done goes back one screen, Escape back to the container.
 */
public abstract class InvTweaksGuiSettingsAbstract extends Screen {
    protected final Screen parentScreen;
    protected final InvTweaksConfig config;
    protected final String on;
    protected final String off;

    protected InvTweaksGuiSettingsAbstract(Screen parentScreen, InvTweaksConfig config) {
        super(Component.literal(InvTweaksLocalization.get("invtweaks.settings.title")));
        this.parentScreen = parentScreen;
        this.config = config;
        this.on = ": " + InvTweaksLocalization.get("invtweaks.settings.on");
        this.off = ": " + InvTweaksLocalization.get("invtweaks.settings.off");
    }

    public static void open(Screen screen) {
        Minecraft.getInstance().gui.pushScreenLayer(screen);
    }

    /** Done: back to the screen below. */
    protected void back() {
        Minecraft.getInstance().gui.popScreenLayer();
    }

    /** Escape: back to the container GUI. */
    @Override
    public void onClose() {
        Minecraft mc = Minecraft.getInstance();
        int guard = 0;
        do {
            mc.gui.popScreenLayer();
        } while (isOwnScreen(mc.gui.screen()) && guard++ < 10);
    }

    static boolean isOwnScreen(Screen screen) {
        return screen instanceof InvTweaksGuiSettingsAbstract || screen instanceof InvTweaksGuiShortcutsHelp;
    }

    @Override
    protected void init() {
        int[] point = this.moveToButtonCoords(1);
        this.addRenderableWidget(Button.builder(Component.literal(InvTweaksLocalization.get("invtweaks.settings.exit")), b -> this.back())
                .bounds(point[0] + 55, this.height / 6 + 168, 200, 20).build());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        graphics.centeredText(this.font, this.title, this.width / 2, 20, 0xFFFFFFFF);
    }

    protected int[] moveToButtonCoords(int index) {
        return new int[]{this.width / 2 - 155 + (index + 1) % 2 * 160, this.height / 6 + index / 2 * 24};
    }

    /** InvTweaksGuiTooltipButton with the tooltip after 800 ms. */
    protected Button button(int x, int y, int width, String label, String tooltip, Button.OnPress onPress) {
        Button.Builder builder = Button.builder(Component.literal(label), onPress).bounds(x, y, width, 20);
        if (tooltip != null) {
            builder.tooltip(Tooltip.create(Component.literal(tooltip)));
        }
        Button button = builder.build();
        button.setTooltipDelay(Duration.ofMillis(InvTweaksConst.TOOLTIP_DELAY));
        return this.addRenderableWidget(button);
    }

    /** A boolean property button: "Label: ON/OFF", toggles the property. */
    protected Button toggle(int index, String property, String label, String tooltip) {
        int[] point = this.moveToButtonCoords(index);
        return this.button(point[0], point[1], 150, this.computeBooleanButtonLabel(property, label), tooltip, b -> {
            boolean value = !Boolean.parseBoolean(this.config.getProperty(property));
            this.config.setProperty(property, Boolean.toString(value));
            b.setMessage(Component.literal(this.computeBooleanButtonLabel(property, label)));
        });
    }

    protected String computeBooleanButtonLabel(String property, String label) {
        return label + (Boolean.parseBoolean(this.config.getProperty(property)) ? this.on : this.off);
    }

    /** The "Not working?" link: text without button background (drawBackground = false). */
    protected static final class TextButton extends AbstractButton {
        private final Runnable action;

        public TextButton(int x, int y, int width, int height, Component message, Runnable action) {
            super(x, y, width, height, message);
            this.action = action;
        }

        @Override
        public void onPress(InputWithModifiers input) {
            this.action.run();
        }

        @Override
        protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
            graphics.text(Minecraft.getInstance().font, this.getMessage(), this.getX(), this.getY() + (this.getHeight() - 8) / 2,
                    this.isHoveredOrFocused() ? 0xFFFFFFA0 : 0xFF999999);
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput output) {
            this.defaultButtonNarrationText(output);
        }
    }
}
