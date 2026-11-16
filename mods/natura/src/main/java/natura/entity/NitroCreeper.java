package natura.entity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

/**
 * NitroCreeper: a fire-proof creeper with a short fuse (12 ticks plus the difficulty, 12 more when charged) that
 * explodes with strength 3, or 20 when charged. A fall of more than 5 blocks sets it off (3 without breaking blocks,
 * or 20). Iron golems kill it with one blow; 50 experience. Gunpowder 2-5 and the music disc: entities/nitro_creeper;
 * charged it drops 20-59 more gunpowder.
 */
public class NitroCreeper extends Creeper {
    private static final int FUSE = 12;
    private int timeSinceIgnited;
    private int lastActiveTime;

    public NitroCreeper(EntityType<? extends NitroCreeper> type, Level level) {
        super(type, level);
        this.xpReward = 50;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Creeper.createAttributes();
    }

    /** onUpdate: its own fuse runs before the vanilla one (30 ticks) and always ends first. */
    @Override
    public void tick() {
        if (this.isAlive()) {
            this.lastActiveTime = this.timeSinceIgnited;
            int state = this.isIgnited() ? 1 : this.getSwellDir();
            this.timeSinceIgnited = Math.max(0, this.timeSinceIgnited + state);
            int powered = this.isPowered() ? 12 : 0;
            if (this.timeSinceIgnited >= FUSE + this.level().getDifficulty().getId() + powered) {
                this.timeSinceIgnited = FUSE;
                if (this.level() instanceof ServerLevel) {
                    this.blowUp(powered > 0 ? 20.0F : 3.0F, true);
                    return;
                }
            }
        }
        super.tick();
    }

    private void blowUp(float strength, boolean grief) {
        this.level().explode(this, this.getX(), this.getY(), this.getZ(), strength,
                grief ? Level.ExplosionInteraction.MOB : Level.ExplosionInteraction.NONE);
        this.discard();
    }

    @Override
    public float getSwelling(float partialTicks) {
        return (this.lastActiveTime + (this.timeSinceIgnited - this.lastActiveTime) * partialTicks) / (FUSE - 2);
    }

    /** fall: more than 5 blocks and it explodes. */
    @Override
    public boolean causeFallDamage(double fallDistance, float damageModifier, DamageSource source) {
        if (!this.level().isClientSide() && fallDistance > 5.0) {
            this.blowUp(this.isPowered() ? 20.0F : 3.0F, this.isPowered());
            return false;
        }
        return super.causeFallDamage(fallDistance, damageModifier, source);
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        if (source.getEntity() instanceof IronGolem) {
            damage = 1000.0F;
        }
        return super.hurtServer(level, source, damage);
    }

    /** dropFewItems: a charged nitro creeper drops 20 to 59 more gunpowder. */
    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
        super.dropCustomDeathLoot(level, source, killedByPlayer);
        if (this.isPowered()) {
            this.spawnAtLocation(level, new ItemStack(Items.GUNPOWDER, this.random.nextInt(40) + 20));
        }
    }
}
