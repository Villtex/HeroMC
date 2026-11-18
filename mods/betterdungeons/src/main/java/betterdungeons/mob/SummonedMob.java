package betterdungeons.mob;

import net.minecraft.world.entity.LivingEntity;
import org.jspecify.annotations.Nullable;

/** IEntityTameable of the summoned mobs: the entity that summoned it. */
public interface SummonedMob {
    @Nullable LivingEntity getSummoner();
}
