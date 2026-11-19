package bibliocraft.printing;

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
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * BlockWritingDesk ("BiblioDesk", metadata = wood, BlockItemWritingDesk "Oak Desk" .. "Jungle Desk"): hardness 3,
 * wood sound, light 0.5 (= 7). Kept apart from {@link Presses} so that it registers first, as in BlockLoader.
 */
public final class Desks {
    public static final List<DeferredBlock<WritingDeskBlock>> DESKS = new ArrayList<>();
    static {
        for (int w = 0; w < 4; w++) {
            int wood = w;
            DeferredBlock<WritingDeskBlock> block = BiblioCraft.BLOCKS.registerBlock(Woods.NAMES[w] + "_desk", p -> new WritingDeskBlock(wood, p),
                    p -> p.mapColor(MapColor.WOOD).instrument(NoteBlockInstrument.BASS).strength(3.0F).sound(SoundType.WOOD).noOcclusion()
                            .ignitedByLava().lightLevel(s -> 7));
            BiblioCraft.ITEMS.registerSimpleBlockItem(block);
            DESKS.add(block);
        }
    }
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<WritingDeskBlockEntity>> DESK_ENTITY = BiblioCraft.BLOCK_ENTITIES.register(
            "writing_desk", () -> new BlockEntityType<>(WritingDeskBlockEntity::new, DESKS.stream().map(DeferredBlock::get).toArray(WritingDeskBlock[]::new)));
    public static final DeferredHolder<MenuType<?>, MenuType<WritingDeskMenu>> DESK_MENU = BiblioCraft.MENUS.register("writing_desk",
            () -> IMenuTypeExtension.create((id, inv, buf) -> new WritingDeskMenu(id, inv, BiblioMenu.clientContainer(inv, buf, 9))));

    private Desks() {
    }

    static void load() {
        // registered through the static fields
    }
}
