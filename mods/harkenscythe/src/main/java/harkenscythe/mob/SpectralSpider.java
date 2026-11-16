package harkenscythe.mob;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WallClimberNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * EntityHSSpectralSpider / EntityHSSpectralCaveSpider: climbs walls, is not slowed by cobwebs and cannot be poisoned;
 * the cave spider poisons what it bites (Normal 2 s, Hard 5 s).
 */
public class SpectralSpider extends SpectralPet {
    private static final EntityDataAccessor<Boolean> CLIMBING = SynchedEntityData.defineId(SpectralSpider.class, EntityDataSerializers.BOOLEAN);
    private final boolean cave;

    public SpectralSpider(EntityType<? extends SpectralSpider> type, Level level, boolean cave) {
        super(type, level, cave ? 8.0F : 10.0F, new Sounds(HSMobs.sound("entity.spider.ambient"), HSMobs.sound("entity.spider.ambient"),
                HSMobs.sound("entity.spider.death"), HSMobs.sound("entity.spider.step"), 0.4F));
        this.cave = cave;
        this.setupGoals();
    }

    public boolean isCave() {
        return this.cave;
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new WallClimberNavigation(this, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(CLIMBING, false);
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide()) {
            this.entityData.set(CLIMBING, this.horizontalCollision);
        }
    }

    @Override
    public boolean onClimbable() {
        return this.entityData.get(CLIMBING);
    }

    @Override
    public void makeStuckInBlock(BlockState state, Vec3 speedMultiplier) {
        if (!state.is(net.minecraft.world.level.block.Blocks.COBWEB)) {
            super.makeStuckInBlock(state, speedMultiplier);
        }
    }

    @Override
    public boolean canBeAffected(MobEffectInstance effect) {
        return !effect.is(MobEffects.POISON) && super.canBeAffected(effect);
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        if (this.cave && target instanceof LivingEntity living) {
            int seconds = level.getDifficulty() == Difficulty.NORMAL ? 2 : level.getDifficulty() == Difficulty.HARD ? 5 : 0;
            if (seconds > 0) {
                living.addEffect(new MobEffectInstance(MobEffects.POISON, seconds * 20, 0), this);
            }
        }
        return super.doHurtTarget(level, target);
    }
}
