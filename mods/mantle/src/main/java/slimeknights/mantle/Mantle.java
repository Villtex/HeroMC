package slimeknights.mantle;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Mantle, the library of Tinkers' Construct 3 (port of Mantle 1.11.117 for Forge 1.20.1, Teil 27).
 *
 * <p>Teil 27a: only the mod and its assets (taken from the release tag at build time). The library code follows in
 * Teil 27b (core) and 27c (client).
 */
@Mod(Mantle.modId)
public final class Mantle {
    public static final String modId = "mantle";
    public static final Logger logger = LoggerFactory.getLogger("Mantle");

    public Mantle(IEventBus modBus) {
    }
}
