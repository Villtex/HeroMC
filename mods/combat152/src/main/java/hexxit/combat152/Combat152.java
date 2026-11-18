package hexxit.combat152;

import net.minecraft.client.AttackIndicatorStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.Item;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;
import net.neoforged.neoforge.event.PlayLevelSoundEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.neoforge.event.entity.player.CriticalHitEvent;
import net.neoforged.neoforge.event.entity.player.SweepAttackEvent;

/**
 * Melee combat of Minecraft 1.5.2 (EntityPlayer.attackTargetEntityWithCurrentItem).
 *
 * <ul>
 *   <li><b>No attack cooldown.</b> 1.5.2 had none: every click dealt full damage. The player's base attack speed is
 *       set to the attribute maximum (1024), so the cooldown is 0.02 ticks and the attack strength is always 1. The
 *       base attack speed of weapons and tools ({@link Item#BASE_ATTACK_SPEED_ID}) is removed, so tooltips do not
 *       show a meaningless "1021.6 Attack Speed".</li>
 *   <li><b>No attack indicator.</b> The HUD reads the strength with partial tick 0, so it would still flash for one
 *       tick after a hit; the client keeps the option "Attack Indicator" on Off.</li>
 *   <li><b>No sweep attack</b> (added in 1.9).</li>
 *   <li><b>Critical hits:</b> falling, not on ground, ladder or vine, not in water, not blind, not riding, target is
 *       living; unlike today also while sprinting. Damage {@code i += rand.nextInt(i / 2 + 2)} instead of x1.5, applied
 *       before the enchantment bonus as in 1.5.2.</li>
 *   <li><b>Axes and shovels</b> of Minecraft deal 1.5.2 damage: axe 3 + material, shovel 1 + material
 *       (wood/gold 0, stone/copper 1, iron 2, diamond 3, netherite 4). Swords, pickaxes and hoes already match.</li>
 *   <li><b>No attack sounds</b> (entity.player.attack.*, added in 1.9); 1.5.2 hit silently.</li>
 * </ul>
 * Sprint knockback, the 0.5 s hurt invulnerability, entity reach 3 and the miss delay of 10 ticks already work as in
 * 1.5.2.
 */
@Mod(Combat152.MOD_ID)
public final class Combat152 {
    public static final String MOD_ID = "combat152";

    public Combat152(IEventBus modBus) {
        modBus.addListener(EntityAttributeModificationEvent.class, Combat152::onAttributes);
        NeoForge.EVENT_BUS.addListener(ItemAttributeModifierEvent.class, Combat152::onItemAttributes);
        NeoForge.EVENT_BUS.addListener(CriticalHitEvent.class, Combat152::onCriticalHit);
        NeoForge.EVENT_BUS.addListener(SweepAttackEvent.class, e -> e.setSweeping(false));
        NeoForge.EVENT_BUS.addListener(PlayLevelSoundEvent.class, Combat152::onSound);
        if (FMLEnvironment.getDist() == Dist.CLIENT) {
            Client.init();
        }
    }

    private static void onAttributes(EntityAttributeModificationEvent event) {
        event.add(EntityTypes.PLAYER, Attributes.ATTACK_SPEED, 1024.0);
    }

    private static void onItemAttributes(ItemAttributeModifierEvent event) {
        event.removeModifier(Attributes.ATTACK_SPEED, Item.BASE_ATTACK_SPEED_ID);
        Identifier id = BuiltInRegistries.ITEM.getKey(event.getItemStack().getItem());
        if (!Identifier.DEFAULT_NAMESPACE.equals(id.getNamespace())) {
            return;
        }
        String path = id.getPath();
        int base;
        if (path.endsWith("_axe")) {
            base = 3;
        } else if (path.endsWith("_shovel")) {
            base = 1;
        } else {
            return;
        }
        int material = switch (path.substring(0, path.lastIndexOf('_'))) {
            case "wooden", "golden" -> 0;
            case "stone", "copper" -> 1;
            case "iron" -> 2;
            case "diamond" -> 3;
            case "netherite" -> 4;
            default -> -1;
        };
        if (material < 0) {
            return;
        }
        // The modifier adds to the fist damage of 1.
        event.replaceModifier(Attributes.ATTACK_DAMAGE,
                new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID, base + material - 1, AttributeModifier.Operation.ADD_VALUE),
                EquipmentSlotGroup.MAINHAND);
    }

    private static void onCriticalHit(CriticalHitEvent event) {
        Player player = event.getEntity();
        int damage = (int) player.getAttributeValue(Attributes.ATTACK_DAMAGE);
        boolean crit = player.fallDistance > 0.0
                && !player.onGround()
                && !player.onClimbable()
                && !player.isInWater()
                && !player.isMobilityRestricted()
                && !player.isPassenger()
                && event.getTarget() instanceof LivingEntity
                && damage > 0;
        event.setCriticalHit(crit);
        if (crit) {
            int bonus = player.getRandom().nextInt(damage / 2 + 2);
            event.setDamageMultiplier((damage + bonus) / (float) damage);
        }
    }

    private static void onSound(PlayLevelSoundEvent event) {
        if (event.getSound() != null
                && event.getSound().value().location().getPath().startsWith("entity.player.attack.")) {
            event.setCanceled(true);
        }
    }

    private static final class Client {
        static void init() {
            NeoForge.EVENT_BUS.addListener(ClientTickEvent.Post.class, e -> {
                var option = Minecraft.getInstance().options.attackIndicator();
                if (option.get() != AttackIndicatorStatus.OFF) {
                    option.set(AttackIndicatorStatus.OFF);
                }
            });
        }
    }
}
