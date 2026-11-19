package chococraft.item;

import chococraft.entity.PurpleChocoboEgg;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** ItemPurpleChocoboEgg: thrown like an egg (velocity 1.5, inaccuracy 1), with the bow sound. */
public class PurpleChocoboEggItem extends Item {
    public PurpleChocoboEggItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 0.5F,
                0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F));
        if (level instanceof ServerLevel server) {
            Projectile.spawnProjectileFromRotation(PurpleChocoboEgg::new, server, stack, player, 0.0F, 1.5F, 1.0F);
        }
        stack.consume(1, player);
        return InteractionResult.SUCCESS;
    }
}
