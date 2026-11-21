package twilightforest;

import hexxit.worldgen152.OldGameIds;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.food.FoodProperties;
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
import net.minecraft.world.entity.monster.Monster;
import twilightforest.entity.BlockGoblin;
import twilightforest.entity.DeathTome;
import twilightforest.entity.GoblinKnightLower;
import twilightforest.entity.GoblinKnightUpper;
import twilightforest.entity.HelmetCrab;
import twilightforest.entity.MiniGhast;
import twilightforest.entity.TowerBroodling;
import twilightforest.entity.TowerGhast;
import twilightforest.entity.TowerGolem;
import twilightforest.entity.TowerTermite;
import twilightforest.entity.FireBeetle;
import twilightforest.entity.HedgeSpider;
import twilightforest.entity.HostileWolf;
import twilightforest.entity.KingSpider;
import twilightforest.entity.Kobold;
import twilightforest.entity.MazeSlime;
import twilightforest.entity.Minotaur;
import twilightforest.entity.MistWolf;
import twilightforest.entity.MosquitoSwarm;
import twilightforest.entity.PinchBeetle;
import twilightforest.entity.Redcap;
import twilightforest.entity.RedcapSapper;
import twilightforest.entity.SkeletonDruid;
import twilightforest.entity.SlimeBeetle;
import twilightforest.entity.SwarmSpider;
import twilightforest.entity.Wraith;
import twilightforest.entity.projectile.NatureBolt;
import twilightforest.entity.projectile.SlimeBlob;
import twilightforest.entity.projectile.TomeBolt;
import twilightforest.entity.passive.BighornSheep;
import twilightforest.entity.passive.Bunny;
import twilightforest.entity.passive.MobileFirefly;
import twilightforest.entity.passive.Penguin;
import twilightforest.entity.passive.QuestRam;
import twilightforest.entity.passive.Raven;
import twilightforest.entity.passive.SmallCritter;
import twilightforest.entity.passive.Squirrel;
import twilightforest.entity.passive.TinyBird;
import twilightforest.entity.passive.WildBoar;
import twilightforest.entity.passive.WildDeer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

/**
 * The creatures of the Twilight Forest (TFCreatures.registerTFCreature: tracking range 80, update every 3 ticks), their
 * spawn eggs (ItemTFSpawnEgg, item 7309 of the pack with the mob id as damage), sounds and the items they drop. The
 * bosses and their parts come with Teil 24f.
 */
public final class TFEntities {
    private static final DeferredRegister.Entities ENTITIES = DeferredRegister.createEntities(TwilightForest.MOD_ID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(TwilightForest.MOD_ID);
    private static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, TwilightForest.MOD_ID);

    /** Spawn eggs and drops for the creative tab, in registration order. */
    public static final List<DeferredItem<Item>> TAB_ITEMS = new ArrayList<>();
    /** (old mob id, old spawner name, entity id) for OldGameIds. */
    private static final List<Object[]> OLD = new ArrayList<>();
    private static final List<Object[]> OLD_EGGS = new ArrayList<>();
    private static final List<Object[]> OLD_ITEMS = new ArrayList<>();

    // ---------------------------------------------------------------- animals
    public static final DeferredHolder<EntityType<?>, EntityType<WildBoar>> WILD_BOAR = mob("wild_boar", 177, "Wild Boar",
            WildBoar::new, MobCategory.CREATURE, b -> b.sized(0.9F, 0.9F));
    public static final DeferredHolder<EntityType<?>, EntityType<BighornSheep>> BIGHORN_SHEEP = mob("bighorn_sheep", 178, "Bighorn Sheep",
            BighornSheep::new, MobCategory.CREATURE, b -> b.sized(0.9F, 1.3F));
    public static final DeferredHolder<EntityType<?>, EntityType<WildDeer>> WILD_DEER = mob("wild_deer", 179, "Wild Deer",
            WildDeer::new, MobCategory.CREATURE, b -> b.sized(0.7F, 2.3F));
    public static final DeferredHolder<EntityType<?>, EntityType<Penguin>> PENGUIN = mob("penguin", 191, "Penguin",
            Penguin::new, MobCategory.CREATURE, b -> b.sized(0.5F, 0.9F));
    public static final DeferredHolder<EntityType<?>, EntityType<TinyBird>> TINY_BIRD = mob("tiny_bird", 194, "Tiny Bird",
            TinyBird::new, MobCategory.CREATURE, b -> b.sized(0.5F, 0.9F));
    public static final DeferredHolder<EntityType<?>, EntityType<Squirrel>> SQUIRREL = mob("squirrel", 195, "Forest Squirrel",
            Squirrel::new, MobCategory.CREATURE, b -> b.sized(0.3F, 0.7F));
    public static final DeferredHolder<EntityType<?>, EntityType<Bunny>> BUNNY = mob("bunny", 196, "Forest Bunny",
            Bunny::new, MobCategory.CREATURE, b -> b.sized(0.3F, 0.7F));
    public static final DeferredHolder<EntityType<?>, EntityType<Raven>> RAVEN = mob("raven", 197, "Forest Raven",
            Raven::new, MobCategory.CREATURE, b -> b.sized(0.3F, 0.7F));
    public static final DeferredHolder<EntityType<?>, EntityType<QuestRam>> QUEST_RAM = mob("quest_ram", 198, "Questing Ram",
            QuestRam::new, MobCategory.CREATURE, b -> b.sized(1.25F, 2.9F));
    public static final DeferredHolder<EntityType<?>, EntityType<MobileFirefly>> FIREFLY = mob("firefly", 213, "Firefly",
            MobileFirefly::new, MobCategory.AMBIENT, b -> b.sized(0.5F, 0.5F));

