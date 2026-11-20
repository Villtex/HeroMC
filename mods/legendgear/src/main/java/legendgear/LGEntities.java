package legendgear;

import legendgear.entity.ArrowStormEntity;
import legendgear.entity.BombBlastEntity;
import legendgear.entity.BombEntity;
import legendgear.entity.EnderBombEntity;
import legendgear.entity.FallingStarEntity;
import legendgear.entity.FireblastEntity;
import legendgear.entity.GrindStarEntity;
import legendgear.entity.HookshotHookEntity;
import legendgear.entity.MagicBoomerangEntity;
import legendgear.entity.MedallionEntity;
import legendgear.entity.QuakeEntity;
import legendgear.entity.WhirlwindEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * The entities of LegendGear.load (registerModEntity(..., 64 or 128, update frequency, true)). The four thrown medallions
 * are one entity with a kind.
 */
public final class LGEntities {
    static final DeferredRegister.Entities ENTITIES = DeferredRegister.createEntities(LegendGear.MOD_ID);

    public static final DeferredHolder<EntityType<?>, EntityType<MedallionEntity>> MEDALLION = ENTITIES.registerEntityType("medallion",
            MedallionEntity::new, MobCategory.MISC, b -> b.noLootTable().sized(0.25F, 0.25F).clientTrackingRange(4).updateInterval(10));
    public static final DeferredHolder<EntityType<?>, EntityType<QuakeEntity>> QUAKE = ENTITIES.registerEntityType("quake",
            QuakeEntity::new, MobCategory.MISC, b -> b.noLootTable().sized(0.25F, 0.25F).clientTrackingRange(4).updateInterval(10));
    public static final DeferredHolder<EntityType<?>, EntityType<ArrowStormEntity>> ARROW_STORM = ENTITIES.registerEntityType("arrow_storm",
            ArrowStormEntity::new, MobCategory.MISC, b -> b.noLootTable().sized(0.25F, 0.25F).clientTrackingRange(4).updateInterval(10));
    public static final DeferredHolder<EntityType<?>, EntityType<MagicBoomerangEntity>> MAGIC_BOOMERANG = ENTITIES.registerEntityType(
            "magic_boomerang", MagicBoomerangEntity::new, MobCategory.MISC, b -> b.noLootTable().sized(0.25F, 0.25F).clientTrackingRange(4).updateInterval(5));
    public static final DeferredHolder<EntityType<?>, EntityType<BombEntity>> BOMB = ENTITIES.registerEntityType("bomb",
            BombEntity::new, MobCategory.MISC, b -> b.noLootTable().sized(0.5F, 0.5F).clientTrackingRange(4).updateInterval(1));
    public static final DeferredHolder<EntityType<?>, EntityType<BombBlastEntity>> BOMB_BLAST = ENTITIES.registerEntityType("bomb_blast",
            BombBlastEntity::new, MobCategory.MISC, b -> b.noLootTable().sized(0.6F, 1.8F).clientTrackingRange(4).updateInterval(10));
    public static final DeferredHolder<EntityType<?>, EntityType<FireblastEntity>> FIREBLAST = ENTITIES.registerEntityType("fireblast",
            FireblastEntity::new, MobCategory.MISC, b -> b.noLootTable().sized(0.25F, 0.25F).clientTrackingRange(4).updateInterval(10));
    public static final DeferredHolder<EntityType<?>, EntityType<WhirlwindEntity>> WHIRLWIND = ENTITIES.registerEntityType("whirlwind",
            WhirlwindEntity::new, MobCategory.MISC, b -> b.noLootTable().sized(0.25F, 0.25F).clientTrackingRange(4).updateInterval(10));
    public static final DeferredHolder<EntityType<?>, EntityType<HookshotHookEntity>> HOOKSHOT_HOOK = ENTITIES.registerEntityType("hookshot_hook",
            HookshotHookEntity::new, MobCategory.MISC, b -> b.noLootTable().sized(0.25F, 0.25F).clientTrackingRange(4).updateInterval(1));
    public static final DeferredHolder<EntityType<?>, EntityType<GrindStarEntity>> GRIND_STAR = ENTITIES.registerEntityType("grind_star",
            GrindStarEntity::new, MobCategory.MISC, b -> b.noLootTable().sized(0.25F, 0.25F).clientTrackingRange(4).updateInterval(1));
    public static final DeferredHolder<EntityType<?>, EntityType<FallingStarEntity>> FALLING_STAR = ENTITIES.registerEntityType("falling_star",
            FallingStarEntity::new, MobCategory.MISC, b -> b.noLootTable().sized(0.5F, 1.0F).clientTrackingRange(8).updateInterval(10));
    public static final DeferredHolder<EntityType<?>, EntityType<EnderBombEntity>> ENDER_BOMB = ENTITIES.registerEntityType("ender_bomb",
            EnderBombEntity::new, MobCategory.MISC, b -> b.noLootTable().sized(10.0F, 10.0F).clientTrackingRange(4).updateInterval(10));

    private LGEntities() {
    }

    static void register(IEventBus modBus) {
        ENTITIES.register(modBus);
    }
}
