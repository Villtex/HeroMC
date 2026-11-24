package tconstruct.tools;

import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.CriticalHitEvent;

/**
 * The parts of AbilityHelper.onLeftClickEntity that the attack damage attribute cannot express, for the normal swing
 * of a player or a mob: the sprint lunge of the longsword (x1.5), the damage of the modifiers (smite, bane of
 * arthropods, the hammer against the undead), the damage modifier of the cleaver (x1.25), the rapier (x0.8) and the
 * scythe (x0.75), all rounded down; the rapier pierces armor; the cutlass hits critically one time in ten.
 */
public final class TEvents {
    private static final RandomSource RANDOM = RandomSource.createThreadSafe();

    private TEvents() {
    }

    public static void register() {
        NeoForge.EVENT_BUS.addListener(EventPriority.LOW, LivingIncomingDamageEvent.class, TEvents::onIncomingDamage);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOW, CriticalHitEvent.class, TEvents::onCriticalHit);
        ToolAbilities.register();
    }

    private static void onIncomingDamage(LivingIncomingDamageEvent event) {
        DamageSource source = event.getSource();
        if (ToolCombat.ATTACKING.get()
                || !(source.is(DamageTypes.PLAYER_ATTACK) || source.is(DamageTypes.MOB_ATTACK) || source.is(DamageTypes.MOB_ATTACK_NO_AGGRO))
                || !(source.getEntity() instanceof LivingEntity attacker) || source.getDirectEntity() != attacker) {
            return;
        }
        ItemStack stack = attacker.getMainHandItem();
        if (!(stack.getItem() instanceof ToolCoreItem tool)) {
            return;
        }
        InfiTool t = InfiTool.of(stack);
        if (t == null || t.broken) {
            return;
        }
        float amount = event.getAmount();
        if (attacker.isSprinting() && tool.type.chargeAttack() > 1.0F) {
            amount = (int) (amount * tool.type.chargeAttack());
        }
        amount += ToolCombat.modDamage(tool.type, t, event.getEntity());
        if (tool.type.damageModifier != 1.0F) {
            amount = (int) (amount * tool.type.damageModifier);
        }
        event.setAmount(amount);
        if (tool.type.pierceArmor()) {
            event.addReductionModifier(DamageContainer.Reduction.ARMOR, (container, reduction) -> 0.0F);
        }
    }

    /** TActiveOmniMod.doesCriticalHit: the cutlass in 10 (the bonus of 1.5.2: 0..damage/2+1). */
    private static void onCriticalHit(CriticalHitEvent event) {
        ItemStack stack = event.getEntity().getMainHandItem();
        if (event.isCriticalHit() || !(stack.getItem() instanceof ToolCoreItem tool) || tool.type != ToolType.CUTLASS || RANDOM.nextInt(10) != 0) {
            return;
        }
        InfiTool t = InfiTool.of(stack);
        if (t == null || t.broken) {
            return;
        }
        int damage = ToolCoreItem.attackDamage(t);
        event.setCriticalHit(true);
        event.setDamageMultiplier((damage + RANDOM.nextInt(damage / 2 + 2)) / (float) damage);
    }
}
