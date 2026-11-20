package meteors.entity;

import meteors.MeteorsMod;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.feline.Cat;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

/**
 * EntityCometKitty: an ocelot of 1.5.2 (today's cat, tamed with fish) in a space helmet, 16 health, immune to fire,
 * drops a Red Meteor Gem (loot table). Half of its kittens are plain cats. The taming advancement is a tame_animal
 * criterion.
 */
public class CometKitty extends Cat {
    public CometKitty(EntityType<? extends Cat> type, Level level) {
        super(type, level);
    }

    @Override
    public @Nullable Cat getBreedOffspring(ServerLevel level, AgeableMob partner) {
        if (this.random.nextBoolean()) {
            return super.getBreedOffspring(level, partner);
        }
        CometKitty baby = MeteorsMod.COMET_KITTY.get().create(level, EntitySpawnReason.BREEDING);
        if (baby != null && this.isTame()) {
            baby.setOwnerReference(this.getOwnerReference());
            baby.setTame(true, true);
        }
        return baby;
    }
}
