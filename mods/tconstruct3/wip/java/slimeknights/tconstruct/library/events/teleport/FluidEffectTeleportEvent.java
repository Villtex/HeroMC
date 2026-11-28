package slimeknights.tconstruct.library.events.teleport;

import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.event.entity.EntityTeleportEvent;
import slimeknights.tconstruct.library.utils.TeleportHelper.ITeleportEventFactory;

/** Event fired when an entity teleports via the fluid effect */
public class FluidEffectTeleportEvent extends EntityTeleportEvent {
  public static final ITeleportEventFactory TELEPORT_FACTORY = FluidEffectTeleportEvent::new;

  public FluidEffectTeleportEvent(Entity entity, double targetX, double targetY, double targetZ) {
    super(entity, (net.minecraft.server.level.ServerLevel) entity.level(), targetX, targetY, targetZ);
  }
}
