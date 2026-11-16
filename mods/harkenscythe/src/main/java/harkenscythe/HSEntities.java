package harkenscythe;

import harkenscythe.entity.LostSoulEntity;
import harkenscythe.entity.SoulEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * The souls of mod_HarkenScythe (registerModEntity ids 1-5, tracking range 80, update interval 3). The lost soul
 * spawns through the biome modifier {@code harkenscythe_core:spawn_soul_lost}. The other mobs follow in Teil 9, part 3.
 */
public final class HSEntities {
    static final DeferredRegister.Entities ENTITIES = DeferredRegister.createEntities(HarkenScythe.MOD_ID);

    public static final DeferredHolder<EntityType<?>, EntityType<SoulEntity>> SOUL_COMMON = soul("soul_common");
    public static final DeferredHolder<EntityType<?>, EntityType<SoulEntity>> SOUL_GRIEVING = soul("soul_grieving");
    public static final DeferredHolder<EntityType<?>, EntityType<SoulEntity>> SOUL_CULLED = soul("soul_culled");
    public static final DeferredHolder<EntityType<?>, EntityType<SoulEntity>> SOUL_WRATHFUL = soul("soul_wrathful");
    public static final DeferredHolder<EntityType<?>, EntityType<LostSoulEntity>> SOUL_LOST = ENTITIES.registerEntityType("soul_lost",
            LostSoulEntity::new, MobCategory.MONSTER, b -> b.sized(0.5F, 0.5F).clientTrackingRange(5).updateInterval(3));

    private HSEntities() {
    }

    private static DeferredHolder<EntityType<?>, EntityType<SoulEntity>> soul(String name) {
        return ENTITIES.registerEntityType(name, SoulEntity::new, MobCategory.MISC,
                b -> b.sized(0.5F, 0.5F).clientTrackingRange(5).updateInterval(3).fireImmune());
    }

    private static void attributes(EntityAttributeCreationEvent event) {
        event.put(SOUL_COMMON.get(), SoulEntity.createAttributes().build());
        event.put(SOUL_GRIEVING.get(), SoulEntity.createAttributes().build());
        event.put(SOUL_CULLED.get(), SoulEntity.createAttributes().build());
        event.put(SOUL_WRATHFUL.get(), SoulEntity.createAttributes().build());
        event.put(SOUL_LOST.get(), LostSoulEntity.createAttributes().build());
    }

    private static void spawnPlacements(RegisterSpawnPlacementsEvent event) {
        event.register(SOUL_LOST.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, LostSoulEntity::checkSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.REPLACE);
    }

    static void register(IEventBus modBus) {
        modBus.addListener(HSEntities::attributes);
        modBus.addListener(HSEntities::spawnPlacements);
    }
}
