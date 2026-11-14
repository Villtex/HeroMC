package armorbarmod;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.ArrayList;
import java.util.List;

/**
 * Armor Bar Mod 0.7.1: icons of the worn armour, the arrows and up to four other tracked items to the left and
 * right of the hotbar, each with a bar for durability or amount and, if wanted, a number. Client only in effect.
 */
@Mod(ArmorBarMod.MOD_ID)
public final class ArmorBarMod {
    public static final String MOD_ID = "mod_armorbarmod";
    public static final List<Unit> UNITS = new ArrayList<>();
    private static final ModConfigSpec SPEC;

    /** Kind of display: an armour slot, or an item tracked by amount or by durability. */
    public enum Kind { FEET, LEGS, CHEST, HEAD, AMOUNT, DURABILITY }

    /** One DisplayUnit of mod_ArmorBarMod.cfg; the defaults are the values of the pack config. */
    public static final class Unit {
        public final Kind kind;
        public final ModConfigSpec.BooleanValue shouldDisplay;
        public final ModConfigSpec.IntValue color;
        public final ModConfigSpec.BooleanValue analogBar;
        public final ModConfigSpec.BooleanValue numericCounter;
        public final ModConfigSpec.IntValue fadeRate;
        public final ModConfigSpec.IntValue offsetX;
        public final ModConfigSpec.IntValue offsetY;
        public final ModConfigSpec.IntValue analogX;
        public final ModConfigSpec.IntValue analogY;
        public final ModConfigSpec.IntValue counterX;
        public final ModConfigSpec.IntValue counterY;
        public final ModConfigSpec.IntValue updateFrequency;
        // only for tracked items
        public final ModConfigSpec.ConfigValue<String> item;
        public final ModConfigSpec.IntValue analogUpperLimit;
        public final ModConfigSpec.BooleanValue displayWhenEmpty;

        Unit(ModConfigSpec.Builder b, String name, Kind kind, String item, boolean display, int x, int y, boolean left, boolean counter) {
            this.kind = kind;
            b.push(name);
            this.shouldDisplay = b.define("should_display", display);
            this.color = b.comment("Colour of the numeric counter").defineInRange("display_color", 1030655, 0, 0xFFFFFF);
            this.analogBar = b.define("display_analog_bar", true);
            this.numericCounter = b.define("display_numeric_counter", counter);
            this.fadeRate = b.comment("Ticks until an unchanged display fades out, 0 = never").defineInRange("fade_rate", 0, 0, 100000);
            this.offsetX = b.comment("Offset from the bottom centre of the screen, x to the right").defineInRange("main_offset_x", x, -10000, 10000);
            this.offsetY = b.comment("y upwards").defineInRange("main_offset_y", y, -10000, 10000);
            this.analogX = b.defineInRange("analog_display_offset_x", 0, -10000, 10000);
            this.analogY = b.defineInRange("analog_display_offset_y", 16, -10000, 10000);
            this.counterX = b.defineInRange("counter_display_offset_x", left ? -14 : 14, -10000, 10000);
            this.counterY = b.defineInRange("counter_display_offset_y", -6, -10000, 10000);
            this.updateFrequency = b.comment("Ticks between updates").defineInRange("update_frequency", 10, 1, 100000);
            if (item != null) {
                this.item = b.comment("Item to track").define("item_to_track", item);
                this.analogUpperLimit = b.comment("Amount that fills the bar").defineInRange("analog_upper_limit", 64, 1, 100000);
                this.displayWhenEmpty = b.define("display_when_empty", true);
            } else {
                this.item = null;
                this.analogUpperLimit = null;
                this.displayWhenEmpty = null;
            }
            b.pop();
        }
    }

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder().push("armorbar");
        UNITS.add(new Unit(b, "feet", Kind.FEET, null, true, 95, 20, false, false));
        UNITS.add(new Unit(b, "legs", Kind.LEGS, null, true, 95, 40, false, false));
        UNITS.add(new Unit(b, "chest", Kind.CHEST, null, true, -111, 20, true, false));
        UNITS.add(new Unit(b, "head", Kind.HEAD, null, true, -111, 40, true, false));
        // item ids 262, 268, 267, 263, 264 of the pack config
        UNITS.add(new Unit(b, "arrow", Kind.AMOUNT, "minecraft:arrow", true, 119, 20, false, true));
        UNITS.add(new Unit(b, "genericdurabilitycounter1", Kind.DURABILITY, "minecraft:wooden_sword", false, -135, 20, true, false));
        UNITS.add(new Unit(b, "genericdurabilitycounter2", Kind.DURABILITY, "minecraft:iron_sword", false, -135, 40, true, false));
        UNITS.add(new Unit(b, "genericamountcounter1", Kind.AMOUNT, "minecraft:coal", false, 119, 40, false, true));
        UNITS.add(new Unit(b, "genericamountcounter2", Kind.AMOUNT, "minecraft:diamond", false, 119, 60, false, true));
        SPEC = b.pop().build();
    }

    public ArmorBarMod(IEventBus modBus, ModContainer container) {
        container.registerConfig(ModConfig.Type.CLIENT, SPEC);
        if (FMLEnvironment.getDist() == Dist.CLIENT) {
            armorbarmod.client.ArmorBarHud.register(modBus);
        }
    }
}
