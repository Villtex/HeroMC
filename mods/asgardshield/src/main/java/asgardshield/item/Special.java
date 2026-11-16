package asgardshield.item;

/**
 * The "specialNumber" of ItemAsgardShield / ItemZedSword (0 for the core materials; 1-6 for the addon materials).
 * Only ENDER (portal particles, repaired with obsidian) and SKULL (cloud on hit) change the behaviour; the others
 * only chose the stats in the original constructors.
 */
public enum Special {
    NONE, NETHER, ENDER, SKULL, PATCHWORK, LIVINGMETAL, BIOMASS
}
