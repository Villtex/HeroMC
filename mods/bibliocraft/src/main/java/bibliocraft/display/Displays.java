package bibliocraft.display;

import bibliocraft.BiblioCraft;
import bibliocraft.Woods;
import bibliocraft.common.BiblioMenu;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * BlockWeaponCase ("BiblioCase", metadata wood * 4 + cloth), BlockLabel ("BiblioLabel", metadata wood) and
 * BlockArmorStand ("Armor Stand"). The cases and labels come before the table in BlockLoader, the armor stand after
 * it, so the armor stand registers from its own holder class {@link ArmorStand}.
 */
public final class Displays {
    /** Material.wood, hardness 3, wood step sound (axe). */
    private static BlockBehaviour.Properties wood(BlockBehaviour.Properties p) {
        return p.mapColor(MapColor.WOOD).instrument(NoteBlockInstrument.BASS).strength(3.0F).sound(SoundType.WOOD).noOcclusion().ignitedByLava();
    }

    /** Index wood * 4 + cloth, as the metadata. */
    public static final List<DeferredBlock<WeaponCaseBlock>> WEAPON_CASES = new ArrayList<>();
    public static final List<DeferredBlock<LabelBlock>> LABELS = new ArrayList<>();
    static {
        for (int w = 0; w < 4; w++) {
            for (int c = 0; c < 4; c++) {
                int wood = w;
                int cloth = c;
                DeferredBlock<WeaponCaseBlock> block = BiblioCraft.BLOCKS.registerBlock(Woods.NAMES[w] + "_case_" + Woods.CASE_CLOTH[c],
                        p -> new WeaponCaseBlock(wood, cloth, p), Displays::wood);
                BiblioCraft.ITEMS.registerItem(block.getId().getPath(), p -> new WeaponCaseItem(block.get(), p),
                        () -> new net.minecraft.world.item.Item.Properties().useBlockDescriptionPrefix());
                WEAPON_CASES.add(block);
            }
        }
        for (int w = 0; w < 4; w++) {
            int wood = w;
            DeferredBlock<LabelBlock> block = BiblioCraft.BLOCKS.registerBlock(Woods.NAMES[w] + "_label", p -> new LabelBlock(wood, p),
                    p -> wood(p).noCollision());
            BiblioCraft.ITEMS.registerSimpleBlockItem(block);
            LABELS.add(block);
        }
    }
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<WeaponCaseBlockEntity>> WEAPON_CASE_ENTITY = BiblioCraft.BLOCK_ENTITIES
            .register("weapon_case", () -> new BlockEntityType<>(WeaponCaseBlockEntity::new,
                    WEAPON_CASES.stream().map(DeferredBlock::get).toArray(WeaponCaseBlock[]::new)));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<LabelBlockEntity>> LABEL_ENTITY = BiblioCraft.BLOCK_ENTITIES.register(
            "label", () -> new BlockEntityType<>(LabelBlockEntity::new, LABELS.stream().map(DeferredBlock::get).toArray(LabelBlock[]::new)));
    public static final DeferredHolder<MenuType<?>, MenuType<WeaponCaseMenu>> WEAPON_CASE_MENU = BiblioCraft.MENUS.register("weapon_case",
            () -> IMenuTypeExtension.create((id, inv, buf) -> new WeaponCaseMenu(id, inv, BiblioMenu.clientContainer(inv, buf, 1))));
    public static final DeferredHolder<MenuType<?>, MenuType<LabelMenu>> LABEL_MENU = BiblioCraft.MENUS.register("label",
            () -> IMenuTypeExtension.create((id, inv, buf) -> new LabelMenu(id, inv, BiblioMenu.clientContainer(inv, buf, 3))));

    private Displays() {
    }

    /** Weapon cases and labels (registered before the table, as in BlockLoader). */
    public static void init(IEventBus modBus) {
        // registered through the static fields
    }

    public static void initArmorStand(IEventBus modBus) {
        ArmorStand.init();
    }

    /** BlockArmorStand: Material.rock (needs a pickaxe for the drop), hardness 3, stone step sound. */
    public static final class ArmorStand {
        public static final DeferredBlock<ArmorStandBlock> BLOCK = BiblioCraft.BLOCKS.registerBlock("armor_stand", ArmorStandBlock::new,
                p -> p.mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(3.0F)
                        .sound(SoundType.STONE).noOcclusion());
        static {
            BiblioCraft.ITEMS.registerSimpleBlockItem(BLOCK);
        }
        public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ArmorStandBlockEntity>> ENTITY = BiblioCraft.BLOCK_ENTITIES.register(
                "armor_stand", () -> new BlockEntityType<>(ArmorStandBlockEntity::new, BLOCK.get()));
        public static final DeferredHolder<MenuType<?>, MenuType<ArmorStandMenu>> MENU = BiblioCraft.MENUS.register("armor_stand",
                () -> IMenuTypeExtension.create((id, inv, buf) -> new ArmorStandMenu(id, inv, BiblioMenu.clientContainer(inv, buf, 4))));

        private ArmorStand() {
        }

        static void init() {
            // class initialisation registers everything
        }
    }
}
