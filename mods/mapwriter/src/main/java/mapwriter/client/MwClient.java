package mapwriter.client;

import com.mojang.blaze3d.platform.InputConstants;
import mapwriter.MapWriterMod;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.common.NeoForge;

/** MwForge / MwKeyHandler / MwTickHandler: keys, login and logout, the tick and the overlay layer. */
public final class MwClient {
    // LWJGL 2 keys of MwKeyHandler: 50 = M, 49 = N, 51 = comma, 52 = period (left free here, Damage Indicators uses it), 201/209 = Page Up/Down
    static final KeyMapping GUI = key("gui", InputConstants.KEY_M);
    static final KeyMapping MODE = key("mode", InputConstants.KEY_N);
    static final KeyMapping GROUP = key("group", InputConstants.KEY_COMMA);
    static final KeyMapping TELEPORT = key("teleport", InputConstants.UNKNOWN.getValue());
    static final KeyMapping ZOOM_IN = key("zoom_in", InputConstants.KEY_PAGEUP);
    static final KeyMapping ZOOM_OUT = key("zoom_out", InputConstants.KEY_PAGEDOWN);

    private MwClient() {
    }

    private static KeyMapping key(String name, int code) {
        return new KeyMapping("key.mapwriter." + name, code, KeyMapping.Category.MISC);
    }

    public static void register(IEventBus modBus) {
        modBus.addListener(RegisterKeyMappingsEvent.class, e -> {
            for (KeyMapping k : new KeyMapping[]{GUI, MODE, GROUP, TELEPORT, ZOOM_IN, ZOOM_OUT}) {
                e.register(k);
            }
        });
        modBus.addListener(RegisterGuiLayersEvent.class, e -> e.registerAbove(VanillaGuiLayers.HOTBAR,
                Identifier.fromNamespaceAndPath(MapWriterMod.MOD_ID, "map"), (g, delta) -> {
                    Mw mw = Mw.current;
                    if (mw != null && Minecraft.getInstance().gui.screen() == null) {
                        mw.renderOverlay(g);
                    }
                }));
        NeoForge.EVENT_BUS.addListener(ClientPlayerNetworkEvent.LoggingIn.class, e -> Mw.open(Minecraft.getInstance()));
        NeoForge.EVENT_BUS.addListener(ClientPlayerNetworkEvent.LoggingOut.class, e -> Mw.close());
        NeoForge.EVENT_BUS.addListener(ClientTickEvent.Post.class, e -> tick());
    }

    private static void tick() {
        Mw mw = Mw.current;
        Minecraft mc = Minecraft.getInstance();
        if (mw == null || mc.player == null || mc.level == null) {
            return;
        }
        mw.tick();
        if (mc.gui.screen() != null) {
            return;
        }
        while (MODE.consumeClick()) {
            mw.nextOverlayMode(1);
        }
        while (GUI.consumeClick()) {
            mc.gui.setScreen(new MwGui(mw));
        }
        while (GROUP.consumeClick()) {
            mw.markers.nextGroup(1);
            mw.message("group " + mw.markers.visibleGroup() + " selected");
        }
        while (TELEPORT.consumeClick()) {
            Marker m = mw.markers.nearestInDirection(mw.playerXInt, mw.playerZInt, mw.playerHeading);
            if (m != null) {
                mw.teleportToOverworldPos(m.x, m.y, m.z);
            }
        }
        while (ZOOM_IN.consumeClick()) {
            mw.overlayView.adjustZoomLevel(-1);
        }
        while (ZOOM_OUT.consumeClick()) {
            mw.overlayView.adjustZoomLevel(1);
        }
    }
}
