package natura;

import hexxit.worldgen152.OldBlockStates;
import hexxit.worldgen152.OldLiveGen;
import hexxit.worldgen152.core.populate.OldGen;
import natura.block.BerryBushBlock;
import natura.block.BloodwoodLogBlock;
import natura.block.NaturaCropBlock;
import natura.block.NaturaSaplingBlock;
import natura.block.NaturaTree;
import natura.block.SaguaroFruitBlock;
import natura.block.SaguaroSproutBlock;
import natura.gen.BloodTreeLargeGen;
import natura.gen.BushTreeGen;
import natura.gen.DarkwoodGen;
import natura.gen.EucalyptusTreeGenShort;
import natura.gen.FusewoodGen;
import natura.gen.GlowshroomGenBlueGreen;
import natura.gen.GlowshroomGenPurple;
import natura.gen.NaturaIds;
import natura.gen.RareTreeGen;
import natura.gen.RedwoodTreeGen;
import natura.gen.SaguaroGen;
import natura.gen.SakuraTreeGen;
import natura.gen.WhiteTreeGen;
import natura.gen.WillowGen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;

import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

/**
 * Natura 2.1.5.1 for NeoForge. The 1.5.2 Nether of Natura (Netherite chunk provider and the Nether parts of the
 * world generators) is ported in part 7b; blocks, items and mobs of the Nether are here.
 */
@Mod(Natura.MOD_ID)
public final class Natura {
    public static final String MOD_ID = "natura";

    public Natura(IEventBus modBus) {
        NaturaBlocks.register(modBus);
        NaturaEntities.register(modBus);
        natura.gen.NaturaWorldGen.register();
        registerOldBlocks();
        NaturaTree.setGrower(Natura::grow);
        if (FMLEnvironment.getDist() == Dist.CLIENT) {
            natura.client.NaturaClient.register(modBus);
        }
    }

    // ---- saplings, saguaro sprouts and glowshrooms: the 1.5.2 generators in the live world ----

    /**
     * The generator of the original for {@code tree}, and how far it reaches from the sapling (sides, below, above;
     * measured over many seeds with a margin for the blocks the generators only look at).
     */
    private record Grower(Supplier<OldGen> gen, int radius, int depth, int height) {
    }

    private static Grower grower(NaturaTree tree) {
        return switch (tree) {
            case REDWOOD -> new Grower(() -> new RedwoodTreeGen(true, NaturaIds.REDWOOD), 25, 22, 156);
            case EUCALYPTUS -> new Grower(() -> new EucalyptusTreeGenShort(0, 1), 9, 3, 18);
            case HOPSEED -> new Grower(() -> new BushTreeGen(true, 2, 3, 2), 9, 3, 8);
            case SAKURA -> new Grower(() -> new SakuraTreeGen(true, 1, 0), 10, 3, 19);
            case GHOSTWOOD -> new Grower(() -> new WhiteTreeGen(true, 2, 1), 10, 3, 19);
            // hangs from the ceiling above the sapling
            case BLOODWOOD -> new Grower(() -> new BloodTreeLargeGen(3, 2), 10, 24, 4);
            case DARKWOOD -> new Grower(() -> new DarkwoodGen(true, 3, 0), 6, 3, 10);
            case FUSEWOOD -> new Grower(() -> new FusewoodGen(true, 3, 1), 6, 3, 10);
            case MAPLE -> new Grower(() -> new RareTreeGen(true, 4, 2, 0, 0), 6, 3, 10);
            case SILVERBELL -> new Grower(() -> new RareTreeGen(true, 4, 2, 1, 1), 6, 3, 10);
            case AMARANTH -> new Grower(() -> new RareTreeGen(true, 9, 8, 2, 2), 6, 3, 20);
            case TIGER -> new Grower(() -> new RareTreeGen(true, 6, 4, 3, 3), 6, 3, 14);
            case WILLOW -> new Grower(() -> new WillowGen(true), 8, 6, 14);
            case SAGUARO -> new Grower(() -> new SaguaroGen(NaturaIds.SAGUARO, 0, true), 6, 3, 10);
            case GREEN_GLOWSHROOM -> new Grower(() -> new GlowshroomGenBlueGreen(true, 0), 6, 3, 10);
            case PURPLE_GLOWSHROOM -> new Grower(() -> new GlowshroomGenPurple(true), 6, 3, 10);
            case BLUE_GLOWSHROOM -> new Grower(() -> new GlowshroomGenBlueGreen(true, 1), 6, 3, 10);
        };
    }

