package slimeknights.tconstruct.library.client.model;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperty;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.neoforged.neoforge.client.event.RegisterConditionalItemModelPropertyEvent;
import net.neoforged.neoforge.common.ItemAbilities;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.modifiers.hook.interaction.GeneralInteractionModifierHook;
import slimeknights.tconstruct.library.tools.helper.ModifierUtil;
import slimeknights.tconstruct.library.tools.helper.ToolDamageUtil;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * Properties for tinker tools.
 * Port (Teil 27f2): 26.3 has no item property functions and no {@code overrides} in item models. The properties keep
 * their 1.20.1 values and are read by the condition {@code tconstruct3:predicate}, which the asset import uses to turn
 * every 1.20.1 override (all values at least the given ones) into a {@code minecraft:condition} of the item model
 * definition. They are no longer registered per item: each property answers 0 for stacks it does not apply to.
 */
public class TinkerItemProperties {
  /** Value of an item property */
  @FunctionalInterface
  public interface Property {
    float get(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity holder, int seed);
  }

  /** All properties by ID */
  private static final Map<Identifier, Property> PROPERTIES = new HashMap<>();

  /** ID for broken property */
  private static final Identifier BROKEN_ID = TConstruct.getResource("broken");
  /** Property declaring broken */
  private static final Property BROKEN = (stack, level, entity, seed) -> ToolDamageUtil.isBroken(stack) ? 1 : 0;

  /** ID for ammo property */
  private static final Identifier AMMO_ID = TConstruct.getResource("ammo");
  /** ID of fireworks rocket for NBT check */
  private static final String FIREWORKS_ID = "minecraft:firework_rocket";
  /** Persistent data key of the loaded crossbow ammo, 1.20.1 {@code ModifiableCrossbowItem.KEY_CROSSBOW_AMMO} (27h) */
  public static final Identifier KEY_CROSSBOW_AMMO = TConstruct.getResource("crossbow_ammo");
  /** Persistent data key of ammo drawn back, 1.20.1 {@code ModifiableLauncherItem.KEY_DRAWBACK_AMMO} (27h) */
  public static final Identifier KEY_DRAWBACK_AMMO = TConstruct.getResource("drawback_ammo");
  /** Int declaring ammo type */
  private static final Property AMMO = (stack, level, entity, seed) -> {
    if (ModifierUtil.checkPersistentPresent(stack, KEY_CROSSBOW_AMMO)) {
      CompoundTag ammo = ToolStack.from(stack).getPersistentData().getCompound(KEY_CROSSBOW_AMMO);
      if (!ammo.isEmpty()) {
        // no sense having two keys for ammo, just set 1 for arrow, 2 for fireworks
        return FIREWORKS_ID.equals(ammo.getStringOr("id", "")) ? 2 : 1;
      }
    }
    return 0;
  };

  /** ID for the pulling property */
  private static final Identifier CHARGING_ID = TConstruct.getResource("charging");
  /** Boolean indicating the bow is pulling */
  private static final Property CHARGING = (stack, level, holder, seed) -> {
    if (holder != null && holder.isUsingItem() && holder.getUseItem() == stack) {
      ItemUseAnimation anim = stack.getUseAnimation();
      if (anim == ItemUseAnimation.BLOCK) {
        return ModifierUtil.checkPersistentPresent(stack, KEY_DRAWBACK_AMMO) ? 2.5f : 2;
      }
      if (anim == ItemUseAnimation.SPEAR) {
        // shouldn't need to worry about arrows on spearing, everything supporting arrows uses just bow or block
        return 1.75f;
      }
      if (anim != ItemUseAnimation.EAT && anim != ItemUseAnimation.DRINK) {
        return ModifierUtil.checkPersistentPresent(stack, KEY_DRAWBACK_AMMO) ? 1.5f : 1;
      }
    }
    return 0;
  };
  /** ID for the pull property */
  private static final Identifier CHARGE_ID = TConstruct.getResource("charge");
  /** Property for bow pull amount */
  private static final Property CHARGE = (stack, level, holder, seed) -> {
    if (holder == null || holder.getUseItem() != stack) {
      return 0.0F;
    }
    int drawtime = ModifierUtil.getPersistentInt(stack, GeneralInteractionModifierHook.KEY_DRAWTIME, -1);
    return drawtime == -1 ? 0 : (float)(stack.getUseDuration(holder) - holder.getUseItemRemainingTicks()) / drawtime;
  };
  /** ID for the cast fishing rods */
  private static final Identifier CAST_ID = TConstruct.getResource("cast");
  /** Property for casting a fishing rod */
  private static final Property CAST = (stack, level, holder, seed) -> {
    // must be a fishing rod, and the player must be fishing
    // does player check first since its the fastest, avoids NBT parsing
    if (holder instanceof Player player && player.fishing != null && stack.canPerformAction(ItemAbilities.FISHING_ROD_CAST)) {
      // must be in a hand, but if both hands have fishing rods, must be the one in the main hand
      ItemStack mainhand = holder.getMainHandItem();
      if (mainhand == stack || holder.getOffhandItem() == stack && !mainhand.canPerformAction(ItemAbilities.FISHING_ROD_CAST)) {
        return 1;
      }
    }
    return 0;
  };

  static {
    register(BROKEN_ID, BROKEN);
    register(AMMO_ID, AMMO);
    register(CHARGING_ID, CHARGING);
    register(CHARGE_ID, CHARGE);
    register(CAST_ID, CAST);
  }

  /** Registers a property for the predicate condition */
  public static void register(Identifier id, Property property) {
    PROPERTIES.put(id, property);
  }

  /** Gets the value of a property, 0 if unknown */
  public static float get(Identifier id, ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity holder, int seed) {
    Property property = PROPERTIES.get(id);
    return property != null ? property.get(stack, level, holder, seed) : 0;
  }

  /** Registers the condition */
  public static void registerConditions(RegisterConditionalItemModelPropertyEvent event) {
    event.register(TConstruct.getResource("predicate"), Predicate.MAP_CODEC);
  }

  /**
   * Condition {@code {"property": "tconstruct3:predicate", "predicate": {"tconstruct3:charging": 1, ...}}}: true if
   * every property is at least the given value, the test of a 1.20.1 model override.
   */
  public record Predicate(Map<Identifier, Float> predicate) implements ConditionalItemModelProperty {
    public static final MapCodec<Predicate> MAP_CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
      Codec.unboundedMap(Identifier.CODEC, Codec.FLOAT).fieldOf("predicate").forGetter(Predicate::predicate)
    ).apply(inst, Predicate::new));

    @Override
    public boolean get(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity owner, int seed, ItemDisplayContext context) {
      for (Map.Entry<Identifier, Float> entry : predicate.entrySet()) {
        if (TinkerItemProperties.get(entry.getKey(), stack, level, owner, seed) < entry.getValue()) {
          return false;
        }
      }
      return true;
    }

    @Override
    public MapCodec<Predicate> type() {
      return MAP_CODEC;
    }
  }
}
