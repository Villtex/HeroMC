package harkenscythe.mob;

import harkenscythe.HSEntities;
import harkenscythe.HarkenScythe;
import harkenscythe.entity.SoulEntity;
import harkenscythe.entity.SpectralPets;
import harkenscythe.event.HSEvents;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * The mobs of mod_HarkenScythe (Teil 9, part 3): registerModEntity ids 10-13 and 20-42 (tracking range 80, update
 * interval 3), the Harbinger's spawn (biome modifier {@code harkenscythe_core:spawn_harbinger}), the spawn eggs
 * (registerEntityEgg, white with black spots) and the hooks the core calls ({@link SpectralPets}).
 */
public final class HSMobs {
    static final DeferredRegister.Entities ENTITIES = DeferredRegister.createEntities(HarkenScythe.MOD_ID);
    static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(HarkenScythe.MOD_ID);
    private static final List<DeferredItem<Item>> EGGS = new ArrayList<>();

    public static final DeferredHolder<EntityType<?>, EntityType<EssenceSlime>> ECTOPLASM = monster("ectoplasm",
            (t, l) -> new EssenceSlime(t, l, false), 0.6F, 0.6F, false);
    public static final DeferredHolder<EntityType<?>, EntityType<EssenceSlime>> HEMOGLOBIN = monster("hemoglobin",
            (t, l) -> new EssenceSlime(t, l, true), 0.6F, 0.6F, false);
    public static final DeferredHolder<EntityType<?>, EntityType<Harbinger>> HARBINGER = monster("harbinger", Harbinger::new, 0.8F, 1.8F, false);
    public static final DeferredHolder<EntityType<?>, EntityType<EvilSpectralMiner>> SPECTRAL_MINER_EVIL = monster("spectral_miner_evil",
            EvilSpectralMiner::new, 0.6F, 1.8F, true);
    public static final DeferredHolder<EntityType<?>, EntityType<SimpleSpectral>> SPECTRAL_CHICKEN = pet("spectral_chicken",
            (t, l) -> new SimpleSpectral(t, l, SimpleSpectral.Kind.CHICKEN, 8.0F, new SpectralPet.Sounds(sound("entity.chicken.ambient"),
                    sound("entity.chicken.hurt"), sound("entity.chicken.hurt"), sound("entity.chicken.step"), 1.0F)), 0.8F, 0.7F, false);
    public static final DeferredHolder<EntityType<?>, EntityType<SimpleSpectral>> SPECTRAL_COW = pet("spectral_cow",
            (t, l) -> new SimpleSpectral(t, l, SimpleSpectral.Kind.COW, 10.0F, new SpectralPet.Sounds(sound("entity.cow.ambient"),
                    sound("entity.cow.hurt"), sound("entity.cow.hurt"), sound("entity.cow.step"), 0.4F)), 0.8F, 0.7F, false);
    public static final DeferredHolder<EntityType<?>, EntityType<SpectralSheep>> SPECTRAL_SHEEP = pet("spectral_sheep", SpectralSheep::new,
            0.9F, 1.3F, false);
    public static final DeferredHolder<EntityType<?>, EntityType<SpectralBat>> SPECTRAL_BAT = pet("spectral_bat", SpectralBat::new, 0.8F, 0.9F, false);
    public static final DeferredHolder<EntityType<?>, EntityType<SpectralPig>> SPECTRAL_PIG = pet("spectral_pig", SpectralPig::new, 0.9F, 0.9F, false);
    public static final DeferredHolder<EntityType<?>, EntityType<SpectralWolf>> SPECTRAL_WOLF = pet("spectral_wolf", SpectralWolf::new, 0.6F, 0.8F, false);
    public static final DeferredHolder<EntityType<?>, EntityType<SpectralOcelot>> SPECTRAL_OCELOT = pet("spectral_ocelot", SpectralOcelot::new,
            0.6F, 0.8F, false);
    public static final DeferredHolder<EntityType<?>, EntityType<SpectralSquid>> SPECTRAL_SQUID = pet("spectral_squid", SpectralSquid::new,
            0.95F, 0.95F, false);
    public static final DeferredHolder<EntityType<?>, EntityType<SimpleSpectral>> SPECTRAL_VILLAGER = pet("spectral_villager",
            (t, l) -> new SimpleSpectral(t, l, SimpleSpectral.Kind.VILLAGER, 10.0F, new SpectralPet.Sounds(SoundEvents.VILLAGER_AMBIENT,
                    SoundEvents.VILLAGER_HURT, SoundEvents.VILLAGER_DEATH, sound("entity.wolf.step"), 0.4F)), 0.6F, 1.8F, false);
    public static final DeferredHolder<EntityType<?>, EntityType<SpectralCreeper>> SPECTRAL_CREEPER = pet("spectral_creeper", SpectralCreeper::new,
            0.8F, 1.8F, false);
    public static final DeferredHolder<EntityType<?>, EntityType<SpectralHumanoid>> SPECTRAL_ZOMBIE = humanoid("spectral_zombie",
            SpectralHumanoid.Kind.ZOMBIE, SoundEvents.ZOMBIE_AMBIENT, SoundEvents.ZOMBIE_HURT, SoundEvents.ZOMBIE_DEATH, SoundEvents.ZOMBIE_STEP, 0.6F);
    public static final DeferredHolder<EntityType<?>, EntityType<SpectralHumanoid>> SPECTRAL_PIG_ZOMBIE = humanoid("spectral_pig_zombie",
            SpectralHumanoid.Kind.PIG_ZOMBIE, SoundEvents.ZOMBIFIED_PIGLIN_AMBIENT, SoundEvents.ZOMBIFIED_PIGLIN_HURT, SoundEvents.ZOMBIFIED_PIGLIN_DEATH,
            SoundEvents.ZOMBIE_STEP, 0.6F);
    public static final DeferredHolder<EntityType<?>, EntityType<SpectralHumanoid>> SPECTRAL_SKELETON = humanoid("spectral_skeleton",
            SpectralHumanoid.Kind.SKELETON, SoundEvents.SKELETON_AMBIENT, SoundEvents.SKELETON_HURT, SoundEvents.SKELETON_DEATH, SoundEvents.SKELETON_STEP,
            0.8F);
    public static final DeferredHolder<EntityType<?>, EntityType<SpectralHumanoid>> SPECTRAL_WITHER_SKELETON = humanoid("spectral_wither_skeleton",
            SpectralHumanoid.Kind.WITHER_SKELETON, SoundEvents.SKELETON_AMBIENT, SoundEvents.SKELETON_HURT, SoundEvents.SKELETON_DEATH,
            SoundEvents.SKELETON_STEP, 0.6F);
    public static final DeferredHolder<EntityType<?>, EntityType<SpectralHumanoid>> SPECTRAL_WITCH = humanoid("spectral_witch",
            SpectralHumanoid.Kind.WITCH, SoundEvents.WITCH_AMBIENT, SoundEvents.WITCH_HURT, SoundEvents.WITCH_DEATH, sound("entity.wolf.step"), 0.6F);
    public static final DeferredHolder<EntityType<?>, EntityType<SpectralSpider>> SPECTRAL_SPIDER = pet("spectral_spider",
            (t, l) -> new SpectralSpider(t, l, false), 1.4F, 0.9F, false);
    public static final DeferredHolder<EntityType<?>, EntityType<SpectralSpider>> SPECTRAL_CAVE_SPIDER = pet("spectral_cave_spider",
            (t, l) -> new SpectralSpider(t, l, true), 0.7F, 0.5F, false);
    public static final DeferredHolder<EntityType<?>, EntityType<SimpleSpectral>> SPECTRAL_SILVERFISH = pet("spectral_silverfish",
            (t, l) -> new SimpleSpectral(t, l, SimpleSpectral.Kind.SILVERFISH, 8.0F, new SpectralPet.Sounds(SoundEvents.SILVERFISH_AMBIENT,
                    SoundEvents.SILVERFISH_HURT, SoundEvents.SILVERFISH_DEATH, SoundEvents.SILVERFISH_STEP, 0.4F)), 0.8F, 0.7F, false);
    public static final DeferredHolder<EntityType<?>, EntityType<SpectralEnderman>> SPECTRAL_ENDERMAN = pet("spectral_enderman",
            SpectralEnderman::new, 0.8F, 2.9F, false);
    public static final DeferredHolder<EntityType<?>, EntityType<SpectralBlaze>> SPECTRAL_BLAZE = pet("spectral_blaze", SpectralBlaze::new,
            0.6F, 1.8F, true);
    public static final DeferredHolder<EntityType<?>, EntityType<SpectralGhast>> SPECTRAL_GHAST = pet("spectral_ghast", SpectralGhast::new,
            4.0F, 4.0F, true);
    public static final DeferredHolder<EntityType<?>, EntityType<SpectralHumanoid>> SPECTRAL_MINER = humanoid("spectral_miner",
            SpectralHumanoid.Kind.MINER, null, SoundEvents.PLAYER_HURT, null, sound("entity.wolf.step"), 0.8F);

