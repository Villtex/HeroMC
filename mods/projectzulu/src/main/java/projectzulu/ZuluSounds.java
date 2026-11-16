package projectzulu;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * The mob sounds of Project Zulu (core/Sounds.java of the original; the ogg files come in through
 * original-assets.txt, the events are in sounds.json from tools/gen.py).
 */
public final class ZuluSounds {
    private static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, ProjectZulu.MOD_ID);

    public static final DeferredHolder<SoundEvent, SoundEvent> ARMADILLO_LIVING = sound("armadillo_living");
    public static final DeferredHolder<SoundEvent, SoundEvent> ARMADILLO_HURT = sound("armadillo_hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> ARMADILLO_DEATH = sound("armadillo_death");

    private ZuluSounds() {
    }

    private static DeferredHolder<SoundEvent, SoundEvent> sound(String name) {
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(ProjectZulu.MOD_ID, name)));
    }

    public static void register(IEventBus modBus) {
        SOUNDS.register(modBus);
    }
}
