package natura;

import natura.entity.BabyHeatscarSpider;
import natura.entity.BloodwoodBoat;
import natura.entity.HeatscarSpider;
import natura.entity.ImpEntity;
import natura.entity.NitroCreeper;
import natura.item.ImpMeatItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.vehicle.boat.Boat;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BoatItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

/**
 * The mobs of Natura (NContent.createEntities): Imp, Heatscar Spider ("FlameSpider"), Nitro Creeper, Baby Heatscar
 * Spider ("FlameSpiderBaby"), their spawn eggs (SpawnEgg 0-3), imp meat (ImpMeat 0 raw / 1 cooked) and the four boats
 * of entity/boats (BloodBoat, EucalyptusBoat, RedwoodBoat, WhiteWoodBoat; in 2.1.5.1 they were never registered).
 * Spawns, drops, recipes: tools/gen.py (biome modifiers, entity loot tables).
 */
public final class NaturaEntities {
    private static final DeferredRegister.Entities ENTITIES = DeferredRegister.createEntities(Natura.MOD_ID);

    /** Breeds imps (BowlStew damage 13 and up): fusewood mushroom stew and the glowshroom stews. */
    public static final TagKey<Item> IMP_FOOD = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(Natura.MOD_ID, "imp_food"));
    /** Tempts imps (any BowlStew). */
    public static final TagKey<Item> IMP_TEMPT_ITEMS = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(Natura.MOD_ID, "imp_tempt_items"));

    // ---- mobs (registerModEntity: tracking range 32 / 64, update interval 5) ----
    public static final DeferredHolder<EntityType<?>, EntityType<ImpEntity>> IMP = entity("imp", ImpEntity::new, MobCategory.CREATURE,
            b -> b.sized(0.9F, 0.9F).fireImmune().clientTrackingRange(2).updateInterval(5));
    public static final DeferredHolder<EntityType<?>, EntityType<HeatscarSpider>> HEATSCAR_SPIDER = entity("heatscar_spider", HeatscarSpider::new,
            MobCategory.MONSTER, b -> b.sized(2.7F, 1.9F).fireImmune().notInPeaceful().clientTrackingRange(2).updateInterval(5));
    public static final DeferredHolder<EntityType<?>, EntityType<NitroCreeper>> NITRO_CREEPER = entity("nitro_creeper", NitroCreeper::new,
            MobCategory.MONSTER, b -> b.sized(0.6F, 1.7F).fireImmune().notInPeaceful().clientTrackingRange(4).updateInterval(5));
    public static final DeferredHolder<EntityType<?>, EntityType<BabyHeatscarSpider>> BABY_HEATSCAR_SPIDER = entity("baby_heatscar_spider",
            BabyHeatscarSpider::new, MobCategory.MONSTER, b -> b.sized(0.9F, 0.7F).fireImmune().notInPeaceful().clientTrackingRange(2).updateInterval(5));

    // ---- boats (the 1.5.2 boat size 1.5 x 0.6; 26.3 boats are 1.375 x 0.5625 and use those) ----
    public static final DeferredHolder<EntityType<?>, EntityType<Boat>> REDWOOD_BOAT_ENTITY = boatEntity("redwood_boat",
            item -> (type, level) -> new Boat(type, level, item), false);
    public static final DeferredHolder<EntityType<?>, EntityType<Boat>> BLOODWOOD_BOAT_ENTITY = boatEntity("bloodwood_boat",
            item -> (type, level) -> new BloodwoodBoat(type, level, item), true);
    public static final DeferredHolder<EntityType<?>, EntityType<Boat>> GHOSTWOOD_BOAT_ENTITY = boatEntity("ghostwood_boat",
            item -> (type, level) -> new Boat(type, level, item), false);
    public static final DeferredHolder<EntityType<?>, EntityType<Boat>> EUCALYPTUS_BOAT_ENTITY = boatEntity("eucalyptus_boat",
            item -> (type, level) -> new Boat(type, level, item), false);

    // ---- items ----
    /** ImpMeat: raw 3 food / 0.2 saturation, cooked 8 / 0.6 (NSpecialFood: 32 ticks, not always edible). */
    public static final DeferredItem<Item> RAW_IMP_MEAT = item("raw_imp_meat", p -> new ImpMeatItem(p, false),
            p -> p.food(new FoodProperties.Builder().nutrition(3).saturationModifier(0.2F).build()));
    public static final DeferredItem<Item> COOKED_IMP_MEAT = item("cooked_imp_meat", p -> new ImpMeatItem(p, true),
            p -> p.food(new FoodProperties.Builder().nutrition(8).saturationModifier(0.6F).build()));

    // boat items: "# #", "###" of the planks; the woods that burn are fuel like vanilla boats
    public static final DeferredItem<Item> REDWOOD_BOAT = item("redwood_boat", p -> new BoatItem(REDWOOD_BOAT_ENTITY.get(), p), boat(true));
    public static final DeferredItem<Item> BLOODWOOD_BOAT = item("bloodwood_boat", p -> new BoatItem(BLOODWOOD_BOAT_ENTITY.get(), p), boat(false));
    public static final DeferredItem<Item> GHOSTWOOD_BOAT = item("ghostwood_boat", p -> new BoatItem(GHOSTWOOD_BOAT_ENTITY.get(), p), boat(false));
    public static final DeferredItem<Item> EUCALYPTUS_BOAT = item("eucalyptus_boat", p -> new BoatItem(EUCALYPTUS_BOAT_ENTITY.get(), p), boat(true));

    // spawn eggs (SpawnEgg 0-3); colours in the item models (tools/gen.py)
    public static final DeferredItem<Item> IMP_SPAWN_EGG = item("imp_spawn_egg", SpawnEggItem::new, p -> p.spawnEgg(IMP.get()));
    public static final DeferredItem<Item> HEATSCAR_SPIDER_SPAWN_EGG = item("heatscar_spider_spawn_egg", SpawnEggItem::new,
            p -> p.spawnEgg(HEATSCAR_SPIDER.get()));
    public static final DeferredItem<Item> NITRO_CREEPER_SPAWN_EGG = item("nitro_creeper_spawn_egg", SpawnEggItem::new,
            p -> p.spawnEgg(NITRO_CREEPER.get()));
    public static final DeferredItem<Item> BABY_HEATSCAR_SPIDER_SPAWN_EGG = item("baby_heatscar_spider_spawn_egg", SpawnEggItem::new,
            p -> p.spawnEgg(BABY_HEATSCAR_SPIDER.get()));

    private NaturaEntities() {
    }

    private static <E extends Entity> DeferredHolder<EntityType<?>, EntityType<E>> entity(String name, EntityType.EntityFactory<E> factory,
            MobCategory category, UnaryOperator<EntityType.Builder<E>> builder) {
        return ENTITIES.registerEntityType(name, factory, category, builder);
    }

    /** The boat entity takes its item lazily (the item is registered after the entity types). */
    private static DeferredHolder<EntityType<?>, EntityType<Boat>> boatEntity(String name,
            Function<Supplier<Item>, EntityType.EntityFactory<Boat>> factory, boolean fireImmune) {
        Supplier<Item> item = DeferredItem.createItem(Identifier.fromNamespaceAndPath(Natura.MOD_ID, name));
        return entity(name, factory.apply(item), MobCategory.MISC, b -> {
            EntityType.Builder<Boat> boat = b.noLootTable().sized(1.375F, 0.5625F).eyeHeight(0.5625F).clientTrackingRange(10);
            return fireImmune ? boat.fireImmune() : boat;
        });
    }

    private static UnaryOperator<Item.Properties> boat(boolean burns) {
        return p -> burns ? p.stacksTo(1).cookingFuel(ContextIntProviders.COOKING_TIME_BOATS) : p.stacksTo(1);
    }

    private static DeferredItem<Item> item(String name, Function<Item.Properties, Item> factory, UnaryOperator<Item.Properties> properties) {
        DeferredItem<Item> item = NaturaBlocks.ITEMS.registerItem(name, factory, properties);
        NaturaBlocks.TAB_ITEMS.add(() -> new ItemStack(item.get()));
        return item;
    }

    private static void attributes(EntityAttributeCreationEvent event) {
        event.put(IMP.get(), ImpEntity.createAttributes().build());
        event.put(HEATSCAR_SPIDER.get(), HeatscarSpider.createAttributes().build());
        event.put(NITRO_CREEPER.get(), NitroCreeper.createAttributes().build());
        event.put(BABY_HEATSCAR_SPIDER.get(), BabyHeatscarSpider.createAttributes().build());
    }

    /**
     * getCanSpawnHere of the original. Imp: only in the Nether (WorldProviderHell), no grass or light needed.
     * Heatscar spiders: any light, not on peaceful. Nitro creeper: EntityMob, dark enough.
     */
    private static void spawnPlacements(RegisterSpawnPlacementsEvent event) {
        event.register(IMP.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                (type, level, reason, pos, random) -> level.getLevel().dimension() == Level.NETHER, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(HEATSCAR_SPIDER.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Monster::checkAnyLightMonsterSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(BABY_HEATSCAR_SPIDER.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Monster::checkAnyLightMonsterSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(NITRO_CREEPER.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Monster::checkMonsterSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
    }

    public static void register(IEventBus modBus) {
        ENTITIES.register(modBus);
        modBus.addListener(NaturaEntities::attributes);
        modBus.addListener(NaturaEntities::spawnPlacements);
    }
}
