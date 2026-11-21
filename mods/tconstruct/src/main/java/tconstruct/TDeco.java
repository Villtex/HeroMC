package tconstruct;

import hexxit.worldgen152.OldBlockStates;
import hexxit.worldgen152.OldGameIds;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.model.data.ModelProperty;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import tconstruct.deco.BarricadeBlock;
import tconstruct.deco.ConnectedGlassBlock;
import tconstruct.deco.MultiBrickBlock;
import tconstruct.deco.PunjiBlock;
import tconstruct.deco.SpeedBlock;
import tconstruct.landmine.LandmineBlock;
import tconstruct.landmine.LandmineBlockEntity;
import tconstruct.landmine.LandmineMenu;
import tconstruct.machine.DrawbridgeBlock;
import tconstruct.machine.DrawbridgeBlockEntity;
import tconstruct.machine.DrawbridgeMenu;
import tconstruct.machine.EssenceExtractorBlock;
import tconstruct.machine.EssenceExtractorBlockEntity;
import tconstruct.machine.FirestarterBlock;
import tconstruct.recipe.Detailing;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Part 25f (TContent.registerBlocks): clear glass, clear panes and the clear stained glass and panes, the multi bricks
 * and fancy bricks, brownstone (speed blocks and slabs), wool slabs, the stone torch, the hambone, the traps (punji
 * sticks, barricades, landmines), the redstone machines (drawbridge, igniter) and the essence extractor, with their
 * chisel steps. Not ported: the light aggregator, light crystal and antilight (no recipe, unfinished in 1.4.3).
 */
