package slimeknights.tconstruct;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import slimeknights.mantle.util.TranslationHelper;
import java.util.Locale;
import java.util.Random;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tinkers' Construct 3.12.1 for the world type Hexxit Integrated (Teil 27). Registered under the mod ID
 * {@code tconstruct3}: Hexxit Legacy keeps Tinkers' Construct 1.4.3 under {@code tconstruct}, both are installed,
 * the world type decides which one is active (data with the load condition {@code worldgen152:world_type},
 * code through {@code hexxit.worldgen152.HexxitWorldType}).
 *
 * <p>Teil 27a: only the mod, its assets (from the release tag, namespace renamed at build time) and the converted
 * data (tools/convert_data.py, switched on per folder in data-enabled.txt once the code that reads it exists).
 */
@Mod(TConstruct.MOD_ID)
public final class TConstruct {
    public static final String MOD_ID = "tconstruct3";
    public static final Logger LOG = LoggerFactory.getLogger("TConstruct3");
    public static final Random RANDOM = new Random();

    public TConstruct(IEventBus modBus) {
    }

    /** An identifier in the namespace of the mod. */
    public static Identifier getResource(String name) {
        return Identifier.fromNamespaceAndPath(MOD_ID, name);
    }
}
