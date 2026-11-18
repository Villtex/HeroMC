package betterdungeons.mob;

import betterdungeons.BetterDungeons;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * The bosses and summoned mobs of BetterDungeons.registerEntities that are no EntityHuman (Strang b2), under the ids of
 * {@link betterdungeons.api.BDEntityIds}: registerModEntity(..., 100, 3, true) for the bosses, 50 for the summoned
 * ones (tracking range in chunks: blocks / 16). TurtleBossPart is a part entity of the turtle and has no type of its
 * own. Drops: loot tables data/betterdungeons/loot_table/entities/*.json; what depends on the mob's state (the ice
 * bull's shared count, the turtle's scales) in Java.
 */
public final class BDCreatures {
    static final DeferredRegister.Entities ENTITIES = DeferredRegister.createEntities(BetterDungeons.MOD_ID);

    public static final DeferredHolder<EntityType<?>, EntityType<MonkingBoss>> MONKING = ENTITIES.registerEntityType("monking", MonkingBoss::new,
            MobCategory.MONSTER, b -> b.sized(1.0F, 2.0F).clientTrackingRange(6).updateInterval(3));
    public static final DeferredHolder<EntityType<?>, EntityType<GolemBoss>> GOLEM_BOSS = ENTITIES.registerEntityType("golem_boss", GolemBoss::new,
            MobCategory.MONSTER, b -> b.sized(1.8F, 3.5F).eyeHeight(3.5F).clientTrackingRange(6).updateInterval(3));
    public static final DeferredHolder<EntityType<?>, EntityType<IceBull>> ICE_BULL = ENTITIES.registerEntityType("ice_bull", IceBull::new,
            MobCategory.MONSTER, b -> b.noLootTable().sized(1.2F, 1.3F).clientTrackingRange(6).updateInterval(3));
    public static final DeferredHolder<EntityType<?>, EntityType<SpiderBoss>> SPIDER_BOSS = ENTITIES.registerEntityType("spider_boss", SpiderBoss::new,
            MobCategory.MONSTER, b -> b.sized(1.7F, 1.5F).eyeHeight(1.5F).fireImmune().clientTrackingRange(6).updateInterval(3));
    public static final DeferredHolder<EntityType<?>, EntityType<SlimeBoss>> SLIME_BOSS = ENTITIES.registerEntityType("slime_boss", SlimeBoss::new,
            MobCategory.MONSTER, b -> b.sized(0.6F, 0.6F).eyeHeight(0.51F).notInPeaceful().clientTrackingRange(6).updateInterval(3));
    public static final DeferredHolder<EntityType<?>, EntityType<TurtleBoss>> TURTLE_BOSS = ENTITIES.registerEntityType("turtle_boss", TurtleBoss::new,
            MobCategory.MONSTER, b -> b.sized(2.6F, 1.5F).clientTrackingRange(6).updateInterval(3));
    public static final DeferredHolder<EntityType<?>, EntityType<Wyvern>> WYVERN = ENTITIES.registerEntityType("wyvern", Wyvern::new,
            MobCategory.MONSTER, b -> b.noLootTable().sized(1.5F, 1.5F).fireImmune().clientTrackingRange(6).updateInterval(3));
    public static final DeferredHolder<EntityType<?>, EntityType<SummonedUndead>> SUMMONED_UNDEAD = ENTITIES.registerEntityType("summoned_undead",
            SummonedUndead::new, MobCategory.MONSTER, b -> b.noLootTable().sized(0.6F, 1.8F).notInPeaceful().clientTrackingRange(3).updateInterval(3));
    public static final DeferredHolder<EntityType<?>, EntityType<SummonedBait>> SUMMONED_BAIT = ENTITIES.registerEntityType("summoned_bait",
            SummonedBait::new, MobCategory.MISC, b -> b.noLootTable().sized(0.6F, 1.8F).clientTrackingRange(3).updateInterval(3));

    private BDCreatures() {
    }

    private static void attributes(EntityAttributeCreationEvent event) {
        event.put(MONKING.get(), MonkingBoss.createAttributes().build());
        event.put(GOLEM_BOSS.get(), GolemBoss.createAttributes().build());
        event.put(ICE_BULL.get(), IceBull.createAttributes().build());
        event.put(SPIDER_BOSS.get(), SpiderBoss.createAttributes().build());
        event.put(SLIME_BOSS.get(), net.minecraft.world.entity.monster.Monster.createMonsterAttributes().build());
        event.put(TURTLE_BOSS.get(), TurtleBoss.createAttributes().build());
        event.put(WYVERN.get(), Wyvern.createAttributes().build());
        event.put(SUMMONED_UNDEAD.get(), SummonedUndead.createAttributes().build());
        event.put(SUMMONED_BAIT.get(), SummonedBait.createAttributes().build());
    }

    public static void register(IEventBus modBus) {
        ENTITIES.register(modBus);
        modBus.addListener(BDCreatures::attributes);
    }

    /** EnchantmentHelper.getLootingModifier of the killer (0 without one). */
    static int looting(ServerLevel level, DamageSource source) {
        if (!(source.getEntity() instanceof LivingEntity killer)) {
            return 0;
        }
        return level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).get(Enchantments.LOOTING)
                .map(holder -> EnchantmentHelper.getItemEnchantmentLevel(holder, killer.getMainHandItem())).orElse(0);
    }
}
