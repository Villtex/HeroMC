package extrabiomes;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.UnaryOperator;

/** Blocks, their items and the ExtrabiomesXL creative tab. */
public final class EbxlBlocks {
    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Extrabiomes.MOD_ID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Extrabiomes.MOD_ID);
    private static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Extrabiomes.MOD_ID);

    // 1.5.2: one block "redrock" with metadata 0/1/2. Hardness 1.5 (cobble 2.0), blast resistance as set there.
    public static final DeferredBlock<Block> RED_ROCK = BLOCKS.registerSimpleBlock("red_rock", rock(1.5F));
    public static final DeferredBlock<Block> RED_COBBLESTONE = BLOCKS.registerSimpleBlock("red_cobblestone", rock(2.0F));
    public static final DeferredBlock<Block> RED_ROCK_BRICK = BLOCKS.registerSimpleBlock("red_rock_brick", rock(1.5F));
    public static final DeferredBlock<Block> CRACKED_SAND = BLOCKS.registerSimpleBlock("cracked_sand", rock(1.2F));

    public static final DeferredItem<BlockItem> RED_ROCK_ITEM = ITEMS.registerSimpleBlockItem(RED_ROCK);
    public static final DeferredItem<BlockItem> RED_COBBLESTONE_ITEM = ITEMS.registerSimpleBlockItem(RED_COBBLESTONE);
    public static final DeferredItem<BlockItem> RED_ROCK_BRICK_ITEM = ITEMS.registerSimpleBlockItem(RED_ROCK_BRICK);
    public static final DeferredItem<BlockItem> CRACKED_SAND_ITEM = ITEMS.registerSimpleBlockItem(CRACKED_SAND);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TAB = TABS.register("extrabiomes", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.extrabiomesxl"))
            .icon(() -> new ItemStack(RED_ROCK_ITEM.get()))
            .displayItems((parameters, output) -> {
                output.accept(CRACKED_SAND_ITEM.get());
                output.accept(RED_ROCK_ITEM.get());
                output.accept(RED_COBBLESTONE_ITEM.get());
                output.accept(RED_ROCK_BRICK_ITEM.get());
            })
            .build());

    private EbxlBlocks() {
    }

    /** Material.rock of 1.5.2: stone sound and map colour, needs a pickaxe; explosion resistance 6 (= 1.2 today). */
    private static UnaryOperator<BlockBehaviour.Properties> rock(float hardness) {
        return properties -> properties
                .mapColor(MapColor.STONE)
                .instrument(NoteBlockInstrument.BASEDRUM)
                .requiresCorrectToolForDrops()
                .strength(hardness, 1.2F);
    }

    static void register(IEventBus modBus) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        TABS.register(modBus);
    }
}
