package harkenscythe.item;

import harkenscythe.entity.LostSoulEntity;
import harkenscythe.entity.SoulEntity;
import harkenscythe.entity.SpectralPets;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

/**
 * ItemHSBook with specialNumber 2: with two blood in a Bloodkeeper or Blood Vessel, holding right-click reads the
 * book; every reading (20 ticks) turns two pages (the damage bar). After the fifteenth reading the ritual raises every
 * soul within sqrt(5) blocks as a spectral pet (Teil 9, part 3: {@link SpectralPets}) for two blood each; letting go
 * before ends the ritual. A failed ritual sounds like a zombie infecting a villager.
 */
public class NecronomiconItem extends BookItem {
    private static final int BLOOD_COST = 2;

    public NecronomiconItem(Properties properties) {
        super(2, properties);
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity user) {
        return 20;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.BLOCK;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!Essence.has(player, BLOOD_COST, true)) {
            return InteractionResult.PASS;
        }
        int progress = stack.getDamageValue();
        player.startUsingItem(hand);
        if (progress == 4) {
            level.playSound(player, player.getX(), player.getY(), player.getZ(), SoundEvents.ENDERMAN_STARE, SoundSource.PLAYERS, 5.0F, 1.0F);
        }
        if (progress % 4 == 2 || progress == 30) {
            Ritual.text(level, player);
        }
        if (progress % 4 == 0 && progress >= 4 || progress == 30) {
            Ritual.text2(level, player);
        }
        if (!level.isClientSide()) {
            stack.setDamageValue(Math.min(progress + 2, stack.getMaxDamage() - 1));
            if (progress >= 30) {
                this.finish(stack, level, player, progress);
            }
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public boolean releaseUsing(ItemStack stack, Level level, LivingEntity entity, int remainingTime) {
        if (entity instanceof Player player && !level.isClientSide()) {
            this.finish(stack, level, player, remainingTime);
        }
        return true;
    }

    /** onPlayerStoppedUsing. */
    private void finish(ItemStack stack, Level level, Player player, int progress) {
        int raised = 0;
        if (progress >= 29 && level instanceof ServerLevel server) {
            for (Entity entity : level.getEntities(player, new AABB(player.blockPosition()).inflate(4.0))) {
                if (player.distanceToSqr(entity) >= 5.0 || !Essence.has(player, BLOOD_COST, true)) {
                    continue;
                }
                boolean done = false;
                if (entity instanceof SoulEntity soul) {
                    done = SpectralPets.create(soul.getFallenSoulName(), server, entity.getX(), entity.getY(), entity.getZ(), level.getRandom(), false);
                } else if (entity instanceof LostSoulEntity) {
                    done = SpectralPets.createMiner(server, entity.getX(), entity.getY(), entity.getZ(), level.getRandom());
                }
                if (done) {
                    Essence.consume(player, BLOOD_COST, true);
                    raised++;
                    level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ZOMBIE_VILLAGER_CURE, SoundSource.PLAYERS, 1.0F, 1.0F);
                    entity.discard();
                }
            }
        }
        stack.setDamageValue(0);
        if (raised == 0) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ZOMBIE_INFECT, SoundSource.PLAYERS, 1.0F, 1.0F);
        }
    }
}
