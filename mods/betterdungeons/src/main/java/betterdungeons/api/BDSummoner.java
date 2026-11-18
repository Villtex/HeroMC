package betterdungeons.api;

/**
 * A mob that keeps count of what it summoned (EntityMage.summonCount): EntitySummonedUndead called
 * {@code summoner.summonCount--} when it died. The summoned mob calls {@link #onSummonDied()} on its summoner.
 */
public interface BDSummoner {
    void onSummonDied();
}
