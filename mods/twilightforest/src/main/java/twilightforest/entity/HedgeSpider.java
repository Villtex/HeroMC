package twilightforest.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.spider.Spider;
import net.minecraft.world.level.Level;

/** EntityTFHedgeSpider: a spider of the hedge maze; it spawns there in any light. Drops string and spider eyes. */
public class HedgeSpider extends TFSpider {
    public HedgeSpider(EntityType<? extends Spider> type, Level level) {
        super(type, level);
    }
}
