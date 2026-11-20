package tconstruct.smeltery;

import hexxit.worldgen152.OldBlockStates;
import hexxit.worldgen152.OldGameIds;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
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
import tconstruct.TConstruct;

import java.util.ArrayList;
import java.util.List;

/**
 * The 24 liquids of liquidMetalStill/liquidMetalFlowing (one block of 1.5.2, the kind in LiquidTextureLogic), as
 * NeoForge fluids with source, flowing fluid, block and bucket (FilledBucket damage = index). They flow like water of
 * 1.5.2 (7 blocks, every 30 ticks) and burn like lava (LiquidMetalBase.onEntityCollidedWithBlock, updateTick).
 */
public final class TFluids {
    static final DeferredRegister<FluidType> TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES, TConstruct.MOD_ID);
    static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(net.minecraft.core.registries.Registries.FLUID, TConstruct.MOD_ID);
    static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(TConstruct.MOD_ID);
    static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(TConstruct.MOD_ID);

    /** Index = liquid meta of 1.5.2: registry name, texture (LiquidMetalBase.textureNames), bucket texture. */
    public static final String[][] NAMES = {
            {"molten_iron", "iron", "iron"}, {"molten_gold", "gold", "gold"}, {"molten_copper", "copper", "copper"}, {"molten_tin", "tin", "tin"},
            {"molten_aluminum", "aluminum", "aluminum"}, {"molten_cobalt", "cobalt", "cobalt"}, {"molten_ardite", "ardite", "ardite"},
            {"molten_bronze", "bronze", "bronze"}, {"molten_alubrass", "alubrass", "alubrass"}, {"molten_manyullyn", "manyullyn", "manyullyn"},
            {"molten_alumite", "alumite", "alumite"}, {"molten_obsidian", "obsidian", "obsidian"}, {"molten_steel", "steel", "steel"},
            {"molten_glass", "glass", "glass"}, {"seared_stone", "stone", "stone"}, {"molten_emerald", "villager", "emerald"},
            {"blood", "cow", "blood"}, {"molten_nickel", "ferrous", "nickel"}, {"molten_lead", "lead", "lead"}, {"molten_silver", "silver", "silver"},
            {"molten_platinum", "shiny", "shiny"}, {"molten_invar", "invar", "invar"}, {"molten_electrum", "electrum", "electrum"},
            {"liquid_ender", "ender", "ender"}
    };
    public static final int IRON = 0, GOLD = 1, COPPER = 2, TIN = 3, ALUMINUM = 4, COBALT = 5, ARDITE = 6, BRONZE = 7, ALUBRASS = 8, MANYULLYN = 9,
            ALUMITE = 10, OBSIDIAN = 11, STEEL = 12, GLASS = 13, STONE = 14, EMERALD = 15, BLOOD = 16, NICKEL = 17, LEAD = 18, SILVER = 19,
            PLATINUM = 20, INVAR = 21, ELECTRUM = 22, ENDER = 23;

    public record Liquid(int index, String name, DeferredHolder<FluidType, FluidType> type, DeferredHolder<Fluid, BaseFlowingFluid.Source> source,
                         DeferredHolder<Fluid, BaseFlowingFluid.Flowing> flowing, DeferredBlock<LiquidBlock> block, DeferredItem<BucketItem> bucket) {
    }

    public static final List<Liquid> ALL = new ArrayList<>();

    static {
        for (int i = 0; i < NAMES.length; i++) {
            String name = NAMES[i][0];
            DeferredHolder<FluidType, FluidType> type = TYPES.register(name, () -> new FluidType(FluidType.Properties.create()
                    .descriptionId("fluid." + TConstruct.MOD_ID + "." + name).lightLevel(9).density(3000).viscosity(6000).temperature(1300)
                    .canSwim(false).canDrown(false).canExtinguish(false).canConvertToSource(false).canHydrate(false).motionScale(0.0023333333)
                    .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL_LAVA).sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY_LAVA)));
            Holder h = new Holder();
            DeferredHolder<Fluid, BaseFlowingFluid.Source> source = FLUIDS.register(name, () -> new MoltenSource(h.props));
            DeferredHolder<Fluid, BaseFlowingFluid.Flowing> flowing = FLUIDS.register("flowing_" + name, () -> new BaseFlowingFluid.Flowing(h.props));
            DeferredBlock<LiquidBlock> block = BLOCKS.registerBlock(name, p -> new MoltenBlock(source.get(), p), () -> BlockBehaviour.Properties.of()
                    .mapColor(MapColor.FIRE).replaceable().noCollision().strength(100.0F).lightLevel(s -> 9).pushReaction(PushReaction.POPPED)
                    .noLootTable().liquid());
            DeferredItem<BucketItem> bucket = ITEMS.registerItem(name + "_bucket", p -> new BucketItem(source.get(), p),
                    p -> p.craftRemainder(Items.BUCKET).stacksTo(1));
            h.props = new BaseFlowingFluid.Properties(type, source, flowing).bucket(bucket).block(block).levelDecreasePerBlock(1).tickRate(30)
                    .slopeFindDistance(4).explosionResistance(100.0F);
            ALL.add(new Liquid(i, name, type, source, flowing, block, bucket));
        }
    }

    private static final class Holder {
        BaseFlowingFluid.@Nullable Properties props;
    }

    public static Liquid get(int index) {
        return ALL.get(index);
    }

    public static Fluid fluid(int index) {
        return ALL.get(index).source().get();
    }

    /** The liquid of a fluid (source or flowing), or null for fluids of other mods and vanilla. */
    public static @Nullable Liquid of(Fluid fluid) {
        for (Liquid liquid : ALL) {
            if (liquid.source().get() == fluid || liquid.flowing().get() == fluid) {
                return liquid;
            }
        }
        return null;
    }

    private TFluids() {
    }

    public static void register(IEventBus modBus) {
        TYPES.register(modBus);
        FLUIDS.register(modBus);
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
    }

    /** Liquid metal (1313/1314) and buckets (6033 + 256, damage = index); the kind sat in a tile entity, molten iron stands in. */
    public static void registerOldIds() {
        OldBlockStates.registerMeta(1314, meta -> get(IRON).block().get().defaultBlockState());
        OldBlockStates.registerMeta(1313, meta -> get(IRON).block().get().defaultBlockState());
        for (Liquid liquid : ALL) {
            OldGameIds.registerItem(6033 + 256, liquid.index(), TConstruct.MOD_ID + ":" + liquid.name() + "_bucket");
        }
    }

    /** LiquidMetalStill.updateTick: sets flammable surroundings on fire like lava. */
    static final class MoltenSource extends BaseFlowingFluid.Source {
        MoltenSource(BaseFlowingFluid.Properties properties) {
            super(properties);
        }

        @Override
        protected boolean isRandomlyTicking() {
            return true;
        }

        @Override
        protected void randomTick(ServerLevel level, BlockPos pos, FluidState state, RandomSource random) {
            int tries = random.nextInt(3);
            BlockPos p = pos;
            for (int i = 0; i < tries; i++) {
                p = p.offset(random.nextInt(3) - 1, 1, random.nextInt(3) - 1);
                if (!level.isLoaded(p)) {
                    return;
                }
                BlockState there = level.getBlockState(p);
                if (there.isAir()) {
                    if (nearFlammable(level, p)) {
                        level.setBlockAndUpdate(p, BaseFireBlock.getState(level, p));
                        return;
                    }
                } else if (there.isSolid()) {
                    return;
                }
            }
            if (tries == 0) {
                for (int i = 0; i < 3; i++) {
                    BlockPos q = pos.offset(random.nextInt(3) - 1, 0, random.nextInt(3) - 1);
                    if (level.isLoaded(q) && level.isEmptyBlock(q.above()) && flammable(level, q)) {
                        level.setBlockAndUpdate(q.above(), BaseFireBlock.getState(level, q.above()));
                    }
                }
            }
        }

        private static boolean nearFlammable(Level level, BlockPos pos) {
            for (net.minecraft.core.Direction d : net.minecraft.core.Direction.values()) {
                if (flammable(level, pos.relative(d))) {
                    return true;
                }
            }
            return false;
        }

        private static boolean flammable(Level level, BlockPos pos) {
            return level.isLoaded(pos) && level.getBlockState(pos).ignitedByLava();
        }
    }

    /** LiquidMetalBase.onEntityCollidedWithBlock: slows to 0.4, 4 lava damage and 15 seconds of fire (not items). */
    static final class MoltenBlock extends LiquidBlock {
        MoltenBlock(FlowingFluid fluid, BlockBehaviour.Properties properties) {
            super(fluid, properties);
        }

        @Override
        protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effects, boolean intersects) {
            entity.setDeltaMovement(entity.getDeltaMovement().multiply(0.4, 1.0, 0.4));
            if (!(entity instanceof ItemEntity) && !entity.fireImmune() && level instanceof ServerLevel server) {
                entity.hurtServer(server, level.damageSources().lava(), 4.0F);
                entity.igniteForSeconds(15.0F);
            }
        }
    }
}
