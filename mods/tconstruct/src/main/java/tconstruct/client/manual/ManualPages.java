package tconstruct.client.manual;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import tconstruct.TConstruct;
import tconstruct.client.ToolStationScreen;
import tconstruct.items.PatternBuilder;
import tconstruct.tools.TMaterial;

import java.util.Locale;

/** client/pages of 1.5.2: one class per page type of the manual XML, same layout numbers. */
public final class ManualPages {
    private static final Identifier CRAFTING = TConstruct.id("textures/gui/bookcrafting.png");
    private static final Identifier FURNACE = TConstruct.id("textures/gui/bookfurnace.png");
    private static final Identifier MODIFY = TConstruct.id("textures/gui/bookmodify.png");
    private static final Identifier SMELTERY = TConstruct.id("textures/gui/booksmeltery.png");

    private ManualPages() {
    }

    private static @Nullable String text(Element element, String tag, int index) {
        NodeList nodes = element.getElementsByTagName(tag);
        return nodes.getLength() > index ? nodes.item(index).getTextContent() : null;
    }

    private static String text(Element element, String tag) {
        String text = text(element, tag, 0);
        return text == null ? "" : text;
    }

    private static ItemStack[] recipe(Element element, String tag) {
        ItemStack[] icons = ManualIcons.recipe(text(element, tag));
        return icons == null ? new ItemStack[]{ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY} : icons;
    }

    private static ItemStack at(ItemStack[] icons, int i) {
        return i < icons.length && icons[i] != null ? icons[i] : ItemStack.EMPTY;
    }

    /** BookPage. */
    public abstract static class Page {
        protected ManualScreen manual;
        protected int side;

        void init(ManualScreen manual, int side) {
            this.manual = manual;
            this.side = side;
        }

        abstract void read(Element element);

        void background(int x, int y) {
        }

        abstract void content(int x, int y);
    }

    public static class Blank extends Page {
        @Override
        void read(Element element) {
        }

        @Override
        void content(int x, int y) {
        }
    }

    /** TextPage and TitlePage ("intro"). */
    public static class Text extends Page {
        String text = "";

        @Override
        void read(Element element) {
            this.text = text(element, "text");
        }

        @Override
        void content(int x, int y) {
            this.manual.drawSplit(this.text, x, y, 178);
        }
    }

    public static class Section extends Page {
        String title = "";
        String body = "";

        @Override
        void read(Element element) {
            this.title = text(element, "title");
            this.body = text(element, "text");
        }

        @Override
        void content(int x, int y) {
            this.manual.drawSplit("§n" + this.title, x + 70, y + 4, 178);
            this.manual.drawSplit(this.body, x, y + 16, 190);
        }
    }

    public static class Picture extends Page {
        String text = "";
        Identifier location;

        @Override
        void read(Element element) {
            this.text = text(element, "text");
            String path = text(element, "location");
            String file = path.substring(path.lastIndexOf('/') + 1).replace(".png", "");
            this.location = TConstruct.id("textures/manual/" + file + ".png");
        }

        @Override
        void background(int x, int y) {
            this.manual.blit(this.location, x, y + 12, 0, 0, 170, 144);
        }

        @Override
        void content(int x, int y) {
            this.manual.drawSplit(this.text, x + 8, y, 178);
        }
    }

    /** ContentsTablePage: icon and text per link. */
    public static class Contents extends Page {
        String text = "";
        String[] iconText = new String[0];
        ItemStack[] icons = new ItemStack[0];

        @Override
        void read(Element element) {
            this.text = text(element, "text");
            NodeList links = element.getElementsByTagName("link");
            this.iconText = new String[links.getLength()];
            this.icons = new ItemStack[links.getLength()];
            for (int i = 0; i < links.getLength(); i++) {
                Element link = (Element) links.item(i);
                this.iconText[i] = text(link, "text");
                this.icons[i] = ManualIcons.icon(text(link, "icon"));
            }
        }

        @Override
        void content(int x, int y) {
            this.manual.drawString("§n" + this.text, x + 25 + this.manual.stringWidth(this.text) / 2, y + 4);
            for (int i = 0; i < this.icons.length; i++) {
                this.manual.item(this.icons[i], x + 16, y + 18 * i + 18);
                int yOffset = this.iconText[i].length() > 40 ? 13 : 18;
                this.manual.drawString(this.iconText[i], x + 38, y + 18 * i + yOffset);
            }
        }
    }

