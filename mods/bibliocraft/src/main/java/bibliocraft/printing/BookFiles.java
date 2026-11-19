package bibliocraft.printing;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.Filterable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.WrittenBookContent;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.fml.loading.FMLPaths;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

/**
 * FileUtil: books saved on the typesetting table are text files named "author, title" in a folder "books" (on a
 * dedicated server in the world folder, in single player in the game's config folder, as getSaveDir did). The file
 * is the title, the author, and for every page a line "#pgx&lt;n&gt;" followed by the page text. Only the server reads
 * and writes them; the client gets the list of names through the typesetting table.
 *
 * <p>Differences: the names are sorted (listFiles order is not defined); characters that cannot be in a file name
 * ('/', '\\', ':' ...) become '_' so a title cannot point outside the folder; pages are written as plain text
 * (Component.getString), which is what the 1.5.2 pages were.
 */
public final class BookFiles {
    private static final Logger LOGGER = LogUtils.getLogger();

    private BookFiles() {
    }

    /** getSaveDir: &lt;world&gt;/books on a dedicated server, config/books otherwise; made if missing. */
    public static Path dir(MinecraftServer server) {
        Path storage = server.isDedicatedServer() ? server.getWorldPath(LevelResource.ROOT) : FMLPaths.CONFIGDIR.get();
        Path books = storage.resolve("books").normalize();
        try {
            Files.createDirectories(books);
        } catch (IOException e) {
            LOGGER.warn("Cannot create the book folder {}", books, e);
        }
        return books;
    }

    /** scanBookDir: the file names in the book folder. */
    public static List<String> list(MinecraftServer server) {
        List<String> names = new ArrayList<>();
        try (Stream<Path> files = Files.list(dir(server))) {
            files.map(p -> p.getFileName().toString()).sorted().forEach(names::add);
        } catch (IOException e) {
            LOGGER.warn("Cannot list the book folder", e);
        }
        return names;
    }

    /** The book list as the tile entity kept it: names joined with "_". */
    public static String listString(MinecraftServer server) {
        return String.join("_", list(server));
    }

    /** "author, title" made safe as a file name. */
    static String fileName(String author, String title) {
        StringBuilder b = new StringBuilder();
        for (char c : (author + ", " + title).toCharArray()) {
            b.append(c < 32 || "/\\:*?\"<>|".indexOf(c) >= 0 ? '_' : c);
        }
        return b.toString();
    }

    private static Path file(MinecraftServer server, WrittenBookContent content) {
        return dir(server).resolve(fileName(content.author(), content.title().raw()));
    }

    /** isBookSaved: a signed book whose file exists. */
    public static boolean isSaved(ItemStack stack, MinecraftServer server) {
        WrittenBookContent content = stack.get(DataComponents.WRITTEN_BOOK_CONTENT);
        return content != null && Files.exists(file(server, content));
    }

    /** saveBook: writes a signed book unless its file is already there. */
    public static void save(ItemStack stack, MinecraftServer server) {
        WrittenBookContent content = stack.get(DataComponents.WRITTEN_BOOK_CONTENT);
        if (content == null) {
            return;
        }
        Path book = file(server, content);
        if (Files.exists(book)) {
            return;
        }
        try (Writer writer = Files.newBufferedWriter(book, StandardCharsets.UTF_8)) {
            writer.write(content.title().raw());
            writer.write("\n");
            writer.write(content.author());
            writer.write("\n");
            List<Filterable<Component>> pages = content.pages();
            for (int i = 0; i < pages.size(); i++) {
                writer.write("#pgx" + i);
                writer.write("\n");
                writer.write(pages.get(i).raw().getString());
                writer.write("\n");
            }
        } catch (IOException e) {
            LOGGER.warn("Cannot save the book {}", book, e);
        }
    }

    /**
     * getBookNumber(world, authortitle): the last book whose file name contains the text, or -1. An empty plate name
     * (a plate without data) matches every file and so gives the last book, as in the original.
     */
    public static int bookNumber(MinecraftServer server, String authortitle) {
        List<String> books = list(server);
        int bookint = -1;
        for (int x = 0; x < books.size(); x++) {
            if (books.get(x).contains(authortitle)) {
                bookint = x;
            }
        }
        return bookint;
    }

    /** loadBook: a written book with the title, author and pages of book number {@code bookint}, or EMPTY. */
    public static ItemStack load(MinecraftServer server, int bookint) {
        List<String> books = list(server);
        if (bookint < 0 || bookint >= books.size()) {
            return ItemStack.EMPTY;
        }
        Path book = dir(server).resolve(books.get(bookint));
        String title = "";
        String author = "";
        List<Filterable<Component>> pages = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(book, StandardCharsets.UTF_8)) {
            String line = reader.readLine();
            if (line != null) {
                title = line;
                line = reader.readLine();
                author = line == null ? "" : line;
                line = reader.readLine();
            }
            // the page parser of the original: empty lines inside a page become " "
            while (line != null) {
                if (!line.contains("#pgx")) {
                    StringBuilder pagetag = new StringBuilder(line);
                    line = reader.readLine();
                    if (line != null) {
                        if (line.isEmpty()) {
                            line = " ";
                        }
                        while (!line.contains("#pgx")) {
                            pagetag.append("\n").append(line);
                            line = reader.readLine();
                            if (line == null) {
                                break;
                            }
                            if (line.isEmpty()) {
                                line = " ";
                            }
                        }
                    }
                    pages.add(Filterable.passThrough(Component.literal(pagetag.toString())));
                } else {
                    line = reader.readLine();
                }
            }
        } catch (IOException e) {
            LOGGER.warn("Cannot read the book {}", book, e);
            return ItemStack.EMPTY;
        }
        ItemStack stack = new ItemStack(Items.WRITTEN_BOOK);
        stack.set(DataComponents.WRITTEN_BOOK_CONTENT, new WrittenBookContent(Filterable.passThrough(title), author, 0, pages, true));
        return stack;
    }
}