    // ---------------------------------------------------------------- monsters of the forest, the hills and the labyrinth
    public static final DeferredHolder<EntityType<?>, EntityType<Redcap>> REDCAP = mob("redcap", 180, "Redcap",
            Redcap::new, MobCategory.MONSTER, b -> b.sized(0.9F, 1.4F));
    public static final DeferredHolder<EntityType<?>, EntityType<SwarmSpider>> SWARM_SPIDER = mob("swarm_spider", 181, "Swarm Spider",
            SwarmSpider::new, MobCategory.MONSTER, b -> b.sized(0.8F, 0.4F).eyeHeight(0.26F));
    public static final DeferredHolder<EntityType<?>, EntityType<SkeletonDruid>> SKELETON_DRUID = mob("skeleton_druid", 184, "Skeleton Druid",
            SkeletonDruid::new, MobCategory.MONSTER, b -> b.sized(0.6F, 1.8F));
    public static final DeferredHolder<EntityType<?>, EntityType<HostileWolf>> HOSTILE_WOLF = mob("hostile_wolf", 185, "Hostile Wolf",
            HostileWolf::new, MobCategory.MONSTER, b -> b.sized(0.6F, 0.8F).notInPeaceful());
    public static final DeferredHolder<EntityType<?>, EntityType<Wraith>> WRAITH = mob("wraith", 186, "Twilight Wraith",
            Wraith::new, MobCategory.MONSTER, b -> b.sized(0.6F, 1.8F).notInPeaceful());
    public static final DeferredHolder<EntityType<?>, EntityType<HedgeSpider>> HEDGE_SPIDER = mob("hedge_spider", 187, "Hedge Spider",
            HedgeSpider::new, MobCategory.MONSTER, b -> b.sized(1.4F, 0.9F).eyeHeight(0.65F));
    public static final DeferredHolder<EntityType<?>, EntityType<Kobold>> KOBOLD = mob("kobold", 199, "Twilight Kobold",
            Kobold::new, MobCategory.MONSTER, b -> b.sized(0.8F, 1.1F));
    public static final DeferredHolder<EntityType<?>, EntityType<MosquitoSwarm>> MOSQUITO_SWARM = mob("mosquito_swarm", 202, "Mosquito Swarm",
            MosquitoSwarm::new, MobCategory.MONSTER, b -> b.sized(0.7F, 1.9F));
    public static final DeferredHolder<EntityType<?>, EntityType<DeathTome>> DEATH_TOME = mob("death_tome", 203, "Death Tome",
            DeathTome::new, MobCategory.MONSTER, b -> b.sized(0.6F, 1.8F));
    public static final DeferredHolder<EntityType<?>, EntityType<Minotaur>> MINOTAUR = mob("minotaur", 204, "Minotaur",
            Minotaur::new, MobCategory.MONSTER, b -> b.sized(0.6F, 1.8F));
    public static final DeferredHolder<EntityType<?>, EntityType<FireBeetle>> FIRE_BEETLE = mob("fire_beetle", 206, "Fire Beetle",
            FireBeetle::new, MobCategory.MONSTER, b -> b.sized(1.1F, 0.75F).eyeHeight(0.25F));
    public static final DeferredHolder<EntityType<?>, EntityType<SlimeBeetle>> SLIME_BEETLE = mob("slime_beetle", 207, "Slime Beetle",
            SlimeBeetle::new, MobCategory.MONSTER, b -> b.sized(0.9F, 1.75F).eyeHeight(0.25F));
    public static final DeferredHolder<EntityType<?>, EntityType<PinchBeetle>> PINCH_BEETLE = mob("pinch_beetle", 208, "Pinch Beetle",
            PinchBeetle::new, MobCategory.MONSTER, b -> b.sized(1.2F, 1.1F).eyeHeight(0.25F));
    public static final DeferredHolder<EntityType<?>, EntityType<MazeSlime>> MAZE_SLIME = mob("maze_slime", 209, "Maze Slime",
            MazeSlime::new, MobCategory.MONSTER, b -> b.sized(0.52F, 0.52F).eyeHeight(0.325F).spawnDimensionsScale(4.0F));
    public static final DeferredHolder<EntityType<?>, EntityType<RedcapSapper>> REDCAP_SAPPER = mob("redcap_sapper", 210, "Redcap Sapper",
            RedcapSapper::new, MobCategory.MONSTER, b -> b.sized(0.9F, 1.4F));
    public static final DeferredHolder<EntityType<?>, EntityType<MistWolf>> MIST_WOLF = mob("mist_wolf", 211, "Mist Wolf",
            MistWolf::new, MobCategory.MONSTER, b -> b.sized(1.4F, 1.9F).notInPeaceful());
    public static final DeferredHolder<EntityType<?>, EntityType<KingSpider>> KING_SPIDER = mob("king_spider", 212, "King Spider",
            KingSpider::new, MobCategory.MONSTER, b -> b.sized(1.6F, 1.6F).eyeHeight(1.2F));

