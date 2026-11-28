package slimeknights.tconstruct.library.tools.helper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.ItemAbility;
import net.neoforged.neoforge.common.ItemAbilities;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.ModifierId;
import slimeknights.tconstruct.library.modifiers.hook.build.ConditionalStatModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.interaction.GeneralInteractionModifierHook;
import slimeknights.tconstruct.library.tools.definition.module.ToolHooks;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.library.tools.nbt.ToolDataComponents;
import slimeknights.tconstruct.library.tools.nbt.ModifierNBT;
import slimeknights.tconstruct.library.tools.stat.ToolStats;
import slimeknights.tconstruct.tools.TinkerTools;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/** Generic modifier hooks that don't quite fit elsewhere */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ModifierUtil {
  /** Shield blocking, a Forge tool action of 1.20.1 that NeoForge 26.3 no longer defines */
  public static final net.neoforged.neoforge.common.ItemAbility SHIELD_BLOCK = net.neoforged.neoforge.common.ItemAbility.get("shield_block");
  /** Persistent data key of the ammo drawn back by a launcher ({@code ModifiableLauncherItem.KEY_DRAWBACK_AMMO}, 27h) */
  private static final net.minecraft.resources.Identifier KEY_DRAWBACK_AMMO = slimeknights.tconstruct.TConstruct.getResource("drawback_ammo");

  /** Drops an item at the given position */
  public static void dropItem(Level level, double x, double y, double z, ItemStack stack) {
    if (!stack.isEmpty() && !level.isClientSide()) {
      ItemEntity ent = new ItemEntity(level, x, y, z, stack);
      ent.setDefaultPickUpDelay();
      RandomSource rand = level.random;
      ent.setDeltaMovement(ent.getDeltaMovement().add((rand.nextFloat() - rand.nextFloat()) * 0.1F,
                                                      rand.nextFloat() * 0.05F,
                                                      (rand.nextFloat() - rand.nextFloat()) * 0.1F));
      level.addFreshEntity(ent);
    }
  }

  /** Drops an item at the entity position */
  public static void dropItem(Entity target, ItemStack stack) {
    dropItem(target.level(), target.getX(), target.getY() + 1, target.getZ(), stack);
  }

  /** Drops an item at the entity position */
  public static void dropItem(Level level, Vec3 location, ItemStack stack) {
    dropItem(level, location.x(), location.y(), location.z(), stack);
  }

  /** Gets the entity as a living entity, or null if they are not a living entity */
  @Nullable
  public static LivingEntity asLiving(@Nullable Entity entity) {
    if (entity instanceof LivingEntity living) {
      return living;
    }
    return null;
  }

  /** Gets the entity as a player, or null if they are not a player */
  @Nullable
  public static Player asPlayer(@Nullable Entity entity) {
    if (entity instanceof Player player) {
      return player;
    }
    return null;
  }

  /**
   * Checks if the given entity pays the resource costs of using a tool, such as draining the tank or consuming ammo.
   * Creative players get their resources for free, matching {@link ToolDamageUtil#directDamage(IToolStackView, int, LivingEntity, ItemStack)} skipping durability for them.
   * @param entity  Entity using the tool. Anything that is not a creative player consumes, including mobs and null.
   * @return  True if resources should be consumed.
   */
  public static boolean consumesResources(@Nullable LivingEntity entity) {
    return !(entity instanceof Player player) || consumesResources(player);
  }

  /**
   * Checks if the given player pays the resource costs of using a tool, such as draining the tank or consuming ammo.
   * Creative players get their resources for free, matching {@link ToolDamageUtil#directDamage(IToolStackView, int, LivingEntity, ItemStack)} skipping durability for them.
   * @param player  Player using the tool. Null consumes, as only creative players get their resources for free.
   * @return  True if resources should be consumed.
   */
  public static boolean consumesResources(@Nullable Player player) {
    return player == null || !player.isCreative();
  }

  /**
   * Direct method to get the level of a modifier from a stack. If you need to get multiple modifier levels, using {@link ToolStack} is faster
   * @param stack     Stack to check
   * @param modifier  Modifier to search for
   * @return  Modifier level, or 0 if not present or the stack is not modifiable
   */
  public static int getModifierLevel(ItemStack stack, ModifierId modifier) {
    if (!stack.isEmpty() && stack.is(TinkerTags.Items.MODIFIABLE)) {
      ModifierNBT modifiers = stack.get(ToolDataComponents.MODIFIERS.get());
      if (modifiers != null) {
        return modifiers.getLevel(modifier);
      }
    }
    return 0;
  }

  /** Checks if the given stack has upgrades */
  public static boolean hasUpgrades(ItemStack stack) {
    if (!stack.isEmpty() && stack.is(TinkerTags.Items.MODIFIABLE)) {
      ModifierNBT upgrades = stack.get(ToolDataComponents.UPGRADES.get());
      return upgrades != null && !upgrades.isEmpty();
    }
    return false;
  }

  /** Checks if the given slot may contain armor */
  public static boolean validArmorSlot(LivingEntity living, EquipmentSlot slot) {
    return slot.isArmor() || living.getItemBySlot(slot).is(TinkerTags.Items.HELD);
  }

  /** Checks if the given slot may contain armor */
  public static boolean validArmorSlot(IToolStackView tool, EquipmentSlot slot) {
    return slot.isArmor() || tool.hasTag(TinkerTags.Items.HELD);
  }

  /** Volatile data of the stack, empty if none (26.3: data component) */
  private static CompoundTag volatileData(ItemStack stack) {
    CompoundTag tag = stack.get(ToolDataComponents.VOLATILE_DATA.get());
    return tag == null ? new CompoundTag() : tag;
  }

  /** Persistent data of the stack, empty if none (26.3: data component) */
  private static CompoundTag persistentData(ItemStack stack) {
    CompoundTag tag = stack.get(ToolDataComponents.PERSISTENT_DATA.get());
    return tag == null ? new CompoundTag() : tag;
  }

  /** Shortcut to get a volatile flag when the tool stack is not needed otherwise */
  public static boolean checkVolatileFlag(ItemStack stack, Identifier flag) {
    return volatileData(stack).getBooleanOr(flag.toString(), false);
  }

  /** Shortcut to get a persistent flag when the tool stack is not needed otherwise */
  public static boolean checkPersistentPresent(ItemStack stack, Identifier key) {
    // note: 1.20.1 checked the volatile data here as well
    return volatileData(stack).contains(key.toString());
  }

  /** Shortcut to get a volatile int value when the tool stack is not needed otherwise */
  public static int getVolatileInt(ItemStack stack, Identifier flag) {
    return volatileData(stack).getIntOr(flag.toString(), 0);
  }

  /** Shortcut to get a volatile int value when the tool stack is not needed otherwise */
  public static int getPersistentInt(ItemStack stack, Identifier flag, int defealtValue) {
    CompoundTag persistent = persistentData(stack);
    String flagString = flag.toString();
    if (slimeknights.mantle.util.TagHelper.contains(persistent, flagString, Tag.TAG_INT)) {
      return persistent.getIntOr(flagString, 0);
    }
    return defealtValue;
  }

  /** Shortcut to get a persistent string value when the tool stack is not needed otherwise */
  public static String getPersistentString(ItemStack stack, Identifier flag) {
    return persistentData(stack).getStringOr(flag.toString(), "");
  }

  /** Checks if a tool can perform the given action */
  public static boolean canPerformAction(IToolStackView tool, ItemAbility action) {
    if (!tool.isBroken()) {
      // can the tool do this action inherently?
      if (tool.getHook(ToolHooks.TOOL_ACTION).canPerformAction(tool, action)) {
        return true;
      }
      for (ModifierEntry entry : tool.getModifierList()) {
        if (entry.getHook(ModifierHooks.TOOL_ACTION).canPerformAction(tool, entry, action)) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Makes the tool use the blocking animation if the blocking modifier is installed, falling back to the given animation.
   * Allows your tool to block while charging up.
   */
  public static ItemUseAnimation blockWhileCharging(IToolStackView tool, ItemUseAnimation fallback) {
    return canPerformAction(tool, slimeknights.tconstruct.library.tools.helper.ModifierUtil.SHIELD_BLOCK) ? ItemUseAnimation.BLOCK : fallback;
  }

  /** Calculates inaccuracy from the conditional tool stat. */
  public static float getInaccuracy(IToolStackView tool, @Nullable LivingEntity living) {
    return 3 * (1 / ConditionalStatModifierHook.getModifiedStat(tool, living, ToolStats.ACCURACY) - 1);
  }

  /** @deprecated use {@link GeneralInteractionModifierHook#addCooldown(IToolStackView, Player, float)} */
  @Deprecated(forRemoval = true)
  public static void addCooldown(IToolStackView tool, Player player) {
    GeneralInteractionModifierHook.addCooldown(tool, player, 1);
  }

  /** Checks if this modifier is the one actively being used. Used for failure sound effects. */
  public static boolean isActiveModifier(IToolStackView tool, ModifierEntry modifier, ModifierEntry activeModifier) {
    // active modifier being us, or a bow is firing and no drawback ammo
    return modifier == activeModifier || (activeModifier.getLevel() == 0 && !tool.getPersistentData().contains(KEY_DRAWBACK_AMMO));
  }

  /**
   * Called before you call {@link Projectile#discard()} to update the fishing rod stack on the player.
   *
   * @param projectile  Projectile, will check if its our fishing bobber.
   * @param damage      Damage to deal to the rod.
   * @param applyCooldown  If true, applies draw speed as an item cooldown.
   * @return hand containing the fishing rod, or null if its in neither hand.
   */
  @SuppressWarnings("UnusedReturnValue") // API
  @Nullable
  public static InteractionHand updateFishingRod(Projectile projectile, int damage, boolean applyCooldown) {
    return updateFishingRod(projectile, damage, applyCooldown, ModifierId.EMPTY);
  }

  /**
   * Called before you call {@link Projectile#discard()} to update the fishing rod stack on the player.
   *
   * @param projectile  Projectile, will check if its our fishing bobber.
   * @param damage      Damage to deal to the rod.
   * @param applyCooldown  If true, applies draw speed as an item cooldown.
   * @param cause       Modifier causing the retraction.
   * @return hand containing the fishing rod, or null if its in neither hand.
   */
  @Nullable
  public static InteractionHand updateFishingRod(Projectile projectile, int damage, boolean applyCooldown, ModifierId cause) {
    if (TinkerTools.fishingHook.isPresent() && projectile.getType() == TinkerTools.fishingHook.get() && projectile.getOwner() instanceof LivingEntity living) {
      ItemStack stack = living.getMainHandItem();
      InteractionHand hand = InteractionHand.MAIN_HAND;
      // must be able to cast
      if (!stack.canPerformAction(ItemAbilities.FISHING_ROD_CAST)) {
        stack = living.getOffhandItem();
        if (!stack.canPerformAction(ItemAbilities.FISHING_ROD_CAST)) {
          return null;
        }
        hand = InteractionHand.OFF_HAND;
      }
      // must be modifiable
      if (stack.is(TinkerTags.Items.MODIFIABLE)) {
        // skip making the tool stack object if not needed, might be asking just for the hand.
        if (applyCooldown || damage > 0) {
          IToolStackView tool = ToolStack.from(stack);
          // trigger cooldown on the item
          if (applyCooldown && living instanceof Player player) {
            addCooldown(tool, player);
          }
          // damage the rod
          if (damage > 0) {
            // if we are applying cooldown, means this is a full retraction from block so this was primary damage
            // no cooldown is done on secondary effects like entity hitting
            ToolDamageUtil.damageAnimated(tool, damage, living, hand, cause);
          }
        }
        return hand;
      }
    }
    return null;
  }

  /** Interface used for {@link #foodConsumer} */
  public interface FoodConsumer {
    /** Called when food is eaten to notify compat that food was eaten */
    void onConsume(Player player, ItemStack stack, int hunger, float saturation);

    /** Called when a list of foods is eaten at once is eaten to notify compat that food was eaten */
    default void onConsume(Player player, List<ItemStack> stacks, int hunger, float saturation) {}
  }

  /** Instance of the current food consumer, will be either no-op or an implementation calling the Diet API, never null. */
  @Nonnull
  public static FoodConsumer foodConsumer = (player, stack, hunger, saturation) -> {};

  /* Shield disabling */
  /** Map of how to disable shields for different targets */
  private static final Map<EntityType<?>, Consumer<Entity>> SHIELD_DISABLER = new HashMap<>();

  /** Registers a method for shield disabling */
  public static void registerShieldDisabler(Consumer<Entity> disabler, EntityType<?>... types) {
    for (EntityType<?> type : types){
      SHIELD_DISABLER.putIfAbsent(type, disabler);
    }
  }

  /** Disables shield for the target entity */
  public static void disableShield(Entity entity) {
    Consumer<Entity> consumer = SHIELD_DISABLER.get(entity.getType());
    if (consumer != null) {
      consumer.accept(entity);
    }
  }
}
