package bibliocraft.client;

import bibliocraft.BiblioCraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jspecify.annotations.Nullable;

/**
 * The plain GuiContainers of the original: a 256 x 256 background texture drawn at (0, 0, xSize, ySize), the title
 * at (8, 6) and "Inventory" at (8, ySize - 96 + 2) in 4210752 (when the original drew them). Subclass for more.
 */
public class BiblioScreen<M extends AbstractContainerMenu> extends AbstractContainerScreen<M> {
    protected final Identifier texture;
    private final @Nullable String heading;
    private final boolean inventoryLabel;

    /** {@code texture}: file name in textures/model (e.g. "bookshelf_gui"); {@code heading} null = no title. */
    public BiblioScreen(M menu, Inventory inventory, Component title, String texture, int width, int height, @Nullable String heading,
            boolean inventoryLabel) {
        super(menu, inventory, title, width, height);
        this.texture = BiblioCraft.id("textures/model/" + texture + ".png");
        this.heading = heading;
        this.inventoryLabel = inventoryLabel;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        graphics.blit(RenderPipelines.GUI_TEXTURED, this.texture, this.leftPos, this.topPos, 0.0F, 0.0F, this.imageWidth, this.imageHeight, 256, 256);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        if (this.heading != null) {
            graphics.text(this.font, this.heading, 8, 6, 0xFF404040, false);
        }
        if (this.inventoryLabel) {
            graphics.text(this.font, Component.translatable("container.inventory"), 8, this.imageHeight - 96 + 2, 0xFF404040, false);
        }
    }
}