    // ---------------------------------------------------------------- the dark tower and the knight stronghold
    public static final DeferredHolder<EntityType<?>, EntityType<MiniGhast>> MINI_GHAST = mob("mini_ghast", 214, "Mini Ghast",
            MiniGhast::new, MobCategory.MONSTER, b -> b.sized(1.1F, 1.5F).fireImmune().notInPeaceful());
    public static final DeferredHolder<EntityType<?>, EntityType<TowerGhast>> TOWER_GHAST = mob("tower_ghast", 215, "Tower Ghast",
            TowerGhast::new, MobCategory.MONSTER, b -> b.sized(4.0F, 6.0F).fireImmune().notInPeaceful());
    public static final DeferredHolder<EntityType<?>, EntityType<TowerGolem>> TOWER_GOLEM = mob("tower_golem", 216, "Tower Golem",
            TowerGolem::new, MobCategory.MONSTER, b -> b.sized(1.4F, 2.9F));
    public static final DeferredHolder<EntityType<?>, EntityType<TowerTermite>> TOWER_TERMITE = mob("tower_termite", 218, "Tower Termite",
            TowerTermite::new, MobCategory.MONSTER, b -> b.sized(0.3F, 0.7F));
    public static final DeferredHolder<EntityType<?>, EntityType<TowerBroodling>> TOWER_BROODLING = mob("tower_broodling", 219, "Redscale Broodling",
            TowerBroodling::new, MobCategory.MONSTER, b -> b.sized(0.8F, 0.4F).eyeHeight(0.26F));
    public static final DeferredHolder<EntityType<?>, EntityType<BlockGoblin>> BLOCK_GOBLIN = mob("block_goblin", 220, "Block&Chain Goblin",
            BlockGoblin::new, MobCategory.MONSTER, b -> b.sized(0.9F, 1.4F));
    public static final DeferredHolder<EntityType<?>, EntityType<GoblinKnightUpper>> UPPER_GOBLIN_KNIGHT = mob("upper_goblin_knight", 221,
            "Upper Goblin Knight", GoblinKnightUpper::new, MobCategory.MONSTER, b -> b.sized(1.1F, 1.3F));
    public static final DeferredHolder<EntityType<?>, EntityType<GoblinKnightLower>> LOWER_GOBLIN_KNIGHT = mob("lower_goblin_knight", 222,
            "Lower Goblin Knight", GoblinKnightLower::new, MobCategory.MONSTER, b -> b.sized(0.7F, 1.1F));
    public static final DeferredHolder<EntityType<?>, EntityType<HelmetCrab>> HELMET_CRAB = mob("helmet_crab", 223, "Helmet Crab",
            HelmetCrab::new, MobCategory.MONSTER, b -> b.sized(0.8F, 1.1F));

