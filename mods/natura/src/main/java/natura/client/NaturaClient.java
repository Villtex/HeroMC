package natura.client;

import natura.Natura;
import natura.NaturaBlocks;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.color.block.BlockTintSources;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.resources.LegacyStuffWrapper;
import net.minecraft.locale.Language;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import java.util.List;

/** Client side: leaf and grass colours, and the tooltips of the original (addInformation). */
public final class NaturaClient {
    /** OverworldLeaves.colorMultiplier: maple leaves are always orange. */
    public static final int MAPLE_COLOR = 0xFFCC5818;

    private NaturaClient() {
    }

    /** OverworldLeaves: amaranth leaves are the biome foliage colour plus 0x222222. */
    private static final BlockTintSource AMARANTH = new BlockTintSource() {
        @Override
        public int color(BlockState state) {
            return 0xFF000000 | (0x48B518 + 0x222222);
        }

        @Override
        public int colorInWorld(BlockState state, BlockAndTintGetter level, BlockPos pos) {
            return 0xFF000000 | ((BiomeColors.getAverageFoliageColor(level, pos) & 0xFFFFFF) + 0x222222);
        }
    };

    /** GrassColorizerAlternate: bluegrass and autumnal grass read their own colour maps (textures/colormap). */
    private static final class ColormapTint implements BlockTintSource {
        private final Identifier texture;
        private final int fallback;
        private int[] pixels;

        ColormapTint(String name, int fallback) {
            this.texture = Identifier.fromNamespaceAndPath(Natura.MOD_ID, "textures/colormap/" + name + ".png");
            this.fallback = fallback;
        }

        private int lookup(double temperature, double downfall) {
            if (this.pixels == null) {
                try {
                    this.pixels = LegacyStuffWrapper.getPixels(Minecraft.getInstance().getResourceManager(), this.texture);
                } catch (Exception e) {
                    this.pixels = new int[0];
                }
            }
            if (this.pixels.length < 65536) {
                return this.fallback;
            }
            downfall *= temperature;
            int i = (int) ((1.0 - temperature) * 255.0);
            int j = (int) ((1.0 - downfall) * 255.0);
            return this.pixels[j << 8 | i];
        }

        @Override
        public int color(BlockState state) {
            return 0xFF000000 | this.fallback;
        }

        @Override
        public int colorInWorld(BlockState state, BlockAndTintGetter level, BlockPos pos) {
            return 0xFF000000 | level.getBlockTint(pos, (biome, x, z) -> {
                Biome.ClimateSettings climate = biome.getModifiedClimateSettings();
                return this.lookup(Mth.clamp(climate.temperature(), 0.0F, 1.0F), Mth.clamp(climate.downfall(), 0.0F, 1.0F));
            });
        }
    }

    /** The inventory colours (GrassBlock.getRenderColor): bluegrass at (0.5, 0.5), autumnal grass at (1, 1). */
    public static final int BLUEGRASS_COLOR = 0x00F3F6;
    public static final int AUTUMNAL_COLOR = 0xC92B00;

    private static void colors(RegisterColorHandlersEvent.BlockTintSources event) {
        event.register(List.of(BlockTintSources.foliage()), NaturaBlocks.REDWOOD_LEAVES.get(), NaturaBlocks.EUCALYPTUS_LEAVES.get(),
                NaturaBlocks.HOPSEED_LEAVES.get());
        event.register(List.of(BlockTintSources.constant(MAPLE_COLOR)), NaturaBlocks.MAPLE_LEAVES.get());
        event.register(List.of(AMARANTH), NaturaBlocks.AMARANTH_LEAVES.get());
        event.register(List.of(BlockTintSources.grass()), NaturaBlocks.TOPIARY_GRASS.get(), NaturaBlocks.TOPIARY_GRASS_SLAB.get());
        BlockTintSource blue = new ColormapTint("bluegrass", BLUEGRASS_COLOR);
        BlockTintSource orange = new ColormapTint("autumnal_grass", AUTUMNAL_COLOR);
        event.register(List.of(blue), NaturaBlocks.BLUEGRASS.get(), NaturaBlocks.BLUEGRASS_SLAB.get());
        event.register(List.of(orange), NaturaBlocks.AUTUMNAL_GRASS.get(), NaturaBlocks.AUTUMNAL_GRASS_SLAB.get());
    }

    /** Lines {@code tooltip.natura.<item>.0}, {@code .1} ... of the language file. */
    private static void tooltips(ItemTooltipEvent event) {
        Identifier id = BuiltInRegistries.ITEM.getKey(event.getItemStack().getItem());
        if (!Natura.MOD_ID.equals(id.getNamespace())) {
            return;
        }
        String prefix = "tooltip." + Natura.MOD_ID + "." + id.getPath() + ".";
        for (int i = 0; Language.getInstance().has(prefix + i); i++) {
            event.getToolTip().add(Component.translatable(prefix + i).withStyle(ChatFormatting.GRAY));
        }
    }

    public static void register(IEventBus modBus) {
        modBus.addListener(NaturaClient::colors);
        NeoForge.EVENT_BUS.addListener(NaturaClient::tooltips);
        natura.client.entity.NaturaEntityRenderers.register(modBus);
    }
}
