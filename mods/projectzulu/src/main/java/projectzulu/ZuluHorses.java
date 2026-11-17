package projectzulu;

import java.util.function.Function;
import java.util.function.UnaryOperator;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import projectzulu.entity.ZuluHorseEntity;

/**
 * Creatures of Project Zulu, Batch E: the seven horses (EntityHorseBeige ... EntityHorseWhite, one class here, the
 * colour is the entity type; horserandom is off in the pack). Same conventions as {@link ZuluEntities} (registration
 * ids in tools/PORTING.md); a separate class so the creature batches do not share one file. Pack configuration:
 * Creature Type creature, Should Despawn false (an Animal never despawns), SecondarySpawnRate 100 (no random gate).
 * Untamed EntityGenericAnimals left peaceful, and the horses could never be tamed: notInPeaceful.
 */
public final class ZuluHorses {
    private static final DeferredRegister.Entities ENTITIES = DeferredRegister.createEntities(ProjectZulu.MOD_ID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ProjectZulu.MOD_ID);
    private static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, ProjectZulu.MOD_ID);

    public static final DeferredHolder<EntityType<?>, EntityType<ZuluHorseEntity>> HORSE_BEIGE = horse("horse_beige");
    public static final DeferredHolder<EntityType<?>, EntityType<ZuluHorseEntity>> HORSE_BLACK = horse("horse_black");
    public static final DeferredHolder<EntityType<?>, EntityType<ZuluHorseEntity>> HORSE_BROWN = horse("horse_brown");
    public static final DeferredHolder<EntityType<?>, EntityType<ZuluHorseEntity>> HORSE_DARK_BLACK = horse("horse_dark_black");
    public static final DeferredHolder<EntityType<?>, EntityType<ZuluHorseEntity>> HORSE_DARK_BROWN = horse("horse_dark_brown");
    public static final DeferredHolder<EntityType<?>, EntityType<ZuluHorseEntity>> HORSE_GREY = horse("horse_grey");
    public static final DeferredHolder<EntityType<?>, EntityType<ZuluHorseEntity>> HORSE_WHITE = horse("horse_white");

    // ---- spawn eggs (colours in the item models, tools/gen.py) ----
    public static final DeferredItem<Item> HORSE_BEIGE_SPAWN_EGG = egg("horse_beige_spawn_egg", HORSE_BEIGE);
    public static final DeferredItem<Item> HORSE_BLACK_SPAWN_EGG = egg("horse_black_spawn_egg", HORSE_BLACK);
    public static final DeferredItem<Item> HORSE_BROWN_SPAWN_EGG = egg("horse_brown_spawn_egg", HORSE_BROWN);
    public static final DeferredItem<Item> HORSE_DARK_BLACK_SPAWN_EGG = egg("horse_dark_black_spawn_egg", HORSE_DARK_BLACK);
    public static final DeferredItem<Item> HORSE_DARK_BROWN_SPAWN_EGG = egg("horse_dark_brown_spawn_egg", HORSE_DARK_BROWN);
    public static final DeferredItem<Item> HORSE_GREY_SPAWN_EGG = egg("horse_grey_spawn_egg", HORSE_GREY);
    public static final DeferredItem<Item> HORSE_WHITE_SPAWN_EGG = egg("horse_white_spawn_egg", HORSE_WHITE);

    // ---- sounds: "sounds.horse" (horse1-4 as random variants) and "sounds.horsehurt"; horseplop was never played ----
    public static final DeferredHolder<SoundEvent, SoundEvent> HORSE_LIVING = sound("horse_living");
    public static final DeferredHolder<SoundEvent, SoundEvent> HORSE_HURT = sound("horse_hurt");

    private ZuluHorses() {
    }

    /**
     * setSize(1.5, 2.0); the rider sits where the 1.5.2 mount put him (mountedYOffset 0.75 * height plus the player's
     * yOffset: feet 1.0 above the horse, i.e. 1.6 with the 0.6 vehicle attachment of the player).
     */
    private static DeferredHolder<EntityType<?>, EntityType<ZuluHorseEntity>> horse(String name) {
        return entity(name, ZuluHorseEntity::new, MobCategory.CREATURE, b -> b.sized(1.5F, 2.0F).passengerAttachments(1.6F).notInPeaceful());
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

    private static DeferredHolder<SoundEvent, SoundEvent> sound(String name) {
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(ProjectZulu.MOD_ID, name)));
    }

    private static void attributes(EntityAttributeCreationEvent event) {
        event.put(HORSE_BEIGE.get(), ZuluHorseEntity.createAttributes().build());
        event.put(HORSE_BLACK.get(), ZuluHorseEntity.createAttributes().build());
        event.put(HORSE_BROWN.get(), ZuluHorseEntity.createAttributes().build());
        event.put(HORSE_DARK_BLACK.get(), ZuluHorseEntity.createAttributes().build());
        event.put(HORSE_DARK_BROWN.get(), ZuluHorseEntity.createAttributes().build());
        event.put(HORSE_GREY.get(), ZuluHorseEntity.createAttributes().build());
        event.put(HORSE_WHITE.get(), ZuluHorseEntity.createAttributes().build());
    }

    /** getCanSpawnHere: block light below 1 (EntityGenericAnimal) and canBlockSeeTheSky (EntityHorseBase.isValidLocation). */
    private static void spawnPlacements(RegisterSpawnPlacementsEvent event) {
        ZuluEntities.daylitGroundPlacement(event, HORSE_BEIGE.get());
        ZuluEntities.daylitGroundPlacement(event, HORSE_BLACK.get());
        ZuluEntities.daylitGroundPlacement(event, HORSE_BROWN.get());
        ZuluEntities.daylitGroundPlacement(event, HORSE_DARK_BLACK.get());
        ZuluEntities.daylitGroundPlacement(event, HORSE_DARK_BROWN.get());
        ZuluEntities.daylitGroundPlacement(event, HORSE_GREY.get());
        ZuluEntities.daylitGroundPlacement(event, HORSE_WHITE.get());
    }

    public static void register(IEventBus modBus) {
        ENTITIES.register(modBus);
        ITEMS.register(modBus);
        SOUNDS.register(modBus);
        modBus.addListener(ZuluHorses::attributes);
        modBus.addListener(ZuluHorses::spawnPlacements);
    }
}
