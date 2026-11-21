package twilightforest.client.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import twilightforest.TwilightForest;
import twilightforest.block.UncraftingMenu;

/**
 * GuiTFGoblinCrafting: the ingredients the input takes apart into are drawn in the assembly grid behind a grey veil
 * (red for broken parts); the uncrafting cost left of the grid, the re-crafting cost right of it, green when the
 * player has the levels, else red.
 */
public class UncraftingScreen extends AbstractContainerScreen<UncraftingMenu> {
    private static final Identifier TEXTURE = TwilightForest.id("textures/gui/uncrafting.png");

    public UncraftingScreen(UncraftingMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 176, 166);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float a) {
        super.extractBackground(g, mouseX, mouseY, a);
        g.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, 256, 256);
        for (int i = 0; i < 9; i++) {
            ItemStack part = this.menu.uncraftingItem(i);
            Slot assembly = this.menu.getSlot(11 + i);
            if (!part.isEmpty() && !assembly.hasItem()) {
                int x = this.leftPos + assembly.x;
                int y = this.topPos + assembly.y;
                g.item(part, x, y);
                g.fill(x, y, x + 16, y + 16, this.menu.isBroken(i) ? 0x80FF8B8B : 0x9F8B8B8B);
            }
        }
        this.cost(g, this.menu.uncraftingCost(), 48);
        this.cost(g, this.menu.recraftingCost(), 130);
    }

    private void cost(GuiGraphicsExtractor g, int cost, int right) {
        if (cost <= 0 || this.minecraft.player == null) {
            return;
        }
        boolean enough = this.minecraft.player.experienceLevel >= cost || this.minecraft.player.getAbilities().instabuild;
        String text = String.valueOf(cost);
        g.text(this.font, text, this.leftPos + right - this.font.width(text), this.topPos + 38, enough ? 0xFF80FF20 : 0xFFA00000, true);
    }
}
