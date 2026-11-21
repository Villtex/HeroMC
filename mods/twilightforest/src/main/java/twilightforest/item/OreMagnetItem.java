package twilightforest.item;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import twilightforest.block.OreMagnet;

/**
 * ItemTFOreMagnet (12 uses): held down for more than 10 ticks and released it pulls the first ore vein on the look line
 * of 32 blocks to the first stone; if nothing moves the line is tried 10° off in the eight directions. One use per block
 * moved, enderman teleport sound.
 */
public class OreMagnetItem extends Item {
    private static final float[][] WIGGLE = {{0, 0}, {10, 0}, {10, 10}, {0, 10}, {-10, 10}, {-10, 0}, {-10, -10}, {0, -10}, {10, -10}};

    public OreMagnetItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        player.startUsingItem(hand);
        return InteractionResult.CONSUME;
    }

    @Override
    public boolean releaseUsing(ItemStack stack, Level level, LivingEntity entity, int remaining) {
        int useTime = this.getUseDuration(stack, entity) - remaining;
        if (level instanceof ServerLevel server && useTime > 10) {
            int moved = 0;
            for (float[] w : WIGGLE) {
                moved = doMagnet(server, entity, w[0], w[1]);
                if (moved != 0) {
                    break;
                }
            }
            if (moved > 0) {
                TFItemUtil.damage(stack, moved, entity);
                server.playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.ENDERMAN_TELEPORT, entity.getSoundSource(), 1.0F, 1.0F);
            }
        }
        return true;
    }

    private static int doMagnet(ServerLevel level, LivingEntity player, float yawOffset, float pitchOffset) {
        double range = 32.0;
        Vec3 src = player.getEyePosition();
        Vec3 look = offsetLook(player, yawOffset, pitchOffset);
        Vec3 dest = src.add(look.scale(range));
        return OreMagnet.doMagnet(level, Mth.floor(src.x), Mth.floor(src.y), Mth.floor(src.z), Mth.floor(dest.x), Mth.floor(dest.y), Mth.floor(dest.z));
    }

    private static Vec3 offsetLook(LivingEntity player, float yawOffset, float pitchOffset) {
        float c = Mth.cos(-(player.getYRot() + yawOffset) * ((float) Math.PI / 180.0F) - (float) Math.PI);
        float s = Mth.sin(-(player.getYRot() + yawOffset) * ((float) Math.PI / 180.0F) - (float) Math.PI);
        float cp = -Mth.cos(-(player.getXRot() + pitchOffset) * ((float) Math.PI / 180.0F));
        float sp = Mth.sin(-(player.getXRot() + pitchOffset) * ((float) Math.PI / 180.0F));
        return new Vec3(s * cp, sp, c * cp);
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.BOW;
    }
}
