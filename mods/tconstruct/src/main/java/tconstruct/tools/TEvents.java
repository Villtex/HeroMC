package tconstruct.tools;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

/**
 * The parts of AbilityHelper.onLeftClickEntity that the attack damage attribute cannot express: the damage modifier of
 * the cleaver (x1.25), the rapier (x0.8) and the scythe (x0.75), applied to the whole hit (with critical hit and
 * enchantments, as in 1.5.2) and rounded down.
 */
public final class TEvents {
    private TEvents() {
    }

    public static void register() {
        NeoForge.EVENT_BUS.addListener(EventPriority.LOW, LivingIncomingDamageEvent.class, TEvents::onIncomingDamage);
    }

    private static void onIncomingDamage(LivingIncomingDamageEvent event) {
        DamageSource source = event.getSource();
        if (!(source.is(DamageTypes.PLAYER_ATTACK) || source.is(DamageTypes.MOB_ATTACK) || source.is(DamageTypes.MOB_ATTACK_NO_AGGRO))
                || !(source.getEntity() instanceof LivingEntity attacker) || source.getDirectEntity() != attacker) {
            return;
        }
        ItemStack stack = attacker.getMainHandItem();
        if (stack.getItem() instanceof ToolCoreItem tool && tool.type.damageModifier != 1.0F) {
            InfiTool t = InfiTool.of(stack);
            if (t != null && !t.broken) {
                event.setAmount((int) (event.getAmount() * tool.type.damageModifier));
            }
        }
    }
}
