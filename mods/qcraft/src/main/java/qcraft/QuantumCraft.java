package qcraft;

import com.mojang.serialization.MapCodec;
import hexxit.worldgen152.OldBlockStates;
import hexxit.worldgen152.OldGameIds;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import qcraft.block.Entanglements;
import qcraft.block.QBlock;
import qcraft.block.QBlockEntity;
import qcraft.block.QuantumComputerBlock;
import qcraft.block.QuantumComputerBlockEntity;
import qcraft.block.QuantumComputerMenu;
import qcraft.block.QuantumOreBlock;
import qcraft.gen.QCraftWorldGen;
import qcraft.item.QBlockItem;
import qcraft.item.QuantumComputerItem;
import qcraft.recipe.EntangledComputerRecipe;
import qcraft.recipe.EntangledQBlockRecipe;
import qcraft.recipe.QBlockRecipe;

/**
 * qCraft 1.01 (FML id "QuantumCraft") for NeoForge. What QuantumCraft/QuantumCraftProxyCommon registered with the
 * switches of the original build: quantum dust (and its ore), qBlocks, quantum computers and quantum goggles. Quantum
 * torch, wire, logic gates and qubits exist in the source but their switches were off ({@code enableQuantumTorch},
 * {@code enableQuantumRedstone}, {@code enableQuantumGates}, {@code enableQuantumStorage} = false), so they were never
 * registered and are not ported.
 */
