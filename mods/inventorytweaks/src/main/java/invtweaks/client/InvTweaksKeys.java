package invtweaks.client;

import com.mojang.blaze3d.platform.InputConstants;
import java.util.HashMap;
import java.util.Map;

/**
 * The key names of the original config (LWJGL 2 names such as {@code R}, {@code LCONTROL}, {@code LMENU},
 * {@code NUMPAD1}) mapped to the key codes of 26.3 ({@link InputConstants}, SDL scancodes).
 */
public final class InvTweaksKeys {
    public static final int NONE = -1;
    private static final Map<String, Integer> BY_NAME = new HashMap<>();
    private static final Map<Integer, String> BY_CODE = new HashMap<>();

    static {
        int[] letters = {InputConstants.KEY_A, InputConstants.KEY_B, InputConstants.KEY_C, InputConstants.KEY_D,
                InputConstants.KEY_E, InputConstants.KEY_F, InputConstants.KEY_G, InputConstants.KEY_H, InputConstants.KEY_I,
                InputConstants.KEY_J, InputConstants.KEY_K, InputConstants.KEY_L, InputConstants.KEY_M, InputConstants.KEY_N,
                InputConstants.KEY_O, InputConstants.KEY_P, InputConstants.KEY_Q, InputConstants.KEY_R, InputConstants.KEY_S,
                InputConstants.KEY_T, InputConstants.KEY_U, InputConstants.KEY_V, InputConstants.KEY_W, InputConstants.KEY_X,
                InputConstants.KEY_Y, InputConstants.KEY_Z};
        for (int i = 0; i < letters.length; i++) {
            put(String.valueOf((char) ('A' + i)), letters[i]);
        }
        int[] digits = {InputConstants.KEY_0, InputConstants.KEY_1, InputConstants.KEY_2, InputConstants.KEY_3,
                InputConstants.KEY_4, InputConstants.KEY_5, InputConstants.KEY_6, InputConstants.KEY_7, InputConstants.KEY_8,
                InputConstants.KEY_9};
        int[] numpad = {InputConstants.KEY_NUMPAD0, InputConstants.KEY_NUMPAD1, InputConstants.KEY_NUMPAD2,
                InputConstants.KEY_NUMPAD3, InputConstants.KEY_NUMPAD4, InputConstants.KEY_NUMPAD5, InputConstants.KEY_NUMPAD6,
                InputConstants.KEY_NUMPAD7, InputConstants.KEY_NUMPAD8, InputConstants.KEY_NUMPAD9};
        for (int i = 0; i < 10; i++) {
            put(String.valueOf(i), digits[i]);
            put("NUMPAD" + i, numpad[i]);
        }
        int[] fkeys = {InputConstants.KEY_F1, InputConstants.KEY_F2, InputConstants.KEY_F3, InputConstants.KEY_F4,
                InputConstants.KEY_F5, InputConstants.KEY_F6, InputConstants.KEY_F7, InputConstants.KEY_F8, InputConstants.KEY_F9,
                InputConstants.KEY_F10, InputConstants.KEY_F11, InputConstants.KEY_F12};
        for (int i = 0; i < fkeys.length; i++) {
            put("F" + (i + 1), fkeys[i]);
        }
        put("LCONTROL", InputConstants.KEY_LCONTROL);
        put("RCONTROL", InputConstants.KEY_RCONTROL);
        put("LSHIFT", InputConstants.KEY_LSHIFT);
        put("RSHIFT", InputConstants.KEY_RSHIFT);
        put("LMENU", InputConstants.KEY_LALT);
        put("RMENU", InputConstants.KEY_RALT);
        put("LMETA", InputConstants.KEY_LGUI);
        put("RMETA", InputConstants.KEY_RGUI);
        put("SPACE", InputConstants.KEY_SPACE);
        put("UP", InputConstants.KEY_UP);
        put("DOWN", InputConstants.KEY_DOWN);
        put("LEFT", InputConstants.KEY_LEFT);
        put("RIGHT", InputConstants.KEY_RIGHT);
        put("TAB", InputConstants.KEY_TAB);
        put("RETURN", InputConstants.KEY_RETURN);
        put("BACK", InputConstants.KEY_BACKSPACE);
        put("ESCAPE", InputConstants.KEY_ESCAPE);
        put("INSERT", InputConstants.KEY_INSERT);
        put("DELETE", InputConstants.KEY_DELETE);
        put("HOME", InputConstants.KEY_HOME);
        put("END", InputConstants.KEY_END);
        put("PRIOR", InputConstants.KEY_PAGEUP);
        put("NEXT", InputConstants.KEY_PAGEDOWN);
        put("CAPITAL", InputConstants.KEY_CAPSLOCK);
        put("NUMLOCK", InputConstants.KEY_NUMLOCK);
        put("SCROLL", InputConstants.KEY_SCROLLLOCK);
        put("PAUSE", InputConstants.KEY_PAUSE);
        put("SYSRQ", InputConstants.KEY_PRINTSCREEN);
        put("GRAVE", InputConstants.KEY_GRAVE);
        put("MINUS", InputConstants.KEY_MINUS);
        put("EQUALS", InputConstants.KEY_EQUALS);
        put("LBRACKET", InputConstants.KEY_LBRACKET);
        put("RBRACKET", InputConstants.KEY_RBRACKET);
        put("SEMICOLON", InputConstants.KEY_SEMICOLON);
        put("APOSTROPHE", InputConstants.KEY_APOSTROPHE);
        put("BACKSLASH", InputConstants.KEY_BACKSLASH);
        put("COMMA", InputConstants.KEY_COMMA);
        put("PERIOD", InputConstants.KEY_PERIOD);
        put("SLASH", InputConstants.KEY_SLASH);
        put("ADD", InputConstants.KEY_ADD);
        put("MULTIPLY", InputConstants.KEY_MULTIPLY);
        put("NUMPADENTER", InputConstants.KEY_NUMPADENTER);
        put("NUMPADEQUALS", InputConstants.KEY_NUMPADEQUALS);
        // aliases the original accepted (Keyboard.getKeyIndex knew both names)
        BY_NAME.put("LWIN", InputConstants.KEY_LGUI);
        BY_NAME.put("RWIN", InputConstants.KEY_RGUI);
    }

    private InvTweaksKeys() {
    }

    private static void put(String name, int code) {
        BY_NAME.put(name, code);
        BY_CODE.putIfAbsent(code, name);
    }

    /** Keyboard.getKeyIndex: {@link #NONE} for an unknown name. */
    public static int getKeyIndex(String name) {
        return BY_NAME.getOrDefault(name.trim().toUpperCase(), NONE);
    }

    /** Keyboard.getKeyName, null for a key without LWJGL 2 name. */
    public static String getKeyName(int code) {
        return BY_CODE.get(code);
    }

    /** Keyboard.isKeyDown. */
    public static boolean isKeyDown(int code) {
        return code != NONE && InputConstants.isKeyDown(code);
    }

    /** The hotbar slot (0..8) of the keys 1-9 and NUMPAD1-9, -1 for any other key. */
    public static int hotbarSlot(int code) {
        String name = getKeyName(code);
        if (name == null) {
            return -1;
        }
        name = name.replace("NUMPAD", "");
        if (name.length() == 1 && name.charAt(0) >= '1' && name.charAt(0) <= '9') {
            return name.charAt(0) - '1';
        }
        return -1;
    }
}
