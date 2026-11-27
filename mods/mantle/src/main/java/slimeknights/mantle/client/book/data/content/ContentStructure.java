package slimeknights.mantle.client.book.data.content;

import lombok.Getter;
import net.minecraft.resources.Identifier;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.client.book.data.BookData;
import slimeknights.mantle.client.book.data.element.TextData;
import slimeknights.mantle.client.screen.book.BookScreen;
import slimeknights.mantle.client.screen.book.element.BookElement;
import slimeknights.mantle.client.screen.book.element.TextElement;
import slimeknights.mantle.util.html.HtmlElement;
import slimeknights.mantle.util.html.HtmlGroup;
import slimeknights.mantle.util.html.HtmlSerializable;

import java.util.ArrayList;

/**
 * Book page showing a structure ({@code "type": "mantle:structure"}).
 * Port (Teil 27c): the 3D preview needs a picture-in-picture renderer in 26.3 and follows in Teil 27k (original
 * classes in {@code mods/mantle/todo-27k}). Until then the page shows its title and description, the structure file
 * ({@code data}) is read but not drawn.
 */
public class ContentStructure extends PageContent {
  public static final transient Identifier ID = Mantle.getResource("structure");

  @Getter
  public String title;
  public String data;
  public TextData[] description;

  @Override
  public void build(BookData book, ArrayList<BookElement> list, boolean rightSide) {
    int y = getTitleHeight();
    if (this.title == null || this.title.isEmpty()) {
      y = 0;
    } else {
      this.addTitle(list, this.title);
    }
    if (this.description != null && this.description.length > 0) {
      list.add(new TextElement(0, y, BookScreen.PAGE_WIDTH, BookScreen.PAGE_HEIGHT - y - 10, this.description));
    }
  }

  @Override
  public HtmlSerializable toHTML(BookData book) {
    return HtmlGroup.indent().add(
      makeTitleHTML(),
      HtmlElement.div().add(TextData.toHtml(description, book))
    );
  }
}
