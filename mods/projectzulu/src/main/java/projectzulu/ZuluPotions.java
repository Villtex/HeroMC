package projectzulu;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import projectzulu.potion.IncendiaryEffect;
import projectzulu.potion.PotionEvents;
import projectzulu.potion.ZuluEffect;

import java.util.ArrayList;
import java.util.List;

/**
 * Teil 11 – Potions: the six effects of PotionManager (ids 21-26 in 1.5.2) and the potions the 1.5.2 bitmask
 * brewing of Project Zulu could make. Durations and levels are the values PotionHelper computed for the damage values
 * the Zulu brewing stand reached (see tools/gen_potions.py, which also writes the brewing recipes as JSON):
 * <ul>
 *   <li>normal 45 s, long (redstone) 120 s, strong (glowstone) level II 22.5 s;</li>
 *   <li>"Shiny" tier (brewed on the Shiny! potion instead of the awkward potion): level II 97.5 s, long level II
 *       135 s, strong level III 48.75 s;</li>
 *   <li>haste, resistance and mining fatigue: 1.5.2 brewed them from Zulu ingredients, 26.3 has no potion for them.</li>
 * </ul>
 */
public final class ZuluPotions {
    private static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, ProjectZulu.MOD_ID);
    private static final DeferredRegister<Potion> POTIONS = DeferredRegister.create(Registries.POTION, ProjectZulu.MOD_ID);
    private static final List<DeferredHolder<Potion, Potion>> TAB_POTIONS = new ArrayList<>();

    // PotionManager: colours of the original
    public static final DeferredHolder<MobEffect, MobEffect> BUBBLING = EFFECTS.register("bubbling", () -> new ZuluEffect(10847046, true));
    public static final DeferredHolder<MobEffect, MobEffect> INCENDIARY = EFFECTS.register("incendiary", () -> new IncendiaryEffect(8733978));
    public static final DeferredHolder<MobEffect, MobEffect> SLOWFALL = EFFECTS.register("slowfall", () -> new ZuluEffect(14079702, false));
    public static final DeferredHolder<MobEffect, MobEffect> CLEANSING = EFFECTS.register("cleansing", () -> new ZuluEffect(9279823, false));
    public static final DeferredHolder<MobEffect, MobEffect> CURSE = EFFECTS.register("curse", () -> new ZuluEffect(7512116, false));
    public static final DeferredHolder<MobEffect, MobEffect> THORN = EFFECTS.register("thorn", () -> new ZuluEffect(1213730, false));

    public static final DeferredHolder<Potion, Potion> SHINING = potion("shining", "shining", BUBBLING, 1, 0);
    public static final DeferredHolder<Potion, Potion> INCENDIARY_POTION = potion("incendiary", "incendiary", INCENDIARY, 1, 0);
    public static final DeferredHolder<Potion, Potion> STRONG_INCENDIARY = potion("strong_incendiary", "incendiary", INCENDIARY, 1, 1);

    static {
        for (String name : new String[] {"slowfall", "thorn", "curse", "cleansing"}) {
            Holder<MobEffect> effect = switch (name) {
                case "slowfall" -> SLOWFALL;
                case "thorn" -> THORN;
                case "curse" -> CURSE;
                default -> CLEANSING;
            };
            potion(name, name, effect, 900, 0);
            potion("long_" + name, name, effect, 2400, 0);
            potion("strong_" + name, name, effect, 450, 1);
            potion("shiny_" + name, name, effect, 1950, 1);
            potion("long_shiny_" + name, name, effect, 2700, 1);
            potion("strong_shiny_" + name, name, effect, 975, 2);
        }
        potion("haste", "haste", MobEffects.HASTE, 5400, 0);
        potion("long_haste", "haste", MobEffects.HASTE, 14400, 0);
        potion("strong_haste", "haste", MobEffects.HASTE, 2700, 1);
        potion("resistance", "resistance", MobEffects.RESISTANCE, 3600, 0);
        potion("long_resistance", "resistance", MobEffects.RESISTANCE, 9600, 0);
        potion("strong_resistance", "resistance", MobEffects.RESISTANCE, 1800, 1);
        potion("mining_fatigue", "mining_fatigue", MobEffects.MINING_FATIGUE, 1800, 0);
        potion("long_mining_fatigue", "mining_fatigue", MobEffects.MINING_FATIGUE, 4800, 0);
        potion("strong_mining_fatigue", "mining_fatigue", MobEffects.MINING_FATIGUE, 900, 1);
    }

    private ZuluPotions() {
    }

    /** Potion "projectzulu:id"; the name gives the translation key item.minecraft.potion.effect.projectzulu.name. */
    private static DeferredHolder<Potion, Potion> potion(String id, String name, Holder<MobEffect> effect, int duration, int amplifier) {
        DeferredHolder<Potion, Potion> holder = POTIONS.register(id,
                () -> new Potion(ProjectZulu.MOD_ID + "." + name, new MobEffectInstance(effect, duration, amplifier)));
        TAB_POTIONS.add(holder);
        return holder;
    }

    public static void register(IEventBus modBus) {
        EFFECTS.register(modBus);
        POTIONS.register(modBus);
        NeoForge.EVENT_BUS.addListener(PotionEvents::onEntityTick);
        NeoForge.EVENT_BUS.addListener(PotionEvents::onDamage);
        // CreativeTab.displayAllReleventItems: the Zulu potions as drinkable and splash potion (+ lingering)
        ZuluCreativeTabs.addExtra((parameters, output) -> {
            for (Item item : new Item[] {Items.POTION, Items.SPLASH_POTION, Items.LINGERING_POTION}) {
                for (DeferredHolder<Potion, Potion> potion : TAB_POTIONS) {
                    output.accept(PotionContents.createItemStack(item, potion));
                }
            }
        });
    }
}