    private static boolean grow(ServerLevel level, BlockPos pos, NaturaTree tree, Random random) {
        Grower g = grower(tree);
        return OldLiveGen.generate(level, g.gen().get(), random, pos.getX(), pos.getY(), pos.getZ(), g.radius(), g.depth(), g.height());
    }

    // ---- ids of the pack -> block states ----

    /** 1.5.2 orientation bits of a log (metadata 4/8) to the axis of today. */
    private static Direction.Axis axis(int meta) {
        return switch (meta & 12) {
            case 4 -> Direction.Axis.X;
            case 8 -> Direction.Axis.Z;
            default -> Direction.Axis.Y;
        };
    }

    /** Block of a metadata value from a list of sub-blocks (values beyond the list fall back to the first). */
    private static BlockState byMeta(List<? extends Supplier<? extends Block>> blocks, int meta) {
        return (meta < blocks.size() ? blocks.get(meta) : blocks.get(0)).get().defaultBlockState();
    }

    private static BlockState log(BlockState state, int meta) {
        return state.hasProperty(RotatedPillarBlock.AXIS) ? state.setValue(RotatedPillarBlock.AXIS, axis(meta)) : state;
    }

    private static void logs(int id, List<? extends Supplier<? extends Block>> blocks) {
        OldBlockStates.registerMeta(id, meta -> log(byMeta(blocks, meta & 3), meta));
    }

    private static void leaves(int id, List<? extends Supplier<? extends Block>> blocks) {
        OldBlockStates.registerMeta(id, meta -> byMeta(blocks, meta & 3));
    }

    private static void saplings(int id, List<? extends Supplier<? extends Block>> blocks) {
        OldBlockStates.registerMeta(id, meta -> byMeta(blocks, meta & 7).setValue(NaturaSaplingBlock.STAGE, (meta & 8) != 0 ? 1 : 0));
    }

    private static void bushes(int id, List<? extends Supplier<? extends Block>> blocks) {
        OldBlockStates.registerMeta(id, meta -> byMeta(blocks, meta & 3).setValue(BerryBushBlock.AGE, BerryBushBlock.age(meta)));
    }

    /** LargeGlowshroom: the faces as on the huge mushrooms of vanilla (1-9 cap, 10 stem with sides, 14 all cap, 15 all stem). */
    private static void largeGlowshroom(int id, Supplier<? extends Block> cap, Supplier<? extends Block> stem) {
        OldBlockStates.registerMeta(id, meta -> {
            if (meta == 10 || meta == 15) {
                return stem.get().defaultBlockState()
                        .setValue(BlockStateProperties.NORTH, true).setValue(BlockStateProperties.SOUTH, true)
                        .setValue(BlockStateProperties.WEST, true).setValue(BlockStateProperties.EAST, true)
                        .setValue(BlockStateProperties.UP, meta == 15).setValue(BlockStateProperties.DOWN, meta == 15);
            }
            boolean all = meta == 14;
            boolean top = meta >= 1 && meta <= 9;
            return cap.get().defaultBlockState()
                    .setValue(BlockStateProperties.UP, all || top)
                    .setValue(BlockStateProperties.DOWN, all)
                    .setValue(BlockStateProperties.NORTH, all || meta >= 1 && meta <= 3)
                    .setValue(BlockStateProperties.SOUTH, all || meta >= 7 && meta <= 9)
                    .setValue(BlockStateProperties.WEST, all || meta == 1 || meta == 4 || meta == 7)
                    .setValue(BlockStateProperties.EAST, all || meta == 3 || meta == 6 || meta == 9);
        });
    }

