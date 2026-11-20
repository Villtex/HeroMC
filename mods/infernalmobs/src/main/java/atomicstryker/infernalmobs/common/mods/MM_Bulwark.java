package atomicstryker.infernalmobs.common.mods;

import atomicstryker.infernalmobs.common.MobModifier;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

/**
 * MM_Bulwark of Infernal Mobs 1.3.1, ported 1:1.
 * The integer halving of 1.5.2 is kept ({@code floor(damage / 2)}, at least 1).
 */
public class MM_Bulwark extends MobModifier {
    private static final String[] suffix = {" of Turtling", " the Defender", " of effing Armor"};
    private static final String[] prefix = {" turtling ", " defensive ", " armoured "};

    public MM_Bulwark(LivingEntity mob, MobModifier prevMod) {
        this.modName = "Bulwark";
        this.nextMod = prevMod;
    }

    @Override
    public float onHurt(LivingEntity mob, DamageSource source, float damage) {
        return super.onHurt(mob, source, Math.max((float) Math.floor(damage / 2), 1.0F));
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
