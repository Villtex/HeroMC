package slimeknights.mantle.mixin;

import net.minecraft.resources.DependantName;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.storage.loot.LootTable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import slimeknights.mantle.registration.RegistrationContext;

import java.util.Optional;

/** Port: block properties without {@code setId} take the key of the block Mantle's registers are creating, see {@link RegistrationContext} */
@Mixin(BlockBehaviour.Properties.class)
public abstract class BlockPropertiesMixin {
  @Shadow private ResourceKey<Block> id;
  @Shadow private DependantName<Block, Optional<ResourceKey<LootTable>>> drops;
  @Shadow private DependantName<Block, String> descriptionId;

  @Inject(method = "effectiveDrops", at = @At("HEAD"), cancellable = true)
  private void mantle$contextDrops(CallbackInfoReturnable<Optional<ResourceKey<LootTable>>> cir) {
    ResourceKey<Block> key = RegistrationContext.currentBlock();
    if (key != null && this.id == null) {
      cir.setReturnValue(this.drops.get(key));
    }
  }

  @Inject(method = "effectiveDescriptionId", at = @At("HEAD"), cancellable = true)
  private void mantle$contextDescription(CallbackInfoReturnable<String> cir) {
    ResourceKey<Block> key = RegistrationContext.currentBlock();
    if (key != null && this.id == null) {
      cir.setReturnValue(this.descriptionId.get(key));
    }
  }
}
