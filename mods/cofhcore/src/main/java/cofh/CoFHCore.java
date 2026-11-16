package cofh;

import cofh.gen.CoFHWorldGen;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/**
 * What the Hexxit pack used of CoFH Core 1.5.2.5 (the jar holds the mods CoFHCore and CoFHWorld):
 * <ul>
 * <li>CoFH World: the vanilla ores replaced with the pack's ore settings and flat bedrock ({@link CoFHWorldGen}).</li>
 * <li>CoFH Core: heads as rare mob drops ({@link HeadDrops}) and {@code /cofh tps} ({@link CoFHCommand}).</li>
 * </ul>
 * Not ported: capes and skins ({@code /cofh cape|skin}, loaded from web addresses that no longer exist), the
 * update check, the fluid and GUI-tab APIs (used only by mods that are not in the pack) and ForgeIRC.
 */
@Mod(CoFHCore.MOD_ID)
public final class CoFHCore {
    public static final String MOD_ID = "cofhcore";

    public CoFHCore(IEventBus modBus) {
        CoFHWorldGen.register();
        NeoForge.EVENT_BUS.register(HeadDrops.class);
        NeoForge.EVENT_BUS.addListener(RegisterCommandsEvent.class, e -> CoFHCommand.register(e.getDispatcher()));
    }
}
