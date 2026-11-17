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
    // birds (Batch A); "sounds.bird" etc. of the original, the numbered oggs are random variants of one event
    public static final DeferredHolder<SoundEvent, SoundEvent> BIRD = sound("bird");
    public static final DeferredHolder<SoundEvent, SoundEvent> BIRD_HURT = sound("bird_hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> EAGLE_LIVING = sound("eagle_living");
    public static final DeferredHolder<SoundEvent, SoundEvent> EAGLE_HURT = sound("eagle_hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> VULTURE_HURT = sound("vulture_hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> HORNBILL_LIVING = sound("hornbill_living");
    public static final DeferredHolder<SoundEvent, SoundEvent> HORNBILL_HURT = sound("hornbill_hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> PELICAN_LIVING = sound("pelican_living");
    public static final DeferredHolder<SoundEvent, SoundEvent> PELICAN_HURT = sound("pelican_hurt");
    // land animals (Batch B)
    public static final DeferredHolder<SoundEvent, SoundEvent> BOAR_LIVING = sound("boar_living");
    public static final DeferredHolder<SoundEvent, SoundEvent> FOX_HURT = sound("fox_hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> FROG_LIVING = sound("frog_living");
    public static final DeferredHolder<SoundEvent, SoundEvent> FROG_HURT = sound("frog_hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> RABBIT_HURT = sound("rabbit_hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> RABBIT_DEAD = sound("rabbit_dead");
    public static final DeferredHolder<SoundEvent, SoundEvent> BEAVER_LIVING = sound("beaver_living");
    public static final DeferredHolder<SoundEvent, SoundEvent> PENGUIN_HURT = sound("penguin_hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> OSTRICH_LIVING = sound("ostrich_living");
    public static final DeferredHolder<SoundEvent, SoundEvent> OSTRICH_HURT = sound("ostrich_hurt");

    private ZuluSounds() {
    }

    private static DeferredHolder<SoundEvent, SoundEvent> sound(String name) {
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(ProjectZulu.MOD_ID, name)));
    }

    public static void register(IEventBus modBus) {
        SOUNDS.register(modBus);
    }
}