    // ---------------------------------------------------------------- projectiles of the mobs
    public static final DeferredHolder<EntityType<?>, EntityType<NatureBolt>> NATURE_BOLT = projectile("nature_bolt", NatureBolt::new, 5);
    public static final DeferredHolder<EntityType<?>, EntityType<TomeBolt>> TOME_BOLT = projectile("tome_bolt", TomeBolt::new, 5);
    public static final DeferredHolder<EntityType<?>, EntityType<SlimeBlob>> SLIME_BLOB = projectile("slime_blob", SlimeBlob::new, 3);
    // Teil 24f: the scepters and the moonworm queen (registerModEntity 150, 2 / 150, 3), the charm effect (80, 3)
    public static final DeferredHolder<EntityType<?>, EntityType<twilightforest.entity.projectile.TwilightWandBolt>> TWILIGHT_WAND_BOLT =
            projectile("twilight_wand_bolt", twilightforest.entity.projectile.TwilightWandBolt::new, 5);
    public static final DeferredHolder<EntityType<?>, EntityType<twilightforest.entity.projectile.MoonwormShot>> MOONWORM_SHOT =
            projectile("moonworm_shot", twilightforest.entity.projectile.MoonwormShot::new, 5);
    public static final DeferredHolder<EntityType<?>, EntityType<twilightforest.entity.CharmEffect>> CHARM_EFFECT = ENTITIES.registerEntityType(
            "charm_effect", twilightforest.entity.CharmEffect::new, MobCategory.MISC,
            b -> b.sized(0.25F, 0.25F).clientTrackingRange(5).updateInterval(3).noSave().noSummon());
    public static final DeferredHolder<EntityType<?>, EntityType<twilightforest.entity.LoyalZombie>> LOYAL_ZOMBIE = ENTITIES.registerEntityType(
            "loyal_zombie", twilightforest.entity.LoyalZombie::new, MobCategory.CREATURE, b -> b.sized(0.6F, 1.8F).clientTrackingRange(5).updateInterval(3));

    // ---------------------------------------------------------------- spawn eggs (TwilightForestMod.registerCreatures colours)
    public static final DeferredItem<Item> WILD_BOAR_SPAWN_EGG = egg("wild_boar", 177, WILD_BOAR);
    public static final DeferredItem<Item> BIGHORN_SHEEP_SPAWN_EGG = egg("bighorn_sheep", 178, BIGHORN_SHEEP);
    public static final DeferredItem<Item> WILD_DEER_SPAWN_EGG = egg("wild_deer", 179, WILD_DEER);
    public static final DeferredItem<Item> PENGUIN_SPAWN_EGG = egg("penguin", 191, PENGUIN);
    public static final DeferredItem<Item> TINY_BIRD_SPAWN_EGG = egg("tiny_bird", 194, TINY_BIRD);
    public static final DeferredItem<Item> SQUIRREL_SPAWN_EGG = egg("squirrel", 195, SQUIRREL);
    public static final DeferredItem<Item> BUNNY_SPAWN_EGG = egg("bunny", 196, BUNNY);
    public static final DeferredItem<Item> RAVEN_SPAWN_EGG = egg("raven", 197, RAVEN);
    public static final DeferredItem<Item> FIREFLY_SPAWN_EGG = egg("firefly", 213, FIREFLY);
    public static final DeferredItem<Item> REDCAP_SPAWN_EGG = egg("redcap", 180, REDCAP);
    public static final DeferredItem<Item> SWARM_SPIDER_SPAWN_EGG = egg("swarm_spider", 181, SWARM_SPIDER);
    public static final DeferredItem<Item> SKELETON_DRUID_SPAWN_EGG = egg("skeleton_druid", 184, SKELETON_DRUID);
    public static final DeferredItem<Item> HOSTILE_WOLF_SPAWN_EGG = egg("hostile_wolf", 185, HOSTILE_WOLF);
    public static final DeferredItem<Item> WRAITH_SPAWN_EGG = egg("wraith", 186, WRAITH);
    public static final DeferredItem<Item> HEDGE_SPIDER_SPAWN_EGG = egg("hedge_spider", 187, HEDGE_SPIDER);
    public static final DeferredItem<Item> KOBOLD_SPAWN_EGG = egg("kobold", 199, KOBOLD);
    public static final DeferredItem<Item> MOSQUITO_SWARM_SPAWN_EGG = egg("mosquito_swarm", 202, MOSQUITO_SWARM);
    public static final DeferredItem<Item> DEATH_TOME_SPAWN_EGG = egg("death_tome", 203, DEATH_TOME);
    public static final DeferredItem<Item> MINOTAUR_SPAWN_EGG = egg("minotaur", 204, MINOTAUR);
    public static final DeferredItem<Item> FIRE_BEETLE_SPAWN_EGG = egg("fire_beetle", 206, FIRE_BEETLE);
    public static final DeferredItem<Item> SLIME_BEETLE_SPAWN_EGG = egg("slime_beetle", 207, SLIME_BEETLE);
    public static final DeferredItem<Item> PINCH_BEETLE_SPAWN_EGG = egg("pinch_beetle", 208, PINCH_BEETLE);
    public static final DeferredItem<Item> MAZE_SLIME_SPAWN_EGG = egg("maze_slime", 209, MAZE_SLIME);
    public static final DeferredItem<Item> REDCAP_SAPPER_SPAWN_EGG = egg("redcap_sapper", 210, REDCAP_SAPPER);
    public static final DeferredItem<Item> MIST_WOLF_SPAWN_EGG = egg("mist_wolf", 211, MIST_WOLF);
    public static final DeferredItem<Item> KING_SPIDER_SPAWN_EGG = egg("king_spider", 212, KING_SPIDER);
    public static final DeferredItem<Item> MINI_GHAST_SPAWN_EGG = egg("mini_ghast", 214, MINI_GHAST);
    public static final DeferredItem<Item> TOWER_GHAST_SPAWN_EGG = egg("tower_ghast", 215, TOWER_GHAST);
    public static final DeferredItem<Item> TOWER_GOLEM_SPAWN_EGG = egg("tower_golem", 216, TOWER_GOLEM);
    public static final DeferredItem<Item> TOWER_TERMITE_SPAWN_EGG = egg("tower_termite", 218, TOWER_TERMITE);
    public static final DeferredItem<Item> TOWER_BROODLING_SPAWN_EGG = egg("tower_broodling", 219, TOWER_BROODLING);
    public static final DeferredItem<Item> BLOCK_GOBLIN_SPAWN_EGG = egg("block_goblin", 220, BLOCK_GOBLIN);
    public static final DeferredItem<Item> LOWER_GOBLIN_KNIGHT_SPAWN_EGG = egg("lower_goblin_knight", 222, LOWER_GOBLIN_KNIGHT);
    public static final DeferredItem<Item> HELMET_CRAB_SPAWN_EGG = egg("helmet_crab", 223, HELMET_CRAB);

