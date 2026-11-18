package betterdungeons.api;

import net.minecraft.world.entity.LivingEntity;

/**
 * Mobs an item summons for its user: EntitySummonedUndead (cursed bone: EntitySummonedUndead(world, player, 0), then
 * setlvl(2)) and EntityBaiter (trickster dagger: EntityBaiter(world, player), type and level 0).
 */
public interface BDSummonedMob {
    void initSummoned(LivingEntity summoner, int type, int level);
}