public final class TDeco {
    private static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, TConstruct.MOD_ID);

    /** The light of a camouflage block (drawbridge, landmine). */
    public static final IntegerProperty LIGHT = IntegerProperty.create("light", 0, 15);
    /** The camouflage block for the model. */
    public static final ModelProperty<BlockState> CAMO = new ModelProperty<>();
    /** The placement direction a drawbridge item keeps (NBT "Placement"). */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> PLACEMENT = TItems.COMPONENTS.registerComponentType("placement",
            b -> b.persistent(com.mojang.serialization.Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT));
    /** DamageSourceLandmine: bypasses armour. */
    public static final ResourceKey<DamageType> LANDMINE_DAMAGE = ResourceKey.create(Registries.DAMAGE_TYPE, TConstruct.id("landmine"));

    public static DamageSource landmineDamage(ServerLevel level) {
        return level.damageSources().source(LANDMINE_DAMAGE);
    }

    /** The block items of this part in creative tab order. */
    public static final List<DeferredItem<?>> ITEMS = new ArrayList<>();

    private static <B extends Block> DeferredBlock<B> block(String name, Function<BlockBehaviour.Properties, B> factory,
                                                           Supplier<BlockBehaviour.Properties> properties) {
        DeferredBlock<B> block = TBlocks.BLOCKS.registerBlock(name, factory, properties);
        ITEMS.add(TItems.ITEMS.registerSimpleBlockItem(block));
        return block;
    }

    private static BlockBehaviour.Properties glass() {
        return BlockBehaviour.Properties.of().strength(0.3F).sound(SoundType.GLASS).noOcclusion().isValidSpawn((s, l, p, e) -> false)
                .isRedstoneConductor((s, l, p) -> false).isSuffocating((s, l, p) -> false).isViewBlocking((s, l, p, e) -> false);
    }

    private static BlockBehaviour.Properties rock(float hardness, float resistance) {
        return BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(hardness, resistance).requiresCorrectToolForDrops();
    }

    // ------------------------------------------------------------------ glass

    /** GlassBlockConnectedMeta "stained" (16 colours, white first) and GlassPaneStained. */
    public static final String[] COLORS = {"white", "orange", "magenta", "light_blue", "yellow", "lime", "pink", "gray", "light_gray", "cyan", "purple",
            "blue", "brown", "green", "red", "black"};
    public static final DeferredBlock<ConnectedGlassBlock> CLEAR_GLASS = block("clear_glass", ConnectedGlassBlock::new, TDeco::glass);
    /** GlassPane 0..2: clear, soul, clear soul (the soul panes had no recipe). */
    public static final String[] PANES = {"clear_glass_pane", "soul_glass_pane", "clear_soul_glass_pane"};
    public static final List<DeferredBlock<IronBarsBlock>> GLASS_PANES = new ArrayList<>();
    public static final List<DeferredBlock<ConnectedGlassBlock>> STAINED_GLASS = new ArrayList<>();
    public static final List<DeferredBlock<IronBarsBlock>> STAINED_PANES = new ArrayList<>();

    static {
        for (String pane : PANES) {
            GLASS_PANES.add(block(pane, IronBarsBlock::new, TDeco::glass));
        }
        for (String color : COLORS) {
            STAINED_GLASS.add(block(color + "_clear_stained_glass", ConnectedGlassBlock::new, TDeco::glass));
        }
        for (String color : COLORS) {
            STAINED_PANES.add(block(color + "_clear_stained_glass_pane", IronBarsBlock::new, TDeco::glass));
        }
    }

    // ------------------------------------------------------------------ bricks

    /** MultiBrick 0..13 / MultiBrickFancy 0..15: name, hardness, blast resistance (getBlockHardness / getExplosionResistance). */
    private record Brick(String name, float hardness, float resistance) {
    }

    private static final Brick[] BRICKS = {new Brick("obsidian", 50.0F, 1200.0F), new Brick("sandstone", 0.8F, 0.8F),
            new Brick("netherrack", 0.4F, 0.4F), new Brick("polished_stone", 1.5F, 6.0F), new Brick("iron", 5.0F, 6.0F), new Brick("gold", 3.0F, 6.0F),
            new Brick("lapis", 3.0F, 3.0F), new Brick("diamond", 5.0F, 6.0F), new Brick("redstone", 5.0F, 6.0F), new Brick("bone", 1.0F, 1.0F),
            new Brick("slime", 1.5F, 1.5F), new Brick("blue_slime", 1.5F, 1.5F), new Brick("endstone", 3.0F, 9.0F),
            new Brick("obsidian_ingot", 50.0F, 1200.0F)};
    public static final List<DeferredBlock<MultiBrickBlock>> MULTI_BRICKS = new ArrayList<>();
    public static final List<DeferredBlock<MultiBrickBlock>> FANCY_BRICKS = new ArrayList<>();

    private static DeferredBlock<MultiBrickBlock> brick(String name, int meta, float hardness, float resistance) {
        boolean slime = meta == 10 || meta == 11;
        return block(name, p -> new MultiBrickBlock(slime, meta == 8, p), () -> {
            BlockBehaviour.Properties p = rock(hardness, resistance);
            if (slime) {
                p = p.sound(SoundType.SLIME_BLOCK);
            }
            if (meta == 8) {
                p = p.isRedstoneConductor((s, l, pos) -> false);
            }
            return p;
        });
    }

    static {
        for (int i = 0; i < BRICKS.length; i++) {
            MULTI_BRICKS.add(brick(BRICKS[i].name() + "_brick", i, BRICKS[i].hardness(), BRICKS[i].resistance()));
        }
        for (int i = 0; i < BRICKS.length; i++) {
            FANCY_BRICKS.add(brick("fancy_" + BRICKS[i].name() + "_brick", i, BRICKS[i].hardness(), BRICKS[i].resistance()));
        }
        // 14, 15: the TConstructBlock default, hardness 3
        FANCY_BRICKS.add(brick("fancy_stone_brick", 14, 3.0F, 3.0F));
        FANCY_BRICKS.add(brick("stone_road", 15, 3.0F, 3.0F));
    }

    // ------------------------------------------------------------------ brownstone

    /** SpeedBlock / SpeedSlab 0..6. */
    public static final String[] BROWNSTONE = {"rough_brownstone", "rough_brownstone_road", "brownstone", "brownstone_brick", "brownstone_road",
            "fancy_brownstone", "chiseled_brownstone"};
    public static final List<DeferredBlock<SpeedBlock.Full>> SPEED_BLOCKS = new ArrayList<>();
    public static final List<DeferredBlock<SpeedBlock.Slab>> SPEED_SLABS = new ArrayList<>();

    static {
        for (int i = 0; i < BROWNSTONE.length; i++) {
            boolean road = i == 1 || i == 4;
            SPEED_BLOCKS.add(block(BROWNSTONE[i], p -> new SpeedBlock.Full(road, p), () -> rock(3.0F, 3.0F).mapColor(MapColor.TERRACOTTA_BROWN)));
        }
        for (int i = 0; i < BROWNSTONE.length; i++) {
            boolean road = i == 1 || i == 4;
            SPEED_SLABS.add(block(BROWNSTONE[i] + "_slab", p -> new SpeedBlock.Slab(road, p), () -> rock(3.0F, 3.0F).mapColor(MapColor.TERRACOTTA_BROWN)));
        }
    }

    // ------------------------------------------------------------------ wool slabs, torch, hambone

    /** SlabBase on Block.cloth (WoolSlab1 0..7, WoolSlab2 8..15): no hardness set, so they break at once. */
    public static final List<DeferredBlock<SlabBlock>> WOOL_SLABS = new ArrayList<>();

    static {
        for (String color : COLORS) {
            WOOL_SLABS.add(block(color + "_wool_slab", SlabBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOL).instabreak()
                    .sound(SoundType.WOOL).ignitedByLava()));
        }
    }

    /** StoneTorch: a torch (light 15, Material.circuits). */
    public static final DeferredBlock<TorchBlock> STONE_TORCH = TBlocks.BLOCKS.registerBlock("stone_torch", p -> new TorchBlock(ParticleTypes.FLAME, p),
            () -> BlockBehaviour.Properties.of().noCollision().instabreak().lightLevel(s -> 15).sound(SoundType.STONE).pushReaction(PushReaction.POPPED));
    public static final DeferredBlock<WallTorchBlock> STONE_WALL_TORCH = TBlocks.BLOCKS.registerBlock("stone_wall_torch",
            p -> new WallTorchBlock(ParticleTypes.FLAME, p), () -> BlockBehaviour.Properties.of().noCollision().instabreak().lightLevel(s -> 15)
                    .sound(SoundType.STONE).pushReaction(PushReaction.POPPED).overrideDescription("block.tconstruct.stone_torch"));
    public static final DeferredItem<StandingAndWallBlockItem> STONE_TORCH_ITEM = TItems.ITEMS.registerItem("stone_torch",
            p -> new StandingAndWallBlockItem(STONE_TORCH.get(), STONE_WALL_TORCH.get(), Direction.DOWN, p), p -> p.useBlockDescriptionPrefix());

    static {
        ITEMS.add(STONE_TORCH_ITEM);
    }

    /** MeatBlock "Hambone": wood, hardness 1, a log turned to the side it was put on. */
    public static final DeferredBlock<RotatedPillarBlock> HAMBONE = block("hambone", RotatedPillarBlock::new,
            () -> BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_RED).strength(1.0F).sound(SoundType.WOOD).ignitedByLava());

    // ------------------------------------------------------------------ traps

    public static final DeferredBlock<PunjiBlock> PUNJI = block("punji_sticks", PunjiBlock::new,
            () -> BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).strength(3.0F).sound(SoundType.GRASS).noOcclusion()
                    .pushReaction(PushReaction.POPPED));
    public static final String[] WOODS = {"oak", "spruce", "birch", "jungle"};
    public static final List<DeferredBlock<BarricadeBlock>> BARRICADES = new ArrayList<>();

    static {
        for (String wood : WOODS) {
            BARRICADES.add(block(wood + "_barricade", BarricadeBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(4.0F)
                    .noOcclusion().ignitedByLava()));
        }
    }

    /** BlockLandmine with the item damage = trigger type: 0 everything (wood), 1 mobs (stone), 2 players (obsidian), 3 redstone only. */
    public static final String[] LANDMINES = {"landmine", "stone_landmine", "obsidian_landmine", "redstone_landmine"};
    public static final List<DeferredBlock<LandmineBlock>> LANDMINE_BLOCKS = new ArrayList<>();

    static {
        for (int i = 0; i < LANDMINES.length; i++) {
            int trigger = i;
            LANDMINE_BLOCKS.add(block(LANDMINES[i], p -> new LandmineBlock(trigger, p), () -> BlockBehaviour.Properties.of().strength(0.5F, 0.0F)
                    .sound(SoundType.METAL).noOcclusion().noCollision().lightLevel(s -> s.getValue(LIGHT)).pushReaction(PushReaction.POPPED)));
        }
    }

    // ------------------------------------------------------------------ redstone machines, essence extractor

    private static BlockBehaviour.Properties machine() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(12.0F, 12.0F).sound(SoundType.METAL).requiresCorrectToolForDrops();
    }

    public static final DeferredBlock<DrawbridgeBlock> DRAWBRIDGE = block("drawbridge", DrawbridgeBlock::new,
            () -> machine().lightLevel(s -> s.getValue(LIGHT)));
    public static final DeferredBlock<FirestarterBlock> IGNITER = block("igniter", FirestarterBlock::new, TDeco::machine);
    public static final DeferredBlock<EssenceExtractorBlock> ESSENCE_EXTRACTOR = block("essence_extractor", EssenceExtractorBlock::new,
            () -> rock(12.0F, 12.0F).noOcclusion());

    // ------------------------------------------------------------------ block entities, menus

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<LandmineBlockEntity>> LANDMINE_ENTITY = TBlocks.BLOCK_ENTITIES.register(
            "landmine", () -> new BlockEntityType<>(LandmineBlockEntity::new, LANDMINE_BLOCKS.stream().map(DeferredBlock::get).toArray(Block[]::new)));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DrawbridgeBlockEntity>> DRAWBRIDGE_ENTITY = TBlocks.BLOCK_ENTITIES.register(
            "drawbridge", () -> new BlockEntityType<>(DrawbridgeBlockEntity::new, DRAWBRIDGE.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<EssenceExtractorBlockEntity>> EXTRACTOR_ENTITY = TBlocks.BLOCK_ENTITIES.register(
            "essence_extractor", () -> new BlockEntityType<>(EssenceExtractorBlockEntity::new, ESSENCE_EXTRACTOR.get()));

    public static final DeferredHolder<MenuType<?>, MenuType<LandmineMenu>> LANDMINE_MENU = MENUS.register("landmine",
            () -> IMenuTypeExtension.create((id, inv, buf) -> new LandmineMenu(id, inv)));
    public static final DeferredHolder<MenuType<?>, MenuType<DrawbridgeMenu>> DRAWBRIDGE_MENU = MENUS.register("drawbridge",
            () -> IMenuTypeExtension.create((id, inv, buf) -> new DrawbridgeMenu(id, inv, buf.readVarInt())));

    // ------------------------------------------------------------------ chisel steps (TContent.addCraftingRecipes)

    static {
        int[][] speed = {{0, 1}, {2, 3}, {3, 4}, {4, 5}, {5, 6}};
        for (int[] step : speed) {
            Detailing.add(() -> SPEED_BLOCKS.get(step[0]).get(), () -> SPEED_BLOCKS.get(step[1]).get().defaultBlockState());
        }
        Detailing.add(() -> Blocks.OBSIDIAN, () -> MULTI_BRICKS.get(0).get().defaultBlockState());
        Detailing.add(() -> Blocks.CHISELED_SANDSTONE, () -> MULTI_BRICKS.get(1).get().defaultBlockState());
        Detailing.addItem(() -> Items.IRON_INGOT, () -> MULTI_BRICKS.get(4).get().defaultBlockState());
        Detailing.addItem(() -> Items.GOLD_INGOT, () -> MULTI_BRICKS.get(5).get().defaultBlockState());
        Detailing.addItem(() -> Items.LAPIS_LAZULI, () -> MULTI_BRICKS.get(6).get().defaultBlockState());
        Detailing.addItem(() -> Items.DIAMOND, () -> MULTI_BRICKS.get(7).get().defaultBlockState());
        Detailing.addItem(() -> Items.REDSTONE, () -> MULTI_BRICKS.get(8).get().defaultBlockState());
        Detailing.addItem(() -> Items.BONE, () -> MULTI_BRICKS.get(9).get().defaultBlockState());
        Detailing.addItem(() -> Items.SLIME_BALL, () -> MULTI_BRICKS.get(10).get().defaultBlockState());
        Detailing.addItem(() -> TWorld.GELATINOUS_SLIME.get(), () -> MULTI_BRICKS.get(11).get().defaultBlockState());
        Detailing.add(() -> Blocks.END_STONE, () -> MULTI_BRICKS.get(12).get().defaultBlockState());
        Detailing.addItem(() -> TItems.MATERIALS.get("obsidian_ingot").get(), () -> MULTI_BRICKS.get(13).get().defaultBlockState());
        for (int i = 0; i < 14; i++) {
            int meta = i;
            Detailing.add(() -> MULTI_BRICKS.get(meta).get(), () -> FANCY_BRICKS.get(meta).get().defaultBlockState());
        }
        Detailing.add(() -> Blocks.STONE_BRICKS, () -> FANCY_BRICKS.get(15).get().defaultBlockState());
        Detailing.add(() -> FANCY_BRICKS.get(15).get(), () -> FANCY_BRICKS.get(14).get().defaultBlockState());
        Detailing.add(() -> FANCY_BRICKS.get(14).get(), () -> Blocks.CHISELED_STONE_BRICKS.defaultBlockState());
    }

    private TDeco() {
    }

    static void register(IEventBus modBus) {
        MENUS.register(modBus);
    }

    /** Old ids of TinkersWorkshop.txt (blocks: game id = config id). */
    static void registerOldIds() {
        String ns = TConstruct.MOD_ID + ":";
        OldBlockStates.register(1329, () -> CLEAR_GLASS.get().defaultBlockState());
        OldGameIds.registerItem(1329, ns + "clear_glass");
        OldBlockStates.registerConnecting(CLEAR_GLASS);
        OldBlockStates.registerMeta(1333, meta -> GLASS_PANES.get(Math.min(meta, 2)).get().defaultBlockState());
        for (int i = 0; i < PANES.length; i++) {
            OldGameIds.registerItem(1333, i, ns + PANES[i]);
            OldBlockStates.registerConnecting(GLASS_PANES.get(i));
        }
        OldBlockStates.registerMeta(1330, meta -> STAINED_GLASS.get(meta & 15).get().defaultBlockState());
        OldBlockStates.registerMeta(1331, meta -> STAINED_PANES.get(meta & 15).get().defaultBlockState());
        for (int i = 0; i < 16; i++) {
            OldGameIds.registerItem(1330, i, ns + COLORS[i] + "_clear_stained_glass");
            OldGameIds.registerItem(1331, i, ns + COLORS[i] + "_clear_stained_glass_pane");
            OldBlockStates.registerConnecting(STAINED_GLASS.get(i));
            OldBlockStates.registerConnecting(STAINED_PANES.get(i));
        }
        OldBlockStates.registerMeta(1316, meta -> MULTI_BRICKS.get(Math.min(meta, 13)).get().defaultBlockState());
        OldBlockStates.registerMeta(1336, meta -> FANCY_BRICKS.get(meta & 15).get().defaultBlockState());
        for (int i = 0; i < 16; i++) {
            if (i < 14) {
                OldGameIds.registerItem(1316, i, ns + BuiltIn.name(MULTI_BRICKS.get(i)));
            }
            OldGameIds.registerItem(1336, i, ns + BuiltIn.name(FANCY_BRICKS.get(i)));
        }
        OldBlockStates.registerMeta(1324, meta -> SPEED_BLOCKS.get(Math.min(meta, 6)).get().defaultBlockState());
        OldBlockStates.registerMeta(1339, meta -> SPEED_SLABS.get(Math.min(meta % 8, 6)).get().defaultBlockState()
                .setValue(SlabBlock.TYPE, meta >= 8 ? SlabType.TOP : SlabType.BOTTOM));
        for (int i = 0; i < 7; i++) {
            OldGameIds.registerItem(1324, i, ns + BROWNSTONE[i]);
            OldGameIds.registerItem(1339, i, ns + BROWNSTONE[i] + "_slab");
        }
        OldBlockStates.registerMeta(2389, meta -> WOOL_SLABS.get(meta % 8).get().defaultBlockState()
                .setValue(SlabBlock.TYPE, meta >= 8 ? SlabType.TOP : SlabType.BOTTOM));
        OldBlockStates.registerMeta(2390, meta -> WOOL_SLABS.get(8 + meta % 8).get().defaultBlockState()
                .setValue(SlabBlock.TYPE, meta >= 8 ? SlabType.TOP : SlabType.BOTTOM));
        for (int i = 0; i < 8; i++) {
            OldGameIds.registerItem(2389, i, ns + COLORS[i] + "_wool_slab");
            OldGameIds.registerItem(2390, i, ns + COLORS[8 + i] + "_wool_slab");
        }
        // StoneTorch: the metadata of BlockTorch (1 east wall .. 4 north wall, 5 standing)
        OldBlockStates.registerMeta(1325, meta -> switch (meta) {
            case 1 -> STONE_WALL_TORCH.get().defaultBlockState().setValue(WallTorchBlock.FACING, Direction.EAST);
            case 2 -> STONE_WALL_TORCH.get().defaultBlockState().setValue(WallTorchBlock.FACING, Direction.WEST);
            case 3 -> STONE_WALL_TORCH.get().defaultBlockState().setValue(WallTorchBlock.FACING, Direction.SOUTH);
            case 4 -> STONE_WALL_TORCH.get().defaultBlockState().setValue(WallTorchBlock.FACING, Direction.NORTH);
            default -> STONE_TORCH.get().defaultBlockState();
        });
        OldGameIds.registerItem(1325, ns + "stone_torch");
        // MeatBlock: meta / 4 = 0 up, 1 east-west, 2 north-south
        OldBlockStates.registerMeta(2381, meta -> HAMBONE.get().defaultBlockState().setValue(RotatedPillarBlock.AXIS,
                switch (meta / 4) {
                    case 1 -> Direction.Axis.X;
                    case 2 -> Direction.Axis.Z;
                    default -> Direction.Axis.Y;
                }));
        OldGameIds.registerItem(2381, ns + "hambone");
        OldBlockStates.registerMeta(2383, meta -> PUNJI.get().defaultBlockState().setValue(PunjiBlock.STICKS, Math.min(meta, 4) + 1));
        OldGameIds.registerItem(2383, ns + "punji_sticks");
        int[] barricades = {2382, 2388, 1343, 1349};
        Direction[] rotation = {Direction.SOUTH, Direction.WEST, Direction.NORTH, Direction.EAST};
        for (int i = 0; i < 4; i++) {
            DeferredBlock<BarricadeBlock> barricade = BARRICADES.get(i);
            OldBlockStates.registerMeta(barricades[i], meta -> barricade.get().defaultBlockState()
                    .setValue(net.minecraft.world.level.block.HorizontalDirectionalBlock.FACING, rotation[(meta / 4) & 3]).setValue(BarricadeBlock.LOGS, meta % 4 + 1));
            OldGameIds.registerItem(barricades[i], ns + WOODS[i] + "_barricade");
        }
        // BlockLandmine: the trigger type was in the tile entity; meta & 7 = the side it lies on
        OldBlockStates.registerMeta(1334, meta -> LANDMINE_BLOCKS.get(0).get().defaultBlockState().setValue(LandmineBlock.ATTACH,
                switch (meta & 7) {
                    case 1 -> Direction.WEST;
                    case 2 -> Direction.EAST;
                    case 3 -> Direction.NORTH;
                    case 4 -> Direction.SOUTH;
                    case 0, 7 -> Direction.UP;
                    default -> Direction.DOWN;
                }));
        for (int i = 0; i < 4; i++) {
            OldGameIds.registerItem(1334, i, ns + LANDMINES[i]);
        }
        OldBlockStates.registerMeta(1337, meta -> (meta == 1 ? IGNITER.get() : DRAWBRIDGE.get()).defaultBlockState());
        OldGameIds.registerItem(1337, 0, ns + "drawbridge");
        OldGameIds.registerItem(1337, 1, ns + "igniter");
        OldBlockStates.register(1348, () -> ESSENCE_EXTRACTOR.get().defaultBlockState());
        OldGameIds.registerItem(1348, ns + "essence_extractor");
    }

    /** The path of a registry name without touching the registry. */
    private static final class BuiltIn {
        static String name(DeferredBlock<?> block) {
            return block.getId().getPath();
        }
    }
}
