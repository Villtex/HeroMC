package twilightforest.entity.boss.hydra;

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
 * The parts of the hydra that are not entities of their own: the body and the tail (EntityDragonPart "body"/"tail"
 * of EntityTFHydra, 6 x 6 and 6 x 2 once the hydra has ticked) and the five neck segments of every head
 * (EntityTFHydraNeck "neck&lt;n&gt;a".."e", 2 x 2, an EntityLiving in 1.5.2 that was never spawned). Hits go to
 * {@link Hydra#attackEntityFromPart}: always armoured (damage / 8, rounded). The legs of 1.5.2 (EntityDragonPart
 * "leg") were never placed and stayed at 0, 0, 0; they are left out.
 */
public class HydraPart extends PartEntity<Hydra> {
    private final String partName;
    private final boolean neck;
    private EntityDimensions size;
    /** EntityLiving.hurtTime of a neck (red tint; set from the hydra and the head container). */
    public int hurtTime;

    public HydraPart(Hydra parent, String name, float width, float height, boolean neck) {
        super(parent);
        this.partName = name;
        this.neck = neck;
        this.size = EntityDimensions.scalable(width, height);
        this.refreshDimensions();
        this.noPhysics = true;
    }

    public String getPartName() {
        return this.partName;
    }

    public boolean isNeck() {
        return this.neck;
    }

    void setSize(float width, float height) {
        if (this.size.width() != width || this.size.height() != height) {
            this.size = EntityDimensions.scalable(width, height);
            this.refreshDimensions();
        }
    }

    /** onUpdate of the part: the last position for interpolation, and EntityLiving's hurtTime countdown. */
    void tickPart() {
        this.setOldPosAndRot();
        if (this.hurtTime > 0) {
            this.hurtTime--;
        }
    }

    void place(double x, double y, double z, float yaw, float pitch) {
        this.setPos(x, y, z);
        this.setYRot(yaw);
        this.setXRot(pitch);
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        return this.getParent().attackEntityFromPart(this, level, source, damage);
    }

    @Override
    public boolean fireImmune() {
        return true;
    }

    @Override
    public boolean isPickable() {
        return true;
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
        return this.size;
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
