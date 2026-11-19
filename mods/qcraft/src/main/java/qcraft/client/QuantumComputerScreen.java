package qcraft.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import qcraft.QuantumCraft;
import qcraft.block.QuantumComputerBlockEntity;
import qcraft.block.QuantumComputerMenu;

/**
 * GuiQuantumComputer: 176x36, one button "Energize" (x+8, y+8, 160x20), disabled and labelled with the error while the
 * computer cannot teleport. No title, no inventory.
 */
public class QuantumComputerScreen extends AbstractContainerScreen<QuantumComputerMenu> {
    private static final Identifier TEXTURE = QuantumCraft.id("textures/gui/quantumcomputer.png");
    private Button button;

    public QuantumComputerScreen(QuantumComputerMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 176, 36);
    }

    @Override
    protected void init() {
        super.init();
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        this.button = this.addRenderableWidget(Button.builder(Component.translatable("gui.quantumcraft.energize"), b -> this.energize())
                .bounds(x + 8, y + 8, this.imageWidth - 16, 20).build());
        this.updateStatus();
    }

    private void energize() {
        this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, QuantumComputerMenu.BUTTON_ENERGIZE);
    }

    private void updateStatus() {
        String key = QuantumComputerBlockEntity.errorKey(this.menu.getError());
        if (key != null) {
            this.button.active = false;
            this.button.setMessage(key.isEmpty() ? Component.empty() : Component.translatable(key));
        } else {
            this.button.active = true;
            this.button.setMessage(Component.translatable("gui.quantumcraft.energize"));
        }
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        this.updateStatus();
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, this.leftPos, this.topPos, 0.0F, 0.0F, this.imageWidth, this.imageHeight, 256, 256);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
    }
}
