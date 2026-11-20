package twilightforest;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.MapCodec;
import hexxit.worldgen152.OldBlockStates;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SpellParticleOption;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;
import twilightforest.block.HedgeBlock;
import twilightforest.block.TFPortalBlock;
import twilightforest.gen.TFTerrain;
import twilightforest.gen.TFWorld;
import twilightforest.world.TFBiomeSource;
import twilightforest.world.TFChunkGenerator;

import java.util.Set;

/**
 * The Twilight Forest 1.18.1 (FML id "TwilightForest") for NeoForge. Teil 24a: the dimension (id 7 of the pack's
 * TwilightForest.cfg) with the terrain, biomes and feature grid of 1.5.2, and the portal. Portals may be made by
 * everyone, only in the overworld and the Twilight Forest.
 */
@Mod(TwilightForest.MOD_ID)
public final class TwilightForest {
    public static final String MOD_ID = "twilightforest";
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final ResourceKey<Level> DIMENSION = ResourceKey.create(Registries.DIMENSION, id("twilight_forest"));

    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MOD_ID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MOD_ID);
    private static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MOD_ID);
    private static final DeferredRegister<PoiType> POI_TYPES = DeferredRegister.create(Registries.POINT_OF_INTEREST_TYPE, MOD_ID);
    private static final DeferredRegister<MapCodec<? extends ChunkGenerator>> GENERATORS = DeferredRegister.create(Registries.CHUNK_GENERATOR, MOD_ID);
    private static final DeferredRegister<MapCodec<? extends BiomeSource>> BIOME_SOURCES = DeferredRegister.create(Registries.BIOME_SOURCE, MOD_ID);

    /** BlockTFPortal: Material.portal, unbreakable, light 0.75, glass sound, drops nothing. */
    public static final DeferredBlock<TFPortalBlock> PORTAL = BLOCKS.registerBlock("twilight_portal", TFPortalBlock::new,
            p -> p.mapColor(MapColor.COLOR_PURPLE).strength(-1.0F, 3600000.0F).noCollision().noOcclusion().lightLevel(s -> 11)
                    .sound(SoundType.GLASS).noLootTable().pushReaction(PushReaction.IMMOVEABLE));
    /** BlockTFHedge metadata 0. */
    public static final DeferredBlock<HedgeBlock> HEDGE = BLOCKS.registerBlock("hedge", HedgeBlock::new,
            p -> p.mapColor(MapColor.PLANT).strength(2.0F, 10.0F).sound(SoundType.GRASS).noLootTable());
    /** BlockTFHedge metadata 1: the leaves of the dark forest canopy (flammable, drops a darkwood sapling with Teil 24b). */
    public static final DeferredBlock<Block> DARKWOOD_LEAVES = BLOCKS.registerBlock("darkwood_leaves", Block::new,
            p -> p.mapColor(MapColor.PLANT).strength(2.0F, 10.0F).sound(SoundType.GRASS).ignitedByLava().noLootTable());

    public static final DeferredItem<?> HEDGE_ITEM = ITEMS.registerSimpleBlockItem(HEDGE);
    public static final DeferredItem<?> DARKWOOD_LEAVES_ITEM = ITEMS.registerSimpleBlockItem(DARKWOOD_LEAVES);

    public static final DeferredHolder<PoiType, PoiType> PORTAL_POI = POI_TYPES.register("twilight_portal",
            () -> new PoiType(Set.copyOf(PORTAL.get().getStateDefinition().getPossibleStates()), 0, 1));

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TAB = TABS.register("twilightforest", () -> CreativeModeTab.builder()
            .title(Component.literal("Twilight Forest"))
            .icon(() -> new ItemStack(HEDGE_ITEM.get()))
            .displayItems((params, output) -> {
                output.accept(HEDGE_ITEM.get());
                output.accept(DARKWOOD_LEAVES_ITEM.get());
            }).build());

    public TwilightForest(IEventBus modBus) {
        TFWorld.register();
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        TABS.register(modBus);
        POI_TYPES.register(modBus);
        GENERATORS.register("twilight_forest", () -> TFChunkGenerator.CODEC);
        BIOME_SOURCES.register("twilight_forest", () -> TFBiomeSource.CODEC);
        GENERATORS.register(modBus);
        BIOME_SOURCES.register(modBus);
        registerOldBlocks();
        NeoForge.EVENT_BUS.addListener(TwilightForest::onLevelLoad);
        NeoForge.EVENT_BUS.addListener(TwilightForest::onPlayerTick);
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    /** The block ids of the pack's TwilightForest.cfg, for worldgen152. */
    private static void registerOldBlocks() {
        OldBlockStates.registerMeta(TFTerrain.HEDGE, meta -> (meta == 1 ? DARKWOOD_LEAVES.get() : HEDGE.get()).defaultBlockState());
        OldBlockStates.register(TFWorld.PORTAL, () -> PORTAL.get().defaultBlockState());
    }

    /** The chunk generator learns the world seed when its level is created, before any chunk is made. */
    private static void onLevelLoad(LevelEvent.Load event) {
        if (event.getLevel() instanceof ServerLevel level && level.getChunkSource().getGenerator() instanceof TFChunkGenerator generator) {
            generator.bind(level.getSeed());
            LOGGER.info("Twilight Forest generation of 1.5.2 active, seed {}", level.getSeed());
        }
    }

    /**
     * TFTickHandler, every 20 ticks per player: a diamond lying in water within 32 blocks of a player in the overworld
     * or the Twilight Forest sparkles, and makes a portal if its pool is right.
     */
    private static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.tickCount % 20 != 0) {
            return;
        }
        ServerLevel level = player.level();
        if (level.dimension() != Level.OVERWORLD && level.dimension() != DIMENSION) {
            return;
        }
        double range = 32.0;
        for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, player.getBoundingBox().inflate(range))) {
            if (item.getItem().is(Items.DIAMOND) && inWater(level, item.getBoundingBox())) {
                for (int k = 0; k < 2; k++) {
                    double d = level.getRandom().nextGaussian() * 0.02;
                    level.sendParticles(SpellParticleOption.create(ParticleTypes.EFFECT, -1, 1.0F), item.getX(), item.getY() + 0.2, item.getZ(),
                            0, 0.0, d, 0.0, 1.0);
                }
                PORTAL.get().tryToCreatePortal(level, Mth.floor(item.getX()), Mth.floor(item.getY()), Mth.floor(item.getZ()));
            }
        }
    }

    /** World.isMaterialInBB(box, Material.water). */
    private static boolean inWater(ServerLevel level, AABB box) {
        int x0 = Mth.floor(box.minX);
        int x1 = Mth.floor(box.maxX + 1.0);
        int y0 = Mth.floor(box.minY);
        int y1 = Mth.floor(box.maxY + 1.0);
        int z0 = Mth.floor(box.minZ);
        int z1 = Mth.floor(box.maxZ + 1.0);
        for (int x = x0; x < x1; x++) {
            for (int y = y0; y < y1; y++) {
                for (int z = z0; z < z1; z++) {
                    if (level.getBlockState(new BlockPos(x, y, z)).is(Blocks.WATER)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
