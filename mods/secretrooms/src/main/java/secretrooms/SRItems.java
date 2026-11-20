package secretrooms;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.DoubleHighBlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraft.core.Direction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * The items of SecretRooms.load. Pack ids: camo paste 7502, iron door 7501 (both - 256 in the code, so as written),
 * wooden door 7503 + 256 = 7759 (its id was not reduced).
 */
public final class SRItems {
    static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(SecretRooms.MOD_ID);
    static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, SecretRooms.MOD_ID);

    public static final DeferredItem<StandingAndWallBlockItem> TORCH_LEVER = ITEMS.registerItem("torch_lever",
            p -> new StandingAndWallBlockItem(SRBlocks.TORCH_LEVER.get(), SRBlocks.WALL_TORCH_LEVER.get(), Direction.DOWN, p),
            p -> p.useBlockDescriptionPrefix());
    public static final DeferredItem<BlockItem> ONE_WAY_GLASS = ITEMS.registerSimpleBlockItem(SRBlocks.ONE_WAY_GLASS);
    public static final DeferredItem<BlockItem> CAMO_GATE = ITEMS.registerSimpleBlockItem(SRBlocks.CAMO_GATE);
    public static final DeferredItem<BlockItem> SECRET_TRAPDOOR = ITEMS.registerSimpleBlockItem(SRBlocks.SECRET_TRAPDOOR);
    public static final DeferredItem<DoubleHighBlockItem> SECRET_WOODEN_DOOR = ITEMS.registerItem("secret_wooden_door",
            p -> new DoubleHighBlockItem(SRBlocks.SECRET_WOODEN_DOOR.get(), p), p -> p.stacksTo(1).useItemDescriptionPrefix());
    public static final DeferredItem<DoubleHighBlockItem> SECRET_IRON_DOOR = ITEMS.registerItem("secret_iron_door",
            p -> new DoubleHighBlockItem(SRBlocks.SECRET_IRON_DOOR.get(), p), p -> p.stacksTo(1).useItemDescriptionPrefix());
    public static final DeferredItem<Item> CAMO_PASTE = ITEMS.registerSimpleItem("camo_paste");
    public static final DeferredItem<BlockItem> GHOST_BLOCK = ITEMS.registerSimpleBlockItem(SRBlocks.GHOST_BLOCK);
    public static final DeferredItem<BlockItem> SECRET_LEVER = ITEMS.registerSimpleBlockItem(SRBlocks.SECRET_LEVER);
    public static final DeferredItem<BlockItem> SECRET_REDSTONE = ITEMS.registerSimpleBlockItem(SRBlocks.SECRET_REDSTONE);
    public static final DeferredItem<BlockItem> SECRET_STONE_BUTTON = ITEMS.registerSimpleBlockItem(SRBlocks.SECRET_STONE_BUTTON);
    public static final DeferredItem<BlockItem> SECRET_WOOD_BUTTON = ITEMS.registerSimpleBlockItem(SRBlocks.SECRET_WOOD_BUTTON);
    public static final DeferredItem<BlockItem> SECRET_PRESSURE_PLATE = ITEMS.registerSimpleBlockItem(SRBlocks.SECRET_PRESSURE_PLATE);
    public static final DeferredItem<BlockItem> SECRET_PLAYER_PLATE = ITEMS.registerSimpleBlockItem(SRBlocks.SECRET_PLAYER_PLATE);
    public static final DeferredItem<BlockItem> SECRET_LIGHT_PLATE = ITEMS.registerSimpleBlockItem(SRBlocks.SECRET_LIGHT_PLATE);
    public static final DeferredItem<BlockItem> SECRET_HEAVY_PLATE = ITEMS.registerSimpleBlockItem(SRBlocks.SECRET_HEAVY_PLATE);
    public static final DeferredItem<BlockItem> SECRET_STAIRS = ITEMS.registerSimpleBlockItem(SRBlocks.SECRET_STAIRS);
    public static final DeferredItem<BlockItem> SECRET_CHEST = ITEMS.registerSimpleBlockItem(SRBlocks.SECRET_CHEST);
    public static final DeferredItem<BlockItem> SECRET_TRAPPED_CHEST = ITEMS.registerSimpleBlockItem(SRBlocks.SECRET_TRAPPED_CHEST);
    public static final DeferredItem<BlockItem> SECRET_LIGHT_DETECTOR = ITEMS.registerSimpleBlockItem(SRBlocks.SECRET_LIGHT_DETECTOR);
    public static final DeferredItem<BlockItem> SOLID_AIR = ITEMS.registerSimpleBlockItem(SRBlocks.SOLID_AIR);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TAB = TABS.register("secretroomsmod", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.secretroomsmod"))
            .icon(() -> new ItemStack(CAMO_PASTE.get()))
            .displayItems((params, output) -> ITEMS.getEntries().forEach(item -> output.accept(item.get())))
            .build());

    private SRItems() {
    }

    static void register(IEventBus modBus) {
        ITEMS.register(modBus);
        TABS.register(modBus);
    }
}
