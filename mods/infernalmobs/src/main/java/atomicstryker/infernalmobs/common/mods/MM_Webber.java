package atomicstryker.infernalmobs.common.mods;

import atomicstryker.infernalmobs.common.MobModifier;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;

/**
 * MM_Webber of Infernal Mobs 1.3.1, ported 1:1.
 */
public class MM_Webber extends MobModifier {
    private long lastAbilityUse = 0L;
    private static final long coolDown = 15000L;
    private static final Class<?>[] modBans = {MM_Gravity.class, MM_Blastoff.class};
    private static final String[] suffix = {" of Traps", " the Mutated", " the Spider"};
    private static final String[] prefix = {" ensnaring ", " webbing "};

    public MM_Webber(LivingEntity mob, MobModifier prevMod) {
        this.modName = "Webber";
        this.nextMod = prevMod;
    }

    @Override
    public boolean onUpdate(LivingEntity mob) {
        if (this.getMobTarget() instanceof Player) {
            this.tryAbility(mob, this.getMobTarget());
        }
        return super.onUpdate(mob);
    }

    @Override
    public float onHurt(LivingEntity mob, DamageSource source, float damage) {
        if (source.getEntity() instanceof LivingEntity attacker) {
            this.tryAbility(mob, attacker);
        }
        return super.onHurt(mob, source, damage);
    }

    private void tryAbility(LivingEntity mob, LivingEntity target) {
        if (target != null && mob.hasLineOfSight(target)) {
            BlockPos pos = BlockPos.containing(target.getX(), target.getY(), target.getZ());
            long time = System.currentTimeMillis();
            if (time > this.lastAbilityUse + coolDown) {
                int offset;
                if (target.level().getBlockState(pos.below()).isAir()) {
                    offset = -1;
                } else {
                    if (!target.level().getBlockState(pos).isAir()) {
                        return;
                    }
                    offset = 0;
                }
                this.lastAbilityUse = time;
                target.level().setBlock(pos.above(offset), Blocks.COBWEB.defaultBlockState(), 3);
                playSound(mob, SoundEvents.SPIDER_AMBIENT, 1.0F, randomPitch(mob));
            }
        }
    }

    @Override
    public Class<?>[] getModsNotToMixWith() {
        return modBans;
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
