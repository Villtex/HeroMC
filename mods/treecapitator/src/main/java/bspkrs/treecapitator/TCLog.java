package bspkrs.treecapitator;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

/** TCLog: debug lines only with allowDebugLogging. */
final class TCLog {
    private static final Logger LOGGER = LogUtils.getLogger();

    private TCLog() {
    }

    static void debug(String format, Object... args) {
        if (TCSettings.SPEC.isLoaded() && TCSettings.ALLOW_DEBUG_LOGGING.get()) {
            LOGGER.info("[TreeCapitator] " + String.format(format, args));
        }
    }
}
