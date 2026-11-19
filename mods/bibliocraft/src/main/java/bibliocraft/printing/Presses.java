package bibliocraft.printing;

import bibliocraft.BiblioCraft;
import bibliocraft.common.BiblioMenu;
import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;

/**
 * BlockTypeMachine "Typesetting Table" (wood, hardness 3), BlockPrintPress "Printing Press" (iron, hardness 3, anvil
 * sound; iron needed a pickaxe to drop), ItemChase "Print Press Chase" (stacks to 64, enchantability 20), ItemPlate
 * "Print Press Plate" (one, carries a book name), ItemEnchantedPlate "Enchanted Plate" (one, 3 uses, glint, carries
 * the stored enchantments of an enchanted book).
 */
public final class Presses {
    /** The plate's NBT "bookName": the saved book it prints. */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> PLATE_BOOK = BiblioCraft.DATA_COMPONENTS.registerComponentType(
            "plate_book", b -> b.persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8));

    public static final DeferredBlock<TypesettingBlock> TYPESETTING_TABLE = BiblioCraft.BLOCKS.registerBlock("typesetting_table", TypesettingBlock::new,
            p -> p.mapColor(MapColor.WOOD).instrument(NoteBlockInstrument.BASS).strength(3.0F).sound(SoundType.WOOD).noOcclusion().ignitedByLava());
    public static final DeferredBlock<PrintPressBlock> PRINTING_PRESS = BiblioCraft.BLOCKS.registerBlock("printing_press", PrintPressBlock::new,
            p -> p.mapColor(MapColor.METAL).strength(3.0F).sound(SoundType.ANVIL).requiresCorrectToolForDrops().noOcclusion());
    static {
        BiblioCraft.ITEMS.registerSimpleBlockItem(TYPESETTING_TABLE);
        BiblioCraft.ITEMS.registerSimpleBlockItem(PRINTING_PRESS);
    }
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TypesettingBlockEntity>> TYPESETTING_ENTITY = BiblioCraft.BLOCK_ENTITIES.register(
            "typesetting_table", () -> new BlockEntityType<>(TypesettingBlockEntity::new, TYPESETTING_TABLE.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PrintPressBlockEntity>> PRESS_ENTITY = BiblioCraft.BLOCK_ENTITIES.register(
            "printing_press", () -> new BlockEntityType<>(PrintPressBlockEntity::new, PRINTING_PRESS.get()));
    public static final DeferredHolder<MenuType<?>, MenuType<PrintPressMenu>> PRESS_MENU = BiblioCraft.MENUS.register("printing_press",
            () -> IMenuTypeExtension.create((id, inv, buf) -> new PrintPressMenu(id, inv, BiblioMenu.clientContainer(inv, buf, 4))));

    public static final DeferredItem<ChaseItem> CHASE = BiblioCraft.ITEMS.registerItem("chase", ChaseItem::new, p -> p.stacksTo(64).enchantable(20));
    public static final DeferredItem<PlateItem> PLATE = BiblioCraft.ITEMS.registerItem("plate", PlateItem::new, p -> p.stacksTo(1));
    public static final DeferredItem<EnchantedPlateItem> ENCHANTED_PLATE = BiblioCraft.ITEMS.registerItem("enchanted_plate", EnchantedPlateItem::new,
            p -> p.durability(3).component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true));

    private Presses() {
    }

    static void load() {
        // registered through the static fields
    }
}
