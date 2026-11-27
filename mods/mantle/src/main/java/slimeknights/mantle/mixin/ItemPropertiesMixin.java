package slimeknights.mantle.mixin;

import net.minecraft.resources.DependantName;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import slimeknights.mantle.registration.RegistrationContext;

/**
 * Port: item properties without {@code setId} take the key of the item Mantle's registers are creating, see
 * {@link RegistrationContext}. Items of blocks get the block description ID as in 1.20.1.
 */
@Mixin(Item.Properties.class)
public abstract class ItemPropertiesMixin {
  @Shadow private ResourceKey<Item> id;
  @Shadow private DependantName<Item, String> descriptionId;
  @Shadow @Final private DependantName<Item, Identifier> model;
  @Shadow @Final private static DependantName<Item, String> ITEM_DESCRIPTION_ID;
  @Shadow @Final private static DependantName<Item, String> BLOCK_DESCRIPTION_ID;

  @Inject(method = "itemIdOrThrow", at = @At("HEAD"), cancellable = true)
  private void mantle$contextId(CallbackInfoReturnable<ResourceKey<Item>> cir) {
    ResourceKey<Item> key = RegistrationContext.currentItem();
    if (key != null && this.id == null) {
      cir.setReturnValue(key);
    }
  }

  @Inject(method = "effectiveDescriptionId", at = @At("HEAD"), cancellable = true)
  private void mantle$contextDescription(CallbackInfoReturnable<String> cir) {
    ResourceKey<Item> key = RegistrationContext.currentItem();
    if (key != null && this.id == null) {
      DependantName<Item, String> name = this.descriptionId == ITEM_DESCRIPTION_ID && RegistrationContext.isBlockItem() ? BLOCK_DESCRIPTION_ID : this.descriptionId;
      cir.setReturnValue(name.get(key));
    }
  }

  @Inject(method = "effectiveModel", at = @At("HEAD"), cancellable = true)
  private void mantle$contextModel(CallbackInfoReturnable<Identifier> cir) {
    ResourceKey<Item> key = RegistrationContext.currentItem();
    if (key != null && this.id == null) {
      cir.setReturnValue(this.model.get(key));
    }
  }
}
