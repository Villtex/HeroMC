package harkenscythe.item;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.item.component.Weapon;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import harkenscythe.HarkenScythe;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * What ItemHSScythe and ItemHSGlaive share: an ItemSword that cannot harvest blocks (1.5 against everything, 15
 * against cobweb, 3 damage per block), hits for 3 + the material's damage and pushes the target a little. Holding
 * right-click (bow pose) for 24 ticks or more charges it: letting go attacks every living thing in reach at double
 * damage, three durability per hit. While a charged attack runs ({@link #isCharged}), a scythe reaps souls and a
 * glaive spills blood (see harkenscythe.event.HSEvents).
 */
public abstract class ReapingWeaponItem extends Item {
    private static final net.minecraft.resources.Identifier CHARGE_SPEED = HarkenScythe.id("charged_attack");
    private static @Nullable LivingEntity charging;
    protected final ToolMaterial material;
    private final float knockback;

    protected ReapingWeaponItem(ToolMaterial material, float knockback, Properties properties) {
        super(properties);
        this.material = material;
        this.knockback = knockback;
    }

    /** The player whose charged attack is running (server thread), else null. */
    public static boolean isCharged(@Nullable Entity attacker) {
        return attacker != null && attacker == charging;
    }

    public static Item.Properties weaponProperties(Item.Properties p, ToolMaterial material) {
        @SuppressWarnings("deprecation")
        Holder<net.minecraft.world.level.block.Block> cobweb = Blocks.COBWEB.builtInRegistryHolder();
        return p.stacksTo(1).durability(material.durability()).repairable(material.repairItems()).enchantable(material.enchantmentValue())
                .attributes(ItemAttributeModifiers.builder()
                        .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID, 2.0F + material.attackDamageBonus(),
                                AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                        .add(Attributes.ATTACK_SPEED, new AttributeModifier(Item.BASE_ATTACK_SPEED_ID, -2.4F, AttributeModifier.Operation.ADD_VALUE),
                                EquipmentSlotGroup.MAINHAND)
                        .build())
                .component(DataComponents.TOOL, new Tool(List.of(Tool.Rule.overrideSpeed(HolderSet.direct(cobweb), 15.0F)), 1.5F, 3, false))
                .component(DataComponents.WEAPON, new Weapon(1));
    }

    @Override
    public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        float yaw = target.getYRot() * Mth.DEG_TO_RAD;
        target.push(-Mth.sin(yaw) * this.knockback * 0.5F, 0.1, Mth.cos(yaw) * this.knockback * 0.5F);
    }

    @Override
    public void postHurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (isCharged(attacker)) {
            stack.hurtAndBreak(2, attacker, EquipmentSlot.MAINHAND);
        }
    }

    /** getDamageVsEntity: doubled during a charged attack. */
    @Override
    public float getAttackDamageBonus(Entity victim, float damage, DamageSource source) {
        return isCharged(source.getEntity()) ? damage : 0.0F;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.BOW;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity user) {
        return 72000;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        player.startUsingItem(hand);
        level.playSound(player, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_BREATH, SoundSource.PLAYERS, 1.0F, 0.9F);
        return InteractionResult.CONSUME;
    }

    @Override
    public boolean releaseUsing(ItemStack stack, Level level, LivingEntity entity, int remainingTime) {
        if (!(entity instanceof Player player) || this.getUseDuration(stack, entity) - remainingTime < 24) {
            return false;
        }
        if (!level.isClientSide()) {
            double reach = this.reachSqr();
            List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, new AABB(player.blockPosition()).inflate(Math.sqrt(reach) + 2.0),
                    e -> e != player && e.isAlive() && player.distanceToSqr(e) < reach);
            AttributeModifier speed = new AttributeModifier(CHARGE_SPEED, 10000.0, AttributeModifier.Operation.ADD_VALUE);
            var attackSpeed = player.getAttribute(Attributes.ATTACK_SPEED);
            charging = player;
            try {
                if (attackSpeed != null) {
                    attackSpeed.addOrUpdateTransientModifier(speed);
                }
                for (LivingEntity target : targets) {
                    if (target instanceof TamableAnimal tamed && tamed.isTame() && tamed.getTarget() != player) {
                        continue;
                    }
                    if (this.inReach(player, target)) {
                        player.attack(target);
                    }
                }
            } finally {
                charging = null;
                if (attackSpeed != null) {
                    attackSpeed.removeModifier(CHARGE_SPEED);
                }
            }
        }
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.IRON_GOLEM_ATTACK, SoundSource.PLAYERS, 1.0F, 1.0F);
        player.swing(player.getUsedItemHand(), net.minecraft.world.item.component.SwingAnimation.DEFAULT, true);
        return true;
    }

    /** Squared reach of the charged attack. */
    protected abstract double reachSqr();

    /** Whether a target within reach is hit (the glaive only strikes ahead). */
    protected boolean inReach(Player player, LivingEntity target) {
        return true;
    }
}
