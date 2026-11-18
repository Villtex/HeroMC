package betterdungeons.mob.ai;

import betterdungeons.api.BDEntityClass;
import betterdungeons.mob.BDBoss;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;

import java.util.Set;

/**
 * AITargetNearestRival: the nearest human ({@link BDEntityClass#isHuman()}) whose kind (before a dot) is one of the
 * given ones. With sight checking it also has to be within 90 degrees of where the mob's head looks, and outside 40
 * degrees a sneaking one is not seen.
 */
public class RivalTargetGoal extends NearestAttackableTargetGoal<LivingEntity> {
    public RivalTargetGoal(Mob mob, boolean checkSight, String... kinds) {
        super(mob, LivingEntity.class, 0, checkSight, false, (target, level) -> suitable(mob, target, checkSight, Set.of(kinds)));
    }

    private static boolean suitable(Mob owner, LivingEntity entity, boolean checkSight, Set<String> kinds) {
        boolean flag = false;
        if (entity instanceof BDEntityClass c && c.isHuman()) {
            String enemyType = c.getEntityClass();
            if (enemyType == null || enemyType.isEmpty()) {
                return false;
            }
            if (enemyType.contains(".")) {
                enemyType = enemyType.substring(0, enemyType.indexOf('.')).trim();
            }
            flag = kinds.contains(enemyType);
        }
        if (checkSight || entity.isPassenger()) {
            double rotDiff = Math.abs(BDBoss.angleBetween(owner, entity));
            double rot = Math.abs(rotDiff - Math.abs(BDBoss.wrap(owner.getYHeadRot())));
            if (rot > 40.0) {
                if (entity.isShiftKeyDown()) {
                    return false;
                }
                if (rot > 90.0) {
                    return false;
                }
            }
        }
        return flag;
    }
}
