package backpack.client;

import backpack.Backpack;
import backpack.inventory.WorkbenchBackpackMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

/**
 * GuiWorkbenchBackpack: the crafting table GUI (/gui/crafting.png) for the small workbench backpack, the mod's own
 * 207 pixel high texture with the two storage rows for the big one. "Crafting" at (28, 6).
 */
public class WorkbenchBackpackScreen extends AbstractContainerScreen<WorkbenchBackpackMenu> {
    private static final Identifier CRAFTING = Identifier.withDefaultNamespace("textures/gui/container/crafting_table.png");
    private static final Identifier BIG = Backpack.id("textures/gui/workbench_backpack.png");

    public WorkbenchBackpackScreen(WorkbenchBackpackMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 176, menu.rows() != 0 ? 207 : 166);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(g, mouseX, mouseY, partialTick);
        g.blit(RenderPipelines.GUI_TEXTURED, this.imageHeight == 207 ? BIG : CRAFTING, this.leftPos, this.topPos, 0.0F, 0.0F, this.imageWidth,
                this.imageHeight, 256, 256);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        g.text(this.font, Component.translatable("container.crafting"), 28, 6, 0xFF404040, false);
        g.text(this.font, Component.translatable("container.inventory"), 8, this.imageHeight - 96 + 2, 0xFF404040, false);
    }
}