    /** The ids the 1.5.2 generation and the saplings write (Overworld and Nether). */
    private static void registerOldBlocks() {
        logs(NaturaIds.TREE, List.of(NaturaBlocks.EUCALYPTUS_LOG, NaturaBlocks.SAKURA_LOG, NaturaBlocks.GHOSTWOOD_LOG, NaturaBlocks.HOPSEED_LOG));
        // SimpleLog: no orientation
        OldBlockStates.registerMeta(NaturaIds.REDWOOD, meta -> byMeta(List.of(NaturaBlocks.REDWOOD_BARK, NaturaBlocks.REDWOOD_HEART,
                NaturaBlocks.REDWOOD_ROOT), meta));
        OldBlockStates.registerMeta(NaturaIds.BLOODWOOD, meta -> meta >= 12 ? NaturaBlocks.BLOODWOOD_BARK.get().defaultBlockState()
                : NaturaBlocks.BLOODWOOD_LOG.get().defaultBlockState().setValue(BloodwoodLogBlock.BARK, BloodwoodLogBlock.bark(meta))
                        .setValue(RotatedPillarBlock.AXIS, axis(meta)));
        logs(NaturaIds.RARE_TREE, List.of(NaturaBlocks.MAPLE_LOG, NaturaBlocks.SILVERBELL_LOG, NaturaBlocks.AMARANTH_LOG, NaturaBlocks.TIGER_LOG));
        logs(NaturaIds.WILLOW, List.of(NaturaBlocks.WILLOW_LOG));
        logs(NaturaIds.DARK_TREE, List.of(NaturaBlocks.DARKWOOD_LOG, NaturaBlocks.FUSEWOOD_LOG));

        leaves(NaturaIds.FLORA_LEAVES, List.of(NaturaBlocks.REDWOOD_LEAVES, NaturaBlocks.EUCALYPTUS_LEAVES, NaturaBlocks.HOPSEED_LEAVES,
                NaturaBlocks.REDWOOD_LEAVES));
        leaves(NaturaIds.FLORA_LEAVES_NO_COLOR, List.of(NaturaBlocks.SAKURA_LEAVES, NaturaBlocks.GHOSTWOOD_LEAVES, NaturaBlocks.BLOODWOOD_LEAVES,
                NaturaBlocks.WILLOW_LEAVES));
        leaves(NaturaIds.RARE_LEAVES, List.of(NaturaBlocks.MAPLE_LEAVES, NaturaBlocks.SILVERBELL_LEAVES, NaturaBlocks.AMARANTH_LEAVES,
                NaturaBlocks.TIGER_LEAVES));
        leaves(NaturaIds.DARK_LEAVES, List.of(NaturaBlocks.DARKWOOD_LEAVES, NaturaBlocks.FLOWERING_DARKWOOD_LEAVES,
                NaturaBlocks.FRUIT_DARKWOOD_LEAVES, NaturaBlocks.FUSEWOOD_LEAVES));

        saplings(NaturaIds.FLORA_SAPLING, List.of(NaturaBlocks.REDWOOD_SAPLING, NaturaBlocks.EUCALYPTUS_SAPLING, NaturaBlocks.HOPSEED_SAPLING,
                NaturaBlocks.SAKURA_SAPLING, NaturaBlocks.GHOSTWOOD_SAPLING, NaturaBlocks.BLOODWOOD_SAPLING, NaturaBlocks.DARKWOOD_SAPLING,
                NaturaBlocks.FUSEWOOD_SAPLING));
        saplings(NaturaIds.RARE_SAPLING, List.of(NaturaBlocks.MAPLE_SAPLING, NaturaBlocks.SILVERBELL_SAPLING, NaturaBlocks.AMARANTH_SAPLING,
                NaturaBlocks.TIGER_SAPLING, NaturaBlocks.WILLOW_SAPLING));

        bushes(NaturaIds.BERRY_BUSH, List.of(NaturaBlocks.RASPBERRY_BUSH, NaturaBlocks.BLUEBERRY_BUSH, NaturaBlocks.BLACKBERRY_BUSH,
                NaturaBlocks.MALOBERRY_BUSH));
        bushes(NaturaIds.NETHER_BERRY_BUSH, List.of(NaturaBlocks.BLIGHTBERRY_BUSH, NaturaBlocks.DUSKBERRY_BUSH, NaturaBlocks.SKYBERRY_BUSH,
                NaturaBlocks.STINGBERRY_BUSH));
        OldBlockStates.registerMeta(NaturaIds.CLOUD, meta -> byMeta(List.of(NaturaBlocks.WHITE_CLOUD, NaturaBlocks.DARK_CLOUD,
                NaturaBlocks.ASH_CLOUD, NaturaBlocks.SULFUR_CLOUD), meta & 3));
        // CropBlock: barley 0-3, cotton 4-8
        OldBlockStates.registerMeta(NaturaIds.CROPS, meta -> meta <= 3
                ? NaturaBlocks.BARLEY_CROP.get().defaultBlockState().setValue(NaturaCropBlock.Barley.AGE, meta)
                : NaturaBlocks.COTTON_CROP.get().defaultBlockState().setValue(NaturaCropBlock.Cotton.AGE, Math.min(meta - 4, 4)));
        OldBlockStates.register(NaturaIds.BLUEBELLS, () -> NaturaBlocks.BLUEBELLS.get().defaultBlockState());
        // SaguaroBlock: 0 trunk (arms from the neighbours), 1/2 sprout, 3-6 fruit
        OldBlockStates.registerMeta(NaturaIds.SAGUARO, meta -> switch (meta) {
            case 1, 2 -> NaturaBlocks.SAGUARO_SPROUT.get().defaultBlockState().setValue(SaguaroSproutBlock.AGE, meta - 1);
            case 3, 4, 5, 6 -> NaturaBlocks.SAGUARO_FRUIT.get().defaultBlockState().setValue(SaguaroFruitBlock.FACING, SaguaroFruitBlock.facing(meta));
            default -> NaturaBlocks.SAGUARO.get().defaultBlockState();
        });
        OldBlockStates.registerConnecting(NaturaBlocks.SAGUARO);
        // ThornVines (BlockVine): 1 south, 2 west, 4 north, 8 east; the top face comes from the block above (OldChunkWriter)
        OldBlockStates.registerMeta(NaturaIds.THORN_VINES, meta -> NaturaBlocks.THORNVINES.get().defaultBlockState()
                .setValue(BlockStateProperties.UP, false)
                .setValue(BlockStateProperties.SOUTH, (meta & 1) != 0)
                .setValue(BlockStateProperties.WEST, (meta & 2) != 0)
                .setValue(BlockStateProperties.NORTH, (meta & 4) != 0)
                .setValue(BlockStateProperties.EAST, (meta & 8) != 0));
        OldBlockStates.registerMeta(NaturaIds.GLOWSHROOM, meta -> byMeta(List.of(NaturaBlocks.GREEN_GLOWSHROOM, NaturaBlocks.PURPLE_GLOWSHROOM,
                NaturaBlocks.BLUE_GLOWSHROOM), meta));
        largeGlowshroom(NaturaIds.GLOWSHROOM_BLUE, NaturaBlocks.BLUE_GLOWSHROOM_BLOCK, NaturaBlocks.BLUE_GLOWSHROOM_STEM);
        largeGlowshroom(NaturaIds.GLOWSHROOM_GREEN, NaturaBlocks.GREEN_GLOWSHROOM_BLOCK, NaturaBlocks.GREEN_GLOWSHROOM_STEM);
        largeGlowshroom(NaturaIds.GLOWSHROOM_PURPLE, NaturaBlocks.PURPLE_GLOWSHROOM_BLOCK, NaturaBlocks.PURPLE_GLOWSHROOM_STEM);
        OldBlockStates.register(NaturaIds.TAINTED_SOIL, () -> NaturaBlocks.TAINTED_SOIL.get().defaultBlockState());
        OldBlockStates.register(NaturaIds.HEAT_SAND, () -> NaturaBlocks.HEAT_SAND.get().defaultBlockState());
        OldBlockStates.registerMeta(NaturaIds.PLANKS, meta -> NaturaBlocks.PLANKS.get(Wood.values()[meta < Wood.values().length ? meta : 0]).get()
                .defaultBlockState());
    }
}
