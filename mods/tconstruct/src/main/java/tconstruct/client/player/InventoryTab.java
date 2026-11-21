package tconstruct.client.player;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

/**
 * InventoryTab: a creative-style tab above the inventory (2 = inventory, 3 = extended armor, 4 = knapsack). The tab
 * of the open screen is inactive and drawn as the selected tab. 1.5.2 used /gui/allitems.png, here the creative tab sprites.
 */
public class InventoryTab extends AbstractButton {
    private final int id;
    private final int textureX;
    private final ItemStack stack;

    public InventoryTab(int id, int x, int y, ItemStack stack, int textureX, boolean enabled) {
        super(x, y, 28, 32, Component.empty());
        this.id = id;
        this.textureX = textureX;
        this.stack = stack;
        this.active = enabled;
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        String kind = this.active ? "unselected" : "selected";
        Identifier sprite = Identifier.withDefaultNamespace("container/creative_inventory/tab_top_" + kind + "_" + (this.textureX + 1));
        g.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, this.getX(), this.getY(), 28, this.active ? 28 : 32);
        g.item(this.stack, this.getX() + 6, this.getY() + 8);
    }

    @Override
    public void onPress(InputWithModifiers input) {
        switch (this.id) {
            case 2 -> TPlayerClient.openInventory(Minecraft.getInstance());
            case 3 -> TPlayerClient.send(1);
            case 4 -> TPlayerClient.send(2);
            default -> {
            }
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        this.defaultButtonNarrationText(output);
    }
}
