package betterdungeons.api;

/**
 * A summoner that counts its summoned undead (EntityMage.summonCount of the necromancer and the liche): an undead
 * calls {@link #summonGone()} when it dies or vanishes (EntitySummonedUndead.setDead).
 */
public interface BDSummoner {
    void summonGone();
}
