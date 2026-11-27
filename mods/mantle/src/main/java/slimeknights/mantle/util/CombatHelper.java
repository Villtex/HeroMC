package slimeknights.mantle.util;

import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileDeflection;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;
import net.neoforged.neoforge.entity.PartEntity;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.entity.player.CriticalHitEvent;
import net.neoforged.neoforge.event.entity.player.SweepAttackEvent;
import slimeknights.mantle.Mantle;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Helpers for attacking with weapons */
@SuppressWarnings("unused")  // API
public class CombatHelper {
  private static final float TO_RADIAN = (float)Math.PI / 180f;
  /** Attribute modifier to disable knockback on a target */
  private static final AttributeModifier ANTI_KNOCKBACK_MODIFIER = new AttributeModifier(Mantle.getResource("anti_knockback"), 1f, Operation.ADD_VALUE);
  /** Tool action to disable the base knockback of the weapon. Requires replacing left click behavior of your weapon. */
  public static final ItemAbility NO_BASE_KNOCKBACK = ItemAbility.get("no_base_knockback");

  private CombatHelper() {}

  /** Gets the item stack in the main hand that contributes to attributes. Exposed for benefit of Tinkers' Construct which can optimize these methods for its tools. */
  public static ItemStack getMainhandAttributeStack(LivingEntity entity) {
    // clientside does not use last item stack, so our best choice is the mainhand stack
    if (entity.level().isClientSide()) {
      return entity.getMainHandItem();
    }
    // serverside, use the last item stack instead of the current. Should be the same, but if they mismatch then last item stack has correct attributes
    // Port: the last hand item is private in 26.3; between ticks it matches the current one
    return entity.getMainHandItem();
  }

  /**
   * Gets a modifiable map that is a copy of the modifiers from the given attribute instance. All operations are guaranteed to have a valid set.
   * Note we use a map instead of a full attribute instance as we don't need the cache or other data structures.
   */
  public static Map<Operation, Set<AttributeModifier>> copyModifiers(AttributeInstance instance) {
    Map<Operation, Set<AttributeModifier>> modifiers = new EnumMap<>(Operation.class);
    for (Operation operation : Operation.values()) {
      Set<AttributeModifier> copy = new HashSet<>();
      for (AttributeModifier modifier : instance.getModifiers()) {
        if (modifier.operation() == operation) {
          copy.add(modifier);
        }
      }
      modifiers.put(operation, copy);
    }
    return modifiers;
  }

  /**
   * Gets a mutable list of modifiers on the given stack for the given slot.
   * @param stack   Stack instance
   * @param slot    Slot to check
   * @param match   Attribute to match
   * @return  List of modifiers.
   */
  public static List<AttributeModifier> getModifiers(ItemStack stack, EquipmentSlot slot, Holder<Attribute> match) {
    List<AttributeModifier> modifiers = new ArrayList<>();
    stack.forEachModifier(slot, (attribute, modifier) -> {
      if (match.equals(attribute)) {
        modifiers.add(modifier);
      }
    });
    return modifiers;
  }

  /** Gets the attribute for the offhand by subtracting mainhand attributes and adding in offhand stack attributes. */
  public static float getOffhandAttribute(ItemStack stack, LivingEntity entity, Holder<Attribute> attribute) {
    AttributeInstance instance = entity.getAttribute(attribute);
    if (instance == null) {
      return (float) entity.getAttributeBaseValue(attribute);
    }

    // fetch attributes for both relevant stacks
    ItemStack mainStack = getMainhandAttributeStack(entity);
    Collection<AttributeModifier> mainModifiers = List.of();
    if (!mainStack.isEmpty()) {
      mainModifiers = getModifiers(mainStack, EquipmentSlot.MAINHAND, attribute);
    }
    Collection<AttributeModifier> offhandModifiers = getModifiers(stack, EquipmentSlot.MAINHAND, attribute);

    // if no modifier changed, can save some work by just using the cached value
    if (mainModifiers.isEmpty() && offhandModifiers.isEmpty()) {
      return (float) instance.getValue();
    }

    // start by creating a modifiable copy of the per operation attribute map
    Map<Operation, Set<AttributeModifier>> modifiers = copyModifiers(instance);
    // remove all mainhand modifiers
    for (AttributeModifier modifier : mainModifiers) {
      modifiers.get(modifier.operation()).remove(modifier);
    }
    // add in all offhand modifiers
    for (AttributeModifier modifier : offhandModifiers) {
      // while there should be no duplicates due to mainhand modifiers above,
      // this will remove duplicates due to AttributeModifier equals only checking UUID
      modifiers.get(modifier.operation()).add(modifier);
    }
    // compute the value
    return (float) computeAttribute(attribute, instance.getBaseValue(), modifiers);
  }

