package bibliocraft.bookcase;

import bibliocraft.BiblioCraft;
import bibliocraft.Woods;
import bibliocraft.common.BiblioMenu;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;

/** BlockBookcase ("Bibliotheca", metadata = wood) and the Redstone book that powers it. */
public final class Bookcases {
    public static final List<DeferredBlock<BookcaseBlock>> BOOKCASES = new ArrayList<>();
    static {
        for (int w = 0; w < 4; w++) {
            int wood = w;
            DeferredBlock<BookcaseBlock> block = BiblioCraft.BLOCKS.registerBlock(Woods.NAMES[w] + "_bookcase", p -> new BookcaseBlock(wood, p),
                    p -> p.mapColor(MapColor.WOOD).instrument(NoteBlockInstrument.BASS).strength(3.0F).sound(SoundType.WOOD).noOcclusion().ignitedByLava());
            BiblioCraft.ITEMS.registerSimpleBlockItem(block);
            BOOKCASES.add(block);
        }
    }
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BookcaseBlockEntity>> BOOKCASE_ENTITY = BiblioCraft.BLOCK_ENTITIES.register(
            "bookcase", () -> new BlockEntityType<>(BookcaseBlockEntity::new, BOOKCASES.stream().map(DeferredBlock::get).toArray(BookcaseBlock[]::new)));
    public static final DeferredHolder<MenuType<?>, MenuType<BookcaseMenu>> BOOKCASE_MENU = BiblioCraft.MENUS.register("bookcase",
            () -> IMenuTypeExtension.create((id, inv, buf) -> new BookcaseMenu(id, inv, BiblioMenu.clientContainer(inv, buf, 16))));
    /** ItemRedstoneBook "Redstone: Volume I": stack size 1, glint, written book icon. */
    public static final DeferredItem<RedstoneBookItem> REDSTONE_BOOK = BiblioCraft.ITEMS.registerItem("redstone_book", RedstoneBookItem::new,
            p -> p.stacksTo(1).component(net.minecraft.core.component.DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true));

    private Bookcases() {
    }

    public static void init(IEventBus modBus) {
        // registered through the static fields
    }
}
