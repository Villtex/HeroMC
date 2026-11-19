package bibliocraft.printing.client;

import bibliocraft.printing.PrintingNetwork;
import bibliocraft.printing.WritingDeskBlockEntity;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.BookViewScreen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.Identifier;
import net.minecraft.server.network.Filterable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.WritableBookContent;
import net.minecraft.world.item.component.WrittenBookContent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import com.mojang.blaze3d.platform.InputConstants;

/**
 * GuiScreenBookWriting: the 1.5.2 book screen for the open book on a writing desk. A writable book can be written
 * in (up to 50 pages of 255 characters, a page must fit 13 lines of 118 pixels), signed with a title of up to 16
 * characters, a signed book can be read. It starts at the desk's page; turning a page sends the page to the server
 * ("BiblioMCBPage"), Done and signing send the pages ("BiblioMCBEdit", only when something changed). Escape closes
 * without sending the text, as before.
 */
public class BookWritingScreen extends Screen {
    private static final int BOOK_W = 192;
    private static final int BOOK_H = 192;
    private final BlockPos pos;
    private final boolean bookIsUnsigned;
    private final List<String> pages = new ArrayList<>();
    private final List<Component> signedPages = new ArrayList<>();
    private boolean bookModified;
    private boolean editingTitle;
    private int updateCount;
    private int bookTotalPages = 1;
    private int currPage;
    private String bookTitle = "";
    private NextPageButton buttonNextPage;
    private NextPageButton buttonPreviousPage;
    private Button buttonDone;
    private Button buttonSign;
    private Button buttonFinalize;
    private Button buttonCancel;

    public BookWritingScreen(WritingDeskBlockEntity desk, boolean editable) {
        super(Component.translatable("book.edit.title"));
        this.pos = desk.getBlockPos();
        this.bookIsUnsigned = editable;
        this.currPage = desk.currentPage();
        ItemStack book = desk.openBook();
        if (editable) {
            WritableBookContent content = book.get(DataComponents.WRITABLE_BOOK_CONTENT);
            if (content != null) {
                content.pages().forEach(p -> this.pages.add(p.raw()));
            }
            // a writable book without pages starts with one empty page (bookPages == null of the original)
            if (this.pages.isEmpty()) {
                this.pages.add("");
            }
            this.bookTotalPages = this.pages.size();
        } else {
            WrittenBookContent content = book.get(DataComponents.WRITTEN_BOOK_CONTENT);
            if (content != null) {
                content.pages().stream().map(Filterable::raw).forEach(this.signedPages::add);
            }
            this.bookTotalPages = Math.max(1, this.signedPages.size());
        }
    }

