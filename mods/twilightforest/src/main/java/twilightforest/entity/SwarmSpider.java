package twilightforest.entity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.spider.Spider;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * EntityTFSwarmSpider: a spider of 3 health, 0.8 x 0.4, half size, 2 experience. The first one of a spawn brings 1..2
 * more beside it (each tries twice to find room). It bites for 1 only in the air and then 1 time in 4, else 0. Up to
 * 16 in a chunk.
 */
public class SwarmSpider extends TFSpider {
    private boolean spawnMore = true;

    public SwarmSpider(EntityType<? extends Spider> type, Level level) {
        super(type, level);
        this.xpReward = 2;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Spider.createAttributes().add(Attributes.MAX_HEALTH, 3.0);
    }

    @Override
    public float renderScale() {
        return 0.5F;
    }

    @Override
    public void tick() {
        if (this.spawnMore) {
            if (this.level() instanceof ServerLevel level) {
                int more = 1 + this.random.nextInt(2);
                for (int i = 0; i < more; i++) {
                    if (!this.spawnAnother(level)) {
                        this.spawnAnother(level);
                    }
                }
            }
            this.spawnMore = false;
        }
        super.tick();
    }

    protected boolean spawnAnother(ServerLevel level) {
        @SuppressWarnings("unchecked")
        EntityType<? extends SwarmSpider> type = (EntityType<? extends SwarmSpider>) this.getType();
        SwarmSpider another = type.create(level, EntitySpawnReason.REINFORCEMENT);
        if (another == null) {
            return false;
        }
        another.spawnMore = false;
        double sx = this.getX() + (this.random.nextBoolean() ? 0.9 : -0.9);
        double sz = this.getZ() + (this.random.nextBoolean() ? 0.9 : -0.9);
        another.snapTo(sx, this.getY(), sz, this.random.nextFloat() * 360.0F, 0.0F);
        if (!another.checkSpawnObstruction(level) || !level.noCollision(another)) {
            another.discard();
            return false;
        }
        level.addFreshEntity(another);
        return true;
    }

    @Override
    public int attackStrength(Entity target) {
        return !this.onGround() && this.random.nextInt(4) == 0 ? 1 : 0;
    }

    @Override
    public int getMaxSpawnClusterSize() {
        return 16;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putBoolean("SpawnMore", this.spawnMore);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.spawnMore = input.getBooleanOr("SpawnMore", false);
    }
}
