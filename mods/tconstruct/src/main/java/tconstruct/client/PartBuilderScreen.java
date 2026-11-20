package tconstruct.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import tconstruct.TConstruct;
import tconstruct.items.PatternBuilder;
import tconstruct.menu.PartBuilderMenu;
import tconstruct.tools.TMaterial;

/**
 * {@code PartCrafterGui}: the part builder, the pattern chest beside it on the left, and on the right the stats of the
 * materials in the two material slots.
 */
public class PartBuilderScreen extends AbstractContainerScreen<PartBuilderMenu> {
    private static final Identifier TEXTURE = TConstruct.id("textures/gui/toolparts.png");
    private static final Identifier CHEST = TConstruct.id("textures/gui/patternchestmini.png");
    private static final Identifier DESCRIPTION = TConstruct.id("textures/gui/description.png");
    private static final int WHITE = 0xFFFFFFFF;
    private static final int GREY = 0xFF404040;

    public PartBuilderScreen(PartBuilderMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected boolean hasClickedOutside(double mouseX, double mouseY, int left, int top) {
        int offsetLeft = this.menu.hasChest() ? 108 : 0;
        return mouseX < left - offsetLeft || mouseY < top || mouseX >= left + this.imageWidth || mouseY >= top + this.imageHeight;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(g, mouseX, mouseY, partialTick);
        int x = this.leftPos;
        int y = this.topPos;
        g.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight, 256, 256);
        g.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x + 39, y + 26, 0, 166, 98, 36, 256, 256);
        if (!this.menu.slots.get(0).hasItem()) {
            g.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x + 39, y + 26, 176, 0, 18, 18, 256, 256);
        }
        if (!this.menu.slots.get(2).hasItem()) {
            g.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x + 57, y + 26, 176, 18, 18, 18, 256, 256);
        }
        if (!this.menu.slots.get(1).hasItem()) {
            g.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x + 39, y + 44, 176, 0, 18, 18, 256, 256);
        }
        if (!this.menu.slots.get(3).hasItem()) {
            g.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x + 57, y + 44, 176, 36, 18, 18, 256, 256);
        }
        if (this.menu.hasChest()) {
            g.blit(RenderPipelines.GUI_TEXTURED, CHEST, x - 116, y + 11, 0, 0, this.imageWidth, this.imageHeight, 256, 256);
        }
        g.blit(RenderPipelines.GUI_TEXTURED, DESCRIPTION, x + this.imageWidth, y, 126, 0, 126, this.imageHeight, 256, 256);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        g.text(this.font, this.title, 6, 6, GREY, false);
        g.text(this.font, Component.translatable("container.inventory"), 8, this.imageHeight - 96 + 2, GREY, false);
        if (this.menu.hasChest()) {
            g.text(this.font, Component.translatable("block.tconstruct.pattern_chest"), -108, this.imageHeight - 148, GREY, false);
        }
        boolean top = this.material(g, this.menu.slots.get(2).getItem(), 8);
        boolean bottom = this.material(g, this.menu.slots.get(3).getItem(), 90);
        if (!top && !bottom) {
            g.centeredText(this.font, Component.translatable("gui.tconstruct.part_building").withStyle(s -> s.withUnderlined(true)),
                    this.imageWidth + 63, 8, WHITE);
            g.textWithWordWrap(this.font, Component.translatable("gui.tconstruct.part_building.help"), this.imageWidth + 8, 24, 115, WHITE, false);
        }
    }

    /** drawMaterialInformation for one material slot. */
    private boolean material(GuiGraphicsExtractor g, ItemStack stack, int offset) {
        TMaterial material = TMaterial.byId(PatternBuilder.partId(stack));
        if (stack.isEmpty() || material == null) {
            return false;
        }
        int x = this.imageWidth + 8;
        g.centeredText(this.font, Component.translatable(material.langKey()).withStyle(s -> s.withUnderlined(true)), this.imageWidth + 63, offset, WHITE);
        g.text(this.font, Component.translatable("gui.tconstruct.base_durability", material.durability), x, offset + 16, WHITE, false);
        g.text(this.font, Component.translatable("gui.tconstruct.handle_modifier", String.valueOf(material.handle)), x, offset + 27, WHITE, false);
        g.text(this.font, Component.translatable("gui.tconstruct.mining_speed", String.valueOf(material.miningSpeed / 100.0F)), x, offset + 38, WHITE,
                false);
        g.text(this.font, Component.translatable("gui.tconstruct.mining_level", ToolStationScreen.level(material.harvestLevel)), x, offset + 49, WHITE,
                false);
        int attack = material.attack;
        String hearts = attack % 2 == 0 ? String.valueOf(attack / 2) : String.valueOf(attack / 2.0F);
        g.text(this.font, Component.translatable("gui.tconstruct.attack",
                Component.translatable(attack == 2 ? "gui.tconstruct.heart" : "gui.tconstruct.hearts", hearts)), x, offset + 60, WHITE, false);
        return true;
    }
}