    // ---------------------------------------------------------------- drops (TFItems; ids of the pack's TwilightForest.cfg + 256)
    public static final DeferredItem<Item> RAVEN_FEATHER = item("raven_feather", 7264, p -> p.rarity(net.minecraft.world.item.Rarity.UNCOMMON));
    public static final DeferredItem<Item> VENISON = item("venison", 7324, p -> p.food(food(3, 0.3F)));
    public static final DeferredItem<Item> COOKED_VENISON = item("cooked_venison", 7323, p -> p.food(food(8, 0.8F)));
    public static final DeferredItem<Item> RAW_MEEF = item("raw_meef", 7295, p -> p.food(food(2, 0.3F)));
    public static final DeferredItem<Item> MEEF_STEAK = item("meef_steak", 7296, p -> p.food(food(6, 0.6F)));
    /** Crafting parts of the magic map and the maze map (Teil 24f). */
    public static final DeferredItem<Item> MAGIC_MAP_FOCUS = item("magic_map_focus", 7290, p -> p.rarity(net.minecraft.world.item.Rarity.UNCOMMON));
    public static final DeferredItem<Item> MAZE_MAP_FOCUS = item("maze_map_focus", 7292, p -> p.rarity(net.minecraft.world.item.Rarity.UNCOMMON));
    /** Crafting part of the ore meter and the transformation powder (Teil 24f). */
    public static final DeferredItem<Item> BORER_ESSENCE = item("borer_essence", 7252, p -> p.rarity(net.minecraft.world.item.Rarity.UNCOMMON));

