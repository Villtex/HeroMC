package backpack.client;

import backpack.Backpack;
import backpack.network.BackpackNetwork;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import com.mojang.blaze3d.platform.InputConstants;

/**
 * GuiBackpackAlt: "Rename your backpack", a text field of 32 characters, OK (only with a name) and Cancel; Enter is
 * OK, Escape is Cancel. The name goes to the server ("BackpackRename").
 */
public class RenameScreen extends Screen {
    private static final Identifier BACKGROUND = Backpack.id("textures/gui/rename_backpack.png");
    private EditBox name;
    private Button ok;

    public RenameScreen() {
        super(Component.translatable("gui.backpack.rename"));
    }

    @Override
    protected void init() {
        this.ok = this.addRenderableWidget(Button.builder(Component.translatable("gui.ok"), b -> this.confirm())
                .bounds(this.width / 2 + 100 - 80, this.height / 2 + 50 - 24, 60, 20).build());
        this.ok.active = false;
        this.addRenderableWidget(Button.builder(Component.translatable("gui.cancel"), b -> this.onClose())
                .bounds(this.width / 2 - 100 + 20, this.height / 2 + 50 - 24, 60, 20).build());
        this.name = new EditBox(this.font, this.width / 2 - 100, this.height / 2 - 50 + 47, 200, 20, Component.translatable("gui.backpack.new_name"));
        this.name.setMaxLength(BackpackNetwork.MAX_NAME);
        this.name.setResponder(s -> this.ok.active = !s.trim().isEmpty());
        this.addRenderableWidget(this.name);
        this.setInitialFocus(this.name);
    }

    private void confirm() {
        String text = this.name.getValue().trim();
        if (!text.isEmpty()) {
            ClientPacketDistributor.sendToServer(new BackpackNetwork.Rename(text));
        }
        this.onClose();
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.key() == InputConstants.KEY_RETURN || event.key() == InputConstants.KEY_NUMPADENTER) {
            if (this.ok.active) {
                this.confirm();
            }
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(g, mouseX, mouseY, partialTick);
        int j = (this.width - 100) / 2;
        int k = (this.height - 50) / 2;
        g.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, j - 100 + 30, k - 50 + 30 + 5, 0.0F, 0.0F, 240, 100, 256, 256);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(g, mouseX, mouseY, partialTick);
        Component title = this.getTitle();
        g.text(this.font, title, this.width / 2 - this.font.width(title) / 2, this.height / 2 - 50 + 20, 0xFF000000, false);
        g.text(this.font, Component.translatable("gui.backpack.new_name"), this.width / 2 - 100, this.height / 2 - 50 + 35, 0xFF404040, false);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
