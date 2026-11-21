package twilightforest;

import hexxit.worldgen152.OldGameIds;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import twilightforest.entity.boss.hydra.Hydra;
import twilightforest.entity.boss.hydra.HydraHead;
import twilightforest.entity.boss.hydra.HydraMortar;

import java.util.ArrayList;
import java.util.List;

/**
 * The hydra (Teil 24f): EntityTFHydra with its heads, necks, body parts and mortars. Own registers for its entity
 * types, egg, sounds and the large flame particle of its breath (EntityTFLargeFlameFX); the hydra boss spawner
 * (1351:2) summons "twilightforest:hydra" and gives it its home with {@code setHomeTo(pos, 50)}.
 */
public final class TFHydra {
    private static final DeferredRegister.Entities ENTITIES = DeferredRegister.createEntities(TwilightForest.MOD_ID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(TwilightForest.MOD_ID);
    private static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, TwilightForest.MOD_ID);
    private static final DeferredRegister<ParticleType<?>> PARTICLES = DeferredRegister.create(Registries.PARTICLE_TYPE, TwilightForest.MOD_ID);

    /** Items for the creative tab (the spawn egg). */
    public static final List<DeferredItem<Item>> TAB_ITEMS = new ArrayList<>();

    /** "Hydra", mob 189: tracking 80 (5 chunks), every 3 ticks. */
    public static final DeferredHolder<EntityType<?>, EntityType<Hydra>> HYDRA = ENTITIES.registerEntityType("hydra", Hydra::new,
            MobCategory.MONSTER, b -> b.sized(16.0F, 12.0F).fireImmune().clientTrackingRange(5).updateInterval(3));
    /** "HydraHead", mod entity 11: tracking 150 (10 chunks), every 3 ticks; it drops nothing. */
    public static final DeferredHolder<EntityType<?>, EntityType<HydraHead>> HYDRA_HEAD = ENTITIES.registerEntityType("hydra_head",
            HydraHead::new, MobCategory.MISC, b -> b.sized(3.0F, 3.0F).fireImmune().noLootTable().clientTrackingRange(10).updateInterval(3));
    /** "tfhydramortar", mod entity 5: tracking 150, every 3 ticks. */
    public static final DeferredHolder<EntityType<?>, EntityType<HydraMortar>> HYDRA_MORTAR = ENTITIES.registerEntityType("hydra_mortar",
            HydraMortar::new, MobCategory.MISC, b -> b.sized(0.75F, 0.75F).clientTrackingRange(10).updateInterval(3));

    public static final DeferredItem<Item> HYDRA_SPAWN_EGG = ITEMS.registerItem("hydra_spawn_egg", SpawnEggItem::new,
            p -> p.spawnEgg(HYDRA.get()));

    /** mob.tf.hydra.* of TFSounds. */
    public static final DeferredHolder<SoundEvent, SoundEvent> HYDRA_GROWL = sound("hydra.growl");
    public static final DeferredHolder<SoundEvent, SoundEvent> HYDRA_HURT = sound("hydra.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> HYDRA_DEATH = sound("hydra.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> HYDRA_ROAR = sound("hydra.roar");
    public static final DeferredHolder<SoundEvent, SoundEvent> HYDRA_WARN = sound("hydra.warn");

    /** "largeflame" of TFClientProxy.spawnParticle (EntityTFLargeFlameFX), the flame breath. */
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> LARGE_FLAME = PARTICLES.register("large_flame",
            () -> new SimpleParticleType(false));

    static {
        TAB_ITEMS.add(HYDRA_SPAWN_EGG);
    }

    private TFHydra() {
    }

    private static DeferredHolder<SoundEvent, SoundEvent> sound(String name) {
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(TwilightForest.id(name)));
    }

    static void register(IEventBus modBus) {
        ENTITIES.register(modBus);
        ITEMS.register(modBus);
        SOUNDS.register(modBus);
        PARTICLES.register(modBus);
        modBus.addListener(EntityAttributeCreationEvent.class, TFHydra::attributes);
    }

    private static void attributes(EntityAttributeCreationEvent event) {
        event.put(HYDRA.get(), Hydra.createAttributes().build());
        event.put(HYDRA_HEAD.get(), HydraHead.createAttributes().build());
    }

    /** The old spawn egg (item 7309 + 256, damage 189) and the spawner name of the hydra. */
    public static void registerOld() {
        OldGameIds.registerItem(7309 + 256, 189, HYDRA_SPAWN_EGG.getId().toString());
        OldGameIds.registerEntity("TwilightForest.Hydra", HYDRA.getId().toString());
    }
}
