package slimeknights.mantle.client.screen.book.element;

import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions.FontContext;
import org.joml.Vector2i;
import slimeknights.mantle.client.screen.book.BookScreen;

import java.util.List;
import java.util.stream.Stream;

public abstract class BookElement {

  /** TODO 1.21: make this field protected instead of public to ensure setter is used. */
  @Setter
  public BookScreen parent;

  protected Minecraft mc = Minecraft.getInstance();
  protected TextureManager renderEngine = this.mc.getTextureManager();

  public int x, y;

  public BookElement(int x, int y) {
    this.x = x;
    this.y = y;
  }

  public abstract void draw(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks, Font fontRenderer);

  public void drawOverlay(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks, Font fontRenderer) {
  }

  public void mouseClicked(double mouseX, double mouseY, int mouseButton) {

  }

  public void mouseReleased(double mouseX, double mouseY, int clickedMouseButton) {

  }

  public void mouseDragged(double clickX, double clickY, double mx, double my, double lastX, double lastY, int button) {

  }

  public void renderToolTip(GuiGraphicsExtractor graphics, Font fontRenderer, ItemStack stack, int x, int y) {
    List<Component> list = net.minecraft.client.gui.screens.Screen.getTooltipFromItem(this.mc, stack);

    Font font = IClientItemExtensions.of(stack).getFont(stack, FontContext.TOOLTIP);
    if (font == null) {
      font = fontRenderer;
    }

    this.drawTooltip(graphics, list, x, y, font);
  }


  private static Stream<ClientTooltipComponent> splitLine(FormattedText text, Font font, int maxWidth) {
    if (text instanceof Component component) {
      if (component.getString().isEmpty()) {
        return Stream.of(component.getVisualOrderText()).map(ClientTooltipComponent::create);
      }
    }
    return font.split(text, maxWidth).stream().map(ClientTooltipComponent::create);
  }

  /**
   * Renders a tooltip in a book.
   * Port (Teil 27c): tooltips are drawn at the end of the frame in screen space in 26.3, so the page relative mouse
   * position goes through the current pose. Wrapping keeps the page width of the original; the tooltip is then kept on
   * the screen by the vanilla positioner and drawn at the normal GUI scale (not the page scale).
   */
  public void drawTooltip(GuiGraphicsExtractor graphics, List<Component> textLines, int mouseX, int mouseY, Font font) {
    // find max width of the tooltip
    int tooltipTextWidth = textLines.stream().mapToInt(font::width).max().orElse(0);
    boolean needsWrap = false;
    int tooltipX = mouseX + 12;
    // if the max width plus the position is too wide, need to fix
    if (tooltipX + tooltipTextWidth + 4 > BookScreen.PAGE_WIDTH) {
      // if repositioning fails, we will need to wrap the tooltip
      tooltipX = mouseX - 16 - tooltipTextWidth;
      if (tooltipX < 4) {
        if (mouseX > BookScreen.PAGE_WIDTH / 2) {
          tooltipTextWidth = mouseX - 12 - 8;
        } else {
          tooltipTextWidth = BookScreen.PAGE_WIDTH - 16 - mouseX;
        }

        needsWrap = true;
      }
    }

    // map to lines, wrapping if needed
    int tooltipTextWidthF = Math.max(tooltipTextWidth, 10);
    List<FormattedCharSequence> lines = needsWrap
      ? textLines.stream().flatMap(text -> text.getString().isEmpty() ? Stream.of(text.getVisualOrderText()) : font.split(text, tooltipTextWidthF).stream()).toList()
      : textLines.stream().map(Component::getVisualOrderText).toList();

    org.joml.Vector2f screen = graphics.pose().transformPosition(mouseX, mouseY, new org.joml.Vector2f());
    graphics.setTooltipForNextFrame(font, lines, (int) screen.x, (int) screen.y);
  }

  /**
   * True if this element contains any text,
   * will not be included for HTML export
   */
  public boolean isText() {
    return false;
  }
}
