package chococraft.client;

import chococraft.entity.AbstractChocobo;
import chococraft.entity.Chicobo;
import chococraft.network.ChocoNetwork;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;

/** GuiNameChocobo: "Name your Chocobo/Chicobo", a text field of 20 characters, Accept (only with a name) and Cancel. */
public class NameChocoboScreen extends Screen {
    private final ChocopediaScreen parent;
    private final AbstractChocobo chocobo;
    private EditBox name;
    private Button accept;

    public NameChocoboScreen(ChocopediaScreen parent, AbstractChocobo chocobo) {
        super(Component.translatable(chocobo instanceof Chicobo ? "gui.chococraft.name_chicobo" : "gui.chococraft.name_chocobo"));
        this.parent = parent;
        this.chocobo = chocobo;
    }

    @Override
    protected void init() {
        this.accept = this.addRenderableWidget(Button.builder(Component.translatable("gui.chococraft.accept"), b -> this.confirm())
                .bounds(this.width / 2 - 100, this.height / 4 + 108, 200, 20).build());
        this.addRenderableWidget(Button.builder(Component.translatable("gui.cancel"), b -> this.minecraft.gui.setScreen(this.parent))
                .bounds(this.width / 2 - 100, this.height / 4 + 132, 200, 20).build());
        this.name = new EditBox(this.font, this.width / 2 - 100, 60, 200, 20, Component.translatable("gui.chococraft.new_name"));
        this.name.setMaxLength(ChocoNetwork.MAX_NAME);
        this.name.setValue(this.chocobo.getChocoboName());
        this.name.setResponder(s -> this.accept.active = !s.trim().isEmpty());
        this.addRenderableWidget(this.name);
        this.setInitialFocus(this.name);
    }

    private void confirm() {
        if (!this.name.getValue().trim().isEmpty()) {
            this.parent.send(ChocoNetwork.RENAME, this.name.getValue());
        }
        this.minecraft.gui.setScreen(this.parent);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if ((event.key() == InputConstants.KEY_RETURN || event.key() == InputConstants.KEY_NUMPADENTER) && this.accept.active) {
            this.confirm();
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(g, mouseX, mouseY, partialTick);
        g.centeredText(this.font, this.getTitle(), this.width / 2, this.height / 4 - 40, 0xFFFFFFFF);
        g.text(this.font, Component.translatable("gui.chococraft.new_name"), this.width / 2 - 100, 47, 0xFFA0A0A0);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
