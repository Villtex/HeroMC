package harkenscythe.mob;

import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

/**
 * EntityHSSpectralOcelot: an ocelot, or (skin 1-3, from the soul of a black, red or siamese cat) a cat drawn at 0.8
 * size; it purrs and meows only when tamed, takes no fall damage and is called "Cat" when tamed.
 */
public class SpectralOcelot extends SpectralPet {
    private static final EntityDataAccessor<Integer> SKIN = SynchedEntityData.defineId(SpectralOcelot.class, EntityDataSerializers.INT);

    public SpectralOcelot(EntityType<? extends SpectralOcelot> type, Level level) {
        super(type, level, 10.0F, new Sounds(null, HSMobs.sound("entity.cat.hurt"), HSMobs.sound("entity.cat.hurt"),
                HSMobs.sound("entity.wolf.step"), 0.4F));
        this.setupGoals();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(SKIN, 0);
    }

    public int getTameSkin() {
        return this.entityData.get(SKIN);
    }

    public void setTameSkin(int skin) {
        this.entityData.set(SKIN, skin);
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        if (!this.isTame()) {
            return null;
        }
        if (this.isInLove()) {
            return HSMobs.sound("entity.cat.purr");
        }
        return this.random.nextInt(4) == 0 ? HSMobs.sound("entity.cat.purreow") : HSMobs.sound("entity.cat.ambient");
    }

    @Override
    public boolean causeFallDamage(double fallDistance, float damageModifier, DamageSource damageSource) {
        return false;
    }

    @Override
    protected Component getTypeName() {
        return this.isTame() ? Component.translatable("entity.minecraft.cat") : super.getTypeName();
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt("CatType", this.getTameSkin());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.setTameSkin(input.getIntOr("CatType", 0));
    }
}
