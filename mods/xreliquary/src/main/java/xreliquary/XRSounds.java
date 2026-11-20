package xreliquary;

import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** mods.xreliquary.sound.load and .shot (the oggs come from the original jar). */
public final class XRSounds {
    static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, Reliquary.MOD_ID);

    public static final DeferredHolder<SoundEvent, SoundEvent> LOAD = sound("load");
    public static final DeferredHolder<SoundEvent, SoundEvent> SHOT = sound("shot");

    private XRSounds() {
    }

    private static DeferredHolder<SoundEvent, SoundEvent> sound(String name) {
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(Reliquary.id(name)));
    }

    static void register(IEventBus modBus) {
        SOUNDS.register(modBus);
    }
}
