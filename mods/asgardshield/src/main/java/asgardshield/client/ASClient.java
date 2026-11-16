package asgardshield.client;

import asgardshield.AsgardShield;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

/** Client side: the HUD (Guard Gauge, Vanguard Count) and the shield colour tint of the item models. */
public final class ASClient {
    private ASClient() {
    }

    public static void register(IEventBus modBus) {
        modBus.addListener(RegisterGuiLayersEvent.class, event -> event.registerAbove(VanillaGuiLayers.HOTBAR,
                Identifier.fromNamespaceAndPath(AsgardShield.MOD_ID, "guard_gauge"), GuardHud::render));
        modBus.addListener(RegisterColorHandlersEvent.ItemTintSources.class, event -> event.register(
                Identifier.fromNamespaceAndPath(AsgardShield.MOD_ID, "shield_color"), ShieldColorTint.MAP_CODEC));
    }
}
