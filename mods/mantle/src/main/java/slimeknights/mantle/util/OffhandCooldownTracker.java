package slimeknights.mantle.util;

import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.component.SwingAnimation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import slimeknights.mantle.Mantle;

import javax.annotation.Nullable;
import java.util.function.Function;

/**
 * Logic to handle offhand having its own cooldown
 *
 * <p>Port: the Forge capability became a NeoForge data attachment (not saved, like the capability), as in Mantle
 * 1.21. Swinging the arm uses the vanilla swing, which sends its own packet in 26.3 (Mantle's SwingArmPacket is gone).
 */
@SuppressWarnings("unused")  // API
public class OffhandCooldownTracker {
  private static final DeferredRegister<AttachmentType<?>> ATTACHMENTS = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, Mantle.modId);
  /** Attachment holding the cooldown of a player */
  public static final DeferredHolder<AttachmentType<?>, AttachmentType<OffhandCooldownTracker>> ATTACHMENT = ATTACHMENTS.register("offhand_cooldown", () -> AttachmentType.builder((IAttachmentHolder holder) -> new OffhandCooldownTracker(holder)).build());
  public static final Function<OffhandCooldownTracker,Float> COOLDOWN_TRACKER = OffhandCooldownTracker::getCooldown;
  public static final Function<OffhandCooldownTracker,Boolean> ATTACK_READY = OffhandCooldownTracker::isAttackReady;

  /** Registers the attachment type */
  public static void register(IEventBus modBus) {
    ATTACHMENTS.register(modBus);
  }

  @Nullable
  private final Player player;
  /** Cooldown of the last attack */
  private int lastCooldown = 0;
  /** Tick the attack will be ready */
  private int attackReady = 0;

  public OffhandCooldownTracker(@Nullable Player player) {
    this.player = player;
  }

  public OffhandCooldownTracker(IAttachmentHolder holder) {
    this(holder instanceof Player p ? p : null);
  }

  /** Gets the number of ticks the player has existed */
  private int getTicksExisted() {
    return player == null ? 0 : player.tickCount;
  }

  /**
   * Applies the given amount of cooldown
   * @param cooldown  Coolddown amount
   */
  public void applyCooldown(int cooldown) {
    this.lastCooldown = cooldown;
    this.attackReady = getTicksExisted() + cooldown;
  }

  /**
   * Returns a number from 0 to 1 denoting the current cooldown amount, akin to {@link Player#getAttackStrengthScale(float)}
   * @return  number from 0 to 1, with 1 being no cooldown
   */
  public float getCooldown() {
    int ticksExisted = getTicksExisted();
    if (ticksExisted > this.attackReady || this.lastCooldown == 0) {
      return 1.0f;
    }
    return Mth.clamp((this.lastCooldown + ticksExisted - this.attackReady) / (float) this.lastCooldown, 0f, 1f);
  }

  /**
   * Checks if we can perform another attack yet.
   * This counteracts rapid clicking for click events
   */
  public boolean isAttackReady() {
    return getTicksExisted() + this.lastCooldown > this.attackReady;
  }


  /* Helpers */

  /** Gets the tracker of the player */
  public static OffhandCooldownTracker get(Player player) {
    return player.getData(ATTACHMENT);
  }

  /**
   * Gets the offhand cooldown for the given player
   * @param player  Player
   * @return  Offhand cooldown
   */
  public static float getCooldown(Player player) {
    return get(player).getCooldown();
  }

  /**
   * Applies cooldown to the given player
   * @param player  Player
   * @param cooldown  Cooldown to apply
   */
  public static void applyCooldown(Player player, int cooldown) {
    get(player).applyCooldown(cooldown);
  }

  /**
   * Applies cooldown to the given player
   * @param player  Player
   * @return  True if attack is ready
   */
  public static boolean isAttackReady(Player player) {
    return get(player).isAttackReady();
  }

  /**
   * Applies cooldown using attack speed
   * @param attackSpeed   Attack speed of the held item
   * @param cooldownTime  Relative cooldown time for the given source, 20 is vanilla
   */
  public static void applyCooldown(Player player, float attackSpeed, int cooldownTime) {
    applyCooldown(player, Math.round(cooldownTime / attackSpeed));
  }

  /** Swings the entities hand without resetting cooldown */
  public static void swingHand(LivingEntity entity, InteractionHand hand, boolean updateSelf) {
    entity.swing(hand, SwingAnimation.DEFAULT, updateSelf);
  }
}
