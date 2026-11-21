package tconstruct.client.player;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import tconstruct.TConstruct;
import tconstruct.TPlayer;
import tconstruct.player.ArmorExtendedMenu;
import tconstruct.player.PlayerData;

/** ArmorExtendedGui: the extra slots (with empty-slot pictures), the player on the left, the tabs. Client check open. */
public class ArmorExtendedScreen extends AbstractContainerScreen<ArmorExtendedMenu> {
    private static final Identifier TEXTURE = TConstruct.id("textures/gui/armorextended.png");
    /** Slot, x, y, u, v of the empty-slot pictures. */
    private static final int[][] EMPTY = {{0, 79, 16, 176, 9}, {1, 79, 34, 176, 27}, {2, 115, 16, 212, 9}, {3, 115, 34, 212, 27},
            {4, 151, 16, 230, 0}, {5, 151, 34, 230, 18}, {6, 151, 52, 230, 36}};
    private InventoryTab knapsackTab;
    private float xMouse;
    private float yMouse;

    public ArmorExtendedScreen(ArmorExtendedMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 176, 166);
    }

    @Override
    protected void init() {
        super.init();
        TPlayerClient.addTabs(this, this::addRenderableWidget, 3);
        this.knapsackTab = null;
    }

    @Override
    public void containerTick() {
        super.containerTick();
        boolean knapsack = this.menu.armor.getItem(PlayerData.KNAPSACK_SLOT).is(TPlayer.KNAPSACK.get());
        if (knapsack && this.knapsackTab == null) {
            this.knapsackTab = TPlayerClient.knapsackTab(this, true);
            this.addRenderableWidget(this.knapsackTab);
        } else if (!knapsack && this.knapsackTab != null) {
            this.removeWidget(this.knapsackTab);
            this.knapsackTab = null;
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(g, mouseX, mouseY, partialTick);
        this.xMouse = mouseX;
        this.yMouse = mouseY;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(g, mouseX, mouseY, partialTick);
        int x = this.leftPos;
        int y = this.topPos;
        g.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight, 256, 256);
        for (int[] e : EMPTY) {
            if (this.menu.armor.getItem(e[0]).isEmpty()) {
                g.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x + e[1], y + e[2], e[3], e[4], 18, 18, 256, 256);
            }
        }
        InventoryScreen.extractEntityInInventoryFollowsMouse(g, x + 8, y + 8, x + 57, y + 78, 30, 0.0625F, this.xMouse, this.yMouse,
                this.minecraft.player);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor g, int mouseX, int mouseY) {
    }
}