    /** SidebarPage: text, then the items with their texts. */
    public static class Sidebar extends Page {
        String text = "";
        String[] iconText = new String[0];
        ItemStack[] icons = new ItemStack[0];

        @Override
        void read(Element element) {
            this.text = text(element, "text");
            NodeList items = element.getElementsByTagName("item");
            this.iconText = new String[items.getLength()];
            this.icons = new ItemStack[items.getLength()];
            for (int i = 0; i < items.getLength(); i++) {
                Element item = (Element) items.item(i);
                this.iconText[i] = text(item, "text");
                this.icons[i] = ManualIcons.icon(text(item, "icon"));
            }
        }

        @Override
        void content(int x, int y) {
            this.manual.drawSplit(this.text, x, y, 178);
            int offset = this.text.length() / 4 + 10;
            for (int i = 0; i < this.icons.length; i++) {
                this.manual.item(this.icons[i], x + 8, y + 18 * i + offset);
                this.manual.drawSplit(this.iconText[i], x + 30, y + 18 * i + offset, 140);
            }
        }
    }

    /** CraftingPage: a 2 x 2 or 3 x 3 grid of bookcrafting.png with the items at twice the size. */
    public static class Crafting extends Page {
        String text = "";
        String size = "";
        ItemStack[] icons = new ItemStack[0];

        @Override
        void read(Element element) {
            this.text = text(element, "text");
            this.icons = recipe(element, "name");
            this.size = text(element, "size");
        }

        private int shift() {
            return this.size.equals("three") && this.side != 1 ? 6 : 0;
        }

        @Override
        void background(int x, int y) {
            x += this.shift();
            y += 12;
            if (this.size.equals("two")) {
                this.manual.blit(CRAFTING, x + 8, y + 46, 0, 116, 154, 78);
            } else if (this.size.equals("three")) {
                this.manual.blit(CRAFTING, x - 8, y + 28, 0, 0, 183, 114);
            }
        }

        @Override
        void content(int x, int y) {
            x += this.shift();
            y += 12;
            this.manual.drawString("§n" + this.text, x + 50, y + 4);
            if (this.size.equals("two")) {
                this.manual.bigItem(at(this.icons, 0), (x + 126) / 2, (y + 68) / 2, true);
                for (int i = 0; i < this.icons.length - 1; i++) {
                    this.manual.bigItem(at(this.icons, i + 1), (x + 14 + 36 * (i % 2)) / 2, (y + 36 * (i / 2) + 52) / 2, false);
                }
            } else if (this.size.equals("three")) {
                this.manual.bigItem(at(this.icons, 0), (x + 138) / 2, (y + 70) / 2, true);
                for (int i = 0; i < this.icons.length - 1; i++) {
                    this.manual.bigItem(at(this.icons, i + 1), (x - 2 + 36 * (i % 3)) / 2, (y + 36 * (i / 3) + 34) / 2, false);
                }
            }
        }
    }

    /** FurnacePage: input, coal, output on bookfurnace.png. */
    public static class Furnace extends Page {
        String text = "";
        ItemStack[] icons = new ItemStack[0];

        @Override
        void read(Element element) {
            this.text = text(element, "text");
            this.icons = recipe(element, "recipe");
        }

        @Override
        void background(int x, int y) {
            this.manual.blit(FURNACE, x + 32, y + 32, 0, 0, 111, 114);
        }

        @Override
        void content(int x, int y) {
            this.manual.drawString("§n" + this.text, x + 50, y + 4);
            this.manual.bigItem(ManualIcons.icon("coal"), (x + 38) / 2, (y + 110) / 2, false);
            this.manual.bigItem(at(this.icons, 0), (x + 106) / 2, (y + 74) / 2, true);
            this.manual.bigItem(at(this.icons, 1), (x + 38) / 2, (y + 38) / 2, false);
        }
    }

    /** ModifierPage: tool + modifier items in the tool station picture. */
    public static class Modifier extends Page {
        String type = "";
        ItemStack[] icons = new ItemStack[0];

        @Override
        void read(Element element) {
            this.type = text(element, "tooltype");
            this.icons = recipe(element, "recipe");
        }

