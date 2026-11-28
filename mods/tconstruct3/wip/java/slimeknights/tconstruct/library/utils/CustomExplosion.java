package slimeknights.tconstruct.library.utils;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Explosion.BlockInteraction;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerExplosion;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.EventHooks;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Helper class for more control over explosions.
 * <p>Port (Teil 27e): in 26.3 {@link Explosion} is an interface and {@link ServerExplosion} keeps its logic private.
 * The block part is the vanilla explosion; damage amount, knockback, the entity filter and bypassing the invulnerable
 * time come in through an {@link ExplosionDamageCalculator}, which the vanilla explosion asks for each entity.
 * Differences to 1.20.1: damage is computed from the vanilla exposure (same formula with {@link #damage} instead of
 * {@code 7 * 2 * radius}); explosions only run on the server.
 */
public class CustomExplosion {
  /** Default predicate for which entities to match */
  public static final Predicate<Entity> DEFAULT_ENTITY_PREDICATE = entity -> entity != null && entity.isAlive() && !entity.isSpectator();

  protected final Level level;
  protected final Vec3 location;
  protected final float radius;
  @Nullable
  protected final Entity source;
  @Nullable
  protected final DamageSource damageSource;
  @Nullable
  protected final ExplosionDamageCalculator damageCalculator;
  protected final boolean fire;
  protected final BlockInteraction blockInteraction;
  /** Maximum damage to deal; setting to 7*2*radius will match the vanilla explosion. */
  protected final float damage;
  /** Entity knockback scale. May be 0 to prevent knockback or negative to reverse knockback */
  protected final float knockback;
  /** Determines which entities are affected by this explosion */
  protected final Predicate<Entity> entityPredicate;
  /** If true, explosion damage bypasses the invulnerability time */
  protected final boolean bypassInvulnerableTime;

  public CustomExplosion(Level level, Vec3 location, float radius, @Nullable Entity sourceEntity, @Nullable Predicate<Entity> entityPredicate, float damage, @Nullable DamageSource damageSource, float knockback, @Nullable ExplosionDamageCalculator damageCalculator, boolean placeFire, BlockInteraction blockInteraction, boolean bypassInvulnerableTime) {
    this.level = level;
    this.location = location;
    this.radius = radius;
    this.source = sourceEntity;
    this.damageSource = damageSource;
    this.damageCalculator = damageCalculator;
    this.fire = placeFire;
    this.blockInteraction = blockInteraction;
    this.entityPredicate = Objects.requireNonNullElse(entityPredicate, DEFAULT_ENTITY_PREDICATE);
    this.damage = damage;
    this.knockback = knockback;
    this.bypassInvulnerableTime = bypassInvulnerableTime;
  }

  public CustomExplosion(Level level, Vec3 location, float radius, @Nullable Entity sourceEntity, @Nullable Predicate<Entity> entityPredicate, float damage, @Nullable DamageSource damageSource, float knockback, @Nullable ExplosionDamageCalculator damageCalculator, boolean placeFire, BlockInteraction blockInteraction) {
    this(level, location, radius, sourceEntity, entityPredicate, damage, damageSource, knockback, damageCalculator, placeFire, blockInteraction, false);
  }

  /** Runs the logic on the server, syncing to the client. Based on {@link ServerLevel#explode} */
  public void handleServer() {
    if (!(level instanceof ServerLevel server)) {
      return;
    }
    DamageSource source = damageSource != null ? damageSource : Explosion.getDefaultDamageSource(level, this.source);
    ServerExplosion explosion = new ServerExplosion(server, this.source, source, new Calculator(), location, radius, fire, blockInteraction);
    if (EventHooks.onExplosionStart(level, explosion)) {
      return;
    }
    int blockCount = explosion.explode();
    boolean large = radius >= 2 && blockInteraction != BlockInteraction.KEEP;
    for (ServerPlayer player : server.players()) {
      if (player.distanceToSqr(location) < 4096) {
        Optional<Vec3> knockback = Optional.ofNullable(explosion.getHitPlayers().get(player));
        player.connection.send(new ClientboundExplodePacket(location, radius, blockCount, knockback, large ? ParticleTypes.EXPLOSION_EMITTER : ParticleTypes.EXPLOSION, SoundEvents.GENERIC_EXPLODE, WeightedList.of(), true));
      }
    }
  }

  /** @deprecated 26.3: explosions run on the server only, use {@link #handleServer()} */
  @Deprecated
  public void doDualSide(Level level, boolean spawnParticles) {
    handleServer();
  }

  /** Damage calculator with the settings of this explosion, delegating blocks to the given calculator */
  private class Calculator extends ExplosionDamageCalculator {
    @Override
    public Optional<Float> getBlockExplosionResistance(Explosion explosion, BlockGetter level, BlockPos pos, BlockState state, FluidState fluid) {
      if (damageCalculator != null) {
        return damageCalculator.getBlockExplosionResistance(explosion, level, pos, state, fluid);
      }
      Optional<Float> resistance = super.getBlockExplosionResistance(explosion, level, pos, state, fluid);
      if (source != null && resistance.isPresent()) {
        return Optional.of(source.getBlockExplosionResistance(explosion, level, pos, state, fluid, resistance.get()));
      }
      return resistance;
    }

    @Override
    public boolean shouldBlockExplode(Explosion explosion, BlockGetter level, BlockPos pos, BlockState state, float power) {
      if (damageCalculator != null) {
        return damageCalculator.shouldBlockExplode(explosion, level, pos, state, power);
      }
      return source == null || source.shouldBlockExplode(explosion, level, pos, state, power);
    }

    @Override
    public boolean shouldDamageEntity(Explosion explosion, Entity entity) {
      if (damage <= 0 || !entityPredicate.test(entity) || (damageCalculator != null && !damageCalculator.shouldDamageEntity(explosion, entity))) {
        return false;
      }
      // the vanilla explosion damages right after asking
      if (bypassInvulnerableTime) {
        entity.setInvulnerableTime(0);
      }
      return true;
    }

    @Override
    public float getKnockbackMultiplier(Entity entity) {
      return entityPredicate.test(entity) ? knockback : 0;
    }

    @Override
    public float getEntityDamageAmount(Explosion explosion, Entity entity, float seenPercent) {
      double distance = Math.sqrt(entity.distanceToSqr(location)) / (radius * 2);
      double strength = (1 - distance) * seenPercent;
      return (float) ((int) ((strength * strength + strength) / 2 * damage + 1));
    }
  }
}
