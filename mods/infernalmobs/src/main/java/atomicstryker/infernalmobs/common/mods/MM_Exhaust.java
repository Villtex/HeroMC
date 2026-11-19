package atomicstryker.infernalmobs.common.mods;

import atomicstryker.infernalmobs.common.MobModifier;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

/**
 * MM_Exhaust of Infernal Mobs 1.3.1, ported 1:1.
 */
public class MM_Exhaust extends MobModifier {
    private static final String[] suffix = {" of Fatigue", " the Drainer"};
    private static final String[] prefix = {" exhausting ", " draining "};

    public MM_Exhaust(LivingEntity mob, MobModifier prevMod) {
        this.modName = "Exhaust";
        this.nextMod = prevMod;
    }

    @Override
    public float onHurt(LivingEntity mob, DamageSource source, float damage) {
        if (source.getEntity() instanceof Player player) {
            player.causeFoodExhaustion(1.0F);
        }
        return super.onHurt(mob, source, damage);
    }

    @Override
    public float onAttack(LivingEntity entity, DamageSource source, float damage) {
        if (entity instanceof Player player) {
            player.causeFoodExhaustion(1.0F);
        }
        return super.onAttack(entity, source, damage);
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
