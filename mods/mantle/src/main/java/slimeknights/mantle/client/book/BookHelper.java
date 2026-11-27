package slimeknights.mantle.client.book;

import net.minecraft.world.item.ItemStack;
import slimeknights.mantle.registration.MantleData;

import javax.annotation.Nullable;
import java.util.Objects;

public class BookHelper {

  public static final String BOOK_COMPOUND = "mantle";
  public static final String BOOK_DATA_COMPOUND = "book";

  public static final String NBT_CURRENT_PAGE = "current_page";

  /**
   * Returns the current saved page on the book
   * Returns an empty string is one is not found
   *
   * @param item The book to check for a saved page on
   * @return The current saved page
   */
  public static String getCurrentSavedPage(@Nullable ItemStack item) {
    // Port: the page lives in the component mantle:book_page (was NBT mantle.book.current_page)
    if (item != null && !item.isEmpty()) {
      return Objects.requireNonNullElse(item.get(MantleData.BOOK_PAGE.get()), "");
    }
    return "";
  }

  /**
   * Saves the current open page to the given book ItemStack.
   *
   * @param stack       the current book stack
   * @param currentPage the current open page
   */
  public static void writeSavedPageToBook(ItemStack stack, String currentPage) {
    stack.set(MantleData.BOOK_PAGE.get(), currentPage);
  }
}
