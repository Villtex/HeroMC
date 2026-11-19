package bibliocraft.shelf;

import bibliocraft.BiblioCraft;
import bibliocraft.Woods;
import bibliocraft.common.BiblioMenu;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
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
 * BlockPotionShelf ("PotionShelf"), BlockGenericShelf ("Shelf") and BlockWeaponRack ("Tool Rack"), each with the four
 * wood types of the original metadata as separate blocks. All three: Material.wood, hardness 3, wood sound.
 */
public final class Shelves {
    public static final List<DeferredBlock<PotionShelfBlock>> POTION_SHELVES = woods("potion_shelf", PotionShelfBlock::new);
    public static final List<DeferredBlock<ShelfBlock>> SHELVES = woods("shelf", ShelfBlock::new);
    public static final List<DeferredBlock<ToolRackBlock>> TOOL_RACKS = woods("tool_rack", ToolRackBlock::new);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PotionShelfBlockEntity>> POTION_SHELF_ENTITY = BiblioCraft.BLOCK_ENTITIES
            .register("potion_shelf", () -> new BlockEntityType<>(PotionShelfBlockEntity::new,
                    POTION_SHELVES.stream().map(DeferredBlock::get).toArray(Block[]::new)));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ShelfBlockEntity>> SHELF_ENTITY = BiblioCraft.BLOCK_ENTITIES
            .register("shelf", () -> new BlockEntityType<>(ShelfBlockEntity::new, SHELVES.stream().map(DeferredBlock::get).toArray(Block[]::new)));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ToolRackBlockEntity>> TOOL_RACK_ENTITY = BiblioCraft.BLOCK_ENTITIES
            .register("tool_rack", () -> new BlockEntityType<>(ToolRackBlockEntity::new, TOOL_RACKS.stream().map(DeferredBlock::get).toArray(Block[]::new)));

    public static final DeferredHolder<MenuType<?>, MenuType<PotionShelfMenu>> POTION_SHELF_MENU = BiblioCraft.MENUS.register("potion_shelf",
            () -> IMenuTypeExtension.create((id, inv, buf) -> new PotionShelfMenu(id, inv, BiblioMenu.clientContainer(inv, buf, 12))));
    public static final DeferredHolder<MenuType<?>, MenuType<ShelfMenu>> SHELF_MENU = BiblioCraft.MENUS.register("shelf",
            () -> IMenuTypeExtension.create((id, inv, buf) -> new ShelfMenu(id, inv, BiblioMenu.clientContainer(inv, buf, 4))));
    public static final DeferredHolder<MenuType<?>, MenuType<ToolRackMenu>> TOOL_RACK_MENU = BiblioCraft.MENUS.register("tool_rack",
            () -> IMenuTypeExtension.create((id, inv, buf) -> new ToolRackMenu(id, inv, BiblioMenu.clientContainer(inv, buf, 4))));

    private Shelves() {
    }

    public static void init(IEventBus modBus) {
        // registered through the static fields
    }

    /** One block per wood (metadata 0 oak, 1 spruce, 2 birch, 3 jungle), with its block item. */
    private static <B extends Block> List<DeferredBlock<B>> woods(String kind, WoodBlockFactory<B> factory) {
        List<DeferredBlock<B>> list = new ArrayList<>();
        for (int w = 0; w < 4; w++) {
            int wood = w;
            Function<BlockBehaviour.Properties, B> ctor = p -> factory.create(wood, p);
            DeferredBlock<B> block = BiblioCraft.BLOCKS.registerBlock(Woods.NAMES[w] + "_" + kind, ctor,
                    p -> p.mapColor(MapColor.WOOD).instrument(NoteBlockInstrument.BASS).strength(3.0F).sound(SoundType.WOOD).noOcclusion().ignitedByLava());
            BiblioCraft.ITEMS.registerSimpleBlockItem(block);
            list.add(block);
        }
        return list;
    }

    @FunctionalInterface
    private interface WoodBlockFactory<B extends Block> {
        B create(int wood, BlockBehaviour.Properties properties);
    }

    /**
     * dropPotion / dropThing / dropWeaponN: a copy of the stack falls out 0.2 in front of the block centre, without
     * motion (adjusti/adjustk by angle). The caller empties the slot afterwards.
     */
    static boolean dropInFront(Level level, BlockPos pos, int angle, ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        float ai = switch (angle) {
            case 0 -> -0.2F;
            case 2 -> 0.2F;
            default -> 0.0F;
        };
        float ak = switch (angle) {
            case 1 -> -0.2F;
            case 3 -> 0.2F;
            default -> 0.0F;
        };
        level.addFreshEntity(new ItemEntity(level, pos.getX() + 0.5F + ai, pos.getY() + 0.5F, pos.getZ() + 0.5F + ak, stack.copy(), 0.0, 0.0, 0.0));
        return true;
    }
}
