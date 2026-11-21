package twilightforest.entity;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.spider.Spider;
import net.minecraft.world.level.Level;

/**
 * EntityTFTowerBroodling (redscale broodling): a swarm spider of the dark tower with 7 health and 3 experience that
 * bites for 2 (4 half the time in the air) and brings more broodlings.
 */
public class TowerBroodling extends SwarmSpider {
    public TowerBroodling(EntityType<? extends Spider> type, Level level) {
        super(type, level);
        this.xpReward = 3;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return SwarmSpider.createAttributes().add(Attributes.MAX_HEALTH, 7.0);
    }

    @Override
    public int attackStrength(Entity target) {
        return !this.onGround() && this.random.nextInt(2) == 0 ? 4 : 2;
    }
}