    private HSMobs() {
    }

    /** A vanilla sound by id (some only exist as sound variants in 26.3, e.g. entity.cow.ambient). */
    public static SoundEvent sound(String id) {
        Identifier key = Identifier.withDefaultNamespace(id);
        return BuiltInRegistries.SOUND_EVENT.getOptional(key).orElseGet(() -> SoundEvent.createVariableRangeEvent(key));
    }

    private static <T extends Entity> DeferredHolder<EntityType<?>, EntityType<T>> register(String name, EntityType.EntityFactory<T> factory,
            MobCategory category, float w, float h, boolean fireImmune) {
        DeferredHolder<EntityType<?>, EntityType<T>> holder = ENTITIES.registerEntityType(name, factory, category, b -> {
            b.sized(w, h).clientTrackingRange(5).updateInterval(3);
            if (fireImmune) {
                b.fireImmune();
            }
            return b;
        });
        EGGS.add(ITEMS.registerItem(name + "_spawn_egg", SpawnEggItem::new, p -> p.spawnEgg(holder.get())));
        return holder;
    }

    private static <T extends Entity> DeferredHolder<EntityType<?>, EntityType<T>> monster(String name, EntityType.EntityFactory<T> factory,
            float w, float h, boolean fireImmune) {
        return register(name, factory, MobCategory.MONSTER, w, h, fireImmune);
    }

