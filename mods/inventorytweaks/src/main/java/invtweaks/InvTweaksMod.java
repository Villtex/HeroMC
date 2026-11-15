package invtweaks;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;

/**
 * Inventory Tweaks 1.54b (forge/InvTweaksMod + ClientProxy). The mod only does something on the client: every
 * inventory change is a normal slot click, so it works on any server. On a dedicated server nothing is loaded
 * (the client classes in {@code invtweaks.client} are only touched when the dist is the client).
 *
 * <p>Other mods can declare their own chest-like screens with an IMC message to this mod, method {@code "chest"},
 * payload {@code "fully.qualified.ScreenClass"} or {@code "fully.qualified.ScreenClass;rowSize"} (row size 9 if
 * omitted). That replaces the {@code @ContainerGUI} annotation and the list of known chest GUIs of the original.
 */
@Mod(InvTweaksMod.MOD_ID)
public final class InvTweaksMod {
    public static final String MOD_ID = "inventorytweaks";

    public InvTweaksMod(IEventBus modBus) {
        if (FMLEnvironment.getDist() == Dist.CLIENT) {
            invtweaks.client.InvTweaksClient.register(modBus);
        }
    }
}
