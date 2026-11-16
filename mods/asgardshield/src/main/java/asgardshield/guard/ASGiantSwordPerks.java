package asgardshield.guard;

import asgardshield.item.ItemZedSword;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** ASGiantSwordPerks: a giant sword guards like a shield, with the enchantments but without perks. */
public final class ASGiantSwordPerks {
    private ASGiantSwordPerks() {
    }

    public static int[] doASSwordCalc(ItemStack stack, ItemZedSword sword, ServerLevel level, Player player, DamageSource source, int damageOutput) {
        int damageOut = damageOutput / 2;
        int playerDamageTaken = (int) (damageOut * sword.damageMultiplier() * -1.0F);
        int swordDamageTaken = damageOut + playerDamageTaken * -1;
        return getASSwordPerk(stack, sword, level, player, source, swordDamageTaken);
    }

    public static int[] getASSwordPerk(ItemStack stack, ItemZedSword sword, ServerLevel level, Player player, DamageSource source, int swordDamage) {
        int[] swordData = {swordDamage, 0};
        RandomSource rand = player.getRandom();
        rand.nextInt(100); // perkChance of 1.5.2, unused
        swordData[0] = ASShieldPerks.applyEnchants(stack, level, player, source, swordDamage);
        swordData[0] *= ASShieldPerks.addedExplosionDamage(source, true);
        ASShieldPerks.harkenAugments(stack, level, player, swordData, rand);
        if (swordData[1] == 0) {
            ASShieldPerks.playGuardSound(player, sword.guardSound());
        }
        return swordData;
    }
}
