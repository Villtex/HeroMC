package slimeknights.tconstruct.library.utils;

import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

/**
 * Harvest tier of a tool. 26.3 has no {@code Tier} (tools carry a {@code minecraft:tool} component with rules, materials
 * name the blocks they cannot harvest as a tag), so Tinkers keeps the 1.20.1 concept itself: a name, as written in the
 * data ({@code minecraft:iron}), and the block tag of blocks this tier cannot harvest. Order: {@link TierSortingRegistry}.
 */
public record Tier(Identifier name, TagKey<Block> incorrectBlocksForDrops) {
  public static final Tier WOOD = vanilla("wood", BlockTags.INCORRECT_FOR_WOODEN_TOOL);
  public static final Tier GOLD = vanilla("gold", BlockTags.INCORRECT_FOR_GOLD_TOOL);
  public static final Tier STONE = vanilla("stone", BlockTags.INCORRECT_FOR_STONE_TOOL);
  public static final Tier COPPER = vanilla("copper", BlockTags.INCORRECT_FOR_COPPER_TOOL);
  public static final Tier IRON = vanilla("iron", BlockTags.INCORRECT_FOR_IRON_TOOL);
  public static final Tier DIAMOND = vanilla("diamond", BlockTags.INCORRECT_FOR_DIAMOND_TOOL);
  public static final Tier NETHERITE = vanilla("netherite", BlockTags.INCORRECT_FOR_NETHERITE_TOOL);

  private static Tier vanilla(String name, TagKey<Block> incorrect) {
    return new Tier(Identifier.withDefaultNamespace(name), incorrect);
  }

  @Override
  public String toString() {
    return name.toString();
  }
}
