package eplus.client;

import com.mojang.blaze3d.platform.InputConstants;
import eplus.EnchantException;
import eplus.EnchantHelper;
import eplus.EnchantTableMenu;
import eplus.EnchantingPlus;
import eplus.EplusConfig;
import eplus.network.EnchantPayload;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.TooltipRenderUtil;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.locale.Language;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

/**
 * GuiModTable: the item slot top left, the list of enchantments (four rows visible, scroll bar on the right) with a
 * level slider per row, "E" (enchant) and "R" (repair) buttons, a "Vanilla" button and info boxes left of the GUI.
 * Rows of conflicting enchantments are purple, rows that cannot be paid for red. Shift shows a description.
 */
public class EnchantTableScreen extends AbstractContainerScreen<EnchantTableMenu> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(EnchantingPlus.MOD_ID, "textures/gui/enchant.png");

    private final Player player;
    private ArrayList<GuiItem> enchantmentArray = new ArrayList<>();
    private double sliderIndex = 0.0;
    private double enchantingPages = 0.0;
    private double sliderY = 0.0;
    private Map<Holder<Enchantment>, Integer> enchantments;
    private boolean sliding = false;
    private ItemStack shownItem = ItemStack.EMPTY;
    private int totalCost = 0;
    private boolean dirty = false;
    private Component error = Component.empty();

    public EnchantTableScreen(EnchantTableMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 209, 182);
        this.player = inventory.player;
    }

    @Override
    protected void init() {
        super.init();
        // GuiIcon with customTexture(0) is a plain 20x20 button
        this.addRenderableWidget(Button.builder(Component.literal("E"), b -> this.actionPerformed(0))
                .bounds(this.leftPos + 9, this.topPos + 40, 20, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("R"), b -> this.actionPerformed(1))
                .bounds(this.leftPos + 9, this.topPos + 60, 20, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("Vanilla"), b -> this.actionPerformed(2))
                .bounds(this.leftPos + this.imageWidth + 10, this.topPos + 5, this.font.width("Vanilla") + 10, 20).build());
        this.dirty = true;
    }

    private void actionPerformed(int id) {
        Map<Holder<Enchantment>, Integer> enchants = new HashMap<>();
        for (GuiItem item : this.enchantmentArray) {
            Integer start = this.enchantments.get(item.enchantment);
            if (start != null && item.enchantmentLevel != start && !item.disabled) {
                enchants.put(item.enchantment, item.enchantmentLevel);
            }
        }
        switch (id) {
            case 0 -> {
                if (!enchants.isEmpty()) {
                    ClientPacketDistributor.sendToServer(new EnchantPayload(enchants, this.totalCost));
                }
            }
            case 1 -> {
                if (enchants.isEmpty() && this.menu.settings().allowRepair()) {
                    this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, EnchantTableMenu.BUTTON_REPAIR);
                }
            }
            default -> this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, EnchantTableMenu.BUTTON_VANILLA);
        }
    }

    private ArrayList<GuiItem> convertMapToGuiItems(Map<Holder<Enchantment>, Integer> map, int x, int y) {
        ArrayList<GuiItem> temp = new ArrayList<>();
        if (map == null) {
            return temp;
        }
        int i = 0;
        int yPos = y;
        for (Map.Entry<Holder<Enchantment>, Integer> entry : map.entrySet()) {
            temp.add(new GuiItem(entry.getKey(), entry.getValue(), x, yPos));
            yPos = y + ++i * 18;
        }
        return temp;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, this.leftPos, this.topPos, 0.0F, 0.0F, this.imageWidth, this.imageHeight, 256, 256);
        for (GuiItem item : this.enchantmentArray) {
            item.show(item.yPos >= this.topPos + 15 && item.yPos < this.topPos + 87);
            item.draw(graphics);
        }
    }

    /** No title or inventory label in the original. */
    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        int adjustedMouseX = mouseX - this.leftPos;
        int adjustedMouseY = mouseY - this.topPos;
        int tempY = adjustedMouseY - 16;
        if (tempY <= 0) {
            tempY = 0;
        }
        if (tempY >= 57) {
            tempY = 57;
        }
        this.sliderIndex = this.sliding ? round(tempY / 57.0 * this.enchantingPages, 0.25) : this.sliderIndex;
        if (this.sliderIndex >= this.enchantingPages) {
            this.sliderIndex = this.enchantingPages;
        }
        this.sliderY = this.sliding ? tempY : 57.0 * (this.sliderIndex / this.enchantingPages);
        if (Double.isNaN(this.sliderY)) {
            this.sliderY = 0.0;
        }
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, this.leftPos + 180, this.topPos + 16 + (int) this.sliderY, 0.0F, 182.0F, 12, 15, 256, 256);

        // drawScreen polled the mouse; the drag starts in mouseClicked and ends in mouseReleased today. As before,
        // leaving the row ends the drag of a slider
        for (GuiItem item : this.enchantmentArray) {
            if (item.dragging && this.getItemFromPos(mouseX, mouseY) != item) {
                item.dragging = false;
            }
        }
        for (GuiItem item : this.enchantmentArray) {
            if (item.dragging) {
                item.scroll(adjustedMouseX - 36);
            }
        }

        int maxWidth = Math.max(1, this.leftPos - 20);
        List<List<FormattedCharSequence>> information = new ArrayList<>();
        information.add(split(String.format("%s: %s", I18n.get("eplus.player.level"), this.player.experienceLevel), maxWidth));
        ItemStack stack = this.menu.item();
        if (!stack.isEmpty() && !this.levelChanged() && stack.isDamaged()) {
            if (this.menu.settings().allowRepair()) {
                information.add(split(String.format("%s: %s", I18n.get("eplus.repair.cost"), this.totalCost), maxWidth));
            }
        } else {
            information.add(split(String.format("%s: %s", I18n.get("eplus.enchanting.cost"), this.totalCost), maxWidth));
        }
        information.add(split(String.format("%s: %s", I18n.get("eplus.enchant.level.max"), this.menu.bookCases()), maxWidth));
        for (int index = 0; index < information.size(); index++) {
            int height = index == 0 ? this.topPos + this.font.lineHeight + 8 : this.topPos + (this.font.lineHeight + 8) * (index + 1);
            for (int i = index - 1; i >= 0; i--) {
                height += (this.font.lineHeight + 3) * (information.get(i).size() - 1);
            }
            drawHoveringText(graphics, this.font, information.get(index), this.leftPos - 20 - maxWidth, height, this.width, this.height);
        }

        GuiItem hovered = this.getItemFromPos(mouseX, mouseY);
        if (this.minecraft.hasShiftDown() && hovered != null) {
            List<FormattedCharSequence> display = new ArrayList<>();
            display.add(Component.literal(hovered.getTranslatedName()).withStyle(ChatFormatting.BOLD).getVisualOrderText());
            display.addAll(this.font.split(Component.literal("§5" + toolTip(hovered.enchantment)), 150));
            graphics.setTooltipForNextFrame(this.font, display, mouseX, mouseY);
        }

        if (!this.error.getString().isEmpty()) {
            drawHoveringText(graphics, this.font, List.of(this.error.getVisualOrderText()),
                    (this.imageWidth + this.leftPos) / 2 - this.font.width(this.error) / 4, this.topPos - this.font.lineHeight, this.width, this.height);
        }
    }

    /** EnchantmentHelp.getToolTip, or the original's error text for enchantments without a description. */
    private static String toolTip(Holder<Enchantment> enchantment) {
        String key = enchantment.unwrapKey().map(k -> "eplus.description." + k.identifier().getNamespace() + "." + k.identifier().getPath()).orElse("");
        if (Language.getInstance().has(key)) {
            return I18n.get(key);
        }
        return "§4" + I18n.get("eplus.error.no.description") + " " + enchantment.unwrapKey().map(k -> k.identifier().toString()).orElse("?");
    }

    private List<FormattedCharSequence> split(String text, int width) {
        return this.font.split(Component.literal(text), width);
    }

    /** MathHelper.round of the original. */
    private static double round(double value, double inc) {
        return Math.round(value / inc) * inc;
    }

    /**
     * GuiScreen.drawHoveringText of 1.5.2: a tooltip box with its text 12 right of and 12 above x, y, kept on screen.
     */
    public static void drawHoveringText(GuiGraphicsExtractor graphics, Font font, List<FormattedCharSequence> lines, int x, int y, int screenWidth,
                                        int screenHeight) {
        if (lines.isEmpty()) {
            return;
        }
        int width = 0;
        for (FormattedCharSequence line : lines) {
            width = Math.max(width, font.width(line));
        }
        int textX = x + 12;
        int textY = y - 12;
        int height = 8;
        if (lines.size() > 1) {
            height += 2 + (lines.size() - 1) * 10;
        }
        if (textX + width > screenWidth) {
            textX -= 28 + width;
        }
        if (textY + height + 6 > screenHeight) {
            textY = screenHeight - height - 6;
        }
        TooltipRenderUtil.extractTooltipBackground(graphics, textX, textY, width, height, null);
        for (int i = 0; i < lines.size(); i++) {
            graphics.text(font, lines.get(i), textX, textY, -1, true);
            if (i == 0) {
                textY += 2;
            }
            textY += 10;
        }
    }

    private GuiItem getItemFromPos(double x, double y) {
        if (x >= this.leftPos + 35 && x <= this.leftPos + this.imageWidth - 32) {
            for (GuiItem item : this.enchantmentArray) {
                if (item.show && y >= item.yPos && y <= item.yPos + item.height) {
                    return item;
                }
            }
        }
        return null;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() == InputConstants.MOUSE_BUTTON_LEFT) {
            GuiItem item = this.getItemFromPos(event.x(), event.y());
            if (item != null && !item.disabled && !item.locked) {
                item.dragging = true;
                item.scroll((int) event.x() - this.leftPos - 36);
                return true;
            }
            int x = (int) event.x() - this.leftPos;
            int y = (int) event.y() - this.topPos;
            if (x >= 180 && x <= 191 && y >= 15 && y <= 87 && this.enchantingPages != 0.0) {
                this.sliding = true;
                return true;
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
        boolean any = this.sliding;
        for (GuiItem item : this.enchantmentArray) {
            if (item.dragging) {
                item.scroll((int) event.x() - this.leftPos - 36);
                any = true;
            }
        }
        return any || super.mouseDragged(event, dx, dy);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (event.button() == InputConstants.MOUSE_BUTTON_LEFT) {
            boolean any = this.sliding;
            this.sliding = false;
            for (GuiItem item : this.enchantmentArray) {
                any |= item.dragging;
                item.dragging = false;
            }
            if (any) {
                return true;
            }
        }
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseScrolled(double x, double y, double scrollX, double scrollY) {
        int mouseX = (int) x - this.leftPos;
        int mouseY = (int) y - this.topPos;
        if (scrollY != 0 && (mouseX >= 35 && mouseX <= this.imageWidth - 32 || mouseX >= 180 && mouseX <= 192) && mouseY >= 15 && mouseY <= 87) {
            if (scrollY < 0) {
                this.sliderIndex += 0.25;
                if (this.sliderIndex >= this.enchantingPages) {
                    this.sliderIndex = this.enchantingPages;
                }
            } else {
                this.sliderIndex -= 0.25;
                if (this.sliderIndex <= 0.0) {
                    this.sliderIndex = 0.0;
                }
            }
            return true;
        }
        return super.mouseScrolled(x, y, scrollX, scrollY);
    }

    protected boolean levelChanged() {
        for (GuiItem item : this.enchantmentArray) {
            if (item.enchantmentLevel != item.privateLevel) {
                return true;
            }
        }
        return false;
    }

    /** updateScreen: rebuild the rows when the item changes, grey out conflicts, sum up the cost. */
    @Override
    protected void containerTick() {
        super.containerTick();
        Map<Holder<Enchantment>, Integer> enchantments = this.menu.getEnchantments();
        // by content: the menu reads a new map whenever its slot reports a change (also when nothing changed)
        if (!enchantments.equals(this.enchantments) || !ItemStack.matches(this.shownItem, this.menu.item())) {
            this.shownItem = this.menu.item().copy();
            this.enchantments = enchantments;
            this.enchantmentArray = this.convertMapToGuiItems(enchantments, 35 + this.leftPos, 15 + this.topPos);
            this.sliderIndex = this.enchantingPages = 0.0;
            this.sliding = false;
            this.error = Component.empty();
        }
        if (this.dirty) {
            ArrayList<GuiItem> temp = this.convertMapToGuiItems(enchantments, 35 + this.leftPos, 15 + this.topPos);
            for (GuiItem item : this.enchantmentArray) {
                for (GuiItem tempItem : temp) {
                    if (item.enchantment == tempItem.enchantment) {
                        item.startingXPos = item.xPos = tempItem.xPos;
                        item.startingYPos = item.yPos = tempItem.yPos;
                    }
                }
            }
            this.dirty = false;
        }

        boolean[] enabled = new boolean[this.enchantmentArray.size()];
        for (int i = 0; i < this.enchantmentArray.size(); i++) {
            GuiItem item = this.enchantmentArray.get(i);
            if (item.enchantmentLevel != 0) {
                for (int i1 = 0; i1 < this.enchantmentArray.size(); i1++) {
                    GuiItem item2 = this.enchantmentArray.get(i1);
                    if (item != item2) {
                        enabled[i1] = enabled[i1] || i != i1
                                && (!Enchantment.areCompatible(item.enchantment, item2.enchantment) || !Enchantment.areCompatible(item2.enchantment, item.enchantment));
                    }
                }
            }
        }
        for (int i = 0; i < this.enchantmentArray.size(); i++) {
            this.enchantmentArray.get(i).disabled = enabled[i];
        }

        this.enchantingPages = this.enchantmentArray.size() / 4.0 > 1.0 ? this.enchantmentArray.size() / 4.0 - 1.0 : 0.0;
        this.totalCost = 0;
        EplusConfig.Settings settings = this.menu.settings();
        if (!this.enchantmentArray.isEmpty() && this.levelChanged()) {
            for (GuiItem item : this.enchantmentArray) {
                item.yPos = item.startingYPos - (int) (72.0 * this.sliderIndex);
                int level = enchantments.get(item.enchantment);
                if (item.enchantmentLevel > level && !item.disabled) {
                    int temp = this.totalCost + this.menu.enchantmentCost(item.enchantment, item.enchantmentLevel, level);
                    try {
                        if (this.menu.canPurchase(this.player, temp)) {
                            item.locked = false;
                        }
                    } catch (EnchantException e) {
                        item.locked = true;
                        this.error = e.component();
                        while (item.locked && item.enchantmentLevel > 0) {
                            item.dragging = false;
                            item.enchantmentLevel--;
                            temp = this.totalCost + this.menu.enchantmentCost(item.enchantment, item.enchantmentLevel, level);
                            try {
                                if (this.menu.canPurchase(this.player, temp)) {
                                    item.locked = false;
                                }
                            } catch (EnchantException ignored) {
                            }
                        }
                    }
                    this.totalCost = temp;
                } else if (item.enchantmentLevel < level && !item.disabled) {
                    if (EnchantHelper.containsKey(EnchantHelper.restrictions(this.menu.item()), item.enchantment, item.enchantmentLevel)) {
                        this.totalCost = this.totalCost + this.menu.disenchantmentCost(item.enchantment, item.enchantmentLevel, level);
                    } else {
                        item.enchantmentLevel++;
                    }
                }
            }
        } else if (settings.allowRepair() && !this.levelChanged()) {
            this.totalCost = this.totalCost + this.menu.repairCostMax();
            for (GuiItem item : this.enchantmentArray) {
                item.yPos = item.startingYPos - (int) (72.0 * this.sliderIndex);
            }
        }
    }

    /** One row: an enchantment with its level slider. */
    private final class GuiItem {
        private final Holder<Enchantment> enchantment;
        private final int height = 18;
        private final int width = 143;
        private final int privateLevel;
        int startingXPos;
        int startingYPos;
        int yPos;
        boolean locked = false;
        private int xPos;
        private int enchantmentLevel;
        private boolean show = true;
        private boolean dragging = false;
        private int sliderX;
        private boolean disabled;

        GuiItem(Holder<Enchantment> enchantment, int level, int x, int y) {
            this.enchantment = enchantment;
            this.enchantmentLevel = level;
            this.privateLevel = level;
            this.xPos = this.startingXPos = x;
            this.yPos = this.startingYPos = y;
            this.sliderX = this.xPos + 1;
        }

        private int maxLevel() {
            return this.enchantment.value().getMaxLevel();
        }

        void draw(GuiGraphicsExtractor graphics) {
            if (!this.show) {
                return;
            }
            int indexX = this.dragging
                    ? this.sliderX
                    : (this.enchantmentLevel <= maxLevel()
                    ? (int) (this.xPos + 1 + (this.width - 6) * ((double) this.enchantmentLevel / maxLevel()))
                    : this.xPos + 1 + this.width - 6);
            graphics.fill(indexX, this.yPos + 1, indexX + 5, this.yPos - 1 + this.height, -16777216);
            graphics.text(EnchantTableScreen.this.font, this.getTranslatedName(), this.xPos + 5, this.yPos + this.height / 4, 1437269760, false);
            if (this.disabled) {
                graphics.fill(this.xPos, this.yPos + 1, this.xPos + this.width, this.yPos - 1 + this.height, 1152057343);
            } else if (this.locked) {
                graphics.fill(this.xPos, this.yPos + 1, this.xPos + this.width, this.yPos - 1 + this.height, 1157562368);
            } else {
                graphics.fill(this.xPos, this.yPos + 1, this.xPos + this.width, this.yPos - 1 + this.height, 1152013823);
            }
        }

        /** Enchantment.getTranslatedName: "Sharpness III", without a number at level 0. */
        String getTranslatedName() {
            String name = this.enchantment.value().description().getString();
            if (this.enchantmentLevel != 0) {
                name = name + " " + I18n.get("enchantment.level." + this.enchantmentLevel);
            }
            return name;
        }

        /** Dragging the slider: the level follows the mouse; going below the old level only as the config allows. */
        void scroll(int xPos) {
            if (this.disabled) {
                return;
            }
            int left = EnchantTableScreen.this.leftPos;
            this.sliderX = xPos + left + 36;
            if (this.sliderX <= left + 36) {
                this.sliderX = left + 36;
            }
            if (this.sliderX >= left + 173) {
                this.sliderX = left + 173;
            }
            float index = (float) xPos / (this.width - 12);
            int tempLevel = (int) Math.floor(this.privateLevel > maxLevel() ? this.privateLevel * index : maxLevel() * index);
            EplusConfig.Settings settings = EnchantTableScreen.this.menu.settings();
            boolean damaged = EnchantTableScreen.this.menu.item().isDamaged();
            if (this.locked) {
                if (tempLevel < this.enchantmentLevel && (!damaged || settings.allowEnchantDamaged())) {
                    this.enchantmentLevel = tempLevel;
                    this.locked = false;
                }
            } else if (tempLevel >= this.privateLevel || settings.allowDisenchanting() && !damaged || settings.allowEnchantDamaged()) {
                this.enchantmentLevel = tempLevel;
            }
            if (this.enchantmentLevel <= 0) {
                this.enchantmentLevel = 0;
            }
        }

        void show(boolean b) {
            this.show = b;
        }
    }
}
