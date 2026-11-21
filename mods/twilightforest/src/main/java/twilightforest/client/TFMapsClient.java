package twilightforest.client;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.gui.map.IMapDecorationRenderer;
import net.neoforged.neoforge.client.gui.map.RegisterMapDecorationRenderersEvent;
import net.neoforged.neoforge.client.renderstate.RegisterRenderStateModifiersEvent;
import net.neoforged.neoforge.common.NeoForge;
import twilightforest.TFMaps;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * TFMagicMapRenderer of 1.5.2 for today's map rendering: the magic map (marked by {@link TFMaps#MAGIC_MAP_MARKER})
 * gets its own 128 x 128 texture from the biome colours instead of the map colours, in the hand and in item frames.
 * The feature icons are drawn from textures/gui/mapicons.png (8 x 8 icons, index = featureID) like the original;
 * the marker is drawn by nobody.
 */
public final class TFMapsClient {
    /** The textures of the magic maps, by the client's map data. */
    private static final Map<MapItemSavedData, MagicTexture> TEXTURES = new WeakHashMap<>();
    private static int nextTexture;

    private TFMapsClient() {
    }

    public static void register(IEventBus modBus) {
        modBus.addListener(RegisterRenderStateModifiersEvent.class, e -> e.registerMapModifier(TFMapsClient::modifyMap));
        modBus.addListener(RegisterMapDecorationRenderersEvent.class, e -> {
            e.register(TFMaps.MAGIC_MAP_MARKER.get(), (decoration, pose, collector, state, atlas, inFrame, light, index) -> true);
            for (int id = 0; id < TFMaps.FEATURE_ICONS.length; id++) {
                if (TFMaps.FEATURE_ICONS[id] != null) {
                    e.register(TFMaps.FEATURE_ICONS[id].get(), featureIcon(id));
                }
            }
        });
        NeoForge.EVENT_BUS.addListener(ClientPlayerNetworkEvent.LoggingOut.class, e -> releaseAll());
    }

    private static boolean isMagicMap(MapItemSavedData data) {
        for (MapDecoration decoration : data.getDecorations()) {
            if (decoration.type().value() == TFMaps.MAGIC_MAP_MARKER.get()) {
                return true;
            }
        }
        return false;
    }

    private static void modifyMap(MapItemSavedData data, net.minecraft.client.renderer.state.MapRenderState state) {
        if (isMagicMap(data)) {
            state.texture = TEXTURES.computeIfAbsent(data, d -> new MagicTexture()).prepare(data);
        }
    }

    private static void releaseAll() {
        for (MagicTexture texture : TEXTURES.values()) {
            Minecraft.getInstance().getTextureManager().release(texture.location);
        }
        TEXTURES.clear();
    }

    /** TFMagicMapRenderer.renderMap, the picture: biome id + 1 per pixel, 0 is the faint checker of an empty map. */
    private static final class MagicTexture {
        private final Identifier location = twilightforest.TwilightForest.id("magic_map/" + nextTexture++);
        private final DynamicTexture texture = new DynamicTexture(this.location::toString, 128, 128, true);
        private byte[] shown;

        MagicTexture() {
            Minecraft.getInstance().getTextureManager().register(this.location, this.texture);
        }

        Identifier prepare(MapItemSavedData data) {
            if (this.shown == null || !java.util.Arrays.equals(this.shown, data.colors)) {
                this.shown = data.colors.clone();
                NativeImage pixels = this.texture.getPixels();
                for (int i = 0; i < 16384; i++) {
                    int colorByte = this.shown[i] & 255;
                    int argb = colorByte == 0 ? ((i + i / 128 & 1) * 8 + 16) << 24 : 0xFF000000 | TFMaps.biomeColor(colorByte - 1);
                    pixels.setPixel(i & 127, i >> 7, argb);
                }
                this.texture.upload();
            }
            return this.location;
        }
    }

    /**
     * The feature icons of TFMagicMapRenderer: translated to the icon's place, rotated, scaled by 4, one 8 x 8 cell
     * of mapicons.png. They lie under the player markers as in 1.5.2 (z -0.02 against -0.04).
     */
    private static IMapDecorationRenderer featureIcon(int icon) {
        float u0 = (icon % 8) / 8.0F;
        float v0 = (icon / 8) / 8.0F;
        float u1 = (icon % 8 + 1) / 8.0F;
        float v1 = (icon / 8 + 1) / 8.0F;
        return (decoration, pose, collector, state, atlas, inFrame, light, index) -> {
            pose.pushPose();
            pose.translate(decoration.x / 2.0F + 64.0F, decoration.y / 2.0F + 64.0F, -0.02F);
            pose.rotateDegrees(Axis.ZP, decoration.rot * 360 / 16.0F);
            pose.scale(4.0F, 4.0F, 3.0F);
            pose.translate(-0.125F, 0.125F, 0.0F);
            float z = 0.002F;
            collector.submitCustomGeometry(pose, RenderTypes.text(TFMaps.MAP_ICONS), (p, buffer) -> {
                buffer.addVertex(p, -1.0F, 1.0F, z).setColor(-1).setUv(u0, v0).setLight(light);
                buffer.addVertex(p, 1.0F, 1.0F, z).setColor(-1).setUv(u1, v0).setLight(light);
                buffer.addVertex(p, 1.0F, -1.0F, z).setColor(-1).setUv(u1, v1).setLight(light);
                buffer.addVertex(p, -1.0F, -1.0F, z).setColor(-1).setUv(u0, v1).setLight(light);
            });
            pose.popPose();
            return true;
        };
    }
}
