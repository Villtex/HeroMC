package slimeknights.mantle.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueOutput;

import javax.annotation.Nullable;

/**
 * Port: NBT methods take {@link ValueOutput} (26.3 block entity serialization); {@link #saveSynced(ValueOutput)}
 * replaces {@code saveSynced(CompoundTag)}, loading is {@code loadAdditional(ValueInput)}.
 */
public class MantleBlockEntity extends BlockEntity {

  public MantleBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
    super(type, pos, state);
  }

  public boolean isClient() {
    return this.getLevel() != null && this.getLevel().isClientSide();
  }

  /**
   * Marks the chunk dirty without performing comparator updates (twice!!) or block state checks
   * Used since most of our markDirty calls only adjust TE data
   */
  public void setChangedFast() {
    if (level != null) {
      if (level.hasChunkAt(worldPosition)) {
        level.getChunkAt(worldPosition).markUnsaved();
      }
    }
  }


  /* Syncing */

  /**
   * If true, this TE syncs when {@link net.minecraft.world.level.Level#blockUpdated(BlockPos, Block) is called
   * Syncs data from {@link #saveSynced(ValueOutput)}
   */
  protected boolean shouldSyncOnUpdate() {
    return false;
  }

  @Override
  @Nullable
  public Packet<ClientGamePacketListener> getUpdatePacket() {
    return shouldSyncOnUpdate() ? ClientboundBlockEntityDataPacket.create(this) : null;
  }

  /**
   * Write data that is synced to the client in {@link #getUpdateTag(HolderLookup.Provider)} and saved in {@link #saveAdditional(ValueOutput)}
   * @param output  Output
   */
  protected void saveSynced(ValueOutput output) {}

  @Override
  public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
    TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, registries);
    saveSynced(output);
    return output.buildResult();
  }

  @Override
  protected void saveAdditional(ValueOutput output) {
    super.saveAdditional(output);
    saveSynced(output);
  }
}
