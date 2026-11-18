package betterdungeons.mob;

import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.entity.PartEntity;
import org.jspecify.annotations.Nullable;

/**
 * EntityTurtlePart ("TurtleBossPart"): the head (300) or a leg (100) of the giant turtle, 1 x 1, fire immune. A hit
 * takes its health ("hearts") and goes on to the turtle; at zero the part is gone. Not saved: the turtle makes new
 * parts with full health after loading, as in 1.5.2 (where the parts killed themselves on loading). A part entity of
 * the turtle here, so the id TurtleBossPart has no entity type of its own.
 */
public class TurtlePart extends PartEntity<TurtleBoss> {
    private static final EntityDimensions SIZE = EntityDimensions.scalable(1.0F, 1.0F);
    private final int index;
    private final boolean head;
    int heartsLife;

    public TurtlePart(TurtleBoss parent, int index, boolean head) {
        super(parent);
        this.index = index;
        this.head = head;
        this.heartsLife = this.maxHealth();
        this.refreshDimensions();
        this.noPhysics = true;
    }

    public boolean isHead() {
        return this.head;
    }

    public int maxHealth() {
        return this.head ? 300 : 100;
    }

    public boolean isGone() {
        return this.getParent().isPartDead(this.index);
    }

    /** setPositionAndRotation from the turtle each tick (also on the client). */
    void place(double x, double y, double z, float yaw) {
        this.xo = this.getX();
        this.yo = this.getY();
        this.zo = this.getZ();
        this.xOld = this.getX();
        this.yOld = this.getY();
        this.zOld = this.getZ();
        this.yRotO = this.getYRot();
        this.setPos(x, y, z);
        this.setYRot(yaw);
        this.setXRot(this.getParent().getXRot());
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        if (this.isGone() || this.isInvulnerableToBase(source)) {
            return false;
        }
        this.heartsLife -= (int) damage;
        if (this.heartsLife <= 0) {
            this.getParent().killPart(this.index);
        }
        return this.getParent().hurtServer(level, source, damage);
    }

    @Override
    public boolean fireImmune() {
        return true;
    }

    @Override
    public boolean isPickable() {
        return !this.isGone();
    }

    @Override
    public boolean is(Entity other) {
        return this == other || this.getParent() == other;
    }

    @Override
    public @Nullable ItemStack getPickResult() {
        return this.getParent().getPickResult();
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        return SIZE;
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
    }
}
