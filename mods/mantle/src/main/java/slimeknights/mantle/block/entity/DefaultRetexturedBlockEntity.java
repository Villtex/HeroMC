package slimeknights.mantle.block.entity;

import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.model.data.ModelData;
import slimeknights.mantle.block.RetexturedBlock;
import slimeknights.mantle.registration.MantleData;
import slimeknights.mantle.util.RetexturedHelper;

import javax.annotation.Nonnull;

import static slimeknights.mantle.util.RetexturedHelper.TAG_TEXTURE;

/**
 * Standard implementation for {@link IRetexturedBlockEntity}, use alongside {@link RetexturedBlock} and {@link RetexturedHelper}.
 * Port: copies the texture from and to the {@link MantleData#BLOCK_TEXTURE} component of the item.
 */
public class DefaultRetexturedBlockEntity extends MantleBlockEntity implements IRetexturedBlockEntity {
  @Nonnull
  @Getter
  private Block texture = Blocks.AIR;
  public DefaultRetexturedBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
    super(type, pos, state);
  }

  @Nonnull
  @Override
  public ModelData getModelData() {
    return RetexturedHelper.getModelData(texture);
  }

  @Override
  public String getTextureName() {
    return RetexturedHelper.getTextureName(texture);
  }

  @Override
  public void updateTexture(String name) {
    updateTexture(RetexturedHelper.getBlock(name));
  }

  @Override
  public void updateTexture(Block texture) {
    Block oldTexture = this.texture;
    this.texture = texture;
    if (oldTexture != this.texture) {
      setChangedFast();
      RetexturedHelper.onTextureUpdated(this);
    }
  }


  /* Item stack components */

  @Override
  protected void applyImplicitComponents(DataComponentGetter components) {
    super.applyImplicitComponents(components);
    this.texture = components.getOrDefault(MantleData.BLOCK_TEXTURE.get(), Blocks.AIR);
  }

  @Override
  protected void collectImplicitComponents(DataComponentMap.Builder components) {
    super.collectImplicitComponents(components);
    IRetexturedBlockEntity.collectImplicitComponents(this, components);
  }

  @Override
  public void removeComponentsFromTag(ValueOutput output) {
    // texture is passed to the stack directly, so remove from pick block
    super.removeComponentsFromTag(output);
    output.discard(TAG_TEXTURE);
  }

  @Override
  protected boolean shouldSyncOnUpdate() {
    return true;
  }

  @Override
  protected void saveSynced(ValueOutput output) {
    super.saveSynced(output);
    if (texture != Blocks.AIR) {
      output.putString(TAG_TEXTURE, getTextureName());
    }
  }

  @Override
  protected void loadAdditional(ValueInput input) {
    super.loadAdditional(input);
    input.getString(TAG_TEXTURE).ifPresent(name -> {
      texture = RetexturedHelper.getBlock(name);
      RetexturedHelper.onTextureUpdated(this);
    });
  }
}
