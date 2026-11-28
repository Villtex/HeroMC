package slimeknights.tconstruct.library.utils;

import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Stand-in for Forge's {@code TierSortingRegistry} (gone in NeoForge 26.3): the harvest tiers in order, by name.
 * Order of 1.20.1 (wood and gold equal, gold sorted after wood) with the copper tier of 26.3 between stone and iron.
 */
public final class TierSortingRegistry {
  private static final List<Tier> SORTED = new ArrayList<>();
  private static final Map<Identifier, Tier> BY_NAME = new HashMap<>();

  static {
    for (Tier tier : new Tier[] { Tier.WOOD, Tier.GOLD, Tier.STONE, Tier.COPPER, Tier.IRON, Tier.DIAMOND, Tier.NETHERITE }) {
      register(tier);
    }
  }

  private TierSortingRegistry() {}

  /** Adds a tier at the end of the order (for addons) */
  public static synchronized void register(Tier tier) {
    if (BY_NAME.putIfAbsent(tier.name(), tier) == null) {
      SORTED.add(tier);
    }
  }

  /** Tiers from lowest to highest */
  public static List<Tier> getSortedTiers() {
    return Collections.unmodifiableList(SORTED);
  }

  /** Gets a tier by its name, or null if unknown */
  @Nullable
  public static Tier byName(Identifier name) {
    return BY_NAME.get(name);
  }

  /** Name of the tier */
  public static Identifier getName(Tier tier) {
    return tier.name();
  }

  /** If true, the tier can harvest the block, i.e. the block is not in the tag of blocks the tier cannot harvest */
  public static boolean isCorrectTierForDrops(Tier tier, BlockState state) {
    return !state.is(tier.incorrectBlocksForDrops());
  }
}