        @Override
        void background(int x, int y) {
            this.manual.blit(MODIFY, x + 12, y + 32, 0, 0, 154, 78);
        }

        @Override
        void content(int x, int y) {
            this.manual.drawString("§nTool Station", x + 60, y + 4);
            ItemStack tool = ManualIcons.icon(this.type.equals("weapon") ? "ironlongsword" : "ironpick");
            this.manual.bigItem(tool, (x + 54) / 2, (y + 54) / 2, false);
            this.manual.bigItem(at(this.icons, 0), (x + 130) / 2, (y + 54) / 2, false);
            this.manual.bigItem(at(this.icons, 1), (x + 18) / 2, (y + 36) / 2, false);
            this.manual.bigItem(at(this.icons, 2), (x + 18) / 2, (y + 74) / 2, false);
        }
    }

    /** BlockCastPage: liquid, cast and the cast block on booksmeltery.png. */
    public static class BlockCast extends Page {
        String text = "";
        ItemStack[] icons = new ItemStack[0];

        @Override
        void read(Element element) {
            this.text = text(element, "text");
            this.icons = recipe(element, "recipe");
        }

        @Override
        void background(int x, int y) {
            this.manual.blit(SMELTERY, x, y + 32, 0, 0, 174, 115);
            this.manual.blit(SMELTERY, x + 62, y + 105, 2, 118, 45, 45);
        }

        @Override
        void content(int x, int y) {
            this.manual.drawString("§n" + this.text, x + 70, y + 4);
            this.manual.bigItem(at(this.icons, 0), (x + 138) / 2, (y + 110) / 2, false);
            this.manual.bigItem(at(this.icons, 1), (x + 70) / 2, (y + 74) / 2, false);
            this.manual.bigItem(at(this.icons, 2), (x + 70) / 2, (y + 110) / 2, false);
            this.manual.drawString("Ingredients:", x + 120, y + 32);
            this.manual.drawString("- " + at(this.icons, 1).getHoverName().getString(), x + 120, y + 42);
            if (!at(this.icons, 2).isEmpty()) {
                this.manual.drawString("- " + at(this.icons, 2).getHoverName().getString(), x + 120, y + 50);
            }
        }
    }

    /** ToolPage: title, description, the parts with their texts. */
    public static class Tool extends Page {
        String title = "";
        ItemStack[] icons = new ItemStack[0];
        String[] iconText = new String[0];

        @Override
        void read(Element element) {
            this.title = text(element, "title");
            NodeList items = element.getElementsByTagName("item");
            this.iconText = new String[items.getLength() + 2];
            this.icons = new ItemStack[items.getLength() + 1];
            for (int i = 0; i < items.getLength(); i++) {
                Element item = (Element) items.item(i);
                this.iconText[i + 2] = text(item, "text");
                this.icons[i + 1] = ManualIcons.icon(text(item, "icon"));
            }
            // the first two <text> of the page (the part texts follow inside the items)
            NodeList texts = element.getChildNodes();
            int found = 0;
            for (int i = 0; i < texts.getLength() && found < 2; i++) {
                if (texts.item(i) instanceof Element child && child.getTagName().equals("text")) {
                    this.iconText[found++] = child.getTextContent();
                }
            }
            for (int i = found; i < 2; i++) {
                this.iconText[i] = "";
            }
            this.icons[0] = ManualIcons.icon(text(element, "icon"));
        }

        @Override
        void content(int x, int y) {
            this.manual.drawString("§n" + this.title, x + 70, y + 4);
            this.manual.drawSplit(this.iconText[0], x, y + 16, 178);
            int size = this.iconText[0].length() / 48;
            this.manual.drawSplit(this.iconText[1], x, y + 28 + 10 * size, 118);
            this.manual.drawString("Crafting Parts: ", x + 124, y + 28 + 10 * size);
            this.manual.item(this.icons[0], x + 50, y);
            for (int i = 1; i < this.icons.length; i++) {
                this.manual.item(this.icons[i], x + 120, y + 20 + 10 * size + 18 * i);
                int partOffset = this.iconText[i + 1].length() > 11 ? -3 : 0;
                this.manual.drawSplit(this.iconText[i + 1], x + 140, y + 24 + 10 * size + 18 * i + partOffset, 44);
            }
        }
    }