@Mod(QuantumCraft.MOD_ID)
public final class QuantumCraft {
    public static final String MOD_ID = "quantumcraft";

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MOD_ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MOD_ID);
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, MOD_ID);
    public static final DeferredRegister.DataComponents DATA_COMPONENTS = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, MOD_ID);
    private static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, MOD_ID);
    private static final DeferredRegister<MapCodec<? extends ICondition>> CONDITIONS = DeferredRegister.create(NeoForgeRegistries.Keys.CONDITION_CODECS, MOD_ID);
    private static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MOD_ID);

    // ---- blocks (ids 2371-2374 of the pack)

    public static final DeferredBlock<QBlock> QBLOCK = BLOCKS.registerBlock("qblock", QBlock::new,
            p -> p.mapColor(MapColor.DIRT).strength(5.0F, 10.0F).sound(SoundType.METAL).noOcclusion().noLootTable().dynamicShape()
                    .lightLevel(QBlock::lightOf)
                    .isRedstoneConductor((s, l, pos) -> QBlock.isNormalCube(s))
                    .isSuffocating((s, l, pos) -> QBlock.isNormalCube(s))
                    .isViewBlocking((s, l, pos, box) -> QBlock.isNormalCube(s)));
    public static final DeferredBlock<QuantumComputerBlock> QUANTUM_COMPUTER = BLOCKS.registerBlock("quantum_computer", QuantumComputerBlock::new,
            p -> p.mapColor(MapColor.METAL).strength(5.0F, 10.0F).sound(SoundType.METAL).requiresCorrectToolForDrops().noOcclusion().noLootTable());
    public static final DeferredBlock<QuantumOreBlock> QUANTUM_ORE = BLOCKS.registerBlock("quantum_ore", p -> new QuantumOreBlock(false, p),
            p -> p.mapColor(MapColor.STONE).strength(3.0F, 5.0F).requiresCorrectToolForDrops());
    public static final DeferredBlock<QuantumOreBlock> QUANTUM_ORE_GLOWING = BLOCKS.registerBlock("quantum_ore_glowing", p -> new QuantumOreBlock(true, p),
            p -> p.mapColor(MapColor.STONE).strength(3.0F, 5.0F).requiresCorrectToolForDrops().lightLevel(s -> 9).randomTicks());

    // ---- items (ids 7526-7528 of the pack, +256)

    public static final DeferredItem<QBlockItem> QBLOCK_ITEM = ITEMS.registerItem("qblock", p -> new QBlockItem(QBLOCK.get(), p),
            p -> p.useBlockDescriptionPrefix());
    public static final DeferredItem<QuantumComputerItem> QUANTUM_COMPUTER_ITEM = ITEMS.registerItem("quantum_computer",
            p -> new QuantumComputerItem(QUANTUM_COMPUTER.get(), p), p -> p.useBlockDescriptionPrefix());
    public static final DeferredItem<BlockItem> QUANTUM_ORE_ITEM = ITEMS.registerSimpleBlockItem(QUANTUM_ORE);
    public static final DeferredItem<BlockItem> QUANTUM_ORE_GLOWING_ITEM = ITEMS.registerSimpleBlockItem(QUANTUM_ORE_GLOWING);
    /** ItemEOS damage 0, 1, 2. */
    public static final DeferredItem<Item> EOS = ITEMS.registerSimpleItem("eos");
    public static final DeferredItem<Item> EOO = ITEMS.registerSimpleItem("eoo");
    public static final DeferredItem<Item> EOE = ITEMS.registerSimpleItem("eoe");
    public static final DeferredItem<Item> QUANTUM_DUST = ITEMS.registerSimpleItem("quantum_dust");
    /**
     * ItemQuantumGoggles: a helmet of an armour material with durability, defence and enchantability 0, its own armour
     * texture and the goggles overlay on the screen (renderHelmetOverlay = the camera overlay of 26.3).
     */
    public static final DeferredItem<Item> QUANTUM_GOGGLES = ITEMS.registerItem("quantum_goggles", Item::new, p -> p.stacksTo(1)
            .component(DataComponents.EQUIPPABLE, Equippable.builder(EquipmentSlot.HEAD)
                    .setAsset(ResourceKey.create(EquipmentAssets.ROOT_ID, id("quantum_goggles")))
                    .setCameraOverlay(id("misc/goggles"))
                    .build()));

    // ---- block entities, menu, recipes

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<QBlockEntity>> QBLOCK_ENTITY = BLOCK_ENTITIES.register("qblock",
            () -> new BlockEntityType<>(QBlockEntity::new, QBLOCK.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<QuantumComputerBlockEntity>> QUANTUM_COMPUTER_ENTITY = BLOCK_ENTITIES.register(
            "qcomputer", () -> new BlockEntityType<>(QuantumComputerBlockEntity::new, QUANTUM_COMPUTER.get()));
    public static final DeferredHolder<MenuType<?>, MenuType<QuantumComputerMenu>> QUANTUM_COMPUTER_MENU = MENUS.register("quantum_computer",
            () -> IMenuTypeExtension.create((id, inv, buf) -> new QuantumComputerMenu(id, inv, buf.readBlockPos())));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<QBlockRecipe>> QBLOCK_RECIPE = SERIALIZERS.register("qblock",
            () -> QBlockRecipe.SERIALIZER);
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<EntangledQBlockRecipe>> ENTANGLED_QBLOCK_RECIPE = SERIALIZERS.register(
            "entangled_qblock", () -> EntangledQBlockRecipe.SERIALIZER);
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<EntangledComputerRecipe>> ENTANGLED_COMPUTER_RECIPE = SERIALIZERS.register(
            "entangled_quantum_computer", () -> EntangledComputerRecipe.SERIALIZER);
    static {
        CONDITIONS.register("config", () -> QCConfig.Condition.CODEC);
    }

    /** CreativeTabQuantumCraft "qCraft" (icon: quantum dust), in the order of the block and item ids. */
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TAB = TABS.register("qcraft", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.quantumcraft"))
            .icon(() -> new ItemStack(QUANTUM_DUST.get()))
            .displayItems((params, output) -> {
                output.accept(QuantumComputerItem.create(-1, 1));
                output.accept(QUANTUM_ORE_GLOWING_ITEM.get());
                output.accept(EOS.get());
                output.accept(EOO.get());
                output.accept(EOE.get());
                output.accept(QUANTUM_DUST.get());
                output.accept(QUANTUM_GOGGLES.get());
            })
            .build());

    public QuantumCraft(IEventBus modBus, ModContainer container) {
        container.registerConfig(ModConfig.Type.SYNCED, QCConfig.SPEC, "QuantumCraft.toml");
        modBus.addListener(ModConfigEvent.Loading.class, e -> applyConfig());
        modBus.addListener(ModConfigEvent.Reloading.class, e -> applyConfig());
        qcraft.item.QComponents.QBLOCK.getId();
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        BLOCK_ENTITIES.register(modBus);
        MENUS.register(modBus);
        DATA_COMPONENTS.register(modBus);
        SERIALIZERS.register(modBus);
        CONDITIONS.register(modBus);
        TABS.register(modBus);
        Entanglements.register();
        QCraftWorldGen.register();
        registerOldIds();
        if (FMLEnvironment.getDist() == Dist.CLIENT) {
            qcraft.client.QCraftClient.init(modBus);
        }
    }

    private static void applyConfig() {
        QCraftWorldGen.enableWorldGen = QCConfig.worldGen();
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    /** Block and item ids of the pack's QuantumCraft.cfg (items shifted by 256), for old worlds, loot and templates. */
    private static void registerOldIds() {
        OldGameIds.registerItem(2371, id("qblock").toString());
        OldGameIds.registerItem(2372, id("quantum_computer").toString());
        OldGameIds.registerItem(2373, id("quantum_ore").toString());
        OldGameIds.registerItem(2374, id("quantum_ore_glowing").toString());
        OldGameIds.registerItem(7526 + 256, 0, id("eos").toString());
        OldGameIds.registerItem(7526 + 256, 1, id("eoo").toString());
        OldGameIds.registerItem(7526 + 256, 2, id("eoe").toString());
        OldGameIds.registerItem(7527 + 256, id("quantum_dust").toString());
        OldGameIds.registerItem(7528 + 256, id("quantum_goggles").toString());
        OldBlockStates.register(QCraftWorldGen.QUANTUM_ORE, () -> QUANTUM_ORE.get().defaultBlockState());
        OldBlockStates.register(QCraftWorldGen.QUANTUM_ORE_GLOWING, () -> QUANTUM_ORE_GLOWING.get().defaultBlockState());
    }
}
