package tconstruct.client.manual;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import tconstruct.TConstruct;
import tconstruct.player.PlayerItems;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * GuiManual: two pages of the manual XML (manuals/firstday, materials, smeltery.xml of the original archive) side by
 * side, turned two at a time. The SmallFontRenderer of 1.5.2 drew the unicode font: here the "minecraft:uniform" font.
 * Client check open.
 */
public class ManualScreen extends Screen {
    private static final org.slf4j.Logger LOGGER = com.mojang.logging.LogUtils.getLogger();
    static final Identifier BOOK_LEFT = TConstruct.id("textures/gui/bookleft.png");
    static final Identifier BOOK_RIGHT = TConstruct.id("textures/gui/bookright.png");
    private static final Style SMALL = Style.EMPTY.withFont(new FontDescription.Resource(Identifier.withDefaultNamespace("uniform")));
    private static final String[] FILES = {"firstday", "materials", "smeltery", "diary"};
    private static final Map<String, Document> CACHE = new HashMap<>();
    private static final Map<String, Supplier<ManualPages.Page>> PAGES = new HashMap<>();

    static {
        PAGES.put("crafting", ManualPages.Crafting::new);
        PAGES.put("picture", ManualPages.Picture::new);
        PAGES.put("text", ManualPages.Text::new);
        PAGES.put("intro", ManualPages.Text::new);
        PAGES.put("sectionpage", ManualPages.Section::new);
        PAGES.put("contents", ManualPages.Contents::new);
        PAGES.put("furnace", ManualPages.Furnace::new);
        PAGES.put("sidebar", ManualPages.Sidebar::new);
        PAGES.put("materialstats", ManualPages.Material::new);
        PAGES.put("toolpage", ManualPages.Tool::new);
        PAGES.put("modifier", ManualPages.Modifier::new);
        PAGES.put("blockcast", ManualPages.BlockCast::new);
        PAGES.put("blank", ManualPages.Blank::new);
    }

    static final int BOOK_WIDTH = 206;
    static final int BOOK_HEIGHT = 200;
    private final @Nullable Document manual;
    private int currentPage;
    private int maxPages;
    private ManualPages.@Nullable Page pageLeft;
    private ManualPages.@Nullable Page pageRight;
    GuiGraphicsExtractor g;

    public ManualScreen(ItemStack stack) {
        super(stack.getHoverName());
        int volume = stack.getItem() instanceof PlayerItems.Manual manual ? manual.volume : 0;
        this.manual = load(FILES[volume]);
    }

    private static @Nullable Document load(String file) {
        return CACHE.computeIfAbsent(file, f -> {
            try (InputStream in = Minecraft.getInstance().getResourceManager().open(TConstruct.id("manuals/" + f + ".xml"))) {
                Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(in);
                doc.getDocumentElement().normalize();
                return doc;
            } catch (Exception e) {
                LOGGER.error("Could not read the manual {}", f, e);
                return null;
            }
        });
    }

    @Override
    protected void init() {
        if (this.manual == null) {
            return;
        }
        this.maxPages = this.manual.getElementsByTagName("page").getLength();
        this.updateText();
        int x = this.width / 2;
        this.addRenderableWidget(new TurnPage(x + BOOK_WIDTH - 50, 180, true));
        this.addRenderableWidget(new TurnPage(x - BOOK_WIDTH + 24, 180, false));
    }

    private void turn(boolean next) {
        this.currentPage += next ? 2 : -2;
        this.updateText();
    }

    private ManualPages.@Nullable Page page(@Nullable Node node, int side) {
        if (node instanceof Element element) {
            Supplier<ManualPages.Page> factory = PAGES.get(element.getAttribute("type"));
            if (factory != null) {
                try {
                    ManualPages.Page page = factory.get();
                    page.init(this, side);
                    page.read(element);
                    return page;
                } catch (Exception e) {
                    LOGGER.warn("Bad manual page {}", element.getAttribute("type"), e);
                }
            }
        }
        return null;
    }

