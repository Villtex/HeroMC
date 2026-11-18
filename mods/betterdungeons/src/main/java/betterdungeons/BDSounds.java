package betterdungeons;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * The sounds EventHandlerBD.onSoundLoad added from Chocolate/sounds ("sounds.&lt;name&gt;" in 1.5.2). The oggs come from
 * the pack's data archive (original-assets.txt); the mob sounds are here for the mob port.
 */
public final class BDSounds {
    static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, BetterDungeons.MOD_ID);

    public static final DeferredHolder<SoundEvent, SoundEvent> HANDGUN = sound("handgun");
    public static final DeferredHolder<SoundEvent, SoundEvent> WIND = sound("wind");
    public static final DeferredHolder<SoundEvent, SoundEvent> SMALL_MONSTER_DEAD = sound("small_monster_dead");
    public static final DeferredHolder<SoundEvent, SoundEvent> SMALL_MONSTER_SPEAK = sound("small_monster_speak");
    public static final DeferredHolder<SoundEvent, SoundEvent> SMALL_MONSTER_HURT = sound("small_monster_hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> SMALL_MONSTER_LAUGHT = sound("small_monster_laught");
    public static final DeferredHolder<SoundEvent, SoundEvent> BREATH_LOUD = sound("breath_loud");
    public static final DeferredHolder<SoundEvent, SoundEvent> BREATH_DOUBLE = sound("breath_double");
    public static final DeferredHolder<SoundEvent, SoundEvent> MAGIC = sound("magic");
    public static final DeferredHolder<SoundEvent, SoundEvent> BUBBLE = sound("bubble");

    private BDSounds() {
    }

    private static DeferredHolder<SoundEvent, SoundEvent> sound(String name) {
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(BetterDungeons.MOD_ID, name)));
    }

    static void register(IEventBus modBus) {
        SOUNDS.register(modBus);
    }
}
