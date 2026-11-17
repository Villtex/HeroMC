package projectzulu;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import projectzulu.entity.CentipedeEntity;
import projectzulu.entity.HauntedArmorEntity;
import projectzulu.entity.LizardEntity;
import projectzulu.entity.LizardSpitEntity;
import projectzulu.entity.MimicEntity;
import projectzulu.entity.MinotaurEntity;
import projectzulu.entity.MummyEntity;
import projectzulu.entity.MummyPharaohEntity;
import projectzulu.entity.SandwormEntity;

import java.util.function.UnaryOperator;

/**
 * Creatures of Project Zulu, Batch D: sandworm, lizard and lizard spit, mummy, mummy pharaoh, mimic, minotaur, haunted
 * armor, centipede. Same conventions as {@link ZuluEntities} (registration ids in tools/PORTING.md); a separate class
 * so the creature batches do not share one file. All are monsters (Creature Type monster; the mimic's "None" fell back
 * to IMob = monster), removed in peaceful. The pharaoh, mimic, minotaur and haunted armor have no biome spawns (they
 * come from the structures of Teil 11); the pharaoh has no egg colours in the pack config, so no spawn egg.
 */
public final class ZuluMonsters {
    private static final DeferredRegister.Entities ENTITIES = DeferredRegister.createEntities(ProjectZulu.MOD_ID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ProjectZulu.MOD_ID);
    private static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, ProjectZulu.MOD_ID);

    // ---- creatures ----
    public static final DeferredHolder<EntityType<?>, EntityType<SandwormEntity>> SANDWORM = entity("sandworm", SandwormEntity::new,
            MobCategory.MONSTER, b -> b.sized(1.5F, 1.0F).notInPeaceful());
    public static final DeferredHolder<EntityType<?>, EntityType<LizardEntity>> LIZARD = entity("lizard", LizardEntity::new,
            MobCategory.MONSTER, b -> b.sized(0.9F, 0.5F).notInPeaceful());
    public static final DeferredHolder<EntityType<?>, EntityType<LizardSpitEntity>> LIZARD_SPIT = entity("lizard_spit", LizardSpitEntity::new,
            MobCategory.MISC, b -> b.sized(1.0F, 1.0F).noLootTable());
    public static final DeferredHolder<EntityType<?>, EntityType<MummyPharaohEntity>> MUMMY_PHARAOH = entity("mummy_pharaoh",
            MummyPharaohEntity::new, MobCategory.MONSTER, b -> b.sized(0.6F, 1.8F).notInPeaceful());
    public static final DeferredHolder<EntityType<?>, EntityType<MummyEntity>> MUMMY = entity("mummy", MummyEntity::new,
            MobCategory.MONSTER, b -> b.sized(0.6F, 1.8F).notInPeaceful());
    public static final DeferredHolder<EntityType<?>, EntityType<MimicEntity>> MIMIC = entity("mimic", MimicEntity::new,
            MobCategory.MONSTER, b -> b.sized(1.0F, 1.5F).notInPeaceful());
    public static final DeferredHolder<EntityType<?>, EntityType<MinotaurEntity>> MINOTAUR = entity("minotaur", MinotaurEntity::new,
            MobCategory.MONSTER, b -> b.sized(1.0F, 2.4F).notInPeaceful());
    /** No setSize in EntityHauntedArmor: the EntityLiving default 0.6 x 1.8. */
    public static final DeferredHolder<EntityType<?>, EntityType<HauntedArmorEntity>> HAUNTED_ARMOR = entity("haunted_armor",
            HauntedArmorEntity::new, MobCategory.MONSTER, b -> b.sized(0.6F, 1.8F).notInPeaceful());
    public static final DeferredHolder<EntityType<?>, EntityType<CentipedeEntity>> CENTIPEDE = entity("centipede", CentipedeEntity::new,
            MobCategory.MONSTER, b -> b.sized(0.65F, 0.5F).notInPeaceful());

    // ---- spawn eggs (colours in the item models, tools/gen.py; none for the pharaoh) ----
    public static final DeferredItem<Item> SANDWORM_SPAWN_EGG = egg("sandworm_spawn_egg", SANDWORM);
    public static final DeferredItem<Item> LIZARD_SPAWN_EGG = egg("lizard_spawn_egg", LIZARD);
    public static final DeferredItem<Item> MUMMY_SPAWN_EGG = egg("mummy_spawn_egg", MUMMY);
    public static final DeferredItem<Item> MIMIC_SPAWN_EGG = egg("mimic_spawn_egg", MIMIC);
    public static final DeferredItem<Item> MINOTAUR_SPAWN_EGG = egg("minotaur_spawn_egg", MINOTAUR);
    public static final DeferredItem<Item> HAUNTED_ARMOR_SPAWN_EGG = egg("haunted_armor_spawn_egg", HAUNTED_ARMOR);
    public static final DeferredItem<Item> CENTIPEDE_SPAWN_EGG = egg("centipede_spawn_egg", CENTIPEDE);

    // ---- sounds (core/Sounds.java; the hurt roars came from SoundHookContainerClass) ----
    public static final DeferredHolder<SoundEvent, SoundEvent> SANDWORM_ROAR = sound("sandworm_roar");
    public static final DeferredHolder<SoundEvent, SoundEvent> LIZARD_HURT = sound("lizard_hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> MUMMY_SHORT_ROAR = sound("mummy_short_roar");
    public static final DeferredHolder<SoundEvent, SoundEvent> MIMIC_LIVING = sound("mimic_living");
    /** BlockJasper (Teil 11): the roar when the pharaoh rises. */
    public static final DeferredHolder<SoundEvent, SoundEvent> MUMMY_ROAR = sound("mummy_roar");
    /** BlockJasper (Teil 11): "streaming.summonwhispers", played like a record while the summoning runs. */
    public static final DeferredHolder<SoundEvent, SoundEvent> SUMMON_WHISPERS = sound("summon_whispers");

    private ZuluMonsters() {
    }

    /** trackingRange 128, updateFrequency 3 as all Project Zulu creatures. */
    private static <E extends Entity> DeferredHolder<EntityType<?>, EntityType<E>> entity(String name, EntityType.EntityFactory<E> factory,
            MobCategory category, UnaryOperator<EntityType.Builder<E>> builder) {
        return ENTITIES.registerEntityType(name, factory, category,
                b -> builder.apply(b.clientTrackingRange(8).updateInterval(3)));
    }

    private static DeferredItem<Item> egg(String name, DeferredHolder<EntityType<?>, ? extends EntityType<?>> type) {
        return ITEMS.registerItem(name, SpawnEggItem::new, p -> p.spawnEgg(type.get()));
    }

    private static DeferredHolder<SoundEvent, SoundEvent> sound(String name) {
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(ProjectZulu.MOD_ID, name)));
    }

    private static void attributes(EntityAttributeCreationEvent event) {
        event.put(SANDWORM.get(), SandwormEntity.createAttributes().build());
        event.put(LIZARD.get(), LizardEntity.createAttributes().build());
        event.put(MUMMY_PHARAOH.get(), MummyPharaohEntity.createAttributes().build());
        event.put(MUMMY.get(), MummyEntity.createAttributes().build());
        event.put(MIMIC.get(), MimicEntity.createAttributes().build());
        event.put(MINOTAUR.get(), MinotaurEntity.createAttributes().build());
        event.put(HAUNTED_ARMOR.get(), HauntedArmorEntity.createAttributes().build());
        event.put(CENTIPEDE.get(), CentipedeEntity.createAttributes().build());
    }

    /**
     * getCanSpawnHere: EntityGenericAnimal checks no darkness by itself (block light below 1); the sandworm adds the
     * open sky. The lizard, mummy and centipede use the light test of EntityMob (isValidLightLevel overridden); lizard
     * and centipede need the sky above, the centipede also passes its secondarySpawnRate of 35 (pack value).
     */
    private static void spawnPlacements(RegisterSpawnPlacementsEvent event) {
        ZuluEntities.daylitGroundPlacement(event, SANDWORM.get());
        event.register(LIZARD.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                (type, level, reason, pos, random) -> Monster.checkMonsterSpawnRules(type, level, reason, pos, random) && level.canSeeSky(pos),
                RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(MUMMY.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Monster::checkMonsterSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(CENTIPEDE.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                (type, level, reason, pos, random) -> 35 - random.nextInt(100) >= 0
                        && Monster.checkMonsterSpawnRules(type, level, reason, pos, random) && level.canSeeSky(pos),
                RegisterSpawnPlacementsEvent.Operation.REPLACE);
    }

    public static void register(IEventBus modBus) {
        ENTITIES.register(modBus);
        ITEMS.register(modBus);
        SOUNDS.register(modBus);
        modBus.addListener(ZuluMonsters::attributes);
        modBus.addListener(ZuluMonsters::spawnPlacements);
    }
}
