package bibliocraft.printing;

import bibliocraft.BiblioCraft;
import io.netty.buffer.ByteBuf;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.Filterable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.WritableBookContent;
import net.minecraft.world.item.component.WrittenBookContent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * The client-to-server packets of BiblioPacketeer. The original sent a whole item stack for "BiblioMCBEdit" and the
 * server put it on the desk as it came; here only the pages (and the title when signing) are sent, and the server
 * builds the book from the one on the desk after checking the player is at the desk, a writable book is there and the
 * limits (the screen writes up to 50 pages of up to 255 characters as in 1.5.2; the packet takes the vanilla book
 * limits so that a longer book written elsewhere can still be edited; a title of 1..16 characters).
 */
public final class PrintingNetwork {
    public static final int MAX_PAGES = 50;
    public static final int MAX_PAGE_LENGTH = 255;
    public static final int MAX_TITLE_LENGTH = 16;

    private PrintingNetwork() {
    }

    /** "BiblioMCBEdit": the edited pages of the writable book on the desk, signed with {@code title} if {@code sign}. */
    public record BookEdit(BlockPos pos, List<String> pages, boolean sign, String title, int page) implements CustomPacketPayload {
        public static final Type<BookEdit> TYPE = new Type<>(BiblioCraft.id("book_edit"));
        public static final StreamCodec<ByteBuf, BookEdit> CODEC = StreamCodec.composite(
                BlockPos.STREAM_CODEC, BookEdit::pos,
                // the vanilla limits: a writable book from elsewhere may hold longer pages than this screen writes
                ByteBufCodecs.stringUtf8(WritableBookContent.PAGE_EDIT_LENGTH).apply(ByteBufCodecs.list(WritableBookContent.MAX_PAGES)), BookEdit::pages,
                ByteBufCodecs.BOOL, BookEdit::sign,
                ByteBufCodecs.stringUtf8(MAX_TITLE_LENGTH), BookEdit::title,
                ByteBufCodecs.VAR_INT, BookEdit::page,
                BookEdit::new);

        @Override
        public Type<BookEdit> type() {
            return TYPE;
        }
    }

    /** "BiblioMCBPage": the page the book on the desk was turned to. */
    public record BookPage(BlockPos pos, int page) implements CustomPacketPayload {
        public static final Type<BookPage> TYPE = new Type<>(BiblioCraft.id("book_page"));
        public static final StreamCodec<ByteBuf, BookPage> CODEC = StreamCodec.composite(
                BlockPos.STREAM_CODEC, BookPage::pos, ByteBufCodecs.VAR_INT, BookPage::page, BookPage::new);

        @Override
        public Type<BookPage> type() {
            return TYPE;
        }
    }

    /** "BiblioType": the saved book chosen in the typesetting table's list. */
    public record TypeBook(BlockPos pos, String book) implements CustomPacketPayload {
        public static final Type<TypeBook> TYPE = new Type<>(BiblioCraft.id("type_book"));
        public static final StreamCodec<ByteBuf, TypeBook> CODEC = StreamCodec.composite(
                BlockPos.STREAM_CODEC, TypeBook::pos, ByteBufCodecs.stringUtf8(1024), TypeBook::book, TypeBook::new);

        @Override
        public Type<TypeBook> type() {
            return TYPE;
        }
    }

    static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(BookEdit.TYPE, BookEdit.CODEC, PrintingNetwork::bookEdit);
        registrar.playToServer(BookPage.TYPE, BookPage.CODEC, PrintingNetwork::bookPage);
        registrar.playToServer(TypeBook.TYPE, TypeBook.CODEC, PrintingNetwork::typeBook);
    }

    private static WritingDeskBlockEntity desk(IPayloadContext context, BlockPos pos) {
        if (context.player() instanceof ServerPlayer player && player.level().isLoaded(pos)
                && player.level().getBlockEntity(pos) instanceof WritingDeskBlockEntity desk && desk.stillValid(player)) {
            return desk;
        }
        return null;
    }

    /** handleBookEdit: overwriteWrittenBook + setCurrentPage. */
    private static void bookEdit(BookEdit payload, IPayloadContext context) {
        WritingDeskBlockEntity desk = desk(context, payload.pos());
        if (desk == null) {
            return;
        }
        ItemStack book = desk.openBook();
        if (!book.is(Items.WRITABLE_BOOK) || payload.pages().isEmpty()) {
            return;
        }
        List<String> pages = payload.pages();
        if (payload.sign()) {
            String title = payload.title().trim();
            if (title.isEmpty()) {
                return;
            }
            ItemStack written = book.transmuteCopy(Items.WRITTEN_BOOK);
            written.remove(DataComponents.WRITABLE_BOOK_CONTENT);
            List<Filterable<Component>> text = pages.stream().map(p -> Filterable.passThrough((Component) Component.literal(p))).toList();
            written.set(DataComponents.WRITTEN_BOOK_CONTENT,
                    new WrittenBookContent(Filterable.passThrough(title), context.player().getPlainTextName(), 0, text, true));
            desk.overwriteWrittenBook(written);
        } else {
            ItemStack edited = book.copy();
            edited.set(DataComponents.WRITABLE_BOOK_CONTENT, new WritableBookContent(pages.stream().map(Filterable::passThrough).toList()));
            desk.overwriteWrittenBook(edited);
        }
        desk.setCurrentPage(Math.max(0, Math.min(payload.page(), WritableBookContent.MAX_PAGES - 1)));
    }

    /** handleBookPageUpdate. */
    private static void bookPage(BookPage payload, IPayloadContext context) {
        WritingDeskBlockEntity desk = desk(context, payload.pos());
        if (desk != null) {
            desk.setCurrentPage(Math.max(0, Math.min(payload.page(), 99)));
        }
    }

    /** handleBookNameUpdate: only a name from the server's book list is taken. */
    private static void typeBook(TypeBook payload, IPayloadContext context) {
        if (context.player() instanceof ServerPlayer player && player.level().isLoaded(payload.pos())
                && player.level().getBlockEntity(payload.pos()) instanceof TypesettingBlockEntity tile && tile.stillValid(player)
                && BookFiles.list(player.level().getServer()).contains(payload.book())) {
            tile.setBookname(payload.book());
        }
    }
}
