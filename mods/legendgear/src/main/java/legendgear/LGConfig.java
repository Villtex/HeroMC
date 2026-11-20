package legendgear;

/**
 * The values of the pack's LegendGear.cfg (z-hexxit-configs), fixed: every toggle of the pack is on except AntiGrinder
 * and EnableCraftyLoading, the lists of the compatibility section are empty. The few values the world generation needs
 * are in {@link legendgear.gen.LGWorldGen}.
 */
public final class LGConfig {
    /** toggles.AntiGrinder (false): loot only from mobs a player hit first at full health. */
    public static final boolean ANTI_GRINDER = false;
    /** toggles.MysticShrubSuperPrizes (true). */
    public static final boolean SHRUB_SUPER_PRIZES = true;
    /** toggles.PrismaticXP (true): experience orbs in rainbow colours (client). */
    public static final boolean PRISMATIC_XP = true;
    /** toggles.EnableStarfall (true). */
    public static final boolean ENABLE_STARFALL = true;
    /** enableBombArrows (no config key, true): sneaking while firing ties a bomb to the arrow. */
    public static final boolean BOMB_ARROWS = true;

    /** general.BombBagCapacity (50), general.QuiverMaxCapacity (200). */
    public static final int BOMB_BAG_CAPACITY = 50;
    public static final int QUIVER_CAPACITY = 200;
    /** general.BombDamage (5), general.MagicBoomerangDamage (6). */
    public static final int BOMB_DAMAGE = 5;
    public static final int BOOMERANG_DAMAGE = 6;
    /** general.EmeraldShardExchangeRate (8): shards per piece, pieces per emerald. */
    public static final int EMERALD_EXCHANGE_RATE = 8;
    /** general.FallenStarLifetime (440), general.StarfallRarity (8400). */
    public static final int FALLEN_STAR_LIFETIME = 440;
    public static final int STARFALL_RARITY = 8400;
    /** Chances of a plain shrub: heart, emerald shard, arrow (0.2 each); of a glowing shrub's jackpot (0.05). */
    public static final double SHRUB_HEART_CHANCE = 0.2;
    public static final double SHRUB_SHARD_CHANCE = 0.2;
    public static final double SHRUB_ARROW_CHANCE = 0.2;
    public static final double SHRUB_JACKPOT_CHANCE = 0.05;

    private LGConfig() {
    }
}
