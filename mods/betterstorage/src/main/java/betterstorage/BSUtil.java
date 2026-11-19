package betterstorage;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** WorldUtils, DirectionUtils and RandomUtils of the original, as far as the port needs them. */
public final class BSUtil {
    /** RandomUtils.random: one unseeded random for everything that was not seed-bound in the original either. */
    public static final RandomSource RANDOM = RandomSource.create();

    private BSUtil() {
    }

    /** RandomUtils.getInt(min, max): min..max-1. */
    public static int getInt(int min, int max) {
        return max > min ? min + RANDOM.nextInt(max - min) : min;
    }

    public static boolean getBoolean(double probability) {
        return RANDOM.nextDouble() < probability;
    }

    /** WorldUtils.dropStackFromBlock: somewhere inside the block, with a little gaussian motion upwards. */
    public static void dropFromBlock(Level level, BlockPos pos, ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }
        double x = pos.getX() + 0.1 + RANDOM.nextFloat() * 0.8F;
        double y = pos.getY() + 0.1 + RANDOM.nextFloat() * 0.8F;
        double z = pos.getZ() + 0.1 + RANDOM.nextFloat() * 0.8F;
        ItemEntity item = new ItemEntity(level, x, y, z, stack);
        item.setDeltaMovement(RANDOM.nextGaussian() * 0.05F, RANDOM.nextGaussian() * 0.05F + 0.2F, RANDOM.nextGaussian() * 0.05F);
        level.addFreshEntity(item);
    }

    /**
     * WorldUtils.dropStackFromEntity: players throw it like a death drop (dropPlayerItemWithRandomChoice, motion scaled
     * by speed / 4), other entities from their eyes in a random direction, not to be picked up for 40 ticks.
     */
    public static void dropFromEntity(Entity entity, ItemStack stack, float speed) {
        if (stack.isEmpty()) {
            return;
        }
        if (entity instanceof Player player) {
            ItemEntity item = player.drop(stack, true, false);
            if (item != null) {
                Vec3 motion = item.getDeltaMovement();
                item.setDeltaMovement(motion.x * speed / 4.0F, motion.y, motion.z * speed / 4.0F);
            }
            return;
        }
        ItemEntity item = new ItemEntity(entity.level(), entity.getX(), entity.getY() + entity.getEyeHeight() - 0.3, entity.getZ(), stack);
        item.setPickUpDelay(40);
        float f1 = RANDOM.nextFloat() * 0.5F;
        float f2 = RANDOM.nextFloat() * (float) (Math.PI * 2);
        item.setDeltaMovement(-Mth.sin(f2) * f1, 0.2, Mth.cos(f2) * f1);
        entity.level().addFreshEntity(item);
    }

    /** DirectionUtils.getRotation: south 0, west 90, north 180, east 270. */
    public static int rotation(Direction dir) {
        return switch (dir) {
            case WEST -> 90;
            case NORTH -> 180;
            case EAST -> 270;
            default -> 0;
        };
    }

    /** DirectionUtils.angleDifference. */
    public static double angleDifference(double angle1, double angle2) {
        return ((angle2 - angle1) % 360.0 + 540.0) % 360.0 - 180.0;
    }

    /** DirectionUtils.angleBetween(e1, e2): the angle of the line from e1 to e2 in the x/z plane, in degrees. */
    public static double angleBetween(Entity e1, Entity e2) {
        return Math.toDegrees(Math.atan2(e2.getZ() - e1.getZ(), e2.getX() - e1.getX()));
    }
}
