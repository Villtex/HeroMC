package atomicstryker.infernalmobs.common.mods;

import atomicstryker.infernalmobs.common.MobModifier;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.player.Player;

/**
 * MM_Storm of Infernal Mobs 1.3.1, ported 1:1.
 */
public class MM_Storm extends MobModifier {
    private long nextAbilityUse = 0L;
    private static final long coolDown = 15000L;
    private static final float MIN_DISTANCE = 3.0F;
    private static final String[] suffix = {" of Lightning", " the Raiden"};
    private static final String[] prefix = {" striking ", " thundering ", " electrified "};

    public MM_Storm(LivingEntity mob, MobModifier prevMod) {
        this.modName = "Storm";
        this.nextMod = prevMod;
    }

    @Override
    public boolean onUpdate(LivingEntity mob) {
        if (this.getMobTarget() instanceof Player) {
            this.tryAbility(mob, this.getMobTarget());
        }
        return super.onUpdate(mob);
    }

    private void tryAbility(LivingEntity mob, LivingEntity target) {
        if (target != null && mob.hasLineOfSight(target)) {
            long time = System.currentTimeMillis();
            if (time > this.nextAbilityUse && mob.distanceTo(target) > MIN_DISTANCE && target.level().canSeeSky(BlockPos.containing(target.getX(), target.getY(), target.getZ()))) {
                this.nextAbilityUse = time + coolDown;
                LightningBolt bolt = EntityTypes.LIGHTNING_BOLT.create(mob.level(), EntitySpawnReason.TRIGGERED);
                if (bolt != null) {
                    bolt.snapTo(target.getX(), target.getY() - 1.0, target.getZ());
                    mob.level().addFreshEntity(bolt);
                }
            }
        }
    }

    @Override
    protected String[] getModNameSuffix() {
        return suffix;
    }

    @Override
    protected String[] getModNamePrefix() {
        return prefix;
    }
}