    private void updateText() {
        if (this.maxPages % 2 == 1) {
            if (this.currentPage > this.maxPages) {
                this.currentPage = this.maxPages;
            }
        } else if (this.currentPage >= this.maxPages) {
            this.currentPage = this.maxPages - 2;
        }
        if (this.currentPage % 2 == 1) {
            this.currentPage--;
        }
        if (this.currentPage < 0) {
            this.currentPage = 0;
        }
        NodeList pages = this.manual.getElementsByTagName("page");
        this.pageLeft = this.page(pages.item(this.currentPage), 0);
        this.pageRight = this.page(pages.item(this.currentPage + 1), 1);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        this.g = g;
        int right = this.width / 2;
        int top = 8;
        g.blit(RenderPipelines.GUI_TEXTURED, BOOK_RIGHT, right, top, 0, 0, BOOK_WIDTH, BOOK_HEIGHT, 256, 256);
        int left = right - BOOK_WIDTH;
        g.blit(RenderPipelines.GUI_TEXTURED, BOOK_LEFT, left, top, 256 - BOOK_WIDTH, 0, BOOK_WIDTH, BOOK_HEIGHT, 256, 256);
        super.extractRenderState(g, mouseX, mouseY, partialTick);
        if (this.pageLeft != null) {
            this.pageLeft.background(left + 16, top + 12);
        }
        if (this.pageRight != null) {
            this.pageRight.background(left + 220, top + 12);
        }
        if (this.pageLeft != null) {
            this.pageLeft.content(left + 16, top + 12);
        }
        if (this.pageRight != null) {
            this.pageRight.content(left + 220, top + 12);
        }
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    // ------------------------------------------------------------------ drawing helpers for the pages

    Font font() {
        return this.font;
    }

    Component small(String text) {
        return Component.literal(text).withStyle(SMALL);
    }

    void drawString(String text, int x, int y) {
        this.g.text(this.font, this.small(text), x, y, 0xFF000000, false);
    }

    int stringWidth(String text) {
        return this.font.width(this.small(text));
    }

    /** drawSplitString: wrapped at {@code width}, FONT_HEIGHT (9) pixels per line. */
    void drawSplit(String text, int x, int y, int width) {
        for (String paragraph : text.split("\n", -1)) {
            if (paragraph.isEmpty()) {
                y += 9;
                continue;
            }
            for (FormattedCharSequence line : this.font.split(this.small(paragraph), width)) {
                this.g.text(this.font, line, x, y, 0xFF000000, false);
                y += 9;
            }
        }
    }

    void blit(Identifier texture, int x, int y, int u, int v, int w, int h) {
        this.g.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, u, v, w, h, 256, 256);
    }

    void item(ItemStack stack, int x, int y) {
        if (stack != null && !stack.isEmpty()) {
            this.g.item(stack, x, y);
        }
    }

    /** An item at twice the size (the glScalef(2) of the pages): x and y are in the doubled coordinates. */
    void bigItem(ItemStack stack, int x, int y, boolean count) {
        if (stack == null || stack.isEmpty()) {
            return;
        }
        this.g.pose().pushMatrix();
        this.g.pose().scale(2.0F, 2.0F);
        this.g.item(stack, x, y);
        if (count && stack.getCount() > 1) {
            this.g.itemDecorations(this.font, stack, x, y, String.valueOf(stack.getCount()));
        }
        this.g.pose().popMatrix();
    }

    /** TurnPageButton: the arrows of bookleft.png. */
    private class TurnPage extends AbstractButton {
        private final boolean next;

        TurnPage(int x, int y, boolean next) {
            super(x, y, 23, 13, Component.empty());
            this.next = next;
        }

        @Override
        protected void extractContents(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
            int u = this.isHovered() ? 23 : 0;
            int v = this.next ? 192 : 205;
            g.blit(RenderPipelines.GUI_TEXTURED, BOOK_LEFT, this.getX(), this.getY(), u, v, 23, 13, 256, 256);
        }

        @Override
        public void onPress(InputWithModifiers input) {
            ManualScreen.this.turn(this.next);
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput output) {
            this.defaultButtonNarrationText(output);
        }
    }
}
