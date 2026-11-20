package tconstruct.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import tconstruct.TConstruct;

/** EntityRegistry.registerModEntity of TConstruct: the tinker arrow ("Arrow", 64/1) and the thrown dagger ("Dagger", 64/1). */
public final class TEntities {
    static final DeferredRegister.Entities ENTITIES = DeferredRegister.createEntities(TConstruct.MOD_ID);

    public static final DeferredHolder<EntityType<?>, EntityType<TinkerArrow>> ARROW = ENTITIES.registerEntityType("arrow", TinkerArrow::new,
            MobCategory.MISC, b -> b.noLootTable().sized(0.5F, 0.5F).eyeHeight(0.13F).clientTrackingRange(4).updateInterval(20));
    public static final DeferredHolder<EntityType<?>, EntityType<ThrownDagger>> DAGGER = ENTITIES.registerEntityType("dagger", ThrownDagger::new,
            MobCategory.MISC, b -> b.noLootTable().sized(0.5F, 0.5F).eyeHeight(0.13F).clientTrackingRange(4).updateInterval(20));

    private TEntities() {
    }

    public static void register(IEventBus modBus) {
        ENTITIES.register(modBus);
    }
}
