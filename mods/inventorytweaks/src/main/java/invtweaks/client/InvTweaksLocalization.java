package invtweaks.client;

import net.minecraft.locale.Language;

/** InvTweaksLocalization: the .properties files of the original are lang/*.json now; unknown keys stay as they are. */
public final class InvTweaksLocalization {
    private InvTweaksLocalization() {
    }

    public static String get(String key) {
        return Language.getInstance().getOrDefault(key);
    }
}
