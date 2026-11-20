package mapwriter.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;
import java.util.function.Function;

/** TextDialog: one line of text with a title; Enter confirms when the validator has no complaint, Escape goes back. */
final class PromptScreen extends Screen {
    private final Screen parent;
    private final String title;
    private final String initial;
    private final Function<String, String> validator;
    private final Consumer<String> onOk;
    private EditBox box;
    private String error = "";

    PromptScreen(Screen parent, String title, String initial, Function<String, String> validator, Consumer<String> onOk) {
        super(Component.literal(title));
        this.parent = parent;
        this.title = title;
        this.initial = initial;
        this.validator = validator;
        this.onOk = onOk;
    }

    @Override
    protected void init() {
        box = new EditBox(font, width / 2 - 100, height / 2 - 10, 200, 20, Component.literal(title));
        box.setMaxLength(64);
        box.setValue(initial);
        addRenderableWidget(box);
        setInitialFocus(box);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.key() == InputConstants.KEY_RETURN || event.key() == InputConstants.KEY_NUMPADENTER) {
            String problem = validator.apply(box.getValue());
            if (problem == null) {
                minecraft.gui.setScreen(parent);
                onOk.accept(box.getValue());
            } else {
                error = problem;
            }
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public void onClose() {
        minecraft.gui.setScreen(parent);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(g, mouseX, mouseY, partialTick);
        g.centeredText(font, title, width / 2, height / 2 - 26, 0xFFFFFFFF);
        if (!error.isEmpty()) {
            g.centeredText(font, error, width / 2, height / 2 + 16, 0xFFFF5555);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
