package tconstruct.tools;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The tooltips of the modifiers are stored as the English strings of 1.5.2 ("§4Haste II", "§4Redstone (10/50)"); for
 * the display the colour codes become styles and the name is looked up as {@code modifier.tconstruct.<name>}
 * (falling back to the stored English).
 */
public final class ToolTips {
    private static final Pattern SUFFIX = Pattern.compile("^(.*?)( (?:II|III|IV|V|VI|VII|VIII|IX|X|X\\+)| \\(\\d+/\\d+\\))?$");

    private ToolTips() {
    }

    public static Component translate(String raw) {
        int i = 0;
        ChatFormatting color = null;
        while (i + 1 < raw.length() && raw.charAt(i) == '§') {
            ChatFormatting f = ChatFormatting.getByCode(raw.charAt(i + 1));
            if (f != null) {
                color = f;
            }
            i += 2;
        }
        String text = raw.substring(i);
        Matcher m = SUFFIX.matcher(text);
        String name = text;
        String suffix = "";
        if (m.matches()) {
            name = m.group(1);
            suffix = m.group(2) == null ? "" : m.group(2);
        }
        String key = "modifier.tconstruct." + name.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "_").replaceAll("^_|_$", "");
        MutableComponent line = Component.translatableWithFallback(key, name).append(suffix);
        return color == null ? line : line.withStyle(color);
    }
}
