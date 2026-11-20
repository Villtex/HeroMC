package xreliquary.item;

import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;
import xreliquary.XRFx;

/** ItemWitherlessRose: anywhere in the inventory it removes the wither effect from its holder. */
public class WitherlessRoseItem extends XRItem {
    public WitherlessRoseItem(Properties properties) {
        super(properties, 2);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity owner, @Nullable EquipmentSlot slot) {
        if (owner instanceof Player player && player.hasEffect(MobEffects.WITHER)) {
            player.removeEffect(MobEffects.WITHER);
            for (int i = 0; i < 10; i++) {
                XRFx.particle(level, ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, 0.0F, 0.0F, 1.0F),
                        player.getX() + level.getRandom().nextGaussian() / 6.0, player.getY() + player.getBbHeight() / 2.0F,
                        player.getZ() + level.getRandom().nextGaussian() / 6.0, 0, 0, 0);
            }
        }
    }
}
