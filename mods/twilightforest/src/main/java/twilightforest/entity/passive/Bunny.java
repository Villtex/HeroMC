package twilightforest.entity.passive;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * EntityTFBunny: four types (0 and 1 Dutch, 2 white, 3 brown), avoids leaves and wood (-1, so it never spawns there).
 * Silent.
 */
public class Bunny extends SmallCritter {
    private static final EntityDataAccessor<Byte> DATA_TYPE = SynchedEntityData.defineId(Bunny.class, EntityDataSerializers.BYTE);

    public Bunny(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        this.setBunnyType(this.random.nextInt(4));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_TYPE, (byte) 0);
    }

    public int getBunnyType() {
        return this.entityData.get(DATA_TYPE);
    }

    public void setBunnyType(int type) {
        this.entityData.set(DATA_TYPE, (byte) type);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt("BunnyType", this.getBunnyType());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.setBunnyType(input.getIntOr("BunnyType", 0));
    }

    @Override
    protected float leaves() {
        return -1.0F;
    }

    @Override
    protected float wood() {
        return -1.0F;
    }

    @Override
    protected float grass() {
        return 10.0F;
    }
}