    private static <T extends Entity> DeferredHolder<EntityType<?>, EntityType<T>> pet(String name, EntityType.EntityFactory<T> factory,
            float w, float h, boolean fireImmune) {
        return register(name, factory, MobCategory.CREATURE, w, h, fireImmune);
    }

    private static DeferredHolder<EntityType<?>, EntityType<SpectralHumanoid>> humanoid(String name, SpectralHumanoid.Kind kind, SoundEvent ambient,
            SoundEvent hurt, SoundEvent death, SoundEvent step, float width) {
        return pet(name, (t, l) -> new SpectralHumanoid(t, l, kind, new SpectralPet.Sounds(ambient, hurt, death, step, 0.4F)), width, 1.8F, false);
    }

    // ------------------------------------------------------------------------------------------------ events
    private static void attributes(EntityAttributeCreationEvent event) {
        event.put(ECTOPLASM.get(), Monster.createMonsterAttributes().build());
        event.put(HEMOGLOBIN.get(), Monster.createMonsterAttributes().build());
        event.put(HARBINGER.get(), Harbinger.createAttributes().build());
        event.put(SPECTRAL_MINER_EVIL.get(), EvilSpectralMiner.createAttributes().build());
        event.put(SPECTRAL_CHICKEN.get(), SpectralPet.spectralAttributes(8.0, 0.3).build());
        event.put(SPECTRAL_COW.get(), SpectralPet.spectralAttributes(10.0, 0.3).build());
        event.put(SPECTRAL_SHEEP.get(), SpectralPet.spectralAttributes(10.0, 0.3).build());
        event.put(SPECTRAL_BAT.get(), SpectralPet.spectralAttributes(6.0, 0.5).build());
        event.put(SPECTRAL_PIG.get(), SpectralPet.spectralAttributes(10.0, 0.3).build());
        event.put(SPECTRAL_WOLF.get(), SpectralPet.spectralAttributes(8.0, 0.3).build());
        event.put(SPECTRAL_OCELOT.get(), SpectralPet.spectralAttributes(10.0, 0.3).build());
        event.put(SPECTRAL_SQUID.get(), SpectralPet.spectralAttributes(10.0, 0.3).build());
        event.put(SPECTRAL_VILLAGER.get(), SpectralPet.spectralAttributes(10.0, 0.3).build());
        event.put(SPECTRAL_CREEPER.get(), SpectralPet.spectralAttributes(10.0, 0.3).build());
        event.put(SPECTRAL_ZOMBIE.get(), SpectralPet.spectralAttributes(10.0, 0.3).build());
        event.put(SPECTRAL_PIG_ZOMBIE.get(), SpectralPet.spectralAttributes(10.0, 0.3).build());
        event.put(SPECTRAL_SKELETON.get(), SpectralPet.spectralAttributes(10.0, 0.3).build());
        event.put(SPECTRAL_WITHER_SKELETON.get(), SpectralPet.spectralAttributes(10.0, 0.3).build());
        event.put(SPECTRAL_WITCH.get(), SpectralPet.spectralAttributes(10.0, 0.3).build());
        event.put(SPECTRAL_SPIDER.get(), SpectralPet.spectralAttributes(10.0, 0.3).build());
        event.put(SPECTRAL_CAVE_SPIDER.get(), SpectralPet.spectralAttributes(8.0, 0.3).build());
        event.put(SPECTRAL_SILVERFISH.get(), SpectralPet.spectralAttributes(8.0, 0.3).build());
        event.put(SPECTRAL_ENDERMAN.get(), SpectralPet.spectralAttributes(10.0, 0.3)
                .add(net.minecraft.world.entity.ai.attributes.Attributes.STEP_HEIGHT, 1.0).build());
        event.put(SPECTRAL_BLAZE.get(), SpectralPet.spectralAttributes(10.0, 0.3).build());
        event.put(SPECTRAL_GHAST.get(), SpectralPet.spectralAttributes(10.0, 0.15).build());
        event.put(SPECTRAL_MINER.get(), SpectralHumanoid.minerAttributes().build());
    }