    /** MaterialPage: the stats of a tool material. */
    public static class Material extends Page {
        String title = "";
        String iconText = "";
        ItemStack[] icons = new ItemStack[4];
        @Nullable TMaterial material;

        @Override
        void read(Element element) {
            this.title = text(element, "title");
            this.iconText = text(element, "text");
            this.icons[0] = ManualIcons.icon(text(element, "icon"));
            String name = text(element, "toolmaterial", 0);
            this.material = byName(name == null ? this.title : name);
            NodeList materials = element.getElementsByTagName("material");
            String icon = materials.getLength() > 0 ? materials.item(0).getTextContent().trim() : "";
            this.icons[1] = ManualIcons.icon(icon);
            PatternBuilder.MaterialSet set = this.material == null ? null : PatternBuilder.set(this.material);
            this.icons[2] = set == null ? ItemStack.EMPTY : set.shard().get();
            this.icons[3] = set == null ? ItemStack.EMPTY : set.rod().get();
        }

        private static @Nullable TMaterial byName(String name) {
            String key = name.replace(" ", "").toLowerCase(Locale.ROOT);
            for (TMaterial m : TMaterial.values()) {
                if (m.name().replace("_", "").toLowerCase(Locale.ROOT).equals(key)) {
                    return m;
                }
            }
            return null;
        }

        private void label(ItemStack stack, int x, int y) {
            if (!stack.isEmpty()) {
                String name = stack.getHoverName().getString();
                this.manual.drawSplit(name, x, y + (name.length() > 12 ? 0 : 3), 52);
            }
        }

        @Override
        void content(int x, int y) {
            this.manual.drawString("§n" + this.title, x + 70, y + 4);
            this.manual.drawSplit(this.iconText, x, y + 16, 178);
            this.manual.drawString("Material: ", x + 108, y + 40);
            this.manual.drawString("Shard: ", x + 108, y + 72);
            this.manual.drawString("Rod: ", x + 108, y + 104);
            this.manual.item(this.icons[1], x + 108, y + 50);
            this.manual.item(this.icons[2], x + 108, y + 82);
            this.manual.item(this.icons[3], x + 108, y + 114);
            this.label(this.icons[1], x + 128, y + 50);
            this.label(this.icons[2], x + 128, y + 82);
            this.label(this.icons[3], x + 128, y + 114);
            TMaterial m = this.material;
            if (m == null) {
                return;
            }
            this.manual.drawString("Durability: " + m.durability, x, y + 40);
            this.manual.drawString("Handle Modifier: " + m.handle + "x", x, y + 50);
            this.manual.drawString("Full Tool Durability: " + (int) (m.durability * m.handle), x, y + 60);
            this.manual.drawString("Mining Speed: " + m.miningSpeed / 100.0F, x, y + 80);
            this.manual.drawString("Mining Level: " + m.harvestLevel + " (" + ToolStationScreen.level(m.harvestLevel).getString() + ")", x, y + 90);
            String heart = m.attack == 2 ? " Heart" : " Hearts";
            if (m.attack % 2 == 0) {
                this.manual.drawString("Base Attack: " + m.attack / 2 + heart, x, y + 100);
            } else {
                this.manual.drawString("Base Attack: " + m.attack / 2.0F + heart, x, y + 100);
            }
            int offset = 0;
            if (!m.ability.isEmpty()) {
                this.manual.drawString("Material ability: " + m.ability, x, y + 120 + 10 * offset);
                offset++;
                if (m.ability.equals("Writable")) {
                    this.manual.drawString("+1 Modifiers", x, y + 120 + 10 * offset);
                }
            }
            if (m.reinforced > 0) {
                this.manual.drawString("Material ability: Reinforced", x, y + 120 + 10 * offset);
                offset++;
                this.manual.drawString("Reinforced level: " + m.reinforced, x, y + 120 + 10 * offset);
                offset++;
            }
            if (m.stonebound > 0.0F) {
                this.manual.drawString("Stonebound level: " + m.stonebound, x, y + 120 + 10 * offset);
            } else if (m.stonebound < 0.0F) {
                this.manual.drawString("Splintering level: " + -m.stonebound, x, y + 120 + 10 * offset);
            }
        }
    }
}
