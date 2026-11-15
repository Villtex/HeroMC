package invtweaks.client;

import com.mojang.blaze3d.platform.InputConstants;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** InvTweaksShortcutMapping: keys that have to be held together ("LCONTROL+LSHIFT"). */
public final class InvTweaksShortcutMapping {
    /** InvTweaksShortcutType: what a mapping of the config does. */
    public enum Type {
        MOVE_ALL_ITEMS,
        MOVE_EVERYTHING,
        MOVE_ONE_STACK,
        MOVE_ONE_ITEM,
        MOVE_UP,
        MOVE_DOWN,
        MOVE_TO_SPECIFIC_HOTBAR_SLOT,
        DROP;

        public static Type fromConfigKey(String key) {
            return switch (key) {
                case InvTweaksConfig.PROP_SHORTCUT_ALL_ITEMS -> MOVE_ALL_ITEMS;
                case InvTweaksConfig.PROP_SHORTCUT_EVERYTHING -> MOVE_EVERYTHING;
                case InvTweaksConfig.PROP_SHORTCUT_DOWN -> MOVE_DOWN;
                case InvTweaksConfig.PROP_SHORTCUT_DROP -> DROP;
                case InvTweaksConfig.PROP_SHORTCUT_ONE_ITEM -> MOVE_ONE_ITEM;
                case InvTweaksConfig.PROP_SHORTCUT_UP -> MOVE_UP;
                default -> null;
            };
        }
    }

    private final List<Integer> keysToHold = new ArrayList<>();

    public InvTweaksShortcutMapping(int... keyCodes) {
        for (int code : keyCodes) {
            this.keysToHold.add(code);
        }
    }

    public InvTweaksShortcutMapping(String... keyNames) {
        for (String name : keyNames) {
            this.keysToHold.add(InvTweaksKeys.getKeyIndex(name.trim().replace("KEY_", "").replace("ALT", "MENU")));
        }
    }

    public boolean isTriggered(Map<Integer, Boolean> pressedKeys) {
        for (int code : this.keysToHold) {
            Boolean pressed = pressedKeys.get(code);
            if (pressed == null || !pressed) {
                return false;
            }
            // AltGr is reported as LCONTROL + RMENU on Windows
            if (code == InputConstants.KEY_LCONTROL && InvTweaksKeys.isKeyDown(InputConstants.KEY_RALT)) {
                return false;
            }
        }
        return true;
    }

    public List<Integer> getKeyCodes() {
        return this.keysToHold;
    }
}
