package invtweaks.client;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

/**
 * InvTweaksItemTreeLoader: reads the XML tree. An element with attributes is an item ({@code id}, optionally
 * {@code potion}) or a tag entry ({@code tag}, was {@code oreDictName}); any other element is a category. The
 * numeric ranges of 1.5.2 ({@code min/max}, {@code dmin/dmax}) are gone with the numeric ids.
 */
public final class InvTweaksItemTreeLoader extends DefaultHandler {
    private InvTweaksItemTree tree;
    private String treeVersion;
    private int itemOrder;
    private final LinkedList<String> categoryStack = new LinkedList<>();

    private InvTweaksItemTreeLoader() {
    }

    public static synchronized InvTweaksItemTree load(Path file, boolean forgeAdditions) throws Exception {
        InvTweaksItemTreeLoader loader = new InvTweaksItemTreeLoader();
        loader.tree = new InvTweaksItemTree();
        parser().parse(file.toFile(), loader);
        if (forgeAdditions) {
            loader.tree.addToolClasses();
        }
        return loader.tree;
    }

    public static synchronized boolean isValidVersion(Path file) throws Exception {
        if (!Files.exists(file)) {
            return false;
        }
        InvTweaksItemTreeLoader loader = new InvTweaksItemTreeLoader();
        loader.tree = new InvTweaksItemTree();
        parser().parse(file.toFile(), loader);
        return InvTweaksConst.TREE_VERSION.equals(loader.treeVersion);
    }

    private static SAXParser parser() throws Exception {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        return factory.newSAXParser();
    }

    @Override
    public void startElement(String uri, String localName, String name, Attributes attributes) {
        String version = attributes.getValue("treeVersion");
        if (attributes.getLength() != 0 && this.treeVersion != null) {
            String id = attributes.getValue("id");
            String tag = attributes.getValue("tag");
            if (id != null) {
                Item item = parse(id).flatMap(BuiltInRegistries.ITEM::getOptional).orElse(null);
                Identifier potion = attributes.getValue("potion") != null ? parse(attributes.getValue("potion")).orElse(null) : null;
                this.tree.addItem(this.categoryStack.getLast(), this.tree.newItem(name.toLowerCase(), item, potion, null, this.itemOrder++));
            } else if (tag != null) {
                TagKey<Item> key = parse(tag).map(location -> TagKey.create(Registries.ITEM, location)).orElse(null);
                this.tree.addItem(this.categoryStack.getLast(), this.tree.newItem(name.toLowerCase(), null, null, key, this.itemOrder++));
            }
        } else {
            if (this.treeVersion == null) {
                this.treeVersion = version;
            }
            if (this.categoryStack.isEmpty()) {
                this.tree.setRootCategory(new InvTweaksItemTreeCategory(name));
            } else {
                this.tree.addCategory(this.categoryStack.getLast(), new InvTweaksItemTreeCategory(name));
            }
            this.categoryStack.add(name);
        }
    }

    @Override
    public void endElement(String uri, String localName, String name) {
        if (!this.categoryStack.isEmpty() && name.equals(this.categoryStack.getLast())) {
            this.categoryStack.removeLast();
        }
    }

    private static java.util.Optional<Identifier> parse(String id) {
        try {
            return java.util.Optional.of(Identifier.parse(id.trim()));
        } catch (RuntimeException e) {
            InvTweaks.log.warn("Invalid id in the item tree: {}", id);
            return java.util.Optional.empty();
        }
    }
}
