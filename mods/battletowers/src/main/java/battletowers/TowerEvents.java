package battletowers;

import battletowers.entity.BattleTowerGolem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import java.util.List;

/**
 * The chest hack of the original's ClientTickHandler, on the server: opening a chest with a golem within 8 blocks of
 * the player wakes the golem, sets the player as its target and closes the chest again (it stays shut while a golem
 * is near); starting to break a chest with a golem within 7 blocks of the chest wakes it too.
 */
final class TowerEvents {
    private TowerEvents() {
    }

    static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        if (player.level().isClientSide() || !(player.level().getBlockState(event.getPos()).getBlock() instanceof ChestBlock)) {
            return;
        }
        List<BattleTowerGolem> golems = player.level().getEntitiesOfClass(BattleTowerGolem.class, player.getBoundingBox().inflate(8.0));
        if (!golems.isEmpty()) {
            golems.getLast().wakeFor(player);
            event.setCanceled(true);
        }
    }

    static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        Player player = event.getEntity();
        if (player.level().isClientSide() || event.getAction() != PlayerInteractEvent.LeftClickBlock.Action.START
                || !(player.level().getBlockState(event.getPos()).getBlock() instanceof ChestBlock)) {
            return;
        }
        List<BattleTowerGolem> golems = player.level().getEntitiesOfClass(BattleTowerGolem.class, new AABB(event.getPos()).inflate(7.0));
        if (!golems.isEmpty()) {
            golems.getLast().wakeFor(player);
        }
    }
}
