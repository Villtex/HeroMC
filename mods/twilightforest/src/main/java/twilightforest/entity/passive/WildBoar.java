package twilightforest.entity.passive;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;
import twilightforest.TFEntities;

/** EntityTFBoar: a pig (saddle, carrot on a stick, porkchops) with its own model and texture, 0.9 x 0.9. */
public class WildBoar extends Pig {
    public WildBoar(EntityType<? extends Pig> type, Level level) {
        super(type, level);
    }

    @Override
    public @Nullable Pig getBreedOffspring(ServerLevel level, AgeableMob partner) {
        return TFEntities.WILD_BOAR.get().create(level, EntitySpawnReason.BREEDING);
    }
}
