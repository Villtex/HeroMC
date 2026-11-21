package tconstruct;

import hexxit.worldgen152.OldBlockStates;
import hexxit.worldgen152.OldGameIds;
import hexxit.worldgen152.OldModData;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.TradeSet;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.jspecify.annotations.Nullable;
import tconstruct.blocks.PatternChestBlockEntity;
import tconstruct.gen.ComponentToolWorkshop;
import tconstruct.gen.TCWorldGen;
import tconstruct.world.OreberryBushBlock;
import tconstruct.world.OreberryItem;
import tconstruct.world.SlimeFluid;
import tconstruct.world.SlimeGelBlock;
import tconstruct.world.SlimeGrassBlock;
import tconstruct.world.SlimeLeavesBlock;
import tconstruct.world.SlimeSaplingBlock;
import tconstruct.world.SlimeTallGrassBlock;

import java.util.ArrayList;
import java.util.List;

/**
 * Part 25d, the world of Tinkers' Construct (TContent.registerBlocks/registerItems for these blocks): ore berry bushes
 * and ore berries, the slime island blocks (congealed slime, slime grass, tall grass, leaves, sapling, liquid blue
 * slime), gelatinous slime and the miniature red heart (loot of the King Slime and of chests), the tinker villager.
 * World generation itself is {@link TCWorldGen}.
 */
