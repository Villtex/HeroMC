package atomicstryker.infernalmobs.common;

import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.lang.reflect.Field;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * EntityEventHandler and SaveEventHandler of the original. Chunk load/unload and the 5-second existence check are
 * replaced by EntityJoinLevelEvent/EntityLeaveLevelEvent, which fire for entities loaded from disk as well.
 */
final class EntityEventHandler {
    private static final Field LAST_HURT;
    /** Entities whose hurt hooks are running, with the damage: a nested hit that 1.5.2 would ignore is cancelled. */
    private static final Map<LivingEntity, Float> HURTING = new IdentityHashMap<>();

    static {
        Field f = null;
        try {
            f = LivingEntity.class.getDeclaredField("lastHurt");
            f.setAccessible(true);
        } catch (ReflectiveOperationException e) {
            // without it every hit during the damage cooldown counts
        }
        LAST_HURT = f;
    }

    private EntityEventHandler() {
    }

    static void register() {
        NeoForge.EVENT_BUS.addListener(EntityJoinLevelEvent.class, EntityEventHandler::onEntityJoinedWorld);
        NeoForge.EVENT_BUS.addListener(EntityLeaveLevelEvent.class, e -> {
            if (!e.getLevel().isClientSide() && e.getEntity() instanceof LivingEntity living) {
                InfernalMobsCore.removeEntFromElites(living);
            }
        });
        NeoForge.EVENT_BUS.addListener(LivingDeathEvent.class, EntityEventHandler::onEntityLivingDeath);
        NeoForge.EVENT_BUS.addListener(LivingChangeTargetEvent.class, EntityEventHandler::onEntityLivingSetAttackTarget);
        NeoForge.EVENT_BUS.addListener(LivingIncomingDamageEvent.class, EntityEventHandler::onEntityLivingHurt);
        NeoForge.EVENT_BUS.addListener(LivingFallEvent.class, EntityEventHandler::onEntityLivingFall);
        NeoForge.EVENT_BUS.addListener(LivingEvent.LivingJumpEvent.class, EntityEventHandler::onEntityLivingJump);
        NeoForge.EVENT_BUS.addListener(EntityTickEvent.Pre.class, EntityEventHandler::onEntityLivingUpdate);
        NeoForge.EVENT_BUS.addListener(LivingDropsEvent.class, EntityEventHandler::onEntityLivingDrops);
    }

    private static void onEntityJoinedWorld(EntityJoinLevelEvent event) {
        if (!event.getLevel().isClientSide() && event.getEntity() instanceof LivingEntity living) {
            InfernalMobsCore.onEntityJoined(living);
        }
    }

    private static void onEntityLivingDeath(LivingDeathEvent event) {
        if (!event.getEntity().level().isClientSide()) {
            MobModifier mod = InfernalMobsCore.getMobModifiers(event.getEntity());
            if (mod != null && mod.onDeath()) {
                event.setCanceled(true);
            }
        }
    }

    private static void onEntityLivingSetAttackTarget(LivingChangeTargetEvent event) {
        if (!event.getEntity().level().isClientSide()) {
            MobModifier mod = InfernalMobsCore.getMobModifiers(event.getEntity());
            if (mod != null) {
                mod.onSetAttackTarget(event.getNewAboutToBeSetTarget());
            }
        }
    }

    /**
     * LivingHurtEvent of 1.5.2 fired after the hurt-cooldown check (a hit not stronger than the last one during the
     * cooldown never reached it, nor the damage). LivingIncomingDamageEvent comes before that check, so such hits are
     * skipped here, and a hit on a mob whose own hooks are still running (Vengeance against Vengeance) is cancelled
     * as 1.5.2 would have ignored it.
     */
    private static void onEntityLivingHurt(LivingIncomingDamageEvent event) {
        LivingEntity victim = event.getEntity();
        if (victim.level().isClientSide()) {
            return;
        }
        float amount = event.getAmount();
        Float running = HURTING.get(victim);
        if (running != null && amount <= running) {
            event.setCanceled(true);
            return;
        }
        if (victim.damageCooldownTime > 10 && !event.getSource().is(DamageTypeTags.BYPASSES_COOLDOWN) && amount <= lastHurt(victim)) {
            return;
        }
        HURTING.put(victim, amount);
        try {
            MobModifier mod = InfernalMobsCore.getMobModifiers(victim);
            if (mod != null) {
                event.setAmount(mod.onHurt(victim, event.getSource(), event.getAmount()));
            }
            if (event.getSource().getEntity() instanceof LivingEntity attacker) {
                mod = InfernalMobsCore.getMobModifiers(attacker);
                if (mod != null) {
                    event.setAmount(mod.onAttack(victim, event.getSource(), event.getAmount()));
                }
            }
        } finally {
            if (running == null) {
                HURTING.remove(victim);
            } else {
                HURTING.put(victim, running);
            }
        }
    }

    private static float lastHurt(LivingEntity entity) {
        try {
            return LAST_HURT == null ? -1.0F : LAST_HURT.getFloat(entity);
        } catch (IllegalAccessException e) {
            return -1.0F;
        }
    }

    private static void onEntityLivingFall(LivingFallEvent event) {
        if (!event.getEntity().level().isClientSide()) {
            MobModifier mod = InfernalMobsCore.getMobModifiers(event.getEntity());
            if (mod != null) {
                event.setCanceled(mod.onFall(event.getDistance()));
            }
        }
    }

    private static void onEntityLivingJump(LivingEvent.LivingJumpEvent event) {
        if (!event.getEntity().level().isClientSide()) {
            MobModifier mod = InfernalMobsCore.getMobModifiers(event.getEntity());
            if (mod != null) {
                mod.onJump(event.getEntity());
            }
        }
    }

    private static void onEntityLivingUpdate(EntityTickEvent.Pre event) {
        if (!event.getEntity().level().isClientSide() && event.getEntity() instanceof LivingEntity living) {
            MobModifier mod = InfernalMobsCore.getMobModifiers(living);
            if (mod != null) {
                mod.onUpdate(living);
            }
        }
    }

    private static void onEntityLivingDrops(LivingDropsEvent event) {
        if (!event.getEntity().level().isClientSide()) {
            MobModifier mod = InfernalMobsCore.getMobModifiers(event.getEntity());
            if (mod != null) {
                mod.onDropItems(event.getEntity(), event.getSource(), event.isRecentlyHit());
                InfernalMobsCore.removeEntFromElites(event.getEntity());
            }
        }
    }
}
