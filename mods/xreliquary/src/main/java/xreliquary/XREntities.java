package xreliquary;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import xreliquary.entity.ShotEntity;
import xreliquary.entity.ThrownEntity;

/** CommonProxy.registerEntityTrackers (range 128, update 5): the nine shots and the thrown things as one entity each. */
public final class XREntities {
    static final DeferredRegister.Entities ENTITIES = DeferredRegister.createEntities(Reliquary.MOD_ID);

    public static final DeferredHolder<EntityType<?>, EntityType<ShotEntity>> SHOT = ENTITIES.registerEntityType("shot", ShotEntity::new,
            MobCategory.MISC, b -> b.noLootTable().sized(0.1F, 0.1F).clientTrackingRange(8).updateInterval(5));
    public static final DeferredHolder<EntityType<?>, EntityType<ThrownEntity>> THROWN = ENTITIES.registerEntityType("thrown", ThrownEntity::new,
            MobCategory.MISC, b -> b.noLootTable().sized(0.25F, 0.25F).clientTrackingRange(8).updateInterval(5));

    private XREntities() {
    }

    static void register(IEventBus modBus) {
        ENTITIES.register(modBus);
    }
}
