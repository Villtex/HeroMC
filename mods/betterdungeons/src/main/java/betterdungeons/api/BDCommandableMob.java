package betterdungeons.api;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * The humans of the mod (EntityHuman, the mercenary EntityKnight) as the controller and the path marker see them: where
 * to stand, whom to attack, which path to walk. The mob port implements it; the items only call it.
 */
public interface BDCommandableMob {
    /** ItemController: human.standingPosition = new PositionHelper(x, y, z, yaw). */
    void setStandingPosition(BlockPos pos, float yaw);

    void setCommandedTarget(LivingEntity target);

    /** ItemPathMarker: human.path (block centres at the y of the clicked block). */
    void setPath(List<Vec3> path);

    /** EntityKnight: the controller assigns it on left click and the path marker gives it paths. */
    boolean isMercenary();
}
