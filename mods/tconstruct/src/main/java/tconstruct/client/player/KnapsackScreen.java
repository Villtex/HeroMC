package tconstruct.client.player;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import tconstruct.TConstruct;
import tconstruct.player.KnapsackMenu;

/** KnapsackGui: 3 rows above the inventory, "Knapsack" and "Inventory" labels, the three tabs. Client check open. */
public class KnapsackScreen extends AbstractContainerScreen<KnapsackMenu> {
    private static final Identifier TEXTURE = TConstruct.id("textures/gui/knapsack.png");

    public KnapsackScreen(KnapsackMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 176, 166);
    }

    @Override
    protected void init() {
        super.init();
        TPlayerClient.addTabs(this, this::addRenderableWidget, 4);
        this.addRenderableWidget(TPlayerClient.knapsackTab(this, false));
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(g, mouseX, mouseY, partialTick);
        g.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, 256, 256);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        g.text(this.font, Component.translatable("inventory.knapsack"), 8, 6, 0xFF404040, false);
        g.text(this.font, Component.translatable("container.inventory"), 8, this.imageHeight - 96 + 4, 0xFF404040, false);
    }
}
