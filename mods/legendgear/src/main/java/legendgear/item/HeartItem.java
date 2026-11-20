package legendgear.item;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

/** HeartPickup: picked up it heals a heart (ForgeEventHooksHandler.handleCollect); used, it only shows hearts. */
public class HeartItem extends Item {
    public HeartItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide()) {
            RandomSource rand = level.getRandom();
            for (int i = 0; i < 7; i++) {
                level.addParticle(ParticleTypes.HEART, player.getX() + rand.nextGaussian(), player.getY() + rand.nextGaussian(),
                        player.getZ() + rand.nextGaussian(), rand.nextGaussian() * 0.02, rand.nextGaussian() * 0.02, rand.nextGaussian() * 0.02);
            }
        }
        return InteractionResult.PASS;
    }
}
