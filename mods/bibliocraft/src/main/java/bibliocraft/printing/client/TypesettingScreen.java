package bibliocraft.printing.client;

import bibliocraft.common.BCConfig;
import bibliocraft.printing.PrintingNetwork;
import bibliocraft.printing.TypesettingBlockEntity;
import java.util.Arrays;
import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

/**
 * GuiTypesetting + GuiSlotBooklist: the list of saved books (from the table, which the server keeps up to date),
 * one row of 24 pixels per book below a header of guitypebackg tiles with "Please Select a Book". Clicking a book
 * (within 110 pixels of the middle) chooses it ("BiblioType"); a click below the header between x 120 and 330
 * closes the screen, as GuiTypesetting.mouseClicked did. Escape or the inventory key close it too.
 */
public class TypesettingScreen extends Screen {
    private static final Identifier BACKGROUND = PrintRender.tex("guitypebackg");
    private static final int TOP = 32;
    private static final int SLOT_HEIGHT = 24;
    private final TypesettingBlockEntity tile;
    private double scroll;

    public TypesettingScreen(TypesettingBlockEntity tile) {
        super(Component.literal("Please Select a Book"));
        this.tile = tile;
    }

    /** The table's book list (an empty list has no rows, as the folder scan of single player gave). */
    private List<String> books() {
        return Arrays.stream(this.tile.bookList()).filter(s -> !s.isEmpty()).toList();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private double maxScroll() {
        return Math.max(0, this.books().size() * SLOT_HEIGHT - (this.height - TOP - 4));
    }

    @Override
    public boolean mouseScrolled(double x, double y, double scrollX, double scrollY) {
        this.scroll = Mth.clamp(this.scroll - scrollY * SLOT_HEIGHT / 2.0, 0.0, this.maxScroll());
        return true;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double x = event.x();
        double y = event.y();
        List<String> books = this.books();
        // GuiSlot.elementClicked
        if (y >= TOP && y < this.height && x >= this.width / 2 - 110 && x <= this.width / 2 + 110) {
            int index = (int) ((y - TOP - 4 + this.scroll) / SLOT_HEIGHT);
            if (y - TOP - 4 + this.scroll >= 0 && index < books.size()) {
                ClientPacketDistributor.sendToServer(new PrintingNetwork.TypeBook(this.tile.getBlockPos(), books.get(index)));
            }
        }
        // GuiTypesetting.mouseClicked
        if (y > 30 && x > 120 && x < 330) {
            this.onClose();
        }
        return true;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (this.minecraft.options.keyInventory.matches(event)) {
            this.onClose();
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public void tick() {
        if (this.tile.isRemoved()) {
            this.onClose();
        }
        this.scroll = Mth.clamp(this.scroll, 0.0, this.maxScroll());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractRenderState(graphics, mouseX, mouseY, a);
        List<String> books = this.books();
        graphics.enableScissor(0, TOP, this.width, this.height);
        for (int i = 0; i < books.size(); i++) {
            int y = TOP + 4 - (int) this.scroll + i * SLOT_HEIGHT;
            if (y + SLOT_HEIGHT >= TOP && y <= this.height) {
                graphics.centeredText(this.font, books.get(i), this.width / 2, y + 1, 0xFFFFFFFF);
            }
        }
        graphics.disableScissor();
        // overlayBackground(0, top): the header tiles, darkened (4210752), and the heading in Config.color
        graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, 0, 0, 0.0F, 0.0F, this.width, TOP, 32, 32, 0xFF404040);
        graphics.centeredText(this.font, "Please Select a Book", this.width / 2, 14, BCConfig.textColor());
    }
}
