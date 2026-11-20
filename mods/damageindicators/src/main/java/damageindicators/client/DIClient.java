package damageindicators.client;

import com.mojang.blaze3d.platform.InputConstants;
import damageindicators.DamageIndicatorsMod;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.common.NeoForge;

/** ClientProxy / DITicker: key, client tick and the two HUD parts (portrait and pop-off numbers). */
public final class DIClient {
    /** KeyBindingReposition opened the settings with '.' (key 52 of LWJGL 2); here it switches the pop-offs on and off. */
    public static final KeyMapping TOGGLE_POPOFFS = new KeyMapping("key.damageindicatorsmod.popoffs",
            InputConstants.KEY_PERIOD, KeyMapping.Category.MISC);

    private DIClient() {
    }

    public static void register(IEventBus modBus) {
        modBus.addListener(RegisterKeyMappingsEvent.class, e -> e.register(TOGGLE_POPOFFS));
        modBus.addListener(RegisterGuiLayersEvent.class, e -> {
            e.registerAbove(VanillaGuiLayers.HOTBAR, Identifier.fromNamespaceAndPath(DamageIndicatorsMod.MOD_ID, "portrait"), Portrait::render);
            e.registerAbove(VanillaGuiLayers.HOTBAR, Identifier.fromNamespaceAndPath(DamageIndicatorsMod.MOD_ID, "popoffs"), Popoffs::render);
        });
        NeoForge.EVENT_BUS.addListener(ClientTickEvent.Post.class, DIClient::tick);
    }

    private static void tick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) {
            Popoffs.clear();
            Portrait.clear();
            return;
        }
        while (TOGGLE_POPOFFS.consumeClick()) {
            boolean on = !DamageIndicatorsMod.POPOFFS.get();
            DamageIndicatorsMod.POPOFFS.set(on);
            mc.player.sendOverlayMessage(net.minecraft.network.chat.Component.literal("Damage Indicators: " + (on ? "on" : "off")));
        }
        Popoffs.tick(mc);
    }
}
