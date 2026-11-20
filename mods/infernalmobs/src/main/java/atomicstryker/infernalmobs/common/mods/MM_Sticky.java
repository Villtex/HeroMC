package atomicstryker.infernalmobs.common.mods;

import atomicstryker.infernalmobs.common.MobModifier;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.util.Prediction;

/**
 * MM_Sticky of Infernal Mobs 1.3.1, ported 1:1.
 */
public class MM_Sticky extends MobModifier {
    private long nextAbilityUse = 0L;
    private static final long coolDown = 15000L;
    private final Class<?>[] disallowed = {Creeper.class};
    private static final String[] suffix = {" of Snagging", " the Quick Fingered", " of Petty Theft", " .. YOINK"};
    private static final String[] prefix = {" thieving ", " snagging ", " quick fingered "};

    public MM_Sticky(LivingEntity mob, MobModifier prevMod) {
        this.modName = "Sticky";
        this.nextMod = prevMod;
    }

    @Override
    public float onHurt(LivingEntity mob, DamageSource source, float damage) {
        if (source.getEntity() instanceof Player p) {
            ItemStack weapon = p.getInventory().getItem(p.getInventory().getSelectedSlot());
            if (!weapon.isEmpty()) {
                long time = System.currentTimeMillis();
                if (time > this.nextAbilityUse) {
                    this.nextAbilityUse = time + coolDown;
                    ItemEntity drop = p.drop(p.getInventory().removeItem(p.getInventory().getSelectedSlot(), 1), false, Prediction.SERVER_ONLY);
                    if (drop != null) {
                        drop.setPickUpDelay(50);
                        playSound(mob, SoundEvents.SLIME_ATTACK, 1.0F, randomPitch(p));
                    }
                }
            }
        }
        return super.onHurt(mob, source, damage);
    }

    @Override
    public Class<?>[] getBlackListMobClasses() {
        return this.disallowed;
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
