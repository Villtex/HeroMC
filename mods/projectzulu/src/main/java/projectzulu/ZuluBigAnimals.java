package projectzulu;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import projectzulu.entity.AlligatorEntity;
import projectzulu.entity.BearEntity;
import projectzulu.entity.ElephantEntity;
import projectzulu.entity.GiraffeEntity;
import projectzulu.entity.GorillaEntity;
import projectzulu.entity.MammothEntity;
import projectzulu.entity.RhinoEntity;
import projectzulu.entity.TreeEntEntity;

import java.util.function.UnaryOperator;

/**
 * Creatures of Project Zulu, Batch C: bears, gorilla, rhino, alligator, giraffe, elephant, mammoth, tree ent. Same
 * conventions as {@link ZuluEntities} (registration ids in tools/PORTING.md); a separate class so the creature batches
 * do not share one file. Pack configuration: all Creature Type creature, Should Despawn false (Animal never despawns),
 * despawnInPeaceful -> notInPeaceful (none of them can be tamed). Sizes are the setSize calls of the originals.
 */
public final class ZuluBigAnimals {
    private static final DeferredRegister.Entities ENTITIES = DeferredRegister.createEntities(ProjectZulu.MOD_ID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ProjectZulu.MOD_ID);
    private static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, ProjectZulu.MOD_ID);

    // ---- creatures ----
    public static final DeferredHolder<EntityType<?>, EntityType<BearEntity.Black>> BLACK_BEAR = entity("black_bear", BearEntity.Black::new,
            b -> b.sized(1.0F, 1.35F));
    public static final DeferredHolder<EntityType<?>, EntityType<BearEntity.Brown>> BROWN_BEAR = entity("brown_bear", BearEntity.Brown::new,
            b -> b.sized(1.5F, 2.1F));
    public static final DeferredHolder<EntityType<?>, EntityType<BearEntity.Polar>> POLAR_BEAR = entity("polar_bear", BearEntity.Polar::new,
            b -> b.sized(2.0F, 2.7F));
    public static final DeferredHolder<EntityType<?>, EntityType<GorillaEntity>> GORILLA = entity("gorilla", GorillaEntity::new,
            b -> b.sized(1.7F, 1.5F));
    public static final DeferredHolder<EntityType<?>, EntityType<RhinoEntity>> RHINO = entity("rhino", RhinoEntity::new,
            b -> b.sized(2.0F, 2.0F));
    public static final DeferredHolder<EntityType<?>, EntityType<AlligatorEntity>> ALLIGATOR = entity("alligator", AlligatorEntity::new,
            b -> b.sized(1.7F, 0.9F));
    public static final DeferredHolder<EntityType<?>, EntityType<GiraffeEntity>> GIRAFFE = entity("giraffe", GiraffeEntity::new,
            b -> b.sized(2.3F, 4.0F));
    public static final DeferredHolder<EntityType<?>, EntityType<ElephantEntity>> ELEPHANT = entity("elephant", ElephantEntity::new,
            b -> b.sized(4.0F, 5.4F));
    public static final DeferredHolder<EntityType<?>, EntityType<MammothEntity>> MAMMOTH = entity("mammoth", MammothEntity::new,
            b -> b.sized(4.5F, 5.4F));
    public static final DeferredHolder<EntityType<?>, EntityType<TreeEntEntity>> TREE_ENT = entity("tree_ent", TreeEntEntity::new,
            b -> b.sized(1.7F, 3.0F));

    // ---- spawn eggs (colours in the item models, tools/gen.py) ----
    public static final DeferredItem<Item> BLACK_BEAR_SPAWN_EGG = egg("black_bear_spawn_egg", BLACK_BEAR);
    public static final DeferredItem<Item> BROWN_BEAR_SPAWN_EGG = egg("brown_bear_spawn_egg", BROWN_BEAR);
    public static final DeferredItem<Item> POLAR_BEAR_SPAWN_EGG = egg("polar_bear_spawn_egg", POLAR_BEAR);
    public static final DeferredItem<Item> GORILLA_SPAWN_EGG = egg("gorilla_spawn_egg", GORILLA);
    public static final DeferredItem<Item> RHINO_SPAWN_EGG = egg("rhino_spawn_egg", RHINO);
    public static final DeferredItem<Item> ALLIGATOR_SPAWN_EGG = egg("alligator_spawn_egg", ALLIGATOR);
    public static final DeferredItem<Item> GIRAFFE_SPAWN_EGG = egg("giraffe_spawn_egg", GIRAFFE);
    public static final DeferredItem<Item> ELEPHANT_SPAWN_EGG = egg("elephant_spawn_egg", ELEPHANT);
    public static final DeferredItem<Item> MAMMOTH_SPAWN_EGG = egg("mammoth_spawn_egg", MAMMOTH);
    public static final DeferredItem<Item> TREE_ENT_SPAWN_EGG = egg("tree_ent_spawn_egg", TREE_ENT);

    // ---- sounds ("sounds.<name>" of the original; oggs and sounds.json via tools/sound-map.txt and gen.py) ----
    public static final DeferredHolder<SoundEvent, SoundEvent> BEAR_LIVING = sound("bear_living");
    public static final DeferredHolder<SoundEvent, SoundEvent> GORILLA_HURT = sound("gorilla_hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> RHINO_LIVING = sound("rhino_living");
    public static final DeferredHolder<SoundEvent, SoundEvent> RHINO_HURT = sound("rhino_hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> CROCODILE_LIVING = sound("crocodile_living");
    public static final DeferredHolder<SoundEvent, SoundEvent> ELEPHANT_LIVING = sound("elephant_living");
    public static final DeferredHolder<SoundEvent, SoundEvent> ELEPHANT_HURT = sound("elephant_hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> MAMMOTH_LIVING = sound("mammoth_living");
    public static final DeferredHolder<SoundEvent, SoundEvent> TREEENT_LIVING = sound("treeent_living");

    private ZuluBigAnimals() {
    }

    /** Creature, tracking range 128 (-> 8 chunks), update frequency 3, gone in peaceful. */
    private static <E extends Entity> DeferredHolder<EntityType<?>, EntityType<E>> entity(String name, EntityType.EntityFactory<E> factory,
            UnaryOperator<EntityType.Builder<E>> builder) {
        return ENTITIES.registerEntityType(name, factory, MobCategory.CREATURE,
                b -> builder.apply(b.clientTrackingRange(8).updateInterval(3).notInPeaceful()));
    }

    private static DeferredItem<Item> egg(String name, DeferredHolder<EntityType<?>, ? extends EntityType<?>> type) {
        return ITEMS.registerItem(name, SpawnEggItem::new, p -> p.spawnEgg(type.get()));
    }

    private static DeferredHolder<SoundEvent, SoundEvent> sound(String name) {
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(ProjectZulu.MOD_ID, name)));
    }

    private static void attributes(EntityAttributeCreationEvent event) {
        event.put(BLACK_BEAR.get(), BearEntity.Black.createAttributes().build());
        event.put(BROWN_BEAR.get(), BearEntity.Brown.createAttributes().build());
        event.put(POLAR_BEAR.get(), BearEntity.Polar.createAttributes().build());
        event.put(GORILLA.get(), GorillaEntity.createAttributes().build());
        event.put(RHINO.get(), RhinoEntity.createAttributes().build());
        event.put(ALLIGATOR.get(), AlligatorEntity.createAttributes().build());
        event.put(GIRAFFE.get(), GiraffeEntity.createAttributes().build());
        event.put(ELEPHANT.get(), ElephantEntity.createAttributes().build());
        event.put(MAMMOTH.get(), MammothEntity.createAttributes().build());
        event.put(TREE_ENT.get(), TreeEntEntity.createAttributes().build());
    }

    /**
     * getCanSpawnHere of EntityGenericAnimal: block light below 1 and, for all of them, canBlockSeeTheSky
     * (isValidLocation). The pack's secondarySpawnRate is 100 except for the mammoth and the tree ent (7 each), whose
     * spawns pass a "7 - rand(100) >= 0" gate first.
     */
    private static void spawnPlacements(RegisterSpawnPlacementsEvent event) {
        ZuluEntities.daylitGroundPlacement(event, BLACK_BEAR.get());
        ZuluEntities.daylitGroundPlacement(event, BROWN_BEAR.get());
        ZuluEntities.daylitGroundPlacement(event, POLAR_BEAR.get());
        ZuluEntities.daylitGroundPlacement(event, GORILLA.get());
        ZuluEntities.daylitGroundPlacement(event, RHINO.get());
        ZuluEntities.daylitGroundPlacement(event, ALLIGATOR.get());
        ZuluEntities.daylitGroundPlacement(event, GIRAFFE.get());
        ZuluEntities.daylitGroundPlacement(event, ELEPHANT.get());
        rareDaylitPlacement(event, MAMMOTH.get(), 7);
        rareDaylitPlacement(event, TREE_ENT.get(), 7);
    }

    private static <T extends Entity> void rareDaylitPlacement(RegisterSpawnPlacementsEvent event, EntityType<T> type, int secondarySpawnRate) {
        event.register(type, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                (t, level, reason, pos, random) -> secondarySpawnRate - random.nextInt(100) >= 0
                        && level.getBrightness(LightLayer.BLOCK, pos) < 1 && level.canSeeSky(pos),
                RegisterSpawnPlacementsEvent.Operation.REPLACE);
    }

    public static void register(IEventBus modBus) {
        ENTITIES.register(modBus);
        ITEMS.register(modBus);
        SOUNDS.register(modBus);
        modBus.addListener(ZuluBigAnimals::attributes);
        modBus.addListener(ZuluBigAnimals::spawnPlacements);
    }
}