    private static void spawnPlacements(RegisterSpawnPlacementsEvent event) {
        event.register(HARBINGER.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.REPLACE);
    }

    private static void creativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.SPAWN_EGGS) {
            EGGS.forEach(event::accept);
        }
    }

    public static void register(IEventBus modBus) {
        ENTITIES.register(modBus);
        ITEMS.register(modBus);
        modBus.addListener(HSMobs::attributes);
        modBus.addListener(HSMobs::spawnPlacements);
        modBus.addListener(HSMobs::creativeTab);
        SpectralPets.hooks = new Hooks();
    }

    // ------------------------------------------------------------------------------------------------ hooks of the core
    static final class Hooks implements SpectralPets.Hooks {
        /** EntityHSSoul.createSpectralPet */
        @Override
        public boolean create(String name, ServerLevel level, double x, double y, double z, RandomSource random, boolean needsMaster) {
            EntityType<? extends SpectralPet> type = switch (name) {
                case "Chicken" -> SPECTRAL_CHICKEN.get();
                case "Cow" -> SPECTRAL_COW.get();
                case "Sheep" -> SPECTRAL_SHEEP.get();
                case "Bat" -> SPECTRAL_BAT.get();
                case "Pig" -> SPECTRAL_PIG.get();
                case "Wolf" -> SPECTRAL_WOLF.get();
                case "Cat_Black", "Cat_Red", "Cat_Siamese", "Ocelot" -> SPECTRAL_OCELOT.get();
                case "Squid" -> SPECTRAL_SQUID.get();
                case "Villager" -> SPECTRAL_VILLAGER.get();
                case "Creeper" -> SPECTRAL_CREEPER.get();
                case "Zombie" -> SPECTRAL_ZOMBIE.get();
                case "PigZombie" -> SPECTRAL_PIG_ZOMBIE.get();
                case "Skeleton" -> SPECTRAL_SKELETON.get();
                case "Skeleton_Wither" -> SPECTRAL_WITHER_SKELETON.get();
                case "Witch" -> SPECTRAL_WITCH.get();
                case "Spider" -> SPECTRAL_SPIDER.get();
                case "CaveSpider" -> SPECTRAL_CAVE_SPIDER.get();
                case "Silverfish" -> SPECTRAL_SILVERFISH.get();
                case "Enderman" -> SPECTRAL_ENDERMAN.get();
                case "Blaze" -> SPECTRAL_BLAZE.get();
                case "Ghast" -> SPECTRAL_GHAST.get();
                default -> null;
            };
            if (type == null) {
                return false;
            }
            SpectralPet pet = type.create(level, EntitySpawnReason.TRIGGERED);
            if (pet == null) {
                return false;
            }
            pet.snapTo(x, y + 0.5, z, random.nextFloat() * 360.0F, 0.0F);
            if (pet instanceof SpectralOcelot ocelot) {
                ocelot.setTameSkin(switch (name) {
                    case "Cat_Black" -> 1;
                    case "Cat_Red" -> 2;
                    case "Cat_Siamese" -> 3;
                    default -> 0;
                });
            }
            if (needsMaster) {
                pet.makeHarbingerMinion();
            }
            return level.addFreshEntity(pet);
        }

        /** EntityHSSoulLost.createSpectralPet */
        @Override
        public boolean createMiner(ServerLevel level, double x, double y, double z, RandomSource random) {
            SpectralHumanoid miner = SPECTRAL_MINER.get().create(level, EntitySpawnReason.TRIGGERED);
            if (miner == null) {
                return false;
            }
            miner.snapTo(x, y + 0.5, z, random.nextFloat() * 360.0F, 0.0F);
            return level.addFreshEntity(miner);
        }

        /** EntityHSSoulLost.interact */
        @Override
        public boolean spawnEvilMiner(ServerLevel level, double x, double y, double z) {
            EvilSpectralMiner miner = SPECTRAL_MINER_EVIL.get().create(level, EntitySpawnReason.TRIGGERED);
            if (miner == null) {
                return false;
            }
            miner.snapTo(x, y, z, 0.0F, 0.0F);
            return level.addFreshEntity(miner);
        }

        /** BlockHSSoulCrucible / BlockHSBloodCrucible.breakBlock */
        @Override
        public boolean spawnEssenceSlime(ServerLevel level, double x, double y, double z, int size, int worth, boolean blood) {
            EssenceSlime slime = (blood ? HEMOGLOBIN : ECTOPLASM).get().create(level, EntitySpawnReason.TRIGGERED);
            if (slime == null) {
                return false;
            }
            slime.snapTo(x, y, z, 0.0F, 0.0F);
            slime.setSize(size, true);
            slime.setResourceWorthTotal(worth, size);
            return level.addFreshEntity(slime);
        }

        @Override
        public void harbingerSoulReap(LivingEntity harbinger, LivingEntity victim) {
            if (harbinger instanceof Harbinger h && victim.level() instanceof ServerLevel level) {
                HSMobs.harbingerSoulReap(level, h, victim);
            }
        }

        @Override
        public boolean keepsSoulLight(LivingEntity entity) {
            return entity instanceof SpectralHumanoid miner && miner.kind() == SpectralHumanoid.Kind.MINER && miner.holdsTorch();
        }
    }

    /**
     * EventHookContainerClassHarkenScythe.harbingerSoulReap: what a Harbinger kills. Wrathful and culled souls are
     * destroyed, a grieving soul rises as an evil spectral miner (for five of its reanimation points), any other soul
     * as a spectral minion (for one); a player leaves a grieving soul (one point more), everything else a soul.
     */
    static void harbingerSoulReap(ServerLevel level, Harbinger harbinger, LivingEntity victim) {
        EntityType<?> type = victim.getType();
        if (type == HSEntities.SOUL_WRATHFUL.get() || type == HSEntities.SOUL_CULLED.get()) {
            level.playSound(null, victim, SoundEvents.ZOMBIE_INFECT, SoundSource.HOSTILE, 1.0F, 1.0F);
            victim.discard();
        } else if (type == HSEntities.SOUL_GRIEVING.get()) {
            if (harbinger.reAnimateResource >= 5) {
                EvilSpectralMiner minion = SPECTRAL_MINER_EVIL.get().create(level, EntitySpawnReason.TRIGGERED);
                if (minion != null) {
                    minion.snapTo(victim.getX(), victim.getY() + 0.5, victim.getZ(), level.getRandom().nextFloat() * 360.0F, 0.0F);
                    level.addFreshEntity(minion);
                }
                level.playSound(null, victim, SoundEvents.ZOMBIE_INFECT, SoundSource.HOSTILE, 1.0F, 1.0F);
                harbinger.reAnimateResource -= 5;
            }
            victim.discard();
        } else if (victim instanceof SoulEntity soul) {
            if (harbinger.reAnimateResource > 0) {
                SpectralPets.create(soul.getFallenSoulName(), level, victim.getX(), victim.getY(), victim.getZ(), level.getRandom(), true);
                level.playSound(null, victim, SoundEvents.ZOMBIE_INFECT, SoundSource.HOSTILE, 1.0F, 1.0F);
                harbinger.reAnimateResource--;
            }
            victim.discard();
        } else if (victim instanceof Player) {
            HSEvents.spawnSoul(level, HSEntities.SOUL_GRIEVING.get(), victim, 1);
            harbinger.reAnimateResource++;
        } else {
            HSEvents.spawnSoul(level, HSEntities.SOUL_COMMON.get(), victim, 1);
        }
    }
}
