package tconstruct.entity;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import tconstruct.TItems;
import tconstruct.tools.InfiTool;
import tconstruct.tools.ToolCombat;
import tconstruct.tools.ToolType;

/**
 * ArrowEntity: a Tinkers' arrow shot from the shortbow. It carries its arrow (the tool stack) and gives it back when
 * picked up; its damage is speed x 2 x Attack / 5 (rounded up, a crit adds 0..half + 1), heavier arrows fall faster
 * (gravity 0.05 x (1.05 + mass / 7.56)) and water slows to 0.8. Fiery / lava set the target on fire, necrotic heals
 * the shooter, the knockback modifier pushes. A hit player gets the arrow into the inventory, a hit mob into an empty
 * equipment slot (it may drop it).
 */
public class TinkerArrow extends AbstractArrow {
    private static final EntityDataAccessor<ItemStack> DATA_ITEM = SynchedEntityData.defineId(TinkerArrow.class, EntityDataSerializers.ITEM_STACK);
    private float mass;
    private float knockbackMod;
    private int punch;

    public TinkerArrow(EntityType<? extends TinkerArrow> type, Level level) {
        super(type, level);
    }

    /** ArrowEntity(world, living, baseSpeed, stack): at the shooter's eyes, along the view, speed {@code speed}. */
    public TinkerArrow(Level level, LivingEntity shooter, float speed, ItemStack arrow) {
        super(TEntities.ARROW.get(), shooter.getX(), shooter.getEyeY() - 0.1F, shooter.getZ(), level, arrow.copyWithCount(1), null);
        this.setOwner(shooter);
        this.entityData.set(DATA_ITEM, arrow.copyWithCount(1));
        InfiTool t = InfiTool.of(arrow);
        this.mass = t == null ? 0.0F : t.getFloat("Mass");
        this.setBaseDamage(2.0 * (t == null ? 0 : t.attack) / 5.0);
        if (shooter instanceof Player) {
            this.pickup = Pickup.ALLOWED;
        }
        aim(this, shooter, speed, 1.0F);
    }

    /** EntityArrow(world, living, speed): from the eyes (0.16 to the side) along the view, spread 0.0075 per inaccuracy. */
    public static void aim(AbstractArrow arrow, LivingEntity shooter, float speed, float inaccuracy) {
        float yaw = shooter.getYRot();
        float pitch = shooter.getXRot();
        arrow.setPos(shooter.getX() - Mth.cos(yaw / 180.0F * (float) Math.PI) * 0.16F, shooter.getEyeY() - 0.1F,
                shooter.getZ() - Mth.sin(yaw / 180.0F * (float) Math.PI) * 0.16F);
        double x = -Mth.sin(yaw / 180.0F * (float) Math.PI) * Mth.cos(pitch / 180.0F * (float) Math.PI);
        double z = Mth.cos(yaw / 180.0F * (float) Math.PI) * Mth.cos(pitch / 180.0F * (float) Math.PI);
        double y = -Mth.sin(pitch / 180.0F * (float) Math.PI);
        double length = Math.sqrt(x * x + y * y + z * z);
        x = x / length + arrow.getRandom().nextGaussian() * 0.0075F * inaccuracy;
        y = y / length + arrow.getRandom().nextGaussian() * 0.0075F * inaccuracy;
        z = z / length + arrow.getRandom().nextGaussian() * 0.0075F * inaccuracy;
        arrow.setDeltaMovement(x * speed, y * speed, z * speed);
        double horizontal = Math.sqrt(x * x + z * z);
        arrow.setYRot((float) (Math.atan2(x, z) * 180.0 / Math.PI));
        arrow.setXRot((float) (Math.atan2(y, horizontal) * 180.0 / Math.PI));
        arrow.yRotO = arrow.getYRot();
        arrow.xRotO = arrow.getXRot();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ITEM, ItemStack.EMPTY);
    }

    /** The arrow as it is rendered and picked up. */
    public ItemStack getArrow() {
        return this.entityData.get(DATA_ITEM);
    }

    /** setKnockbackModStrength (the knockback modifier of the bow) and the punch level of 1.5.2. */
    public void setKnockback(float modifier, int punch) {
        this.knockbackMod = modifier;
        this.punch = punch;
    }

    @Override
    protected double getDefaultGravity() {
        return 0.05F * (1.05F + this.mass / 1.26F / 6.0F);
    }

    @Override
    protected float getWaterInertia() {
        return 0.8F;
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return new ItemStack(TItems.tool(ToolType.ARROW));
    }

    /**
     * The knockback of 1.5.2: x by punch only, z by punch + modifier (as in ArrowEntity), 0.1 up; the hurt knockback
     * of the damage stays.
     */
    @Override
    protected void doKnockback(LivingEntity target, DamageSource source) {
        if (this.punch > 0 || this.knockbackMod > 0.0F) {
            Vec3 m = this.getDeltaMovement();
            double f3 = Math.sqrt(m.x * m.x + m.z * m.z);
            if (f3 > 0.0) {
                float knockback = this.punch + this.knockbackMod;
                target.push(m.x * this.punch * 0.6F / f3, 0.1, m.z * knockback * 0.6F / f3);
                target.needsSync = true;
            }
        }
    }

    @Override
    protected void doPostHurtEffects(LivingEntity target) {
        ItemStack arrow = this.getArrow();
        InfiTool t = InfiTool.of(arrow);
        if (t != null) {
            ToolCombat.fire(t, target);
            if (this.getOwner() instanceof LivingEntity shooter) {
                ToolCombat.necrotic(t, shooter);
            }
        }
        if (this.pickup == Pickup.ALLOWED && !arrow.isEmpty()) {
            if (target instanceof Player player) {
                player.getInventory().add(arrow.copy());
            } else if (target instanceof Mob mob) {
                for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.MAINHAND, EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST,
                        EquipmentSlot.HEAD}) {
                    if (mob.getItemBySlot(slot).isEmpty()) {
                        mob.setItemSlot(slot, arrow.copy());
                        break;
                    }
                }
            }
        }
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putFloat("mass", this.mass);
        output.putFloat("knockback_mod", this.knockbackMod);
        output.putInt("punch", this.punch);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.mass = input.getFloatOr("mass", 0.0F);
        this.knockbackMod = input.getFloatOr("knockback_mod", 0.0F);
        this.punch = input.getIntOr("punch", 0);
        this.entityData.set(DATA_ITEM, this.getPickupItemStackOrigin().copy());
    }
}
