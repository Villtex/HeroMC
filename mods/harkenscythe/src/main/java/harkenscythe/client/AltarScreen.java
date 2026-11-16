package harkenscythe.client;

import harkenscythe.HarkenScythe;
import harkenscythe.altar.AltarMenu;
import harkenscythe.altar.AltarRecipe;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.object.book.BookModel;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

/**
 * GuiHSSoulAltar / GuiHSBloodAltar: the altar's own background, the opening book of the enchanting table (Book of
 * Shadows or Book of Carnage), the essence of the crucibles at the top right, the cost of the item below it and the
 * button of its recipe (Enthrall, Entwine, Imbue, Germinate, Augment, Attune).
 */
public class AltarScreen extends AbstractContainerScreen<AltarMenu> {
    private static final int WHITE = 0xFFFFFFFF;
    private static final int GREY = 0xFF404040;
    private final Identifier background;
    private final Identifier book;
    private final Component essenceLabel;
    private final RandomSource random = RandomSource.create();
    private BookModel bookModel;
    private Button button;
    private float flip;
    private float oFlip;
    private float flipT;
    private float flipA;
    private float open;
    private float oOpen;
    private ItemStack last = ItemStack.EMPTY;

    public AltarScreen(AltarMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        boolean blood = menu.isBlood();
        this.background = HarkenScythe.id(blood ? "textures/gui/altar_of_blood.png" : "textures/gui/altar_of_souls.png");
        this.book = HarkenScythe.id(blood ? "textures/entity/altar/book_of_carnage.png" : "textures/entity/altar/book_of_shadows.png");
        this.essenceLabel = Component.translatable("container." + HarkenScythe.MOD_ID + (blood ? ".collected_blood" : ".captured_souls"));
        this.titleLabelX = 6;
        this.inventoryLabelX = 6;
    }

    @Override
    protected void init() {
        super.init();
        this.bookModel = new BookModel(this.minecraft.getEntityModels().bakeLayer(ModelLayers.BOOK));
        this.button = this.addRenderableWidget(Button.builder(Component.empty(), b -> this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 0))
                .bounds(this.leftPos + 59, this.topPos + 45, 86, 19).build());
        this.updateButton();
    }

    private void updateButton() {
        AltarRecipe.Action action = this.menu.action();
        this.button.visible = action != null;
        if (action != null) {
            this.button.setMessage(Component.translatable("container." + HarkenScythe.MOD_ID + ".altar." + action.getSerializedName()));
        }
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        this.updateButton();
        this.tickBook();
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        graphics.blit(RenderPipelines.GUI_TEXTURED, this.background, this.leftPos, this.topPos, 0.0F, 0.0F, this.imageWidth, this.imageHeight, 256, 256);
        float a = this.minecraft.getDeltaTracker().getGameTimeDeltaPartialTick(false);
        int x0 = this.leftPos + 14;
        int y0 = this.topPos + 14;
        graphics.book(this.bookModel, this.book, 40.0F, Mth.lerp(a, this.oOpen, this.open), Mth.lerp(a, this.oFlip, this.flip), x0, y0, x0 + 38, y0 + 31);
    }

    /** drawGuiContainerForegroundLayer: the names, the essence (white, a bit to the left from 1000 on) and the cost. */
    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        super.extractLabels(graphics, mouseX, mouseY);
        graphics.text(this.font, this.essenceLabel, 94, 6, GREY, false);
        int essence = this.menu.essence();
        graphics.text(this.font, String.valueOf(essence), essence > 999 ? 146 : 150, 24, WHITE, false);
        if (this.menu.getSlot(0).hasItem()) {
            graphics.text(this.font, String.valueOf(this.menu.cost()), 150, 51, WHITE, false);
        }
    }

    /** func_74205_h: the book of the enchanting GUI opens while the item has a recipe and turns its pages when it changes. */
    private void tickBook() {
        ItemStack current = this.menu.getSlot(0).getItem();
        if (!ItemStack.matches(current, this.last)) {
            this.last = current.copy();
            do {
                this.flipT += this.random.nextInt(4) - this.random.nextInt(4);
            } while (this.flip <= this.flipT + 1.0F && this.flip >= this.flipT - 1.0F);
        }
        this.oFlip = this.flip;
        this.oOpen = this.open;
        this.open = Mth.clamp(this.open + (this.menu.action() != null ? 0.2F : -0.2F), 0.0F, 1.0F);
        float diff = Mth.clamp((this.flipT - this.flip) * 0.4F, -0.2F, 0.2F);
        this.flipA += (diff - this.flipA) * 0.9F;
        this.flip += this.flipA;
    }
}