  /** Computes the value for the given attribute. Copied from {@link AttributeInstance#calculateValue} */
  public static double computeAttribute(Holder<Attribute> attribute, double base, Map<Operation,Set<AttributeModifier>> modifiers) {
    // addition modifiers
    for (AttributeModifier modifier : modifiers.get(Operation.ADD_VALUE)) {
      base += modifier.amount();
    }
    // multiply base
    double value = base;
    for (AttributeModifier modifier : modifiers.get(Operation.ADD_MULTIPLIED_BASE)) {
      value += base * modifier.amount();
    }
    // multiply total
    for (AttributeModifier modifier : modifiers.get(Operation.ADD_MULTIPLIED_TOTAL)) {
      value *= 1.0 + modifier.amount();
    }
    return attribute.value().sanitizeValue(value);
  }

  /** Gets the value of the attribute for the given hand, using {@link #getOffhandAttribute(ItemStack, LivingEntity, Holder)} for the offhand and {@link LivingEntity#getAttributeValue(Holder)} for mainhand. */
  public static float getAttribute(ItemStack stack, LivingEntity entity, Holder<Attribute> attribute, InteractionHand hand) {
    if (hand == InteractionHand.MAIN_HAND) {
      return (float) entity.getAttributeValue(attribute);
    } else {
      return getOffhandAttribute(stack, entity, attribute);
    }
  }

  /** Checks if the given entity can be attacked. */
  public static boolean isAttackable(Entity attacker, Entity target) {
    return target.isAttackable() && !target.skipAttackInteraction(attacker);
  }

  /**
   * Performs an attack, mimicking  {@link Player#attack(Entity)}.
   * For use in {@link net.minecraft.world.item.Item#interactLivingEntity(ItemStack, Player, LivingEntity, InteractionHand)} primarily,
   * but can also be used to fake an attack similar to {@link net.neoforged.neoforge.common.extensions.IItemExtension#onLeftClickEntity(ItemStack, Player, Entity)}.
   *
   * @param stack         Stack used for attacking.
   * @param target        Entity target
   * @param targetLiving  Target as a living entity. Should be the same instance as {@code target} even for multipart.
   * @param hand          Hand used for attacking.
   */
  public static boolean attack(ItemStack stack, Player player, Entity target, @Nullable LivingEntity targetLiving, InteractionHand hand) {
    return attack(stack, player, target, targetLiving, hand, player.damageSources().playerAttack(player));
  }

  /**
   * Performs an attack, mimicking {@link Player#attack(Entity)} but allowing the damage source to be swapped.
   * For use in {@link net.minecraft.world.item.Item#interactLivingEntity(ItemStack, Player, LivingEntity, InteractionHand)} primarily,
   * but can also be used to fake an attack similar to {@link net.neoforged.neoforge.common.extensions.IItemExtension#onLeftClickEntity(ItemStack, Player, Entity)}.
   *
   * @param stack         Stack used for attacking.
   * @param target        Entity target
   * @param targetLiving  Target as a living entity. Should be the same instance as {@code target} even for multipart.
   * @param hand          Hand used for attacking.
   * @param damageSource  Damage source to apply
   */
  public static boolean attack(ItemStack stack, Player player, Entity target, @Nullable LivingEntity targetLiving, InteractionHand hand, DamageSource damageSource) {
    // Port: instead of a copy of Player#attack (which changed a lot up to 26.3), the attack runs through the vanilla
    // method itself. For the offhand, both hands are swapped for the duration of the attack (with their attribute
    // modifiers) and the attack strength follows the offhand cooldown. The damage source of the weapon is used.
    if (!isAttackable(player, target)) {
      return false;
    }
    if (hand == InteractionHand.MAIN_HAND) {
      player.attack(target);
      return true;
    }
    Inventory inventory = player.getInventory();
    int mainSlot = inventory.getSelectedSlot();
    ItemStack mainStack = inventory.getItem(mainSlot);
    ItemStack offStack = inventory.getItem(Inventory.SLOT_OFFHAND);
    AttributeMap attributes = player.getAttributes();
    int oldTicker = getAttackStrengthTicker(player);
    try {
      removeModifiers(attributes, mainStack);
      inventory.setItem(mainSlot, offStack);
      inventory.setItem(Inventory.SLOT_OFFHAND, mainStack);
      addModifiers(attributes, offStack);
      // attack strength scale = clamp((ticker + 0.5) / delay)
      float cooldown = OffhandCooldownTracker.getCooldown(player);
      setAttackStrengthTicker(player, Math.round(cooldown * player.getCurrentItemAttackStrengthDelay() - 0.5f));
      player.attack(target);
    } finally {
      removeModifiers(attributes, offStack);
      inventory.setItem(mainSlot, mainStack);
      inventory.setItem(Inventory.SLOT_OFFHAND, offStack);
      addModifiers(attributes, mainStack);
      setAttackStrengthTicker(player, oldTicker);
    }
    OffhandCooldownTracker.applyCooldown(player, getOffhandAttribute(stack, player, Attributes.ATTACK_SPEED), 20);
    return true;
  }

