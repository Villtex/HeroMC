package projectzulu;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import projectzulu.entity.ArmadilloEntity;

import java.util.function.Function;
import java.util.function.UnaryOperator;

/**
 * The creatures of Project Zulu, with the pack configuration (config/Project Zulu) as the standard: only mobs with
 * shouldExist=true are here (horserandom is off). Registration in the original: trackingRange 128, updateFrequency 3
 * (CreatureDeclaration.setRegistrationProperties) -> clientTrackingRange(8), updateInterval(3). Spawns per biome,
 * spawn-egg colours and drops: tools/gen.py from ProjectZuluMobBiomeConfig.cfg.
 */
public final class ZuluEntities {
    private static final DeferredRegister.Entities ENTITIES = DeferredRegister.createEntities(ProjectZulu.MOD_ID);
    static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ProjectZulu.MOD_ID);

    // ---- creatures ----
    public static final DeferredHolder<EntityType<?>, EntityType<ArmadilloEntity>> ARMADILLO = entity("armadillo", ArmadilloEntity::new,
            MobCategory.CREATURE, b -> b.sized(0.6F, 1.8F));

    // ---- spawn eggs (colours in the item models, tools/gen.py) ----
    public static final DeferredItem<Item> ARMADILLO_SPAWN_EGG = egg("armadillo_spawn_egg", ARMADILLO);

    private ZuluEntities() {
    }

    private static <E extends Entity> DeferredHolder<EntityType<?>, EntityType<E>> entity(String name, EntityType.EntityFactory<E> factory,
            MobCategory category, UnaryOperator<EntityType.Builder<E>> builder) {
        return ENTITIES.registerEntityType(name, factory, category,
                b -> builder.apply(b.clientTrackingRange(8).updateInterval(3)));
    }

    private static DeferredItem<Item> egg(String name, DeferredHolder<EntityType<?>, ? extends EntityType<?>> type) {
        return item(name, SpawnEggItem::new, p -> p.spawnEgg(type.get()));
    }

    private static DeferredItem<Item> item(String name, Function<Item.Properties, Item> factory, UnaryOperator<Item.Properties> properties) {
        return ITEMS.registerItem(name, factory, properties);
    }

    private static void attributes(EntityAttributeCreationEvent event) {
        event.put(ARMADILLO.get(), ArmadilloEntity.createAttributes().build());
    }

    /**
     * getCanSpawnHere of EntityGenericAnimal: on the ground, block light below 1 (isValidLightLevel); the armadillo
     * additionally needs sky above (isValidLocation). The secondarySpawnRate of the pack is 100 everywhere, so no
     * extra random gate is ported.
     */
    private static void spawnPlacements(RegisterSpawnPlacementsEvent event) {
        event.register(ARMADILLO.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                (type, level, reason, pos, random) -> level.getBrightness(net.minecraft.world.level.LightLayer.BLOCK, pos) < 1
                        && level.canSeeSky(pos),
                RegisterSpawnPlacementsEvent.Operation.REPLACE);
    }

    public static void register(IEventBus modBus) {
        ENTITIES.register(modBus);
        ITEMS.register(modBus);
        ZuluSounds.register(modBus);
        modBus.addListener(ZuluEntities::attributes);
        modBus.addListener(ZuluEntities::spawnPlacements);
    }
}