public final class TWorld {
    private static final DeferredRegister<FluidType> FLUID_TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES, TConstruct.MOD_ID);
    private static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(Registries.FLUID, TConstruct.MOD_ID);
    private static final DeferredRegister<VillagerProfession> PROFESSIONS = DeferredRegister.create(Registries.VILLAGER_PROFESSION, TConstruct.MOD_ID);

    /** The block items of this part in creative tab order. */
    public static final List<DeferredItem<?>> ITEMS = new ArrayList<>();

    // ------------------------------------------------------------------ items

    /** OreBerries 0..5. */
    public static final String[] BERRIES = {"iron", "gold", "copper", "tin", "aluminum", "essence"};
    public static final List<DeferredItem<OreberryItem>> ORE_BERRIES = new ArrayList<>();

    static {
        for (int i = 0; i < BERRIES.length; i++) {
            boolean essence = i == 5;
            ORE_BERRIES.add(TItems.ITEMS.registerItem(BERRIES[i] + "_oreberry", p -> new OreberryItem(essence, p)));
        }
    }

    /** StrangeFood 0: 2 hunger, saturation 1.0. */
    public static final DeferredItem<Item> GELATINOUS_SLIME = TItems.ITEMS.registerItem("gelatinous_slime", Item::new,
            p -> p.food(new FoodProperties.Builder().nutrition(2).saturationModifier(1.0F).build()));
    /** HeartCanister 1 (the canisters themselves follow with part 25e). */
    public static final DeferredItem<Item> MINIATURE_RED_HEART = TItems.ITEMS.registerItem("miniature_red_heart", Item::new, p -> p.stacksTo(10));

    // ------------------------------------------------------------------ blocks

    private static <B extends Block> DeferredBlock<B> block(String name, java.util.function.Function<BlockBehaviour.Properties, B> factory,
                                                           java.util.function.Supplier<BlockBehaviour.Properties> properties) {
        DeferredBlock<B> block = TBlocks.BLOCKS.registerBlock(name, factory, properties);
        ITEMS.add(TItems.ITEMS.registerSimpleBlockItem(block));
        return block;
    }

    /** OreberryBush (1317: iron, gold, copper, tin) and OreberryBushEssence (1318: aluminum, essence). */
    public static final List<DeferredBlock<OreberryBushBlock>> BUSHES = new ArrayList<>();

    static {
        for (int i = 0; i < BERRIES.length; i++) {
            int index = i;
            BUSHES.add(block(BERRIES[i] + "_oreberry_bush", p -> new OreberryBushBlock(index >= 4, ORE_BERRIES.get(index), p),
                    () -> BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).strength(0.3F).sound(SoundType.METAL).noOcclusion()
                            .isSuffocating((s, l, p) -> false).pushReaction(PushReaction.POPPED)));
        }
    }

    /** SlimeGel 0..2. */
    public static final String[] GEL_COLORS = {"blue", "green", "purple"};
    public static final List<DeferredBlock<SlimeGelBlock>> GEL = new ArrayList<>();

    static {
        for (String color : GEL_COLORS) {
            GEL.add(block("congealed_" + color + "_slime", SlimeGelBlock::new,
                    () -> BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_LIGHT_BLUE).strength(0.5F).sound(SoundType.SLIME_BLOCK)));
        }
    }

    private static BlockBehaviour.Properties slimeGrass() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.GRASS).strength(0.6F).sound(SoundType.GRASS).randomTicks();
    }

    /** SlimeGrass 0 (on blue slime dirt) and 1 (on dirt). */
    public static final DeferredBlock<SlimeGrassBlock> SLIME_GRASS = block("slime_grass", SlimeGrassBlock::new, TWorld::slimeGrass);
    public static final DeferredBlock<SlimeGrassBlock> SLIME_GRASS_DIRT = block("slime_grass_dirt", SlimeGrassBlock::new, TWorld::slimeGrass);
    public static final DeferredBlock<SlimeTallGrassBlock> SLIME_TALL_GRASS = block("slime_tall_grass", SlimeTallGrassBlock::new,
            () -> BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_LIGHT_BLUE).replaceable().noCollision().instabreak().sound(SoundType.GRASS)
                    .offsetType(BlockBehaviour.OffsetType.XYZ).pushReaction(PushReaction.POPPED));
    public static final DeferredBlock<SlimeLeavesBlock> SLIME_LEAVES = block("slime_leaves", SlimeLeavesBlock::new,
            () -> BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_LIGHT_BLUE).strength(0.2F).randomTicks().sound(SoundType.SLIME_BLOCK)
                    .noOcclusion().isValidSpawn((s, l, p, e) -> false).isSuffocating((s, l, p) -> false)
                    .ignitedByLava().pushReaction(PushReaction.POPPED).isRedstoneConductor((s, l, p) -> false));
    public static final DeferredBlock<SlimeSaplingBlock> SLIME_SAPLING = block("slime_sapling", SlimeSaplingBlock::new,
            () -> BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).noCollision().randomTicks().instabreak().sound(SoundType.SLIME_BLOCK)
                    .pushReaction(PushReaction.POPPED));

    // ------------------------------------------------------------------ liquid blue slime (1350, Material.water)
    // water-like: entities swim in it as in water (without it NeoForge does not move them at all)

    public static final DeferredHolder<FluidType, FluidType> SLIME_TYPE = FLUID_TYPES.register("liquid_blue_slime",
            () -> new FluidType(FluidType.Properties.create().descriptionId("block.tconstruct.liquid_blue_slime").canExtinguish(true)
                    .canConvertToSource(false).isWaterLike(true).supportsBoating(true).canHydrate(true).motionScale(0.014).canSwim(true).canDrown(true)
                    .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL).sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY)));

    private static final class FluidHolder {
        BaseFlowingFluid.@Nullable Properties props;
    }

    private static final FluidHolder FLUID_PROPS = new FluidHolder();
    public static final DeferredHolder<Fluid, SlimeFluid.Source> SLIME_SOURCE = FLUIDS.register("liquid_blue_slime",
            () -> new SlimeFluid.Source(FLUID_PROPS.props));
    public static final DeferredHolder<Fluid, SlimeFluid.Flowing> SLIME_FLOWING = FLUIDS.register("flowing_liquid_blue_slime",
            () -> new SlimeFluid.Flowing(FLUID_PROPS.props));
    public static final DeferredBlock<LiquidBlock> SLIME_POOL = TBlocks.BLOCKS.registerBlock("liquid_blue_slime",
            p -> new LiquidBlock(SLIME_SOURCE.get(), p), () -> BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_LIGHT_BLUE).replaceable()
                    .noCollision().strength(100.0F).pushReaction(PushReaction.POPPED).noLootTable().liquid().sound(SoundType.EMPTY));

    static {
        // BlockFluidClassic: 8 quanta (7 blocks of flow), tick rate 20, no new sources
        FLUID_PROPS.props = new BaseFlowingFluid.Properties(SLIME_TYPE, SLIME_SOURCE, SLIME_FLOWING).block(SLIME_POOL).levelDecreasePerBlock(1)
                .tickRate(20).slopeFindDistance(4).explosionResistance(100.0F);
    }

    // ------------------------------------------------------------------ villager 78943

    public static final ResourceKey<VillagerProfession> TINKER = ResourceKey.create(Registries.VILLAGER_PROFESSION, TConstruct.id("tinker"));
    public static final ResourceKey<TradeSet> TINKER_TRADES = ResourceKey.create(Registries.TRADE_SET, TConstruct.id("tinker/level_1"));

    static {
        // VillagerRegistry.registerVillagerType: no work station, the villagers of the tool workshop are born with it; the
        // chunk writer gives structure villagers level 2 (so they keep their profession), they pick their trades from there
        PROFESSIONS.register("tinker", () -> new VillagerProfession(Component.translatable("entity.minecraft.villager.tconstruct.tinker"),
                PoiType.NONE, PoiType.NONE, com.google.common.collect.ImmutableSet.of(), com.google.common.collect.ImmutableSet.of(), null,
                Int2ObjectMap.ofEntries(Int2ObjectMap.entry(1, TINKER_TRADES), Int2ObjectMap.entry(2, TINKER_TRADES))));
    }

    private TWorld() {
    }

    static void register(IEventBus modBus) {
        FLUID_TYPES.register(modBus);
        FLUIDS.register(modBus);
        PROFESSIONS.register(modBus);
    }

    /** Old ids of TinkersWorkshop.txt: blocks (game id = config id) and items (config id + 256). */
    static void registerOldIds() {
        String ns = TConstruct.MOD_ID + ":";
        OldBlockStates.registerMeta(1317, meta -> BUSHES.get(meta & 3).get().defaultBlockState().setValue(OreberryBushBlock.AGE, meta >> 2 & 3));
        OldBlockStates.registerMeta(1318, meta -> BUSHES.get(4 + Math.min(meta & 3, 1)).get().defaultBlockState()
                .setValue(OreberryBushBlock.AGE, meta >> 2 & 3));
        for (int meta = 0; meta < 16; meta++) {
            OldGameIds.registerItem(1317, meta, ns + BERRIES[meta & 3] + "_oreberry_bush");
            OldGameIds.registerItem(1318, meta, ns + BERRIES[4 + Math.min(meta & 3, 1)] + "_oreberry_bush");
        }
        for (int i = 0; i < BERRIES.length; i++) {
            OldGameIds.registerItem(6037 + 256, i, ns + BERRIES[i] + "_oreberry");
        }
        OldGameIds.registerItem(6038 + 256, 0, ns + "gelatinous_slime");
        OldGameIds.registerItem(6027 + 256, 1, ns + "miniature_red_heart");
        OldBlockStates.registerMeta(1345, meta -> GEL.get(Math.min(meta, 2)).get().defaultBlockState());
        for (int i = 0; i < GEL_COLORS.length; i++) {
            OldGameIds.registerItem(1345, i, ns + "congealed_" + GEL_COLORS[i] + "_slime");
        }
        OldBlockStates.registerMeta(1350, meta -> SLIME_POOL.get().defaultBlockState().setValue(LiquidBlock.LEVEL, meta & 15));
        OldBlockStates.registerMeta(2384, meta -> ((meta & 1) == 1 ? SLIME_GRASS_DIRT : SLIME_GRASS).get().defaultBlockState());
        OldGameIds.registerItem(2384, 0, ns + "slime_grass");
        OldGameIds.registerItem(2384, 1, ns + "slime_grass_dirt");
        OldBlockStates.register(2385, () -> SLIME_LEAVES.get().defaultBlockState());
        OldGameIds.registerItem(2385, ns + "slime_leaves");
        OldBlockStates.register(2386, () -> SLIME_TALL_GRASS.get().defaultBlockState());
        OldGameIds.registerItem(2386, ns + "slime_tall_grass");
        OldBlockStates.registerMeta(2387, meta -> SLIME_SAPLING.get().defaultBlockState().setValue(SlimeSaplingBlock.STAGE, (meta & 8) != 0 ? 1 : 0));
        OldGameIds.registerItem(2387, ns + "slime_sapling");
        OldGameIds.registerProfession(ComponentToolWorkshop.VILLAGER, TINKER);
        // the pattern chest of the tool workshop: 30 slots
        OldModData.registerTile(ComponentToolWorkshop.PatternChest.class, (tile, blockEntity, registries) -> {
            if (blockEntity instanceof PatternChestBlockEntity chest) {
                for (int i = 0; i < tile.slots().length && i < chest.getContainerSize(); i++) {
                    if (tile.slots()[i] != null) {
                        ItemStack stack = OldGameIds.stack(tile.slots()[i], registries);
                        chest.setItem(i, stack);
                    }
                }
            }
        });
    }
}