    @Override
    public void tick() {
        super.tick();
        this.updateCount++;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    protected void init() {
        int top = 4 + BOOK_H;
        if (this.bookIsUnsigned) {
            this.buttonSign = this.addRenderableWidget(Button.builder(Component.translatable("book.signButton"), b -> this.action(3))
                    .bounds(this.width / 2 - 100, top, 98, 20).build());
            this.buttonDone = this.addRenderableWidget(Button.builder(Component.translatable("gui.done"), b -> this.action(0))
                    .bounds(this.width / 2 + 2, top, 98, 20).build());
            this.buttonFinalize = this.addRenderableWidget(Button.builder(Component.translatable("book.finalizeButton"), b -> this.action(5))
                    .bounds(this.width / 2 - 100, top, 98, 20).build());
            this.buttonCancel = this.addRenderableWidget(Button.builder(Component.translatable("gui.cancel"), b -> this.action(4))
                    .bounds(this.width / 2 + 2, top, 98, 20).build());
        } else {
            this.buttonDone = this.addRenderableWidget(Button.builder(Component.translatable("gui.done"), b -> this.action(0))
                    .bounds(this.width / 2 - 100, top, 200, 20).build());
        }
        int left = (this.width - BOOK_W) / 2;
        this.buttonNextPage = this.addRenderableWidget(new NextPageButton(left + 120, 2 + 154, true, b -> this.action(1)));
        this.buttonPreviousPage = this.addRenderableWidget(new NextPageButton(left + 38, 2 + 154, false, b -> this.action(2)));
        this.updateButtons();
    }

    private void updateButtons() {
        this.buttonNextPage.visible = !this.editingTitle && (this.currPage < this.bookTotalPages - 1 || this.bookIsUnsigned);
        this.buttonPreviousPage.visible = !this.editingTitle && this.currPage > 0;
        this.buttonDone.visible = !this.bookIsUnsigned || !this.editingTitle;
        if (this.bookIsUnsigned) {
            this.buttonSign.visible = !this.editingTitle;
            this.buttonCancel.visible = this.editingTitle;
            this.buttonFinalize.visible = this.editingTitle;
            this.buttonFinalize.active = !this.bookTitle.trim().isEmpty();
        }
    }

    /** sendBookToServer: trailing empty pages are dropped. */
    private void sendBookToServer(boolean sign) {
        if (this.bookIsUnsigned && this.bookModified) {
            while (this.pages.size() > 1 && this.pages.getLast().isEmpty()) {
                this.pages.removeLast();
            }
            ClientPacketDistributor.sendToServer(new PrintingNetwork.BookEdit(this.pos, List.copyOf(this.pages), sign, this.bookTitle.trim(),
                    this.currPage));
        }
    }

    /** actionPerformed by button id (0 done, 1 next, 2 previous, 3 sign, 4 cancel, 5 finalize). */
    private void action(int id) {
        if (id == 0) {
            this.minecraft.gui.setScreen(null);
            this.sendBookToServer(false);
        } else if (id == 3 && this.bookIsUnsigned) {
            this.editingTitle = true;
        } else if (id == 1) {
            if (this.currPage < this.bookTotalPages - 1) {
                this.currPage++;
            } else if (this.bookIsUnsigned) {
                this.addNewPage();
                if (this.currPage < this.bookTotalPages - 1) {
                    this.currPage++;
                }
            }
        } else if (id == 2) {
            if (this.currPage > 0) {
                this.currPage--;
            }
        } else if (id == 5 && this.editingTitle) {
            this.sendBookToServer(true);
            this.minecraft.gui.setScreen(null);
        } else if (id == 4 && this.editingTitle) {
            this.editingTitle = false;
        }
        this.updateButtons();
        ClientPacketDistributor.sendToServer(new PrintingNetwork.BookPage(this.pos, this.currPage));
    }

    private void addNewPage() {
        if (this.pages.size() < PrintingNetwork.MAX_PAGES) {
            this.pages.add("");
            this.bookTotalPages++;
            this.bookModified = true;
        }
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (super.keyPressed(event)) {
            return true;
        }
        if (!this.bookIsUnsigned) {
            return false;
        }
        int key = event.key();
        if (this.editingTitle) {
            if (key == InputConstants.KEY_BACKSPACE) {
                if (!this.bookTitle.isEmpty()) {
                    this.bookTitle = this.bookTitle.substring(0, this.bookTitle.length() - 1);
                    this.updateButtons();
                }
                return true;
            }
            if (event.isConfirmation()) {
                if (!this.bookTitle.isEmpty()) {
                    this.sendBookToServer(true);
                    this.minecraft.gui.setScreen(null);
                }
                return true;
            }
            return false;
        }
        if (event.isPaste()) {
            this.append(this.minecraft.keyboardHandler.getClipboard());
            return true;
        }
        if (key == InputConstants.KEY_BACKSPACE) {
            String page = this.page();
            if (!page.isEmpty()) {
                this.setPage(page.substring(0, page.length() - 1));
            }
            return true;
        }
        if (event.isConfirmation()) {
            this.append("\n");
            return true;
        }
        return false;
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        if (!this.bookIsUnsigned || !event.isAllowedChatCharacter()) {
            return false;
        }
        if (this.editingTitle) {
            if (this.bookTitle.length() < PrintingNetwork.MAX_TITLE_LENGTH) {
                this.bookTitle = this.bookTitle + event.codepointAsString();
                this.updateButtons();
                this.bookModified = true;
            }
        } else {
            this.append(event.codepointAsString());
        }
        return true;
    }

    /** func_74158_i: the text of the current page. */
    private String page() {
        return this.currPage >= 0 && this.currPage < this.pages.size() ? this.pages.get(this.currPage) : "";
    }

    /** func_74159_a. */
    private void setPage(String text) {
        if (this.currPage >= 0 && this.currPage < this.pages.size()) {
            this.pages.set(this.currPage, text);
            this.bookModified = true;
        }
    }

    /** func_74160_b: appends if the page still fits (with the cursor) in 118 pixels height and stays below 256 characters. */
    private void append(String text) {
        String page = this.page() + text;
        int height = this.font.wordWrapHeight(FormattedText.of(page + "§0_"), 118);
        if (height <= 118 && page.length() <= PrintingNetwork.MAX_PAGE_LENGTH) {
            this.setPage(page);
        }
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractBackground(graphics, mouseX, mouseY, a);
        graphics.blit(RenderPipelines.GUI_TEXTURED, BookViewScreen.BOOK_LOCATION, (this.width - BOOK_W) / 2, 2, 0.0F, 0.0F, BOOK_W, BOOK_H, 256, 256);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractRenderState(graphics, mouseX, mouseY, a);
        int left = (this.width - BOOK_W) / 2;
        int top = 2;
        int black = 0xFF000000;
        if (this.editingTitle) {
            String title = this.bookTitle + (this.updateCount / 6 % 2 == 0 ? "§0_" : "§7_");
            String edit = Component.translatable("book.editTitle").getString();
            graphics.text(this.font, edit, left + 36 + (116 - this.font.width(edit)) / 2, top + 16 + 16, black, false);
            graphics.text(this.font, title, left + 36 + (116 - this.font.width(title)) / 2, top + 48, black, false);
            String author = Component.translatable("book.byAuthor", this.minecraft.player == null ? "" : this.minecraft.player.getName().getString())
                    .getString();
            graphics.text(this.font, "§8" + author, left + 36 + (116 - this.font.width(author)) / 2, top + 48 + 10, black, false);
            graphics.textWithWordWrap(this.font, Component.translatable("book.finalizeWarning"), left + 36, top + 80, 116, black, false);
        } else {
            String indicator = Component.translatable("book.pageIndicator", this.currPage + 1, this.bookTotalPages).getString();
            graphics.text(this.font, indicator, left - this.font.width(indicator) + BOOK_W - 44, top + 16, black, false);
            if (this.bookIsUnsigned) {
                String text = this.page();
                if (this.font.isBidirectional()) {
                    text = text + "_";
                } else {
                    text = text + (this.updateCount / 6 % 2 == 0 ? "§0_" : "§7_");
                }
                graphics.textWithWordWrap(this.font, FormattedText.of(text), left + 36, top + 16 + 16, 116, black, false);
            } else if (this.currPage >= 0 && this.currPage < this.signedPages.size()) {
                graphics.textWithWordWrap(this.font, this.signedPages.get(this.currPage), left + 36, top + 16 + 16, 116, black, false);
            }
        }
    }

    /** GuiButtonNextPage: the page arrows of the book texture (now sprites), 23 x 13, with the button click. */
    static final class NextPageButton extends Button {
        private static final Identifier FORWARD = Identifier.withDefaultNamespace("widget/page_forward");
        private static final Identifier FORWARD_HIGHLIGHTED = Identifier.withDefaultNamespace("widget/page_forward_highlighted");
        private static final Identifier BACKWARD = Identifier.withDefaultNamespace("widget/page_backward");
        private static final Identifier BACKWARD_HIGHLIGHTED = Identifier.withDefaultNamespace("widget/page_backward_highlighted");
        private final boolean nextPage;

        NextPageButton(int x, int y, boolean nextPage, OnPress onPress) {
            super(x, y, 23, 13, Component.translatable(nextPage ? "book.page_button.next" : "book.page_button.previous"), onPress, DEFAULT_NARRATION);
            this.nextPage = nextPage;
        }

        @Override
        protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
            boolean hover = this.isHovered();
            Identifier sprite = this.nextPage ? (hover ? FORWARD_HIGHLIGHTED : FORWARD) : (hover ? BACKWARD_HIGHLIGHTED : BACKWARD);
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, this.getX(), this.getY(), 23, 13);
        }
    }
}
