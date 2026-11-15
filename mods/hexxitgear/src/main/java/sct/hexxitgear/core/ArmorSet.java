package sct.hexxitgear.core;

import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import sct.hexxitgear.HexxitGear;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * ArmorSet with the buff handlers BuffTribalSet, BuffThiefSet and BuffScaleSet: a player wearing all four
 * pieces of a set gets its buffs every tick. Declaration order is the order the original checked the sets in.
 */
public enum ArmorSet {
    TRIBAL("Tribal", List.of(HexxitGear.TRIBAL_HELMET, HexxitGear.TRIBAL_CHEST, HexxitGear.TRIBAL_LEGGINGS, HexxitGear.TRIBAL_BOOTS),
            ArmorSet::buffTribal),
    THIEF("Thief", List.of(HexxitGear.THIEF_HELMET, HexxitGear.THIEF_CHEST, HexxitGear.THIEF_LEGGINGS, HexxitGear.THIEF_BOOTS),
            ArmorSet::buffThief),
    SCALE("Scale", List.of(HexxitGear.SCALE_HELMET, HexxitGear.SCALE_CHEST, HexxitGear.SCALE_LEGGINGS, HexxitGear.SCALE_BOOTS),
            ArmorSet::buffScale);

    /** Thief set: landMovementFactor 0.2 instead of the walk speed 0.1 (0.13 sprinting). */
    public static final Identifier THIEF_SPEED = Identifier.fromNamespaceAndPath(HexxitGear.MOD_ID, "thief_speed");
    /** Thief set: jumpMovementFactor 0.1 (landMovementFactor * 0.5) instead of 0.02 (0.026 sprinting). */
    public static final float THIEF_AIR_SPEED = 0.1F;
    private static final double WALK_SPEED = 0.1;
    /** Today's sprint modifier: +30 % of the total, as 1.5.2 added 0.3 times the walk speed. */
    private static final double SPRINT_FACTOR = 1.3;

    private final String name;
    private final List<? extends Supplier<? extends Item>> armors;
    private final Consumer<Player> buffs;

    ArmorSet(String name, List<? extends Supplier<? extends Item>> armors, Consumer<Player> buffs) {
        this.name = name;
        this.armors = armors;
        this.buffs = buffs;
    }

    public String getName() {
        return this.name;
    }

    /** ArmorSet.getMatchingSet: the set of which all four pieces are worn, or null. */
    public static ArmorSet match(Player player) {
        List<Item> worn = new ArrayList<>(4);
        for (EquipmentSlot slot : new EquipmentSlot[] {EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD}) {
            if (!player.getItemBySlot(slot).isEmpty()) {
                worn.add(player.getItemBySlot(slot).getItem());
            }
        }
        ArmorSet found = null;
        for (ArmorSet set : values()) {
            int matched = 0;
            for (Supplier<? extends Item> armor : set.armors) {
                if (worn.contains(armor.get())) {
                    matched++;
                }
            }
            if (matched == 4) {
                found = set;
            }
        }
        return found;
    }

    /** IBuffHandler.applyPlayerBuffs, every tick while the set is active. */
    public void applyBuffs(Player player) {
        this.buffs.accept(player);
    }

    private static void effect(Player player, net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect> effect, int duration, int amplifier) {
        if (!player.level().isClientSide()) {
            player.addEffect(new MobEffectInstance(effect, duration, amplifier));
        }
    }

    /** BuffTribalSet: strength I (20 ticks), night vision (420 ticks), jump boost III (40 ticks). */
    private static void buffTribal(Player player) {
        effect(player, MobEffects.STRENGTH, 20, 0);
        effect(player, MobEffects.NIGHT_VISION, 420, 0);
        effect(player, MobEffects.JUMP_BOOST, 40, 2);
    }

    /** BuffThiefSet: strength I (20 ticks), night vision (420 ticks), faster on the ground and in the air. */
    private static void buffThief(Player player) {
        effect(player, MobEffects.STRENGTH, 20, 0);
        effect(player, MobEffects.NIGHT_VISION, 420, 0);
        // landMovementFactor = 0.2 replaced walking (0.1) and sprinting (0.13) speed alike
        double factor = 0.2 / (player.isSprinting() ? WALK_SPEED * SPRINT_FACTOR : WALK_SPEED) - 1.0;
        setThiefSpeed(player, factor);
    }

    /** BuffScaleSet: strength I, fire resistance (amplifier 1), resistance I, each 20 ticks. */
    private static void buffScale(Player player) {
        effect(player, MobEffects.STRENGTH, 20, 0);
        effect(player, MobEffects.FIRE_RESISTANCE, 20, 1);
        effect(player, MobEffects.RESISTANCE, 20, 0);
    }

    static void setThiefSpeed(Player player, double factor) {
        AttributeInstance speed = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed == null) {
            return;
        }
        AttributeModifier current = speed.getModifier(THIEF_SPEED);
        if (current != null && current.amount() == factor) {
            return;
        }
        speed.removeModifier(THIEF_SPEED);
        speed.addTransientModifier(new AttributeModifier(THIEF_SPEED, factor, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
    }

    static void clearThiefSpeed(Player player) {
        AttributeInstance speed = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null && speed.getModifier(THIEF_SPEED) != null) {
            speed.removeModifier(THIEF_SPEED);
        }
    }
}