    // ---------------------------------------------------------------- sounds (TFSounds)
    public static final DeferredHolder<SoundEvent, SoundEvent> TINY_BIRD_CHIRP = sound("tiny_bird.chirp");
    public static final DeferredHolder<SoundEvent, SoundEvent> TINY_BIRD_HURT = sound("tiny_bird.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> RAVEN_CAW = sound("raven.caw");
    public static final DeferredHolder<SoundEvent, SoundEvent> RAVEN_SQUAWK = sound("raven.squawk");
    public static final DeferredHolder<SoundEvent, SoundEvent> REDCAP_AMBIENT = sound("redcap.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> REDCAP_HURT = sound("redcap.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> REDCAP_DIE = sound("redcap.die");
    public static final DeferredHolder<SoundEvent, SoundEvent> KOBOLD_AMBIENT = sound("kobold.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> KOBOLD_HURT = sound("kobold.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> KOBOLD_DIE = sound("kobold.die");
    public static final DeferredHolder<SoundEvent, SoundEvent> WRAITH_SOUND = sound("wraith");
    public static final DeferredHolder<SoundEvent, SoundEvent> MOSQUITO_SOUND = sound("mosquito");

    private TFEntities() {
    }

    private static <E extends Entity> DeferredHolder<EntityType<?>, EntityType<E>> mob(String name, int oldId, String oldName,
            EntityType.EntityFactory<E> factory, MobCategory category, UnaryOperator<EntityType.Builder<E>> builder) {
        OLD.add(new Object[]{oldId, oldName, TwilightForest.MOD_ID + ":" + name});
        return ENTITIES.registerEntityType(name, factory, category, b -> builder.apply(b.clientTrackingRange(5).updateInterval(3)));
    }

    /** Projectiles: tracking range 150 (here 9 chunks). */
    static <E extends Entity> DeferredHolder<EntityType<?>, EntityType<E>> projectile(String name, EntityType.EntityFactory<E> factory,
            int updateInterval) {
        return ENTITIES.registerEntityType(name, factory, MobCategory.MISC,
                b -> b.sized(0.25F, 0.25F).clientTrackingRange(9).updateInterval(updateInterval));
    }

    /** ItemFood(food, saturation): the same saturation modifier as today (saturation = food × modifier × 2). */
    private static FoodProperties food(int nutrition, float saturation) {
        return new FoodProperties.Builder().nutrition(nutrition).saturationModifier(saturation).build();
    }

    private static DeferredItem<Item> egg(String name, int oldId, DeferredHolder<EntityType<?>, ? extends EntityType<?>> type) {
        DeferredItem<Item> egg = ITEMS.registerItem(name + "_spawn_egg", SpawnEggItem::new, p -> p.spawnEgg(type.get()));
        TAB_ITEMS.add(egg);
        OLD_EGGS.add(new Object[]{oldId, egg});
        return egg;
    }

    private static DeferredItem<Item> item(String name, int oldId, UnaryOperator<Item.Properties> properties) {
        DeferredItem<Item> item = ITEMS.registerItem(name, Item::new, properties);
        TAB_ITEMS.add(item);
        OLD_ITEMS.add(new Object[]{oldId + 256, item});
        return item;
    }

    private static DeferredHolder<SoundEvent, SoundEvent> sound(String name) {
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(TwilightForest.id(name)));
    }

    public static void register(IEventBus modBus) {
        // breaking infested tower wood lets a termite out (BlockTFTowerWood.onBlockDestroyedByPlayer)
        twilightforest.block.TowerWoodBlock.TERMITE = (level, pos) -> {
            TowerTermite termite = TOWER_TERMITE.get().create(level, net.minecraft.world.entity.EntitySpawnReason.TRIGGERED);
            if (termite != null) {
                termite.snapTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0.0F, 0.0F);
                level.addFreshEntity(termite);
                termite.spawnAnim();
            }
        };
        ENTITIES.register(modBus);
        ITEMS.register(modBus);
        SOUNDS.register(modBus);
        modBus.addListener(TFEntities::attributes);
        modBus.addListener(TFEntities::spawnPlacements);
    }

    /** The old names (spawners, other mods' lists) and item ids for worldgen152. */
    public static void registerOld() {
        for (Object[] old : OLD) {
            OldGameIds.registerEntity(twilightforest.gen.TFTreasure.SPAWNER_PREFIX + old[1], (String) old[2]);
        }
        // the ram of the quest grove (Teil 24d); it keeps its home at the grove itself (QuestRam.customServerAiStep)
        OldGameIds.registerEntity(twilightforest.gen.structure.ComponentTFQuestGrove.QUEST_RAM, QUEST_RAM.getId().toString());
        for (Object[] old : OLD_EGGS) {
            @SuppressWarnings("unchecked")
            DeferredItem<Item> egg = (DeferredItem<Item>) old[1];
            OldGameIds.registerItem(7309 + 256, (Integer) old[0], egg.getId().toString());
        }
        for (Object[] old : OLD_ITEMS) {
            @SuppressWarnings("unchecked")
            DeferredItem<Item> item = (DeferredItem<Item>) old[1];
            OldGameIds.registerItem((Integer) old[0], item.getId().toString());
        }
    }

    private static void attributes(EntityAttributeCreationEvent event) {
        // EntityPig / EntitySheep / EntityCow: 10 health, AI speed 0.25 (vanilla values of today)
        event.put(WILD_BOAR.get(), net.minecraft.world.entity.animal.pig.Pig.createAttributes().build());
        event.put(BIGHORN_SHEEP.get(), net.minecraft.world.entity.animal.sheep.Sheep.createAttributes().build());
        event.put(WILD_DEER.get(), net.minecraft.world.entity.animal.cow.Cow.createAttributes().build());
        event.put(PENGUIN.get(), Penguin.createAttributes().build());
        event.put(TINY_BIRD.get(), TinyBird.createAttributes().build());
        event.put(SQUIRREL.get(), SmallCritter.createAttributes().build());
        event.put(BUNNY.get(), SmallCritter.createAttributes().build());
        event.put(RAVEN.get(), Raven.createAttributes().build());
        event.put(QUEST_RAM.get(), QuestRam.createAttributes().build());
        event.put(FIREFLY.get(), MobileFirefly.createAttributes().build());
        event.put(REDCAP.get(), Redcap.createAttributes().build());
        event.put(REDCAP_SAPPER.get(), RedcapSapper.createAttributes().build());
        event.put(SWARM_SPIDER.get(), SwarmSpider.createAttributes().build());
        event.put(HEDGE_SPIDER.get(), net.minecraft.world.entity.monster.spider.Spider.createAttributes().build());
        event.put(KING_SPIDER.get(), KingSpider.createAttributes().build());
        event.put(SKELETON_DRUID.get(), SkeletonDruid.createAttributes().build());
        event.put(HOSTILE_WOLF.get(), HostileWolf.createAttributes().build());
        event.put(MIST_WOLF.get(), MistWolf.createAttributes().build());
        event.put(WRAITH.get(), Wraith.createAttributes().build());
        event.put(KOBOLD.get(), Kobold.createAttributes().build());
        event.put(MOSQUITO_SWARM.get(), MosquitoSwarm.createAttributes().build());
        event.put(DEATH_TOME.get(), DeathTome.createAttributes().build());
        event.put(MINOTAUR.get(), Minotaur.createAttributes().build());
        event.put(FIRE_BEETLE.get(), FireBeetle.createAttributes().build());
        event.put(SLIME_BEETLE.get(), SlimeBeetle.createAttributes().build());
        event.put(PINCH_BEETLE.get(), PinchBeetle.createAttributes().build());
        event.put(MAZE_SLIME.get(), net.minecraft.world.entity.monster.Monster.createMonsterAttributes().build());
        event.put(MINI_GHAST.get(), MiniGhast.createAttributes().build());
        event.put(TOWER_GHAST.get(), TowerGhast.createAttributes().build());
        event.put(TOWER_GOLEM.get(), TowerGolem.createAttributes().build());
        event.put(TOWER_TERMITE.get(), TowerTermite.createAttributes().build());
        event.put(TOWER_BROODLING.get(), TowerBroodling.createAttributes().build());
        event.put(BLOCK_GOBLIN.get(), BlockGoblin.createAttributes().build());
        event.put(UPPER_GOBLIN_KNIGHT.get(), GoblinKnightUpper.createAttributes().build());
        event.put(LOWER_GOBLIN_KNIGHT.get(), GoblinKnightLower.createAttributes().build());
        event.put(HELMET_CRAB.get(), HelmetCrab.createAttributes().build());
        event.put(LOYAL_ZOMBIE.get(), twilightforest.entity.LoyalZombie.createAttributes().build());
    }

    /**
     * getCanSpawnHere of 1.5.2. EntityAnimal: grass below and light above 8 (Animal::checkAnimalSpawnRules), plus the
     * path weight of EntityCreature (≥ 0); the squirrel and bunny only the path weight.
     */
    private static void spawnPlacements(RegisterSpawnPlacementsEvent event) {
        RegisterSpawnPlacementsEvent.Operation replace = RegisterSpawnPlacementsEvent.Operation.REPLACE;
        Heightmap.Types ground = Heightmap.Types.MOTION_BLOCKING_NO_LEAVES;
        event.register(WILD_BOAR.get(), SpawnPlacementTypes.ON_GROUND, ground, Animal::checkAnimalSpawnRules, replace);
        event.register(BIGHORN_SHEEP.get(), SpawnPlacementTypes.ON_GROUND, ground, Animal::checkAnimalSpawnRules, replace);
        event.register(WILD_DEER.get(), SpawnPlacementTypes.ON_GROUND, ground, Animal::checkAnimalSpawnRules, replace);
        event.register(PENGUIN.get(), SpawnPlacementTypes.ON_GROUND, ground, Animal::checkAnimalSpawnRules, replace);
        event.register(QUEST_RAM.get(), SpawnPlacementTypes.ON_GROUND, ground, Animal::checkAnimalSpawnRules, replace);
        event.register(TINY_BIRD.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING,
                (t, level, reason, pos, random) -> Animal.checkAnimalSpawnRules(t, level, reason, pos, random)
                        && SmallCritter.weight(level, pos, 200.0F, 15.0F, 9.0F) >= 0.0F, replace);
        event.register(RAVEN.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING,
                (t, level, reason, pos, random) -> Animal.checkAnimalSpawnRules(t, level, reason, pos, random)
                        && SmallCritter.weight(level, pos, 200.0F, 15.0F, 9.0F) >= 0.0F, replace);
        event.register(SQUIRREL.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING,
                (t, level, reason, pos, random) -> SmallCritter.weight(level, pos, 12.0F, 15.0F, 10.0F) >= 0.0F, replace);
        event.register(BUNNY.get(), SpawnPlacementTypes.ON_GROUND, ground,
                (t, level, reason, pos, random) -> SmallCritter.weight(level, pos, -1.0F, -1.0F, 10.0F) >= 0.0F, replace);
        event.register(FIREFLY.get(), SpawnPlacementTypes.NO_RESTRICTIONS, ground, MobileFirefly::checkSpawnRules, replace);
        // EntityMob: dark enough (isValidLightLevel); the spiders of the hedge maze in any light
        for (var type : List.of(REDCAP, REDCAP_SAPPER, SKELETON_DRUID, KOBOLD, DEATH_TOME, MINOTAUR, FIRE_BEETLE, SLIME_BEETLE,
                PINCH_BEETLE, WRAITH, TOWER_GOLEM, TOWER_TERMITE, BLOCK_GOBLIN, UPPER_GOBLIN_KNIGHT, LOWER_GOBLIN_KNIGHT, HELMET_CRAB, MINI_GHAST)) {
            monster(event, type.get());
        }
        for (var type : List.of(SWARM_SPIDER, HEDGE_SPIDER, KING_SPIDER, TOWER_BROODLING)) {
            event.register(type.get(), SpawnPlacementTypes.ON_GROUND, ground, (t, level, reason, pos, random) ->
                    Monster.checkAnyLightMonsterSpawnRules(t, level, reason, pos, random)
                            && (twilightforest.entity.TFMobUtil.nearestFeature(level, pos) == twilightforest.gen.TFFeature.HEDGE_MAZE
                            || Monster.isDarkEnoughToSpawn(level, pos, random)), replace);
        }
        event.register(HOSTILE_WOLF.get(), SpawnPlacementTypes.ON_GROUND, ground, HostileWolf::checkSpawnRules, replace);
        event.register(MIST_WOLF.get(), SpawnPlacementTypes.ON_GROUND, ground, HostileWolf::checkSpawnRules, replace);
        event.register(MAZE_SLIME.get(), SpawnPlacementTypes.ON_GROUND, ground, MazeSlime::checkSpawnRules, replace);
        // EntityTFTowerGhast: any light, not on peaceful, nothing in the way
        event.register(TOWER_GHAST.get(), SpawnPlacementTypes.NO_RESTRICTIONS, ground, Monster::checkAnyLightMonsterSpawnRules, replace);
        // EntityTFMosquitoSwarm: any light in the swamp
        event.register(MOSQUITO_SWARM.get(), SpawnPlacementTypes.ON_GROUND, ground, (t, level, reason, pos, random) ->
                Monster.checkAnyLightMonsterSpawnRules(t, level, reason, pos, random)
                        && (level.getBiome(pos).is(net.minecraft.resources.ResourceKey.create(Registries.BIOME, TwilightForest.id("twilight_swamp")))
                        || Monster.isDarkEnoughToSpawn(level, pos, random)), replace);
    }

    @SuppressWarnings("unchecked")
    private static <T extends Monster> void monster(RegisterSpawnPlacementsEvent event, EntityType<T> type) {
        event.register(type, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.REPLACE);
    }

    /** The stacks of the creative tab. */
    public static List<ItemStack> tabStacks() {
        List<ItemStack> stacks = new ArrayList<>();
        TAB_ITEMS.forEach(item -> stacks.add(new ItemStack(item.get())));
        return stacks;
    }
}
