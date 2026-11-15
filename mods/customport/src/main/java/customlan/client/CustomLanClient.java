package customlan.client;

import customlan.CLOptions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.WorldOptionsScreen;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.NeoForge;

/**
 * The client half. In 1.5.2 the mod replaced GuiShareToLan (port and IP fields) and ScreenChatOptions (button
 * "Custom LAN Options"). In 26.3 a world is opened to LAN in the world options screen, which already has a port
 * field: it starts with the port from optionsCLP.txt (25565) instead of a random one, the port used is remembered,
 * and the screen gets the "Custom LAN Options" button.
 */
public final class CustomLanClient {
    private CustomLanClient() {
    }

    private static boolean wasPublished;

    public static void init() {
        NeoForge.EVENT_BUS.addListener(ScreenEvent.Init.Post.class, CustomLanClient::onScreenInit);
        NeoForge.EVENT_BUS.addListener(ClientTickEvent.Post.class, e -> onTick());
    }

    private static void onScreenInit(ScreenEvent.Init.Post event) {
        if (!(event.getScreen() instanceof WorldOptionsScreen screen)) {
            return;
        }
        IntegratedServer server = Minecraft.getInstance().getSingleplayerServer();
        if (server == null) {
            return;
        }
        if (!server.isPublished()) {
            CLOptions.checkOptions();
            for (GuiEventListener child : screen.children()) {
                if (child instanceof EditBox port) {
                    port.setValue(String.valueOf(CLOptions.serverPort));
                    break;
                }
            }
        }
        event.addListener(Button.builder(Component.literal("Custom LAN Options"),
                b -> Minecraft.getInstance().gui.setScreen(new GuiCustomLan(screen))).bounds(screen.width - 155, 5, 150, 20).build());
    }

    /** onGuiClosed of GuiShareToLan saved the port; here the port the world was published on. */
    private static void onTick() {
        IntegratedServer server = Minecraft.getInstance().getSingleplayerServer();
        boolean published = server != null && server.isPublished();
        if (published && !wasPublished && server.getPort() > 0) {
            CLOptions.checkOptions();
            CLOptions.serverPort = server.getPort();
            CLOptions.saveOptions();
        }
        wasPublished = published;
    }
}
