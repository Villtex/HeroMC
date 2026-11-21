package twilightforest.entity.passive;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.Level;

/** EntityTFSquirrel: likes wood (15) and leaves (12) a bit more than grass (10). Silent. */
public class Squirrel extends SmallCritter {
    public Squirrel(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    @Override
    protected float leaves() {
        return 12.0F;
    }

    @Override
    protected float wood() {
        return 15.0F;
    }

    @Override
    protected float grass() {
        return 10.0F;
    }
}
