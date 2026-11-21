package twilightforest.client;

import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.color.block.BlockTintSources;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import twilightforest.TFBlocks;

import java.util.List;

/** Client side of Teil 24b: block colours (colorMultiplier / getRenderColor), the critter renderers, the firefly glow. */
public final class TFClient {
    private TFClient() {
    }

    /** A tint with a fixed inventory colour and a colour in the world. */
    private interface WorldTint {
        int at(BlockAndTintGetter level, BlockPos pos);
    }

    private static BlockTintSource tint(int item, WorldTint world) {
        return new BlockTintSource() {
            @Override
            public int color(BlockState state) {
                return 0xFF000000 | item;
            }

            @Override
            public int colorInWorld(BlockState state, BlockAndTintGetter level, BlockPos pos) {
                return 0xFF000000 | world.at(level, pos);
            }
        };
    }

    /** BlockTFLeaves: the foliage colour of the 3 x 3 biomes, mixed for canopy and mangrove. */
    private static int foliage(BlockAndTintGetter level, BlockPos pos) {
        return BiomeColors.getAverageFoliageColor(level, pos) & 0xFFFFFF;
    }

    /** Rainbow oak: red, green and blue run in triangle waves through x, y and z. */
    private static int rainbow(BlockPos pos) {
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        int red = x * 32 + y * 16;
        if ((red & 256) != 0) {
            red = 255 - (red & 0xFF);
        }
        red &= 255;
        int blue = y * 32 + z * 16;
        if ((blue & 256) != 0) {
            blue = 255 - (blue & 0xFF);
        }
        blue ^= 255;
        int green = x * 16 + z * 32;
        if ((green & 256) != 0) {
            green = 255 - (green & 0xFF);
        }
        green &= 255;
        return red << 16 | blue << 8 | green;
    }

    /** BlockTFMagicLeaves.colorMultiplier: a fade between a spring and a fall colour along the position. */
    private static int magic(BlockPos pos, int fx, int fy, int fz, int r0, int g0, int b0, int r1, int g1, int b1) {
        int fade = pos.getX() * fx + pos.getY() * fy + pos.getZ() * fz;
        if ((fade & 256) != 0) {
            fade = 255 - (fade & 0xFF);
        }
        fade &= 255;
        float spring = (255 - fade) / 255.0F;
        float fall = fade / 255.0F;
        int red = (int) (spring * r0 + fall * r1);
        int green = (int) (spring * g0 + fall * g1);
        int blue = (int) (spring * b0 + fall * b1);
        return (red & 0xFF) << 16 | (green & 0xFF) << 8 | blue & 0xFF;
    }

    /** BlockTFTowerWood.colorMultiplier: a grey between 128 and 255 along the position. */
    private static int towerWood(BlockPos pos) {
        int value = pos.getX() * 31 + pos.getY() * 15 + pos.getZ() * 33;
        if ((value & 256) != 0) {
            value = 255 - (value & 0xFF);
        }
        value &= 255;
        value >>= 1;
        value |= 128;
        return value << 16 | value << 8 | value;
    }

    /** ColorizerFoliage.getFoliageColorBasic, the inventory colour of the oak leaves and the grass-coloured plants. */
    public static final int FOLIAGE_BASIC = 0x48B518;

    private static void colors(RegisterColorHandlersEvent.BlockTintSources event) {
        event.register(List.of(tint(FOLIAGE_BASIC, TFClient::foliage)), TFBlocks.TWILIGHT_OAK_LEAVES.get());
        event.register(List.of(tint(6330464, (l, p) -> ((foliage(l, p) & 0xFEFEFE) + 0x469A66) / 2)), TFBlocks.CANOPY_LEAVES.get());
        event.register(List.of(tint(8431445, (l, p) -> ((foliage(l, p) & 0xFEFEFE) + 0xC0E694) / 2)), TFBlocks.MANGROVE_LEAVES.get());
        event.register(List.of(tint(FOLIAGE_BASIC, (l, p) -> rainbow(p))), TFBlocks.RAINBOW_OAK_LEAVES.get());
        event.register(List.of(tint(6986775, (l, p) -> magic(p, 16, 16, 16, 106, 156, 23, 251, 108, 27))), TFBlocks.TIME_LEAVES.get());
        event.register(List.of(tint(7130346, (l, p) -> magic(p, 27, 63, 39, 108, 204, 234, 96, 107, 121))), TFBlocks.TRANSFORMATION_LEAVES.get());
        event.register(List.of(tint(16576836, (l, p) -> magic(p, 31, 33, 32, 252, 241, 68, 237, 172, 9))), TFBlocks.MINING_LEAVES.get());
        event.register(List.of(tint(3558403, (l, p) -> magic(p, 63, 63, 63, 54, 76, 3, 168, 199, 43))), TFBlocks.SORTING_LEAVES.get());
        event.register(List.of(tint(0xFFFFFF, (l, p) -> towerWood(p))), TFBlocks.TOWER_WOOD.get(), TFBlocks.TOWER_WOOD_CRACKED.get(),
                TFBlocks.TOWER_WOOD_MOSSY.get(), TFBlocks.TOWER_WOOD_INFESTED.get());
        // BlockTFFireJet: the grass colour of the 3 x 3 biomes on the plain smoker and fire jet
        event.register(List.of(tint(0xFFFFFF, (l, p) -> BiomeColors.getAverageGrassColor(l, p) & 0xFFFFFF)), TFBlocks.SMOKER.get(), TFBlocks.FIRE_JET.get());
        // BlockTFPlant: mayapple, clover and fiddlehead take the grass colour
        event.register(List.of(BlockTintSources.grass()), TFBlocks.MAYAPPLE.get(), TFBlocks.CLOVER_PATCH.get(), TFBlocks.FIDDLEHEAD.get());
    }

    public static void register(IEventBus modBus) {
        twilightforest.client.entity.TFEntityRenderers.register(modBus);
        modBus.addListener(TFClient::colors);
        modBus.addListener(EntityRenderersEvent.RegisterLayerDefinitions.class, e -> {
            e.registerLayerDefinition(CritterRenderer.FIREFLY, CritterRenderer::firefly);
            e.registerLayerDefinition(CritterRenderer.CICADA, CritterRenderer::cicada);
            e.registerLayerDefinition(CritterRenderer.MOONWORM, CritterRenderer::moonworm);
        });
        modBus.addListener(EntityRenderersEvent.RegisterRenderers.class,
                e -> e.registerBlockEntityRenderer(TFBlocks.CRITTER_ENTITY.get(), CritterRenderer::new));
        modBus.addListener(RegisterParticleProvidersEvent.class, e -> e.registerSpriteSet(TFBlocks.FIREFLY_PARTICLE.get(), FireflyParticle.Provider::new));
    }
}
