package slimeknights.mantle.block.entity;

import net.minecraft.world.level.block.entity.BlockEntityTypes;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.event.BlockEntityTypeAddBlocksEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Port: 26.3 lets mods add blocks to vanilla block entity types ({@link BlockEntityTypeAddBlocksEvent}), so Mantle's
 * sign blocks use the vanilla {@code SignBlockEntity} of {@link BlockEntityType#SIGN} instead of an own type. That also
 * keeps the vanilla sign renderer and editing screen working. Only the block list of the 1.20.1 class is left.
 */
public final class MantleSignBlockEntity {
  private MantleSignBlockEntity() {}

  /** Sign blocks to add to the vanilla block entity type */
  private static final List<Supplier<? extends Block>> SIGN_BLOCKS = new ArrayList<>();

  /**
   * Registers a sign block to be added to the vanilla block entity type, should be called during registration
   * @param sign  Sign block supplier
   */
  public static void registerSignBlock(Supplier<? extends Block> sign) {
    synchronized (SIGN_BLOCKS) {
      SIGN_BLOCKS.add(sign);
    }
  }

  /** Adds all registered sign blocks to the vanilla type, mod bus listener */
  public static void addBlocks(BlockEntityTypeAddBlocksEvent event) {
    event.modify(net.minecraft.world.level.block.entity.BlockEntityTypes.SIGN, SIGN_BLOCKS.stream().map(Supplier::get).toArray(Block[]::new));
  }
}
