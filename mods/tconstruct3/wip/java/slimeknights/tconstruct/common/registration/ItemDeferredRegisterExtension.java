package slimeknights.tconstruct.common.registration;

import net.minecraft.world.item.Item;
import slimeknights.mantle.registration.deferred.ItemDeferredRegister;
import slimeknights.mantle.registration.object.ItemObject;
import slimeknights.tconstruct.library.tools.part.IMaterialItem;

import java.util.function.Supplier;

public class ItemDeferredRegisterExtension extends ItemDeferredRegister {
  public ItemDeferredRegisterExtension(String modID) {
    super(modID);
  }

  // TODO 27i: registerCast(...) for CastItemObject/PartCastItem (deferred with the casts)
}
