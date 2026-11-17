package projectzulu;

import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Creatures of Project Zulu,Batch D: sandworm, lizard and lizard spit, mummy, mummy pharaoh, mimic, minotaur, haunted armor, centipede. Same conventions as {@link ZuluEntities} (registration ids in
 * tools/PORTING.md); a separate class so the creature batches do not share one file.
 */
public final class ZuluMonsters {
    private static final DeferredRegister.Entities ENTITIES = DeferredRegister.createEntities(ProjectZulu.MOD_ID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ProjectZulu.MOD_ID);
    private static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, ProjectZulu.MOD_ID);

    private ZuluMonsters() {
    }

    private static void attributes(EntityAttributeCreationEvent event) {
    }

    private static void spawnPlacements(RegisterSpawnPlacementsEvent event) {
    }

    public static void register(IEventBus modBus) {
        ENTITIES.register(modBus);
        ITEMS.register(modBus);
        SOUNDS.register(modBus);
        modBus.addListener(ZuluMonsters::attributes);
        modBus.addListener(ZuluMonsters::spawnPlacements);
    }
}
