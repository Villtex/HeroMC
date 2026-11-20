package meteors.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.level.Level;

/**
 * EntityAlienCreeper: a creeper that is always charged (data watcher 17 = 1), immune to fire, 20 health, and drops a
 * Red Meteor Gem or Meteor Chips (loot table). Creepers already flee from cats, and so from comet kitties.
 */
public class AlienCreeper extends Creeper {
    public AlienCreeper(EntityType<? extends AlienCreeper> type, Level level) {
        super(type, level);
    }

    @Override
    public boolean isPowered() {
        return true;
    }
}
