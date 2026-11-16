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
import projectzulu.entity.EagleEntity;
import projectzulu.entity.FinchEntity;
import projectzulu.entity.HornBillEntity;
import projectzulu.entity.PelicanEntity;
import projectzulu.entity.VultureEntity;

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
            MobCategory.CREATURE, b -> b.sized(0.6F, 1.8F).notInPeaceful());
    // birds (Batch A): Creature Type ambient, the vulture monster (despawnInPeaceful of the pack -> notInPeaceful)
    public static final DeferredHolder<EntityType<?>, EntityType<FinchEntity>> RED_FINCH = entity("red_finch", FinchEntity::new,
            MobCategory.AMBIENT, b -> b.sized(0.5F, 0.5F).notInPeaceful());
    public static final DeferredHolder<EntityType<?>, EntityType<FinchEntity>> GREEN_FINCH = entity("green_finch", FinchEntity::new,
            MobCategory.AMBIENT, b -> b.sized(0.5F, 0.5F).notInPeaceful());
    public static final DeferredHolder<EntityType<?>, EntityType<FinchEntity>> BLUE_FINCH = entity("blue_finch", FinchEntity::new,
            MobCategory.AMBIENT, b -> b.sized(0.5F, 0.5F).notInPeaceful());
    public static final DeferredHolder<EntityType<?>, EntityType<EagleEntity>> EAGLE = entity("eagle", EagleEntity::new,
            MobCategory.AMBIENT, b -> b.sized(1.0F, 1.4F).notInPeaceful());
    public static final DeferredHolder<EntityType<?>, EntityType<VultureEntity>> VULTURE = entity("vulture", VultureEntity::new,
            MobCategory.MONSTER, b -> b.sized(1.0F, 1.4F).notInPeaceful());
    public static final DeferredHolder<EntityType<?>, EntityType<HornBillEntity>> HORN_BILL = entity("horn_bill", HornBillEntity::new,
            MobCategory.AMBIENT, b -> b.sized(0.8F, 1.2F).notInPeaceful());
    public static final DeferredHolder<EntityType<?>, EntityType<PelicanEntity>> PELICAN = entity("pelican", PelicanEntity::new,
            MobCategory.AMBIENT, b -> b.sized(1.5F, 1.4F).notInPeaceful());

    // ---- spawn eggs (colours in the item models, tools/gen.py) ----
    public static final DeferredItem<Item> ARMADILLO_SPAWN_EGG = egg("armadillo_spawn_egg", ARMADILLO);
    public static final DeferredItem<Item> RED_FINCH_SPAWN_EGG = egg("red_finch_spawn_egg", RED_FINCH);
    public static final DeferredItem<Item> GREEN_FINCH_SPAWN_EGG = egg("green_finch_spawn_egg", GREEN_FINCH);
    public static final DeferredItem<Item> BLUE_FINCH_SPAWN_EGG = egg("blue_finch_spawn_egg", BLUE_FINCH);
    public static final DeferredItem<Item> EAGLE_SPAWN_EGG = egg("eagle_spawn_egg", EAGLE);
    public static final DeferredItem<Item> VULTURE_SPAWN_EGG = egg("vulture_spawn_egg", VULTURE);
    public static final DeferredItem<Item> HORN_BILL_SPAWN_EGG = egg("horn_bill_spawn_egg", HORN_BILL);
    public static final DeferredItem<Item> PELICAN_SPAWN_EGG = egg("pelican_spawn_egg", PELICAN);

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
        event.put(RED_FINCH.get(), FinchEntity.createAttributes().build());
        event.put(GREEN_FINCH.get(), FinchEntity.createAttributes().build());
        event.put(BLUE_FINCH.get(), FinchEntity.createAttributes().build());
        event.put(EAGLE.get(), EagleEntity.createAttributes().build());
        event.put(VULTURE.get(), VultureEntity.createAttributes().build());
        event.put(HORN_BILL.get(), HornBillEntity.createAttributes().build());
        event.put(PELICAN.get(), PelicanEntity.createAttributes().build());
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
        // birds: secondarySpawnRate of the pack (5, horn bill 25), block light below 1, sky above
        birdPlacement(event, RED_FINCH.get(), 5);
        birdPlacement(event, GREEN_FINCH.get(), 5);
        birdPlacement(event, BLUE_FINCH.get(), 5);
        birdPlacement(event, EAGLE.get(), 5);
        birdPlacement(event, VULTURE.get(), 5);
        birdPlacement(event, HORN_BILL.get(), 25);
        birdPlacement(event, PELICAN.get(), 5);
    }

    /**
     * getCanSpawnHere of the birds: "secondarySpawnRate - rand.nextInt(100) >= 0" (the pack value is not 100 for the
     * birds), block light below 1 and isValidLocation = canBlockSeeTheSky.
     */
    private static <T extends Entity> void birdPlacement(RegisterSpawnPlacementsEvent event, EntityType<T> type, int secondarySpawnRate) {
        event.register(type, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                (t, level, reason, pos, random) -> secondarySpawnRate - random.nextInt(100) >= 0
                        && level.getBrightness(net.minecraft.world.level.LightLayer.BLOCK, pos) < 1 && level.canSeeSky(pos),
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
