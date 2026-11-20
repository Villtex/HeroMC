package legendgear;

import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * The sounds ExtraSounds added ("assets.&lt;name&gt;" in 1.5.2; "heart" is aheart.ogg). The oggs come from the original
 * jar (original-assets.txt).
 */
public final class LGSounds {
    static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, LegendGear.MOD_ID);

    public static final DeferredHolder<SoundEvent, SoundEvent> SINE = sound("sine");
    public static final DeferredHolder<SoundEvent, SoundEvent> HEART = sound("heart");
    public static final DeferredHolder<SoundEvent, SoundEvent> SPEEDBOOST = sound("speedboost");
    public static final DeferredHolder<SoundEvent, SoundEvent> MONEYSMALL = sound("moneysmall");
    public static final DeferredHolder<SoundEvent, SoundEvent> MONEYMID = sound("moneymid");
    public static final DeferredHolder<SoundEvent, SoundEvent> MONEYBIG = sound("moneybig");
    public static final DeferredHolder<SoundEvent, SoundEvent> PARTIALCHARGE = sound("partialcharge");
    public static final DeferredHolder<SoundEvent, SoundEvent> FULLCHARGE = sound("fullcharge");
    public static final DeferredHolder<SoundEvent, SoundEvent> REPEL = sound("repel");
    public static final DeferredHolder<SoundEvent, SoundEvent> BOOMERANG = sound("boomerang");
    public static final DeferredHolder<SoundEvent, SoundEvent> LIFT = sound("lift");
    public static final DeferredHolder<SoundEvent, SoundEvent> THROW = sound("throw");
    public static final DeferredHolder<SoundEvent, SoundEvent> FIREBUILD = sound("firebuild");
    public static final DeferredHolder<SoundEvent, SoundEvent> HIGHCHARGE = sound("highcharge");
    public static final DeferredHolder<SoundEvent, SoundEvent> SWORDCHARGE = sound("swordcharge");
    public static final DeferredHolder<SoundEvent, SoundEvent> WHIRLWIND = sound("whirlwind");
    public static final DeferredHolder<SoundEvent, SoundEvent> POPIN = sound("popin");
    public static final DeferredHolder<SoundEvent, SoundEvent> SWORDPLACE = sound("swordplace");
    public static final DeferredHolder<SoundEvent, SoundEvent> SWORDTAKE = sound("swordtake");
    public static final DeferredHolder<SoundEvent, SoundEvent> UNCHAIN = sound("unchain");
    public static final DeferredHolder<SoundEvent, SoundEvent> WAH = sound("wah");
    public static final DeferredHolder<SoundEvent, SoundEvent> WAHWAH = sound("wahwah");
    public static final DeferredHolder<SoundEvent, SoundEvent> BAH = sound("bah");
    public static final DeferredHolder<SoundEvent, SoundEvent> GAH = sound("gah");
    public static final DeferredHolder<SoundEvent, SoundEvent> STARFALL = sound("starfall");
    public static final DeferredHolder<SoundEvent, SoundEvent> STARCAUGHT = sound("starcaught");
    public static final DeferredHolder<SoundEvent, SoundEvent> TWINKLE = sound("twinkle");
    public static final DeferredHolder<SoundEvent, SoundEvent> SPRINKLE = sound("sprinkle");
    public static final DeferredHolder<SoundEvent, SoundEvent> TRANSFORM = sound("transform");
    public static final DeferredHolder<SoundEvent, SoundEvent> FLUTEATTACK = sound("fluteattack");
    public static final DeferredHolder<SoundEvent, SoundEvent> FLUTESUSTAIN = sound("flutesustain");
    public static final DeferredHolder<SoundEvent, SoundEvent> DASH = sound("dash");
    public static final DeferredHolder<SoundEvent, SoundEvent> ESCALATE = sound("escalate");

    private LGSounds() {
    }

    private static DeferredHolder<SoundEvent, SoundEvent> sound(String name) {
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(LegendGear.id(name)));
    }

    static void register(IEventBus modBus) {
        SOUNDS.register(modBus);
    }
}
