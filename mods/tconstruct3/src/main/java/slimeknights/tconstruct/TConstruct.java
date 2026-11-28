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

    /** Returns the given resource prefixed with the Tinkers namespace */
    public static String resourceString(String res) {
        return MOD_ID + ':' + res;
    }

    /** Prefixes the given unlocalized name with the Tinkers prefix */
    public static String prefix(String name) {
        return MOD_ID + "." + name.toLowerCase(Locale.US);
    }

    /** Makes a Tinker's description ID */
    public static String makeDescriptionId(String type, String name) {
        return type + "." + MOD_ID + "." + name;
    }

    /** Makes a translation key for the given name, such as {@code gui.tconstruct3.name} */
    public static String makeTranslationKey(String base, String name) {
        return net.minecraft.util.Util.makeDescriptionId(base, getResource(name));
    }

    /** Makes a translation text component for the given name */
    public static MutableComponent makeTranslation(String base, String name) {
        return Component.translatable(makeTranslationKey(base, name));
    }

    /** Makes a translation text component; arguments other than text, numbers and booleans become strings (26.3 rejects them) */
    public static MutableComponent makeTranslation(String base, String name, Object... arguments) {
        Object[] args = new Object[arguments.length];
        for (int i = 0; i < arguments.length; i++) {
            args[i] = TranslationHelper.arg(arguments[i]);
        }
        return Component.translatable(makeTranslationKey(base, name), args);
    }

    /** Called in the constructor of internal classes that addons should not extend */
    public static void sealTinkersClass(Object self, String base, String solution) {
        String name = self.getClass().getName();
        if (!name.startsWith("slimeknights.tconstruct.")) {
            throw new IllegalStateException(base + " being extended from invalid package " + name + ". " + solution);
        }
    }
}
