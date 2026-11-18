package betterdungeons.item;

import betterdungeons.BetterDungeons;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.Optional;
import java.util.Random;

/** HelperPlayer, BDHelper and what several weapons of chocolate.mods.BD.items share. */
public final class BDItemUtil {
    private static final Identifier CHARGE = Identifier.fromNamespaceAndPath(BetterDungeons.MOD_ID, "charged_attack");
    public static final Random RAND = new Random();

    private BDItemUtil() {
    }

    /** HelperPlayer.getTarget: the first entity on the player's line of sight within dist (blocks stop it). */
    public static @Nullable Entity getTarget(Player player, double dist) {
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        HitResult block = player.level().clip(new ClipContext(eye, eye.add(look.scale(dist)), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
        if (block.getType() != HitResult.Type.MISS) {
            dist = block.getLocation().distanceTo(eye) - 1.0;
        }
        Vec3 end = eye.add(look.scale(dist));
        AABB box = player.getBoundingBox().expandTowards(look.scale(dist)).inflate(1.0);
        for (Entity e : player.level().getEntities(player, box)) {
            if (e.isPickable()) {
                Optional<Vec3> hit = e.getBoundingBox().inflate(e.getBbWidth(), e.getBbHeight(), e.getBbWidth()).clip(eye, end);
                if (hit.isPresent()) {
                    return e;
                }
            }
        }
        return null;
    }

    /** BDHelper.getAngleBetweenEntities. */
    public static double angleBetween(Entity entity, Entity target) {
        double angle = Math.toDegrees(Math.atan2(entity.getX() - target.getX(), entity.getZ() - target.getZ()));
        return -Mth.wrapDegrees(angle - 180.0);
    }

    /** The sneak teleport of the ninja dagger and the walker sword: 4 blocks along the view, portal particles. */
    static void teleportForward(Player player) {
        Level level = player.level();
        level.playSound(null, player, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
        portalParticles(player);
        double x = -Math.sin(Math.toRadians(player.getYRot()));
        double z = Math.cos(Math.toRadians(player.getYRot()));
        double y = -Math.sin(Math.toRadians(player.getXRot()));
        x *= 1.0 - Math.abs(y);
        z *= 1.0 - Math.abs(y);
        int dist = 4;
        if (player instanceof ServerPlayer server) {
            server.teleportTo(player.getX() + x * dist, player.getY() + y * dist, player.getZ() + z * dist);
        }
        portalParticles(player);
    }

    private static void portalParticles(Player player) {
        for (int i = 0; i < 6; i++) {
            player.level().addParticle(ParticleTypes.PORTAL, player.getX() + RAND.nextFloat() - 0.5, player.getY() + RAND.nextFloat() - 0.5,
                    player.getZ() + RAND.nextFloat() - 0.5, RAND.nextFloat() - 0.5F, RAND.nextFloat() - 0.5F, RAND.nextFloat() - 0.5F);
        }
    }

    /**
     * attackTargetEntityWithCurrentItem while getDamageVsEntity returned the cached charge damage: a normal attack of
     * the player with the attack damage set to that value for the hit.
     */
    static void attackWithDamage(Player player, Entity target, int damage) {
        AttributeInstance attribute = player.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attribute == null) {
            player.attack(target);
            return;
        }
        attribute.removeModifier(CHARGE);
        double current = attribute.getValue();
        attribute.addTransientModifier(new AttributeModifier(CHARGE, damage - current, AttributeModifier.Operation.ADD_VALUE));
        try {
            player.attack(target);
        } finally {
            attribute.removeModifier(CHARGE);
        }
    }

    static boolean isLiving(Entity e) {
        return e instanceof LivingEntity;
    }
}
