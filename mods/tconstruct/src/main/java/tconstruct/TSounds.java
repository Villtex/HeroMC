package tconstruct;

import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** "sounds.little_saw" (building a tool) and "sounds.frypan_hit". */
public final class TSounds {
    private static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, TConstruct.MOD_ID);
    public static final DeferredHolder<SoundEvent, SoundEvent> LITTLE_SAW = SOUNDS.register("little_saw", SoundEvent::createVariableRangeEvent);
    public static final DeferredHolder<SoundEvent, SoundEvent> FRYPAN_HIT = SOUNDS.register("frypan_hit", SoundEvent::createVariableRangeEvent);

    private TSounds() {
    }

    static void register(IEventBus modBus) {
        SOUNDS.register(modBus);
    }
}
