package betterdungeons;

import betterdungeons.projectile.BDBlockProjectile;
import betterdungeons.projectile.BDMagicArrow;
import betterdungeons.projectile.BDProjectile;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * The projectiles of BetterDungeons.registerEntities (registerModEntity(..., trackingRange, updateFrequency, true)):
 * MagicArrow 64/20, BlockProjectile 50/5, BDProjectile 64/3. The mobs follow in their own registration class; their
 * ids are in {@link betterdungeons.api.BDEntityIds}.
 */
public final class BDEntities {
    static final DeferredRegister.Entities ENTITIES = DeferredRegister.createEntities(BetterDungeons.MOD_ID);

    public static final DeferredHolder<EntityType<?>, EntityType<BDMagicArrow>> MAGIC_ARROW = ENTITIES.registerEntityType("magic_arrow",
            BDMagicArrow::new, MobCategory.MISC, b -> b.noLootTable().sized(0.5F, 0.5F).eyeHeight(0.13F).clientTrackingRange(4).updateInterval(20));
    public static final DeferredHolder<EntityType<?>, EntityType<BDBlockProjectile>> BLOCK_PROJECTILE = ENTITIES.registerEntityType(
            "block_projectile", BDBlockProjectile::new, MobCategory.MISC, b -> b.noLootTable().sized(1.0F, 1.0F).clientTrackingRange(4).updateInterval(5));
    public static final DeferredHolder<EntityType<?>, EntityType<BDProjectile>> PROJECTILE = ENTITIES.registerEntityType("projectile",
            BDProjectile::new, MobCategory.MISC, b -> b.noLootTable().sized(1.0F, 1.0F).clientTrackingRange(4).updateInterval(3));

    private BDEntities() {
    }

    static void register(IEventBus modBus) {
        ENTITIES.register(modBus);
    }
}