  /** Removes the main hand modifiers of the stack */
  private static void removeModifiers(AttributeMap attributes, ItemStack stack) {
    if (!stack.isEmpty()) {
      stack.forEachModifier(EquipmentSlot.MAINHAND, (attribute, modifier) -> {
        AttributeInstance instance = attributes.getInstance(attribute);
        if (instance != null) {
          instance.removeModifier(modifier);
        }
      });
    }
  }

  /** Adds the main hand modifiers of the stack */
  private static void addModifiers(AttributeMap attributes, ItemStack stack) {
    if (!stack.isEmpty()) {
      stack.forEachModifier(EquipmentSlot.MAINHAND, (attribute, modifier) -> {
        AttributeInstance instance = attributes.getInstance(attribute);
        if (instance != null) {
          instance.removeModifier(modifier.id());
          instance.addTransientModifier(modifier);
        }
      });
    }
  }

  private static java.lang.reflect.Field attackStrengthTicker;

  /** Field {@code LivingEntity#attackStrengthTicker} (protected) */
  private static java.lang.reflect.Field attackStrengthTickerField() {
    if (attackStrengthTicker == null) {
      try {
        java.lang.reflect.Field field = LivingEntity.class.getDeclaredField("attackStrengthTicker");
        field.setAccessible(true);
        attackStrengthTicker = field;
      } catch (ReflectiveOperationException e) {
        throw new IllegalStateException("Missing LivingEntity#attackStrengthTicker", e);
      }
    }
    return attackStrengthTicker;
  }

  private static int getAttackStrengthTicker(LivingEntity entity) {
    try {
      return attackStrengthTickerField().getInt(entity);
    } catch (IllegalAccessException e) {
      throw new IllegalStateException(e);
    }
  }

  private static void setAttackStrengthTicker(LivingEntity entity, int value) {
    try {
      attackStrengthTickerField().setInt(entity, value);
    } catch (IllegalAccessException e) {
      throw new IllegalStateException(e);
    }
  }


  /* Damage source creation */

  /** Makes a damage source from the given key */
  public static Holder<DamageType> damageType(RegistryAccess access, ResourceKey<DamageType> key) {
    return access.lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(key);
  }

  /** Makes a damage source from the given key */
  public static DamageSource damageSource(RegistryAccess access, ResourceKey<DamageType> key) {
    return new DamageSource(damageType(access, key));
  }

  /** Makes a damage source from the given key */
  public static DamageSource damageSource(Level level, ResourceKey<DamageType> key) {
    return new DamageSource(damageType(level.registryAccess(), key));
  }

  /** Makes a damage source from the given key for direct damage from an entity. */
  public static DamageSource damageSource(ResourceKey<DamageType> key, Entity entity) {
    return new DamageSource(damageType(entity.level().registryAccess(), key), entity);
  }

  /** Makes a damage source from the given key for indirect damage, such as from a projectile. */
  public static DamageSource damageSource(ResourceKey<DamageType> key, Entity direct, @Nullable Entity causing) {
    return new DamageSource(damageType(direct.level().registryAccess(), key), direct, causing);
  }
}
